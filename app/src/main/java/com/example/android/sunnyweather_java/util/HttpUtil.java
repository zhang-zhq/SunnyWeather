package com.example.android.sunnyweather_java.util;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    //用于发送http请求，并传入对应的地址，根据回调来处理服务器的响应，
    /**
     * enqueue(callback);回调，因为服务器是耗时操作，尽量不要放在主线程中，只能开启子线程，但是子线程不能return
     * 拿不到返回的数据，因此只能定义方法，在子线程内部回调，拿到数据
     *  okhttp3.Callback callback是自带的回调接口
     *  enqueue(callback)在内部实现了子线程开启
     *  操作技术Retrofit
     * */
    public static void setOkhttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address).build();
        client.newCall(request).enqueue(callback);
        Log.d("HttpUtil","网络执行了");
    }
}
