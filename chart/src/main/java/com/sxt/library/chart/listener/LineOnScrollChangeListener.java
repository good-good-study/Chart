package com.sxt.library.chart.listener;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;

import com.sxt.library.chart.base.BaseChart;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by izhaohu on 2017/12/5.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class LineOnScrollChangeListener implements View.OnScrollChangeListener {

    private List<View> lines = new ArrayList<>();

    public LineOnScrollChangeListener addLine(View line) {
        if (lines != null) {
            if (!lines.contains(line)) {
                lines.add(line);
            }
        } else {
            lines = new ArrayList<>();
            lines.add(line);
        }
        return this;
    }

    public void clearLines() {
        if (this.lines != null) this.lines.clear();
    }

    @Override
    public void onScrollChange(View view, int i, int i1, int i2, int i3) {
        if (lines != null && lines.size() > 0) {
            for (int j = 0; j < lines.size(); j++) {
//                if (lines.get(j) instanceof BeizerCurveLine) {
//                    ((BeizerCurveLine) lines.get(j)).start();
//                } else if (lines.get(j) instanceof ChartBar) {
//                    ((ChartBar) lines.get(j)).start();
//                }
                if (lines.get(j) instanceof BaseChart) {
                    ((BaseChart) lines.get(j)).start();
                    Log.i("onScrollChange", "onScrollChange");
                }
            }
        }
    }
}
