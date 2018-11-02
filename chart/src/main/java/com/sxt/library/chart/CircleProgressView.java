package com.sxt.library.chart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by sxt on 2017/7/13.
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CircleProgressView extends View {

    private Paint basePaint, baseLabelPaint;
    private float progress, maxValue;
    private float basePadding = 30;
    private float startX, endX, startY, endY;
    private float radius, radiusX, radiusY;

    /**
     * 顶部的Label 文字
     */
    private String[] labelStrs;
    /**
     * 顶部的Label 颜色
     */
    private int[] labelColors;
    /**
     * 动画持续的时长
     */
    private long duration = 3000;
    private String subTitle;
    private int roundBgColor;

    public CircleProgressView(Context context) {
        super(context);
        init();
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.BLACK);
        basePaint.setTextSize(size2sp(14, getContext()));
        basePaint.setTextAlign(Paint.Align.CENTER);
        basePaint.setStrokeWidth(dip2px(6));
        basePaint.setStrokeCap(Paint.Cap.ROUND);
        basePaint.setDither(true);

        baseLabelPaint = new Paint();
        baseLabelPaint.setColor(Color.BLACK);
        baseLabelPaint.setTextSize(dip2px(14));
        baseLabelPaint.setTextAlign(Paint.Align.LEFT);
        Typeface font0 = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        baseLabelPaint.setTypeface(font0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            startX = getPaddingLeft() + basePadding;
            endX = getMeasuredWidth() - getPaddingRight() - basePadding;
            startY = getMeasuredHeight() - getPaddingBottom() - basePadding;
            endY = getPaddingTop() + basePadding;

            radiusX = startX + (endX - startX) / 2;
            radiusY = endY + (startY - endY) / 2;
            radius = (endX - startX) / 4;
        }
    }

    public void setProgress(int progress, int maxValue, String subTitle) {
        this.progress = progress;
        this.maxValue = maxValue;
        this.subTitle = subTitle;
        initListener();
        initAnimator();
        valueAnimator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLabels(canvas);//画顶部Label
        drawCircleRound(canvas);//画底板圆
        drawCircleProgress(canvas);//画进度
    }

    private void drawCircleRound(Canvas canvas) {
        //画圆弧背景
        Paint paint = new Paint(basePaint);
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size2sp(6, getContext()));
        canvas.drawCircle(radiusX, radiusY, radius, paint);
    }

    private void drawCircleProgress(final Canvas canvas) {
        updateUi(canvas);
    }

    public void updateUi(Canvas canvas) {
        Paint paint = new Paint(basePaint);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size2sp(10, getContext()));

        RectF rectF = new RectF(//
                radiusX - radius,
                radiusY - radius,
                radiusX + radius,
                radiusY + radius);
        canvas.drawArc(
                rectF,
                -90,
                progress * mAnimatorValue,
                false,
                paint);

        /**
         * 已知圆弧半径，圆弧夹角，起始点坐标，怎么求终点坐标？
         半径r,角度θ,圆弧中心(a,b)，起点坐标(x0,y0) 
         a,b请根据起点坐标折算成中心坐标
         x=a+r*cosθ
         y=b+r*sinθ
         这两个函数中的θ 
         都是指的“弧度”而非“角度”
         弧度的计算公式为：2*PI/360*角度；30°角度的弧度= 2*PI/360*30 

         假设一个圆的圆心坐标是(a,b)，半径为r，则圆上每个点的
         X坐标=a + Math.sin(2*Math.PI / 360) * r ；
         Y坐标=b + Math.cos(2*Math.PI / 360) * r 
         */
        double arcPI = Math.PI * 2 / 360;
        float angle = progress * mAnimatorValue - 90;
        float x = (float) (radiusX + radius * Math.cos(arcPI * angle));
        float y = (float) (radiusY + radius * Math.sin(arcPI * angle));

        //画中心 放射点
        Paint radioPaint = new Paint(basePaint);
        radioPaint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, 7, radioPaint);

        Paint textPaint = new Paint(basePaint);
        textPaint.setTextSize(size2sp(40, getContext()));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.RED);
        Typeface font0 = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        textPaint.setTypeface(font0);

        BigDecimal divide1 = new BigDecimal(Float.toString(0.8f)).add(new BigDecimal(Float.toString(0.2f * mAnimatorValue)));
        float result = new BigDecimal(maxValue).multiply(new BigDecimal(divide1.floatValue()), new MathContext(5)).floatValue();
        canvas.drawText(String.valueOf((int) result), radiusX, radiusY + 2 * basePadding, textPaint);
        canvas.drawText(String.valueOf(subTitle == null || subTitle.length() == 0 ? "今日步数" : subTitle), radiusX, radiusY + textPaint.getFontMetrics().top * 0.6f, basePaint);
    }

    float mAnimatorValue;
    private ValueAnimator valueAnimator;
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;

    private void initListener() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // if (mAnimatorValue != 0.0)
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void initAnimator() {
        valueAnimator = ValueAnimator.ofFloat(0, 1f).setDuration(duration);
        valueAnimator.addUpdateListener(mUpdateListener);
    }

    /**
     * 画顶部的Label
     */
    private void drawLabels(Canvas canvas) {
        if (labelStrs == null || labelStrs.length == 0) return;
        if (labelColors == null || labelColors.length == 0) return;

        //在坐标系左上角 画单位
        float labelCenterY = endY + basePadding;

        Paint leftLabelPaint = new Paint(baseLabelPaint);
        leftLabelPaint.setTextSize(size2sp(16, getContext()));
        leftLabelPaint.setTextAlign(Paint.Align.LEFT);
        Typeface font0 = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        leftLabelPaint.setTypeface(font0);
        canvas.drawText(labelStrs[0], startX + basePadding / 2, labelCenterY, leftLabelPaint);

        float top0 = leftLabelPaint.getFontMetrics().top;
        float descent0 = leftLabelPaint.getFontMetrics().descent;

        //左上角的标题label
        Paint rectPaint = new Paint(basePaint);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(ContextCompat.getColor(getContext(), labelColors[0]));
        canvas.drawRect(startX, labelCenterY + top0 * 0.8f, startX + basePadding / 3, labelCenterY + descent0 / 2, rectPaint);
    }

    public CircleProgressView setLabels(String[] labelStrs, int[] labelColors) {
        this.labelStrs = labelStrs;
        this.labelColors = labelColors;
        return this;
    }

    public CircleProgressView setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public CircleProgressView setRoundBgColor(int colorResId) {
        this.roundBgColor = colorResId;
        return this;
    }

    float size2sp(float sp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, context.getResources().getDisplayMetrics());
    }

    int dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
