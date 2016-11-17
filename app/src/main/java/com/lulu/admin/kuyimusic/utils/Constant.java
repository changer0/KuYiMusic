package com.lulu.admin.kuyimusic.utils;

import android.app.Application;
import android.content.Context;

/**
 * 常量池, 放的是平常到的常量
 * Created by Admin on 2016/5/21.
 */
public class Constant {
    public static final String SP_NAME = "KuyiMusic";
    public static final String DB_NAME = "KuyiPlayer.db";
    //最近播放显示的最大的条数
    public static final int PLAY_RECORD_MAX = 5;

    //百度音乐的根地址
    public static final String BAIDU_URL = "http://music.baidu.com/";

    //热歌榜
    public static final String BAIDU_DAYHOT = "top/dayhot/?pst=shouyeTop";
    //经典老歌榜
    public static final String BAIDU_OLD = "top/oldsong/?pst=shouyeTop";

    //搜索
    public static final String BAIDU_SEARCH = "search/song";
    //全世界的人都这样写, 在材料中有个html文件
    //表示客户端浏览器的
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0";


    //成功标记
    public static final int SUCCESS = 0;
    //失败标记
    public static final int FAILED = 1;

    public static final String DIR_LRC = "/KuyiMusicLrc";




}
