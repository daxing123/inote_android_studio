package org.dayup.inotes;

import java.security.NoSuchAlgorithmException;

import org.dayup.activities.CommonApplication;
import org.dayup.common.Log;
import org.dayup.common.Mi;
import org.dayup.inotes.INotesPreferences.PK;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.constants.Constants.AppFiles;
import org.dayup.inotes.constants.Constants.ResultCode;
import org.dayup.inotes.constants.Constants.SyncMode;
import org.dayup.inotes.constants.Constants.Themes;
import org.dayup.inotes.db.INotesDBHelper;
import org.dayup.inotes.sync.exception.AuthenticationErrorException;
import org.dayup.inotes.sync.manager.SyncEmailManager;
import org.dayup.inotes.sync.manager.SyncManager;
import org.dayup.inotes.utils.ConvertUtils;
import org.dayup.inotes.utils.StringUtils;
import org.dayup.tasks.BackgroundTaskManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author Nicky
 * 
 */
public class INotesApplication extends CommonApplication implements
        OnSharedPreferenceChangeListener {

    private static final String TAG = INotesApplication.class.getSimpleName();

    private final INotesDBHelper dbHelper = new INotesDBHelper(this);
    private BackgroundTaskManager backgroundTaskManager;

    private SharedPreferences settings;
    private INotesAccountManager accountManager;
    private SyncManager notesSyncManager = null;

    private int themeType = Themes.THEME_LIGHT;
    private int themeTmp;

    private static int TARGET_HEIGHT = 800;
    private static int TARGET_WIDTH = 480;

    private boolean manualSync = false;
    private String versionCode = null;
    private String versionName = null;
    private Mi mi;

    public Mi getMi() {
        return mi;
    }

    private int mResultCode = ResultCode.NO_CHANGE;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper.getWritableDatabase();
        mi = ck(this);
        accountManager = new INotesAccountManager(this);
        accountManager.init();
        initMkDirs();
        initSetting();
        initTargetHeightWidth();
        registerNetWorkState();
        backgroundTaskManager = new BackgroundTaskManager(30000);
        resetSyncManager();
    }

    private static final String CONTENT_KEY = "content://org.dayup.inote.key/key";

    public static Mi ck(Context context) {
        Mi mi = new Mi();
        String key1 = "";
        try {
            key1 = org.dayup.decription.Utils.getDigest();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse(CONTENT_KEY), null, null,
                    new String[] {
                        key1
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                int cols = cursor.getColumnCount();
                if (cols == 1) {// key.version = 1
                    mi.setV(1);
                    mi.setLc(System.currentTimeMillis());
                    mi.setIrk(key1.equals(cursor.getString(0)));
                } else {
                    mi.setLc(cursor.getLong(3));
                    mi.setV(cursor.getInt(2));
                    mi.setK2(cursor.getString(1));
                    mi.setK1(cursor.getString(0));
                    try {
                        mi.setIrk(key1.equals(mi.getK1())
                                && org.dayup.decription.Utils.getDigest2("" + mi.getLc()).equals(
                                        mi.getK2()) && mi.getLc() % 2 == 0);
                    } catch (NoSuchAlgorithmException e) {
                        mi.setIrk(false);
                        Log.e(TAG, e.getMessage());
                    }
                }
            } else {
                return null;
            }
        } catch (SecurityException se) {
            mi.setIrk(false);
            Toast.makeText(context,
                    "Invalid app apk, please install/update it from Android Market.",
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, se.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mi;
    }

    public INotesAccountManager getAccountManager() {
        return accountManager;
    }

    public INotesDBHelper getDBHelper() {
        return this.dbHelper;
    }

    public BackgroundTaskManager getBackgroundTaskManager() {
        return backgroundTaskManager;
    }

    private void initMkDirs() {
        AppFiles.INOTES_DIR.mkdirs();
        AppFiles.INOTES_TMP_FILE_DIR.mkdirs();
    }

    private void initSetting() {
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(this);
        manualSync = settings.getBoolean(PK.MANUAL_SYNC, false);
        themeType = StringUtils.parseInt(settings.getString(PK.THEME_KEY, ""), Themes.THEME_LIGHT);
        themeTmp = themeType;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, PK.MANUAL_SYNC)) {
            manualSync = sharedPreferences.getBoolean(PK.MANUAL_SYNC, true);
        } else if (TextUtils.equals(key, PK.THEME_KEY)) {
            themeType = StringUtils.parseInt(sharedPreferences.getString(PK.THEME_KEY, ""),
                    Themes.THEME_LIGHT);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
        backgroundTaskManager.shutdown();
        dbHelper.close();
    }

    @Override
    protected void initCommunication() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initDialogNotes() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getOptionMenuHelpStringId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public void setResultCode(int resultCode) {
        this.mResultCode = resultCode;
    }

    public void resetSyncManager() {
        notesSyncManager = getSyncMangerInstance();
        try {
            if (notesSyncManager != null) {
                notesSyncManager.instance(this);
            }
        } catch (AuthenticationErrorException e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(getApplicationContext(), R.string.preferences_authorize_faild,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get SyncManager instance, if Local_mode is on,return null;
     * 
     * @return
     */
    private SyncManager getSyncMangerInstance() {
        SyncManager instance = null;
        synchronized (SyncManager.class) {
            if (instance == null) {
                instance = new SyncEmailManager();
            }
        }
        return instance;
    }

    public SyncManager getSyncManager() {
        if (notesSyncManager == null) {
            resetSyncManager();
        }
        return notesSyncManager;
    }

    public void removeSyncManager() {
        notesSyncManager = null;
    }

    public void stopSynchronize() {
        if (notesSyncManager != null) {
            notesSyncManager.setSynchronizing(false);
        }
    }

    public String getSupprotEmail() {
        String email = "support+gnotes-0.1.0@appest.com";
        try {
            email = getResources().getString(R.string.support_email).replace("VERSIONCODE",
                    getVersionCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return email;
    }

    public String getVersionCode() {
        if (versionCode == null) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionCode = "" + info.versionCode;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return versionCode;
    }

    public String getVersionName() {
        if (versionName == null) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = "" + info.versionName;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return versionName;
    }

    // network status
    public boolean isWifiOnly() {
        return settings.getBoolean(PK.SYNC_WIFI_ONLY, false);
    }

    public boolean isManualSync() {
        return settings.getBoolean(PK.MANUAL_SYNC, false);
    }

    private boolean isWifiEnable = false;
    private boolean isNetWorkEnable = true;

    public boolean isWifiEnable() {
        return isWifiEnable;
    }

    public void setWifiEnable(boolean isWifiEnable) {
        this.isWifiEnable = isWifiEnable;
    }

    public boolean isNetWorkEnable() {
        return isNetWorkEnable;
    }

    public void setNetWorkEnable(boolean isNetWorkEnable) {
        this.isNetWorkEnable = isNetWorkEnable;
    }

    public long getCurrentAccountId() {
        return accountManager.getAccountId();
    }

    public boolean startSync(int syncMode) {
        if (notesSyncManager == null) {
            return false;
        }
        if (accountManager.isLocalMode()) {
            return false;
        }

        if (!manualSync || syncMode == SyncMode.MANUAL) {
            notesSyncManager.sync(syncMode);
            return true;
        }
        return false;
    }

    public boolean sdExists() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    // ///////////////Theme//////////////////////////////

    public int getThemeTmp() {
        return themeTmp;
    }

    public void setThemeTmp(int themeTmp) {
        this.themeTmp = themeTmp;
    }

    public int getThemeType() {
        return themeType;
    }

    public void setThemeType(int themeType) {
        this.themeType = themeType;
    }

    public boolean isLightTheme() {
        return Themes.THEME_LIGHT == themeType;
    }

    public boolean isBlackTheme() {
        return Themes.THEME_BLACK == themeType;
    }

    public boolean isThemeChanged() {
        return themeTmp != themeType;
    }

    private final static int MV = 1;

    public boolean hasMi() {
        if (mi == null) {
            mi = ck(this);
        }
        if (mi != null && mi.isIrk() && mi.getV() >= MV) {
            return true;
        }

        return false;
    }

    private BroadcastReceiver networkReceiver;

    private void registerNetWorkState() {
        new RegisterNetWorkStateTask().execute();
    }

    class RegisterNetWorkStateTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            networkReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    checkNetworkState(context);
                }

            };
            registerReceiver(networkReceiver, filter);
            return null;
        }

    }

    private void checkNetworkState(Context context) {
        Log.d(TAG, "************** CheckNetwork ************");
        ConnectivityManager cmng = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo = cmng.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetInfo.isConnected()) {
            Log.i(TAG, "wifi enable");
            setWifiEnable(true);
            setNetWorkEnable(true);
            startSync(SyncMode.ALL);
        } else {
            setWifiEnable(false);
            NetworkInfo activeNetInfo = cmng.getActiveNetworkInfo();
            if (activeNetInfo != null) {
                setNetWorkEnable(true);
                if (isWifiOnly()) {
                    stopSynchronize();
                    Log.i(TAG, "has network not wifi, not startSync");
                } else {
                    Log.i(TAG, "has network not wifi, startSync");
                    startSync(SyncMode.ALL);
                }
            } else {
                setNetWorkEnable(false);
                stopSynchronize();
            }
        }
    }

    private void initTargetHeightWidth() {
        TARGET_HEIGHT = getResources().getDisplayMetrics().heightPixels;
        TARGET_WIDTH = getResources().getDisplayMetrics().widthPixels;
        float tmp = ConvertUtils.px2dip(this, TARGET_WIDTH < TARGET_HEIGHT ? TARGET_WIDTH
                : TARGET_HEIGHT);
    }

    public int getTargetHeight() {
        return TARGET_HEIGHT;
    }

    public int getTargetWidth() {
        return TARGET_WIDTH;
    }

}
