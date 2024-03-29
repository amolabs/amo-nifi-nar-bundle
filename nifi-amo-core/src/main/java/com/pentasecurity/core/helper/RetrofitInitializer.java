package com.pentasecurity.core.helper;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInitializer {
    private static final Retrofit retrofitAmoStorage = new Retrofit.Builder()
            .baseUrl("http://127.0.0.1:9090/api/v1/")
//            .baseUrl("https://storage.market.amolabs.io/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final Retrofit retrofitAmoChain = new Retrofit.Builder()
            .baseUrl("http://172.105.213.114:26657")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final Retrofit retrofitMarket = new Retrofit.Builder()
            .baseUrl("http://localhost:8080/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private RetrofitInitializer(){}

    public static Retrofit getRetrofitAmoStorage() {
        return retrofitAmoStorage;
    }
    public static Retrofit getRetrofitAmoChain() {
        return retrofitAmoChain;
    }
    public static Retrofit getRetrofitMarket() {
        return retrofitMarket;
    }
}
