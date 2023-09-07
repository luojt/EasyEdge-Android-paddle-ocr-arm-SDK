package com.baidu.ai.edge.ui.layout;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by ruanshimin on 2018/5/4.
 */

public class ActionBarLayout extends RelativeLayout {
    private int layoutHeight = 150;

    public ActionBarLayout(Context context) {
        super(context);
    }

    public ActionBarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionBarLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, layoutHeight);
        setBackgroundColor(Color.BLACK);
        setAlpha(0.9f);
    }
}
