package com.example.expreg.p8_program.SensorHandlers;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.example.expreg.p8_program.Model.AccelerometerMeasure;
import com.example.expreg.p8_program.R;

import java.util.ArrayList;
import java.util.List;

public class MyAccelerometerHandler extends MySensorHandler {
    protected static double mCutoffAccel = 0.1 * 9.82;
    protected static double mCutoffBrake = -0.1 * 9.82;
    protected TextView mSensorTextView = null;
    protected SurfaceView mColorBox = null;
    protected List<AccelerometerMeasure> myList = new ArrayList<>();

    public MyAccelerometerHandler(Context context, MySQLiteHelper db) {
        super(context, db, Sensor.TYPE_ACCELEROMETER);
        mSensorTextView = (TextView) ((Activity)context).findViewById(R.id.accelerometer_text);
        mColorBox = (SurfaceView) ((Activity)context).findViewById(R.id.color_box);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("SensorChanged", "Sensor has changed");
        AccelerometerMeasure result = new AccelerometerMeasure(mTrip, event.values[0], event.values[1], event.values[2]);
        mCalibrationManager.add(result);
        myList.add(result);

        if (myList.size() >= 200 && !this.mCalibrate) {
            mDb.addMeasures(myList);
            myList.clear();
        }

        if (this.hardAcceleration()) {
            mColorBox.setBackgroundColor(0xFFFF0000);
        }
        else {
            mColorBox.setBackgroundColor(0xFF00FF00);
        }

        String strx = "Accelerometer x-axis: " + event.values[0] + "\n";
        String stry = "Accelerometer y-axis: " + event.values[1] + "\n";
        String strz = "Accelerometer z-axis: " + event.values[2];
        mSensorTextView.setText(strx + stry + strz);
    }

    private boolean hardAcceleration() {
        float avgy = 0;
        if (mCalibrationManager.size() >= 200)
            avgy = mCalibrationManager.calcAverage().getAcc_y();

        return (avgy > mCutoffAccel || avgy < mCutoffBrake);
    }
}