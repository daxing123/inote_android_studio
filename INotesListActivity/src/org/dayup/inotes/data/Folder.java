package org.dayup.inotes.data;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.dayup.inotes.db.Field.Status;
import org.dayup.inotes.db.FolderField;
import org.dayup.inotes.db.INotesDBHelper;
import org.dayup.inotes.db.INotesDBHelper.Transactable;
import org.dayup.inotes.db.NoteField;
import org.dayup.inotes.db.Table;
import org.dayup.inotes.utils.StringUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

public class Folder extends BaseData {
    public static final Table table = new Table(FolderField.TABLE_NAME, FolderField.values(),
            FolderField.modified_time, FolderField.created_time);
    public final static long ALL_FOLDER_ID = -1;

    public final static String TOP_LABEL_NAME = "Notes";

    public long id;
    public long accountId;
    public String name;
    // public long startCheckpoint;
    // public long endCheckpoint;
    public String displayName;
    public int position;

    public static Folder createAllLabel(long accoutnId, String name) {
        Folder all = new Folder();
        all.id = ALL_FOLDER_ID;
        all.accountId = accoutnId;
        all.displayName = name;
        all.name = name;
        return all;
    }

    public static Folder createFolder(Folder folder, INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(FolderField.account_id.name(), folder.accountId);
        values.put(FolderField.name.name(), folder.name);
        folder.id = table.create(values, dbHelper);
        // folder.startCheckpoint = 0;
        // folder.endCheckpoint = 0;
        return folder;

    }

    public static boolean updateFolder(Folder folder, INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(FolderField.account_id.name(), folder.accountId);
        values.put(FolderField.name.name(), folder.name);
        // values.put(FolderField.startCheckPoint.name(),
        // folder.startCheckpoint);
        // values.put(FolderField.endCheckPoint.name(), folder.endCheckpoint);

        String whereClause = FolderField._id.name() + "=?";
        String[] whereArgs = {
            folder.id + ""
        };
        int count = table.update(values, whereClause, whereArgs, dbHelper);
        return count > 0;
    }

    /**
     * remove account while remove all the folder of client
     * 
     * @param id
     * @param dbHelper
     */
    public static void deleteFolderByAccountIdForever(long id, INotesDBHelper dbHelper) {
        table.deleteById(FolderField.account_id, id + "", dbHelper);
    }

    public static void deleteFolderByIdForever(final long accountId, final long id,
            INotesDBHelper dbHelper) {
        dbHelper.doInTransaction(new Transactable<Boolean>() {

            @Override
            public Boolean doIntransaction(INotesDBHelper dbHelper) {
                Note.deleteNotesForeverByFolderId(accountId, id, dbHelper);
                table.deleteById(FolderField._id, id + "", dbHelper);
                return true;
            }
        });
    }

    /**
     * return the folder without display name, always support the sync data
     * 
     * @param id
     * @param accountId
     * @param dbHelper
     * @return
     */
    public static Folder getFolderById(long id, long accountId, INotesDBHelper dbHelper) {
        String selection = "Folder._id=?";
        String[] selectionArgs = {
            id + ""
        };
        List<Folder> folderList = getAllFoldersByAccountId(accountId, selection, selectionArgs,
                null, dbHelper);
        if (folderList.size() > 0) {
            return folderList.get(0);
        }
        return null;
    }

    /**
     * return the folder with the display name, always used for UI display
     * 
     * @param ids
     * @param accountId
     * @param dbHelper
     * @return
     */
    public static Folder getFolderWithDisplaynameById(long id, long accountId,
            INotesDBHelper dbHelper) {
        String selection = "Folder._id=?";
        String[] selectionArgs = {
            id + ""
        };
        List<Folder> folderList = getAllFoldersWithDisplayNameByAccountId(accountId, selection,
                selectionArgs, null, dbHelper);
        if (folderList.size() > 0) {
            return folderList.get(0);
        }
        return null;
    }

    public static List<Folder> getAllFolders(String selection, String[] selectionArgs,
            String orderBy, INotesDBHelper dbHelper) {
        Cursor c = null;
        List<Folder> folderList = new ArrayList<Folder>();
        try {
            c = table.query(selection, selectionArgs,
                    orderBy == null ? "Folder._id desc" : orderBy, dbHelper);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Folder folder = cursorToFolder(c);
                folderList.add(folder);
                c.moveToNext();
            }
            return folderList;
        } finally {
            if (c != null) {
                c.close();
            }
        }

    }

    public static Folder getFolderByName(long accountId, String name, INotesDBHelper dbHelper) {
        String selection = FolderField.name.name() + " like \'" + StringUtils.escapeSql(name)
                + "\'";
        List<Folder> foldersList = getAllFoldersByAccountId(accountId, selection, null, null,
                dbHelper);
        if (foldersList.size() > 0 && name.equals(foldersList.get(0).name)) {
            return foldersList.get(0);
        }
        return null;
    }

    /**
     * return the folders without the display name, always used for sync data
     * 
     * @param accountId
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @param dbHelper
     * @return
     */
    public static List<Folder> getAllFoldersByAccountId(long accountId, String selection,
            String[] selectionArgs, String orderBy, INotesDBHelper dbHelper) {
        if (selection == null) {
            selection = FolderField.account_id.name() + " = " + accountId;
        } else {
            selection = selection + " and " + FolderField.account_id.name() + " = " + accountId;
        }
        return getAllFolders(selection, selectionArgs, orderBy, dbHelper);
    }

    /**
     * return the folders with the display name, always used for UI display
     * 
     * @param accountId
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @param dbHelper
     * @return
     */
    public static List<Folder> getAllFoldersWithDisplayNameByAccountId(long accountId,
            String selection, String[] selectionArgs, String orderBy, INotesDBHelper dbHelper) {
        List<Folder> folders = getAllFoldersByAccountId(accountId, selection, selectionArgs,
                orderBy, dbHelper);
        return setFoldersDisPlayname(getAllFoldersName(accountId, dbHelper), folders);
    }

    private static List<Folder> setFoldersDisPlayname(List<String> folderNames,
            List<Folder> folderList) {
        List<Folder> newFolders = folderList;
        for (Folder folder : newFolders) {
            setNoteDisplayName(folder, folderNames);
        }
        return newFolders;
    }

    private static void setNoteDisplayName(Folder folder, List<String> folderNames) {
        if (TextUtils.isEmpty(folder.name) || !folder.name.contains("/")) {
            folder.displayName = folder.name;
            return;
        }

        String tmp = folder.name;
        int lastEnd = 0;
        int end = 0;
        for (int i = 0;; i++) {
            end = tmp.indexOf("/", lastEnd);
            if (end != -1 && folderNames.contains(tmp.substring(0, end))) {
                lastEnd = end + 1;
            } else {
                folder.displayName = tmp.substring(lastEnd);
                folder.position = i;
                return;
            }

        }
    }

    public static List<String> getAllFoldersName(long accountId, INotesDBHelper dbHelper) {
        Cursor c = null;
        List<String> folderNames = new ArrayList<String>();
        String selection = FolderField.account_id.name() + " = " + accountId;

        try {
            c = table.query(selection, null, "Folder._id desc", dbHelper);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Folder folder = cursorToFolder(c);
                folderNames.add(folder.name);
                c.moveToNext();
            }
            return folderNames;
        } finally {
            if (c != null) {
                c.close();
            }
        }

    }

    private static Folder cursorToFolder(Cursor c) {
        Folder folder = new Folder();
        folder.id = c.getLong(c.getColumnIndex(FolderField._id.name()));
        folder.accountId = c.getLong(c.getColumnIndex(FolderField.account_id.name()));
        folder.name = c.getString(c.getColumnIndex(FolderField.name.name()));
        // folder.startCheckpoint =
        // c.getLong(c.getColumnIndex(FolderField.startCheckPoint.name()));
        // folder.endCheckpoint =
        // c.getLong(c.getColumnIndex(FolderField.endCheckPoint.name()));
        folder.createdTime = c.getLong(c.getColumnIndex(FolderField.created_time.name()));
        folder.modifiedTime = c.getLong(c.getColumnIndex(FolderField.modified_time.name()));
        return folder;
    }

    public static Comparator<Folder> folderComparatorPosition = new Comparator<Folder>() {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.CHINA);

        @Override
        public int compare(Folder f1, Folder f2) {
            int p1 = f1.position;
            int p2 = f2.position;
            String dn1 = f1.displayName;
            String dn2 = f2.displayName;

            if (p1 - p2 == 0) {
                return collator.compare(dn1, dn2);
            }
            if (p1 - p2 > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    public static List<Folder> getAllFoldersChangedByAccountId(long accountId,
            INotesDBHelper dbHelper) {
        StringBuffer selection = new StringBuffer();
        selection.append(FolderField.account_id.name()).append(" = ? and ")
                .append(FolderField._id.name()).append(" IN ( SELECT ")
                .append(NoteField.folder_id.name()).append(" FROM ").append(NoteField.TABLE_NAME)
                .append(" WHERE ").append(NoteField._status.name()).append(" = ? AND ")
                .append(NoteField._deleted.name()).append(" = ? )");
        String[] selectionArgs = new String[] {
                accountId + "", Status.SYNC_UPDATE + "", Status.DELETED_NO + ""
        };
        return getAllFolders(selection.toString(), selectionArgs, null, dbHelper);
    }
}
