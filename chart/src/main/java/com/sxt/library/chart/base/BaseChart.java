package com.sxt.library.chart.base;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by sxt on 2017/12/30.
 */

public class BaseChart extends View {

    public final String TAG = this.getClass().getName();
    public static final int DEFAULT_DURATION = 1500;

    public BaseChart(Context context) {
        this(context, null);
    }

    public BaseChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void start() {
    }

    /**
     * 检测制定View是否被遮住显示不全
     *
     * @return
     */
    public boolean isCover(View view) {
        Rect rect = new Rect();
        if (view.getGlobalVisibleRect(rect)) {
            if (rect.width() >= view.getMeasuredWidth() && rect.height() >= view.getMeasuredHeight() * 0.8) {
                return true;
            }
        }
        return false;
    }

    protected float size2sp(float sp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, context.getResources().getDisplayMetrics());
    }

    protected int dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
