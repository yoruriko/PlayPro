package com.ricogao.playpro.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.ricogao.playpro.R;

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

public class TrackFragment extends EventFragment implements OnMapReadyCallback {

    private final static String TAG = TrackFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private Subscription readDataSub;

    private LatLngBounds.Builder builder;
    private List<Record> records;
    private List<LatLng> latLngs;
    private List<RecordHolder> holder;
    private GroundOverlay fieldOverlay;
    private TileOverlay currentOverlay;

    private boolean isFieldShow;

    @BindView(R.id.btn_type_track)
    ImageButton btnTypeTrack;
    @BindView(R.id.btn_type_heatmap)
    ImageButton btnTypeHeatmap;

    @BindView(R.id.tv_duration)
    TextView tvDuration;
    @BindView(R.id.tv_distance)
    TextView tvDistance;
    @BindView(R.id.tv_date)
    TextView tvDate;

    @BindView(R.id.tv_speed_slowest)
    TextView tvMinSpeed;
    @BindView(R.id.tv_speed_fastest)
    TextView tvMaxSpeed;
    @BindView(R.id.btn_show_field)
    ImageButton btnShowField;

    @OnClick(R.id.btn_type_track)
    void onTypeTrackClick(ImageButton btn) {
        showColouredTitle();
        btnTypeHeatmap.setSelected(false);
    }


    @OnClick(R.id.btn_type_heatmap)
    void onTypeHeatmapClick(ImageButton btn) {
        showHeatMap();
        btnTypeTrack.setSelected(false);
    }

    @OnClick(R.id.btn_show_field)
    void onShowFieldClick() {
        if (isFieldShow) {
            hideField();
        } else {
            showField();
        }
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


    private void processData() {

        tvDuration.setText(TimeUtil.formatDuration(event.getDuration()));
        tvDistance.setText(String.format("%.2f", event.getDistance() * 0.001f));
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getTimestamp()));

        tvMaxSpeed.setText("Fastest: " + String.format("%.2f", event.getMaxSpeed() * 3.6f) + " km/h");


        holder = new ArrayList<>();
        records = new ArrayList<>();
        latLngs = new ArrayList<>();
        builder = new LatLngBounds.Builder();

        readDataSub = Observable.from(event.getRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Record>() {
                    @Override
                    public void onCompleted() {
                        showColouredTitle();
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 17));
                        if (event.getFieldId() == -1) {
                            isFieldShow = false;
                            btnShowField.setVisibility(View.GONE);
                        } else {
                            showField();
                        }
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

    private void showField() {
        isFieldShow = true;
        btnShowField.setImageResource(R.drawable.ic_grid_on_black_24dp);

        List<LatLng> bound = event.getField().getBound();

        Location l1 = new Location("Playpro");
        l1.setLatitude(bound.get(0).latitude);
        l1.setLongitude(bound.get(0).longitude);
        Location l2 = new Location("Playpro");
        l2.setLatitude(bound.get(1).latitude);
        l2.setLongitude(bound.get(1).longitude);
        Location l3 = new Location("Playpro");
        l3.setLatitude(bound.get(3).latitude);
        l3.setLongitude(bound.get(3).longitude);

        float width = l1.distanceTo(l2);
        float height = l1.distanceTo(l3);
        float bering = l3.bearingTo(l1);


        fieldOverlay = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.footballpitch))
                .anchor(0, 0)
                .position(event.getField().getBound().get(0), width, height)
                .bearing(bering));
    }

    private void hideField() {
        isFieldShow = false;
        btnShowField.setImageResource(R.drawable.ic_grid_off_black_24dp);

        if (fieldOverlay != null) {
            fieldOverlay.remove();
            fieldOverlay = null;
        }
    }

    private void showColouredTitle() {
        if (currentOverlay != null) {
            currentOverlay.remove();
        }
        btnTypeTrack.setSelected(true);

        ColouredPolylineTileOverlay.PointCollection<RecordHolder> collection = new ColouredPolylineTileOverlay.PointCollection<RecordHolder>() {
            @Override
            public List<RecordHolder> getPoints() {
                return holder;
            }
        };

        ColouredPolylineTileOverlay<RecordHolder> overlayProvider = new ColouredPolylineTileOverlay<RecordHolder>(this.getContext(), collection, 0, 12.52);
        currentOverlay = mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(overlayProvider));
    }


    private void showHeatMap() {
        if (currentOverlay != null) {
            currentOverlay.remove();
        }
        btnTypeHeatmap.setSelected(true);

        HeatmapTileProvider provider = new HeatmapTileProvider.Builder().data(latLngs).build();
        currentOverlay = mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
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
