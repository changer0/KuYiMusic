package com.lulu.admin.kuyimusic.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.lulu.admin.kuyimusic.MyPlayerAPP;

/**
 * Created by Admin on 2016/5/25.
 */
public class AppUtils {


    /**
     *
     * @param view 目标控件
     */
    public static void hideInputMethod(View view){
        //获取输入法的服务
        InputMethodManager imm = (InputMethodManager) MyPlayerAPP.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //判断是否在激活状态
        if(imm.isActive()){
            //隐藏输入法!!,
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


}
