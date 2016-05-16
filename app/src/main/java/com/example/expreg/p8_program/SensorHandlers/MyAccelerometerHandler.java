package com.example.expreg.p8_program.SensorHandlers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.example.expreg.p8_program.Model.AccelerometerMeasure;
import com.example.expreg.p8_program.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyAccelerometerHandler extends MySensorHandler {
    protected static double mCutoffAccel = 0.1 * 9.82;
    protected static double mCutoffBrake = -0.1 * 9.82;
    protected TextView mSensorTextView = null;
    protected SurfaceView mColorBox = null;
    protected List<AccelerometerMeasure> myList = new ArrayList<>();
    protected long colourTimer = 0;
    protected long redTime = 5000000000L;
    protected boolean accelerating = false;
    protected Location lastKnownLocation = null;
    private Timer timer = new Timer();
    public static int time = 40;
    float[] alphas = new float[]{0.95f,0.96f,0.97f,0.98f,0.99f};
    List<float[]> fusedOrientations = new ArrayList<>();
    List<float[]> gyroRotations = new ArrayList<>();
    List<float[]> gyroOrientations = new ArrayList<>();

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

    protected String strx = "";
    protected String stry = "";
    protected String strz = "";
    protected  int test = 1;

    public MyAccelerometerHandler(Context context, MySQLiteHelper db) {
        this(context, db, null);
    }

    public MyAccelerometerHandler(Context context, MySQLiteHelper db, GoogleApiClient client) {
        super(context, db, Sensor.TYPE_ACCELEROMETER, client);
        mSensorTextView = (TextView) ((Activity)context).findViewById(R.id.accelerometer_text);
        mColorBox = (SurfaceView) ((Activity)context).findViewById(R.id.color_box);
        initer();
    }

    private void initer(){
    for(int i = 0; i < 5; i++){
        gyroRotations.add(new float[9]);
        gyroOrientations.add(new float[3]);
        fusedOrientations.add(new float[3]);
    }
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
                mSensorTextView.setText(strx + stry + strz);
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
    private void handleGyro(SensorEvent event){

        if(accOrientation == null)
            return;

        if(timestamp == 0){
            for(int i = 0; i < 5;i++) {
                System.arraycopy(accRotationmatrix, 0, gyroRotations.get(i), 0, 9);
            }
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
        for(int i = 0 ; i < 5 ;i++){
            gyroRotations.set(i,matrixMultiplication(gyroRotations.get(i),deltaRotationMatrix));
            SensorManager.getOrientation(gyroRotations.get(i),gyroOrientations.get(i));
        }
    }

    private float[] matrixMultiplication(float[] A, float[] B ){

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

/*
    private boolean hardAcceleration() {
        float diffy = 0;
        if (mCalibrationManager.size() >= mFrequency)
            diffy = mCalibrationManager.getMax().getAcc_y() - mCalibrationManager.getMin().getAcc_y();

        return (diffy > mCutoffAccel || diffy < mCutoffBrake);
    }
    */

    public void start(int frequency){
        super.start(frequency);
        this.myList.clear();
        timer = new Timer();
        timer.scheduleAtFixedRate(new SensorFusion(),1000,time);
    }

    @Override
    public void stop() {
        super.stop();
        timer.cancel();
        timer.purge();
        mColorBox.setBackgroundColor(0xFF00FF00);
        init = true;
    }

    class SensorFusion extends TimerTask{
        public void run(){


            if(init){
                for(int i = 0; i < 5; i++){
                    fusedOrientations.set(i,new float[]{accOrientation[0],accOrientation[1],accOrientation[2]});
                    gyroRotations.set(i,getRotationMatrixFromOrientation(fusedOrientations.get(i)));
                    System.arraycopy(fusedOrientations.get(i), 0, gyroOrientations.get(i),0, 3);
                }
                init = false;
                return;
            }

            for(int i = 0; i < 5; i++ ){
                float oneMinusAlpha = 1f - alphas[i];

                if (gyroOrientations.get(i)[0] < -0.5 * Math.PI && accOrientation[0] > 0.0) {
                    fusedOrientations.get(i)[0]= (float) (alphas[i] * (gyroOrientations.get(i)[0]+ 2.0 * Math.PI) + oneMinusAlpha * accOrientation[0]);
                    fusedOrientations.get(i)[0] -= (fusedOrientations.get(i)[0]> Math.PI) ? 2.0 * Math.PI : 0;
                }
                else if (accOrientation[0] < -0.5 * Math.PI && gyroOrientations.get(i)[0]> 0.0) {
                    fusedOrientations.get(i)[0]= (float) (alphas[i] * gyroOrientations.get(i)[0] + oneMinusAlpha * (accOrientation[0] + 2.0 * Math.PI));
                    fusedOrientations.get(i)[0] -= (fusedOrientations.get(i)[0] > Math.PI)? 2.0 * Math.PI : 0;
                }
                else {
                    fusedOrientations.get(i)[0] = alphas[i] * gyroOrientations.get(i)[0]+ oneMinusAlpha * accOrientation[0];
                }

                if (gyroOrientations.get(i)[1] < -0.5 * Math.PI && accOrientation[1] > 0.0) {
                    fusedOrientations.get(i)[1] = (float) (alphas[i] * (gyroOrientations.get(i)[1] + 2.0 * Math.PI) + oneMinusAlpha * accOrientation[1]);
                    fusedOrientations.get(i)[1] -= (fusedOrientations.get(i)[1] > Math.PI) ? 2.0 * Math.PI : 0;
                }
                else if (accOrientation[1] < -0.5 * Math.PI && gyroOrientations.get(i)[1] > 0.0) {
                    fusedOrientations.get(i)[1] = (float) (alphas[i] * gyroOrientations.get(i)[1] + oneMinusAlpha * (accOrientation[1] + 2.0 * Math.PI));
                    fusedOrientations.get(i)[1] -= (fusedOrientations.get(i)[1] > Math.PI)? 2.0 * Math.PI : 0;
                }
                else {
                    fusedOrientations.get(i)[1] = alphas[i] * gyroOrientations.get(i)[1] + oneMinusAlpha * accOrientation[1];
                }

                if ( gyroOrientations.get(i)[2]< -0.5 * Math.PI && accOrientation[2] > 0.0) {
                    fusedOrientations.get(i)[2] = (float) (alphas[i] * ( gyroOrientations.get(i)[2] + 2.0 * Math.PI) + oneMinusAlpha * accOrientation[2]);
                    fusedOrientations.get(i)[2] -= (fusedOrientations.get(i)[2] > Math.PI) ? 2.0 * Math.PI : 0;
                }
                else if (accOrientation[2] < -0.5 * Math.PI &&  gyroOrientations.get(i)[2] > 0.0) {
                    fusedOrientations.get(i)[2]= (float) (alphas[i] *  gyroOrientations.get(i)[2] + oneMinusAlpha * (accOrientation[2] + 2.0 * Math.PI));
                    fusedOrientations.get(i)[2] -= (fusedOrientations.get(i)[2] > Math.PI)? 2.0 * Math.PI : 0;
                }
                else {
                    fusedOrientations.get(i)[2]= alphas[i] *  gyroOrientations.get(i)[2]+ oneMinusAlpha * accOrientation[2];
                }

                gyroRotations.set(i,getRotationMatrixFromOrientation(fusedOrientations.get(i)));
                System.arraycopy(fusedOrientations.get(i), 0, gyroOrientations.get(i), 0, 3);
            }

            float[] values = new float[5];
            for(int i  = 0; i < 5; i++){
                values[i] = acceleration[1] - ( gyroRotations.get(i)[7] * 10f);
            }

            Log.d("SensorChanged", "Sensor has changed");

            AccelerometerMeasure result = new AccelerometerMeasure(mTrip, acceleration[1],values[0],values[1],values[2],values[3],values[4]);
            //mCalibrationManager.add(result);
            myList.add(result);

            if (myList.size() >= 10 && !mCalibrate) {
                mDb.addMeasures(myList);
                myList.clear();
            }
/*
            if (hardAcceleration()) {
                colourTimer = System.nanoTime();
                mColorBox.setBackgroundColor(0xFFFF0000);
                if (!accelerating && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
                    accelerating = true;
                    lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    // TODO: Add to database as start of a hard acceleration
                }
            }
            else if (accelerating && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
                accelerating = false;
                lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                // TODO: Add to database as end of hard acceleration
            }

            if (System.nanoTime() - colourTimer > redTime) {
                colourTimer = 0;
                mColorBox.setBackgroundColor(0xFF00FF00);
            }
*/
             strx = "Accelerometer x-axis: " +  (acceleration[1] - (gyroRotation[7] * 10f)) + "\n"; 
             stry = "Accelerometer y-axis: " +  acceleration[1] + "\n";
             strz = "Accelerometer z-axis: " +  acceleration[2];

        }
    }


}