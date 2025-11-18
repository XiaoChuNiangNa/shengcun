package com.example.myapplication3;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Map;

public class UIUpdater {
    private final MainActivity activity;
    private ValueAnimator typingAnimator;

    public UIUpdater(MainActivity activity) {
        this.activity = activity;
    }

    // 初始化视图
    public void initViews() {
        activity.tvAreaInfo = activity.findViewById(R.id.tv_area_info);
        activity.tvLife = activity.findViewById(R.id.tv_life);
        activity.tvHunger = activity.findViewById(R.id.tv_hunger);
        activity.tvThirst = activity.findViewById(R.id.tv_thirst);
        activity.tvStamina = activity.findViewById(R.id.tv_stamina);
        activity.tvTip = activity.findViewById(R.id.tv_tip);
        activity.btnBackpack = activity.findViewById(R.id.btn_backpack);
        activity.btnEquipment = activity.findViewById(R.id.btn_equipment);
        activity.btnFunctions = activity.findViewById(R.id.btn_functions);
        activity.ivSetting = activity.findViewById(R.id.iv_setting);
        activity.backgroundView = activity.findViewById(R.id.backgroundView);
        activity.tvAreaDescription = activity.findViewById(R.id.tv_area_description);
        activity.tvScrollTip = activity.findViewById(R.id.tv_area_description);
        activity.tvTime = activity.findViewById(R.id.tv_time);
        activity.tvDay = activity.findViewById(R.id.tv_day);
        activity.tvTemperature = activity.findViewById(R.id.tv_temperature);
        activity.ivClock = activity.findViewById(R.id.iv_clock);
        activity.ivThermometer = activity.findViewById(R.id.iv_thermometer);
        activity.tvEquipStatus = activity.findViewById(R.id.tv_equip_status);
        activity.scrollView = activity.findViewById(R.id.scrollView);
    }


    // 更新区域信息（已修复dbHelper访问方式）
    public void updateAreaInfo() {
        String areaType = activity.gameMap.getTerrainType(activity.currentX, activity.currentY);
        AreaResourceManager.AreaResource areaResource = AreaResourceManager.getInstance().getAreaResource(areaType);
        // 关键修改：通过DataManager的getDbHelper()方法获取dbHelper
        Map<String, Object> cdInfo = activity.dataManager.getDbHelper().getResourceCDInfo(MyApplication.currentUserId, activity.currentX, activity.currentY);
        int collectCount = activity.dataManager.getIntValue(cdInfo.get("collect_count"), 0);
        long lastCollectTime = activity.dataManager.getLongValue(cdInfo.get("last_collect_time"), 0);

        StringBuilder areaInfo = new StringBuilder();
        StringBuilder description = new StringBuilder(AreaDescription.getDescription(areaType));

        if (areaType.equals("未知区域")) {
            areaInfo.append("未知区域（超出地图范围）");
            description.append("\n无法采集：未知区域无资源");
        } else if (areaResource == null) {
            areaInfo.append(areaType);
            description.append("\n无法采集：区域未配置资源");
        } else {
            boolean isOverMaxTimes = collectCount >= areaResource.maxCollectTimes;
            long remainingCD = (lastCollectTime + areaResource.recoveryMinutes * 60 * 1000) - System.currentTimeMillis();
            boolean isInCooldown = isOverMaxTimes && remainingCD > 0;
            boolean isToolSuitable = Constant.isToolSuitableForArea(activity.currentEquip, areaType);

            areaInfo.append(areaType);

            if (isInCooldown) {
                long minutes = remainingCD / (60 * 1000);
                long seconds = (remainingCD % (60 * 1000)) / 1000;
                description.append(String.format("\n无法采集：已达最大采集次数（%d/%d），剩余冷却：%d分%d秒",
                        collectCount, areaResource.maxCollectTimes, minutes, seconds));
            } else if (isOverMaxTimes) {
                description.append(String.format("\n可采集：冷却已结束（最大次数：%d）", areaResource.maxCollectTimes));
            } else if (!isToolSuitable) {
                //description.append("\n无法采集：当前工具不适合该区域");
            } else {
                description.append("\n当前区域可采集");
            }
        }

        activity.tvAreaInfo.setText(areaInfo.toString());
        startTypingAnimation(description.toString(), 10);
    }

    // 打字动画
    public void startTypingAnimation(CharSequence text, long delayMillis) {
        if (typingAnimator != null && typingAnimator.isRunning()) {
            typingAnimator.cancel();
        }

        activity.tvAreaDescription.setText("");
        typingAnimator = ValueAnimator.ofInt(0, text.length());
        typingAnimator.setDuration(text.length() * delayMillis);
        typingAnimator.setInterpolator(new LinearInterpolator());
        typingAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            activity.tvAreaDescription.setText(text.subSequence(0, progress));
        });
        typingAnimator.start();
    }

    // 更新生存状态显示
    public void updateStatusDisplays() {
        // 关键修复：避免在初始化时误判死亡状态
        // 只在游戏真正开始后检查死亡状态
        GameStateManager gameStateManager = GameStateManager.getInstance(activity);
        boolean isGameStarted = gameStateManager.isGameStarted();
        
        // 增加数据完整性检查 - 只在数据加载完成后执行死亡检查
        // 如果UIUpdater创建时活动属性还没初始化完成，跳过死亡检查
        if (activity.life == 0 && !activity.isDataLoaded) {
            Log.d("GameOverCheck", "数据加载中，跳过死亡检查 (生命值=" + activity.life + ", isDataLoaded=" + activity.isDataLoaded + ")");
            // 仅更新UI显示，不进行死亡判定
            updateLifeDisplay();
        } else {
            // 正常更新所有状态显示
            updateLifeDisplay();
            
            // 添加详细的生命值检查日志
            Log.d("GameOverCheck", "详细检查死亡条件:");
            Log.d("GameOverCheck", "  - 当前生命值: " + activity.life);
            Log.d("GameOverCheck", "  - 游戏开始状态: " + isGameStarted);
            Log.d("GameOverCheck", "  - 生命值<=0: " + (activity.life <= 0));
            Log.d("GameOverCheck", "  - 综合条件 (isGameStarted && life<=0): " + (isGameStarted && activity.life <= 0));

            // 关键修复：只在游戏已开始且生命值<=0时触发游戏结束
            if (isGameStarted && activity.life <= 0) {
                Log.i("GameOverTrigger", "触发游戏结束条件: 游戏已开始且生命值<=0 (生命值=" + activity.life + ")");
                activity.handler.postDelayed(() -> {
                    if (!activity.isFinishing()) {
                        activity.showGameOverScreen();
                    }
                }, 100);
                return;
            } else if (isGameStarted && activity.life > 0) {
                Log.d("GameOverCheck", "游戏进行中，生命值正常 (" + activity.life + ")");
            } else if (!isGameStarted) {
                Log.d("GameOverCheck", "游戏未开始，不检查死亡状态");
            }
        }
        
        // 更新其他状态显示
        updateHungerDisplay();
        updateThirstDisplay();
        updateStaminaDisplay();
    }

    // 更新生命显示
    private void updateLifeDisplay() {
        activity.life = Math.max(0, Math.min(100, activity.life));
        activity.tvLife.setText("生命：" + activity.life);
        activity.tvLife.setTextColor(activity.life <= 30 ?
                activity.getResources().getColor(android.R.color.holo_red_dark) :
                activity.getResources().getColor(android.R.color.holo_green_dark));
    }

    // 更新饥饿显示
    private void updateHungerDisplay() {
        activity.hunger = Math.max(0, Math.min(100, activity.hunger));
        activity.tvHunger.setText("饥饿：" + activity.hunger);
        activity.tvHunger.setTextColor(activity.hunger <= 20 ?
                activity.getResources().getColor(android.R.color.holo_red_dark) :
                activity.getResources().getColor(android.R.color.holo_orange_dark));
    }

    // 更新口渴显示
    private void updateThirstDisplay() {
        activity.thirst = Math.max(0, Math.min(100, activity.thirst));
        activity.tvThirst.setText("口渴：" + activity.thirst);
        activity.tvThirst.setTextColor(activity.thirst <= 20 ?
                activity.getResources().getColor(android.R.color.holo_red_dark) :
                activity.getResources().getColor(android.R.color.holo_blue_light));
    }

    // 更新体力显示
    private void updateStaminaDisplay() {
        activity.stamina = Math.max(0, Math.min(100, activity.stamina));
        activity.tvStamina.setText("体力：" + activity.stamina);
        activity.tvStamina.setTextColor(activity.stamina <= 20 ?
                activity.getResources().getColor(android.R.color.holo_red_dark) :
                activity.getResources().getColor(android.R.color.holo_green_dark));
    }

    // 更新时间显示
    public void updateTimeDisplay() {
        String timeStr = String.format("%02d:00", activity.gameHour);
        activity.tvTime.setText(timeStr);
        activity.tvDay.setText(String.format("天数：%d", activity.gameDay));
        activity.ivClock.setRotation((activity.gameHour % 12) * 30f);
    }

    // 更新温度显示
    public void updateTemperatureDisplay() {
        activity.tvTemperature.setText(String.format("%d°C", activity.temperature));

        if (activity.temperature < Constant.TEMPERATURE_NORMAL_MIN) {
            activity.tvTemperature.setTextColor(activity.getResources().getColor(android.R.color.holo_blue_light));
        } else if (activity.temperature > Constant.TEMPERATURE_NORMAL_MAX) {
            activity.tvTemperature.setTextColor(activity.getResources().getColor(android.R.color.holo_red_light));
        } else {
            activity.tvTemperature.setTextColor(activity.getResources().getColor(android.R.color.holo_green_dark));
        }

        updateThermometerIcon();
    }

    // 更新体温计图标
    private void updateThermometerIcon() {
        if (activity.temperature < Constant.TEMPERATURE_NORMAL_MIN) {
            activity.ivThermometer.setImageResource(R.drawable.ic_thermometer_cold);
        } else if (activity.temperature > Constant.TEMPERATURE_NORMAL_MAX) {
            activity.ivThermometer.setImageResource(R.drawable.ic_thermometer_hot);
        } else {
            activity.ivThermometer.setImageResource(R.drawable.ic_thermometer_normal);
        }
    }

    // 刷新装备状态（已修复dbHelper访问方式）
    public void refreshEquipStatus() {
        if (activity.currentEquip == null || activity.currentEquip.isEmpty() || activity.currentEquip.equals("无")) {
            activity.tvEquipStatus.setText("当前装备：无");
        } else {
            new DurabilityQueryTask(this).execute();
        }
    }

    // 安全的异步任务类 - 耐久度查询
    private static class DurabilityQueryTask extends android.os.AsyncTask<Void, Void, Integer> {
        private WeakReference<UIUpdater> updaterRef;

        DurabilityQueryTask(UIUpdater updater) {
            this.updaterRef = new WeakReference<>(updater);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            UIUpdater updater = updaterRef.get();
            if (updater != null && updater.activity != null && !updater.activity.isFinishing()) {
                return updater.activity.dataManager.getDbHelper().getDurability(MyApplication.currentUserId, updater.activity.currentEquip);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer durability) {
            UIUpdater updater = updaterRef.get();
            if (updater != null && updater.activity != null && !updater.activity.isFinishing()) {
                if (durability <= 0) {
                    updater.activity.tvEquipStatus.setText("当前装备：" + updater.activity.currentEquip + "（已损坏）");
                } else {
                    updater.activity.tvEquipStatus.setText("当前装备：" + updater.activity.currentEquip + "（耐久：" + durability + "）");
                }
            }
        }
    }
}