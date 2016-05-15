package com.example.expreg.p8_program.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.expreg.p8_program.Constants;
import com.example.expreg.p8_program.DB.MySQLiteHelper;
import com.example.expreg.p8_program.R;

public class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            updatePreference(preference);
        }

        findPreference("pref_detectionStyle").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String val = (String) newValue;
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (val.equals("lenient")) {
                    sharedPrefs.edit().putFloat("pref_accelerationThreshold", Constants.ACCELERATION_THRESHOLD_LENIENT).putFloat("pref_decelerationThreshold", Constants.DECELERATION_THRESHOLD_LENIENT).apply();
                }
                else if (val.equals("strict")) {
                    sharedPrefs.edit().putFloat("pref_accelerationThreshold", Constants.ACCELERATION_THRESHOLD_STRICT).putFloat("pref_decelerationThreshold", Constants.DECELERATION_THRESHOLD_STRICT).apply();
                }
                return true;
            }
        });

        findPreference("pref_exportDB").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MySQLiteHelper.getInstance(getActivity()).exportDB(getActivity());
                Toast.makeText(getActivity(), "DB Exported!", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        findPreference("pref_deleteDB").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Database")
                        .setMessage("Do you really want to delete the database?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                MySQLiteHelper.getInstance(getActivity()).deleteDB();
                                Toast.makeText(getActivity(), "DB Deleted!", Toast.LENGTH_LONG).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key));
    }

    public void updatePreference (Preference pref) {
        if (pref == null) return;
        if (pref instanceof EditTextPreference) {
            EditTextPreference editPref = (EditTextPreference) pref;
            pref.setSummary(editPref.getText());
        }
    }
}