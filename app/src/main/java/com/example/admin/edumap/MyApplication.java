package com.example.admin.edumap;

import android.app.Application;

import com.nd.slp.tp.sdk.SlpSdk;
import com.nd.smartcan.accountclient.UCEnv;
import com.nd.smartcan.frame.AppDelegate;

/**
 * Created by CongHao on 2016/8/28.
 *
 */
/*
public class MyApplication extends SmartCanApp {

    @Override
    protected void beforeCreate() {

    }

    @Override
    protected void afterCreate() {
        //智慧学伴初始化方法。
        // UCEnv.Product(生产环境)，UCEnv.PreProduct(预生产环境)
        SlpSdk.initialize(UCEnv.Product);

        //初始化，根据UCEnv设置BaseApi
        RequestService.initialize(UCEnv.Product);
    }
}
*/

public class MyApplication extends Application {

    protected AppDelegate mAppDelegate ;

    @Override
    public void onCreate() {
        super.onCreate();
        //不能删除new AppDelegate(this);
        mAppDelegate = new AppDelegate(this);

        // UCEnv.Product(生产环境)，UCEnv.PreProduct(预生产环境)
        UCEnv ucEnv = UCEnv.PreProduct;

        //智慧学伴初始化方法。
        SlpSdk.initialize(ucEnv, "vorg_fep_edumap");

        //根据UCEnv设置BaseApi
        RequestService.initialize(ucEnv);
        RequestService.getBaseApi();
    }

    @Override
    public void onTerminate() {
        mAppDelegate.release();
        super.onTerminate();
    }

}