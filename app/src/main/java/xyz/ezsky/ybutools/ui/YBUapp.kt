package xyz.ezsky.ybutools.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.ezsky.ybutools.ui.jwxtapp.CourseSchedule
import xyz.ezsky.ybutools.ui.jwxtapp.Examlist
import xyz.ezsky.ybutools.ui.jwxtapp.JwxtHome
import xyz.ezsky.ybutools.ui.mainpage.loginpage
import xyz.ezsky.ybutools.ui.mainpage.mainpage
import xyz.ezsky.ybutools.ui.settingpage.setting
import xyz.ezsky.ybutools.ui.xxmhpage.SeeArt

/**
 * APP的总导航
 */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun YBUapp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "MainPage") {
        composable("MainPage") { mainpage(navController) }
        composable("Setting") { setting(navController) }
        composable("JwxtHome") { JwxtHome(navController) }
        composable("CourseSchedule") { CourseSchedule(navController) }
        composable("Examlist") { Examlist(navController) }
        composable("SeeArt?GGDM={GGDM}", arguments = listOf(
            navArgument("GGDM") {
            //表示传递的参数是String类型
            type = NavType.StringType
        })) {backStackEntry ->
            val GGDM = backStackEntry.arguments?.getString("GGDM") ?: "1"
            SeeArt(navController,GGDM) }
        composable("loginpage?appserverID={appserverID}&appServerName={appServerName}&approuter={approuter}", arguments = listOf(
            navArgument("appserverID") {
                //表示传递的参数是String类型
                type = NavType.StringType
            }, navArgument("appServerName") {
                type = NavType.StringType
            }, navArgument("approuter") {
                type = NavType.StringType
            }
        )
        ) { backStackEntry ->
            val appserverID = backStackEntry.arguments?.getString("appserverID") ?: "1"
            val appServerName=backStackEntry.arguments?.getString("appServerName") ?: ""
            val approuter=backStackEntry.arguments?.getString("approuter") ?: "JwxtHome"
            loginpage(navController,appserverID,appServerName,approuter) }
    }
}
