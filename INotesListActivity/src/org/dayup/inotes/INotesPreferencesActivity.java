package org.dayup.inotes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import org.dayup.inotes.setup.AccountSelectActivity;
import org.dayup.inotes.utils.ThemeUtils;

/**
 * Created by myatejx on 15/10/29.
 */
public class INotesPreferencesActivity extends AppCompatActivity implements
        INotesPreferencesFragment.PrefItemOnClickListener {

    private Toolbar toolbar;
    private INotesPreferencesFragment iNotesPreferencesFragment;
    private INotesPreferencesSubSyncFragment iNotesPreferencesSubSyncFragment;

    private INotesApplication application;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (INotesApplication) getApplicationContext();
        ThemeUtils themeUtils = new ThemeUtils(application);
        themeUtils.onActivityCreateSetTheme(this);

        setContentView(R.layout.inote_preference_activity);

        initToolbar();
        iNotesPreferencesFragment = new INotesPreferencesFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.pref_container, iNotesPreferencesFragment)
                .commit();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_settings);
        setSupportActionBar(toolbar);//这句得在getSupport之前
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //ToolBar显示返回按钮
    }

    @Override public void prefItemClick() {
        iNotesPreferencesSubSyncFragment = new INotesPreferencesSubSyncFragment();
        getFragmentManager().beginTransaction()
                .hide(iNotesPreferencesFragment)
                .add(R.id.pref_container, iNotesPreferencesSubSyncFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }


    //addToBackStack(null)不起作用时重写这个
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
