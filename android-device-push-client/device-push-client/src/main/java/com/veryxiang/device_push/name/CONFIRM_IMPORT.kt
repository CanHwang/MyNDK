package com.veryxiang.device_push.name


/**
 * 确认设备入库函数 server -> client
 */
object CONFIRM_IMPORT {
    const val name = "CONFIRM_IMPORT"

    
    class Request {
        /**
         * 设备ID
         */
        var deviceId: String? = null

        /**
         * 用于确认入库的随机数
         */
        var nonce2: String? = null

        /**
         * 设备密钥
         */
        var deviceKey: String? = null

        /**
         * 设备维护密码
         */
        var password: String? = null

        /**
         * 设备型号
         */
        var model: String? = null

        /**
         * 设备附加参数JSON（例如：校准参数等), 如果没有则是null
         */
        var extraParams: String? = null

        override fun toString(): String {
            return "Request(deviceId=$deviceId, nonce2=$nonce2, deviceKey=$deviceKey, password=$password, model=$model, extraParams=$extraParams)"
        }

    }
}