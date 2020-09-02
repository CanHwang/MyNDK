package com.veryxiang.common.exception

/**
 * @Description 系统异常
 * @Auther: YangChengXiao
 * @Email yangchengxiao@sitlink.com.cn
 * @Date: 2019/11/7 16:45
 */
open class SystemException : java.lang.RuntimeException {
    private var exceptionCode: ExceptionCode

    /**
     * 只有异常信息枚举的构造
     * @param code
     */
    constructor(code: ExceptionCode) : super(code.toString()) {
        exceptionCode = code
    }

    /**
     * 包含原始异常信息和异常枚举的构造
     * @param code
     */
    constructor(code: ExceptionCode, e: Throwable?) : super(code.toString(), e) {
        exceptionCode = code
    }

    /**
     * 包含原始异常信息和异常枚举的构造
     * @param code
     * @param originMsg
     */
    constructor(code: ExceptionCode, originMsg: String?) : super(
        ExceptionCode.toString(
            code,
            originMsg
        )
    ) {
        exceptionCode = code
    }

    /**
     * 包含原始异常信息、自定义异常信息和异常枚举的构造
     * @param code
     * @param originMsg
     */
    constructor(code: ExceptionCode, originMsg: String?, e: Throwable?) : super(
        ExceptionCode.toString(code, originMsg),
        e
    ) {
        exceptionCode = code
    }

    fun getExceptionCode(): ExceptionCode
    {
        return exceptionCode;
    }

    companion object {
        private const val serialVersionUID = 4506528946166983826L
    }
}