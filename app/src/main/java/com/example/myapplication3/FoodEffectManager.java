package com.example.myapplication3;

import java.util.HashMap;
import java.util.Map;

/**
 * 食物效果管理类：集中管理所有食物的恢复属性及特殊效果
 */
public class FoodEffectManager {

    // 食物效果数据类
    public static class FoodEffect {
        public int life;       // 生命恢复值（可为正负）
        public int hunger;     // 饥饿恢复值（正值减少饥饿）
        public int thirst;     // 口渴恢复值（正值减少口渴）
        public int stamina;    // 体力恢复值
        public String special; // 特殊效果描述（如体温变化、持续恢复等）

        public FoodEffect(int life, int hunger, int thirst, int stamina, String special) {
            this.life = life;
            this.hunger = hunger;
            this.thirst = thirst;
            this.stamina = stamina;
            this.special = special;
        }
    }

    // 食物与效果的映射表（键值对应ItemConstants中的物品常量）
    private static final Map<String, FoodEffect> FOOD_EFFECT_MAP = new HashMap<>();

    // 静态初始化所有食物效果
    static {
        // 基础食物
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_WATER, new FoodEffect(-5, 0, 5, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_BERRY, new FoodEffect(0, 5, 5, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_MEDICINE, new FoodEffect(10, 0, 0, 5, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_APPLE, new FoodEffect(0, 10, 5, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_ACORN, new FoodEffect(0, 15, -5, 5, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_ICE, new FoodEffect(-5, 0, 10, 0, "体温-5"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_SNOW_LOTUS, new FoodEffect(20, 0, 0, 0, "体温设置到默认体温"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_FISH, new FoodEffect(-10, 10, -5, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_CACTUS_FRUIT, new FoodEffect(-5, 10, 5, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_DRIED_BREAD, new FoodEffect(0, 20, -10, 10, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_HERB, new FoodEffect(20, 0, 0, 0, "每小时生命+5，持续5小时"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_SHELL, new FoodEffect(-5, 5, -5, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_MUSHROOM, new FoodEffect(-10, 5, 0, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_TRUFFLE, new FoodEffect(-5, 10, 5, 5, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_KELP, new FoodEffect(-5, 5, 5, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_COCONUT, new FoodEffect(10, 10, 10, 10, "体温-5"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_CRAWFISH, new FoodEffect(-20, 5, 0, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_RICE, new FoodEffect(-5, 5, -5, 5, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_CARROT, new FoodEffect(0, 5, 5, 5, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_POTATO, new FoodEffect(-5, 5, -5, 5, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_BEET, new FoodEffect(0, 5, 5, 0, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_SPINACH, new FoodEffect(0, 5, 0, 5, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_CORN, new FoodEffect(0, 5, 0, 5, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_HONEY, new FoodEffect(5, 10, -5, 10, "无"));

        // 烹饪食品
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_GRILLED_FISH, new FoodEffect(0, 30, -10, 30, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_FISH_SOUP, new FoodEffect(0, 20, 20, 20, "体温+10"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_FRUIT_PIE, new FoodEffect(0, 30, 10, 30, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_ADVANCED_HERB, new FoodEffect(40, 0, 0, 10, "每小时生命+5，持续10小时"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_MUSHROOM_SOUP, new FoodEffect(10, 40, 20, 10, "体温+5"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_KELP_SOUP, new FoodEffect(0, 15, 20, 10, "体温+5"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_GRILLED_CRAWFISH, new FoodEffect(0, 20, 0, 10, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_ICY_SNOW_LOTUS_COCONUT, new FoodEffect(45, 35, 60, 30, "体温-5"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_HONEY_APPLE_SLICE, new FoodEffect(20, 60, 15, 40, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_RICE_PORRIDGE, new FoodEffect(10, 40, 40, 30, "每小时生命+5，持续5小时"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_KELP_WINTER_MELON_SOUP, new FoodEffect(20, 60, 60, 40, "体温+10"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_ROASTED_POTATO, new FoodEffect(15, 40, 10, 30, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_FRUIT_SMOOTHIE, new FoodEffect(15, 50, 60, 30, "体温-10"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_CRAWFISH_SHELL_SOUP, new FoodEffect(10, 40, 30, 30, "每小时生命+5，持续5小时"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_STEAMED_CORN, new FoodEffect(10, 35, 15, 25, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_MUSHROOM_TRUFFLE_FRY, new FoodEffect(20, 55, 20, 35, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_BERRY_HONEY_BREAD, new FoodEffect(25, 70, 20, 50, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_BEET_HONEY_DRINK, new FoodEffect(15, 45, 50, 35, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_BOILED_SPINACH, new FoodEffect(20, 30, 40, 25, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_ROASTED_ACORN, new FoodEffect(15, 60, 0, 30, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_CACTUS_FRUIT_ICE_DRINK, new FoodEffect(10, 40, 70, 25, "体温-10"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_CARROT_POTATO_SOUP, new FoodEffect(25, 55, 50, 40, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_COCONUT_BERRY_DRINK, new FoodEffect(30, 50, 60, 40, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_TRUFFLE_MUSHROOM_SOUP, new FoodEffect(35, 65, 40, 45, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_APPLE_HONEY_DRINK, new FoodEffect(20, 55, 40, 40, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_KELP_FISH_SOUP, new FoodEffect(25, 60, 50, 45, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_WINTER_MELON_CRAWFISH_SOUP, new FoodEffect(30, 65, 60, 50, "无"));
        FOOD_EFFECT_MAP.put(ItemConstants.ITEM_DARK_FOOD, new FoodEffect(-5, 5, 5, 5, "无"));
    }

    /**
     * 获取食物效果
     * @param itemType 物品类型（对应ItemConstants中的常量）
     * @return 食物效果对象，若不存在则返回null
     */
    public static FoodEffect getFoodEffect(String itemType) {
        return FOOD_EFFECT_MAP.get(itemType);
    }
}
