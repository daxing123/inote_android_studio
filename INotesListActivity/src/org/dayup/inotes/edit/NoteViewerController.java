package org.dayup.inotes.edit;

import org.dayup.inotes.R;
import org.dayup.inotes.views.LocaterTextView;
import org.dayup.inotes.views.LocaterTextView.LocaterListener;

import android.text.Layout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class NoteViewerController {
    private LocaterTextView noteViewerView;

    public NoteViewerController(View mainView, INoteViewerController iNoteViewerController) {
        noteViewerView = (LocaterTextView) mainView.findViewById(R.id.note_detail);
        noteViewerView.setFocusableInTouchMode(false);
        noteViewerView.setOnClickListener(new SwitchToEditListener(iNoteViewerController));
    }

    public void setViewerLocaterListener(LocaterListener l) {
        noteViewerView.setLocaterListener(l);
    }

    public void setContent(CharSequence context) {
        noteViewerView.setText(context);
    }

    public LocaterTextView getNoteViewerView() {
        return noteViewerView;
    }

    public CharSequence getContent() {
        return noteViewerView.getText();
    }

    public Layout getViewerLayout() {
        return noteViewerView.getLayout();
    }

    public void hide() {
        noteViewerView.setVisibility(View.GONE);
    }

    public boolean isShown() {
        return noteViewerView.getVisibility() == View.VISIBLE;
    }

    public void show(CharSequence content) {
        noteViewerView.setVisibility(View.VISIBLE);
        noteViewerView.setText(content);
    }

    public void setSelectionStart(int start) {
        if (start > 0) {
            noteViewerView.setSelection(start);
        }
    }

    private static class SwitchToEditListener implements OnClickListener {
        private final INoteViewerController iNoteViewerController;

        public SwitchToEditListener(INoteViewerController iNoteViewerController) {
            this.iNoteViewerController = iNoteViewerController;
        }

        @Override
        public void onClick(View v) {
            iNoteViewerController.switchToEdit();
        }
    }

    public int getContentHeight() {
        return noteViewerView.getContentHeight();
    }

    public void setLayoutHeight(int paramsHeight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, paramsHeight);
        noteViewerView.setLayoutParams(params);
    }
}
