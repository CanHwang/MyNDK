package com.sitlink.sitlinklib.charge

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class Gaotong(switch_right: String, switch_left: String, rightValue: Int, leftValue: Int, call:ChargeInterface.Model) : HardWare() {

    init {
        System.loadLibrary("native-lib")
    }
    val TAG="Gaotong"
    lateinit var sm: SensorManager
    var switch_right=switch_right
    var switch_left=switch_left
    var rightValue:Int = rightValue
    var leftValue:Int = leftValue
    var call:ChargeInterface.Model=call
    //开启普通充电
    private val openNormalCharge1 = "/sys/class/gpio/gpio928/value"
    private val openNormalCharge2 = "/sys/class/gpio/gpio969/value"
    private val openNormalCharge3 = "/sys/class/gpio/gpio943/value"
    override fun initSensorPort(ctx:Context) {
        //从系统服务中获得传感器管理器
        sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var sensors = sm.getSensorList(Sensor.TYPE_ALL)
        getProximitySensor(sm, sensors);
        println("leftValue=="+leftValue+"       rightValue=="+rightValue)
    }

    private fun getProximitySensor(sensorManager: SensorManager, sensors: List<Sensor>) {
        // 打印每个传感器信息
        var iIndex = 1
        for (item in sensors) {
            if (item.type == Sensor.TYPE_PROXIMITY) {
                if (iIndex == 1 && "1".equals(switch_left)) {//开启左侧距离传感
                    Log.d("leung", "getProximitySensor Left")
                    sensorManager.registerListener(mSensorEventListenerLeft, item, SensorManager.SENSOR_DELAY_NORMAL)
                }
                if (iIndex == 2 && "1".equals(switch_right)) {//开启右侧陀螺仪传感
                    Log.d("leung", "getProximitySensor Right")
                    sensorManager.registerListener(mSensorEventListenerRight, item, SensorManager.SENSOR_DELAY_NORMAL)
                }
                iIndex++
            }
        }
    }

    private val mSensorEventListenerLeft = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            val proximity = sensorEvent.values[0]
            if (proximity.toInt() <= leftValue) {
//                Log.d("Leftportrecive", "有人情况 ST接近传感  proximity=" + proximity)
                isHaveMan2 = true
            }else {
//                Log.d("Leftportrecive", "没人情况 ST接近传感  proximity=" + proximity)
                isHaveMan2 = false
            }
            call.isHaveMan(isHaveMan1,isHaveMan2)
        }
        override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    }

    private val mSensorEventListenerRight = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            val proximity = sensorEvent.values[0]
            if (proximity.toInt() <= rightValue) {
//                Log.d("Rightportrecive", "有人情况 ST陀螺仪  proximity=" + proximity)
                isHaveMan1 = true
            }else {
//                Log.d("Rightportrecive", "没人情况 ST陀螺仪  proximity=" + proximity)
                isHaveMan1 = false
            }
            call.isHaveMan(isHaveMan1,isHaveMan2)
        }
        override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    }

    //打开充电
    override fun OpenCharge(use:String?) {
        Log.i(TAG,"setOpenCharge  "+ use)
        if(!"single".equals(use)){//三口 三码独立控制
            if("01".equals(use)) writeGpio(openNormalCharge1, "1")
            else if("02".equals(use)) writeGpio(openNormalCharge2, "1")
            else if("03".equals(use)) writeGpio(openNormalCharge3, "1")
        }else{//单口 单码控制  或断网情况下
            writeGpio(openNormalCharge1, "1")
            writeGpio(openNormalCharge2, "1")
            writeGpio(openNormalCharge3, "1")
        }
    }
    //关闭充电
    override fun CloseCharge(usb:String) {
        Log.i(TAG,"setCloseCharge  "+usb)
        if(!"single".equals(usb)){//三口 三码独立控制
            if("01".equals(usb)) writeGpio(openNormalCharge1, "0")
            else if("02".equals(usb)) writeGpio(openNormalCharge2, "0")
            else if("03".equals(usb)) writeGpio(openNormalCharge3, "0")
        }else{//单口 单码控制
            writeGpio(openNormalCharge1, "0")
            writeGpio(openNormalCharge2, "0")
            writeGpio(openNormalCharge3, "0")
        }
    }
    override fun destroy(){
//        sm.unregisterListener(this)
    }
    external fun writeGpio(path: String, value: String): Int
}