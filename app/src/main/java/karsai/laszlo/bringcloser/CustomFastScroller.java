package karsai.laszlo.bringcloser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.futuremind.recyclerviewfastscroll.FastScroller;

/**
 * Created by Laci on 28/06/2018.
 */

public class CustomFastScroller extends FastScroller {

    private RecyclerView mRecyclerView;

    public CustomFastScroller(Context context) {
        super(context);
    }

    public CustomFastScroller(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mRecyclerView != null) {
            super.onLayout(changed, l, t, r, b);
        }
    }

    @Override
    public void setRecyclerView(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        super.setRecyclerView(recyclerView);

        requestLayout();
    }
}
