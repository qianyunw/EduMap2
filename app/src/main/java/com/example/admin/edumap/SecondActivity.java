package com.example.admin.edumap;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.amap.api.services.district.DistrictSearchQuery;
import com.example.admin.edumap.adapter.ViewPagerAdapter;
import com.example.admin.edumap.constant.CurrentLocationSingleton;
import com.example.admin.edumap.constant.UrlConstant;
import com.example.admin.edumap.fragment.FirstFragment;
import com.example.admin.edumap.fragment.MoreFragment;
import com.example.admin.edumap.fragment.SecondFragment;
import com.example.admin.edumap.fragment.ThirdFragment;
import com.example.admin.edumap.util.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class SecondActivity extends AppCompatActivity implements OnDistrictSearchListener, OnMarkerClickListener, AMap.OnMapClickListener {
	private AMap aMap;
	private String DistrictResult;
	private Map SchoolDistrictResult;
	public static final int SHOW_DISTRICT = 1;
	public static final int SHOW_RESPONSE = 0;
	public static final int SHOW_ACTIVITY = 2;
	public static final int NO_RESPONSE = 3;
	public int schoolDistricNumber;
	private DistrictColor[] districtColor;
	private Marker[] markers;
	private ArrayList<Map> result;
	private BottomSheetBehavior mBottomSheetBehavior;
	private ViewPager viewPager;
	private TextView firstTabTextView;
	private RelativeLayout upLayout;
	private float scale;
	private ImageView mapButton;
	final Context context = this;

	public class DistrictColor{
		public int r;
		public int g;
		public int b;
		DistrictColor(int r1, int g1, int b1)
		{
			r = r1;
			g = g1;
			b = b1;
		}
		DistrictColor(){}
	}

	//现有不null
	MapView mMapView = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCollector.addActivity(this);
		setContentView(R.layout.activity_second);
		upLayout = (RelativeLayout)findViewById(R.id.upLayout);
		upLayout.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
					upLayout.setVisibility(View.GONE);
				}
				return true;
			}
		});

//		mapButton = (ImageView) findViewById(R.id.choose_map);
//
//		// add button listener
//		mapButton.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//
//				AlertDialog.Builder builder;
//				final AlertDialog dialog;
//
//				LayoutInflater inflater = getLayoutInflater();
//				// 添加自定义的布局文件
//				View layout = LayoutInflater.from(context).inflate(
//						R.layout.custom_dialog, null);
//				builder = new AlertDialog.Builder(context,R.style.TANCStyle);
//				dialog = builder.create();
//				dialog.setView(layout);
//				Window dialogWindow = dialog.getWindow();
//				WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//				lp.y = -100; // 新位置Y坐标
//
//				WindowManager m = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//				Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
//
//				dialogWindow.setAttributes(lp);
//
//				RelativeLayout dialogClose = (RelativeLayout) layout.findViewById(R.id.dialog_close);
//				dialogClose.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						dialog.dismiss();
//					}
//				});
//
//				dialog.show();
//			}
//		});




		mapButton = (ImageView) findViewById(R.id.map_floating_type);

		// add button listener
		mapButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// custom dialog
				final Dialog dialog = new Dialog(context, R.style.TANCStyle);
				dialog.setContentView(R.layout.custom_dialog);

				Window dialogWindow = dialog.getWindow();
				WindowManager.LayoutParams lp = dialogWindow.getAttributes();

				lp.y = -100; // 新位置Y坐标

				dialogWindow.setAttributes(lp);

				RelativeLayout dialogClose = (RelativeLayout) dialog.findViewById(R.id.dialog_close);
				// if button is clicked, close the custom dialog
				dialogClose.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				dialog.show();


				ImageView heat_map = (ImageView) dialogWindow.findViewById(R.id.heat_map);
				final ImageView map_type_legend_hot = (ImageView) findViewById(R.id.map_type_legend_hot);
				final ImageView map_type_legend = (ImageView) findViewById(R.id.map_type_legend);
				heat_map.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						WebView web_view_heat_map = (WebView) findViewById(R.id.web_view_heat_map);
						map_type_legend_hot.setVisibility(View.VISIBLE);
						map_type_legend.setVisibility(View.GONE);
						web_view_heat_map.setVisibility(View.VISIBLE);
						web_view_heat_map.getSettings().setJavaScriptEnabled(true);
						web_view_heat_map.setWebViewClient(new WebViewClient());
						web_view_heat_map.loadUrl("http://202.112.88.61:8080/mapserver/mobile/heatMap");
						dialog.dismiss();
					}
				});

				ImageView normal_map = (ImageView) dialogWindow.findViewById(R.id.normal_map);
				normal_map.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						WebView web_view_heat_map = (WebView) findViewById(R.id.web_view_heat_map);
						if(web_view_heat_map.getVisibility() == View.VISIBLE) {
							web_view_heat_map.setVisibility(View.GONE);
							map_type_legend_hot.setVisibility(View.GONE);
							map_type_legend.setVisibility(View.VISIBLE);
						}
						dialog.dismiss();
					}
				});

			}
		});



		DisplayMetrics metrics=new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		scale = metrics.widthPixels / 1080f;
		System.out.println(metrics.widthPixels);
		//获取地图控件引用
		mMapView = (MapView) findViewById(R.id.map);
		//在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
		mMapView.onCreate(savedInstanceState);

		View bottomSheet = findViewById( R.id.second_bottom_sheet );
		mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

		viewPager = (ViewPager) findViewById(R.id.second_tabanim_viewpager);
		setupViewPager(viewPager);
		viewPager.setOffscreenPageLimit(5);
		TabLayout tabLayout = (TabLayout) findViewById(R.id.second_tabanim_tabs);
		tabLayout.setupWithViewPager(viewPager);View view = View.inflate(SecondActivity.this, R.layout.custom_tab_layout, null);
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

//		districtColor = new DistrictColor[13];
//		districtColor[0] = new DistrictColor(192,204,246);//jinrongjie
//		districtColor[1] = new DistrictColor(158,222,207);//xinjiekouxuequ
//		districtColor[2] = new DistrictColor(138,206,164);//okxichanganjie
//		districtColor[3] = new DistrictColor(192,204,246);//guang anmen wai
//		districtColor[4] = new DistrictColor(158,222,207);//yuetan
//		districtColor[5] = new DistrictColor(192,204,246);//dazhanlan chunjie
//		districtColor[6] = new DistrictColor(254,252,121);//okshichahai
//		districtColor[7] = new DistrictColor(138,206,164);//guang anmen nei
//		districtColor[8] = new DistrictColor(192,204,246);
//		districtColor[9] = new DistrictColor(254,252,121);//taoranting
//		districtColor[10] = new DistrictColor(254,252,121);//okzhanlanlu
//		districtColor[11] = new DistrictColor(192,204,246);
//		districtColor[12] = new DistrictColor(153,154,0);


		districtColor = new DistrictColor[16];
		districtColor[0] = new DistrictColor(192,204,246);//jinrongjie
		districtColor[1] = new DistrictColor(158,222,207);//xinjiekouxuequ
		districtColor[2] = new DistrictColor(138,206,164);//okxichanganjie
		districtColor[3] = new DistrictColor(192,204,246);//guang anmen wai
		districtColor[4] = new DistrictColor(158,222,207);//yuetan
		districtColor[5] = new DistrictColor(192,204,246);//dazhanlan chunjie
		districtColor[6] = new DistrictColor(254,252,121);//okshichahai
		districtColor[7] = new DistrictColor(138,206,164);//guang anmen nei
		districtColor[8] = new DistrictColor(192,204,246);
		districtColor[9] = new DistrictColor(254,252,121);//taoranting
		districtColor[10] = new DistrictColor(254,252,121);//okzhanlanlu
		districtColor[11] = new DistrictColor(192,204,246);
		districtColor[12] = new DistrictColor(153,154,0);
		districtColor[13] = new DistrictColor(254,252,121);//okzhanlanlu
		districtColor[14] = new DistrictColor(192,204,246);
		districtColor[15] = new DistrictColor(153,154,0);



		if (aMap == null) {
			aMap = mMapView.getMap();
			setUpMap();

			Bundle bundle=getIntent().getExtras();
			DistrictResult = bundle.getString("districtResult");
			TextView currentLocationText = (TextView)findViewById(R.id.current_location);
			currentLocationText.setText(DistrictResult);

			aMap.setOnMarkerClickListener(this);
			DistrictSearch search = new DistrictSearch(getApplicationContext());
			DistrictSearchQuery query = new DistrictSearchQuery( );
			query.setKeywords(DistrictResult);
			query.setShowBoundary(true);
			search.setQuery(query);
			search.setOnDistrictSearchListener(this);

			search.searchDistrictAnsy();

		}
	}


	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

		adapter.addFrag(new FirstFragment(getResources().getColor(R.color.light)), "权威发布");
		adapter.addFrag(new SecondFragment(getResources().getColor(R.color.light)), "本区学区");
		adapter.addFrag(new ThirdFragment(getResources().getColor(R.color.light)), "教学质量");
		adapter.addFrag(new MoreFragment(getResources().getColor(R.color.light)), "…更多");
		viewPager.setAdapter(adapter);
	}

	private void setUpMap() {
		aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
	}


	//获取行政区、学区的坐标点
	@Override
	public void onDistrictSearched(com.amap.api.services.district.DistrictResult districtResult) {

		if (districtResult == null|| districtResult.getDistrict()==null) {
			return;
		}
		final DistrictItem item = districtResult.getDistrict().get(0);

		if (item == null) {
			return;
		}
		LatLonPoint centerLatLng=item.getCenter();
		if(centerLatLng!=null){
			final float mapScale = (scale + 5)/6;
			float scaleDistrict = 9f * mapScale;
			switch(DistrictResult)
			{
				case "海淀区":
					scaleDistrict = 11.0f * mapScale;
					break;
				case"西城区":
					scaleDistrict = 12.6f * mapScale;
					districtColor[0] = new DistrictColor(192,204,246);//jinrongjie
					districtColor[1] = new DistrictColor(158,222,207);//xinjiekouxuequ
					districtColor[2] = new DistrictColor(138,206,164);//okxichanganjie
					districtColor[3] = new DistrictColor(192,204,246);//guang anmen wai
					districtColor[4] = new DistrictColor(158,222,207);//yuetan
					districtColor[5] = new DistrictColor(192,204,246);//dazhanlan chunjie
					districtColor[6] = new DistrictColor(254,252,121);//okshichahai
					districtColor[7] = new DistrictColor(138,206,164);//guang anmen nei
					districtColor[8] = new DistrictColor(192,204,246);
					districtColor[9] = new DistrictColor(254,252,121);//taoranting
					districtColor[10] = new DistrictColor(254,252,121);//okzhanlanlu
					districtColor[11] = new DistrictColor(192,204,246);
					districtColor[12] = new DistrictColor(153,154,0);
					districtColor[13] = new DistrictColor(254,252,121);//okzhanlanlu
					districtColor[14] = new DistrictColor(192,204,246);
					districtColor[15] = new DistrictColor(153,154,0);
					break;
				case"通州区":
					scaleDistrict = 10.6f * mapScale;
					districtColor[0] = new DistrictColor(254,252,121);//张家湾
					districtColor[1] = new DistrictColor(192,204,246);//梨园
					districtColor[2] = new DistrictColor(80,200,180);//中仓
					districtColor[3] = new DistrictColor(192,204,246);//潞城
					districtColor[4] = new DistrictColor(213,224,95);//台湖
					districtColor[5] = new DistrictColor(135,213,111);//永顺
					districtColor[6] = new DistrictColor(254,252,121);//新华
					districtColor[7] = new DistrictColor(138,206,164);//西集
					districtColor[8] = new DistrictColor(213,224,95);//于家务
					districtColor[9] = new DistrictColor(254,252,121);//宋庄
					districtColor[10] = new DistrictColor(153,154,01);//北苑
					districtColor[11] = new DistrictColor(192,204,246);//永乐
					districtColor[12] = new DistrictColor(80,200,180);//玉桥
					districtColor[13] = new DistrictColor(80,200,180);//郭县
					districtColor[14] = new DistrictColor(192,204,246);//马驹
					districtColor[15] = new DistrictColor(153,154,0);//永顺
					centerLatLng = new LatLonPoint(39.801409,116.714954);
					break;
			}
			aMap.moveCamera(
					CameraUpdateFactory.newLatLngZoom(new LatLng(centerLatLng.getLatitude(), centerLatLng.getLongitude()),scaleDistrict));
		}

		new Thread() {
			public void run() {

				String[] polyStr = item.districtBoundary();
				if (polyStr == null || polyStr.length == 0) {
					return;
				}
				for (String str : polyStr) {
					String[] lat = str.split(";");
					PolylineOptions polylineOption = new PolylineOptions();
					boolean isFirst=true;
					LatLng firstLatLng=null;
					for (String latstr : lat) {
						String[] lats = latstr.split(",");
						if(isFirst){
							isFirst=false;
							firstLatLng=new LatLng(Double
									.parseDouble(lats[1]), Double
									.parseDouble(lats[0]));
						}
						polylineOption.add(new LatLng(Double
								.parseDouble(lats[1]), Double
								.parseDouble(lats[0])));
					}
					if(firstLatLng!=null){
						polylineOption.add(firstLatLng);
					}

					polylineOption.width(6).color(Color.BLUE);
					Message message=handler.obtainMessage();
					message.what = SHOW_DISTRICT;
					message.obj=polylineOption;
					handler.sendMessage(message);
				}
			}
		}.start();

		new Thread (new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(UrlConstant.BASE_URL+"schoolAreaList?districtName="+DistrictResult);
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
					ArrayList<Map> result = parseJSONWithJOSNObject(response.toString());
					Message message = new Message();
					message.what = SHOW_RESPONSE;
					message.obj = result;
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

	}


	//显示多边形
	private Handler handler=new Handler(){
		public void handleMessage(Message msg){

			switch (msg.what) {
				case SHOW_DISTRICT:
					PolylineOptions polylineOption=(PolylineOptions) msg.obj;
					aMap.addPolyline(polylineOption);
					break;
				case SHOW_RESPONSE:
					result = (ArrayList<Map>) msg.obj;
					schoolDistricNumber = result.size();
					markers = new Marker[schoolDistricNumber];
					for(int i = 0; i < result.size(); i++) {
						Gson gson = new Gson();
						ArrayList<Map> saPoints = gson.fromJson(result.get(i).get("saPoints").toString(), new TypeToken<ArrayList<Map>>(){}.getType());
						String saName = gson.fromJson(result.get(i).get("saName").toString(), new TypeToken<String>(){}.getType());
						Map saMiddlepointTemp = gson.fromJson(result.get(i).get("saMiddlepoint").toString(), new TypeToken<Map>(){}.getType());
						LatLng saMiddlepoint = new LatLng(Double.parseDouble(saMiddlepointTemp.get("lat").toString()), Double.parseDouble(saMiddlepointTemp.get("lng").toString()));
						PolygonOptions schoolDistrictPolylineOption = new PolygonOptions();
						for(int j = 0; j < saPoints.size(); j++) {
							schoolDistrictPolylineOption.add(new LatLng(Double.parseDouble(saPoints.get(j).get("lat").toString()), Double.parseDouble(saPoints.get(j).get("lng").toString())));
							//System.out.println(saPoints.get(j).get("lat").toString());
						}
						schoolDistrictPolylineOption.add(new LatLng(Double.parseDouble(saPoints.get(0).get("lat").toString()), Double.parseDouble(saPoints.get(0).get("lng").toString())));
						schoolDistrictPolylineOption.fillColor(Color.argb(150, districtColor[i%16].r, districtColor[i%16].g, districtColor[i%16].b)).strokeWidth(0);
						//System.out.println(saName+String.valueOf(i));
						//System.out.println(saMiddlepoint.latitude);
						//System.out.println(saMiddlepoint.longitude);
						aMap.addPolygon(schoolDistrictPolylineOption);

						Bitmap btm = TextConvert(saName);

						markers[i] = aMap.addMarker(new MarkerOptions().position(saMiddlepoint)
								.title(saName)
								.icon(BitmapDescriptorFactory.fromBitmap(btm)));
					}

					aMap.addMarker(new MarkerOptions().position(new LatLng(CurrentLocationSingleton.getInstance().getCurrentLat(), CurrentLocationSingleton.getInstance().getCurrentLon()))
							.title("currentLocation")
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location)));


					System.out.println(CurrentLocationSingleton.getInstance().getCurrentLat());
					System.out.println( CurrentLocationSingleton.getInstance().getCurrentLon());

					break;
				case SHOW_ACTIVITY:
					Intent it = new Intent(SecondActivity.this, ThirdActivity.class);
					Bundle bundle=new Bundle();
					Serializable tempSchoolDistrictResult= (Serializable) SchoolDistrictResult;
					bundle.putSerializable("SchoolDistrictResult", tempSchoolDistrictResult);
					bundle.putDouble("lai",0);
					bundle.putDouble("lon",0);
					bundle.putString("DistrictResult",DistrictResult);
					it.putExtras(bundle);
					startActivity(it);
					break;
				case NO_RESPONSE:
					ToastUtil.show(ActivityCollector.getTopActivity(),"对不起，没有找到对应学区");
			}
		}
	};


	/**
	 * 对单击地图事件回调
	 */
	@Override
	public void onMapClick(final LatLng point) {

		new Thread (new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(UrlConstant.BASE_URL+"search?lng=" + String.valueOf(point.longitude)+"&lat="+String.valueOf(point.latitude));
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
					String resultMsg = (String) map.get("resultMsg");
					if(resultMsg.equals("找到学区")) {
						Map resulttemp = (Map) map.get("result");
						SchoolDistrictResult = (Map) resulttemp.get("schoolArea");
						Message message = new Message();
						message.what = SHOW_ACTIVITY;
						handler.sendMessage(message);
					}else {
						Message message = new Message();
						message.what = NO_RESPONSE;
						handler.sendMessage(message);
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if(connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();

	}


	/**
	 * 对marker标注点点击响应事件
	 */
	@Override
	public boolean onMarkerClick(final Marker marker) {
		for(int i = 0; i < schoolDistricNumber; i++)
		{
			if (marker.equals(markers[i])) {
				if (aMap != null) {
					//	jumpPoint(marker);
					//Toast toast=Toast.makeText(getApplicationContext(), "你点击的是" + marker.getTitle() , Toast.LENGTH_SHORT);
					//toast.show();

					Intent it = new Intent(SecondActivity.this, ThirdActivity.class);
					Bundle bundle=new Bundle();
					SchoolDistrictResult  = result.get(i);
					Serializable tmpmap= (Serializable) SchoolDistrictResult;
					bundle.putSerializable("SchoolDistrictResult", tmpmap);
					bundle.putDouble("lai",0);
					bundle.putDouble("lon",0);
					bundle.putString("DistrictResult",DistrictResult);
					it.putExtras(bundle);
					startActivity(it);

				}
				break;
			}
		}

		return true;
	}



	//解析Json数据
	private ArrayList<Map> parseJSONWithJOSNObject(String jsonData) {
		Gson gson = new Gson();
		Map map = gson.fromJson(jsonData, new TypeToken<Map>(){}.getType());
		ArrayList<Map> result = (ArrayList<Map>)map.get("result");
		return result;
	}

	//文字传图片
	public Bitmap TextConvert(final String text){

		final Paint textPaint = new Paint() {
			{
				setColor(Color.WHITE);
				setTextAlign(Align.LEFT);
				setTextSize(35f * scale);
				setAntiAlias(true);
			}
		};
		final Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), bounds);

		final Bitmap bmp = Bitmap.createBitmap(bounds.width() + 12, bounds.height()+ 16, Bitmap.Config.ARGB_8888); //use ARGB_8888 for better quality
		final Canvas canvas = new Canvas(bmp);
		canvas.drawARGB(1000, 255, 64, 129);
		canvas.drawText(text, 5f, 38f * scale , textPaint);
		return bmp;

		// bmp.recycle();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		ActivityCollector.removeActivity(this);
	}
	@Override
	protected void onResume() {
		super.onResume();
		//在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
		//在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
		mMapView.onSaveInstanceState(outState);
	}
}