package com.sxt.library.chart.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by izhaohu on 2017/8/22.
 */
public class DateFormatUtil {

    /**
     * 日期转换成秒数
     */
    public static long getSecondsFromDate(String expireDate) {
        if (expireDate == null || expireDate.trim().equals(""))
            return 0;
        if (expireDate.contains("+0800")) {
            expireDate = expireDate.replaceAll("\\+0800", "");
        }
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(expireDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 日期转换成秒数
     */
    public static long getSecondsFromDate(String expireDate, String flag) {
        if (expireDate == null || expireDate.trim().equals(""))
            return 0;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(flag);
        Date date = null;
        try {
            date = sdf.parse(expireDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public static String getTimeHHMMSS(long millis) {
        if (millis > 0) {
            long minute = 60 * 1000L;
            long hour = millis * 60;
            int hours = (int) (millis / hour);
            int minutes = (int) ((millis - hours * hour) / minute);
            int seconds = (int) ((millis - minutes * minute) / 1000);

            return (hours == 0 ? "" : (hours < 10 ? "0" + hours : hours + ":"))
                    + (minutes == 0 ? "00" : minutes < 10 ? "0" + minutes : minutes)
                    + ":" + (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds);
        }
        return "00:00";
    }

    /**
     * 秒数转化为日期
     *
     * @param seconds 当前时间毫秒值
     * @param flag    目标日期格式
     * @return
     */
    public static String getDateFromSeconds(String seconds, String flag) {
        if (seconds == null)
            return " ";
        else {
            Date date = new Date();
            try {
                date.setTime(Long.parseLong(seconds));
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(flag);
            return sdf.format(date);
        }
    }

    /**
     * @param beginTime 课程开始时间
     * @param endTime   课程结束时间
     * @return 如果时间已过期 则返回 "课程已过期"
     * 距离开课大于24小时 , 返回 "距离开课还有n天"
     * 距离开课大于8小时  , 返回 "今日开课"
     */
    public static String formatTime(String beginTime, String endTime) {

        if (beginTime == null || endTime == null) return "课程已过期";

        long currentTimeMillis = System.currentTimeMillis();//计算系统当前秒数
        long brginTimeMillis = getSecondsFromDate(beginTime);//计算开始时间
        long endTimeMillis = getSecondsFromDate(endTime);//计算结束时间
        if (currentTimeMillis >= endTimeMillis) {
            return "课程已过期";
        }

        String currentMM = getDateFromSeconds(String.valueOf(currentTimeMillis), "MM");
        String currentDD = getDateFromSeconds(String.valueOf(currentTimeMillis), "dd");

        String beginDD = getDateFromSeconds(String.valueOf(brginTimeMillis), "dd");
        String beginMM = getDateFromSeconds(String.valueOf(brginTimeMillis), "MM");

        if (currentDD.startsWith("0")) {
            currentDD = currentDD.substring(0, beginDD.length());
        }
        if (beginDD.startsWith("0")) {
            beginDD = beginDD.substring(0, beginDD.length());
        }
        int cd = Integer.parseInt(currentDD);
        int bd = Integer.parseInt(beginDD);

        if (currentMM.equals(beginMM) && currentDD.equals(beginDD)) {//同月 并且 同一天
            return "今日开课";
        } else if (currentMM.equals(beginMM) && !currentDD.equals(beginDD)) {//同月 不同一天
            if (bd - cd == 1) {
                return "明天开课";
            } else {
                return "距离开课还有" + (bd - cd) + "天";
            }
        } else if (!currentMM.equals(beginMM)) {//不同月份 暂时以 24小时为一天 计算

            if (brginTimeMillis % currentTimeMillis == 0) {
                long day = (brginTimeMillis - currentTimeMillis) / (24 * 60 * 60 * 1000);
                return "距离开课还有" + day + "天";
            } else {
                long day = (brginTimeMillis - currentTimeMillis) / (24 * 60 * 60 * 1000);
                return "距离开课还有" + (day + 1) + "天";
            }
        }
        return "";
    }

}
