package com.example.android.sunnyweather_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.sunnyweather_java.gson.Basic;
import com.example.android.sunnyweather_java.gson.Forecast;
import com.example.android.sunnyweather_java.gson.Weather;
import com.example.android.sunnyweather_java.service.AutoUpdateService;
import com.example.android.sunnyweather_java.util.HttpUtil;
import com.example.android.sunnyweather_java.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 将解析到实体类的天气数据，在活动中获取，并展示到界面上
 * */
public class WeatherActivity extends AppCompatActivity {
    private ImageView bingInc;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private Button navButton;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //将状态栏隐藏
        if(Build.VERSION.SDK_INT > 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        bingInc = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_text);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.teal_700);
        //每个应用都有一个默认的配置文件，使用getDefaultSharedPreferences来获取
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //读取配置文件中天气所对应的值，没有则为null
        String weatherString = prefs.getString("weather", null);
        final  String weatherId;
        if(weatherString != null){
            //读到了数据，则直接进行解析,存入Weather对象
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //没有缓存就去请求服务器获取数据
           weatherId = getIntent().getStringExtra("weather_id");
            //滚动显示页面控件显示为不可见
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        //加载背景图片
        String imageBack = prefs.getString("bing_sc",null);
        if(imageBack != null){
            Glide.with(this).load(imageBack).into(bingInc);
        }else{
            loadBingPic();
        }
        navButton = (Button) findViewById(R.id.nav_button);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //开启滑动菜单
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }
    /**
     * 请求服务器，获取天气信息
     * */
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=22f29af884574389aab843837a80059d";
        HttpUtil.setOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气消息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //从服务端获取消息，并转成字符串
                final String responseText = response.body().string();
                //通过解析将数据封装到对应的实体类
               final Weather weather = Utility.handleWeatherResponse(responseText);
               //开启子线程向主线程切换
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //解析数据成功，weather中有数据
                        if(weather != null && "ok".equals(weather.status)){
                            /**
                             * //将从服务器中返回的字符串存入本地配置文件中，第二次访问时直接从配置文件中读取数据并解析，
                             * 不用再请求网络
                             * */
                          SharedPreferences.Editor editor =
                                  PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                          editor.putString("weather",responseText);
                          editor.apply();
                          //我的理解是展示到页面
                          showWeatherInfo(weather);
                          Intent intent =new Intent(WeatherActivity.this, AutoUpdateService.class);
                          startService(intent);
                        }else{
                            Log.d("Tag",weather.status);
                            Toast.makeText(WeatherActivity.this,"没有获取天气信息，为空",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 处理并展示weather实体类中的信息
     * */
    private void showWeatherInfo(Weather weather){
        // 先获取城市的名称
        String cityName = weather.basic.cityName;
        //在获取更新事件
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        //获取当前的温度
        String degree = weather.now.temperature + "^C";
        //获取天气通知
        String weatherInfo = weather.now.more.info;
        //设置在界面中显示
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //这个是什么意思？
        forecastLayout.removeAllViews();
        for (Forecast forecast: weather.forecastList) {
            //将子项页面加载进当前的布局
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String cashWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(cashWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 加载天气界面的背景图片
     * */
    public void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.setOkhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String imageResponse = response.body().string();
                //这里把图片存入持久化
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_sc",imageResponse);
                editor.apply();
                //这里不需要解析
                //开启对界面的设置，传递参数
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(imageResponse).into(bingInc);
                    }
                });
            }
        });
    }
}