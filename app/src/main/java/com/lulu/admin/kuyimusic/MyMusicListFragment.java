package com.lulu.admin.kuyimusic;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lulu.admin.kuyimusic.adapter.MyMusicListAdapter;
import com.lulu.admin.kuyimusic.utils.MediaUtils;
import com.lulu.admin.kuyimusic.vo.Mp3Info;

import java.util.ArrayList;

/**
 * 本地播放列表的Adapter
 * Created by Admin on 2016/5/7.
 */
public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "MyMusicListFragment";
    private ListView listView_my_music;

    private ImageView imageView_album;
    private TextView textView_songName, textView_singer;
    private ImageView imageView_play_pause, imageView_next;
    private ArrayList<Mp3Info> mp3Infos;
    private MainActivity mainActivity;
    private MyMusicListAdapter myMusicListAdapter;



    //播放位置
//    private int position = 0;


    //最先开始执行的的方法onAttach
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        Log.i(TAG, "onAttach");
    }

    public static MyMusicListFragment newInstance() {
        //额, 第三包的sample中就这样写的, 用来获取Fragment实例
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_list_layout, null);

        textView_songName = (TextView) view.findViewById(R.id.textView_songName);
        textView_singer = (TextView) view.findViewById(R.id.textView_singer);
        imageView_album = (ImageView) view.findViewById(R.id.imageView_album);
        imageView_play_pause = (ImageView) view.findViewById(R.id.imageView_play_pause);
        imageView_next = (ImageView) view.findViewById(R.id.imageView_next);


        listView_my_music = (ListView) view.findViewById(R.id.listView_my_music);


        listView_my_music.setOnItemClickListener(this);
        imageView_play_pause.setOnClickListener(this);
        imageView_album.setOnClickListener(this);
        imageView_next.setOnClickListener(this);
        textView_singer.setOnClickListener(this);
        textView_songName.setOnClickListener(this);



        //加载数据
        loadData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //重要!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //之前绑定Service时放到了onCreateView中,这样如果回来之后就不会调用了, 所以我把他放到了onResume方法中
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解除绑定服务
        mainActivity.unbindPlayServie();
    }


    /**
     * 加载本地音乐列表
     */
    public void loadData() {
        //查询歌曲信息
        mp3Infos = MediaUtils.getMp3Infos(mainActivity);

        if(mp3Infos == null){
            return;
        }
        //给ListView填充Adapter, 在这里将PlayService维护的mp3Infos加入到Adapter中去
        myMusicListAdapter = new MyMusicListAdapter(mainActivity, mp3Infos);
        listView_my_music.setAdapter(myMusicListAdapter);
    }

    //点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mainActivity.playService.getChangePlayList() != mainActivity.playService.MY_MUSIC_LIST) {
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(mainActivity.playService.MY_MUSIC_LIST);
        }

        //列表单击播放
        mainActivity.playService.play(position);

        //保存播放记录
        savePlayRecord();
    }

    /**
     * 保存播放记录
     */
    private void savePlayRecord() {

        //先取出当前播放的音乐
        Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());

        //接下来开始做查询
        try {
            //因为现在是在MyMusiclistFragment中, 所以此处使用mp3Info.getId()没有问题
            Mp3Info playRecordMp3Info =
                    mainActivity.myApplication.
                            dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getId()));
            if (playRecordMp3Info == null) {
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前的播放时间, 采用当前的系统时间(毫秒)
                //将数据保存到数据库
                mainActivity.myApplication.dbUtils.save(mp3Info);
            } else {
                //如果记录存在的话, 只需要更新操作就好
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                mainActivity.myApplication.dbUtils.update(playRecordMp3Info, "playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }


    //回调的UI状设置
    public void changerUIStatusOnPlay(int position) {
//        this.position = position;
        if (position >= 0 && position < mainActivity.playService.getMp3Infos().size()) {
            System.out.println("当前的播放位置: " + position);
            Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(position);
            textView_songName.setText(mp3Info.getTitle());
            textView_singer.setText(mp3Info.getArtist());

            Bitmap albumBitmap = MediaUtils.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            imageView_album.setImageBitmap(albumBitmap);
            if (mainActivity.playService.isPlaying()) {
                imageView_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
            } else {
                imageView_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_play_pause: {
                if (mainActivity.playService.isPlaying()) {
                    imageView_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                    mainActivity.playService.pause();

                } else {

                    if (mainActivity.playService.isPause()) {

                        imageView_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainActivity.playService.start();
                    } else {
                        //在默认情况下直接播放第0首音乐
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                    }
                }
            }
            break;
            case R.id.imageView_next: {
                //点击下一首
                mainActivity.playService.next();
                break;
            }

            case R.id.textView_singer:
            case R.id.textView_songName:
            case R.id.imageView_album:
                //点击跳转到PlayActivity
                Intent intent = new Intent(mainActivity, PlayActivity.class);
                startActivity(intent);
                break;
        }
    }
}
