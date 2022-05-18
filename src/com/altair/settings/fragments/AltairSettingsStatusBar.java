/*
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.altair.settings.utils.DeviceUtils;
import com.altair.settings.utils.StatusBarIcon;
import com.altair.settings.utils.TelephonyUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.fuelgauge.BatteryUtils;
import com.android.settingslib.search.SearchIndexable;

import com.lineage.support.preferences.SystemSettingListPreference;
import com.lineage.support.preferences.SystemSettingSwitchPreference;

import java.util.Arrays;
import java.util.List;

import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AltairSettingsStatusBar extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AltairSettingsStatusBar";

    private static final String CATEGORY_NETWORK = "network_category";
    private static final String CATEGORY_BATTERY = "status_bar_battery_key";
    private static final String CATEGORY_CLOCK = "status_bar_clock_key";

    private static final String ICON_BLACKLIST = "icon_blacklist";

    private static final String KEY_DATA_DISABLED_ICON = "data_disabled_icon";
    private static final String KEY_ROAMING_INDICATOR_ICON = "roaming_indicator_icon";
    private static final String KEY_SHOW_FOURG_ICON = "show_fourg_icon";

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_SHOW_BATTERY = "status_bar_show_battery";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_CIRCLE = 1;
    private static final int BATTERY_STYLE_TEXT = 2;
    private static final int BATTERY_STYLE_TWO_TONE_PORTRAIT = 3;

    private static final String NETWORK_TRAFFIC_SETTINGS = "network_traffic_settings";

    private Context mContext;
    private ContentResolver mResolver;

    private StatusBarIcon mClockIcon;
    private StatusBarIcon mBatteryIcon;

    private LineageSystemSettingListPreference mStatusBarClock;
    private LineageSystemSettingListPreference mStatusBarAmPm;
    private LineageSystemSettingListPreference mStatusBarBatteryShowPercent;

    private SwitchPreferenceCompat mStatusBarShowBattery;

    private PreferenceCategory mStatusBarBatteryCategory;
    private PreferenceCategory mStatusBarClockCategory;

    private boolean mBatteryPresent;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.altair_settings_status_bar;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mResolver = getActivity().getContentResolver();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        final PreferenceCategory networkCategory = prefScreen.findPreference(CATEGORY_NETWORK);
        SystemSettingSwitchPreference dataDisabledIcon = findPreference(KEY_DATA_DISABLED_ICON);
        SystemSettingSwitchPreference roamingIndicatorIcon =
                findPreference(KEY_ROAMING_INDICATOR_ICON);
        SystemSettingSwitchPreference showFourgIcon = findPreference(KEY_SHOW_FOURG_ICON);

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            networkCategory.removePreference(dataDisabledIcon);
            networkCategory.removePreference(roamingIndicatorIcon);
            networkCategory.removePreference(showFourgIcon);
        }

        mClockIcon = new StatusBarIcon(mContext, "clock");
        mStatusBarClockCategory = prefScreen.findPreference(CATEGORY_CLOCK);

        mStatusBarAmPm = findPreference(STATUS_BAR_AM_PM);
        mStatusBarClock = findPreference(STATUS_BAR_CLOCK_STYLE);
        mStatusBarClock.setOnPreferenceChangeListener(this);

        mBatteryIcon = new StatusBarIcon(mContext, "battery");
        mStatusBarBatteryCategory = prefScreen.findPreference(CATEGORY_BATTERY);

        Intent intent = BatteryUtils.getBatteryIntent(getContext());
        if (intent != null) {
            mBatteryPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);
        }

        mStatusBarShowBattery = findPreference(STATUS_BAR_SHOW_BATTERY);
        mStatusBarShowBattery.setOnPreferenceChangeListener(this);

        mStatusBarBatteryShowPercent = findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        LineageSystemSettingListPreference statusBarBattery =
                findPreference(STATUS_BAR_BATTERY_STYLE);
        statusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(statusBarBattery.getIntValue(BATTERY_STYLE_TEXT));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onResume() {
        super.onResume();

        final String curIconBlacklist = Settings.Secure.getString(getContext().getContentResolver(),
                ICON_BLACKLIST);

        if (TextUtils.delimitedStringContains(curIconBlacklist, ',', "clock")) {
            getPreferenceScreen().removePreference(mStatusBarClockCategory);
        } else {
            getPreferenceScreen().addPreference(mStatusBarClockCategory);
        }

        if (!mBatteryPresent ||
                TextUtils.delimitedStringContains(curIconBlacklist, ',', "battery")) {
            getPreferenceScreen().removePreference(mStatusBarBatteryCategory);
        } else {
            getPreferenceScreen().addPreference(mStatusBarBatteryCategory);
        }

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        final boolean disallowCenteredClock = DeviceUtils.hasCenteredCutout(getActivity())
                    || getNetworkTrafficStatus() != 0;

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (disallowCenteredClock) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
            }
        } else if (disallowCenteredClock) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        } else {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
        }

        mStatusBarShowBattery.setChecked(mBatteryIcon.isEnabled());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case STATUS_BAR_SHOW_BATTERY:
                mBatteryIcon.setEnabled((Boolean) newValue);
                break;
            case STATUS_BAR_BATTERY_STYLE:
                enableStatusBarBatteryDependents(Integer.parseInt((String) newValue));
                break;
        }
        return true;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != BATTERY_STYLE_TEXT);
    }

    private int getNetworkTrafficStatus() {
        int mode = LineageSettings.Secure.getInt(getActivity().getContentResolver(),
                LineageSettings.Secure.NETWORK_TRAFFIC_MODE, 0);
        int position = LineageSettings.Secure.getInt(getActivity().getContentResolver(),
                LineageSettings.Secure.NETWORK_TRAFFIC_POSITION, /* Center */ 1);
        return mode != 0 && position == 1 ? 1 : 0;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.altair_settings_status_bar;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    return keys;
                }
            };
}
