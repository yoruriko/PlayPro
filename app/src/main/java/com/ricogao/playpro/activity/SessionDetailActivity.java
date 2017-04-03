package com.ricogao.playpro.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Event_Table;
import com.ricogao.playpro.model.Record;
import com.ricogao.playpro.util.ColouredPolylineTileOverlay;


import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/3/19.
 */

public class SessionDetailActivity extends FragmentActivity implements OnMapReadyCallback {

    public final static String TAG = SessionDetailActivity.class.getSimpleName();
    private long eventId;
    private GoogleMap mGoogleMap;
    private Subscription readDataSub;
    private double distance;
    private long duration;
    private LatLngBounds.Builder builder;
    private List<RecordHolder> holder;
    private List<Record> records;

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
        readData();
    }

    private void readData() {
        readDataSub = Observable
                .just(readEvent(eventId))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        holder = new ArrayList<RecordHolder>();
                        records = new ArrayList<Record>();
                        builder = new LatLngBounds.Builder();
                        duration = event.getDuration();
                        distance = event.getDistance();
                    }
                })
                .map(new Func1<Event, List<Record>>() {

                    @Override
                    public List<Record> call(Event event) {
                        return event.loadAssociatedRecords();
                    }
                })
                .flatMap(new Func1<List<Record>, Observable<Record>>() {

                    @Override
                    public Observable<Record> call(List<Record> records) {
                        return Observable.from(records);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Record>() {
                    @Override
                    public void onCompleted() {
                        showColouredTitle();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Record loading with Error:" + e.getMessage());
                    }

                    @Override
                    public void onNext(Record record) {
                        RecordHolder rh = new RecordHolder(record);
                        builder.include(rh.getLatLng());
                        holder.add(rh);
                        records.add(record);
                    }
                });
    }

    private void showColouredTitle() {
        mGoogleMap.clear();

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 17));

        ColouredPolylineTileOverlay.PointCollection<RecordHolder> collection = new ColouredPolylineTileOverlay.PointCollection<RecordHolder>() {
            @Override
            public List<RecordHolder> getPoints() {
                return holder;
            }
        };

        ColouredPolylineTileOverlay<RecordHolder> overlayProvider = new ColouredPolylineTileOverlay<RecordHolder>(this, collection, 0, 12.52);
        mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(overlayProvider));


    }

    private Event readEvent(long eventId) {
        return new Select()
                .from(Event.class)
                .where(Condition.column(Event_Table.id.getNameAlias()).eq(eventId))
                .querySingle();
    }

    private class RecordHolder implements ColouredPolylineTileOverlay.PointHolder {
        Record record;

        public RecordHolder(Record record) {
            this.record = record;
        }

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
