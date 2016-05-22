package com.goodweather.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.goodweather.app.R;
import com.goodweather.app.service.AutoUpdateService;
import com.goodweather.app.util.ParseUtil;
import com.goodweather.app.util.VolleyUtil;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WeatherActivity extends AppCompatActivity implements OnClickListener {

    @BindView(R.id.switch_city)
    Button mSwitchCity;
    @BindView(R.id.city_name)
    TextView mCityName;
    @BindView(R.id.refresh_weather)
    Button mRefreshWeather;
    @BindView(R.id.publish_text)
    TextView mPublishText;
    @BindView(R.id.current_date)
    TextView mCurrentDate;
    @BindView(R.id.weather_desp)
    TextView mWeatherDesp;
    @BindView(R.id.temp1)
    TextView mTemp1;
    @BindView(R.id.temp2)
    TextView mTemp2;
    @BindView(R.id.weather_info_layout)
    LinearLayout mWeatherInfoLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.weather_layout);
        ButterKnife.bind(this);


        String weatherCode = getIntent().getStringExtra("weather_code");
        if (!TextUtils.isEmpty(weatherCode)) {
            mPublishText.setText("同步中...");
            mWeatherInfoLayout.setVisibility(View.INVISIBLE);
            mCityName.setVisibility(View.INVISIBLE);
            queryWeatherInfo(weatherCode);
        } else {
            showWeather();
        }
        mSwitchCity.setOnClickListener(this);
        mRefreshWeather.setOnClickListener(this);
    }

    //query weather with weatherCode
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://weatherapi.market.xiaomi.com/wtr-v2/weather?cityId=" + weatherCode;
        RequestQueue mRequestQueue = VolleyUtil.getRequestQueue();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(address, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("123456", response.toString());
                ParseUtil.handleWeatherResponse(WeatherActivity.this, response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPublishText.setText("同步失败");
                    }
                });
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    //read weatherInfo saved in prefs,show it on ui
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCityName.setText(prefs.getString("city_name", ""));
        mTemp1.setText(prefs.getString("temp1", ""));
        mTemp2.setText(prefs.getString("temp2", ""));
        mWeatherDesp.setText(prefs.getString("weather_desp", ""));
        mPublishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        mCurrentDate.setText(prefs.getString("current_date", ""));
        mWeatherInfoLayout.setVisibility(View.VISIBLE);
        mCityName.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


    @OnClick({R.id.switch_city, R.id.refresh_weather})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                mPublishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
        }
    }
}
