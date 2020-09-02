package com.veryxiang.device_push.name


/**
 * 查询设备充电业务列表
 * server -> client
 */
object GET_CHARGE_BUSINESS {
    const val name = "GET_CHARGE_BUSINESS"


    class ChargeBusiness {
         var port //充电端口
                = 0
        var duration //充电时长s
                = 0
        var left //剩余时长
                = 0
        var business //业务名
                : String? = null

        override fun toString(): String {
            return "ChargeBusiness(port=$port, duration=$duration, left=$left, business=$business)"
        }

    }


    class Response {
        var businessList: List<ChargeBusiness>? = null
        override fun toString(): String {
            return "Response(businessList=$businessList)"
        }

    }
}