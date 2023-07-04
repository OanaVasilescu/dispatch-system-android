package com.example.dispatchsystem.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://9da01187trial-dev-dispatch-system-cap-srv.cfapps.us10-001.hana.ondemand.com/app/";
    //private static final String BASE_URL = " http://110.0.2.2:8080/";
    private static RetrofitClient mInstance;
    private Retrofit retrofit;

    private RetrofitClient () {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }


    public API getAPI () {
        return retrofit.create(API.class);
    }

}
