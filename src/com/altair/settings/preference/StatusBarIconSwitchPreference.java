/*
 * Copyright (C) 2013 The CyanogenMod project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altair.settings.preference;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;

import java.util.Set;

public class StatusBarIconSwitchPreference extends SwitchPreference {

    public static final String ICON_BLACKLIST = "icon_blacklist";

    private Set<String> mBlacklist;

    public StatusBarIconSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public StatusBarIconSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusBarIconSwitchPreference(Context context) {
        super(context, null);
    }

    @Override
    protected boolean persistBoolean(boolean value) {
        if (shouldPersist()) {
            if (value == getPersistedBoolean(!value)) {
                // It's already there, so the same as persisting
                return true;
            }
            setBooleanValue(value);
            return true;
        }
        return false;
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return getBooleanValue();
    }

    private Boolean getBooleanValue() {
        mBlacklist = getList();
        return !mBlacklist.contains(getKey());
    }

    private void setBooleanValue(Boolean value) {
        mBlacklist = getList();
        if (value) {
            if (!mBlacklist.contains(getKey())) {
                mBlacklist.add(getKey());
            }
        }
        else {
            if (mBlacklist.contains(getKey())) {
                mBlacklist.remove(getKey());
            }
        }
        setList(mBlacklist);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setChecked(getBooleanValue());
    }

    private ArraySet<String> getList() {
        ContentResolver contentResolver = getContext().getContentResolver();
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

    private void setList(Set<String> blacklist) {
        ContentResolver contentResolver = getContext().getContentResolver();
        Settings.Secure.putStringForUser(contentResolver, ICON_BLACKLIST,
                TextUtils.join(",", blacklist), ActivityManager.getCurrentUser());
    }
}
