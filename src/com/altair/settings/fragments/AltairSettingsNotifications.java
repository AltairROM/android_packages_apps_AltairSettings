/*
 * SPDX-FileCopyrightText: 2019-2026 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.custom.Utils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.lineage.support.preferences.SystemSettingListPreference;
import com.lineage.support.preferences.SystemSettingSeekBarPreference;
import com.lineage.support.preferences.SystemSettingSwitchPreference;

import java.util.Arrays;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AltairSettingsNotifications extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AltairSettingsNotifications";

    private static final String FLASHLIGHT_CATEGORY = "flashlight_category";
    private static final String FLASHLIGHT_CALL_PREF = "flashlight_on_call";
    private static final String FLASHLIGHT_DND_PREF = "flashlight_on_call_ignore_dnd";
    private static final String FLASHLIGHT_RATE_PREF = "flashlight_on_call_rate";

    private SystemSettingListPreference mFlashOnCall;
    private SystemSettingSwitchPreference mFlashOnCallIgnoreDND;
    private SystemSettingSeekBarPreference mFlashOnCallRate;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.altair_settings_notifications;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Context context = getActivity().getApplicationContext();
        final ContentResolver resolver = context.getContentResolver();

        if (!Utils.deviceHasFlashlight(context)) {
            final PreferenceCategory flashlightCategory = prefScreen.findPreference(FLASHLIGHT_CATEGORY);
            prefScreen.removePreference(flashlightCategory);
        } else {
            mFlashOnCall = (SystemSettingListPreference) prefScreen.findPreference(FLASHLIGHT_CALL_PREF);
            mFlashOnCall.setOnPreferenceChangeListener(this);

            mFlashOnCallIgnoreDND = (SystemSettingSwitchPreference) prefScreen.findPreference(FLASHLIGHT_DND_PREF);
            int value = Settings.System.getInt(resolver, Settings.System.FLASHLIGHT_ON_CALL, 0);

            mFlashOnCallRate = (SystemSettingSeekBarPreference) prefScreen.findPreference(FLASHLIGHT_RATE_PREF);

            mFlashOnCallIgnoreDND.setEnabled(value > 1);
            mFlashOnCallRate.setEnabled(value > 0);
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
        if (preference == mFlashOnCall) {
            int value = Integer.parseInt((String) newValue);
            mFlashOnCallIgnoreDND.setEnabled(value > 1);
            mFlashOnCallRate.setEnabled(value > 0);
            return true;
        }
        return false;
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
                    sir.xmlResId = R.xml.altair_settings_notifications;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!Utils.deviceHasFlashlight(context)) {
                        keys.add(FLASHLIGHT_CALL_PREF);
                        keys.add(FLASHLIGHT_DND_PREF);
                        keys.add(FLASHLIGHT_RATE_PREF);
                    }

                    return keys;
                }
            };
}
