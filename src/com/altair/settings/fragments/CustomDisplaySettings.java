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
import com.android.settingslib.search.SearchIndexable;
import com.lineage.support.preferences.GlobalSettingListPreference;

import java.util.Arrays;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class CustomDisplaySettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomDisplaySettings";

    public static final String KEY_SMART_PIXELS = "smart_pixels";

    private AmbientDisplayConfiguration mConfig;

    private boolean mEnableSmartPixels;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_display_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        // Smart Pixels
        boolean enableSmartPixels = getContext().getResources().
                getBoolean(com.android.internal.R.bool.config_supportSmartPixels);
        Preference SmartPixels = findPreference(KEY_SMART_PIXELS);
        if (!enableSmartPixels) {
            prefScreen.removePreference(SmartPixels);
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

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
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
                            getBoolean(com.android.internal.R.bool.config_supportSmartPixels);
                    if (!enableSmartPixels) {
                        keys.add(KEY_SMART_PIXELS);
                    }

                    return keys;
                }
            };
}

