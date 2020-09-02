package com.veryxiang.common.utils

import android.app.Application
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.KEY_ALGORITHM_RSA
import android.util.Base64
import com.veryxiang.common.enums.BusinessExceptionEnum
import com.veryxiang.common.enums.SystemExceptionEnum
import com.veryxiang.common.exception.BusinessException
import com.veryxiang.common.exception.SystemException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


/**
 * 安全工具，本地加密和解密
 */
class SecureUtils {

    private val AES_ALGO="AES/CBC/PKCS7Padding"
    private val RSA_KEY_NAME="VeryXiangRSA";
    private val AES_KEY_NAME="VeryXiangAES";
    private val ANDROID_KEY_STORE = "AndroidKeyStore";
    private val appContext: Context;
    private var privateKey: PrivateKey? = null;
    private var publicKey: PublicKey? = null;
    private var aesKey: SecretKey? = null;
    private val BLOCK_SIZE_FOR_ENC = 245;
    private val BLOCK_SIZE_FOR_DEC = 256;
    private var isFallbackAES = false;

    protected constructor(app: Context)
    {
        appContext = app.applicationContext;
        initRSA();
        initAES();
    }

    companion object {
        private var instance: SecureUtils? = null;

        fun getInstance(app: Context?=null): SecureUtils
        {
            if (instance != null) return instance!!;
            if (app == null) {
                throw SystemException(SystemExceptionEnum.BUG, "SecureUtils.getInstance首次调用必须传入Context对象");
            }
            synchronized(SecureUtils::class.java) {
                if (instance != null) return instance!!;
                instance = SecureUtils(app);
                return instance!!;
            }
        }
    }

    //初始化RSA加密
    private fun initRSA()
    {
        val ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        val key = ks.getKey(RSA_KEY_NAME, null);
        if (key == null || (key as? PrivateKey) == null) {
            generateRSA();
        }
        loadRSA();
    }

    /**
     * 从密钥库中加载
     */
    private fun loadRSA()
    {
        val ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        privateKey = ks.getKey(RSA_KEY_NAME, null) as PrivateKey;
        publicKey = ks.getCertificate(RSA_KEY_NAME).publicKey;
    }

    /**
     * 生成RSA密钥对
     */
    private fun generateRSA()
    {
        val start = GregorianCalendar()
        //有效期从1970/1/1 00:00:00开始, 主要为了防止设备时间不准确归0
        start.set(1970, 1, 1, 0,0,0);
        val end: Calendar = GregorianCalendar()
        //有效期结束为当前时间+100年
        end.add(Calendar.YEAR, 100);

        val spec: AlgorithmParameterSpec
        spec = if (Build.VERSION.SDK_INT < 23) {
            KeyPairGeneratorSpec.Builder(appContext)
                .setAlias(RSA_KEY_NAME)
                .setSubject(X500Principal("CN=$RSA_KEY_NAME"))
                .setSerialNumber(BigInteger.valueOf(2020))
                .setStartDate(start.time).setEndDate(end.time)
                .setKeySize(2048)
                .build()
        } else {
            //KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT or or KeyProperties.PURPOSE_VERIFY
            KeyGenParameterSpec.Builder(RSA_KEY_NAME,  KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeyValidityStart(start.time)
                .setKeyValidityEnd(end.time)
                .setKeySize(2048)
                .build()
        }
        val kpGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
        kpGenerator.initialize(spec);
        // Generate private/public keys
        kpGenerator.generateKeyPair();
    }


    private fun initAES()
    {
        if (Build.VERSION.SDK_INT < 23) {
            initAESFallback();
            return;
        }
        val ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        val key = ks.getKey(AES_KEY_NAME, null);
        if (key != null && (key as? SecretKey) != null) {
            aesKey = key;
            return;
        }
        try {
            val aesSpec = KeyGenParameterSpec.Builder(
                    AES_KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setKeySize(128)
                .build()
            val keyGenerator: KeyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            keyGenerator.init(aesSpec)
            keyGenerator.generateKey()
            loadAES();
        } catch(e: Throwable) {
            e.printStackTrace();
            initAESFallback();
        }
    }

    private fun loadAES() {
        val ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        val key = ks.getKey(AES_KEY_NAME, null);
        if (key == null) {
            throw SystemException(SystemExceptionEnum.BUG, "AES KEY not in keystore");
        }
        aesKey = key as SecretKey;
    }


/**
* 读取文件到字符串，忽略所有异常
*/
private fun readFileToString(f: File): String?
{
    try {
        FileInputStream(f).use {
            val ins = it;
            val bytes = ByteArray(ins.available());
            ins.read(bytes);
            return bytes.toString(charset("UTF-8"));
        }
    } catch(e: Throwable) {
        e.printStackTrace();
        return null;
    }
}

private fun saveToFile(s: String, f: File)
{
    FileOutputStream(f).use {
        it.write(s.toByteArray(charset("UTF-8")));
    }
}

/**
* 当AndroidKeyStore不支持AES时，采用模拟的方式支持：生成的AES KEY采用RSA加密存储到文件中
*/
private fun initAESFallback()
{
    val f = File(appContext.applicationInfo.dataDir + "/$AES_KEY_NAME");
    if (f.exists() && f.isFile) {
        val fileData = readFileToString(f);
        if (fileData != null) {
            try {
                aesKey = SecretKeySpec(decryptByRSA(fileData), AES_ALGO);
                isFallbackAES = true;
                return;
            } catch(e: Throwable) {
                e.printStackTrace()
            }
        }
    }
    //随机生成
    val k = ByteArray(16);
    RandomUtils.generateRandomBytes(k);
    saveToFile(encryptByRSA(k), f);
    aesKey = SecretKeySpec(k, AES_ALGO);
    isFallbackAES = true;
}

/**
* 使用公钥加密
* @param data 待加密数据
* @param k
* @return base64编码的字符串
*/
fun encryptByRSA(data: ByteArray): String {
    val k = publicKey;
    var cipher: Cipher? = null
    cipher = try {
        Cipher.getInstance("RSA/ECB/PKCS1Padding")
    } catch (e: NoSuchAlgorithmException) {
        throw SystemException(SystemExceptionEnum.BUG, "没RSA算法", e)
    } catch (e: NoSuchPaddingException) {
        throw SystemException(SystemExceptionEnum.BUG, "RSA/ECB/PKCS1Padding算法", e)
    }
    try {
        cipher.init(Cipher.ENCRYPT_MODE, k)
    } catch (e: InvalidKeyException) {
        throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "公钥非法", e)
    }
    try {
        if (data.size <= BLOCK_SIZE_FOR_ENC) {
            return Base64.encodeToString(cipher.doFinal(data), Base64.NO_WRAP)
        }
        var n: Int = data.size / BLOCK_SIZE_FOR_ENC
        if (data.size % BLOCK_SIZE_FOR_ENC !== 0) {
            n++
        }
        n *= BLOCK_SIZE_FOR_DEC
        val encBuf = ByteArray(n)
        var ipos = 0
        var opos = 0
        while (data.size - ipos > BLOCK_SIZE_FOR_ENC) {
            cipher.doFinal(data, ipos, BLOCK_SIZE_FOR_ENC, encBuf, opos)
            ipos += BLOCK_SIZE_FOR_ENC
            opos += BLOCK_SIZE_FOR_DEC
        }
        if (data.size - ipos > 0) {
            cipher.doFinal(data, ipos, data.size - ipos, encBuf, opos)
        }
        return Base64.encodeToString(encBuf, Base64.NO_WRAP)
    } catch (e: IllegalBlockSizeException) {
        throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "数据非法", e)
    } catch (e: BadPaddingException) {
        throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "数据填充非法", e)
    }
}

/**
* 使用私钥钥解密
* @param data base64编码的待解密数据
* @param k
* @return 解密后
*/
fun decryptByRSA(data: String): ByteArray {
    val k = privateKey;
    var cipher: Cipher? = null
    cipher = try {
        Cipher.getInstance("RSA/ECB/PKCS1Padding")
    } catch (e: NoSuchAlgorithmException) {
        throw SystemException(SystemExceptionEnum.BUG, "没RSA算法", e)
    } catch (e: NoSuchPaddingException) {
        throw SystemException(SystemExceptionEnum.BUG, "RSA/ECB/PKCS1Padding算法", e)
    }
    try {
        cipher.init(Cipher.DECRYPT_MODE, k)
    } catch (e: InvalidKeyException) {
        throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "私钥非法", e)
    }
    try {
        val src: ByteArray = Base64.decode(data, Base64.DEFAULT)
        if (src.size % BLOCK_SIZE_FOR_DEC !== 0) {
            throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "数据长度非法")
        }
        val n: Int = src.size / BLOCK_SIZE_FOR_DEC
        if (n == 1) {
            return cipher.doFinal(src)
        }
        var ipos = 0
        var opos = 0
        val out =
            ByteArray((n - 1) * BLOCK_SIZE_FOR_ENC + BLOCK_SIZE_FOR_DEC)
        for (i in 0 until n) {
            val dataSize = cipher.doFinal(src, ipos, BLOCK_SIZE_FOR_DEC, out, opos)
            opos += dataSize
            ipos += BLOCK_SIZE_FOR_DEC
        }
        return Arrays.copyOfRange(out, 0, opos)
    } catch (e: IllegalBlockSizeException) {
        throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "数据非法", e)
    } catch (e: BadPaddingException) {
        throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "数据填充非法", e)
    }
}

/**
* 使用AES加密数据到base64字符串
*/
fun encryptByAES(data: ByteArray): String
{
    val cipher = try {
        Cipher.getInstance(AES_ALGO)
    } catch (e: NoSuchAlgorithmException) {
        throw SystemException(
            SystemExceptionEnum.BUG,
            "${AES_ALGO}算法",
            e
        )
    }
    try {
        if (isFallbackAES) {
            val iv = ByteArray(16);
            RandomUtils.generateRandomBytes(iv)
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        }
    } catch (e: InvalidKeyException) {
        throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "AES密钥非法", e)
    }
    val enc = cipher.doFinal(data);
    val full = ByteArray(16+enc.size);
    System.arraycopy(cipher!!.iv, 0, full, 0, 16);
    System.arraycopy(enc, 0, full, 16, enc.size);
    return Base64.encodeToString(full, Base64.NO_WRAP);
}

/**
* 使用AES解密base64的加密
*/
fun decryptByAES(data: String): ByteArray
{
    val cipher = try {
        Cipher.getInstance(AES_ALGO)
    } catch (e: NoSuchAlgorithmException) {
        throw SystemException(
            SystemExceptionEnum.BUG,
            "${AES_ALGO}算法",
            e
        )
    }
    val src = Base64.decode(data, Base64.DEFAULT);
    if (src.size < 32) throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "错误的待解密数据");
    try {
        cipher.init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(src, 0,16));
    } catch (e: InvalidKeyException) {
        throw BusinessException(BusinessExceptionEnum.INVALID_PARAMS, "AES密钥非法", e)
    }
    return cipher.doFinal(src, 16, src.size - 16);
}


}