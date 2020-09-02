#include <jni.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <android/log.h>
#include <errno.h>

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,"native_lib",__VA_ARGS__)

static int Write_gpio(char *vpath, char *value){
    int fd;
    int ret;
    //Write GPIO
    LOGD("  fail in open file10 %s\n",  vpath);
    fd = open(vpath, O_RDWR);
    if(fd < 0)
    {
        LOGD("  fail in open error %d\n",  errno);
        LOGD("  fail in open file11 %s\n",  vpath);
        LOGD("  fail in open file12 %d\n",  fd);

        close(fd);
        return -1;
    }
    ret = write(fd, value, strlen(value));
    LOGD("fail in open file222 %s\n",  vpath);
    close(fd);
    return ret > 0 ? ret : -1;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_sitlink_sitlinklib_charge_Gaotong_writeGpio(JNIEnv *env, jobject instance, jstring vpath,jstring value) {
    ///取得接受java传过来的参数
    char *dev_vpath = NULL;
    char *dev_value = NULL;
    if(vpath){
        dev_vpath = (char *) (*env).GetStringUTFChars(vpath, NULL);
        LOGD("open dev_path %s\n", dev_vpath);
    }

    if(value){
        dev_value = (char *) (*env).GetStringUTFChars(value, NULL);
        LOGD("open dev_value %s\n", dev_value);
    }

    //完成这些传递过来的参数
    int result = -1;
    if(dev_vpath && dev_value){
        result = Write_gpio(dev_vpath, dev_value);
    }
    //释放变量资源
    if(dev_vpath){
        (*env).ReleaseStringUTFChars(vpath, dev_vpath);
    }

    if(dev_value){
        (*env).ReleaseStringUTFChars(value, dev_value);
    }
    return result;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_sitlink_sitlinklib_charge_Wotewode_writeGpio(JNIEnv *env, jobject instance, jstring vpath,jstring value) {
    ///取得接受java传过来的参数
    char *dev_vpath = NULL;
    char *dev_value = NULL;
    if(vpath){
        dev_vpath = (char *) (*env).GetStringUTFChars(vpath, NULL);
        LOGD("open dev_path %s\n", dev_vpath);
    }

    if(value){
        dev_value = (char *) (*env).GetStringUTFChars(value, NULL);
        LOGD("open dev_value %s\n", dev_value);
    }

    //完成这些传递过来的参数
    int result = -1;
    if(dev_vpath && dev_value){
        result = Write_gpio(dev_vpath, dev_value);
    }
    //释放变量资源
    if(dev_vpath){
        (*env).ReleaseStringUTFChars(vpath, dev_vpath);
    }

    if(dev_value){
        (*env).ReleaseStringUTFChars(value, dev_value);
    }
    return result;
}


extern "C"
JNIEXPORT jint JNICALL Java_com_sitlink_sitlinklib_charge_Zhiwu_writeGpio(JNIEnv *env, jobject instance, jstring vpath,jstring value) {
    ///取得接受java传过来的参数
    char *dev_vpath = NULL;
    char *dev_value = NULL;
    if(vpath){
        dev_vpath = (char *) (*env).GetStringUTFChars(vpath, NULL);
        LOGD("open dev_path %s\n", dev_vpath);
    }

    if(value){
        dev_value = (char *) (*env).GetStringUTFChars(value, NULL);
        LOGD("open dev_value %s\n", dev_value);
    }

    //完成这些传递过来的参数
    int result = -1;
    if(dev_vpath && dev_value){
        result = Write_gpio(dev_vpath, dev_value);
    }
    //释放变量资源
    if(dev_vpath){
        (*env).ReleaseStringUTFChars(vpath, dev_vpath);
    }

    if(dev_value){
        (*env).ReleaseStringUTFChars(value, dev_value);
    }
    return result;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_sitlink_sitlinklib_hardwaretest_TestItemActivity_WriteGPIOValue(JNIEnv *env, jobject instance, jstring vpath,jstring value) {
    ///取得接受java传过来的参数
    char *dev_vpath = NULL;
    char *dev_value = NULL;
    if(vpath){
        dev_vpath = (char *) (*env).GetStringUTFChars(vpath, NULL);
        LOGD("open dev_path %s\n", dev_vpath);
    }

    if(value){
        dev_value = (char *) (*env).GetStringUTFChars(value, NULL);
        LOGD("open dev_value %s\n", dev_value);
    }

    //完成这些传递过来的参数
    int result = -1;
    if(dev_vpath && dev_value){
        result = Write_gpio(dev_vpath, dev_value);
    }
    //释放变量资源
    if(dev_vpath){
        (*env).ReleaseStringUTFChars(vpath, dev_vpath);
    }

    if(dev_value){
        (*env).ReleaseStringUTFChars(value, dev_value);
    }
    return result;
}

static int Read_gpio(char *vpath){
    char value_str[3];
    int fd;
    int result = -1;
    fd = open(vpath, O_RDONLY);
    result= read(fd, value_str, 3);
    close(fd);
    return result;//(atoi(value_str));
}
extern "C"
JNIEXPORT jint JNICALL Java_com_sitlink_sitlinklib_charge_Wotewode_readyGpio(JNIEnv *env, jobject thiz,jstring vpath) {
    char *dev_vpath = NULL;
    int result = -1;
    LOGD("++ReadGPIOValue  %d \n", result);
    if(vpath){
        dev_vpath = (char *) (*env).GetStringUTFChars(vpath, NULL);
        LOGD("open dev_path %s\n", dev_vpath);
    }
    if(dev_vpath){
        result = Read_gpio(dev_vpath);
        (*env).ReleaseStringUTFChars(vpath, dev_vpath);
    }
    LOGD("--ReadGPIOValue  %d \n", result);
    return result;
}




