package xyz.ezsky.ybutools.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import xyz.ezsky.ybutools.ui.mainpage.screen.LocalAppContext
import xyz.ezsky.ybutools.ui.theme.YBUtoolsTheme


class MainActivity : ComponentActivity() {
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
                    }

                }
            }
        }
    }

}
//初始化okkv
private fun initOkkv(Activity: Activity) {
    Okkv.Builder(MMKVStore(Activity)).cache().build().init().default()
    // 如果不使用缓存，请手动指定 key
    Okkv.Builder(MMKVStore(Activity)).build().init().default("no_cache")
}
