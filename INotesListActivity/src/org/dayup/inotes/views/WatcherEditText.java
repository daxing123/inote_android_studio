package org.dayup.inotes.views;

import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.AttributeSet;

public class WatcherEditText extends AutoLinkEditText {
    private static final String TAG = WatcherEditText.class.getSimpleName();
    private boolean recordedInHistory = true;
    private boolean isQueryState = false;
    private ArrayList<TextHistory> historyList = new ArrayList<TextHistory>();
    private int currentHistoryPosition = 0;
    private boolean justAddTag = false;
    private boolean paragraphState = false;

    public final String paragraphStr = " - ";

    class TextHistory {
        public int selectionPosition;
        public String delStr;
        public String addStr;
    }

    public WatcherEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.addTextChangedListener(new NotesEditorCompositeWatcher());
        this.setLinksClickable(false);
        this.setAutoLinkMask(Linkify.ALL);
    }

    public static interface OnEditHistoryChangedListener {
        void editHistoryChangedistener(int position, int size);
    }

    private OnEditHistoryChangedListener historySetListener;

    public static interface OnParagraphStateChangedListener {
        void paragraphStateChangedistener(boolean ParagraphState);
    }

    private OnParagraphStateChangedListener paragraphStateListener;

    public void setOnEditHistoryChangedListener(OnEditHistoryChangedListener editHistorySetListener) {
        this.historySetListener = editHistorySetListener;
    }

    public void setRecordedInHistory(boolean flag) {
        recordedInHistory = flag;
    }

    public void setQueryState(boolean flag) {
        isQueryState = flag;
    }

    public void undoAction() {
        if (currentHistoryPosition < historyList.size() && currentHistoryPosition >= 0) {
            recordedInHistory = false;
            StringBuffer sb = new StringBuffer(this.getText().toString());
            StringBuffer newSb = new StringBuffer();
            TextHistory tmp = historyList.get(currentHistoryPosition);
            int selection = tmp.selectionPosition;
            if (tmp.selectionPosition <= sb.length()) {
                if ("".equals(tmp.delStr)) {// undo add operate
                    newSb.append(sb.subSequence(0, tmp.selectionPosition));
                    newSb.append(sb.subSequence(tmp.selectionPosition + tmp.addStr.length(),
                            sb.length()));
                } else {
                    if ("".equals(tmp.addStr)) {// undo del operate
                        newSb.append(sb.subSequence(0, tmp.selectionPosition));
                        newSb.append(tmp.delStr);
                        newSb.append(sb.subSequence(tmp.selectionPosition, sb.length()));
                        selection += tmp.delStr.length();
                    } else {// undo update operate
                        StringBuffer tmpSb = new StringBuffer();
                        tmpSb.append(sb.subSequence(0, tmp.selectionPosition));
                        tmpSb.append(sb.subSequence(tmp.selectionPosition + tmp.addStr.length(),
                                sb.length()));

                        newSb.append(tmpSb.subSequence(0, tmp.selectionPosition));
                        newSb.append(tmp.delStr);
                        newSb.append(tmpSb.subSequence(tmp.selectionPosition, tmpSb.length()));
                        selection += tmp.delStr.length();
                    }
                }
                currentHistoryPosition--;
                if (currentHistoryPosition < 0) {
                    currentHistoryPosition = 0;
                }
            }
            setText(newSb.toString());
            setSelection(selection);
        }
    }

    public void redoAction() {
        currentHistoryPosition++;
        if (currentHistoryPosition < historyList.size() && currentHistoryPosition >= 0) {
            recordedInHistory = false;
            StringBuffer sb = new StringBuffer(this.getText().toString());
            StringBuffer newSb = new StringBuffer();
            TextHistory tmp = historyList.get(currentHistoryPosition);
            int selection = tmp.selectionPosition;
            if (tmp.selectionPosition <= sb.length()) {
                if ("".equals(tmp.delStr)) {// redo add operate
                    newSb.append(sb.subSequence(0, tmp.selectionPosition));
                    newSb.append(tmp.addStr);
                    newSb.append(sb.subSequence(tmp.selectionPosition, sb.length()));
                    selection += tmp.addStr.length();
                } else {
                    if ("".equals(tmp.addStr)) {// redo del operate
                        newSb.append(sb.subSequence(0, tmp.selectionPosition));
                        newSb.append(sb.subSequence(tmp.selectionPosition + tmp.delStr.length(),
                                sb.length()));
                    } else {// redo update operate
                        StringBuffer tmpSb = new StringBuffer();
                        tmpSb.append(sb.subSequence(0, tmp.selectionPosition));
                        tmpSb.append(sb.subSequence(tmp.selectionPosition + tmp.delStr.length(),
                                sb.length()));

                        newSb.append(tmpSb.subSequence(0, tmp.selectionPosition));
                        newSb.append(tmp.addStr);
                        newSb.append(tmpSb.subSequence(tmp.selectionPosition, tmpSb.length()));
                        selection += tmp.addStr.length();
                    }
                }
            }
            setText(newSb.toString());
            setSelection(selection);
        }
    }

    private boolean allowRemoveHistory = true;

    public void showAndSetText(CharSequence content) {
        allowRemoveHistory = false;
        setText(content);
    }

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

    class NotesEditorCompositeWatcher implements TextWatcher {

        private String beforeStr;
        private int end = 0;
        private boolean paragraphAction = false;
        private int start = 0;
        private int count = 0;
        private int before = 0;

        @Override
        public void afterTextChanged(Editable s) {
            if (isQueryState) {
                return;
            }

            if (recordedInHistory) {
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

                if (allowRemoveHistory) {
                    if (currentHistoryPosition < historyList.size() - 1
                            && currentHistoryPosition >= 0) {
                        for (int i = historyList.size() - 1; i > currentHistoryPosition; i--) {
                            historyList.remove(i);
                        }
                    }
                } else {
                    allowRemoveHistory = true;
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
                recordedInHistory = true;
            }
            end = start + count;
            if (historySetListener != null) {
                historySetListener.editHistoryChangedistener(currentHistoryPosition,
                        historyList.size());
            }

            if (justAddTag) {
                setSelection(end + 2);
                justAddTag = false;
            }
            Linkify.addLinks(s, Linkify.ALL);
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

    public int getContentHeight() {
        return getLineHeight() * getLineCount();
    }

    public void setAutoLinkListener(AutoLinkEditListener autoLinkListener) {
        super.setAutoLinkListener(autoLinkListener);
    }

}