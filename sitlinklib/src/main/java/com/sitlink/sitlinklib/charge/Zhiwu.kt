package com.sitlink.sitlinklib.charge

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import com.ziver.com.vl53l0

class Zhiwu (switch_right: String, switch_left: String, rightValue: Int, leftValue: Int,call:ChargeInterface.Model) : HardWare()  {
    init {
        System.loadLibrary("native-lib")
        System.loadLibrary("laservl53l0")
    }
    val TAG="Zhiwu"
    //开启普通充电1
    private val openNormalCharge1 = "/sys/kernel/custom_gpio/gpio51_enable"
    //开启普通充电2
    private val openNormalCharge2 = "/sys/kernel/custom_gpio/gpio52_enable"
    var switch_right=switch_right
    var switch_left=switch_left
    var rightValue:Int = rightValue
    var leftValue:Int = leftValue
    var call:ChargeInterface.Model=call
    var diantance = 0
    var diantance2 = 0
    private var delayHandler:Handler? = Handler()

    override fun initSensorPort(ctx: Context) {
        vl53l0.Laser_open()
        delayHandler?.postDelayed({ vl53l0.Laserx_open() }, 200)
        delayHandler?.postDelayed(runnable, 1000)
    }
    private var runnable: Runnable = object : Runnable {
        override fun run() {
            diantance = 0
            diantance2 = 0
            if("1".equals(switch_right)){
                diantance = vl53l0.Laser_measure()
            }
            if("1".equals(switch_left)){
                diantance2 = vl53l0.Laserx_measure()
            }
            if((diantance >= 0 && diantance<350) || (diantance2 >= 0 && diantance2<350)){//有人情况((diantance > 5 && diantance<260) || (diantance2 > 5 && diantance2<260))
                Log.d("serialportrecive", "有人情况  diantance  "+diantance+"    diantance2 "+diantance2)
                isHaveMan1=true
                isHaveMan2=true
            }else{//没人情况
                Log.d("serialportrecive", "没人情况  diantance  "+diantance+"    diantance2 "+diantance2)
                isHaveMan1=false
                isHaveMan2=false
            }
            call.isHaveMan(isHaveMan1,isHaveMan2)
            delayHandler?.postDelayed(this, 2000)
        }
    }
    override fun OpenCharge(use: String?) {
        Log.i(TAG,"setOpenCharge  "+ use)
        if(!"single".equals(use)){//三口 三码独立控制
            if("01".equals(use)) writeGpio(openNormalCharge2, "1")
            else if("02".equals(use)) writeGpio(openNormalCharge1, "1")
        }else{//单口 单码控制  或断网情况下
            writeGpio(openNormalCharge1, "1")
            writeGpio(openNormalCharge2, "1")
        }
    }

    override fun CloseCharge(usb: String) {
        Log.i(TAG,"setCloseCharge  "+usb)
        if(!"single".equals(usb)){//三口 三码独立控制
            if("01".equals(usb)) writeGpio(openNormalCharge2, "0")
            else if("02".equals(usb)) writeGpio(openNormalCharge1, "0")
        }else{//单口 单码控制
            writeGpio(openNormalCharge1, "0")
            writeGpio(openNormalCharge2, "0")
        }
    }

    override fun destroy() {
        if (null != delayHandler) {
            delayHandler?.removeCallbacksAndMessages(null)
            delayHandler = null
        }
    }
    external fun writeGpio(path: String, value: String): Int
}