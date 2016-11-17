package com.lulu.admin.kuyimusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lulu.admin.kuyimusic.R;
import com.lulu.admin.kuyimusic.vo.SearchResult;

import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by Admin on 2016/5/25.
 */
public class NetMusicAdapter extends BaseAdapter {

    private ArrayList<SearchResult> searchResults;
    private Context context;

    public NetMusicAdapter(Context context, ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
        this.context = context;
    }

    public NetMusicAdapter(Context context) {
        this.context = context;
    }

    public ArrayList<SearchResult> getSearchResults(){
        return searchResults;
    }

    public void setSearchResults(ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    @Override
    public int getCount() {
        return searchResults.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView textView_title;
        TextView textView_singer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.net_item_music_list, null);
            vh = new ViewHolder();
            vh.textView_title = (TextView) convertView.findViewById(R.id.textView_title);
            vh.textView_singer = (TextView) convertView.findViewById(R.id.textView_singer);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        vh.textView_title.setText(searchResults.get(position).getMusicName());
        vh.textView_singer.setText(searchResults.get(position).getArtist());

        return convertView;
    }
}
