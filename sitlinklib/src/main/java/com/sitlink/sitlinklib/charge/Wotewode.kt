package com.sitlink.sitlinklib.charge

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class Wotewode(switch_right: String, switch_left: String, rightValue: Int, leftValue: Int,call:ChargeInterface.Model) : HardWare(), SensorEventListener {

    init {
        System.loadLibrary("native-lib")
    }
    val TAG="Wotewode"
    lateinit var sensorAccelerometer: Sensor
    lateinit var proximitySensor: Sensor
    lateinit var sm: SensorManager
    var senerTpye:Int = 1
    var switch_right=switch_right
    var switch_left=switch_left
    var rightValue:Int = rightValue
    var leftValue:Int = leftValue
    var call:ChargeInterface.Model=call
    //开启普通充电
    private val openNormalCharge1 = "/sys/devices/platform/charge_module/charge_en1"
    private val openNormalCharge2 = "/sys/devices/platform/charge_module/charge_en2"
    private val openNormalCharge3 = "/sys/devices/platform/charge_module/charge_en3"
    //获取左侧硬件传感信息
    private val senerInfo = "/sys/devices/platform/soc/soc:ap-apb/70600000.i2c/i2c-1/1-0047/input/input2/driver/ps_type"
    override fun initSensorPort(ctx:Context) {
        senerTpye=readyGpio(senerInfo)//0是三洋 读到1是士兰微     陀螺仪默认是士兰微
        Log.i(TAG,"MainActivity  senerType=="+senerTpye)
        //从系统服务中获得传感器管理器
        sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (null==sensorAccelerometer) {
            Log.i(TAG,"陀螺仪传感器不可用")
        }
        proximitySensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (null==proximitySensor) {
            Log.i(TAG,"接近传感器不可用")
        }
        if("1".equals(switch_right)) {//开启右侧陀螺仪传感
            sm.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        if("1".equals(switch_left)) {//开启左侧距离传感
            sm.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }
    private var timestamp: Float = 0.toFloat()
    ////坐标轴都是手机从左侧到右侧的水平方向为x轴正向，从手机下部到上部为y轴正向，垂直于手机屏幕向上为z轴正向
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            ////从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值；否则，为负值
            if (timestamp != 0f) {
                if(-1==senerTpye){//ST
                    if (event.values[1].toInt() <= rightValue) {
                        Log.d("serialportrecive", "有人情况 ST陀螺仪  event.values[1]=" + event.values[1])
                        isHaveMan1 = true
                    }else {
                        Log.d("serialportrecive", "没人情况 ST陀螺仪  event.values[1]=" + event.values[1])
                        isHaveMan1 = false
                    }
                }else {
                    if (event.values[1].toInt() > rightValue) {//< 1) {
                        Log.d("serialportrecive", "有人情况 士兰微陀螺仪  event.values[1]=" + event.values[1])
                        isHaveMan1 = true
                    } else {
                        Log.d("serialportrecive", "没人情况 士兰微陀螺仪  event.values[1]=" + event.values[1])
                        isHaveMan1 = false
                    }
                }
            }
            //将当前时间赋值给timestamp
            timestamp = event.timestamp.toFloat()
        } else if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            if(-1==senerTpye){//ST
                if (event.values[0].toInt() <= leftValue) {
                    Log.d("serialportrecive", "有人情况 ST接近传感  event.values[0]=" + event.values[0])
                    isHaveMan2 = true
                }else {
                    Log.d("serialportrecive", "没人情况 ST接近传感  event.values[0]=" + event.values[0])
                    isHaveMan2 = false
                }
            }else if (0 == senerTpye) {//三洋
                if (event.values[0].toInt() < 1) {//> Constants.leftValue){//< 1) {
                    Log.d("serialportrecive", "有人情况 三洋接近传感  event.values[0]=" + event.values[0])
                    isHaveMan2 = true
                } else {
                    Log.d("serialportrecive", "没人情况 三洋接近传感  event.values[0]=" + event.values[0])
                    isHaveMan2 = false
                }
            } else if (1 == senerTpye || -1 == senerTpye) {//士兰微
                if (event.values[0].toInt() > leftValue) {//< 1) {
                    Log.d("serialportrecive", "有人情况 士兰微接近传感  event.values[0]=" + event.values[0])
                   isHaveMan2 = true
                } else {
                    Log.d("serialportrecive", "没人情况 士兰微接近传感  event.values[0]=" + event.values[0])
                    isHaveMan2 = false
                }
            }
        }
        call.isHaveMan(isHaveMan1,isHaveMan2)
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
        sm.unregisterListener(this)
    }
    external fun writeGpio(path: String, value: String): Int
    external fun readyGpio(path: String): Int
}