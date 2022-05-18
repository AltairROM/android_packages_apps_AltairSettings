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
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.util.PathParser
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

class IconShapePreference @JvmOverloads constructor(
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
            adapter = IconShapeAdapter(ctx, pkgs, themeUtils)

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

    private fun createPreviewDrawable(pkg: String): Drawable? {
        val pm = context.packageManager
        val res = try {
            when (pkg) {
                PACKAGE -> Resources.getSystem()
                "default" -> pm.getResourcesForApplication(PACKAGE)
                else -> pm.getResourcesForApplication(pkg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Resources not found for package: $pkg")
            return null
        }
        val pathData = res.getString(
                res.getIdentifier("config_icon_mask", "string", pkg)
        )
        return IconShapePreviewDrawable(pathData, getThemeIconColor())
    }

    private fun getThemeIconColor(): Int {
        val tv = TypedValue()
        val theme = context.theme

        if (theme.resolveAttribute(android.R.attr.colorControlNormal, tv, true)) {
            return if (tv.resourceId != 0) context.getColor(tv.resourceId) else tv.data
        }
        if (theme.resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
            return if (tv.resourceId != 0) context.getColor(tv.resourceId) else tv.data
        }
        return 0xff000000.toInt()
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

    private inner class IconShapeAdapter(
        private val ctx: Context,
        private val pkgs: List<String>,
        private val themeUtils: ThemeUtils
    ) : RecyclerView.Adapter<IconShapeAdapter.IconShapeViewHolder>() {

        private var selectedPkg: String = getApplied(themeUtils)
        private val overlayExecutor = Executors.newSingleThreadExecutor()
        private val mainHandler = Handler(Looper.getMainLooper())

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconShapeViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.pref_option_icon_shape, parent, false)
            return IconShapeViewHolder(v)
        }

        override fun onBindViewHolder(holder: IconShapeViewHolder, position: Int) {
            var pkg = pkgs[position]
            val label = if (pkg == PACKAGE) context.getString(R.string.default_value) else getLabelSafe(ctx, pkg)

            holder.name.text = label
            holder.name.setTextColor(resolveTextColorPrimary(holder.name.context))
            holder.name.visibility = View.VISIBLE

            holder.image.setImageDrawable(createPreviewDrawable(pkg))
            holder.image.setPadding(8, 8, 8, 8)
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

        override fun getItemCount(): Int = pkgs.size

        inner class IconShapeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.option_label)
            val image: ImageView = itemView.findViewById(R.id.option_thumbnail)
        }
    }

    private class IconShapePreviewDrawable(
        pathData: String,
        color: Int,
    ) : Drawable() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = color
        }

        private val path: Path = try {
            PathParser.createPathFromPathData(pathData)
        } catch (_: RuntimeException) {
            PathParser.createPathFromPathData(DEFAULT_ICON_SHAPE)
        }

        override fun draw(canvas: Canvas) {
            val r = bounds
            if (r.isEmpty) return

            canvas.save()
            val sx = r.width() / 100f
            val sy = r.height() / 100f
            canvas.translate(r.left.toFloat(), r.top.toFloat())
            canvas.scale(sx, sy)
            canvas.drawPath(path, paint)
            canvas.restore()
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    companion object {
        private const val TAG = "NavigationBarStylePreference"
        private const val CATEGORY = ThemeUtils.ICON_SHAPE_KEY
        private const val PACKAGE = ThemeUtils.TARGET_PACKAGE_ANDROID
        private const val DEFAULT_ICON_SHAPE = "M50 0A50 50,0,1,1,50 100A50 50,0,1,1,50 0"

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
