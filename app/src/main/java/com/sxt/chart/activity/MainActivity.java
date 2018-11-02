package com.sxt.chart.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import com.sxt.chart.R;
import com.sxt.library.chart.bean.ChartBean;
import com.sxt.library.chart.bean.ChartPieBean;
import com.sxt.library.chart.listener.LineOnScrollChangeListener;
import com.sxt.library.chart.BeizerCurveLine;
import com.sxt.library.chart.ChartBar;
import com.sxt.library.chart.ChartPie;
import com.sxt.library.chart.CircleProgressView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    String[] lineName;
    String[] lineUnit;
    int[] lineColor;
    int[] shaderColor;

    private Handler handler = new Handler();
    private List<ChartBean> chartBeanList0;
    private List<ChartBean> chartBeanList;
    private List<ChartPieBean> pieBeanList;

    private ViewSwitcher viewSwitcher;
    private LinearLayout lineLayoutList;
    private NestedScrollView scrollView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LineOnScrollChangeListener onScrollChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    protected void initView() {
        scrollView = findViewById(R.id.scrollview);
        viewSwitcher = findViewById(R.id.viewSwitcher);
        lineLayoutList = findViewById(R.id.line_layout_list);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorAccent, R.color.main_blue, R.color.main_green);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lineLayoutList.removeAllViews();
                        swipeRefreshLayout.setRefreshing(false);
                        lineLayoutList.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(MainActivity.this, R.anim.layout_animation_vertical));
                        init();
                    }
                }, 2000);
            }
        });
        initData();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);//第一次来 并不会调用onRefresh方法  android bug
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        viewSwitcher.setDisplayedChild(1);
                        init();
                    }
                }, 2000);
            }
        });
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (onScrollChangeListener == null) {
                onScrollChangeListener = new LineOnScrollChangeListener();
                scrollView.setOnScrollChangeListener(onScrollChangeListener);
            } else {
                onScrollChangeListener.clearLines();
            }
        }
        for (int i = 0; i < 20; i++) {
            if (i == 0) {
                drawPie();
            } else if (i == 1) {
                drawBar();
            } else if (i == 2) {
                drawLine();
            } else if (i == 3) {
                drawCircleProgress();
            } else if (i % 2 == 0) {
                drawBar();
            } else if (i % 3 == 0) {
                drawLine();
            } else {
                drawPie();
            }
        }
    }

    private void drawCircleProgress() {
        View view = View.inflate(this, R.layout.item_circle_progress, null);
        lineLayoutList.addView(view);
        CircleProgressView itemView = (CircleProgressView) view.findViewById(R.id.chart_circle_progress);
        itemView
                .setDuration(2000)
                .setLabels(
                        new String[]{"运动详情"},
                        new int[]{R.color.colorPrimaryDark}).setProgress(new Random().nextInt(361),
                new Random().nextInt(10001),
                "今日步数");
    }

    //柱状图-------------------------------------------------------------------------------------
    private void drawBar() {
        View barView = View.inflate(this, R.layout.item_chart_bar, null);
        lineLayoutList.addView(barView);
        barView.setTag(lineLayoutList.getChildCount() - 1);

        final ChartBar chartBar = (ChartBar) barView.findViewById(R.id.chartbar);
        //设置柱状图的数据源
        chartBar
                .setRectData(chartBeanList0)
                .setLabels(
                        new String[]{getString(R.string.string_label_smzl), getString(R.string.string_label_smzl_bad), getString(R.string.string_label_smzl_good)},
                        new int[]{lineColor[0], lineColor[1], lineColor[3]})
                .start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            onScrollChangeListener.addLine(chartBar);
        }
        chartBar.start();
    }

    private void initData() {
        lineName = new String[]{getString(R.string.string_label_press), getString(R.string.string_label_xt), getString(R.string.string_label_hb), getString(R.string.string_label_bt)};
        lineColor = new int[]{R.color.violet_rgb_185_101_255, R.color.red_rgb_255_127_87, R.color.red, R.color.blue_rgba_24_261_255, R.color.green_rgb_40_220_162};
        shaderColor = new int[]{R.color.violet_sharder, R.color.red_sharder, R.color.red_sharder, R.color.blue_sharder, R.color.green_sharder};
        lineUnit = new String[]{getString(R.string.string_unit_xt), getString(R.string.string_unit_hb), getString(R.string.string_unit_press), getString(R.string.string_unit_bt)};

        chartBeanList = new ArrayList<>();
        chartBeanList.add(new ChartBean("9月", 20));
        chartBeanList.add(new ChartBean("1", 80));
        chartBeanList.add(new ChartBean("2", 58));
        chartBeanList.add(new ChartBean("3", 100));
        chartBeanList.add(new ChartBean("4", 20));
        chartBeanList.add(new ChartBean("5", 70));
        chartBeanList.add(new ChartBean("6", 10));


        chartBeanList0 = new ArrayList<>();
        chartBeanList0.add(new ChartBean("9月", 20));
        chartBeanList0.add(new ChartBean("1", 80));
        chartBeanList0.add(new ChartBean("2", 58));
        chartBeanList0.add(new ChartBean("3", 100));
        chartBeanList0.add(new ChartBean("4", 20));
        chartBeanList0.add(new ChartBean("5", 70));
        chartBeanList0.add(new ChartBean("6", 10));
        chartBeanList0.add(new ChartBean("7", 30));
        chartBeanList0.add(new ChartBean("8", 5));

        pieBeanList = new ArrayList<>();
        pieBeanList.add(new ChartPieBean(3090, "押金使用", R.color.main_green));
        pieBeanList.add(new ChartPieBean(501f, "天猫购物", R.color.blue_rgba_24_261_255));
        pieBeanList.add(new ChartPieBean(800, "话费充值", R.color.orange));
        pieBeanList.add(new ChartPieBean(1000, "生活缴费", R.color.red_2));
        pieBeanList.add(new ChartPieBean(2300, "早餐", R.color.progress_color_default));
    }

    private void drawPie() {
        //底部的曲线图
        View childAt = View.inflate(this, R.layout.item_chart_pie, null);
        lineLayoutList.addView(childAt);
        ChartPie chartPie = childAt.findViewById(R.id.chart_pie);
        chartPie.setData(pieBeanList).start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //将当前曲线添加到ScrollView的滑动监听中
            onScrollChangeListener.addLine(chartPie);
        }
        chartPie.start();
    }

    private void drawLine() {
        //底部的曲线图
        View childAt = View.inflate(this, R.layout.item_chart_line, null);
        lineLayoutList.addView(childAt);
        BeizerCurveLine chartLine = (BeizerCurveLine) childAt.findViewById(R.id.chart_line);
        BeizerCurveLine.CurveLineBuilder builder = new BeizerCurveLine.CurveLineBuilder();
        List<ChartBean> chartBeans = new ArrayList<>();

        for (int y = 0; y < chartBeanList.size(); y++) {
            ChartBean chartBean = chartBeanList.get(y);
            chartBeans.add(new ChartBean(chartBean.x, chartLine.parseFloat(String.valueOf(chartBean.y))));
        }
        chartLine.setMaxXNum(6);

        chartLine.setLabels(new String[]{lineName[0], getString(R.string.string_label_press_hight), getString(R.string.string_label_press_lower)}, new int[]{lineColor[0], lineColor[2], lineColor[1]});
        builder.builder(chartBeans, lineColor[0], shaderColor[0]);

        builder.build(chartLine);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //将当前曲线添加到ScrollView的滑动监听中
            onScrollChangeListener.addLine(chartLine);
        }
        chartLine.start();
    }
}
