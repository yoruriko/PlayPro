package com.ricogao.playpro.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;
import com.ricogao.playpro.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/3/15.
 */

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    @OnClick(R.id.btn_start)
    protected void onStartClick() {
        Intent it = new Intent(MainActivity.this, RecordActivity.class);
        startActivity(it);
    }

    @OnClick(R.id.btn_enter_session)
    protected void onSessionClick() {
        Intent it = new Intent(MainActivity.this, SessionListActivity.class);
        startActivity(it);
    }

    @BindView(R.id.radar_chart)
    RadarChart radarChart;

    @BindView(R.id.total_session_tv)
    TextView tvTotalSession;

    @BindView(R.id.total_distance_tv)
    TextView tvTotalDistance;

    @BindView(R.id.total_time_tv)
    TextView tvTotalDuration;

    private int sessionCount;
    private float totalDistance;
    private long totalDuration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_layout);
        ButterKnife.bind(this);

        loadData();


        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(3f, 0));
        entries.add(new Entry(4f, 1));
        entries.add(new Entry(7f, 2));
        entries.add(new Entry(8f, 3));
        entries.add(new Entry(2f, 4));
        entries.add(new Entry(5f, 5));

        List<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(4f, 0));
        entries2.add(new Entry(6f, 1));
        entries2.add(new Entry(5f, 2));
        entries2.add(new Entry(6f, 3));
        entries2.add(new Entry(4f, 4));
        entries2.add(new Entry(5f, 5));

        RadarDataSet dataSet = new RadarDataSet(entries, "Last session");
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setColor(Color.CYAN);
        dataSet.setDrawFilled(true);

        RadarDataSet dataSet2 = new RadarDataSet(entries2, "General");
        dataSet2.setValueTextColor(Color.WHITE);
        dataSet2.setColor(Color.YELLOW);
        dataSet2.setDrawFilled(true);

        List<String> labels = new ArrayList<>();
        labels.add("Speed");
        labels.add("Power");
        labels.add("Stamina");
        labels.add("Agility");
        labels.add("Recovery time");
        labels.add("Skill");

        List<RadarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        dataSets.add(dataSet2);
        RadarData data = new RadarData(labels, dataSets);

        radarChart.setWebColor(Color.WHITE);
        radarChart.getXAxis().setTextColor(Color.WHITE);
        radarChart.getYAxis().setDrawLabels(false);
        radarChart.setDescription("");
        radarChart.setData(data);
        radarChart.getLegend().setTextColor(Color.WHITE);
        radarChart.invalidate();
        radarChart.animate();

    }

    private void loadData() {

        Subscription loadDataSub = Observable
                .just(new Select()
                        .from(Event.class)
                        .orderBy(Event_Table.timestamp.getNameAlias(), false)
                        .queryList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        sessionCount = events.size();
                    }
                })
                .flatMap(new Func1<List<Event>, Observable<Event>>() {
                    @Override
                    public Observable<Event> call(List<Event> events) {
                        return Observable.from(events);
                    }
                })
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {
                        tvTotalSession.setText(sessionCount + "");
                        tvTotalDistance.setText(String.format("%.2f", totalDistance * 0.001f) + " km");
                        tvTotalDuration.setText(TimeUtil.formatHour(totalDuration) + " h");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Reading with error:" + e.getMessage());
                    }

                    @Override
                    public void onNext(Event event) {
                        totalDistance += event.getDistance();
                        totalDuration += event.getDuration();
                    }
                });
    }
}
