package com.veryxiang.common.exception

/**
 * @Description 其他4xx业务异常
 * @Auther: YangChengXiao
 * @Email yangchengxiao@sitlink.com.cn
 * @Date: 2019/11/12 9:16
 */
open class BusinessException : RuntimeException {
    private var exceptionCode: ExceptionCode

    /**
     * 只有异常枚举的构造方法，原始异常信息为""
     * @param code
     */
    constructor(code: ExceptionCode) : super(code.toString()) {
        exceptionCode = code
    }

    constructor(code: ExceptionCode, e: Throwable?) : super(code.toString(), e) {
        exceptionCode = code
    }

    /**
     * 带异常枚举与原始异常信息的构造方法
     * @param code
     * @param originExtMsg
     */
    constructor(code: ExceptionCode, originExtMsg: String?) : super(
        ExceptionCode.toString(
            code,
            originExtMsg
        )
    ) {
        exceptionCode = ExceptionCode(code, originExtMsg)
    }

    /**
     * 带异常类的构造方法
     * @param code
     * @param originExtMsg
     */
    constructor(
        code: ExceptionCode,
        originExtMsg: String?,
        throwable: Throwable
    ) : super(ExceptionCode.toString(code, originExtMsg), throwable) {
        exceptionCode = ExceptionCode(code, originExtMsg)
    }

    fun getExceptionCode(): ExceptionCode
    {
        return exceptionCode;
    }

    companion object {
        private const val serialVersionUID = 6493393354118089881L
    }
}