package com.veryxiang.common.enums

import com.veryxiang.common.exception.ExceptionCode

/**
 * @Description 基础业务异常信息错误码列表, 这里只定义常用的，基础的错误码，更具体的错误码应当定义到具体项目中.
 * 基础错误码定义范围为[1-999], 其它模块具体的业务错误码应当定义在1000以上.
 * 其中[1-499]为业务错误
 * [500-999]为系统错误
 * [1000-) 为具体的业务错误
 * @Auther: YangChengXiao
 * @Email yangchengxiao@sitlink.com.cn
 * @Date: 2019/11/12 9:19
 */
object BusinessExceptionEnum {
    val INVALID_PARAMS: ExceptionCode = ExceptionCode.define(400, "参数不合法!")
    val UNAUTHORIZED: ExceptionCode = ExceptionCode.define(401, "未认证!")
    val FORBIDDEN: ExceptionCode = ExceptionCode.define(403, "没有权限!")
    val NOT_FOND: ExceptionCode = ExceptionCode.define(404, "请求资源未找到!")
    val EXISTS: ExceptionCode = ExceptionCode.define(405, "请求创建的资源已经存在!")
    val INSUFFIENT: ExceptionCode = ExceptionCode.define(406, "资源不足!")
    val RANGE: ExceptionCode = ExceptionCode.define(407, "请求的范围不允许!")
}