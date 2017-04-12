package com.ricogao.playpro.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Record;
import com.ricogao.playpro.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/4/5.
 */

public class ChartFragment extends EventFragment {

    @BindView(R.id.speed_line_chart)
    LineChart speedChart;

    @BindView(R.id.step_line_chart)
    LineChart stepChart;

    @BindView(R.id.radar_chart)
    RadarChart statsChart;

    @BindColor(R.color.background_card)
    int backgroundCard;

    @BindColor(R.color.font_grey)
    int fontGrey;

    private Subscription subscription;

    private List<Entry> speedEntries;
    private List<Entry> stepEntries;
    private List<String> labels;
    private long start;

    private int count;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chart_fragment_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showChart();
    }

    @Override
    public void onDestroy() {
        if (subscription != null && subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void setEvent(Event event) {
        super.setEvent(event);
        processChart();
    }

    private void processChart() {
        speedEntries = new ArrayList<>();
        stepEntries = new ArrayList<>();
        labels = new ArrayList<>();
        start = event.getRecords().get(0).getTimestamp();

        subscription = Observable
                .from(event.getRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<Record>() {
                    @Override
                    public void call(Record record) {
                        speedEntries.add(new Entry((record.getSpeed() * 3.6f), count));
                        stepEntries.add(new Entry(record.getSteps(), count));
                        labels.add(TimeUtil.formatMinSec(record.getTimestamp() - start));
                        count++;
                    }
                });

    }

    private void showChart() {
        showLineChart(speedChart, speedEntries, labels);
        showLineChart(stepChart, stepEntries, labels);
        showRadarChart();
    }


    private void showLineChart(LineChart lineChart, List<Entry> entries, List<String> labels) {
        LineDataSet dataSet = new LineDataSet(entries, "chart");

        dataSet.setDrawValues(false);
        dataSet.setDrawCubic(true);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);

        LineData data = new LineData(labels, dataSet);

        lineChart.setGridBackgroundColor(backgroundCard);
        lineChart.getXAxis().setTextColor(fontGrey);
        lineChart.getAxisLeft().setTextColor(fontGrey);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDescription("Time: minute");
        lineChart.setDescriptionColor(fontGrey);

        lineChart.setData(data);
        lineChart.invalidate();
    }

    private void showRadarChart() {
        if (event.getAnalysis() == null) {
            return;
        }

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(event.getAnalysis().getTopSpeedScore(), 0));
        entries.add(new Entry(event.getAnalysis().getAvgSpeedScore(), 1));
        entries.add(new Entry(event.getAnalysis().getStaminaScore(), 2));
        entries.add(new Entry(event.getAnalysis().getActiveScore(), 3));
        entries.add(new Entry(event.getAnalysis().getPositionScore(), 4));
        entries.add(new Entry(event.getAnalysis().getWorkRateScore(), 5));

        RadarDataSet dataSet = new RadarDataSet(entries, "Last session");
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setColor(Color.CYAN);
        dataSet.setDrawFilled(true);


        List<String> labels = new ArrayList<>();
        labels.add("Top Speed");
        labels.add("Avg Speed");
        labels.add("Stamina");
        labels.add("Active");
        labels.add("Position");
        labels.add("Work rate");


        RadarData data = new RadarData(labels, dataSet);

        statsChart.setWebColor(Color.WHITE);
        statsChart.getXAxis().setTextColor(Color.WHITE);
        statsChart.getYAxis().setDrawLabels(false);
        statsChart.setDescription("");
        statsChart.setData(data);
        statsChart.getLegend().setEnabled(false);
        statsChart.invalidate();
    }

}
