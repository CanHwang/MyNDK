package com.veryxiang.device_push.name


/**
 * 关闭网络
 */
object NETWORK_OFF {
    const val name = "NETWORK_OFF"

    class Request {
        /**
         * 收到指令后延迟多少秒关闭网络, 如果为0则立即关闭网络, 如果为-1则该字段无效，需要取消在delay后关闭网络
         */
        val delay = 0

        /**
         * 在这个绝对时间进行关闭网络, unix时间戳, 如果为-1则表示该字段无效，需取消在绝对时间关闭网络
         */
        val at: Long = 0

        /**
         * 在设备关闭网络后的enableAfter秒后开启网络
         */
        val enableAfter = 0

        override fun toString(): String {
            return "Request(delay=$delay, at=$at, enableAfter=$enableAfter)"
        }

    }
}
