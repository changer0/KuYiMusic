package com.lulu.admin.kuyimusic.utils;

import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.lulu.admin.kuyimusic.vo.SearchResult;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 专门用来下载音乐和歌词的工具类
 * Created by Admin on 2016/5/27.
 */
public class DownloadUtils {
    private static final String TAG = "DownloadUtils";
    public static final int SUCCESS_LRC = 2;//下载歌词成功
    public static final int FAIED_LRC = 3;//下载歌词失败
    public static final int SUCCESS_MP3 = 4;


    private static DownloadUtils sInstance;

    private ExecutorService mThreadPool;


    public synchronized static DownloadUtils getsInstance() {
        if (sInstance == null) {

            sInstance = new DownloadUtils();

        }
        return sInstance;
    }

    //在创建对象时, 创建单个线程放入线程池
    private DownloadUtils() {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    /**
     * @param searchResult
     */
    public void download(final SearchResult searchResult) {

    }

/*
    http://music.baidu.com/song/265046969


    <div id="lyricCont" class="lyric-content" style="display: none;"
    data-lrclink="/data2/lrc/23132f075b8aa0446c9d7f8aedabe29d/265180519/265180519.lrc"></div>
    <a href="" id="lyricSwitch" class="lyric-switch" style="display: none;">
    <span class="text" title="展开龙梅子,杨海彪《情歌继续唱Ⅱ》寂寞的人伤心的歌歌词">展开</span>
    <span class="icon"></span>
    </a>

    </div>
*/

    /**
     * 歌词的下载
     *
     * @param url
     * @param musicName
     * @param handler
     */
    public void downloadLRC(final String url, final String musicName, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT)
//                            .timeout(6000).get();
//                    Log.d(TAG, "run: " + doc.toString());
//                    Elements lrcTag = doc.select("div.lyric-content");
//                    String lrcUrl = lrcTag.attr("data-lrcLink");
//                    File lrcDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_LRC);
//                    //如果不存在该目录就创建该目录
//                    if(!lrcDirFile.exists()){
//                        lrcDirFile.mkdir();
//                    }
//                    //拼接成真正的下载路径
//                    lrcUrl = Constant.BAIDU_URL + lrcUrl;
//                    //目标歌词, 就是给我们的歌词命名
//                    String target = lrcDirFile + "/" + musicName + ".lrc";

                    // 2016/11/17新的方式获取lrc歌词
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
//                    Log.d(TAG, "run: doc" + doc.toString());
                    Elements lrcBtn = doc.select("a.down-lrc-btn");
                    String lrcJson = lrcBtn.attr("data-lyricdata");

                    if (!TextUtils.isEmpty(lrcJson)) {
//                        Log.d(TAG, "run: lrcJson" + lrcJson);
                        JSONObject jsonObj = new JSONObject(lrcJson);
                        String lrcUrl = jsonObj.optString("href");

                        File lrcDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_LRC);
                        //如果不存在该目录就创建该目录
                        if (!lrcDirFile.exists()) {
                            lrcDirFile.mkdirs();
                        }
                        String target = lrcDirFile + "/" + musicName + ".lrc";
                        //用OKhttpClient第三方包下载
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(lrcUrl).build();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {//下载成功
                            PrintStream ps = new PrintStream(new File(target));
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            //下载已经下载成功了, 通知Handler
                            //将下载的路径传过去方便读取
                            handler.obtainMessage(SUCCESS_LRC, target).sendToTarget();
                        } else {
                            handler.obtainMessage(FAIED_LRC).sendToTarget();
                        }


                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }


}
