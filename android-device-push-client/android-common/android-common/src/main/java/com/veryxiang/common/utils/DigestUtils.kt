package com.veryxiang.common.utils

import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and


object DigestUtils {
    private const val MD5_ALGORITHM_NAME = "MD5"
    private val HEX_CHARS =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    /**
     * Calculate the MD5 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return the digest
     */
    fun md5Digest(bytes: ByteArray): ByteArray {
        return digest(MD5_ALGORITHM_NAME, bytes)
    }

    /**
     * Calculate the MD5 digest of the given stream.
     * @param inputStream the InputStream to calculate the digest over
     * @return the digest
     * @since 4.2
     */
    @Throws(IOException::class)
    fun md5Digest(inputStream: InputStream): ByteArray {
        return digest(MD5_ALGORITHM_NAME, inputStream)
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given bytes.
     * @param bytes the bytes to calculate the digest over
     * @return a hexadecimal digest string
     */
    fun md5DigestAsHex(bytes: ByteArray): String {
        return digestAsHexString(MD5_ALGORITHM_NAME, bytes)
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given stream.
     * @param inputStream the InputStream to calculate the digest over
     * @return a hexadecimal digest string
     * @since 4.2
     */
    @Throws(IOException::class)
    fun md5DigestAsHex(inputStream: InputStream): String {
        return digestAsHexString(MD5_ALGORITHM_NAME, inputStream)
    }

    /**
     * Append a hexadecimal string representation of the MD5 digest of the given
     * bytes to the given [StringBuilder].
     * @param bytes the bytes to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     */
    fun appendMd5DigestAsHex(
        bytes: ByteArray,
        builder: StringBuilder
    ): StringBuilder {
        return appendDigestAsHex(MD5_ALGORITHM_NAME, bytes, builder)
    }

    /**
     * Append a hexadecimal string representation of the MD5 digest of the given
     * inputStream to the given [StringBuilder].
     * @param inputStream the inputStream to calculate the digest over
     * @param builder the string builder to append the digest to
     * @return the given string builder
     * @since 4.2
     */
    @Throws(IOException::class)
    fun appendMd5DigestAsHex(
        inputStream: InputStream,
        builder: StringBuilder
    ): StringBuilder {
        return appendDigestAsHex(MD5_ALGORITHM_NAME, inputStream, builder)
    }

    /**
     * Create a new [MessageDigest] with the given algorithm.
     * Necessary because `MessageDigest` is not thread-safe.
     */
    private fun getDigest(algorithm: String): MessageDigest {
        return try {
            MessageDigest.getInstance(algorithm)
        } catch (ex: NoSuchAlgorithmException) {
            throw IllegalStateException(
                "Could not find MessageDigest with algorithm \"$algorithm\"",
                ex
            )
        }
    }

    private fun digest(algorithm: String, bytes: ByteArray): ByteArray {
        return getDigest(algorithm).digest(bytes)
    }

    @Throws(IOException::class)
    private fun digest(
        algorithm: String,
        inputStream: InputStream
    ): ByteArray {
        val messageDigest = getDigest(algorithm)
        val buffer = ByteArray(4096)
        var bytesRead = -1
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
             messageDigest.update(buffer, 0, bytesRead)
        }
        return messageDigest.digest()
    }

    private fun digestAsHexString(
        algorithm: String,
        bytes: ByteArray
    ): String {
        val hexDigest = digestAsHexChars(algorithm, bytes)
        return String(hexDigest)
    }

    @Throws(IOException::class)
    private fun digestAsHexString(
        algorithm: String,
        inputStream: InputStream
    ): String {
        val hexDigest = digestAsHexChars(algorithm, inputStream)
        return String(hexDigest)
    }

    private fun appendDigestAsHex(
        algorithm: String,
        bytes: ByteArray,
        builder: StringBuilder
    ): StringBuilder {
        val hexDigest = digestAsHexChars(algorithm, bytes)
        return builder.append(hexDigest)
    }

    @Throws(IOException::class)
    private fun appendDigestAsHex(
        algorithm: String,
        inputStream: InputStream,
        builder: StringBuilder
    ): StringBuilder {
        val hexDigest = digestAsHexChars(algorithm, inputStream)
        return builder.append(hexDigest)
    }

    private fun digestAsHexChars(algorithm: String, bytes: ByteArray): CharArray {
        val digest = digest(algorithm, bytes)
        return encodeHex(digest)
    }

    @Throws(IOException::class)
    private fun digestAsHexChars(
        algorithm: String,
        inputStream: InputStream
    ): CharArray {
        val digest = digest(algorithm, inputStream)
        return encodeHex(digest)
    }

    private fun encodeHex(bytes: ByteArray): CharArray {
        val chars = CharArray(32)
        var i = 0
        while (i < chars.size) {
            val b = bytes[i / 2]
            chars[i] = HEX_CHARS[(b.toUInt().shr(4) and 0xfU).toInt()]
            chars[i + 1] = HEX_CHARS[(b and 0xf).toInt()]
            i = i + 2
        }
        return chars
    }
}
