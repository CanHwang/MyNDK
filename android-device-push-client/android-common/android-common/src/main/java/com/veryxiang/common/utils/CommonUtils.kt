package com.veryxiang.common.utils

import com.veryxiang.common.exception.InvalidParamsException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Type


/**
 * @Description 公共工具类
 * @Auther: YangChengXiao
 * @Email yangchengxiao@sitlink.com.cn
 * @Date: 2019/11/4 16:55
 */
object CommonUtils {
    /**
     * YangChengXiao
     * 判断对象是否为null，只要有一个属性不为null，返回true(序列化常量字段serialVersionUID除外)
     * @param object
     * @return
     */
    fun objIsNull(`object`: Any): Boolean {
        // 得到类对象
        val clazz: Class<*> = `object`.javaClass
        // 得到所有属性
        val fields: Array<Field> = clazz.declaredFields
        //定义返回结果，默认为false
        var flag = false
        for (field in fields) {
            //设置获取私有属性
            field.setAccessible(true)
            var fieldValue: Any? = null
            var fieldName: String? = null
            try {
                //得到属性值
                fieldValue = field.get(`object`)
                //得到属性类型
                val fieldType: Type = field.getGenericType()
                //得到属性名
                fieldName = field.getName()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            if (fieldValue != null && "serialVersionUID" != fieldName) {
                flag = true
                break
            }
        }
        return flag
    }

    /**
     * 获取四字码的int型表示
     * @param fourCode
     * @return
     */
    fun fourCodeToInt(fourCode: String): Int {
        if (fourCode.length > 4) {
            throw InvalidParamsException("非法的四字码")
        }
        var res = 0
        var i: Int
        i = 0
        while (i < fourCode.length) {
            val ch = fourCode.codePointAt(i)
            if (isValidFourCC(ch)) {
                res = res shl 8
                res = res or ch
            } else {
                throw InvalidParamsException("非法的四字码")
            }
            ++i
        }
        while (i < 4) {
            res = res shl 8
            ++i
        }
        return res
    }

    /**
     * 判断ch字符码是否是合法的4字码字符
     * @param ch
     * @return
     */
    private fun isValidFourCC(ch: Int): Boolean {
        return ch >= 0x41 && ch <= 0x5A || ch >= 0x61 && ch <= 0x7a || ch >= 0x30 && ch <= 0x39 || ch == 0x20
    }

    /**
     * 获取四字码的String型表示
     * @param fourCode
     * @return
     */
    fun fourCodeToString(fourCode: Int): String {
        val result = StringBuilder()
        var ch = fourCode ushr 24
        if (isValidFourCC(ch)) {
            result.appendCodePoint(ch)
        } else {
            throw InvalidParamsException("非法的四字码")
        }
        ch = fourCode ushr 16 and 0xFF
        if (ch == 0) return result.toString()
        if (isValidFourCC(ch)) {
            result.appendCodePoint(ch)
        } else {
            throw InvalidParamsException("非法的四字码")
        }
        ch = fourCode ushr 8 and 0xFF
        if (ch == 0) return result.toString()
        if (isValidFourCC(ch)) {
            result.appendCodePoint(ch)
        } else {
            throw InvalidParamsException("非法的四字码")
        }
        ch = fourCode and 0xFF
        if (ch == 0) return result.toString()
        if (isValidFourCC(ch)) {
            result.appendCodePoint(ch)
        } else {
            throw InvalidParamsException("非法的四字码")
        }
        return result.toString()
    }

    /**
     * MD5哈希
     * @param orgStr
     * @return 返回32个16进制字符串
     */
    fun MD5Encode(orgStr: String): String {
        return DigestUtils.md5DigestAsHex(orgStr.toByteArray())
    }

    /**
     * MD5哈希
     * @param orgStr
     * @return 返回16位的字符串(截取一半)
     */
    fun MD5EncodeHalf(orgStr: String): String {
        return MD5Encode(orgStr).substring(0, 16)
    }

    /**
     * 执行文件MD5哈希
     * @param file
     * @return 返回32个16进制字符串
     * @throws IOException
     */
    @Throws(IOException::class)
    fun MD5File(file: File): String {
        FileInputStream(file).use({ ins -> return DigestUtils.md5DigestAsHex(ins) })
    }


    /**
     * 对字符串进行trim, 如果为空则抛出name不能为空的InvalidParamsException
     * @param v 待检查的字符串
     * @param name 字符串的名字
     * @return trim之后的v
     */
    fun trimNoEmpty(v: String?, name: String): String? {
        var v = v
        var empty = false
        if (v != null) {
            v = v.trim { it <= ' ' }
            empty = v.isEmpty()
        } else {
            empty = true
        }
        if (empty) {
            throw InvalidParamsException(name + "不能为空")
        }
        return v
    }

    /**
     * 校验对象o不能为空，如果为空则抛出name不能为空的InvalidParamsException
     * @param o 待检查的对象
     * @param name 对象名
     */
    fun checkNotEmpty(o: Any?, name: String) {
        if (o == null) throw InvalidParamsException(name + "不能为空")
    }
}
