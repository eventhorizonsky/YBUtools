package xyz.ezsky.ybutools.ui


import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import xyz.ezsky.ybutools.R
import xyz.ezsky.ybutools.ui.mainpage.screen.AppScreen
import xyz.ezsky.ybutools.ui.mainpage.screen.HomeScreen
import xyz.ezsky.ybutools.ui.mainpage.screen.ListScreen
import xyz.ezsky.ybutools.ui.mainpage.screen.MineScreen

//主页面导航
interface YBUDestination {
    val icon: ImageVector
    val route: String
    val stringResource:Int
    val screen: @Composable (NavController) -> Unit
}

/**
 * Rally app navigation destinations
 */
object Home : YBUDestination {
    override val icon = Icons.Filled.Home
    override val route = "home"
    override val stringResource= R.string.home
    override val screen: @Composable (NavController) -> Unit = {param -> HomeScreen(param) }

}

object List : YBUDestination {
    override val icon = Icons.Filled.List
    override val route = "list"
    override val stringResource= R.string.list
    override val screen: @Composable (NavController) -> Unit = {param -> ListScreen(param) }
}

object Mine : YBUDestination {
    override val icon = Icons.Filled.Person
    override val route = "mine"
    override val stringResource= R.string.mine
    override val screen: @Composable (NavController) -> Unit = {param ->  MineScreen(param) }
}
object App : YBUDestination {
    override val icon = Icons.Filled.DateRange
    override val route = "app"
    override val stringResource= R.string.app
    override val screen: @Composable (NavController) -> Unit = { param -> AppScreen(param) }
}

val rallyTabRowScreens = listOf(Home, List,App, Mine)
class YBUDestinationSaver : Saver<YBUDestination, Bundle> {
    override fun restore(value: Bundle): YBUDestination {
        val route = value.getString("route") ?: error("Missing route")
        return when (route) {
            "home" -> Home
            "list" -> List
            "app" ->App
            "mine" ->Mine
            // 其他 YBUDestination 的恢复逻辑...
            else -> error("Unknown route: $route")
        }
    }

    override fun SaverScope.save(value: YBUDestination): Bundle {
        val bundle = Bundle()
        bundle.putString("route", value.route)
        return bundle
    }
}
