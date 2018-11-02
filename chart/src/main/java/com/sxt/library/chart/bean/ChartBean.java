package com.sxt.library.chart.bean;

/**
 * Created by sxt on 2017/7/13.
 */

public class ChartBean {

    public String x;
    public float y;
    public long millis;

    public ChartBean(String x, float y) {
        this.x = x;
        this.y = y;
    }


    public ChartBean(String x, float y, long millis) {
        this.x = x;
        this.y = y;
        this.millis = millis;
    }

    @Override
    public String toString() {
        return "ChartBean{" +
                "x='" + x + '\'' +
                ", y=" + y +
                '}';
    }
}
