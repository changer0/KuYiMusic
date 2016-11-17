package com.lulu.admin.kuyimusic;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lidroid.xutils.DbUtils;
import com.lulu.admin.kuyimusic.utils.Constant;

/**
 * 我们把全局的变量的放在该类中
 *getApplicationContext():当前应用的上下文
 * 需要配置清单文件:
 * 在<application中换成自己的name
 * android:name=".MyPlayerAPP"
 * Created by Admin on 2016/5/21.
 */
public class MyPlayerAPP extends Application {
    //可以全局去引用, SharePreference用来做临时保存用的
    public static SharedPreferences sp;
    //声明一个全局的dbUtils
    public static DbUtils dbUtils;

    public static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        dbUtils = DbUtils.create(getApplicationContext(), Constant.DB_NAME);
        //用于获取系统应用程序的上下文
        context = this.getApplicationContext();
    }
}
