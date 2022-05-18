/*
 * SPDX-FileCopyrightText: 2022 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings;

import android.content.Context;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class TopLevelSettingsPreferenceController extends BasePreferenceController {

    public TopLevelSettingsPreferenceController(Context context,
            String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
}
