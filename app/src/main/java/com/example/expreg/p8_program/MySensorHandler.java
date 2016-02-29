package com.example.expreg.p8_program;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class MySensorHandler implements SensorEventListener{
    protected TextView mSensorTextView = null;
    protected SensorManager mSensorManager = null;
    protected Sensor mSensor = null;

    public MySensorHandler(TextView view, int sensorType, Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        mSensorTextView = view;
    }

    public void start() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO find way to get sensor name
        String str = "Sensor: " + event.values[0];
        mSensorTextView.setText(str);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something if accuracy changes
    }
}
