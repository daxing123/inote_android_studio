package org.dayup.inotes.views;

import org.dayup.inotes.R;
import org.dayup.inotes.utils.ConvertUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class NoteHeader extends RelativeLayout {
    private Paint mLine;
    private int lineColor;

    public NoteHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NoteHeader);
        lineColor = a.getInteger(R.styleable.NoteHeader_lineColor, 0x64ff0000);
        a.recycle();
        mLine = new Paint();
        mLine.setStyle(Paint.Style.STROKE);
        mLine.setStrokeWidth(ConvertUtils.dip2px(context, 2));
        mLine.setColor(lineColor);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(getLeft() + 10, getHeight() - 1, getRight() - 10, getHeight() - 1, mLine);
        // canvas.drawLine(10, getTop(), 10, getBottom(), paint);
        // canvas.drawLine(15, getTop(), 15, getBottom(), paint);
        super.onDraw(canvas);
    }

}