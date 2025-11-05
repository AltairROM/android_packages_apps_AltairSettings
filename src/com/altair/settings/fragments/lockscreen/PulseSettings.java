/*
 * SPDX-FileCopyrightText: 2016-2025 crDroid Android Project
 * SPDX-FileCopyrightText: 2025 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.altair.settings.fragments.lockscreen;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PulseSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pulse_settings);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR_SETTINGS;
    }
}
