package org.dayup.inotes.setup;

import com.fsck.k9.mail.Store;
import com.fsck.k9.mail.Transport;

/**
 * The currently available connection security types.
 * 
 * <p>
 * Right now this enum is only used by {@link ServerSettings} and converted to
 * store- or transport-specific constants in the different {@link Store} and
 * {@link Transport} implementations. In the future we probably want to change
 * this and use {@code ConnectionSecurity} exclusively.
 * </p>
 */
public enum ConnectionSecurity {
    NONE, SSL_TLS_OPTIONAL, SSL_TLS_REQUIRED, STARTTLS_OPTIONAL, STARTTLS_REQUIRED
}
