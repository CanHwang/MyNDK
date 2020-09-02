package com.veryxiang.device_push.name


/**
 * 控制设备进入维护模式
 * server -> client
 */
object ENTER_MAINTAIN {
    const val name = "ENTER_MAINTAIN"


    class Request {
        /**
         * 控制进入还是离开运维模式
         */
        val maintainMode = false

        override fun toString(): String {
            return "Request(maintainMode=$maintainMode)"
        }

    }
}
