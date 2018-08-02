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

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.View;

import java.util.Set;

import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.preference.SecureSettingSwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StatusBarClockSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "StatusBarClockSettings";

    private static final String ICON_BLACKLIST = "icon_blacklist";

    private static final String STATUS_BAR_SHOW_CLOCK = "status_bar_show_clock";
    private static final String STATUS_BAR_CLOCK = "status_bar_clock";
    private static final String CLOCK_SECONDS = "clock_seconds";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";

    private LineageSystemSettingListPreference mStatusBarClock;
    private SecureSettingSwitchPreference mClockSeconds;
    private LineageSystemSettingListPreference mStatusBarAmPm;
    private SwitchPreference mStatusBarShowClock;

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.status_bar_clock_settings);

        mStatusBarShowClock =
                (SwitchPreference) findPreference(STATUS_BAR_SHOW_CLOCK);
        mStatusBarShowClock.setOnPreferenceChangeListener(this);
        
        mStatusBarAmPm =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_AM_PM);
        mClockSeconds =
                (SecureSettingSwitchPreference) findPreference(CLOCK_SECONDS);
        mStatusBarClock =
                (LineageSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK);
    }

    @Override
    public void onResume() {
        super.onResume();

        final boolean hasNotch = getResources().getBoolean(
                org.lineageos.platform.internal.R.bool.config_haveNotch);

        final String curIconBlacklist = Settings.Secure.getString(getContext().getContentResolver(),
                ICON_BLACKLIST);

        if (TextUtils.delimitedStringContains(curIconBlacklist, ',', "clock")) {
            mStatusBarShowClock.setChecked(false);
            mStatusBarAmPm.setEnabled(false);
            mClockSeconds.setEnabled(false);
            mStatusBarClock.setEnabled(false);
        }
        else {
            mStatusBarShowClock.setChecked(true);
        }

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (hasNotch) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch_rtl);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_rtl);
            }
        } else if (hasNotch) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarShowClock) {
            boolean value = (Boolean) newValue;
            setStatusBarIcon("clock", value);
            if (!DateFormat.is24HourFormat(getActivity())) {
                mStatusBarAmPm.setEnabled(value);
            }
            mClockSeconds.setEnabled(value);
            mStatusBarClock.setEnabled(value);
            return true;
        }
        return false;
    }

    private void setStatusBarIcon(String key, boolean value) {
        ContentResolver contentResolver = getContext().getContentResolver();
        ArraySet<String> ret = new ArraySet<>();
        String blackListStr = Settings.Secure.getStringForUser(contentResolver, ICON_BLACKLIST,
                ActivityManager.getCurrentUser());
        if (blackListStr == null) {
            blackListStr = "rotate,headset";
        }
        String[] blacklist = blackListStr.split(",");
        for (String slot : blacklist) {
            if (!TextUtils.isEmpty(slot)) {
                ret.add(slot);
            }
        }
        if (value) {
            if (ret.contains(key)) {
                ret.remove(key);
            }
        }
        else {
            if (!ret.contains(key)) {
                ret.add(key);
            }
        }
        Settings.Secure.putStringForUser(contentResolver, ICON_BLACKLIST,
                TextUtils.join(",", ret), ActivityManager.getCurrentUser());
    }
}
