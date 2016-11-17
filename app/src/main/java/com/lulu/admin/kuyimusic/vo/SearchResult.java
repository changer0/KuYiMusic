package com.lulu.admin.kuyimusic.vo;

/**
 *
 * 用来存放搜索结果的对象, 它本身也是Mp3Info
 * Created by Admin on 2016/5/24.
 */
public class SearchResult {
    private String musicName;
    private String url;
    private String artist;
    private String album;

    public SearchResult() {
    }

    public SearchResult(String musicName, String url, String artist, String album) {
        this.musicName = musicName;
        this.url = url;
        this.artist = artist;
        this.album = album;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "musicName='" + musicName + '\'' +
                ", url='" + url + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                '}';
    }
}
