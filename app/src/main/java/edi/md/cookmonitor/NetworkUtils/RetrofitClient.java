package edi.md.cookmonitor.NetworkUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Igor on 25.11.2019
 */

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRemoteServiceClient(String url){
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build();
        return retrofit;
    }

    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(9, TimeUnit.SECONDS)
                .writeTimeout(9,TimeUnit.SECONDS)
                .build();
    }
}
