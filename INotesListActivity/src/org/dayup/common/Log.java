/**
 * 
 */
package org.dayup.common;

/**
 * @author Nicky
 * 
 */
public abstract class Log {

    public static boolean IS_LOG_ENABLED = true;

    public static void i(String tag, String msg) {
        if (IS_LOG_ENABLED) {
            android.util.Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable e) {
        if (IS_LOG_ENABLED) {
            android.util.Log.i(tag, msg, e);
        }
    }

    public static void d(String tag, String msg) {
        if (IS_LOG_ENABLED) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable e) {
        android.util.Log.e(tag, msg, e);
    }

    public static void v(String tag, String msg) {
        if (IS_LOG_ENABLED) {
            android.util.Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg, Exception e) {
        android.util.Log.w(tag, msg, e);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(tag, msg);
    }

}
