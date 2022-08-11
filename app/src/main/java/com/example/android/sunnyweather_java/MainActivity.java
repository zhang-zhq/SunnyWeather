package com.example.android.sunnyweather_java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 这里使用缓存数据，一开始就读取缓存数据，如果读取到，就说明用户已经访问过对应城市的天气，
         * 这时候就不用在让用户选择城市，直接跳转到天气页面
         * */
        /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null) != null){
            Intent intent  = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }*/
    }
}