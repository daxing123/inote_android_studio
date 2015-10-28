package org.dayup.inotes.sync.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.constants.Constants;
import org.dayup.inotes.constants.Constants.AppFiles;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.db.Field.Status;
import org.dayup.inotes.sync.ImapConsts;
import org.dayup.inotes.sync.authorize.AuthAccount;
import org.dayup.inotes.sync.manager.SyncEmailManager;
import org.dayup.inotes.utils.StringUtils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.fsck.k9.controller.AppendMessagesListener;
import com.fsck.k9.controller.MessageRetrievalListener;
import com.fsck.k9.mail.AuthenticationFailedException;
import com.fsck.k9.mail.FetchProfile;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.Folder.FolderType;
import com.fsck.k9.mail.Folder.OpenMode;
import com.fsck.k9.mail.K9;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Part;
import com.fsck.k9.mail.internet.BinaryTempFileBody;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.mail.store.ImapStore;

public class ImapStoreClient {

    private static final String TAG = ImapStoreClient.class.getSimpleName();
    private ImapStore imapStore;
    private Context context;
    private SyncEmailManager syncEmailMan;
    private INotesAccountManager accountManager;

    public void init(Account account, Context context) throws AuthenticationFailedException {
        this.context = context;
        try {
            accountManager = ((INotesApplication) context.getApplicationContext())
                    .getAccountManager();
            AuthAccount authAccount = new AuthAccount(account);
            K9.app = (Application) context;
            imapStore = new ImapStore(authAccount);

            syncEmailMan = (SyncEmailManager) ((INotesApplication) context.getApplicationContext())
                    .getSyncManager();
        } catch (Exception e) {
            throw new AuthenticationFailedException(e.getLocalizedMessage());
        }
    }

    public ImapStore getImapStore() {
        return imapStore;
    }

    public synchronized void checkSettings(Account account, Context context)
            throws MessagingException {
        init(account, context);
        imapStore.checkSettings();
    }

    /**
     * @return
     * @throws AuthenticationFailedException
     */
    public void fetchNewFolder(long accountId) throws AuthenticationFailedException {
        try {
            // delete local folders that have been deleted in mail box
            INotesApplication application = (INotesApplication) context;
            List<org.dayup.inotes.data.Folder> localFolderList = org.dayup.inotes.data.Folder
                    .getAllFoldersByAccountId(accountId, null, null, null,
                            application.getDBHelper());

            @SuppressWarnings("unchecked")
            List<Folder> folderList = (List<Folder>) imapStore.getPersonalNamespaces(true);
            List<String> folderNames = pareseRemoteFolder(folderList);

            notifyFolderFetched(accountId, folderNames);

            // delete remote deleted local folder
            for (org.dayup.inotes.data.Folder localFolder : localFolderList) {
                if (!folderNames.contains(localFolder.name)) {
                    org.dayup.inotes.data.Folder.deleteFolderByIdForever(accountId, localFolder.id,
                            application.getDBHelper());
                }
            }
        } catch (AuthenticationFailedException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getMessage());
        } catch (MessagingException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getMessage());
        }
    }

    private List<String> pareseRemoteFolder(List<Folder> folderList) {
        List<String> folderNames = new ArrayList<String>();
        for (Folder f : folderList) {
            Log.i(TAG, "folder name:  " + f.getName());
            if (f.getName().startsWith(ImapConsts.IMAP__INOTES_FOLDER)) {
                folderNames.add(f.getName());
            }
        }
        return folderNames;
    }

    private void notifyFolderFetched(long accountId, List<String> rFolderNames) {
        if (rFolderNames.size() == 0) {
            return;
        }

        INotesApplication application = (INotesApplication) context;
        List<String> localFolderNames = org.dayup.inotes.data.Folder.getAllFoldersName(accountId,
                application.getDBHelper());

        for (String remoteName : rFolderNames) {
            if (!localFolderNames.contains(remoteName)) {
                org.dayup.inotes.data.Folder folder = new org.dayup.inotes.data.Folder();
                folder.name = remoteName;
                folder.accountId = accountId;
                org.dayup.inotes.data.Folder.createFolder(folder, application.getDBHelper());
                if (org.dayup.inotes.data.Folder.TOP_LABEL_NAME.equals(folder.name)) {
                    Note.mergeAllFolderNotes2TopLabel(folder.accountId, folder.id,
                            application.getDBHelper());
                }
                syncEmailMan.refreshSyncingViews();
            }

        }
    }

    private Folder getFolder(String folderName) throws AuthenticationFailedException {
        Folder folder = null;
        boolean folderExists;
        try {
            folder = imapStore.getFolder(folderName);
            folderExists = folder.exists();
            if (!folderExists) {
                Log.i(TAG, "Label '" + folderName + "' does not exist yet. Creating.");
                folder.create(FolderType.HOLDS_MESSAGES);
            }
            folder.open(OpenMode.READ_WRITE);
        } catch (AuthenticationFailedException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getMessage());
        } catch (MessagingException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getMessage());
        }

        return folder;
    }

    private Folder getGmailTrashFolder() throws AuthenticationFailedException, IOException {
        Folder folder = null;
        boolean folderExists;
        try {
            folder = imapStore.getGmailTrashFolder();
            folderExists = folder.exists();
            if (!folderExists) {
                folder.create(FolderType.HOLDS_MESSAGES);
            }
            folder.open(OpenMode.READ_WRITE);
        } catch (AuthenticationFailedException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getMessage());
        } catch (MessagingException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getMessage());
        }

        return folder;
    }

    /**
     * @param messages
     * @return
     * @throws MessagingException
     */
    public List<MimeMessage> createNotes(List<MimeMessage> messages, String folderName)
            throws MessagingException {
        Folder folder = null;
        try {
            folder = getFolder(folderName);
            createNotes(messages, folder);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            throw new MessagingException(e.getLocalizedMessage());
        } finally {
            if (folder != null) {

                folder.close();

            }
        }
        return null;
    }

    /**
     * @param msgs
     * @throws MessagingException
     */
    public void deleteNote(Message[] msgs, String folderName) throws MessagingException {
        Folder folder = null;
        try {
            folder = getFolder(folderName);
            deleteNote(msgs, folder);
            folder.expunge();
            if (folder != null) {
                folder.close();
            }

            if (accountManager.isGmailAccount()) {
                Folder trash = getGmailTrashFolder();
                if (trash != null) {
                    List<Message> needDelMsg = new ArrayList<Message>();
                    for (Message msg : msgs) {
                        String uid = trash.getUidFromMessageId(msg);
                        if (!StringUtils.isEmpty(uid)) {
                            msg.setUid(trash.getUidFromMessageId(msg));
                            needDelMsg.add(msg);
                        }
                    }
                    if (needDelMsg.size() > 0) {
                        trash.delete(needDelMsg.toArray(new Message[needDelMsg.size()]), null);
                        trash.expunge();
                    }
                    if (trash != null) {
                        trash.close();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
            throw new MessagingException(e.getLocalizedMessage());
        } finally {
            if (folder != null) {
                folder.close();

            }
        }
    }

    public void rename(String oldName, String newName) throws MessagingException {
        Folder folder = null;
        try {
            folder = imapStore.getFolder(oldName);
            folder.open(OpenMode.READ_WRITE);
            folder.rename(oldName, newName);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            throw new MessagingException(e.getLocalizedMessage());
        } finally {
            if (folder != null) {
                folder.close();
            }
        }
    }

    /**
     * @param messages
     * @return
     * @throws MessagingException
     */
    public List<MimeMessage> updateNotes(List<MimeMessage> messages, String folderName)
            throws MessagingException {
        Folder folder = null;
        try {
            boolean isGmailAccount = accountManager.isGmailAccount();
            Message[] msgs = messages.toArray(new Message[messages.size()]);
            folder = getFolder(folderName);

            HashMap<String, String[]> labelsArray = new HashMap<String, String[]>();
            if (isGmailAccount) {
                for (Message msg : msgs) {
                    String uid = msg.getUid();
                    if (!StringUtils.isEmpty(uid)) {
                        try {
                            String[] value = folder.getLabelsByUid(uid);
                            labelsArray.put(msg.getMessageId(), value);
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage(), e);
                        }
                    }
                }
            }

            deleteNote(msgs, folder);
            folder.expunge();
            if (folder != null) {
                folder.close();
            }

            if (isGmailAccount) {
                Folder trash = getGmailTrashFolder();
                List<Message> needDelMsg = new ArrayList<Message>();
                for (Message msg : msgs) {
                    String uid = trash.getUidFromMessageId(msg);
                    if (!StringUtils.isEmpty(uid)) {
                        msg.setUid(trash.getUidFromMessageId(msg));
                        needDelMsg.add(msg);
                    }
                }
                trash.delete(needDelMsg.toArray(new Message[needDelMsg.size()]), null);
                trash.expunge();
                if (trash != null) {
                    trash.close();
                }
            }

            folder = getFolder(folderName);
            createNotes(messages, folder);
            if (labelsArray.size() > 0) {
                String[] labels = null;
                for (MimeMessage msgItem : messages) {
                    labels = labelsArray.get(msgItem.getMessageId());
                    if (labels != null && labels.length > 0) {
                        Message[] needlabelMessage = new Message[] {
                            msgItem
                        };
                        for (String label : labelsArray.get(msgItem.getMessageId())) {
                            folder.setLabel(needlabelMessage, label, true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
            throw new MessagingException(e.getLocalizedMessage());
        } finally {
            if (folder != null) {

                folder.close();

            }
        }
        return null;
    }

    /**
     * @param messages
     *            messageid is not null
     * @param folder
     * @throws MessagingException
     */
    public void createNotes(List<MimeMessage> messages, Folder folder) throws MessagingException {
        if (folder != null) {
            folder.appendMessages(messages.toArray(new Message[messages.size()]), appendMsglistener);
        }
    }

    /**
     * @param msgs
     *            uid is not null
     * @param folder
     * @throws MessagingException
     */
    public void deleteNote(Message[] msgs, Folder folder) throws MessagingException {
        if (folder != null) {
            try {
                if (accountManager.isGmailAccount()) {
                    folder.setLabel(msgs, folder.getName(), false);
                }
                folder.delete(msgs, getTrashFolder());
            } catch (MessagingException e) {
                // 要删除的邮件已经在服务器端被删除
                if (e.getMessage().contains("Can, not, find, message")) {
                    return;
                }
                // 邮箱中找不到相应名字的垃圾箱，且不能创建
                if (e.getMessage().contains("does not exist and could not be created for")) {
                    folder.delete(msgs, null);
                    return;
                }
                throw new MessagingException(e.getLocalizedMessage());
            }
        }
    }

    public Message[] getMessages(String folderName) throws AuthenticationFailedException {
        Folder folder = null;
        Message[] msgs = null;
        try {
            folder = getFolder(folderName);
            msgs = getMessages(folder, null);
            return msgs;
        } catch (AuthenticationFailedException e) {
            throw new AuthenticationFailedException(e.getMessage());
        } catch (MessagingException e) {
            Log.e(TAG, "", e);
            throw new AuthenticationFailedException(e.getMessage());
        } finally {
            if (folder != null) {

                folder.close();

            }
        }
    }

    /**
     * @param start
     * @param end
     * @param folder
     * @param listener
     * @return
     * @throws MessagingException
     */
    public Message[] getMessages(int start, int end, Folder folder,
            MessageRetrievalListener listener) throws MessagingException {
        return folder.getMessages(start, end, null, listener);
    }

    /**
     * @param folder
     * @param listener
     * @return
     * @throws MessagingException
     */
    public Message[] getMessages(Folder folder, MessageRetrievalListener listener)
            throws MessagingException {
        return folder.getMessages(listener);
    }

    public Message[] fetchMessages(final Message[] messages, final String folderName,
            final long accountId) throws MessagingException {
        Folder folder = null;
        try {
            folder = getFolder(folderName);
            return fetchMessages(folder, messages, new MessageRetrievalListener() {

                @Override
                public void messagesFinished(int total) {

                }

                @Override
                public void messageStarted(String uid, int number, int ofTotal) {

                }

                @Override
                public void messageFinished(Message message, int number, int ofTotal) {
                    try {
                        syncEmailMan.notifyMessageFetched(message, folderName, accountId);
                    } catch (MessagingException e) {
                        Log.e(TAG, "", e);
                    }
                }

                @Override
                public void messagefilteredByLarge(String subject) {
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "", e);
            throw new MessagingException(e.getLocalizedMessage());
        } finally {
            if (folder != null) {

                folder.close();

            }
        }
    }

    /**
     * @param folder
     * @param messages
     * @param listener
     * @return
     * @throws MessagingException
     */
    public Message[] fetchMessages(Folder folder, Message[] messages,
            MessageRetrievalListener listener) throws MessagingException {
        ArrayList<Message> msgs = new ArrayList<Message>();
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.BODY);
        fp.add(FetchProfile.Item.FLAGS);
        BinaryTempFileBody.setTempDirectory(AppFiles.INOTES_TMP_FILE_DIR);
        try {
            for (Message msg : messages) {
                if (syncEmailMan == null || !syncEmailMan.isSynchronizing()) {
                    break;
                }
                Message[] m = new Message[] {
                    msg
                };
                folder.fetch(m, fp, listener);
                msgs.add(m[0]);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
            throw new MessagingException(e.getLocalizedMessage());
        }
        return msgs.toArray(messages);
    }

    public void fetchPart(Message message, Folder folder, Part part,
            MessageRetrievalListener listener) throws MessagingException {
        folder.fetchPart(message, part, listener);
    }

    AppendMessagesListener appendMsglistener = new AppendMessagesListener() {

        @Override
        public void messageFinished(String uid, String messageID, int number, int total,
                long startedTime) {
            Log.i(TAG, "callback..." + uid + " : " + messageID);
            if (syncEmailMan != null) {
                syncEmailMan.updateNotesStatus(uid, messageID, Status.SYNC_DONE);
            }

        }

    };

    private String getTrashFolder() {
        if (accountManager.isGooglemailAccount()) {
            return Constants.ImapParameters.TRASH_FOLDER_GOOGLE_MAIL;
        }

        if (accountManager.isGmailAccount()) {
            return Constants.ImapParameters.TRASH_FOLDER_GMAIL;
        }

        return Constants.ImapParameters.TRASH_FOLDER;
    }
}
