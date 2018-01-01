package org.schabi.newpipe.settings;

import android.os.Bundle;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;

public class MainSettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (App.isSpecial()) {
            addPreferencesFromResource(R.xml.main_settings);
        } else {
            addPreferencesFromResource(R.xml.main_settings2);
        }
    }
}
