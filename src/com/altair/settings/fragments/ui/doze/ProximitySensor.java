/*
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments.ui.doze;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProximitySensor implements SensorEventListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "ProximitySensor";

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Context mContext;
    private ExecutorService mExecutorService;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;

    private boolean mSawNear = false;
    private long mInPocketTime = 0;
    private int mWakelockTimeoutMs;
    private int mHandWaveMaxDeltaNs;
    private int mPocketMinDeltaNs;

    private Vibrator mVibrator;

    public ProximitySensor(Context context) {
        mContext = context;
        final Resources res = context.getResources();
        mSensorManager = mContext.getSystemService(SensorManager.class);
        final boolean wakeup =
            res.getBoolean(com.android.internal.R.bool.config_deviceHaveWakeUpProximity);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY, wakeup);
        mWakelockTimeoutMs =
            res.getInteger(R.integer.config_dozePulseProximity_WakelockTimeoutMs);
        mHandWaveMaxDeltaNs =
            res.getInteger(R.integer.config_dozePulseProximity_HandwaveMaxDeltaNs);
        mPocketMinDeltaNs =
            res.getInteger(R.integer.config_dozePulseProximity_PocketMinDeltaNs);
        if (DEBUG) {
            Log.d(TAG, "WakelockTimeoutMs: " + String.valueOf(mWakelockTimeoutMs));
            Log.d(TAG, "HandwaveMaxDeltaNs: " + String.valueOf(mHandWaveMaxDeltaNs));
            Log.d(TAG, "PocketMinDeltaNs: " + String.valueOf(mPocketMinDeltaNs));
        }
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mExecutorService = Executors.newSingleThreadExecutor();
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null && !mVibrator.hasVibrator()) {
            mVibrator = null;
        }
    }

    private Future<?> submit(Runnable runnable) {
        return mExecutorService.submit(runnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean isRaiseToWake = Utils.isRaiseToWakeEnabled(mContext);
        boolean isNear = event.values[0] < mSensor.getMaximumRange();
        if (mSawNear && !isNear) {
            if (shouldPulse(event.timestamp)) {
                if (isRaiseToWake) {
                    mWakeLock.acquire(mWakelockTimeoutMs);
                    mPowerManager.wakeUp(SystemClock.uptimeMillis(),
                        PowerManager.WAKE_REASON_GESTURE, TAG);
                } else {
                    Utils.launchDozePulse(mContext);
                    doHapticFeedback();
                }
            }
        } else {
            mInPocketTime = event.timestamp;
        }
        mSawNear = isNear;
    }

    private boolean shouldPulse(long timestamp) {
        long delta = timestamp - mInPocketTime;
        boolean shouldPulse = false;

        if (delta < mHandWaveMaxDeltaNs)
            shouldPulse = Utils.handwaveGestureEnabled(mContext);

        if (!shouldPulse && delta >= mPocketMinDeltaNs)
            shouldPulse = Utils.pocketGestureEnabled(mContext);

        return shouldPulse;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* Empty */
    }

    protected void enable() {
        if (DEBUG) Log.d(TAG, "Enabling");
        submit(() -> {
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        });
    }

    protected void disable() {
        if (DEBUG) Log.d(TAG, "Disabling");
        submit(() -> {
            mSensorManager.unregisterListener(this, mSensor);
        });
    }

    private void doHapticFeedback() {
        if (mVibrator == null) {
            return;
        }
        int val = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.DOZE_GESTURE_VIBRATE, 0, UserHandle.USER_CURRENT);
        if (val > 0) {
            mVibrator.vibrate(VibrationEffect.createOneShot(val,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
}
