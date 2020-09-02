package com.ziver.com;

public class vl53l0 {

    static public native int Laser_open();
    static public native int Laser_close();
    static public native int Laser_calibration(int mode);
    static public native int Laser_setmode(int mode);
    static public native int Laser_getmode();
    static public native int Laser_setrangeprofle(int mode);
    static public native int Laser_getrangeprofle();
    static public native int Laser_settimedrange(int mode);
    static public native int Laser_gettimedrange();
    static public native int Laser_measure();

    static public native int Laser_save_calibration();

    static public native int Laserx_open();
    static public native int Laserx_close();
    static public native int Laserx_calibration(int mode);
    static public native int Laserx_setmode(int mode);
    static public native int Laserx_getmode();
    static public native int Laserx_setrangeprofle(int mode);
    static public native int Laserx_getrangeprofle();
    static public native int Laserx_settimedrange(int mode);
    static public native int Laserx_gettimedrange();
    static public native int Laserx_measure();

    static public native int Laserx_save_calibration();
    static public native int Screen_Setfull(int mode);
}
