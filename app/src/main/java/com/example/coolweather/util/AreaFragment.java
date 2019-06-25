package com.example.coolweather.util;

import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.example.coolweather.MainActivity;
import com.example.coolweather.R;
import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;

import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AreaFragment extends Fragment {
    //各个级别
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private TextView titleText;
    private Button backButton;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //获取各个列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    //选择的列表
    private Province selectorProvince;
    private City selectorCity;
    //级别
    private int currentLevel;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText= view.findViewById(R.id.title_text);
        backButton=view.findViewById(R.id.back_button);
        listView=view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectorProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectorCity=cityList.get(position);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentLevel==LEVEL_COUNTY){
                    dataList.clear();
                    queryCities();

                }else if(currentLevel==LEVEL_CITY){
                    dataList.clear();
                    queryProvinces();
                }
            }
        });
        dataList.clear();
        queryProvinces();
    }

    //查询全国所有省份，先在数据库里面查询，如果没有再去服务器上查询
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            for (Province province:provinceList){
                dataList.add(province.getPrivinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel =LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china/";
            queryFromServer(address,"province");
        }
    }
    //查询省份的全部市，先在数据库里面查询，如果没有再去服务器上查询
    private void queryCities() {
        titleText.setText(selectorProvince.getPrivinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("proviceid = ?", String.valueOf(selectorProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }
        else {
            int provinceCode= selectorProvince.getProviceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    //查询市内的全部县，先在数据库里面查询，如果没有再去服务器上查询
    private void queryCounties() {
        titleText.setText(selectorCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList = DataSupport.where("cityid=?", String.valueOf(selectorCity.getId())).find(Country.class);
        if (countryList.size()>0){
            dataList.clear();
            for (Country country:countryList){
                dataList.add(country.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode= selectorProvince.getProviceCode();
            int cityCode = selectorCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    //根据上面的地址和类型在服务器上查询
    private void queryFromServer(String url,final String  type){
        HttpUtil.sentOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProviceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectorProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountry(responseText,selectorCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }
}
