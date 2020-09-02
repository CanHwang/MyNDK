package com.veryxiang.device_push.name


/**
 * 执行关机，后一次指令覆盖前一次指令
 * server -> client
 */
object POWER_OFF {
    const val name = "POWER_OFF"

    class Request {
        /**
         * 收到指令后延迟多少秒关机, 如果为0则立即关机, 如果为-1则该字段无效，需要取消在delay后关机
         */
        val delay = 0

        /**
         * 在这个绝对时间进行关机, unix时间戳, 如果为-1则表示该字段无效，需取消在绝对时间关机
         */
        val at: Long = 0

        /**
         * 在设备关机后的bootAfter秒启动设备, 如果-1则表示该字段无效，需取消关机后重启的功能
         */
        val bootAfter = 0

        override fun toString(): String {
            return "Request(delay=$delay, at=$at, bootAfter=$bootAfter)"
        }

    }
}
