package com.veryxiang.device_push

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import androidx.annotation.RequiresApi
import com.veryxiang.common.enums.BusinessExceptionEnum
import com.veryxiang.common.enums.SystemExceptionEnum
import com.veryxiang.common.exception.BusinessException
import com.veryxiang.common.exception.SystemException
import com.veryxiang.common.utils.SecuritySharedPreferences
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * 设备密钥管理器
 */
class DeviceKeyManager {

    private val deviceKeySP: SecuritySharedPreferences;
    private val KEYSTORE_NAME="AndroidKeyStore";

    protected constructor(ctx: Context)
    {
        deviceKeySP = SecuritySharedPreferences(ctx.applicationContext, "DeviceKeyManager", MODE_PRIVATE);
    }

    companion object {
        private var instance: DeviceKeyManager? = null;

        /**
         * 获取设备密钥管理器实例
         */
        fun getInstance(ctx: Context?=null): DeviceKeyManager
        {
            if (instance != null) return instance!!;
            if (ctx == null) {
                throw SystemException(SystemExceptionEnum.BUG, "DeviceKeyManager.getInstance 首次调用必须传入Context对象");
            }
            synchronized(DeviceKeyManager::class.java) {
                if (instance != null) return instance!!;
                instance = DeviceKeyManager(ctx);
            }
            return instance!!;
        }
    }


    /**
     * 保存设备key
     */
    fun saveDeviceKey(keyName: String, key: String) {
        val nativeKeyName = "DEVICE_KEY.$keyName"
        if (Build.VERSION.SDK_INT >= 23) {
            saveDeviceKeyInKS(nativeKeyName, key);
        } else {
            saveDeviceKeyInSP(nativeKeyName, key);
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun saveDeviceKeyInKS(keyName: String, key: String)
    {
        val ks: KeyStore = KeyStore.getInstance(KEYSTORE_NAME);
        ks.load(null);
        val sk = KeyStore.SecretKeyEntry(
            SecretKeySpec(
                key.toByteArray(charset("UTF-8")),
                "HmacSHA256"
            )
        );
        val kprop: KeyProtection = KeyProtection.Builder(KeyProperties.PURPOSE_SIGN).build();
        ks.setEntry(keyName, sk, kprop);
    }

    private fun saveDeviceKeyInSP(keyName: String, key: String)
    {
        deviceKeySP.edit().putString(keyName, key).commit();
    }

    /**
     * 删除设备key
     */
    fun deleteDeviceKey(keyName: String)
    {
        val nativeKeyName = "DEVICE_KEY.$keyName"
        if (Build.VERSION.SDK_INT >= 23) {
            val ks: KeyStore = KeyStore.getInstance(KEYSTORE_NAME);
            ks.load(null);
            ks.deleteEntry(nativeKeyName);
        } else {
            deviceKeySP.edit().remove(nativeKeyName).commit();
        }
    }

    /**
     * 判断是否存在keyName
     */
    fun hasDeviceKey(keyName: String): Boolean
    {
        val nativeKeyName = "DEVICE_KEY.$keyName"
        if (Build.VERSION.SDK_INT >= 23) {
            val ks: KeyStore = KeyStore.getInstance(KEYSTORE_NAME);
            ks.load(null);
            return ks.getKey(nativeKeyName, null) != null;
        } else {
            return deviceKeySP.getString(nativeKeyName, null) != null;
        }
    }

    /**
     * 获取设备密钥
     */
    fun getDeviceKey(keyName: String): SecretKey
    {
        val nativeKeyName = "DEVICE_KEY.$keyName"
        if (Build.VERSION.SDK_INT >= 23) {
            val ks: KeyStore = KeyStore.getInstance(KEYSTORE_NAME);
            ks.load(null);
            return ks.getKey(nativeKeyName, null) as? SecretKey ?: throw BusinessException(BusinessExceptionEnum.NOT_FOND, "设备key不存在: $keyName");
        } else {
            val k=deviceKeySP.getString(nativeKeyName, null);
            if (k != null) {
                return SecretKeySpec(k.toByteArray(), "HmacSHA256");
            } else {
                throw BusinessException(BusinessExceptionEnum.NOT_FOND, "设备key不存在: $keyName");
            }
        }
    }
}