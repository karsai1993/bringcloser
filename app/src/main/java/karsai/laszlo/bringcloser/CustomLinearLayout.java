package karsai.laszlo.bringcloser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * http://alisonhuang-blog.logdown.com/posts/290009-design-support-library-coordinator-layout-and-behavior
 */

public class CustomLinearLayout extends LinearLayout implements CoordinatorLayout.AttachedBehavior {
    public CustomLinearLayout(Context context) {
        super(context);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new MoveUpwardBehavior();
    }
}