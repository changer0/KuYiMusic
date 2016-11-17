package com.lulu.admin.kuyimusic.utils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.lulu.admin.kuyimusic.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 搜索工具类
 * Created by Admin on 2016/5/25.
 */
public class SearchMusicUtils {
    private static final String TAG = "SearchMusicUtils";
    private static final int SIZE = 20;//只查询前SIZE条记录
    private static final String URL = Constant.BAIDU_URL + Constant.BAIDU_SEARCH;
    private static SearchMusicUtils sInstance;//当前实例
    private OnSearchResultLister mListener;//
    private ExecutorService mThreadTool;//创建一个线程池, 用来创建线程, 该线程用来完成获取搜索的信息的功能,不能阻塞主线程!

    //此处是同步方法!!
    public synchronized static SearchMusicUtils getsInstance() {
        if (sInstance == null) {
            sInstance = new SearchMusicUtils();
        }
        //返回当前实例
        return sInstance;
    }

    //构造方法私有化, 单例模式!
    private SearchMusicUtils() {
        //创建单个线程在线程池中
        mThreadTool = Executors.newSingleThreadExecutor();
    }

    /**
     * 设置监听的同时返回当前对象
     *
     * @param listener
     * @return
     */
    public SearchMusicUtils setListener(OnSearchResultLister listener) {
        mListener = listener;
        return this;
    }


    public void search(final String key, final int page) {


        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Constant.SUCCESS:
                        if (mListener != null) {
                            //如果数据获取成功, 就回调下面的方法
                            mListener.onSearchResult((ArrayList<SearchResult>) msg.obj);
                        }
                        break;
                    case Constant.FAILED:
                        if (mListener != null) {
                            mListener.onSearchResult(null);
                        }
                        break;
                    default:
                        break;
                }
            }
        };


        /**
         * 进入search后先执行下面的方法
         */
        mThreadTool.execute(new Runnable() {
            @Override
            public void run() {
                //获取到查询的数据
                ArrayList<SearchResult> searchResults = getMusicList(key, page);
                if (searchResults == null) {
                    handler.sendEmptyMessage(Constant.FAILED);
                    return;
                }
                Message msg = Message.obtain();
                msg.obj = searchResults;
                msg.what = Constant.SUCCESS;
                handler.sendMessage(msg);
            }


        });
    }

    /**
     * 核心业务类,使用Jsoup从搜索结果中抓取数据
     *
     * @param key: 查询的关键字
     * @param page : 查询的页
     * @return : SearchResult的集合
     */
    private ArrayList<SearchResult> getMusicList(String key, int page) {
        //获取开始的标号,
        final String start = String.valueOf((page - 1) * SIZE);


        //因为需要传入参数了所以需要用Jsoup的data方法

        try {
            //根据下面原链接地址
            //http://music.baidu.com/search/song?s=1&key=周杰伦&start=20&size=20
            Document doc = Jsoup.
                    connect(URL).data("key", key, "start", start, "size", String.valueOf(SIZE)).
                    userAgent(Constant.USER_AGENT).timeout(6 * 1000).get();
            Log.d(TAG, "getMusicList: " + doc.toString());
            /*
                <div class="song-item">

                        <a href="/song/7316935"  target="_blank"  title="龙卷风">
                            龙卷风
                        </a>
                    <span class="author_list" title="周杰伦">
						<a  target = "_blank"hidefocus="true" href="/artist/7994">周杰伦</a>
					</span>

                </div>

            */
            Elements songTitles = doc.select("div.song-item.clearfix");
            ArrayList<SearchResult> searchResults = new ArrayList<>();
            Elements songInfoAs = null;
            Element songInfoA = null;

            //从每个div标签中查找
            for (int i = 0; i < songTitles.size(); i++) {
                //在div中查找a标签的集合
                songInfoAs = songTitles.get(i).getElementsByTag("a");
                SearchResult searchResult = new SearchResult();
                if (songInfoAs != null) {
                    for (int j = 0; j < songInfoAs.size(); j++) {
                        //此info是指每个a标签中的info
                        songInfoA = songInfoAs.get(j);
                        //startsWith:Compares the specified string to this string to determine if the specified string is a prefix(前缀).
                        //startsWith:比较两个字符串, 检查是否该字符串是其前缀
                        //找出歌名和链接
                        if (songInfoA.attr("href").startsWith("/song")) {
                            searchResult.setMusicName(songInfoA.text());
                            searchResult.setUrl(songInfoA.attr("href"));
                        }
                        if (songInfoA.attr("href").startsWith("/album")) {
                            searchResult.setAlbum(songInfoA.text());
                        }
                        if (songInfoA.attr("href").startsWith("/data")) {
                            searchResult.setArtist(songInfoA.text());
                        }
                    }
                    //非空判断, 如果音乐名字或者链接没有就视为没有此音乐
                    if (TextUtils.isEmpty(searchResult.getMusicName()) || TextUtils.isEmpty(searchResult.getUrl())) {
                        break;
                    }
                    searchResults.add(searchResult);
                }
            }

            return searchResults;

        } catch (IOException e) {
            return null;

        }

        //测试数据
//        ArrayList<SearchResult> searchResults = new ArrayList<>();
//        SearchResult searchResult1 = new SearchResult("牛逼", "132", "卧槽", "哈拉齐");
//        SearchResult searchResult2 = new SearchResult("牛逼2", "132", "卧槽2", "哈拉齐2");
//
//        searchResults.add(searchResult1);
//        searchResults.add(searchResult2);


    }

    //监听回调机制
    public interface OnSearchResultLister {
        void onSearchResult(ArrayList<SearchResult> results);
    }

}

