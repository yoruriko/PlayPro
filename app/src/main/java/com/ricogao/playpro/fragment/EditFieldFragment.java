package com.ricogao.playpro.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ricogao.playpro.R;
import com.ricogao.playpro.model.Analysis;
import com.ricogao.playpro.model.Event;
import com.ricogao.playpro.model.Field;
import com.ricogao.playpro.model.Record;
import com.ricogao.playpro.util.AnalysisUtil;
import com.ricogao.playpro.util.PolygonUtils;
import com.ricogao.playpro.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ricogao on 2017/4/8.
 */

public class EditFieldFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    private final static String TAG = EditFieldFragment.class.getSimpleName();
    private GoogleMap mGoogleMap;

    //order in top left,top right,bottom right, bottom left
    private List<LatLng> estimateBounds;

    private List<LatLng> currentBounds;

    private List<LatLng> track;
    private Subscription subscription;
    private LatLngBounds.Builder builder;
    private Marker markers[] = new Marker[3];

    private Polygon polygonBound;
    private Field currentField;
    private Analysis currentAnalysis;
    private Event event;
    private boolean isNewField;

    private PolygonUtils polygonUtils;
    private SharedPreferencesUtil spUtil;


    private int insideCount, centreCount, fontCount, backCount;

    private List<LatLng> centreBound, fontBound, backBound;

    private SaveFieldListener listener;

    public interface SaveFieldListener {
        void onSaveFinished();
    }

    public void setListener(SaveFieldListener listener) {
        this.listener = listener;
    }

    @BindColor(R.color.blue50)
    int blue50;
    @BindView(R.id.edt_name)
    EditText edtName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_field_fragment_layout, container, false);
        ButterKnife.bind(this, view);
        initView();
        polygonUtils = new PolygonUtils();
        spUtil = new SharedPreferencesUtil(this.getContext());
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerDragListener(this);
        processData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null && subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    private void initView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    public void setData(Event event, Field field) {
        this.event = event;
        if (field != null) {
            currentField = field;
            isNewField = false;
        } else {
            currentField = new Field();
            isNewField = true;
        }
    }

    public void saveField() {
        runAnalysis();
    }

    private void setField() {
        currentField.setName(edtName.getText().toString());
        currentField.setBound(currentBounds);
        currentField.save();
        event.setFieldId(currentField.getId());
        event.setField(currentField);
    }

    private void setAnalysis() {
        currentAnalysis.save();
        event.setAnalysisId(currentAnalysis.getId());
        event.setAnalysis(currentAnalysis);
    }

    private void processData() {
        builder = new LatLngBounds.Builder();
        track = new ArrayList<>();

        subscription = Observable
                .from(event.getRecords())
                .map(new Func1<Record, LatLng>() {

                    @Override
                    public LatLng call(Record record) {
                        return new LatLng(record.getLatitude(), record.getLongitude());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<LatLng>() {
                    @Override
                    public void onCompleted() {
                        edtName.setText(currentField.getName());
                        LatLngBounds b = builder.build();
                        estimateBounds = getBounds(b);
                        showDefaultBounds();
                        showTrack(track);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(b, 17));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(LatLng latLng) {
                        builder.include(latLng);
                        track.add(latLng);
                    }
                });
    }


    private void showTrack(List<LatLng> track) {
        mGoogleMap.addPolyline(new PolylineOptions().addAll(track).width(5).color(Color.RED).geodesic(true));
    }

    private void showDefaultBounds() {
        if (isNewField) {
            showBounds(estimateBounds);
        } else {
            showBounds(currentField.getBound());
        }
    }

    private void runAnalysis() {

        getCentreBound();
        getFontBackBound();

        subscription = Observable.from(event.getRecords())
                .subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .map(new Func1<Record, LatLng>() {
                    @Override
                    public LatLng call(Record record) {
                        return new LatLng(record.getLatitude(), record.getLongitude());
                    }
                })
                .subscribe(new Subscriber<LatLng>() {
                    @Override
                    public void onCompleted() {
                        currentAnalysis = AnalysisUtil.generateAnalysisFromEvent(event, spUtil.getPosition(), centreCount, insideCount - centreCount,
                                fontCount, insideCount - fontCount - backCount, backCount);
                        setField();
                        setAnalysis();
                        event.save();

                        if (listener != null) {
                            listener.onSaveFinished();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(LatLng latLng) {
                        checkPosition(latLng);
                    }
                });
    }


    private void checkPosition(LatLng data) {
        if (polygonUtils.isInsidePolygon(data, currentBounds)) {
            insideCount++;
            if (polygonUtils.isInsidePolygon(data, centreBound)) {
                centreCount++;
            }
            if (polygonUtils.isInsidePolygon(data, fontBound)) {
                fontCount++;
            }
            if (polygonUtils.isInsidePolygon(data, backBound)) {
                backCount++;
            }
        }
    }

    private void getCentreBound() {
        double dTopLat = .3f * (currentBounds.get(1).latitude - currentBounds.get(0).latitude);
        double dTopLng = .3f * (currentBounds.get(1).longitude - currentBounds.get(0).longitude);
        double dBotLat = .3f * (currentBounds.get(2).latitude - currentBounds.get(3).latitude);
        double dBotLng = .3f * (currentBounds.get(2).longitude - currentBounds.get(3).longitude);

        centreBound = new ArrayList<>();
        centreBound.add(new LatLng(currentBounds.get(0).latitude + dTopLat, currentBounds.get(0).longitude + dTopLng));
        centreBound.add(new LatLng(currentBounds.get(1).latitude - dTopLat, currentBounds.get(1).longitude - dTopLng));
        centreBound.add(new LatLng(currentBounds.get(2).latitude - dBotLat, currentBounds.get(2).longitude - dBotLng));
        centreBound.add(new LatLng(currentBounds.get(3).latitude + dBotLat, currentBounds.get(3).longitude + dBotLng));
    }

    private void getFontBackBound() {
        double dLeftLat = .3f * (currentBounds.get(3).latitude - currentBounds.get(0).latitude);
        double dLeftLng = .3f * (currentBounds.get(3).longitude - currentBounds.get(0).longitude);
        double dRightLat = .3f * (currentBounds.get(2).latitude - currentBounds.get(1).latitude);
        double dRightLng = .3f * (currentBounds.get(2).longitude - currentBounds.get(1).longitude);

        fontBound = new ArrayList<>();
        fontBound.add(currentBounds.get(0));
        fontBound.add(currentBounds.get(1));
        fontBound.add(new LatLng(currentBounds.get(1).latitude + dRightLat, currentBounds.get(1).longitude + dRightLng));
        fontBound.add(new LatLng(currentBounds.get(0).latitude + dLeftLat, currentBounds.get(0).longitude + dLeftLng));

        backBound = new ArrayList<>();
        backBound.add(new LatLng(currentBounds.get(3).latitude - dLeftLat, currentBounds.get(3).longitude - dLeftLng));
        backBound.add(new LatLng(currentBounds.get(2).latitude - dRightLat, currentBounds.get(2).longitude - dRightLng));
        backBound.add(currentBounds.get(2));
        backBound.add(currentBounds.get(3));
    }

    private void showBounds(List<LatLng> bound) {
        markers[0] = mGoogleMap.addMarker(new MarkerOptions().position(bound.get(0)).title("top left").draggable(true));
        markers[1] = mGoogleMap.addMarker(new MarkerOptions().position(bound.get(1)).title("top right").draggable(true));
        markers[2] = mGoogleMap.addMarker(new MarkerOptions().position(bound.get(2)).title("bottom right").draggable(true));

        currentBounds = bound;

        polygonBound = mGoogleMap.addPolygon(new PolygonOptions().addAll(bound).strokeColor(blue50).fillColor(blue50).geodesic(true));
    }

    private List<LatLng> getBounds(LatLngBounds bound) {
        List<LatLng> list = new ArrayList<>();
        //top left
        list.add(new LatLng(bound.northeast.latitude, bound.southwest.longitude));
        //top right
        list.add(bound.northeast);
        //bottom right
        list.add(new LatLng(bound.southwest.latitude, bound.northeast.longitude));
        //bottom left
        list.add(bound.southwest);

        return list;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        calculateBound();
    }

    private void calculateBound() {
        LatLng p1 = markers[0].getPosition();
        LatLng p2 = markers[1].getPosition();
        LatLng p3 = markers[2].getPosition();

        double dLat = p3.latitude - p2.latitude;
        double dLng = p3.longitude - p2.longitude;
        LatLng p4 = new LatLng(p1.latitude + dLat, p1.longitude + dLng);

        currentBounds.set(0, p1);
        currentBounds.set(1, p2);
        currentBounds.set(2, p3);
        currentBounds.set(3, p4);

        polygonBound.setPoints(currentBounds);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
    }
}
