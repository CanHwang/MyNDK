package com.veryxiang.device_push

import android.R.attr
import android.util.Log
import com.veryxiang.common.enums.SystemExceptionEnum
import com.veryxiang.common.exception.BusinessException
import com.veryxiang.common.exception.ExceptionCode
import com.veryxiang.common.exception.SystemException
import com.veryxiang.common.utils.JsonUtils.parseFlex
import com.veryxiang.device_push.WSPushProtocol.WSPushCallResult
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


/**
 * 设备函数管理器
 */
class DevicePushHandlers {

    companion object {
        val TAG="DevicePushHandlers"
    }
    /**
     * 存放WSPushName处理器
     */
    internal class WSPushNameHandlerBean(
        bean: Any,
        m: Method)
    {
        private var m: Method = m
        private var bean: WeakReference<Any> = WeakReference(bean)
        private var paramTypes: Array<Class<*>?>? = m.parameterTypes
        private var returnType: Class<*>? = m.returnType

        /**
         * 执行调用
         */
        operator fun invoke(
            s: WSPushProtocol?,
            param: Any?,
            reply: WSPushCallResult?
        ) {
            val o: Any? = bean!!.get()
            if (o == null) { //组件对象已经被释放?
                Log.w(TAG,"WSPush函数处理组件被释放: method=${m!!.name}")
                reply?.error(SystemExceptionEnum.CANCEL)
                return
            }
            val params = arrayOfNulls<Any?>(paramTypes!!.size)
            var returnVal: Any? = null
            var handleResult = true
            try {
                //绑定参数
                for (i in paramTypes!!.indices) {
                    val type = paramTypes!![i]
                    if (WSPushProtocol::class.java.isAssignableFrom(type!!)) { //处理函数要求WSPushProtocol参数
                        params[i] = s
                        continue
                    }
                    if (type == WSPushCallResult::class.java) { //处理函数要求WSPushProtocol.WSPushCallResult参数, 意味着本invoke不处理返回值
                        params[i] = reply
                        handleResult = false
                        continue
                    }
                    if (param == null) {
                        params[i] = null
                        continue
                    }
                    if (param.javaClass == type) {
                        params[i] = param
                        continue
                    }
                    if (param is JSONObject && !type.isArray && !type.isEnum &&
                        !type.isPrimitive && !type.isInterface && type != String::class.java
                    ) {
                        params[i] = parseFlex(param.toString(), type)
                    }
                }
                returnVal = m.invoke(o, *params)
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "WSPush函数处理组件非公开?: method=${m.name}", e)
                reply?.error(ExceptionCode(SystemExceptionEnum.BUG, "WSPush函数为非公开函数"))
                return
            } catch (e: InvocationTargetException) {
                Log.w(TAG, "WSPush函数处理组件失败?: method=${m.name}", e.targetException)
                if (reply != null && !reply.isDone()) {
                    val exp = e.targetException
                    if (exp is SystemException) {
                        reply.error((exp as SystemException).getExceptionCode())
                    } else if (exp is BusinessException) {
                        reply.error(exp.getExceptionCode())
                    } else {
                        reply.error(ExceptionCode(SystemExceptionEnum.UNKNOWN, e.message))
                    }
                }
                return
            } catch(e: BusinessException) {
                Log.w(TAG, "WSPush函数处理组件失败?: method=${m.name}", e);
                if (reply != null && !reply.isDone()) {
                    reply.error(e.getExceptionCode());
                }
                return;
            } catch(e: SystemException) {
                Log.w(TAG, "WSPush函数处理组件失败?: method=${m.name}", e);
                if (reply != null && !reply.isDone()) {
                    reply.error(e.getExceptionCode());
                }
                return;
            } catch(e: Throwable) {
                Log.w(TAG, "WSPush函数处理组件失败?: method=${m.name}", e);
                if (reply != null && !reply.isDone()) {
                    reply.error(ExceptionCode(SystemExceptionEnum.UNKNOWN, e.message));
                }
                return;
            }

            if (handleResult && reply != null && !reply.isDone()) {
                if (returnVal != null && returnVal.javaClass != Void::class.java && !returnVal.javaClass.isPrimitive
                    && !returnVal.javaClass.isInterface
                    && !returnVal.javaClass.isArray
                    && returnVal.javaClass != String::class.java && !returnVal.javaClass.isEnum
                ) {
                    reply.result(returnVal)
                } else {
                    reply.result()
                }
            }
        }

    }

    /**
     * WSPush函数处理器
     */
    private val nameHandlers: HashMap<String, WSPushNameHandlerBean> = HashMap();

    /**
     * 扫描handlerObject标注了@WSPushNameHandler的方法
     */
    fun addHandlers(handlerObject: Any)
    {
        for(m: Method in handlerObject.javaClass.methods) {
            val mark = m.getAnnotation(WSPushNameHandler::class.java) ?: continue
            if (nameHandlers.get(mark.value) != null) {
                throw SystemException(SystemExceptionEnum.BUG, "重复同名的函数处理器");
            }
            nameHandlers.put(mark.value, WSPushNameHandlerBean(handlerObject, m));
        }
    }

    /**
     * 获取支持的函数列表
     */
    fun getFeatures(names: ArrayList<String>) {
        for(name: String in nameHandlers.keys) {
            names.add(name);
        }
    }

    /**
     * 调用实现函数
     */
    fun invoke(s: WSPushProtocol, name: String, params: JSONObject?, reply: WSPushCallResult?)
    {
        val bean: WSPushNameHandlerBean? = nameHandlers.get(name)
        if (bean == null) {
            Log.e(TAG, "不存在的WSPush函数调用, name=${name}")
            reply?.error(ExceptionCode.define(SystemExceptionEnum.BUG.code, "不存在的函数!"))
            return
        }
        bean.invoke(s, params, reply)
    }

}