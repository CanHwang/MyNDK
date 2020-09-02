package com.veryxiang.common.exception
import com.veryxiang.common.enums.BusinessExceptionEnum
/**
 * 参数非法业务异常
 */
class InvalidParamsException : BusinessException {
    constructor() : super(BusinessExceptionEnum.INVALID_PARAMS) {}
    constructor(e: Throwable?) : super(BusinessExceptionEnum.INVALID_PARAMS, e) {}
    constructor(msg: String?) : super(BusinessExceptionEnum.INVALID_PARAMS, msg) {}
    constructor(code: ExceptionCode?, msg: String?) : super(code!!, msg) {}
    constructor(code: ExceptionCode?) : super(code!!) {}
}