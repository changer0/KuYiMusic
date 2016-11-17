package com.lulu.admin.kuyimusic.adapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lulu.admin.kuyimusic.R;
import com.lulu.admin.kuyimusic.utils.MediaUtils;
import com.lulu.admin.kuyimusic.vo.Mp3Info;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Admin on 2016/5/7.
 */
public class MyMusicListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Mp3Info> mp3Infos;

    public MyMusicListAdapter(Context context, ArrayList<Mp3Info> mp3Infos) {
        this.context = context;
        this.mp3Infos = mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

       @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    private int flag = 1;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_music_list, null);
            vh = new ViewHolder();
            vh.textView_title = (TextView) convertView.findViewById(R.id.textView_title);
            vh.textView_singer = (TextView) convertView.findViewById(R.id.textView_singer);
            vh.textView_time = (TextView) convertView.findViewById(R.id.textView_time);
            vh.imageView_icon = (ImageView) convertView.findViewById(R.id.imageView_icon);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        Mp3Info mp3Info = mp3Infos.get(position);
        vh.textView_title.setText(mp3Info.getTitle());
        vh.textView_singer.setText(mp3Info.getAlbum());
        vh.textView_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        Bitmap albumBitmap = MediaUtils.getArtwork(context, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
        if(albumBitmap != null){
            vh.imageView_icon.setImageBitmap(albumBitmap);
        }


        return convertView;
    }

    static class ViewHolder {
        TextView textView_title;
        TextView textView_singer;
        TextView textView_time;
        ImageView imageView_icon;
    }
}
