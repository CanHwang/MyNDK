package com.veryxiang.device_push

/**
 * 注解在方法上，表示这是一个WSPush函数的处理过程
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WSPushNameHandler(
    /**
     * 函数名
     * @return
     */
    val value: String
)
