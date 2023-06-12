package xyz.ezsky.ybutools.ui.jwxtapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.getweek
import xyz.ezsky.ybutools.data.jsxt.entity.*
import xyz.ezsky.ybutools.data.jsxt.tools.Parser

@SuppressLint("SuspiciousIndentation")
@Composable
fun CourseSchedule(navController: NavController){
    val coroutineScope = rememberCoroutineScope()
    var coursedetail by  remember { mutableStateOf(arrayListOf<CourseDetailBean>()) }
    var coursebase  by  remember { mutableStateOf(arrayListOf<CourseBaseBean>()) }
    var week  by  remember { mutableStateOf("") }
    var xnxq by  remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }
    var xnxqlist by mutableStateOf<List<String>>(emptyList())
    var coursebaseokkv by okkv("jwxt_coursebase","")
    var coursedetailokkv by okkv("jwxt_coursedetail","")
    if(xnxq==""){
        var xnxqokkv by okkv("jwxt_xnxq","")

        var weekokkv by okkv("jwxt_week","")
        xnxq=xnxqokkv
        week=weekokkv

    }
    if(xnxqlist.isEmpty()){
        var xnxqlistokkv by okkv("jwxt_xnxqlist","")
        val gson = Gson()
        val xnxqListType = object : TypeToken<List<String>>() {}.type
        xnxqlist=gson.fromJson(xnxqlistokkv?:"{}", xnxqListType)
    }
    val gson=Gson()
    val courseDetailListType = object : TypeToken<List<CourseDetailBean>>() {}.type
    coursedetail = gson.fromJson(coursedetailokkv, courseDetailListType)
    val courseBaseListType = object : TypeToken<List<CourseBaseBean>>() {}.type
    coursebase = gson.fromJson(coursebaseokkv, courseBaseListType)
    coursedetail.forEach{item->
        if(item.day==7){
            item.day=0
        }
    }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if(coursedetailokkv==""||coursebaseokkv==""){

                        getweek()
                        JwxtNetwork.getmycourse(xnxq).getOrNull()

                    }

                } catch (e: Exception) {
                    // 处理异常


                }
            }}}
    Column {


    Button(onClick = { showDialog=true }) {
        Text(text = xnxq+":"+week)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(8) { rowIndex ->
            when(rowIndex){
                0->TableCell(data = "/")
                1->TableCell(data = "星期一")
                2->TableCell(data = "星期二")
                3->TableCell(data = "星期三")
                4->TableCell(data = "星期四")
                5->TableCell(data = "星期五")
                6->TableCell(data = "星期六")
                7->TableCell(data = "星期日")

            }

        }
        items(56) { rowIndex ->

                    val cellData = coursedetail.find { it.startNode/2==(rowIndex/8)&&it.day==rowIndex%8-1&&it.startWeek<=10&&10<=it.endWeek }

            if (cellData != null) {
                TableCell(data =coursebase.find{it.id==cellData.id}?.courseName?:"")
            }
                    if(rowIndex%8==0){TableCell(data = "第${rowIndex/8+1}节课")}
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
                LazyColumn {
                    xnxqlist.forEach { yearSemester ->
                        var isSelected = yearSemester == xnxq

                        item{TextButton(
                            onClick = {       xnxq=yearSemester
                                showDialog=false
                                coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    try {
                                            JwxtNetwork.getmycourse(xnxq).getOrNull()



                                    } catch (e: Exception) {
                                        // 处理异常


                                    }
                                }} },
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {       xnxq=yearSemester
                                    showDialog=false
                                    coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            JwxtNetwork.getmycourse(xnxq).getOrNull()



                                        } catch (e: Exception) {
                                            // 处理异常


                                        }
                                    }} }
                            )
                            Text(text = yearSemester)
                        }}
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

class course(
    val name:String,
    val color:String,
    val room:String,
    val teacher:String,

)
@Composable
fun TableCell(data: String) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(Color.LightGray)
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = data)
    }
}
@Composable
fun CourseCell(course: course) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {

        }
    }
}