package org.dayup.inotes.views;

import java.util.HashMap;
import java.util.Map;

import org.dayup.common.Log;

import android.content.Context;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.EditText;

public class AutoLinkEditText extends EditText {

    private final String TAG = AutoLinkEditText.class.getSimpleName();

    private static final String SCHEME_TEL = "tel:";
    private static final String SCHEME_HTTP = "http:";
    private static final String SCHEME_EMAIL = "mailto:";

    public static final int AUTOLINK_TYPE_TEL = 1;
    public static final int AUTOLINK_TYPE_WEB = 2;
    public static final int AUTOLINK_TYPE_EMAIL = 3;
    public static final int AUTOLINK_TYPE_OTHER = 4;

    private NotesEditorWatcher watcher;

    private static final Map<String, Integer> sSchemaActionResMap = new HashMap<String, Integer>();
    static {
        sSchemaActionResMap.put(SCHEME_TEL, AUTOLINK_TYPE_TEL);
        sSchemaActionResMap.put(SCHEME_HTTP, AUTOLINK_TYPE_WEB);
        sSchemaActionResMap.put(SCHEME_EMAIL, AUTOLINK_TYPE_EMAIL);
    }

    private AutoLinkEditListener listener;
    private OnEditModeExitListener onEditModeExitListener = null;

    public void setAutoLinkListener(AutoLinkEditListener listener) {
        this.listener = listener;
    }

    public interface AutoLinkEditListener {
        void showAutoLinkBtn(int linkType, URLSpan url);

        void hideAutoLinkBtn();
    }

    public static interface OnEditModeExitListener {
        void onEditModeExit();
    }

    public void setOnEditModeExitListener(OnEditModeExitListener onEditModeExitListener) {
        this.onEditModeExitListener = onEditModeExitListener;
    }

    public AutoLinkEditText(Context context) {
        super(context, null);
        init(context);
    }

    public AutoLinkEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
        init(context);
    }

    public AutoLinkEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.setLinksClickable(false);
        this.setAutoLinkMask(Linkify.ALL);
        watcher = new NotesEditorWatcher();
    }

    public void setAutoLinkWatcher(boolean flag) {
        if (flag) {
            this.addTextChangedListener(watcher);
        } else {
            this.removeTextChangedListener(watcher);
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (selStart != selEnd && listener != null) {
            listener.hideAutoLinkBtn();
        } else {
            checkPosition();
        }
    }

    class NotesEditorWatcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            Linkify.addLinks(AutoLinkEditText.this, Linkify.ALL);
            checkPosition();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && getSelectionStart() == getSelectionEnd()) {

            int x = (int) event.getX();
            int y = (int) event.getY();
            x -= getTotalPaddingLeft();
            y -= getTotalPaddingTop();
            x += getScrollX();
            y += getScrollY();

            Layout layout = getLayout();
            if (layout != null) {
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                Selection.setSelection(getText(), off);
            }
        }
        Log.d(TAG, "onTouchEvent()");
        return super.onTouchEvent(event);
    }

    private void checkPosition() {
        if (listener != null) {
            if (getText() instanceof Spanned) {
                int selStart = getSelectionStart();
                int selEnd = getSelectionEnd();

                int min = Math.min(selStart, selEnd);
                int max = Math.max(selStart, selEnd);

                final URLSpan[] urls = ((Spanned) getText()).getSpans(min, max, URLSpan.class);
                if (urls.length == 1) {
                    int linkType = AUTOLINK_TYPE_OTHER;
                    for (String schema : sSchemaActionResMap.keySet()) {
                        if (urls[0].getURL().indexOf(schema) >= 0) {
                            linkType = sSchemaActionResMap.get(schema);
                            break;
                        }
                    }

                    listener.showAutoLinkBtn(linkType, urls[0]);
                } else {
                    listener.hideAutoLinkBtn();
                }
            }
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {

        // Log.d("GoogleTaskEditor2Activity", "onKeyPreIme - " + keyCode + ", "
        // + event + ", " + isInEditMode());
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // hard keyboard? TODO
            if (onEditModeExitListener != null) {
                onEditModeExitListener.onEditModeExit();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
