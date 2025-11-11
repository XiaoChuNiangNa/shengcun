package com.example.myapplication3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 状态效果管理器：处理持续型状态效果（如每小时生命恢复）
 */
public class StatusEffectManager {
    // 持续效果数据类
    public static class PersistentEffect {
        public String type; // 效果类型：如"LIFE_REGEN"
        public int value;   // 每次恢复值
        public int duration; // 剩余持续时间（游戏小时）
        public int interval; // 触发间隔（游戏小时，固定为1）

        public PersistentEffect(String type, int value, int duration) {
            this.type = type;
            this.value = value;
            this.duration = duration;
            this.interval = 1;
        }
    }

    // 存储用户的持续效果（key: userId, value: 效果列表）
    private static final Map<Integer, List<PersistentEffect>> USER_EFFECTS = new ConcurrentHashMap<>();

    /**
     * 添加持续效果
     */
    public static void addPersistentEffect(int userId, String type, int value, int duration) {
        USER_EFFECTS.computeIfAbsent(userId, k -> new ArrayList<>())
                .add(new PersistentEffect(type, value, duration));
    }

    /**
     * 每小时触发一次持续效果
     * 需在游戏时间每增加1小时时调用
     */
    public static void onHourPassed(int userId) {
        // 首先处理状态效果管理器中的效果
        List<PersistentEffect> effects = USER_EFFECTS.get(userId);
        if (effects != null && !effects.isEmpty()) {
            DBHelper dbHelper = DBHelper.getInstance(null);
            Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
            int life = (int) userStatus.get("life");

            Iterator<PersistentEffect> iterator = effects.iterator();
            while (iterator.hasNext()) {
                PersistentEffect effect = iterator.next();
                // 处理生命恢复效果
                if ("LIFE_REGEN".equals(effect.type)) {
                    int newLife = Math.min(100, life + effect.value);
                    dbHelper.updateUserStatus(userId, Collections.singletonMap("life", newLife));
                    // 更新当前生命用于叠加效果
                    life = newLife;
                }
                // 减少持续时间
                effect.duration--;
                // 持续时间结束，移除效果
                if (effect.duration <= 0) {
                    iterator.remove();
                }
            }
        }
        
        // 然后处理随机事件管理器中的状态效果
        RandomEventManager.onHourPassed(userId);
    }
}