package com.example.expreg.p8_program.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;

import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.example.expreg.p8_program.Model.AccelerometerMeasure;
import com.example.expreg.p8_program.R;
import com.example.expreg.p8_program.SensorHandlers.MyAccelerometerHandler;
import com.example.expreg.p8_program.SensorHandlers.MyCircularQueue;
import com.example.expreg.p8_program.SensorHandlers.MySensorHandler;

public class MainActivity extends AppCompatActivity {
    AccelerometerMeasure calibrateAvg = null;
    AccelerometerMeasure calibrateVar = null;

    // Text views
    protected TextView mAccelerometerTextView = null;

    // Sensor handlers
    protected MySensorHandler mAccelerometerHandler = null;

    // Buttons
    protected Button exportButton = null;
    protected Button deleteButton = null;
    protected Button startTripButton = null;
    protected Button stopTripButton = null;
    protected Button startCalibrationButton = null;
    protected Button stopCalibrationButton = null;

    // Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Gets the text views
        mAccelerometerTextView = (TextView) findViewById(R.id.accelerometer_text);

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
        // TODO: Convert to shared preferences
        File file = new File(MyCircularQueue.filename);
        if(file.exists()) {
            this.calibrateAvg = MyCircularQueue.readAverage(this);
            this.calibrateVar = MyCircularQueue.readVariance(this);
        }

        mAccelerometerHandler = new MyAccelerometerHandler(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAccelerometerHandler.stop();
    }

    // Options menu
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

    //Button methods
    public void export(View view){
        MySQLiteHelper.getInstance(this).exportDB();
        Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
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