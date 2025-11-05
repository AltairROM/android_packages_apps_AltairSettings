/*
 * SPDX-FileCopyrightText: 2019-2023 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Surface;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class AltairSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.altair_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Context context = getActivity().getApplicationContext();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR_SETTINGS;
    }

    public static void lockCurrentOrientation(Activity activity) {
        int currentRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = activity.getResources().getConfiguration().orientation;
        int frozenRotation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        switch (currentRotation) {
            case Surface.ROTATION_0:
                frozenRotation = orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                frozenRotation = orientation == Configuration.ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                frozenRotation = orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            case Surface.ROTATION_270:
                frozenRotation = orientation == Configuration.ORIENTATION_PORTRAIT
                        ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }
        activity.setRequestedOrientation(frozenRotation);
    }
}
