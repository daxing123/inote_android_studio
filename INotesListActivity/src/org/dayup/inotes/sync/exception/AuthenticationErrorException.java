package org.dayup.inotes.sync.exception;

/**
 * @author chiron
 * 
 */
public class AuthenticationErrorException extends INotesSyncException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7135466631215128675L;

    public AuthenticationErrorException() {
        super();
    }

    public AuthenticationErrorException(String detailMessage) {
        super(detailMessage);
    }
}
