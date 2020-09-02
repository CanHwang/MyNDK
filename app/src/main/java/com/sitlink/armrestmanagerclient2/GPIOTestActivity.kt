package com.sitlink.armrestmanagerclient2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_gpiotest.*

class GPIOTestActivity : AppCompatActivity() {
    var TAG=this.javaClass.simpleName
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    val gpio_en2 = "/sys/devices/platform/gpio_module/gpio_en2"
    val gpio_en3 = "/sys/devices/platform/gpio_module/gpio_en3"
    val gpio_en4 = "/sys/devices/platform/gpio_module/gpio_en4"
    var handler:Handler=Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpiotest)
    }

    var gpio_en1_reault=-1;
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(event?.keyCode==134){
            gpio_en1_reault=1
        }else{
            gpio_en1_reault=-1
        }
//        if(event?.keyCode==KEY_FN_F4)
        Log.e(TAG,"onKeyDown  keyCode ${keyCode}  event  ${event}")
        return super.onKeyDown(keyCode, event)
    }

    external fun ReadGPIOValue(vpath: String): Int
    external fun WriteGPIOValue(vpath: String, value: String): Int

    fun OpenPower(view: View) {
        val result = WriteGPIOValue(gpio_en2, "1")
        Log.e(TAG,"gpio_en2 写入1 结果为${result}")
        if(result==0)
            Toast.makeText(this,"电源开启成功",Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this,"电源开启失败",Toast.LENGTH_SHORT).show()
    }

    fun ClosePower(view: View) {
        val result = WriteGPIOValue(gpio_en2, "0")
        Log.e(TAG,"gpio_en2 写入0 结果为${result}")
        if(result==0)
            Toast.makeText(this,"电源关闭成功",Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this,"电源关闭失败",Toast.LENGTH_SHORT).show()
    }

    fun ResetMoter(view: View) {
        WriteGPIOValue(gpio_en3, "0")
        WriteGPIOValue(gpio_en4, "0")
        WriteGPIOValue(gpio_en2, "1")
        handler.postDelayed({
            val result = WriteGPIOValue(gpio_en2, "0")
            Log.e(TAG,"gpio_en2 写入0 结果为${result}")
            if(result==0)
                Toast.makeText(this,"舵机复位成功",Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this,"舵机复位失败",Toast.LENGTH_SHORT).show()
        },3000)
    }

    //第一步：GPIO3写1；（开启电源）
    //第二步：GPIO1写0；（左转）
    //第三步：当GPIO4接收到1信号后，GPIO1写1；(复位)
    //第四步：GPIO3写0.（关闭电源）
    fun TurnLeft(view: View) {
        WriteGPIOValue(gpio_en3, "1")
        WriteGPIOValue(gpio_en4, "1")
        WriteGPIOValue(gpio_en2, "1")
        WriteGPIOValue(gpio_en3, "0")
        handler.postDelayed({
            Log.e(TAG, "gpio_en1 读取结果: " + gpio_en1_reault)
            if(gpio_en1_reault==1) {
                WriteGPIOValue(gpio_en3, "1")
                handler.postDelayed({
                    WriteGPIOValue(gpio_en2, "0")
                    WriteGPIOValue(gpio_en3, "0")
                    WriteGPIOValue(gpio_en4, "0")
                    Toast.makeText(this,"舵机左转成功",Toast.LENGTH_SHORT).show()
                },500)
            }
            else
                Toast.makeText(this,"舵机左转失败",Toast.LENGTH_SHORT).show()
         },2000)
    }

    //第一步：GPIO3写1；（开启电源）
    //第二步：GPIO2写0；（左转）
    //第三步：当GPIO4接收到1信号后，GPIO2写1；(复位)
    //第四步：GPIO3写0.（关闭电源）
    fun TurnRight(view: View) {
        WriteGPIOValue(gpio_en3, "1")
        WriteGPIOValue(gpio_en4, "1")
        WriteGPIOValue(gpio_en2, "1")
        WriteGPIOValue(gpio_en4, "0")
        handler.postDelayed({
            Log.e(TAG, "gpio_en1 读取结果: " + gpio_en1_reault)
            if(gpio_en1_reault==1) {
                WriteGPIOValue(gpio_en4, "1")
                handler.postDelayed({
                    WriteGPIOValue(gpio_en2, "0")
                    WriteGPIOValue(gpio_en3, "0")
                    WriteGPIOValue(gpio_en4, "0")
                    Toast.makeText(this,"舵机右转成功",Toast.LENGTH_SHORT).show()
                },500)
            }
            else
                Toast.makeText(this,"舵机右转失败",Toast.LENGTH_SHORT).show()
        },2000)
    }

}
