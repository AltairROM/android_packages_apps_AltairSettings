/*
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments;

import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.theme.MonetUtils;
import com.android.internal.util.theme.ThemeUtils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.darkmode.DarkModePreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.lang.CharSequence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AltairSettingsThemes extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AltairSettingsThemes";

    private static final String KEY_ACCENT_COLOR = "theme_accent_color";
    private static final String KEY_ENHANCED_COLORS = "theme_enhanced_colors";
    private static final String KEY_THEME_STYLE = "theme_color_style";
    private static final String KEY_THEME_DARK_UI_MODE = "theme_dark_ui_mode";
    private static final String KEY_THEME_ICON_SHAPE = ThemeUtils.ICON_SHAPE_KEY;
    private static final String KEY_THEME_NAVBAR_STYLE = ThemeUtils.NAVBAR_KEY;

    private Context mContext;
    private Resources mResources;

    private UiModeManager mUiModeManager;
    private ThemeUtils mThemeUtils;
    private MonetUtils mMonetUtils;

    private List<String> mAccentColorValues;
    private List<String> mAccentColorNames;
    private String mAccentColorValue;
    private String mThemeStyleValue;

    private Preference mAccentColorPreference;
    private SwitchPreferenceCompat mEnhancedColorsPreference;
    private ListPreference mThemeStylePreference;
    private DarkModePreference mDarkMode;
    private Preference mIconShapePreference;
    private Preference mNavbarStylePreference;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.altair_settings_themes;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mResources = getResources();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mUiModeManager = getContext().getSystemService(UiModeManager.class);
        mThemeUtils = new ThemeUtils(mContext);
        mMonetUtils = new MonetUtils(mContext);

        mAccentColorPreference = prefScreen.findPreference(KEY_ACCENT_COLOR);
        mAccentColorPreference.setOnPreferenceChangeListener(this);
        mAccentColorValues = Arrays.asList(mResources.getStringArray(
                R.array.theme_accent_color_values));
        mAccentColorNames = Arrays.asList(mResources.getStringArray(
                R.array.theme_accent_color_names));
        mAccentColorValue = mMonetUtils.getAccentColor();

        mEnhancedColorsPreference = prefScreen.findPreference(KEY_ENHANCED_COLORS);
        mEnhancedColorsPreference.setOnPreferenceChangeListener(this);

        mThemeStylePreference = prefScreen.findPreference(KEY_THEME_STYLE);
        mThemeStylePreference.setOnPreferenceChangeListener(this);
        mThemeStyleValue = mMonetUtils.getThemeStyle();
        updateThemeStyleValue();

        mDarkMode = findPreference(KEY_THEME_DARK_UI_MODE);
        mDarkMode.setOnPreferenceChangeListener(this);

        mIconShapePreference = prefScreen.findPreference(KEY_THEME_ICON_SHAPE);
        updateSummary(mIconShapePreference, "android");

        mNavbarStylePreference = prefScreen.findPreference(KEY_THEME_NAVBAR_STYLE);
        updateSummary(mNavbarStylePreference, "com.android.launcher3");

        updatePreferences();
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
        String key = preference.getKey();
        switch (key) {
            case KEY_ACCENT_COLOR:
                mAccentColorValue = mMonetUtils.getAccentColor();
                updateAccentColorSummary();
                break;
            case KEY_ENHANCED_COLORS:
                boolean enabled = (Boolean) newValue;
                mMonetUtils.setEnhancedColors(enabled);
                break;
            case KEY_THEME_STYLE:
                mThemeStyleValue = (String) newValue;
                mMonetUtils.setThemeStyle(mThemeStyleValue);
                updateThemeStyleValue();
                updateThemeStyleSummary();
                break;
            case KEY_THEME_DARK_UI_MODE:
                mUiModeManager.setNightModeActivated((boolean) newValue);
                break;
            case KEY_THEME_NAVBAR_STYLE:
                updateSummary(mNavbarStylePreference, "com.android.launcher3");
                break;
        }
        return true;
    }

    public void updateSummary(Preference preference, String target) {
        String currentPackageName = mThemeUtils.getOverlayInfos(preference.getKey(), target)
                .stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse(target);

        List<String> pkgs = mThemeUtils.getOverlayPackagesForCategory(preference.getKey(), target);
        List<String> labels = mThemeUtils.getLabels(preference.getKey(), target);

        preference.setSummary(target.equals(currentPackageName) ? "Default"
                : labels.get(pkgs.indexOf(currentPackageName)));
    }

    private void updateAccentColorSummary() {
        String summary = mResources.getString(R.string.wallpaper_color);
        final String color = "#" + mAccentColorValue;
        final int index = mAccentColorValues.indexOf(color.toLowerCase());
        if (index >= 0) {
            summary = mAccentColorNames.get(index);
        }
        mAccentColorPreference.setSummary(summary);
    }

    private void updateThemeStyleValue() {
        mThemeStylePreference.setValue(mThemeStyleValue);
    }

    private void updateThemeStyleSummary() {
        final int index = mThemeStylePreference.findIndexOfValue(mThemeStyleValue);
        mThemeStylePreference.setSummary(mThemeStylePreference.getEntries()[index].toString());
    }

    private void updatePreferences() {
        final boolean enhancedColors = mMonetUtils.isEnhancedColorsEnabled();
        mEnhancedColorsPreference.setChecked(enhancedColors);
        updateAccentColorSummary();
        updateThemeStyleSummary();
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.altair_settings_themes;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    return keys;
                }
            };
}
