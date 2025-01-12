package com.example.donappt5.views;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import androidx.preference.PreferenceFragmentCompat;

import com.example.donappt5.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_settings);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}