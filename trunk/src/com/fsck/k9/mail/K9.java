package com.fsck.k9.mail;

import android.app.Application;

import com.fsck.k9.mail.internet.DecoderUtil;

public class K9 {

    private static final String TAG = DecoderUtil.class.getSimpleName();
    /**
     * Some log messages can be sent to a file, so that the logs can be read
     * using unprivileged access (eg. Terminal Emulator) on the phone, without
     * adb. Set to null to disable
     */
    public static final String logFile = null;
    // public static final String logFile =
    // Environment.getExternalStorageDirectory() + "/k9mail/debug.log";

    /**
     * If this is enabled, various development settings will be enabled It
     * should NEVER be on for Market builds Right now, it just governs
     * strictmode
     **/
    public static boolean DEVELOPER_MODE = true;

    public static final String IDENTITY_HEADER = "X-K9mail-Identity";

    public static Application app = null;
    /**
     * If this is enabled there will be additional logging information sent to
     * Log.d, including protocol dumps. Controlled by Preferences at run-time
     */
    public static boolean DEBUG = false;

    /**
     * Should K-9 log the conversation it has over the wire with SMTP servers?
     */

    public static boolean DEBUG_PROTOCOL_SMTP = true;

    /**
     * Should K-9 log the conversation it has over the wire with IMAP servers?
     */

    public static boolean DEBUG_PROTOCOL_IMAP = true;

    /**
     * Should K-9 log the conversation it has over the wire with POP3 servers?
     */

    public static boolean DEBUG_PROTOCOL_POP3 = true;

    /**
     * Should K-9 log the conversation it has over the wire with WebDAV servers?
     */

    public static boolean DEBUG_PROTOCOL_WEBDAV = true;

    public static final String LOG_TAG = "k9";

    /**
     * If this is enabled than logging that normally hides sensitive information
     * like passwords will show that information.
     */
    public static boolean DEBUG_SENSITIVE = false;

    /**
     * Max time (in millis) the wake lock will be held for when background sync
     * is happening
     */
    public static final int WAKE_LOCK_TIMEOUT = 600000;

    public static final int MANUAL_WAKE_LOCK_TIMEOUT = 120000;

    public static final int PUSH_WAKE_LOCK_TIMEOUT = 60000;

    public static final int MAIL_SERVICE_WAKE_LOCK_TIMEOUT = 30000;

    public static final int BOOT_RECEIVER_WAKE_LOCK_TIMEOUT = 60000;

    /**
     * Time the LED is on/off when blinking on new email notification
     */
    public static final int NOTIFICATION_LED_ON_TIME = 500;
    public static final int NOTIFICATION_LED_OFF_TIME = 2000;

    public static final boolean NOTIFICATION_LED_WHILE_SYNCING = false;
    public static final int NOTIFICATION_LED_FAST_ON_TIME = 100;
    public static final int NOTIFICATION_LED_FAST_OFF_TIME = 100;

    public static final int NOTIFICATION_LED_BLINK_SLOW = 0;
    public static final int NOTIFICATION_LED_BLINK_FAST = 1;

    public static final int NOTIFICATION_LED_SENDING_FAILURE_COLOR = 0xffff0000;

    // Must not conflict with an account number
    public static final int FETCHING_EMAIL_NOTIFICATION = -5000;
    public static final int SEND_FAILED_NOTIFICATION = -1500;
    public static final int CONNECTIVITY_ID = -3;

}
