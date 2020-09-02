package com.sitlink.sitlinklib.monitor.bean

import android.database.Cursor
import com.sitlink.sitlinklib.sqlitecore.BaseDao


class LoggerDao : BaseDao<Logger>() {
    override fun createTable(): String {
        println("LoggingAspect   createTable")
        return "create table if not exists tb_logger(deviceId varchar(15),source varchar(15),level varchar(2),type varchar(15),data varchar(100), description varchar(100),timestamp INTEGER not null)"
    }


    override fun query(sql: String): MutableList<Logger>? {
        var cursor: Cursor? = null
        try {
            cursor = database?.rawQuery(sql, null)
            var result = getResult(cursor!!, Logger())
            cursor?.close()
            return result
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }
    fun delete(where:String,parameter:Array<String>){
        try {
            database!!.delete("tb_logger",where,parameter)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
