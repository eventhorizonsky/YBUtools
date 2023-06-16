package xyz.ezsky.ybutools.ui.mainpage.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.jsxt.entity.ArtListData
import xyz.ezsky.ybutools.data.xxmh.XxmhNetwork

/**
 * 【列表】页面
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListScreen(navController: NavController) {
    var state by remember { mutableStateOf(0) }
    val titles = newstabs

    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ArtListData?>(null)}
    var jwxtusernameOkkv by okkv("jwxt_username","")
    var islogin=jwxtusernameOkkv!=""
    val current=LocalAppContext.current

    val fetchDataScope = rememberCoroutineScope()
    fun fetchData() {
        isLoading = true
        result=null

        fetchDataScope.launch {
            withContext(Dispatchers.IO) {
            try {

                val response = XxmhNetwork.getartlist(newstabs[state].ID, 10, 1)
                if (response.isSuccess) {
                    result = response.getOrNull()
                }
            } catch (e: Exception) {
                Log.e("test", e.toString())
                // 处理异常
            } finally {
                isLoading = false
            }}
        }
    }

    LaunchedEffect(state) {
        if(islogin){ fetchData()}else{
            result
        }

    }

    Column {

        ScrollableTabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected =state == index,
                    onClick = {state=index},
                    text = { Text(text = title.name, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

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
               Button(onClick = {   navController.navigate("loginpage?appserverID=${YBUjwxt.appID}&appServerName=${current.getString(YBUjwxt.title)}&approuter=${YBUjwxt.router}")}) {
                   Text(text = "前往教务系统登录获取信息")
               }
            }
        LazyColumn{
            if (result!=null){
            items(result!!.datas.cxlmxdggxx.rows) { item ->
                ListItem(
                    modifier = Modifier
                        .clickable { navController.navigate("SeeArt?GGDM=${item.GGDM}") },
                    headlineContent = { },
                    leadingContent = {item.GGBT?.let { Text(it) }
                    },
                    trailingContent = { },

                )
                Divider()
            }}
        }

    }} }

interface newstab{
    val name:String
    val ID:String
}
object xxyw:newstab {
    override val name="学校要闻"
    override val  ID="ca1ebc06db09499c8684111dccd493ae"
}
object bgtz:newstab {
    override val name="办公通知"
    override val  ID="de203df7c42a48bcba9596cceaebd0b9"
}
object xndt:newstab {
    override val name="校内动态"
    override val  ID="d3374d3d036f4d2ca7dac597e962fed8"
}
object dzgk:newstab {
    override val name="党政公开"
    override val  ID="e260101c16b64409ba2ad7f21aaef95a"
}
object xkjs:newstab {
    override val name="学科建设"
    override val  ID="2f717356dd8b43e3a28d6aedf954374b"
}
object dqxx:newstab {
    override val name="党群信息"
    override val  ID="cc67d93cffe541cea3a4fb3fd9b272a0"
}
object rsxx:newstab {
    override val name="人事信息"
    override val  ID="7e19f8e4a6324a57a38f60ec068aceca"
}
object jwxx:newstab {
    override val name="教务信息"
    override val  ID="ca1ebc06db09499c8684111dccd499ae"
}
object kyxx:newstab {
    override val name="科研信息"
    override val  ID="b165aede4b294509bd773ce61291a2ee"
}
object xtxx:newstab {
    override val name="学团信息"
    override val  ID="9a7b55b7e28347d896e1e2556d9f6a7f"
}
object jyxx:newstab {
    override val name="就业信息"
    override val  ID="ca1ebc06db09499c8684111dccd513ae"
}
object yjsxx:newstab {
    override val name="研究生信息"
    override val  ID="42c13a6415d641b6941c02481bc70934"
}
object wsxx:newstab {
    override val name="外事信息"
    override val  ID="ca1ebc06db09499c8684111dccd501ae"
}
object bzfw:newstab {
    override val name="保障服务"
    override val  ID="20ad84719b3e453ea73238a398a8ab4e"
}
val newstabs = listOf(xxyw, bgtz, xndt, dzgk, xkjs, dqxx, rsxx, jwxx, kyxx, xtxx, jyxx, yjsxx, wsxx, bzfw)

