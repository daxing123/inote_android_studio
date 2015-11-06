package org.dayup.inotes;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import org.dayup.common.Analytics;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.setup.AccountSelectActivity;
import org.dayup.inotes.utils.ThemeUtils;
import org.dayup.inotes.utils.Utils20;

/**
 * Created by myatejx on 15/10/29.
 */
public class INotesPreferencesFragment extends PreferenceFragment {

    public final static int SET_ACCOUNT = 0x007;
    private PreferenceScreen accountSetting;
    private INotesApplication application;

    public static class PK {
        public static final String ACCOUNT_SETTING = "prefkey_account";
        public static final String THEME_KEY = "prefkey_theme";
        public static final String SYNC_WIFI_ONLY = "prefkey_wifi_only";
        public static final String MANUAL_SYNC = "prefkey_sync_manually";
        public static final String FEED_BACK = "prefkey_feed_back";
        public static final String SHARE_APP = "prefkey_share_app";
        public static final String DELETE_CONFIRM = "prefkey_delete_confirm";
        public static final String LAST_QUERY_STRING = "query";
        public static final String OPTION_SORT_BY = "sort_by";
        public static final String SYNC_SETTING = "prefkey_sync_setting";
    }

    public interface PrefItemOnClickListener {
        void prefItemClick();
    }

    /*public PrefItemOnClickListener prefItemOnClickListener;

    public void SetPrefItemOnClickListener(PrefItemOnClickListener prefItemOnClickListener) {
        this.prefItemOnClickListener = prefItemOnClickListener;
    }*/

    @Override public void onCreate(Bundle savedInstanceState) {
        application = (INotesApplication) getActivity().getApplicationContext();
        //        ThemeUtils themeUtils = new ThemeUtils(application);
        //        themeUtils.onActivityCreateSetTheme();

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.inotes_preference);

        accountSetting = (PreferenceScreen) findPreference(PK.ACCOUNT_SETTING);
        accountSetting.setOnPreferenceClickListener(accountSettingClickListener);
        initAccountSettingPreference();

        /*findPreference(PK.SYNC_SETTING).setIntent(
                new Intent(getActivity(), INotesPreferencesSubSync.class));*/
        initThemeSettingPreference();

        findPreference(PK.SYNC_SETTING).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override public boolean onPreferenceClick(Preference preference) {
                        if (getActivity() instanceof PrefItemOnClickListener)
                            ((PrefItemOnClickListener) getActivity()).prefItemClick();

                        return false;
                    }
                });

        findPreference(PK.FEED_BACK).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = null;
                        try {
                            StringBuffer sb = new StringBuffer("\n\n\n\n\n\n---------\n");
                            sb.append(collectPhoneInfo());

                            Uri emailUri = Uri.parse(application.getSupprotEmail());
                            intent = new Intent(Intent.ACTION_SENDTO, emailUri);
                            intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e("INotesPreferences", e.getMessage(), e);
                        }
                        return true;
                    }
                });

        findPreference(PK.SHARE_APP).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        share();
                        return true;
                    }
                });

    }

    private void initThemeSettingPreference() {
        ListPreference themeListPreference = (ListPreference) findPreference(PK.THEME_KEY);
        final String[] themeArrayValues = getResources().getStringArray(R.array.array_theme_values);
        final String[] themeArray = getResources().getStringArray(R.array.array_theme);
        themeListPreference.setValue(themeArrayValues[application.getThemeType()]);
        themeListPreference
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue != null) {
                            setListPreferenceSummary(preference, newValue, themeArrayValues,
                                    themeArray);
                            reload();
                        }
                        return true;
                    }
                });

        setListPreferenceSummary(themeListPreference, themeListPreference.getValue(),
                themeArrayValues, themeArray);

    }

    private void setListPreferenceSummary(Preference preference, Object newValue,
            String[] arrayValue, String[] arrayLabel) {
        for (int i = 0; i < arrayValue.length; i++) {
            if (arrayValue[i].equals(newValue)) {
                preference.setSummary("" + arrayLabel[i]);
            }
        }
    }

    private void reload() {

        Intent intent = getActivity().getIntent();
        Utils20.overridePendingTransition(getActivity(), 0, 0);
        intent.addFlags(Utils20.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();
        Utils20.overridePendingTransition(getActivity(), 0, 0);
        startActivity(intent);
    }

    private String collectPhoneInfo() {
        return String.format("VersionCode:%s\nVersionName:%s\nCarrier:%s\nModel:%s\nFirmware:%s\n",
                application.getVersionCode(), application.getVersionName(), Build.BRAND,
                // Build.DEVICE,
                // Build.BOARD,
                // Build.DISPLAY,
                Build.MODEL,
                // Build.PRODUCT,
                Build.VERSION.RELEASE);
    }

    private void share() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {
                ""
        });
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                getString(R.string.preferences_share_app_subject));
        intent.putExtra(android.content.Intent.EXTRA_TEXT,
                getResources().getString(R.string.share_content));
        intent.setType("text/plain");
        startActivity(
                Intent.createChooser(intent, getString(R.string.preferences_share_app_title)));
    }

    @Override public void onResume() {
        super.onResume();
        initAccountSettingPreference();

    }

    @Override public void onPause() {
        super.onPause();
    }

    private void initAccountSettingPreference() {
        Account account = application.getAccountManager().getAccount();
        if (account.isLocalMode()) {
            accountSetting.setSummary(R.string.preferences_local_mode);
        } else {
            accountSetting.setSummary(account.email);
        }
    }

    Preference.OnPreferenceClickListener accountSettingClickListener = new Preference.OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {

            Intent i = new Intent(getActivity(), AccountSelectActivity.class);
            startActivityForResult(i, SET_ACCOUNT);
            return false;
        }
    };

    @Override public void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(getActivity());
        } catch (Exception e) {
            Log.d("INotesPreference", e.toString());
        }
    }

    @Override public void onStop() {
        super.onStop();
        Analytics.endFlurry(getActivity());
    }

}
