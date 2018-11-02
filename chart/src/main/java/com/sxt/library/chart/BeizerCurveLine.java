package com.sxt.library.chart;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.sxt.library.chart.base.BaseChart;
import com.sxt.library.chart.bean.ChartBean;
import com.sxt.library.chart.utils.DateFormatUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sxt on 2017/8/5.
 */
public class BeizerCurveLine extends BaseChart {

    private Paint basePaint, baseLabelPaint, xyPaint;
    /**
     * 触摸操作的画笔
     */
    private Paint touchPaint;
    /**
     * 曲线画笔
     */
    private Paint curvePaint;
    /**
     * 边框线画笔
     */
    private Paint coverPaint;
    /**
     * 边框线路径
     */
    private Path coverPath;
    /**
     * 阴影画笔
     */
    private Paint fillPaint;
    /**
     * 阴影路径
     */
    private Path fillPath;
    private float basePadding = 30;
    private float startX, endX, startY, endY;
    /**
     * 当前是否是填充状态
     */
    private boolean isFilled;
    /**
     * 是否要显示边框线
     */
    private boolean isShowCoverLine;
    /**
     * 是否要显示xy轴
     */
    private boolean isShowXy;
    /**
     * 辅助网格线是否显示
     */
    private boolean isShowHintLines;
    /**
     * 是否需要执行动画
     */
    private boolean isPlayAnimator;
    /**
     * 是否需要执行动画
     */
    private boolean isCanTouch;
    /**
     * 网格线画笔
     */
    private Paint hintPaint;
    /**
     * Y轴最大值  默认取100
     */
    private int maxValueOfY = 100;
    /**
     * 顶部的Label 文字
     */
    private String[] labelStrs;
    /**
     * 顶部的Label 颜色
     */
    private int[] labelColors;
    /**
     * 曲线的数据源
     */
    private Map<Integer, List<ChartBean>> curveDataLists;
    /**
     * 曲线的画笔颜色的集合
     */
    private Map<Integer, Integer> curvePaintColors;
    private Map<Integer, Integer> curveShaderColors;
    /**
     * 曲线路径集合
     */
    private List<Path> pathList;
    /**
     * 动画执行的时长
     */
    private long duration;
    /**
     * 左上角的单位
     */
    private String unit;
    /**
     * hintLine 的默认数量
     */
    private int hintLinesNum = 6;
    private float curveXo;
    /**
     * x轴显示的 坐标数量   4 个点  分 5 个阶段
     */
    private int xNum = 4;
    /**
     * y轴的最大刻度
     */
    private boolean isShowFloat = false;

    public BeizerCurveLine(Context context) {
        super(context);
    }

    public BeizerCurveLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BeizerCurveLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        initPaint();
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BeizerCurveLine);
            if (typedArray != null) {
                try {
                    duration = typedArray.getInt(R.styleable.BeizerCurveLine_line_duration, DEFAULT_DURATION);
                    isPlayAnimator = typedArray.getBoolean(R.styleable.BeizerCurveLine_line_isPlayAnimator, true);
                    isCanTouch = typedArray.getBoolean(R.styleable.BeizerCurveLine_line_isCanTouch, false);
                    isFilled = typedArray.getBoolean(R.styleable.BeizerCurveLine_line_isFilled, false);
                    isShowCoverLine = typedArray.getBoolean(R.styleable.BeizerCurveLine_line_isShowCoverLine, true);
                    isShowHintLines = typedArray.getBoolean(R.styleable.BeizerCurveLine_line_isShowHintLines, true);
                    isShowXy = typedArray.getBoolean(R.styleable.BeizerCurveLine_line_isShowXy, false);
                    isShowFloat = typedArray.getBoolean(R.styleable.BeizerCurveLine_line_isShowFloat, false);
                    unit = typedArray.getString(R.styleable.BeizerCurveLine_line_unit);
                    int line_xy_color = typedArray.getColor(R.styleable.BeizerCurveLine_line_xy_color, Color.GRAY);
                    int line_hint_color = typedArray.getColor(R.styleable.BeizerCurveLine_line_hint_color, Color.GRAY);
                    float line_hint_width = typedArray.getDimension(R.styleable.BeizerCurveLine_line_hint_width, 0.5f);
                    float line_cover_width = typedArray.getDimension(R.styleable.BeizerCurveLine_line_cover_width, 3.5f);
                    xyPaint.setColor(line_xy_color);
                    hintPaint.setColor(line_hint_color);
                    hintPaint.setStrokeWidth(line_hint_width);
                    coverPaint.setStrokeWidth(line_cover_width);

                } finally {
                    typedArray.recycle();
                }
            }
        }
    }

    private void initPaint() {
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.GRAY);
        basePaint.setStrokeWidth(dip2px(0.5f));
        basePaint.setTextSize(dip2px(10));
        basePaint.setTextAlign(Paint.Align.LEFT);
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

        curvePaint = new Paint(basePaint);
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setStrokeWidth(dip2px(4));

        coverPaint = new Paint(basePaint);
        coverPaint.setStyle(Paint.Style.STROKE);
        coverPaint.setStrokeWidth(dip2px(4));

        fillPaint = new Paint(basePaint);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setStrokeWidth(dip2px(4));

        coverPath = new Path();
        fillPath = new Path();

        touchPaint = new Paint(hintPaint);
        touchPaint.setStyle(Paint.Style.FILL);
        touchPaint.setColor(Color.BLACK);
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

    @Override
    protected void onDraw(Canvas canvas) {
        drawNoTouch(canvas);
        if (isCanTouch && onTouch && mAnimatorValue == 1.0) {//曲线绘制完成之后才能进行触摸绘制
            drawOnTouch(canvas);
        }
        super.onDraw(canvas);
    }

    private float downX, downY, moveX, moveY, startX0, startY0;
    private boolean onTouch = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanTouch) return super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mAnimatorValue == 1.0) {//曲线绘制完成才能绘制辅助线
                    onTouch = true;
                    downX = event.getX();
                    downY = event.getY();
                    moveX = downX;
                    moveY = downY;
                    startX0 = moveX;
                    startY0 = moveY;
                }
                Log.i("line", "Down");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mAnimatorValue == 1.0) {
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
                startX0 = moveX;
                startY0 = moveY;
                postDelayedInvalidate();
                Log.i("line", "Up");
                break;
        }
        if (onTouch && mAnimatorValue == 1.0) {
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
        //无论有没有数据 都要显示坐标轴线
        drawLabels(canvas);//画顶部的Label
        drawLines(canvas);//画横线

        if (this.curveDataLists == null || this.curveDataLists.size() == 0) {
            return;
        }
        drawXY(canvas, curveDataLists.get(0));//画XY轴

        for (int i = 0; i < curveDataLists.size(); i++) {
            List<ChartBean> chartBeanList = curveDataLists.get(i);
            if (chartBeanList.size() <= 1) {//如果只有一条数据 就 drawPoint
                drawPoint(canvas);
                canvas.save();
            } else {
                drawCurveLines(canvas);
            }
        }
    }

    @SuppressLint("ResourceType")
    private void drawOnTouch(Canvas canvas) {

        //这里获取int整型数值 ，刚好与数据源的索引吻合 ，如果数据长度过短，可能会索引越界，可以对index进行判断
        int index = (int) ((moveX - startX) / getDx());
        if (index >= curveDataLists.get(0).size()) index = curveDataLists.get(0).size() - 1;
        float y = curveDataLists.get(0).get(index).y;
        float dy0 = (startY - endY) / hintLinesNum;
        float maxValue = calculateMaxValueOfY(); //计算最大值
        float dy = (dy0 * (hintLinesNum - 1)) / (maxValue <= 0 ? hintLinesNum - 1 : maxValue);

        float x1 = curveXo + index * getDx();
        float y1 = startY - y * dy;
        canvas.drawLine(x1, startY, x1, endY, touchPaint);//辅助线 Y
        canvas.drawLine(startX + 2 * basePadding, y1, endX, y1, touchPaint);//辅助线 X

        //画指示点
        Paint paint = new Paint(touchPaint);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(dip2px(9.5f));
        canvas.drawPoint(x1, y1, paint);//画圆点

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(dip2px(6.5f));
        canvas.drawPoint(x1, y1, paint);//画圆点

        //画指示数据
        Paint p = new Paint(touchPaint);
        p.setColor(Color.BLACK);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(dip2px(15));
        if (isShowFloat) {
            canvas.drawText(String.valueOf(y), x1, y1 - basePadding / 2, p);
        } else {
            canvas.drawText(String.valueOf((int) y), x1, y1 - basePadding / 2, p);
        }
    }

    private void drawPoint(Canvas canvas) {
        //计算最大值
        float maxValue = calculateMaxValueOfY();
        float dy0 = (startY - endY) / hintLinesNum;
        float dy = (dy0 * (hintLinesNum - 1)) / maxValue;
        for (int i = 0; i < curveDataLists.size(); i++) {
            for (int j = 0; j < curveDataLists.get(i).size(); j++) {

                List<ChartBean> chartBeanList = curveDataLists.get(i);
                if (chartBeanList.size() <= 1) {//如果只有一条数据 就 drawPoint

                    fillPaint.setColor(ContextCompat.getColor(getContext(), curvePaintColors.get(i)));
                    float yValue = startY - curveDataLists.get(i).get(j).y * dy;
                    canvas.drawPoint(curveXo, yValue, fillPaint);
                }
            }
        }
    }

    private void initPath() {
        //计算最大值
        float maxValue = calculateMaxValueOfY();
        curveXo = startX + basePadding * 2.5f;//最左边的横坐标

        //float dx = (endX - startX) / curveDataLists.get(0).size();
        float dx = getDx();
        float dy0 = (startY - endY) / hintLinesNum;
        float dy = (dy0 * (hintLinesNum - 1)) / (maxValue <= 0 ? hintLinesNum - 1 : maxValue);
        float preX;
        float preY;
        float currentX;
        float currentY;
        float targetEndX;
        pathList = new ArrayList<>();
        for (int j = 0; j < curveDataLists.size(); j++) {
            Path curvePath = new Path();
            targetEndX = curveXo + getDx() * (curveDataLists.get(j).size() - 1);
            List<ChartBean> curveBeanList = curveDataLists.get(j);
            for (int i = curveBeanList.size() - 1; i >= 0; i--) {
                if (i == curveBeanList.size() - 1) {
                    float yValue = startY - curveBeanList.get(i).y * dy;
                    curvePath.moveTo(targetEndX, yValue);
                    continue;
                }
                //到这里 肯定不是起点
                preX = curveXo + dx * (i + 1);
                preY = startY - curveBeanList.get(i + 1).y * dy;
                currentX = curveXo + dx * i;
                currentY = startY - curveBeanList.get(i).y * dy;

                curvePath.cubicTo(
                        (preX + currentX) / 2, preY,
                        (preX + currentX) / 2, currentY,
                        currentX, currentY);
            }
            pathList.add(curvePath);
        }
    }

    private float calculateMaxValueOfY() {
        if (curveDataLists != null && curveDataLists.size() > 0) {
            float max = curveDataLists.get(0).get(0).y;
            for (int j = 0; j < curveDataLists.size(); j++) {
                for (int i = 0; i < curveDataLists.get(j).size(); i++) {
                    float f = curveDataLists.get(j).get(i).y;
                    if (max < f) {
                        max = f;
                    }
                }
            }
            return max;
        }
        return 0;
    }

    private PathMeasure pathMeasureCover;
    private float curveLength;

    private void drawCurveLines(Canvas canvas) {
        if (pathList == null || pathList.size() == 0) return;
        Paint currentCoverPaint = null, currentFillPaint = null;
        for (int i = 0; i < pathList.size(); i++) {
            //初始化画笔
            if (isShowCoverLine) {
                currentCoverPaint = new Paint(coverPaint);
                currentCoverPaint.setColor(ContextCompat.getColor(getContext(), curvePaintColors.get(i)));
            }
            if (isFilled) {
                currentFillPaint = new Paint(fillPaint);
                currentFillPaint.setColor(ContextCompat.getColor(getContext(), curvePaintColors.get(i)));
                currentFillPaint.setShader(getShader(
                        new int[]{
                                ContextCompat.getColor(getContext(), curveShaderColors.get(i)),
                                ContextCompat.getColor(getContext(), android.R.color.transparent)}));
            }
            //开启动画
            if (isPlayAnimator) {
                pathMeasureCover = new PathMeasure(pathList.get(i), false);
                curveLength = pathMeasureCover.getLength();
                Path dst = new Path();//接收截取的path
                Path dst0 = new Path();
                //根据动画值从线段总长度不断截取绘制造成动画效果
                pathMeasureCover.getSegment(curveLength * (1 - mAnimatorValue), curveLength, dst, true);
                //画阴影
                if (isFilled) {
                    dst.lineTo(curveXo, startY);
                    //dst.lineTo((curveXo + getDx() * (curveDataLists.get(i).size() - 1) - curveXo) * (1 - mAnimatorValue) + curveXo, startY);
                    dst.lineTo((getDx() * (curveDataLists.get(i).size() - 1)) * mAnimatorValue + curveXo, startY);
                    dst.close();
                    assert currentFillPaint != null;
                    canvas.drawPath(dst, currentFillPaint);
                }
                //画曲线 在这里执行 是为了防止阴影將曲线覆盖 造成曲线 线宽 显示不全
                if (isShowCoverLine) {
                    pathMeasureCover.getSegment(curveLength * (1 - mAnimatorValue), curveLength, dst0, true);
                    assert currentCoverPaint != null;
                    canvas.drawPath(dst0, currentCoverPaint);
                }
                if (!isFilled && !isShowCoverLine) {
                    canvas.drawPath(dst0, curvePaint);
                }

            } else {
                if (isFilled) {
                    fillPath.set(pathList.get(i));
                    fillPath.lineTo(curveXo, startY);
                    //fillPath.lineTo(endX, startY);
                    fillPath.lineTo(curveXo + getDx() * curveDataLists.get(i).size(), startY);
                    fillPath.close();
                    assert currentFillPaint != null;
                    canvas.drawPath(fillPath, currentFillPaint);
                }
                if (isShowCoverLine) {
                    coverPath.set(pathList.get(i));
                    assert currentCoverPaint != null;
                    canvas.drawPath(coverPath, currentCoverPaint);
                }
                if (!isFilled && !isShowCoverLine) {
                    canvas.drawPath(pathList.get(i), curvePaint);
                }
            }
        }
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
        Typeface font0 = Typeface.create(Typeface.MONOSPACE, Typeface.DEFAULT_BOLD.getStyle());
        leftLabelPaint.setTypeface(font0);
        canvas.drawText(labelStrs[0], startX + basePadding * 0.6f, labelCenterY, leftLabelPaint);

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

    /**
     * 画网格线
     */
    private void drawLines(Canvas canvas) {
        if (isShowHintLines) {
            float dy = (startY - endY) / hintLinesNum;
            float x0 = startX + basePadding * 2;
            for (int i = 0; i < hintLinesNum + 1; i++) {
                if (i == hintLinesNum) {
                    //顶部的横线
                    canvas.drawLine(startX, startY - dy * i, endX, startY - dy * i, hintPaint);
                } else {
                    canvas.drawLine(x0, startY - dy * i, endX, startY - dy * i, hintPaint);
                }
                if (i == hintLinesNum - 1) {
                    //在坐标系左上角 画单位
                    Paint unitPaint = new Paint(xyPaint);
                    unitPaint.setTextAlign(Paint.Align.LEFT);
                    float baseY = startY - dy * i - basePadding / 2;
                    canvas.drawText(unit, startX, baseY, unitPaint);
                    //画Y轴最大刻度
                    float maxValue = calculateMaxValueOfY();
                    String max = maxValue <= 0 ? "" : (isShowFloat ? String.valueOf(maxValue) : String.valueOf((int) maxValue));
                    canvas.drawText(max,
                            startX, baseY + basePadding * 1.3f,

                            unitPaint);
                }
                if (i == 0) {
                    Paint unitPaint = new Paint(xyPaint);
                    unitPaint.setTextAlign(Paint.Align.LEFT);
                    //画Y轴最低刻度
                    canvas.drawText(String.valueOf(0), startX, startY, unitPaint);
                }
            }
        }
    }

    private void drawXY(Canvas canvas, List<ChartBean> xDatas) {
        if (isShowXy) {
            canvas.drawLine(startX, startY, endX, startY, xyPaint);//X轴
            canvas.drawLine(startX, startY, startX, endY, xyPaint);//Y轴
        }
        float x0 = startX + basePadding;
        float dx = (endX - curveXo - basePadding) / xNum;
        float y = startY + basePadding * 2;
        xyPaint.setTextAlign(Paint.Align.LEFT);
        for (int i = 0; i < xDatas.size(); i++) {//画X轴刻度
            canvas.drawText(xDatas.get(i).x, x0 + dx * i, y, xyPaint);
        }
    }

    public BeizerCurveLine setMaxXNum(int xNum) {
        this.xNum = xNum;
        return this;
    }

    /**
     * 计算 x 轴 平均值
     *
     * @return
     */
    private float getDx() {
        if (curveDataLists == null || curveDataLists.get(0) == null) return 0;
        return (endX - curveXo) / this.xNum;//按照最多显示7天数据的长度计算
    }

    public BeizerCurveLine setLabels(String[] labelStrs, int[] labelColors) {
        this.labelStrs = labelStrs;
        this.labelColors = labelColors;
        return this;
    }

    public BeizerCurveLine setPlayAnimator(boolean isPlayAnimator) {
        this.isPlayAnimator = isPlayAnimator;
        return this;
    }

    public BeizerCurveLine setMaxValueOfY(int maxValueOfY) {
        this.maxValueOfY = maxValueOfY;
        return this;
    }

    public BeizerCurveLine setCoverLine(boolean isShowCoverLine) {
        this.isShowCoverLine = isShowCoverLine;
        return this;
    }

    public BeizerCurveLine setCoverLine(int coverLineColor) {
        if (coverPaint != null) coverPaint.setColor(coverLineColor);
        return this;
    }

    public BeizerCurveLine setCoverLineWidth(float widthDpValue) {
        if (coverPaint != null) coverPaint.setStrokeWidth(dip2px(widthDpValue));
        return this;
    }

    public BeizerCurveLine setFillState(boolean isFilled) {
        this.isFilled = isFilled;
        return this;
    }

    public BeizerCurveLine setXYShowState(boolean xyShowState) {
        this.isShowXy = xyShowState;
        return this;
    }

    public BeizerCurveLine setXYColor(int colorResId) {
        if (xyPaint != null) xyPaint.setColor(ContextCompat.getColor(getContext(), colorResId));
        return this;
    }

    public BeizerCurveLine setHintLineColor(int colorResId) {
        if (hintPaint != null) hintPaint.setColor(ContextCompat.getColor(getContext(), colorResId));
        return this;
    }

    public BeizerCurveLine setShowFloat(boolean isShowFloat) {
        this.isShowFloat = isShowFloat;
        return this;
    }

    public BeizerCurveLine setShowHintLines(boolean showHintLines) {
        this.isShowHintLines = showHintLines;
        return this;
    }

    public BeizerCurveLine setAnimDurationTime(long duration) {
        this.duration = duration;
        return this;
    }

    private BeizerCurveLine setHintLinesNum(int hintLinesNum) {
        if (hintLinesNum > -1) {
            this.hintLinesNum = hintLinesNum;
        }
        return this;
    }

    public BeizerCurveLine setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    private LinearGradient getShader(int colors[]) {
        if (colors != null) {
            if (colors.length == 0) {//说明用户没有传入颜色值 此时显示默认的颜色来填充
                colors = new int[]{Color.GREEN, ContextCompat.getColor(getContext(), R.color.alpha_sharder)};
            }
        } else {
            colors = new int[]{Color.GREEN, Color.GREEN, ContextCompat.getColor(getContext(), R.color.alpha_sharder)};
        }
        return new LinearGradient(//填充方向是以Y轴为基准 若是自上而下 就是 0,0,0,getMeasuredHeight()
                startX, endY, startX, startY,
                colors,
                null, Shader.TileMode.CLAMP);
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
                if (floatStr.length() > 2 && floatStr.contains("/")) {
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

    public static class CurveLineBuilder {

        public Map<Integer, List<ChartBean>> curveDataLists;
        public Map<Integer, Integer> curvePaintColors;
        public Map<Integer, Integer> curveShaderColors;
        private int index;

        public CurveLineBuilder() {
            curveDataLists = new HashMap<>();
            curvePaintColors = new HashMap<>();
            curveShaderColors = new HashMap<>();
        }

        public CurveLineBuilder builder(List<ChartBean> curveBeans, int coverLineColor, int shaderColor) {
            if (curveBeans != null && curveBeans.size() > 0) {
                int index = this.index;
                this.curveDataLists.put(index, curveBeans);
                this.curvePaintColors.put(index, coverLineColor);
                this.curveShaderColors.put(index, shaderColor);
                this.index++;
            }
//            else {
//                throw new IllegalArgumentException("无效参数data或color");
//            }
            return this;
        }

        public void build(BeizerCurveLine beizerCurveLine) {
            beizerCurveLine.startDraw(curveDataLists, curvePaintColors, curveShaderColors);
        }
    }

    private void startDraw(Map<Integer, List<ChartBean>> curveDataLists, Map<Integer, Integer> curvePaintColors, Map<Integer, Integer> curveShaderColors) {
        if (curveDataLists == null || curveDataLists.size() == 0 || curvePaintColors == null || curvePaintColors.size() == 0)
            return;
        this.curveDataLists = curveDataLists;
        this.curvePaintColors = curvePaintColors;
        this.curveShaderColors = curveShaderColors;

        start();
    }

    private boolean starting = false;
    private boolean isFirst = true;
    private float mAnimatorValue;
    private ValueAnimator valueAnimator;

    private void startDraw() {
        if (curveDataLists == null || curveDataLists.size() == 0 || curvePaintColors == null || curvePaintColors.size() == 0)
            return;
        if (!isFirst || starting) {
            return;
        }
        starting = true;
        initPath();
        if (isPlayAnimator) {
            startAnimator();
        } else {
            invalidate();
        }
    }

    @Override
    public void start() {
        super.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isCover(BeizerCurveLine.this)) {
                startDraw();
            } else {
                this.post(new Runnable() {//可以避免页面未初始化完成造成的 空白
                    @Override
                    public void run() {//可以避免页面未初始化完成造成的 空白
                        if (isCover(BeizerCurveLine.this)) {
                            startDraw();
                        }
                    }
                });
            }
        } else {
            this.post(new Runnable() {
                @Override
                public void run() {
                    startDraw();
                }
            });
        }
    }

    private void startAnimator() {
        valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
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
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (valueAnimator != null && valueAnimator.isRunning()) valueAnimator.cancel();
        super.onDetachedFromWindow();
    }
}
