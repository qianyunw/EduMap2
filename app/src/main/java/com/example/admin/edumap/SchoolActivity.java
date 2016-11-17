package com.example.admin.edumap;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class SchoolActivity extends AppCompatActivity {

    private Map schoolInfo;
    private LatLng schoolDistrictMiddlepoint;
    private String sName;
   // private List<Item> itemList = new ArrayList<Item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_school);

        ImageView re = (ImageView) findViewById(R.id.img_return);
        re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCollector.removeActivity();
            }
        });


        Bundle bundle = getIntent().getExtras();
        final Serializable serializableMap = (Serializable) bundle.get("schoolInfo");
        schoolInfo = (Map) serializableMap;
        if (schoolInfo != null) {
            sName = schoolInfo.get("sName").toString();
            String sCharacter = schoolInfo.get("sCharacter").toString();
            String sAddress = schoolInfo.get("sAddress").toString();
            String sType = schoolInfo.get("sType").toString();
            String sScore = schoolInfo.get("sScore").toString();
            String sDescription = schoolInfo.get("sDescription").toString();
            String sUrl = schoolInfo.get("sUrl").toString();
            String sPhone = schoolInfo.get("sPhone").toString();
            Gson gson = new Gson();
            Map schoolDistrictMiddlepointTemp = gson.fromJson(schoolInfo.get("sMiddlepoint").toString(), new TypeToken<Map>() {}.getType());
            schoolDistrictMiddlepoint = new LatLng(Double.parseDouble(schoolDistrictMiddlepointTemp.get("lat").toString()), Double.parseDouble(schoolDistrictMiddlepointTemp.get("lng").toString()));

//            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
//            appBarLayout.addOnOffsetChangedListener(this);


            TextView school_name = (TextView) findViewById(R.id.school_name);
            school_name.setText(sName);
            TextView title = (TextView) findViewById(R.id.title);
            title.setText(sName);
            TextView school_character_type = (TextView) findViewById(R.id.school_character_type);
            school_character_type.setText(sCharacter + " " + sType);
            TextView school_address = (TextView) findViewById(R.id.school_address);
            school_address.setText(sAddress);
            TextView school_phone = (TextView) findViewById(R.id.school_phone);
            school_phone.setText(sPhone);
            TextView school_url = (TextView) findViewById(R.id.school_url);
            school_url.setText("进入官网" + " " + sUrl);
            TextView school_description = (TextView) findViewById(R.id.school_description);
            school_description.setText(sDescription);

            TextView score = ((TextView) findViewById(R.id.score));
            if (sScore != null) score.setText(sScore + "分");
            else score.setText("0分");

            ArrayList<ImageView> stars = new ArrayList<ImageView>();
            stars.add((ImageView) findViewById(R.id.star1));
            stars.add((ImageView) findViewById(R.id.star2));
            stars.add((ImageView) findViewById(R.id.star3));
            stars.add((ImageView) findViewById(R.id.star4));
            stars.add((ImageView) findViewById(R.id.star5));

            float starScore;
            if (sScore != null) starScore = Float.parseFloat(sScore);
            else starScore = 0;
            for (int j = 0; j < starScore / 2; j++) {
                stars.get(j).setImageResource(R.drawable.score_full);
            }
            if (starScore % 2 != 0)
                stars.get((int) starScore / 2).setImageResource(R.drawable.score_half);
            else
                stars.get((int) starScore / 2).setImageResource(R.drawable.score_empty);
            for (int j = (int) (starScore / 2) + 1; j < 5; j++) {
                stars.get(j).setImageResource(R.drawable.score_empty);
            }

            FloatingActionButton my = (FloatingActionButton) findViewById(R.id.button_route);
            my.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(SchoolActivity.this, RouteActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("serializableMap", serializableMap);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

        }
    }
//
//    @Override
//    public void onOffsetChanged(AppBarLayout appBarLayout, int offset)
//    {
//        if (offset == 0) {
//
//            RelativeLayout mLinerLayout = (RelativeLayout) findViewById(R.id.real_bar);
//            mLinerLayout.setVisibility(View.INVISIBLE);
//        }
//        else if (Math.abs(offset) >= appBarLayout.getTotalScrollRange()) {
//            RelativeLayout mLinerLayout = (RelativeLayout) findViewById(R.id.real_bar);
//            mLinerLayout.setVisibility(View.VISIBLE);
//            TextView title = (TextView) findViewById(R.id.title);
//            title.setText(sName);
//        }
//        else {
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
