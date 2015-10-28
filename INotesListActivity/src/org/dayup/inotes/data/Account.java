package org.dayup.inotes.data;

import java.util.ArrayList;
import java.util.List;

import org.dayup.decription.Base64Coder;
import org.dayup.inotes.constants.Constants.DefaultAuthParams;
import org.dayup.inotes.db.AccountField;
import org.dayup.inotes.db.Field.Status;
import org.dayup.inotes.db.INotesDBHelper;
import org.dayup.inotes.db.Table;

import android.content.ContentValues;
import android.database.Cursor;

public class Account extends BaseData {

    public static final Table table = new Table(AccountField.TABLE_NAME, AccountField.values(),
            AccountField.modifyTime, AccountField.createdTime);

    public long id;
    public String email;
    public String password;
    public int activity;
    public long startCheckPoint;
    public long endCheckPoint;
    public long lastSyncTime;
    public long defaultFolderId;

    public long topLabelId = Folder.ALL_FOLDER_ID;

    public String imap;
    public String smtp;
    public String port;
    public String securityType;
    public String authType;

    public boolean isLocalMode() {
        return id == Status.LOCAL_MODE_ACCOUNT_ID;
    }

    private String getEncodePassword() {
        return Base64Coder.encodeString(password);
    }

    private String getDecodePassword() {
        return Base64Coder.decodeString(password);
    }

    public static Account getGMailAccount(String email, String password) {
        Account account = new Account();
        account.email = email;
        account.password = password;
        account.imap = DefaultAuthParams.IMAP;
        account.port = DefaultAuthParams.PORT_993;
        account.securityType = DefaultAuthParams.SECURITY_TYPE;
        account.authType = DefaultAuthParams.AUTH_TYPE;
        account.smtp = "";
        account.startCheckPoint = 0;
        account.endCheckPoint = 0;
        account.lastSyncTime = 0;
        account.activity = Status.ACCOUNT_ACTIVITY_FREEZE;
        account.defaultFolderId = Folder.ALL_FOLDER_ID;
        return account;
    }

    public static Account getDefaultAccount() {
        Account account = new Account();
        account.id = Status.LOCAL_MODE_ACCOUNT_ID;
        account.email = "";
        account.password = "";
        account.imap = "";
        account.smtp = "";
        account.activity = Status.ACCOUNT_ACTIVITY_FREEZE;
        account.defaultFolderId = Folder.ALL_FOLDER_ID;
        return account;
    }

    public static Account createAccount(Account account, INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(AccountField.email.name(), account.email);
        values.put(AccountField.password.name(), account.getEncodePassword());
        values.put(AccountField.activity.name(), account.activity);
        values.put(AccountField.default_folder_id.name(), account.defaultFolderId);
        values.put(AccountField.imap.name(), account.imap);
        values.put(AccountField.smtp.name(), account.smtp);
        values.put(AccountField.port.name(), account.port);
        values.put(AccountField.security_type.name(), account.securityType);
        values.put(AccountField.auth_type.name(), account.authType);

        account.id = table.create(values, dbHelper);
        return account;
    }

    public static boolean updateAccount(Account account, INotesDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(AccountField.email.name(), account.email);
        values.put(AccountField.password.name(), account.getEncodePassword());
        values.put(AccountField.activity.name(), account.activity);
        values.put(AccountField.default_folder_id.name(), account.defaultFolderId);
        values.put(AccountField.imap.name(), account.imap);
        values.put(AccountField.smtp.name(), account.smtp);
        values.put(AccountField.security_type.name(), account.securityType);
        values.put(AccountField.auth_type.name(), account.authType);

        String whereClause = AccountField._id.name() + "=?";
        String[] whereArgs = {
            account.id + ""
        };
        int ret = table.update(values, whereClause, whereArgs, dbHelper);
        return ret > 0;

    }

    public static void deleteAccountByIdForever(long id, INotesDBHelper dbHelper) {
        table.deleteById(AccountField._id, id + "", dbHelper);
    }

    public static Account getActiveAccount(INotesDBHelper dbHelper) {
        String selection = AccountField.activity.name() + "=?";
        String[] selectionArgs = {
            Status.ACCOUNT_ACTIVITY_ACTIVE + ""
        };
        List<Account> accountList = getAllAccounts(selection, selectionArgs, null, dbHelper);
        if (accountList.size() > 0) {
            return accountList.get(0);
        }
        return null;
    }

    public static Account getAccountById(long id, INotesDBHelper dbHelper) {
        String selection = "Account._id=?";
        String[] selectionArgs = {
            id + ""
        };
        List<Account> accountList = getAllAccounts(selection, selectionArgs, null, dbHelper);
        if (accountList.size() > 0) {
            return accountList.get(0);
        }
        return null;
    }

    public static List<Account> getAllAccounts(String selection, String[] selectionArgs,
            String orderBy, INotesDBHelper dbHelper) {

        Cursor c = null;
        List<Account> accountList = new ArrayList<Account>();
        try {
            c = table.query(selection, selectionArgs, orderBy == null ? "Account._id desc"
                    : orderBy, dbHelper);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                accountList.add(cursorToAccount(c));
                c.moveToNext();
            }
            return accountList;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * when remove the current account, need reset a new current account
     * 
     * @param dbHelper
     * @return
     */
    public static Account getDefaultCurrentAccount(INotesDBHelper dbHelper) {
        String orderBy = "Account._id asc";
        List<Account> accounts = getAllAccounts(null, null, orderBy, dbHelper);
        if (accounts.size() > 0) {
            return accounts.get(0);
        } else {
            return getDefaultAccount();
        }
    }

    private static Account cursorToAccount(Cursor c) {
        Account account = new Account();
        account.id = c.getLong(c.getColumnIndex(AccountField._id.name()));
        account.email = c.getString(c.getColumnIndex(AccountField.email.name()));
        account.password = c.getString(c.getColumnIndex(AccountField.password.name()));
        // TODO 加密和解密可以在调用接口之前，并非DAO时
        account.password = account.getDecodePassword();
        account.imap = c.getString(c.getColumnIndex(AccountField.imap.name()));
        account.smtp = c.getString(c.getColumnIndex(AccountField.smtp.name()));
        account.activity = c.getInt(c.getColumnIndex(AccountField.activity.name()));
        account.defaultFolderId = c
                .getLong(c.getColumnIndex(AccountField.default_folder_id.name()));
        account.modifiedTime = c.getLong(c.getColumnIndex(AccountField.modifyTime.name()));
        account.createdTime = c.getLong(c.getColumnIndex(AccountField.createdTime.name()));

        account.port = c.getString(c.getColumnIndex(AccountField.port.name()));
        account.securityType = c.getString(c.getColumnIndex(AccountField.security_type.name()));
        account.authType = c.getString(c.getColumnIndex(AccountField.auth_type.name()));
        return account;
    }

    public static void activeAccountById(long id, INotesDBHelper dbHelper) {
        String sql = "UPDATE " + AccountField.TABLE_NAME + " SET " + AccountField.activity.name()
                + " = CASE WHEN " + AccountField._id.name() + " = " + id + " THEN "
                + Status.ACCOUNT_ACTIVITY_ACTIVE + " ELSE " + Status.ACCOUNT_ACTIVITY_FREEZE
                + " END";
        dbHelper.getWritableDatabase().execSQL(sql);
    }

}
