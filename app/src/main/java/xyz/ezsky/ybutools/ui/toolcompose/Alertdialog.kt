package xyz.ezsky.ybutools.ui.toolcompose

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import xyz.ezsky.ybutools.data.forums.YbuApp
import xyz.ezsky.ybutools.data.jsxt.entity.Grades
import xyz.ezsky.ybutools.ui.mainpage.screen.LocalAppContext

/**
 * 错误弹窗
 */
@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "发生错误",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Text(
                text = errorMessage,
                style = TextStyle(
                    fontSize = 16.sp
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(text = "OK")
            }
        }
    )
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UpdateDialog(
    currentVersion:String,
    AppInfo:YbuApp,
    onDismiss: () -> Unit

) {
    var showDialog by remember { mutableStateOf(false) }
    val current = LocalAppContext.current
    var hasStoragePermission by remember { mutableStateOf(false) }
    var hasInstallPermission by remember { mutableStateOf(false) }
    //当前需要申请的权限
    val storagePermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val installPermissionState = rememberPermissionState(
        Manifest.permission.REQUEST_INSTALL_PACKAGES
    )
    var canRequestPackageInstalls = remember {
        mutableStateOf(
            current.packageManager.canRequestPackageInstalls()
        )
    }
    if (showDialog) {
        if(hasStoragePermission&&hasInstallPermission){
            upgradeApk(current,AppInfo){
                showDialog=!showDialog
            }
        }
        when (storagePermissionState.status) {
            PermissionStatus.Granted -> {//已授权
                hasStoragePermission=true
                when (canRequestPackageInstalls.value) {
                    true -> {
                        hasInstallPermission=true

                    }
                    false -> {
                        Log.e("permission",installPermissionState.status.toString())
                        AlertDialog(
                            onDismissRequest = { onDismiss() },
                            title = {
                                Text(
                                    text = "权限请求",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                )
                            },
                            text = {
                                Text(
                                    text = "需要安装权限来安装最新的APP包，如果拒绝将无法从内部完成更新",
                                    style = TextStyle(
                                        fontSize = 16.sp
                                    )
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {

                                        val intent = Intent()
                                            .setAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                            .setData(
                                                Uri.parse(
                                                    java.lang.String.format(
                                                        "package:%s",
                                                        current.packageName
                                                    )
                                                )
                                            );
                                        current.applicationContext.packageManager
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        current.startActivity(intent)

                                    }
                                ) {
                                    Text(text = "前往授权")
                                }
                                Button(
                                    onClick = {
                                        canRequestPackageInstalls.value=current.packageManager.canRequestPackageInstalls()
                                        if(!canRequestPackageInstalls.value){
                                            Toast.makeText(current.applicationContext, "未授权安装权限", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text(text = "我已授权")
                                }
                                Button(
                                    onClick = { onDismiss() }
                                ) {
                                    Text(text = "跳过")
                                }
                            }
                        )
                    }
                }
            }

            is PermissionStatus.Denied -> {
                AlertDialog(
                    onDismissRequest = { onDismiss() },
                    title = {
                        Text(
                            text = "权限请求",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    },
                    text = {
                        Text(
                            text = "需要存储权限来保存更新的APP包，如果拒绝将无法从内部完成更新",
                            style = TextStyle(
                                fontSize = 16.sp
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                storagePermissionState.launchPermissionRequest()
                            }
                        ) {
                            Text(text = "授权")
                        }
                        Button(
                            onClick = { onDismiss() }
                        ) {
                            Text(text = "跳过")
                        }
                    }
                )

            }
        }

    }


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "发现更新",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Text(
                text = "当前版本：${currentVersion},最新版本：${AppInfo.version} \n" +
                        "更新内容：${AppInfo.described}",
                style = TextStyle(
                    fontSize = 16.sp
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { showDialog=!showDialog }
            ) {
                Text(text = "更新")
            }
            Button(
                onClick = { onDismiss() }
            ) {
                Text(text = "跳过")
            }
        }
    )
}
@Composable
fun gradeDialog(
    grade: Grades,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = grade.courseName,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Column {
                Text(text = "课程学期：" + grade.kkxq)
                Text(text = "课程编号：" + grade.kcbh)
                Text(text = "课程名称：" + grade.courseName)
                Text(text = "成绩：" + grade.score)
                Text(text = "特殊原因：" + grade.tsyy)
                Text(text = "学分：" + grade.xf)
                Text(text = "总学时：" + grade.zxs)
                Text(text = "绩点：" + grade.jd)
                Text(text = "补重学期：" + grade.bcxq)
                Text(text = "考核方式：" + grade.khfs)
                Text(text = "考试性质：" + grade.ksxz)
                Text(text = "课程属性：" + grade.kcsx)
                Text(text = "课程性质：" + grade.kcxz)
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(text = "OK")
            }
        }
    )
}
// ViewModel

