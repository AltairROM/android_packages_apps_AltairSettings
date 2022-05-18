/*
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-FileCopyrightText: Altair ROM Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.altair.settings.preferences

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.util.AttributeSet
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

import com.android.internal.util.theme.MonetUtils
import com.android.settings.R

import kotlin.math.min

class AccentColorPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle
) : Preference(context, attrs, defStyleAttr) {

    private var dialog: AlertDialog? = null
    private var recyclerView: RecyclerView? = null

    private val entries: Array<String> =
        context.resources.getStringArray(R.array.theme_accent_color_names)

    private val entryValues: Array<String> =
        context.resources.getStringArray(R.array.theme_accent_color_values)

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        val idx = indexOfValue(getAccentColor())
        summary = if (idx >= 0) entries[idx] else context.getString(R.string.wallpaper_color)
    }

    override fun onDetached() {
        dialog?.dismiss()
        dialog = null
        recyclerView = null
        super.onDetached()
    }

    override fun onClick() {
        val ctx = context
        val currentColor = getAccentColor()
        val selectedIndex = indexOfValue(currentColor).coerceAtLeast(0)

        val content = LayoutInflater.from(ctx).inflate(R.layout.selector_item_view, null)

        recyclerView = content.findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)

            val isLandscape = ctx.resources.configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE
            val span = if (isLandscape) 2 else 1
            layoutManager = GridLayoutManager(ctx, span)
            adapter = AccentColorAdapter(selectedIndex)

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
            .setNeutralButton(R.string.reset) { dialog, which ->
                MonetUtils(context).setAccentColor(MonetUtils.ACCENT_COLOR_DEFAULT)
                dialog.dismiss()
            }
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

    private fun getAccentColor(): String {
        return MonetUtils(context).getAccentColor()
    }

    private fun indexOfValue(key: String): Int =
        entryValues.indexOfFirst { it == key }

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

    private inner class AccentColorAdapter(applied: Int) :
        RecyclerView.Adapter<AccentColorAdapter.AccentColorViewHolder>() {

        private var selectedIndex: Int = applied.coerceIn(0, entries.size - 1)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccentColorViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.pref_option_accent_color, parent, false)
            return AccentColorViewHolder(v)
        }

        override fun onBindViewHolder(holder: AccentColorViewHolder, position: Int) {
            val color = entryValues[position]

            holder.image.setImageDrawable(AccentColorPreviewDrawable(color))
            holder.image.setPadding(8, 8, 8, 8)
            holder.name.visibility = View.VISIBLE
            holder.name.text = entries[position]
            holder.itemView.isActivated = (color == getAccentColor())

            holder.itemView.setOnClickListener {
                val old = selectedIndex
                selectedIndex = position

                MonetUtils(context).setAccentColor(color)

                summary = entries[position]
                callChangeListener(color)

                notifyItemChanged(old)
                notifyItemChanged(selectedIndex)

                dialog?.dismiss()
            }
        }

        override fun getItemCount(): Int = entries.size

        inner class AccentColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.option_label)
            val image: ImageView = itemView.findViewById(R.id.option_thumbnail)
        }
    }

    private class AccentColorPreviewDrawable(
        colorVal: String,
    ) : Drawable() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = Color.parseColor(colorVal)
        }

        private val path: Path = PathParser.createPathFromPathData(ICON_SHAPE)

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
        private const val DIALOG_MAX_WIDTH_DP = 320
        private const val DIALOG_MAX_WIDTH_DP_LANDSCAPE = 640
        private const val ICON_SHAPE = "M50 0A50 50,0,1,1,50 100A50 50,0,1,1,50 0"
    }
}
