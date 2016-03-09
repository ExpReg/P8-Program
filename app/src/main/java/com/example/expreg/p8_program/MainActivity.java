package com.example.expreg.p8_program;

import android.hardware.Sensor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient = null;

    // Text views
    //protected TextView mLocationTextView = null;
    protected TextView mAccelerometerTextView = null;
    //protected TextView mMagnetometerTextView = null;
    //protected TextView mGyroscopeTextView = null;

    // Sensor handlers
    //protected MyLocationHandler mLocationHandler = null;
    protected MySensorHandler mAccelerometerHandler = null;
    //protected MySensorHandler mMagnetometerHandler = null;
    //protected MySensorHandler mGyroscopeHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();

        MySQLiteHelper db = new MySQLiteHelper(this);

        // Gets the text views
        //mLocationTextView = (TextView) findViewById(R.id.location_text);
        mAccelerometerTextView = (TextView) findViewById(R.id.accelerometer_text);
        //mMagnetometerTextView = (TextView) findViewById(R.id.magnetometer_text);
        //mGyroscopeTextView = (TextView) findViewById(R.id.gyroscope_text);

        // Creates the sensor handlers
        //mLocationHandler = new MyLocationHandler(mGoogleApiClient, mLocationTextView, this);
        mAccelerometerHandler = new MySensorHandler(mAccelerometerTextView, Sensor.TYPE_ACCELEROMETER, this, db);
        //mMagnetometerHandler = new MySensorHandler(mMagnetometerTextView, Sensor.TYPE_MAGNETIC_FIELD, this);
        //mGyroscopeHandler = new MySensorHandler(mGyroscopeTextView, Sensor.TYPE_GYROSCOPE, this);

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i("MainActivity", "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mAccelerometerHandler.start();
        //mMagnetometerHandler.start();
        //mGyroscopeHandler.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mLocationHandler.stop();
        mAccelerometerHandler.stop();
        //mMagnetometerHandler.stop();
        //mGyroscopeHandler.stop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //mLocationHandler.start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("MainActivity", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("MainActivity", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
}
