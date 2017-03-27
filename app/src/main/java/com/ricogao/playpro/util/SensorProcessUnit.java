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

    //filter reading sample size
    private final static int SAMPLE_SIZE = 50;

    //range of the time threshold of a step, unit ms
    private final static long MIN_STEP_THRESHOLD = 200;
    private final static long MAX_STEP_THRESHOLD = 2000;

    //store raw readings within window
    private int filterCount = 0;
    private float filterReadings[] = new float[3];


    //flags to record last direction for peak detection
    private boolean lastDirectionUp = false;

    //counter for peak detection
    private int upCount;
    private int lastUpCount;

    //record peak and valley in the wave
    private float samplePeak;
    private float sampleValley;

    //record last peak time
    private long lastPeakTime = -1;

    //for change in Acceleration calculating
    private float lastReading = -1;

    //store sample old
    private float oldSample = -1;

    //record reading counts
    private int sampleCount = 0;

    //store samples for dynamic threshold and precision
    private float sampleReadings[] = new float[50];

    //for sample precision calculation
    private float lastPeak;
    private float peakToPeakSum;
    private int peakCount;

    private float samplePrecision = SensorManager.STANDARD_GRAVITY * 0.2f;
    private float sampleThreshold = SensorManager.STANDARD_GRAVITY * 1.2f;

    private OnStepListener listener;

    public interface OnStepListener {
        void onStep();
    }

    public void setStepListener(OnStepListener listener) {
        this.listener = listener;
    }

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
            processReading();
        }

    }

    /**
     * Smooth readings, minimise error caused by noise
     */
    private void processReading() {
        //take average of magnitude with window size, cancel out noise in sensor's reading
        double accX = Math.pow(filterReadings[0] * (1.0f / WINDOW_SIZE), 2);
        double accY = Math.pow(filterReadings[1] * (1.0f / WINDOW_SIZE), 2);
        double accZ = Math.pow(filterReadings[2] * (1.0f / WINDOW_SIZE), 2);

        //depending on how the phone is positioned,reading on each direction will behave differently
        //uses the average magnitude to cancel out the offset on one particular axi and therefore
        //avoid calculating the direction
        float accAvg = (float) Math.sqrt(accX + accY + accZ);
        updateSamples(accAvg);

        clearReadingUtils();
    }

    private void clearReadingUtils() {
        //reset fields
        Arrays.fill(filterReadings, 0);
        filterCount = 0;
    }


    private void updateSamples(float reading) {

        sampleReadings[sampleCount] = reading;
        sampleCount++;

        //first reading
        if (lastReading == -1) {
            lastReading = reading;
            oldSample = reading;
            return;
        }

        //change in Acceleration
        float dA = Math.abs(lastReading - reading);
        lastReading = reading;

        if (dA > samplePrecision) {
            //update new sample if condition holds
            long sampleTime = System.currentTimeMillis();

            if (detectPeak(oldSample, reading) && isTimeValid(lastPeakTime, sampleTime)) {
                if (listener != null) {
                    listener.onStep();
                }
            }
            //old update sample old when change in Acc lager than precision
            oldSample = reading;
        }

        if (sampleCount == SAMPLE_SIZE) {
            processSample();
        }
    }

    /**
     * update precision and threshold dynamically
     */
    private void processSample() {

        //Dynamic Threshold = (Max-Min)/2 in the last 50 samples
        sampleThreshold = (samplePeak - sampleValley) * 0.5f;

        //Dynamic Precision = (Change in peak to peak)/ num of peaks
        if (peakCount > 0) {//avoid divide by 0
            samplePrecision = peakToPeakSum * (1.0f / peakCount);
        }
        clearSampleUtils();

    }

    private void clearSampleUtils() {
        //clear sample readings and counters
        Arrays.fill(sampleReadings, 0);

        samplePeak = sampleThreshold;
        sampleValley = sampleThreshold;

        sampleCount = 0;
        peakToPeakSum = 0;
        peakCount = 0;
    }


    private boolean isTimeValid(long lastTime, long currentTime) {

        long dT = currentTime - lastTime;
        //update last peak time
        lastPeakTime = currentTime;

        return lastTime == -1 || (MIN_STEP_THRESHOLD < dT && dT <= MAX_STEP_THRESHOLD);
    }

    /**
     * Detect Peak in readings
     *
     * @param oldSample last Valid sample
     * @param newSample current Valid sample
     * @return is current reading on the peak of wave
     */
    private boolean detectPeak(float oldSample, float newSample) {


        //detect direction of current reading
        boolean isDirectionUp = (newSample >= oldSample);

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
        4. the peak is above the dynamic threshold

        Condition for a Valid Valley:
        1. last direction is down
        2. current direction is up
        3. the valley is below the dynamic threshold
        */

        if (!isDirectionUp && lastDirectionUp && lastUpCount >= 2 && oldSample > sampleThreshold) {
            updatePeak(oldSample);

            return true;
        } else if (!lastDirectionUp && isDirectionUp && oldSample < sampleThreshold) {
            updateValley(oldSample);
            return false;
        }

        lastDirectionUp = isDirectionUp;

        return false;
    }


    private void updatePeak(float sample) {

        if (peakCount > 0) {
            //change in peak value
            float dPeak = Math.abs(lastPeak - sample);
            if (samplePeak < sample) {
                samplePeak = sample;
            }
            peakToPeakSum += dPeak;
        }

        peakCount++;
        lastPeak = sample;

    }

    private void updateValley(float sample) {
        if (sampleValley > sample) {
            sampleValley = sample;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(TAG, "Accuracy Change of Sensor " + sensor.getName() + " to " + i);
    }
}
