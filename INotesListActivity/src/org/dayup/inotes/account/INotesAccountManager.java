package org.dayup.inotes.account;

import org.dayup.common.Log;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.constants.Constants;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.data.Folder;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.db.Field.Status;
import org.dayup.inotes.db.INotesDBHelper;
import org.dayup.inotes.db.INotesDBHelper.Transactable;
import org.dayup.inotes.sync.client.ImapStoreClient;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

public class INotesAccountManager {
    private final static String TAG = INotesAccountManager.class.getSimpleName();
    private INotesApplication application;
    private INotesDBHelper dbHelper;
    private Account mAccount;

    public INotesAccountManager(Context context) {
        this.application = (INotesApplication) context.getApplicationContext();
        this.dbHelper = application.getDBHelper();
    }

    public Account getAccount() {
        return mAccount;
    }

    public void setAccount(Account account) {
        this.mAccount = account;
    }

    public String getEmail() {
        return this.mAccount.email;
    }

    public String getPassword() {
        return mAccount.password;
    }

    public boolean isLocalMode() {
        return mAccount.isLocalMode();
    }

    public boolean isAccountFreezed() {
        return mAccount.activity == Status.ACCOUNT_ACTIVITY_FREEZE;
    }

    public void setAccountFreezed() {
        mAccount.activity = Status.ACCOUNT_ACTIVITY_FREEZE;
    }

    public long getDefaultFolderId() {
        return mAccount.defaultFolderId;
    }

    public void setDefaultFolderId(long folderId) {
        this.mAccount.defaultFolderId = folderId;
        Account.updateAccount(mAccount, dbHelper);
    }

    public long getTopLabelId() {
        if (mAccount.topLabelId == Folder.ALL_FOLDER_ID) {
            Folder f = Folder.getFolderByName(mAccount.id, Folder.TOP_LABEL_NAME, dbHelper);
            if (f != null) {
                mAccount.topLabelId = f.id;
            }
        }
        return mAccount.topLabelId;
    }

    public long getAccountId() {
        return mAccount.id;
    }

    public void init() {
        mAccount = Account.getActiveAccount(dbHelper);
        if (mAccount == null) {
            mAccount = Account.getDefaultCurrentAccount(dbHelper);
            switchAccount(mAccount);
        }
    }

    public void insertActiveAccount(final Account account) {
        application.getDBHelper().doInTransaction(new Transactable<Boolean>() {

            @Override
            public Boolean doIntransaction(INotesDBHelper dbHelper) {
                Account.createAccount(account, dbHelper);
                Account.activeAccountById(account.id, dbHelper);
                return true;
            }
        });
        mAccount = account;
        mAccount.activity = Status.ACCOUNT_ACTIVITY_ACTIVE;
    }

    public void switchAccount(Account account) {
        Account.activeAccountById(account.id, application.getDBHelper());
        mAccount = account;
        mAccount.activity = Status.ACCOUNT_ACTIVITY_ACTIVE;
    }

    public boolean switchAccount(long id) {
        application.stopSynchronize();
        Account account = Account.getAccountById(id, application.getDBHelper());
        if (account == null) {
            return false;
        }
        Account.activeAccountById(account.id, application.getDBHelper());
        mAccount = account;
        mAccount.activity = Status.ACCOUNT_ACTIVITY_ACTIVE;
        return true;
    }

    public void resetAccountPassword(String password) {
        mAccount.password = password;
        Account.updateAccount(mAccount, dbHelper);
    }

    public void deleteAccount(Account account) {
        application.stopSynchronize();
        clearAccountDataForever(account.id);
        if (mAccount.id == account.id) {
            mAccount = Account.getDefaultCurrentAccount(application.getDBHelper());
            switchAccount(mAccount);
        }
    }

    /**
     * 只有在remove acoount时调用，完全是物理删除，不进行同步
     * 
     * @param accountId
     */
    private void clearAccountDataForever(final long accountId) {
        application.getDBHelper().doInTransaction(new Transactable<Boolean>() {

            @Override
            public Boolean doIntransaction(INotesDBHelper dbHelper) {
                Folder.deleteFolderByAccountIdForever(accountId, dbHelper);
                Note.deleteNotesForeverByAccountId(accountId, dbHelper);
                Account.deleteAccountByIdForever(accountId, dbHelper);
                return true;
            }
        });
    }

    public interface CallBackListener {
        public void callBack(Account account, Throwable e);
    }

    public void authorizeAccount(Account account, CallBackListener callBack) {
        new AccountAuthorizeTask(account, callBack).execute();
    }

    private class AccountAuthorizeTask extends AsyncTask<String, String, Throwable> {

        private CallBackListener callBack;
        private Account account;

        public AccountAuthorizeTask(Account account, CallBackListener callBack) {
            this.account = account;
            this.callBack = callBack;
        }

        @Override
        protected Throwable doInBackground(String... params) {
            ImapStoreClient imapStoreClient = new ImapStoreClient();
            try {

                if (account == null) {
                    return new IllegalStateException("Account can't be null");
                }
                imapStoreClient.checkSettings(account, application);
                return null;
            } catch (Exception e) {
                if (TextUtils.isEmpty(e.getMessage())) {
                    Log.e(TAG, "Unknow Exception", e);
                } else {
                    Log.e(TAG, e.getMessage(), e);
                }
                return e;

            }
        }

        @Override
        protected void onPostExecute(Throwable result) {
            callBack.callBack(account, result);
        }

    }

    /*************** Check the account of email sync ********************/
    public boolean isGmailAccount() {

        String imap = mAccount.imap;
        for (String googleImap : Constants.Google_MAIL_IMAP) {
            if (TextUtils.equals(imap, googleImap)) {
                return true;
            }
        }
        return false;
    }

    public boolean isGooglemailAccount() {
        String userName = getEmail().toLowerCase();
        return userName.endsWith(Constants.GOOGLE_MAIL);
    }
}
