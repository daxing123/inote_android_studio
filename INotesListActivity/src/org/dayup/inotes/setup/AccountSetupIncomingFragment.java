package org.dayup.inotes.setup;

import android.app.Fragment;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.R;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.account.INotesAccountManager.CallBackListener;
import org.dayup.inotes.constants.Constants.DefaultAuthParams;
import org.dayup.inotes.constants.Constants.ResultCode;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.utils.ViewUtils;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class AccountSetupIncomingFragment extends Fragment {
    private AccountSetupIncomingActivity mActivity;
    private INotesApplication application;
    private EditText mUsernameView, mPasswordView, mServerView, mPortView;
    private Spinner mSecurityTypeView;
    private ProgressDialog progressDialog;

    public static final int USER_CONFIG_MASK = 0x0b;

    private static ConnectionSecurity[] SECURITY_VALUES = {
            ConnectionSecurity.NONE, ConnectionSecurity.SSL_TLS_OPTIONAL,
            ConnectionSecurity.SSL_TLS_REQUIRED, ConnectionSecurity.STARTTLS_OPTIONAL,
            ConnectionSecurity.STARTTLS_REQUIRED
    };

    private static int[] IMAP_POARS = {
            143, 993, 993, 143, 143
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (AccountSetupIncomingActivity) getActivity();
        application = (INotesApplication) mActivity.getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_setup_incoming_fragment, container, true);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mUsernameView = (EditText) view.findViewById(R.id.account_username);
        mPasswordView = (EditText) view.findViewById(R.id.account_password);
        mServerView = (EditText) view.findViewById(R.id.account_server);
        mPortView = (EditText) view.findViewById(R.id.account_port);
        mSecurityTypeView = (Spinner) view.findViewById(R.id.account_security_type);
        mUsernameView.addTextChangedListener(validationTextWatcher);
        mPasswordView.addTextChangedListener(validationTextWatcher);
        mServerView.addTextChangedListener(validationTextWatcher);
        mPortView.addTextChangedListener(validationTextWatcher);

        SpinnerOption securityTypes[] = {
                new SpinnerOption(ConnectionSecurity.NONE.ordinal(),
                        getString(R.string.account_setup_incoming_security_none_label)),
                new SpinnerOption(ConnectionSecurity.SSL_TLS_OPTIONAL.ordinal(),
                        getString(R.string.account_setup_incoming_security_ssl_optional_label)),
                new SpinnerOption(ConnectionSecurity.SSL_TLS_REQUIRED.ordinal(),
                        getString(R.string.account_setup_incoming_security_ssl_label)),
                new SpinnerOption(ConnectionSecurity.STARTTLS_OPTIONAL.ordinal(),
                        getString(R.string.account_setup_incoming_security_tls_optional_label)),
                new SpinnerOption(ConnectionSecurity.STARTTLS_REQUIRED.ordinal(),
                        getString(R.string.account_setup_incoming_security_tls_label)),
        };

        ArrayAdapter<SpinnerOption> securityTypesAdapter = new ArrayAdapter<SpinnerOption>(
                mActivity, android.R.layout.simple_spinner_item, securityTypes);
        securityTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSecurityTypeView.setAdapter(securityTypesAdapter);

        mSecurityTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                updatePortFromSecurityType();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mUsernameView.setText(SetupData.getUsername());
        mPasswordView.setText(SetupData.getPassword());
        mServerView.setText(SetupData.getImapServer());
        SpinnerOption.setSpinnerOptionValue(mSecurityTypeView, SetupData.getDefaultSecurityType());
        mPortView.setText(getPortFromSecurityType());
    }

    private void updatePortFromSecurityType() {
        String port = getPortFromSecurityType();
        mPortView.setText(port);
    }

    private String getPortFromSecurityType() {
        int securityType = (Integer) ((SpinnerOption) mSecurityTypeView.getSelectedItem()).value;
        return IMAP_POARS[securityType] + "";
    }

    TextWatcher validationTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            validateFields();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        validateFields();
    }

    private void validateFields() {
        boolean enabled = ViewUtils.isTextViewNotEmpty(mUsernameView)
                && ViewUtils.isTextViewNotEmpty(mPasswordView)
                && ViewUtils.isServerNameValid(mServerView)
                && ViewUtils.isPortFieldValid(mPortView);
        mActivity.onEnableProceedButtons(enabled);
        // Warn (but don't prevent) if password has leading/trailing spaces
        checkPasswordSpaces(mPasswordView);
    }

    private void checkPasswordSpaces(EditText passwordField) {
        Editable password = passwordField.getText();
        int length = password.length();
        if (length > 0) {
            if (password.charAt(0) == ' ' || password.charAt(length - 1) == ' ') {
                passwordField.setError(getString(R.string.account_password_spaces_error));
            }
        }
    }

    public void onNext() {
        plainAuthorize();
    }

    public void plainAuthorize() {
        progressDialog = new ProgressDialog(mActivity);
        //progressDialog.setTitle(R.string.account_authorizing);
        //progressDialog.setMessage(getString(R.string.text_login_wait));
        progressDialog.setMessage(getString(R.string.account_authorizing));
        progressDialog.show();
        Account account = new Account();
        account.email = mUsernameView.getText().toString();
        account.password = mPasswordView.getText().toString();
        SetupData.setUsername(account.email);
        SetupData.setPassword(account.password);
        account.imap = mServerView.getText().toString().trim();
        account.port = getServerPort();
        account.securityType = getSecurityType();
        account.authType = DefaultAuthParams.AUTH_TYPE;
        application.getAccountManager().authorizeAccount(account, callBack);

    }

    private String getSecurityType() {
        int securityType = (Integer) ((SpinnerOption) mSecurityTypeView.getSelectedItem()).value;
        String security = "";
        switch (SECURITY_VALUES[securityType]) {
        case SSL_TLS_OPTIONAL:
            security = "ssl";
            break;
        case SSL_TLS_REQUIRED:
            security = "ssl+";
            break;
        case STARTTLS_OPTIONAL:
            security = "tls";
            break;
        case STARTTLS_REQUIRED:
            security = "tls+";
            break;
        case NONE:
            security = "";
            break;
        }
        return security;
    }

    private String getServerPort() {
        String serverPort = mPortView.getText().toString().trim();
        if (TextUtils.isEmpty(serverPort)) {
            serverPort = getPortFromSecurityType();
        }
        return serverPort;
    }

    private INotesAccountManager.CallBackListener callBack = new CallBackListener() {

        @Override
        public void callBack(Account account, Throwable result) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result == null) {
                handleAuthTokenSuccess(account);
                mActivity.startAccountLoginSuccessActivity();
            } else {
                mActivity.showErrorDialog(mActivity, result);
            }

        }
    };

    private void handleAuthTokenSuccess(Account account) {
        application.getAccountManager().insertActiveAccount(account);
        application.stopSynchronize();
        application.setResultCode(ResultCode.RESET_AUTH);
        application.resetSyncManager();
    }
}
