package com.codebugfix.isparking.network;

import com.codebugfix.isparking.util.Constants;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit=null;
    public static Retrofit getClient(){
        if(retrofit== null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient())
                    .build();
            return retrofit;
        }
        return retrofit;
    }
}
