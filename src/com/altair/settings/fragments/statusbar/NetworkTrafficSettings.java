/*
 * SPDX-FileCopyrightText: 2017-2023 The LineageOS Project
 * SPDX-FileCopyrightText: 2019-2025 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments.statusbar;

//import android.content.ContentResolver;
//import android.content.Context;
import android.os.Bundle;
//import android.os.UserHandle;
//import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

public class NetworkTrafficSettings extends SettingsPreferenceFragment {

    private static final String TAG = "NetworkTrafficSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_traffic_settings);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR_SETTINGS;
    }
}
