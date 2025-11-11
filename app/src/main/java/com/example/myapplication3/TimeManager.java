package com.example.myapplication3;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

public class TimeManager {
    private final MainActivity activity;
    private Runnable timeUpdateRunnable;
    private Runnable temperatureUpdateRunnable;

    public TimeManager(MainActivity activity) {
        this.activity = activity;
    }

    // 启动冷却刷新（解决cdRefreshRunnable私有访问问题）
    public void startCDRefresh() {
        // 通过EventHandler的getter方法获取cdRefreshRunnable
        activity.eventHandler.setCdRefreshRunnable(() -> {
            updateCollectButton();
            activity.handler.postDelayed(activity.eventHandler.getCdRefreshRunnable(), 1000);
        });
        activity.handler.post(activity.eventHandler.getCdRefreshRunnable());
    }

    // 更新采集按钮状态（解决dbHelper私有访问问题）
    private void updateCollectButton() {
        String areaType = activity.gameMap.getTerrainType(activity.currentX, activity.currentY);
        AreaResourceManager.AreaResource areaResource = AreaResourceManager.getInstance().getAreaResource(areaType);
        if (areaResource == null) {
            ((android.widget.Button) activity.findViewById(R.id.btn_collect)).setEnabled(false);
            ((android.widget.Button) activity.findViewById(R.id.btn_collect)).setText("无法采集");
            return;
        }

        // 通过DataManager的getDbHelper()访问dbHelper
        Map<String, Object> cdInfo = activity.dataManager.getDbHelper().getResourceCDInfo(MyApplication.currentUserId, activity.currentX, activity.currentY);
        int collectCount = activity.dataManager.getIntValue(cdInfo.get("collect_count"), 0);
        long lastCollectTime = activity.dataManager.getLongValue(cdInfo.get("last_collect_time"), 0);
        long currentTime = System.currentTimeMillis();

        if (collectCount >= areaResource.maxCollectTimes) {
            long remainingCD = (lastCollectTime + areaResource.recoveryMinutes * 60 * 1000) - currentTime;
            if (remainingCD > 0) {
                long minutes = remainingCD / 60000;
                long seconds = (remainingCD % 60000) / 1000;
                ((android.widget.Button) activity.findViewById(R.id.btn_collect)).setText(
                        String.format("采集次数已达上限（剩余%d分%d秒）", minutes, seconds)
                );
                ((android.widget.Button) activity.findViewById(R.id.btn_collect)).setEnabled(false);
            } else {
                ((android.widget.Button) activity.findViewById(R.id.btn_collect)).setText("采集（冷却已结束）");
                ((android.widget.Button) activity.findViewById(R.id.btn_collect)).setEnabled(true);
            }
        } else {
            ((android.widget.Button) activity.findViewById(R.id.btn_collect)).setText("采集");
            ((android.widget.Button) activity.findViewById(R.id.btn_collect)).setEnabled(true);
        }
    }

    // 启动时间更新（注释掉原有的基于现实时间的游戏时间更新）
    public void startTimeUpdates() {
        // 注释掉原有的每过现实时间增加游戏时间的逻辑
        // 现在游戏时间将在移动和采集动作成功时增加
        /*
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                activity.gameHour++;
                StatusEffectManager.onHourPassed(MyApplication.currentUserId);

                if (activity.gameHour >= Constant.GAME_HOURS_PER_DAY) {
                    activity.gameHour = 0;
                    activity.gameDay++;
                }

                activity.uiUpdater.updateTimeDisplay();
                saveTimeData();

                if (activity.gameHour == Constant.REFRESH_HOUR && activity.gameDay > activity.lastRefreshDay) {
                    resetCollectTimes();
                    activity.lastRefreshDay = activity.gameDay;
                }

                activity.handler.postDelayed(this, Constant.REAL_MINUTES_PER_GAME_HOUR);
            }
        };
        activity.handler.post(timeUpdateRunnable);
        */
    }

    // 重置采集次数（解决dbHelper私有访问问题）
    public void resetCollectTimes() {
        activity.currentCollectTimes = 0;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("global_collect_times", activity.currentCollectTimes);
                updates.put("last_refresh_day", activity.gameDay);
                // 通过DataManager的getDbHelper()访问dbHelper
                activity.dataManager.getDbHelper().updateUserStatus(MyApplication.currentUserId, updates);
                activity.dataManager.getDbHelper().clearAreaCollectTimes(MyApplication.currentUserId);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                activity.tvTip.setText("新的一天开始了！");
                activity.uiUpdater.updateAreaInfo();
            }
        }.execute();
    }

    // 保存时间数据（解决dbHelper私有访问问题）
    public void saveTimeData() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("game_hour", activity.gameHour);
        updates.put("game_day", activity.gameDay);
        // 通过DataManager的getDbHelper()访问dbHelper
        activity.dataManager.getDbHelper().updateUserStatus(MyApplication.currentUserId, updates);
    }

    // 启动温度更新
    public void startTemperatureUpdates() {
        temperatureUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                int change = activity.random.nextInt(3) - 1;
                activity.temperature = Math.max(34, Math.min(40, activity.temperature + change));
                activity.uiUpdater.updateTemperatureDisplay();
                saveTemperatureData();
                activity.handler.postDelayed(this, 10000);
            }
        };
        activity.uiUpdater.updateTemperatureDisplay();
        activity.handler.postDelayed(temperatureUpdateRunnable, 10000);
    }

    // 保存温度数据（解决dbHelper私有访问问题）
    private void saveTemperatureData() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("temperature", activity.temperature);
        // 通过DataManager的getDbHelper()访问dbHelper
        activity.dataManager.getDbHelper().updateUserStatus(MyApplication.currentUserId, updates);
    }

    // 处理onResume
    public void handleOnResume() {
        if (timeUpdateRunnable != null) activity.handler.removeCallbacks(timeUpdateRunnable);
        if (temperatureUpdateRunnable != null) activity.handler.removeCallbacks(temperatureUpdateRunnable);

        if (activity.isNeedReloadData) {
            activity.handler.postDelayed(() -> {
                activity.dataManager.loadGameData();
                activity.uiUpdater.updateStatusDisplays();
                activity.gameMap.loadUserTerrains(MyApplication.currentUserId);
                activity.backgroundView.setCurrentCoord(activity.currentX, activity.currentY, true);
                startTimeUpdates();
                startTemperatureUpdates();
                activity.isNeedReloadData = false;
            }, 300);
        } else {
            activity.gameMap.loadUserTerrains(MyApplication.currentUserId);
            activity.backgroundView.setCurrentCoord(activity.currentX, activity.currentY, true);
            restartTimers();
        }
    }

    // 处理onPause（解决dbHelper私有访问问题）
    public void handleOnPause() {
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("game_hour", activity.gameHour);
        statusData.put("game_day", activity.gameDay);
        statusData.put("temperature", activity.temperature);
        // 通过DataManager的getDbHelper()访问dbHelper
        activity.dataManager.getDbHelper().updateUserStatus(MyApplication.currentUserId, statusData);

        if (timeUpdateRunnable != null) activity.handler.removeCallbacks(timeUpdateRunnable);
        if (temperatureUpdateRunnable != null) activity.handler.removeCallbacks(temperatureUpdateRunnable);
    }

    // 重启定时器
    private void restartTimers() {
        if (timeUpdateRunnable != null) activity.handler.removeCallbacks(timeUpdateRunnable);
        if (temperatureUpdateRunnable != null) activity.handler.removeCallbacks(temperatureUpdateRunnable);

        saveTimeData();
        saveTemperatureData();
        startTimeUpdates();
        startTemperatureUpdates();
    }
}