package com.veryxiang.device_push.name

import java.util.*


/**
 * 汇报设备状态
 */
object REPORT_DEVICE_STATUS {
    const val name = "REPORT_DEVICE_STATUS"


    class Response {
        /**
         * 目前配置的场地ID
         */
        var tenantId: String? = null

        /**
         * 应用程序类型, 0为未安装业务应用
         */
        var appType = 0

        /**
         * 应用程序版本, 0为未安装业务应用
         */
        var appVer = 0

        /**
         * 管理程序版本
         */
        var managerVer = 0

        /**
         * 固件版本
         */
        var fwVer = 0

        /**
         * 设备位置，经度
         */
        var lng: Double? = null

        /**
         * 设备位置，纬度
         */
        var lat: Double? = null

        /**
         * 屏幕是否亮屏
         */
        var screenOn = false

        /**
         * 是否进入维护模式
         */
        var maintainMode = false

        /**
         * 目前的网络类型2g/3g/4g/5g/wifi
         */
        var networkType: String? = null

        /**
         * 本机IP
         */
        var innerIP: String? = null

        /**
         * CPU使用率
         */
        var cpuUsage = 0

        /**
         * 当前内存使用情况
         * @param memUsage 高16位位内存总量, 低16位为内存使用量, 单位M
         */
        var memUsage = 0
        /**
         * CPU温度, 摄氏度
         */
        var cpuTemperature: Double? = null


        /**
         * 流量使用量, 从每个月1日开始累计的流量，单位MB
         */
        var bwUsage: Long? = null

        /**
         * 获取4g信号强度，单位dbm
         */
        var dbm: Long = 0

        /**
         * 磁盘剩余空间, 单位MB
         */
        var diskFree: Long = 0

        /**
         * 设备端时间, unix时间戳
         */
        var clientTime: Long = 0

        /**
         * 充电口的状态
         * key => 充电口号
         * value => 充电口剩余开电时间, 单位s, 如果为0表示充电口目前是关闭状态, 如果是-1表示目前充电口处于长开并未倒计时状态
         */
        var chargePortStatus: HashMap<Int, Int>? = null

        /**
         * 设备MAC
         */
        var mac: String? = null

        /**
         * 设备的ICCID
         */
        var iccid: String? = null

        override fun toString(): String {
            return "Response(tenantId=$tenantId, appType=$appType, appVer=$appVer, managerVer=$managerVer, fwVer=$fwVer, lng=$lng, lat=$lat, screenOn=$screenOn, maintainMode=$maintainMode, networkType=$networkType, innerIP=$innerIP, cpuUsage=$cpuUsage, memUsage=$memUsage, dbm=$dbm, diskFree=$diskFree, clientTime=$clientTime, chargePortStatus=$chargePortStatus, mac=$mac, iccid=$iccid)"
        }

    }
}