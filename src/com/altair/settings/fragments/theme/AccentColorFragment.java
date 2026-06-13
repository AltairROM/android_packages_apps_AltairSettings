/*
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments.theme;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.utils.MonetUtils;

import com.lineage.support.preferences.CustomSeekBarPreference;

import java.lang.CharSequence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class AccentColorFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_ACCENT_COLOR = "theme_colors_accent_color";
    private static final String KEY_RICHER_COLORS = "theme_colors_richer_colors";
    private static final String KEY_LUMINANCE_FACTOR = "theme_colors_luminance_factor";
    private static final String KEY_CHROMA_FACTOR = "theme_colors_chroma_factor";
    private static final String KEY_WHOLE_PALETTE = "theme_colors_whole_palette";
    private static final String KEY_TINT_BACKGROUND = "theme_colors_tint_background";

    private Context mContext;
    private Resources mResources;

    private MonetUtils mMonetUtils;

    private List<String> mAccentColorValues;
    private List<String> mAccentColorNames;
    private String mAccentColorValue;

    private Preference mAccentColorPreference;
    private SwitchPreferenceCompat mRicherColorsPreference;
    private CustomSeekBarPreference mChromaFactorPreference;
    private CustomSeekBarPreference mLuminanceFactorPreference;
    private SwitchPreferenceCompat mWholePalettePreference;
    private SwitchPreferenceCompat mTintBackgroundPreference;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.accent_color;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mResources = getResources();
        mMonetUtils = new MonetUtils(getActivity());

        mAccentColorValues = Arrays.asList(mResources.getStringArray(
                R.array.theme_accent_color_values));
        mAccentColorNames = Arrays.asList(mResources.getStringArray(
                R.array.theme_accent_color_names));
        mAccentColorValue = mMonetUtils.getAccentColor();

        final PreferenceScreen prefScreen = getPreferenceScreen();
        mAccentColorPreference = prefScreen.findPreference(KEY_ACCENT_COLOR);
        mAccentColorPreference.setOnPreferenceChangeListener(this);
        mRicherColorsPreference = prefScreen.findPreference(KEY_RICHER_COLORS);
        mRicherColorsPreference.setOnPreferenceChangeListener(this);
        mChromaFactorPreference = prefScreen.findPreference(KEY_CHROMA_FACTOR);
        mChromaFactorPreference.setOnPreferenceChangeListener(this);
        mLuminanceFactorPreference = prefScreen.findPreference(KEY_LUMINANCE_FACTOR);
        mLuminanceFactorPreference.setOnPreferenceChangeListener(this);
        mWholePalettePreference = prefScreen.findPreference(KEY_WHOLE_PALETTE);
        mWholePalettePreference.setOnPreferenceChangeListener(this);
        mTintBackgroundPreference = prefScreen.findPreference(KEY_TINT_BACKGROUND);
        mTintBackgroundPreference.setOnPreferenceChangeListener(this);

        updatePreferences();
        setHasOptionsMenu(true);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR_SETTINGS;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.reset_button, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset_button) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.theme_colors_reset_settings_title)
                    .setMessage(R.string.theme_colors_reset_settings_message)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             mMonetUtils.setRicherColors(MonetUtils.RICHER_COLORS_DEFAULT);
                             mMonetUtils.setLuminanceFactor(MonetUtils.LUMINANCE_FACTOR_DEFAULT);
                             mMonetUtils.setChromaFactor(MonetUtils.CHROMA_FACTOR_DEFAULT);
                             mMonetUtils.setWholePalette(MonetUtils.WHOLE_PALETTE_DEFAULT);
                             mMonetUtils.setTintBackground(MonetUtils.TINT_BACKGROUND_DEFAULT);
                             updatePreferences();
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null);
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case KEY_ACCENT_COLOR:
                mAccentColorValue = mMonetUtils.getAccentColor();
                updateAccentColorSummary();
                break;
            case KEY_RICHER_COLORS:
                boolean enabled = (Boolean) newValue;
                mMonetUtils.setRicherColors(enabled);
                mLuminanceFactorPreference.setEnabled(!enabled);
                mChromaFactorPreference.setEnabled(!enabled);
                break;
            case KEY_LUMINANCE_FACTOR:
                int lumin = (Integer) newValue;
                mMonetUtils.setLuminanceFactor(lumin == 0 ? 0d : 1d + ((double) lumin / 100d));
                break;
            case KEY_CHROMA_FACTOR:
                int chroma = (Integer) newValue;
                mMonetUtils.setChromaFactor(chroma == 0 ? 0d : 1d + ((double) chroma / 100d));
                break;
            case KEY_WHOLE_PALETTE:
                mMonetUtils.setWholePalette((Boolean) newValue);
                break;
            case KEY_TINT_BACKGROUND:
                mMonetUtils.setTintBackground((Boolean) newValue);
                break;
        }

        return true;
    }

    public void updateAccentColorSummary() {
        String summary = mResources.getString(R.string.theme_colors_wallpaper_accent_color);
        final String color = "#" + mAccentColorValue;
        final int index = mAccentColorValues.indexOf(color.toLowerCase());
        if (index >= 0) {
            summary = mAccentColorNames.get(index);
        }
        mAccentColorPreference.setSummary(summary);
    }

    private void updatePreferences() {
        updateAccentColorSummary();

        final boolean richerColors = mMonetUtils.isRicherColorsEnabled();
        mRicherColorsPreference.setChecked(richerColors);

        final float lumin = (float) mMonetUtils.getLuminanceFactor();
        int luminV = 0;
        if (lumin > 1d) {
            luminV = Math.round((lumin - 1f) * 100f);
        } else if (lumin < 1d) {
            luminV = -1 * Math.round((1f - lumin) * 100f);
        }
        mLuminanceFactorPreference.setValue(luminV);
        mLuminanceFactorPreference.setEnabled(!richerColors);

        final float chroma = (float) mMonetUtils.getChromaFactor();
        int chromaV = 0;
        if (chroma > 1d) {
            chromaV = Math.round((chroma - 1f) * 100f);
        } else if (chroma < 1d) {
            chromaV = -1 * Math.round((1f - chroma) * 100f);
        }
        mChromaFactorPreference.setValue(chromaV);
        mChromaFactorPreference.setEnabled(!richerColors);

        mWholePalettePreference.setChecked(mMonetUtils.isWholePaletteEnabled());
        mTintBackgroundPreference.setChecked(mMonetUtils.isTintBackgroundEnabled());
    }
}
