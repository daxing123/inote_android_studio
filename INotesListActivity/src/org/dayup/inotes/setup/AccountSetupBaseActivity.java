package org.dayup.inotes.setup;

import android.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import org.dayup.activities.BaseActivity;
import org.dayup.common.Analytics;
import org.dayup.inotes.R;
import org.dayup.inotes.views.INotesDialog;

import android.app.Activity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.fsck.k9.mail.AuthenticationFailedException;
import com.fsck.k9.mail.MessagingException;

public abstract class AccountSetupBaseActivity extends BaseActivity {

    private final static String TAG = AccountSetupBaseActivity.class.getSimpleName();
    private ActionBar mBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initActionBar();
        initToolbar();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.account_sigin_in);
        setSupportActionBar(toolbar);//这句得在getSupport之前
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //ToolBar显示返回按钮
    }

    private void initActionBar() {
        mBar = getActionBar();
        mBar.setHomeButtonEnabled(true);
        mBar.setDisplayHomeAsUpEnabled(true);
        mBar.setTitle(R.string.account_sigin_in);
    }

    protected void setActionBarTitle(int titleRes) {
        //mBar.setTitle(titleRes);
        toolbar.setTitle(titleRes);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getXmlAttribute(XmlResourceParser xml, String name) {
        int resId = xml.getAttributeResourceValue(null, name, 0);
        if (resId == 0) {
            return xml.getAttributeValue(null, name);
        } else {
            return getString(resId);
        }
    }

    protected Provider findProviderForDomain(String domain) {
        try {
            XmlResourceParser xml = getResources().getXml(R.xml.providers);
            int xmlEventType;
            Provider provider = null;
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                if (xmlEventType == XmlResourceParser.START_TAG && "provider".equals(xml.getName())
                        && domain.equalsIgnoreCase(getXmlAttribute(xml, "domain"))) {
                    provider = new Provider();
                    provider.id = getXmlAttribute(xml, "id");
                    provider.label = getXmlAttribute(xml, "label");
                    provider.domain = getXmlAttribute(xml, "domain");
                    provider.server = getXmlAttribute(xml, "server");
                    provider.security = getXmlAttribute(xml, "security");
                    return provider;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to load provider settings.", e);
        }
        return null;
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

    protected void showErrorDialog(final Activity activity, Throwable result) {
        if (activity.isFinishing()) {
            return;
        }
        //final INotesDialog dialog = new INotesDialog(activity, iNotesApplication.getThemeType());
        final INotesDialog dialog = new INotesDialog(activity);
        dialog.setTitle(R.string.dialog_title_login_failed);
        if (result instanceof AuthenticationFailedException) {
            if (TextUtils.equals(result.getMessage(), "Unsupported protocol")) {
                dialog.setMessage(R.string.text_security_type_incorrect);
            } else {
                dialog.setMessage(R.string.text_username_password_incorrect);
            }
        } else if (result instanceof MessagingException) {
            dialog.setMessage(R.string.text_connection_server_failed);
        } else {
            dialog.setMessage(R.string.text_authorize_failed);
        }
        dialog.setPositiveButton(R.string.dialog_button_need_help, new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.inotes_setup_account_help_url)));
                activity.startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }
}
