/*
 * Copyright (C) 2016 The CyanogenMod project
 * Copyright (C) 2017 The LineageOS project
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
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.lineage.support.preferences.CustomSeekBarPreference;
import com.lineage.support.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

import lineageos.preference.LineageSystemSettingListPreference;

@SearchIndexable
public class CustomQSSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "CustomQSSettings";

    private static final String QS_QUICK_PULLDOWN = "qs_quick_pulldown";
    private static final String QS_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String QS_ROWS_LANDSCAPE = "qs_rows_landscape";
    private static final String QS_COLUMNS_PORTRAIT = "qs_columns_portrait";
    private static final String QS_COLUMNS_LANDSCAPE = "qs_columns_landscape";
    private static final String QS_COLUMNS_QUICKBAR = "qs_columns_quickbar";
    private static final String QS_COLUMNS_QUICKBAR_AUTO = "qs_columns_quickbar_auto";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private static final int DEFAULT_QS_ROWS_PORTRAIT = 3;
    private static final int DEFAULT_QS_ROWS_LANDSCAPE = 2;
    private static final int DEFAULT_QS_COLUMNS_PORTRAIT = 4;
    private static final int DEFAULT_QS_COLUMNS_LANDSCAPE = 4;
    private static final int DEFAULT_QS_QUICKBAR_COLUMNS = 6;

    private LineageSystemSettingListPreference mQuickPulldown;
    private CustomSeekBarPreference mQsRowsPortrait;
    private CustomSeekBarPreference mQsRowsLandscape;
    private CustomSeekBarPreference mQsColumnsPortrait;
    private CustomSeekBarPreference mQsColumnsLandscape;
    private SystemSettingSwitchPreference mQsQuickBarAuto;
    private CustomSeekBarPreference mQsQuickBarColumns;

    private ContentResolver mContentResolver;

    private int mQuickBarColumns = DEFAULT_QS_QUICKBAR_COLUMNS;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_qs_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentResolver = getActivity().getContentResolver();

        mQuickPulldown = findPreference(QS_QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        int value = Settings.System.getIntForUser(mContentResolver,
                Settings.System.QS_LAYOUT_ROWS, DEFAULT_QS_ROWS_PORTRAIT,
                UserHandle.USER_CURRENT);
        mQsRowsPortrait = findPreference(QS_ROWS_PORTRAIT);
        mQsRowsPortrait.setValue(value);
        mQsRowsPortrait.setOnPreferenceChangeListener(this);

        value = Settings.System.getIntForUser(mContentResolver,
                Settings.System.QS_LAYOUT_ROWS_LANDSCAPE, DEFAULT_QS_ROWS_LANDSCAPE,
                UserHandle.USER_CURRENT);
        mQsRowsLandscape = findPreference(QS_ROWS_LANDSCAPE);
        mQsRowsLandscape.setValue(value);
        mQsRowsLandscape.setOnPreferenceChangeListener(this);

        value = Settings.System.getIntForUser(mContentResolver,
                Settings.System.QS_LAYOUT_COLUMNS, DEFAULT_QS_COLUMNS_PORTRAIT,
                UserHandle.USER_CURRENT);
        mQsColumnsPortrait = findPreference(QS_COLUMNS_PORTRAIT);
        mQsColumnsPortrait.setValue(value);
        mQsColumnsPortrait.setOnPreferenceChangeListener(this);

        value = Settings.System.getIntForUser(mContentResolver,
                Settings.System.QS_LAYOUT_COLUMNS_LANDSCAPE, DEFAULT_QS_COLUMNS_LANDSCAPE,
                UserHandle.USER_CURRENT);
        mQsColumnsLandscape = findPreference(QS_COLUMNS_LANDSCAPE);
        mQsColumnsLandscape.setValue(value);
        mQsColumnsLandscape.setOnPreferenceChangeListener(this);

        mQsQuickBarAuto = (SystemSettingSwitchPreference) findPreference(QS_COLUMNS_QUICKBAR_AUTO);
        mQsQuickBarColumns = findPreference(QS_COLUMNS_QUICKBAR);
        value = Settings.System.getIntForUser(mContentResolver,
                Settings.System.QS_QUICKBAR_COLUMNS, mQuickBarColumns,
                UserHandle.USER_CURRENT);
        mQsQuickBarAuto.setChecked(value == -1);
        if (value != -1) {
            mQuickBarColumns = value;
        }
        mQsQuickBarColumns.setValue(mQuickBarColumns);
        mQsQuickBarAuto.setOnPreferenceChangeListener(this);
        mQsQuickBarColumns.setOnPreferenceChangeListener(this);

        updateQuickSettingsPreferences(value == -1);
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

        // Adjust QS panel preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.qs_quick_pulldown_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.qs_quick_pulldown_values_rtl);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateQuickSettingsPreferences(boolean auto) {
        mQsQuickBarAuto.setChecked(auto);
        mQsQuickBarColumns.setEnabled(!auto);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQuickPulldown) {
            updateQuickPulldownSummary(Integer.parseInt((String) newValue));
        } else if (preference == mQsRowsPortrait) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(mContentResolver,
                    Settings.System.QS_LAYOUT_ROWS, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsRowsLandscape) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(mContentResolver,
                    Settings.System.QS_LAYOUT_ROWS_LANDSCAPE, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsPortrait) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(mContentResolver,
                    Settings.System.QS_LAYOUT_COLUMNS, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsLandscape) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(mContentResolver,
                    Settings.System.QS_LAYOUT_COLUMNS_LANDSCAPE, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsQuickBarColumns) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(mContentResolver,
                    Settings.System.QS_QUICKBAR_COLUMNS, val, UserHandle.USER_CURRENT);
            mQuickBarColumns = val;
            return true;
        } else if (preference == mQsQuickBarAuto) {
            boolean enabled = (Boolean) newValue;
            int val = enabled ? -1 : mQuickBarColumns;
            Settings.System.putIntForUser(mContentResolver,
                    Settings.System.QS_QUICKBAR_COLUMNS, val, UserHandle.USER_CURRENT);
            updateQuickSettingsPreferences(enabled);
            return true;
        }
        return true;
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
        }
        mQuickPulldown.setSummary(summary);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_qs_settings;
                    result.add(sir);

                    return result;
                }
            };
}

