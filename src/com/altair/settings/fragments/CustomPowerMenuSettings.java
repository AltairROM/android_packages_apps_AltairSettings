/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.altair.settings.fragments.ButtonBacklightBrightness;
import com.altair.settings.utils.DeviceUtils;
import com.altair.settings.utils.TelephonyUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.widget.LockPatternUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;

import com.lineage.support.preferences.CustomSeekBarPreference;
import com.lineage.support.preferences.SecureSettingSwitchPreference;

import java.util.Arrays;
import java.util.List;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

public class CustomPowerMenuSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomPowerMenuSettings";

    private static final String KEY_POWER_MENU_BACKGROUND_ALPHA = "power_menu_background_alpha";
    private static final String KEY_GLOBAL_ACTIONS_PANEL_ENABLED = "global_actions_panel_enabled";
    private static final String KEY_DEVICE_CONTROLS_ENABLED = "controls_enabled";
    private static final String KEY_POWER_MENU_LOCKED_SHOW_CONTENT = "power_menu_locked_show_content";

    private static final String PREF_GLOBAL_ACTIONS_PANEL_AVAILABLE =
            Settings.Secure.GLOBAL_ACTIONS_PANEL_AVAILABLE;
    private static final String PREF_GLOBAL_ACTIONS_PANEL_ENABLED =
            Settings.Secure.GLOBAL_ACTIONS_PANEL_ENABLED;
    private static final String PREF_DEVICE_CONTROLS_ENABLED =
            Settings.Secure.CONTROLS_ENABLED;
    private static final String PREF_POWER_MENU_LOCKED_SHOW_CONTENT =
            Settings.Secure.POWER_MENU_LOCKED_SHOW_CONTENT;

    private CustomSeekBarPreference mPowerMenuBackgroundAlpha;
    private SecureSettingSwitchPreference mGlobalActionsPanelEnabled;
    private SecureSettingSwitchPreference mDeviceControlsEnabled;
    private SecureSettingSwitchPreference mPowerMenuLockedShowContent;

    private Context mContext;
    private Handler mHandler;
    private ContentResolver mContentResolver;

    private boolean mCardsAvailable;
    private boolean mControlsAvailable;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.custom_power_menu_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mHandler = new Handler();
        mContentResolver = getActivity().getContentResolver();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mCardsAvailable = getSecurePrefInt(PREF_GLOBAL_ACTIONS_PANEL_AVAILABLE, 0) != 0;
        mControlsAvailable = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONTROLS);

        // Power menu background transparency
        mPowerMenuBackgroundAlpha = findPreference(KEY_POWER_MENU_BACKGROUND_ALPHA);
        int alphaValue = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.POWER_MENU_BACKGROUND_ALPHA, 50, UserHandle.USER_CURRENT);
        mPowerMenuBackgroundAlpha.setValue(100 - alphaValue);
        mPowerMenuBackgroundAlpha.setOnPreferenceChangeListener(this);

        // Global actions panel
        mGlobalActionsPanelEnabled = findPreference(KEY_GLOBAL_ACTIONS_PANEL_ENABLED);
        if (mCardsAvailable) {
            boolean enabled = getSecurePrefInt(PREF_GLOBAL_ACTIONS_PANEL_ENABLED, 0) != 0;
            mGlobalActionsPanelEnabled.setChecked(enabled);
            mGlobalActionsPanelEnabled.setOnPreferenceChangeListener(this);
        } else {
            prefScreen.removePreference(mGlobalActionsPanelEnabled);
        }

        // Device controls
        mDeviceControlsEnabled = findPreference(KEY_DEVICE_CONTROLS_ENABLED);
        if (mControlsAvailable) {
            boolean enabled = getSecurePrefInt(PREF_DEVICE_CONTROLS_ENABLED, 1) != 0;
            mDeviceControlsEnabled.setChecked(enabled);
            mDeviceControlsEnabled.setOnPreferenceChangeListener(this);
        } else {
            prefScreen.removePreference(mDeviceControlsEnabled);
        }

        // Sensitive content
        mPowerMenuLockedShowContent = findPreference(KEY_POWER_MENU_LOCKED_SHOW_CONTENT);
        boolean enabled = getSecurePrefInt(PREF_POWER_MENU_LOCKED_SHOW_CONTENT, 0) != 0;
        mPowerMenuLockedShowContent.setChecked(enabled);
        updatePowerMenuLockedPreference();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private int getSecurePrefInt(String key, int defaultValue) {
        return Settings.Secure.getInt(mContentResolver, key, defaultValue);
    }

    private void putSecurePrefInt(String key, int newValue) {
        Settings.Secure.putInt(mContentResolver, key, newValue);
    }

    private boolean isEnabled() {
        boolean cardsEnabled = getSecurePrefInt(PREF_GLOBAL_ACTIONS_PANEL_ENABLED, 0) != 0;
        boolean controlsEnabled = getSecurePrefInt(PREF_DEVICE_CONTROLS_ENABLED, 1) != 0;
        return (mCardsAvailable && cardsEnabled) || (mControlsAvailable && controlsEnabled);
    }

    private boolean isSecure() {
        final LockPatternUtils utils = FeatureFactory.getFactory(mContext)
                .getSecurityFeatureProvider()
                .getLockPatternUtils(mContext);
        int userId = UserHandle.myUserId();
        return utils.isSecure(userId);
    }

    private CharSequence getSummary() {
        final int res;
        if (!isSecure()) {
            res = R.string.power_menu_privacy_not_secure;
        } else if (mCardsAvailable && mControlsAvailable) {
            res = R.string.power_menu_privacy_show;
        } else if (!mCardsAvailable && mControlsAvailable) {
            res = R.string.power_menu_privacy_show_controls;
        } else if (mCardsAvailable) {
            res = R.string.power_menu_privacy_show_cards;
        } else {
            // In this case, neither cards nor controls are available. This preference should not
            // be accessible as the power menu setting is not accessible
            return "";
        }
        return mContext.getText(res);
    }

    private void updatePowerMenuLockedPreference() {
        mPowerMenuLockedShowContent.setSummary(getSummary());
        mPowerMenuLockedShowContent.setEnabled(isEnabled() && isSecure());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPowerMenuBackgroundAlpha) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(mContentResolver,
                    Settings.System.POWER_MENU_BACKGROUND_ALPHA, 100 - val,
                        UserHandle.USER_CURRENT);
            return true;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if ((preference == mGlobalActionsPanelEnabled) ||
            (preference == mDeviceControlsEnabled)) {
            updatePowerMenuLockedPreference();
        }

        return super.onPreferenceTreeClick(preference);
    }
}

