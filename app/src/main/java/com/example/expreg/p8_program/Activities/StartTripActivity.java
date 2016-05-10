package com.example.expreg.p8_program.Activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.example.expreg.p8_program.R;
import com.example.expreg.p8_program.SensorHandlers.MyAccelerometerHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class StartTripActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;
    protected MyAccelerometerHandler mAccelerometerHandler;
    protected SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_trip);

        mGoogleApiClient = buildGoogleApiClient();
        mAccelerometerHandler = new MyAccelerometerHandler(this);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        int frequency = Integer.parseInt(mSharedPref.getString("pref_sensorFrequency", "20"));
        mAccelerometerHandler.start(frequency);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAccelerometerHandler.stop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    // Buttons
    public void stopTrip(View view) {
        finish();
    }

    // Google play services methods
    protected synchronized GoogleApiClient buildGoogleApiClient() {
        Log.i("StartTripActivity", "Building GoogleApiClient");
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
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
