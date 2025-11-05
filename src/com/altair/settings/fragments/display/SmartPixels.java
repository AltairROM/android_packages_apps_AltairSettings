/*
 * SPDX-FileCopyrightText: 2018 CarbonROM
 * SPDX-FileCopyrightText: 2019-2023 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments.display;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.lineage.support.preferences.SystemSettingSwitchPreference;

public class SmartPixels extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SmartPixels";

    private static final String ON_POWER_SAVE = "smart_pixels_on_power_save";
    private static final String SMART_PIXELS_FOOTER = "smart_pixels_footer";

    private SystemSettingSwitchPreference mSmartPixelsOnPowerSave;

    ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smart_pixels);

        mResolver = getActivity().getContentResolver();

        mSmartPixelsOnPowerSave = findPreference(ON_POWER_SAVE);

        findPreference(SMART_PIXELS_FOOTER).setTitle(R.string.smart_pixels_warning_text);

        updateDependency();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ALTAIR_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        updateDependency();
        return true;
    }

    private void updateDependency() {
        boolean mUseOnPowerSave = (Settings.System.getIntForUser(
                mResolver, Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                0, UserHandle.USER_CURRENT) == 1);
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        if (pm.isPowerSaveMode() && mUseOnPowerSave) {
            mSmartPixelsOnPowerSave.setEnabled(false);
        } else {
            mSmartPixelsOnPowerSave.setEnabled(true);
        }
    }
}
