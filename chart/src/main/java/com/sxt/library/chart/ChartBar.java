package com.sxt.library.chart;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.sxt.library.chart.base.BaseChart;
import com.sxt.library.chart.bean.ChartBean;
import com.sxt.library.chart.utils.DateFormatUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sxt on 2017/7/13.
 */
public class ChartBar extends BaseChart {

    private List<ChartBean> datas;
    private float basePadding = 30;
    private float startX, endX, startY, endY;
    private boolean onTouch, isCanTouch, isUserAnimator;
    private float downX, downY, moveX, moveY;
    private Paint basePaint, baseLabelPaint, xyPaint, hintPaint, rectPaint;
    /**
     * 顶部的Label 文字
     */
    private String[] labelStrs;
    /**
     * 顶部的Label 颜色
     */
    private int[] labelColors;
    private long duration;
    /**
     * 触摸操作的画笔
     */
    private Paint touchPaint;
    /**
     * 用于存储对应的触摸柱状图的颜色
     */
    private Map<Integer, Integer> touchColors = new HashMap<>();

    private boolean starting = false;
    private boolean isFirst = true;
    private float mAnimatedValue;
    private float mAnimatedValueMax = 1.0f;
    private ValueAnimator valueAnimator;

    public ChartBar(Context context) {
        super(context);
    }

    public ChartBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        initPaint();
        if (attrs != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChartBar, 0, 0);
            try {
                duration = typedArray.getInteger(R.styleable.ChartBar_bar_duration, DEFAULT_DURATION);
                isCanTouch = typedArray.getBoolean(R.styleable.ChartBar_bar_isCanTouch, false);
                isUserAnimator = typedArray.getBoolean(R.styleable.ChartBar_bar_isUseAnimator, false);
                int lineXyColor = typedArray.getColor(R.styleable.ChartBar_bar_line_xy_color, Color.GRAY);
                int lineHintColor = typedArray.getColor(R.styleable.ChartBar_bar_line_xy_color, Color.GRAY);
                xyPaint.setColor(lineXyColor);
                hintPaint.setColor(lineHintColor);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            } finally {
                typedArray.recycle();
            }
        }
    }

    private void initPaint() {
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.GRAY);
        basePaint.setStrokeWidth(dip2px(0.5f));
        basePaint.setTextSize(dip2px(10));
        basePaint.setTextAlign(Paint.Align.CENTER);
        basePaint.setStrokeCap(Paint.Cap.ROUND);
        basePaint.setDither(true);

        baseLabelPaint = new Paint();
        baseLabelPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        baseLabelPaint.setTextSize(dip2px(14));
        baseLabelPaint.setTextAlign(Paint.Align.LEFT);
        Typeface font0 = Typeface.create(Typeface.SANS_SERIF, Typeface.DEFAULT_BOLD.getStyle());
        baseLabelPaint.setTypeface(font0);

        xyPaint = new Paint(basePaint);
        xyPaint.setColor(Color.GRAY);
        xyPaint.setStrokeWidth(dip2px(1));

        hintPaint = new Paint(basePaint);
        hintPaint.setStrokeWidth(0.5f);

        touchPaint = new Paint(hintPaint);
        touchPaint.setStyle(Paint.Style.FILL);
        touchPaint.setColor(ContextCompat.getColor(getContext(), R.color.alpha));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            startX = getPaddingLeft() + basePadding;
            endX = getMeasuredWidth() - getPaddingRight() - basePadding;
            startY = getMeasuredHeight() - getPaddingBottom() - basePadding * 3;
            endY = getPaddingTop() + basePadding * 4;
        }
    }

    public ChartBar setRectData(List<ChartBean> datas) {
        this.datas = datas;
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLabels(canvas);//画顶部Label
        drawLine(canvas);//画横线

        if (mAnimatedValue == 0) return;
        drawNoTouch(canvas);
        if (isCanTouch && onTouch && mAnimatedValue == mAnimatedValueMax) {//曲线绘制完成之后才能进行触摸绘制
            drawOnTouch(canvas);
        }
        super.onDraw(canvas);
    }

    private void drawOnTouch(Canvas canvas) {
        //这里获取int整型数值 ，刚好与数据源的索引吻合 ，如果数据长度过短，可能会索引越界，可以对index进行判断
        int index = (int) ((moveX - startX) / getDx());
        if (index >= datas.size()) index = datas.size() - 1;
        float y = datas.get(index).y;

        float dx = getDx();
        float dy = (startY - endY - basePadding) / 100;

        float x0 = startX + index * dx;
        float x1 = x0 + 0.5f * dx;
        float y1 = startY - y * dy;

        //画矩形
        canvas.drawRect(x0, y1, x0 + dx, startY, touchPaint);
        Paint p = new Paint(touchPaint);
        p.setTextSize(dip2px(15));
        p.setColor(ContextCompat.getColor(getContext(), touchColors.get(index)));
        canvas.drawText(String.valueOf(y), x1, y1 - basePadding / 2, p);

//        canvas.drawLine(x1, startY, x1, endY, touchPaint);//辅助线 Y
//        canvas.drawLine(startX + 2 * basePadding, y1, endX, y1, touchPaint);//辅助线 X
//
//        //画指示点
//        Paint paint = new Paint(touchPaint);
//        paint.setColor(Color.WHITE);
//        paint.setStrokeWidth(dip2px(9.5f));
//        canvas.drawPoint(x1, y1, paint);//画圆点
//
//        paint.setColor(Color.BLACK);
//        paint.setStrokeWidth(dip2px(6.5f));
//        canvas.drawPoint(x1, y1, paint);//画圆点
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanTouch) return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mAnimatedValue == mAnimatedValueMax) {//曲线绘制完成才能绘制辅助线
                    onTouch = true;
                    downX = event.getX();
                    downY = event.getY();
                    moveX = downX;
                    moveY = downY;
                }
                Log.i("line", "Down");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mAnimatedValue == mAnimatedValueMax) {
                    moveX = event.getX();
                    moveY = event.getY();
                    if (moveX >= getLeft() && moveX <= getRight() && moveY >= getTop() && moveY <= getBottom()) {
                        getParent().requestDisallowInterceptTouchEvent(true);//绘制区域内 允许子view响应触摸事件
                        invalidate();
                    } else {
                        postDelayedInvalidate();
                    }
                }
                Log.i("line", "Move");
                break;
            case MotionEvent.ACTION_UP:
                moveX = event.getX();
                moveY = event.getY();
                postDelayedInvalidate();
                Log.i("line", "Up");
                break;
        }
        if (onTouch && mAnimatedValue == mAnimatedValueMax) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public void postDelayedInvalidate() {
        onTouch = false;//置为响应触摸操作的绘制
        getParent().requestDisallowInterceptTouchEvent(false);//离开绘制区域,拦截触摸事件
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        }, 1000);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler();

    private void drawNoTouch(Canvas canvas) {
        if (datas == null || datas.size() == 0 || xyPaint == null) return;
        drawX(canvas); //画X轴
        //drawY(canvas); //画Y轴
        darwRect(canvas);//画矩形
    }

    private void drawLine(Canvas canvas) {
        //X轴
        canvas.drawLine(startX, startY, endX, startY, hintPaint);
        //顶部的横线
        canvas.drawLine(startX, endY, endX, endY, hintPaint);
    }

    /**
     * 画顶部的Label
     */
    private void drawLabels(Canvas canvas) {
        if (labelStrs == null || labelStrs.length == 0) return;
        if (labelColors == null || labelColors.length == 0) return;

        //在坐标系左上角 画单位
        float labelCenterY = endY - basePadding * 1.5f;

        Paint leftLabelPaint = new Paint(baseLabelPaint);
        leftLabelPaint.setTextSize(size2sp(15, getContext()));
        leftLabelPaint.setTextAlign(Paint.Align.LEFT);
        Typeface font0 = Typeface.create(Typeface.SANS_SERIF, Typeface.DEFAULT_BOLD.getStyle());
        leftLabelPaint.setTypeface(font0);
        canvas.drawText(labelStrs[0], startX + basePadding / 2, labelCenterY, leftLabelPaint);

        float top0 = leftLabelPaint.getFontMetrics().top;
        float descent0 = leftLabelPaint.getFontMetrics().descent;

        //左上角的标题label
        Paint rectPaint = new Paint(basePaint);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(ContextCompat.getColor(getContext(), labelColors[0]));
        canvas.drawRect(startX, labelCenterY + top0 * 0.8f, startX + basePadding / 3, labelCenterY + descent0 / 2, rectPaint);

        if (labelStrs.length != 3 || labelColors.length != 3) return;
        //右上角的label
        float left = endX - basePadding * 8;
        float baseY = endY - basePadding;
        float right = left + 4.5F * basePadding;
        float DX = basePadding / 2;

        Paint paint = new Paint(basePaint);
        paint.setTextSize(dip2px(10));
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);

        canvas.drawText(labelStrs[1], left, baseY, paint);//低压
        float top1 = paint.getFontMetrics().top;
        float descent1 = paint.getFontMetrics().descent;

        canvas.drawText(labelStrs[2], right, baseY, paint);//高压
        float top2 = paint.getFontMetrics().top;
        float descent2 = paint.getFontMetrics().descent;

        rectPaint.setColor(ContextCompat.getColor(getContext(), labelColors[1]));
        float top11 = top1 * 0.8f;
        float descent11 = descent1 * 0.6f;
        canvas.drawRect(
                left - DX + top11 - descent11,
                baseY + top11,
                left - DX,
                baseY + descent11,
                rectPaint);

        float top22 = top2 * 0.8f;
        //float descent22 = descent2 * 0.6f;
        rectPaint.setColor(ContextCompat.getColor(getContext(), labelColors[2]));
        canvas.drawRect(
                right - DX + top11 - descent11,
                baseY + top22,
                right - DX,
                baseY + descent11,
                rectPaint);
    }

    private void darwRect(Canvas canvas) {
        float dx = getDx();
        float dy = (startY - endY - basePadding) / 100;
        for (int i = 0; i < datas.size(); i++) {

            RectF rectf = new RectF(startX + dx * i, startY - datas.get(i).y * dy * (isUserAnimator ? mAnimatedValue : 1), startX + dx * (i + 1), startY);
            rectPaint = new Paint(basePaint);
            if (datas.get(i).y > 90 || datas.get(i).y <= 10) {
                rectPaint.setColor(ContextCompat.getColor(getContext(), labelColors[2]));
                touchColors.put(i, labelColors[2]);
            } else if (datas.get(i).y >= 60) {
                rectPaint.setColor(ContextCompat.getColor(getContext(), labelColors[1]));
                touchColors.put(i, labelColors[1]);
            } else {
                rectPaint.setColor(ContextCompat.getColor(getContext(), labelColors[0]));
                touchColors.put(i, labelColors[0]);
            }
            canvas.drawRect(rectf, rectPaint);
        }
    }

    private void drawX(Canvas canvas) {
        float dx = getDx();
        for (int i = 0; i < datas.size(); i++) {
            canvas.drawText(datas.get(i).x, (float) (startX + dx * (i + 0.5)), startY + 2 * basePadding, xyPaint);
        }
    }

    private void drawY(Canvas canvas) {
        canvas.drawLine(startX, startY, startX, endY, basePaint);
        //canvas.drawText(String.valueOf(0), startX - basePadding, startY - basePadding / 3, xyPaint);
        //canvas.drawText(String.valueOf(100), startX - basePadding, endY, xyPaint);
    }

    public ChartBar setLabels(String[] labelStrs, int[] labelColors) {
        this.labelStrs = labelStrs;
        this.labelColors = labelColors;
        return this;
    }

    public ChartBar setXYColor(int colorRes) {
        xyPaint.setColor(ContextCompat.getColor(getContext(), colorRes));
        return this;
    }

    public ChartBar setHintLineColor(int colorId) {
        hintPaint.setColor(ContextCompat.getColor(getContext(), colorId));
        return this;
    }

    public ChartBar setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    @NonNull
    public String parseDate(String currentDate) {
        if (currentDate != null && currentDate.length() > 0) {
            try {
                String currentStr = String.valueOf(DateFormatUtil.getSecondsFromDate(currentDate));//转换成毫秒值
                String mm = DateFormatUtil.getDateFromSeconds(currentStr, "MM");
                String dd = DateFormatUtil.getDateFromSeconds(currentStr, "dd");
                if (mm.startsWith("0")) {
                    mm = mm.substring(1, mm.length());
                }
                if (dd.startsWith("0")) {
                    dd = dd.substring(1, dd.length());
                }

                return dd + "/" + mm;

            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

    /**
     * 数据可能是 1/5  4/5
     *
     * @param floatStr
     * @return
     */
    public float parseFloat(String floatStr) {
        if (floatStr != null && floatStr.length() > 0) {
            try {
                if (floatStr.length() > 2 && floatStr.contains("/") && floatStr.indexOf("/") == 1) {
                    String[] split = floatStr.split("/");
                    return new BigDecimal(split[0]).divide(new BigDecimal(split[1])).floatValue();
                } else {
                    return Float.parseFloat(floatStr) <= 0 ? 0 : Float.parseFloat(floatStr);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    /**
     * 计算 x 轴 平均值
     *
     * @return
     */
    private float getDx() {
        return (endX - startX) / 9;
    }

    public void start() {
        super.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isCover(ChartBar.this)) {
                startAnimator();
            } else {
                this.post(new Runnable() {//可以避免页面未初始化完成造成的 空白
                    @Override
                    public void run() {
                        if (isCover(ChartBar.this)) {
                            startAnimator();
                        }
                    }
                });
            }
        } else {
            this.post(new Runnable() {//可以避免页面未初始化完成造成的 空白
                @Override
                public void run() {
                    startAnimator();
                }
            });
        }
    }

    private void startAnimator() {
        if (!isUserAnimator) {
            mAnimatedValue = mAnimatedValueMax;
            invalidate();
            isFirst = false;
            return;
        }
        if (!isFirst || starting) {//只能绘制一次 或者正在绘制过程中的话不能再次绘制
            return;
        }
        starting = true;
        valueAnimator = ValueAnimator.ofFloat(0, mAnimatedValueMax).setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimatedValue = (float) valueAnimator.getAnimatedValue();
                if (starting) {
                    invalidate();
                }
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                starting = false;
                isFirst = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (valueAnimator != null && valueAnimator.isRunning()) valueAnimator.cancel();
        super.onDetachedFromWindow();
    }
}
