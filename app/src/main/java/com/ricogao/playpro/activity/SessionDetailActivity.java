package com.ricogao.playpro.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.fragment.ChartFragment;
import com.ricogao.playpro.fragment.DetailFragment;
import com.ricogao.playpro.fragment.EventFragment;
import com.ricogao.playpro.fragment.FieldFragment;
import com.ricogao.playpro.fragment.TrackFragment;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;
import com.ricogao.playpro.model.Record;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/3/19.
 */

public class SessionDetailActivity extends FragmentActivity {

    public final static String TAG = SessionDetailActivity.class.getSimpleName();
    private long eventId;
    private EventFragment trackFragment, fieldFragment, chartFragment, detailFragment;
    private Subscription readDataSub;

    private Event currentEvent;

    @BindView(R.id.bar_track)
    View barTrack;
    @BindView(R.id.bar_field)
    View barField;
    @BindView(R.id.bar_chart)
    View barChart;
    @BindView(R.id.bar_detail)
    View barDetail;

    @OnClick(R.id.btn_track)
    void onTrackClick() {
        switchFragment(trackFragment);

        barTrack.setVisibility(View.VISIBLE);
        barField.setVisibility(View.INVISIBLE);
        barChart.setVisibility(View.INVISIBLE);
        barDetail.setVisibility(View.INVISIBLE);
    }


    @OnClick(R.id.btn_field)
    void onFieldClick() {
        if (fieldFragment == null) {
            fieldFragment = new FieldFragment();
            fieldFragment.setEvent(currentEvent);
        }
        switchFragment(fieldFragment);

        barTrack.setVisibility(View.INVISIBLE);
        barField.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.INVISIBLE);
        barDetail.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btn_chart)
    void onChartClick() {
        if (chartFragment == null) {
            chartFragment = new ChartFragment();
            chartFragment.setEvent(currentEvent);
        }

        switchFragment(chartFragment);

        barTrack.setVisibility(View.INVISIBLE);
        barField.setVisibility(View.INVISIBLE);
        barChart.setVisibility(View.VISIBLE);
        barDetail.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btn_details)
    void onDetailClick() {
        if (detailFragment == null) {
            detailFragment = new DetailFragment();
            detailFragment.setEvent(currentEvent);
        }
        switchFragment(detailFragment);

        barTrack.setVisibility(View.INVISIBLE);
        barField.setVisibility(View.INVISIBLE);
        barChart.setVisibility(View.INVISIBLE);
        barDetail.setVisibility(View.VISIBLE);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail_layout);
        ButterKnife.bind(this);

        trackFragment = new TrackFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, trackFragment).commit();
        eventId = getIntent().getLongExtra("eventId", 0);
        readData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (readDataSub != null && readDataSub.isUnsubscribed()) {
            readDataSub.unsubscribe();
        }
    }

    private Event readEvent(long eventId) {
        return new Select()
                .from(Event.class)
                .where(Condition.column(Event_Table.id.getNameAlias()).eq(eventId))
                .querySingle();
    }

    private void switchFragment(EventFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void readData() {
        readDataSub = Observable
                .just(readEvent(eventId))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        currentEvent = event;
                    }
                })
                .map(new Func1<Event, List<Record>>() {
                    @Override
                    public List<Record> call(Event event) {
                        return event.loadAssociatedRecords();
                    }
                }).subscribe(new Subscriber<List<Record>>() {
                    @Override
                    public void onCompleted() {
                        trackFragment.setEvent(currentEvent);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "Load record with error:" + e.getMessage());
                    }

                    @Override
                    public void onNext(List<Record> records) {
                        Log.i(TAG, records.size() + " records is found.");
                    }
                });

    }


}
