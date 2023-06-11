package xyz.ezsky.ybutools.data.jsxt

import android.content.Context
import com.heyanle.okkv2.core.okkv
import okhttp3.*
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import xyz.ezsky.ybutools.data.DatabaseProvider
import xyz.ezsky.ybutools.data.jsxt.entity.Grades
import xyz.ezsky.ybutools.data.jsxt.tools.Base64Utils
import java.io.IOException
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
    }
}

