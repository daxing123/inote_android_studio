package org.dayup.inotes.views;

import java.util.ArrayList;

import org.dayup.inotes.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class LinedEditText extends EditText {

    class TextHistory {
        public int selectionPosition;
        public String delStr;
        public String addStr;
    }

    public static interface OnEditHistoryChangedListener {
        void editHistoryChangedistener(int position, int size);
    }

    public static interface OnEditModeExitListener {
        void onEditModeExit();
    }

    public static interface OnParagraphStateChangedListener {
        void paragraphStateChangedistener(boolean ParagraphState);
    }

    private Paint mPaint;
    private int lineColors;

    private OnEditModeExitListener onEditModeExitListener = null;
    private boolean setHistory = false;
    private ArrayList<TextHistory> historyList = new ArrayList<TextHistory>();
    private int currentHistoryPosition = 0;
    private boolean justAddTag = false;
    private boolean paragraphState = false;
    private OnEditHistoryChangedListener historySetListener;
    private OnParagraphStateChangedListener paragraphStateListener;
    public final String paragraphStr = " - ";

    public void setOnEditHistoryChangedListener(OnEditHistoryChangedListener editHistorySetListener) {
        this.historySetListener = editHistorySetListener;
    }

    public void setOnEditModeExitListener(OnEditModeExitListener onEditModeExitListener) {
        this.onEditModeExitListener = onEditModeExitListener;
    }

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinedTextView);
        lineColors = a.getInteger(R.styleable.LinedTextView_lineColors, 0x5ac0c0c0);
        a.recycle();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(lineColors);
        setPadding(20, 0, 20, 0);
        this.addTextChangedListener(new NotesEditorCompositeWatcher());
    }

    // public void doEditAction(int actionType) {
    // switch (actionType) {
    // case TaskContainterView.TOOL_ACTION_BACK:
    // if (currentHistoryPosition < historyList.size() && currentHistoryPosition
    // >= 0) {
    // setHistory = true;
    // StringBuffer sb = new StringBuffer(this.getText().toString());
    // StringBuffer newSb = new StringBuffer();
    // TextHistory tmp = historyList.get(currentHistoryPosition);
    // int selection = tmp.selectionPosition;
    // if (tmp.selectionPosition <= sb.length()) {
    // if ("".equals(tmp.delStr)) {// undo add operate
    // newSb.append(sb.subSequence(0, tmp.selectionPosition));
    // newSb.append(sb.subSequence(tmp.selectionPosition + tmp.addStr.length(),
    // sb.length()));
    // } else {
    // if ("".equals(tmp.addStr)) {// undo del operate
    // newSb.append(sb.subSequence(0, tmp.selectionPosition));
    // newSb.append(tmp.delStr);
    // newSb.append(sb.subSequence(tmp.selectionPosition, sb.length()));
    // selection += tmp.delStr.length();
    // } else {// undo update operate
    // StringBuffer tmpSb = new StringBuffer();
    // tmpSb.append(sb.subSequence(0, tmp.selectionPosition));
    // tmpSb.append(sb.subSequence(
    // tmp.selectionPosition + tmp.addStr.length(), sb.length()));
    //
    // newSb.append(tmpSb.subSequence(0, tmp.selectionPosition));
    // newSb.append(tmp.delStr);
    // newSb.append(tmpSb.subSequence(tmp.selectionPosition, tmpSb.length()));
    // selection += tmp.delStr.length();
    // }
    // }
    // currentHistoryPosition--;
    // if (currentHistoryPosition < 0) {
    // currentHistoryPosition = 0;
    // }
    // }
    // setText(newSb.toString());
    // setSelection(selection);
    // }
    // break;
    // case TaskContainterView.TOOL_ACTION_FORWARD:
    // currentHistoryPosition++;
    // if (currentHistoryPosition < historyList.size() && currentHistoryPosition
    // >= 0) {
    // setHistory = true;
    // StringBuffer sb = new StringBuffer(this.getText().toString());
    // StringBuffer newSb = new StringBuffer();
    // TextHistory tmp = historyList.get(currentHistoryPosition);
    // int selection = tmp.selectionPosition;
    // if (tmp.selectionPosition <= sb.length()) {
    // if ("".equals(tmp.delStr)) {// redo add operate
    // newSb.append(sb.subSequence(0, tmp.selectionPosition));
    // newSb.append(tmp.addStr);
    // newSb.append(sb.subSequence(tmp.selectionPosition, sb.length()));
    // selection += tmp.addStr.length();
    // } else {
    // if ("".equals(tmp.addStr)) {// redo del operate
    // newSb.append(sb.subSequence(0, tmp.selectionPosition));
    // newSb.append(sb.subSequence(
    // tmp.selectionPosition + tmp.delStr.length(), sb.length()));
    // } else {// redo update operate
    // StringBuffer tmpSb = new StringBuffer();
    // tmpSb.append(sb.subSequence(0, tmp.selectionPosition));
    // tmpSb.append(sb.subSequence(
    // tmp.selectionPosition + tmp.delStr.length(), sb.length()));
    //
    // newSb.append(tmpSb.subSequence(0, tmp.selectionPosition));
    // newSb.append(tmp.addStr);
    // newSb.append(tmpSb.subSequence(tmp.selectionPosition, tmpSb.length()));
    // selection += tmp.addStr.length();
    // }
    // }
    // }
    // setText(newSb.toString());
    // setSelection(selection);
    // }
    // break;
    // }
    // }

    public void setTagAdded(Boolean flag) {
        justAddTag = flag;
    }

    public void setParagraphStateListener(OnParagraphStateChangedListener paragraphStateListener) {
        this.paragraphStateListener = paragraphStateListener;
    }

    public void setParagraphState(Boolean flag) {
        paragraphState = flag;
        if (paragraphStateListener != null) {
            paragraphStateListener.paragraphStateChangedistener(paragraphState);
        }
    }

    public boolean getParagraphState() {
        return paragraphState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();

        Paint paint = mPaint;
        int height = getHeight();
        int line_height = getLineHeight();
        int page_size = height / line_height;
        if (count < page_size) {
            count = page_size;
        }
        for (int i = 1; i < count; i++) {
            int posY = 0;
            posY = i * line_height;
            canvas.drawLine(getLeft() + 20, posY, getRight() - 20, posY, paint);
        }
        super.onDraw(canvas);
    }

    class NotesEditorCompositeWatcher implements TextWatcher {

        private String beforeStr;
        private int end = 0;
        private boolean paragraphAction = false;
        private int start = 0;
        private int count = 0;
        private int before = 0;

        @Override
        public void afterTextChanged(Editable s) {
            if (!setHistory) {
                paragraphState = lastRowIsParagraph(s);
                int slength = s.length();
                if (paragraphState && !paragraphAction && slength > beforeStr.length()) {
                    if ('\n' == s.charAt(start) && start < slength) {
                        String tmp = "\n" + paragraphStr + "\n";
                        int tmpStart = start - tmp.length() + 1;
                        int tmpEnd = start + 1;
                        if (tmpStart >= 0 && tmpEnd <= slength
                                && tmp.equals(s.subSequence(tmpStart, tmpEnd).toString())) {
                            for (int i = start - 1; i >= 0; i--) {
                                if ('\n' == s.charAt(i)) {
                                    paragraphAction = true;
                                    getText().replace(i, start, "").toString();
                                    paragraphState = false;
                                    if (paragraphStateListener != null) {
                                        paragraphStateListener
                                                .paragraphStateChangedistener(paragraphState);
                                    }
                                    return;
                                }
                            }
                        } else {
                            paragraphAction = true;
                            getText().replace(start, start + 1, "\n" + paragraphStr).toString();
                            setSelection(start + 1 + paragraphStr.length());
                            return;
                        }
                    }
                }
                paragraphAction = false;

                if (currentHistoryPosition < historyList.size() - 1 && currentHistoryPosition >= 0) {
                    for (int i = historyList.size() - 1; i > currentHistoryPosition; i--) {
                        historyList.remove(i);
                    }
                }
                if (!beforeStr.equals(s.toString())) {
                    TextHistory tmp = new TextHistory();
                    tmp.selectionPosition = start;
                    if (before == 0 && count != 0) {// add operate
                        tmp.delStr = "";
                        tmp.addStr = s.subSequence(start, start + count).toString();
                    } else if (before != 0 && count == 0) {// del operate
                        tmp.delStr = beforeStr.substring(start, start + before);
                        tmp.addStr = "";
                    } else {// update operate
                        tmp.delStr = beforeStr.substring(start, start + before);
                        tmp.addStr = s.subSequence(start, start + count).toString();
                    }
                    historyList.add(tmp);
                    currentHistoryPosition = historyList.size() - 1;
                } else if (historyList.size() == 0) {
                    TextHistory tmp = new TextHistory();
                    tmp.selectionPosition = start;
                    tmp.delStr = "";
                    tmp.addStr = "";
                    historyList.add(tmp);
                    currentHistoryPosition = historyList.size() - 1;
                }
            } else {
                setHistory = false;
            }
            end = start + count;
            historySetListener
                    .editHistoryChangedistener(currentHistoryPosition, historyList.size());
            if (justAddTag) {
                setSelection(end + 2);
                justAddTag = false;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeStr = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            this.start = start;
            this.before = before;
            this.count = count;
        }

        private boolean lastRowIsParagraph(Editable s) {
            int slength = s.length();
            if (start >= slength)
                return false;
            if ('\n' == s.charAt(start)) {
                int lastRowStart = 0;
                for (int i = start - 1; i >= 0; i--) {
                    if ('\n' == s.charAt(i)) {
                        lastRowStart = i;
                        break;
                    }
                }
                int tmpStart = lastRowStart == 0 ? lastRowStart : lastRowStart + 1;
                int tmpEnd = tmpStart + paragraphStr.length();
                if (tmpStart < slength && tmpEnd <= slength
                        && paragraphStr.equals(s.subSequence(tmpStart, tmpEnd).toString())) {
                    return true;
                }
            }
            return false;
        }
    }

    private void editHistoryChanged(int position, int size) {
        if (historySetListener != null) {
            historySetListener.editHistoryChangedistener(position, size);
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // TODO hard keyboard?
            if (onEditModeExitListener != null) {
                onEditModeExitListener.onEditModeExit();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
}