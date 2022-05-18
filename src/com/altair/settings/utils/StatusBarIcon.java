/*
 * Copyright (C) 2013 The CyanogenMod project
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

