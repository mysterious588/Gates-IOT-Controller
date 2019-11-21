package com.IOT.Gates;

import com.IOT.Gates.Models.fcm.FirebaseCloudMessage;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface API {

    @POST("send")
    Call<ResponseBody> sendNotification(
            @HeaderMap Map<String, String> headers,
            @Body FirebaseCloudMessage message
            );
}
