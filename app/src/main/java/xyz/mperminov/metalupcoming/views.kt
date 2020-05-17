package xyz.mperminov.metalupcoming

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import splitties.experimental.InternalSplittiesApi
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent

@InternalSplittiesApi
fun Context.emptyView(): View {
    return view<LinearLayout> {
        layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
        orientation = LinearLayout.VERTICAL
        addView(view<ImageView> {
            setImageDrawable(resources.getDrawable(R.drawable.ic_empty, theme))
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        })
        addView(view<TextView> {
            setText(R.string.nothing_found)
            setTextAppearance(R.style.StateTextStyle)
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

@InternalSplittiesApi
fun Context.errorView(reloadClickListener: (View) -> Unit): View {
    return view<LinearLayout> {
        layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
        orientation = LinearLayout.VERTICAL
        addView(view<ImageView> {
            setImageDrawable(resources.getDrawable(R.drawable.ic_error, theme))
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        })
        addView(view<TextView> {
            setText(R.string.check_connection)
            setTextAppearance(R.style.StateTextStyle)
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                wrapContent,
                wrapContent
            ).apply {
                setMargins(0, 16.dp, 0, 0)
            }
        })
        addView(view<Button> {
            setOnClickListener { v -> reloadClickListener(v) }
            setText(R.string.retry)
            setTextAppearance(R.style.StateTextStyle)
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

fun Context.isDarkSystemThemeOn(): Boolean {
    return resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

enum class Theme {
    DARK, LIGHT
}
