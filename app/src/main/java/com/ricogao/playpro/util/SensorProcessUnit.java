package com.ricogao.playpro.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by ricogao on 2017/3/1.
 * Processing Unit for handling sensor events, recording steps and state of movement.
 */

public class SensorProcessUnit implements SensorEventListener {

    private static final String TAG = SensorProcessUnit.class.getSimpleName();

    //raw reading filter window size
    private final static int WINDOW_SIZE = 5;

    //range of the peak acceleration,unit ms^-2
    private final static float MIN_PEAK_VALUE = SensorManager.STANDARD_GRAVITY * 1.2f;
    private final static float MAX_PEAK_VALUE = SensorManager.STANDARD_GRAVITY * 2.5f;

    //range of the time threshold of a step, unit ms
    private final static long MIN_STEP_THRESHOLD = 200;
    private final static long MAX_STEP_THRESHOLD = 2000;

    //store raw readings within window
    private int filterCount = 0;
    private float filterReadings[] = new float[3];

    //store lastReading readings
    private float lastReading = -1;

    //flags to record last direction for peak detection
    private boolean lastDirectionUp = false;

    //counter for peak detection
    private int upCount;
    private int lastUpCount;

    //record peak and valley in the wave
    private float wavePeak;
    private float waveValley;

    //record last peak time
    private long lastPeakTime;


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //avoid concurrency issues
        synchronized (this) {
            //only take reading from the Accelerometer
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                filterReading(sensorEvent);
            }
        }
    }

    /**
     * Filter Raw event with a given window size to cancel noises and avoid too much calculation
     *
     * @param event Raw sensor event
     */
    synchronized private void filterReading(SensorEvent event) {

        filterReadings[0] += event.values[0];
        filterReadings[1] += event.values[1];
        filterReadings[2] += event.values[2];

        filterCount++;

        if (filterCount == WINDOW_SIZE) {
            //take average of magnitude with window size, cancel out noise in sensor's reading
            double accX = Math.pow(filterReadings[0] / WINDOW_SIZE, 2);
            double accY = Math.pow(filterReadings[1] / WINDOW_SIZE, 2);
            double accZ = Math.pow(filterReadings[2] / WINDOW_SIZE, 2);

            //depending on how the phone is positioned,reading on each direction will behave differently
            //uses the average magnitude to cancel out the offset on one particular axi and therefore
            //avoid calculating the direction
            float accAvg = (float) Math.sqrt(accX + accY + accZ);
            detectStep(accAvg);

            //reset fields
            Arrays.fill(filterReadings, 0);
            filterCount = 0;
        }


    }

    private void detectStep(float reading) {
        //first reading
        if (lastReading == -1) {
            //update lastReading and exit;
            lastReading = reading;
            return;
        }

        if (detectPeak(lastReading, reading)) {
            //update current time
            long currPeakTime = System.currentTimeMillis();
            if (isTimeValid(lastPeakTime, currPeakTime)) {

            }

            lastPeakTime = currPeakTime;
        }


        lastReading = reading;
    }

    private boolean isTimeValid(long lastTime, long currentTime) {
        long dT = currentTime - lastTime;
        return MIN_STEP_THRESHOLD < dT && dT <= MAX_STEP_THRESHOLD;
    }

    /**
     * Detect Peak in readings
     *
     * @param oldReading last valid reading
     * @param newReading current reading
     * @return is current reading on the peak of wave
     */
    private boolean detectPeak(float oldReading, float newReading) {

        //detect direction of current reading
        boolean isDirectionUp = (newReading >= oldReading);

        //update up counts
        if (isDirectionUp) {
            upCount++;
        } else {
            lastUpCount = upCount;
            upCount = 0;
        }

        /*
        Condition for a Valid Peak:
        1. current direction is down
        2. last direction is up
        3. perilous reading have more than 2 continue up
        4. the peak is within the range we defined

        Condition for a Valid Valley:
        1. last direction is down
        2. current direction is up
        */
        if (!isDirectionUp && lastDirectionUp && lastUpCount >= 2
                && (MIN_PEAK_VALUE <= oldReading && oldReading < MAX_PEAK_VALUE)) {
            wavePeak = oldReading;
            return true;
        } else if (!lastDirectionUp && isDirectionUp) {
            waveValley = oldReading;
        }

        lastDirectionUp = isDirectionUp;

        return false;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(TAG, "Accuracy Change of Sensor " + sensor.getName() + " to " + i);
    }
}
