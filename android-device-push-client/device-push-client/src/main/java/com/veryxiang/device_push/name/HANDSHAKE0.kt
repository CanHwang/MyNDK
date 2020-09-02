package com.veryxiang.device_push.name


object HANDSHAKE0 {
    /**
     * 服务端发送到客户端握手0
     * 入口参数: {C1: "随机字符串，长度至少64"}
     * 无返回
     */
    const val name = "HANDSHAKE0"

    class Request {
        var C1: String? = null

        override fun toString(): String {
            return "Request(C1=$C1)"
        }
    }
}
