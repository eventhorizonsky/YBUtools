package xyz.ezsky.ybutools.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import xyz.ezsky.ybutools.ui.theme.colors.DefaultColor
import xyz.ezsky.ybutools.ui.theme.colors.GreenappleColor
import xyz.ezsky.ybutools.ui.theme.colors.MidnightduskColor
import xyz.ezsky.ybutools.ui.theme.colors.StrawberryColor
import xyz.ezsky.ybutools.ui.theme.colors.TakoColor
import xyz.ezsky.ybutools.ui.theme.colors.TealturqoiseColor
import xyz.ezsky.ybutools.R


/**
 * Created by HeYanLe on 2023/2/18 22:47.
 * https://github.com/heyanLE
 */

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

fun YBUThemeMode.getColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) darkColorScheme else lightColorScheme
}
enum class YBUThemeMode(
    val darkColorScheme: ColorScheme,
    val lightColorScheme: ColorScheme,
    val titleResId: Int,
    val darkElevationOverlay: Color = Color.Unspecified,
    val lightElevationOverlay: Color = Color.Unspecified,
) {

    Default(
        DefaultColor.Dark.colorScheme,
        DefaultColor.Light.colorScheme,
        R.string.theme_default
    ),

    Greenapple(
        GreenappleColor.Dark.colorScheme,
        GreenappleColor.Light.colorScheme,
        R.string.theme_greenapple
    ),

    Midnightdusk(
        MidnightduskColor.Dark.colorScheme,
        MidnightduskColor.Light.colorScheme,
        R.string.theme_midnightdusk,
        MidnightduskColor.Dark.elevationOverlay,
        MidnightduskColor.Light.elevationOverlay,
    ),

    Strawberry(
        StrawberryColor.Dark.colorScheme,
        StrawberryColor.Light.colorScheme,
        R.string.theme_strawberry,
    ),



    Tako(
        TakoColor.Dark.colorScheme,
        TakoColor.Light.colorScheme,
        R.string.theme_tako,
        TakoColor.Dark.elevationOverlay,
        TakoColor.Light.elevationOverlay
    ),

    Tealturqoise(
        TealturqoiseColor.Dark.colorScheme,
        TealturqoiseColor.Light.colorScheme,
        R.string.theme_tealturqoise,
        TealturqoiseColor.Dark.elevationOverlay,
        TealturqoiseColor.Light.elevationOverlay
    )

}
