package xyz.ezsky.ybutools.ui.mainpage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import xyz.ezsky.ybutools.ui.Home
import xyz.ezsky.ybutools.ui.YBUDestination
import xyz.ezsky.ybutools.ui.YBUDestinationSaver
import xyz.ezsky.ybutools.ui.rallyTabRowScreens

/**
 * APP主页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mainpage(navController: NavController) {

    var currentScreen: YBUDestination by rememberSaveable(
        stateSaver = YBUDestinationSaver()
    ) { mutableStateOf(Home) }


    Scaffold(topBar = {
        if (currentScreen.route == "mine") {
            TopAppBar(title = { Text(text = stringResource(id = currentScreen.stringResource)) },
                actions = {
                    IconButton(onClick = { navController.navigate("Setting") }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "设置"
                        )
                    }
                })
        } else {
            TopAppBar(title = {
                Text(text = stringResource(id = currentScreen.stringResource))
            }
            )
        }
    }, bottomBar = {
        NavigationBar {
            val navItem = rallyTabRowScreens
            navItem.forEach { item ->
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    selected = currentScreen == item,
                    onClick = { currentScreen = item },
                    icon = { Icon(item.icon, contentDescription = item.route) },
                    label = { Text(stringResource(id = item.stringResource)) },
                    alwaysShowLabel = false,
                )
            }

        }
    }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {

            currentScreen.screen(navController)


        }
    }
}