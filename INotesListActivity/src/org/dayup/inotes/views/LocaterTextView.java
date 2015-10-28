package org.dayup.inotes.views;

import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.utils.ConvertUtils;

import android.content.Context;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class LocaterTextView extends EditText {

    private final String TAG = LocaterTextView.class.getSimpleName();

    private LocaterListener listener;
    private INotesApplication application;

    public void setLocaterListener(LocaterListener listener) {
        this.listener = listener;
    }

    public interface LocaterListener {
        void selection(int off);
    }

    public LocaterTextView(Context context) {
        super(context, null);
        init(context);
    }

    public LocaterTextView(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
        init(context);
    }

    public LocaterTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        application = (INotesApplication) context.getApplicationContext();
    }

    public int getContentHeight() {
        StaticLayout layout = new StaticLayout(getText().toString(), getPaint(),
                (application.getTargetWidth() - ConvertUtils.dip2px(application, 8)),
                Alignment.ALIGN_NORMAL, 1.2f, 0, false);
        return layout.getHeight() + getPaddingTop() + getPaddingBottom();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && getSelectionStart() == getSelectionEnd()) {
            if (listener != null) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();
                x += getScrollX();
                y += getScrollY();

                Layout layout = getLayout();
                if (layout != null) {
                    int line = getLineForVertical(layout, y);
                    if (listener != null) {
                        listener.selection(line < 0 ? getText().length() : layout
                                .getOffsetForHorizontal(line, x));
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public int getLineForVertical(Layout layout, int vertical) {
        int high = layout.getLineCount(), low = -1, guess;
        if (layout.getLineBottom(high - 1) < vertical) {
            return -1;
        }

        while (high - low > 1) {
            guess = (high + low) / 2;

            if (layout.getLineTop(guess) > vertical)
                high = guess;
            else
                low = guess;
        }

        if (low < 0)
            return -1;
        else
            return low;
    }
}