package com.example.myapplication3;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class SaveData {
    public int currentX;
    public int currentY;
    public int life;
    public int hunger;
    public int thirst;
    public int stamina;
    public int gold;
    public int backpackCap;
    public String currentEquip;
    public int gameHour;
    public int gameDay;
    public int temperature;
    public int hopePoints;  // 修复：补充分号

    // 背包物品（物品类型 -> 数量）
    public Map<String, Integer> backpackItems;
    // 物品耐久度（物品类型 -> 耐久度）
    public Map<String, Integer> itemDurability;
    // 已装备物品列表（装备类型）
    public List<String> equippedItems;

    /**
     * 从用户状态构建存档数据（包含安全类型转换）
     */
    public static SaveData fromUserStatus(Map<String, Object> userStatus,
                                          Map<String, Integer> backpack,
                                          Map<String, Integer> durability,
                                          List<String> equipped) {
        SaveData data = new SaveData();

        // 基础状态赋值（使用安全转换，避免null或类型错误）
        data.currentX = getIntValue(userStatus.get("current_x"), 1);
        data.currentY = getIntValue(userStatus.get("current_y"), 1);
        data.life = getIntValue(userStatus.get("life"), Constant.INIT_LIFE);
        data.hunger = getIntValue(userStatus.get("hunger"), Constant.INIT_HUNGER);
        data.thirst = getIntValue(userStatus.get("thirst"), Constant.INIT_THIRST);
        data.stamina = getIntValue(userStatus.get("stamina"), Constant.INIT_STAMINA);
        data.gold = getIntValue(userStatus.get("gold"), 0);
        data.backpackCap = getIntValue(userStatus.get("backpack_cap"), 10);
        data.gameHour = getIntValue(userStatus.get("game_hour"), 6);
        data.gameDay = getIntValue(userStatus.get("game_day"), 1);
        data.temperature = getIntValue(userStatus.get("temperature"), Constant.TEMPERATURE_DEFAULT);
        data.hopePoints = getIntValue(userStatus.get("hope_points"), 0);  // 补充hopePoints赋值

        // 装备相关赋值
        data.currentEquip = equipped != null && !equipped.isEmpty() ? equipped.get(0) : "无";  // 假设第一个为当前装备
        data.equippedItems = equipped;

        // 背包和耐久度赋值
        data.backpackItems = backpack;
        data.itemDurability = durability;

        return data;
    }

    /**
     * 安全获取int值，避免null或类型转换错误
     */
    private static int getIntValue(Object obj, int defaultValue) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return defaultValue;
    }

    // Getter方法
    public Map<String, Object> getUserStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("current_x", currentX);
        status.put("current_y", currentY);
        status.put("life", life);
        status.put("hunger", hunger);
        status.put("thirst", thirst);
        status.put("stamina", stamina);
        status.put("gold", gold);
        status.put("backpack_cap", backpackCap);
        status.put("game_hour", gameHour);
        status.put("game_day", gameDay);
        status.put("temperature", temperature);
        status.put("hope_points", hopePoints);
        return status;
    }

    public Map<String, Integer> getBackpack() {
        return backpackItems;
    }

    public List<Equipment> getEquipments() {
        List<Equipment> equipments = new java.util.ArrayList<>();
        if (equippedItems != null) {
            for (String equipType : equippedItems) {
                Integer durability = itemDurability != null ? itemDurability.get(equipType) : 100;
                equipments.add(new Equipment(0, equipType, durability != null ? durability : 100, 100, true));
            }
        }
        return equipments;
    }

    public List<AchievementItem> getAchievements() {
        // 暂不实现，返回空列表
        return new java.util.ArrayList<>();
    }
}