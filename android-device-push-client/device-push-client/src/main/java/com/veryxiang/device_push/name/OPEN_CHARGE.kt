package com.veryxiang.device_push.name




/**
 * 设备开关电
 */
object OPEN_CHARGE {
    const val name = "OPEN_CHARGE"

    class Request {
        /**
         * 设备充电端口, 0 为全部端口
         */
        val port = 0

        /**
         * 开电时长, 单位秒，当值为0则表示结束此开电业务
         */
        val duration = 0

        /**
         * 开电业务名
         * 不同的(开电业务名+port)终端应当并行管理，不同的开电业务不能叠加和相互干扰，以实现多充电业务并行.
         * 例如A业务开电1号端口300秒，B业务开电1号端口600秒，那么A业务倒计时为0时，因为B业务还在持续，因此1号端口需要持续到B业务结束后才能关闭
         */
        val business: String? = null

        override fun toString(): String {
            return "Request(port=$port, duration=$duration, business=$business)"
        }

    }
}
