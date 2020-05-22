package com.example.myapplication;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    public static String UriRes="https://beiyou.bytedance.com/";
    @GET("api/invoke/video/invoke/video")
    Call<List<VedioInfo>> getArticles();
}
