/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import com.altair.settings.display.OverlayCategoryPreferenceController;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.darkmode.DarkModePreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import com.lineage.support.colorpicker.ColorPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class CustomThemeSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "CustomThemeSettings";

    private static final String KEY_THEME_DARK_UI_MODE = "theme_dark_ui_mode";
    private static final String KEY_THEME_ACCENT_COLOR = "theme_accent_color";

    private static final int DEFAULT_ACCENT_COLOR = 0xff008577; // material_deep_teal_500

    private ContentResolver mResolver;
    private UiModeManager mUiModeManager;

    ColorPreference mThemeAccentColor;
    DarkModePreference mDarkMode;

    private int mAccentColor;
    private int[] mAccentColorValues;
    private String[] mAccentColorNames;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_theme_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolver = getActivity().getContentResolver();
        mUiModeManager = getContext().getSystemService(UiModeManager.class);

        mDarkMode = findPreference(KEY_THEME_DARK_UI_MODE);
        mDarkMode.setOnPreferenceChangeListener(this);

        mAccentColor = Settings.System.getIntForUser(mResolver,
                Settings.System.ACCENT_COLOR, DEFAULT_ACCENT_COLOR, UserHandle.USER_CURRENT);
        mAccentColorValues = getResources().getIntArray(R.array.accent_color_picker_values);
        mAccentColorNames = getResources().getStringArray(R.array.accent_color_picker_names);

        mThemeAccentColor = findPreference(KEY_THEME_ACCENT_COLOR);
        mThemeAccentColor.saveValue(mAccentColor);
        mThemeAccentColor.setOnPreferenceChangeListener(this);
        setAccentColorSummary();

        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.theme_substratum_warning);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
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
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.primary_color"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.font"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.icon_pack.android"));
        return controllers;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDarkMode) {
            final boolean enabled = (boolean) newValue;
            mUiModeManager.setNightModeActivated(enabled);
            return true;
        } else if (preference == mThemeAccentColor) {
            mAccentColor = (int) newValue;
            Settings.System.putIntForUser(mResolver,
                    Settings.System.ACCENT_COLOR, mAccentColor, UserHandle.USER_CURRENT);
            setAccentColorSummary();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    private void setAccentColorSummary() {
        for (int i = 0; i < mAccentColorValues.length; i++) {
            if (mAccentColorValues[i] == mAccentColor) {
                mThemeAccentColor.setSummary(mAccentColorNames[i]);
                return;
            }
        }
        String hexColor = String.format("#%08X", mAccentColor);
        mThemeAccentColor.setSummary(hexColor);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_theme_settings;
                    result.add(sir);

                    return result;
                }
            };
}

