package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class VedioInfo {
    @SerializedName("_id")
    public String id;
    @SerializedName("feedurl")
    public String feedurl;
    @SerializedName("nickname")
    public String nickname;
    @SerializedName("description")
    public String description;
    @SerializedName("likecount")
    public int likecount;
    @SerializedName("avatar")
    public String avatar;

    public String getId() {
        return id;
    }

    public String getFeedurl() {
        return feedurl;
    }

    public String getNickname() {
        return nickname;
    }

    public String getDescription() {
        return description;
    }

    public int getLikecount() {
        return likecount;
    }

    public String getAvatar() {
        return avatar;
    }
}
