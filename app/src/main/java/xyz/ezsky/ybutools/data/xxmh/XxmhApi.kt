package xyz.ezsky.ybutools.data.xxmh

import TrustAllCertificates
import TrustAllCertificates.hostnameVerifier
import TrustAllCertificates.sslSocketFactory
import TrustAllCertificates.trustManager
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
import retrofit2.http.*
import xyz.ezsky.ybutools.data.jsxt.entity.ArtListData
import xyz.ezsky.ybutools.data.jsxt.entity.Row
import xyz.ezsky.ybutools.data.jsxt.tools.AESHelper

import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * 信息门户接口
 */
interface RequestService {

    @FormUrlEncoded
    @POST(value="/authserver/login")
    fun loginAuth(
        @FieldMap formData: Map<String, String>
    ): Call<ResponseBody>
    @FormUrlEncoded
    @POST(value="/publicappnew/sys/lwPsTzggApp/modules/ggll/cxlmxdggxx.do")
    fun getartlist(
        @FieldMap formData: Map<String, String>
    ): Call<ResponseBody>
    @GET(value="/authserver/login")
    fun getlogininfo(

    ): Call<ResponseBody>
    @GET(value="/login?service=http://portal.ybu.edu.cn/ywtb-portal/official/index.html")
    fun loginPortal(

    ): Call<ResponseBody>
    @POST(value = "/publicappnew/sys/lwPsTzggApp/ggll/loadNoticeDetailInfo.do")
    fun getArticle(
        @Query("data") data: String
    ):Call<ResponseBody>

}

/**
 * 可保存session会话
 */
class MyCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies
    }
}

/**
 * 教务系统相关请求工具
 */
class XxmhNetwork {

    companion object {

        private val interceptor = Interceptor { chain ->
            val request = chain.request()
            val requestBuilder = request.newBuilder()
            val url = request.url()
            val builder = url.newBuilder()



            requestBuilder.url(builder.build())
                .method(request.method(), request.body())

            chain.proceed(requestBuilder.build())
        }


        val dns = Dns.SYSTEM
        //创建OKhttp
        private val client: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .dns(dns)
            .cookieJar(MyCookieJar())
            .connectTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier(hostnameVerifier)
            .readTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)


        private var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://authserver.ybu.edu.cn/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()
        private var retrofit2: Retrofit = Retrofit.Builder()
            .baseUrl("http://portal.ybu.edu.cn/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()

        var service: RequestService = retrofit.create(RequestService::class.java)
        var service2: RequestService = retrofit2.create(RequestService::class.java)
        private var jwxtusernameOkkv by okkv("jwxt_username","")
        private var jwxtpasswordOkkv by okkv("jwxt_password","")
        @RequiresApi(Build.VERSION_CODES.O)
        fun getartlist(LMDM:String,pageSize:Int,pageNumber: Int):Result<List<Row>>{

            try {
                login()
                val formData = HashMap<String, String>()
                formData["LMDM"] = LMDM
                formData["pageSize"] = pageSize.toString()
                formData["pageNumber"] = pageNumber.toString()
                val call= service2.getartlist(formData)
                val response = call.execute()

                if (response.isSuccessful) {
                    val html = response.body()?.string()
                    if(html!=null){
                        val gson = Gson()
                        val result=gson.fromJson(html, ArtListData::class.java)

                        return Result.success(result.datas.cxlmxdggxx.rows)
                    }
                    else{
                        return Result.failure(IOException("发生错误"))
                    }
                }else{
                    return Result.failure(IOException("发生错误"))
                }
            }catch (e: IOException){
                Log.e("tes",e.toString())
                return Result.failure(IOException(e))
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun test(GGDM:String):Result<String>{

            try {
            login()
            val call= service2.getArticle("{'GGDM':'${GGDM}'}")
            val response = call.execute()

            if (response.isSuccessful) {
                val html = response.body()?.string()
                Log.e("tset",html?:"无数据")
                return Result.success(html?:"无数据")
            }else{
                return Result.failure(IOException("wushuju"))
            }
            }catch (e: IOException){
                Log.e("tes",e.toString())
                return Result.failure(IOException(e))
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun login(){
            try {
            val call= service.getlogininfo()
            val res = call.execute()
            Log.e("test",call.request().url().toString()?:"1213")
            val html = res.body()?.string()
            val document = Jsoup.parse(html)
            val lt = document.select("#casLoginForm input[name=lt]").attr("value") ?:""
            if(lt==""){return}
            val dllt =document.select("#casLoginForm input[name=dllt]").attr("value") ?:""
            val execution =document.select("#casLoginForm input[name=execution]").attr("value") ?:""
            val eventId =document.select("#casLoginForm input[name=_eventId]").attr("value")?:""
            val rmShown =document.select("#casLoginForm input[name=rmShown]").attr("value")?:""
            val pwdDefaultEncryptSalt =document.select("#pwdDefaultEncryptSalt").attr("value")?:""
            val password=AESHelper.encryptAES(jwxtpasswordOkkv,pwdDefaultEncryptSalt)
                val formData = HashMap<String, String>()
                formData["username"] = jwxtusernameOkkv
                formData["lt"] = lt
                formData["dllt"] = dllt
                formData["execution"] = execution
                formData["_eventId"] = eventId
                formData["rmShown"] = rmShown
                formData["password"] = password

            val call2 = service.loginAuth(formData =formData)
                val response=call2.execute()

                 val call3= service2.loginPortal()
                val request = call3.execute()
                Log.e("ERRO",request.body()?.string()?:"")


        } catch (e: IOException){
            Log.e("ERRO",e.toString())
        }
        }

    }
}

