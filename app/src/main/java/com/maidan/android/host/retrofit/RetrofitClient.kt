package com.maidan.android.host.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null

    val instance: Retrofit
        get(){
            if (retrofit == null){
                retrofit = Retrofit.Builder()
                        .baseUrl("http://172.12.0.49:3000/api/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return retrofit!!
        }
}