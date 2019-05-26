# Chart
功能 : 安卓原生canvas绘制 贝塞尔曲线、柱状图、饼状图
--
优化:
--
1. 仅绘制可视区域
2. 曲线图/柱状图 添加触摸操作 ,显示当前选中的数据
3. 优化控件onTouch事件与ScrollView滑动冲突 , 手指滑出控件区域 , scrollView拦截事件,响应自身滑动

Usage:
--
1. 添加jitpack.io到你的project下的build.gradle:
	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
			}
		}

2. 添加依赖（app里面的build.gradle）
	dependencies {
	       implementation 'com.github.good-good-study:Chart:1.0.0.0'
	}


有哪些图表?
--
BeizerCurveLine(贝塞尔曲线)、ChartPie(饼状图)、ChartBar(柱状图)
--
1. BeizerCurveLine

      <com.sxt.library.chart.BeizerCurveLine
              xmlns:android="http://schemas.android.com/apk/res/android"
              android:id="@+id/chart_line"
              android:layout_width="match_parent"
              android:layout_height="250dp"
              android:layout_gravity="center_horizontal|bottom"
              android:background="@drawable/white_solid_round_10"
              app:line_cover_width="3.5dp"
              app:line_duration="1500"
              app:line_hint_color="@color/main_blue"
              app:line_isCanTouch="true"
              app:line_isFilled="true"
              app:line_unit="@string/string_unit_xt" />


2. ChartPie

      <com.sxt.library.chart.ChartPie xmlns:app="http://schemas.android.com/apk/res-auto"
              android:id="@+id/chart_pie"
              android:layout_width="match_parent"
              android:layout_height="280dp"
              android:layout_gravity="bottom|center_horizontal"
              android:background="@drawable/white_solid_round_10"
              app:pie_duration="2000"
              app:pie_isDrawCenter="true"
              app:pie_isDrawLines="true"
              app:pie_isUseAnimator="true"
              app:pie_text_color="@color/text_color_2" />

3. ChartBar

      <com.sxt.library.chart.ChartBar xmlns:app="http://schemas.android.com/apk/res-auto"
              android:id="@+id/chartbar"
              android:layout_width="match_parent"
              android:layout_height="250dp"
              android:layout_gravity="center_horizontal|bottom"
              android:background="@drawable/white_solid_round_10"
              app:bar_duration="800"
              app:bar_isCanTouch="true"
              app:bar_isUseAnimator="true"/>
