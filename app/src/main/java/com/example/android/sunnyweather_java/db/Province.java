package com.example.android.sunnyweather_java.db;

import org.litepal.crud.LitePalSupport;
//省的实体类
public class Province extends LitePalSupport {
    private int id;
    private String provinceName;
    private int provinceCode; //省的代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
