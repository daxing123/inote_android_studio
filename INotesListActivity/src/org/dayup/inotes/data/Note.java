package org.dayup.inotes.data;

import java.util.ArrayList;
import java.util.List;

import org.dayup.common.Log;
import org.dayup.inotes.db.Field.Status;
import org.dayup.inotes.db.INotesDBHelper;
import org.dayup.inotes.db.INotesDBHelper.Transactable;
import org.dayup.inotes.db.NoteField;
import org.dayup.inotes.db.Table;
import org.dayup.inotes.utils.StringUtils;
import org.dayup.inotes.utils.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class Note extends BaseData {

    private final static String TAG = Note.class.getSimpleName();

    public static final Table table = new Table(NoteField.TABLE_NAME, NoteField.values(),
            NoteField.modified_time, NoteField.created_time);

    public String id;
    public long folderId;
    public long accountId;
    public String content;
    public String messageId;
    public String content_old;

    public static Note createNote(Note note, INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(NoteField._id.name(), note.id);
        values.put(NoteField.account_id.name(), note.accountId);
        values.put(NoteField.folder_id.name(), note.folderId);
        values.put(NoteField.content.name(), note.content);
        values.put(NoteField.content_old.name(), note.content);
        values.put(NoteField.sId.name(), note.sid);
        values.put(NoteField.created_time.name(), note.createdTime > 0 ? note.createdTime : null);
        values.put(NoteField.modified_time.name(), note.modifiedTime > 0 ? note.modifiedTime : null);
        values.put(NoteField._deleted.name(), note.deleted);
        values.put(NoteField._status.name(), note.status);
        long rowId = table.create(values, dbHelper);
        return note;
    }

    public static boolean updateNote(Note note, INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(NoteField._id.name(), note.id);
        values.put(NoteField.account_id.name(), note.accountId);
        values.put(NoteField.folder_id.name(), note.folderId);
        values.put(NoteField.content.name(), note.content);
        if (!TextUtils.isEmpty(note.sid)) {
            values.put(NoteField.sId.name(), note.sid);
        } else {
            // 没有同步过的，更新本地基准版本
            values.put(NoteField.content.name(), note.content);
        }
        values.put(NoteField._status.name(), Status.SYNC_UPDATE);

        String whereClause = NoteField._id.name() + "=?";
        String[] whereArgs = {
            note.id
        };
        int count = table.update(values, whereClause, whereArgs, dbHelper);
        return count > 0;
    }

    public static void deleteNote(Note note, INotesDBHelper dbHelper) {
        if (note.hasSynced()) {
            deleteNotesByIdLogical(note.id, note.accountId, dbHelper);
        } else {
            deleteNoteByIdForever(note.id, note.accountId, dbHelper);
        }
    }

    public static boolean deleteNotesByIdLogical(String id, long accountId, INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(NoteField._deleted.name(), Status.DELETED_YES);
        values.put(NoteField._status.name(), Status.SYNC_UPDATE);

        String whereClause = NoteField._id.name() + "=? and " + NoteField.account_id.name() + "=?";
        String[] whereArgs = {
                id, accountId + ""
        };
        int count = table.update(values, whereClause, whereArgs, dbHelper);
        return count > 0;
    }

    /**
     * remove the account while clear the notes of client
     * 
     * @param accountId
     * @param dbHelper
     */
    public static void deleteNotesForeverByAccountId(long accountId, INotesDBHelper dbHelper) {
        table.deleteById(NoteField.account_id, accountId + "", dbHelper);
    }

    public static Note getNoteById(String id, INotesDBHelper dbHelper) {
        String selection = "Note._id=?";
        String[] selectionArgs = {
            id
        };
        List<Note> noteList = getAllNotes(selection, selectionArgs, null, dbHelper);
        if (noteList.size() > 0) {
            return noteList.get(0);
        }
        return null;
    }

    public static Note getNoteNotDeletedById(long accountId, String id, INotesDBHelper dbHelper) {
        StringBuffer selection = new StringBuffer();
        selection.append(NoteField._id.name()).append(" = ?");
        String[] selectionArgs = new String[] {
            id
        };
        List<Note> notes = getAllNotesByAccountId(accountId, selection.toString(), selectionArgs,
                null, dbHelper);
        return notes.size() > 0 ? notes.get(0) : null;
    }

    public static List<Note> getAllNotesByAccountId(long accountId, String selection,
            String[] selectionArgs, String orderBy, INotesDBHelper dbHelper) {
        if (selection == null) {
            selection = NoteField._deleted.name() + " =" + Status.DELETED_NO + " and ("
                    + NoteField.account_id.name() + "=" + accountId + ")";
        } else {
            selection = selection + " and " + NoteField._deleted.name() + "=" + Status.DELETED_NO
                    + " and (" + NoteField.account_id.name() + "=" + accountId + ")";
        }
        return getAllNotes(selection, selectionArgs, orderBy, dbHelper);
    }

    private static List<Note> getAllNotes(String selection, String[] selectionArgs, String orderBy,
            INotesDBHelper dbHelper) {

        Cursor c = null;
        List<Note> notesList = new ArrayList<Note>();
        try {
            c = table.query(selection, selectionArgs,
                    StringUtils.isEmpty(orderBy) ? "Note.modified_time desc" : orderBy, dbHelper);
            if (c != null && c.moveToFirst()) {
                do {
                    notesList.add(cursorToNote(c));
                } while (c.moveToNext());
            }
            return notesList;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private static Note cursorToNote(Cursor c) {
        Note note = new Note();
        note.id = c.getString(c.getColumnIndex(NoteField._id.name()));
        note.messageId = StringUtils.uuId2MessageId(note.id);
        note.sid = c.getString(c.getColumnIndex(NoteField.sId.name()));
        note.accountId = c.getLong(c.getColumnIndex(NoteField.account_id.name()));
        note.folderId = c.getLong(c.getColumnIndex(NoteField.folder_id.name()));
        note.content = c.getString(c.getColumnIndex(NoteField.content.name()));
        note.content_old = c.getString(c.getColumnIndex(NoteField.content_old.name()));
        note.status = c.getInt(c.getColumnIndex(NoteField._status.name()));
        note.createdTime = c.getLong(c.getColumnIndex(NoteField.created_time.name()));
        note.modifiedTime = c.getLong(c.getColumnIndex(NoteField.modified_time.name()));
        return note;
    }

    public static String getNoteSubject(String content) {
        if (StringUtils.isEmpty(content)) {
            return "";
        }
        try {
            String[] strArray = content.split("\n");
            if (strArray.length >= 2) {
                StringBuffer sb = new StringBuffer();
                for (int i = 1, j = strArray.length; i < j; i++) {
                    sb.append(strArray[i]);
                    if (i != j - 1) {
                        sb.append("\n");
                    }
                }
                return strArray[0].trim();
            } else {
                return content;
            }
        } catch (Exception e) {
            Log.e("Note", e.getMessage(), e);
            return content;
        }
    }

    public static boolean mergeLocalModeNote2CurrentAccount(long accountId, INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(NoteField.account_id.name(), accountId);
        values.put(NoteField._status.name(), Status.SYNC_NEW);

        String whereClause = NoteField.account_id.name() + "=?";
        String[] whereArgs = {
            Status.LOCAL_MODE_ACCOUNT_ID + ""
        };
        int count = table.update(values, whereClause, whereArgs, dbHelper);
        return count > 0;

    }

    public static boolean mergeAllFolderNotes2TopLabel(long accountId, long topLabelId,
            INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(NoteField.folder_id.name(), topLabelId);
        String whereClause = NoteField.account_id.name() + "=? and " + NoteField.folder_id.name()
                + "=?";
        String[] whereArgs = {
                accountId + "", Folder.ALL_FOLDER_ID + ""
        };
        int count = table.update(values, whereClause, whereArgs, dbHelper);
        return count > 0;

    }

    public static List<Note> getNotesByFolder(Folder folder, String orderBy, INotesDBHelper dbHelper) {
        StringBuffer sb = new StringBuffer();
        ArrayList<String> whereArgs = new ArrayList<String>();

        if (folder.id != Folder.ALL_FOLDER_ID) {
            sb.append(" Note.folder_id = ?");
            whereArgs.add("" + folder.id);
        }

        String[] args = new String[whereArgs.size()];
        for (int i = 0, j = whereArgs.size(); i < j; i++) {
            args[i] = whereArgs.get(i);
        }
        if (whereArgs.size() == 0 && sb.length() == 0) {
            return Note.getAllNotesByAccountId(folder.accountId, null, null, orderBy, dbHelper);
        } else {
            return Note.getAllNotesByAccountId(folder.accountId, sb.toString(), args, orderBy,
                    dbHelper);
        }
    }

    public static List<Note> getLocalAddedNotes(long userId, INotesDBHelper dbHelper) {
        String selection = "Note._status=? or (Note._status=? and Note.sid is null)";
        String[] selectionArgs = {
                "" + Status.SYNC_NEW, "" + Status.SYNC_UPDATE
        };
        return getAllNotesByAccountId(userId, selection, selectionArgs, NoteField.folder_id.name(),
                dbHelper);
    }

    public static List<Note> getLocalDeletedNotes(long userId, INotesDBHelper dbHelper) {
        String selection = new StringBuffer().append("(Note.account_id =").append(userId)
                .append(" or Note.account_id = " + Status.LOCAL_MODE_ACCOUNT_ID + ")")
                .append("and Note._deleted <>").append(Status.DELETED_NO)
                .append(" and Note.sid is not null").toString();
        Log.d(TAG, selection);
        Cursor c = null;
        List<Note> notesList = new ArrayList<Note>();
        try {
            c = table.query(selection, null, NoteField.folder_id.name(), dbHelper);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Note n = cursorToNote(c);
                notesList.add(n);
                c.moveToNext();
            }
            return notesList;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static List<Note> getLocalUpdatedNotes(long userId, INotesDBHelper dbHelper) {
        String selection = "Note._status=? and Note.sid is not null";
        String[] selectionArgs = {
            "" + Status.SYNC_UPDATE
        };
        return getAllNotesByAccountId(userId, selection, selectionArgs, NoteField.folder_id.name(),
                dbHelper);
    }

    public static List<Note> getNeedSyncNotes(long userId, INotesDBHelper dbHelper) {
        String selection = "Note.account_id =? and (Note._status<>? or Note._deleted <>?)";
        String[] selectionArgs = {
                userId + "", "" + Status.SYNC_DONE, "" + Status.DELETED_NO
        };
        return getAllNotes(selection, selectionArgs, "Note.modified_time desc", dbHelper);
    }

    public static Note getNoteBySid(String sid, long folderId, long accountId,
            INotesDBHelper dbHelper) {
        StringBuffer selection = new StringBuffer();
        selection.append(NoteField.sId.name()).append(" =? and ")
                .append(NoteField.folder_id.name()).append(" =?");

        String[] selectionArgs = {
                sid, folderId + ""
        };
        List<Note> notes = getAllNotesByAccountId(accountId, selection.toString(), selectionArgs,
                null, dbHelper);
        if (notes.size() > 0) {
            return notes.get(0);
        }
        return null;
    }

    public static boolean deleteNoteByIdForever(String id, long accountId, INotesDBHelper dbHelper) {
        table.deleteByIdAndUserId(NoteField._id, id, NoteField.account_id, accountId, dbHelper);
        return true;
    }

    public static void deleteNotesForeverByFolderId(long accountId, long folderId,
            INotesDBHelper dbHelper) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE from ").append(NoteField.TABLE_NAME).append(" where (")
                .append(NoteField.account_id.name()).append(" = ").append(accountId)
                .append(" and ").append(NoteField.folder_id.name()).append(" = ").append(folderId)
                .append(")");
        dbHelper.getWritableDatabase().execSQL(sql.toString());
    }

    public static Note getNoteByIdNotDel(String id, INotesDBHelper dbHelper) {
        if (id != null) {
            StringBuffer selection = new StringBuffer();
            selection.append(NoteField._id.name()).append(" = ? and ")
                    .append(NoteField._deleted.name()).append(" = ?");

            String[] selectionArgs = {
                    id, Status.DELETED_NO + ""
            };
            List<Note> notes = getAllNotes(selection.toString(), selectionArgs, null, dbHelper);
            if (notes.size() > 0) {
                return notes.get(0);
            }
        }
        return null;
    }

    private static final String NOTE_WHERECLAUSE_BASE = NoteField._id.name() + "=? and "
            + NoteField.account_id.name() + "=?";

    public static boolean updateNotesStatus(Note note, long accountId, INotesDBHelper dbHelper,
            int status, boolean isModifyTime) {
        ContentValues values = new ContentValues();
        values.put(NoteField._status.name(), status);
        if (note.sid != null) {
            values.put(NoteField.sId.name(), note.sid);
        }
        if (isModifyTime) {
            values.put(NoteField.created_time.name(), note.createdTime);
            values.put(NoteField.modified_time.name(), note.modifiedTime);
        }

        String[] whereArgs = {
                note.id, accountId + ""
        };
        int ret = table.updateWithoutModifyDate(values, NOTE_WHERECLAUSE_BASE, whereArgs, dbHelper);
        return ret > 0;
    }

    public static Note getNoteBySIdNotDel(long accountId, String sId, INotesDBHelper dbHelper) {
        String selection = NoteField.sId.name() + " = ?";
        String[] selectionArgs = {
            sId
        };
        List<Note> notes = getAllNotesByAccountId(accountId, selection, selectionArgs, null,
                dbHelper);
        if (notes.size() > 0) {
            return notes.get(0);
        }
        return null;
    }

    public static boolean updateNotesWithoutModifyDate(Note note, long accountId,
            INotesDBHelper dbHelper) {
        String[] whereArgs = {
                note.id, accountId + ""
        };
        ContentValues values = new ContentValues();
        values.put(NoteField.folder_id.name(), note.folderId);
        values.put(NoteField.content.name(), note.content);
        values.put(NoteField.content_old.name(), note.content_old);
        if (!TextUtils.isEmpty(note.sid)) {
            values.put(NoteField.sId.name(), note.sid);
        }
        values.put(NoteField.modified_time.name(), note.modifiedTime);
        values.put(NoteField._status.name(), note.status);
        int count = table.updateWithoutModifyDate(values, NOTE_WHERECLAUSE_BASE, whereArgs,
                dbHelper);
        return count > 0;
    }

    public static void moveToFolder(final Note note, long toFolderId, INotesDBHelper dbHelper) {
        final Note newNote = new Note();
        newNote.id = Utils.getRandomUUID36();
        newNote.folderId = toFolderId;
        newNote.content = note.content;
        newNote.modifiedTime = note.modifiedTime;
        newNote.createdTime = note.createdTime;
        newNote.accountId = note.accountId;
        newNote.status = Status.SYNC_NEW;
        dbHelper.doInTransaction(new Transactable<Boolean>() {

            @Override
            public Boolean doIntransaction(INotesDBHelper dbHelper) {
                deleteNote(note, dbHelper);
                createNote(newNote, dbHelper);
                return true;
            }
        });
    }

    public static List<Note> getAllSyncedNoteByFolderId(long folderId, long accountId,
            INotesDBHelper dbHelper) {
        StringBuffer selection = new StringBuffer();
        selection.append(NoteField._status.name()).append("=? and ").append(NoteField.sId.name())
                .append(" is not null and ").append(NoteField.folder_id.name()).append("=? and ")
                .append(NoteField.account_id.name()).append(" =?");
        String[] selectionArgs = new String[] {
                Status.SYNC_DONE + "", folderId + "", accountId + ""
        };
        return getAllNotesByAccountId(accountId, selection.toString(), selectionArgs, null,
                dbHelper);
    }

    public static List<String> getAllSyncedNoteSidByFolderId(long folderId, long accountId,
            INotesDBHelper dbHelper) {
        StringBuffer selection = new StringBuffer();
        selection.append(NoteField._status.name()).append("=? and ").append(NoteField.sId.name())
                .append(" is not null and ").append(NoteField.folder_id.name()).append("=? and ")
                .append(NoteField.account_id.name()).append(" =?");
        String[] selectionArgs = new String[] {
                Status.SYNC_DONE + "", folderId + "", accountId + ""
        };
        Cursor c = null;
        List<String> noteSid = new ArrayList<String>();
        try {
            c = table.query(selection.toString(), selectionArgs, "Note.modified_time desc",
                    dbHelper);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                noteSid.add(c.getString(c.getColumnIndex(NoteField.sId.name())));
                c.moveToNext();
            }
            return noteSid;
        } finally {
            if (c != null) {
                c.close();
            }
        }

    }

    public static Note getDuplicatedNoteForSyncErro(Note note, INotesDBHelper dbHelper) {
        StringBuffer selection = new StringBuffer();
        selection.append(NoteField.created_time.name()).append(" = ? and ")
                .append(NoteField.content.name()).append(" =? and ")
                .append(NoteField.folder_id.name()).append(" = ?");

        String[] selectionArgs = new String[] {
                note.createdTime + "", note.content, note.folderId + ""
        };
        List<Note> notes = getAllNotesByAccountId(note.accountId, selection.toString(),
                selectionArgs, null, dbHelper);
        return notes.size() > 0 ? notes.get(0) : null;
    }

    public static Cursor getNoteCursor4SuggestionSearch(long accountId, String query,
            String selection, String[] selectionArgs, INotesDBHelper dbHelper) {
        StringBuffer contentSelection = new StringBuffer();
        contentSelection.append(NoteField.content.name()).append(" like \'%")
                .append(StringUtils.escapeSql(query)).append("%\' and ")
                .append(NoteField._deleted.name()).append(" = ").append(Status.DELETED_NO)
                .append(" and (").append(NoteField.account_id.name()).append(" = ")
                .append(accountId).append(" or ").append(NoteField.account_id.name()).append(" = ")
                .append(Status.LOCAL_MODE_ACCOUNT_ID).append(")");
        selection = TextUtils.isEmpty(selection) ? contentSelection.toString() : selection
                + " and " + contentSelection.toString();
        String[] columns = {
                BaseColumns._ID, NoteField.content.name(), NoteField._id.name()
        };
        return dbHelper.getWritableDatabase().query(Note.table.tableName(), columns, selection,
                selectionArgs, null, null, null);
    }

    public static List<Note> getNotes4SuggestionSearch(long accountId, String query,
            INotesDBHelper dbHelper) {
        StringBuffer selection = new StringBuffer();
        selection.append(NoteField.content.name()).append(" like \'%")
                .append(StringUtils.escapeSql(query)).append("%\' and ")
                .append(NoteField._deleted.name()).append(" =?").append(" and (")
                .append(NoteField.account_id.name()).append(" =?").append(" or ")
                .append(NoteField.account_id.name()).append(" = ")
                .append(Status.LOCAL_MODE_ACCOUNT_ID).append(")");
        String[] selectionArgs = new String[] {
                Status.DELETED_NO + "", accountId + ""
        };

        return getAllNotes(selection.toString(), selectionArgs, null, dbHelper);

    }

}
