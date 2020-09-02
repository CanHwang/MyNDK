package com.veryxiang.common.enums

import com.veryxiang.common.exception.ExceptionCode

/**
 * @Description 系统异常信息错误码列表
 * [500-999]为系统错误, 这些错误为硬错误，是因为服务器配置或者资源出现问题导致的错误，而不是因为业务逻辑导致 (例如数据库主键冲突不属于系统异常错误，而是业务错误，但是数据库链接不上则属于系统错误)
 */
object SystemExceptionEnum {
    val UNKNOWN: ExceptionCode = ExceptionCode.define(500, "系统未知错误!")
    val NOTIMPLEMENT: ExceptionCode = ExceptionCode.define(501, "接口未实现!")
    val GATEWAY: ExceptionCode = ExceptionCode.define(502, "网关错误!")
    val UNAVAILABLE: ExceptionCode = ExceptionCode.define(503, "服务不可用错误!")
    val FUSING: ExceptionCode = ExceptionCode.define(510, "服务熔断!")
    val BAD_CONFIGURATION: ExceptionCode = ExceptionCode.define(520, "系统配置错误!")
    val DATABASE: ExceptionCode = ExceptionCode.define(550, "数据库系统错误!")
    val FILEIO: ExceptionCode = ExceptionCode.define(551, "文件系统错误!")
    val NETWORK: ExceptionCode = ExceptionCode.define(552, "网络错误!")
    val REDIS: ExceptionCode = ExceptionCode.define(554, "Redis错误!")
    val MQ: ExceptionCode = ExceptionCode.define(555, "消息队列错误!")
    val MONGODB: ExceptionCode = ExceptionCode.define(556, "MONGODB错误!")
    val SERIALIZE: ExceptionCode = ExceptionCode.define(557, "序列化或反序列化错误!")
    val BUG: ExceptionCode = ExceptionCode.define(558, "程序出现BUG！")
    val CANCEL: ExceptionCode = ExceptionCode.define(559, "操作被取消!")
}