package com.example.admin.edumap;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.example.admin.edumap.adapter.ViewPagerAdapter;
import com.example.admin.edumap.constant.CurrentLocationSingleton;
import com.example.admin.edumap.constant.UrlConstant;
import com.example.admin.edumap.fragment.MoreFragment;
import com.example.admin.edumap.fragment.RelationFragment;
import com.example.admin.edumap.fragment.SecondFragment;
import com.example.admin.edumap.fragment.ThirdFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThirdActivity extends AppCompatActivity implements OnMarkerClickListener,
		OnInfoWindowClickListener, InfoWindowAdapter {
	private static ImageView buttonSift;
	private AMap aMap;
	private Map SchoolDistrictResult;
	public static final int SHOW_DISTRICT = 1;
	public static final int SHOW_RESPONSE = 0;
	public static final int SHOW_RIGHT = 2;
	public static final int SHOW_UNION = 3;
	public int schoolNumber;
	private ArrayList<Map> result;
	private ArrayList<Map> primaryResult;
	private ArrayList<Map> middleResult;
	private ArrayList<Map> highResult;
	private Double lai;
	private Double lon;
	private BottomSheetBehavior mBottomSheetBehavior;
	private ViewPager viewPager;
	private TextView firstTabTextView;
	private RelativeLayout upLayout;
	private float scale;
	private int typeLeft;
	private int typeRight;
	private Map schoolLeft;
	private String connectType;
	private ArrayList<Polyline> connections;
	private ArrayList<Polyline> unionConnections;
	private float currentScale;
	private ImageView mapButton;
	final Context context = this;
	private ArrayList<Marker> markers;
	private LinearLayout pastSiftMore;
	private ArrayList<Double> Distances;
	private Double minDistance;
	private Double maxDistance;
	private double[] distanceLevel;

	private double currentLat;
	private double currentLng;
	private String saId;

	//现有不null
	MapView mMapView = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCollector.addActivity(this);
		setContentView(R.layout.activity_second);
		markers = new ArrayList<Marker>();
		distanceLevel = new double[2];
		Distances = new ArrayList<Double>();

		mapButton = (ImageView) findViewById(R.id.map_floating_type);
		ImageView mapButton2 = (ImageView) findViewById(R.id.map_floating_resource);
		ImageView mapButton3 = (ImageView) findViewById(R.id.map_floating_group);
		ImageView map_type_legend = (ImageView) findViewById(R.id.map_type_legend);

		mapButton2.setVisibility(View.VISIBLE);
		mapButton3.setVisibility(View.VISIBLE);
		map_type_legend.setVisibility(View.VISIBLE);

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
		//scale = (scale + 5)/6;

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

		buttonSift = (ImageView) findViewById(R.id.button_sift);
		buttonSift.setVisibility(View.VISIBLE);

		buttonSift.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				showPopupWindow(view);
			}
		});

		//获取地图控件引用
		mMapView = (MapView) findViewById(R.id.map);
		//在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
		mMapView.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		Serializable serializableMap = (Serializable) bundle.get("SchoolDistrictResult");
		SchoolDistrictResult = (Map)serializableMap;
		currentLat = CurrentLocationSingleton.getInstance().getCurrentLat();
		currentLng = CurrentLocationSingleton.getInstance().getCurrentLon();

		String DistrictResult = bundle.get("DistrictResult").toString();
		TextView currentLocationText = (TextView)findViewById(R.id.current_location);
		currentLocationText.setText(DistrictResult + "--" + SchoolDistrictResult.get("saName").toString());

		lai = (Double) bundle.get("lai");
		lon = (Double) bundle.get("lon");


		View bottomSheet = findViewById( R.id.second_bottom_sheet );
		mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

		viewPager = (ViewPager) findViewById(R.id.second_tabanim_viewpager);
		setupViewPager(viewPager);
		viewPager.setOffscreenPageLimit(5);
		TabLayout tabLayout = (TabLayout) findViewById(R.id.second_tabanim_tabs);
		tabLayout.setupWithViewPager(viewPager);

		View view = View.inflate(ThirdActivity.this, R.layout.custom_tab_layout, null);
		tabLayout.getTabAt(0).setCustomView(view);
		firstTabTextView = (TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.custom_tab_textview);
		firstTabTextView.setText("对口升学");

		tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
				mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
				upLayout.setVisibility(View.VISIBLE);
				if(tab.getPosition() == 0)
					firstTabTextView.setTextColor(getResources().getColor(R.color.accent));
				switch (tab.getPosition()) {
					case 0:
						Spinner spinnerLeft = (Spinner) findViewById(R.id.spinner_left);
						spinnerLeft.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
								final List<String> schoolDataLeft = new ArrayList<String>();
								ArrayList<Map> typeResultLeft = new ArrayList<Map>();
								typeLeft = 0;
								switch (position) {
									case 0:
										typeLeft = 0;
										break;
									case 1:
										typeLeft = 1;
										typeResultLeft = primaryResult;
										break;
									case 2:
										typeLeft = 2;
										typeResultLeft = middleResult;
										break;
									case 3:
										typeLeft = 3;
										typeResultLeft = highResult;
										break;
								}
								for(int i = 0; i < typeResultLeft.size(); i++) {
									schoolDataLeft.add((String)typeResultLeft.get(i).get("sName"));
									System.out.println((String)typeResultLeft.get(i).get("sName"));
								}
								ArrayAdapter<String> adapter = new ArrayAdapter<String>(ThirdActivity.this, android.R.layout.simple_list_item_1, schoolDataLeft);
								ListView listViewLeft = (ListView) findViewById(R.id.list_left);
								listViewLeft.setAdapter(adapter);

								final ArrayList<Map> finalTypeResultLeft = typeResultLeft;
								listViewLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
										schoolLeft = finalTypeResultLeft.get(position);
										SearchRelation();
									}
								});
							}
							@Override
							public void onNothingSelected(AdapterView<?> parent) {
							}
						});




						Spinner spinnerRight = (Spinner) findViewById(R.id.spinner_right);
						spinnerRight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
								typeRight = 0;
								switch (position) {
									case 0:
										typeRight = 0;
										break;
									case 1:
										typeRight = 1;
										break;
									case 2:
										typeRight = 2;
										break;
									case 3:
										typeRight = 3;
										break;
								}
								SearchRelation();
							}
							@Override
							public void onNothingSelected(AdapterView<?> parent) {
							}
						});


						break;
					case 1:
						break;
					case 2:
						break;
					case 3:
						break;
				}
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
				switch (tab.getPosition()) {
					case 0:
						Spinner spinnerLeft = (Spinner) findViewById(R.id.spinner_left);
						spinnerLeft.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
								final List<String> schoolDataLeft = new ArrayList<String>();
								ArrayList<Map> typeResultLeft = new ArrayList<Map>();
								typeLeft = 0;
								switch (position) {
									case 0:
										typeLeft = 0;
										break;
									case 1:
										typeLeft = 1;
										typeResultLeft = primaryResult;
										break;
									case 2:
										typeLeft = 2;
										typeResultLeft = middleResult;
										break;
									case 3:
										typeLeft = 3;
										typeResultLeft = highResult;
										break;
								}
								for(int i = 0; i < typeResultLeft.size(); i++) {
									schoolDataLeft.add((String)typeResultLeft.get(i).get("sName"));
								}
								ArrayAdapter<String> adapter = new ArrayAdapter<String>(ThirdActivity.this, android.R.layout.simple_list_item_1, schoolDataLeft);
								ListView listViewLeft = (ListView) findViewById(R.id.list_left);
								listViewLeft.setAdapter(adapter);

								final ArrayList<Map> finalTypeResultLeft = typeResultLeft;
								listViewLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
										schoolLeft = finalTypeResultLeft.get(position);
										SearchRelation();
									}
								});
							}
							@Override
							public void onNothingSelected(AdapterView<?> parent) {
							}
						});




						Spinner spinnerRight = (Spinner) findViewById(R.id.spinner_right);
						spinnerRight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
								typeRight = 0;
								switch (position) {
									case 0:
										typeRight = 0;
										break;
									case 1:
										typeRight = 1;
										break;
									case 2:
										typeRight = 2;
										break;
									case 3:
										typeRight = 3;
										break;
								}
								SearchRelation();
							}
							@Override
							public void onNothingSelected(AdapterView<?> parent) {
							}
						});
						break;
					case 1:
						break;
					case 2:
						break;
					case 3:
						break;
				}
			}
		});



		if (aMap == null) {
			aMap = mMapView.getMap();
			setUpMap();
		}
	}


	private void showPopupWindow(View view) {

		// 一个自定义的布局，作为显示的内容
		final View contentView = LayoutInflater.from(context).inflate(
				R.layout.custom_sift_window, null);
		// 设置按钮的点击事件
		Button siftType = (Button) contentView.findViewById(R.id.sift_type);
		siftType.setOnClickListener(new buttonSiftClickListener(R.id.sift_type,contentView));
		Button siftScore = (Button) contentView.findViewById(R.id.sift_score);
		siftScore.setOnClickListener(new buttonSiftClickListener(R.id.sift_score,contentView));
		Button siftDistance = (Button) contentView.findViewById(R.id.sift_distance);
		siftDistance.setOnClickListener(new buttonSiftClickListener(R.id.sift_distance,contentView));

		final PopupWindow popupWindow = new PopupWindow(contentView,
				ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT, true);
		popupWindow.setTouchable(true);
		popupWindow.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				Log.i("mengdd", "onTouch : ");
				return false;
				// 这里如果返回true的话，touch事件将被拦截
				// 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
			}
		});
		// 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
		// 我觉得这里是API的一个bug
		popupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_window_transplant_bug));
		// 设置好参数之后再show
		popupWindow.showAsDropDown(view);

	}


	private class buttonSiftClickListener implements View.OnClickListener {
		private int temp;
		private View contentView;

		public buttonSiftClickListener(int id, View v) {
			temp = id;
			contentView = v;
		}

		public void onClick(View v) {
			for(int i = 0; i < markers.size(); i++) {
				markers.get(i).setVisible(true);
			}
			LinearLayout currentSiftMore;
			switch (temp) {
				case R.id.sift_type:
					currentSiftMore = (LinearLayout) contentView.findViewById(R.id.sift_type_more);
					if(pastSiftMore != null && pastSiftMore.getId() == currentSiftMore.getId() && pastSiftMore.getVisibility() == View.VISIBLE)						currentSiftMore.setVisibility(View.GONE);
					else {
						if(pastSiftMore != null)
							pastSiftMore.setVisibility(View.GONE);
						currentSiftMore.setVisibility(View.VISIBLE);

						CheckBox siftType1 = (CheckBox) contentView.findViewById(R.id.sift_type1);
						siftType1.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_type1, contentView));
						CheckBox siftType2 = (CheckBox) contentView.findViewById(R.id.sift_type2);
						siftType2.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_type2, contentView));
						CheckBox siftType3 = (CheckBox) contentView.findViewById(R.id.sift_type3);
						siftType3.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_type3, contentView));
						siftType1.setChecked(true);
						siftType2.setChecked(true);
						siftType3.setChecked(true);
					}
					pastSiftMore = currentSiftMore;

					break;
				case R.id.sift_score:
					currentSiftMore = (LinearLayout) contentView.findViewById(R.id.sift_score_more);
					if(pastSiftMore != null && pastSiftMore.getId() == currentSiftMore.getId() && pastSiftMore.getVisibility() == View.VISIBLE)						currentSiftMore.setVisibility(View.GONE);
					else {
						if(pastSiftMore != null)
							pastSiftMore.setVisibility(View.GONE);
						currentSiftMore.setVisibility(View.VISIBLE);

						CheckBox siftScore1 = (CheckBox) contentView.findViewById(R.id.sift_score1);
						siftScore1.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_score1, contentView));
						CheckBox siftScore2 = (CheckBox) contentView.findViewById(R.id.sift_score2);
						siftScore2.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_score2, contentView));
						CheckBox siftScore3 = (CheckBox) contentView.findViewById(R.id.sift_score3);
						siftScore3.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_score3, contentView));
						siftScore1.setChecked(true);
						siftScore2.setChecked(true);
						siftScore3.setChecked(true);
					}

					pastSiftMore = currentSiftMore;
					break;
				case R.id.sift_distance:

					currentSiftMore = (LinearLayout) contentView.findViewById(R.id.sift_distance_more);
					if(pastSiftMore != null && pastSiftMore.getId() == currentSiftMore.getId() && pastSiftMore.getVisibility() == View.VISIBLE)
						currentSiftMore.setVisibility(View.GONE);
					else {
						if(pastSiftMore != null)
							pastSiftMore.setVisibility(View.GONE);
						currentSiftMore.setVisibility(View.VISIBLE);

						CheckBox siftDistance1 = (CheckBox) contentView.findViewById(R.id.sift_distance1);
						siftDistance1.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_distance1, contentView));
						CheckBox siftDistance2 = (CheckBox) contentView.findViewById(R.id.sift_distance2);
						siftDistance2.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_distance2, contentView));
						CheckBox siftDistance3 = (CheckBox) contentView.findViewById(R.id.sift_distance3);
						siftDistance3.setOnClickListener(new buttonSiftMoreClickListener(R.id.sift_distance3, contentView));

						if(currentLat == 0 && currentLng == 0)
						{
							Toast toast=Toast.makeText(getApplicationContext(), "请打开您的GPS定位", Toast.LENGTH_SHORT);

						}
						else {

							if(Distances.size() == 0) {
								minDistance = 9999999999.0;
								maxDistance = 0.0;
								Gson gson = new Gson();
								for(int i = 0; i < result.size(); i++) {
									Map schoolInfo = result.get(i);
									Map schoolDistrictMiddlepointTemp = gson.fromJson(schoolInfo.get("sMiddlepoint").toString(), new TypeToken<Map>(){}.getType());
									LatLng startLatlng = new LatLng(Double.parseDouble(schoolDistrictMiddlepointTemp.get("lat").toString()), Double.parseDouble(schoolDistrictMiddlepointTemp.get("lng").toString()));
									double distanceTemp = AMapUtils.calculateLineDistance(startLatlng, new LatLng(currentLat, currentLng));
									if(minDistance > distanceTemp){
										minDistance = distanceTemp;
									}
									if(maxDistance < distanceTemp){
										maxDistance = distanceTemp;
									}
									Distances.add(distanceTemp);
								}
							}

							DecimalFormat decimalFormat = new DecimalFormat("0.0");

							if(maxDistance < 3000) {
								distanceLevel[0] = 1000;
								distanceLevel[1] = 2000;
								siftDistance1.setText("0-1km");
								siftDistance2.setText("0-2km");
								siftDistance3.setText("<3km");
							}
							else {
								distanceLevel[0] = minDistance + (maxDistance - minDistance)/3;
								distanceLevel[1] = distanceLevel[0] + (maxDistance - minDistance)/3;
								siftDistance1.setText(String.valueOf(Math.max(0, (int)(minDistance/1000)-1))+"-"+String.valueOf(decimalFormat.format(distanceLevel[0]/1000))+"km");
								siftDistance2.setText(String.valueOf(Math.max(0, (int)(minDistance/1000)-1))+"-"+String.valueOf(decimalFormat.format(distanceLevel[1]/1000))+"km");
								siftDistance3.setText("<"+String.valueOf((int)(maxDistance/1000)+1)+"km");
							}
						}
						siftDistance1.setChecked(true);
						siftDistance2.setChecked(true);
						siftDistance3.setChecked(true);
					}

					pastSiftMore = currentSiftMore;
					break;

				default:
					break;
			};
		}
	};

	private class buttonSiftMoreClickListener implements View.OnClickListener {
		private int temp;
		private View contentView;

		public buttonSiftMoreClickListener(int id, View v) {
			temp = id;
			contentView = v;
		}
		public void onClick(View v) {
			final ArrayList<Marker> targetMarkers = new ArrayList<Marker>();
			switch (temp) {
				case R.id.sift_type1:
					for(int i = 0; i < result.size(); i++) {
						Map schoolInfo = result.get(i);
						String sType = schoolInfo.get("sType").toString();
						if(sType.equals("小学")){
							targetMarkers.add(markers.get(i));
						}
					}
					break;
				case R.id.sift_type2:
					for(int i = 0; i < result.size(); i++) {
						Map schoolInfo = result.get(i);
						String sType = schoolInfo.get("sType").toString();
						if(sType.equals("初中")){
							targetMarkers.add(markers.get(i));
						}
					}
					break;
				case R.id.sift_type3:
					for(int i = 0; i < result.size(); i++) {
						Map schoolInfo = result.get(i);
						String sType = schoolInfo.get("sType").toString();
						if(sType.equals("高中")){
							targetMarkers.add(markers.get(i));
						}
					}
					break;
				case R.id.sift_score1:
					for(int i = 0; i < result.size(); i++) {
						Map schoolInfo = result.get(i);
						String sScore = schoolInfo.get("sScore").toString();
						float score;
						if(sScore == "")
							score = 0;
						else
							score = Float.valueOf(sScore);
						if(score >= 0 && score < 4){
							targetMarkers.add(markers.get(i));
						}
					}
					break;
				case R.id.sift_score2:
					for(int i = 0; i < result.size(); i++) {
						Map schoolInfo = result.get(i);
						String sScore = schoolInfo.get("sScore").toString();
						float score;
						if(sScore == "")
							score = 0;
						else
							score = Float.valueOf(sScore);
						if(score >= 4 && score < 7){
							targetMarkers.add(markers.get(i));
						}
					}
					break;
				case R.id.sift_score3:
					for(int i = 0; i < result.size(); i++) {
						Map schoolInfo = result.get(i);
						String sScore = schoolInfo.get("sScore").toString();
						float score;
						if(sScore == "")
							score = 0;
						else
							score = Float.valueOf(sScore);
						if(score >= 7 && score <= 10){
							targetMarkers.add(markers.get(i));
						}
					}
					break;
				case R.id.sift_distance1:
					for(int i = 0; i < result.size(); i++) {
						if(Distances.get(i) < distanceLevel[0])
							targetMarkers.add(markers.get(i));
					}
					break;
				case R.id.sift_distance2:
					for(int i = 0; i < result.size(); i++) {
						if(Distances.get(i) < distanceLevel[1])
							targetMarkers.add(markers.get(i));
					}
					break;
				case R.id.sift_distance3:
					for(int i = 0; i < result.size(); i++) {
						targetMarkers.add(markers.get(i));
					}
					break;

				default:
					break;
			};
			CheckBox targetCheckBox = (CheckBox) contentView.findViewById(temp);
			if(targetCheckBox.isChecked()) {
				for(int i = 0; i < targetMarkers.size(); i++) {
					targetMarkers.get(i).setVisible(true);
				}
			}
			else {
				for(int i = 0; i < targetMarkers.size(); i++) {
					targetMarkers.get(i).setVisible(false);
				}
			}
		}
	};




	private void SearchRelation() {

		connectType = null;
		if(typeLeft == 1 && typeRight == 2) {
			connectType = "小学-初中";
		}
		else if(typeLeft == 2 && typeRight == 3) {
			connectType = "初中-高中";
		}
		if(connectType != null && schoolLeft != null) {

			new Thread (new Runnable() {
				@Override
				public void run() {
					HttpURLConnection connection = null;
					try {
						Gson gson = new Gson();
						String sId = (String) schoolLeft.get("sId");
						System.out.println(sId);

						URL url = new URL(UrlConstant.BASE_URL+"schoolMapping?sId="+sId+"&connectType="+connectType);
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
						message.what = SHOW_RIGHT;
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

		else {
			final List<String> noSchoolDataRight = new ArrayList<String>();
			noSchoolDataRight.add("请选择学校和关系");
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(ThirdActivity.this, android.R.layout.simple_list_item_1, noSchoolDataRight);
			ListView listViewRight = (ListView) findViewById(R.id.list_right);
			listViewRight.setAdapter(adapter);
		}
	}

	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

		adapter.addFrag(new RelationFragment(getResources().getColor(R.color.light)), "对口升学");
		adapter.addFrag(new SecondFragment(getResources().getColor(R.color.light)), "学区政策");
		adapter.addFrag(new ThirdFragment(getResources().getColor(R.color.light)), "教学质量");
		adapter.addFrag(new MoreFragment(getResources().getColor(R.color.light)), "…更多");
		viewPager.setAdapter(adapter);
	}

	private void setUpMap() {

		Gson gson = new Gson();
		Map schoolDistrictMiddlepointTemp = gson.fromJson(SchoolDistrictResult.get("saMiddlepoint").toString(), new TypeToken<Map>(){}.getType());
		saId = gson.fromJson(SchoolDistrictResult.get("saId").toString(), new TypeToken<String>(){}.getType());
		LatLng schoolDistrictMiddlepoint = new LatLng(Double.parseDouble(schoolDistrictMiddlepointTemp.get("lat").toString()), Double.parseDouble(schoolDistrictMiddlepointTemp.get("lng").toString()));
		float saScaleparam = Float.parseFloat(SchoolDistrictResult.get("saScaleparam").toString());

		ArrayList<Map> saPoints = gson.fromJson(SchoolDistrictResult.get("saPoints").toString(), new TypeToken<ArrayList<Map>>(){}.getType());
		PolylineOptions polylineOption = new PolylineOptions();
		for(int j = 0; j < saPoints.size(); j++) {
			polylineOption.add(new LatLng(Double.parseDouble(saPoints.get(j).get("lat").toString()), Double.parseDouble(saPoints.get(j).get("lng").toString())));
			//System.out.println(saPoints.get(j).get("lat").toString());
		}
		polylineOption.add(new LatLng(Double.parseDouble(saPoints.get(0).get("lat").toString()), Double.parseDouble(saPoints.get(0).get("lng").toString())));
		aMap.addPolyline(polylineOption.setDottedLine(true).width(6).color(Color.BLUE));


		PolygonOptions schoolDistrictPolylineOption = new PolygonOptions();
		for(int j = 0; j < saPoints.size(); j++) {
			schoolDistrictPolylineOption.add(new LatLng(Double.parseDouble(saPoints.get(j).get("lat").toString()), Double.parseDouble(saPoints.get(j).get("lng").toString())));
			//System.out.println(saPoints.get(j).get("lat").toString());
		}
		schoolDistrictPolylineOption.add(new LatLng(Double.parseDouble(saPoints.get(0).get("lat").toString()), Double.parseDouble(saPoints.get(0).get("lng").toString())));

		schoolDistrictPolylineOption.fillColor(Color.argb(60,100,100,100)).strokeWidth(0);
		aMap.addPolygon(schoolDistrictPolylineOption);

		final float mapScale = (scale + 5)/6;
		aMap.moveCamera(
				CameraUpdateFactory.newLatLngZoom(new LatLng(schoolDistrictMiddlepoint.latitude, schoolDistrictMiddlepoint.longitude),saScaleparam * mapScale + 1f));
		currentScale = saScaleparam + 1f;
		aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
		aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
		aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式

		new Thread (new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					Gson gson = new Gson();
					String SchoolDistrictResultTemp = gson.fromJson(SchoolDistrictResult.get("saId").toString(), new TypeToken<String>(){}.getType());

					URL url = new URL(UrlConstant.BASE_URL+"schoolList?saId="+SchoolDistrictResultTemp);
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



	//显示学校
	private Handler handler=new Handler(){
		public void handleMessage(Message msg){

			switch (msg.what) {
				case SHOW_RESPONSE:

					result = (ArrayList<Map>) msg.obj;
					schoolNumber = result.size();
					primaryResult = new ArrayList<Map>();
					middleResult = new ArrayList<Map>();
					highResult = new ArrayList<Map>();

					for(int i = 0; i < result.size(); i++) {
						Gson gson = new Gson();
						String sName = gson.fromJson(result.get(i).get("sName").toString(), new TypeToken<String>(){}.getType());
						Map saMiddlepointTemp = gson.fromJson(result.get(i).get("sMiddlepoint").toString(), new TypeToken<Map>(){}.getType());
						LatLng sMiddlepoint = new LatLng(Double.parseDouble(saMiddlepointTemp.get("lat").toString()), Double.parseDouble(saMiddlepointTemp.get("lng").toString()));
						String sType = gson.fromJson(result.get(i).get("sType").toString(), new TypeToken<String>(){}.getType());

						switch (sType){
							case "小学":
								primaryResult.add(result.get(i));
								break;
							case "初中":
								middleResult.add(result.get(i));
								break;
							case "高中":
								highResult.add(result.get(i));
								break;
							default:
								break;
						}


						Bitmap btm = TextConvert(sName,sType);
						markers.add(aMap.addMarker(new MarkerOptions()
								.position(sMiddlepoint)
								.title(sName)
								.snippet(String.valueOf(i))
								.perspective(true)
								.icon(BitmapDescriptorFactory.fromBitmap(btm))));

					}

					aMap.addMarker(new MarkerOptions().position(new LatLng(currentLat, currentLng))
							.title("currentLocation")
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location)));

					if( lai != 0 && lon != 0) {
						System.out.println(lai);
						System.out.println(lon);
						aMap.addMarker(new MarkerOptions()
								.position(new LatLng(lai, lon))
								.title("location")
								.perspective(true)
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
					}
					break;


				case SHOW_UNION:
					ArrayList<Map> result = (ArrayList<Map>) msg.obj;
					for(int i = 0; i < result.size(); i++) {
						Map union = result.get(i);
						String unName = union.get("unName").toString();
						//ToastUtil.show(ThirdActivity.this, unName);
						String unType = union.get("unType").toString();
						System.out.println(unName);
						System.out.println(unType);
						ImageView unionImage = (ImageView) findViewById(R.id.map_union7);
						int unionColor = Color.rgb(0,0,0);
						switch (i){
							case 0:
								unionImage = (ImageView) findViewById(R.id.map_union1);
								break;
							case 1:
								unionImage = (ImageView) findViewById(R.id.map_union2);
								break;
							case 2:
								unionImage = (ImageView) findViewById(R.id.map_union3);
								break;
							case 3:
								unionImage = (ImageView) findViewById(R.id.map_union4);
								break;
							case 4:
								unionImage = (ImageView) findViewById(R.id.map_union5);
								break;
							case 5:
								unionImage = (ImageView) findViewById(R.id.map_union6);
								break;
							case 6:
								unionImage = (ImageView) findViewById(R.id.map_union7);
								break;
							default:
								break;
						}
						switch (unType){
							case "0.0":
								unionImage.setImageResource(R.drawable.union1);
								unionColor = Color.rgb(71,137,235);
								break;
							case "1.0":
								unionImage.setImageResource(R.drawable.union2);
								unionColor = Color.rgb(58,207,191);
								break;
							case "2.0":
								unionImage.setImageResource(R.drawable.union3);
								unionColor = Color.rgb(250,242,107);
								break;
							case "3.0":
								unionImage.setImageResource(R.drawable.union4);
								unionColor = Color.rgb(8,180,84);
								break;
							case "4.0":
								unionImage.setImageResource(R.drawable.union5);
								unionColor = Color.rgb(73,232,61);
								break;
							case "5.0":
								unionImage.setImageResource(R.drawable.union6);
								unionColor = Color.rgb(203,60,171);
								break;
							case "6.0":
								unionImage.setImageResource(R.drawable.union7);
								unionColor = Color.rgb(232,61,69);
								break;
							default:
								break;
						}
						unionImage.setVisibility(View.VISIBLE);
						ArrayList<Map> unMember = (ArrayList<Map>)union.get("unMember");
						List<LatLng> unMemberLatlng = new ArrayList<LatLng>();
						Gson gson = new Gson();
						for(int j = 0; j < unMember.size(); j++) {
							String sName = gson.fromJson(unMember.get(j).get("sName").toString(), new TypeToken<String>(){}.getType());
							String sType = gson.fromJson(unMember.get(j).get("sType").toString(), new TypeToken<String>(){}.getType());
							Map sMiddlepointTemp = gson.fromJson(unMember.get(j).get("sMiddlepoint").toString(), new TypeToken<Map>(){}.getType());
							LatLng sMiddlepoint = new LatLng(Double.parseDouble(sMiddlepointTemp.get("lat").toString()), Double.parseDouble(sMiddlepointTemp.get("lng").toString()));
							unMemberLatlng.add(sMiddlepoint);
							Map schoolAreaSchoolMapping = gson.fromJson(unMember.get(j).get("schoolAreaSchoolMapping").toString(), new TypeToken<Map>(){}.getType());
							String mySaId =  gson.fromJson(schoolAreaSchoolMapping.get("saId").toString(), new TypeToken<String>(){}.getType());

							System.out.println(mySaId);
							System.out.println(saId);

							//通过比较该学校学区ID与当前学区ID 决定是否另加标签
							//注意字符串相等的判断
							if(! mySaId.equals(saId)) {
								//System.out.println("wwwwwwwwwwwwwwwwwwwwwwwww");
								Bitmap btm = TextConvert(sName,sType);
								aMap.addMarker(new MarkerOptions()
										.position(sMiddlepoint)
										.snippet("-1")
										.title(sName)
										.perspective(true)
										.icon(BitmapDescriptorFactory.fromBitmap(btm)));
							}
						}
						unMemberLatlng.add(unMemberLatlng.get(0));
						PolylineOptions polylineOption = new PolylineOptions();
						for(int j = 0; j < unMemberLatlng.size(); j++) {
							polylineOption.add(unMemberLatlng.get(j));
						}
						unionConnections.add(aMap.addPolyline(polylineOption.setDottedLine(true).width(11).color(unionColor)));
					}

					break;


				case SHOW_RIGHT:
					ArrayList<Map> rightResultTemp = (ArrayList<Map>) msg.obj;
					ArrayList<Map> rightResult = new ArrayList<Map>();
					final List<String> schoolDataRight = new ArrayList<String>();
					if(rightResultTemp != null) {
						for(int i = 0; i < rightResultTemp.size(); i++) {
							Map school = (Map) rightResultTemp.get(i).get("school");
							schoolDataRight.add(school.get("sName").toString());
							rightResult.add(school);
						}
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(ThirdActivity.this, android.R.layout.simple_list_item_1, schoolDataRight);
						ListView listViewRight = (ListView) findViewById(R.id.list_right);
						listViewRight.setAdapter(adapter);

						if(connections == null)
							connections =  new ArrayList<Polyline>();
						else{
							for(int i = 0; i < connections.size(); i++) {
								connections.get(i).remove();
							}
						}
						Gson gson = new Gson();
						Map tempStartLatLng = gson.fromJson(schoolLeft.get("sMiddlepoint").toString(), new TypeToken<Map>(){}.getType());
						LatLng startLatLng = new LatLng(Double.parseDouble(tempStartLatLng.get("lat").toString()), Double.parseDouble(tempStartLatLng.get("lng").toString()));
						//谁最低就以谁为中心
						//谁距离大就以谁缩放
						LatLng centerLatLng = startLatLng;
						float realMaxDistance = 0;
						for (int i = 0; i < rightResult.size(); i++) {
							PolylineOptions polylineOption = new PolylineOptions();
							Map temp = gson.fromJson(rightResult.get(i).get("sMiddlepoint").toString(), new TypeToken<Map>(){}.getType());
							LatLng endLatLng = new LatLng(Double.parseDouble(temp.get("lat").toString()), Double.parseDouble(temp.get("lng").toString()));
							polylineOption.width(10).color(Color.BLUE).add(startLatLng).add(endLatLng);
							connections.add(aMap.addPolyline(polylineOption));

							Bitmap btm = TextConvert(rightResult.get(i).get("sName").toString(),rightResult.get(i).get("sType").toString());
							aMap.addMarker(new MarkerOptions()
									.position(endLatLng)
									.title(rightResult.get(i).get("sName").toString())
									.snippet(String.valueOf(i))
									.perspective(true)
									.icon(BitmapDescriptorFactory.fromBitmap(btm)));

							float tempDistance = AMapUtils.calculateLineDistance(startLatLng, endLatLng);
							if(realMaxDistance < tempDistance)
								realMaxDistance = tempDistance;
							if(centerLatLng.latitude > endLatLng.latitude)
								centerLatLng = endLatLng;
						}
						float scale = currentScale;
						aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLatLng.latitude, centerLatLng.longitude), scale));
						float currentScreenDistance = aMap.getScalePerPixel() * 750f;

						System.out.println(realMaxDistance);
						System.out.println(currentScreenDistance);
						System.out.println(scale);
						int step = 1;
						while(realMaxDistance > currentScreenDistance) {
							step++;
							scale -= 0.6/step;
							aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLatLng.latitude , startLatLng.longitude), scale));
							currentScreenDistance = aMap.getScalePerPixel() * 750f;
							System.out.println(realMaxDistance);
							System.out.println(currentScreenDistance);
							System.out.println(scale);
						}

					}
					else {
						final List<String> noSchoolDataRight = new ArrayList<String>();
						noSchoolDataRight.add("未找到对应学校");
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(ThirdActivity.this, android.R.layout.simple_list_item_1, noSchoolDataRight);
						ListView listViewRight = (ListView) findViewById(R.id.list_right);
						listViewRight.setAdapter(adapter);

					}

			}
		}
	};

	@SuppressLint("NewApi")
	public Bitmap TextConvert(String text, String Type){

		final int[] color = new int[3];
		switch(Type)
		{
			case "小学":
				color[0] = 130;
				color[1] = 204;
				color[2] = 2;
				break;
			case "初中":
				color[0] = 8;
				color[1] = 180;
				color[2] = 84;
				break;
			case "高中":
				color[0] = 58;
				color[1] = 207;
				color[2] = 191;
				break;
			default:
				color[0] = 254;
				color[1] = 221;
				color[2] = 0;
				break;
		};
		final Paint textPaint = new Paint() {
			{
				setColor(Color.WHITE);
				//setColor(Color.BLACK);
				setTextAlign(Align.LEFT);
				setTextSize(35f * scale);
				setAntiAlias(true);
				setTypeface(Typeface.DEFAULT_BOLD);
			}
		};

		if(text.length() > 6)
		{
			text = text.substring(0,6);
			text += "…";
		}


		final Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), bounds);

		final Bitmap bmp = Bitmap.createBitmap(bounds.width()+ 16, bounds.height()+ 16 + 30, Bitmap.Config.ARGB_8888); //use ARGB_8888 for better quality
		final Canvas canvas = new Canvas(bmp);
		//canvas.drawARGB(1000, 255, 255, 255);
//		    Paint iconPaint=new Paint();
//		    iconPaint.setDither(true);//防抖动
//		    iconPaint.setFilterBitmap(true);
//		    canvas.drawBitmap(icon, 3f, 3f, iconPaint);
//
		final Paint rectPaint = new Paint();

		rectPaint.setColor(Color.rgb(color[0], color[1], color[2]));
		canvas.drawRect(0, 0, bounds.width()+ 16, bounds.height()+ 16, rectPaint);
		//rectPaint.setColor(Color.WHITE);
		//canvas.drawRect(2, 2, bounds.width()+ 14, bounds.height()+ 14, rectPaint);
		rectPaint.setColor(Color.rgb(color[0], color[1], color[2]));
		canvas.drawRect(4, 4, bounds.width()+ 12, bounds.height()+ 12, rectPaint);

		canvas.drawText(text, 8f, 40f * scale, textPaint);

		Paint trianglePaint = new Paint();
		trianglePaint.setColor(Color.rgb(color[0],color[1],color[2]));
		trianglePaint.setStyle(Paint.Style.FILL);//设置填满
		trianglePaint.setAntiAlias(true);

		Path path = new Path();
		path.moveTo(bounds.width()/2 - 20, bounds.height()+ 14);// 此点为多边形的起点
		path.lineTo(bounds.width()/2 + 20, bounds.height()+ 14);
		path.lineTo(bounds.width()/2, bounds.height() + 30);
		path.close(); // 使这些点构成封闭的多边形
		canvas.drawPath(path, trianglePaint);


		return bmp;

		// bmp.recycle();
	}



	//解析Json数据
	private ArrayList<Map> parseJSONWithJOSNObject(String jsonData) {
		Gson gson = new Gson();
		Map map = gson.fromJson(jsonData, new TypeToken<Map>(){}.getType());
		ArrayList<Map> result = (ArrayList<Map>)map.get("result");
		return result;
	}


	/**
	 * 对marker标注点点击响应事件
	 */
	@Override
	public boolean onMarkerClick(final Marker marker) {
		if (aMap != null) {
			if(unionConnections == null)
				unionConnections =  new ArrayList<Polyline>();
			else {
				for (int i = 0; i < unionConnections.size(); i++) {
					unionConnections.get(i).remove();
				}
				unionConnections.clear();
			}

			ImageView unionImage = (ImageView) findViewById(R.id.map_union1);
			if(unionImage.getVisibility() == View.VISIBLE)
				unionImage.setVisibility(View.INVISIBLE);
			unionImage = (ImageView) findViewById(R.id.map_union2);
			if(unionImage.getVisibility() == View.VISIBLE)
				unionImage.setVisibility(View.INVISIBLE);
			unionImage = (ImageView) findViewById(R.id.map_union3);
			if(unionImage.getVisibility() == View.VISIBLE)
				unionImage.setVisibility(View.INVISIBLE);
			unionImage = (ImageView) findViewById(R.id.map_union4);
			if(unionImage.getVisibility() == View.VISIBLE)
				unionImage.setVisibility(View.INVISIBLE);
			unionImage = (ImageView) findViewById(R.id.map_union5);
			if(unionImage.getVisibility() == View.VISIBLE)
				unionImage.setVisibility(View.INVISIBLE);
			unionImage = (ImageView) findViewById(R.id.map_union6);
			if(unionImage.getVisibility() == View.VISIBLE)
				unionImage.setVisibility(View.INVISIBLE);
			unionImage = (ImageView) findViewById(R.id.map_union7);
			if(unionImage.getVisibility() == View.VISIBLE)
				unionImage.setVisibility(View.INVISIBLE);


			if(marker.isInfoWindowShown()) {
				marker.hideInfoWindow();
			}
			else {
				int i = Integer.parseInt(marker.getSnippet());
				if(i != -1) {
					marker.showInfoWindow();

					Map schoolInfo = result.get(i);
					final String sId = schoolInfo.get("sId").toString();
					System.out.println(sId);
					new Thread (new Runnable() {
						@Override
						public void run() {
							HttpURLConnection connection = null;
							try {
								URL url = new URL(UrlConstant.BASE_URL+"union?sId="+sId);
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
								ArrayList<Map> result = gson.fromJson(response.toString(), new TypeToken<ArrayList<Map>>(){}.getType());

								Message message = new Message();
								message.what = SHOW_UNION;
								message.obj = result;
								if(message.obj != null)
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
			}

		}

		return true;
	}



	/**
	 * 监听点击infowindow窗口事件回调
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
	}
	/**
	 * 监听自定义infowindow窗口的infocontents事件回调
	 */
	@Override
	public View getInfoContents(Marker marker) {
		View infoContent = getLayoutInflater().inflate(
				R.layout.custom_info_window, null);
		render(marker, infoContent);
		return infoContent;
	}

	/**
	 * 监听自定义infowindow窗口的infowindow事件回调
	 */
	@Override
	public View getInfoWindow(Marker marker) {
		View infoWindow = getLayoutInflater().inflate(
				R.layout.custom_info_window, null);

		render(marker, infoWindow);
		return infoWindow;
	}

	/**
	 * 自定义infowinfow窗口
	 */
	public void render(Marker marker, View view) {
		int i = Integer.parseInt(marker.getSnippet());
		Map schoolInfo = result.get(i);
		String sName = schoolInfo.get("sName").toString();
		String sCharacter = schoolInfo.get("sCharacter").toString();
		String sAddress = schoolInfo.get("sAddress").toString();
		String sType = schoolInfo.get("sType").toString();
		String sScore = schoolInfo.get("sScore").toString();

		if(sType != null)
		{
			switch(sType)
			{
				case "小学":
					view.setBackgroundResource(R.drawable.bubble_xiaoxue);
					break;
				case "初中":
					view.setBackgroundResource(R.drawable.bubble_chuzhong);
					break;
				case "高中":
					view.setBackgroundResource(R.drawable.bubble_gaozhong);
					break;
				default:
					break;

			};
		}

		TextView name = ((TextView) view.findViewById(R.id.name));
		if(sName != null)	name.setText(sName);
		else name.setText("");

		TextView character = ((TextView) view.findViewById(R.id.character));
		if(sCharacter != null)	character.setText(sCharacter);
		else character .setText("");

		TextView type = ((TextView) view.findViewById(R.id.type));
		if(sType != null)	type.setText(sType);
		else type.setText("");

		TextView score = ((TextView) view.findViewById(R.id.score));
		if(sScore != null)	score.setText(sScore+"分");
		else score.setText("0分");

		TextView address = ((TextView) view.findViewById(R.id.address));
		if(sAddress != null)	address.setText(sAddress);
		else address.setText("");

		TextView more = ((TextView) view.findViewById(R.id.more));
		more.setOnClickListener(new moreClickListener(schoolInfo));

		ArrayList<ImageView> stars = new ArrayList<ImageView>();
		stars.add((ImageView) view.findViewById(R.id.star1));
		stars.add((ImageView) view.findViewById(R.id.star2));
		stars.add((ImageView) view.findViewById(R.id.star3));
		stars.add((ImageView) view.findViewById(R.id.star4));
		stars.add((ImageView) view.findViewById(R.id.star5));

		float starScore;
		if(sScore != null)	starScore = Float.parseFloat(sScore);
		else	starScore = 0;
		for(int j = 0; j < starScore/2; j++)
		{
			stars.get(j).setImageResource(R.drawable.score_full);
		}
		if(starScore%2 != 0)
			stars.get((int)starScore/2).setImageResource(R.drawable.score_half);
		else
			stars.get((int)starScore/2).setImageResource(R.drawable.score_empty);
		for(int j = (int)(starScore/2)+1 ; j < 5; j++)
		{
			stars.get(j).setImageResource(R.drawable.score_empty);
		}

	}

	private class moreClickListener implements View.OnClickListener {
		private Map schoolInfo;

		public moreClickListener(Map schoolInfoi) {
			schoolInfo = schoolInfoi;
		}
		public void onClick(View v) {
			Intent it = new Intent(ThirdActivity.this, SchoolActivity.class);
			Bundle bundle=new Bundle();
			Serializable tmpmap= (Serializable) schoolInfo;
			bundle.putSerializable("schoolInfo", tmpmap);
			it.putExtras(bundle);
			startActivity(it);

		}
	};

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
