package com.codebugfix.isparking.network;

import com.codebugfix.isparking.model.IsparkDetail;
import com.codebugfix.isparking.model.IsparkList;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceAPI {

    @GET("ispark/Park")
    Call<List<IsparkList>> getIspark();


    @GET("ispark/ParkDetay")
    Call<IsparkDetail> getIsparkDetail(@Query("id") int id);
}
