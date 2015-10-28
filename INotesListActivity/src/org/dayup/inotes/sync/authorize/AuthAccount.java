package org.dayup.inotes.sync.authorize;

import java.net.URLEncoder;

import com.fsck.k9.Account;

public class AuthAccount extends Account {

    private org.dayup.inotes.data.Account mAccount;

    public AuthAccount(org.dayup.inotes.data.Account account) {
        super();
        this.mAccount = account;
    }

    @Override
    public synchronized String getStoreUri() {
        return getStoreUriLocal();
    }

    private String getStoreUriLocal() {
        // imap+ssl+://auth:user:password@server:port
        String userName = encode(mAccount.email.toLowerCase().trim());
        String authToken = encode(mAccount.password).replace("+", "%20");
        return String.format("imap%s://%s:%s@%s", "+" + mAccount.securityType, mAccount.authType
                + ":" + userName, authToken, mAccount.imap + ":" + mAccount.port);
    }

    private String encode(String paramString) {
        if (paramString == null) {
            return null;
        } else {
            return URLEncoder.encode(paramString);
        }
    }

}
