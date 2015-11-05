package org.dayup.inotes;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseBooleanArray;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import org.dayup.activities.BaseActivity;
import org.dayup.common.Analytics;
import org.dayup.common.Log;
import org.dayup.inotes.INotesPreferences.PK;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.adapter.NoteListAdapter;
import org.dayup.inotes.adapter.SpinnerSelectorAdapter;
import org.dayup.inotes.adapter.SpinnerSelectorsHelper;
import org.dayup.inotes.constants.Constants.RequestCode;
import org.dayup.inotes.constants.Constants.ResultCode;
import org.dayup.inotes.constants.Constants.SyncMode;
import org.dayup.inotes.constants.Constants.SyncStatus;
import org.dayup.inotes.data.Account;
import org.dayup.inotes.data.Folder;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.db.Field.Status;
import org.dayup.inotes.sync.exception.AuthenticationErrorException;
import org.dayup.inotes.sync.manager.NetworkException;
import org.dayup.inotes.sync.manager.SyncManager;
import org.dayup.inotes.sync.manager.SyncManager.RefreshSyncedListener;
import org.dayup.inotes.sync.manager.SyncManager.SyncingRefreshUIListener;
import org.dayup.inotes.utils.ShareUtils;
import org.dayup.tasks.BackgroundTaskManager.BackgroundTaskStatusListener;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Nicky
 */
public class INotesListActivity extends BaseActivity implements SyncingRefreshUIListener,
        RefreshSyncedListener {

    private final String TAG = INotesListActivity.class.getSimpleName();
    private SpinnerSelectorAdapter actionBarAdapter;
    private ArrayList<Note> noteslist = new ArrayList<Note>();
    private Folder currentFolder;
    private SpinnerSelectorsHelper mHelper;
    private INotesAccountManager accountManager;

    private View mAccountSpinner = null;
    private ViewGroup mActionBarCustomView;
    private TextView mAccountSpinnerLine1View;
    //private AccountDropdownPopup mAccountDropdown;

    private SharedPreferences sp;
    private SyncManager notesSyncManager;

    private DrawerLayout dl;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FloatingActionButton fab;
    private android.support.v7.widget.Toolbar toolbar;
    private Spinner spinner;

    private MenuItem sort_create, sort_modify, sort_name;

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private int click = 0;
    private ActionMode actionMode;
    private boolean isInActionMode = false;

    private final int HIDE_LAYOUT_DURATION = 5000;

    public static class SortByTypes {
        public static final int CREATE_DOWN = 400;
        public static final int MODIFY_DOWN = 401;
        public static final int CREATE_UP = 402;
        public static final int MODIFY_UP = 403;
        public static final int A_Z_UP = 404;
        public static final int A_Z_DOWN = 405;
    }

    private int sortByType = SortByTypes.MODIFY_DOWN;
    private LinearLayout sortByLayout;
    private View mSortCreatClickView, mSortModifyClickView, mSortAzClickView;
    private ImageView mSortCreatIcon, mSortModifyIcon, mSortAzIcon;
    private Animation alpha_in, alpha_out;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inotes_list);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        alpha_in = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
        alpha_out = AnimationUtils.loadAnimation(this, R.anim.alpha_out);

        /*if (Constants.TEST_MODE) {
            Log.d(TAG, "test mode");
        } else if (!iNotesApplication.hasMi()) {
            Intent it = new Intent(this, KeyErrorDialogActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(it);
            finish();
        }*/

        init();
        initViews();
        initEnvironment();
    }

    private void init() {
        accountManager = iNotesApplication.getAccountManager();
        mHelper = new SpinnerSelectorsHelper(iNotesApplication);
        resetCurrentFolder();
    }

    private void initEnvironment() {
        notesSyncManager = iNotesApplication.getSyncManager();
        if (notesSyncManager != null) {
            notesSyncManager.setRefreshSyncedListener(this);
            notesSyncManager.setRefreshSyncingListener(this);
        }
        iNotesApplication.getBackgroundTaskManager().addBackgroundTaskStatusListener(
                backgroundTaskStatusListener);
    }

    private BackgroundTaskStatusListener backgroundTaskStatusListener = new BackgroundTaskStatusListener() {
        @Override
        public void onBackgroundException(Throwable e) {
            if (e instanceof AuthenticationErrorException) {
                if (!iNotesApplication.isNetWorkEnable()) {
                    Toast.makeText(INotesListActivity.this, getString(R.string.network_unabled),
                            Toast.LENGTH_LONG).show();
                    mHandler.sendEmptyMessage(SyncStatus.ERROR);
                } else {
                    reAuthorize();
                }
            } else if (e instanceof NetworkException) {
                Log.e(TAG, e.getMessage(), e);
                mHandler.sendEmptyMessage(SyncStatus.ERROR);
            } else {
                Log.e(TAG, e.getMessage(), e);
                mHandler.sendEmptyMessage(SyncStatus.SUCCESS);
            }

        }

        @Override
        public void onLoadBegin() {
            mHandler.sendEmptyMessage(SyncStatus.SYNCING);
        }

        @Override
        public void onLoadEnd() {
            mHandler.sendEmptyMessage(SyncStatus.SUCCESS);
        }
    };

    private void resetCurrentFolder() {
        Folder folder = Folder.getFolderWithDisplaynameById(accountManager.getDefaultFolderId(),
                accountManager.getAccountId(), dbHelper);
        if (folder == null) {
            List<Folder> folders = Folder.getAllFoldersWithDisplayNameByAccountId(
                    accountManager.getAccountId(), null, null, null, dbHelper);
            if (folders.size() == 1) {
                currentFolder = folders.get(0);
                accountManager.setDefaultFolderId(currentFolder.id);
            } else {
                String name = iNotesApplication.getResources().getString(R.string.folder_all);
                currentFolder = Folder.createAllLabel(accountManager.getAccountId(), name);
            }
        } else {
            currentFolder = folder;
        }
    }

    private void reAuthorize() {
        accountManager.authorizeAccount(accountManager.getAccount(), reAuthCallBack);

    }

    private INotesAccountManager.CallBackListener reAuthCallBack = new INotesAccountManager.CallBackListener() {

        @Override
        public void callBack(Account account, Throwable result) {
            if (result == null) {
                accountManager.resetAccountPassword(account.password);
                startSync(SyncMode.ALL);
            } else {
                iNotesApplication.stopSynchronize();
                accountManager.setAccountFreezed();
                mHandler.sendEmptyMessage(SyncStatus.TOKEN_TIMEOUT);
            }

        }
    };

    private void initViews() {
        initToolbar();
        initRecycleView();
        initOtherView();
    }

    private void initOtherView() {
        fab = (FloatingActionButton) findViewById(R.id.bt);
        fab.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                startDetailActivity(null);
            }
        });
    }

    private void initRecycleView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter = new MyAdapter(this, R.layout.rv_item_linear, noteslist));
        recyclerView.addItemDecoration(new RvItemDecoration(15));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.SetOnRvItemClickListener(new RvItemClickListener());
        /*ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemtouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);*/

    }

    class RvItemClickListener implements MyAdapter.OnRvItemClickListener {
        @Override public void onItemClick(int position) {
            Note note = adapter.getItem(position);
            if (note == null) {
                return;
            }

            if (!isInActionMode()) {
                startDetailActivity(note);
            } else {
                adapter.toggleSelected(position);
            }

        }

        @Override public void onItemLongClick(int position) {

            if (!isInActionMode()) {
                actionMode = startActionMode(mActionMode);
                adapter.toggleSelected(position);
            }
        }
    }

    private boolean isInActionMode() {
        return isInActionMode;
    }

    private ActionMode.Callback mActionMode = new ActionMode.Callback() {

        @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.list_select_menu, menu);
            toolbar.setVisibility(View.GONE);
            isInActionMode = true;

            return true;
        }

        @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return false;
        }

        @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.delete:
                SparseBooleanArray selectedIds = adapter.getSelectedItemIds();
                for (int i = (selectedIds.size() - 1); i >= 0; i--) {
                    if (selectedIds.valueAt(i)) {
                        adapter.removeData(selectedIds.keyAt(i));
                    }
                }
                isInActionMode = false;
                actionMode.finish();

                return true;

            case R.id.share:

                return true;
            default:
                return false;
            }
        }

        @Override public void onDestroyActionMode(ActionMode mode) {
            toolbar.setVisibility(View.VISIBLE);
            adapter.removeSelection();
            isInActionMode = false;

        }
    };

    /*private ItemTouchHelper.Callback itemtouchCallback = new ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT) {
        @Override public boolean onMove(RecyclerView recyclerView,
                RecyclerView.ViewHolder viewHolder,
                RecyclerView.ViewHolder target) {
            return false;
        }

        @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            adapter.removeData(position);
        }
    };*/

    private void changeLayout() {
        if (click == 0) {
            recyclerView.setLayoutManager(
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            recyclerView
                    .setAdapter(adapter = new MyAdapter(this, R.layout.rv_item_grid, noteslist));
            click = 1;
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView
                    .setAdapter(adapter = new MyAdapter(this, R.layout.rv_item_linear, noteslist));
            click = 0;
        }
        adapter.SetOnRvItemClickListener(new RvItemClickListener());
    }

    /*private void initListView() {
        TextView emptyView = (TextView) findViewById(R.id.list_empty_view);
        listView = (ListView) findViewById(android.R.id.list);
        listView.setEmptyView(emptyView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new NoteListAdapter(this, noteslist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Note note = adapter.getItem(arg2);
                if (note == null) {
                    return;
                }

                if (isInSelectionMode()) {
                    adapter.resetSelectedItems(arg2);
                    arg1.invalidate();
                    updateSelectionModeView();

                } else {
                    startDetailActivity(note);
                }

            }
        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                    long id) {
                Note note = adapter.getItem(position);
                if (note == null) {
                    return false;
                }
                adapter.resetSelectedItems(position);
                view.invalidate();
                updateSelectionMode();
                return true;
            }
        });
    }
*/
    private void initToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbar_with_spinner);
        spinner = (Spinner) findViewById(R.id.spinner);
        actionBarAdapter = new SpinnerSelectorAdapter(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        /*dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                dl, toolbar, R.string.app_name, R.string.app_name) {
            //监听抽屉关闭事件
            public void onDrawerClosed(View view) {

            }

            //监听抽屉打开事件
            public void onDrawerOpened(View drawerView) {

            }

        };
        dl.post(new Runnable() {
            @Override
            public void run() {
                actionBarDrawerToggle.syncState();
            }
        });
        dl.setDrawerListener(actionBarDrawerToggle);*/
    }
    /*
      private void initActionBar() {
            actionBar = getSupportActionBar();
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(R.layout.action_bar_spinner);
            mActionBarCustomView = (ViewGroup) actionBar.getCustomView();
            mAccountSpinner = mActionBarCustomView.findViewById(R.id.folder_account_spinner);
            mAccountSpinnerLine1View = (TextView) mActionBarCustomView
                    .findViewById(R.id.spinner_line_1);

            mAccountDropdown = new AccountDropdownPopup(this);
            mAccountDropdown.setHorizontalOffset((int) getResources().getDimension(
                    R.dimen.select_list_spinner_offset_v));
            mAccountDropdown.setAdapter(actionBarAdapter);
            mAccountSpinner.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAccountDropdown();
                }
            });

        }
    */
    private void setSortByShowType(int sortByType) {
        switch (sortByType) {
        case SortByTypes.CREATE_UP:
            sort_create.setIcon(R.drawable.ic_sortby_creat_up1);
            sort_modify.setIcon(R.drawable.ic_sortby_modify_down0);
            sort_name.setIcon(R.drawable.ic_sortby_name_down0);
            break;
        case SortByTypes.CREATE_DOWN:
            sort_create.setIcon(R.drawable.ic_sortby_creat_down1);
            sort_modify.setIcon(R.drawable.ic_sortby_modify_down0);
            sort_name.setIcon(R.drawable.ic_sortby_name_down0);
            break;
        case SortByTypes.MODIFY_UP:
            sort_modify.setIcon(R.drawable.ic_sortby_modify_up1);
            sort_name.setIcon(R.drawable.ic_sortby_name_down0);
            sort_create.setIcon(R.drawable.ic_sortby_creat_down0);
            break;
        case SortByTypes.MODIFY_DOWN:
            sort_modify.setIcon(R.drawable.ic_sortby_modify_down1);
            sort_name.setIcon(R.drawable.ic_sortby_name_down0);
            sort_create.setIcon(R.drawable.ic_sortby_creat_down0);
            break;
        case SortByTypes.A_Z_UP:
            sort_name.setIcon(R.drawable.ic_sortby_name_up1);
            sort_modify.setIcon(R.drawable.ic_sortby_modify_down0);
            sort_create.setIcon(R.drawable.ic_sortby_creat_down0);
            break;
        case SortByTypes.A_Z_DOWN:
            sort_name.setIcon(R.drawable.ic_sortby_name_down1);
            sort_modify.setIcon(R.drawable.ic_sortby_modify_down0);
            sort_create.setIcon(R.drawable.ic_sortby_creat_down0);
            break;
        }
    }

    private void showAccountDropdown() {
        requarySpinnerSelectors();
        if (actionBarAdapter != null && actionBarAdapter.getCount() > 0) {
            //mAccountDropdown.show();
        }
    }

    private void requarySpinnerSelectors() {
        actionBarAdapter.setData(mHelper.getSelectors());
        mAccountSpinnerLine1View.setText(currentFolder.displayName);
    }

    private void switchToAccount(long accountId) {
        if (accountId == Status.LOCAL_MODE_ACCOUNT_ID) {
            return;
        }
        if (accountManager.getAccountId() == accountId && !accountManager.isAccountFreezed()) {
            return;
        }

        if (accountManager.switchAccount(accountId)) {
            resetCurrentFolder();
            requarySpinnerSelectors();
            requery();
            iNotesApplication.resetSyncManager();
            startSync(SyncMode.ALL);
        }
    }

    private void switchToFolder(long folderId) {
        if (folderId != currentFolder.id) {
            accountManager.setDefaultFolderId(folderId);
            resetCurrentFolder();
            requarySpinnerSelectors();
            requery();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO 需要明确那些情况下重新刷新
        resetCurrentFolder();
        //        requarySpinnerSelectors();
        requery();

        startSyncByResult();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*if (mAccountDropdown != null && mAccountDropdown.isShowing()) {
            mAccountDropdown.dismiss();
        }*/

    }

    private void requery() {

        String orderBy = "";
        switch (sortByType) {
        case SortByTypes.CREATE_UP:
            orderBy = " Note.created_time COLLATE LOCALIZED asc ";
            break;
        case SortByTypes.CREATE_DOWN:
            orderBy = " Note.created_time COLLATE LOCALIZED desc ";
            break;
        case SortByTypes.MODIFY_UP:
            orderBy = " Note.modified_time COLLATE LOCALIZED asc ";
            break;
        case SortByTypes.MODIFY_DOWN:
            orderBy = " Note.modified_time COLLATE LOCALIZED desc ";
            break;
        case SortByTypes.A_Z_UP:
            orderBy = " Note.content COLLATE LOCALIZED asc ";
            break;
        case SortByTypes.A_Z_DOWN:
            orderBy = " Note.content COLLATE LOCALIZED desc ";
            break;
        }
        noteslist = (ArrayList<Note>) Note.getNotesByFolder(currentFolder, orderBy,
                iNotesApplication.getDBHelper());
        adapter.setData(noteslist, sortByType);
    }

    private MenuItem syncItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inotes_list_activity, menu);
        syncItem = menu.findItem(R.id.menu_sync);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        syncItem.setVisible(!accountManager.isLocalMode());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        /*case R.id.menu_insert:
            startDetailActivity(null);
            return true;*/
        case R.id.menu_sync:
            startSync(SyncMode.MANUAL);
            return true;

        case R.id.menu_toggle_view:
            changeLayout();

            return true;
        case R.id.sort_by:
            //showSortByLayout();
            startActionMode(new SortbyModeCallback());

            return true;
        case R.id.menu_search:
            onSearchRequested();
            return true;
        case R.id.menu_settings:
            startINotesPreferences();

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void startINotesPreferences() {
        Intent intent = new Intent(this, INotesPreferencesActivity.class);
        //        Intent intent = new Intent(this, INotesPreferencesActivity.class);
        startActivityForResult(intent, RequestCode.SET_PREFERENCE);
    }

    private void startDetailActivity(Note note) {
        String id = note == null ? null : note.id;
        long folderId = note == null ? currentFolder.id : note.folderId;
        Intent i = new Intent(this, INotesDetailActivity.class);
        i.putExtra(INotesDetailActivity.NOTE_ID, id);
        i.putExtra(INotesDetailActivity.FOLDER_ID, folderId);
        startActivityForResult(i, RequestCode.NORMAL_START);
    }

    // Based on Spinner.DropdownPopup
    /*private class AccountDropdownPopup extends IcsListPopupWindow {

        public AccountDropdownPopup(Context context) {
            super(context);
            setAnchorView(mAccountSpinner);
            setModal(true);
            setPromptPosition(POSITION_PROMPT_ABOVE);
            setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    if (actionBarAdapter.isFolder(position)) {
                        long folderId = actionBarAdapter.getItemId(position);
                        switchToFolder(folderId);
                    } else if (actionBarAdapter.isAccount(position)) {
                        long accountId = actionBarAdapter.getItemId(position);
                        switchToAccount(accountId);
                    }

                    dismiss();
                }

            });
        }

        @Override
        public void show() {
            setWidth(INotesListActivity.this.getResources().getDimensionPixelSize(
                    R.dimen.account_dropdown_dropdownwidth));
            // if (actionBarAdapter.getCount() > MAX_SHOW_ITEM_COUNT) {
            //
            // setHeight((int) context.getResources().getDimension(
            // R.dimen.select_list_spinner_height));
            // } else {
            // // reset default value
            // setHeight(0);
            // }
            // setHeight(0);
            setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
            super.show();
            // List view is instantiated in super.show(), so we need to do this
            // after...
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }*/

    /*******************
     * Action Mode
     ********************/
    private ActionMode mSortByMode;

    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
        case RequestCode.SET_PREFERENCE:
            if (iNotesApplication.isThemeChanged()) {
                iNotesApplication.setThemeTmp(iNotesApplication.getThemeType());
                reload();
            } else {
                invalidateOptionsMenu();
            }
            break;
        default:
            break;
        }
    }

    private class SortbyModeCallback implements ActionMode.Callback {

        @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mSortByMode = mode;
            toolbar.setVisibility(View.GONE);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.sort_by_menu, menu);
            sort_create = menu.findItem(R.id.sortby_create_menu);
            sort_modify = menu.findItem(R.id.sortby_modify_menu);
            sort_name = menu.findItem(R.id.sortby_name_menu);

            return true;
        }

        @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(R.string.sort_by);
            sortByType = sp.getInt(PK.OPTION_SORT_BY, SortByTypes.MODIFY_DOWN);
            setSortByShowType(sortByType);

            return true;
        }

        @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
            case R.id.sortby_create_menu:
                if (sortByType == SortByTypes.CREATE_DOWN) {
                    sortByType = SortByTypes.CREATE_UP;
                } else {
                    sortByType = SortByTypes.CREATE_DOWN;
                }
                break;
            case R.id.sortby_modify_menu:
                if (sortByType == SortByTypes.MODIFY_DOWN) {
                    sortByType = SortByTypes.MODIFY_UP;
                } else {
                    sortByType = SortByTypes.MODIFY_DOWN;
                }
                break;
            case R.id.sortby_name_menu:
                if (sortByType == SortByTypes.A_Z_DOWN) {
                    sortByType = SortByTypes.A_Z_UP;
                } else {
                    sortByType = SortByTypes.A_Z_DOWN;
                }
                break;

            }

            setSortByShowType(sortByType);
            sp.edit().putInt(PK.OPTION_SORT_BY, sortByType).commit();
            requery();

            /*mHandler.removeCallbacks(sortByHide);
            mHandler.postDelayed(sortByHide, HIDE_LAYOUT_DURATION);*/

            return true;
        }

        @Override public void onDestroyActionMode(ActionMode mode) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    /*private class SelectionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mSelectionMode = mode;
            adapter.setSelectMode(true);
            adapter.notifyDataSetChanged();
            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);

            toolbar.setVisibility(View.GONE);

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.list_select_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            //listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
            listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
            int num = getSelectedCount();
            mode.setTitle(String.format(getString(R.string.action_mode_select), num));
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            TreeMap<Integer, Note> selectItems = adapter.getSelectItems();
            switch (item.getItemId()) {
            case R.id.delete:
                batchDeleteNote(mode, selectItems);
                break;
            case R.id.share:
                BatchShareNote(mode, selectItems);
                mode.finish();
            default:
                break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            TypedArray array = getTheme().obtainStyledAttributes(new int[] {
                    R.attr.actionBarItemBackground
            });
            listView.setSelector(array.getDrawable(0));
            array.recycle();
            mSelectionMode = null;
            onDeselectAll();
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            requery();
            toolbar.setVisibility(View.VISIBLE);
        }

    }*/

    /*private void onDeselectAll() {
        adapter.clearSelection();
        if (isInSelectionMode()) {
            finishSelectionMode();
        }
    }*/

    public void BatchShareNote(ActionMode mode, TreeMap<Integer, Note> selectItems) {
        StringBuffer sb = new StringBuffer();
        int count = 0;
        for (Integer position : selectItems.keySet()) {
            Note note = selectItems.get(position);
            if (note != null) {
                sb.append(note.content);
                sb.append("\n");
                count++;
            }
            sb.append("\n\n");
        }
        sb.append(getString(R.string.share_by_inotes_content));

        if (count > 0) {
            new ShareUtils(this).shareNotes(sb.toString(), count);
        } else {
            Toast.makeText(this, R.string.no_note_share, Toast.LENGTH_SHORT).show();
        }
        mode.finish();
    }

    public void batchDeleteNote(ActionMode mode, final TreeMap<Integer, Note> selectItems) {
        if (selectItems.size() == 0) {
            Toast.makeText(INotesListActivity.this, R.string.toast_no_item_selected,
                    Toast.LENGTH_LONG).show();
            mode.finish();
            return;
        }

        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PK.DELETE_CONFIRM, true)) {
            showBatchDeleteNotesDialog(mode, selectItems);
        } else {
            for (Note note : selectItems.values()) {
                Note.deleteNote(note, dbHelper);
            }
            mode.finish();
        }

    }

    private void showBatchDeleteNotesDialog(final ActionMode mode,
            final TreeMap<Integer, Note> selectItems) {
        final TreeMap<Integer, Note> selectItemsClone = new TreeMap<Integer, Note>(selectItems);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_confirm_dialog, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.delete_confirm_checkbox);
        //final INotesDialog dialog = new INotesDialog(this, iNotesApplication.getThemeType());
        //final INotesDialog dialog = new INotesDialog(this);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_confirm_delete)
                .setMessage(selectItemsClone.size() > 1 ? R.string.batch_delete_confirm
                        : R.string.delete_confirm)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (checkBox.isChecked()) {
                            Editor editor = PreferenceManager.getDefaultSharedPreferences(
                                    INotesListActivity.this).edit();
                            editor.putBoolean(PK.DELETE_CONFIRM, !checkBox.isChecked()).commit();
                        }
                        for (Note note : selectItemsClone.values()) {
                            Note.deleteNote(note, dbHelper);
                        }
                        requery();
                        for (Note note : selectItemsClone.values()) {
                            if (note.hasSynced()) {
                                startSync(SyncMode.LOCAL_CHANGED);
                                break;
                            }
                        }
                        mode.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void startSyncByResult() {
        Log.i(TAG, "resultCode:" + iNotesApplication.getResultCode());
        switch (iNotesApplication.getResultCode()) {
        case ResultCode.NOTE_LOCAL_CHANGED:
            startSync(SyncMode.LOCAL_CHANGED);
            break;
        case ResultCode.RESET_AUTH:
            startSync(SyncMode.ALL);
            break;
        default:
            break;
        }
    }

    private void startSync(int syncMode) {

        if (syncMode == SyncMode.MANUAL) {

            if (accountManager.isLocalMode()) {
                // TODO show dialog to set account
            } else if (accountManager.isAccountFreezed()) {
                switchToAccount(accountManager.getAccountId());
            } else if (iNotesApplication.isWifiOnly() && !iNotesApplication.isWifiEnable()) {
                showWifiSettingDialog();
            } else {
                if (iNotesApplication.startSync(syncMode)) {
                    updateStatus(SyncStatus.SYNCING);
                }
            }
        } else if (!accountManager.isAccountFreezed()
                && !accountManager.isLocalMode() && iNotesApplication.isNetWorkEnable()) {
            if (iNotesApplication.isWifiOnly() && !iNotesApplication.isWifiEnable()) {
                Toast.makeText(this, getString(R.string.wifi_unabled), Toast.LENGTH_SHORT).show();
                return;
            }
            if (iNotesApplication.startSync(syncMode)) {
                updateStatus(SyncStatus.SYNCING);
            }
        }
    }

    private void showWifiSettingDialog() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.wifi_remind_message)
                .setPositiveButton(R.string.wifi_remind_btn_ok,
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(INotesListActivity.this,
                                        INotesPreferencesSubSync.class);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton(R.string.wifi_remind_btn_cancle, null)
                .show();
    }

    @Override
    public void onSynchronized(boolean result, int syncMode) {
        if (notesSyncManager == null || !notesSyncManager.isSynchronizing()) {
            mHandler.sendEmptyMessage(SyncStatus.SUCCESS);
            return;
        }

        if (result) {
            mHandler.sendEmptyMessage(SyncStatus.SUCCESS);
            Log.i(TAG, "sync finished.");
        } else {
            mHandler.sendEmptyMessage(SyncStatus.ERROR);
            Log.i(TAG, "sync failed.");
        }
    }

    @Override
    public void refreshUI() {
        mHandler.sendEmptyMessage(SyncStatus.SYNCING);
        Log.i(TAG, "update view.");
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            resetCurrentFolder();
            requarySpinnerSelectors();
            requery();
            switch (msg.what) {
            case SyncStatus.SYNCING:
                updateStatus(SyncStatus.SYNCING);
                break;
            case SyncStatus.SUCCESS:
                updateStatus(SyncStatus.SUCCESS);
                break;
            case SyncStatus.ERROR:
                updateStatus(SyncStatus.ERROR);
                break;
            case SyncStatus.TOKEN_TIMEOUT:
                Toast.makeText(INotesListActivity.this, R.string.toast_token_timeout,
                        Toast.LENGTH_SHORT).show();
                updateStatus(SyncStatus.ERROR);
                break;
            }
        }
    };

    private void updateStatus(int status) {

        if (syncItem == null) {
            return;
        }

        switch (status) {
        case SyncStatus.SUCCESS:
            syncItem.setActionView(null);
            break;
        case SyncStatus.SYNCING:
            syncItem.setActionView(R.layout.syncing_progress);
            break;
        case SyncStatus.ERROR:
            syncItem.setActionView(null);
            break;
        }
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (sortByLayout.getVisibility() == View.VISIBLE) {
                mHandler.post(sortByHide);
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }*/

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (sortByLayout.getVisibility() == View.VISIBLE) {
                Rect r = new Rect();
                getViewRectRelativeToSelf(sortByLayout, r);
                if (!r.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    mHandler.removeCallbacks(sortByHide);
                    mHandler.post(sortByHide);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }*/

    /*private void getViewRectRelativeToSelf(View v, Rect r) {
        int[] loc = new int[2];
        v.getLocationOnScreen(loc);
        int left = loc[0];
        int top = loc[1];

        r.set(left, top, left + v.getMeasuredWidth(), top + v.getMeasuredHeight());
    }*/

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
}