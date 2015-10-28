package org.dayup.inotes.setup;

import org.dayup.common.Analytics;
import org.dayup.inotes.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AccountSetupIncomingActivity extends AccountSetupBaseActivity implements
        OnClickListener {

    private Button nextBtn;
    private AccountSetupIncomingFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_incoming);
        initViews();
        mFragment = (AccountSetupIncomingFragment) getFragmentManager().findFragmentById(
                R.id.setup_fragment);

    }

    private void initViews() {
        findViewById(R.id.account_authen_previous).setOnClickListener(this);
        nextBtn = (Button) findViewById(R.id.account_authen_next);
        nextBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.account_authen_previous:
            doPrevious();
            break;
        case R.id.account_authen_next:
            doNext();
            break;
        }

    }

    private void doNext() {
        mFragment.onNext();

    }

    private void doPrevious() {
        finish();
    }

    public void onEnableProceedButtons(boolean enable) {
        if (nextBtn != null) {
            nextBtn.setEnabled(enable);
        }

    }

    public void startAccountLoginSuccessActivity() {
        Intent i = new Intent(this, AccountLoginSuccessActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(this);
        } catch (Exception e) {
            Log.d("AccountSetupIncomingActivity", e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.endFlurry(this);
    }
}
