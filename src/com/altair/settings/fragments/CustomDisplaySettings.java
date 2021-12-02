/*
 * Copyright (C) 2019-2021 Altair ROM Project
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
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.AmbientDisplayAlwaysOnPreferenceController;
import com.android.settings.display.AmbientDisplayNotificationsPreferenceController;
import com.android.settings.gestures.DoubleTapScreenPreferenceController;
import com.android.settings.gestures.PickupGesturePreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.development.SystemPropPoker;
import com.android.settingslib.search.SearchIndexable;
import com.lineage.support.preferences.GlobalSettingListPreference;

import java.util.Arrays;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class CustomDisplaySettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomDisplaySettings";

    public static final String KEY_SMART_PIXELS = "smart_pixels";
    public static final String KEY_ENABLE_BLURS_ON_WINDOWS = "enable_blurs_on_windows";

    static final String SUPPORTS_BACKGROUND_BLUR_SYSPROP = "ro.surface_flinger.supports_background_blur";
    static final String DISABLE_BLURS_SYSPROP = "persist.sys.sf.disable_blurs";
    private boolean mBlurSupported;

    private AmbientDisplayConfiguration mConfig;

    private boolean mEnableSmartPixels;

    private SwitchPreference mEnableBlursOnWindows;

    private ContentResolver mContentResolver;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_display_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentResolver = getActivity().getContentResolver();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        // Smart Pixels
        boolean enableSmartPixels = getContext().getResources().
                getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
        Preference SmartPixels = findPreference(KEY_SMART_PIXELS);
        if (!enableSmartPixels) {
            prefScreen.removePreference(SmartPixels);
        }

        // Blur
        mBlurSupported = SystemProperties.getBoolean(SUPPORTS_BACKGROUND_BLUR_SYSPROP, false);
        mEnableBlursOnWindows = findPreference(KEY_ENABLE_BLURS_ON_WINDOWS);
        if (mBlurSupported) {
            boolean isEnabled = !SystemProperties.getBoolean(
                    DISABLE_BLURS_SYSPROP, false /* default */);
            mEnableBlursOnWindows.setChecked(isEnabled);
            mEnableBlursOnWindows.setOnPreferenceChangeListener(this);
        } else {
            prefScreen.removePreference(mEnableBlursOnWindows);
        }
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(AmbientDisplayAlwaysOnPreferenceController.class).setConfig(getConfig(context));
        use(AmbientDisplayNotificationsPreferenceController.class).setConfig(getConfig(context));
        use(DoubleTapScreenPreferenceController.class).setConfig(getConfig(context));
        use(PickupGesturePreferenceController.class).setConfig(getConfig(context));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEnableBlursOnWindows) {
            final boolean isDisabled = !(Boolean) newValue;
            SystemProperties.set(DISABLE_BLURS_SYSPROP, isDisabled ? "1" : "0");
            SystemPropPoker.getInstance().poke();
            return true;
        }
        return false;
    }

    private AmbientDisplayConfiguration getConfig(Context context) {
        if (mConfig == null) {
            mConfig = new AmbientDisplayConfiguration(context);
        }
        return mConfig;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_display_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    boolean enableSmartPixels = context.getResources().
                            getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
                    if (!enableSmartPixels) {
                        keys.add(KEY_SMART_PIXELS);
                    }

                    return keys;
                }
            };
}

