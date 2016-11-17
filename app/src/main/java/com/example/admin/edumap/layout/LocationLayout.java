package com.example.admin.edumap.layout;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.admin.edumap.ActivityCollector;
import com.example.admin.edumap.R;
import com.example.admin.edumap.SearchActivity;

public class LocationLayout extends LinearLayout{

	public LocationLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.location, this);


		ImageView re = (ImageView) findViewById(R.id.img_return);
		re.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				ActivityCollector.removeActivity();
			}
		});

		ImageView button_search = (ImageView) findViewById(R.id.button_search);
		button_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivityCollector.getTopActivity(), SearchActivity.class);
				ActivityCollector.getTopActivity().startActivity(intent);
			}
		});
	}
}