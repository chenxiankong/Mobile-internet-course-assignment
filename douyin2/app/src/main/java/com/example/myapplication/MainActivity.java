package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity  {
    private ViewPager2 viewPager2 ;
    private ViewPagerAdapter viewPagerAdapter;
    private RecyclerView mViewPagerImpl; //ViewPage2内部是通过RecyclerView去实现的，它位于ViewPager2的第0个位置
    public List<VedioInfo> list_source;//视频等资源列表
    private ExecutorService executorService; //线程池

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_main);
        //获取数据
        list_source=new ArrayList<VedioInfo>();
        getData();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init()  {
        //线程池初始化
        executorService = Executors.newFixedThreadPool(20);//线程池初始化
        //资源列表初始化
        viewPager2 = findViewById(R.id.viewpager2);
        viewPagerAdapter = new ViewPagerAdapter(this,viewPager2,list_source,list_source.size());//适配器初始化
        viewPager2.setAdapter(viewPagerAdapter);//适配器绑定
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);//设置viewpager2滑动方向
        viewPager2.setOffscreenPageLimit(5);//设置viewPager2缓冲数目

        //viewPager2滑动事件监听，处理滑动时视频的播放暂停
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int old_position=-1;//记录当前页的前一个位置
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                //无限下滑功能
                    //每次item数量最大值增加当前的一半
                if(position==ViewPagerAdapter.ItemCount-1){
                    ViewPagerAdapter.ItemCount+=ViewPagerAdapter.ItemCount;
                }
                //将播放任务提交到线程池
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        viewPager2.post(new Runnable() {
                            @Override
                            public void run() {
                                startPlay(position,old_position);
                                old_position=position;
                            }
                        });
                    }
                });
            }
        });
        //ViewPage2内部是通过RecyclerView去实现的，它位于ViewPager2的第0个位置
        mViewPagerImpl = (RecyclerView) viewPager2.getChildAt(0);
    }
    //处理当滑动到新的item页面时，item的动作事件，（当前Item播放，其他Item暂停）
    private void startPlay(int position,int old_position) {
        //获取Viewpager2子item数量
        int count = mViewPagerImpl.getChildCount();
        for (int i = 0; i < count; i++) {
            //如果当前item 的mPosition与当前选择页的position相同，则为要寻找的页
            View itemView = mViewPagerImpl.getChildAt(i);
            final ViewPagerAdapter.ViewPagerViewHolder viewHolder = ( ViewPagerAdapter.ViewPagerViewHolder) itemView.getTag();
            //当前item页播放，当前页的前一页暂停
            if(viewHolder.mPosition==position){
                Log.d("TAG", "startPlay: "+String.valueOf(position)+"   "+String.valueOf(count));
                viewHolder.mImage_pause.setVisibility(View.INVISIBLE);
                viewHolder.iv_cover.setVisibility(View.INVISIBLE);
                viewHolder.mVideoview.start();
                viewHolder.handler.postDelayed(viewHolder.runnable,0);
                if(old_position<position) break;
                continue;
            }
            if(viewHolder.mPosition==old_position){
                viewHolder.mVideoview.pause();
                viewHolder.handler.removeCallbacks(viewHolder.runnable);
                viewHolder.mImage_pause.setVisibility(View.VISIBLE);
                if(old_position>position) break;
                continue;
            }
        }
    }

    private void getData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.UriRes)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getArticles().enqueue(new Callback<List<VedioInfo>>() {
            @Override
            public void onResponse(Call<List<VedioInfo>> call, Response<List<VedioInfo>> response) {
                if (response.body() != null) {
                    for(int i=0;i<response.body().size();i++)
                    {
                        VedioInfo info=new VedioInfo();
                        info.id=response.body().get(i).id;
                        info.feedurl=response.body().get(i).feedurl;
                        info.nickname=response.body().get(i).nickname;
                        info.description=response.body().get(i).description;
                        info.likecount=response.body().get(i).likecount;
                        info.avatar=response.body().get(i).avatar;
                        Gson gson = new Gson();
                        String s = gson.toJson(info);
                        list_source.add(info);
                    }
                    init();//取得数据在进行viewpager2的初始化
                }
            }
            @Override
            public void onFailure(Call<List<VedioInfo>> call, Throwable t) {
            }
        });
    }
}
