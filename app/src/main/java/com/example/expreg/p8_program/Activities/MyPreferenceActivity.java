package com.example.expreg.p8_program.Activities;

import android.app.Activity;
import android.os.Bundle;

public class MyPreferenceActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();
    }
}