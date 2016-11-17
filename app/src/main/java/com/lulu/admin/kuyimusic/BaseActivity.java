package com.lulu.admin.kuyimusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Admin on 2016/5/8.
 * 此类是音乐主界面的基类
 * 用于绑定服务 ,设置更新监听等
 */
public abstract class BaseActivity extends FragmentActivity {

    //用于获取PlayService的实例
    protected PlayService playService;
    private boolean isBound = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取到绑定的PlayService
            PlayService.PlayBind playBind = (PlayService.PlayBind) service;
            playService = playBind.getPlayService();//获得了PlayService对象
            //设置状态更新, 先进行注册
            playService.setMusicUpdateListener(musicUpdateListener);

            musicUpdateListener.onChange(playService.getCurrentPosition());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService = null;
        }
    };


    private PlayService.MusicUpdateListener musicUpdateListener = new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    /**
     * 模板方法设计模式,具体的实现在子类中实现
     */
    public abstract void publish(int progress);

    public abstract void change(int position);

    //绑定服务
    public void bindPlayService() {

        if (!isBound) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            isBound = true;
        }


    }

    //解除绑定服务
    public void unbindPlayServie() {
        if (isBound) {
            unbindService(conn);
            /**这是个大坑大坑大坑大坑大坑大坑大坑大坑
             * 大坑大坑大坑大坑大坑大坑大坑大坑大坑大
             * 坑大坑大坑大坑大坑大坑大坑大坑大坑大坑
             * 大坑大坑大坑大坑大坑大坑大坑大坑大坑大
             * 坑大坑大坑大坑大坑大坑大坑大坑大坑**/
            isBound = false;
        }
    }

    public PlayService.MusicUpdateListener getMusicUpdateListener() {
        return musicUpdateListener;
    }
}
