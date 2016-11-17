package com.lulu.admin.kuyimusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lulu.admin.kuyimusic.adapter.MyMusicListAdapter;
import com.lulu.admin.kuyimusic.utils.Constant;
import com.lulu.admin.kuyimusic.vo.Mp3Info;

import java.util.ArrayList;
import java.util.List;

/**
 * 最近播放的Activity
 */
public class PlayRecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView_record = null;
    private TextView textView_record = null;
    private MyPlayerAPP myPlayerAPP = null;
    private ArrayList<Mp3Info> mp3Infos = null;
    private MyMusicListAdapter adapter = null;

    public static final int CLEAR_ALL = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record_list);

        listView_record = (ListView) findViewById(R.id.listView_record);
        textView_record = (TextView) findViewById(R.id.textView_record);
        myPlayerAPP = (MyPlayerAPP) getApplication();
        listView_record.setOnItemClickListener(this);

//        System.out.println("PlayRecordList initData()");
        initData();


    }


    //初始化最近播放的数据
    private void initData() {
        //要记住再让其显示
        listView_record.setVisibility(View.VISIBLE);

        try { //查询最近播放的记录, 排序跟据playTime排序倒序!!!
            List<Mp3Info> list = myPlayerAPP.dbUtils.findAll(Selector.from(Mp3Info.class).
                    where("playTime", "!=", 0).orderBy("playTime", true).limit(Constant.PLAY_RECORD_MAX));
            if (list == null || list.size() == 0) {
                Toast.makeText(PlayRecordListActivity.this, "当前没有播放记录", Toast.LENGTH_SHORT).show();
            } else {
                mp3Infos = (ArrayList<Mp3Info>) list;

                adapter = new MyMusicListAdapter(this, mp3Infos);
                listView_record.setAdapter(adapter);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
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

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList() != PlayService.PLAY_RECORD_MUSIC_LIST) {
            playService.setMp3Infos(mp3Infos);
            playService.setChangePlayList(PlayService.PLAY_RECORD_MUSIC_LIST);
        }

        playService.play(position);
    }

    //用来清空整个最近播放列表
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, CLEAR_ALL, 1, "清空最近播放记录");
        return super.onCreateOptionsMenu(menu);
    }

    //菜单栏事件监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case CLEAR_ALL:


                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("警告!").setMessage("是否清空最近播放列表?").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //先从数据库中读取playTime不为0的mp3Info然后再进行update
                        try {
                            List<Mp3Info> list = myPlayerAPP.dbUtils.
                                    findAll(Selector.from(Mp3Info.class).where("playTime", "!=", "0"));
                            ArrayList<Mp3Info> ms = (ArrayList<Mp3Info>) list;
                            for (Mp3Info m :
                                    ms) {
                                if (m.getPlayTime() != 0) {
                                    m.setPlayTime(0);
                                }
                            }

                            myPlayerAPP.dbUtils.updateAll(ms, "playTime");
                            listView_record.setVisibility(View.GONE);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }

                    }
                }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();


                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
