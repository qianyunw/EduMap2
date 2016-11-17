package com.example.admin.edumap.layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.admin.edumap.ActivityCollector;
import com.example.admin.edumap.R;
import com.example.admin.edumap.ThirdActivity;
import com.example.admin.edumap.constant.UrlConstant;
import com.example.admin.edumap.util.AMapUtil;
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
import java.util.List;
import java.util.Map;

public class SearchLayout extends LinearLayout implements GeocodeSearch.OnGeocodeSearchListener,TextWatcher,
		PoiSearch.OnPoiSearchListener, View.OnClickListener, Inputtips.InputtipsListener {


	private AutoCompleteTextView searchText;// 输入搜索关键字
	private String keyWord = "";// 要输入的poi搜索关键字
	private ProgressDialog progDialog = null;// 搜索时进度条
	private PoiResult poiResult; // poi返回的结果
	private PoiSearch.Query query;// Poi查询条件类
	private PoiSearch poiSearch;// POI搜索
	private GeocodeSearch geocoderSearch;
	public static final int SHOW_RESPONSE = 0;
	public static final int NO_RESPONSE = 1;
	private GeocodeAddress addressResult;



	public SearchLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.search, this);
		ImageView searButton = (ImageView) findViewById(R.id.button_search);
		searButton.setOnClickListener(this);
		searchText = (AutoCompleteTextView) findViewById(R.id.keyWord);
		searchText.addTextChangedListener(this);// 添加文本输入框监听事件
	}

	/**
	 * 点击搜索按钮
	 */
	public void searchButton() {
		keyWord = AMapUtil.checkEditText(searchText);
		if ("".equals(keyWord)) {
			ToastUtil.show(ActivityCollector.getTopActivity(), "请输入搜索关键字");
			return;
		} else {
			doSearchQuery();
		}
	}


	/**
	 * 显示进度框
	 */
	private void showProgressDialog() {
		if (progDialog == null)
			progDialog = new ProgressDialog(ActivityCollector.getTopActivity());
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(false);
		progDialog.setMessage("正在搜索:\n" + keyWord + "所在学区…");
		progDialog.show();
	}

	/**
	 * 隐藏进度框
	 */
	private void dissmissProgressDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}


	/**
	 * 开始进行poi搜索
	 */
	protected void doSearchQuery() {
		showProgressDialog();// 显示进度框
		query = new PoiSearch.Query(keyWord, "", "北京市");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
		query.setPageSize(10);// 设置每页最多返回多少条poiitem
		query.setPageNum(0);// 设置查第一页
		query.setCityLimit(true);

		poiSearch = new PoiSearch(ActivityCollector.getTopActivity(), query);
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();

		geocoderSearch = new GeocodeSearch(ActivityCollector.getTopActivity());
		geocoderSearch.setOnGeocodeSearchListener(this);
		//progDialog = new ProgressDialog(ActivityCollector.getTopActivity());

		dissmissProgressDialog();

		getLatlon(keyWord);
	}


	/**
	 * poi没有搜索到数据，返回一些推荐城市的信息
	 */
	private void showSuggestCity(List<SuggestionCity> cities) {
		String infomation = "推荐城市\n";
		for (int i = 0; i < cities.size(); i++) {
			infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
					+ cities.get(i).getCityCode() + "城市编码:"
					+ cities.get(i).getAdCode() + "\n";
		}
		ToastUtil.show(ActivityCollector.getTopActivity(), infomation);

	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String newText = s.toString().trim();
		if (!AMapUtil.IsEmptyOrNullString(newText)) {
			InputtipsQuery inputquery = new InputtipsQuery(newText, "北京市");
			Inputtips inputTips = new Inputtips(ActivityCollector.getTopActivity(), inputquery);
			inputTips.setInputtipsListener(this);
			inputTips.requestInputtipsAsyn();
		}
	}


	/**
	 * POI信息查询回调方法
	 */
	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		dissmissProgressDialog();// 隐藏对话框
		if (rCode == 1000) {
			if (result != null && result.getQuery() != null) {// 搜索poi的结果
				if (result.getQuery().equals(query)) {// 是否是同一条
					poiResult = result;
					// 取得搜索到的poiitems有多少页
					List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
					List<SuggestionCity> suggestionCities = poiResult
							.getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

					if (poiItems != null && poiItems.size() > 0) {
					} else if (suggestionCities != null
							&& suggestionCities.size() > 0) {
						showSuggestCity(suggestionCities);
					} else {
						ToastUtil.show(ActivityCollector.getTopActivity(),
								"no_result");
					}
				}
			} else {
				ToastUtil.show(ActivityCollector.getTopActivity(),
						"no_result");
			}
		} else {
			ToastUtil.showerror(ActivityCollector.getTopActivity(), rCode);
		}

	}

	@Override
	public void onPoiItemSearched(PoiItem item, int rCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			/**
			 * 点击搜索按钮
			 */
			case R.id.button_search:
				searchButton();
				break;
			default:
				break;
		}
	}

	@Override
	public void onGetInputtips(List<Tip> tipList, int rCode) {
		if (rCode == 1000) {// 正确返回
			List<String> listString = new ArrayList<String>();
			for (int i = 0; i < tipList.size(); i++) {
				listString.add(tipList.get(i).getName());
			}
			ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
					ActivityCollector.getTopActivity().getApplication().getApplicationContext(),
					R.layout.route_inputs, listString);
			searchText.setAdapter(aAdapter);
			aAdapter.notifyDataSetChanged();
		} else {
			ToastUtil.showerror(ActivityCollector.getTopActivity(), rCode);
		}


	}

	/**
	 * 显示进度条对话框
	 */
	public void showDialog() {
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(true);
		progDialog.setMessage("正在获取地址");
		progDialog.show();
	}

	/**
	 * 隐藏进度条对话框
	 */
	public void dismissDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}

	/**
	 * 响应地理编码
	 */
	public void getLatlon(final String name) {
		showDialog();
		GeocodeQuery query = new GeocodeQuery(name, "北京市");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
		geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
	}

	/**
	 * 地理编码查询回调
	 */
	@Override
	public void onGeocodeSearched(GeocodeResult result, int rCode) {
		dismissDialog();
		if (rCode == 1000) {
			if (result != null && result.getGeocodeAddressList() != null
					&& result.getGeocodeAddressList().size() > 0) {

				final GeocodeAddress address = result.getGeocodeAddressList().get(0);
				addressResult = address;

//				String addressResultDistrict = addressResult.getDistrict();
//			这里不对，相当于自己调用自己。
//				keyWord += addressResultDistrict;
//				if ("".equals(keyWord)) {
//					ToastUtil.show(ActivityCollector.getTopActivity(), "请输入搜索关键字");
//					return;
//				} else {
//					doSearchQuery();
//				}


				new Thread (new Runnable() {
					@Override
					public void run() {
						HttpURLConnection connection = null;
						try {
							URL url = new URL(UrlConstant.BASE_URL+"search?lng=" + String.valueOf(address.getLatLonPoint().getLongitude())+"&lat="+String.valueOf(address.getLatLonPoint().getLatitude()));
							System.out.println("lng="+address.getLatLonPoint().getLongitude()+"&lat="+address.getLatLonPoint().getLatitude());
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
								Map SchoolDistrictResult = (Map) resulttemp.get("schoolArea");
								Message message = new Message();
								message.what = SHOW_RESPONSE;
								message.obj = SchoolDistrictResult;
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

			} else {
				ToastUtil.show(ActivityCollector.getTopActivity(), "no_result");
			}

		} else {
			ToastUtil.showerror(ActivityCollector.getTopActivity(), rCode);
		}
	}

	private Handler handler=new Handler(){
		public void handleMessage(Message msg){

			switch (msg.what) {
				case SHOW_RESPONSE:
					Map SchoolDistrictResult = (Map) msg.obj;
					dissmissProgressDialog();
					Intent it = new Intent(ActivityCollector.getTopActivity(), ThirdActivity.class);
					Bundle bundle=new Bundle();

					Serializable tempSchoolDistrictResult= (Serializable) SchoolDistrictResult;
					bundle.putSerializable("SchoolDistrictResult", tempSchoolDistrictResult);
					double lai = addressResult.getLatLonPoint().getLatitude();
					double lon = addressResult.getLatLonPoint().getLongitude();
					bundle.putDouble("lai",lai);
					bundle.putDouble("lon",lon);
					bundle.putSerializable("DistrictResult",addressResult.getDistrict());
					it.putExtras(bundle);

					System.out.println(String.valueOf(lai));
					System.out.println(String.valueOf(lon));

					ActivityCollector.getTopActivity().startActivity(it);
					break;
				case NO_RESPONSE:
					dissmissProgressDialog();
					ToastUtil.show(ActivityCollector.getTopActivity(),"对不起，没有找到对应学区");
			}
		}
	};




	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
	}
}
