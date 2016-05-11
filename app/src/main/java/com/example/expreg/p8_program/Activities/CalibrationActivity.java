package com.example.expreg.p8_program.Activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.expreg.p8_program.Model.AccelerometerMeasure;
import com.example.expreg.p8_program.SensorHandlers.MyCircularQueue;

public class CalibrationActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private MyCircularQueue mQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mQueue = new MyCircularQueue(1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int frequency = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_sensorFrequency", "20"));
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 1000000 / frequency);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putFloat("pref_accAvg", mQueue.getAverage().getAcc_y())
                .putFloat("pref_accVar", mQueue.getVariance().getAcc_y())
                .apply();
        Toast.makeText(this, "Calibrated!", Toast.LENGTH_LONG).show();
    }

    public void onSensorChanged(SensorEvent event) {
        AccelerometerMeasure measure = new AccelerometerMeasure(0, event.values[0], event.values[1], event.values[2]);
        mQueue.add(measure);
        if (mQueue.isAtFullCapacity()) {
            finish();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }
}