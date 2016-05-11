package com.example.expreg.p8_program.SensorHandlers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.google.android.gms.common.api.GoogleApiClient;

public abstract class MySensorHandler implements SensorEventListener {
    protected SensorManager mSensorManager = null;
    protected GoogleApiClient mGoogleApiClient = null;
    protected MyCircularQueue mCircularQueue = null;
    protected Sensor mSensor = null;
    protected MySQLiteHelper mDb = null;
    protected Context mContext = null;
    protected float[] mOutput;
    protected int mTrip = 0;

    public MySensorHandler(Context context, int sensorType) {
        this(context, sensorType, null);
    }

    public MySensorHandler(Context context, int sensorType, GoogleApiClient client) {
        mContext = context;
        mDb = MySQLiteHelper.getInstance(context);
        mCircularQueue = new MyCircularQueue(25);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        mGoogleApiClient = client;
    }

    public void start(int frequency) {
        if (mDb != null) {
            mTrip = mDb.getLastTrip() + 1;
        }

        mSensorManager.registerListener(this, mSensor, 1000000 / frequency);
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),1000000 / frequency );
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),1000000 / frequency );
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something if accuracy changes
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