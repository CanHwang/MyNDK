package com.veryxiang.device_push.name

/**
 * 查询能力列表
 */
object FEATURES {
    const val name = "FEATURES"

    class Response {
        val names: ArrayList<String> = ArrayList();

        override fun toString(): String {
            return "Response(names=$names)"
        }
    }
}