package com.lulu.admin.kuyimusic;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.lulu.admin.kuyimusic.MainActivity;
import com.lulu.admin.kuyimusic.R;
import com.lulu.admin.kuyimusic.vo.SearchResult;

/**
 * Created by Admin on 2016/5/27.
 */
public class DownloadDialogFragment extends DialogFragment {

    private SearchResult searchResult;
    private MainActivity mainActivity;


    public DownloadDialogFragment() {
    }

    public DownloadDialogFragment(SearchResult searchResult) {
        //当前要下载的对象
        this.searchResult = searchResult;
    }

    private String[] items;

    //获取到MainActivity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        items = new String[]{getString(R.string.download), getString(R.string.cancel)};
    }

    //创建对话框
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);//返回键可取消

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    //下载
                    case 0:
                        //执行下载方法
                        downloadMusic();
                        break;
                    //取消
                    case 1:
                        dialog.dismiss();
                        break;
                }


            }


        });

        return builder.show();
    }

    //下载音乐
    private void downloadMusic() {
        Toast.makeText(mainActivity, "正在下载" + searchResult.getMusicName(), Toast.LENGTH_SHORT).show();
        //用DowloadUtils完成下载功能


    }








}
