
package com.example.admin.edumap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.admin.edumap.okhttp.HttpHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nd.slp.tp.sdk.LoginActivity;
import com.nd.smartcan.accountclient.CurrentUser;
import com.nd.smartcan.accountclient.UCManager;
import com.nd.smartcan.accountclient.core.AccountException;
import com.nd.smartcan.accountclient.core.User;
import com.nd.smartcan.core.restful.ClientResource;
import com.nd.smartcan.core.restful.ResourceException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class TestActivityOld extends ActionBarActivity {
    private static final String TAG = TestActivityOld.class.getName();
    private static final String BUNDLE_KEY_RESULT = "RESULT";
    private static final int HANDLER_MSG_KEY_REQUEST = 10;
    private static final int HANDLER_MSG_KEY_LOGOUT = 11;
    private MyHandler myHandler;
    private EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CurrentUser currentUser = UCManager.getInstance().getCurrentUser();
        User user = currentUser.getUser();


        editText = (EditText) findViewById(R.id.edit_text);

        myHandler = new MyHandler(this);

        new UserInfoThread().start();

    }

    public void onClickLogout(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //不能在UI线程中调用logout方法。
                    UCManager.getInstance().logout(TestActivityOld.this);
                    TestActivityOld.this.myHandler.sendMessage(TestActivityOld.this.myHandler.obtainMessage(HANDLER_MSG_KEY_LOGOUT));
                } catch (AccountException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void clientResourceGet(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ClientResource clientResource = new ClientResource(RequestService.getUserInfo());
                    Bundle bundle = new Bundle();
                    bundle.putString(BUNDLE_KEY_RESULT, clientResource.get());

                    Message message = new Message();
                    message.what = HANDLER_MSG_KEY_REQUEST;
                    message.setData(bundle);
                    TestActivityOld.this.myHandler.sendMessage(message);
                } catch (ResourceException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void requestByOkHttp3(View view) {
        try {
            Request request = new Request.Builder().url(RequestService.getPapers()).build();
            HttpHelper.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String strBody = response.body().string();
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            editText.setText(strBody);
                        }
                    });
                    Log.d(TAG, strBody);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG, "unregisterInvalidTokenListener");
        super.onDestroy();
    }

    class UserInfoThread extends Thread {
        public void run() {
            try {
                //获取用户信息，不能在UI线程中调用该方法。
                User user = UCManager.getInstance().getCurrentUser().getUserInfo();
                CurrentUser currentUser = UCManager.getInstance().getCurrentUser();
                user = currentUser.getUser();
                Map map = user.getOrgExInfo();
                Log.d(TAG, (String)map.get("org_user_code"));

            } catch (AccountException e) {
                e.printStackTrace();
            }
        }
    }

    static class MyHandler extends Handler {
        WeakReference<TestActivityOld> mActivity;
        MyHandler(TestActivityOld activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            TestActivityOld theActivity = mActivity.get();
            if(theActivity == null) return;
            theActivity.editText.setText(msg.getData().getString(BUNDLE_KEY_RESULT));
            switch (msg.what) {
                case HANDLER_MSG_KEY_REQUEST:

                    Gson gson = new Gson();
                    Map result = gson.fromJson(msg.getData().getString(BUNDLE_KEY_RESULT), new TypeToken<Map>(){}.getType());
                    System.out.println(result.get("nick_name").toString());
                    theActivity.editText.setText(msg.getData().getString(BUNDLE_KEY_RESULT));
                    break;
                case HANDLER_MSG_KEY_LOGOUT:
                    LoginActivity.launch(theActivity, TestActivityOld.class);
                    theActivity.finish();
                    break;
                default:
                    break;
            }
        }
    }

//
//    //解析Json数据
//    private static ArrayList<Map> parseJSONWithJOSNObject(String jsonData) {
//        Gson gson = new Gson();
//        Map map = gson.fromJson(jsonData, new TypeToken<Map>(){}.getType());
//        ArrayList<Map> result = (ArrayList<Map>)map.get("result");
//        return result;
//    }

}