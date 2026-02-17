/*
 * SPDX-FileCopyrightText: 2019-2026 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.altair.settings.utils.DeviceUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.lineage.support.preferences.CustomSeekBarPreference;
import com.lineage.support.preferences.SystemSettingSwitchPreference;

import java.util.Arrays;
import java.util.List;

import lineageos.providers.LineageSettings;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AltairSettingsSound extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AltairSettingsSound";

    private static final String KEY_VOLUME_PANEL_POSITION = "volume_panel_on_left";
    private static final String KEY_SHOW_APP_VOLUME = "show_app_volume";
    /*
    private static final String KEY_MAX_MUSIC_VOLUME = "max_music_volume";
    private static final String KEY_MAX_CALL_VOLUME = "max_call_volume";
    private static final String KEY_MAX_ALARM_VOLUME = "max_alarm_volume";

    private static final String CATEGORY_VOLUME_PANEL = "volume_panel_control";
    */

    private ContentResolver mResolver;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.altair_settings_sound;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolver = getActivity().getContentResolver();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        /*
        final PreferenceCategory volumePanel = prefScreen.findPreference(CATEGORY_VOLUME_PANEL);

        final boolean hasVolumeKeys = DeviceUtils.hasVolumeKeys(getActivity());
        if (hasVolumeKeys) {
            // Volume steps
            setVolumeStepsPreference(prefScreen, KEY_MAX_MUSIC_VOLUME);
            setVolumeStepsPreference(prefScreen, KEY_MAX_CALL_VOLUME);
            setVolumeStepsPreference(prefScreen, KEY_MAX_ALARM_VOLUME);
        } else {
            prefScreen.removePreference(volumePanel);
        }
        */

        boolean mediaFocus = Settings.System.getIntForUser(getContext().getContentResolver(),
            Settings.System.MULTI_AUDIO_FOCUS_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        SystemSettingSwitchPreference mediaFocusPref = findPreference("multi_audio_focus_enabled");
        mediaFocusPref.setChecked(mediaFocus);
    }

    /*
    private void setVolumeStepsPreference(PreferenceScreen prefScreen, String key) {
        final int defaultValue = Settings.System.getIntForUser(mResolver, "default_" + key, 15,
                UserHandle.USER_CURRENT);
        final int value = Settings.System.getIntForUser(mResolver, key, defaultValue, UserHandle.USER_CURRENT);
        CustomSeekBarPreference pref = prefScreen.findPreference(key);
        pref.setDefaultValue(defaultValue);
        pref.setValue(value);
        pref.setOnPreferenceChangeListener(this);
    }
    */

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
        if (!(preference instanceof CustomSeekBarPreference)) {
            return false;
        }
        Settings.System.putIntForUser(mResolver, preference.getKey(), (Integer) newValue, UserHandle.USER_CURRENT);
        return true;
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
                    sir.xmlResId = R.xml.altair_settings_sound;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!DeviceUtils.hasVolumeKeys(context)) {
                        keys.add(KEY_VOLUME_PANEL_POSITION);
                        keys.add(KEY_SHOW_APP_VOLUME);
                    }

                    return keys;
                }
            };
}
