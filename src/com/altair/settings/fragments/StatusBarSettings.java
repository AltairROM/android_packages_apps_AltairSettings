/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS project
 * Copyright (C) 2019-2020 Altair ROM
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

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.StatusBarIcon;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.lineage.support.preferences.CustomSeekBarPreference;

import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "StatusBar";

    private static final String STATUS_BAR_SHOW_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_CLOCK = "status_bar_clock";
    private static final String CLOCK_SECONDS = "clock_seconds";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_SHOW_BATTERY = "status_bar_show_battery";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 2;

    private StatusBarIcon mClockIcon;
    private StatusBarIcon mBatteryIcon;

    private SwitchPreference mStatusBarShowClock;
    private LineageSystemSettingListPreference mStatusBarClock;
    private LineageSystemSettingListPreference mStatusBarAmPm;

    private SwitchPreference mStatusBarShowBattery;
    private LineageSystemSettingListPreference mStatusBarBattery;
    private LineageSystemSettingListPreference mStatusBarBatteryShowPercent;

    private static boolean sHasNotch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_settings);
        ContentResolver resolver = getActivity().getContentResolver();

        mClockIcon = new StatusBarIcon(getContext(), "clock");

        mStatusBarShowClock =
                (SwitchPreference) findPreference(STATUS_BAR_SHOW_CLOCK);
        mStatusBarShowClock.setOnPreferenceChangeListener(this);

        mStatusBarAmPm =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_AM_PM);
        mStatusBarClock =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK);
        mStatusBarClock.setOnPreferenceChangeListener(this);

        mStatusBarShowBattery =
                (SwitchPreference) findPreference(STATUS_BAR_SHOW_BATTERY);
        mStatusBarShowBattery.setOnPreferenceChangeListener(this);

        mBatteryIcon = new StatusBarIcon(getContext(), "battery");

        mStatusBarBatteryShowPercent =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        mStatusBarBattery =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(mStatusBarBattery.getIntValue(2));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean iconEnabled = mClockIcon.isEnabled();
        mStatusBarShowClock.setChecked(iconEnabled);

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        final boolean disallowCenteredClock = sHasNotch || getNetworkTrafficStatus() != 0;

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (disallowCenteredClock) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch_rtl);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_rtl);
            }
        } else if (disallowCenteredClock) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        } else {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
        }

        iconEnabled = mBatteryIcon.isEnabled();
        mStatusBarShowBattery.setChecked(iconEnabled);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (preference == mStatusBarShowClock) {
            mClockIcon.setEnabled((Boolean) newValue);
            if (DateFormat.is24HourFormat(getActivity())) {
                mStatusBarAmPm.setEnabled(false);
            }
            return true;
        } else if (preference == mStatusBarShowBattery) {
            mBatteryIcon.setEnabled((Boolean) newValue);
            return true;
        } else if (key == STATUS_BAR_BATTERY_STYLE) {
            int value = Integer.parseInt((String) newValue);
            enableStatusBarBatteryDependents(value);
        }
        return true;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
    }

    private int getNetworkTrafficStatus() {
        return LineageSettings.Secure.getInt(getActivity().getContentResolver(),
                LineageSettings.Secure.NETWORK_TRAFFIC_MODE, 0);
    }
}

