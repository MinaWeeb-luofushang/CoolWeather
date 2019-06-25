package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    public String date;
    @SerializedName("cond")
    public More mored;
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
    @SerializedName("tmp")
    public Temperature temperature;
    public class Temperature{
        @SerializedName("max")
        public String max;
        @SerializedName("min")
        public String min;
    }
}
