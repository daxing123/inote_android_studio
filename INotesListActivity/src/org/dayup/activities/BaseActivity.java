package org.dayup.activities;

import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.R;
import org.dayup.inotes.db.INotesDBHelper;
import org.dayup.inotes.utils.ThemeUtils;
import org.dayup.inotes.utils.Utils;
import org.dayup.inotes.utils.Utils20;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.SparseArray;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class BaseActivity extends SherlockFragmentActivity {

    private static final int BASE_ID = 1000000;
    protected INotesApplication iNotesApplication = null;
    protected ThemeUtils mThemeUtils;
    private SparseArray<DialogHandler> dialogs = new SparseArray<DialogHandler>();

    protected INotesDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        iNotesApplication = (INotesApplication) getApplication();
        dbHelper = iNotesApplication.getDBHelper();
        mThemeUtils = new ThemeUtils(iNotesApplication);
        mThemeUtils.onActivityCreateSetTheme(this);

        super.onCreate(savedInstanceState);
    }

    public synchronized void addDialogHandler(DialogHandler handler) {
        if (handler.getId() == -1) {
            handler.setId(dialogs.size() + BASE_ID);
        }
        dialogs.put(handler.getId(), handler);
    }

    public synchronized void removeDialogHandler(DialogHandler handler) {
        dialogs.remove(handler.getId());
        removeDialog(handler.getId());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        DialogHandler handler = dialogs.get(id);
        if (handler != null) {
            return handler.onCreateDialog();
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        DialogHandler handler = dialogs.get(id);
        if (handler != null) {
            handler.onPrepareDialog(dialog);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (iNotesApplication != null) {
            iNotesApplication.activeActivities--;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (iNotesApplication != null) {
            iNotesApplication.activeActivities++;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0, len = dialogs.size(); i < len; i++) {
            DialogHandler handler = dialogs.valueAt(i);
            removeDialog(handler.getId());
        }
        dialogs.clear();
    }

    public void startVoiceRecognitionActivity(SherlockFragment fragment, int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        if (fragment != null) {
            startActivityFromFragment(fragment, intent, requestCode);
        } else {
            startActivityForResult(intent, requestCode);
        }

    }

    public void showVoiceSearchMarketDialog() {
        voiceSearchMarketDialog.show();
    }

    protected DialogHandler voiceSearchMarketDialog = new DialogHandler(this) {

        @Override
        public Dialog onCreateDialog() {
            return new AlertDialog.Builder(BaseActivity.this)
                    .setTitle(R.string.g_voice_search_dialog_title)
                    .setMessage(R.string.g_voice_search_dialog_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri
                                    .parse("market://details?id=com.google.android.voicesearch"));
                            Utils.startUnknowActivity(BaseActivity.this, intent,
                                    R.string.android_market_not_find);
                        }
                    }).setNegativeButton(android.R.string.cancel, null).create();
        }
    };

    protected void reload() {
        Intent intent = getIntent();
        Utils20.overridePendingTransition(this, 0, 0);
        intent.addFlags(Utils20.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        Utils20.overridePendingTransition(this, 0, 0);
        startActivity(intent);
    }

}
