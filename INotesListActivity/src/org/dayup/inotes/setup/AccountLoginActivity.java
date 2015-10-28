package org.dayup.inotes.setup;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dayup.common.Analytics;
import org.dayup.common.Log;
import org.dayup.inotes.R;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.account.INotesAccountManager.CallBackListener;
import org.dayup.inotes.constants.Constants.ResultCode;
import org.dayup.inotes.data.Account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.fsck.k9.mail.AuthenticationFailedException;
import com.fsck.k9.mail.MessagingException;

public class AccountLoginActivity extends AccountSetupBaseActivity implements TextWatcher {
    private EditText editUsername, editPasword;
    private ProgressBar progress;
    private ImageView warningImage;
    private TextView warningText;
    private Button mManualButton, btnConfirm;
    private CheckBox checkShowPsd;
    private INotesAccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountManager = iNotesApplication.getAccountManager();
        setContentView(R.layout.account_login_layout);
        init();
        setActionBarTitle(R.string.account_sigin_in);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        editUsername = (EditText) findViewById(R.id.account_authen_edit_username);
        editPasword = (EditText) findViewById(R.id.account_authen_edit_password);
        editPasword.setTypeface(Typeface.MONOSPACE);
        checkShowPsd = (CheckBox) findViewById(R.id.login_show_pwd);
        progress = (ProgressBar) findViewById(R.id.account_authen_progress);
        warningImage = (ImageView) findViewById(R.id.account_authen_warning_image);
        warningText = (TextView) findViewById(R.id.account_authen_warning_text);
        mManualButton = (Button) findViewById(R.id.account_authen_manual);
        btnConfirm = (Button) findViewById(R.id.account_authen_confirm);
        mManualButton.setOnClickListener(new BtnManualBtnOnClick());
        btnConfirm.setOnClickListener(new BtnSetupNextOnClick());
        editUsername.addTextChangedListener(this);
        editPasword.addTextChangedListener(this);
        checkShowPsd.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showPsd(isChecked, editPasword);
            }

        });
        editPasword.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String username = editUsername.getText().toString();
                    username = setDefaultMail(username);
                    validateFields();
                }

            }
        });
        onEnableProceedButtons(false);
    }

    private void onEnableProceedButtons(boolean enabled) {
        btnConfirm.setEnabled(enabled);
        mManualButton.setEnabled(enabled);
    }

    private void showPsd(boolean isChecked, EditText editPasword) {
        if (isChecked) {
            editPasword.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editPasword.setTypeface(Typeface.MONOSPACE);
        } else {
            editPasword.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editPasword.setTypeface(Typeface.MONOSPACE);
        }
        int index = editPasword.getText() == null ? 0 : editPasword.getText().length();
        editPasword.setSelection(index);
    }

    private boolean isEmailValid(String username) {
        return EmailFormat(username) && notContain(username);
    }

    private boolean notContain(String email) {
        List<Account> accounts = Account.getAllAccounts(null, null, null,
                iNotesApplication.getDBHelper());
        for (Account account : accounts) {
            if (email.equals(account.email)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIsNotEmpty(String username, String passwd) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(passwd)) {
            return false;
        }
        return true;

    }

    private boolean EmailFormat(String email) {// 邮箱判断正则表达式
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher mc = pattern.matcher(email);
        return mc.matches();
    }

    private String setDefaultMail(String input) {
        boolean flag = false;
        String username = input;
        char[] chars = input.toCharArray();
        char a = '@';

        if (TextUtils.isEmpty(username)) {
            return username;
        }

        for (char b : chars) {
            if (a == b) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            username += "@gmail.com";
            editUsername.setText(username);
        }
        return username;
    }

    class BtnManualBtnOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            SetupData.setUsername(editUsername.getText().toString());
            SetupData.setPassword(editPasword.getText().toString().trim());
            SetupData.setProvider(findProviderForDomain(SetupData.getDomain()));
            startAccountSetUpAccountTypeActivity();
        }
    }

    class BtnSetupNextOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            String email = editUsername.getText().toString();
            String password = editPasword.getText().toString().trim();
            SetupData.setUsername(email);
            SetupData.setPassword(password);
            SetupData.setProvider(findProviderForDomain(SetupData.getDomain()));
            if (SetupData.getProvider() != null) {
                plainAuthorize(email, password);
            } else {
                startAccountSetUpAccountTypeActivity();
            }

        }
    }

    private void startAccountLoginSuccessActivity() {
        Intent i = new Intent(AccountLoginActivity.this, AccountLoginSuccessActivity.class);
        startActivity(i);
    }

    public void startAccountSetUpAccountTypeActivity() {
        Intent i = new Intent(AccountLoginActivity.this, AccountSetupAccountTypeActivity.class);
        startActivity(i);
    }

    public void plainAuthorize(String email, String password) {
        beforeAuthorizeTaskUIDisplay();
        accountManager.authorizeAccount(SetupData.getAuthorizeAccount(), callBack);
    }

    private void beforeAuthorizeTaskUIDisplay() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(editPasword.getWindowToken(), 0);
        progress.setVisibility(View.VISIBLE);
        warningText.setVisibility(View.VISIBLE);
        warningText.setText(R.string.text_login_wait);
        warningImage.setVisibility(View.GONE);
        onEnableProceedButtons(false);
    }

    private INotesAccountManager.CallBackListener callBack = new CallBackListener() {

        @Override
        public void callBack(Account account, Throwable result) {
            progress.setVisibility(View.GONE);
            onEnableProceedButtons(true);
            if (result == null) {
                handleAuthTokenSuccess(account);
                warningText.setVisibility(View.GONE);
                startAccountLoginSuccessActivity();
            } else {
                warningImage.setVisibility(View.VISIBLE);
                if (result instanceof AuthenticationFailedException) {
                    warningText.setText(R.string.text_username_password_incorrect);
                } else if (result instanceof MessagingException) {
                    warningText.setText(result.getMessage());
                } else {
                    warningText.setText(R.string.text_login_failed);
                }
                showErrorDialog(AccountLoginActivity.this, result);

            }

        }
    };

    private void handleAuthTokenSuccess(Account account) {
        accountManager.insertActiveAccount(account);
        iNotesApplication.stopSynchronize();
        iNotesApplication.setResultCode(ResultCode.RESET_AUTH);
        iNotesApplication.resetSyncManager();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        validateFields();
    }

    private void validateFields() {
        String email = editUsername.getText().toString();
        String password = editPasword.getText().toString().trim();
        onEnableProceedButtons(checkIsNotEmpty(email, password) && isEmailValid(email));
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(this);
        } catch (Exception e) {
            Log.d("AccountLoginActivity", e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.endFlurry(this);
    }
}
