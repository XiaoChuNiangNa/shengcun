package com.example.myapplication3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AreaInfoReceiver extends BroadcastReceiver {
    private final MainActivity activity;

    public AreaInfoReceiver(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.action.UPDATE_AREA_INFO".equals(intent.getAction())) {
            activity.uiUpdater.updateAreaInfo();
            Log.d("AreaInfoReceiver", "收到区域信息更新广播，执行刷新");
        }
    }
}