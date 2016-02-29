package com.example.expreg.p8_program;

import android.hardware.Sensor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient = null;
    protected TextView mLocationTextView = null;
    protected TextView mAccelerometerTextView = null;
    protected MyLocationHandler mLocationHandler = null;
    protected MySensorHandler mAccelerometerHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();

        // Gets the text views
        mLocationTextView = (TextView) findViewById(R.id.location_text);
        mAccelerometerTextView = (TextView) findViewById(R.id.accelerometer_text);

        // Creates the sensor handlers
        mLocationHandler = new MyLocationHandler(mGoogleApiClient, mLocationTextView, this);
        mAccelerometerHandler = new MySensorHandler(mAccelerometerTextView, Sensor.TYPE_ACCELEROMETER, this);
        // TODO add other sensors same way as accelerometer
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationHandler.stop();
        mAccelerometerHandler.stop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationHandler.start();
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
