/*
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.fragments.theme;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.theme.MonetUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.Utils;

import com.lineage.support.preferences.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class AccentColorPicker extends SettingsPreferenceFragment {
    private static final String TAG = "AccentColorPicker";

    private RecyclerView mRecyclerView;
    private MonetUtils mMonetUtils;

    private List<String> mAccentColorNames;
    private List<String> mAccentColorValues;

    private String mAccentColorValue;

    private Context mContext;
    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.theme_accent_color_title);

        mContext = getActivity().getApplicationContext();
        mResolver = getActivity().getContentResolver();
        mMonetUtils = new MonetUtils(getActivity());

        mAccentColorValue = mMonetUtils.getAccentColor();

        final Resources res = getResources();
        mAccentColorNames = Arrays.asList(res.getStringArray(R.array.theme_accent_color_names));
        mAccentColorValues = Arrays.asList(res.getStringArray(R.array.theme_accent_color_values));

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picker_recycler_view, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        Adapter mAdapter = new Adapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ALTAIR_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                    .setTitle(R.string.theme_colors_reset_accent_color_title)
                    .setMessage(R.string.theme_colors_reset_accent_color_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mMonetUtils.setAccentColor(MonetUtils.ACCENT_COLOR_DEFAULT);
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null);
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.CustomViewHolder> {
        Context context;
        int mSelectedColor;

        public Adapter(Context context) {
            this.context = context;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.picker_option_generic, parent, false);
            CustomViewHolder vh = new CustomViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, final int position) {
            final int selectedColor = Color.parseColor(mAccentColorValues.get(position));
            final int currentColor = mAccentColorValue == MonetUtils.ACCENT_COLOR_DEFAULT
                    ? 0 : Color.parseColor("#" + mAccentColorValue);

            holder.image.setBackgroundResource(R.drawable.accent_background);
            final int viewColor = Color.parseColor(mAccentColorValues.get(position));
            holder.image.setBackgroundTintList(ColorStateList.valueOf(viewColor));
            holder.itemView.setActivated(selectedColor == currentColor);
            holder.name.setText(mAccentColorNames.get(position));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String oldColor = String.format("#%06X", (0xFFFFFF & currentColor));
                    final String newColor = String.format("#%06X", (0xFFFFFF & selectedColor));

                    updateActivatedStatus(oldColor, false);
                    updateActivatedStatus(newColor, true);

                    mAccentColorValue = String.format("%06X", (0xFFFFFF & selectedColor));
                    mMonetUtils.setAccentColor(mAccentColorValue);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAccentColorValues.size();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView image;
            public CustomViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.option_label);
                image = (ImageView) itemView.findViewById(R.id.option_thumbnail);
            }
        }

        private void updateActivatedStatus(String color, boolean isActivated) {
            int index = mAccentColorValues.indexOf(color);
            if (index < 0) {
                return;
            }
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(index);
            if (holder != null && holder.itemView != null) {
                holder.itemView.setActivated(isActivated);
            }
        }
    }
}
