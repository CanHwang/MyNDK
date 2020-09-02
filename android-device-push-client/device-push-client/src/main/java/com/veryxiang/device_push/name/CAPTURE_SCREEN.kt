package com.veryxiang.device_push.name

/**
 * 截屏操作
 * server -> client
 */
object CAPTURE_SCREEN {
    const val name = "CAPTURE_SCREEN"


    class Request {
        /**
         * 文件上传token
         */
        val fileToken: String? = null

        override fun toString(): String {
            return "Request(fileToken=$fileToken)"
        }

    }
}
