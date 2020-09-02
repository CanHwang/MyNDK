package com.veryxiang.common.utils

import com.veryxiang.common.utils.HexUtils.bytesToHex
import java.security.SecureRandom

/**
 * 随机数工具
 */
object RandomUtils {
    private val secureRandom =
        ThreadLocal<SecureRandom>()

    /**
     * 获取当前线程的SecureRandom对象
     * @return
     */
    fun getSecureRandom(): SecureRandom {
        var r = secureRandom.get()
        if (r != null) return r
        r = SecureRandom()
        secureRandom.set(r)
        return r
    }

    /**
     * 产生字节序列
     * @param bytes
     */
    fun generateRandomBytes(bytes: ByteArray) {
        getSecureRandom().nextBytes(bytes)
    }

    /**
     * 产生随机字节序列并转换为16进制字符串
     * @param len
     * @return
     */
    fun generateRandomBytesAsHex(len: Int): String {
        val bytes = ByteArray(len)
        generateRandomBytes(bytes)
        return bytesToHex(bytes)!!
    }
}