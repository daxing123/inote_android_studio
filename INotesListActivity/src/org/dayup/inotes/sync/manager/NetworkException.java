package org.dayup.inotes.sync.manager;

import org.dayup.inotes.sync.exception.INotesSyncException;

public class NetworkException extends INotesSyncException {
    /**
	 * 
	 */
    private static final long serialVersionUID = -8509957540943328464L;

    public NetworkException() {
        super();
    }

    public NetworkException(String detailMessage) {
        super(detailMessage);
    }
}
