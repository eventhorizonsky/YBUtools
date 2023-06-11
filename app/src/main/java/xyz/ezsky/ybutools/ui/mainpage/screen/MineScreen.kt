package xyz.ezsky.ybutools.ui.mainpage.screen


import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heyanle.okkv2.core.okkv
import xyz.ezsky.ybutools.R

val LocalAppContext = staticCompositionLocalOf<Context> { error("No App Context provided") }

/**
 * 【我的】页面
 */
@Composable
fun MineScreen(navController: NavController) {

    LazyColumn {

        items(appitems) { item ->
            item.determineIsLogin()
            val islogin=item.islogin
            val title = LocalAppContext.current.getString(item.title)
            appitem(
                icon = item.icon,
                title = title,
               islogin=islogin,
                onButtonClick = {

                    if(islogin){navController.navigate(item.router)}else{
                        navController.navigate("loginpage?appserverID=${item.appID}&appServerName=${title}&approuter=${item.router}")
                    }
                }
            )
        }
    }

}


@Composable
fun appitem(icon: ImageVector, title: String, onButtonClick: () -> Unit,islogin:Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // 提供一个合适的描述
            modifier = Modifier
                .size(32.dp)
                .padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onButtonClick,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            if(islogin){
                Text(text = "查看")
            }else{
                Text(text = "登录")
            }

        }
    }
    Divider()
}

interface Appitem {
    val icon: ImageVector
    val title: Int
    val appID: Int
    val router:String
    var islogin:Boolean
    fun determineIsLogin(){

    }
}

object YBUjwxt : Appitem {
    override val icon = Icons.Filled.Person
    override val title = R.string.ybujwxt
    override val appID = 1
    override val router = "JwxtHome"
    override var islogin: Boolean = false

    override fun determineIsLogin() {
        val jwxtusernameOkkv by okkv("jwxt_username","")
        if(jwxtusernameOkkv!=""){  islogin = true}

    }
}


object YBUvpn : Appitem {
    override val icon = Icons.Filled.Person
    override val title = R.string.ybuvpn
    override val appID = 2
    override val router="VpnHome"
    override var islogin: Boolean = false
    override fun determineIsLogin() {


    }
}

object YBUxfb : Appitem {
    override val icon = Icons.Filled.Person
    override val title = R.string.ybuxfb
    override val appID = 2
    override val router="XfbHome"
    override var islogin: Boolean = false
    override fun determineIsLogin() {


    }
}

object YBUweb : Appitem {
    override val icon = Icons.Filled.Person
    override val title = R.string.ybuweb
    override val appID = 3
    override val router="WebHome"
    override var islogin: Boolean = false
    override fun determineIsLogin() {


    }
}

val appitems = listOf(YBUjwxt, YBUvpn, YBUxfb, YBUweb)
