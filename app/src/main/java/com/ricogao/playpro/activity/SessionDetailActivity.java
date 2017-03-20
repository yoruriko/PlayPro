package com.ricogao.playpro.activity;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;
import com.ricogao.playpro.model.Record;
import com.ricogao.playpro.util.ColouredPolylineTileOverlay;

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

public class SessionDetailActivity extends FragmentActivity implements OnMapReadyCallback {
    private long eventId;
    private GoogleMap mGoogleMap;
    private Subscription readDataSub;
    private double distance;
    private long duration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail_layout);
        ButterKnife.bind(this);
        initView();
        eventId = getIntent().getLongExtra("eventId", 0);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (readDataSub != null && readDataSub.isUnsubscribed()) {
            readDataSub.unsubscribe();
        }
    }

    private void initView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        //readData();
    }

    private void readData() {
        readDataSub = Observable
                .create(new Observable.OnSubscribe<Event>() {
                    @Override
                    public void call(Subscriber<? super Event> subscriber) {
                        subscriber.onNext(readEvent(eventId));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        duration = event.getDuration();
                        distance = event.getDistance();
                    }
                })
                .map(new Func1<Event, List<Record>>() {

                    @Override
                    public List<Record> call(Event event) {
                        return event.getRecords();
                    }
                })
                .subscribe();
    }

    private Event readEvent(long eventId) {
        return new Select()
                .from(Event.class)
                .where(Condition.column(Event_Table.id.getNameAlias()).eq(eventId))
                .querySingle();
    }

    private class RecordHolder implements ColouredPolylineTileOverlay.PointHolder {
        Record record;

        @Override
        public LatLng getLatLng() {
            return new LatLng(record.getLatitude(), record.getLongitude());
        }

        @Override
        public long getTime() {
            return record.getTimestamp();
        }
    }
}
