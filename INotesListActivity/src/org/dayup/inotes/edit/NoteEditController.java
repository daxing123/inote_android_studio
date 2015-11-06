package org.dayup.inotes.edit;

import java.sql.Date;

import org.dayup.common.Log;
import org.dayup.inotes.R;
import org.dayup.inotes.utils.AudioUtils;
import org.dayup.inotes.utils.DateUtils;
import org.dayup.inotes.utils.DateUtils.DatePattern;
import org.dayup.inotes.views.AutoLinkEditText;
import org.dayup.inotes.views.AutoLinkEditText.AutoLinkEditListener;
import org.dayup.inotes.views.AutoLinkEditText.OnEditModeExitListener;
import org.dayup.inotes.views.LocaterTextView;
import org.dayup.inotes.views.WatcherEditText;
import org.dayup.inotes.views.WatcherEditText.OnEditHistoryChangedListener;

import android.content.Context;
import android.text.Editable;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class NoteEditController implements AutoLinkEditListener, LocaterTextView.LocaterListener {
    private static final String TAG = NoteEditController.class.getSimpleName();
    private WatcherEditText noteEditView;
    private View toolbar;
    private ImageView autoLinkIV;
    private InputMethodManager imm;
    private final INoteEditController iNoteEditController;
    private final Context context;
    private int textOffset = 0;
    private String timeFormat = DatePattern.HM_COLON_12;
    private ImageButton undoBtn;
    private ImageButton redoBtn;
    private TextView timeText;

    public NoteEditController(Context context, View mainView,
            INoteEditController iNoteEditController) {
        timeFormat = android.text.format.DateFormat.is24HourFormat(context) ? DatePattern.HM_COLON_24
                : DatePattern.HM_COLON_12;

        this.imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.context = context;
        this.iNoteEditController = iNoteEditController;
        noteEditView = (WatcherEditText) mainView.findViewById(R.id.note_editor_composite);
        toolbar = mainView.findViewById(R.id.edit_tool_bar);
        initToolBar(toolbar);
        timeText = (TextView) mainView.findViewById(R.id.time_text);
        autoLinkIV = (ImageView) mainView.findViewById(R.id.detail_autolink_btn);

        noteEditView.setAutoLinkListener(this);
        noteEditView.setOnEditModeExitListener(new OnNoteEditModeExitListener(iNoteEditController));
        noteEditView.setOnEditHistoryChangedListener(new OnEditHistoryChangedListener() {

            @Override
            public void editHistoryChangedistener(int currentHistoryPosition, int historySize) {
                setToolActionBtnEnabled(currentHistoryPosition, historySize);
            }
        });


    }

    public Editable getContent() {
        return noteEditView.getText();
    }

    public WatcherEditText getEditText() {
        return noteEditView;
    }

    public boolean isShown() {
        return noteEditView.getVisibility() == View.VISIBLE;
    }

    public void show(CharSequence content) {
        noteEditView.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        timeText.setVisibility(View.GONE);

        noteEditView.showAndSetText(content);
        int textLength = noteEditView.getText().length();
        if (textOffset > textLength) {
            textOffset = textLength;
        }
        noteEditView.requestFocus();
        noteEditView.setSelection(textOffset);
        showInputMethodManager();
    }

    public int getSelectionStart() {
        return noteEditView.getSelectionStart();
    }

    public void hide() {
        noteEditView.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        timeText.setVisibility(View.VISIBLE);
        hideAutoLinkBtn();
    }

    public void hideInputMethodManager() {
        imm.hideSoftInputFromWindow(noteEditView.getWindowToken(), 0);
    }

    private void showInputMethodManager() {
        noteEditView.postDelayed(new Runnable() {

            @Override
            public void run() {
                imm.showSoftInput(noteEditView, 0);
            }
        }, 100);
    }

    @Override
    public void showAutoLinkBtn(int linkType, final URLSpan url) {
        autoLinkIV.setVisibility(View.VISIBLE);
        switch (linkType) {
        case AutoLinkEditText.AUTOLINK_TYPE_TEL:
            autoLinkIV.setImageResource(R.drawable.autolink_tel);
            break;
        case AutoLinkEditText.AUTOLINK_TYPE_EMAIL:
            autoLinkIV.setImageResource(R.drawable.autolink_mail);
            break;
        case AutoLinkEditText.AUTOLINK_TYPE_WEB:
        default:
            autoLinkIV.setImageResource(R.drawable.autolink_web);
            break;
        }
        autoLinkIV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                url.onClick(v);
                iNoteEditController.switchToViewer();
            }
        });
    }

    @Override
    public void hideAutoLinkBtn() {
        autoLinkIV.setVisibility(View.GONE);
    }

    @Override
    public void selection(int off) {
        textOffset = off;
    }

    public void soundRecognizResult(String str) {
        int start = noteEditView.getSelectionStart();
        noteEditView.getText().replace(start, noteEditView.getSelectionEnd(), str);
        noteEditView.setSelection(start + str.length());
        showInputMethodManager();
    }

    private static class OnNoteEditModeExitListener implements OnEditModeExitListener {
        private final INoteEditController iNoteEditController;

        public OnNoteEditModeExitListener(INoteEditController iNoteEditController) {
            this.iNoteEditController = iNoteEditController;
        }

        @Override
        public void onEditModeExit() {
            iNoteEditController.switchToViewer();
        }

    }

    private void initToolBar(View toolbar) {
        ImageButton paragraphBtn = (ImageButton) toolbar.findViewById(R.id.detail_tool_paragraph);
        ImageButton dateBtn = (ImageButton) toolbar.findViewById(R.id.detail_tool_date);
        ImageButton timeBtn = (ImageButton) toolbar.findViewById(R.id.detail_tool_time);
        ImageButton recognizBtn = (ImageButton) toolbar.findViewById(R.id.detail_tool_recogniz);

        undoBtn = (ImageButton) toolbar.findViewById(R.id.detail_tool_back);
        redoBtn = (ImageButton) toolbar.findViewById(R.id.detail_tool_forward);

        paragraphBtn.setOnClickListener(new AddParagraphListener(noteEditView));
        dateBtn.setOnClickListener(new AddDateListener(context, noteEditView));
        timeBtn.setOnClickListener(new AddTimeListener(timeFormat, noteEditView));
        if (AudioUtils.checkRecAvailable(context)) {
            recognizBtn.setOnClickListener(new SoundRecogniz(iNoteEditController));
        } else {
            recognizBtn.setVisibility(View.GONE);
        }
        undoBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                noteEditView.setTagAdded(false);
                noteEditView.undoAction();
            }
        });

        redoBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                noteEditView.setTagAdded(false);
                noteEditView.redoAction();
            }
        });

        toolbar.findViewById(R.id.detail_attach_count_layout).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        iNoteEditController.onAttachCountClickListener(v);
                    }
                });
        Log.d(TAG, "############## initToolbar #############");
        setToolActionBtnEnabled(0, 0);
    }

    private void setToolActionBtnEnabled(int currentHistoryPosition, int historySize) {
        Log.d(TAG, "setToolActionBtnEnabled: currentHistoryPosition = " + currentHistoryPosition
                + ", historySize = " + historySize);
        if (historySize > 1) {// initialization had one history
            if (currentHistoryPosition >= historySize - 1) {
                redoBtn.setEnabled(false);
                undoBtn.setEnabled(true);
            } else if (currentHistoryPosition <= 0) {
                undoBtn.setEnabled(false);
                redoBtn.setEnabled(true);
            } else {
                redoBtn.setEnabled(true);
                undoBtn.setEnabled(true);
            }
        } else {
            redoBtn.setEnabled(false);
            undoBtn.setEnabled(false);
        }
    }

    private static class AddParagraphListener implements OnClickListener {
        private WatcherEditText editText;

        public AddParagraphListener(WatcherEditText editText) {
            this.editText = editText;
        }

        @Override
        public void onClick(View v) {
            if (checkLindHead()) {
                editText.setParagraphState(false);
                return;
            }
            if (!editText.getParagraphState()) {
                int position = editText.getSelectionStart();
                for (int i = editText.getSelectionStart() - 1; i >= 0; i--) {
                    if ('\n' == editText.getText().charAt(i) || i == 0) {
                        position = (i == 0) ? 0 : i + 1;
                        break;
                    }
                }
                editText.getText().replace(position, position, editText.paragraphStr);
                int end = editText.getSelectionStart();
                for (int i = editText.getSelectionStart() - 1, j = editText.getText().length(); i < j; i++) {
                    if ('\n' == editText.getText().charAt(i) || i == j - 1) {
                        end = (i == j - 1) ? j : i;
                        break;
                    }
                }
                editText.setSelection(end);
                editText.setParagraphState(true);
            } else {
                editText.setParagraphState(false);
            }
        }

        private boolean checkLindHead() {
            int lineHeader = editText.getSelectionStart();
            for (int i = editText.getSelectionStart() - 1; i >= 0; i--) {
                if ('\n' == editText.getText().charAt(i) || i == 0) {
                    lineHeader = (i == 0) ? 0 : i + 1;
                    break;
                }
            }
            int end = lineHeader + editText.paragraphStr.length();
            if (editText.length() >= end
                    && editText.paragraphStr.equals(editText.getText().subSequence(lineHeader, end)
                            .toString())) {
                editText.getText().replace(lineHeader, end, "");
                return true;
            }
            return false;
        }

    }

    private static class AddDateListener implements OnClickListener {
        private EditText editText;
        private Context context;

        public AddDateListener(Context context, EditText editText) {
            this.editText = editText;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            int start = editText.getSelectionStart();
            StringBuffer addStr = new StringBuffer();
            addStr.append(android.text.format.DateFormat.getDateFormat(context).format(
                    System.currentTimeMillis()));
            addStr.append(" ");
            int selection = start + addStr.length();
            editText.getText().replace(start, editText.getSelectionEnd(), addStr);
            editText.setSelection(selection);
        }

    }

    private static class AddTimeListener implements OnClickListener {
        private EditText editText;
        private String timeFormat;

        public AddTimeListener(String timeFormat, EditText editText) {
            this.editText = editText;
            this.timeFormat = timeFormat;
        }

        @Override
        public void onClick(View v) {
            int start = editText.getSelectionStart();
            StringBuffer addStr = new StringBuffer();
            addStr.append(DateUtils.formatTime(new Date(System.currentTimeMillis()), timeFormat));
            addStr.append(" ");
            int selection = start + addStr.length();
            editText.getText().replace(start, editText.getSelectionEnd(), addStr);
            editText.setSelection(selection);
        }

    }

}
