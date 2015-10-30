package org.dayup.inotes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import org.dayup.inotes.setup.AccountSelectActivity;

/**
 * Created by myatejx on 15/10/29.
 */
public class INotesPreferencesActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inote_preference_activity);

        initToolbar();
        INotesPreferencesFragment iNotesPreferencesFragment = new INotesPreferencesFragment();
        replaceFragment(R.id.pref_container, iNotesPreferencesFragment);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_settings);
        setSupportActionBar(toolbar);//这句得在getSupport之前
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //ToolBar显示返回按钮
    }

    private void replaceFragment(int viewId, Fragment fragment) {
        getFragmentManager().beginTransaction().replace(viewId, fragment).commit();
    }

}
