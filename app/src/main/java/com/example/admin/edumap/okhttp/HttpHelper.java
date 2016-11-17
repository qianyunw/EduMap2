package com.example.admin.edumap.okhttp;

import com.nd.slp.tp.sdk.SlpMacContent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by CongHao on 2016/9/1.
 *
 */
public class HttpHelper {
    private volatile static HttpHelper mInstance;
    private static OkHttpClient httpClient;

    private HttpHelper() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                .addInterceptor(new SdpInterceptor())
                .build();
    }

    public static HttpHelper getInstance () {
        if(mInstance == null) {
            synchronized (HttpHelper.class) {
                if(mInstance == null) {
                    mInstance = new HttpHelper();
                }
            }
        }
        return mInstance;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    final class SdpInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder();
//            builder.header(HTTP.DATE_HEADER, new Date(System.currentTimeMillis()).toString());
            String url = chain.request().url().toString();
            String method = chain.request().method();
            builder.header("Authorization", SlpMacContent.getMacContent(url, method));
            builder.method(originalRequest.method(), originalRequest.body());
            Request request = builder.build();
            Response response = chain.proceed(request);
            return response;
        }

    }
}
