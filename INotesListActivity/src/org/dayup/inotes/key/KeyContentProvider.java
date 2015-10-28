package org.dayup.inotes.key;

import org.dayup.decription.Utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.PreferenceObfuscator;
import com.android.vending.licensing.ServerManagedPolicy;

public class KeyContentProvider extends ContentProvider {
    private String tag = "kp";

    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhWr74a5q3D2ZMmPzctwe4Hwo3sK7dUbe/MgwmSsZIKfI3o+3OBDm6Q9Arvi03RRjI5GkplLPHeXoqMFb1/MkaKfmAGiW8LzA/QTCpdb3hbxkiAQKK6UH6PJ3J468agAnuDu8z9PWmuSE6ZeXsjw+e0qvZpVnrPedU46fqp4EcnS6EWd+C6hrgqVOVvKuaa82qxZOJIfe2S94qf5sYLbLMDvG+FWLQGWBHjznkAoqsmg+mOM6xYNDmdDcOhEB0rvwhsCWG38O30c6/f27QSW1J+YZZrtTGTU0yYbw1TrMvgpawARCDFI8QvAZikcpHKh6pnvm7SgBd0bH6GdmGyTzPQIDAQAB";
    private static final byte[] SALT = new byte[] {
            -42, 55, 30, -128, -103, -57, 74, -64, 21, 66, -95, -43, 77, -117, -36, -113, -11, 42,
            -64, 89
    };
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    private PreferenceObfuscator preferences;
    private final static String KEY_LT = "lt";
    private final static String KEY_EC = "ec";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        String deviceId = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
        SALT[2] = 34;
        AESObfuscator obf = new AESObfuscator(SALT, getContext().getPackageName(), deviceId);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences = new PreferenceObfuscator(sp, obf);
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        mChecker = new LicenseChecker(getContext(), new ServerManagedPolicy(getContext(), obf),
                BASE64_PUBLIC_KEY);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        MatrixCursor c = new MatrixCursor(new String[] {
                "KEY", "K2", "V", "LC", "S"
        });
        try {
            String lc = "1";
            String key1 = Utils.getDigest();
            String key2 = key1;
            if (selectionArgs != null && selectionArgs.length > 0 && selectionArgs[0].equals(key1)) {
                lc = getLastCheck();
                key2 = Utils.getDigest2(lc);
            }
            c.addRow(new Object[] {
                    key1, key2, getVersionCode(), lc, Secure.ANDROID_ID
            });
        } catch (Throwable e) {
            c.addRow(new Object[] {
                    "ERROR", "0", "0", Secure.ANDROID_ID
            });
        }
        doCheck();
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private void doCheck() {
        mChecker.checkAccess(mLicenseCheckerCallback);
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow() {
            Log.d(tag, "a");
            setLastCheck(true);
        }

        public void dontAllow() {
            Log.d(tag, "da");
            setLastCheck(false);
            // Intent intent = new
            // Intent(getContext(),KeyErrorDialogActivity.class);
            // getContext().startActivity(intent);
        }

        public void applicationError(ApplicationErrorCode errorCode) {
            Log.e(tag, "ae: " + errorCode);
            if (errorCode != ApplicationErrorCode.CHECK_IN_PROGRESS) {
                setLastCheck(false);
            }
        }
    }

    private int getVersionCode() {
        int code = 0;
        try {
            PackageInfo info = getContext().getPackageManager().getPackageInfo(
                    getContext().getPackageName(), 0);
            code = info.versionCode;
        } catch (Exception e) {
            Log.e(tag, e.getMessage());
        }
        return code;
    }

    private String getLastCheck() {
        return preferences.getString(KEY_LT, "0");
    }

    private void setLastCheck(boolean right) {
        boolean underLimited = true;
        if (right) {
            preferences.putString(KEY_EC, "0");
        } else {
            int ec = getEc();
            if (ec < 5) {
                preferences.putString(KEY_EC, "" + (ec + 1));
            } else {
                underLimited = false;
            }
        }

        long lt = System.currentTimeMillis();
        boolean b = lt % 2 == 0;
        long t = (right || underLimited) ? (b ? lt : lt + 1) : (b ? lt + 1 : lt);

        preferences.putString(KEY_LT, "" + t);
        preferences.commit();
    }

    private int getEc() {
        try {
            return Integer.parseInt(preferences.getString(KEY_EC, "0"));
        } catch (NumberFormatException e) {
            Log.e(tag, e.getMessage());
        }
        return 0;
    }

}
