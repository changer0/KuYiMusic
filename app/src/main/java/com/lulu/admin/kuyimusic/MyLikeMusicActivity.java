package com.lulu.admin.kuyimusic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lulu.admin.kuyimusic.adapter.MyMusicListAdapter;
import com.lulu.admin.kuyimusic.vo.Mp3Info;

import java.util.ArrayList;
import java.util.List;

/**
 * 这是我喜欢音乐播放列表的Activity
 * <p/>
 * 一定要先进行绑定服务!!!!!!!!!!!!!!!!!!!!!!!!!!
 * <p/>
 * <p/>
 * 重复利用MyMusicListAdapter和item_music_list布局文件
 */
public class MyLikeMusicActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView listView_like = null;
    MyPlayerAPP myPlayerAPP = null;
    private ArrayList<Mp3Info> likeMp3Infos = null;
    private MyMusicListAdapter adapter = null;
    //用来标记是否我们的播放列表已经填充
    private boolean isChange = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPlayerAPP = (MyPlayerAPP) getApplication();

        setContentView(R.layout.activity_my_like_music);
        listView_like = (ListView) findViewById(R.id.listView_like);
        //给listView注册监听事件
        listView_like.setOnItemClickListener(this);//单击播放
        listView_like.setOnItemLongClickListener(this);//长按取消收藏


        //首先进行初始化数据
        initData();
    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayServie();
    }

    private void initData() {


        try {
            List<Mp3Info> list = myPlayerAPP.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike", "=", "1"));


            if (list == null || list.size() == 0) {
                //将listView隐藏!!!
                listView_like.setVisibility(View.GONE);
                return;
            }
            //要记住再让其显示
            listView_like.setVisibility(View.VISIBLE);
            likeMp3Infos = (ArrayList<Mp3Info>) list;
            //记住要做非空判断
            if (likeMp3Infos != null) {
                adapter = new MyMusicListAdapter(this, likeMp3Infos);
                listView_like.setAdapter(adapter);
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单击item监听事件
     * 播放
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList() != PlayService.LIKE_MUSIC_LIST) {
            //把"我喜欢"的列表直接设置到mp3Infos中
            playService.setMp3Infos(likeMp3Infos);
//            System.out.println("MyLikeMusicActivity中的播放位置为: " + position);
            playService.setChangePlayList(PlayService.LIKE_MUSIC_LIST);
        }
        playService.play(position);

        //在此处坐一下简单的解释:
        //当点击每个播放列表时,将会自动保存播放记录
        //保存播放记录
        savePlayRecord();
    }

    /**
     * 保存当前的播放记录
     */
    private void savePlayRecord() {

        //同样还是先取出当前的播放的音乐信息
        Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
        //还是进行查询比较!!
        try {
            Mp3Info playRecordMp3Info = myPlayerAPP.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getMp3InfoId()));
            if (playRecordMp3Info == null) {

                //此处不用设置Mp3InfoId
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                myPlayerAPP.dbUtils.save(mp3Info);

            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                myPlayerAPP.dbUtils.update(playRecordMp3Info, "playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }


    }

    /**
     * 长按取消收藏!
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告!").setMessage("是否取消收藏??").setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Mp3Info likeMp3Info = likeMp3Infos.get(position);
                int isLike = likeMp3Info.getIsLike();
                if (isLike == 1) {//做下判断
                    likeMp3Info.setIsLike(0);

                    try {
                        //更新一下数据库
                        myPlayerAPP.dbUtils.update(likeMp3Info, "isLike");
                        //重新加载数据
                        initData();
                        listView_like.deferNotifyDataSetChanged();
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();

        return true;
    }
}
