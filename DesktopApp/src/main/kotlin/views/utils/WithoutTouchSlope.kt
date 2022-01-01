package views.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration

@Composable
fun WithoutTouchSlop(content: @Composable () -> Unit) {
    fun ViewConfiguration.withoutTouchSlop() = object : ViewConfiguration {
        override val longPressTimeoutMillis get() =
            this@withoutTouchSlop.longPressTimeoutMillis

        override val doubleTapTimeoutMillis get() =
            this@withoutTouchSlop.doubleTapTimeoutMillis

        override val doubleTapMinTimeMillis get() =
            this@withoutTouchSlop.doubleTapMinTimeMillis

        override val touchSlop get() = 0.1f
    }

    CompositionLocalProvider(
        LocalViewConfiguration provides LocalViewConfiguration.current.withoutTouchSlop()
    ) {
        content()
    }
}