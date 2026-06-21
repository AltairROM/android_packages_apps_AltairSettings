/*
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;

public class MonetUtils {

    private static final String OVERLAY_ACCENT_COLOR = "android.theme.customization.accent_color";
    private static final String OVERLAY_SYSTEM_PALETTE = "android.theme.customization.system_palette";
    private static final String OVERLAY_THEME_STYLE = "android.theme.customization.theme_style";
    private static final String OVERLAY_ENHANCED_COLORS = "android.theme.customization.enhanced_colors";
    private static final String TIMESTAMP_FIELD = "_applied_timestamp";

    public static final String ACCENT_COLOR_DEFAULT = "";
    public static final String THEME_STYLE_DEFAULT = "TONAL_SPOT";
    public static final boolean ENHANCED_COLORS_DEFAULT = false;

    private Context mContext;

    public MonetUtils(Context context) {
        mContext = context;
    }

    /*
     * Private helper functions.
     */

    private JSONObject getSettingsJson() throws JSONException {
        final String overlayPackageJson = Settings.Secure.getStringForUser(
                mContext.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                UserHandle.USER_CURRENT);
        JSONObject object;
        if (overlayPackageJson == null || overlayPackageJson.isEmpty()) {
            return new JSONObject();
        }
        return new JSONObject(overlayPackageJson);
    }

    private void putSettingsJson(JSONObject object) {
        Settings.Secure.putStringForUser(
                mContext.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                object.toString(), UserHandle.USER_CURRENT);
    }

    private void setBooleanValue(String overlay, boolean value) {
        try {
            JSONObject object = getSettingsJson();
            if (!value)
                object.remove(overlay);
            else
                object.putOpt(overlay, 1);
            putSettingsJson(object);
        } catch (JSONException | IllegalArgumentException ignored) {}
    }

    private void setDoubleValue(String overlay, double value) {
        try {
            JSONObject object = getSettingsJson();
            if (value == 0)
                object.remove(overlay);
            else
                object.putOpt(overlay, value);
            putSettingsJson(object);
        } catch (JSONException | IllegalArgumentException ignored) {}
    }

    private void setStringValue(String overlay, String value) {
        try {
            JSONObject object = getSettingsJson();
            if (value == null || value == "")
                object.remove(overlay);
            else
                object.putOpt(overlay, value);
            putSettingsJson(object);
        } catch (JSONException | IllegalArgumentException ignored) {}
    }

    private boolean getBooleanValue(String overlay, boolean defaultValue) {
        boolean value;

        try {
            JSONObject object = getSettingsJson();
            value = object.optInt(overlay, defaultValue ? 1 : 0) == 1;
        } catch (JSONException | IllegalArgumentException ignored) {
            value = defaultValue;
        }

        return value;
    }

    private double getDoubleValue(String overlay, double defaultValue) {
        double value;

        try {
            JSONObject object = getSettingsJson();
            value = object.optDouble(overlay, defaultValue);
        } catch (JSONException | IllegalArgumentException ignored) {
            value = defaultValue;
        }

        return value;
    }

    private String getStringValue(String overlay, String defaultValue) {
        String value;

        try {
            JSONObject object = getSettingsJson();
            value = object.optString(overlay, defaultValue);
        } catch (JSONException | IllegalArgumentException ignored) {
            value = defaultValue;
        }

        return value;
    }

    /*
     * Public class functions.
     */

    // Returns true if enhanced accent colors is enabled, false if not.
    public boolean isEnhancedColorsEnabled() {
        return getBooleanValue(OVERLAY_ENHANCED_COLORS, ENHANCED_COLORS_DEFAULT);
    }

    // Enables or disables enhanced accent colors.
    public void setEnhancedColors(boolean enable) {
        setBooleanValue(OVERLAY_ENHANCED_COLORS, enable);
    }

    // Returns true if accent color is set, false if not.
    public boolean isAccentColorSet() {
        return getAccentColor() != ACCENT_COLOR_DEFAULT;
    }

    // Returns the current accent color.
    public String getAccentColor() {
        return getStringValue(OVERLAY_ACCENT_COLOR, ACCENT_COLOR_DEFAULT);
    }

    // Sets the accent color. Setting to ACCENT_COLOR_DEFAULT removes the custom accent color and
    // returns the system to using the color obtained from the current wallpaper.
    public void setAccentColor(String color) {
        setStringValue(OVERLAY_ACCENT_COLOR, color);
        setStringValue(OVERLAY_SYSTEM_PALETTE, color);
    }

    // Returns the current theme style.
    public String getThemeStyle() {
        return getStringValue(OVERLAY_THEME_STYLE, THEME_STYLE_DEFAULT);
    }

    // Sets the theme style.
    public void setThemeStyle(String value) {
        setStringValue(OVERLAY_THEME_STYLE, value);
    }
}
