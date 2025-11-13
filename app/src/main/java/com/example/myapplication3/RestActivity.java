package com.example.myapplication3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class RestActivity extends BaseActivity {

    private DBHelper dbHelper;
    private int userId;
    private TextView tvTip, tvShelterType, tvRestLight, tvRestHeavy;
    private Button btnRestLight, btnRestHeavy;
    private ImageButton btnBack;
    private static final long REST_COOLDOWN = 10 * 60 * 1000; // 10分钟冷却
    private String currentShelterType; // 当前庇护所类型

    // 休整类型枚举
    private enum RestType {
        LIGHT, // 小憩一会
        HEAVY  // 安心休整
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);

        // 初始化数据库和用户ID
        dbHelper = DBHelper.getInstance(this);
        userId = MyApplication.currentUserId;

        // 初始化视图
        initViews();
        // 获取当前庇护所类型
        currentShelterType = getCurrentShelterType();
        // 更新UI显示
        updateShelterInfo();
        updateRestStatus();

        // 按钮点击事件
        btnBack.setOnClickListener(v -> {
            // 检查是否从基地进入，如果是则返回基地
            if (isFromBase()) {
                startActivity(new Intent(RestActivity.this, BaseActivity.class));
                finish();
            } else {
                finish();
            }
        });
        btnRestLight.setOnClickListener(v -> performRest(RestType.LIGHT));
        btnRestHeavy.setOnClickListener(v -> performRest(RestType.HEAVY));
    }

    private void initViews() {
        tvTip = findViewById(R.id.tv_tip);
        tvShelterType = findViewById(R.id.tv_shelter_type);
        tvRestLight = findViewById(R.id.tv_rest_light);
        tvRestHeavy = findViewById(R.id.tv_rest_heavy);
        btnRestLight = findViewById(R.id.btn_rest_light);
        btnRestHeavy = findViewById(R.id.btn_rest_heavy);
        btnBack = findViewById(R.id.btn_back);
    }

    // 获取当前最高级别的庇护所类型
    private String getCurrentShelterType() {
        if (dbHelper.getBuildingCount(userId, Constant.BUILDING_BRICK_HOUSE) > 0) {
            return "砖瓦屋";
        } else if (dbHelper.getBuildingCount(userId, Constant.BUILDING_STONE_HOUSE) > 0) {
            return "小石屋";
        } else if (dbHelper.getBuildingCount(userId, Constant.BUILDING_WOOD_HOUSE) > 0) {
            return "小木屋";
        } else if (dbHelper.getBuildingCount(userId, Constant.BUILDING_THATCH_HOUSE) > 0) {
            return "茅草屋";
        } else {
            return "野外";
        }
    }

    // 更新庇护所信息及休整消耗/恢复显示
    private void updateShelterInfo() {
        tvShelterType.setText("当前庇护所：" + currentShelterType);

        // 根据庇护所类型设置两种休整的消耗和恢复
        String lightDesc, heavyDesc;
        switch (currentShelterType) {
            case "茅草屋":
                lightDesc = "消耗：饥饿20 口渴20 | 恢复：体力25 生命10";
                heavyDesc = "消耗：饥饿40 口渴40 | 恢复：体力50 生命30";
                break;
            case "小木屋":
                lightDesc = "消耗：饥饿15 口渴15 | 恢复：体力35 生命20";
                heavyDesc = "消耗：饥饿30 口渴30 | 恢复：体力70 生命50";
                break;
            case "小石屋":
                lightDesc = "消耗：饥饿10 口渴10 | 恢复：体力45 生命30";
                heavyDesc = "消耗：饥饿20 口渴20 | 恢复：体力90 生命70";
                break;
            case "砖瓦屋":
                lightDesc = "消耗：饥饿5 口渴5 | 恢复：体力50 生命40";
                heavyDesc = "消耗：饥饿10 口渴10 | 恢复：体力100 生命100";
                break;
            default: // 野外
                lightDesc = "消耗：饥饿20 口渴20 | 恢复：体力20";
                heavyDesc = "消耗：饥饿40 口渴40 | 恢复：体力40";
                break;
        }
        tvRestLight.setText(lightDesc);
        tvRestHeavy.setText(heavyDesc);
    }

    // 更新休整状态提示
    private void updateRestStatus() {
        Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
        // 安全获取值，避免 null 导致崩溃
        int currentHour = getIntValue(userStatus.get("game_hour"), 0);
        long lastRestTime = getLongValue(userStatus.get("last_rest_time"), 0);
        long currentTime = System.currentTimeMillis();
        boolean isCooling = (lastRestTime != 0 && currentTime - lastRestTime < REST_COOLDOWN);

        // 小憩无时间限制，仅检查冷却
        if (isCooling) {
            long remaining = REST_COOLDOWN - (currentTime - lastRestTime);
            long minutes = remaining / (60 * 1000);
            long seconds = (remaining % (60 * 1000)) / 1000;
            tvTip.setText(String.format("休整冷却中，剩余 %d分%d秒", minutes, seconds));
            btnRestLight.setEnabled(false);
            btnRestHeavy.setEnabled(false);
        } else {
            // 安心休整时间限制提示
            if (currentHour >= 18 && currentHour <= 23) {
                tvTip.setText("可以进行安心休整（18:00-23:00）");
                btnRestHeavy.setEnabled(true);
            } else if (currentHour >= 0 && currentHour < 6) {
                tvTip.setText("当前时间（0:00-6:00）不可安心休整");
                btnRestHeavy.setEnabled(false);
            } else {
                tvTip.setText("仅18:00-23:00可进行安心休整");
                btnRestHeavy.setEnabled(false);
            }
            btnRestLight.setEnabled(true); // 小憩始终可执行（无时间限制）
        }
    }

    // 执行休整逻辑
    private void performRest(RestType type) {
        new AsyncTask<Void, Void, Boolean>() {
            private String toastMsg;
            private RandomEventManager.RandomEvent randomEvent = null;

            @Override
            protected Boolean doInBackground(Void... voids) {
                // 修复：确保userStatus非空，避免空指针
                Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
                if (userStatus == null) {
                    userStatus = new HashMap<>();
                }

                int currentHour = getIntValue(userStatus.get("game_hour"), 0);
                long lastRestTime = getLongValue(userStatus.get("last_rest_time"), 0);
                long currentTime = System.currentTimeMillis();

                // 检查冷却
                if (lastRestTime != 0 && currentTime - lastRestTime < REST_COOLDOWN) {
                    toastMsg = "休整冷却中，请稍后再试";
                    return false;
                }

                // 修复：安心休整时间判断冲突（18:00-23:00有效）
                if (type == RestType.HEAVY) {
                    if (currentHour < 18 || currentHour > 23) {
                        toastMsg = "仅18:00-23:00可进行安心休整";
                        return false;
                    }
                }

                // 获取当前状态（添加默认值避免类型转换错误）
                int hunger = getIntValue(userStatus.get("hunger"), 0);
                int thirst = getIntValue(userStatus.get("thirst"), 0);
                int stamina = getIntValue(userStatus.get("stamina"), 0);
                int life = getIntValue(userStatus.get("life"), 0);
                int currentDay = getIntValue(userStatus.get("game_day"), 0);

                // 确保庇护所类型有默认值（默认野外）
                String currentShelterType = (String) userStatus.get("shelter_type");
                if (currentShelterType == null || currentShelterType.isEmpty()) {
                    currentShelterType = "野外";
                }

                // 检查是否有篝火建筑
                boolean hasCampfire = dbHelper.getBuildingCount(userId, Constant.BUILDING_CAMPFIRE) > 0;

                // 处理随机事件
                String restTypeString = type == RestType.LIGHT ? "LIGHT" : "HEAVY";
                randomEvent = RandomEventManager.handleRestEvent(userId, restTypeString, currentShelterType, currentHour, hasCampfire);
                
                // 应用随机事件效果
                if (randomEvent != null) {
                    RandomEventManager.applyEventEffects(userId, randomEvent);
                    
                    // 如果是狼群袭击，重新获取生命值
                    if (randomEvent.type == RandomEventManager.EventType.WOLF_ATTACK) {
                        userStatus = dbHelper.getUserStatus(userId);
                        life = getIntValue(userStatus.get("life"), life);
                    }
                }

                // 根据庇护所类型和休整类型计算消耗和恢复
                int hungerCost, thirstCost, staminaRecover, lifeRecover;
                int newHour = currentHour;
                int newDay = currentDay;

                switch (currentShelterType) {
                    case "茅草屋":
                        if (type == RestType.LIGHT) {
                            hungerCost = 20; thirstCost = 20; staminaRecover = 25; lifeRecover = 10; newHour += 3;
                        } else {
                            hungerCost = 40; thirstCost = 40; staminaRecover = 50; lifeRecover = 30; newHour = 6; newDay += 1;
                        }
                        break;
                    case "小木屋":
                        if (type == RestType.LIGHT) {
                            hungerCost = 15; thirstCost = 15; staminaRecover = 35; lifeRecover = 20; newHour += 3;
                        } else {
                            hungerCost = 30; thirstCost = 30; staminaRecover = 70; lifeRecover = 50; newHour = 6; newDay += 1;
                        }
                        break;
                    case "小石屋":
                        if (type == RestType.LIGHT) {
                            hungerCost = 10; thirstCost = 10; staminaRecover = 45; lifeRecover = 30; newHour += 3;
                        } else {
                            hungerCost = 20; thirstCost = 20; staminaRecover = 90; lifeRecover = 70; newHour = 6; newDay += 1;
                        }
                        break;
                    case "砖瓦屋":
                        if (type == RestType.LIGHT) {
                            hungerCost = 5; thirstCost = 5; staminaRecover = 50; lifeRecover = 40; newHour += 3;
                        } else {
                            hungerCost = 10; thirstCost = 10; staminaRecover = 100; lifeRecover = 100; newHour = 6; newDay += 1;
                        }
                        break;
                    default: // 野外
                        if (type == RestType.LIGHT) {
                            hungerCost = 20; thirstCost = 20; staminaRecover = 20; lifeRecover = 0; newHour += 3;
                        } else {
                            hungerCost = 40; thirstCost = 40; staminaRecover = 40; lifeRecover = 0; newHour = 6; newDay += 1;
                        }
                        break;
                }

                // 处理跨天（小憩+3小时可能超过24点）
                if (newHour >= 24) {
                    newHour -= 24;
                    newDay += 1;
                }

                // 检查状态是否足够
                if (hunger < hungerCost || thirst < thirstCost) {
                    toastMsg = "状态不足，无法进行休整（饥饿需" + hungerCost + "，口渴需" + thirstCost + "）";
                    return false;
                }

                // 计算新状态（限制上下限）
                int newHunger = Math.max(0, hunger - hungerCost);
                int newThirst = Math.max(0, thirst - thirstCost);
                int newStamina = Math.min(100, stamina + staminaRecover);
                int newLife = Math.min(100, life + lifeRecover);

                // 更新数据库
                Map<String, Object> updates = new HashMap<>();
                updates.put("hunger", newHunger);
                updates.put("thirst", newThirst);
                updates.put("stamina", newStamina);
                updates.put("life", newLife);
                updates.put("last_rest_time", currentTime);
                updates.put("game_hour", newHour);
                updates.put("game_day", newDay);

                dbHelper.updateUserStatus(userId, updates);
                
                // 构建消息
                if (randomEvent != null) {
                    toastMsg = type == RestType.LIGHT ? "小憩成功！\n" : "安心休整成功！\n";
                    toastMsg += randomEvent.description;
                } else {
                    toastMsg = type == RestType.LIGHT ? "小憩成功啦！" : "安心休整成功啦！";
                }
                
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                showToast(toastMsg);
                if (success) {
                    finish();
                } else {
                    updateRestStatus(); // 刷新冷却状态显示
                }
            }
        }.execute();
    }

    private int getIntValue(Object obj, int defaultValue) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Long) {
            return ((Long) obj).intValue();
        }
        return defaultValue;
    }

    private long getLongValue(Object obj, long defaultValue) {
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        return defaultValue;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRestStatus(); // 重新进入时刷新状态
    }

    /**
     * 显示Toast消息
     * @param message 要显示的消息
     */
    public void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查是否从基地进入
     */
    protected boolean isFromBase() {
        return getIntent().getBooleanExtra("from_base", false);
    }

    /**
     * 禁用系统返回键，只允许使用按钮返回
     */
    @Override
    public void onBackPressed() {
        // 空实现，禁用系统返回功能
        // 用户只能通过页面上的返回按钮返回
    }
}