package xyz.ezsky.ybutools.ui.toolcompose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import xyz.ezsky.ybutools.R
import xyz.ezsky.ybutools.ui.settingpage.DarkModeItem
import xyz.ezsky.ybutools.ui.settingpage.theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun developing(navController: NavController) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.developing)) },
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
                text ="功能仍在开发中，敬请期待"
            )

        }
    }


}