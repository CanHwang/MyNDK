package com.sitlink.sitlinklib.monitor.bean

import com.sitlink.sitlinklib.sqlitecore.annotion.DbFiled
import com.sitlink.sitlinklib.sqlitecore.annotion.DbTable
import android.R.attr.password
import android.R.attr.name



@DbTable("tb_logger")
class Logger {
    @DbFiled("deviceId")
    lateinit var deviceId:String//设备id
    @DbFiled("source")
    lateinit var source:String//日志来源   应用日志,推送指令,系统监控,系统异常
    @DbFiled("level")
    lateinit var level:String//日志级别  (I, W, E, F)
    @DbFiled("type")
    lateinit var type:String//日志类型  监控管理程序，业务软件，硬件检测，系统固件, 其它服务名
    @DbFiled("data")
     var data:HashMap<String, Object>?=null//
    @DbFiled("description")
    lateinit var description:String//日志描述  功能名+参数
    @DbFiled("timestamp")
    lateinit var timestamp:String//日志时间

    constructor (deviceId:String,source:String,level: String,type: String,data: HashMap<String, Object>?, description:String,timestamp: Long) {
        this.deviceId=deviceId
        this.source=source
        this.level=level
        this.type=type
        this.data=data
        this.description  = description
        this.timestamp  = timestamp.toString()
    }
    constructor() {}
    override fun toString(): String {
        return "  deviceId ${deviceId}  source ${source}  level  $level  type $type  data  $data  description $description  timestamp  $timestamp"
    }
}