package xyz.mperminov.metalupcoming

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.res.ResourcesCompat
import splitties.views.dsl.core.button
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.wrapContent

fun Context.emptyView(): View {
    return LinearLayout(this).apply {
        layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
        orientation = LinearLayout.VERTICAL
        addView(imageView {
            setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_empty, theme))
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        })
        addView(textView {
            setText(R.string.nothing_found)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextAppearance(R.style.StateTextStyle)
            } else {
                @Suppress("DEPRECATION")
                setTextAppearance(this@emptyView, R.style.StateTextStyle)
            }
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                wrapContent,
                wrapContent
            ).apply {
                setMargins(0, 16.dp, 0, 0)
            }
        })
    }
}

fun Context.errorView(reloadClickListener: (View) -> Unit): View {
    return LinearLayout(this).apply {
        layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
        orientation = LinearLayout.VERTICAL
        addView(imageView {
            setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_error, theme))
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        })
        addView(textView {
            setText(R.string.check_connection)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextAppearance(R.style.StateTextStyle)
            } else {
                @Suppress("DEPRECATION")
                setTextAppearance(this@errorView, R.style.StateTextStyle)
            }
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                wrapContent,
                wrapContent
            ).apply {
                setMargins(0, 16.dp, 0, 0)
            }
        })
        addView(button {
            setOnClickListener { v -> reloadClickListener(v) }
            backgroundTintList =
                ColorStateList.valueOf(this@errorView.getColorFromTheme(R.attr.dateTextColor))
            setText(R.string.retry)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextAppearance(R.style.StateTextStyle)
            } else {
                @Suppress("DEPRECATION")
                setTextAppearance(this@errorView, R.style.StateTextStyle)
            }
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                wrapContent,
                wrapContent
            ).apply {
                setMargins(0, 8.dp, 0, 0)
            }
        })
    }
}

fun Context.progressView(): View {
    return LinearLayout(this).apply {
        layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
        orientation = LinearLayout.VERTICAL
        addView(ProgressBar(this@progressView).apply {
            isIndeterminate = true
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        })
    }
}

fun Context.isDarkSystemThemeOn(): Boolean {
    return resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

fun Context.getColorFromTheme(id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}


enum class Theme {
    DARK, LIGHT
}
