package com.veryxiang.device_push.name

/**
 * 服务器请求设备端拉取通知列表 server -> client
 * 收到此命令的客户端需要调用设备通知接口拉取设备通知信息
 */
object SYNC_NOTIFICATION {
    const val name = "SYNC_NOTIFICATION"
}
