package com.veryxiang.devicepushapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.content.edit
import com.veryxiang.common.exception.ExceptionCode
import com.veryxiang.common.exception.InvalidParamsException
import com.veryxiang.common.utils.CommonUtils
import com.veryxiang.common.utils.RandomUtils
import com.veryxiang.common.utils.SecureUtils
import com.veryxiang.common.utils.SecuritySharedPreferences
import com.veryxiang.device_push.DeviceKeyManager
import com.veryxiang.device_push.DevicePushClient
import com.veryxiang.device_push.DevicePushHandlers
import com.veryxiang.device_push.WSPushNameHandler
import com.veryxiang.device_push.name.CANCEL_IMPORT
import com.veryxiang.device_push.name.CONFIRM_IMPORT
import com.veryxiang.device_push.name.FEATURES
import com.veryxiang.device_push.name.PREPARE_IMPORT
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //初始化DeviceKeyManager
        DeviceKeyManager.getInstance(this);
        safeSettings = SecuritySharedPreferences(this, "safe-settings", Context.MODE_PRIVATE);
        clearKeys();
        setupDefaultKey();
        initHandlers();
        connectServer();
    }
    val TAG="MainActivity";

    val DEVICE_KEY_NAME="DEVICE_KEY";
    val DEFAULT_DEVICE_KEY_NAME="DEFAULT_DEVICE_KEY";
    val DEVICE_ID="867956241437412";
    val WSP_URL="ws://192.168.11.239:9090/v2-wsp/device-push";

    private var client: DevicePushClient?=null;
    private val devicePushHandlers: DevicePushHandlers=DevicePushHandlers();
    private lateinit var safeSettings:SecuritySharedPreferences;
    private val handler = Handler();

    /**
     * 初始化函数处理
     */
    fun initHandlers()
    {
        devicePushHandlers.addHandlers(this);
    }


    //清除所有key
    fun clearKeys()
    {
        val keyManager = DeviceKeyManager.getInstance(this);
        keyManager.deleteDeviceKey(DEVICE_KEY_NAME);
        keyManager.deleteDeviceKey(DEFAULT_DEVICE_KEY_NAME);
    }

    //设置默认key
    fun setupDefaultKey()
    {
        val keyManager = DeviceKeyManager.getInstance(this);
        keyManager.saveDeviceKey(DEFAULT_DEVICE_KEY_NAME, "54jvKejDE49INv3SdbwuovitZy1cOesu4XuAZ3oVCV3HqkShsSv2");
    }

    //连接服务器
    fun connectServer()
    {
        val keyManager = DeviceKeyManager.getInstance(this);
        client = DevicePushClient(devicePushHandlers, WSP_URL, DEVICE_ID, DEVICE_KEY_NAME, DEFAULT_DEVICE_KEY_NAME);
        client?.onConnected = {
            Log.i(TAG, "Connected");
            generatePrepareImportUrl();

        }
        client?.onClosed = {c: DevicePushClient, exp: Throwable? ->
            nonce1=null;
            nonce2=null;
            Log.i(TAG, "Closed")
            Log.e(TAG, exp.toString())
            handler.postDelayed({
                connectServer();

            }, 3000);

        }
        client?.connect();

    }

    //生成准备入库url
    fun generatePrepareImportUrl(): String?
    {
        if (client?.getStatus() != DevicePushClient.SessionStatus.statusNoKey) {
            return null;
        }
        nonce1 = RandomUtils.generateRandomBytesAsHex(16);
        val url = "http://192.168.11.214/v2-static/prepare-import.html?deviceId=${DEVICE_ID}&nonce1=${nonce1}";
        Log.i(TAG, "url=${url}");
        return url;
    }


    var nonce1: String?=null;
    var nonce2: String?=null;

    //准备导入处理函数
    @WSPushNameHandler(PREPARE_IMPORT.name)
    fun prepareImportHandler(req: PREPARE_IMPORT.Request): PREPARE_IMPORT.Response
    {
        if (req.nonce1 == null || req.nonce1 != nonce1) {
            throw InvalidParamsException("nonce1参数不正确");
        }
        if (req.nonce2 == null) {
            throw InvalidParamsException("nonce2参数不正确");
        }
        nonce1 = null;
        nonce2 = req.nonce2;
        val rep = PREPARE_IMPORT.Response();
        rep.deviceId = DEVICE_ID;
        rep.deviceType = CommonUtils.fourCodeToInt("ARM");
        rep.fwVer = 1;
        rep.iccid = "898602b6101990014618"
        rep.mac = "20:72:0D:39:10:36"
        rep.imei = DEVICE_ID;
        rep.managerVer = 1;
        rep.model = "SITLINK-A-01";
        return rep;
    }

    //确认导入处理函数
    @WSPushNameHandler(CONFIRM_IMPORT.name)
    fun confirmImportHandler(req: CONFIRM_IMPORT.Request)
    {
        if (req.nonce2 == null || req.nonce2 != nonce2) {
            throw InvalidParamsException("nonce2参数不正确");
        }
        if (req.deviceId != DEVICE_ID) {
            throw InvalidParamsException("deviceId参数不正确");
        }
        if (req.deviceKey == null || req.password == null) {
            throw InvalidParamsException("设备密钥或者设备维护密码为空");
        }
        nonce1 = null;
        nonce2 = null;
        val keyManager = DeviceKeyManager.getInstance(this);
        keyManager.saveDeviceKey(DEVICE_KEY_NAME, req.deviceKey!!);
        safeSettings.edit().putString("maintain-password", req.password);
        handler.postDelayed({
            client?.close();
            client = null;

        }, 100);
    }

    @WSPushNameHandler(CANCEL_IMPORT.name)
    fun cancelImport(req: CANCEL_IMPORT.Request)
    {
        if (req.deviceId != DEVICE_ID) {
            throw InvalidParamsException("设备ID不正确");
        }
        Log.e(TAG, req.message);
        nonce2 = null;
        nonce1 = null;
    }
}
