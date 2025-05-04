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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

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
    private static final String KEY_SCREEN_OFF_UDFPS_ENABLED = "screen_off_udfps_enabled";
    private static final String KEY_FP_SUCCESS_VIBRATE = "fp_success_vibrate";
    private static final String KEY_FP_ERROR_VIBRATE = "fp_error_vibrate";
    private static final String KEY_RIPPLE_EFFECT = "enable_ripple_effect";

    private Context mContext;

    private ThemeUtils mThemeUtils;

    private Preference mClockFontPreference;
    private Preference mScreenOffUdfps;
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

        FingerprintManager mFingerprintManager = (FingerprintManager)
                getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        mFingerprintVib = findPreference(KEY_FP_SUCCESS_VIBRATE);
        mFingerprintVibErr = findPreference(KEY_FP_ERROR_VIBRATE);
        mRippleEffect = findPreference(KEY_RIPPLE_EFFECT);
        mScreenOffUdfps = findPreference(KEY_SCREEN_OFF_UDFPS_ENABLED);

        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
            gestCategory.removePreference(mScreenOffUdfps);
            gestCategory.removePreference(mFingerprintVib);
            gestCategory.removePreference(mFingerprintVibErr);
            gestCategory.removePreference(mRippleEffect);
        } else {
            Resources resources = getResources();
            boolean screenOffUdfpsAvailable = resources.getBoolean(
                    com.android.internal.R.bool.config_supportScreenOffUdfps) ||
                    !TextUtils.isEmpty(resources.getString(
                        com.android.internal.R.string.config_dozeUdfpsLongPressSensorType));
            if (!screenOffUdfpsAvailable) {
                gestCategory.removePreference(mScreenOffUdfps);
            }
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

                    FingerprintManager mFingerprintManager = (FingerprintManager)
                            context.getSystemService(Context.FINGERPRINT_SERVICE);
                    if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
                        keys.add(KEY_FP_SUCCESS_VIBRATE);
                        keys.add(KEY_FP_ERROR_VIBRATE);
                        keys.add(KEY_RIPPLE_EFFECT);
                    } else {
                        Resources resources = context.getResources();
                        boolean screenOffUdfpsAvailable = resources.getBoolean(
                            com.android.internal.R.bool.config_supportScreenOffUdfps) ||
                            !TextUtils.isEmpty(resources.getString(
                                com.android.internal.R.string.config_dozeUdfpsLongPressSensorType));
                        if (!screenOffUdfpsAvailable) {
                            keys.add(KEY_SCREEN_OFF_UDFPS_ENABLED);
                        }
                    }

                    return keys;
                }
            };
}
