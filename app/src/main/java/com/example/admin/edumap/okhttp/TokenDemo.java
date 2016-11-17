package com.example.admin.edumap.okhttp;

import android.util.Log;
import com.nd.smartcan.core.security.SecurityDelegate;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by CongHao on 2016/9/7.
 *
 */
public class TokenDemo {
    public TokenDemo(final String url, final int method) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String host = URI.create(url).getAuthority();
                String uri = getURI(host, url);
                String strMACContent = SecurityDelegate.getInstance().calculateMACContent(method, host, uri, false);
                try {
                    JSONObject jsonObject = new JSONObject(strMACContent);
                    strMACContent = " MAC id=\"" + jsonObject.optString("access_token")
                            + "\",nonce=\"" + jsonObject.optString("nonce")
                            + "\",mac=\"" + jsonObject.optString("mac") + "\"";
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    Request request = new Request.Builder().addHeader("Authorization", strMACContent).url(url).build();
                    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("TokenDemo", e.getMessage());
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String strBody = response.body().string();
                            Log.d("TokenDemo", strBody);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    public static String getURI(String host, String uri) {
        if (host != null && host.length() >= 0) {
            int index = uri.indexOf(host);
            return index == -1 ? "" : uri.substring(index + host.length());
        } else {
            return "";
        }
    }
}
