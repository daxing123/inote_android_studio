package org.dayup.inotes.utils;

import java.text.SimpleDateFormat;

import org.dayup.common.Log;

import android.content.Context;

public class ConvertUtils {
    private static final String TAG = ConvertUtils.class.getSimpleName();

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int byte2int(byte b[]) {
        return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
    }

    public static byte[] int2byte(int n) {
        byte[] buf = new byte[4];
        buf[0] = (byte) (n >> 24);
        buf[1] = (byte) (n >> 16);
        buf[2] = (byte) (n >> 8);
        buf[3] = (byte) n;
        return buf;
    }

    public static int string2Int(String str) {
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static long string2Long(String str, long def) {

        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return def;
    }

    public static long remoteTime2LocalTime(String remoteTime) {
        try {
            return Long.parseLong(remoteTime);
        } catch (NumberFormatException e) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            return DateUtils.parse2longUTC(remoteTime, sdf);
        }
    }
}
