package com.ricogao.playpro.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.ricogao.playpro.R;

import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Record;
import com.ricogao.playpro.util.ColouredPolylineTileOverlay;
import com.ricogao.playpro.util.TimeUtil;

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
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/4/4.
 */

public class TrackFragment extends Fragment implements OnMapReadyCallback {

    private final static String TAG = TrackFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private Subscription readDataSub;

    private Event event;

    private double distance;
    private long duration;
    private LatLngBounds.Builder builder;
    private List<Record> records;
    private List<LatLng> latLngs;
    private CameraUpdate update;
    private List<RecordHolder> holder;

    @BindView(R.id.btn_type_track)
    ImageButton btnTypeTrack;
    @BindView(R.id.btn_type_sate)
    ImageButton btnTypeSate;
    @BindView(R.id.btn_type_heatmap)
    ImageButton btnTypeHeatmap;

    @BindView(R.id.tv_duration)
    TextView tvDuration;
    @BindView(R.id.tv_distance)
    TextView tvDistance;
    @BindView(R.id.tv_date)
    TextView tvDate;

    @OnClick(R.id.btn_type_track)
    void onTypeTrackClick(ImageButton btn) {
        showColouredTitle();
        btnTypeHeatmap.setSelected(false);
        btnTypeSate.setSelected(false);
    }


    @OnClick(R.id.btn_type_heatmap)
    void onTypeHeatmapClick(ImageButton btn) {
        showHeatMap();
        btnTypeTrack.setSelected(false);
        btnTypeSate.setSelected(false);
    }


    @OnClick(R.id.btn_type_sate)
    void onTypeStateClick(ImageButton btn) {
        btn.setSelected(true);
        btnTypeHeatmap.setSelected(false);
        btnTypeTrack.setSelected(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_fragment_layout, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (readDataSub != null && readDataSub.isUnsubscribed()) {
            readDataSub.unsubscribe();
        }
    }

    private void initView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        processData();
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    private void processData() {

        tvDuration.setText(TimeUtil.formatDuration(event.getDuration()));
        tvDistance.setText(String.format("%.2f", event.getDistance() * 0.001f));
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getTimestamp()));


        holder = new ArrayList<RecordHolder>();
        records = new ArrayList<Record>();
        latLngs = new ArrayList<LatLng>();
        builder = new LatLngBounds.Builder();
        duration = event.getDuration();
        distance = event.getDistance();

        readDataSub = Observable.from(event.getRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Record>() {
                    @Override
                    public void onCompleted() {
                        showColouredTitle();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Process data with error:" + e.getMessage());
                    }

                    @Override
                    public void onNext(Record record) {
                        RecordHolder rh = new RecordHolder(record);
                        builder.include(rh.getLatLng());
                        latLngs.add(rh.getLatLng());
                        holder.add(rh);
                        records.add(record);
                    }
                });
    }

    private void showColouredTitle() {
        btnTypeTrack.setSelected(true);
        mGoogleMap.clear();

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 17));

        ColouredPolylineTileOverlay.PointCollection<RecordHolder> collection = new ColouredPolylineTileOverlay.PointCollection<RecordHolder>() {
            @Override
            public List<RecordHolder> getPoints() {
                return holder;
            }
        };

        ColouredPolylineTileOverlay<RecordHolder> overlayProvider = new ColouredPolylineTileOverlay<RecordHolder>(this.getContext(), collection, 0, 12.52);
        mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(overlayProvider));
    }


    private void showHeatMap() {
        btnTypeHeatmap.setSelected(true);
        mGoogleMap.clear();

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 17));
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder().data(latLngs).build();
        mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
    }


    private class RecordHolder implements ColouredPolylineTileOverlay.PointHolder {
        Record record;

        RecordHolder(Record record) {
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
