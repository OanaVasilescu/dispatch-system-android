package com.example.dispatchsystem.api;

import com.example.dispatchsystem.model.ArduinoData;
import com.example.dispatchsystem.model.Credentials;
import com.example.dispatchsystem.model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface API {
    @POST("arduinoData")
    Call<ResponseBody> sendArduinoData (
            @Body ArduinoData data
    );

    @POST("login")
    Call<User> checkUser (
            @Body Credentials credentials
    );


    @POST("register")
    Call<ResponseBody> createUser(
            @Body User user
    );

}
