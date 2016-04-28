package com.example.expreg.p8_program.SensorHandlers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.expreg.p8_program.DB.MySQLiteHelper;

public abstract class MySensorHandler implements SensorEventListener {
    protected SensorManager mSensorManager = null;
    protected MyCircularQueue mCalibrationManager = null;
    protected Sensor mSensor = null;
    protected MySQLiteHelper mDb = null;
    protected Context mContext = null;
    protected float[] mOutput;
    protected int mTrip = 0;
    protected int mFrequency = 0;
    protected boolean mCalibrate = false;

    public MySensorHandler(Context context, int sensorType) {
        this(context, null, sensorType);
    }

    public MySensorHandler(Context context, MySQLiteHelper db, int sensorType) {
        mContext = context;
        mDb = db;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mCalibrationManager = new MyCircularQueue(mContext);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
    }

    public void start(int frequency) {
        this.start(frequency, false);
    }

    public void start(int frequency, boolean calibrate) {
        this.mCalibrate = calibrate;
        this.mFrequency = frequency;
        if (mDb != null) {
            mTrip = mDb.getLastTrip() + 1;
        }
        mSensorManager.registerListener(this, mSensor, 1000000 / frequency);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
        if (this.mCalibrate == true) {
            mCalibrationManager.save();
        }
        this.mCalibrate = false;
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