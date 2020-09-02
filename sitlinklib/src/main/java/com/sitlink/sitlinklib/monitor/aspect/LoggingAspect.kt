package com.sitlink.sitlinklib.monitor.aspect

import android.util.Log
import com.sitlink.sitlinklib.monitor.annotation.Logging
import com.sitlink.sitlinklib.monitor.bean.Logger
import com.sitlink.sitlinklib.monitor.bean.LoggerDao
import com.sitlink.sitlinklib.sqlitecore.BaseDaoFactory
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import java.lang.Exception
import com.sitlink.sitlinklib.sqlitecore.IBaseDao
import java.util.*


@Aspect
class LoggingAspect{
    val TAG = this.javaClass.simpleName
    var baseDao: IBaseDao<Logger>? = null
    init {
        Log.d(TAG, "init 初始化代码块");
        baseDao = BaseDaoFactory.getInstance().getDataHelper(LoggerDao::class.java,Logger::class.java)
    }
    companion object {
        lateinit var imei:String
    }
    /**
    * 声明切入点，使用@Pointcuts注解来标明匹配：
    */
    @Pointcut("execution(@com.sitlink.sitlinklib.monitor.annotation.Logging * *(..))")
    fun methodAnnotationLogging() {}

    /**
     * @Around()
     * @After()
     * @Before()
     */
    @Around("methodAnnotationLogging()")
    fun doLoggingMethod(joinPoint : ProceedingJoinPoint):Object?{
        var signature:MethodSignature=joinPoint.signature as MethodSignature
        var methodName=signature.method.name
        //获取请求的方法参数值
        val args = joinPoint.args
        var params= StringBuffer()
        for (i in 0 until args.size) {
           var objects= args[i] as Object
            if(!args[i].toString().contains("android"))
                 params.append(args[i].toString()+",")
        }
        var level=signature.method.getAnnotation(Logging::class.java).level
        var source=signature.method.getAnnotation(Logging::class.java).source
        var type=signature.method.getAnnotation(Logging::class.java).type
        var funValue=signature.method.getAnnotation(Logging::class.java).functionName
        var description=signature.method.getAnnotation(Logging::class.java).description
        Log.d(TAG, "doLoggingMethod before  功能名:"+funValue+" 方法名:"+methodName+" 参数:"+params.toString());
        baseDao?.insert(Logger(imei,source,level,type,null,funValue+""+description+" 参数:"+params.toString(),Date().getTime()))
        var obj: Object? =null
        try {
            obj= joinPoint.proceed() as Object?
        }catch (e:Exception){
            e.printStackTrace()
        }
        Log.d(TAG, "doLoggingMethod after");
        return obj
    }

}