package com.example.expreg.p8_program.SensorHandlers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.example.expreg.p8_program.Constants;
import com.example.expreg.p8_program.Model.AccelerometerMeasure;
import com.example.expreg.p8_program.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyAccelerometerHandler extends MySensorHandler {
    protected static double mAccelerationThreshold;
    protected static double mDecelerationThreshold;
    protected View mView;
    protected List<AccelerometerMeasure> myList = new ArrayList<>();
    protected long mColourTimer = 0;
    protected boolean mAccelerating = false;
    protected String mDetectionType;
    protected Location lastKnownLocation = null;
    private Timer timer = new Timer();
    public static int time = 40;

    //Sensor variables
    protected float[] magnetic = new float[3];
    protected float[] acceleration = new float[3];
    protected float[] gyroRotation = new float[9];
    // Orientation variables
    protected float[] accRotationmatrix = new float[9];
    protected float[] accOrientation = null;
    protected float[] gyroOrientation = new float[3];
    protected float[] fusedOrientation = new float[9];
    protected float alpha = 0.97f;
    protected boolean init = true;

    //Taken from the ANDROID TURTORIAL http://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-gyro
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp = 0;

    protected int test = 1;

    public MyAccelerometerHandler(Context context) {
        this(context, null);
    }

    public MyAccelerometerHandler(Context context, GoogleApiClient client) {
        super(context, Sensor.TYPE_ACCELEROMETER, client);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mAccelerationThreshold = sharedPrefs.getFloat("pref_accelerationThreshold", Constants.ACCELERATION_THRESHOLD_LENIENT);
        mDecelerationThreshold = sharedPrefs.getFloat("pref_decelerationThreshold", Constants.DECELERATION_THRESHOLD_LENIENT);
        mView = ((Activity)mContext).findViewById(R.id.activity_start_trip);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, acceleration, 0, 3);
                if(SensorManager.getRotationMatrix(accRotationmatrix,null,acceleration,magnetic)){
                    SensorManager.getRotationMatrix(accRotationmatrix,null,acceleration,magnetic);
                    accOrientation = new float[3];
                    SensorManager.getOrientation(accRotationmatrix, accOrientation);
                }
                handleAcceleration();
                break;

            case Sensor.TYPE_GYROSCOPE:
                handleGyro(event);
            break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values,0,magnetic,0,3);
            break;

        }
    }

    // Taken FROM THE ANDROID TURTORIAL http://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-gyro
    private void handleGyro(SensorEvent event) {

        if(accOrientation == null)
            return;

        if(timestamp == 0){
            System.arraycopy(accRotationmatrix,0,gyroRotation,0,9);
        }
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;

            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float)Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            float EPSILON = 0.00000001f;
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        gyroRotation = matrixMultiplication(gyroRotation,deltaRotationMatrix);
        SensorManager.getOrientation(gyroRotation, gyroOrientation);
    }

    private float[] matrixMultiplication(float[] A, float[] B ) {
        float[] result = new float[9];
        for(int i = 0; i < 9; i++){
            result[i] = A[(i/3) * 3]*B[(i % 3)] + A[((i/3) * 3) + 1]*B[(i % 3) + 3] + A[((i/3) * 3) + 2]*B[(i % 3) + 6];
        }
        return result;
    }

    // TAKEN FROM http://plaw.info/2012/03/android-sensor-fusion-tutorial/2/
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private void handleAcceleration() {
        if (isHardAcceleration()) {
            if (!mAccelerating)  {
                mAccelerating = true;
                mDetectionType = detectionType();
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    mDb.addDetection(mTrip, lastKnownLocation, mDetectionType + " Start", new Date().toString());
                }
            }
        } else if (mAccelerating) {
            mAccelerating = false;
            mColourTimer = System.nanoTime();
            mView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorHardAcceleration));
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mDb.addDetection(mTrip, lastKnownLocation, mDetectionType + " End", new Date().toString());
            }
        }

        if (System.nanoTime() - mColourTimer > Constants.TIME_AS_RED) {
            mColourTimer = 0;
            mView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorNoAcceleration));
        }
    }

    private boolean isHardAcceleration() {
        float diffy = 0;
        if (mCircularQueue.isAtFullCapacity())
            diffy = Math.abs(mCircularQueue.getMax().getAcc_y() - mCircularQueue.getMin().getAcc_y());
        if (detectionType().equals("Acceleration")) {
            return diffy > mAccelerationThreshold;
        }
        else {
            return diffy > mDecelerationThreshold;
        }
    }

    // TODO: Find better way to detect if acceleration or deceleration.
    private String detectionType() {
        if (mCircularQueue.getMax().getAcc_y() > 0 && mCircularQueue.getMin().getAcc_y() > 0) {
            return "Acceleration";
        }
        else {
            return "Deceleration";
        }
    }

    public void start(int frequency) {
        super.start(frequency);
        this.myList.clear();
        this.mAccelerating = false;
        timer = new Timer();
        timer.scheduleAtFixedRate(new SensorFusion(),1000,time);
    }

    @Override
    public void stop() {
        super.stop();
        timer.cancel();
        timer.purge();
        mView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorNoAcceleration));
        init = true;
    }

    class SensorFusion extends TimerTask {
        public void run() {
            float oneMinusAlpha = 1f - alpha;
            if(init) {
                fusedOrientation[0] = accOrientation[0];
                fusedOrientation[1] = accOrientation[1];
                fusedOrientation[2] = accOrientation[2];
                gyroRotation = getRotationMatrixFromOrientation(fusedOrientation);
                System.arraycopy(fusedOrientation,0,gyroOrientation,0,3);
                init = false;
                return;
            }

            fusedOrientation[0] = alpha * gyroOrientation[0] + oneMinusAlpha * accOrientation[0];
            fusedOrientation[1] = alpha * gyroOrientation[1] + oneMinusAlpha * accOrientation[1];
            fusedOrientation[2] = alpha * gyroOrientation[2] + oneMinusAlpha * accOrientation[2];

            gyroRotation = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
            Log.d("SensorChanged", "Sensor has changed");

            // TODO Get calibrated average with the following commented line. Use key "pref_accVar" for the variance.
            // PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("pref_accAvg", "someDefaultValue");
            AccelerometerMeasure result = new AccelerometerMeasure(mTrip, acceleration[1],acceleration[1] - (gyroRotation[7] * 10.12889f), acceleration[1]);
            mCircularQueue.add(result);
            myList.add(result);

            if (myList.size() >= 10) {
                mDb.addMeasures(myList);
                myList.clear();
            }
        }
    }
}