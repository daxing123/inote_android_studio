/**
 * 
 */
package org.dayup.inotes.key;

import org.dayup.activities.BaseActivity;
import org.dayup.inotes.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;

/**
 * @author dato
 * 
 */
public class KeyErrorDialogActivity extends BaseActivity {
    private Button reconnectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_error_layout);
        reconnectBtn = (Button) findViewById(R.id.buy_button);
        reconnectBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                buyKey();
            }
        });

        initActionBar();

    }

    private void initActionBar() {
        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(false);
        bar.setTitle(R.string.title_pay_for);
    }

    private void buyKey() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://search?q=pname:org.dayup.inotes"));
        startActivity(intent);
    }

}
