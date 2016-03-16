package com.example.expreg.p8_program;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.example.expreg.p8_program.Model.SensorMeasure;

public class MySensorHandler implements SensorEventListener{
    protected TextView mSensorTextView = null;
    protected SensorManager mSensorManager = null;
    protected Sensor mSensor = null;
    protected MySQLiteHelper mDb;
    protected int mTrip = 0;

    protected double mCutoffAccel = 0.1 * 9.82;
    protected double mCutoffBrake = 0.1 * 9.82;

    protected float[] output;

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

    public void start() {
        mTrip = mDb.getLastTrip() + 1;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorMeasure result = new SensorMeasure(mTrip, event.values[0], event.values[1], event.values[2]);
        output = lowPassFilter(event.values.clone(), output);
        Log.d("SensorChanged", "Sensor has changed");
        if (this.mDb != null) {
            mDb.addMeasure(result);
            if (result.getAcc_y() > mCutoffAccel) {
                mDb.addDetection(result, "Acceleration");
            }
            else if (result.getAcc_y() < mCutoffBrake) {
                mDb.addDetection(result, "Brake");
            }
        }
        else {
            Log.d("NoDB", "Database is null");
        }

        String str = mSensor.getName() + ": " + event.values[0];
        mSensorTextView.setText(str);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something if accuracy changes
    }

    public String getDBLocation(){
        return "/data/com.example.expreg.p8_program/databases/" + mDb.getDatabaseName();

    }

    private float[] lowPassFilter(float[] input, float[] output) {
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
