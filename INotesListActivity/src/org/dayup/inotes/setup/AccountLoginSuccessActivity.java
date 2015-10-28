package org.dayup.inotes.setup;

import android.support.v7.app.ActionBar;
import org.dayup.activities.BaseActivity;
import org.dayup.common.Analytics;
import org.dayup.common.Log;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.INotesPreferences;
import org.dayup.inotes.R;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.data.Note;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class AccountLoginSuccessActivity extends BaseActivity {

    private Account successAccount;
    private INotesApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (INotesApplication) getApplication();
        setContentView(R.layout.account_login_success_layout);

        successAccount = application.getAccountManager().getAccount();
        ((TextView) findViewById(R.id.account_success_account)).setText(successAccount.email);
        ((Button) findViewById(R.id.done_button)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                doDone();
            }
        });
        initActionBar();
    }

    private void initActionBar() {
        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(false);
        bar.setTitle(R.string.sign_in_success);

    }

    private void doDone() {
        mergeLocalDataToCurrentAccount();

        goBackToINotesPreferences();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBackToINotesPreferences();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void mergeLocalDataToCurrentAccount() {
        // 由于没有folder的新增操作，需要合并的数据只有notes
        Note.mergeLocalModeNote2CurrentAccount(successAccount.id, dbHelper);
    }

    private void goBackToINotesPreferences() {
        Intent intent = new Intent(AccountLoginSuccessActivity.this, INotesPreferences.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(this);
        } catch (Exception e) {
            Log.d("AccountLoginSuccessActivity", e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.endFlurry(this);
    }
}
