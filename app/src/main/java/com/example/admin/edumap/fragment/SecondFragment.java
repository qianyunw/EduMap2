package com.example.admin.edumap.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.admin.edumap.R;

/**
 * Created by zxt on 2016/5/27.
 */
public class SecondFragment extends BaseFragment {
    int color;

    public SecondFragment() {
    }
    @SuppressLint("ValidFragment")
    public SecondFragment(int color) {
        this.color = color;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.second_fragment,null);
            final FrameLayout frameLayout = (FrameLayout) rootView.findViewById(R.id.dummyfrag_bg);
            frameLayout.setBackgroundColor(color);
        }
        return super.onCreateView(inflater,container,savedInstanceState);
    }
}