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

package com.altair.settings.fragments.navbar;

import android.content.pm.ActivityInfo;
import android.os.UserHandle;

import lineageos.providers.LineageSettings;

public class HomeDoubleTap extends NavbarKeyAppPicker {

    @Override
    protected void setPackage(String packageName, String friendlyAppString) {
        LineageSettings.System.putStringForUser(
                getContentResolver(), LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_APP, packageName,
                UserHandle.USER_CURRENT);
        LineageSettings.System.putStringForUser(
                getContentResolver(), LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_APP_FR_NAME,
                friendlyAppString,
                UserHandle.USER_CURRENT);
    }

    @Override
    protected void setPackageActivity(ActivityInfo ai) {
        LineageSettings.System.putStringForUser(
                getContentResolver(), LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_ACTIVITY,
                ai != null ? ai.name : "NONE",
                UserHandle.USER_CURRENT);
    }
}
