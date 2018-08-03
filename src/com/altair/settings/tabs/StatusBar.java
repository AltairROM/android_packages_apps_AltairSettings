/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
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

package com.altair.settings.tabs;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.text.format.DateFormat;
import android.view.View;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.altair.settings.preference.CustomSeekBarPreference;
import com.altair.settings.StatusBarIcon;

import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.preference.SecureSettingSwitchPreference;

public class StatusBar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "StatusBar";

    private static final String STATUS_BAR_SHOW_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_CLOCK = "status_bar_clock";
    private static final String CLOCK_SECONDS = "clock_seconds";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_SHOW_BATTERY = "status_bar_show_battery";
    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private StatusBarIcon mClockIcon;
    private StatusBarIcon mBatteryIcon;
    private boolean m24HourClock;

    private LineageSystemSettingListPreference mStatusBarClock;
    private LineageSystemSettingListPreference mStatusBarAmPm;
    private SwitchPreference mStatusBarShowClock;
    private SwitchPreference mStatusBarShowBattery;
    private SwitchPreference mBatteryPercentage;
    private LineageSystemSettingListPreference mQuickPulldown;
    private CustomSeekBarPreference mQsRowsPort;
    private CustomSeekBarPreference mQsRowsLand;
    private CustomSeekBarPreference mQsColumnsPort;
    private CustomSeekBarPreference mQsColumnsLand;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tab_status_bar);
        ContentResolver resolver = getActivity().getContentResolver();

        mClockIcon = new StatusBarIcon(getContext(), "clock");

        mStatusBarShowClock =
                (SwitchPreference) findPreference(STATUS_BAR_SHOW_CLOCK);
        mStatusBarShowClock.setOnPreferenceChangeListener(this);
        
        mStatusBarAmPm =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_AM_PM);
        mStatusBarClock =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK);

        m24HourClock = DateFormat.is24HourFormat(getActivity());

        mBatteryIcon = new StatusBarIcon(getContext(), "battery");

        mStatusBarShowBattery =
                (SwitchPreference) findPreference(STATUS_BAR_SHOW_BATTERY);
        mStatusBarShowBattery.setOnPreferenceChangeListener(this);

        boolean show = Settings.System.getInt(resolver,
                Settings.System.SHOW_BATTERY_PERCENT, 1) == 1;
        mBatteryPercentage = (SwitchPreference) findPreference("show_battery_percent");
        mBatteryPercentage.setChecked(show);
        mBatteryPercentage.setOnPreferenceChangeListener(this);

        mQuickPulldown =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        int value = Settings.System.getIntForUser(resolver,
                Settings.System.QS_ROWS_PORTRAIT, 3, UserHandle.USER_CURRENT);
        mQsRowsPort = (CustomSeekBarPreference) findPreference("qs_rows_portrait");
        mQsRowsPort.setValue(value);
        mQsRowsPort.setOnPreferenceChangeListener(this);

        value = Settings.System.getIntForUser(resolver,
                Settings.System.QS_ROWS_LANDSCAPE, 3, UserHandle.USER_CURRENT);
        mQsRowsLand = (CustomSeekBarPreference) findPreference("qs_rows_landscape");
        mQsRowsLand.setValue(value);
        mQsRowsLand.setOnPreferenceChangeListener(this);

        value = Settings.System.getIntForUser(resolver,
                Settings.System.QS_COLUMNS_PORTRAIT, 3, UserHandle.USER_CURRENT);
        mQsColumnsPort = (CustomSeekBarPreference) findPreference("qs_columns_portrait");
        mQsColumnsPort.setValue(value);
        mQsColumnsPort.setOnPreferenceChangeListener(this);

        value = Settings.System.getIntForUser(resolver,
                Settings.System.QS_COLUMNS_LANDSCAPE, 3, UserHandle.USER_CURRENT);
        mQsColumnsLand = (CustomSeekBarPreference) findPreference("qs_columns_landscape");
        mQsColumnsLand.setValue(value);
        mQsColumnsLand.setOnPreferenceChangeListener(this);
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

        if (m24HourClock) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        final boolean hasNotch = getResources().getBoolean(
                org.lineageos.platform.internal.R.bool.config_haveNotch);

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (hasNotch) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch_rtl);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_rtl);
            }
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_qs_pulldown_values_rtl);
        } else if (hasNotch) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        }

        iconEnabled = mBatteryIcon.isEnabled();
        mStatusBarShowBattery.setChecked(iconEnabled);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarShowClock) {
            mClockIcon.setEnabled((Boolean) newValue);
            if (m24HourClock) {
                mStatusBarAmPm.setEnabled(false);
            }
            return true;
        } else if (preference == mStatusBarShowBattery) {
            mBatteryIcon.setEnabled((Boolean) newValue);
            return true;
        } else if (preference == mBatteryPercentage) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_BATTERY_PERCENT, value ? 1 : 0);
            return true;
        } else if (preference == mQuickPulldown) {
            updateQuickPulldownSummary(Integer.parseInt((String) newValue));
        } else if (preference == mQsRowsPort) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_ROWS_PORTRAIT, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsRowsLand) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_ROWS_LANDSCAPE, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsPort) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_COLUMNS_PORTRAIT, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsLand) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_COLUMNS_LANDSCAPE, val, UserHandle.USER_CURRENT);
            return true;
        }
        return true;
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_off);
                break;

            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources().getString(value == PULLDOWN_DIR_LEFT
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right));
                break;
        }
        mQuickPulldown.setSummary(summary);
    }
}

