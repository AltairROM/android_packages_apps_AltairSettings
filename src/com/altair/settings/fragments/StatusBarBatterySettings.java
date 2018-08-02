/*
 * Copyright (C) GZOSP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altair.settings.fragments;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.altair.settings.StatusBarIcon;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StatusBarBatterySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_SHOW_BATTERY = "status_bar_show_battery";

    private SwitchPreference mStatusBarShowBattery;
    private SwitchPreference mBatteryPercentage;

    private StatusBarIcon mBatteryIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.status_bar_battery_settings);
        final ContentResolver resolver = getActivity().getContentResolver();

        mBatteryIcon = new StatusBarIcon(getContext(), "battery");

        mStatusBarShowBattery =
                (SwitchPreference) findPreference(STATUS_BAR_SHOW_BATTERY);
        mStatusBarShowBattery.setOnPreferenceChangeListener(this);

        boolean show = Settings.System.getInt(resolver,
                Settings.System.SHOW_BATTERY_PERCENT, 1) == 1;
        mBatteryPercentage = (SwitchPreference) findPreference("show_battery_percent");
        mBatteryPercentage.setChecked(show);
        mBatteryPercentage.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean iconEnabled = mBatteryIcon.isEnabled();
        mStatusBarShowBattery.setChecked(iconEnabled);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarShowBattery) {
            mBatteryIcon.setEnabled((Boolean) newValue);
            return true;
        } else if (preference == mBatteryPercentage) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_BATTERY_PERCENT, value ? 1 : 0);
            return true;
        }
        return false;
    }
}
