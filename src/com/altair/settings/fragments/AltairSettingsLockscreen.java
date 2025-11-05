/*
 * SPDX-FileCopyrightText: 2019-2025 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.DeviceUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.utils.ThemeUtils;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AltairSettingsLockscreen extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AltairSettingsLockscreen";

    private static final String LOCKSCREEN_GESTURES_CATEGORY = "lockscreen_gestures_category";
    private static final String KEY_LOCKSCREEN_FONT = ThemeUtils.LOCKSCREEN_FONT_KEY;
    private static final String KEY_FP_SUCCESS_VIBRATE = "fp_success_vibrate";
    private static final String KEY_FP_ERROR_VIBRATE = "fp_error_vibrate";
    private static final String KEY_RIPPLE_EFFECT = "enable_ripple_effect";

    private Context mContext;

    private ThemeUtils mThemeUtils;

    private Preference mClockFontPreference;
    private Preference mFingerprintVib;
    private Preference mFingerprintVibErr;
    private Preference mRippleEffect;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.altair_settings_lockscreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mThemeUtils = new ThemeUtils(mContext);

        PreferenceCategory gestCategory = findPreference(LOCKSCREEN_GESTURES_CATEGORY);

        mClockFontPreference = findPreference(KEY_LOCKSCREEN_FONT);
        updateSummary(mClockFontPreference, "android");

        mFingerprintVib = findPreference(KEY_FP_SUCCESS_VIBRATE);
        mFingerprintVibErr = findPreference(KEY_FP_ERROR_VIBRATE);
        mRippleEffect = findPreference(KEY_RIPPLE_EFFECT);

        boolean hasFingerprint = DeviceUtils.hasFingerprint(mContext);
        if (!hasFingerprint) {
            gestCategory.removePreference(mRippleEffect);
        }
        boolean hapticAvailable = DeviceUtils.hasVibrator(mContext);
        if (!hasFingerprint || !hapticAvailable) {
            gestCategory.removePreference(mFingerprintVib);
            gestCategory.removePreference(mFingerprintVibErr);
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
        String key = preference.getKey();
        switch (key) {
            case KEY_LOCKSCREEN_FONT:
                updateSummary(mClockFontPreference, "android");
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
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

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.altair_settings_lockscreen;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    boolean hasFingerprint = DeviceUtils.hasFingerprint(context);
                    if (!hasFingerprint) {
                        keys.add(KEY_RIPPLE_EFFECT);
                    }
                    boolean hapticAvailable = DeviceUtils.hasVibrator(context);
                    if (!hasFingerprint || !hapticAvailable) {
                        keys.add(KEY_FP_SUCCESS_VIBRATE);
                        keys.add(KEY_FP_ERROR_VIBRATE);
                    }

                    return keys;
                }
            };
}
