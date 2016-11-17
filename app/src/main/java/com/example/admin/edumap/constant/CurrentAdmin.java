package com.example.admin.edumap.constant;

import java.io.Serializable;

/**
 * Created by admin on 2016/6/2.
 */
public class CurrentAdmin implements Serializable {

    private static class CurrentAdminHolder {
        //实例
        static final CurrentAdmin INSTANCE = new CurrentAdmin();
        public static String adminStr;

    }

    public void setCurrentAdmin (String adminStr) {
        CurrentAdminHolder.adminStr = adminStr;
    }

    public static CurrentAdmin getInstance() {
        return CurrentAdminHolder.INSTANCE;
    }

    public String getCurrentAdmin() {
        return CurrentAdminHolder.adminStr;
    }
    /**
     * private的构造函数用于避免外界直接使用new来实例化对象
     */
    private CurrentAdmin() {
    }
    /**
     * readResolve方法应对单例对象被序列化时候
     */
    private Object readResolve() {
        return getInstance();
    }
}