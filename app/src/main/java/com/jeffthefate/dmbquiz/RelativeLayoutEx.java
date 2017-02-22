package com.jeffthefate.dmbquiz;

import android.content.Context;
import android.os.Build;
import android.util.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class RelativeLayoutEx extends RelativeLayout {

    private Context context;
    private DisplayMetrics metrics;

    public RelativeLayoutEx(Context context) {
        super(context);
        this.context = context;
        metrics = new DisplayMetrics();
    }

    public RelativeLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        metrics = new DisplayMetrics();
    }

    public RelativeLayoutEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        metrics = new DisplayMetrics();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(Constants.LOG_TAG, "incoming width spec: " + widthMeasureSpec);
        Log.i(Constants.LOG_TAG, "incoming height spec: " + heightMeasureSpec);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int realWidth = metrics.widthPixels;
        int realHeight = metrics.heightPixels;
        Log.i(Constants.LOG_TAG, "real width: " + realWidth);
        Log.i(Constants.LOG_TAG, "real height: " + realHeight);
        /**
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            realWidth = metrics.widthPixels;
            realHeight = metrics.heightPixels;
        }
        Log.i(Constants.LOG_TAG, "real width: " + realWidth);
        Log.i(Constants.LOG_TAG, "real height: " + realHeight);
         */
        final int realWidthMeasureSpec = MeasureSpec.makeMeasureSpec(realWidth, MeasureSpec.EXACTLY);
        final int realHeightMeasureSpec = MeasureSpec.makeMeasureSpec(realHeight, MeasureSpec.EXACTLY);
        Log.i(Constants.LOG_TAG, "real width spec: " + realWidthMeasureSpec);
        Log.i(Constants.LOG_TAG, "real height spec: " + realHeightMeasureSpec);
        super.onMeasure(realWidthMeasureSpec, realHeightMeasureSpec);
    }

}