package com.veryxiang.device_push.name


/**
 * 准备入库函数 server -> client
 */
object PREPARE_IMPORT {
    const val name = "PREPARE_IMPORT"

    
    class Request {
        /**
         * 设备ID
         */
        var deviceId: String? = null

        /**
         * 准备入库用到的随机数
         */
        var nonce1: String? = null

        /**
         * 确认入库用到的随机数
         */
        var nonce2: String? = null

        override fun toString(): String {
            return "Request(deviceId=$deviceId, nonce1=$nonce1, nonce2=$nonce2)"
        }

    }

    open class Response {
        /**
         * 设备ID
         */
        var deviceId: String? = null

        /**
         * 设备类型四字码
         */
        var deviceType = 0

        /**
         * 设备IMEI号
         */
        var imei: String? = null

        /**
         * 设备ICCID
         */
        var iccid: String? = null

        /**
         * 设备MAC地址
         */
        var mac: String? = null

        /**
         * 固件版本
         */
        var fwVer = 0

        /**
         * 管理程序版本
         */
        var managerVer = 0

        /**
         * 如果设备有能力判定自己的型号，则会响应这个字段, 否则为空
         */
        var model: String? = null

        /**
         * 用于设备型号探测的硬件数据, 如果model为空, 则管理程序需上传硬件信息, key/value形式，由后端进行设备型号匹配和探测
         */
        var probeInfo: HashMap<String, String>? = null

        /**
         * 设备附加数据JSON对象格式（例如校准参数等), 如无需传递该参数则可为空
         */
        var extraParams: String? = null

        override fun toString(): String {
            return "Response(deviceId=$deviceId, deviceType=$deviceType, imei=$imei, iccid=$iccid, mac=$mac, fwVer=$fwVer, managerVer=$managerVer, model=$model, probeInfo=$probeInfo, extraParams=$extraParams)"
        }

    }
}