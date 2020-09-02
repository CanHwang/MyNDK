package com.veryxiang.device_push.name


/**
 * 服务器通知设备执行升级检测
 * server -> client
 */
object CHECK_UPDATE {
    const val name = "CHECK_UPDATE"

    class Request {
        /**
         * 是否执行固件升级检测
         */
        val checkFirmware = false

        /**
         * 是否执行管理程序升级检测
         */
        val checkManager = false

        /**
         * 是否执行业务程序升级检测
         */
        val checkApp = false

        override fun toString(): String {
            return "Request(checkFirmware=$checkFirmware, checkManager=$checkManager, checkApp=$checkApp)"
        }

    }
}