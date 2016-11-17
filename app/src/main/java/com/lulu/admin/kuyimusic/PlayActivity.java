package com.lulu.admin.kuyimusic;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.douzi.android.view.DefaultLrcBuilder;
import com.douzi.android.view.ILrcBuilder;
import com.douzi.android.view.ILrcView;
import com.douzi.android.view.LrcRow;
import com.douzi.android.view.LrcView;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lulu.admin.kuyimusic.utils.Constant;
import com.lulu.admin.kuyimusic.utils.DownloadUtils;
import com.lulu.admin.kuyimusic.utils.MediaUtils;
import com.lulu.admin.kuyimusic.utils.SearchMusicUtils;
import com.lulu.admin.kuyimusic.vo.Mp3Info;
import com.lulu.admin.kuyimusic.vo.SearchResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 播放主界面, 通过点击"我的音乐"界面中的右下角图片跳转
 */
public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "PlayActivity";

    private static final int UPDATE_LRC = 0x1000;
    private TextView textView_title, textView_end_time, textView_start_time;
    private ImageView imageView_album, imageView_play_mode,
            imageView_prev, imageView_play_pause, imageView_next, imageView_favorite;
    private SeekBar seekBar;

    //    private ArrayList<Mp3Info> mp3Infos;
    private int position;
    //更新播放时间的标记
    private static final int UPDATE_TIME = 0x1;
    //接收由"我的音乐"界面传过来, isPause
    private boolean isPause = false;

    private ViewPager viewPager;
    private ArrayList<View> views = new ArrayList<>();
    //将全局应用对象设置为字段
    private MyPlayerAPP app;

    //歌词显示页面
    private LrcView lrcView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        app = (MyPlayerAPP) getApplication();


        textView_end_time = (TextView) findViewById(R.id.textView_end_time);
        textView_start_time = (TextView) findViewById(R.id.textView_start_time);

        imageView_play_mode = (ImageView) findViewById(R.id.imageView_play_mode);
        imageView_prev = (ImageView) findViewById(R.id.imageView_prev);
        imageView_play_pause = (ImageView) findViewById(R.id.imageView_play_pause);
        imageView_next = (ImageView) findViewById(R.id.imageView_next);
        imageView_favorite = (ImageView) findViewById(R.id.imageView_favorite);
        seekBar = (SeekBar) findViewById(R.id.seekBar);


        //初始化ViewPager
        viewPager = (ViewPager) findViewById(R.id.viewPgaer);
        initViewPager();

        //事件的注册
        imageView_prev.setOnClickListener(this);
        imageView_next.setOnClickListener(this);
        imageView_play_pause.setOnClickListener(this);
        imageView_play_mode.setOnClickListener(this);
        imageView_favorite.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);


        //给Handler赋值
        myHandler = new MyHandler(this);
    }

    //初始化ViewPager
    private void initViewPager() {
        //解析到我们显示专辑图片的页面
        View album_image_layout = getLayoutInflater().inflate(R.layout.album_image_layout, null);
        textView_title = (TextView) album_image_layout.findViewById(R.id.textView_title);
        imageView_album = (ImageView) album_image_layout.findViewById(R.id.imageView_album);
        views.add(album_image_layout);

        View lrc_layout = getLayoutInflater().inflate(R.layout.lrc_layout, null);
        lrcView = (LrcView) lrc_layout.findViewById(R.id.lrcView);

        //在此处设置滚动事件
        lrcView.setListener(new ILrcView.LrcViewListener() {
            @Override
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (playService.isPlaying()) {
                    playService.seekTo((int) row.time);
                }
            }
        });
        lrcView.setLoadingTipText("正在加载歌词");
        lrcView.setBackgroundResource(R.mipmap.img_back_lrc);
       
        views.add(lrc_layout);
        viewPager.setAdapter(new MyPagerAdapter());


    }


    @Override
    protected void onResume() {
        super.onResume();
        //绑定PlayService
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Activity暂停时解绑(被覆盖)
        unbindPlayServie();
    }


    private static MyHandler myHandler;

    static class MyHandler extends Handler {

        private PlayActivity playActivity;

        public MyHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //要有判空操作
            if (playActivity != null) {
                View toastView = playActivity.getToastView();
                TextView textView = (TextView) toastView.findViewById(R.id.textView_toast);
                Toast t = new Toast(playActivity);
                t.setView(toastView);
                t.setDuration(Toast.LENGTH_SHORT);


                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.textView_start_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;

                    case UPDATE_LRC:
                        //更新歌词
                        if (msg.obj != null) {


                            playActivity.lrcView.seekLrcToTime(Long.parseLong(msg.obj.toString()));
                        }
                        break;
                    case DownloadUtils.SUCCESS_LRC:

                        textView.setText("歌词下载成功o((≧▽≦o)！！");
                        t.show();

                        playActivity.loadLRC(new File((String) msg.obj));
                        break;
                    case DownloadUtils.FAIED_LRC:

                        textView.setText("歌词下载失败(>﹏<。)～……");
                        t.show();
                        ILrcBuilder builder = new DefaultLrcBuilder();
                        List<LrcRow> rows = builder.getLrcRows("歌词下载失败! ");
                        playActivity.lrcView.setLrc(rows);


                        break;
                }
            }
        }
    }

    @Override
    public void publish(int progress) {
//        System.out.println(MediaUtils.formatTime(progress));
        //更新播放时间的操作
//        textView_start_time.setText(MediaUtils.formatTime(progress));//此处不能用, 因为不能在线程中更新UI
        //可以用Handler来实现
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
        //进度条的更新
        seekBar.setProgress(progress);

        //通过Handler处理歌词进度更新
        myHandler.obtainMessage(UPDATE_LRC, progress).sendToTarget();

    }

    @Override
    public void change(int position) {
        //因为不管你播放还是播放界面总是要更新的!!!
        //需要判断, 位置是否合法
//        System.out.println("PlayActivity中的position为: " + position);
        if (position >= 0 && position < playService.getMp3Infos().size()) {
            Mp3Info mp3Info = playService.getMp3Infos().get(position);
            textView_title.setText(mp3Info.getTitle());
            Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
            imageView_album.setImageBitmap(albumBitmap);
            textView_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));

            if (playService.isPlaying()) {
                imageView_play_pause.setImageResource(R.mipmap.pause);
            } else {
                imageView_play_pause.setImageResource(R.mipmap.play);
            }


            seekBar.setProgress(0);
            seekBar.setMax((int) mp3Info.getDuration());

            //更新播放模式
            switch (playService.getPlay_mode()) {
                case PlayService.ORDER_PLAY:
                    imageView_play_mode.setImageResource(R.mipmap.order);
                    imageView_play_mode.setTag(PlayService.ORDER_PLAY);
                    break;
                case PlayService.RANDOM_PLAY:
                    imageView_play_mode.setImageResource(R.mipmap.random);
                    imageView_play_mode.setTag(PlayService.RANDOM_PLAY);
                    break;
                case PlayService.SINGLE_PLAY:
                    imageView_play_mode.setImageResource(R.mipmap.single);
                    imageView_play_mode.setTag(PlayService.SINGLE_PLAY);
                    break;
                default:
                    break;
            }

            //初始化收藏按钮的状态值
            try {

                Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", getId(mp3Info)));


                if (likeMp3Info != null) {
                    if (likeMp3Info.getIsLike() == 1) {
                        imageView_favorite.setImageResource(R.mipmap.liked);
                    } else {
                        imageView_favorite.setImageResource(R.mipmap.like);
                    }

                } else {
                    imageView_favorite.setImageResource(R.mipmap.like);
                }


            } catch (DbException e) {
                e.printStackTrace();
            }


            //将歌词显示
            lrcDisplay(mp3Info);

        }
    }


    /**
     * 负责歌词的显示
     * @param mp3Info
     */
    private void lrcDisplay(final Mp3Info mp3Info){



        //歌词显示
        final String songName = mp3Info.getTitle();

        System.out.println("songName:" + songName);

        String lrcPath = Environment.getExternalStorageDirectory() + Constant.DIR_LRC + "/" + songName + ".lrc";
        File lrcFile = new File(lrcPath);
        //如果本地不存在就去网络上搜索
        if (!lrcFile.exists()) {
            //下载
            SearchMusicUtils.getsInstance().setListener(new SearchMusicUtils.OnSearchResultLister() {
                @Override
                public void onSearchResult(ArrayList<SearchResult> results) {


                        //这个地方耽误了我好久!!!!!!!!!!!!!艹艹
                        if(results != null){
                            Log.d(TAG, "onSearchResult: " + results.get(0));
                            //搜索出来之后
                            SearchResult searchResult = results.get(0);
                            String url = Constant.BAIDU_URL + searchResult.getUrl();


                            //Handler目的是在下载完了之后可以通知我们
                            DownloadUtils.getsInstance().downloadLRC(url, songName, myHandler);
                        }else{
                            myHandler.sendEmptyMessage(DownloadUtils.FAIED_LRC);
                        }



                }
            }).search(songName + " " + mp3Info.getArtist(), 1);
        } else {
            loadLRC(lrcFile);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //播放暂停
            case R.id.imageView_play_pause: {
                //如果是播放状态就
                if (playService.isPlaying()) {
                    imageView_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        imageView_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        playService.start();
                    } else {
                        playService.play(playService.getCurrentPosition());
                    }
                }
                break;
            }

            case R.id.imageView_next: {
                playService.next();
                break;
            }
            case R.id.imageView_prev: {
                playService.prev();
                break;
            }
            //播放模式的点击事件
            case R.id.imageView_play_mode: {
                View toastView = getToastView();
                TextView textView = (TextView) toastView.findViewById(R.id.textView_toast);
                Toast t = new Toast(this);
                t.setView(toastView);
                t.setDuration(Toast.LENGTH_SHORT);

                int mode = (int) imageView_play_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        textView.setText(R.string.random_play);
                        t.show();
                        imageView_play_mode.setImageResource(R.mipmap.random);
                        imageView_play_mode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        break;
                    case PlayService.RANDOM_PLAY:
                        textView.setText(R.string.single_play);
                        t.show();
                        imageView_play_mode.setImageResource(R.mipmap.single);
                        imageView_play_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        break;
                    case PlayService.SINGLE_PLAY:
                        textView.setText(R.string.order_play);
                        t.show();
                        imageView_play_mode.setImageResource(R.mipmap.order);
                        imageView_play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        break;
                    default:
                        break;
                }
                break;
            }
            //收藏功能的点击事件
            case R.id.imageView_favorite: {

                //得到当前的mp3信息
                Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
                //需要使用数据库的内容了
                try {
                    //将当前播放音乐的id与数据库表中的mp3InfoId比较, 查找
                    //mp3InfoId增加该列的原因: 因为xUtils工具类保存数据时会将原来的id给覆盖掉(因为其本身自增长,按照自己的增长顺序设置id)
                    Mp3Info likeMp3Info =
                            app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", getId(mp3Info)));//务必注意此处app.dbUtils.findById();和当前方法的区别

//                    System.out.println("我喜欢" + likeMp3Info);
                    if (likeMp3Info == null) {
                        //在保存的同时需要将我们添加的字段mp3InfoId给给设置进去
                        mp3Info.setMp3InfoId(mp3Info.getId());//此时的ID一定是在"我的音乐"列表中的ID
                        //设置为1,也就是喜欢
                        mp3Info.setIsLike(1);
                        //这就说明在收藏数据库中没有该音乐的信息, 需要将其保存到数据库中
                        app.dbUtils.save(mp3Info);
                        //改变收藏的状态
                        imageView_favorite.setImageResource(R.mipmap.liked);
                    } else {

                        int isLike = likeMp3Info.getIsLike();
                        if (isLike == 1) {
                            likeMp3Info.setIsLike(0);
                            imageView_favorite.setImageResource(R.mipmap.like);
                        } else {
                            likeMp3Info.setIsLike(1);
                            imageView_favorite.setImageResource(R.mipmap.liked);
                        }

                        //将状态值更新到数据库中
                        app.dbUtils.update(likeMp3Info, "isLike");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }

    private long getId(Mp3Info mp3Info) {
        long id = 0;

        switch (playService.getChangePlayList()) {
            case PlayService.MY_MUSIC_LIST:
                id = mp3Info.getId();
                break;
            case PlayService.LIKE_MUSIC_LIST:
                id = mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;
    }

    //SeekBar的监听事件
    //当进度发生变化
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //是否是用户"改变了进度了
        if (fromUser) {
//            playService.pause();
            playService.seekTo(progress);
//            playService.start();
        }
    }

    //开始拖动
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    //结束拖动
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    private View getToastView() {
        LayoutInflater inflater = getLayoutInflater();
        return inflater.inflate(R.layout.toast_play_mode, null);
    }

    //ViewPager的Adapter
    class MyPagerAdapter extends PagerAdapter {

        //实例化选项卡
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = views.get(position);
            container.addView(v);
            return v;
        }

        //删除选项卡
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }


    //加载歌词
    private void loadLRC(File lrcFile) {
        StringBuffer buffer = new StringBuffer(1024 * 10);
        char[] chars = new char[1024];

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(lrcFile)));
            int len = -1;
            while ((len = in.read(chars)) != -1) {
                buffer.append(chars, 0, len);
            }
            in.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(buffer.toString());
        lrcView.setLrc(rows);

    }


}

