package org.dayup.inotes.sync.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.dayup.common.Log;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.constants.Constants.SyncMode;
import org.dayup.inotes.data.Folder;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.db.Field.Status;
import org.dayup.inotes.diff.Diff_match_patch;
import org.dayup.inotes.diff.Diff_match_patch.Patch;
import org.dayup.inotes.sync.ImapConsts;
import org.dayup.inotes.sync.MessageCompose;
import org.dayup.inotes.sync.client.ImapStoreClient;
import org.dayup.inotes.sync.exception.AuthenticationErrorException;
import org.dayup.inotes.utils.StringUtils;

import android.text.TextUtils;

import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.AuthenticationFailedException;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.MimeMessage;

/**
 * @author Nicky
 * 
 */
public class SyncEmailManager extends SyncManager {

    private final String TAG = SyncEmailManager.class.getSimpleName();

    private ImapStoreClient imapStoreClient;
    private String loginUser;

    public SyncEmailManager() {

    }

    @Override
    public void instance(INotesApplication application) {
        super.instance(application);
        imapStoreClient = new ImapStoreClient();
        try {
            if (!application.getAccountManager().isLocalMode()) {
                imapStoreClient.init(application.getAccountManager().getAccount(), application);
                loginUser = application.getAccountManager().getEmail();
            }
        } catch (AuthenticationFailedException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationErrorException(e.getLocalizedMessage());
        }
    }

    @Override
    public Boolean doSyncINotes(final long accountId, int syncMode) {
        Log.d(TAG, "doSyncGnotes");
        boolean result = true;
        try {
            switch (syncMode) {
            case SyncMode.LOCAL_CHANGED:
                doFetchRemoteMessagesLite(accountId);
                doAddNotes(accountId);
                doDeleteNotes(accountId);
                doUpdateNotes(accountId);
                break;
            case SyncMode.ALL:
            default:
                doFetchRemoteMessages(accountId);
                doAddNotes(accountId);
                doDeleteNotes(accountId);
                doUpdateNotes(accountId);
                break;
            }
        } catch (AuthenticationFailedException e) {
            throw new AuthenticationErrorException("");
        } catch (MessagingException e) {
            throw new NetworkException("");
        }
        Log.d(TAG, "Finish sync");
        return result;
    }

    private void doFetchRemoteMessagesLite(long accountId) throws AuthenticationFailedException {
        List<Folder> localFolders = Folder.getAllFoldersChangedByAccountId(accountId,
                application.getDBHelper());
        for (Folder folder : localFolders) {
            handleServerFolderChange(folder);
        }
    }

    private void doFetchRemoteMessages(long accountId) throws AuthenticationFailedException {

        imapStoreClient.fetchNewFolder(accountId);

        List<Folder> localFolders = Folder.getAllFoldersByAccountId(accountId, null, null, null,
                application.getDBHelper());

        for (Folder folder : localFolders) {
            handleServerFolderChange(folder);
        }
        Log.i(TAG, "doFetchRemoteMessages() end");
    }

    private void handleServerFolderChange(Folder folder) throws AuthenticationFailedException {
        long accountId = folder.accountId;
        List<Message> m = new ArrayList<Message>();
        List<String> remoteSIds = new ArrayList<String>();
        List<String> localSids = Note.getAllSyncedNoteSidByFolderId(folder.id, accountId,
                application.getDBHelper());
        try {
            Message[] messages = imapStoreClient.getMessages(folder.name);
            for (Message msg : messages) {
                remoteSIds.add(msg.getUid());
                if (!localSids.contains(msg.getUid()))
                    m.add(msg);
            }
            if (m.size() > 0) {
                fetMessages(m, folder);
            }
        } catch (AuthenticationFailedException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getLocalizedMessage());
        } catch (MessagingException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getLocalizedMessage());
        }

        handleRemoteDeletedNotes(accountId, folder.id, remoteSIds);
    }

    private void handleRemoteDeletedNotes(long accountId, long folderId, List<String> remoteSIds) {
        List<Note> localNotes = Note.getAllSyncedNoteByFolderId(folderId, accountId,
                application.getDBHelper());
        for (Note localNote : localNotes) {
            if (!remoteSIds.contains(localNote.sid)) {
                Note.deleteNoteByIdForever(localNote.id, accountId, application.getDBHelper());
            }
        }
    }

    private void fetMessages(List<Message> pendingMessages, Folder folder)
            throws MessagingException {
        Message[] messages = imapStoreClient.fetchMessages(
                pendingMessages.toArray(new Message[pendingMessages.size()]), folder.name,
                folder.accountId);
        if (messages != null && messages.length > 0) {
            refreshSyncingViews();
        }
    }

    private void doAddNotes(long accountId) throws MessagingException {
        Log.i(TAG, "doAddNotes() begin");
        MimeMessage msg;
        List<MimeMessage> pendingComposeMessages = new ArrayList<MimeMessage>();
        HashMap<Long, List<Note>> folderIdNotesMap = buildLocalChangedFolder2NotesMap(Note
                .getLocalAddedNotes(accountId, application.getDBHelper()));
        if (folderIdNotesMap.size() == 0) {
            Log.i(TAG, "doAddNotes() end ..no added notes");
            return;
        }
        try {

            for (long folderId : folderIdNotesMap.keySet()) {
                for (Note note : folderIdNotesMap.get(folderId)) {
                    msg = MessageCompose.messageFromNotes(note, application);
                    if (loginUser != null) {
                        msg.setFrom(new Address(loginUser));
                    }
                    pendingComposeMessages.add(msg);
                }
                if (pendingComposeMessages.size() > 0) {
                    if (folderId == Folder.ALL_FOLDER_ID) {
                        imapStoreClient.createNotes(pendingComposeMessages,
                                ImapConsts.IMAP__INOTES_FOLDER);
                    } else {
                        Folder folder = Folder.getFolderById(folderId, accountId,
                                application.getDBHelper());
                        imapStoreClient.createNotes(pendingComposeMessages,
                                folder == null ? ImapConsts.IMAP__INOTES_FOLDER : folder.name);
                    }
                    pendingComposeMessages.clear();
                }

            }

            handleRemoteUidNullErro(accountId);

        } catch (MessagingException e) {
            Log.e(TAG, " ", e);
            throw new MessagingException(e.getLocalizedMessage());
        }
        Log.i(TAG, "doAddNotes() end");
    }

    /**
     * 对方邮箱可能没有实现新增返回uID，为避免出现重复提� ��需要，需要对本地未处理的added notes和remote进比对
     * 
     * @param accountId
     * @throws AuthenticationFailedException
     */
    private void handleRemoteUidNullErro(long accountId) throws AuthenticationFailedException {
        if (Note.getLocalAddedNotes(accountId, application.getDBHelper()).size() > 0) {
            doFetchRemoteMessages(accountId);
        }
    }

    private void doDeleteNotes(long accountId) throws MessagingException {
        Log.i(TAG, "doDeleteNotes() begin");
        MimeMessage msg;
        List<MimeMessage> pendingDeleteMessages = new ArrayList<MimeMessage>();
        HashMap<Long, List<Note>> folderId2NotesMap = buildLocalChangedFolder2NotesMap(Note
                .getLocalDeletedNotes(accountId, application.getDBHelper()));

        if (folderId2NotesMap.size() == 0) {
            Log.i(TAG, "doDeleteNotes() end... no deleted notes");
            return;
        }
        try {
            for (long folderId : folderId2NotesMap.keySet()) {
                for (Note note : folderId2NotesMap.get(folderId)) {
                    msg = MessageCompose.messageFromDeletedNotes(note);
                    if (loginUser != null) {
                        msg.setFrom(new Address(loginUser));
                    }
                    msg.getMessageId();
                    pendingDeleteMessages.add(msg);
                }
                Message[] delMessages = (Message[]) pendingDeleteMessages
                        .toArray(new Message[pendingDeleteMessages.size()]);
                if (delMessages.length > 0) {
                    if (folderId == Folder.ALL_FOLDER_ID) {
                        imapStoreClient.deleteNote(delMessages, ImapConsts.IMAP__INOTES_FOLDER);
                    } else {
                        Folder folder = Folder.getFolderById(folderId, accountId,
                                application.getDBHelper());
                        imapStoreClient.deleteNote(delMessages, folder.name);
                    }
                    pendingDeleteMessages.clear();
                }
                // TODO 感觉可能有点危险,确保删除都是成功的
                for (Note note : folderId2NotesMap.get(folderId)) {
                    Note.deleteNoteByIdForever(note.id, accountId, application.getDBHelper());
                }

            }
        } catch (MessagingException e) {
            Log.e(TAG, " ", e);
            throw new MessagingException(e.getLocalizedMessage());
        }
        Log.i(TAG, "doDeleteNotes() end");
    }

    private void doUpdateNotes(long accountId) throws MessagingException {
        Log.i(TAG, "doUpdateNotes() begin");
        MimeMessage msg;
        List<MimeMessage> pendingUpdateMessages = new ArrayList<MimeMessage>();

        HashMap<Long, List<Note>> folderId2NotesMap = buildLocalChangedFolder2NotesMap(Note
                .getLocalUpdatedNotes(accountId, application.getDBHelper()));
        if (folderId2NotesMap.size() == 0) {
            Log.i(TAG, "doUpdateNotes() end... no updated notes");
            return;
        }
        try {
            for (long folderId : folderId2NotesMap.keySet()) {

                for (Note note : folderId2NotesMap.get(folderId)) {
                    msg = MessageCompose.messageFromNotes(note, application);
                    if (loginUser != null) {
                        msg.setFrom(new Address(loginUser));
                    }
                    msg.getMessageId();
                    pendingUpdateMessages.add(msg);
                }
                if (pendingUpdateMessages.size() > 0) {
                    if (folderId == Folder.ALL_FOLDER_ID) {
                        imapStoreClient.updateNotes(pendingUpdateMessages,
                                ImapConsts.IMAP__INOTES_FOLDER);
                    } else {
                        Folder folder = Folder.getFolderById(folderId, accountId,
                                application.getDBHelper());
                        imapStoreClient.updateNotes(pendingUpdateMessages, folder.name);
                    }
                    pendingUpdateMessages.clear();
                }

            }

        } catch (MessagingException e) {
            Log.e(TAG, " ", e);
            throw new MessagingException(e.getLocalizedMessage());
        }
        Log.i(TAG, "doUpdateNotes() end");

    }

    public void updateNotesStatus(String uid, String messageId, int status) {
        Note note = Note.getNoteByIdNotDel(messageId, application.getDBHelper());
        if (note != null) {
            note.sid = uid;
            Note.updateNotesStatus(note, note.accountId, application.getDBHelper(), status, false);
        }
    }

    public void notifyMessageFetched(Message message, String folderName, long accountId)
            throws MessagingException {
        Folder folder = Folder.getFolderByName(accountId, folderName, application.getDBHelper());
        long folderId;
        if (folder == null) {
            Folder notesFolder = Folder.getFolderByName(accountId, ImapConsts.IMAP__INOTES_FOLDER,
                    application.getDBHelper());
            folderId = notesFolder == null ? Folder.ALL_FOLDER_ID : notesFolder.id;
        } else {
            folderId = folder.id;
        }

        Note note = MessageCompose.notesFromMessage(message);
        note.accountId = accountId;
        note.folderId = folderId;

        Note noteLocal = getRelativeNoteLocal(note);

        if (noteLocal == null) {
            Log.i(TAG, "add a remote note to local:" + note.sid);
            note.status = Status.SYNC_DONE;
            Note.createNote(note, application.getDBHelper());
            if (TextUtils.isEmpty(note.id)) {
                throw new MessagingException("create note failed.." + note.sid);
            }
        } else {
            Log.i(TAG, "update a remote note to local:" + note.sid);
            Boolean result = doDiff(note, noteLocal);
            note.status = result ? Status.SYNC_UPDATE : Status.SYNC_DONE;
            if (!Note.updateNotesWithoutModifyDate(note, accountId, application.getDBHelper())) {
                throw new MessagingException("update note failed.." + note.sid);
            }
        }
    }

    public boolean doDiff(Note noteRemote, Note noteLocal) {
        if (noteLocal.status != Status.SYNC_UPDATE) {
            noteRemote.content_old = noteRemote.content;
            return false;
        }
        if (TextUtils.equals(noteRemote.content, noteLocal.content)) {
            return false;
        }
        Diff_match_patch diff = new Diff_match_patch();
        String old = StringUtils.isEmpty(noteLocal.content_old) ? noteLocal.content
                : noteLocal.content_old;
        LinkedList<Patch> patch = diff.patch_make(old, noteRemote.content);
        Object[] res = diff.patch_apply(patch, noteLocal.content);
        String result = String.valueOf(res[0]);
        noteRemote.content = result;
        noteRemote.content_old = result;
        return true;
    }

    private Note getRelativeNoteLocal(Note remoteNote) {
        if (TextUtils.isEmpty(remoteNote.id)) {
            return null;
        }
        Note localNote = Note.getNoteNotDeletedById(remoteNote.accountId, remoteNote.id,
                application.getDBHelper());
        if (localNote == null) {
            localNote = Note.getDuplicatedNoteForSyncErro(remoteNote, application.getDBHelper());
        }
        return localNote;
    }

    private HashMap<Long, List<Note>> buildLocalChangedFolder2NotesMap(List<Note> notes) {
        HashMap<Long, List<Note>> map = new HashMap<Long, List<Note>>();
        for (Note note : notes) {
            if (map.containsKey(note.folderId)) {
                map.get(note.folderId).add(note);
            } else {
                List<Note> tmp = new ArrayList<Note>();
                tmp.add(note);
                map.put(note.folderId, tmp);
            }
        }
        return map;
    }
}