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
        if (pref.getKey().equals("pref_detectionStyle")) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPrefs.getString("pref_detectionStyle","default").equals("lenient")) {
                sharedPrefs.edit().putFloat("pref_accelerationThreshold", 3.0f).putFloat("pref_decelerationThreshold", 4.0f).apply();
            }
            else if (sharedPrefs.getString("pref_detectionStyle","default").equals("strict")) {
                sharedPrefs.edit().putFloat("pref_accelerationThreshold", 1.5f).putFloat("pref_decelerationThreshold", 2.0f).apply();
            }
        }
        if (pref instanceof EditTextPreference) {
            EditTextPreference editPref = (EditTextPreference) pref;
            pref.setSummary(editPref.getText());
        }
    }
}