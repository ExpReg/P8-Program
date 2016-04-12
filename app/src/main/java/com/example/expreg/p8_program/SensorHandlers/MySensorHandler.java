package com.example.expreg.p8_program.SensorHandlers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import com.example.expreg.p8_program.DB.MySQLiteHelper;

public abstract class MySensorHandler implements SensorEventListener {
    protected TextView mSensorTextView = null;
    protected SensorManager mSensorManager = null;
    protected Sensor mSensor = null;
    protected MySQLiteHelper mDb;
    protected Context mContext;
    protected int mTrip = 0;
    protected float[] mOutput;
    protected int frequency = 0;
    protected boolean calibrate = false;
    protected MyCalibrationManager mCalibrationManager = null;


    public MySensorHandler(TextView view, int sensorType, Context context) {
        this(view, sensorType, context, null);
    }

    public MySensorHandler(TextView view, int sensorType, Context context, MySQLiteHelper db) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mContext = context;
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        mSensorTextView = view;
        mDb = db;
    }

    public void start(int frequency) {
        this.start(frequency, false);
    }

    public void start(int frequency, boolean calibrate) {
        this.calibrate = calibrate;
        if (calibrate == true) {
            mCalibrationManager = new MyCalibrationManager(mContext);
        }
        this.frequency = frequency;
        if (mDb != null) {
            mTrip = mDb.getLastTrip() + 1;
        }
        mSensorManager.registerListener(this, mSensor, 1000000 / frequency);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
        if (this.calibrate == true) {
            mCalibrationManager.save();
        }
        this.calibrate = false;
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