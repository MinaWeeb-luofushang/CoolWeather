package com.example.coolweather.db;


import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {
    private int id;
    private String privinceName;
    private int proviceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPrivinceName() {
        return privinceName;
    }

    public void setPrivinceName(String privinceName) {
        this.privinceName = privinceName;
    }

    public int getProviceCode() {
        return proviceCode;
    }

    public void setProviceCode(int proviceCode) {
        this.proviceCode = proviceCode;
    }
}
