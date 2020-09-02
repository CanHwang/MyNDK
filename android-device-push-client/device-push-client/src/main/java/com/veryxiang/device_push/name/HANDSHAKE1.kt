package com.veryxiang.device_push.name


object HANDSHAKE1 {
    /**
     * 客户端发送到服务端发送到握手1
     * 入口参数: {hash: "64字节的HMAC_SHA256哈希, 客户端结算，由服务器校验"}
     * 返回结果 {hash: "64字节的HMAC_SHA256哈希, 服务端计算，由客户端校验"}
     */
    const val name = "HANDSHAKE1"

    class Request {
        var hash: String? = null

        override fun toString(): String {
            return "Request(hash=$hash)"
        }
    }

    class Response {
        var hash: String? = null

        override fun toString(): String {
            return "Response(hash=$hash)"
        }
    }
}