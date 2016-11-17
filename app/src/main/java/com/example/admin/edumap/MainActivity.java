package com.example.admin.edumap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;
import com.example.admin.edumap.adapter.ViewPagerAdapter;
import com.example.admin.edumap.constant.CurrentLocationSingleton;
import com.example.admin.edumap.constant.UrlConstant;
import com.example.admin.edumap.fragment.FirstFragment;
import com.example.admin.edumap.fragment.MoreFragment;
import com.example.admin.edumap.fragment.SecondFragment;
import com.example.admin.edumap.fragment.ThirdFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nd.smartcan.accountclient.UCManager;

import org.apache.log4j.chainsaw.Main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@SuppressWarnings("ResourceAsColor")
public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {

    private static int result;
    private static int x;
    private static int y;
    private static int imageWidth;
    private static int imageHeight;
    private static int bitmapWidth;
    private static int bitmapHeight;
    private static double scaleWidth;
    private static double scaleHeight;
    private static ImageView imageBeijing;
    private static ImageView alternative;
    private static int[] pic;
    private static Bitmap bitmap;
    private static Bitmap[] district;
    private static String[] stringDistricts;
    private TextView currentLocationText;
    private String currentDistrict;
    private double currentLat;
    private double currentLng;
    public static final int SHOW_LOCATION = 1;
    private AMapLocationClient mlocationClient;
    private OnLocationChangedListener mListener;
    private AMapLocationClientOption mLocationOption;
    private BottomSheetBehavior mBottomSheetBehavior;
    private ViewPager viewPager;
    private TextView firstTabTextView;
    private RelativeLayout upLayout;
    private static Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_main);


        upLayout = (RelativeLayout)findViewById(R.id.upLayout);
        upLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    upLayout.setVisibility(View.GONE);
                }
                return true;
            }
        });

        View bottomSheet = findViewById( R.id.main_bottom_sheet );
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        viewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(5);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(viewPager);

        View view = View.inflate(MainActivity.this, R.layout.custom_tab_layout, null);
        tabLayout.getTabAt(0).setCustomView(view);
        firstTabTextView = (TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.custom_tab_textview);

        //tabLayout.setSelected(false);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                upLayout.setVisibility(View.VISIBLE);
                if(tab.getPosition() == 0)
                    firstTabTextView.setTextColor(getResources().getColor(R.color.accent));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                if(tab.getPosition() == 0)
                    firstTabTextView.setTextColor(getResources().getColor(R.color.black));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                upLayout.setVisibility(View.VISIBLE);
                if(tab.getPosition() == 0)
                    firstTabTextView.setTextColor(getResources().getColor(R.color.accent));
            }
        });

        /* 对上半部分的布局，加上这个监听器即可 */

        ImageView re = (ImageView)findViewById(R.id.img_return);
        re.setVisibility(View.INVISIBLE);
        currentLocationText = (TextView)findViewById(R.id.current_location);

        //声明mLocationOption对象
        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //Hight_Accuracy设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(50000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();


        imageBeijing = (ImageView) findViewById(R.id.image_beijing);
        alternative = (ImageView) findViewById(R.id.alternative);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.beijing, options);
        //options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        options.inSampleSize = calculateInSampleSize(options, 15, 15);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beijing, options);
        result = -1;

        pic = new int [16];
        pic[0] = R.drawable.changping;
        pic[1] = R.drawable.daxing;
        pic[2] = R.drawable.fangshan;
        pic[3] = R.drawable.huairou;
        pic[4] = R.drawable.mentougou;
        pic[5] = R.drawable.miyun;
        pic[6] = R.drawable.pinggu;
        pic[7] = R.drawable.shunyi;
        pic[8] = R.drawable.tongzhou;
        pic[9] = R.drawable.yanqing;
        pic[10] = R.drawable.chaoyang;
        pic[11] = R.drawable.dongcheng;
        pic[12] = R.drawable.fengtai;
        pic[13] = R.drawable.haidian;
        pic[14] = R.drawable.shijingshan;
        pic[15] = R.drawable.xicheng;

        district = new Bitmap [16];
        for(int i = 0; i < 16; i++) {
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), pic[i], options2);
            //options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options2.inSampleSize = calculateInSampleSize(options2, 15, 15);
            options2.inJustDecodeBounds = false;
            district[i] = BitmapFactory.decodeResource(getResources(), pic[i], options2);
        }

        stringDistricts = new String[16];
        stringDistricts[0] = "昌平区";
        stringDistricts[1] = "大兴区";
        stringDistricts[2] = "房山区";
        stringDistricts[3] = "怀柔区";
        stringDistricts[4] = "门头沟区";
        stringDistricts[5] = "密云区";
        stringDistricts[6] = "平谷区";
        stringDistricts[7] = "顺义区";
        stringDistricts[8] = "通州区";
        stringDistricts[9] = "延庆区";
        stringDistricts[10] = "朝阳区";
        stringDistricts[11] = "东城区";
        stringDistricts[12] = "丰台区";
        stringDistricts[13] = "海淀区";
        stringDistricts[14] = "石景山区";
        stringDistricts[15] = "西城区";

        //top = imageBeijing.getBottom(); 注：必须写在oncreat后面才不为0
        //left = imageBeijing.getRight();X

        imageBeijing.setOnClickListener(new clickListener());
        imageBeijing.setOnTouchListener(new touchListener());

        FloatingActionButton my = (FloatingActionButton) findViewById(R.id.my);
        my.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                appContext = MainActivity.this.getApplicationContext();
                //判断是否需要登录
                if(UCManager.getInstance().getCurrentUser() != null) { //用户已登录
                    Intent intent = new Intent(MainActivity.this, TestActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(MainActivity.this, MyActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //adapter.getItem(-1);
        adapter.addFrag(new FirstFragment(getResources().getColor(R.color.light)), "权威发布");
        adapter.addFrag(new SecondFragment(getResources().getColor(R.color.light)), "教育资源");
        adapter.addFrag(new ThirdFragment(getResources().getColor(R.color.light)), "教学质量");
        adapter.addFrag(new MoreFragment(getResources().getColor(R.color.light)), "…更多");
        viewPager.setAdapter(adapter);
    }


    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                currentLng = amapLocation.getLongitude();
                currentLat = amapLocation.getLatitude();
                CurrentLocationSingleton.getInstance().setCurrentLatLon(amapLocation.getLatitude(),amapLocation.getLongitude());
                System.out.println(CurrentLocationSingleton.getInstance().getCurrentLat());
                System.out.println( CurrentLocationSingleton.getInstance().getCurrentLon());

                currentDistrict = amapLocation.getDistrict();//城区信息

                new Thread (new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection connection = null;
                        try {
                            URL url = new URL(UrlConstant.BASE_URL+"search?lng="+currentLng+"&lat="+currentLat);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(8000);
                            connection.setReadTimeout(8000);
                            InputStream in = connection.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            Gson gson = new Gson();
                            Map map = gson.fromJson(response.toString(), new TypeToken<Map>(){}.getType());
                            Map result = (Map) map.get("result");
                            Map schoolArea = (Map) result.get("schoolArea");
                            String saName = (String) schoolArea.get("saName");
                            Message message = new Message();
                            message.what = SHOW_LOCATION;
                            message.obj = saName;
                            handler.sendMessage(message);

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if(connection != null) {
                                connection.disconnect();
                            }
                        }
                    }
                }).start();


                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                String currentPosition = "lat:" + currentLat +  "Lon:"+ currentLng + amapLocation.getDistrict();;
                System.out.println(currentPosition);
            }

            else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "请在设备的设置中开启app的定位权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){

            switch (msg.what) {
                case SHOW_LOCATION:
                    String currentSchoolDistrict = (String) msg.obj;
                    currentLocationText.setText(currentDistrict+"-"+currentSchoolDistrict);
                    break;

            }
        }
    };


    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }



    public int calculateInSampleSize(BitmapFactory.Options op, int reqWidth, int reqheight) {
        int originalWidth = op.outWidth;
        int originalHeight = op.outHeight;
        int inSampleSize = 1;
        if (originalWidth > reqWidth || originalHeight > reqheight) {
            int halfWidth = originalWidth / 2;
            int halfHeight = originalHeight / 2;
            while ((halfWidth / inSampleSize > reqWidth)
                    &&(halfHeight / inSampleSize > reqheight)) {
                inSampleSize *= 2;

            }
        }
        return inSampleSize;
    }

    public class clickListener implements OnClickListener{
        public void onClick(View v) {
            for(int i = 0; i < 16; i++)
            {
                if(district[i].getPixel((int)(x*scaleWidth),(int)((y+80)*scaleHeight)) != 0)
                {
                    result = i;
                    break;
                }
            }
            alternative.setImageResource(pic[result]);
            alternative.setVisibility(View.VISIBLE);

            Intent it = new Intent(MainActivity.this, SecondActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("districtResult", stringDistricts[result]);
            it.putExtras(bundle);
            startActivity(it);
        }
    };

    public class touchListener implements OnTouchListener{

        public boolean onTouch(View v, MotionEvent event)
        {
            imageWidth = imageBeijing.getWidth();
            imageHeight = imageBeijing.getHeight();
            bitmapWidth = bitmap.getWidth();
            bitmapHeight = bitmap.getHeight();
            scaleWidth = (double)bitmapWidth/imageWidth;
            scaleHeight = (double)bitmapHeight/imageHeight;
            x = (int) event.getX();
            y = (int) event.getY();
            Log.i("imageWidth", String.valueOf(imageWidth));
            Log.i("imageHeight", String.valueOf(imageHeight));
            Log.i("bitmapWidth", String.valueOf(bitmapWidth));
            Log.i("bitmapHeight", String.valueOf(bitmapHeight));
//需要与不同手机适配，上下两处都要改
            if(bitmap.getPixel((int)(x*scaleWidth),(int)((y+80)*scaleHeight))==0)
            {
                Log.i("touming", "透明区域");
                return true;//透明区域返回true
            }
            else
            {
                Log.i("x", String.valueOf(x));
                Log.i("y", String.valueOf(y));
                return false;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

}
