package org.dayup.inotes.views;

import org.dayup.common.Log;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ResizeLayout extends ScrollView {

    private final String TAG = ResizeLayout.class.getSimpleName();

    private boolean allowedIntercept = true;
    private OnResizeListener mListener;
    private boolean minChange = false;

    private int minKeyboardChange = 110;
    private int minSizeChange = 50;

    public interface OnResizeListener {
        void onConfigChangedComplete();

        void OnKeyboardShown();

        void OnKeyboardHidden();
    }

    public void setOnResizeListener(OnResizeListener l) {
        mListener = l;
    }

    public ResizeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (Math.abs(oldh - h) < minSizeChange) {
            minChange = true;
        } else {
            minChange = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if ((!changed || minChange) && mListener != null) {
            mListener.onConfigChangedComplete();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mListener != null) {
            final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
            final int actualHeight = getHeight();
            Log.i(TAG, "proposedheight: " + proposedheight);
            Log.i(TAG, "actualHeight: " + actualHeight);
            Log.i(TAG, "d: " + (actualHeight - proposedheight));
            if (actualHeight != 0 && Math.abs(actualHeight - proposedheight) > minKeyboardChange) {
                if (actualHeight > proposedheight) {
                    mListener.OnKeyboardShown();
                } else {
                    mListener.OnKeyboardHidden();
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setAllowedInterceptTouchEvent(boolean allowedIntercept) {
        this.allowedIntercept = allowedIntercept;
    }

    public boolean dispatchTouchEvent(MotionEvent e) {
        boolean flag = (allowedIntercept ? super.dispatchTouchEvent(e) : false);
        Log.d(TAG, "dispatchTouchEvent .... " + flag);
        if (!allowedIntercept) {
            getChildAt(0).dispatchTouchEvent(e);
        }
        return flag;

    }
}
