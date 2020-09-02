package com.veryxiang.device_push.name

/**
 * 获取Token函数 client -> server
 */
object GET_ACCESS_TOKEN {
    const val name = "GET_ACCESS_TOKEN"
    
    class Response {
        /**
         * 接口请求accessToken
         */
        var accessToken: String? = null

        /**
         * accessToken过期时间s
         */
        var exireTime: Long = 0

        /**
         * accessToken的创建时间s
         */
        var createAt: Long = 0

        override fun toString(): String {
            return "Response(accessToken=$accessToken, exireTime=$exireTime, createAt=$createAt)"
        }

    }
}
