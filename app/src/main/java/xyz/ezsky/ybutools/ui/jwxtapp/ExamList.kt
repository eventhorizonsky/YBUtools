package xyz.ezsky.ybutools.ui.jwxtapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.R
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.getexams
import xyz.ezsky.ybutools.data.jsxt.entity.Exams
import xyz.ezsky.ybutools.ui.mainpage.screen.LocalAppContext
import xyz.ezsky.ybutools.ui.mainpage.screen.YBUjwxt
import xyz.ezsky.ybutools.ui.toolcompose.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Examlist(navController: NavController){
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val current = LocalAppContext.current
    var jwxtusernameOkkv by okkv("jwxt_username", "")
    var islogin = jwxtusernameOkkv != ""
    var examslist by remember { mutableStateOf<List<Exams>>(emptyList()) }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    isLoading=true
                   val data=getexams()
                    if(data.isSuccess){
                        examslist=data.getOrDefault(emptyList())
                    }
                    isLoading=false
                } catch (e: Exception) {
                    // 处理异常


                }
            }}}
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.exam)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "回退"
                    )
                }
            })
    }) { innerPadding ->
        if(islogin){

        LazyColumn(Modifier.padding(innerPadding)) {
            item{
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.background
                    )
                }
            }
            items(examslist) { item ->
                var showDialog by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = { Text(item.courseName) },
                    leadingContent = {
                        Text(text = item.id)
                    },
                    trailingContent = {
                        Text(text = item.examTime)
                    },
                    modifier = Modifier.clickable(onClick = { showDialog = true })

                )
                Divider()
                if (showDialog&&item.courseName!="暂无考试安排") {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(text = item.courseName) },
                        text = {
                            Column() {
                                Text(text = "监考老师： " + item.teacher)
                                Text(text = "考场房间： " + item.examRoom)
                                Text(text = "课程编号： " + item.courseCode)
                                Text(text = "考试场次： " + item.examSession)
                                Text(text = "考试模式： " + item.examMode)
                                Text(text = "校区：     " + item.campus)
                                Text(text = "座位号：   " + item.seatNumber)
                                Text(text = "准考证：   " + item.admissionTicket)
                                Text(text = "备注：     " + item.note)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showDialog = false }) {
                                Text(text = "确定")
                            }
                        }
                    )
                }
            }

        }
    }else{
            ErrorDialog(errorMessage = "尚未登录") {
                navController.popBackStack()
                navController.navigate(
                    "loginpage?appserverID=${YBUjwxt.appID}&appServerName=${
                        current.getString(
                            YBUjwxt.title
                        )
                    }&approuter=${YBUjwxt.router}"
                )
            }
        }
    }

}