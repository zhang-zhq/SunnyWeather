package com.example.android.sunnyweather_java;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android.sunnyweather_java.db.City;
import com.example.android.sunnyweather_java.db.Country;
import com.example.android.sunnyweather_java.db.Province;
import com.example.android.sunnyweather_java.util.HttpUtil;
import com.example.android.sunnyweather_java.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList; //省列表
    private List<City> cityList; //市列表
    private List<Country> countryList; //县列表
    /**
     * 选中的省市县
     * */
    private Province selectedProvince;
    private City selectedCity;
    private Country selectedCountry;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         //绑定对应的xml文件为碎片,这里面不在ACtivity中，只能通过先获取对应的页面view，才能获取俩面的控件
        View view = inflater.inflate(R.layout.choose_area, container, false);
        listView = (ListView) view.findViewById(R.id.list_view);
        backButton = (Button) view.findViewById(R.id.back_button);
        titleText = (TextView) view.findViewById(R.id.title_text);
        //把list集合的数据传入适配器
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /**
         * 这里使用于给ListView 设置点击事件，通常来说，在Activity中OnCreate()，执行完执行
         * */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    //通过provinceList.get(position)来获取用户点击的是哪一个省份
                    selectedProvince = provinceList.get(position);
                    queryCities();//当用户选择了对应的省份后，查询改省的地级市
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel == LEVEL_COUNTRY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
        Log.d("ChooseAreaFragment","onActivityCreated");
    }

    /**
     * 查询全国所有的省份，优先从数据库中查，查不到，再去去网络
     * */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);//将返回按键隐藏
        provinceList = LitePal.findAll(Province.class);
        //先从数据库中查询所有省份
        if(provinceList.size() > 0 ){
            dataList.clear();
            //将从数据库中查询出来的所有省份的名称添加到指定集合中
            for (Province province: provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //查询完毕将当前级别设置为省级
            currentLevel = LEVEL_PROVINCE;
            Log.d("ChooseAreaFragment","冲数据库中查省份");
        }else{
            //数据库中没有数据
            String address = "http://guolin.tech/api/china";
            Log.d("ChooseAreaFragment"," queryProvinces");
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询选择的身份的所有市优先从数据库中查，查不到，再去去网络
     * */
    private void queryCities(){
        //设置标题为省
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //根据所选的省份id查询出对应的所有地级市
        cityList = LitePal.where("provinceid =?", String.valueOf(selectedProvince.getId()))
                .find(City.class);
        Log.d("ChooseAreaFragment","queryCities()");
        if(cityList.size() > 0){
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //查询完毕将当前级别设置为省级
            currentLevel = LEVEL_CITY;
        }else{
            //如果没有查到,就去服务器查
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode ;
            queryFromServer(address,"city");
            Log.d("ChooseAreaFragment","啊啊啊");
        }
    }
    /**
     * 查询选中的市下面所有县，优先从数据库中查，查不到，再去去网络
     * */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList = LitePal.where("cityid = ?",String.valueOf(selectedCity.getId()))
                .find(Country.class);
        if(countryList.size() > 0){
            dataList.clear();
            for (Country country: countryList) {
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //查询完毕将当前级别设置为省级
            currentLevel = LEVEL_COUNTRY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"country");
        }
    }
    /**
     * 根据传入的地址和类型来从服务器上查询数据
     * */
    public void  queryFromServer(String address,  final String type){
        Log.d("ChooseAreaFragment","queryFromServer");
        showProgressDialog();//显示进度条对话框
        HttpUtil.setOkhttpRequest(address, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //获取服务器返回的数据
                Log.d("ChooseAreaFragment","onResponse");
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                     result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                     result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("country".equals(type)){
                     result = Utility.handleCountryResponse(responseText,selectedCity.getId());
                }
                Log.d("ChooseAreaFragment","string怎么说");
                //如果解析成功
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("country".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //通过runOnUiThread方法回到主线程处理逻辑
                //当请求回调结果出现异常时，执行该方法
                Log.d("ChooseAreaFragment",e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    /**
     * 显示进度条对话框
     * */
    public void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭对话框
     * */
    public void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
