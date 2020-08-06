/*
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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.SystemUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.lineage.support.preferences.GlobalSettingListPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class DisplayExtraSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "DisplayExtra";

    private static final String KEY_REFRESH_RATE_SETTING = "refresh_rate_setting";

    private GlobalSettingListPreference mVariableRefreshRate;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.display_extra_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mVariableRefreshRate = prefScreen.findPreference(KEY_REFRESH_RATE_SETTING);
        boolean hasVariableRefreshRate =
            getContext().getResources().getBoolean(com.android.internal.R.bool.config_hasVariableRefreshRate);

        if (!hasVariableRefreshRate) {
            prefScreen.removePreference(mVariableRefreshRate);
        } else {
            int defVarRateSetting = getContext().getResources().getInteger(
                 com.android.internal.R.integer.config_defaultVariableRefreshRateSetting);
            int mVarRateSetting = Settings.Global.getInt(getContext().getContentResolver(),
                 Settings.Global.REFRESH_RATE_SETTING, defVarRateSetting);
            mVariableRefreshRate.setValue(String.valueOf(mVarRateSetting));
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

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.display_extra_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    boolean hasVariableRefreshRate =
                        context.getResources().getBoolean(com.android.internal.R.bool.config_hasVariableRefreshRate);
                    if (!hasVariableRefreshRate) {
                        keys.add(KEY_REFRESH_RATE_SETTING);
                    }

                    return keys;
                }
            };
}

