package xyz.ezsky.ybutools.data.jsxt.tools
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// 返回当前周的第一天（周一）的日期
@RequiresApi(Build.VERSION_CODES.O)
// 根据当前周数倒推得到学期的第一周的周一日期
fun getFirstMondayOfFirstWeek(currentWeek: Int): LocalDate {
    val firstDayOfWeek = DayOfWeek.MONDAY
    val currentDate = LocalDate.now()
    val daysDifference = (currentWeek - 1) * 7
    return currentDate.minusDays(daysDifference.toLong()).with(firstDayOfWeek)
}

// 根据第一周的周一日期和当前日期计算当前是第几周
@RequiresApi(Build.VERSION_CODES.O)
fun calculateWeekNumber(firstMonday: LocalDate, currentDate: LocalDate): Int {
    val daysDifference = ChronoUnit.DAYS.between(firstMonday, currentDate)
    return (daysDifference / 7).toInt() + 1
}

@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    val currentWeek = 2

    // 获取第一周的周一日期
    val firstMonday = getFirstMondayOfFirstWeek(currentWeek)
    println("第一周的周一日期：$firstMonday")
    // 将 LocalDate 对象转换为字符串
    val dateString = localDateToString(firstMonday)
    println("转换后的日期字符串：$dateString")

    // 将字符串还原为 LocalDate 对象
    val restoredDate = stringToLocalDate(dateString)
    println("还原后的日期：$restoredDate")
    // 计算当前是第几周
    val currentDate = LocalDate.now()
    val weekNumber = calculateWeekNumber(firstMonday, currentDate)
    println("当前是第 $weekNumber 周")
}
// 将 LocalDate 对象转换为字符串
@RequiresApi(Build.VERSION_CODES.O)
fun localDateToString(date: LocalDate): String {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    return date.format(formatter)
}

// 将字符串还原为 LocalDate 对象
@RequiresApi(Build.VERSION_CODES.O)
fun stringToLocalDate(dateString: String): LocalDate {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    return LocalDate.parse(dateString, formatter)
}