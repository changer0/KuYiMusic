package com.lulu.admin.kuyimusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


public class SplashActivity extends Activity {


    private static final int START_ACTIVITY = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        //去除标题
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);


        //先把服务启动起来,
        //note: 如果先启动后绑定, 再解除绑定时服务, 服务不会销毁
        Intent intent = new Intent(this, PlayService.class);
        startService(intent);


        //延迟1s启动主界面
        handler.sendEmptyMessageDelayed(START_ACTIVITY, 1000);


    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case START_ACTIVITY:
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    //使当前Activity出栈, 如果不添加此代码, 返回时还会进入该界面
                    SplashActivity.this.finish();
                    break;
            }

        }
    };


}
