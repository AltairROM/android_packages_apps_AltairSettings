/*
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.preferences

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.android.internal.util.theme.ThemeUtils
import com.android.settings.R

import java.util.concurrent.Executors

import kotlin.math.min

class NavigationBarStylePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle
) : Preference(context, attrs, defStyleAttr) {

    private var dialog: AlertDialog? = null
    private var recyclerView: RecyclerView? = null

    private val themeUtils = ThemeUtils(context)

    private val pkgs: List<String> = themeUtils.getOverlayPackagesForCategory(CATEGORY, PACKAGE)

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        updateSummary()
    }

    override fun onDetached() {
        dialog?.dismiss()
        dialog = null
        recyclerView = null
        super.onDetached()
    }

    override fun onClick() {
        val ctx = context
        val currentPkg = getAppliedPackage()

        val content = LayoutInflater.from(ctx).inflate(R.layout.selector_item_view, null)

        recyclerView = content.findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)

            val isLandscape = ctx.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val span = if (isLandscape) 2 else 1
            layoutManager = GridLayoutManager(ctx, span)
            adapter = NavbarStyleAdapter(ctx, pkgs, themeUtils)

            post {
                val fraction = if (isLandscape) 0.75f else 0.6f
                val maxHeight = (ctx.resources.displayMetrics.heightPixels * fraction).toInt()
                layoutParams.height = maxHeight
                requestLayout()
            }
        }

        dialog = AlertDialog.Builder(ctx)
            .setTitle(title)
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .also { dlg ->
                dlg.setOnDismissListener {
                    dialog = null
                    recyclerView = null
                }
                dlg.show()
                applyDialogWidth(dlg)
                tintDialogAccent(dlg)
            }
    }

    private fun applyDialogWidth(dlg: AlertDialog) {
        val w = dlg.window ?: return
        val ctx = context

        val wm = ctx.getSystemService(WindowManager::class.java)

        val boundsWidthPx = try {
            wm.currentWindowMetrics.bounds.width()
        } catch (_: Throwable) {
            ctx.resources.displayMetrics.widthPixels
        }

        val density = ctx.resources.displayMetrics.density

        val maxDp = if (ctx.resources.configuration.orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {
            DIALOG_MAX_WIDTH_DP_LANDSCAPE
        } else {
            DIALOG_MAX_WIDTH_DP
        }
        val maxWidthPx = (maxDp * density + 0.5f).toInt()

        val targetPx = (boundsWidthPx * 0.85f).toInt()
        w.setLayout(min(maxWidthPx, targetPx), WindowManager.LayoutParams.WRAP_CONTENT)
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun tintDialogAccent(dlg: AlertDialog) {
        val tv = TypedValue()
        val theme = context.theme
        if (!theme.resolveAttribute(android.R.attr.colorAccent, tv, true)) return

        val accent = if (tv.resourceId != 0) context.getColor(tv.resourceId) else tv.data
        if (accent == 0) return

        val w = dlg.window
        if (w != null) {
            val titleId = context.resources.getIdentifier("alertTitle", "id", "android")
            val titleView = if (titleId != 0) w.decorView.findViewById<TextView>(titleId) else null
            titleView?.setTextColor(accent)
        }

        dlg.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(accent)
        dlg.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(accent)
    }

    private fun getAppliedPackage(): String {
        return themeUtils.getOverlayInfos(CATEGORY, PACKAGE)
            .firstOrNull { it.isEnabled }?.packageName ?: PACKAGE
    }

    private fun updateSummary() {
        val applied = getAppliedPackage()
        summary = if (applied == PACKAGE) {
            context.getString(R.string.default_value)
        } else {
            getLabelSafe(context, applied)
        }
    }

    private inner class NavbarStyleAdapter(
        private val ctx: Context,
        private val pkgs: List<String>,
        private val themeUtils: ThemeUtils
    ) : RecyclerView.Adapter<NavbarStyleAdapter.NavbarStyleViewHolder>() {

        private var selectedPkg: String = getApplied(themeUtils)
        private val overlayExecutor = Executors.newSingleThreadExecutor()
        private val mainHandler = Handler(Looper.getMainLooper())

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavbarStyleViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.pref_option_navbar_style, parent, false)
            return NavbarStyleViewHolder(v)
        }

        override fun onBindViewHolder(holder: NavbarStyleViewHolder, position: Int) {
            var pkg = pkgs[position]
            val label = if (pkg == PACKAGE) context.getString(R.string.default_value) else getLabelSafe(ctx, pkg)

            holder.name.text = label
            holder.image1.setBackgroundDrawable(getDrawableSafe(ctx, pkg, "ic_sysbar_back"))
            holder.image2.setBackgroundDrawable(getDrawableSafe(ctx, pkg, "ic_sysbar_home"))
            holder.image3.setBackgroundDrawable(getDrawableSafe(ctx, pkg, "ic_sysbar_recent"))
            holder.name.setTextColor(resolveTextColorPrimary(holder.name.context))
            holder.itemView.isActivated = (pkg == selectedPkg)

            holder.itemView.setOnClickListener {
                if (pkg == selectedPkg) return@setOnClickListener

                val old = selectedPkg
                val pending = pkg
                themeUtils.setOverlayEnabled(CATEGORY, old, old)
                themeUtils.setOverlayEnabled(CATEGORY, pending, PACKAGE)

                selectedPkg = pending
                notifyItemChanged(pkgs.indexOf(old).takeIf { it >= 0 } ?: 0)
                notifyItemChanged(position)
                updateSummary()

                dialog?.dismiss()
            }
        }

        private fun resolveTextColorPrimary(ctx: Context): Int {
            val tv = TypedValue()
            return if (ctx.theme.resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
                if (tv.resourceId != 0) ctx.getColor(tv.resourceId) else tv.data
            } else {
                0xff000000.toInt()
            }
        }

        inner class NavbarStyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.option_label)
            val image1: ImageView = itemView.findViewById(R.id.image1)
            val image2: ImageView = itemView.findViewById(R.id.image2)
            val image3: ImageView = itemView.findViewById(R.id.image3)
        }

        override fun getItemCount(): Int = pkgs.size

        private fun applyOverlayInBackground(
            oldPkg: String,
            newPkg: String,
            onDone: () -> Unit
        ) {
            overlayExecutor.execute {
                try {
                    themeUtils.setOverlayEnabled(CATEGORY, oldPkg, oldPkg)
                    themeUtils.setOverlayEnabled(CATEGORY, newPkg, PACKAGE)
                } finally {
                    mainHandler.post { onDone() }
                }
            }
        }

        private fun getDrawableSafe(context: Context, pkg: String, drawableName: String): Drawable? {
            return try {
                val pm = context.packageManager
                val pkgName = when (pkg) {
                    PACKAGE -> SETTINGS
                    else -> pkg
                }
                val res = pm.getResourcesForApplication(pkgName)
                val id = res.getIdentifier(drawableName, "drawable", pkgName)
                if (id == 0) return null
                res.getDrawable(id, context.getTheme())
            } catch (e: Exception) {
                Log.e(TAG, "Drawable load failed for pkg: $pkg, name: $drawableName", e)
                null
            }
        }
    }

    companion object {
        private const val TAG = "NavigationBarStylePreference"
        private const val CATEGORY = ThemeUtils.NAVBAR_KEY
        private const val PACKAGE = ThemeUtils.TARGET_PACKAGE_LAUNCHER
        private const val SETTINGS = "com.android.settings"

        private const val DIALOG_MAX_WIDTH_DP = 320
        private const val DIALOG_MAX_WIDTH_DP_LANDSCAPE = 640

        private fun getApplied(themeUtils: ThemeUtils): String {
            return themeUtils.getOverlayInfos(CATEGORY, PACKAGE)
                .firstOrNull { it.isEnabled }?.packageName ?: PACKAGE
        }

        private fun getLabelSafe(context: Context, pkg: String): String {
            return try {
                val pm = context.packageManager
                pm.getApplicationInfo(pkg, 0).loadLabel(pm).toString()
            } catch (_: PackageManager.NameNotFoundException) {
                pkg
            }
        }
    }
}
