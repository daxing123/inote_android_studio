package org.dayup.inotes.setup;

import org.dayup.inotes.constants.Constants.DefaultAuthParams;
import org.dayup.inotes.data.Account;

import android.text.TextUtils;

public class SetupData {

    private String mUsername;
    private String mPassword;
    private Provider mProvider;

    private static SetupData INSTANCE = null;
    public static final String IMAP_PORT_NORMAL = "143";
    public static final String IMAP_PORT_SSL = "993";

    public static synchronized SetupData getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SetupData();
        }
        return INSTANCE;
    }

    private SetupData() {
    }

    public static void setProvider(Provider provider) {
        getInstance().mProvider = provider;
    }

    public static Provider getProvider() {
        return getInstance().mProvider;
    }

    public static void setUsername(String username) {
        getInstance().mUsername = username;
    }

    public static String getUsername() {
        return getInstance().mUsername;
    }

    public static void setPassword(String password) {
        getInstance().mPassword = password;
    }

    public static String getPassword() {
        return getInstance().mPassword;
    }

    public static boolean isSupportEmail() {
        String email = getInstance().mUsername;
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        return email.endsWith("@gmail.com");

    }

    public static String getDomain() {
        String username = getInstance().mUsername;
        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("username can't be null");
        }
        String[] tmp = username.split("@");

        return tmp[tmp.length - 1];
    }

    public static String getImapServer() {
        if (getInstance().mProvider != null) {
            return getInstance().mProvider.server;
        } else {
            String username = getInstance().mUsername;
            if (TextUtils.isEmpty(username)) {
                throw new IllegalArgumentException("username can't be null");
            }
            String[] tmp = username.split("@");

            return "imap." + tmp[tmp.length - 1];
        }
    }

    public static int getDefaultSecurityType() {
        if (getInstance().mProvider != null) {
            // TODO 需要根据实际提供的来决定
            return ConnectionSecurity.SSL_TLS_OPTIONAL.ordinal();
        } else {
            return ConnectionSecurity.NONE.ordinal();
        }
    }

    public static Account getAuthorizeAccount() {
        Provider provider = getProvider();
        if (provider == null) {
            return null;
        }
        Account account = new Account();
        account.email = getUsername();
        account.password = getPassword();
        account.imap = provider.server;
        account.port = DefaultAuthParams.PORT_993;
        account.securityType = provider.security;
        account.authType = DefaultAuthParams.AUTH_TYPE;
        return account;
    }
}
