package com.sitlink.sitlinklib.sqlitecore;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * Created by Administrator on 2017/1/9 0009.
 */

public class BaseDaoFactory {
    //数据库保存路径
    private String sqliteDatabasePath;
    //数据库操作对象
    private SQLiteDatabase sqLiteDatabase;
    //工厂单例对象
    private static  BaseDaoFactory instance=new BaseDaoFactory();

    public BaseDaoFactory(){
        sqliteDatabasePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/sitlink.db";
        openDatabase();
    }

    public  synchronized  <T extends  BaseDao<M>,M> T getDataHelper(Class<T> clazz,Class<M> entityClass){
        BaseDao baseDao=null;
        try {
            baseDao=clazz.newInstance();
            baseDao.init(entityClass,sqLiteDatabase);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) baseDao;
    }

    private void openDatabase() {
        this.sqLiteDatabase=SQLiteDatabase.openOrCreateDatabase(sqliteDatabasePath,null);
    }

    public  static  BaseDaoFactory getInstance()
    {
        return instance;
    }
}
