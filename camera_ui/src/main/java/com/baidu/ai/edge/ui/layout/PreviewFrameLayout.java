package com.baidu.ai.edge.ui.layout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.baidu.ai.edge.ui.R;
import com.baidu.ai.edge.ui.view.PreviewDecoratorView;
import com.baidu.ai.edge.ui.view.PreviewView;

/**
 * Created by ruanshimin on 2018/5/3.
 */

public class PreviewFrameLayout extends FrameLayout {
    private PreviewView mPreviewView;
    private PreviewDecoratorView mPreviewDecoratorView;

    public PreviewFrameLayout(@NonNull Context context) {
        super(context);
    }

    public PreviewFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PreviewFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mPreviewView = (PreviewView) findViewById(R.id.preview_view);
        mPreviewDecoratorView = (PreviewDecoratorView) findViewById(R.id.preview_decorator_view);
        mPreviewView.layout(0, 0, right, mPreviewView.getActualHeight());
        mPreviewDecoratorView.layout(0, 0, right, getMeasuredHeight());
    }
}
