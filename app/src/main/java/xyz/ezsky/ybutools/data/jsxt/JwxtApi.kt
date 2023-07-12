package xyz.ezsky.ybutools.data.jsxt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.heyanle.okkv2.core.okkv
import okhttp3.*
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import xyz.ezsky.ybutools.data.DatabaseProvider
import xyz.ezsky.ybutools.data.jsxt.entity.Exams
import xyz.ezsky.ybutools.data.jsxt.entity.Grades
import xyz.ezsky.ybutools.data.jsxt.entity.Student
import xyz.ezsky.ybutools.data.jsxt.tools.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 教务系统接口
 */
interface RequestService {

    @GET(value="jsxsd/framework/xsMain.jsp")
    fun verlogin(

    ): Call<ResponseBody>

    @GET(value="jsxsd/kscj/cjcx_list")
    fun getGradesList(

    ): Call<ResponseBody>
    @GET(value="/jsxsd/xsks/xsksap_list?xnxqid=2022-2023-2")
    fun getExamsList(

    ): Call<ResponseBody>
    @GET(value="/jsxsd/grxx/xsxx")
    fun getStudentInfo(

    ): Call<ResponseBody>
    @GET(value="/jsxsd/framework/xsMain_new.jsp?t1=1")
    fun getweekInfo(

    ): Call<ResponseBody>
    @GET(value = "/jsxsd/xskb/xskb_list.do")
    fun getCourse(
        @Query("xnxq01id") xnxq01id: String = ""
    ): Call<ResponseBody>
    @GET(value="/jsxsd/grxx/xszpLoad")
    @Streaming
    fun getImage(): Call<ResponseBody>
    @POST(value = "jsxsd/xk/LoginToXkLdap")
    fun logIn(
        @Query("userAccount") userAccount: String,
        @Query("encoded") encoded: String
    ): Call<ResponseBody>
}

/**
 * 可保存session会话
 */
class SessionCookieJar : CookieJar {
    private var cookies: List<Cookie>? = null

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies ?: emptyList()
    }
}

/**
 * 教务系统相关请求工具
 */
class JwxtNetwork {

    companion object {

        //创建拦截器
        private val interceptor = Interceptor { chain ->
            val request = chain.request()
            val requestBuilder = request.newBuilder()
            val url = request.url()
            val builder = url.newBuilder()
            requestBuilder.url(builder.build())
                .method(request.method(), request.body())
                .addHeader("clientType", "IOS")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
            chain.proceed(requestBuilder.build())
        }

        //创建OKhttp
        private val client: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .cookieJar(SessionCookieJar())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)


        private var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://jwxt.ybu.edu.cn/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()

        var service: RequestService = retrofit.create(RequestService::class.java)
        private var jwxtusernameOkkv by okkv("jwxt_username","")
        private var jwxtpasswordOkkv by okkv("jwxt_password","")

        // 登录方法
        fun logInjwxt(userAccount: String, password: String): Result<Boolean> {
            val encoded = Base64Utils.encode(userAccount) + "%%%" + Base64Utils.encode(password)
            val call = service.logIn(userAccount, encoded)
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    // 登录成功，进行下一步操作
                    val versult=verifyLogin()
                    if(versult.isSuccess){
                        jwxtusernameOkkv=userAccount
                        jwxtpasswordOkkv=password
                    }
                    return versult


                } else {
                    // 可以根据错误代码进行相应的处理
                    return Result.failure(IOException("登录失败，请检查您的账号密码"))
                }
            } catch (e: IOException) {
                return Result.failure(e)
            }
        }
        // 登出方法
        suspend fun logout(context:Context) {
            // 清除保存的用户名和密码
            jwxtusernameOkkv = ""
            jwxtpasswordOkkv = ""
            // 清除Cookie
            val cookieJar = client.build().cookieJar() as SessionCookieJar
            cookieJar.saveFromResponse(HttpUrl.parse("http://jwxt.ybu.edu.cn/")!!, emptyList())
            val database = DatabaseProvider.getDatabase(context)
            val gradeDao = database.gradeDao()
            gradeDao.deleteAllGrades()
            val cacheDir = context.cacheDir
            val fileName = URLEncoder.encode("avatar", "UTF-8")
            val file = File(cacheDir, fileName)
            // 删除缓存图片
            if (file.exists()) {
                file.delete()
            }
        }
        // 验证登录方法
        private fun verifyLogin(): Result<Boolean>  {
            val call = service.verlogin()
            val response = call.execute()
            if (response.isSuccessful) {

                return Result.success(true)
            } else {
                return Result.failure(IOException("登录失败，请检查您的账号密码"))
            }
        }
        // 从本地缓存加载图片
       fun loadImageFromCache(context: Context): Bitmap? {
            val cacheDir = context.cacheDir
            val fileName = URLEncoder.encode("avatar", "UTF-8")
            val file = File(cacheDir, fileName)

            if (file.exists()) {
                val inputStream = FileInputStream(file)
                return BitmapFactory.decodeStream(inputStream)
            }
            Log.e("test","响应体为null")
            return null
        }

        // 保存图片到本地缓存
        private fun saveImageToCache(context: Context, bitmap: Bitmap) {
            val cacheDir = context.cacheDir
            val fileName = URLEncoder.encode("avatar", "UTF-8")
            val file = File(cacheDir, fileName)

            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }

        // 从互联网获取图片并缓存到本地
        fun fetchImageAndCache(context: Context) {

                try {
                    logInjwxt(jwxtusernameOkkv, jwxtpasswordOkkv)
                    val call = service.getImage()

                    val response = call.execute()
                    val responseBody=response.body()
                    if (responseBody != null) {
                        val inputStream = responseBody.byteStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        // 处理获取到的图片
                        saveImageToCache(context, bitmap)

                    } else {
                        Log.e("test","响应体为空")
                        // 处理响应体为空的情况
                    }

                } catch (e: Exception) {
                    Log.e("test","响应体为空$e")
                    e.printStackTrace()
            }

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getweek(): Result<Boolean>{
            //进行登录操作
            try {
                logInjwxt(jwxtusernameOkkv, jwxtpasswordOkkv)
                val call = service.getweekInfo()
                val response = call.execute()

                if (response.isSuccessful) {
                    // 解析 HTML 数据
                    val html = response.body()?.string()
                    val doc = Jsoup.parse(html)
                    // 提取所有类似于 "xxxx-xxxx-x" 的学年学期字符串
                    val pattern = Regex("""\d{4}-\d{4}-\d""")
                    val yearSemesterList = doc.select("select[name=xnxq01id] option")
                        .map { it.text() }
                        .filter { pattern.matches(it) }

                    // 获取被选中的字符串
                    val selectedYearSemester = doc.select("select[name=xnxq01id] option[selected=selected]")
                        .first()
                        ?.text()

                    // 获取第16周的数字
                    val weekText = doc.select("#li_showWeek .main_text").first()?.text()
                    val weekNumber = weekText?.replace("第", "")?.replace("周", "")?.toIntOrNull()
                    var xnxqlistokkv by okkv("jwxt_xnxqlist","")
                    var weekokkv by okkv("jwxt_week","2020-1-1")
                    var xnxq by okkv("jwxt_xnxq","")
                    val gson=Gson()
                    val firstMonday = getFirstMondayOfFirstWeek(weekNumber?:3)
                    xnxqlistokkv=gson.toJson(yearSemesterList)
                    weekokkv= localDateToString(firstMonday)
                    xnxq=selectedYearSemester?:"2022-2023-1"
                    return Result.success(true)
                } else {
                    val errorCode = response.code()
                    return Result.failure(IOException("获取失败$errorCode"))
                }  }catch (e: IOException){
                return Result.failure(IOException(e))
            }
        }
        /**
         * 获取学生信息
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun getStudentInfo(context: Context): Result<Boolean>{
            //进行登录操作
            try {
                logInjwxt(jwxtusernameOkkv, jwxtpasswordOkkv)
                val call = service.getStudentInfo()
                val response = call.execute()

            if (response.isSuccessful) {
                // 解析 HTML 数据
                val html = response.body()?.string()
                val document = Jsoup.parse(html)
                val studentNumber = document.select("td:contains(学号)").first()?.nextElementSibling()
                    ?.text()?:""
                val name = document.select("td:contains(姓名)").first()?.nextElementSibling()?.text()?:""
                val gender = document.select("td:contains(性别)").first()?.nextElementSibling()?.text()?:""
                val nation = document.select("td:contains(民族)").first()?.nextElementSibling()?.text()?:""
                val birthdate = document.select("td:contains(出生日期)").first()?.nextElementSibling()?.text()?:""
                val college = document.select("td:contains(学院)").first()?.nextElementSibling()?.text()?:""
                val major = document.select("td:contains(校内专业（类）)").first()?.nextElementSibling()?.text()?:""
                val grade = document.select("td:contains(年级)").first()?.nextElementSibling()?.text()?:""
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val updateTime=currentDateTime.format(formatter)
                val student= Student(name,studentNumber,gender,nation,birthdate,college,major,grade,updateTime)
                val gson = Gson()
                val jsonString = gson.toJson(student)
                var studentInfoOkkv by okkv("jwxt_studentinfo","")
                studentInfoOkkv=jsonString
                return Result.success(true)
            } else {
                val errorCode = response.code()
                return Result.failure(IOException("获取失败$errorCode"))
            }  }catch (e: IOException){
                return Result.failure(IOException(e))
            }

        }
        fun getmycourse(xnxqid:String): Result<Boolean>{
            try {
                logInjwxt(jwxtusernameOkkv, jwxtpasswordOkkv)
                val call = service.getCourse(xnxq01id =xnxqid )
                val response = call.execute()
                if (response.isSuccessful) {
                    // 解析 HTML 数据
                    val html = response.body()?.string()
                    var coursedata=html?.let { QzBrParser(it) }?.getCourse()
                    if(coursedata!=null){
                        val gson = Gson()
                        var coursebaseokkv by okkv("jwxt_coursebase","")
                        coursebaseokkv=gson.toJson(coursedata.base)
                        var coursedetailokkv by okkv("jwxt_coursedetail","")
                        coursedetailokkv=gson.toJson(coursedata.detail)
                        return Result.success(true)

                    }else{
                        val errorCode = response.code()
                        return Result.failure(IOException("获取失败$errorCode"))
                        }



                } else {
                    val errorCode = response.code()
                    return Result.failure(IOException("获取失败$errorCode"))
                }

            }catch (e: IOException){
                return Result.failure(IOException(e))
            }
        }
        //获取成绩和总分
        suspend fun getgrades(context: Context): Result<Boolean>{
            try {
                logInjwxt(jwxtusernameOkkv, jwxtpasswordOkkv)
                val call = service.getGradesList()
                val response = call.execute()
                if (response.isSuccessful) {
                    // 解析 HTML 数据
                    val html = response.body()?.string()
                    val document = Jsoup.parse(html)
                    val table = document.select("table")[0]
                    val rows = table.select("tr")

                    // 获取所修总学分
                    val creditText = document.select("div:contains(所修总学分)").not("table").text()

                    val regex = Regex("[\\d.]+")
                    val matches = regex.findAll(creditText )

                    var match = matches.firstOrNull()
                    var creditOkkv by okkv("jwxt_credit",0.0)
                    val credit = match?.value?.toDoubleOrNull() ?: 0.0
                    creditOkkv=credit
                    match=matches.elementAtOrNull(1)
                    var gpaOkkv by okkv("jwxt_gpa",0.0)
                    val gpa =match?.value?.toDoubleOrNull() ?: 0.0
                    gpaOkkv=gpa
                    match=matches.elementAtOrNull(2)
                    var averageScoreOkkv by okkv("jwxt_averageScore",0.0)
                    val averageScore = match?.value?.toDoubleOrNull() ?: 0.0
                    averageScoreOkkv=averageScore
                    // 遍历表格数据并创建学生列表
                    val gradesList = mutableListOf<Grades>()
                    for (i in 1 until rows.size) {
                        val row =rows.get(i)
                        val cells = row.select("td")
                        if (cells!=null) {
                            val listid = cells[0].text()
                            val courseName = cells[3].text()
                            val score = cells[4].text()
                            gradesList.add(Grades(listid=listid, courseName=courseName, score=score))
                        }
                    }
                    val database = DatabaseProvider.getDatabase(context)
                    val gradeDao = database.gradeDao()
                    gradeDao.deleteAllGrades()
                    for(grades in gradesList){
                        gradeDao.insertGrades(grades)
                    }
                    return Result.success(true)
                } else {
                    val errorCode = response.code()
                    return Result.failure(IOException("获取失败$errorCode"))
                }

            }catch (e: IOException){
                return Result.failure(IOException(e))
            }
        }
        fun getexams(): Result<List<Exams>>{
            try {
                logInjwxt(jwxtusernameOkkv, jwxtpasswordOkkv)
                val call = service.getExamsList()
                val response = call.execute()
                if (response.isSuccessful) {
                    // 解析 HTML 数据
                    val html = response.body()?.string()
                    val document = Jsoup.parse(html)
                    val table = document.select("table")[0]
                    val rows = table.select("tr")

                    // 遍历表格数据并创建学生列表
                    val examsList = mutableListOf<Exams>()
                    for (i in 1 until rows.size) {
                        val row =rows.get(i)
                        val cells = row.select("td")
                        if (cells.size>1) {
                            val id = cells[0].text()
                            val campus = cells[1].text()
                            val examSession = cells[2].text()
                            val courseCode = cells[3].text()
                            val courseName = cells[4].text()
                            val teacher = cells[5].text()
                            val examMode = cells[6].text()
                            val examTime = cells[7].text()
                            val examRoom = cells[8].text()
                            val seatNumber = cells[9].text()
                            val admissionTicket = cells[10].text()
                            val note = cells[11].text()

                            examsList.add(Exams(id,campus, examSession, courseCode, courseName, teacher, examMode, examTime, examRoom, seatNumber, admissionTicket, note))
                        }
                    }
                    if(examsList.isEmpty()){examsList.add(Exams("","","","","暂无考试安排","","","","","","",""))}

                    return Result.success(examsList)
                } else {
                    val errorCode = response.code()
                    return Result.failure(IOException("获取失败$errorCode"))
                }

            }catch (e: IOException){
                return Result.failure(IOException(e))
            }
        }
    }
}

