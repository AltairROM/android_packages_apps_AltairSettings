/*
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings

import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment

class SettingsIcon : SettingsPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings_icons)
    }

    override fun getMetricsCategory(): Int = MetricsProto.MetricsEvent.ALTAIR_SETTINGS
}
