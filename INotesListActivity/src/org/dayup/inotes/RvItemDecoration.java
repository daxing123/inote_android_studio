package org.dayup.inotes;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by myatejx on 15/11/3.
 */
public class RvItemDecoration extends RecyclerView.ItemDecoration {

    public int space;

    public RvItemDecoration(int space) {
        this.space = space;
    }

    @Override public void getItemOffsets(
            Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        //此处不能写成!=0，因为数据的第一行，position就是0.写成0的话，等于说第一行就没有间距了
        if (parent.getChildAdapterPosition(view) != -1) {
            outRect.top = space;
            outRect.bottom = space;
            outRect.left = space;
            outRect.right = space;
        }

    }
}
