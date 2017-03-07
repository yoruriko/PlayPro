package com.ricogao.playpro.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.ricogao.playpro.R;
import com.ricogao.playpro.util.PermissionUtil;
import com.ricogao.playpro.util.SensorProcessUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


@SuppressWarnings("MissingPermission")
public class RecordActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = RecordActivity.class.getSimpleName();
    private final static int MY_PERMISSION_REQUEST_LOCATION = 99;
    private final String REQUESTING_LOCATION_UPDATES_KEY = "requestLocationUpdateKey";
    private final String[] LOCATION_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final static double SPEED_LIMIT = 12.52;

    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private boolean isUpdating;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorProcessUnit spu;

    @BindView(R.id.tv_reading)
    TextView tvReading;

    @OnClick(R.id.btn)
    public void clickBtn() {
        initSensorListeners();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);
        initFragment();


        buildGoogleApiClient();
        buildLocationRequest();
        mGoogleApiClient.connect();

        if (savedInstanceState != null) {
            updateValuesFromBundle(savedInstanceState);
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, isUpdating);
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            isUpdating = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
        }
    }

    private void initFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initSensorListeners() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        spu = new SensorProcessUnit();

        if (mSensor != null) {
            mSensorManager.registerListener(spu, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            spu.setUpdateRecordListener(new SensorProcessUnit.OnUpdateRecordListener() {
                @Override
                public void onUpdateRecord(float[] value) {
                    tvReading.setText("Gx:" + value[0] + "\nGy:" + value[1] + "\nGz:" + value[2]);
                }
            });
        }

    }

    private void removeSensorListeners() {
        if (mSensor != null && spu != null) {
            mSensorManager.unregisterListener(spu);
            spu = null;
            mSensor = null;
        }
    }

    @Override
    protected void onResume() {
        if (mGoogleApiClient.isConnected() && isUpdating) {
            startLocationUpdate();
        }
        super.onResume();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        checkLocationPermission();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    protected synchronized void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void checkLocationPermission() {
        if (PermissionUtil.checkPermission(this, LOCATION_PERMISSIONS)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This App needs the location permission,Please Accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermission();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                RecordActivity.this.finish();
                            }
                        })
                        .create()
                        .show();
            } else {
                requestPermission();
            }
        } else {
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(RecordActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSION_REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST_LOCATION && PermissionUtil.verifyPermission(grantResults)) {
            if (mGoogleMap != null && PermissionUtil.checkPermission(this, LOCATION_PERMISSIONS)) {
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation == null) {
            Toast.makeText(this, "mLastLocation is null", Toast.LENGTH_SHORT).show();
        } else {
            LatLng mLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 20));
        }
    }

    protected void startLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended with " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection Failed with " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLastLocation == null) {
            mLastLocation = location;
            return;
        }

        checkLocationReading(mLastLocation, location);


    }

    private boolean checkLocationReading(Location lastLocation, Location currLocation) {

        //measure in meters
        float dD = lastLocation.distanceTo(currLocation);
        //measure in ms
        long dT = currLocation.getTime() - lastLocation.getTime();

        if (dT <= 0) {
            //drop readings with same timestamp due to signal errors
            Log.e(TAG, "Error reading with same timestamp");
            return false;
        }

        //measure in m/s
        float dV = currLocation.hasSpeed() ? currLocation.getSpeed() : (dD / (float) (dT * 1000));
        if (dV > SPEED_LIMIT) {
            //drop reading with very large speed
            Log.e(TAG, "Error reading with too large speed");
            return false;
        }

        Log.i(TAG, "Valid Reading: dD:" + dD + ",dT:" + dT + ",dV:" + dV);


        return true;
    }
}
