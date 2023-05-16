package com.example.dispatchsystem.api;

import com.example.dispatchsystem.model.ArduinoData;
import com.example.dispatchsystem.model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface API {
    @POST("arduinoData")
    Call<ResponseBody> sendArduinoData (
            @Body ArduinoData data
    );

    @POST("user/login")
    Call<User> checkUser (
            @Body User user
    );

    @POST("user")
    Call<ResponseBody> createUser(
            @Body User user
    );

}
