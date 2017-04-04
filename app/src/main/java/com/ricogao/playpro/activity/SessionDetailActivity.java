package com.ricogao.playpro.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.fragment.TrackFragment;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;
import com.ricogao.playpro.model.Record;


import java.util.List;

import butterknife.ButterKnife;
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
    private TrackFragment trackFragment;
    private Subscription readDataSub;

    private Event currentEvent;


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
