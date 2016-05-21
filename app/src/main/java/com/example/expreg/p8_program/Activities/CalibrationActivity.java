package com.example.expreg.p8_program.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
        mQueue = new MyCircularQueue(200);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new AlertDialog.Builder(this)
                .setTitle("Calibrating").setMessage("Calibrating the sensors. Put the phone flat on a surface with the screen up and wait two seconds for a Calibrated! message to show.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mSensorManager.registerListener(CalibrationActivity.this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
                    }}).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putFloat("pref_accAvg", mQueue.getAverage().getAcc_y())
                .putFloat("pref_accVar", mQueue.getVariance().getAcc_y())
                .apply();
        Toast.makeText(this, "Calibrated!", Toast.LENGTH_LONG).show();
    }

    public void onSensorChanged(SensorEvent event) {
        AccelerometerMeasure measure = new AccelerometerMeasure(0, event.values[0], event.values[1], event.values[2],0f,0f,0f);
        mQueue.add(measure);
        if (mQueue.isAtFullCapacity()) {
            finish();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }
}