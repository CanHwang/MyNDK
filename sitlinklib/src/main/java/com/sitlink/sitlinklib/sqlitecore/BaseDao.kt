package com.sitlink.sitlinklib.sqlitecore

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.sitlink.sitlinklib.sqlitecore.annotion.DbFiled
import com.sitlink.sitlinklib.sqlitecore.annotion.DbTable
import java.lang.reflect.Field
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by Administrator on 2017/1/9 0009.
 */

abstract class BaseDao<T : Any> : IBaseDao<T> {
    /**]
     * 持有数据库操作类的引用
     */
    open var database: SQLiteDatabase? = null
    /**
     * 保证实例化一次
     */
    private var isInit = false
    /**
     * 持有操作数据库表所对应的java类型
     * User
     */
    private var entityClass: Class<T>? = null
    /**
     * 维护这表名与成员变量名的映射关系
     * key---》表的列名
     * value --》Field
     * class  methoFiled
     * {
     * Method  setMthod
     * Filed  fild
     * }
     */
    private var cacheMap: HashMap<String, Field>? = null
    /**
     * 表名
     */
    private var tableName: String? = null

    /**
     * @param entity
     * @param sqLiteDatabase
     * @return
     * 实例化一次
     */
    @Synchronized
    fun init(entity: Class<T>, sqLiteDatabase: SQLiteDatabase): Boolean {
        if (!isInit) {
            entityClass = entity
            database = sqLiteDatabase
            if (entity.getAnnotation(DbTable::class.java) == null) {
                tableName = entity.javaClass.simpleName
            } else {
                tableName = entity.getAnnotation(DbTable::class.java)!!.value
            }
            if (!database!!.isOpen) {
                return false
            }
            if (!TextUtils.isEmpty(createTable())) {
                database!!.execSQL(createTable())
            }
            cacheMap = HashMap()
            initCacheMap()
            isInit = true
        }
        return isInit
    }

    /**
     * 维护映射关系
     */
    private fun initCacheMap() {
        /*
        第一条数据  查0个数据
         */
        val sql = "select * from " + this.tableName + " limit 1 , 0"
        var cursor: Cursor? = null
        try {
            cursor = database?.rawQuery(sql, null)
            /**
             * 表的列名数组
             */
            val columnNames = cursor?.columnNames
            /**
             * 拿到Filed数组
             */
            val colmunFields = entityClass?.getFields();//需要有无参构造方法
            if (colmunFields != null) {
                for (filed in colmunFields) {
                    filed.isAccessible = true
                }
            }
            /**
             * 开始找对应关系
             */
            for (colmunName in columnNames!!) {
                /**
                 * 如果找到对应的Filed就赋值给他
                 * User
                 */
                var colmunFiled: Field? = null
                for (field in colmunFields!!) {
                    var fieldName: String? = null
                    if (field.getAnnotation(DbFiled::class.java) != null) {
                        fieldName = field.getAnnotation(DbFiled::class.java)!!.value
                    } else {
                        fieldName = field.name
                    }
                    /**
                     * 如果表的列名 等于了  成员变量的注解名字
                     */
                    if (colmunName.equals(fieldName)) {
                        colmunFiled = field
                        break
                    }
                }
                //找到了对应关系
                if (colmunFiled != null) {
                    cacheMap?.put(colmunName, colmunFiled)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    override fun delete(where: T): Int {
        val map = getValues(where)
        val condition = Condition(map)
        return database!!.delete(tableName, condition.whereClause, condition.whereArgs)
    }

    override fun query(where: T): List<T> {
        return query(where, null, null, null)
    }

    override fun query(where: T, orderBy: String?, startIndex: Int?, limit: Int?): List<T> {
        val map = getValues(where)
        var limitString: String? = null
        if (startIndex != null && limit != null) {
            limitString = "$startIndex , $limit"
        }

        val condition = Condition(map)
        val cursor = database!!.query(
            tableName,
            null,
            condition.whereClause,
            condition.whereArgs,
            null,
            null,
            orderBy,
            limitString
        )
        val result = getResult(cursor, where)
        cursor.close()
        return result
    }

    open fun getResult(cursor: Cursor, where: T): MutableList<T> {
        val list = mutableListOf<T>()

        var item: Any
        while (cursor.moveToNext()) {
            try {
                item = where.javaClass.newInstance()
                /**
                 * 列名  name
                 * 成员变量名  Filed;
                 */
                val iterator = cacheMap!!.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    /**
                     * 得到列名
                     */
                    val colomunName = entry.key as String
                    /**
                     * 然后以列名拿到  列名在游标的位子
                     */
                    val colmunIndex = cursor.getColumnIndex(colomunName)

                    val field = entry.value as Field

                    val type = field.type
                    if (colmunIndex != -1) {
                        if (type == String::class.java) {
                            //反射方式赋值
                            field.set(item, cursor.getString(colmunIndex))
                        } else if (type == Double::class.java) {
                            field.set(item, cursor.getDouble(colmunIndex))
                        } else if (type == Int::class.java) {
                            field.set(item, cursor.getInt(colmunIndex))
                        } else if (type == Long::class.java) {
                            field.set(item, cursor.getLong(colmunIndex))
                        } else if (type == ByteArray::class.java) {
                            field.set(item, cursor.getBlob(colmunIndex))
                            /*
                            不支持的类型
                             */
                        } else {
                            continue
                        }
                    }
                }
                list.add(item)
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        }
        return list
    }

    /**
     * 讲 map 转换成ContentValues
     * @param map
     * @return
     */
    private fun getContentValues(map: Map<String, String>): ContentValues {
        val contentValues = ContentValues()
        val keys = map.keys
        val iterator = keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val value = map[key]
            if (value != null) {
                contentValues.put(key, value)
            }
        }
        return contentValues
    }

    /**
     * 将对象拥有的成员变量
     * 转换成  表的列名  ---》成员变量的值
     * 如  tb_name  ----> "张三"
     * 这样的map集合
     * User
     * name  "zhangsn"
     * @param entity
     * @return
     */
    private fun getValues(entity: T): Map<String, String> {
        val result = HashMap<String, String>()
        val filedsIterator = cacheMap!!.values.iterator()
        /**
         * 循环遍历 映射map的  Filed
         */
        while (filedsIterator.hasNext()) {
            /**
             *
             */
            val colmunToFiled = filedsIterator.next()
            var cacheKey: String? = null
            var cacheValue: String? = null
            if (colmunToFiled.getAnnotation(DbFiled::class.java) != null) {
                cacheKey = colmunToFiled.getAnnotation(DbFiled::class.java)?.value
            } else {
                cacheKey = colmunToFiled.name
            }
            try {
                if (null == colmunToFiled.get(entity)) {
                    continue
                }
                cacheValue = colmunToFiled.get(entity)?.toString()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            result.put(cacheKey!!, cacheValue!!);
        }
        return result
    }

    override fun insert(entity: T): Long? {
        val map = getValues(entity)
        val values = getContentValues(map)
        return database?.insert(tableName, null, values)
    }

    override fun update(entity: T, where: T): Int {
        var reslut = -1
        val values = getValues(entity)
        /**
         * 将条件对象 转换map
         */
        val whereClause = getValues(where)

        val condition = Condition(whereClause)
        val contentValues = getContentValues(values)
        reslut = database!!.update(tableName, contentValues, condition.whereClause, condition.whereArgs)
        return reslut
    }

    /**
     * 封装修改语句
     *
     */
    internal inner class Condition(whereClause: Map<String, String>) {
        /**
         * 查询条件
         * name=? && password =?
         */
        val whereClause: String

        val whereArgs: Array<String>

        init {
            val list = ArrayList<String>()
            val stringBuilder = StringBuilder()

            stringBuilder.append(" 1=1 ")
            val keys = whereClause.keys
            val iterator = keys.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val value = whereClause[key]

                if (value != null) {
                    /*
                    拼接条件查询语句
                    1=1 and name =? and password=?
                     */
                    stringBuilder.append(" and $key =?")
                    /**
                     * ？----》value
                     */
                    list.add(value)
                }
            }
            this.whereClause = stringBuilder.toString()
            this.whereArgs = list.toTypedArray()

        }
    }

    /**
     * 创建表
     * @return
     */
    protected abstract fun createTable(): String
}
