package com.example.admin.edumap;

import com.nd.smartcan.accountclient.UCEnv;

/**
 * Created by ch on 2016/9/26.
 */

public class RequestService {

    public static final String BASE_API_PRE_PRODUCT = "http://fepapi.beta.web.sdp.101.com/v1/";
    public static final String BASE_API_PRODUCT = "http://fepapi.edu.web.sdp.101.com/v1/";
    private volatile static RequestService mInstance;
    private static String baseApi;

    private RequestService(UCEnv ucEnv) {
        switch (ucEnv) {
            case PreProduct:
                baseApi = BASE_API_PRE_PRODUCT;
                break;
            case Product:
                baseApi = BASE_API_PRODUCT;
                break;
            default:
                baseApi = null;
                break;
        }
        if(baseApi == null) {
            throw new IllegalArgumentException(RequestService.class.getName() + "始化失败, UCEnv参数错误。");
        }
    }

    public static void initialize(UCEnv ucEnv) {
        if (mInstance == null) {
            synchronized(RequestService.class) {
                if(mInstance == null) {
                    mInstance = new RequestService(ucEnv);
                }
            }
        }
    }
    public static RequestService getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(RequestService.class.getName() + "未初始化, 请先调用initialize(UCEnv).");
        }
        return mInstance;
    }
    public static String getBaseApi() {
        if (baseApi == null) {
            throw new IllegalStateException(RequestService.class.getName() + "未初始化, 请先调用initialize(UCEnv).");
        }
        return baseApi;
    }

    public static String getUserInfo() {
        return getBaseApi() + "users/actions/get_user_info";
    }

    public static String getPapers() {
        return getBaseApi() + "papers";
    }
}