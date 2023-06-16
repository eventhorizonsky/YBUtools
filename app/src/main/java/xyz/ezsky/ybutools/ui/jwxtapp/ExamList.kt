package xyz.ezsky.ybutools.ui.jwxtapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.getexams
import xyz.ezsky.ybutools.data.jsxt.entity.Exams

@Composable
fun Examlist(navController: NavController){
    val coroutineScope = rememberCoroutineScope()
    var examslist by remember { mutableStateOf<List<Exams>>(emptyList()) }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                   val data=getexams()
                    if(data.isSuccess){
                        examslist=data.getOrDefault(emptyList())
                    }
                } catch (e: Exception) {
                    // 处理异常


                }
            }}}
    LazyColumn {
        item{ Text(text = "考试安排")}
    items(examslist) { item ->
        ListItem(
            headlineContent = { Text(item.courseName) },
            leadingContent = {
                Text(text = item.id)
            },
            trailingContent = {
                Column {
                    Text(text = item.teacher)
                    Text(text = item.examTime)
                    Text(text = item.examRoom)
                    Text(text = item.courseCode)
                    Text(text = item.examSession)
                    Text(text = item.examMode)
                    Text(text = item.campus)
                    Text(text = item.seatNumber)
                    Text(text = item.admissionTicket)
                    Text(text = item.note)
                }
            }
        )
        Divider()
    }

}
}