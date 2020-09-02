package com.veryxiang.common.utils

import com.veryxiang.common.exception.InvalidParamsException

/**
 * 16进制字符串操作工具
 */
object HexUtils {
    /**
     * 将字节数组转为16进制字符串, 大写
     * @param bytes
     * @return
     */
    @kotlin.ExperimentalUnsignedTypes
    fun bytesToHex(bytes: ByteArray?): String? {
        if (bytes == null) return null
        val sb = StringBuilder()
        for (i in bytes.indices) {
            val b = bytes[i].toUInt()
            val h = (b.shr(4) and 0xFU).toInt();
            if (h >= 10) {
                sb.appendCodePoint(h - 10 + 0x41)
            } else {
                sb.appendCodePoint(h + 0x30)
            }
            val l = (b and 0xFU).toInt();
            if (l >= 10) {
                sb.appendCodePoint(l - 10 + 0x41)
            } else {
                sb.appendCodePoint(l + 0x30)
            }
        }
        return sb.toString()
    }

    /**
     * 将字节数组转为16进制字符串, 小写
     * @param bytes
     * @return
     */
    @kotlin.ExperimentalUnsignedTypes
    fun bytesToHexLower(bytes: ByteArray?): String? {
        if (bytes == null) return null
        val sb = StringBuilder()
        for (i in bytes.indices) {
            val b = bytes[i].toUInt()
            val h = b.shr(4).and(0xFU).toInt();
            if (h >= 10) {
                sb.appendCodePoint(h - 10 + 0x61)
            } else {
                sb.appendCodePoint(h + 0x30)
            }
            val l = b.and(0xFU).toInt();
            if (l >= 10) {
                sb.appendCodePoint(l - 10 + 0x61)
            } else {
                sb.appendCodePoint(l + 0x30)
            }
        }
        return sb.toString()
    }

    /**
     * 将16进制字符串转换为字节数组
     * @param hex 16进制字符串
     * @return 字节数组
     */
    @kotlin.ExperimentalUnsignedTypes
    fun hexToBytes(hex: String?): ByteArray? {
        if (hex == null) return null
        val n = hex.length
        if (n and 1 != 0) {
            throw InvalidParamsException("错误的16进制字符串")
        }
        val r = ByteArray(n / 2)
        var i = 0
        while (i < n) {
            var v: UInt
            var c = hex.codePointAt(i)
            v = if (c >= 0x30 && c <= 0x39) {
                (c - 0x30).toUInt()
            } else if (c >= 0x41 && c <= 0x46) {
                (c - 0x41 + 0xA).toUInt()
            } else if (c >= 0x61 && c <= 0x66) {
                (c - 0x61 + 0xA).toUInt()
            } else {
                throw InvalidParamsException("错误的16进制字符串")
            }
            v = v shl 4
            c = hex.codePointAt(i + 1)
            if (c >= 0x30 && c <= 0x39) {
                v = v or (c - 0x30).toUInt()
            } else if (c >= 0x41 && c <= 0x46) {
                v = v or (c - 0x41 + 0xA).toUInt()
            } else if (c >= 0x61 && c <= 0x66) {
                v = v or (c - 0x61 + 0xA).toUInt()
            } else {
                throw InvalidParamsException("错误的16进制字符串")
            }
            r[i shr 1] = v.toByte();
            i += 2
        }
        return r
    }

    /**
     * 归一化16进制字符串为大写字符串，如果包含非法字符则返回null
     * @param hex 未归一化的16进制字符串
     * @return
     */
    fun normalizeHexString(hex: String?): String? {
        if (hex == null) return null
        var caseWrong = false
        for (i in 0 until hex.length) {
            val c = hex.codePointAt(i)
            if (c >= 0x30 && c <= 0x39 || c >= 0x41 && c <= 0x46) {
            } else if (c >= 0x61 && c <= 0x66) {
                caseWrong = true
            } else return null
        }
        return if (!caseWrong) hex else hex.toUpperCase()
    }

    /**
     * 归一化16进制字符串为小写字符串，如果包含非法字符则返回null
     * @param hex 未归一化的16进制字符串
     * @return
     */
    fun normalizeHexStringLower(hex: String?): String? {
        if (hex == null) return null
        var caseWrong = false
        for (i in 0 until hex.length) {
            val c = hex.codePointAt(i)
            if (c >= 0x30 && c <= 0x39 || c >= 0x61 && c <= 0x66) {
            } else if (c >= 0x41 && c <= 0x46) {
                caseWrong = true
            } else return null
        }
        return if (!caseWrong) hex else hex.toLowerCase()
    }
}