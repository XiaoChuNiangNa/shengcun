package com.example.myapplication3;

import android.util.Log;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 随机事件管理器：处理游戏中各种随机事件
 */
public class RandomEventManager {
    private static final String TAG = "RandomEventManager";
    
    // 事件类型枚举
    public enum EventType {
        WOLF_ATTACK,         // 狼群袭击
        TRAVELING_MERCHANT,  // 流浪商人拜访
        SNAKE_BITE,          // 毒蛇袭击
        HIDDEN_CHEST,        // 隐藏宝箱
        HEAVY_RAIN,          // 暴雨
        CHAIN_COLLECTION,    // 连锁采集
        POOR_QUALITY_TOOL,   // 劣质工具
        GET_LOST             // 迷路
    }
    
    // 事件数据类
    public static class RandomEvent {
        public EventType type;
        public String description; // 事件描述
        public Map<String, Object> effects; // 事件效果
        
        public RandomEvent(EventType type, String description, Map<String, Object> effects) {
            this.type = type;
            this.description = description;
            this.effects = effects;
        }
    }
    
    // 状态效果数据类
    public static class StatusEffect {
        public String type; // 效果类型
        public int value;   // 效果值
        public int duration; // 持续时间（游戏小时）
        
        public StatusEffect(String type, int value, int duration) {
            this.type = type;
            this.value = value;
            this.duration = duration;
        }
    }
    
    // 存储用户的状态效果（key: userId, value: 效果列表）
    private static final Map<Integer, List<StatusEffect>> USER_STATUS_EFFECTS = new ConcurrentHashMap<>();
    
    // 流浪商人状态（key: userId, value: 是否启用贸易）
    private static final Map<Integer, Boolean> TRAVELING_MERCHANT_STATUS = new ConcurrentHashMap<>();
    
    /**
     * 处理休息事件的随机事件
     */
    public static RandomEvent handleRestEvent(int userId, String restType, String shelterType, 
                                            int gameHour, boolean hasCampfire) {
        Random random = new Random();
        
        // 夜间狼群袭击检查
        if (gameHour >= 18 || gameHour < 6) {
            // 庇护所内未制造篝火时选择安心休整
            if (restType.equals("HEAVY") && !shelterType.equals("野外") && !hasCampfire) {
                if (random.nextDouble() < 0.5) { // 50%概率
                    return createWolfAttackEvent();
                }
            }
            // 野外选择安心休整
            else if (restType.equals("HEAVY") && shelterType.equals("野外")) {
                // 100%概率
                return createWolfAttackEvent();
            }
        }
        
        // 流浪商人拜访检查（小憩，庇护所内，9:00-15:00）
        if (restType.equals("LIGHT") && !shelterType.equals("野外") && 
            gameHour >= 9 && gameHour <= 15) {
            if (random.nextDouble() < 0.1) { // 10%概率
                return createTravelingMerchantEvent();
            }
        }
        
        return null;
    }
    
    /**
     * 处理采集事件的随机事件
     */
    public static RandomEvent handleCollectEvent(int userId, String areaType) {
        Random random = new Random();
        
        // 毒蛇袭击检查（树林或针叶林）
        if (areaType.equals("树林") || areaType.equals("针叶林")) {
            if (random.nextDouble() < 0.1) { // 10%概率
                return createSnakeBiteEvent();
            }
        }
        
        // 隐藏宝箱检查
        if (areaType.equals("岩石区") || areaType.equals("雪山") || areaType.equals("雪原")) {
            if (random.nextDouble() < 0.05) { // 5%概率
                return createHiddenChestEvent();
            }
        } else if (areaType.equals("沙漠") || areaType.equals("草原")) {
            if (random.nextDouble() < 0.01) { // 1%概率
                return createHiddenChestEvent();
            }
        }
        
        // 连锁采集检查
        if (random.nextDouble() < 0.05) { // 5%概率
            return createChainCollectionEvent();
        }
        
        // 劣质工具检查
        if (random.nextDouble() < 0.05) { // 5%概率
            return createPoorQualityToolEvent();
        }
        
        return null;
    }
    
    /**
     * 处理移动事件的随机事件
     */
    public static RandomEvent handleMoveEvent(int userId, String terrainType) {
        Random random = new Random();
        
        // 暴雨检查
        if (random.nextDouble() < 0.1) { // 10%概率
            return createHeavyRainEvent();
        }
        
        // 迷路检查
        if (random.nextDouble() < 0.05) { // 5%概率
            return createGetLostEvent();
        }
        
        return null;
    }
    
    /**
     * 创建狼群袭击事件
     */
    private static RandomEvent createWolfAttackEvent() {
        Map<String, Object> effects = new HashMap<>();
        effects.put("life", -30);
        
        return new RandomEvent(EventType.WOLF_ATTACK, 
                               "你遇到了一群野狼，损失了一些生命值！", 
                               effects);
    }
    
    /**
     * 创建流浪商人拜访事件
     */
    private static RandomEvent createTravelingMerchantEvent() {
        Map<String, Object> effects = new HashMap<>();
        effects.put("merchant_available", true);
        
        return new RandomEvent(EventType.TRAVELING_MERCHANT, 
                               "有位流浪商人来拜访你咯", 
                               effects);
    }
    
    /**
     * 创建毒蛇袭击事件
     */
    private static RandomEvent createSnakeBiteEvent() {
        Map<String, Object> effects = new HashMap<>();
        effects.put("status_effect", new StatusEffect("SNAKE_BITE", -10, 4));
        
        return new RandomEvent(EventType.SNAKE_BITE, 
                               "你被毒蛇咬伤了！", 
                               effects);
    }
    
    /**
     * 创建隐藏宝箱事件
     */
    private static RandomEvent createHiddenChestEvent() {
        Map<String, Object> effects = new HashMap<>();
        // 随机获得大量资源
        String[] resources = {"黄金", "钻石", "高级草药", "稀有材料"};
        String randomResource = resources[new Random().nextInt(resources.length)];
        effects.put("resource_bonus", randomResource);
        effects.put("bonus_amount", new Random().nextInt(5) + 3); // 3-7个
        
        return new RandomEvent(EventType.HIDDEN_CHEST, 
                               "你发现了一个隐藏的宝箱！", 
                               effects);
    }
    
    /**
     * 创建暴雨事件
     */
    private static RandomEvent createHeavyRainEvent() {
        Map<String, Object> effects = new HashMap<>();
        effects.put("status_effect", new StatusEffect("HEAVY_RAIN", 0, 10));
        
        return new RandomEvent(EventType.HEAVY_RAIN, 
                               "突然下起了暴雨！", 
                               effects);
    }
    
    /**
     * 创建连锁采集事件
     */
    private static RandomEvent createChainCollectionEvent() {
        Map<String, Object> effects = new HashMap<>();
        effects.put("resource_multiplier", 2);
        
        return new RandomEvent(EventType.CHAIN_COLLECTION, 
                               "你发现了一些额外的资源！", 
                               effects);
    }
    
    /**
     * 创建劣质工具事件
     */
    private static RandomEvent createPoorQualityToolEvent() {
        Map<String, Object> effects = new HashMap<>();
        effects.put("tool_durability", -5);
        
        return new RandomEvent(EventType.POOR_QUALITY_TOOL, 
                               "你的工具意外损坏了！", 
                               effects);
    }
    
    /**
     * 创建迷路事件
     */
    private static RandomEvent createGetLostEvent() {
        Map<String, Object> effects = new HashMap<>();
        effects.put("time_penalty", 5);
        
        return new RandomEvent(EventType.GET_LOST, 
                               "你迷路了！", 
                               effects);
    }
    
    /**
     * 应用事件效果
     */
    public static void applyEventEffects(int userId, RandomEvent event) {
        if (event == null) return;
        
        // 修复：使用MyApplication的Context而不是null
        DBHelper dbHelper = DBHelper.getInstance(MyApplication.getAppContext());
        Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
        
        switch (event.type) {
            case WOLF_ATTACK:
                // 减少生命值
                int currentLife = (int) userStatus.get("life");
                int newLife = Math.max(0, currentLife - 30);
                dbHelper.updateUserStatus(userId, Collections.singletonMap("life", newLife));
                Log.i(TAG, "狼群袭击：生命值减少30，当前生命：" + newLife);
                break;
                
            case TRAVELING_MERCHANT:
                // 启用流浪商人贸易
                TRAVELING_MERCHANT_STATUS.put(userId, true);
                Log.i(TAG, "流浪商人拜访：启用贸易功能");
                break;
                
            case SNAKE_BITE:
                // 添加毒蛇咬伤状态效果
                StatusEffect snakeEffect = (StatusEffect) event.effects.get("status_effect");
                addStatusEffect(userId, snakeEffect);
                Log.i(TAG, "毒蛇袭击：添加持续伤害效果");
                break;
                
            case HIDDEN_CHEST:
                // 添加额外资源到背包
                String resource = (String) event.effects.get("resource_bonus");
                int amount = (int) event.effects.get("bonus_amount");
                dbHelper.updateBackpackItem(userId, resource, amount);
                Log.i(TAG, "隐藏宝箱：获得" + resource + " ×" + amount);
                break;
                
            case HEAVY_RAIN:
                // 添加暴雨状态效果
                StatusEffect rainEffect = (StatusEffect) event.effects.get("status_effect");
                addStatusEffect(userId, rainEffect);
                Log.i(TAG, "暴雨：添加感冒效果");
                break;
                
            case CHAIN_COLLECTION:
                // 资源翻倍效果在当前采集操作中处理
                Log.i(TAG, "连锁采集：资源获取翻倍");
                break;
                
            case POOR_QUALITY_TOOL:
                // 减少工具耐久度
                String currentTool = (String) userStatus.get("current_equip");
                if (currentTool != null && !currentTool.equals("无")) {
                    dbHelper.updateDurability(userId, currentTool, -5);
                    int currentDurability = dbHelper.getDurability(userId, currentTool);
                    Log.i(TAG, "劣质工具：" + currentTool + "耐久度减少5，当前耐久：" + currentDurability);
                }
                break;
                
            case GET_LOST:
                // 增加移动时间消耗
                Log.i(TAG, "迷路：移动时间增加5小时");
                // 这个效果在移动处理中应用
                break;
        }
    }
    
    /**
     * 添加状态效果
     */
    private static void addStatusEffect(int userId, StatusEffect effect) {
        USER_STATUS_EFFECTS.computeIfAbsent(userId, k -> new ArrayList<>())
                          .add(effect);
    }
    
    /**
     * 每小时处理状态效果
     */
    public static void onHourPassed(int userId) {
        List<StatusEffect> effects = USER_STATUS_EFFECTS.get(userId);
        if (effects == null || effects.isEmpty()) return;
        
        // 修复：使用MyApplication的Context而不是null
        DBHelper dbHelper = DBHelper.getInstance(MyApplication.getAppContext());
        Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
        
        Iterator<StatusEffect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            
            switch (effect.type) {
                case "SNAKE_BITE":
                    // 每小时减少生命值
                    int currentLife = (int) userStatus.get("life");
                    int newLife = Math.max(0, currentLife + effect.value);
                    dbHelper.updateUserStatus(userId, Collections.singletonMap("life", newLife));
                    Log.i(TAG, "毒蛇咬伤效果：生命值减少" + (-effect.value) + "，当前生命：" + newLife);
                    break;
                    
                case "HEAVY_RAIN":
                    // 暴雨效果，在采集时增加额外消耗
                    Log.i(TAG, "暴雨感冒效果：剩余时间" + effect.duration + "小时");
                    break;
            }
            
            // 减少持续时间
            effect.duration--;
            
            // 持续时间结束，移除效果
            if (effect.duration <= 0) {
                iterator.remove();
                Log.i(TAG, "状态效果结束：" + effect.type);
            }
        }
    }
    
    /**
     * 检查是否有暴雨效果
     */
    public static boolean hasHeavyRainEffect(int userId) {
        List<StatusEffect> effects = USER_STATUS_EFFECTS.get(userId);
        if (effects != null) {
            for (StatusEffect effect : effects) {
                if ("HEAVY_RAIN".equals(effect.type)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检查流浪商人是否可用
     */
    public static boolean isTravelingMerchantAvailable(int userId) {
        return TRAVELING_MERCHANT_STATUS.getOrDefault(userId, false);
    }
    
    /**
     * 禁用流浪商人贸易（离开庇护所时）
     */
    public static void disableTravelingMerchant(int userId) {
        TRAVELING_MERCHANT_STATUS.put(userId, false);
        Log.i(TAG, "离开庇护所：禁用流浪商人贸易");
    }
    
    /**
     * 获取连锁采集倍数
     */
    public static int getCollectionMultiplier(int userId, RandomEvent event) {
        if (event != null && event.type == EventType.CHAIN_COLLECTION) {
            return (int) event.effects.get("resource_multiplier");
        }
        return 1;
    }
    
    /**
     * 获取迷路时间惩罚
     */
    public static int getLostTimePenalty(int userId, RandomEvent event) {
        if (event != null && event.type == EventType.GET_LOST) {
            return (int) event.effects.get("time_penalty");
        }
        return 0;
    }
}