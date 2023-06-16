import java.security.cert.X509Certificate
import javax.net.ssl.*

object TrustAllCertificates {
    val trustManager: X509TrustManager by lazy {
        val trustAllManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        trustAllManager
    }

    val hostnameVerifier: HostnameVerifier by lazy {
        HostnameVerifier { _, _ -> true }
    }

    val sslSocketFactory: SSLSocketFactory by lazy {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        sslContext.socketFactory
    }
}
