package xyz.ezsky.ybutools.ui.toolcompose

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.forums.YbuApp
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
//下载id
private var downloadId=-1L

private const val REQUEST_CODE_UNKNOWN_APP = 100
//下载apk

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("RememberReturnType", "CoroutineCreationDuringComposition")
@Composable
fun upgradeApk(context: Context, upgradeInfo: YbuApp,onDismiss: () -> Unit){

    //设置apk下载地址：本机存储的download文件夹下
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    upgradeInfo.url="http://192.168.123.104:8080"+upgradeInfo.url
    //找到该路径下的对应名称的apk文件，有可能已经下载过了
    val file = File(dir, "${upgradeInfo.url?.split("/")?.last()}")
    // 处理下载中状态，显示下载进度
    var progress by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    var downloaded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("下载中...") },
        text = {
            if(downloaded){
                Text("下载完成，请安装")
            }else{
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LinearProgressIndicator(progress = progress.toFloat() / 100)
                    Text(" $progress%", modifier = Modifier.padding(top = 32.dp))
                }
            }

        },
        confirmButton = {},
        dismissButton = {
            Button(
                onClick = {
                    // TODO: 取消下载
                    onDismiss()
                }
            ) {
                Text("取消")
            }
        }
    )

    //开辟线程
    coroutineScope.launch {
        withContext(Dispatchers.IO) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // 1、判断是否下载过apk
        if (file.exists()) {
            downloaded=true
            val authority: String = context.applicationContext.packageName+ ".fileProvider"
            // "content://" 类型的uri   --将"file://"类型的uri变成"content://"类型的uri
            val uri = FileProvider.getUriForFile(context, authority, file)
            // 5、安装apk, content://和file://路径都需要
            installAPK(context,uri,file)
        }else{
            // 处理下载中状态，显示下载进度
            val totalSize = getDownloadTotalSize(upgradeInfo.url)
            // 2、DownloadManager配置
            val request = DownloadManager.Request(Uri.parse(upgradeInfo.url?:"" ))  //处理中文下载地址
            // 设置下载路径和下载的apk名称
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${upgradeInfo.url?.split("/")?.last()}")
            // 下载时在通知栏内显示下载进度条
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            // 设置MIME类型，即设置下载文件的类型, 如果下载的是android文件, 设置为application/vnd.android.package-archive
            request.setMimeType("application/vnd.android.package-archive")
            // 3、获取到下载id
            downloadId = downloadManager.enqueue(request)
            // 4、动态更新下载进度


            // 开辟IO线程
                coroutineScope.launch {
                    val success = checkDownloadProgress(downloadManager,downloadId,file,totalSize){childValue ->
                        progress=childValue

                    }
                    if (success) {
                        downloaded=true
                        // 下载文件"content://"类型的uri ，DownloadManager通过downloadId
                        val uri = downloadManager.getUriForDownloadedFile(downloadId)
                        // 通过downLoadId查询下载的apk文件转成"file://"类型的uri
                        val file= queryDownloadedApk(context, downloadId)
                        // 5、安装apk
                        installAPK(context, uri,file)
                    } else {
                        if (file.exists()) {
                            // 当不需要的时候，清除之前的下载文件，避免浪费用户空间
                            file.delete()
                        }
                        // 删除下载任务和文件
                        downloadManager.remove(downloadId)
                        // 隐藏进度条显示按钮，重新下载
                    }
                    cancel()
                }
                cancel()
            }
        }
        cancel()
    }}
private fun getDownloadTotalSize(url: String?): Long {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.setRequestProperty("Accept-Encoding", "identity")
    conn.connect()
    val totalSize = conn.contentLength.toLong()
    conn.disconnect()
    return totalSize
}

//检查下载进度
@SuppressLint("Range")
suspend fun checkDownloadProgress(
    manager: DownloadManager,
    downloadId: Long,
    file: File,
    totalSize:Long,
    onProgressChange: (Int) -> Unit
): Boolean {
    var isDownloadComplete = false

    while (!isDownloadComplete) {
        val q = DownloadManager.Query()
        q.setFilterById(downloadId)
        val cursor = manager.query(q)

        if (cursor.moveToFirst()) {
            val bytes_downloaded = file.length()
            val dl_progress = if (totalSize > 0) {
                ((bytes_downloaded * 100) / totalSize).toInt()
            } else {
                0
            }
            onProgressChange(dl_progress)
            var status=cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    isDownloadComplete = true
                }
                DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING -> {
                    delay(50)
                }
                else -> {
                    if (file.exists()) {
                        //当不需要的时候，清除之前的下载文件，避免浪费用户空间
                        file.delete()
                    }
                    manager.remove(downloadId)
                    isDownloadComplete = true
                }
            }
        } else {
            if (file.exists()) {
                //当不需要的时候，清除之前的下载文件，避免浪费用户空间
                file.delete()
            }
            manager.remove(downloadId)
            isDownloadComplete = true
        }

        cursor.close()
    }

    return isDownloadComplete
}



//中文路径转成GB编码
fun encodeGB(string: String): String{
    //转换中文编码
    val split = string.split("/".toRegex()).toTypedArray()
    for (i in 1 until split.size) {
        try {
            split[i] = URLEncoder.encode(split[i], "GB2312")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        split[0] = split[0] + "/" + split[i]
    }
    split[0] = split[0].replace("\\+".toRegex(), "%20") //处理空格
    return split[0]
}
//调用系统安装apk
fun installAPK(context: Context, apkUri: Uri,apkFile: File?) {

        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //安卓7.0版本以上安装
            intent.action = Intent.ACTION_VIEW
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        } else {
            //安卓6.0-7.0版本安装
            intent.action = Intent.ACTION_DEFAULT
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            apkFile?.let {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }



}
@SuppressLint("Range")
private fun queryDownloadedApk(context: Context, downloadId: Long): File? {
    var targetApkFile: File? = null
    val downloader = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    if (downloadId != -1L) {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
        val cur: Cursor? = downloader.query(query)
        if (cur != null) {
            if (cur.moveToFirst()) {
                val uriString: String =
                    cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                if (!TextUtils.isEmpty(uriString)) {
                    targetApkFile = Uri.parse(uriString).path?.let { File(it) }
                }
            }
            cur.close()
        }
    }
    return targetApkFile
}


