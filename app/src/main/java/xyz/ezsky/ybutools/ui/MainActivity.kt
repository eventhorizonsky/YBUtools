package xyz.ezsky.ybutools.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.forums.ForumsNetwork.Companion.getlastapp
import xyz.ezsky.ybutools.data.forums.YbuApp
import xyz.ezsky.ybutools.ui.mainpage.screen.LocalAppContext
import xyz.ezsky.ybutools.ui.settingpage.getVersionName
import xyz.ezsky.ybutools.ui.theme.YBUtoolsTheme
import xyz.ezsky.ybutools.ui.toolcompose.ErrorDialog
import xyz.ezsky.ybutools.ui.toolcompose.UpdateDialog
import java.lang.Integer.min


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initOkkv(this)
        val appContext = applicationContext // 获取应用的 Context 对象
        setContent {

            YBUtoolsTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalAppContext provides appContext) {
                        YBUapp()
                        checkForUpdate(true){}
                    }


                }
            }
        }
    }

}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun checkForUpdate(checkNecessity: Boolean,
                   onDismiss: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var readDialog by remember { mutableStateOf(false) }
    var lastAppInfo by remember { mutableStateOf(YbuApp()) }
    val viewModelScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentAppVersion = getVersionName(context)
    if (showDialog) {
        UpdateDialog(currentAppVersion,lastAppInfo) {
            showDialog = !showDialog
            onDismiss()
        }
    }
    if(!readDialog){
        viewModelScope.launch(Dispatchers.IO) {
            val result = getlastapp()
            if (result.isSuccess) {
                val latestAppVersion = result.getOrNull()?.version ?: 0 // 从 YbuApp 中获取版本号
                Log.e("tag", latestAppVersion.toString())
                Log.e("tag2", currentAppVersion)
                if (!isLocalVersionGreaterThan(latestAppVersion as String, currentAppVersion)) {
                    if(result.getOrNull()?.isMandatory.equals("Y")||!checkNecessity){
                        // 版本更新可用，显示更新弹窗
                        showDialog = true
                        readDialog = !readDialog
                        lastAppInfo=result.getOrNull()?: YbuApp()
                    }
                } else {
                    Log.e(
                        "tag3",
                        isLocalVersionGreaterThan(
                            latestAppVersion as String,
                            currentAppVersion
                        ).toString()
                    )
                }

            } else {
                // 处理接口调用失败的情况
            }
        }
    }

}

private fun isLocalVersionGreaterThan(last: String, local: String): Boolean {
    val localVersionParts = local.split(".")
    val lastVersionParts = last.split(".")

    val minLength = min(localVersionParts.size, lastVersionParts.size)

    for (i in 0 until minLength) {
        val localPart = localVersionParts[i].toIntOrNull()
        val lastPart = lastVersionParts[i].toIntOrNull()

        if (localPart != null && lastPart != null) {
            if (localPart > lastPart) {
                return true
            } else if (localPart < lastPart) {
                return false
            }
        } else {
            // Handle invalid version format (e.g., non-numeric parts)
            return false
        }
    }

    // If we reach here, it means the common parts are equal
    // If local version has more parts, consider it greater
    return localVersionParts.size >= lastVersionParts.size
}


@Composable
// 显示更新弹窗（示例）
private fun showUpdateDialog(message: String) {
    ;
}


//初始化okkv
private fun initOkkv(Activity: Activity) {
    Okkv.Builder(MMKVStore(Activity)).cache().build().init().default()
    // 如果不使用缓存，请手动指定 key
    Okkv.Builder(MMKVStore(Activity)).build().init().default("no_cache")
}
