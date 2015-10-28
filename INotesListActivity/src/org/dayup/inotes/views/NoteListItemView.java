package org.dayup.inotes.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class NoteListItemView extends RelativeLayout {

    public NoteListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NoteListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean isSelected;

    public NoteListItemView(Context context) {
        super(context);

    }

    public void setNoteSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public void draw(Canvas canvas) {
        setSelected(isSelected);
        super.draw(canvas);
    }

}
