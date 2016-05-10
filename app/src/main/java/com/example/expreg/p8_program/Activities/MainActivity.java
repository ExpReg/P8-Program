package com.example.expreg.p8_program.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Environment;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.example.expreg.p8_program.Model.AccelerometerMeasure;
import com.example.expreg.p8_program.R;
import com.example.expreg.p8_program.SensorHandlers.MyAccelerometerHandler;
import com.example.expreg.p8_program.SensorHandlers.MyCircularQueue;
import com.example.expreg.p8_program.SensorHandlers.MySensorHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient = null;
    AccelerometerMeasure calibrateAvg = null;
    AccelerometerMeasure calibrateVar = null;

    // Text views
    //protected TextView mLocationTextView = null;
    protected TextView mAccelerometerTextView = null;
    //protected TextView mMagnetometerTextView = null;
    //protected TextView mGyroscopeTextView = null;
    protected EditText mFreqChange = null;

    // Sensor handlers
    //protected MyLocationHandler mLocationHandler = null;
    protected MySensorHandler mAccelerometerHandler = null;
    //protected MySensorHandler mMagnetometerHandler = null;
    //protected MySensorHandler mGyroscopeHandler = null;

    // Buttons
    protected Button exportButton = null;
    protected Button deleteButton = null;
    protected Button startTripButton = null;
    protected Button stopTripButton = null;
    protected Button startCalibrationButton = null;
    protected Button stopCalibrationButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        buildGoogleApiClient();

        // Gets the text views
        //mLocationTextView = (TextView) findViewById(R.id.location_text);
        mAccelerometerTextView = (TextView) findViewById(R.id.accelerometer_text);
        //mMagnetometerTextView = (TextView) findViewById(R.id.magnetometer_text);
        //mGyroscopeTextView = (TextView) findViewById(R.id.gyroscope_text);
        //mFreqChange = (EditText) findViewById(R.id.freq_message);

        // Gets the buttons
        exportButton = (Button) findViewById(R.id.exportButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        startTripButton = (Button) findViewById(R.id.startTripButton);
        stopTripButton = (Button) findViewById(R.id.stopTripButton);
        startCalibrationButton = (Button) findViewById(R.id.startCalibrationButton);
        stopCalibrationButton = (Button) findViewById(R.id.stopCalibrationButton);

        stopTripButton.setEnabled(false);
        stopCalibrationButton.setEnabled(false);

        // Gets the calibration stuff from file
        File file = new File(MyCircularQueue.filename);
        if(file.exists()) {
            this.calibrateAvg = MyCircularQueue.readAverage(this);
            this.calibrateVar = MyCircularQueue.readVariance(this);
        }

        // Creates the sensor handlers
        //mLocationHandler = new MyLocationHandler(mGoogleApiClient, mLocationTextView, this);
        mAccelerometerHandler = new MyAccelerometerHandler(this);
        //mMagnetometerHandler = new MySensorHandler(mMagnetometerTextView, Sensor.TYPE_MAGNETIC_FIELD, this);
        //mGyroscopeHandler = new MySensorHandler(mGyroscopeTextView, Sensor.TYPE_GYROSCOPE, this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //List<SensorMeasure> measures = db.getAllMeasures();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings: {
                Intent intent = new Intent(this, MyPreferenceActivity.class);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
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

    // onPause
    // onResume

    // Google play services methods
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

    //Buttons
    public void export(View view){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String currentDBPath = mAccelerometerHandler.getDBLocation();
        String copyDBPath = "test.sqlite";
        File currentDB = new File(data, currentDBPath);
        File copyDB = new File(sd, copyDBPath);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(copyDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteDB(View view){
        new AlertDialog.Builder(this)
                .setTitle("Delete Database")
                .setMessage("Do you really want to delete the database?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        MySQLiteHelper.getInstance(getApplicationContext()).deleteDB();
                        Toast.makeText(MainActivity.this, "DB Deleted!", Toast.LENGTH_LONG).show();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void startTrip(View view) {
        Intent myIntent = new Intent(this, StartTripActivity.class);
        startActivity(myIntent);
    }

    public void stopTrip(View view) {
        mAccelerometerHandler.stop();
        startTripButton.setEnabled(true);
        startCalibrationButton.setEnabled(true);
        stopTripButton.setEnabled(false);
        exportButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    public void startCalibration(View view) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int frequency = sharedPref.getInt(getString(R.string.pref_sensorFrequency), 20);

        mAccelerometerHandler.start(frequency, true);
        startTripButton.setEnabled(false);
        startCalibrationButton.setEnabled(false);
        stopCalibrationButton.setEnabled(true);
        exportButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    public void stopCalibration(View view) {
        mAccelerometerHandler.stop();
        startTripButton.setEnabled(true);
        startCalibrationButton.setEnabled(true);
        stopCalibrationButton.setEnabled(false);
        exportButton.setEnabled(true);
        deleteButton.setEnabled(true);
        this.calibrateAvg = MyCircularQueue.readAverage(this);
        this.calibrateVar = MyCircularQueue.readVariance(this);
    }
}