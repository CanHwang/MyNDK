package com.veryxiang.device_push.name

/**
 * 执行设备重启
 */
object REBOOT {
    const val name = "REBOOT"

    class Request {
        /**
         * 收到指令后延迟多少秒重启, 如果为0则立即重启, 如果为-1则该字段无效，需要忽略
         */
        val delay = 0

        /**
         * 在这个绝对时间进行重启, unix时间戳, 如果为-1则表示该字段无效，需忽略
         */
        val at: Long = 0

        override fun toString(): String {
            return "Request(delay=$delay, at=$at)"
        }

    }

}
