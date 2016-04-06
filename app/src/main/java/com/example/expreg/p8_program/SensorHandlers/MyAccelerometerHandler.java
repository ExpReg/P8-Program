package com.example.expreg.p8_program.SensorHandlers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;
import android.widget.TextView;

import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.example.expreg.p8_program.Model.AccelerometerMeasure;
import com.example.expreg.p8_program.Model.SensorMeasure;

public class MyAccelerometerHandler extends MySensorHandler {
    protected double mCutoffAccel = 0.1 * 9.82;
    protected double mCutoffBrake = -0.1 * 9.82;

    public MyAccelerometerHandler(TextView view, Context context, MySQLiteHelper db) {
        super(view, Sensor.TYPE_ACCELEROMETER, context, db);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        AccelerometerMeasure result = new AccelerometerMeasure(mTrip, event.values[0], event.values[1], event.values[2]);
        mOutput = lowPassFilter(event.values.clone(), mOutput);
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
        String strx = "Accelerometer x-axis: " + event.values[0] + "\n";
        String stry = "Accelerometer y-axis: " + event.values[1] + "\n";
        String strz = "Accelerometer z-axis: " + event.values[2];
        mSensorTextView.setText(strx + stry + strz);
    }
}