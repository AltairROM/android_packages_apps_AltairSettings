/*
 * SPDX-FileCopyrightText: 2019-2026 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments;

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.altair.settings.utils.DeviceUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import static com.android.systemui.shared.recents.utilities.Utilities.isLargeScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AltairSettingsNavigation extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AltairSettingsNavigation";

    private static final String KEY_DISABLE_NAV_KEYS = "disable_nav_keys";

    private static final String KEY_ENABLE_TASKBAR = "enable_taskbar";
    private static final String KEY_NAVBAR_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    private static final String KEY_NAVBAR_KEY_ORDER = "navigationbar_key_order";
    private static final String KEY_NAVBAR_LAYOUT_MODE = "navbar_layout_mode";

    private static final String KEY_BACK_LONG_PRESS = "navigation_back_long_press";
    private static final String KEY_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "navigation_app_switch_long_press";
    private static final String KEY_APP_SWITCH_DOUBLE_TAP = "navigation_app_switch_double_tap";
    private static final String KEY_EDGE_LONG_SWIPE = "navigation_bar_edge_long_swipe";

    private static final String CATEGORY_NAVBAR_OPTIONS = "navigation_bar_options_category";
    private static final String CATEGORY_NAVBAR_ACTIONS = "navigation_bar_actions_category";

    private Context mContext;
    private Handler mHandler;
    private ContentResolver mResolver;

    private SwitchPreferenceCompat mDisableNavigationKeys;

    private SwitchPreferenceCompat mEnableTaskbar;
    private SwitchPreferenceCompat mNavbarArrowKeys;
    private SwitchPreferenceCompat mNavbarKeyOrder;
    private ListPreference mNavbarLayout;

    private ListPreference mBackLongPressAction;
    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mAppSwitchLongPressAction;
    private ListPreference mAppSwitchDoubleTapAction;
    private ListPreference mEdgeLongSwipeAction;

    private PreferenceCategory mNavigationOptionsCategory;
    private PreferenceCategory mNavigationActionsCategory;

    private LineageHardwareManager mHardware;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.altair_settings_navigation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
        mResolver = getActivity().getContentResolver();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mHardware = LineageHardwareManager.getInstance(getActivity());

        // Force Navigation bar related options
        mDisableNavigationKeys = findPreference(KEY_DISABLE_NAV_KEYS);

        mNavigationOptionsCategory = findPreference(CATEGORY_NAVBAR_OPTIONS);
        mNavigationActionsCategory = findPreference(CATEGORY_NAVBAR_ACTIONS);

        Action defaultBackLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnBackBehavior));
        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));
        Action defaultAppSwitchDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnAppSwitchBehavior));
        Action backLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                defaultBackLongPressAction);
        Action homeLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);
        Action appSwitchDoubleTapAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                defaultAppSwitchDoubleTapAction);
        Action edgeLongSwipeAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION,
                Action.NOTHING);

        // Navigation bar arrow keys while typing
        mNavbarArrowKeys = findPreference(KEY_NAVBAR_ARROW_KEYS);

        // Navigation bar key order
        mNavbarKeyOrder = findPreference(KEY_NAVBAR_KEY_ORDER);

        // Navigation bar layout mode
        mNavbarLayout = findPreference(KEY_NAVBAR_LAYOUT_MODE);

        // Navigation bar back long press
        mBackLongPressAction = initList(KEY_BACK_LONG_PRESS,
                backLongPressAction);

        // Navigation bar home long press
        mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS,
                homeLongPressAction);

        // Navigation bar home double tap
        mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP,
                homeDoubleTapAction);

        // Navigation bar app switch long press
        mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS,
                appSwitchLongPressAction);

        // Navigation bar app switch double tap
        mAppSwitchDoubleTapAction = initList(KEY_APP_SWITCH_DOUBLE_TAP,
                appSwitchDoubleTapAction);

        // Edge long swipe gesture
        mEdgeLongSwipeAction = initList(KEY_EDGE_LONG_SWIPE, edgeLongSwipeAction);

        // Hardware key disabler
        if (isKeyDisablerSupported(getActivity())) {
            // Remove keys that can be provided by the navbar
            updateDisableNavkeysOption();
            enableNavigationCategories(mDisableNavigationKeys.isChecked());
            mDisableNavigationKeys.setDisableDependentsState(true);
        } else {
            prefScreen.removePreference(mDisableNavigationKeys);
        }
        updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked(), /* force */ true);

        // Only show the navigation bar category on devices that have a navigation bar
        // or support disabling the hardware keys
        if (!hasNavigationBar() && !isKeyDisablerSupported(getActivity())) {
            enableNavigationCategories(false);
        }

        mEnableTaskbar = findPreference(KEY_ENABLE_TASKBAR);
        if (mEnableTaskbar != null) {
            if (!isLargeScreen(getContext()) || !hasNavigationBar()) {
                mNavigationActionsCategory.removePreference(mEnableTaskbar);
            } else {
                mEnableTaskbar.setOnPreferenceChangeListener(this);
                mEnableTaskbar.setChecked(LineageSettings.System.getInt(mResolver,
                        LineageSettings.System.ENABLE_TASKBAR,
                        isLargeScreen(getContext()) ? 1 : 0) == 1);
                toggleTaskBarDependencies(mEnableTaskbar.isChecked());
            }
        }

        List<Integer> unsupportedValues = new ArrayList<>();
        List<String> entries = new ArrayList<>(
                Arrays.asList(res.getStringArray(R.array.navbar_key_action_entries)));
        List<String> values = new ArrayList<>(
                Arrays.asList(res.getStringArray(R.array.navbar_key_action_values)));

        // hide split screen option unconditionally - it doesn't work at the moment
        // once someone gets it working again: hide it only for low-ram devices
        // (check ActivityManager.isLowRamDeviceStatic())
        unsupportedValues.add(Action.SPLIT_SCREEN.ordinal());

        for (int unsupportedValue: unsupportedValues) {
            entries.remove(unsupportedValue);
            values.remove(unsupportedValue);
        }

        String[] actionEntries = entries.toArray(new String[0]);
        String[] actionValues = values.toArray(new String[0]);

        mBackLongPressAction.setEntries(actionEntries);
        mBackLongPressAction.setEntryValues(actionValues);

        mHomeLongPressAction.setEntries(actionEntries);
        mHomeLongPressAction.setEntryValues(actionValues);

        mHomeDoubleTapAction.setEntries(actionEntries);
        mHomeDoubleTapAction.setEntryValues(actionValues);

        mAppSwitchLongPressAction.setEntries(actionEntries);
        mAppSwitchLongPressAction.setEntryValues(actionValues);

        mAppSwitchDoubleTapAction.setEntries(actionEntries);
        mAppSwitchDoubleTapAction.setEntryValues(actionValues);

        mEdgeLongSwipeAction.setEntries(actionEntries);
        mEdgeLongSwipeAction.setEntryValues(actionValues);
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

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = getPreferenceScreen().findPreference(key);
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
        LineageSettings.System.putInt(mResolver, setting, Integer.valueOf(value));
    }

    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(mResolver, setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBackLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mEdgeLongSwipeAction) {
            handleListChange(mEdgeLongSwipeAction, newValue,
                    LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION);
            return true;
        } else if (preference == mEnableTaskbar) {
            toggleTaskBarDependencies((Boolean) newValue);
            LineageSettings.System.putInt(mResolver, LineageSettings.System.ENABLE_TASKBAR,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        }
        return false;
    }

    private static void setButtonNavigationMode(String overlayPackage) {
        IOverlayManager overlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        try {
            overlayManager.setEnabledExclusiveInCategory(overlayPackage, UserHandle.USER_CURRENT);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void enablePreference(Preference pref, boolean enabled) {
        if (pref != null) {
            pref.setEnabled(enabled);
        }
    }

    private void toggleTaskBarDependencies(boolean enabled) {
        enablePreference(mNavbarArrowKeys, !enabled);
        enablePreference(mNavbarKeyOrder, !enabled);
        enablePreference(mNavbarLayout, !enabled);
        enablePreference(mBackLongPressAction, !enabled);
        enablePreference(mHomeLongPressAction, !enabled);
        enablePreference(mHomeDoubleTapAction, !enabled);
        enablePreference(mAppSwitchLongPressAction, !enabled);
        enablePreference(mAppSwitchDoubleTapAction, !enabled);
    }

    private static void writeDisableNavkeysOption(Context context, boolean enabled) {
        LineageSettings.System.putIntForUser(context.getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, enabled ? 1 : 0, UserHandle.USER_CURRENT);
    }

    private void updateDisableNavkeysOption() {
        boolean enabled = LineageSettings.System.getIntForUser(mResolver,
                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;

        mDisableNavigationKeys.setChecked(enabled);
    }

    private void updateDisableNavkeysCategories(boolean navbarEnabled, boolean force) {
        // Toggle navbar options and actions depending on navbar state
        if (mNavigationOptionsCategory != null && mNavigationActionsCategory != null) {
            if (force || navbarEnabled) {
                if (DeviceUtils.isGestureNavigationEnabled(getContext())) {
                    // Gesture navigation - add edge long swipe and remove everything else
                    mNavigationOptionsCategory.removePreference(mNavbarArrowKeys);
                    mNavigationOptionsCategory.removePreference(mNavbarKeyOrder);
                    mNavigationOptionsCategory.removePreference(mNavbarLayout);

                    mNavigationActionsCategory.removePreference(mBackLongPressAction);
                    mNavigationActionsCategory.removePreference(mHomeLongPressAction);
                    mNavigationActionsCategory.removePreference(mHomeDoubleTapAction);
                    mNavigationActionsCategory.removePreference(mAppSwitchLongPressAction);
                    mNavigationActionsCategory.removePreference(mAppSwitchDoubleTapAction);
                    mNavigationActionsCategory.addPreference(mEdgeLongSwipeAction);
                } else {
                    // Three-button navigation - remove edge long wipe and add everything else
                    mNavigationOptionsCategory.addPreference(mNavbarArrowKeys);
                    mNavigationOptionsCategory.addPreference(mNavbarKeyOrder);
                    mNavigationOptionsCategory.addPreference(mNavbarLayout);

                    mNavigationActionsCategory.addPreference(mBackLongPressAction);
                    mNavigationActionsCategory.addPreference(mHomeLongPressAction);
                    mNavigationActionsCategory.addPreference(mHomeDoubleTapAction);
                    mNavigationActionsCategory.addPreference(mAppSwitchLongPressAction);
                    mNavigationActionsCategory.addPreference(mAppSwitchDoubleTapAction);
                    mNavigationActionsCategory.removePreference(mEdgeLongSwipeAction);
                }
            }
        }
    }

    private void enableNavigationCategories(boolean enable) {
        setCategoryEnabled(mNavigationOptionsCategory, enable);
        setCategoryEnabled(mNavigationActionsCategory, enable);
    }

    private void setCategoryEnabled(PreferenceCategory category, boolean enable) {
        if (category != null) {
            category.setEnabled(enable);
            for (int i = 0; i < category.getPreferenceCount(); i++) {
                category.getPreference(i).setEnabled(enable);
            }
        }
    }

    private static boolean hasNavigationBar() {
        boolean hasNavigationBar = false;
        try {
            IWindowManager windowManager = WindowManagerGlobal.getWindowManagerService();
            hasNavigationBar = windowManager.hasNavigationBar(Display.DEFAULT_DISPLAY);
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting navigation bar status");
        }
        return hasNavigationBar;
    }

    private static boolean isKeyDisablerSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }

    public static void restoreKeyDisabler(Context context) {
        if (!isKeyDisablerSupported(context)) {
            return;
        }

        boolean enabled = LineageSettings.System.getIntForUser(context.getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;

        writeDisableNavkeysOption(context, enabled);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mDisableNavigationKeys) {
            mDisableNavigationKeys.setEnabled(false);
            enableNavigationCategories(false);
            if (!mDisableNavigationKeys.isChecked()) {
                setButtonNavigationMode(NAV_BAR_MODE_3BUTTON_OVERLAY);
            }
            writeDisableNavkeysOption(getActivity(), mDisableNavigationKeys.isChecked());
            updateDisableNavkeysOption();
            updateDisableNavkeysCategories(true, false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDisableNavigationKeys.setEnabled(true);
                    enableNavigationCategories(mDisableNavigationKeys.isChecked());
                    updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked(), false);
                }
            }, 1000);
        }

        return super.onPreferenceTreeClick(preference);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.altair_settings_navigation;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!isKeyDisablerSupported(context)) {
                        keys.add(KEY_DISABLE_NAV_KEYS);
                    }

                    if (hasNavigationBar()) {
                        if (DeviceUtils.isGestureNavigationEnabled(context)) {
                            keys.add(KEY_NAVBAR_ARROW_KEYS);
                            keys.add(KEY_NAVBAR_KEY_ORDER);
                            keys.add(KEY_NAVBAR_LAYOUT_MODE);
                            keys.add(KEY_BACK_LONG_PRESS);
                            keys.add(KEY_HOME_LONG_PRESS);
                            keys.add(KEY_HOME_DOUBLE_TAP);
                            keys.add(KEY_APP_SWITCH_LONG_PRESS);
                            keys.add(KEY_APP_SWITCH_DOUBLE_TAP);
                        } else {
                            keys.add(KEY_EDGE_LONG_SWIPE);
                        }
                    }

                    return keys;
                }
            };
}
