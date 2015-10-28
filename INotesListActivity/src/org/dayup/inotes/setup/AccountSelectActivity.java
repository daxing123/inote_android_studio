package org.dayup.inotes.setup;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import org.dayup.activities.BaseActivity;
import org.dayup.common.Analytics;
import org.dayup.common.Log;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.R;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.constants.Constants.ResultCode;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.views.INotesDialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AccountSelectActivity extends BaseActivity {

    private INotesApplication application;
    private INotesAccountManager accountManager;
    private AccountItemAdapter accountListAdapter;
    private ListView listView;
    private long preAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (INotesApplication) getApplication();
        accountManager = application.getAccountManager();
        preAccountId = accountManager.getAccountId();
        setContentView(R.layout.account_select_layout);
        initActionBar();
        initViews();
        requery();
    }

    private void initActionBar() {
        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(R.string.switch_account);
    }

    private void initViews() {
        accountListAdapter = new AccountItemAdapter(this);
        listView = (ListView) findViewById(R.id.account_list);
        listView.setAdapter(accountListAdapter);
        TextView emptyView = (TextView) findViewById(R.id.account_list_empty_view);
        listView.setEmptyView(emptyView);

    }

    private void requery() {

        List<Account> accounts = Account
                .getAllAccounts(null, null, null, application.getDBHelper());
        accountListAdapter.setData(accounts);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inotes_account_select_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.menu_add_account:
            startAccountLoginActivity();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void startAccountLoginActivity() {
        Intent i = new Intent(AccountSelectActivity.this, AccountLoginActivity.class);
        startActivity(i);
    }

    class AccountItemAdapter extends BaseAdapter {
        private List<Account> list = new ArrayList<Account>();
        private Context context;

        public AccountItemAdapter(Context context) {
            this.context = context;
        }

        public void setData(List<Account> accounts) {
            this.list = accounts;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = View.inflate(context, R.layout.account_item, null);
            } else {
                v = convertView;
            }
            final Account item = list.get(position);

            final LinearLayout selectLayout = (LinearLayout) v.findViewById(R.id.account_name);
            final TextView name = (TextView) v.findViewById(R.id.account_name_text);
            final ImageView delete = (ImageView) v.findViewById(R.id.account_delete);
            name.setText(item.email);
            selectLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    selectAccount(item);
                }

            });
            delete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    showDeleteConfirmDialog(item);
                }

            });

            return v;
        }

    }

    private void selectAccount(Account account) {
        application.stopSynchronize();
        accountManager.switchAccount(account);
        finish();
    }

    private void removeAccount(Account account) {
        accountManager.deleteAccount(account);
        requery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preAccountId != accountManager.getAccountId()) {
            iNotesApplication.resetSyncManager();
            iNotesApplication.setResultCode(ResultCode.RESET_AUTH);
        } else {
            iNotesApplication.setResultCode(ResultCode.NO_CHANGE);
        }
    }

    private void showDeleteConfirmDialog(final Account account) {
        //final INotesDialog dialog = new INotesDialog(this, iNotesApplication.getThemeType());
        final INotesDialog dialog = new INotesDialog(this);
        dialog.setTitle(R.string.dialog_title_remove_account);
        dialog.setMessage(R.string.dialog_remove_account_message);
        dialog.setPositiveButton(android.R.string.ok, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                removeAccount(account);
                dialog.dismiss();
                finish();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(this);
        } catch (Exception e) {
            Log.d("AccountSelectActivity", e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.endFlurry(this);
    }
}
