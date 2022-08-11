package com.example.android.sunnyweather_java.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //使用该注解来将java和JSON数据对应起来
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
