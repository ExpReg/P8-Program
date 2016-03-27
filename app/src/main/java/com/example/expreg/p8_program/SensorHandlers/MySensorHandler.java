package com.example.expreg.p8_program.SensorHandlers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import com.example.expreg.p8_program.DB.MySQLiteHelper;

public abstract class MySensorHandler implements SensorEventListener{
    protected TextView mSensorTextView = null;
    protected SensorManager mSensorManager = null;
    protected Sensor mSensor = null;
    protected MySQLiteHelper mDb;
    protected int mTrip = 0;
    protected float[] mOutput;

    public MySensorHandler(TextView view, int sensorType, Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        mSensorTextView = view;
    }

    public MySensorHandler(TextView view, int sensorType, Context context, MySQLiteHelper db) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        mSensorTextView = view;
        mDb = db;
    }

    public void start(int frequency) {
        mTrip = mDb.getLastTrip() + 1;
        mSensorManager.registerListener(this, mSensor, 1000000 / frequency);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something if accuracy changes
    }

    public String getDBLocation(){
        return "/data/com.example.expreg.p8_program/databases/" + mDb.getDatabaseName();
    }

    protected float[] lowPassFilter(float[] input, float[] output) {
        float ALPHA = 0.25f; //ALPHA is the cut-off/threshold.
        if(output == null ) {
            return input;
        }
        for(int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }

        return output;
    }
}