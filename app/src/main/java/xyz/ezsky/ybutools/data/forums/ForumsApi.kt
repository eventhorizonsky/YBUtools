package xyz.ezsky.ybutools.data.forums

import TrustAllCertificates.hostnameVerifier
import TrustAllCertificates.sslSocketFactory
import TrustAllCertificates.trustManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import xyz.ezsky.ybutools.data.jsxt.entity.*
import java.io.IOException
import java.util.concurrent.TimeUnit

interface RequestService {


    @GET(value="/forums/post/list")
    fun getpostlist(

    ): Call<ResponseBody>
    @GET("/forums/post/{id}")
    fun getPost(
        @Path("id") id: String
    ): Call<ResponseBody>
    @GET(value="/system/app/last")
    fun getapplast(

    ): Call<ResponseBody>

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
class ForumsNetwork{

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
            .baseUrl("http://192.168.123.104:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()
        var service: RequestService = retrofit.create(RequestService::class.java)

        fun getlastapp(): Result<YbuApp> {
            try {
                val call = service.getapplast()
                val response = call.execute()

                if (response.isSuccessful) {
                    val html = response.body()?.string()
                    if (html != null) {
                        val gson = Gson()
                        val parser = JsonParser()

                        // 使用JsonParser解析JSON
                        val jsonElement = parser.parse(html)

                        if (jsonElement.isJsonObject) {
                            val jsonObject = jsonElement.asJsonObject
                            if (jsonObject.has("data")) {
                                val dataJson = jsonObject.getAsJsonObject("data")

                                // 将"data"字段转换为YbuApp对象
                                val ybuApp = gson.fromJson(dataJson, YbuApp::class.java)

                                return Result.success(ybuApp)
                            } else {
                                return Result.failure(IOException("JSON中缺少'data'字段"))
                            }
                        } else {
                            return Result.failure(IOException("JSON格式不正确"))
                        }
                    } else {
                        return Result.failure(IOException("发生错误"))
                    }
                } else {
                    return Result.failure(IOException("无数据"))
                }
            } catch (e: IOException) {
                return Result.failure(IOException(e))
            }
        }

        fun listpost():Result<List<ForumsPost>>{
            try {
                val call= service.getpostlist()
                val response = call.execute()

                if (response.isSuccessful) {
                    val html = response.body()?.string()
                    if(html!=null){
                        val gson = Gson()
                        val result=gson.fromJson(html, forumlist::class.java)

                        return Result.success(result.rows)
                    }
                    else{
                        return Result.failure(IOException("发生错误"))
                    }
                }else{
                    return Result.failure(IOException("无数据"))
                }
            }catch (e: IOException){
                Log.e("tes",e.toString())
                return Result.failure(IOException(e))
            }
        }
        fun getpostfromid(id: String):Result<ForumsPost>{
            try {
                val call= service.getPost(id)
                val response = call.execute()

                if (response.isSuccessful) {
                    val html = response.body()?.string()
                    if(html!=null){
                        val gson = Gson()
                        val result=gson.fromJson(html, forumresult::class.java)

                        return Result.success(result.data)
                    }
                    else{
                        return Result.failure(IOException("发生错误"))
                    }
                }else{
                    return Result.failure(IOException("无数据"))
                }
            }catch (e: IOException){
                Log.e("tes",e.toString())
                return Result.failure(IOException(e))
            }
        }
    }

}
