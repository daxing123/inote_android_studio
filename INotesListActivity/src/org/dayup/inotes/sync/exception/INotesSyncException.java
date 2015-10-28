package org.dayup.inotes.sync.exception;

public class INotesSyncException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public INotesSyncException() {
        super();
    }

    public INotesSyncException(String detailMessage) {
        super(detailMessage);
    }
}
