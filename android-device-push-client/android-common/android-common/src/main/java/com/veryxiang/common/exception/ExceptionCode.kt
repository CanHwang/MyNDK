package com.veryxiang.common.exception

import com.veryxiang.common.enums.SystemExceptionEnum




/**
 * 异常错误码
 */
class ExceptionCode : java.io.Serializable {
    /**
     * 获取错误码
     * @return 错误码
     */
    var code: Int

    /**
     * 获取错误信息
     * @return 错误信息
     */
    var msg: String?

    constructor(exp: Throwable) {
        if (exp is BusinessException) {
            val ec = exp.getExceptionCode()
            code = ec.code
            msg = ec.msg
        } else if (exp is SystemException) {
            val ec = exp.getExceptionCode()
            code = ec.code
            msg = ec.msg
        } else { //其它异常
            code = SystemExceptionEnum.UNKNOWN.code
            msg = exp.message
        }
    }

    /**
     * 创建异常错误码
     * @param code 错误码
     * @param msg 错误信息
     */
    constructor(code: Int, msg: String?) {
        this.code = code
        this.msg = msg
    }

    /**
     * 复制异常错误码
     * @param exp 要复制的异常错误码
     */
    constructor(exp: ExceptionCode) {
        code = exp.code
        msg = exp.msg
    }

    /***
     * 复制异常错误码，用新的错误信息替换旧的信息
     * @param exp 要复制的异常错误码
     * @param msg 新的错误信息
     */
    constructor(exp: ExceptionCode, msg: String?) {
        code = exp.code
        this.msg = msg
    }

    /**
     * 判断错误码是否是业务异常码, 否则是系统错误码
     * @return
     */
    val isBussinessCode: Boolean
        get() = code >= 1 && code <= 499 || code >= 1000

    override fun toString(): String {
        return if (msg == null || msg?.length == 0) {
            "code: $code"
        } else {
            "code: $code, msg: $msg"
        }
    }

    companion object {
        private const val serialVersionUID = 8292830152014113799L

        /**
         * 创建ExceptionCode
         * @param code 错误码
         * @param msg 错误信息
         * @return
         */
        fun define(code: Int, msg: String?): ExceptionCode {
            return ExceptionCode(code, msg)
        }

        /**
         * 将code转换成字符串
         * @param code
         * @param newMsg 新的错误信息
         * @return 错误字符串
         */
        fun toString(
            code: ExceptionCode,
            newMsg: String?
        ): String {
            var msg = code.msg
            if (newMsg != null && newMsg.length != 0) {
                msg = newMsg
            }
            return if (msg != null && msg.length != 0) {
                "code: " + code.code + ", msg: " + msg
            } else {
                "code: " + code.code
            }
        }
    }
}