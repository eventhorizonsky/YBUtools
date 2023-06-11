package xyz.ezsky.ybutools.ui.jwxtapp

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.google.gson.Gson
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.R
import xyz.ezsky.ybutools.data.DatabaseProvider
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.fetchImageAndCache
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.getStudentInfo
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.getgrades
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.loadImageFromCache
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.logout
import xyz.ezsky.ybutools.data.jsxt.entity.Grades
import xyz.ezsky.ybutools.data.jsxt.entity.Gradesinfo
import xyz.ezsky.ybutools.data.jsxt.entity.Student
import xyz.ezsky.ybutools.ui.mainpage.screen.YBUjwxt
import xyz.ezsky.ybutools.ui.toolcompose.ErrorDialog

/**
 * 【教务系统】主页面
 */
@RequiresApi(Build.VERSION_CODES.O)
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
    val imageBitmap: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    var studentinfo by remember { mutableStateOf(Student("", "", "", "", "", "", "", "", "")) }
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
                                    try {
                                        fetchImageAndCache(context)
                                        isLoading = true
                                        getgrades(context)
                                        getStudentInfo(context)
                                        val gson = Gson()
                                        var studentInfoOkkv by okkv("jwxt_studentinfo", "")
                                        // 将字符串转换回对象
                                        val restoredPerson =
                                            gson.fromJson(studentInfoOkkv, Student::class.java)
                                        studentinfo = restoredPerson
                                        grades = gradeDao.getAllGrades()
                                        val creditOkkv by okkv("jwxt_credit", 0.0)
                                        val gpaOkkv by okkv("jwxt_gpa", 0.0)
                                        val averageScoreOkkv by okkv("jwxt_averageScore", 0.0)
                                        gradesinfo =
                                            Gradesinfo(creditOkkv, averageScoreOkkv, gpaOkkv)
                                        isLoading = false

                                    } catch (e: Exception) {
                                        // 处理异常
                                        isError = true
                                        isLoading = false
                                        errortext = e.toString()

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
                            fetchImageAndCache(context)
                            getgrades(context)
                            getStudentInfo(context)
                            val gson = Gson()
                            var studentInfoOkkv by okkv("jwxt_studentinfo", "")
                            // 将字符串转换回对象
                            val restoredPerson = gson.fromJson(studentInfoOkkv, Student::class.java)
                            studentinfo = restoredPerson
                            grades = gradeDao.getAllGrades()
                            val creditOkkv by okkv("jwxt_credit", 0.0)
                            val gpaOkkv by okkv("jwxt_gpa", 0.0)
                            val averageScoreOkkv by okkv("jwxt_averageScore", 0.0)
                            gradesinfo = Gradesinfo(creditOkkv, averageScoreOkkv, gpaOkkv)
                            isLoading = false

                        } else {
                            val gson = Gson()
                            var studentInfoOkkv by okkv("jwxt_studentinfo", "")
                            // 将字符串转换回对象
                            val restoredPerson =
                                gson.fromJson(studentInfoOkkv, Student::class.java)
                            studentinfo = restoredPerson
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
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 绘制圆形头像
                                Box(
                                    modifier = Modifier.size(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 加载图片失败时的占位符
                                    val painter: Painter = rememberImagePainter(
                                        data = loadImageFromCache(context),
                                        builder = {
                                            transformations(CircleCropTransformation())
                                            placeholder(R.drawable.profile) // 设置占位颜色
                                        }
                                    )
                                    Image(
                                        painter = painter,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(100.dp),
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.Center
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // 姓名作为标题，学号作为副标题
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = studentinfo.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "更新时间:",
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.End,
                                            fontStyle = FontStyle.Italic,


                                            )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = studentinfo.studentnumber)
                                        Text(
                                            text = "${studentinfo.updateTime}",
                                            fontSize = 6.sp,
                                            textAlign = TextAlign.End,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }

                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row() {
                                Column(Modifier.padding(8.dp)) {
                                    Text(text = "性别: ${studentinfo.gender}")
                                    Text(text = "民族: ${studentinfo.nation}")
                                    Text(text = "年级: ${studentinfo.grade}")

                                }
                                // 其他信息
                                Column(Modifier.padding(8.dp)) {
                                    Text(text = "学院: ${studentinfo.academy}")
                                    Text(text = "专业: ${studentinfo.professional}")
                                    Text(text = "生日: ${studentinfo.birthdate}")
                                }

                            }

                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier
                            .padding(16.dp, 0.dp, 16.dp, 0.dp)
                            .fillMaxWidth()
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(8.dp,16.dp)
                                .fillMaxWidth(1f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "绩点",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "学分",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "平均分",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                        }
                        Row(
                            modifier = Modifier
                                .padding(8.dp, 0.dp, 8.dp, 16.dp)
                                .fillMaxWidth(1f),

                            ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${gradesinfo.gpa}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${gradesinfo.credit}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary

                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${gradesinfo.averageScore}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }


                }
               item{
                   TabRow(selectedTabIndex = 0) {
                       Tab(
                           selected = true,
                           onClick = {},
                           text = { Text(text = "成绩列表", maxLines = 2, overflow = TextOverflow.Ellipsis) }
                       )

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
