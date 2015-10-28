package org.dayup.inotes.setup;

import org.dayup.common.Analytics;
import org.dayup.common.Log;
import org.dayup.inotes.R;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.account.INotesAccountManager.CallBackListener;
import org.dayup.inotes.constants.Constants.ResultCode;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.views.INotesProgressDialog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class AccountSetupAccountTypeActivity extends AccountSetupBaseActivity implements
        OnClickListener {
    private INotesAccountManager accountManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_account_type);
        accountManager = iNotesApplication.getAccountManager();
        initView();
    }

    private void initView() {
        findViewById(R.id.google).setOnClickListener(this);
        findViewById(R.id.imap).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.google:
            onGoogle();
            break;
        case R.id.imap:
            onImap();
            break;
        }

    }

    private void onImap() {
        startAccountSetupIncomingActivity();
    }

    private void onGoogle() {
        progressDialog = new INotesProgressDialog(this, iNotesApplication.getThemeType());
        progressDialog.show();
        accountManager
                .authorizeAccount(
                        Account.getGMailAccount(SetupData.getUsername(), SetupData.getPassword()),
                        callBack);
    }

    private INotesAccountManager.CallBackListener callBack = new CallBackListener() {

        @Override
        public void callBack(Account account, Throwable result) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result == null) {
                handleAuthTokenSuccess(account);
                startAccountLoginSuccessActivity();
            } else {
                showErrorDialog(AccountSetupAccountTypeActivity.this, result);
            }

        }
    };

    private void handleAuthTokenSuccess(Account account) {
        accountManager.insertActiveAccount(account);
        iNotesApplication.stopSynchronize();
        iNotesApplication.setResultCode(ResultCode.RESET_AUTH);
        iNotesApplication.resetSyncManager();
    }

    private void startAccountLoginSuccessActivity() {
        Intent i = new Intent(AccountSetupAccountTypeActivity.this,
                AccountLoginSuccessActivity.class);
        startActivity(i);
    }

    private void startAccountSetupIncomingActivity() {
        Intent i = new Intent(AccountSetupAccountTypeActivity.this,
                AccountSetupIncomingActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(this);
        } catch (Exception e) {
            Log.d("AccountSetupAccountTypeActivity", e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.endFlurry(this);
    }
}
