package com.veryxiang.device_push.name


/**
 * 取消入库 server -> client
 */
object CANCEL_IMPORT {
    const val name = "CANCEL_IMPORT"

    
    class Request {
        /**
         * 设备ID
         */
        var deviceId: String? = null

        /**
         * 取消信息
         */
        var message: String? = null

        override fun toString(): String {
            return "Request(deviceId=$deviceId, message=$message)"
        }

    }
}
