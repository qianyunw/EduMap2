package com.example.admin.edumap.constant;

import java.io.Serializable;

/**
 * Created by admin on 2016/6/2.
 */
public class CurrentLocationSingleton implements Serializable {

    private static class CurrentLocationHolder {
        //实例
        static final CurrentLocationSingleton INSTANCE = new CurrentLocationSingleton();
        public static double currentLat;
        public static double currentLon;

    }

    public void setCurrentLatLon (double lat, double lon) {
        CurrentLocationHolder.currentLat = lat;
        CurrentLocationHolder.currentLon = lon;
    }

    public static CurrentLocationSingleton getInstance() {
        return CurrentLocationHolder.INSTANCE;
    }

    public double getCurrentLat() {
        return CurrentLocationHolder.currentLat;
    }
    public double getCurrentLon() {
        return CurrentLocationHolder.currentLon;
    }
    /**
     * private的构造函数用于避免外界直接使用new来实例化对象
     */
    private CurrentLocationSingleton() {
    }
    /**
     * readResolve方法应对单例对象被序列化时候
     */
    private Object readResolve() {
        return getInstance();
    }
}