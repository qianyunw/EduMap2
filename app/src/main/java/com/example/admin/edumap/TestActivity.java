
package com.example.admin.edumap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.edumap.constant.CurrentAdmin;
import com.example.admin.edumap.constant.CurrentLocationSingleton;
import com.example.admin.edumap.okhttp.HttpHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nd.android.smartcan.network.Method;
import com.nd.slp.tp.sdk.LoginActivity;
import com.nd.slp.tp.sdk.SlpLoginCallback;
import com.nd.smartcan.accountclient.CurrentUser;
import com.nd.smartcan.accountclient.UCManager;
import com.nd.smartcan.accountclient.core.AccountException;
import com.nd.smartcan.accountclient.core.User;
import com.nd.smartcan.core.restful.ClientResource;
import com.nd.smartcan.core.restful.ResourceException;
import com.nd.smartcan.core.security.SecurityDelegate;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class TestActivity extends ActionBarActivity {
    private List<Item> itemList = new ArrayList<Item>();
    private static final String TAG = TestActivity.class.getName();
    private static final String BUNDLE_KEY_RESULT = "RESULT";
    private static final int HANDLER_MSG_KEY_REQUEST = 10;
    private static final int HANDLER_MSG_KEY_LOGOUT = 11;
    private MyHandler myHandler;
    private TextView editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_my_old);

        initItems();
        ItemAdapter adapter = new ItemAdapter (TestActivity.this, R.layout.my_item, itemList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        CurrentUser currentUser = UCManager.getInstance().getCurrentUser();
        User user = currentUser.getUser();

        Button logout = (Button) findViewById(R.id.logout);
        logout.setVisibility(View.VISIBLE);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //不能在UI线程中调用logout方法。
                            UCManager.getInstance().logout(TestActivity.this);
                            TestActivity.this.myHandler.sendMessage(TestActivity.this.myHandler.obtainMessage(HANDLER_MSG_KEY_LOGOUT));
                        } catch (AccountException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });


        editText = (TextView) findViewById(R.id.text_admin);

        myHandler = new MyHandler(this);

        new UserInfoThread().start();

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
                    TestActivity.this.myHandler.sendMessage(message);
                } catch (ResourceException e) {
                    e.printStackTrace();
                }
            }
        }).start();



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
        WeakReference<TestActivity> mActivity;
        MyHandler(TestActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            TestActivity theActivity = mActivity.get();
            if(theActivity == null) return;
            theActivity.editText.setText(msg.getData().getString(BUNDLE_KEY_RESULT));
            switch (msg.what) {
                case HANDLER_MSG_KEY_REQUEST:
                    Gson gson = new Gson();
                    Map result = gson.fromJson(msg.getData().getString(BUNDLE_KEY_RESULT), new TypeToken<Map>(){}.getType());
                   // System.out.println(result.get("nick_name").toString());
                    theActivity.editText.setText(result.get("nick_name").toString());
                    CurrentAdmin.getInstance().setCurrentAdmin(result.get("nick_name").toString());
                    break;
                case HANDLER_MSG_KEY_LOGOUT:
                    LoginActivity.launch(theActivity, TestActivity.class);
                    theActivity.finish();
                    break;
                default:
                    break;
            }
        }
    }


    private void initItems(){
        Item location = new Item ("我的地址", R.drawable.icon_location);
        itemList.add(location);
        Item district = new Item ("所属学区", R.drawable.icon_district);
        itemList.add(district);
        Item policy = new Item ("学区政策", R.drawable.icon_policy);
        itemList.add(policy);
        Item history = new Item ("浏览历史", R.drawable.icon_history);
        itemList.add(history);
    }


    public class Item {

        private String name;
        private int ImageId;

        public Item(String name, int ImageId) {
            this.ImageId = ImageId;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getImageId(){
            return ImageId;
        }
    }

    public class ItemAdapter extends ArrayAdapter<Item> {

        private int resourceId;

        public ItemAdapter (Context context, int textViewRescouceId, List<Item> objects) {
            super(context, textViewRescouceId, objects);
            resourceId = textViewRescouceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = getItem(position);
            View view;
            ViewHolder viewHolder;
            if(convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceId, null);
                viewHolder = new ViewHolder();
                viewHolder.itemImage = (ImageView) view.findViewById(R.id.my_image);
                viewHolder.itemName = (TextView) view.findViewById(R.id.my_name);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.itemImage.setImageResource(item.getImageId());
            viewHolder.itemName.setText(item.getName());
            return view;
        }
        class ViewHolder {
            ImageView itemImage;
            TextView itemName;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "unregisterInvalidTokenListener");
        ActivityCollector.removeActivity(this);
    }

}