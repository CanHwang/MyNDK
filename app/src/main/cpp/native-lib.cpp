#include <jni.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <android/log.h>

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,"native_lib",__VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_sitlink_armrestmanagerclient2_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
static int Write_gpio(char *vpath, char *value)
{
    int fd;
    int ret;

    //Write GPIO
    fd = open(vpath, O_RDWR | O_SYNC);//O_RDWR);
    if(fd < 0)
    {
        LOGD("Write fail in open file %s\n",  vpath);
        close(fd);
        return -1;
    }
    LOGD("Write in6 open   fd= %d  vpath= %s  value= %s\n",fd,vpath,value);
    ret = write(fd, value, sizeof(value));
    LOGD("Write in6 open   sizeof(value)== %d\n"+ sizeof(value));
    close(fd);
    LOGD("Write in6 open   ret %d\n",  ret);
    return ret > 0 ? 0 : -1;
}
extern "C"
JNIEXPORT jint
JNICALL Java_com_sitlink_armrestmanagerclient2_MainActivity_WriteGPIOValue(
        JNIEnv *env,
        jclass thiz,
        jstring vpath,
        jstring value)
{
    char *dev_vpath = NULL;
    char *dev_value = NULL;
    int result = -1;

    LOGD("++WriteGPIOValue  %d \n", result);
    if(vpath){
        dev_vpath = (char *) (*env).GetStringUTFChars(vpath, NULL);
        LOGD("open dev_path %s\n", dev_vpath);
    }

    if(value){
        dev_value = (char *) (*env).GetStringUTFChars(value, NULL);
        LOGD("open dev_value %s\n", dev_value);
    }

    if(dev_vpath && dev_value){
        result = Write_gpio(dev_vpath, dev_value);
    }

    if(dev_vpath){
        (*env).ReleaseStringUTFChars(vpath, dev_vpath);
    }

    if(dev_value){
        (*env).ReleaseStringUTFChars(value, dev_value);
    }
    LOGD("--WriteGPIOValue  %d \n", result);

    return result;
}
extern "C"
JNIEXPORT jint
JNICALL Java_com_sitlink_armrestmanagerclient2_GPIOTestActivity_WriteGPIOValue(
        JNIEnv *env,
        jclass thiz,
        jstring vpath,
        jstring value)
{
    char *dev_vpath = NULL;
    char *dev_value = NULL;
    int result = -1;

    LOGD("++WriteGPIOValue  %d \n", result);
    if(vpath){
        dev_vpath = (char *) (*env).GetStringUTFChars(vpath, NULL);
        LOGD("open dev_path %s\n", dev_vpath);
    }

    if(value){
        dev_value = (char *) (*env).GetStringUTFChars(value, NULL);
        LOGD("open dev_value %s\n", dev_value);
    }

    if(dev_vpath && dev_value){
        result = Write_gpio(dev_vpath, dev_value);
    }

    if(dev_vpath){
        (*env).ReleaseStringUTFChars(vpath, dev_vpath);
    }

    if(dev_value){
        (*env).ReleaseStringUTFChars(value, dev_value);
    }
    LOGD("--WriteGPIOValue  %d \n", result);

    return result;
}
static int Read_gpio(char *vpath)
{
    char value_str[3];
    int fd;
    int result = -1;
    //if(!vpath){
    //return -1;
    // }
    fd = open(vpath, O_RDONLY);
    //if(fd < 0){
    //LOGD("fail in open file %s\n",  vpath);
    //close(fd);
    // return -1;
    //}
    result= read(fd, value_str, 3);
    //if (read(fd, value_str, 3) < 0) {
    //LOGD("Failed to read value!\n");
    //return -1;
    // }
    close(fd);
    return result;//(atoi(value_str));
}
extern "C"
JNIEXPORT jint
JNICALL Java_com_sitlink_armrestmanagerclient2_GPIOTestActivity_ReadGPIOValue(
        JNIEnv *env,
        jclass thiz,
        jstring vpath)
{
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
