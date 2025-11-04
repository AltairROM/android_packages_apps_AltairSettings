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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.DeviceUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;

import lineageos.preference.LineageSecureSettingListPreference;
import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AltairSettingsQS extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AltairSettingsQS";

    private static final String QS_HEADER_CATEGORY = "qs_header_category";
    private static final String QS_TILE_ACTIONS_CATEGORY = "qs_tile_actions_category";

    private static final String KEY_QUICK_PULLDOWN = "qs_quick_pulldown";
    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_BRIGHTNESS_SLIDER_HAPTIC = "qs_brightness_slider_haptic";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String KEY_QS_TILE_HAPTIC = "qs_tile_haptic";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;
    private static final int PULLDOWN_DIR_ALWAYS = 3;

    private LineageSystemSettingListPreference mQuickPulldown;
    private LineageSecureSettingSwitchPreference mShowBrightnessSlider;
    private LineageSecureSettingListPreference mBrightnessSliderPosition;
    private LineageSecureSettingSwitchPreference mShowAutoBrightness;
    private Preference mBrightnessSliderHaptic;
    private Preference mQsTileHaptic;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.altair_settings_qs;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = mContext.getContentResolver();

        PreferenceCategory headerCategory = findPreference(QS_HEADER_CATEGORY);
        PreferenceCategory tileActionsCategory = findPreference(QS_TILE_ACTIONS_CATEGORY);

        mBrightnessSliderHaptic = findPreference(KEY_BRIGHTNESS_SLIDER_HAPTIC);
        mQsTileHaptic = findPreference(KEY_QS_TILE_HAPTIC);

        mQuickPulldown = findPreference(KEY_QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        mShowBrightnessSlider = findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
        mShowBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean showSlider = LineageSettings.Secure.getIntForUser(resolver,
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) != 0;

        mBrightnessSliderPosition = findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
        mBrightnessSliderPosition.setEnabled(showSlider);

        boolean hapticAvailable = DeviceUtils.hasVibrator(mContext);
        if (hapticAvailable) {
            mBrightnessSliderHaptic.setEnabled(showSlider);
        } else {
            headerCategory.removePreference(mBrightnessSliderHaptic);
            tileActionsCategory.removePreference(mQsTileHaptic);
        }

        mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
        boolean automaticAvailable = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (automaticAvailable) {
            mShowAutoBrightness.setEnabled(showSlider);
        } else {
            headerCategory.removePreference(mShowAutoBrightness);
        }
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

        // Adjust QS panel preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.qs_quick_pulldown_entries_rtl);
        }
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
        ContentResolver resolver = getActivity().getContentResolver();

        String key = preference.getKey();
        switch (key) {
            case KEY_QUICK_PULLDOWN:
                updateQuickPulldownSummary(Integer.parseInt((String) newValue));
                break;
            case KEY_SHOW_BRIGHTNESS_SLIDER:
                final boolean value = (Boolean) newValue;
                mBrightnessSliderPosition.setEnabled(value);
                if (mBrightnessSliderHaptic != null)
                    mBrightnessSliderHaptic.setEnabled(value);
                if (mShowAutoBrightness != null)
                    mShowAutoBrightness.setEnabled(value);
                return true;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.qs_quick_pulldown_off);
                break;

            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.qs_quick_pulldown_summary,
                    getResources().getString(value == PULLDOWN_DIR_LEFT
                        ? R.string.qs_quick_pulldown_summary_left
                        : R.string.qs_quick_pulldown_summary_right));
                break;

            case PULLDOWN_DIR_ALWAYS:
                summary = getResources().getString(
                    R.string.qs_quick_pulldown_always);
                break;
        }
        mQuickPulldown.setSummary(summary);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.altair_settings_qs;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();

                    boolean automaticAvailable = res.getBoolean(
                            com.android.internal.R.bool.config_automatic_brightness_available);
                    if (!automaticAvailable) {
                        keys.add(KEY_SHOW_AUTO_BRIGHTNESS);
                    }

                    boolean hapticAvailable = DeviceUtils.hasVibrator(context);
                    if (!hapticAvailable) {
                        keys.add(KEY_BRIGHTNESS_SLIDER_HAPTIC);
                        keys.add(KEY_QS_TILE_HAPTIC);
                    }

                    return keys;
                }
            };
}
