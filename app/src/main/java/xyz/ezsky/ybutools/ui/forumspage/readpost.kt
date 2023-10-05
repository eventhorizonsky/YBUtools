package xyz.ezsky.ybutools.ui.forumspage

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.R
import xyz.ezsky.ybutools.data.forums.ForumsNetwork.Companion.getpostfromid
import xyz.ezsky.ybutools.data.jsxt.entity.ArticleResponse
import xyz.ezsky.ybutools.data.jsxt.entity.ForumsPost
import xyz.ezsky.ybutools.data.jsxt.entity.forumresult
import xyz.ezsky.ybutools.data.xxmh.XxmhNetwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun readpost(navController: NavController,id:String) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ForumsPost?>(null) }
    LaunchedEffect(Unit) {

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    isLoading=true
                    var respose = getpostfromid(id)
                    if(respose.isSuccess){
                        result = respose.getOrNull()
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
            title = { Text(text = result?.title?:"加载中") },
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
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    text = result?.description?:""
                )
            }


        }
    }


}