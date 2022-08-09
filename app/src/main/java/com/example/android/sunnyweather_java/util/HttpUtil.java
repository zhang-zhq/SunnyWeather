package com.example.android.sunnyweather_java.util;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    //用于发送http请求，并传入对应的地址，根据回调来处理服务器的响应，
    public static void setOkhttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address).build();
        client.newCall(request).enqueue(callback);
        Log.d("HttpUtil","网络执行了");
    }
}
