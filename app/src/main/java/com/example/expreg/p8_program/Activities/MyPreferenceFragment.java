package com.example.expreg.p8_program.Activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.expreg.p8_program.R;

public class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
