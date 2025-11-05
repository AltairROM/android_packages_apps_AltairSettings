/*
 * SPDX-FileCopyrightText: 2023-2025 crDroid Android Project
 * SPDX-FileCopyrightText: 2025 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments.ui;

import android.os.Bundle;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class EdgeLightSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.edge_light_settings);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR_SETTINGS;
    }
}
