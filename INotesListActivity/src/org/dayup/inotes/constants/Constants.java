package org.dayup.inotes.constants;

import java.io.File;

import android.os.Environment;

public class Constants {

    public static final boolean TEST_MODE = false;

    public static class Themes {
        // 与资源文件中array_value值对应
        public static final int THEME_LIGHT = 0;
        public static final int THEME_BLACK = 1;
    }

    public static class DefaultAuthParams {
        public final static String IMAP = ImapServers.GMAIL_IMAP_SERVER;
        public final static String PORT_993 = "993";
        public final static String SECURITY_TYPE = "ssl";
        public final static String AUTH_TYPE = "PLAIN";
    }

    public static class ResultCode {
        public final static int NO_CHANGE = 200;
        public final static int NOTE_LOCAL_CHANGED = 201;
        public final static int RESET_AUTH = 202;
    }

    public static class RequestCode {
        public static final int NORMAL_START = 0;
        public static final int SET_PREFERENCE = 1001;
        public static final int NOTE_EDIT = 1002;
        public static final int REQUEST_CODE_VOICE_RECOGNITION = 1003;
    }

    public static class SyncMode {
        public final static int LOCAL_CHANGED = 1;
        public final static int ALL = 2;
        public final static int MANUAL = 3;
    }

    public static class AppFiles {
        public static File INOTES_DIR = new File(Environment.getExternalStorageDirectory(),
                "/.iNotes/");
        public static File INOTES_TMP_FILE_DIR = new File(
                Environment.getExternalStorageDirectory(), "/.iNotes/tmp/");
    }

    public static final String GOOGLE_MAIL = "googlemail.com";

    public static final String[] Google_MAIL_IMAP = new String[] {
            "imap.gmail.com", "imap.googlemail.com"
    };

    public static class ImapParameters {
        public static final String TRASH_FOLDER = "Trash";
        public static final String TRASH_FOLDER_QQ = "INBOX";
        public static final String TRASH_FOLDER_GMAIL = "[Gmail]/Trash";
        public static final String TRASH_FOLDER_GOOGLE_MAIL = "[Google Mail]/Trash";
    }

    public static class ImapServers {
        public static final String GMAIL_IMAP_SERVER = "imap.gmail.com";
        public static final String YAHOO_IMAP_SERVER = "imap.mail.yahoo.com";
        public static final String AOL_IMAP_SERVER = "imap.aol.com";
    }

    public static class SyncStatus {
        public final static int SUCCESS = 1;
        public final static int SYNCING = 2;
        public final static int ERROR = 3;
        public final static int TOKEN_TIMEOUT = 4;
    }
}
