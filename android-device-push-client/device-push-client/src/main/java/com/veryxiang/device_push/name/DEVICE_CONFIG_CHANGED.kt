package com.veryxiang.device_push.name

/**
 * 设备配置变更 server -> client
 * 当客户端收到这个通知时，应当主动调用服务器的配置拉取接口，得到新的配置应用新的配置
 */
object DEVICE_CONFIG_CHANGED {
    const val name = "DEVICE_CONFIG_CHANGED"
}
