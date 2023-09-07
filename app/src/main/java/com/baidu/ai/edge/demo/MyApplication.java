package com.baidu.ai.edge.demo;

import android.app.Application;
import android.content.Context;

import com.baidu.ai.edge.core.base.BaseManager;

// import android.os.Environment;
// import xcrash.XCrash;

public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        /*
        String basePath = Environment.getExternalStorageDirectory().toString() + "/" + context.getPackageName();
        XCrash.InitParameters params = new XCrash.InitParameters();
        params.setAppVersion(BaseManager.VERSION);
        params.setLogDir(basePath + "/xCrash");
        XCrash.init(this, params);
        */
        // XCrash.testJavaCrash(true); // 测试JAVA报错日志
        // XCrash.testNativeCrash(true); // 测试NATIVE报错日志
    }
}
