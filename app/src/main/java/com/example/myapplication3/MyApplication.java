package com.example.myapplication3;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    public static int currentUserId = -1;
    public static String currentEquip = "无";
    private static Context context;
    
    // 测试账号的特殊UID
    public static final int TEST_ACCOUNT_UID = -100;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        
        // 延迟初始化测试账号，避免数据库连接池问题
        // 测试账号初始化将在首次数据库访问时自动进行
    }

    public static Context getAppContext() {
        return context;
    }
}