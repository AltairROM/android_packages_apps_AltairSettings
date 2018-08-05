/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.altair.settings.preference.ButtonBacklightBrightness;
import com.altair.settings.preference.CustomSeekBarPreference;
import com.altair.settings.utils.DeviceUtils;
import com.altair.settings.utils.TelephonyUtils;

import org.lineageos.internal.util.ScreenType;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

import java.util.List;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

import com.android.internal.utils.du.DUActionUtils;
import com.android.settings.R;

public class NavigationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "Navigation";

    private static final String KEY_NAVBAR_VISIBILITY = "navbar_visibility";
    private static final String KEY_NAVBAR_MODE = "navbar_mode";

    private static final String KEY_SMARTBAR_SETTINGS = "smartbar_settings";
    private static final String KEY_FLING_SETTINGS = "fling_settings";

    private static final String KEY_STOCK_NAVBAR_SETTINGS = "navigation_bar";
    private static final String KEY_NAVIGATION_BAR_MENU_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    private static final String KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press";

    private static final String KEY_CATEGORY_NAVBAR_GENERAL = "category_navbar_general";
    private static final String KEY_NAVBAR_HEIGHT_PORT = "navbar_height_portrait";
    private static final String KEY_NAVBAR_HEIGHT_LAND = "navbar_height_landscape";
    private static final String KEY_NAVBAR_WIDTH = "navbar_width";
    private static final String KEY_PULSE_SETTINGS = "pulse_settings";

    private static final int NAVBAR_MODE_STOCK = 0;
    private static final int NAVBAR_MODE_SMARTBAR = 1;
    private static final int NAVBAR_MODE_FLING = 2;

    private SwitchPreference mNavbarVisibility;
    private ListPreference mNavbarMode;
    private PreferenceScreen mFlingSettings;
    private PreferenceScreen mSmartbarSettings;
    private PreferenceScreen mStockNavbarSettings;
    private SwitchPreference mNavigationBarMenuArrowKeys;
    private ListPreference mNavigationHomeLongPressAction;
    private ListPreference mNavigationHomeDoubleTapAction;
    private ListPreference mNavigationAppSwitchLongPressAction;

    private PreferenceCategory mCategoryNavbarGeneral;
    private CustomSeekBarPreference mBarHeightPort;
    private CustomSeekBarPreference mBarHeightLand;
    private CustomSeekBarPreference mBarWidth;
    private PreferenceScreen mPulseSettings;

    private boolean mHasHardwareNavbar = false;
    private boolean mIsNavSwitchingMode = false;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tab_navigation);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mHasHardwareNavbar = !DUActionUtils.hasNavbarByDefault(getActivity());

        mNavbarVisibility = (SwitchPreference) findPreference(KEY_NAVBAR_VISIBILITY);
        if (!mHasHardwareNavbar) {
            prefScreen.removePreference(mNavbarVisibility);
        } else {
            mNavbarVisibility.setChecked(isNavbarVisible());
            mNavbarVisibility.setOnPreferenceChangeListener(this);
        }
        mNavbarMode = (ListPreference) findPreference(KEY_NAVBAR_MODE);

        mSmartbarSettings = (PreferenceScreen) findPreference(KEY_SMARTBAR_SETTINGS);
        mFlingSettings = (PreferenceScreen) findPreference(KEY_FLING_SETTINGS);
        mStockNavbarSettings = (PreferenceScreen) findPreference(KEY_STOCK_NAVBAR_SETTINGS);
        mNavigationBarMenuArrowKeys = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_MENU_ARROW_KEYS);

        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));

        Action homeLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);

        mNavigationHomeLongPressAction = initList(KEY_NAVIGATION_HOME_LONG_PRESS,
                homeLongPressAction);
        mNavigationHomeDoubleTapAction = initList(KEY_NAVIGATION_HOME_DOUBLE_TAP,
                homeDoubleTapAction);
        mNavigationAppSwitchLongPressAction = initList(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS,
                appSwitchLongPressAction);

        mCategoryNavbarGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVBAR_GENERAL);

        int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_MODE,
                0);
        mNavbarMode.setValue(String.valueOf(mode));
        mNavbarMode.setOnPreferenceChangeListener(this);

        int size = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_HEIGHT, 100, UserHandle.USER_CURRENT);
        mBarHeightPort = (CustomSeekBarPreference) findPreference(KEY_NAVBAR_HEIGHT_PORT);
        mBarHeightPort.setValue(size);
        mBarHeightPort.setOnPreferenceChangeListener(this);

        final boolean canMove = DUActionUtils.navigationBarCanMove();
        if (canMove) {
            mCategoryNavbarGeneral.removePreference(findPreference(KEY_NAVBAR_HEIGHT_LAND));
            size = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_WIDTH, 100, UserHandle.USER_CURRENT);
            mBarWidth = (CustomSeekBarPreference) findPreference(KEY_NAVBAR_WIDTH);
            mBarWidth.setValue(size);
            mBarWidth.setOnPreferenceChangeListener(this);
        } else {
            mCategoryNavbarGeneral.removePreference(findPreference(KEY_NAVBAR_WIDTH));
            size = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT_LANDSCAPE, 100, UserHandle.USER_CURRENT);
            mBarHeightLand = (CustomSeekBarPreference) findPreference(KEY_NAVBAR_HEIGHT_LAND);
            mBarHeightLand.setValue(size);
            mBarHeightLand.setOnPreferenceChangeListener(this);
        }

        mPulseSettings = (PreferenceScreen) findPreference(KEY_PULSE_SETTINGS);

        // Override key actions on Go devices in order to hide any unsupported features
        if (ActivityManager.isLowRamDeviceStatic()) {
            String[] actionEntriesGo = res.getStringArray(R.array.hardware_keys_action_entries_go);
            String[] actionValuesGo = res.getStringArray(R.array.hardware_keys_action_values_go);

            mNavigationHomeLongPressAction.setEntries(actionEntriesGo);
            mNavigationHomeLongPressAction.setEntryValues(actionValuesGo);

            mNavigationHomeDoubleTapAction.setEntries(actionEntriesGo);
            mNavigationHomeDoubleTapAction.setEntryValues(actionValuesGo);

            mNavigationAppSwitchLongPressAction.setEntries(actionEntriesGo);
            mNavigationAppSwitchLongPressAction.setEntryValues(actionValuesGo);
        }

        updateNavbarPreferences();

        mHandler = new Handler();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private boolean isNavbarVisible() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_VISIBLE,
                !mHasHardwareNavbar ? 1 : 0) != 0;
    }

    private void updateNavbarPreferences() {
        boolean enabled = isNavbarVisible();
        int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_MODE,
                0);
        if (mHasHardwareNavbar && !enabled) {
            mode = NAVBAR_MODE_STOCK;
        }

        if (!mHasHardwareNavbar) {
            mNavbarVisibility.setEnabled(!mIsNavSwitchingMode);
        }
        mNavbarMode.setEnabled(enabled && !mIsNavSwitchingMode);

        mSmartbarSettings.setEnabled(enabled && (mode == NAVBAR_MODE_SMARTBAR) && !mIsNavSwitchingMode);
        mFlingSettings.setEnabled(enabled && (mode == NAVBAR_MODE_FLING) && !mIsNavSwitchingMode);

        mStockNavbarSettings.setEnabled(enabled && (mode == NAVBAR_MODE_STOCK) && !mIsNavSwitchingMode);
        mNavigationBarMenuArrowKeys.setEnabled(enabled && (mode == NAVBAR_MODE_STOCK) && !mIsNavSwitchingMode);
        mNavigationHomeLongPressAction.setEnabled((mode == NAVBAR_MODE_STOCK) && !mIsNavSwitchingMode);
        mNavigationHomeDoubleTapAction.setEnabled((mode == NAVBAR_MODE_STOCK) && !mIsNavSwitchingMode);
        mNavigationAppSwitchLongPressAction.setEnabled((mode == NAVBAR_MODE_STOCK) && !mIsNavSwitchingMode);

        mBarHeightPort.setEnabled(enabled && !mIsNavSwitchingMode);
        if (mBarHeightLand != null) {
            mBarHeightLand.setEnabled(enabled && !mIsNavSwitchingMode);
        }
        if (mBarWidth != null) {
            mBarWidth.setEnabled(enabled && !mIsNavSwitchingMode);
        }
        mPulseSettings.setEnabled(enabled && !mIsNavSwitchingMode);
    }

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        LineageSettings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mNavigationHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mNavigationAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference.equals(mNavbarMode)) {
            int mode = Integer.parseInt(((String) newValue).toString());
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_MODE, mode);
            mNavbarMode.setValue(String.valueOf(mode));
            updateNavbarPreferences();
            return true;
        } else if (preference.equals(mNavbarVisibility)) {
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            updateNavbarPreferences();
            boolean showing = ((Boolean)newValue);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_VISIBLE,
                    showing ? 1 : 0);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                    updateNavbarPreferences();
                }
            }, 500);
            return true;
        } else if (preference == mBarHeightPort) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBarHeightLand) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT_LANDSCAPE, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBarWidth) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_WIDTH, val, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
}
