package xyz.ezsky.ybutools.data.jsxt.tools

import android.os.Build
import androidx.annotation.RequiresApi
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.UnsupportedEncodingException
import java.security.*
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@RequiresApi(Build.VERSION_CODES.O)
fun main(){
    println(AESHelper.encryptAES("20011028Xyz","i9ZB0fj73ZSWeAZh"))
    println(AESHelper.decryptAES("20011028Xyz","i9ZB0fj73ZSWeAZh",
        AESHelper.randomString(16)
    ))
}
object AESHelper {
    init {
        java.security.Security.addProvider(BouncyCastleProvider())
    }
    /**
     * 金智教务系统的加密实现 Java
     *
     * @param password
     * @param key
     * @return
     * @throws Exception
     */

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun encryptAES(password: String, key: String): String {
        val randomString = randomString(RANDOM_STRING_LENGTH)
        val randomIv = randomString(RANDOM_IV_LENGTH)
        // 金智的加密步骤
        // 1.随机的64位字符拼接在密码前面
        // 2.标准的AES-128-CBC加密
        // 3.将加密后的结果进行Base64编码
        // 4.随机iv并不影响加密和解密的结果，因此，固定或者随机都可以，但必须是16位

        // 解密步骤
        // 1.将密文串进行Base64解码
        // 2.标准的AES-128-CBC解密
        // 3.裁掉密码串前面的64随机字符串
        // 4.随机iv并不影响加密和解密的结果，因此，固定或者随机都可以，但必须是16位
//        String decrypt = decryptAES(encrypt, key, randomString(RANDOM_IV_LENGTH));
//        System.out.println(encrypt);
//        System.out.println(decrypt);
        return Base64Encrypt(AESEncrypt(randomString + password, key, randomIv))
    }

    /**
     * 金智教务系统的解密实现 Java
     *
     * @param encrypt
     * @param key
     * @param iv
     * @return
     * @throws Exception
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun decryptAES(encrypt: String, key: String, iv: String): String {
        return AESDecrypt(Base64Decrypt(encrypt), key, iv).substring(RANDOM_STRING_LENGTH)
    }

    private const val CIPHER_NAME = "AES/CBC/PKCS5Padding"
    private const val CHARSETNAME = "UTF-8"
    private const val AES = "AES"
    private const val BASE_STRING = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678"
    private const val RANDOM_IV_LENGTH = 16
    private const val RANDOM_STRING_LENGTH = 64

    /**
     * AES加密
     *
     * @param data
     * @param key
     * @param iv
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        UnsupportedEncodingException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    private fun AESEncrypt(data: String, key: String, iv: String): ByteArray {
        val cipher = Cipher.getInstance(CIPHER_NAME)
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset(CHARSETNAME)), AES)
        val ivParameterSpec = IvParameterSpec(iv.toByteArray(charset(CHARSETNAME)))
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        return cipher.doFinal(data.toByteArray(charset(CHARSETNAME)))
    }

    /**
     * Base64编码
     *
     * @param data
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun Base64Encrypt(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }

    /**
     * Base64解码
     *
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(UnsupportedEncodingException::class)
    private fun Base64Decrypt(data: String): ByteArray {
        return Base64.getDecoder().decode(data.toByteArray(charset(CHARSETNAME)))
    }

    /**
     * AES解密
     *
     * @param data
     * @param key
     * @param iv
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    @Throws(
        UnsupportedEncodingException::class,
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    private fun AESDecrypt(data: ByteArray, key: String, iv: String): String {
        val cipher = Cipher.getInstance(CIPHER_NAME)
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset(CHARSETNAME)), AES)
        val ivParameterSpec = IvParameterSpec(iv.toByteArray(charset(CHARSETNAME)))
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        return cipher.doFinal(data).toString()+ CHARSETNAME
    }

    /**
     * 获取随机字符
     *
     * @param bits
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Throws(NoSuchAlgorithmException::class)
    fun randomString(bits: Int): String {
        val buffer = StringBuffer()
        for (i in 0 until bits) {
            val random = Random()
            buffer.append(BASE_STRING[random.nextInt(BASE_STRING.length)])
        }
        return buffer.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val key = "yvri8lzohm72ub4t"
        val data = "20011028Xyz"
        val s = encryptAES2(data, key)
        println(s)
        println(decryptAES2(s, key))
    }

    /**
     * 合肥工业大学等启用了辅导猫的登陆加密过程，其实还有个特殊字符的编码过程，暂时先这么写吧
     *
     * @param data
     * @param key
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchProviderException
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        UnsupportedEncodingException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchProviderException::class
    )
    fun encryptAES2(data: String, key: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC")
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset(CHARSETNAME)), AES)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val doFinal = cipher.doFinal(data.toByteArray(charset(CHARSETNAME)))
        return Base64Encrypt(doFinal)
    }

    /**
     * 合肥工业大学等启用了辅导猫的登陆解密过程
     *
     * @param data
     * @param key
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchProviderException
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        UnsupportedEncodingException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchProviderException::class
    )
    fun decryptAES2(data: String, key: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC")
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset(CHARSETNAME)), AES)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val base64Decrypt = Base64Decrypt(data)
        val doFinal = cipher.doFinal(base64Decrypt)
        return doFinal.toString()+CHARSETNAME
    }
}