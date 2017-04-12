package com.ricogao.playpro.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.activity.SessionDetailActivity;
import com.ricogao.playpro.activity.SessionListActivity;
import com.ricogao.playpro.model.Analysis;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;
import com.ricogao.playpro.util.CircleTransform;
import com.ricogao.playpro.util.SharedPreferencesUtil;
import com.ricogao.playpro.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
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
import rx.subscriptions.CompositeSubscription;

/**
 * Created by ricogao on 2017/4/5.
 */

public class MainFragment extends Fragment {
    private final static String TAG = MainFragment.class.getSimpleName();

    @OnClick(R.id.btn_enter_session)
    protected void onSessionClick() {
        Intent it = new Intent(this.getContext(), SessionListActivity.class);
        startActivity(it);
    }

    @OnClick(R.id.panel_fastest_speed)
    void onFastestSpeedClick() {
        startEvent(fastestSpeedEvent.getId());
    }

    @OnClick(R.id.panel_longest_distance)
    void onLongestDistanceClick() {
        startEvent(longestDistanceEvent.getId());
    }

    @OnClick(R.id.panel_longest_duration)
    void onLongestDurationClick() {
        startEvent(longestDurationEvent.getId());
    }

    @BindView(R.id.radar_chart)
    RadarChart radarChart;

    @BindView(R.id.total_session_tv)
    TextView tvTotalSession;

    @BindView(R.id.total_distance_tv)
    TextView tvTotalDistance;

    @BindView(R.id.total_time_tv)
    TextView tvTotalDuration;

    @BindView(R.id.profile_name_tv)
    TextView tvName;

    @BindView(R.id.profile_img)
    ImageView imgProfile;

    @BindView(R.id.profile_position_tv)
    TextView tvPosition;

    @BindView(R.id.tv_fastest_speed)
    TextView tvFastestSpeed;
    @BindView(R.id.tv_longest_distance)
    TextView tvLongestDistance;
    @BindView(R.id.tv_longest_duration)
    TextView tvLongestDuration;
    @BindView(R.id.tv_date_fastest_speed)
    TextView tvDateFastestSpeed;
    @BindView(R.id.tv_date_longest_distance)
    TextView tvDateLongestDistance;
    @BindView(R.id.tv_date_longest_duration)
    TextView tvDateLongestDuration;


    private int sessionCount;
    private float totalDistance;
    private long totalDuration;
    private SharedPreferencesUtil spUtil;
    private Event fastestSpeedEvent, longestDurationEvent, longestDistanceEvent;

    private long longestDuration;
    private float fastestSpeed, longestDistance;
    private float topSpeedScore, avgSpeedScore, staminaScore, positionScore, activeScore, workRateScore;

    private CompositeSubscription loadDataSub;
    private List<Analysis> analysises;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loadDataSub = new CompositeSubscription();

        View view = inflater.inflate(R.layout.activity_main_content, container, false);
        ButterKnife.bind(this, view);

        spUtil = new SharedPreferencesUtil(this.getContext());
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loadDataSub != null && loadDataSub.isUnsubscribed()) {
            loadDataSub.unsubscribe();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        loadAnalysis();
        tvName.setText(spUtil.getUsername());
        tvPosition.setText(spUtil.getPosition().getPositionName());
        showProfileImage();
    }

    private void showProfileImage() {

        Picasso.with(this.getContext())
                .load(spUtil.getProfileImageUri())
                .placeholder(R.drawable.blank_profile)
                .centerCrop()
                .fit()
                .transform(new CircleTransform())
                .into(imgProfile);

    }

    private void showRadarChart() {

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(analysises.get(0).getTopSpeedScore(), 0));
        entries.add(new Entry(analysises.get(0).getAvgSpeedScore(), 1));
        entries.add(new Entry(analysises.get(0).getStaminaScore(), 2));
        entries.add(new Entry(analysises.get(0).getActiveScore(), 3));
        entries.add(new Entry(analysises.get(0).getPositionScore(), 4));
        entries.add(new Entry(analysises.get(0).getWorkRateScore(), 5));

        List<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(topSpeedScore / (float) analysises.size(), 0));
        entries2.add(new Entry(avgSpeedScore / (float) analysises.size(), 1));
        entries2.add(new Entry(staminaScore / (float) analysises.size(), 2));
        entries2.add(new Entry(activeScore / (float) analysises.size(), 3));
        entries2.add(new Entry(positionScore / (float) analysises.size(), 4));
        entries2.add(new Entry(workRateScore / (float) analysises.size(), 5));

        RadarDataSet dataSet = new RadarDataSet(entries, "Last session");
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setColor(Color.CYAN);
        dataSet.setDrawFilled(true);

        RadarDataSet dataSet2 = new RadarDataSet(entries2, "General");
        dataSet2.setValueTextColor(Color.WHITE);
        dataSet2.setColor(Color.YELLOW);
        dataSet2.setDrawFilled(true);

        List<String> labels = new ArrayList<>();
        labels.add("Top Speed");
        labels.add("Avg Speed");
        labels.add("Stamina");
        labels.add("Active");
        labels.add("Position");
        labels.add("Work rate");

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
    }

    private void loadAnalysis() {
        analysises = new ArrayList<>();

        Subscription subscription = Observable
                .just(new Select()
                        .from(Analysis.class)
                        .queryList())
                .doOnNext(new Action1<List<Analysis>>() {
                              @Override
                              public void call(List<Analysis> list) {
                                  analysises = list;
                                  topSpeedScore = 0;
                                  avgSpeedScore = 0;
                                  staminaScore = 0;
                                  activeScore = 0;
                                  positionScore = 0;
                                  workRateScore = 0;
                              }
                          }
                )
                .flatMap(new Func1<List<Analysis>, Observable<Analysis>>() {
                    @Override
                    public Observable<Analysis> call(List<Analysis> analysises) {
                        return Observable.from(analysises);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Analysis>() {
                    @Override
                    public void onCompleted() {
                        if (analysises.size() > 0) {
                            showRadarChart();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(Analysis analysis) {
                        topSpeedScore += analysis.getTopSpeedScore();
                        avgSpeedScore += analysis.getAvgSpeedScore();
                        staminaScore += analysis.getStaminaScore();
                        workRateScore += analysis.getWorkRateScore();
                        positionScore += analysis.getPositionScore();
                        activeScore += analysis.getActiveScore();
                    }
                });
        loadDataSub.add(subscription);
    }

    private void loadData() {

        Subscription subscription = Observable
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
                        totalDistance = 0;
                        totalDuration = 0;
                    }
                })
                .flatMap(new Func1<List<Event>, Observable<Event>>() {
                    @Override
                    public Observable<Event> call(List<Event> events) {
                        return Observable.from(events);
                    }
                })
                .doOnNext(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        event.loadAssociatedAnalysis();
                    }
                })
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {
                        showData();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Reading with error:" + e.getMessage());
                    }

                    @Override
                    public void onNext(Event event) {
                        totalDistance += event.getDistance();
                        totalDuration += event.getDuration();
                        if (event.getMaxSpeed() > fastestSpeed) {
                            fastestSpeed = event.getMaxSpeed();
                            fastestSpeedEvent = event;
                        }

                        if (event.getDuration() > longestDuration) {
                            longestDuration = event.getDuration();
                            longestDurationEvent = event;
                        }

                        if (event.getDistance() > longestDistance) {
                            longestDistance = event.getDistance();
                            longestDistanceEvent = event;
                        }
                    }
                });

        loadDataSub.add(subscription);
    }

    private void showData() {
        tvTotalSession.setText(sessionCount + "");
        tvTotalDistance.setText(String.format("%.2f", totalDistance * 0.001f) + " km");
        tvTotalDuration.setText(TimeUtil.formatHour(totalDuration) + " h");

        if (fastestSpeedEvent != null) {
            tvDateFastestSpeed.setText(new SimpleDateFormat("yyyy/MM/dd").format(fastestSpeedEvent.getTimestamp()));
            tvDateLongestDistance.setText(new SimpleDateFormat("yyyy/MM/dd").format(longestDistanceEvent.getTimestamp()));
            tvDateLongestDuration.setText(new SimpleDateFormat("yyyy/MM/dd").format(longestDurationEvent.getTimestamp()));

            tvFastestSpeed.setText(String.format("%.2f", fastestSpeed * 3.6f) + " km/h");
            tvLongestDistance.setText(String.format("%.2f", longestDistance * 0.001f) + " km");
            tvLongestDuration.setText(TimeUtil.formatDuration(longestDuration));
        }
    }

    private void startEvent(long eventId) {
        Intent it = new Intent(this.getContext(), SessionDetailActivity.class);
        it.putExtra("eventId", eventId);
        startActivity(it);
    }
}
