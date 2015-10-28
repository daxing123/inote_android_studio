package org.dayup.inotes;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.*;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import org.dayup.activities.BaseActivity;
import org.dayup.activities.DialogHandler;
import org.dayup.common.Analytics;
import org.dayup.common.Log;
import org.dayup.inotes.INotesPreferences.PK;
import org.dayup.inotes.account.INotesAccountManager;
import org.dayup.inotes.constants.Constants.ResultCode;
import org.dayup.inotes.data.Folder;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.db.Field.Status;
import org.dayup.inotes.edit.INoteEditController;
import org.dayup.inotes.edit.INoteViewerController;
import org.dayup.inotes.edit.NoteEditController;
import org.dayup.inotes.edit.NoteViewerController;
import org.dayup.inotes.utils.DateUtils;
import org.dayup.inotes.utils.DateUtils.DatePattern;
import org.dayup.inotes.utils.ShareUtils;
import org.dayup.inotes.utils.Utils;
import org.dayup.inotes.views.INotesDialog;
import org.dayup.inotes.views.INotesDialog.INotesDialogListItemOnClickListener;
import org.dayup.inotes.views.ResizeLayout;

import java.text.DateFormat;
import java.util.Collections;
import java.util.List;

public class INotesDetailActivity extends BaseActivity implements INoteViewerController,
        INoteEditController {

    private final String TAG = INotesDetailActivity.class.getSimpleName();

    public static final String NOTE_ID = "note_id";
    public static final String FOLDER_ID = "folder_id";

    private INotesAccountManager accountManager;

    private Note note;
    private long folderId;
    private DateFormat dateFormat;

    //private ActionBar actionBar;
    private Toolbar toolbar;
    private NoteEditController noteEditController;
    private NoteViewerController noteViewerController;
    private TextView timeText;
    private ResizeLayout resizeLayout;

    private boolean needSave = false;
    private boolean hasMoveTo = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountManager = iNotesApplication.getAccountManager();

        setContentView(R.layout.activity_inotes_detail);
        init();
        //initActionBar();
        initToolbar();
        initView();
        parseIntentData();

    }

    private void init() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dateFormat = android.text.format.DateFormat.getDateFormat(this);
    }

    private void parseIntentData() {
        Intent mIntent = getIntent();
        StringBuffer noteString = new StringBuffer();
        if (Intent.ACTION_SEND.equals(mIntent.getAction())) {
            String subject = mIntent.getStringExtra(Intent.EXTRA_SUBJECT);
            CharSequence text = mIntent.getCharSequenceExtra(Intent.EXTRA_TEXT);

            if (!TextUtils.isEmpty(subject)) {
                if (TextUtils.isEmpty(text)) {
                    noteString.append(subject);
                    noteString.append("\n");
                } else if ((!TextUtils.isEmpty(text) && !text.toString().trim().startsWith(subject))) {
                    noteString.append(subject);
                    noteString.append("\n");
                }
            }

            if (!TextUtils.isEmpty(text)) {
                noteString.append(text);
            }
        } else {
            String id = getIntent().getStringExtra(NOTE_ID);
            long fId = getIntent().getLongExtra(FOLDER_ID, Folder.ALL_FOLDER_ID);
            folderId = fId == Folder.ALL_FOLDER_ID ? accountManager.getTopLabelId() : fId;
            if (!TextUtils.isEmpty(id)) {
                note = Note.getNoteById(id, iNotesApplication.getDBHelper());
            }
        }
        if (note == null) {
            note = new Note();
            note.content = noteString.toString();
            displayNote(note, true);
            // timeText.setVisibility(View.GONE);
        } else {
            displayNote(note, false);
        }
    }

    public void displayNote(Note note, boolean isEditing) {
        Log.d(TAG, "displayNote");
        CharSequence content = note.content;
        if (isEditing) {
            switchToEdit(content);
        } else {
            switchToViewer(content);
        }

        toolbar.setTitle(getCurrentFolderName());
        setDateTimeText();
    }

    private void switchToEdit(CharSequence content) {
        needSave = true;
        noteViewerController.hide();
        noteEditController.show(content);
    }

    private void switchToViewer(CharSequence content) {
        noteEditController.hideInputMethodManager();
        noteEditController.hide();
        noteViewerController.show(content);
        noteViewerController.setSelectionStart(noteEditController.getSelectionStart());
    }

    private void setDateTimeText() {
        StringBuffer sb = new StringBuffer();
        sb.append(getString(R.string.detail_last_edit)).append(" ");
        sb.append(getDateText()).append(", ")
                .append(DateUtils.formatTime(note.modifiedTime, DatePattern.HM_COLON_12));
        timeText.setText(sb.toString());
    }

    private String getDateText() {
        if (note.modifiedTime == 0) {
            note.modifiedTime = System.currentTimeMillis();
        }
        // long days = DateUtils.getCurrentDiff(new Date(note.modifiedTime));
        // if (days < -1) {
        // return String.format(getString(R.string.date_n_days_before),
        // Math.abs(days) + "");
        // } else if (days == -1) {
        // return getString(R.string.date_yesterday);
        // } else if (days == 0) {
        // return getString(R.string.date_today);
        // } else {
        // return dateFormat.format(note.modifiedTime);
        // }
        return dateFormat.format(note.modifiedTime);
    }

    private void initView() {
        View layout = findViewById(R.id.detail_layout);
        timeText = (TextView) findViewById(R.id.time_text);
        noteEditController = new NoteEditController(this, layout, this);
        noteViewerController = new NoteViewerController(layout, this);
        noteViewerController.setViewerLocaterListener(noteEditController);
        resizeLayout = (ResizeLayout) findViewById(R.id.detail_scroll_layout);
    }

    /*private void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }*/

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setTitle("iNotes");
        setSupportActionBar(toolbar);//这句得在getSupport之前
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //ToolBar显示返回按钮
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.save); //ToolBar自定义返回按钮图标
    }

    private String getCurrentFolderName() {
        if (folderId != Folder.ALL_FOLDER_ID) {
            Folder folder = Folder.getFolderWithDisplaynameById(folderId,
                    iNotesApplication.getCurrentAccountId(), dbHelper);
            if (folder != null) {
                return folder.displayName;
            }
        }
        return getString(R.string.folder_all);
    }

    @Override
    protected void onResume() {
        super.onResume();
        iNotesApplication.setResultCode(ResultCode.NO_CHANGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inotes_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.menu_insert:
            insertNote();
            return true;
        case R.id.menu_discard:
            needSave = false;
            finish();
            return true;
        case R.id.menu_share:
            shareNote();
            return true;
        case R.id.menu_move_to:
            moveNoteTo();
            return true;
        case R.id.menu_delete:
            deleteNote();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }

    }

    private void insertNote() {
        Intent i = new Intent(this, INotesDetailActivity.class);
        i.putExtra(INotesDetailActivity.NOTE_ID, "");
        i.putExtra(INotesDetailActivity.FOLDER_ID, folderId);
        startActivity(i);
        finish();
    }

    private String getNoteTextContent() {
        String content = "";
        if (noteEditController.isShown()) {
            content = noteEditController.getContent().toString();
        } else {
            content = noteViewerController.getContent().toString();
        }
        return content;
    }

    private void shareNote() {
        new ShareUtils(this).shareNote(getNoteTextContent() + "\n\n"
                + getString(R.string.share_by_inotes_content));
    }

    private void moveNoteTo() {
        if (isMoveAllowed()) {
            new MoveToFolderDialogHandler(this).show();
        } else {
            Toast.makeText(INotesDetailActivity.this, R.string.toast_move_to_forbid,
                    Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isMoveAllowed() {
        if (TextUtils.isEmpty(note.id)) {
            return true;
        }
        Note n = Note.getNoteById(note.id, dbHelper);
        if (n == null) {
            return true;
        }
        note = n;
        if (iNotesApplication.getSyncManager().isSynchronizing() && note.isLocalAdded()) {
            return false;
        }
        return true;

    }

    private void deleteNote() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PK.DELETE_CONFIRM, true)) {
            showDeleteConfirmDialog();
        } else {
            note.deleted = Status.DELETED_YES;
            needSave = true;
            finish();
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNote();
    }

    private void saveNote() {
        if (!needSave) {
            return;
        }

        long accountId = iNotesApplication.getAccountManager().getAccountId();
        if (needDeleteNote()) {
            if (note.hasSynced()) {
                Note.deleteNotesByIdLogical(note.id, accountId, dbHelper);
            } else {
                Note.deleteNoteByIdForever(note.id, accountId, dbHelper);
            }
            iNotesApplication.setResultCode(ResultCode.NOTE_LOCAL_CHANGED);
            return;
        }

        if (needNewNote()) {
            note.accountId = accountId;
            note.folderId = folderId == Folder.ALL_FOLDER_ID ? accountManager.getTopLabelId()
                    : folderId;
            note.content = getNoteTextContent();
            note.status = Status.SYNC_NEW;
            note.id = Utils.getRandomUUID36();
            Note.createNote(note, dbHelper);
            iNotesApplication.setResultCode(ResultCode.NOTE_LOCAL_CHANGED);
            return;
        }

        if (needUpdateNote()) {
            if (hasMoveTo) {
                Note.moveToFolder(note, folderId, dbHelper);
                iNotesApplication.setResultCode(ResultCode.NOTE_LOCAL_CHANGED);
            } else {
                note.folderId = folderId == Folder.ALL_FOLDER_ID ? accountManager.getTopLabelId()
                        : folderId;
                note.content = getNoteTextContent();
                Note.updateNote(note, dbHelper);
                iNotesApplication.setResultCode(ResultCode.NOTE_LOCAL_CHANGED);
            }
            return;
        }

    }

    private boolean needUpdateNote() {
        if (note.deleted == Status.DELETED_YES) {
            return false;
        }
        if (TextUtils.isEmpty(note.id)) {
            return false;
        }
        if (!note.content.equals(getNoteTextContent())) {
            return true;
        }
        if (!TextUtils.equals(note.content, getNoteTextContent())) {
            return true;
        }
        if (note.folderId != folderId) {
            return true;
        }
        return false;
    }

    private boolean needNewNote() {
        if (note.deleted == Status.DELETED_YES) {
            return false;
        }
        return TextUtils.isEmpty(note.id) && !TextUtils.isEmpty(getNoteTextContent());
    }

    private boolean needDeleteNote() {
        if (note.deleted == Status.DELETED_YES) {
            return true;
        }
        return !TextUtils.isEmpty(note.id) && TextUtils.isEmpty(getNoteTextContent());
    }

    private void showDeleteConfirmDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.delete_confirm_dialog, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.delete_confirm_checkbox);
        //final INotesDialog dialog = new INotesDialog(this, iNotesApplication.getThemeType());
        final INotesDialog dialog = new INotesDialog(this);
        dialog.setTitle(R.string.dialog_title_confirm_delete);
        dialog.setMessage(R.string.delete_confirm);
        dialog.setView(view);
        dialog.setPositiveButton(android.R.string.ok, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(
                            INotesDetailActivity.this).edit();
                    editor.putBoolean(PK.DELETE_CONFIRM, !checkBox.isChecked()).commit();
                }
                note.deleted = Status.DELETED_YES;
                needSave = true;
                dialog.dismiss();
                finish();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }

    private class MoveToFolderDialogHandler extends DialogHandler {
        private String[] folderName = null;
        private long[] folderIds = null;
        private int selected = -1;

        public MoveToFolderDialogHandler(BaseActivity activity) {
            super(activity);
        }

        @Override
        public Dialog onCreateDialog() {
            if (!getDataFromDB()) {
                Toast.makeText(INotesDetailActivity.this, R.string.toast_move_to_no_folder,
                        Toast.LENGTH_LONG).show();
                return null;
            }

            return createDialog();
        }

        private Dialog createDialog() {
            //INotesDialog dialog = new INotesDialog(INotesDetailActivity.this, iNotesApplication.getThemeType());
            INotesDialog dialog = new INotesDialog(INotesDetailActivity.this);
            dialog.setTitle(R.string.dialog_title_move_to_folder);
            dialog.setSingleChoiceItems(folderName, selected,
                    new INotesDialogListItemOnClickListener() {

                        @Override
                        public void onClick(Dialog dialog, int position) {
                            if (folderId == folderIds[position]) {
                                return;
                            }
                            Folder to = Folder.getFolderWithDisplaynameById(folderIds[position],
                                    iNotesApplication.getCurrentAccountId(), dbHelper);
                            if (to == null) {
                                Toast.makeText(INotesDetailActivity.this,
                                        "Fail: Can not be moved to a non-existing folder!",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            moveToList(to);
                            needSave = true;
                            hasMoveTo = true;
                            dialog.dismiss();
                        }

                    });
            return dialog;
        }

        private void moveToList(Folder to) {
            toolbar.setTitle(to.displayName);
            folderId = to.id;
            accountManager.setDefaultFolderId(folderId);
        }

        private boolean getDataFromDB() {
            List<Folder> folders = Folder.getAllFoldersWithDisplayNameByAccountId(iNotesApplication
                    .getAccountManager().getAccountId(), null, null, null, dbHelper);
            Collections.sort(folders, Folder.folderComparatorPosition);
            int listSize = folders.size();
            if (listSize == 0) {
                return false;
            }
            folderName = new String[listSize];
            folderIds = new long[listSize];
            for (int i = 0, size = listSize; i < size; i++) {
                folderName[i] = folders.get(i).displayName;
                folderIds[i] = folders.get(i).id;
                if (folderIds[i] == folderId) {
                    selected = i;
                }
            }
            return true;
        }

    }

    @Override
    public void switchToViewer() {
        switchToViewerMethod();
    }

    // for switchToViewer and onAttachCountClickListener
    private void switchToViewerMethod() {
        if (noteEditController.isShown()) {
            switchToViewer(noteEditController.getContent());
        }
    }

    @Override
    public void switchToEdit() {
        needSave = true;
        if (noteViewerController.isShown()) {
            switchToEdit(noteViewerController.getContent());
        }
    }

    @Override
    public void onAttachCountClickListener(View v) {
        switchToViewerMethod();
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
}
