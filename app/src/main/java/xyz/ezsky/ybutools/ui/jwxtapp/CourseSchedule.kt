package xyz.ezsky.ybutools.ui.jwxtapp

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.getweek
import xyz.ezsky.ybutools.data.jsxt.entity.*
import xyz.ezsky.ybutools.data.jsxt.tools.calculateWeekNumber
import xyz.ezsky.ybutools.data.jsxt.tools.stringToLocalDate
import xyz.ezsky.ybutools.ui.mainpage.screen.LocalAppContext
import xyz.ezsky.ybutools.ui.mainpage.screen.YBUjwxt
import xyz.ezsky.ybutools.ui.toolcompose.ErrorDialog
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@SuppressLint("SuspiciousIndentation")
@Composable
fun CourseSchedule(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var xnxqlist by mutableStateOf<List<String>>(emptyList())
    var xnxq by remember { mutableStateOf("") }
    var week by remember { mutableStateOf(0) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    var skipPartiallyExpanded by remember { mutableStateOf(false) }
    var edgeToEdgeEnabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    var jwxtusernameOkkv by okkv("jwxt_username", "")
    var islogin = jwxtusernameOkkv != ""
    val current = LocalAppContext.current
    if (islogin) {
        fun changexnxq(yearSemester: String) {
            isLoading = true
            showDialog = false
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        JwxtNetwork.getmycourse(yearSemester).getOrNull()
                        var xnxqokkv by okkv("jwxt_xnxq", "")
                        xnxqokkv = yearSemester
                        xnxq = yearSemester
                        isLoading = false


                    } catch (e: Exception) {
                        // 处理异常


                    }
                }
            }
        }

        if(week==0){
            var weekokkv by okkv("jwxt_week", "2023-07-03")
            val currentDate = LocalDate.now()
            week = calculateWeekNumber(stringToLocalDate(weekokkv), currentDate)
        }
        var pagerState = rememberPagerState(18,week-1)
        fun initdate() {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        isLoading = true
                        getweek()
                        var xnxqokkv by okkv("jwxt_xnxq", "")
                        var weekokkv by okkv("jwxt_week", "2023-07-03")
                        changexnxq(xnxqokkv)
                        val currentDate = LocalDate.now()
                        week = calculateWeekNumber(stringToLocalDate(weekokkv), currentDate)
                        pagerState.scrollToPage(week-1)
                        JwxtNetwork.getmycourse(xnxqokkv).getOrNull()
                        isLoading = false

                    } catch (e: Exception) {
                        // 处理异常
                    }
                }
            }
        }
        if (xnxq == "") {
            var xnxqokkv by okkv("jwxt_xnxq", "")
            xnxq = xnxqokkv
        }
        if (xnxqlist.isEmpty()) {
            var xnxqlistokkv by okkv("jwxt_xnxqlist", "")
            val gson = Gson()
            val xnxqListType = object : TypeToken<List<String>>() {}.type
            if (xnxqlistokkv != "") {
                xnxqlist = gson.fromJson(xnxqlistokkv, xnxqListType)
            } else {
                initdate()
            }

        }

        Scaffold(topBar = {
            TopAppBar(
                title = { Text(text = "${xnxq}   第${pagerState.currentPage+1}周") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "回退"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { openBottomSheet = !openBottomSheet }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        }) { innerPadding ->

            Column(Modifier.padding(innerPadding)) {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.background
                    )
                }

                HorizontalPager(state = pagerState){ page ->
                    courseSch(xnxq, page+1)

                }

                if (openBottomSheet) {
                    val windowInsets = if (edgeToEdgeEnabled)
                        WindowInsets(0) else BottomSheetDefaults.windowInsets

                    ModalBottomSheet(
                        onDismissRequest = { openBottomSheet = false },
                        sheetState = bottomSheetState,
                        windowInsets = windowInsets
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp), horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "设置", style = MaterialTheme.typography.titleLarge)
                        }
                        Divider()
                        LazyColumn {
                            item {
                                ListItem(
                                    headlineContent = { Text("当前学期为 $xnxq") },
                                    trailingContent = {
                                        Button(onClick = { showDialog = true }) {
                                            Text(text = "设置")
                                        }
                                    }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { Text("当前周为 $week") },
                                    trailingContent = {
                                        var expanded by remember { mutableStateOf(false) }
                                        Button(onClick = { expanded= true }) {
                                            Text(text = "设置")
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            for (number in 1..18) {
                                                DropdownMenuItem(
                                                    text = {Text(text = number.toString())},
                                                    onClick = {
                                                        week = number
                                                        expanded = false
                                                        coroutineScope.launch {
                                                        withContext(Dispatchers.IO) {
                                                            pagerState.scrollToPage(number-1)
                                                        }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                            item {
                                Button(onClick = { initdate() }) {
                                    Text(text = "重置")
                                }
                            }
                        }
                        Column(Modifier.height(50.dp)) {

                        }


                    }
                }
            }



            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(text = "选择学年学期")
                    },
                    text = {
                        LazyColumn(Modifier.height(300.dp)) {
                            xnxqlist.forEach { yearSemester ->
                                var isSelected = yearSemester == xnxq

                                item {
                                    TextButton(
                                        onClick = {
                                            changexnxq(yearSemester)
                                        },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { changexnxq(yearSemester) }
                                        )
                                        Text(text = yearSemester)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {

                    },
                    dismissButton = {

                    }
                )
            }

        }
    } else {
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


@Composable
fun courseSch(xnxq: String = "", week: Int = 1) {
    val coroutineScope = rememberCoroutineScope()
    var coursedetail by remember { mutableStateOf(arrayListOf<CourseDetailBean>()) }
    var coursebase by remember { mutableStateOf(arrayListOf<CourseBaseBean>()) }
    var jwxtusernameOkkv by okkv("jwxt_username", "")
    var islogin = jwxtusernameOkkv != ""
    var coursebaseokkv by okkv("jwxt_coursebase", "")
    var coursedetailokkv by okkv("jwxt_coursedetail", "")
    fun getCourse() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {

                    JwxtNetwork.getmycourse(xnxq).getOrNull()

                } catch (e: Exception) {
                    // 处理异常
                }
            }
        }
    }
    if (coursedetailokkv == "" || coursebaseokkv == "") {
        LaunchedEffect(Unit) {
            getCourse()
        }
    } else {
        val gson = Gson()
        val courseDetailListType = object : TypeToken<List<CourseDetailBean>>() {}.type
        coursedetail = gson.fromJson(coursedetailokkv, courseDetailListType)
        val courseBaseListType = object : TypeToken<List<CourseBaseBean>>() {}.type
        coursebase = gson.fromJson(coursebaseokkv, courseBaseListType)
        coursedetail.forEach { item ->
            if (item.day == 7) {
                item.day = 0
            }

        }
    }


    LaunchedEffect(xnxq) {
        if (islogin) {
            getCourse()
        }

    }


    Column {

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(8) { rowIndex ->
                when (rowIndex) {
                    0 -> WeekCell(data = " ")
                    1 -> WeekCell(data = "一")
                    2 -> WeekCell(data = "二")
                    3 -> WeekCell(data = "三")
                    4 -> WeekCell(data = "四")
                    5 -> WeekCell(data = "五")
                    6 -> WeekCell(data = "六")
                    7 -> WeekCell(data = "日")

                }

            }
            items(56) { rowIndex ->

                val cellData =
                    coursedetail.find { it.startNode / 2 == (rowIndex / 8) && it.day == rowIndex % 8 - 1 && it.startWeek <= week && week <= it.endWeek }

                if (cellData != null) {
                    TableCell(data = coursebase.find { it.id == cellData.id }, cellData)
                }
                if (rowIndex % 8 == 0) {
                    when (rowIndex / 8 + 1) {
                        1 -> CourseCell("1", "08:00", "09:35")
                        2 -> CourseCell("2", "09:55", "11:30")
                        3 -> CourseCell("3", "13:00", "14:35")
                        4 -> CourseCell("4", "14:55", "16:30")
                        5 -> CourseCell("5", "17:30", "19:05")
                        6 -> CourseCell("6", "19:25", "21:00")

                    }
                }
            }

        }
    }
}

@Composable
fun TableCell(data: CourseBaseBean?, baseBean: CourseDetailBean) {
    var showDialog by remember { mutableStateOf(false) }
    if (data != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(2.dp)
                .clickable { showDialog = true }
        )
        {
            Text(
                text = data.courseName,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(5.dp),
                fontSize = 12.sp
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = data.courseName) },
                text = {
                    Column() {
                        Text(text = "授课老师：    " + baseBean.teacher)
                        Text(text = "上课房间：    " + baseBean.room)
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

@Composable
fun WeekCell(data: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = data)
    }
}

@Composable
fun CourseCell(course: String, starttime: String, endtime: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = course)
            Text(text = starttime, fontSize = 8.sp)
            Text(text = endtime, fontSize = 8.sp)
        }
    }
}