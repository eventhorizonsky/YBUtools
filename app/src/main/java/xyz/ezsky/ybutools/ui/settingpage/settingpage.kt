package xyz.ezsky.ybutools.ui.settingpage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import xyz.ezsky.ybutools.R
import xyz.ezsky.ybutools.ui.checkForUpdate
import xyz.ezsky.ybutools.ui.theme.DarkMode
import xyz.ezsky.ybutools.ui.theme.YBUThemeMode
import xyz.ezsky.ybutools.ui.theme.YBUThemeController

/**
 * 设置页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun setting(navController: NavController) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.setting)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "回退"
                    )
                }
            })
    }) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                text = stringResource(id = R.string.dark_set),
                color = MaterialTheme.colorScheme.primary
            )
            DarkModeItem()
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                text = stringResource(id = R.string.theme),
                color = MaterialTheme.colorScheme.primary
            )
            theme()
            VersionScreen()
        }

    }


}
@Composable
fun VersionScreen() {
    val context = LocalContext.current
    val version = getVersionName(context)
    val githubUrl = "https://github.com/eventhorizonsky/YBUtools/releases"
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    var checkUpdate by remember { mutableStateOf(false) }
    if(checkUpdate){
        checkForUpdate(false){
            checkUpdate=false
        }
    }
    LazyColumn {
        item {
            ListItem(
                modifier = Modifier
                    .clickable {
                        checkUpdate=true
                    },
                headlineContent = { },
                leadingContent = {
                    Text("版本号")
                },
                trailingContent = {
                                    Text(version)
                },

                )
        }
    }
}

fun getVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }
}

@Composable
fun DarkModeItem() {
    val theme = YBUThemeController.YBUThemeState.value
    val list = listOf(
        Triple(Icons.Filled.Android, stringResource(R.string.dark_auto), DarkMode.Auto),
        Triple(Icons.Filled.WbSunny, stringResource(R.string.dark_off), DarkMode.Light),
        Triple(Icons.Filled.NightsStay, stringResource(R.string.dark_on), DarkMode.Dark)
    )

    val enableColor = MaterialTheme.colorScheme.primary
    val disableColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    ) {
        list.forEachIndexed { index, (image, text, mode) ->
            val currentColor = if (theme.darkMode == mode) enableColor else disableColor
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .border(
                        width = 1.dp,
                        color = currentColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        if (theme.darkMode != mode) {
                            YBUThemeController.changeDarkMode(mode)
                        }
                    }
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = image,
                    contentDescription = text,
                    tint = currentColor,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(text = text, color = currentColor)
            }
        }
    }


}

@Composable
fun theme() {
    val theme = YBUThemeController.YBUThemeState.value
    val isDark = when (theme.darkMode) {
        DarkMode.Dark -> true
        DarkMode.Light -> false
        DarkMode.Auto -> isSystemInDarkTheme()
    }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp)
    ) {
        items(YBUThemeMode.values()) { item ->
            ThemePreviewItem(
                selected = !theme.isDynamicColor && theme.themeMode == item,
                colorScheme = if (isDark) item.darkColorScheme else item.lightColorScheme,
                stringResource(id = item.titleResId)
            ) {
                YBUThemeController.changeThemeMode(item, false)
            }
        }

    }
}

@Composable
fun ThemePreviewItem(
    selected: Boolean,
    colorScheme: ColorScheme,
    title: String,
    onClick: () -> Unit,
) {
    val dividerColor = colorScheme.onSurface.copy(alpha = 0.2f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(9f / 16f)
                .border(
                    width = 4.dp,
                    color = if (selected) {
                        colorScheme.primary
                    } else {
                        dividerColor
                    },
                    shape = RoundedCornerShape(17.dp),
                )
                .padding(4.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(colorScheme.background)
                .clickable(onClick = onClick),
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .weight(0.7f)
                        .padding(end = 4.dp)
                        .background(
                            color = colorScheme.onSurface,
                            shape = MaterialTheme.shapes.small,
                        ),
                )

                Box(
                    modifier = Modifier.weight(0.3f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(R.string.theme),
                            tint = colorScheme.primary,
                        )
                    }
                }
            }

            // Cover
            Box(
                modifier = Modifier
                    .padding(start = 8.dp, top = 2.dp)
                    .background(
                        color = dividerColor,
                        shape = MaterialTheme.shapes.small,
                    )
                    .fillMaxWidth(0.5f)
                    .aspectRatio(19 / 27F),
            ) {
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(width = 24.dp, height = 16.dp)
                        .clip(RoundedCornerShape(5.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(12.dp)
                            .background(colorScheme.tertiary),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(12.dp)
                            .background(colorScheme.secondary),
                    )
                }
            }

            // Bottom bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    tonalElevation = 3.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .fillMaxWidth()
                            .background(colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(17.dp)
                                .background(
                                    color = colorScheme.primary,
                                    shape = CircleShape,
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .alpha(0.6f)
                                .height(17.dp)
                                .weight(1f)
                                .background(
                                    color = colorScheme.onSurface,
                                    shape = MaterialTheme.shapes.small,
                                ),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
    }

}