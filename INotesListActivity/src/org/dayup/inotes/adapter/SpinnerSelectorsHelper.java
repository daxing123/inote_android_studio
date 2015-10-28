package org.dayup.inotes.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.R;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.data.Folder;
import org.dayup.inotes.db.Field.Status;

import android.content.Context;

public class SpinnerSelectorsHelper {

    private INotesApplication application;
    private INotesAccountManager accountManager;

    public SpinnerSelectorsHelper(Context context) {
        this.application = (INotesApplication) context.getApplicationContext();
        this.accountManager = application.getAccountManager();
    }

    public ArrayList<SpinnerSelecter> getSelectors() {
        ArrayList<SpinnerSelecter> selectors = new ArrayList<SpinnerSelecter>();
        setHeaderLabelSelectors(selectors);

        setFoldersSelectors(selectors);

        setAccountsSelectors(selectors);
        return selectors;
    }

    private void setFoldersSelectors(ArrayList<SpinnerSelecter> selectors) {
        List<Folder> folders = Folder.getAllFoldersWithDisplayNameByAccountId(
                accountManager.getAccountId(), null, null, null, application.getDBHelper());
        if (folders.size() != 1) {
            setFolderAllSelectors(selectors);
        }
        setFolderSelectors(folders, selectors);
    }

    private void setAccountsSelectors(ArrayList<SpinnerSelecter> selectors) {
        List<Account> accounts = Account
                .getAllAccounts(null, null, null, application.getDBHelper());
        if (accounts.size() > 1) {
            setMiddleLabelSelectors(selectors);
            setAccountSelectors(selectors, accounts);
        }
    }

    private void setHeaderLabelSelectors(ArrayList<SpinnerSelecter> selectors) {
        SpinnerSelecter header = new SpinnerSelecter();
        header.id = SpinnerSelecter.NO_ID;
        header.type = SpinnerSelecter.LABEL_TYPE_HEADER;
        header.displayName = accountManager.isLocalMode() ? application.getResources().getString(
                R.string.local_mode) : accountManager.getEmail();
        selectors.add(header);
    }

    private void setMiddleLabelSelectors(ArrayList<SpinnerSelecter> selectors) {
        SpinnerSelecter middle = new SpinnerSelecter();
        middle.id = SpinnerSelecter.NO_ID;
        middle.type = SpinnerSelecter.LABEL_TYPE_MIDDLE;
        middle.displayName = application.getResources().getString(R.string.account);
        selectors.add(middle);
    }

    private void setFolderAllSelectors(ArrayList<SpinnerSelecter> selectors) {
        SpinnerSelecter allFolder = new SpinnerSelecter();
        allFolder.id = Folder.ALL_FOLDER_ID;
        allFolder.type = SpinnerSelecter.TYPE_FOLDER;
        allFolder.displayName = application.getResources().getString(R.string.folder_all);
        selectors.add(allFolder);
    }

    private void setFolderSelectors(List<Folder> folders, ArrayList<SpinnerSelecter> selectors) {
        // TODO 显示顺序与GMail一致
        Collections.sort(folders, Folder.folderComparatorPosition);
        for (Folder folder : folders) {
            SpinnerSelecter selector = new SpinnerSelecter();
            selector.id = folder.id;
            selector.displayName = folder.displayName;
            selector.type = SpinnerSelecter.TYPE_FOLDER;
            selectors.add(selector);
        }
    }

    private void setAccountSelectors(ArrayList<SpinnerSelecter> selectors, List<Account> accounts) {
        if (accounts.size() == 0) {
            SpinnerSelecter selector = new SpinnerSelecter();
            selector.id = Status.LOCAL_MODE_ACCOUNT_ID;
            selector.displayName = application.getResources().getString(R.string.local_mode);
            selector.type = SpinnerSelecter.TYPE_ACCOUNT;
            selectors.add(selector);
        }

        for (Account account : accounts) {
            SpinnerSelecter selector = new SpinnerSelecter();
            selector.id = account.id;
            selector.displayName = account.email;
            selector.type = SpinnerSelecter.TYPE_ACCOUNT;
            selectors.add(selector);
        }

    }

}
