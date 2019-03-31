package com.myproject.dummy;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    private Checkable mCheckable;

    public CheckableRelativeLayout(Context context) {
        this(context, null);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isChecked() {
        return mCheckable == null ? false : mCheckable.isChecked();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Find Checkable child
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View v = getChildAt(i);
            if (v instanceof Checkable) {
                mCheckable = (Checkable) v;
                break;
            }
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if(mCheckable != null)
            mCheckable.setChecked(checked);
    }

    @Override
    public void toggle() {
        if(mCheckable != null)
            mCheckable.toggle();
    }
}
