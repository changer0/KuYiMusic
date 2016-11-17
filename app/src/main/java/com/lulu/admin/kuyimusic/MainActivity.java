/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lulu.admin.kuyimusic;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.astuetz.viewpager.extensions.sample.QuickContactFragment;

/**
 * 主布局在此
 */
public class MainActivity extends BaseActivity {

    private static final int MENU_I_LIKE = 0x100;
    private static final int MENU_RESENT = 0x200;
    private static final int MENU_SETTING = 0x300;
    private final Handler handler = new Handler();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;
    private MyMusicListFragment myMusicListFragment;
    private NetMusicListFragment netMusicListFragment;
    public MyPlayerAPP myApplication;


    private Drawable oldBackground = null;
    //当前的主题颜色
    public int currentColor = 0xFF3F9FE0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
        //获取自定义全局属性
        myApplication = (MyPlayerAPP) getApplication();
        //改变主题颜色
//        changeColor(currentColor);
        changeColor(myApplication.sp.getInt("currentColor", currentColor));

    }

    /**
     * 更新进度条
     *
     * @param progress
     */
    @Override
    public void publish(int progress) {

    }

    /**
     * 状态该表
     *
     * @param position
     */
    @Override
    public void change(int position) {
        if (pager.getCurrentItem() == 0) {
            //这是回调"我的音乐"中的changerUIStatusOnPlay方法更改UI界面
            myMusicListFragment.changerUIStatusOnPlay(position);
        } else if (pager.getCurrentItem() == 1) {

        }
    }

    //菜单栏设置
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
        menu.add(0, MENU_I_LIKE, 1, R.string.my_like_music);
        menu.add(0, MENU_RESENT, 2, R.string.resent_play_list);
        menu.add(0, MENU_SETTING, 3, R.string.setting);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单栏监听事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {

            case R.id.action_contact:
                QuickContactFragment dialog1 = new QuickContactFragment();
                dialog1.show(getSupportFragmentManager(), "QuickContactFragment");
                return true;
            case MENU_I_LIKE:
//                Toast.makeText(MainActivity.this, "我喜欢, 功能马上实现", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MyLikeMusicActivity.class));
                break;
            case MENU_RESENT:
//                Toast.makeText(MainActivity.this, "最近播放, 功能马上实现", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, PlayRecordListActivity.class));
                break;
            case MENU_SETTING:
                SettingThemeFragment dia = new SettingThemeFragment(this);
                //getSupportFragmentManager注意导包一定不能导入错!!!
                dia.show(getSupportFragmentManager(), "SettingThemeFragment");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeColor(int newColor) {

        tabs.setIndicatorColor(newColor);

        // change ActionBar color just if an ActionBar is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Drawable colorDrawable = new ColorDrawable(newColor);
            Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
            LayerDrawable ld = new LayerDrawable(new Drawable[]{colorDrawable, bottomDrawable});

            if (oldBackground == null) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ld.setCallback(drawableCallback);
                } else {
                    getActionBar().setBackgroundDrawable(ld);
                }

            } else {

                TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldBackground, ld});

                // workaround for broken ActionBarContainer drawable handling on
                // pre-API 17 builds
                // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    td.setCallback(drawableCallback);
                } else {
                    getActionBar().setBackgroundDrawable(td);
                }

                td.startTransition(200);

            }

            oldBackground = ld;

            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);

        }

        currentColor = newColor;

    }

    public void onColorClicked(View v) {

        int color = Color.parseColor(v.getTag().toString());
        changeColor(color);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentColor", currentColor);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentColor = savedInstanceState.getInt("currentColor");
        changeColor(currentColor);
    }

    private Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {getString(R.string.my_music),getString(R.string.net_music)};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (myMusicListFragment == null) {
                    myMusicListFragment = MyMusicListFragment.newInstance();
                }

                return myMusicListFragment;
            } else if (position == 1) {
                if (netMusicListFragment == null) {
                    netMusicListFragment = NetMusicListFragment.newInstance();
                }
                return netMusicListFragment;
            }
            return null;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //保存当前播放的一些状态, 存入到sp中
        //sp位于我们自己定义的Application中
        MyPlayerAPP app = (MyPlayerAPP) getApplication();
        SharedPreferences.Editor editor = app.sp.edit();
        editor.putInt("currentPosition", playService.getCurrentPosition());
        editor.putInt("play_mode", playService.getPlay_mode());
        editor.putInt("currentColor", currentColor);
        editor.apply();
    }
}