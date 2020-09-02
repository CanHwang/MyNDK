package com.veryxiang.common.utils

import android.util.Log
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.veryxiang.common.enums.SystemExceptionEnum
import com.veryxiang.common.exception.SystemException
import java.io.IOException

/**
 * @Description
 * @Auther: YangChengXiao
 * @Email yangchengxiao@sitlink.com.cn
 * @Date: 2019/11/6 9:00
 */
object JsonUtils {
    val mapper: ObjectMapper = ObjectMapper()
    val mapperFlex: ObjectMapper = createFlexObjectMapper()
    private fun createFlexObjectMapper(): ObjectMapper {
        val r = ObjectMapper()
        r.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return r
    }

    fun serialize(obj: Any): String {
        return if (obj.javaClass == String::class.java) {
            obj as String
        } else try {
            mapper.writeValueAsString(obj)
        } catch (e: JsonProcessingException) {
            e.printStackTrace();
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }

    fun <T> parse(json: String, tClass: Class<T>?): T {
        return try {
            mapper.readValue(json, tClass)
        } catch (e: IOException) {
            e.printStackTrace()
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }

    /**
     * 解析对象，宽松模式，不存在的属性也不报错
     * @param json
     * @param tClass
     * @param <T>
     * @return
    </T> */
    fun <T> parseFlex(json: String, tClass: Class<T>?): T {
        return try {
            mapperFlex.readValue(json, tClass)
        } catch (e: IOException) {
            e.printStackTrace()
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }

    fun <E> parseList(
        json: String,
        eClass: Class<E>?
    ): List<E> {
        return try {
            mapper.readValue(
                json, mapper.getTypeFactory().constructCollectionType(
                    MutableList::class.java, eClass
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }

    fun <E> parseListFlex(
        json: String,
        eClass: Class<E>?
    ): List<E> {
        return try {
            mapperFlex.readValue(
                json, mapperFlex.getTypeFactory().constructCollectionType(
                    MutableList::class.java, eClass
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }

    fun <K, V> parseMap(
        json: String,
        kClass: Class<K>?,
        vClass: Class<V>?
    ): Map<K, V> {
        return try {
            mapper.readValue(
                json, mapper.getTypeFactory().constructMapType(
                    MutableMap::class.java, kClass, vClass
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }

    fun <K, V> parseMapFlex(
        json: String,
        kClass: Class<K>?,
        vClass: Class<V>?
    ): Map<K, V> {
        return try {
            mapperFlex.readValue(
                json, mapperFlex.getTypeFactory().constructMapType(
                    MutableMap::class.java, kClass, vClass
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }

    fun <T> nativeRead(json: String, type: TypeReference<T>?): T {
        return try {
            mapper.readValue(json, type)
        } catch (e: IOException) {
            e.printStackTrace()
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }

    fun <T> nativeReadFlex(json: String, type: TypeReference<T>?): T {
        return try {
            mapperFlex.readValue(json, type)
        } catch (e: IOException) {
            e.printStackTrace()
            throw SystemException(SystemExceptionEnum.SERIALIZE)
        }
    }
}