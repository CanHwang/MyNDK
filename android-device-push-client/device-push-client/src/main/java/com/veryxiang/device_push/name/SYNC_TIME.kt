package com.veryxiang.device_push.name

/**
 * 服务器要求设备端同步时间
 */
object SYNC_TIME {
    const val name = "SYNC_TIME"


    class Request {
        /**
         * 服务器时间戳ms
         */
        val now: Long = 0

        override fun toString(): String {
            return "Request(now=$now)"
        }

    }
}
