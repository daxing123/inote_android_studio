package org.dayup.inotes.db;

public interface Field {
    public static class Status {

        public final static int SYNC_NEW = 0;
        public final static int SYNC_UPDATE = 1;
        public final static int SYNC_DONE = 2;

        public final static int DELETED_YES = 1;
        public final static int DELETED_NO = 0;

        public static final int ACCOUNT_ACTIVITY_ACTIVE = 1;
        public static final int ACCOUNT_ACTIVITY_FREEZE = 2;

        public static final int LOCAL_MODE_ACCOUNT_ID = 0;
    }

    String name();

    int index();

    String type();
}
