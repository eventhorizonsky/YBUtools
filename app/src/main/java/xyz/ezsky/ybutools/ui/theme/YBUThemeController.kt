package xyz.ezsky.ybutools.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import com.heyanle.okkv2.core.okkv

/**
 * 皮肤控制器
 * 参考：https://github.com/heyanLE
 */
enum class DarkMode {
    Auto, Dark, Light
}

data class YBUThemeState(
    val themeMode: YBUThemeMode,
    val darkMode: DarkMode,
    val isDynamicColor: Boolean,
) {
    @SuppressLint("ComposableNaming")
    @Composable
    @ReadOnlyComposable
    fun isDark(): Boolean {
        return when (darkMode) {
            DarkMode.Dark -> true
            DarkMode.Light -> false
            else -> isSystemInDarkTheme()
        }
    }
}

object YBUThemeController {

    var curThemeColor: ColorScheme? = null

    private var themeModeOkkv by okkv("theme_mode", YBUThemeMode.Greenapple.name)
    private var darkModeOkkv by okkv("dark_mode", DarkMode.Auto.name)
    private var isDynamicColorOkkv by okkv("is_dynamic_color", def = true)

    val YBUThemeState = mutableStateOf(
        YBUThemeState(
            kotlin.runCatching { YBUThemeMode.valueOf(themeModeOkkv) }
                .getOrElse { YBUThemeMode.Default },
            DarkMode.valueOf(darkModeOkkv),
            isDynamicColorOkkv && isSupportDynamicColor()
        )
    )


    fun changeDarkMode(darkMode: DarkMode) {
        darkModeOkkv = darkMode.name
        YBUThemeState.value = YBUThemeState.value.copy(
            darkMode = darkMode
        )
    }

    fun changeThemeMode(
        themeMode: YBUThemeMode,
        isDynamicColor: Boolean = YBUThemeState.value.isDynamicColor
    ) {
        themeModeOkkv = themeMode.name
        isDynamicColorOkkv = isDynamicColor
        YBUThemeState.value = YBUThemeState.value.copy(
            themeMode = themeMode,
            isDynamicColor = isDynamicColor
        )

    }

    fun changeIsDynamicColor(isDynamicColor: Boolean) {
        // 安卓 12 才有该功能
        val real = (isDynamicColor && isSupportDynamicColor())
        isDynamicColorOkkv = real
        YBUThemeState.value = YBUThemeState.value.copy(
            isDynamicColor = isDynamicColor && isSupportDynamicColor()
        )
    }

    fun isSupportDynamicColor(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }


}