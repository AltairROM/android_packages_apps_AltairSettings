/*
 * Copyright (C) 2019-2025 Altair ROM Project
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
import android.content.Context;
import android.content.Intent;
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
import com.android.settingslib.search.SearchIndexable;

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

    //private static final String KEY_USE_OLD_MOBILETYPE = "use_old_mobiletype";
    private static final String KEY_DATA_DISABLED_ICON = "data_disabled_icon";
    private static final String KEY_ROAMING_INDICATOR_ICON = "roaming_indicator_icon";
    private static final String KEY_SHOW_FOURG_ICON = "show_fourg_icon";
    private static final String KEY_SHOW_VOLTE_ICON = "show_volte_icon";
    private static final String KEY_SHOW_VOWIFI_ICON = "show_vowifi_icon";
    private static final String KEY_VOLTE_VOWIFI_OVERRIDE = "volte_vowifi_override";

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_SHOW_BATTERY = "status_bar_show_battery";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 2;

    private static final String NETWORK_TRAFFIC_SETTINGS = "network_traffic_settings";

    private Context mContext;
    private ContentResolver mResolver;

    //private SwitchPreference mUseOldMobileType;

    private StatusBarIcon mClockIcon;
    private StatusBarIcon mBatteryIcon;

    private LineageSystemSettingListPreference mStatusBarClock;
    private LineageSystemSettingListPreference mStatusBarAmPm;

    private SwitchPreferenceCompat mStatusBarShowBattery;
    private LineageSystemSettingListPreference mStatusBarBatteryShowPercent;

    private PreferenceCategory mStatusBarBatteryCategory;
    private PreferenceCategory mStatusBarClockCategory;

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
        //mUseOldMobileType = findPreference(Settings.System.USE_OLD_MOBILETYPE);
        SystemSettingSwitchPreference dataDisabledIcon = findPreference(KEY_DATA_DISABLED_ICON);
        SystemSettingSwitchPreference roamingIndicatorIcon =
                findPreference(KEY_ROAMING_INDICATOR_ICON);
        SystemSettingSwitchPreference showFourgIcon = findPreference(KEY_SHOW_FOURG_ICON);
        SystemSettingSwitchPreference showVoLTEIcon = findPreference(KEY_SHOW_VOLTE_ICON);
        SystemSettingSwitchPreference showVoWiFiIcon = findPreference(KEY_SHOW_VOWIFI_ICON);
        SystemSettingSwitchPreference voLTEvoWiFiOverride =
                findPreference(KEY_VOLTE_VOWIFI_OVERRIDE);

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            //networkCategory.removePreference(mUseOldMobileType);
            networkCategory.removePreference(dataDisabledIcon);
            networkCategory.removePreference(roamingIndicatorIcon);
            networkCategory.removePreference(showFourgIcon);
            networkCategory.removePreference(showVoLTEIcon);
            networkCategory.removePreference(showVoWiFiIcon);
            networkCategory.removePreference(voLTEvoWiFiOverride);
        /*} else {
            boolean configUseOldMobileType = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_useOldMobileIcons);
            boolean showing = Settings.System.getIntForUser(mResolver,
                    Settings.System.USE_OLD_MOBILETYPE,
                    configUseOldMobileType ? 1 : 0, UserHandle.USER_CURRENT) != 0;
            mUseOldMobileType.setChecked(showing);
            mUseOldMobileType.setOnPreferenceChangeListener(this);*/
        }

        mClockIcon = new StatusBarIcon(mContext, "clock");
        mStatusBarClockCategory = prefScreen.findPreference(CATEGORY_CLOCK);

        mStatusBarAmPm = findPreference(STATUS_BAR_AM_PM);
        mStatusBarClock = findPreference(STATUS_BAR_CLOCK_STYLE);
        mStatusBarClock.setOnPreferenceChangeListener(this);

        mBatteryIcon = new StatusBarIcon(mContext, "battery");
        mStatusBarBatteryCategory = prefScreen.findPreference(CATEGORY_BATTERY);

        mStatusBarShowBattery = findPreference(STATUS_BAR_SHOW_BATTERY);
        mStatusBarShowBattery.setOnPreferenceChangeListener(this);

        mStatusBarBatteryShowPercent = findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        LineageSystemSettingListPreference statusBarBattery =
                findPreference(STATUS_BAR_BATTERY_STYLE);
        statusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(statusBarBattery.getIntValue(2));
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
            /*case KEY_USE_OLD_MOBILETYPE:
                Settings.System.putIntForUser(mResolver, Settings.System.USE_OLD_MOBILETYPE,
                        (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
                break;*/
        }
        return true;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
    }

    private int getNetworkTrafficStatus() {
        int mode = LineageSettings.Secure.getInt(getActivity().getContentResolver(),
                LineageSettings.Secure.NETWORK_TRAFFIC_MODE, 0);
        int position = LineageSettings.Secure.getInt(getActivity().getContentResolver(),
                LineageSettings.Secure.NETWORK_TRAFFIC_POSITION, /* Center */ 1);
        return mode != 0 && position == 1 ? 1 : 0;
    }

    private int getClockPosition() {
        return LineageSettings.System.getInt(getActivity().getContentResolver(),
                STATUS_BAR_CLOCK_STYLE, 2);
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
