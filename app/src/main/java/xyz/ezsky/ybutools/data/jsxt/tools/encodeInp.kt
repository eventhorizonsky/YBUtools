package xyz.ezsky.ybutools.data.jsxt.tools
import android.util.Base64

object Base64Utils {

        fun encode(input: String): String {
            val data = input.toByteArray(charset("UTF-8"))
            return Base64.encodeToString(data, Base64.DEFAULT)
        }
    }
