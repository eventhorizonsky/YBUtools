package xyz.ezsky.ybutools.ui.mainpage


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ezsky.ybutools.data.jsxt.JwxtNetwork.Companion.logInjwxt
import xyz.ezsky.ybutools.ui.toolcompose.ErrorDialog
import java.io.IOException

/**
 * 登录用页面
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun loginpage(navController: NavController,appserverID:String,appserverName:String,approuter:String) {

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var errortext by rememberSaveable { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            text = appserverName,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color =  MaterialTheme.colorScheme.primary
            )
        )

        TextField(

            value = username,
            onValueChange = { username = it },
            label = { Text("账号") },
            modifier = Modifier
                .padding(vertical = 10.dp)
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            label = { Text("密码") },
            modifier = Modifier
                .padding(vertical = 10.dp)
        )

        Button(
            onClick = {
                if (isButtonEnabled) {
                    isButtonEnabled = false
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            try {
                                val result= loginaction(appserverID,username,password)
                                if(result.isSuccess){
                                    withContext(Dispatchers.Main) {
                                        navController.popBackStack()
                                        navController.navigate(approuter)
                                    }
                                    isButtonEnabled = true
                                }else{
                                    Log.e("",result.toString())
                                    isError=true
                                    errortext=result.exceptionOrNull()?.message ?: "未知错误"
                                    isButtonEnabled = true}
                                // 处理登录结果
                            } catch (e: Exception) {
                                isError=true
                                errortext= e.toString()
                                isButtonEnabled = true
                                Log.e("", e.toString())
                            }

                        }
                    }



                }

            },
            enabled = isButtonEnabled
        ) {
            if (isButtonEnabled) {
                Text("登录")
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("登录中...", style = TextStyle(color = Color.Gray))
                }
            }
        }
        if(isError){
            ErrorDialog(
                errorMessage = errortext,
                onDismiss = { isError = false }
            )
        }

    }
}
fun loginaction(appID:String,username:String,password:String):Result<Boolean>{
    when(appID){
        "1"->return logInjwxt(username,password)
        else->return Result.failure(IOException("未指定appID"))
    }

}