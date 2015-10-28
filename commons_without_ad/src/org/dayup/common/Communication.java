package org.dayup.common;

import java.util.HashMap;

import org.dayup.decription.Decrypt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Should never throw any exception in this class.
 * 
 * @author dmks
 * 
 */
public class Communication {
    private static final String VERSION_CODE_KEY = "versionCode";
    private static final String PAY_VERSION_CODE_KEY = "pay_versionCode";
    private static final String PACKAGE_NAME_KEY = "package";
    private static final String LAST_PULL_POINT_KEY = "last_pull_point";
    private static final String PRO_VERSION_CODE_KEY = "pro_versionCode";

    public static final String TAG = Communication.class.getSimpleName();
    public static final String PREF_NAME = "communication";

    public static class Field {
        public static final String ID = "id";
        public static final String VERSION = "version";
    }

    private static final String CONFIG_PREFIX = "CONFIG_";
    // private static final String DEFAULT_BASE_URL =
    // "http://help.dayup.org/gtask_note.out";
    // private static final String DEBUG_BASE_URL =
    // "http://help.dayup.org/gtask_note.test.out";
    private static final long DEFAULT_MIN_INTERVAL = 86400000; // 24h
    private static final String COMMUNICATION_CONFIG_ID = "communication_config";

    private HashMap<String, JSONObject> cache = new HashMap<String, JSONObject>();

    public HashMap<String, JSONObject> getCache() {
        return cache;
    }

    private SharedPreferences preferences;
    private PackageManager packageManager;
    private DisplayMetrics displayMetrics;
    private String packageName;
    private Mi keyInfo;
    private Context context;
    private boolean isPro = false;

    private long lastPullPoint;
    private long minInterval = DEFAULT_MIN_INTERVAL;
    private String baseUrl;
    private boolean debug;

    public Communication(Context context, boolean debug, String baseUrl) {
        initParam(context, debug, baseUrl);
    }

    public Communication(Context context, Mi keyInfo, boolean debug, String baseUrl) {
        initParam(context, debug, baseUrl);
        this.keyInfo = keyInfo;
    }

    public Communication(Context context, boolean isPro, boolean debug, String baseUrl) {
        initParam(context, debug, baseUrl);
        this.isPro = isPro;
    }

    public Context getContext() {
        return context;
    }

    public void pull() {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return innerPull();
                } catch (Throwable e) {
                    Log.w(TAG, "Fail to pull configiration from server!", e);
                    return false;
                }
            }
        }.execute();
    }

    private void initParam(Context context, boolean debug, String baseUrl) {
        packageManager = context.getPackageManager();
        packageName = context.getPackageName();
        displayMetrics = context.getResources().getDisplayMetrics();
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.context = context;
        lastPullPoint = preferences.getLong(LAST_PULL_POINT_KEY, 0);
        this.debug = debug;
        this.baseUrl = baseUrl;
    }

    private boolean innerPull() {
        JSONObject config = getConfig(COMMUNICATION_CONFIG_ID);
        if (config != null && !debug) {
            try {
                minInterval = config.getLong("minInterval");
                baseUrl = config.getString("baseUrl");
            } catch (JSONException e) {
                Log.e(TAG, "Can't get properties from the communication config json object", e);
            }
        }

        if (!debug && lastPullPoint + minInterval >= System.currentTimeMillis()) {
            // don't need to read
            return false;
        }

        String url = buildUrl();
        String content = HttpUtils.doHttpGet(url);
        // Log.d(TAG, "URL = " + url);
        // Log.d(TAG, "Content = " + content);
        if (content == null || content.length() == 0) {
            Log.e(TAG, "Can't retrieve content from " + url);
            return false;
        }
        content = Decrypt.decrypt(content);
        // Log.d(TAG, "Decrypted Content = " + content);
        try {
            JSONArray array = new JSONArray(content);
            Editor editor = preferences.edit();
            for (int i = 0, len = array.length(); i < len; i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.getString(Field.ID);
                int version = obj.getInt(Field.VERSION);

                JSONObject existing = getConfig(id);
                if (existing == null || existing.getInt(Field.VERSION) < version) {
                    if (checkCondition(obj)) {
                        cache.put(id, obj);
                        // Log.d(TAG, "id:"+id + "  obj:"+obj.toString() );
                        editor.putString(CONFIG_PREFIX + id, obj.toString());
                    }
                }
            }

            lastPullPoint = System.currentTimeMillis();
            editor.putLong(LAST_PULL_POINT_KEY, lastPullPoint).commit();
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse json result from " + url, e);
            return false;
        }
    }

    private boolean checkCondition(JSONObject obj) {
        if (obj.has(VERSION_CODE_KEY)) {
            try {
                JSONObject code = obj.getJSONObject(VERSION_CODE_KEY);
                PackageInfo info = packageManager.getPackageInfo(packageName, 0);
                int versionCode = info.versionCode;
                int value = code.getInt("value");
                String op = code.getString("op");
                if (!opcheck(op, versionCode, value)) {
                    return false;
                }
            } catch (JSONException e) {
                Log.w(TAG, "Fail to check condition", e);
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Fail to check condition", e);
            }
        }
        if (obj.has(PACKAGE_NAME_KEY)) {
            try {
                String tgtName = obj.getString(PACKAGE_NAME_KEY);
                if (!packageName.equals(tgtName)) {
                    return false;
                }
            } catch (JSONException e) {
                Log.w(TAG, "Fail to check condition", e);
            }
        }

        if (obj.has(PAY_VERSION_CODE_KEY)) {
            try {
                JSONObject code = obj.getJSONObject(PAY_VERSION_CODE_KEY);
                int value = code.getInt("value");
                String op = code.getString("op");
                int pay_versionCode = 0;
                if (keyInfo != null) {
                    pay_versionCode = keyInfo.getV();
                }
                if (!opcheck(op, pay_versionCode, value)) {
                    return false;
                }

            } catch (JSONException e) {
                Log.w(TAG, "Fail to check condition", e);
            }
        }

        if (obj.has(PRO_VERSION_CODE_KEY)) {
            try {
                JSONObject code = obj.getJSONObject(PRO_VERSION_CODE_KEY);
                int value = code.getInt("value");
                String op = code.getString("op");
                int proVersionCode = 0;
                if (isPro) {
                    proVersionCode = keyInfo.getV();
                }
                if (!opcheck(op, proVersionCode, value)) {
                    return false;
                }

            } catch (JSONException e) {
                Log.w(TAG, "Fail to check condition", e);
            }
        }
        return true;
    }

    private boolean opcheck(String op, int versionCode, int value) {
        if ("<".equals(op)) {
            if (!(versionCode < value)) {
                return false;
            }
        } else if (">".equals(op)) {
            if (!(versionCode > value)) {
                return false;
            }
        } else if ("<=".equals(op)) {
            if (!(versionCode <= value)) {
                return false;
            }
        } else if (">=".equals(op)) {
            if (!(versionCode >= value)) {
                return false;
            }
        } else {
            if (!(versionCode == value)) {
                return false;
            }
        }
        return true;
    }

    private String buildUrl() {
        return buildUrl(baseUrl);
    }

    public String buildUrl(String baseUrl) {
        StringBuffer buf = new StringBuffer();
        buf.append(baseUrl).append("?").append("package=").append(packageName);
        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            buf.append("&").append("version_code=").append(info.versionCode);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error getting package info!", e);
        }

        buf.append("&").append("os=").append(Build.VERSION.SDK);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        if (width > height) {
            int s = height;
            height = width;
            width = s;
        }
        buf.append("&screen_width=").append(width).append("&screen_height=").append(height);
        return buf.toString();
    }

    public JSONObject getConfig(String id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        String config = preferences.getString(CONFIG_PREFIX + id, null);
        if (config == null) {
            return null;
        }

        try {
            JSONObject obj = new JSONObject(config);
            if (checkCondition(obj)) {
                cache.put(id, obj);
                return obj;
            } else {
                preferences.edit().remove(CONFIG_PREFIX + id).commit();
                return null;
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing the json string stored in preferences!", e);
            return null;
        }

    }

    public void saveConfig(JSONObject config) {
        try {
            String id = config.getString(Field.ID);
            cache.put(id, config);
            preferences.edit().putString(CONFIG_PREFIX + id, config.toString()).commit();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to save config!", e);
        }

    }

}
