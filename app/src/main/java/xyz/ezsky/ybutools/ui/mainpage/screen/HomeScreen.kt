package xyz.ezsky.ybutools.ui.mainpage.screen

import LazyLoadMoreColum
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork
import xyz.ezsky.ybutools.data.jsxt.entity.ArtListData
import xyz.ezsky.ybutools.data.jsxt.entity.CourseBaseBean
import xyz.ezsky.ybutools.data.jsxt.entity.CourseDetailBean
import xyz.ezsky.ybutools.data.jsxt.entity.Row
import xyz.ezsky.ybutools.data.jsxt.tools.calculateWeekNumber
import xyz.ezsky.ybutools.data.jsxt.tools.stringToLocalDate
import xyz.ezsky.ybutools.data.xxmh.XxmhNetwork
import xyz.ezsky.ybutools.ui.jwxtapp.CourseCell
import xyz.ezsky.ybutools.ui.jwxtapp.TableCell
import xyz.ezsky.ybutools.ui.toolcompose.Banner
import xyz.ezsky.ybutools.ui.toolcompose.BannerData
import java.time.LocalDate
import java.util.*


/**
 * 【首页】页面
 */
@OptIn(ExperimentalPagerApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {
    val bannerdata = BannerData(
        "https://www.ezsky.xyz/wp-content/uploads/2023/06/%E5%A3%81%E7%BA%B8-1536x864.jpg",
        ""
    )
    val imageUrls = listOf(
        bannerdata, bannerdata, bannerdata
    )
    var isLoading by remember { mutableStateOf(false) }
    var jwxtusernameOkkv by okkv("jwxt_username", "")
    var islogin = jwxtusernameOkkv != ""
    val current = LocalAppContext.current
    LazyColumn {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(10.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) { Banner(imageUrls, 30000) }
        }

        item {
            Row(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val applist = listOf(
                    app(Icons.Filled.Dataset, "我的课表") {
                        navController.navigate("CourseSchedule")
                    },
                    app(Icons.Filled.DateRange, "考试安排") { navController.navigate("Examlist") },
                    app(Icons.Filled.DateRange, "登录网络") { navController.navigate("JwxtHome") },
                    app(Icons.Filled.DateRange, "教务系统") { navController.navigate("JwxtHome") },
                )
                applist.forEach {
                    appitem(icon = it.appicon, text = it.appname) { it.action() }
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.background
                    )
                }
                if (!islogin) {
                    Button(onClick = {
                        navController.navigate(
                            "loginpage?appserverID=${YBUjwxt.appID}&appServerName=${
                                current.getString(
                                    YBUjwxt.title
                                )
                            }&approuter=${YBUjwxt.router}"
                        )
                    }) {
                        Text(text = "前往教务系统登录获取信息")
                    }
                }
            }
        }
        item{
            Column(Modifier
                .padding(20.dp)) {
                val coroutineScope = rememberCoroutineScope()
                var coursedetail by remember { mutableStateOf(arrayListOf<CourseDetailBean>()) }
                var coursebase by remember { mutableStateOf(arrayListOf<CourseBaseBean>()) }
                var jwxtusernameOkkv by okkv("jwxt_username", "")
                var islogin = jwxtusernameOkkv != ""
                var coursebaseokkv by okkv("jwxt_coursebase", "")
                var coursedetailokkv by okkv("jwxt_coursedetail", "")
                var xnxq by remember { mutableStateOf("") }
                var week by remember { mutableStateOf(0) }
                var xnxqlist by mutableStateOf<List<String>>(emptyList())
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
                                JwxtNetwork.getweek()
                                var xnxqokkv by okkv("jwxt_xnxq", "")
                                var weekokkv by okkv("jwxt_week", "2023-07-03")
                                xnxq=xnxqokkv
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
                val context = LocalContext.current
                val calendar = Calendar.getInstance()
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                // 将星期天（Calendar.SUNDAY）的值由1映射为7
                val weekday = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
                val cellData =
                    coursedetail.filter {it.day == weekday-1 && it.startWeek <= week && week <= it.endWeek }
                Text(text = "今日课表", fontWeight = FontWeight.Bold)
                if (cellData != null) {
                    cellData.forEach{item->
                        val coursetime=when(item.startNode/2+1){
                            1 -> "08:00-09:35"
                            2 -> "09:55-11:30"
                            3 -> "13:00-14:35"
                            4 -> "14:55-16:30"
                            5 ->  "17:30-19:05"
                            6 -> "19:25-21:00"

                            else -> {""}
                        }
                        coursecell(data = coursebase.find { it.id == item.id },item,coursetime)}

                }

            }
        }

    }


}
@Composable
fun coursecell(data: CourseBaseBean?, baseBean: CourseDetailBean,time:String){
    if (data != null) {
    Card(modifier = Modifier
        .padding(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Box(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primary)
                    .width(4.dp)
                    .size(80.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Column{
                    Text(
                        text =data.courseName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text =time,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Text(
                        text =""+baseBean.room,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }}
        }
    }}
}

data class app(
    val appicon: ImageVector,
    val appname: String,
    val action: () -> Unit
)

@Composable
fun appitem(icon: ImageVector, text: String, onclick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
    ) {
        Card(
            modifier = Modifier
                .size(50.dp)
                .clickable { onclick() },
            shape = RoundedCornerShape(5.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Button Icon",
                    modifier = Modifier.size(30.dp)
                )
            }
        }


        Text(
            text = text,
            modifier = Modifier.padding(top = 5.dp),
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp)
        )
    }
}