package com.veryxiang.device_push.name

/**
 * 导入旧系统的设备 server -> client, 由新老系统交接时使用，当新老系统完全交接后该指令将被禁用
 */
object IMPORT_OLD_DEVICE {
    const val name = "IMPORT_OLD_DEVICE"


    class Request : PREPARE_IMPORT.Response() {
        /**
         * 用于确认入库的随机数（由设备端生成）
         */
        var nonce2: String? = null
    }
}
