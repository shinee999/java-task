package com.example.myapplication.services;

import com.example.myapplication.model.LoginRequest;
import com.example.myapplication.model.LoginResponse;
import com.example.myapplication.model.Task;
import com.google.gson.JsonObject;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @GET("dev/index.php/v1/tasks/select")
    Call<List<Task>> getTasks(@Header("Authorization") String token);

    @POST("index.php/login")
    Call<LoginResponse> login(
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Body LoginRequest loginRequest
    );
}