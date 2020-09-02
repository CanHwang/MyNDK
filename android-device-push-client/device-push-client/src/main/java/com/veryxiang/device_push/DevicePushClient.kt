package com.veryxiang.device_push

import android.util.Log
import com.veryxiang.common.enums.BusinessExceptionEnum
import com.veryxiang.common.enums.SystemExceptionEnum
import com.veryxiang.common.exception.BusinessException
import com.veryxiang.common.exception.ExceptionCode
import com.veryxiang.common.exception.SystemException
import com.veryxiang.common.utils.HexUtils.bytesToHex
import com.veryxiang.common.utils.RandomUtils
import com.veryxiang.device_push.name.FEATURES
import com.veryxiang.device_push.name.HANDSHAKE0
import com.veryxiang.device_push.name.HANDSHAKE1
import com.veryxiang.device_push.name.PING
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.net.URLEncoder
import java.security.Key
import java.util.concurrent.TimeUnit
import javax.crypto.Mac


/**
 * 设备推送客户端
 */
class DevicePushClient: WSPushProtocol  {

    private var okHttpClient: OkHttpClient?=null;
    private var CONNECT_TIME_OUT: Long = 30;
    private var PING_INTERVAL: Long = 55;
    private var websocket: WebSocket? = null;
    private val url: String;
    val deviceId: String;
    private val formalKeyName: String;
    private val defaultKeyName: String;
    private lateinit var keyType: String;
    private lateinit var C0: String;
    private lateinit var C1: String;
    private val deviceKeyManager: DeviceKeyManager;
    private var sendPing: Runnable?;

    /**
     * 发送ping时设置为false, 在ping回调时设置为true, 如果在下一轮ping时, pingDone还是false则认为网络问题关闭连接
     */
    private var pingDone: Boolean = true;
    val TAG = "DevicePushClient";

    /**
     * 连接完成回调
     */
    var onConnected: ((client: DevicePushClient)->Unit)? = null;

    /**
     * 连接关闭回调, exp 如果不为空则为异常关闭
     */
    var onClosed: ((client: DevicePushClient, exp: Throwable?)->Unit)? = null;

    /**
     * 会话状态
     */
    enum class SessionStatus {
        /**
         * 未连接
         */
        statusNotConnected,
        /**
         * 握手中
         */
        statusHandshake,

        /**
         * 未配置正式密钥的设备连接
         */
        statusNoKey,

        /**
         * 配置了正式密钥的连接
         */
        statusHasKey,

        /**
         * 连接已经断开
         */
        statusClosed
    }

    /**
     * 记录当前连接状态
     */
    private var status: SessionStatus = SessionStatus.statusNotConnected;

    private val handlers: DevicePushHandlers;


    /**
     * 创建设备推送客户端
     * @param url 连接的URL
     * @param deviceId 设备ID
     * @param formalKeyName 设备正式key的名字
     * @param defaultKeyName 设备缺省key的名字
     */
    constructor(handlers: DevicePushHandlers, url: String, deviceId: String, formalKeyName: String, defaultKeyName: String)
    {
        this.handlers = handlers;
        this.url = url;
        this.deviceId = deviceId;
        this.formalKeyName = formalKeyName;
        this.defaultKeyName = defaultKeyName;
        deviceKeyManager = DeviceKeyManager.getInstance(null);
        sendPing = Runnable {
            if (status == SessionStatus.statusHasKey || status == SessionStatus.statusNoKey) {
                if(!pingDone) {
                    closeException = SystemException(SystemExceptionEnum.NETWORK, "PING超时");
                    close();
                } else {
                    ping();
                    delayPing();
                }
            }
        }
    }


    /**
     * 设置连接超时和ping间隔，单位ms
     */
    fun setTimeout(connectTimeout: Long?, pingInterval: Long?)
    {
        if (connectTimeout != null) {
            CONNECT_TIME_OUT = connectTimeout;
        }
        if (pingInterval != null) {
            PING_INTERVAL = pingInterval;
        }
    }

    /**
     * 判断是否存在设备正式key
     */
    private fun hasFormalKey(): Boolean
    {
        return deviceKeyManager.hasDeviceKey(formalKeyName);
    }

    /**
     * 判断是否存在设备缺省key
     */
    private fun hasDefaultKey(): Boolean
    {
        return deviceKeyManager.hasDeviceKey(defaultKeyName);
    }

    private fun ping()
    {
        pingDone = false;
        call(PING.name, null, {ec,res ->
           if (ec != null) {
               close();
           }
            pingDone = true;
        }, Void::class.java);
    }

    private fun delayPing()
    {
        executor.postDelayed(sendPing, PING_INTERVAL*1000);
    }

    /**
     * 获取连接状态
     */
    fun getStatus():SessionStatus
    {
        return status;
    }

    /**
     * 开启连接
     */
    fun connect()
    {
        if (websocket != null || status != SessionStatus.statusNotConnected || okHttpClient != null) {
            throw SystemException(SystemExceptionEnum.BUG, "设备连接对象不能重复连接使用");
        }
        okHttpClient = OkHttpClient.Builder().readTimeout(CONNECT_TIME_OUT+PING_INTERVAL, TimeUnit.SECONDS)
            .writeTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS).build();
        //{deviceId}/{keyType}/{C0}
        C0 = RandomUtils.generateRandomBytesAsHex(32);
        if (hasFormalKey()) {
            keyType = "haskey";
        } else if (hasDefaultKey()) {
            keyType = "nokey";
        } else {
            throw RuntimeException("正式key或者缺省key未设置");
        }
        val req = Request.Builder().url( "${url}/${URLEncoder.encode(deviceId)}/${keyType}/${C0}").build();
        websocket = okHttpClient?.newWebSocket(req, object: WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                if(websocket != webSocket) {
                   closeInstance(webSocket)
                    return
                };
                status = SessionStatus.statusHandshake;
            }

            /** Invoked when a text (type `0x1`) message has been received. */
            override fun onMessage(webSocket: WebSocket, text: String) {
                if(websocket != webSocket) return;
                handleMessage(text);
            }

            /** Invoked when a binary (type `0x2`) message has been received. */
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                if(websocket != webSocket) return;
            }

            /**
             * Invoked when the remote peer has indicated that no more incoming messages will be transmitted.
             */
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                if(websocket != webSocket) {
                    closeInstance(webSocket)
                    return
                };
                executor.post({
                    closeException = null;
                    close();
                })
            }

            /**
             * Invoked when both peers have indicated that no more messages will be transmitted and the
             * connection has been successfully released. No further calls to this listener will be made.
             */
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if(websocket != webSocket) {
                   closeInstance(webSocket)
                    return;
                }
                executor.post({
                    closeException = null;
                    close();
                })
            }

            /**
             * Invoked when a web socket has been closed due to an error reading from or writing to the
             * network. Both outgoing and incoming messages may have been lost. No further calls to this
             * listener will be made.
             */
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if(websocket != webSocket) {
                   closeInstance(webSocket)
                   return
                }
                t.printStackTrace()
                executor.post({
                    closeException = t;
                    close();
                })
            }

        });
    }

    private fun closeInstance(webSocket: WebSocket)
    {
        try {
            webSocket.cancel();
        } catch(e: Exception) {
            ;
        }
        try {
            webSocket.close(0, "close");
        } catch(e: Exception) {
            ;
        }
    }

    override fun sendText(msg: String) {
        websocket?.send(msg)?:throw SystemException(SystemExceptionEnum.NETWORK, "连接未建立!");
    }

    override fun handleCall(name: String, params: JSONObject?, reply: WSPushCallResult?) {
        if (status == SessionStatus.statusHandshake) { //握手中
            if (name != HANDSHAKE0.name) { //握手未完成就调用其它函数
                closeException = SystemException(SystemExceptionEnum.BUG, "服务端未认证");
                close();
                return;
            }
            val req: HANDSHAKE0.Request? = parseJSON(params, HANDSHAKE0.Request::class.java);
            if (req == null) {
                closeException = SystemException(SystemExceptionEnum.BUG, "不和规格的HANDSHAKE0");
                close();
                return;
            }
            C1 = req.C1?:run{
                closeException = SystemException(SystemExceptionEnum.BUG, "不和规格的HANDSHAKE0");
                close();
                return;
            }
            //发送客户端签名
            val key: Key;
            try {
                if (keyType == "nokey") {
                    key = deviceKeyManager.getDeviceKey(defaultKeyName);
                } else {
                    key = deviceKeyManager.getDeviceKey(formalKeyName);
                }
            } catch(e: Exception) {
                e.printStackTrace();
                closeException = e;
                close();
                return;
            }
            val reqHanshake1 = HANDSHAKE1.Request();
            reqHanshake1.hash = HMAC_SHA256(C0+C1+deviceId, key);
            call(HANDSHAKE1.name, reqHanshake1, {e: ExceptionCode?, res: Any? ->
                hanshake1(e, res);
            }, HANDSHAKE1.Response::class.java);

        }
        if (status != SessionStatus.statusHasKey && status != SessionStatus.statusNoKey) { //连接已经关闭
            return;
        }
        if (name == FEATURES.name) { //请求features
            if (reply != null) {
                val rep = FEATURES.Response();
                handlers.getFeatures(rep.names);
                reply.result(rep);
            }
            return;
        }
        handlers.invoke(this, name, params, reply);
    }

    /**
     * 处理握手1
     */
    private fun hanshake1(e: ExceptionCode?, res: Any?)
    {
        if(e != null){
            if (e.isBussinessCode) {
                closeException = BusinessException(e);
            } else {
                closeException = SystemException(e);
            }
            close();
            return
        }
        val rep: HANDSHAKE1.Response = res as? HANDSHAKE1.Response ?:run {close(); return}
        val key: Key;
        try {
            if (keyType == "nokey") {
                key = deviceKeyManager.getDeviceKey(defaultKeyName);
            } else {
                key = deviceKeyManager.getDeviceKey(formalKeyName);
            }
        } catch(e: Exception) {
            e.printStackTrace();
            closeException = e;
            close();
            return;
        }
        //验证服务端签名
        if (HMAC_SHA256(C1+C0+deviceId, key) != rep.hash) { //服务端签名错误
            Log.e(DevicePushClient::class.simpleName, "Bad server signature");
            closeException = BusinessException(BusinessExceptionEnum.UNAUTHORIZED, "服务端签名不正确");
            close();
            return;
        }
        if (keyType == "nokey") {
            status = SessionStatus.statusNoKey;
        } else {
            status = SessionStatus.statusHasKey;
        }
        delayPing();
        try {
            onConnected?.invoke(this);
        } catch(e: Throwable) {
            closeException = e;
            close();
        }
    }

    companion object {
        /**
         * hamc_sha256算法
         */
        private fun HMAC_SHA256(data: String, key: Key): String? {
            return try {
                val mac: Mac = Mac.getInstance("HmacSHA256")
                mac.init(key)
                bytesToHex(mac.doFinal(data.toByteArray(charset("UTF-8"))))
            } catch (e: Throwable) {
                e.printStackTrace();
                throw SystemException(SystemExceptionEnum.BUG, e)
            }
        }
    }

    @Synchronized
    override fun close()
    {
        websocket?:return;
        status = SessionStatus.statusClosed;
        val ws: WebSocket? = websocket;
        websocket = null;
        if (ws != null) {
            closeInstance(ws);
        }
        Log.i(TAG, "Close websocket connection");
        super.close();
        executor.removeCallbacks(sendPing);
        val e = closeException;
        closeException = null;
        sendPing = null;
        okHttpClient?.connectionPool?.evictAll();
        if (onClosed != null) {
            val onClosedTmp = onClosed;
            onClosed = null;
            executor.post({
                onClosedTmp?.invoke(this, e);
            });
        }
    }


}