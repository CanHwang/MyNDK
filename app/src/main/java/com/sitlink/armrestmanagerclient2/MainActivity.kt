package com.sitlink.armrestmanagerclient2

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() , SensorEventListener {

    lateinit var sensorManager: SensorManager
    lateinit var proximitySensor: Sensor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //从系统服务中获得传感器管理器
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor= sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor == null) {
            Log.i(TAG, "接近传感器不可用")
        }
        longjiaozhun.isEnabled=false
        // Example of a call to a native method
//        stringFromJNI()
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun WriteGPIOValue(vpath: String, value: String): Int

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    var TAG="MainActivity"
    private val jiaozhunLeft = "/sys/devices/virtual/input/input2/enable_ps_sensor"// 左
    private val jiaozhunRight = "/sys/devices/virtual/input/input3/enable_ps_sensory"// 右
    fun duanjuli(view: View) {
        Log.d(TAG, "jiaozhunLeft11====")
        val result = WriteGPIOValue(jiaozhunLeft, "3")
        Log.d(TAG, "jiaozhunLeft22====$result")
        if (result == -1) {
            proxsensor_left.text = "左侧短距离校准失败"
        }else{
            proxsensor_left.text = "左侧短距离校准成功"
            longjiaozhun.isEnabled=true
        }
    }

    fun changjuli(view: View) {
        Log.d(TAG, "jiaozhunLeftLong11====")
        val result = WriteGPIOValue(jiaozhunLeft, "2")
        Log.d(TAG, "jiaozhunLeftLong22====$result")
        if (result != -1) {
            //校准完再开启传感  左侧接近传感
            sensorManager.registerListener(this@MainActivity,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        if (null != delayHandler) {
            delayHandler.removeCallbacksAndMessages(null)
        }
    }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] <= 20) {//< proximitySensor.getMaximumRange()) { // Detected something nearby  检测到附近的东西
                Log.i(TAG,"proximitySensor------------> ST检测到附近的东西" + event.values[0] + "  jiaozhunValues==" + proximitySensor.maximumRange)
                val message = Message.obtain()
                message.what = 1
                message.arg1 = event.values[0].toInt()
                if (null != delayHandler) delayHandler.sendMessage(message)
            } else {// Nothing is nearby  附近没什么
                Log.i(TAG,"proximitySensor------------> ST附近没什么" + event.values[0] + "  jiaozhunValues==" + proximitySensor.maximumRange)
                val message = Message.obtain()
                message.what = 2
                message.arg1 = event.values[0].toInt()
                if (null != delayHandler) delayHandler.sendMessage(message)
            }
        }
    }
    internal var delayHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                1->{
                    proxsensor_left.text = "小于20判定为有人 传感值 " + msg.arg1
                }
                2->{
                    proxsensor_left.text = "没人 传感值 " + msg.arg1
                }
            }
        }
    }
}
