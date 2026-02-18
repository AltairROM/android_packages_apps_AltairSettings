/*
 * SPDX-FileCopyrightText: 2017-2021 crDroid Android Project
 * SPDX-FileCopyrightText: 2026 Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments.ui.doze;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DozeReceiver extends BroadcastReceiver {

    private static final boolean DEBUG = false;
    private static final String TAG = "DozeReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (DEBUG) Log.d(TAG, "Starting service");
            Utils.enableService(context);
        }
    }
}
