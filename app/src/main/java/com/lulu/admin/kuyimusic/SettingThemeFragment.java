package com.lulu.admin.kuyimusic;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

/**
 * Created by Admin on 2016/5/22.
 */
public class SettingThemeFragment extends DialogFragment implements View.OnClickListener {

    private MainActivity mainActivity;
    private ImageView imageView_red,
            imageView_green,
            imageView_blue,
            imageView_default;

    public SettingThemeFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public SettingThemeFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //将默认的Dialog的title去掉
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting_theme, container, false);
        imageView_blue = (ImageView) v.findViewById(R.id.imageView_blue);
        imageView_red = (ImageView) v.findViewById(R.id.imageView_red);
        imageView_green = (ImageView) v.findViewById(R.id.imageView_green);
        imageView_default = (ImageView) v.findViewById(R.id.imageView_default);

        imageView_blue.setOnClickListener(this);
        imageView_red.setOnClickListener(this);
        imageView_green.setOnClickListener(this);
        imageView_default.setOnClickListener(this);
        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView_blue:
                mainActivity.changeColor(0xFF0000FF);
                mainActivity.currentColor = 0xFF0000FF;
                break;
            case R.id.imageView_red:
                mainActivity.changeColor(0xFFFF0000);
                mainActivity.currentColor = 0xFFFF0000;
                break;
            case R.id.imageView_green:
                mainActivity.changeColor(0xFF00FF00);
                mainActivity.currentColor = 0xFF00FF00;
                break;
            default:
                mainActivity.changeColor(0xFF3F9FE0);
                mainActivity.currentColor = 0xFF3F9FE0;
                break;


        }
    }
}
