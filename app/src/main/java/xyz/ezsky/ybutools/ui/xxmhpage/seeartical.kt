package xyz.ezsky.ybutools.ui.xxmhpage

import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.jsxt.entity.ArticleResponse
import xyz.ezsky.ybutools.data.xxmh.XxmhNetwork

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SeeArt(navController: NavController,GGDM:String) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ArticleResponse?>(null)}
    LaunchedEffect(Unit) {

            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        isLoading=true
                        var respose = XxmhNetwork.test(GGDM)
                        if(respose.isSuccess){
                            val gson = Gson()
                            result = gson.fromJson(respose.getOrNull(), ArticleResponse::class.java)
                        isLoading=false
                        }else{
                            isLoading=false
                        }
                    } catch (e: Exception) {
                        Log.e("test",e.toString())
                        isLoading=false
                        // 处理异常
                    }
                }

        }}
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(text = "文章详情") },
                navigationIcon = {
                    IconButton(onClick = { result=null
                        navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "回退"
                        )
                    }
                },
                actions = {


                }

            )
        }) { innerPadding ->
    Column(Modifier.padding(innerPadding)) {

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

    LazyColumn {

        item {
            if(result!=null){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // 标题
                    Text(
                        text = result!!.data.GG_DATA.GGBT,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    // 发布信息和阅读数
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 发布信息
                        Text(
                            text = result!!.data.GG_DATA.FBBMJC+"  "+result!!.data.GG_DATA.FBSJ,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )

                        // 阅读数
                        Text(
                            text = result!!.data.GG_DATA.YDS,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    // 分割线
                    Divider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    HtmlWebView(htmlContent = result!!.data.GG_DATA.GGNR, context = LocalContext.current) }
            }
        }
    }}} }
}
@Composable
fun HtmlWebView(htmlContent: String,context: Context) {

    var currentHtmlContent by remember { mutableStateOf(htmlContent) }
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true // 启用 JavaScript
            settings.loadsImagesAutomatically = true // 自动加载图片
            settings.domStorageEnabled = true // 启用 DOM Storage
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 支持混合内容，包括 HTTPS 和 HTTP 的图片加载
        }
    }

    // 当 htmlContent 发生变化时更新 WebView 的内容
    LaunchedEffect(htmlContent) {
        currentHtmlContent = htmlContent
        webView.loadDataWithBaseURL(null,"<style>\n" +
                "    img {\n" +
                "        max-width: 100%;\n" +
                "        max-height: 100%;\n" +
                "    }\n" +
                "</style>$htmlContent", "text/html", "UTF-8", null)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { webView }
    )
}

