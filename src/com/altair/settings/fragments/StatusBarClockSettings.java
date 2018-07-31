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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.Menu;
import android.widget.EditText;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.Date;

import lineageos.providers.LineageSettings;

public class StatusBarClockSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "StatusBarClockSettings";

    private static final String STATUS_BAR_SHOW_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock_style";
    private static final String STATUS_BAR_CLOCK_SECONDS = "status_bar_clock_seconds";
    private static final String STATUS_BAR_CLOCK_AM_PM_STYLE = "status_bar_am_pm";

    private SwitchPreference mStatusBarClock;
    private ListPreference mClockStyle;
    private SwitchPreference mClockSeconds;
    private ListPreference mClockAmPmStyle;

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.status_bar_clock_settings);

        ContentResolver resolver = getActivity().getContentResolver();

        /*mStatusBarClock = (SwitchPreference) findPreference(STATUS_BAR_SHOW_CLOCK);
        mStatusBarClock.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK, 1) == 1));
        mStatusBarClock.setOnPreferenceChangeListener(this);*/

        /*mClockStyle = (ListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
        mClockStyle.setOnPreferenceChangeListener(this);
        mClockStyle.setValue(Integer.toString(LineageSettings.System.getInt(resolver,
                LineageSettings.System.STATUS_BAR_CLOCK, 0)));
        mClockStyle.setSummary(mClockStyle.getEntry());*/

        /*mClockSeconds = (SwitchPreference) findPreference(STATUS_BAR_CLOCK_SECONDS);
        mClockSeconds.setOnPreferenceChangeListener(this);
        int clockSeconds = Settings.System.getInt(resolver,
            Settings.System.STATUS_BAR_CLOCK_SECONDS, 0);
        mClockSeconds.setChecked(clockSeconds != 0);*/

        mClockAmPmStyle = (ListPreference) findPreference(STATUS_BAR_CLOCK_AM_PM_STYLE);
        /*mClockAmPmStyle.setOnPreferenceChangeListener(this);
        mClockAmPmStyle.setValue(Integer.toString(LineageSettings.System.getInt(resolver,
                LineageSettings.System.STATUS_BAR_AM_PM, 0)));*/
        boolean is24hour = DateFormat.is24HourFormat(getActivity());
        if (is24hour) {
            mClockAmPmStyle.setSummary(R.string.status_bar_am_pm_info);
        } else {
            mClockAmPmStyle.setSummary(mClockAmPmStyle.getEntry());
        }
        mClockAmPmStyle.setEnabled(!is24hour);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        AlertDialog dialog;
        ContentResolver resolver = getActivity().getContentResolver();
        /*if (preference == mStatusBarClock) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CLOCK, value ? 1 : 0);
            return true;
        } else if (preference == mClockStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mClockStyle.findIndexOfValue((String) newValue);
            LineageSettings.System.putInt(resolver,
                    LineageSettings.System.STATUS_BAR_CLOCK, val);
            mClockStyle.setSummary(mClockStyle.getEntries()[index]);
            return true;
        } else if (preference == mClockSeconds) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CLOCK_SECONDS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mClockAmPmStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mClockAmPmStyle.findIndexOfValue((String) newValue);
            LineageSettings.System.putInt(resolver,
                    LineageSettings.System.STATUS_BAR_AM_PM, val);
            mClockAmPmStyle.setSummary(mClockAmPmStyle.getEntries()[index]);
            return true;
        }*/
        return false;
    }
}
