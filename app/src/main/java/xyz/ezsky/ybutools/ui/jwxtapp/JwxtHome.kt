package xyz.ezsky.ybutools.ui.jwxtapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.R
import xyz.ezsky.ybutools.data.DatabaseProvider
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.getgrades
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.logout
import xyz.ezsky.ybutools.data.jsxt.entity.Grades
import xyz.ezsky.ybutools.data.jsxt.entity.Gradesinfo
import xyz.ezsky.ybutools.ui.mainpage.screen.YBUjwxt
import xyz.ezsky.ybutools.ui.toolcompose.ErrorDialog

/**
 * 【教务系统】主页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JwxtHome(navController: NavController) {
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errortext by rememberSaveable { mutableStateOf("") }
    var grades by remember { mutableStateOf<List<Grades>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var isMenuExpanded by remember { mutableStateOf(false) }
    var gradesinfo by remember { mutableStateOf(Gradesinfo(0.0, 0.0, 0.0)) }
    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)
    val gradeDao = database.gradeDao()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.ybujwxt)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "回退"
                    )
                }
            },
            actions = {

                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "菜单"
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(text = "更新数据")
                        },
                        onClick = {
                            isMenuExpanded = false
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    isLoading = true
                                    val result = getgrades(context)
                                    if (result.isSuccess) {
                                        grades = gradeDao.getAllGrades()
                                        val creditOkkv by okkv("jwxt_credit", 0.0)
                                        val gpaOkkv by okkv("jwxt_gpa", 0.0)
                                        val averageScoreOkkv by okkv("jwxt_averageScore", 0.0)
                                        gradesinfo =
                                            Gradesinfo(creditOkkv, averageScoreOkkv, gpaOkkv)
                                        isLoading = false
                                    }
                                }
                            }
                        })
                    DropdownMenuItem(
                        text = {
                            Text(text = "登出")
                        },
                        onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    logout(context)
                                    YBUjwxt.islogin = false
                                    withContext(Dispatchers.Main) { // 使用 Dispatchers.Main 切换到主线程
                                        navController.popBackStack()
                                        navController.navigateUp()
                                    }
                                }
                            }

                        })
                }

            }

        )
    }) { innerPadding ->
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        isLoading = true
                        if (gradeDao.getGradesCount() <= 0) {

                            val result = getgrades(context)
                            if (result.isSuccess) {
                                grades = gradeDao.getAllGrades()
                                val creditOkkv by okkv("jwxt_credit", 0.0)
                                val gpaOkkv by okkv("jwxt_gpa", 0.0)
                                val averageScoreOkkv by okkv("jwxt_averageScore", 0.0)
                                gradesinfo = Gradesinfo(creditOkkv, averageScoreOkkv, gpaOkkv)
                                isLoading = false
                            }
                        } else {
                            grades = gradeDao.getAllGrades()
                            val creditOkkv by okkv("jwxt_credit", 0.0)
                            val gpaOkkv by okkv("jwxt_gpa", 0.0)
                            val averageScoreOkkv by okkv("jwxt_averageScore", 0.0)
                            gradesinfo = Gradesinfo(creditOkkv, averageScoreOkkv, gpaOkkv)
                            isLoading = false
                        }
                        // 处理登录结果
                    } catch (e: Exception) {
                        // 处理异常
                        isError = true
                        isLoading = false
                        errortext = e.toString()

                    }
                }
            }
        }
        Column(Modifier.padding(innerPadding)) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.background
                )
            }


            LazyColumn {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "绩点: ${gradesinfo.gpa}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "学分: ${gradesinfo.credit}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "平均分: ${gradesinfo.averageScore}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                items(grades) { item ->
                    ListItem(
                        headlineContent = { Text(item.courseName) },
                        leadingContent = {
                            Text(text = item.listid)
                        },
                        trailingContent = { Text(text = item.score) }
                    )
                    Divider()
                }
            }
            if (isError) {
                ErrorDialog(
                    errorMessage = errortext,
                    onDismiss = { isError = false }
                )
            }
        }
    }


}
