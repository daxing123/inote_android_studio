package org.dayup.inotes;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import org.dayup.common.Analytics;
import org.dayup.inotes.INotesPreferences;
import org.dayup.inotes.R;

/**
 * Created by myatejx on 15/10/30.
 */
public class INotesPreferencesSubSyncFragment extends PreferenceFragment {
    //private static final String TAG = INotesPreferencesSubSync.class.getSimpleName();
    private CheckBoxPreference wifiOnlyPreference;
    public static int dotype = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.inotes_preferences_sync);
        init();
    }

    private void init() {
        // serverConnectedPreference = (CheckBoxPreference)
        // findPreference(PK.SERVER_CONNECTED);
        wifiOnlyPreference = (CheckBoxPreference) findPreference(INotesPreferences.PK.SYNC_WIFI_ONLY);
        wifiOnlyPreference.setOnPreferenceClickListener(wifiOnlyClickListener);
    }

    Preference.OnPreferenceClickListener wifiOnlyClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                    .putBoolean(INotesPreferences.PK.SYNC_WIFI_ONLY, !((CheckBoxPreference) preference).isChecked());
            return false;
        }

    };

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            getActivity().finish();
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    public void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(getActivity());
        } catch (Exception e) {
            //Log.d(TAG, e.toString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Analytics.endFlurry(getActivity());
    }

}
