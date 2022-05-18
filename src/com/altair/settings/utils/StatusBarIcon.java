/*
 * SPDX-FileCopyrightText: The CyanogenMod Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.utils;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;

import androidx.preference.SwitchPreference;

import java.util.Set;

public class StatusBarIcon {

    public static final String ICON_BLACKLIST = "icon_blacklist";

    private Context mContext;
    private String mKey;
    private Set<String> mBlacklist;

    public StatusBarIcon(Context context, String key) {
        mKey = key;
        mContext = context;
    }

    public Boolean isEnabled() {
        mBlacklist = getIconList();
        return !mBlacklist.contains(mKey);
    }

    public void setEnabled(Boolean value) {
        mBlacklist = getIconList();
        if (value) {
            if (mBlacklist.contains(mKey)) {
                mBlacklist.remove(mKey);
            }
        }
        else {
            if (!mBlacklist.contains(mKey)) {
                mBlacklist.add(mKey);
            }
        }
        setIconList(mBlacklist);
    }

    private ArraySet<String> getIconList() {
        ContentResolver contentResolver = mContext.getContentResolver();
        ArraySet<String> ret = new ArraySet<>();
        String blackListStr = Settings.Secure.getStringForUser(contentResolver, ICON_BLACKLIST,
                ActivityManager.getCurrentUser());
        if (blackListStr == null) {
            blackListStr = "rotate,headset";
        }
        String[] blacklist = blackListStr.split(",");
        for (String slot : blacklist) {
            if (!TextUtils.isEmpty(slot)) {
                ret.add(slot);
            }
        }
        return ret;
    }

    private void setIconList(Set<String> blacklist) {
        ContentResolver contentResolver = mContext.getContentResolver();
        Settings.Secure.putStringForUser(contentResolver, ICON_BLACKLIST,
                TextUtils.join(",", blacklist), ActivityManager.getCurrentUser());
    }
}
