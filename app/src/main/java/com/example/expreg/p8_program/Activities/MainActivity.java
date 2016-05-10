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
    // Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains("pref_accAvg") || !PreferenceManager.getDefaultSharedPreferences(this).contains("pref_accVar")) {
            Intent intent = new Intent(this, CalibrationActivity.class);
            startActivity(intent);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    public void startTrip(View view) {
        Intent myIntent = new Intent(this, StartTripActivity.class);
        startActivity(myIntent);
    }
}