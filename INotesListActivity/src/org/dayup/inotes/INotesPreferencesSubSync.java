package org.dayup.inotes;

import org.dayup.common.Analytics;
import org.dayup.inotes.INotesPreferences.PK;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

/**
 * @author Nicky
 * 
 */
public class INotesPreferencesSubSync extends SherlockPreferenceActivity {

    private static final String TAG = INotesPreferencesSubSync.class.getSimpleName();
    private CheckBoxPreference wifiOnlyPreference;
    public static int dotype = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.inotes_preferences_sync);
        init();
    }

    private void init() {
        // serverConnectedPreference = (CheckBoxPreference)
        // findPreference(PK.SERVER_CONNECTED);
        wifiOnlyPreference = (CheckBoxPreference) findPreference(PK.SYNC_WIFI_ONLY);
        wifiOnlyPreference.setOnPreferenceClickListener(wifiOnlyClickListener);
    }

    OnPreferenceClickListener wifiOnlyClickListener = new OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            PreferenceManager.getDefaultSharedPreferences(INotesPreferencesSubSync.this).edit()
                    .putBoolean(PK.SYNC_WIFI_ONLY, !((CheckBoxPreference) preference).isChecked());
            return false;
        }

    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(this);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.endFlurry(this);
    }
}
