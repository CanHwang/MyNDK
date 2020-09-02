package com.sitlink.sitlinklib.charge

import android.content.Context

abstract class HardWare {
    var isHaveMan1:Boolean = false
    var isHaveMan2:Boolean = false
    abstract fun initSensorPort(ctx: Context)
    abstract fun OpenCharge(use: String?)
    abstract fun CloseCharge(usb: String)
    abstract fun destroy()
}