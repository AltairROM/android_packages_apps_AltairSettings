package com.altair.settings.fragments.navbar;

import android.content.pm.ActivityInfo;
import android.os.UserHandle;

import lineageos.providers.LineageSettings;

public class AppSwitchDoubleTap extends NavbarKeyAppPicker {

    @Override
    protected void setPackage(String packageName, String friendlyAppString) {
        LineageSettings.System.putStringForUser(
                getContentResolver(), LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP, packageName,
                UserHandle.USER_CURRENT);
        LineageSettings.System.putStringForUser(
                getContentResolver(), LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP_FR_NAME,
                friendlyAppString,
                UserHandle.USER_CURRENT);
    }

    @Override
    protected void setPackageActivity(ActivityInfo ai) {
        LineageSettings.System.putStringForUser(
                getContentResolver(), LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_ACTIVITY,
                ai != null ? ai.name : "NONE",
                UserHandle.USER_CURRENT);
    }
}
