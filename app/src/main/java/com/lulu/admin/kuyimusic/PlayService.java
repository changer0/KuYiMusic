package com.lulu.admin.kuyimusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.lulu.admin.kuyimusic.utils.MediaUtils;
import com.lulu.admin.kuyimusic.vo.Mp3Info;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 所有的控制操作放在该Service
 * 音乐播放的服务组件
 * 此服务实现的功能:
 * 1, 播放
 * 2, 暂停
 * 3, 上一首
 * 4, 下一首
 * 5, 获取当前播放进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mPlayer;
    //用来表示当前的播放的音乐的位置
    private int currentPosition;
    //从数据库中查询音乐文件, 得到播放列表,MediaUtils工具类实现
    private ArrayList<Mp3Info> mp3Infos;

    private MusicUpdateListener musicUpdateListener;
    private boolean isPause = false;


    //用来判断当前的播放列表
    public static final int MY_MUSIC_LIST = 0x1;//我的音乐列表
    public static final int LIKE_MUSIC_LIST = 0x2;//我喜欢列表
    public static final int PLAY_RECORD_MUSIC_LIST = 0x3;//最近播放列表
    private int changePlayList = MY_MUSIC_LIST;


    //播放模式
    public static final int ORDER_PLAY = 1;//顺序播放
    public static final int RANDOM_PLAY = 2;//随机播放
    public static final int SINGLE_PLAY = 3;//单曲循环
    private int play_mode = ORDER_PLAY;


    /**
     * 选择播放列表的getter和setter方法
     *
     * @return
     */
    public int getChangePlayList() {
        return changePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        this.changePlayList = changePlayList;
    }

    /**
     * 不同的界面需要的mp3Infos的信息不同所以需要改变PlayService中的mp3Infos
     *
     * @return
     */
    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    /**
     * 获取播放模式
     *
     * @return
     */
    public int getPlay_mode() {
        return play_mode;
    }

    /**
     * 设置播放模式
     * int ORDER_PLAY = 1;//顺序播放
     * int RANDOM_PLAY = 2;//随机播放
     * int SINGLE_PLAY = 3;//单曲循环
     *
     * @param play_mode
     */
    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    //对外界暴露暂停方法
    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    //服务创建方法
    @Override
    public void onCreate() {
        super.onCreate();
        //从全局应用中得到sp对象, 将在Activity销毁时存入的数据进行恢复
        MyPlayerAPP app = (MyPlayerAPP) getApplication();
        //得到当前播放位置,默认为0
        currentPosition = app.sp.getInt("currentPosition", 0);
        play_mode = app.sp.getInt("play_mode", PlayService.ORDER_PLAY);

        //用于播放音乐
        mPlayer = new MediaPlayer();
        //用工具类中获取mp3信息
        mp3Infos = MediaUtils.getMp3Infos(this);

        //给播放完成和错误注册事件进行注册
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        //启动进程
        es.execute(updateStatusRunnable);
    }

    //线程池, 只允许新建单个线程
    private ExecutorService es = Executors.newSingleThreadExecutor();


    public PlayService() {
    }


    //绑定Activity, 直接绑定本Service类!!!!
    class PlayBind extends Binder {
        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBind();
    }

    //此线程用于更新进度!!!!
    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateListener != null && mPlayer != null && mPlayer.isPlaying()) {
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        //回收线程
        if (es != null && !es.isShutdown()) {
            //如果Service销毁了, 那么该线程就需要回收了
            es.shutdown();
            es = null;
        }
    }

    //播放, 有两种方式
    //1, 从列表中点击播放
    //2, 点击下面按钮播放, 就是从断点处播放
    public void play(int position) {
        //此处目的是为了不让其报错, 在播放时, 如果有溢出产生就默认播放第一首
        Mp3Info mp3Info = null;
        if (position < 0 || position >= mp3Infos.size()) {
            position = 0;
        }
        mp3Info = mp3Infos.get(position);
        try {
            /**
             * Resets the MediaPlayer to its uninitialized state. After calling this method,
             * you will have to initialize it again by setting the data source and calling prepare().
             */
            //播放之前需要reset(), 并且之后必须prepare(),
            mPlayer.reset();
            mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
            mPlayer.prepare();
            mPlayer.start();
            //更新当前播放歌曲的位置
            currentPosition = position;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //在播放时把当前的音乐index传给更新监听器
        if (musicUpdateListener != null) {
            musicUpdateListener.onChange(currentPosition);
        }

    }

    //暂停
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            //暂定方法执行时
            isPause = true;
        }
    }

    //下一首
    public void next() {
        //如果当前歌曲播放到最后一首就让其从从头播放!!!
        if (currentPosition >= mp3Infos.size() - 1) {//!!!!!!!!!!!!
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void prev() {

        if (currentPosition - 1 < 0) {
            currentPosition = mp3Infos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    //真正进行播放开始
    public void start() {
        //在不为空和非播放状态
        if (!mPlayer.isPlaying() && mPlayer != null) {
            mPlayer.start();
            //得到当前的播放位置, 也就是为了确定播放的是哪一首歌
//            currentPosition  = mPlayer.getCurrentPosition();
//            System.out.println("我调用start方法之后的position为: " + currentPosition);
            isPause = false;
        }
    }

    //给Activity访问的方法!用来判断是否正在播放
    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    //得到当前的, 进度
    public int getCurrentProgress() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    //得到当前的播放位置
    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getDuration() {
        return mPlayer.getDuration();

    }

    /**
     * 跳转到的位置
     *
     * @param msc
     */
    public void seekTo(int msc) {
        mPlayer.seekTo(msc);
    }


    /**
     * 监听回调!!!!!!!观察者设计模式
     * 用于更新状态的接口
     */
    public interface MusicUpdateListener {
        //进度值得更新
        void onPublish(int progress);
        //切换歌曲位置的方法
        void onChange(int position);
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
    //播放出错之后
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }
    //用于随机播放用
    private Random random = new Random();

    //播放完成之后触法的事件
    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                //顺序播放直接播放下一首
                next();
                break;
            case RANDOM_PLAY:
                //随机播放
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                //重复播放自己
                play(currentPosition);
                break;
            default:
                break;
        }
    }
}
