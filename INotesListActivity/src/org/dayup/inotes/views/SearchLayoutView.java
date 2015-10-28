package org.dayup.inotes.views;

import org.dayup.inotes.R;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView.OnEditorActionListener;

public class SearchLayoutView extends LinearLayout {
    private EditText searchTitleEt = null;
    private ImageView searchClear = null;
    private ImageView searchRecogniz;
    private View searchArea;
    private View searchPlate;
    private Context mContext;

    public SearchLayoutView(Context context) {
        this(context, null);
    }

    public SearchLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.search_view, this, true);
        searchTitleEt = (EditText) findViewById(R.id.search_et);
        searchTitleEt.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                postUpdateFocusedState();
            }
        });
        searchClear = (ImageView) findViewById(R.id.edit_clear_btn);
        searchClear.setVisibility(View.GONE);
        searchRecogniz = (ImageView) findViewById(R.id.search_recogniz);
        searchPlate = findViewById(R.id.search_plate);
        searchArea = findViewById(R.id.recogniz_area);
        searchClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                searchTitleEt.setText("");
            }
        });
        searchTitleEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    searchClear.setVisibility(View.VISIBLE);
                    searchArea.setVisibility(View.GONE);
                } else {
                    searchClear.setVisibility(View.GONE);
                    searchArea.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setRecognizClick(OnClickListener l) {
        searchRecogniz.setOnClickListener(l);
    }

    public void setTitleOnEditorActionListener(OnEditorActionListener l) {
        searchTitleEt.setOnEditorActionListener(l);
    }

    public void setQuickAddWidgetsStatus(boolean enabled) {
        searchTitleEt.setEnabled(enabled);
    }

    public EditText getTitleEdit() {
        return searchTitleEt;
    }

    public void setTitleText(CharSequence text) {
        searchTitleEt.setText(text);
        try {
            searchTitleEt.setSelection(searchTitleEt.getText().length());
        } catch (Exception e) {
        }
    }

    public View getRecognizArea() {
        return searchArea;
    }

    public void appendTitleText(String str) {
        int start = searchTitleEt.getSelectionStart();
        searchTitleEt.getText().replace(start, searchTitleEt.getSelectionEnd(), str);
        searchTitleEt.setSelection(start + str.length());
    }

    public void showInputSoft() {
        searchTitleEt.requestFocus();
        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(searchTitleEt, 0);
    }

    public void hideInputSoft() {
        if (searchTitleEt != null) {
            ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(searchTitleEt.getWindowToken(), 0);
        }
    }

    public String getTitleText() {
        return searchTitleEt.getText().toString();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        postUpdateFocusedState();
    }

    private void postUpdateFocusedState() {
        post(mUpdateDrawableStateRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(mUpdateDrawableStateRunnable);
        super.onDetachedFromWindow();
    }

    private Runnable mUpdateDrawableStateRunnable = new Runnable() {
        public void run() {
            updateFocusedState();
        }
    };

    private void updateFocusedState() {
        boolean focused = searchTitleEt.hasFocus();
        searchPlate.getBackground().setState(focused ? FOCUSED_STATE_SET : EMPTY_STATE_SET);
        searchArea.getBackground().setState(focused ? FOCUSED_STATE_SET : EMPTY_STATE_SET);
        invalidate();
    }
}
