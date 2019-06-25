package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    /**
     * 处理服务器返回的省级数据
     * */
    public static boolean handleProviceResponse(String response){
        //新的判断方式为null或者为0
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvice = new JSONArray(response);
                for (int i=0;i<allProvice.length();i++){
                    JSONObject jsonObject = allProvice.getJSONObject(i);
                    Province province = new Province();
                    province.setPrivinceName(jsonObject.getString("name"));
                    province.setProviceCode(jsonObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 处理服务器返回的市级数据
     * */
    public static boolean handleCityResponse(String response,int proviceId) {
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i=0;i<allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProviceId(proviceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理服务器返回的县级数据
     * */
    public static boolean handleCountry(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                System.out.println(allCounties);
                for (int i=0;i<allCounties.length();i++){
                    JSONObject countObject = allCounties.getJSONObject(i);
                    Country country = new Country();
                    country.setCountyName(countObject.getString("name"));
                    country.setWeatherId(countObject.getString("weather_id"));
                    country.setCityId(cityId);
                    country.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析json转为实体类
     * */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
