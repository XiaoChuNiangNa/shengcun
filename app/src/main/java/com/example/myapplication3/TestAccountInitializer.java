package com.example.myapplication3;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试账号初始属性配置类
 * 集中管理测试账号的所有初始数据
 */
public class TestAccountInitializer {

    /**
     * 获取测试账号的初始生存状态
     */
    public static Map<String, Object> getInitialStatus() {
        Map<String, Object> status = new HashMap<>();

        // 基础生存指标（保持100）
//        status.put("life", Constant.INIT_LIFE);
//        status.put("hunger", Constant.INIT_HUNGER);
//        status.put("thirst", Constant.INIT_THIRST);
//        status.put("stamina", Constant.INIT_STAMINA);

        //自定义生存指标
        status.put("life", 1000);
        status.put("hunger", 1000);
        status.put("thirst", 1000);
        status.put("stamina", 1000);

        // 资源与属性修改
        status.put("gold", 10000); // 初始金钱10000
        status.put("hope_points", 1000); // 希望点数1000
        status.put("backpack_cap", 10000); // 背包容量10000

        // 建筑不解锁
        status.put("is_synthesis_unlocked", 0);
        status.put("is_building_unlocked", 0);
        status.put("is_cooking_unlocked", 0);
        status.put("is_smelting_unlocked", 0);
        status.put("is_trading_unlocked", 0);
        status.put("is_sleep_unlocked", 0);

        // 初始位置（随机生成或固定值）
        int[] spawnPoint = chooseRandomSpawnPoint();
        status.put("current_x", spawnPoint[0]);
        status.put("current_y", spawnPoint[1]);

        // 时间与温度默认值
        status.put("game_hour", Constant.GAME_HOUR_DEFAULT);
        status.put("game_day", Constant.GAME_DAY_DEFAULT);
        status.put("temperature", Constant.TEMPERATURE_DEFAULT);

        return status;
    }

    /**
     * 获取测试账号的初始背包资源（所有资源数量99，无工具）
     */
    public static Map<String, Integer> getInitialBackpack() {
        Map<String, Integer> backpack = new HashMap<>();

        // 基础资源（非工具类）
        backpack.put(ItemConstants.ITEM_WEED, 99);
        backpack.put(ItemConstants.ITEM_BERRY, 99);
        backpack.put(ItemConstants.ITEM_APPLE, 99);
        backpack.put(ItemConstants.ITEM_HERB, 99);
        backpack.put(ItemConstants.ITEM_FIBER, 99);
        backpack.put(ItemConstants.ITEM_ICE, 99);
        backpack.put(ItemConstants.ITEM_WOOD, 99);
        backpack.put(ItemConstants.ITEM_HONEYCOMB, 99);
        backpack.put(ItemConstants.ITEM_ACORN, 99);
        backpack.put(ItemConstants.ITEM_VINE, 99);
        backpack.put(ItemConstants.ITEM_RESIN, 99);
        backpack.put(ItemConstants.ITEM_TRUFFLE, 99);
        backpack.put(ItemConstants.ITEM_STONE, 99);
        backpack.put(ItemConstants.ITEM_IRON_ORE, 99);
        backpack.put(ItemConstants.ITEM_GEM, 99);
        backpack.put(ItemConstants.ITEM_FLINT, 99);
        backpack.put(ItemConstants.ITEM_SULFUR, 99);
        backpack.put(ItemConstants.ITEM_COAL, 99);
        backpack.put(ItemConstants.ITEM_SNOW_LOTUS, 99);
        backpack.put(ItemConstants.ITEM_OBSIDIAN, 99);
        backpack.put(ItemConstants.ITEM_WATER, 99);
        backpack.put(ItemConstants.ITEM_FISH, 99);
        backpack.put(ItemConstants.ITEM_KELP, 99);
        backpack.put(ItemConstants.ITEM_SAND, 99);
        backpack.put(ItemConstants.ITEM_SHELL, 99);
        backpack.put(ItemConstants.ITEM_COCONUT, 99);
        backpack.put(ItemConstants.ITEM_CRAWFISH, 99);
        backpack.put(ItemConstants.ITEM_CACTUS_FRUIT, 99);
        backpack.put(ItemConstants.ITEM_MUSHROOM, 99);
        backpack.put(ItemConstants.ITEM_REED, 99);
        backpack.put(ItemConstants.ITEM_CLAY, 99);
        backpack.put(ItemConstants.ITEM_IRON_INGOT, 99);
        backpack.put(ItemConstants.ITEM_GOLD, 99);

        // 食材类资源
        backpack.put(ItemConstants.ITEM_RICE, 99);
        backpack.put(ItemConstants.ITEM_WINTER_MELON, 99);
        backpack.put(ItemConstants.ITEM_CORN, 99);
        backpack.put(ItemConstants.ITEM_BEET, 99);
        backpack.put(ItemConstants.ITEM_SPINACH, 99);
        backpack.put(ItemConstants.ITEM_CARROT, 99);
        backpack.put(ItemConstants.ITEM_POTATO, 99);

        // 合成类资源（非工具）
        backpack.put(ItemConstants.ITEM_MEDICINE, 99);
        backpack.put(ItemConstants.ITEM_HONEY, 99);
        backpack.put(ItemConstants.ITEM_GRASS_ROPE, 99);
        backpack.put(ItemConstants.ITEM_REINFORCED_ROPE, 99);
        backpack.put(ItemConstants.ITEM_HARD_ROPE, 99);
        backpack.put(ItemConstants.ITEM_WOODEN_PLANK, 99);
        backpack.put(ItemConstants.ITEM_NAIL, 99);
        backpack.put(ItemConstants.ITEM_GUNPOWDER, 99);
        backpack.put(ItemConstants.ITEM_WOODEN_BOAT, 99);
        backpack.put(ItemConstants.ITEM_STONE_BRICK, 99);
        backpack.put(ItemConstants.ITEM_CEMENT, 99);
        backpack.put(ItemConstants.ITEM_BRICK, 99);
        backpack.put(ItemConstants.ITEM_IRON_PLATE, 99);
        backpack.put(ItemConstants.ITEM_GLASS, 99);
        backpack.put(ItemConstants.ITEM_DIAMOND, 99);
        backpack.put(ItemConstants.ITEM_CHARCOAL, 99);
        backpack.put(ItemConstants.ITEM_CLAY_POT, 99);
        backpack.put(ItemConstants.ITEM_BOILED_WATER, 99);

        // 烹饪类资源
        backpack.put(ItemConstants.ITEM_GRILLED_FISH, 99);
        backpack.put(ItemConstants.ITEM_GRILLED_CRAWFISH, 99);
        backpack.put(ItemConstants.ITEM_FISH_SOUP, 99);
        backpack.put(ItemConstants.ITEM_MUSHROOM_SOUP, 99);
        backpack.put(ItemConstants.ITEM_KELP_SOUP, 99);
        backpack.put(ItemConstants.ITEM_FRUIT_PIE, 99);
        backpack.put(ItemConstants.ITEM_ADVANCED_HERB, 99);

        return backpack;
    }

    /**
     * 获取初始装备列表（空列表，无初始工具）
     */
    public static Map<String, Integer> getInitialEquipment() {
        return new HashMap<>(); // 无初始装备
    }

    /**
     * 随机生成初始出生点（复用原逻辑）
     */
    public static int[] chooseRandomSpawnPoint() {
        // 可根据实际需求实现随机坐标生成逻辑
        return new int[]{0, 0}; // 示例默认坐标
    }
}