package com.baidu.ai.edge.ui.layout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.baidu.ai.edge.ui.R;
import com.baidu.ai.edge.ui.view.PreviewView;

/**
 * Created by ruanshimin on 2018/5/3.
 */

public class CameraLayout extends FrameLayout {
    private PreviewFrameLayout mPreviewFrameLayout;
    private ActionBarLayout mActionBarLayout;
    private RelativeLayout mOperationFrameLayout;
    private PreviewView mPreviewView;

    public CameraLayout(@NonNull Context context) {
        super(context);
    }

    public CameraLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.preview_frame_layout);
        mActionBarLayout = (ActionBarLayout) findViewById(R.id.action_bar);
        mOperationFrameLayout = (RelativeLayout) findViewById(R.id.operation_bar);
        mPreviewView = (PreviewView) findViewById(R.id.preview_view);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int actionBarHeight = mActionBarLayout.getMeasuredHeight();
        int operationBarHeight = mOperationFrameLayout.getMeasuredHeight();

        mActionBarLayout.layout(left, top, right, actionBarHeight);
        mOperationFrameLayout.layout(left, bottom - top - operationBarHeight, right, bottom);

        mPreviewFrameLayout.layout(left, actionBarHeight, right, bottom - operationBarHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mActionBarLayout.measure(widthMeasureSpec, heightMeasureSpec);
        mOperationFrameLayout.measure(widthMeasureSpec, heightMeasureSpec);

        int actionBarHeight = mActionBarLayout.getMeasuredHeight();
        int operationBarHeight = mOperationFrameLayout.getMeasuredHeight();
        int previewHeight = height - actionBarHeight - operationBarHeight;

        mPreviewView.setLayoutSize(width, previewHeight);

        int previewFrameLayoutHeightMeasureSpec = MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY);
        mPreviewFrameLayout.measure(widthMeasureSpec, previewFrameLayoutHeightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}
