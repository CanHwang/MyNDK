package com.veryxiang.device_push

import android.os.Handler
import com.veryxiang.common.enums.SystemExceptionEnum
import com.veryxiang.common.exception.ExceptionCode
import com.veryxiang.common.exception.SystemException
import com.veryxiang.common.utils.JsonUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/**
 * 设备推送协议
 */
abstract class WSPushProtocol {

    /**
     * 调用结果回应对象
     */
    class WSPushCallResult {
        val id: Int;
        val owner: WSPushProtocol;
        var done: Boolean = false;

        constructor(id: Int, owner: WSPushProtocol) {
            this.id = id;
            this.owner = owner;
        }

        /**
         * 回复调用响应，如果回复失败返回false
         */
        fun result(v: Any?=null): Boolean
        {
            if (done) {
                throw SystemException(SystemExceptionEnum.BUG, "result/error已经被调用");
            }
            done = true;
            try {
                owner.sendPayload("_result", v, null, id);
                return true;
            } catch(e: Throwable) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 回复调用错误，如果失败返回false
         */
        fun error(exp: ExceptionCode): Boolean
        {
            if (done) {
                throw SystemException(SystemExceptionEnum.BUG, "result/error已经被调用")
            }
            done = true
            try {
                owner.sendPayload("_result", null, exp, id);
                return true;
            } catch(e: Throwable) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 判断该结果是否已经完成
         */
        fun isDone(): Boolean
        {
            return done;
        }
    }

    protected val executor = Handler();

    /**
     * 调用ID
     */
    private val nextId: AtomicInteger = AtomicInteger(0)

    /**
     * 导致连接关闭的异常对象
     */
    protected var closeException: Throwable?=null;

    /**
     * 存储结果回调
     */
    private class OnResultCall internal constructor(cb: (e: ExceptionCode?, res: Any?) -> Unit, resultType: Class<*>?) {
        var cb: (e: ExceptionCode?, res: Any?) -> Unit
        var resultType: Class<*>?

        init {
            this.cb = cb
            this.resultType = resultType
        }
    }

    /**
     * 结果回调映射表
     */
    private val pendingCallbacks: ConcurrentHashMap<Int, OnResultCall> = ConcurrentHashMap()

    /**
     * 分配下一个调用id, 1-0x7FFFFFFF 直接循环
     * @return
     */
    private fun allocNextId(): Int {
        var v: Int = nextId.incrementAndGet()
        if (v == 0) { //绕过0
            v = nextId.incrementAndGet()
        }
        if (v < 0) { // > 0x7FFFFFFF
            v += Int.MIN_VALUE
            v++
        }
        return v
    }


    /**
     * 发送playload
     * @param name
     * @param params
     * @param exp
     * @param id
     * @throws JSONException
     * @throws IOException
     */
    private fun sendPayload(
        name: String,
        params: Any?,
        exp: ExceptionCode?,
        id: Int
    ) {
        val payload = JSONObject()
        try {
            payload.put("name", name)
            if (id != 0) {
                payload.put("id", id)
            }
            if (params != null) {
                if (params is JSONObject) {
                    payload.put("data", params)
                } else if (params is Map<*,*>) {
                    payload.put("data", JSONObject(params))
                } else {
                    payload.put("data", JSONObject(JsonUtils.serialize(params)))
                }
            }
            if (exp != null) {
                val err = JSONObject()
                err.put("code", exp.code)
                err.put("message", exp.msg)
                payload.put("err", err)
            }
        } catch (e: JSONException) {
            throw SystemException(SystemExceptionEnum.SERIALIZE, "JSON序列化失败", e)
        }
        try {
            sendText(payload.toString());
        } catch (e: IOException) {
            throw SystemException(SystemExceptionEnum.NETWORK, "网络连接失效", e)
        } catch (e: Exception) {
            throw SystemException(SystemExceptionEnum.UNKNOWN, "网络连接发送信息失败", e)
        }
    }

    /**
     * 由派生类实现发送数据
     */
    protected abstract fun sendText(msg: String);

    /**
     * 调用并不等返回值, 如果发生错误则直接抛出异常
     * @param name
     * @param params
     */
    fun call(name: String, params: Any?) {
        call(name, params, null, null)
    }

    /**
     * 发除调用并接收回调，如果发生错误设置了回调则在回调中接收错误，否则错误直接抛出
     * @param name
     * @param params
     * @param cb
     * @param resultType 结果类型, 如果resultType是null则回调的res是JSONObject类型
     */
    fun call(
        name: String,
        params: Any?,
        cb: ((e: ExceptionCode?, res: Any?) -> Unit)?,
        resultType: Class<*>?
    ) {
        val id: Int
        if (cb != null) {
            id = allocNextId()
            if (pendingCallbacks.contains(id)) {
                throw SystemException(SystemExceptionEnum.BUG, "调用ID重复")
            }
            val cc = OnResultCall(cb, resultType)
            pendingCallbacks.put(id, cc)
            try {
                sendPayload(name, params, null, id)
            } catch (e: SystemException) { //发生异常，没有发出去, 直接移除回调
                pendingCallbacks.remove(id)
                executor.post{ cb(e.getExceptionCode(), null) }
                return
            }
        } else {
            id = 0
            sendPayload(name, params, null, id)
        }
    }

    /**
     * 解析json对象到java对象
     * @param params
     * @param tClass
     * @return
    </T> */
    open fun <T>  parseJSON(params: JSONObject?, tClass: Class<T>?): T? {
        return if (params == null || tClass == null) {
            return null;
        } else JsonUtils.parseFlex(params.toString(), tClass)
    }

    /**
     * 处理消息, 派生类调用
     * @param message
     */
    protected fun handleMessage(message: String) {
        val payload: JSONObject
        val name: String?
        val id: Int
        var params: JSONObject? = null
        val err: JSONObject?
        var code = 0
        var msg: String? = null
        try {
            payload = JSONObject(message)
            name = payload.getString("name")
            if (name == null || name.isEmpty()) {
                throw JSONException("No name")
            }
            id = payload.optInt("id")
            params = payload.optJSONObject("data")
            err = payload.optJSONObject("err")
            if (err != null) {
                code = err.getInt("code")
                msg = err.optString("message")
            }
        } catch (e: JSONException) { //不符合格式
            executor.post{closeException = e; close();}
            return
        }
        if (name == "_result") { //这是一个结果调用
            if (id == 0) { //错误
                val exp = SystemException(SystemExceptionEnum.BUG, "结果调用没有id");
                executor.post{
                    closeException = exp;
                    close();
                }
                return
            }
            val cc: OnResultCall? = pendingCallbacks.remove(id)
            if (cc == null) {
                val exp = SystemException(SystemExceptionEnum.BUG, "调用id没有对应的回调");
                executor.post{
                    closeException = exp;
                    close();
                }
                return
            }
            if (err != null) { //结果是异常
                executor.post {
                    cc.cb(ExceptionCode.define(code, msg), null)
                };
            } else {
                if (cc.resultType == null || cc.resultType == JSONObject::class.java || params == null) {
                    executor.post {
                        cc.cb(null, params)
                    }
                } else {
                    var res: Any? = null
                    res = try {
                        parseJSON(params, cc.resultType)
                    } catch (e: SystemException) {
                        executor.post{
                            cc.cb(e.getExceptionCode(), res)
                        }
                        return
                    }
                    executor.post {
                        cc.cb(null, res)
                    }
                }
            }
            return
        }
        val req = params
        //这是一个普通调用
        if (id != 0) {
            val reply = WSPushCallResult(id, this)
            executor.post { handleCall(name, req, reply) }
        } else {
            executor.post { handleCall(name, req, null) }
        }
    }

    /**
     * 由派生类实现处理函数调用
     * @param name 函数名
     * @param params 参数
     * @param reply 结果响应对象，可空
     */
    protected abstract fun handleCall(
        name: String,
        params: JSONObject?,
        reply: WSPushCallResult?
    )

    /**
     * 清理挂起的调用
     * @return
     */
    private fun cleanPendingCallbacks() {
        while (!pendingCallbacks.isEmpty()) {
            for (id in pendingCallbacks.keys) {
                val cc: OnResultCall? = pendingCallbacks.remove(id)
                if (cc != null) {
                    asyncHandleCallback(cc.cb)
                }
                break
            }
        }
    }

    private fun asyncHandleCallback(cb: (e: ExceptionCode?, res: Any?) -> Unit) {
        executor.post{ cb(ExceptionCode(SystemExceptionEnum.CANCEL, "连接关闭"), null) }
    }

    /**
     * 执行关闭
     */
    @Synchronized
    open fun close() {
        cleanPendingCallbacks();
    }
}