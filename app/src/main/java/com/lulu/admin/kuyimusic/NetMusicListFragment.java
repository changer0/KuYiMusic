package com.lulu.admin.kuyimusic;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.lulu.admin.kuyimusic.adapter.NetMusicAdapter;
import com.lulu.admin.kuyimusic.utils.AppUtils;
import com.lulu.admin.kuyimusic.utils.Constant;
import com.lulu.admin.kuyimusic.utils.SearchMusicUtils;
import com.lulu.admin.kuyimusic.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Admin on 2016/5/7.
 */
public class NetMusicListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {



    private MainActivity mainActivity = null;
    private ListView listView_net_music = null;
    private SwipeRefreshLayout swip_refresh = null;
    private LinearLayout ll_search_container = null;
    private LinearLayout ll_search_btn_container = null;
    private ImageButton ib_search_btn = null;
    private EditText et_search_content = null;
    private int page = 1;//搜索第1页
    public static final int HANDLE_LOAD_NETDATA = 0x1;//下拉刷新Handler的标记值
    //换种方式来设置适配器
    private NetMusicAdapter netMusicAdapter = null;

    private View footerView = null;


    //专门用来保存搜索结果的list集合
    private ArrayList<SearchResult> searchResults = new ArrayList<>();


    //实例创建
    public static NetMusicListFragment newInstance() {
        NetMusicListFragment net = new NetMusicListFragment();
        return net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //UI组件的初始化
        View view = inflater.inflate(R.layout.net_music_list, null);
        listView_net_music = (ListView) view.findViewById(R.id.listView_net_music);
        ll_search_container = (LinearLayout) view.findViewById(R.id.ll_search_container);
        ll_search_btn_container = (LinearLayout) view.findViewById(R.id.ll_search_btn_container);
        ib_search_btn = (ImageButton) view.findViewById(R.id.ib_search_btn);
        et_search_content = (EditText) view.findViewById(R.id.et_search_content);


        swip_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swip_refresh);
        swip_refresh.setColorSchemeResources(android.R.color.holo_red_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light);

        footerView = LayoutInflater.from(mainActivity).inflate(R.layout.footview_layout, null);


        //监听事件的使用
        listView_net_music.setOnItemClickListener(this);
        ll_search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);
        swip_refresh.setOnRefreshListener(this);


        //给适配器初始化
        netMusicAdapter = new NetMusicAdapter(mainActivity);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //加载数据时在此处
        swip_refresh.setRefreshing(true);
        loadNetData();
    }

    private void loadNetData() {

        //执行异步加载网络音乐的任务
        new LoadNetDataTask().execute(Constant.BAIDU_URL + Constant.BAIDU_DAYHOT);
    }


    //音乐列表的单击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //索引合法判断,即是否越界
        if(position >= netMusicAdapter.getSearchResults().size() || position < 0){
            return ;
        }
        showDownloadDialog(position);
//        System.out.println("监听事件响应了!!!!");
    }

    private void showDownloadDialog(int position) {

        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment(netMusicAdapter.getSearchResults().get(position));
        downloadDialogFragment.show(getFragmentManager(), "download");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击了搜索按钮
            case R.id.ll_search_btn_container:
                //先隐藏其本身
                ll_search_btn_container.setVisibility(View.GONE);
                //然后显示搜索框
                ll_search_container.setVisibility(View.VISIBLE);
                break;
            case R.id.ib_search_btn:
                //处理搜索事件
                searchMusic();

                break;
            default:
                break;
        }
    }

    //搜索音乐的方法
    private void searchMusic() {
        //点击之后需要将键盘隐藏掉!
        AppUtils.hideInputMethod(et_search_content);
        //搜索框恢复到初始化状态
        ll_search_btn_container.setVisibility(View.VISIBLE);
        ll_search_container.setVisibility(View.GONE);
        //获取到被搜索的关键字
        String key = et_search_content.getText().toString();
        if (TextUtils.isEmpty(key)) {
            Toast.makeText(mainActivity, "请输入关键字", Toast.LENGTH_SHORT).show();
            return;
        }

        //进行搜索, 这是个痛苦的过程, 此处还是应用了 >>观察者设计模式<<
        //因为网络搜索也是一个异步的任务, 所以我们并不清楚什么时候搜索结束, 所以需要我们设计一个监听和回调机制
        //也就是说, 当获取完网络数据后回调我们的方法完成UI等的更新操作
        SearchMusicUtils.getsInstance().setListener(new SearchMusicUtils.OnSearchResultLister() {
            @Override
            public void onSearchResult(ArrayList<SearchResult> results) {
                //当网络搜索完毕后会回调该方法, 并将results数据传送回来
                //此时要重新设置数据源
                //你会发现下面的做法没有用set方法, 因为在这里如果results为空的话会不方便处理
                ArrayList<SearchResult> sr = netMusicAdapter.getSearchResults();
                if (sr == null) {
                    sr = new ArrayList<SearchResult>();
                }
                sr.clear();
                sr.addAll(results);

                netMusicAdapter.notifyDataSetChanged();
            }
        }).search(key, page);

    }



    /**
     * 获取网络数据的异步任务类
     */
    private class LoadNetDataTask extends AsyncTask<String, Integer, Integer> {

        //在主线程中运行, 并且在doInBackground()方法之前运行
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //让listView隐藏
//            listView_net_music.setVisibility(View.GONE);
            //清空
            searchResults.clear();
        }

        @Override
        protected Integer doInBackground(String... params) {

            //传入的参数为url地址
            String url = params[0];
            //使用Jsoup组件请求网络, 并解析音乐数据(根据) userAgent():表示本地使用的什么浏览器
            //timeout:超时时间
            try {

                 /*
                 <span class="song-title " style='width: 240px;'>
                    <a href="/song/121353608" target="_blank" title="刘珂矣半壶纱" data-film="null">
                    半壶纱
                    </a>
                </span>
                <span class="singer"  style="width: 240px;" >
                    <span class="author_list" title="刘珂矣">
                    <a hidefocus="true" href="/artist/132632388">刘珂矣</a>
                    </span>
                </span>
                  */
                //通过上面的标签去获得音乐的基本信息
                //接下来就是解析这个静态页面!!!!!(太痛苦了!!!!!!)
                Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6 * 1000).get();

//                System.out.println(doc);


                //通过分析页面, 在类选择器中的song-title一项span标签保存着歌曲名 , 这是获取了一个集合
                Elements songTitles = doc.select("span.song-title");
                //author_list属性中保存着歌手名
                Elements Artists = doc.select("span.author_list");
                //进行查询,
                for (int i = 0; i < songTitles.size()-1; i++) {
                    //创建一个保存搜索结果的对象
                    SearchResult searchResult = new SearchResult();
                    //因为我们之前已经获取到了span.song-title所有span标签
                    //现在从得到的集合songTitles中获取到相应的"a"标签
                    //此时也会获取到n个"a"标签放在urls中
                    Elements songTitleTagA = songTitles.get(i).getElementsByTag("a");
                    //所以从这n个"a"标签中取第0个, 找到其中属性"href", 然后把它的值返回作为歌曲的路径
                    searchResult.setUrl(songTitleTagA.get(0).attr("href"));
                    //而歌曲名的获取可以直接从第0个"a"标签的内容中获取
                    searchResult.setMusicName(songTitleTagA.get(0).text());

                    //接下来就要获取歌手的名字了
                    Elements artistTagA = Artists.get(i).getElementsByTag("a");

//                    Log.i("NetMusicFragment", artistTagA.text());
                    searchResult.setArtist(artistTagA.text());

                    //把专辑设置为
                    searchResult.setAlbum(getString(R.string.day_hot));
                    searchResults.add(searchResult);
                }

//                System.out.println(searchResults);

            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }


            return 1;
        }

        //在doInBackground()方法之后执行, 其中的result参数就是doInBackground方法的返回值
        //如果返回1说明读取成功, 返回-1说明读取失败!!!!
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            //在这个方法中给listview填充数据
            if (result == 1) {
//                System.out.println(searchResults);

                netMusicAdapter.setSearchResults(searchResults);
                listView_net_music.setAdapter(netMusicAdapter);
                //再加一个footView, 用于到最后的时候显示, 增加界面友好
                listView_net_music.addFooterView(footerView);
            }

            //最后让listView得以显示
//            listView_net_music.setVisibility(View.VISIBLE);
            swip_refresh.setRefreshing(false);

        }
    }


//    下拉刷新方法
//    此方法在子线程中运行, 不能直接修改UI
    @Override
    public void onRefresh() {
        Message msg = Message.obtain();
        handleLoadNetData.sendEmptyMessage(HANDLE_LOAD_NETDATA);
    }

    Handler handleLoadNetData = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLE_LOAD_NETDATA:
                    //在加载之前将FooterView先去掉
                    listView_net_music.removeFooterView(footerView);
                    loadNetData();
                    break;
            }
        }
    };


}
