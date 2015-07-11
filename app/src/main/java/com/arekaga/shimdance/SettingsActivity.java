package com.arekaga.shimdance;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {
    /**
     * Key to get and save Sampling Rate in application preferences
     */
    public final static String SAMPLING_RATE = "settings_sampling_rate";

    /**
     * Key to get and save Accel Range in application preferences
     */
    public final static String ACCEL_RANGE = "settings_accel_range";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ConnectionFragment())
                .commit();
    }

    public static class ConnectionFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
