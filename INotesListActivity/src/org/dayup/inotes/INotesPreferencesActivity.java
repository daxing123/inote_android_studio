package org.dayup.inotes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.dayup.inotes.setup.AccountSelectActivity;

/**
 * Created by myatejx on 15/10/29.
 */
public class INotesPreferencesActivity extends AppCompatActivity{
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        INotesPreferencesFragment iNotesPreferencesFragment = new INotesPreferencesFragment();
        replaceFragment(R.id.pref_container, iNotesPreferencesFragment);
    }

    private void replaceFragment(int viewId, Fragment fragment) {
        getFragmentManager().beginTransaction().replace(viewId, fragment).commit();
    }


}
