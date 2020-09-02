package com.veryxiang.device_push.exception

import com.veryxiang.common.exception.ExceptionCode




object  ExceptionCodeEnum {
    val INVALID_HANDSHAKE_PARAMS = ExceptionCode.define(1700, "握手参数非法!")
    val HANDSHAKE_NOT_COMPLETED = ExceptionCode.define(1701, "握手未完成!")
    val DEVICE_NOT_ON_THIS_INSTANCE = ExceptionCode.define(1702, "设备未与本实例建立连接!")
    val DEVICE_NOT_CONNECTED = ExceptionCode.define(1703, "设备未与服务建立连接!")
    val INSTANCE_NOT_FOUND = ExceptionCode.define(1704, "设备连接的服务实例不存在!")
    val INSTANCE_NOT_UP = ExceptionCode.define(1705, "设备连接的服务实例状态不是UP!")
    val DEVICE_NOT_FOUND = ExceptionCode.define(1000, "设备不存在")
    val DEVICE_TYPE_NOT_FOUND = ExceptionCode.define(1001, "设备类型不存在")
    val TENANT_NOT_FOUND = ExceptionCode.define(1002, "场地不存在")
    val TERMINAL_NOT_FOUND = ExceptionCode.define(1003, "大楼不存在")
    val FLOOR_NOT_FOUND = ExceptionCode.define(1004, "楼层不存在")
    val GATE_NOT_FOUND = ExceptionCode.define(1005, "门牌不存在")
    val AREA_NOT_FOUND = ExceptionCode.define(1006, "片区不存在")
    val FIRMWARE_NOT_FOUND = ExceptionCode.define(1007, "固件不存在")
    val FIRMWARE_VER_NOT_FOUND = ExceptionCode.define(1008, "固件版本不存在")
    val MANAGER_NOT_FOUND = ExceptionCode.define(1009, "管理程序不存在")
    val MANAGER_VER_NOT_FOUND = ExceptionCode.define(1010, "管理程序版本不存在")
    val APP_NOT_FOUND = ExceptionCode.define(1011, "业务软件不存在")
    val APP_VER_NOT_FOUND = ExceptionCode.define(1012, "业务软件版本不存在")
    val PROFILE_NOT_FOUND = ExceptionCode.define(1013, "配置不存在")
    val MODEL_NOT_FOUND = ExceptionCode.define(1014, "型号不存在")
    val LABEL_NOT_FOUND = ExceptionCode.define(1015, "标签不存在")
    val DEVICE_NOT_PROBE = ExceptionCode.define(1016, "设备型号无法探测")
    val MODEL_IN_USING = ExceptionCode.define(1017, "还存在设备与该设备型号关联")
    val DEVICE_IS_TERMINATED = ExceptionCode.define(1018, "设备已经被废弃")
}