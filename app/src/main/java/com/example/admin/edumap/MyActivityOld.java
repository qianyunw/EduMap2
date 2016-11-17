package com.example.admin.edumap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.android.smartcan.network.Method;
import com.nd.slp.tp.sdk.LoginActivity;
import com.nd.slp.tp.sdk.SlpLoginCallback;
import com.nd.smartcan.accountclient.UCManager;
import com.nd.smartcan.accountclient.core.AccountException;
import com.nd.smartcan.accountclient.core.User;
import com.nd.smartcan.core.security.SecurityDelegate;

import java.util.ArrayList;
import java.util.List;

public class MyActivityOld extends AppCompatActivity{

    private List<Item> itemList = new ArrayList<Item>();
    private static final String TAG = "MyEduMap";
    private static final int REQUEST_CODE_LOGIN = 1;
    private static Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_my);

        initItems();
        ItemAdapter adapter = new ItemAdapter (MyActivityOld.this, R.layout.my_item, itemList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        LinearLayout login = (LinearLayout) findViewById(R.id.background);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyActivityOld.this, TestActivity.class);
                startActivity(intent);
            }
        });


        appContext = this.getApplicationContext();
        //判断是否需要登录
        if(UCManager.getInstance().getCurrentUser() != null) { //用户已登录
            startActivity(new Intent(this, TestActivity.class));
            finish();
        } else {
            //以startActivityForResult方式调用LoginActivity
            LoginActivity.launchForResult(this, REQUEST_CODE_LOGIN);

            SlpLoginCallbackImpl slpLoginCallback = new SlpLoginCallbackImpl();
//            LoginActivity.launchForResult(this, REQUEST_CODE_LOGIN, slpLoginCallback);

            /*
            LoginActivity.launchForResult(this, REQUEST_CODE_LOGIN);
            LoginActivity.registerLoginTask(new LoginTask() {
                @Override
                public void doSth() {
                    Log.d(TAG, "登录成功，通知回调！");
                }
            });
            */
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


    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "CurrentUserId = " + UCManager.getInstance().getCurrentUserId());
            new MyThread().start();
            startActivity(new Intent(this, TestActivity.class));
            finish();
        } else if (resultCode == Activity.RESULT_CANCELED){
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyThread extends Thread {
        public void run() {
            try {
                //获取用户信息，不能在UI线程中调用该方法。
                User user = UCManager.getInstance().getCurrentUser().getUserInfo();
                Log.d(TAG, "login name = " + user.getOrgExInfo().get("org_user_code"));
                String token= SecurityDelegate.getInstance().calculateMACContent(Method.GET, "http://fepapi.beta.web.sdp.101.com", "/v1/commonapi/get_codes", false);
                Log.d(TAG, "token = " + token);

            } catch (AccountException e) {
                e.printStackTrace();
            }
        }
    }

    static class SlpLoginCallbackImpl implements SlpLoginCallback {
        @Override
        public void onSuccessful() {
            Log.d(TAG, "SlpLoginCallbackImpl.onSuccessful() 登录成功回调。");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        Intent receiverIntent = new Intent("com.nd.slp.tp.login.callback.receiver");
                        receiverIntent.putExtra("result", true);
                        appContext.sendBroadcast(receiverIntent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onError() {
            Log.d(TAG, "SlpLoginCallbackImpl.onError() 登录失败回调。");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        Intent receiverIntent = new Intent("com.nd.slp.tp.login.callback.receiver");
                        receiverIntent.putExtra("result", false);
                        receiverIntent.putExtra("errorMessage", "SlpLoginCallback，登录失败！");
                        appContext.sendBroadcast(receiverIntent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
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
        ActivityCollector.removeActivity(this);
    }
}
