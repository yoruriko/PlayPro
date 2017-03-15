package com.ricogao.playpro.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * Created by ricogao on 2017/3/1.
 */

public class SensorProcessUnit implements SensorEventListener {

    private static final String TAG = SensorProcessUnit.class.getSimpleName();
    private OnUpdateRecordListener mListener;

    public interface OnUpdateRecordListener {
        void onUpdateRecord(float[] value);
    }

    public void setUpdateRecordListener(OnUpdateRecordListener listener) {
        mListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mListener.onUpdateRecord(sensorEvent.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(TAG, sensor.getName() + " accuracy changed: " + i);
    }
}
