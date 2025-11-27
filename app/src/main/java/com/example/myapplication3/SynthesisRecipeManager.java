package com.example.myapplication3;

import com.example.myapplication3.Constant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 合成配方管理类，集中存储和管理所有合成配方
public class SynthesisRecipeManager {

    // 获取所有合成配方列表
    public static List<Recipe> getAllSynthesisRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        // 添加所有合成配方


        // 斧头系列
        recipes.add(new Recipe(ItemConstants.EQUIP_STONE_AXE, createStoneAxeReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_IRON_AXE, createIronAxeReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_DIAMOND_AXE, createDiamondAxeReq()));

        // 镐子系列
        recipes.add(new Recipe(ItemConstants.EQUIP_STONE_PICKAXE, createStonePickaxeReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_IRON_PICKAXE, createIronPickaxeReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_DIAMOND_PICKAXE, createDiamondPickaxeReq()));

        // 镰刀系列
        recipes.add(new Recipe(ItemConstants.EQUIP_STONE_SICKLE, createStoneSickleReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_IRON_SICKLE, createIronSickleReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_DIAMOND_SICKLE, createDiamondSickleReq()));

        // 鱼竿系列
        recipes.add(new Recipe(ItemConstants.EQUIP_STONE_FISHING_ROD, createStoneFishingRodReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_IRON_FISHING_ROD, createIronFishingRodReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_DIAMOND_FISHING_ROD, createDiamondFishingRodReq()));
        
        // 铲子系列
        recipes.add(new Recipe(ItemConstants.EQUIP_STONE_SHOVEL, createStoneShovelReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_IRON_SHOVEL, createIronShovelReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_DIAMOND_SHOVEL, createDiamondShovelReq()));
        
        // 锤子系列
        recipes.add(new Recipe(ItemConstants.EQUIP_STONE_HAMMER, createStoneHammerReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_IRON_HAMMER, createIronHammerReq()));
        recipes.add(new Recipe(ItemConstants.EQUIP_DIAMOND_HAMMER, createDiamondHammerReq()));

        // 其他合成物品
        recipes.add(new Recipe(ItemConstants.ITEM_STONE, createStoneFromSmallStonesReq()));//小石子合成石头
        recipes.add(new Recipe(ItemConstants.ITEM_MEDICINE, createHerbReq()));//草药
        recipes.add(new Recipe(ItemConstants.ITEM_HONEY, createHoneyReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_GRASS_ROPE, createGrassRopeReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_REINFORCED_ROPE, createReinforcedRopeReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_HARD_ROPE, createHardRopeReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_WOODEN_PLANK, createWoodenPlankReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_NAIL, createNailReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_GUNPOWDER, createGunpowderReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_WOODEN_BOAT, createWoodenBoatReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_STONE_BRICK, createStoneBrickReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_CEMENT, createCementReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_BRICK, createBrickReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_IRON_PLATE, createIronPlateReq()));

        return recipes;
    }

    // 草药合成配方（3个药草合成1个草药）
    private static Map<String, Integer> createHerbReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_HERB, 3);
        return req;
    }

    // 石斧合成配方（3个石头+2个木头）
    private static Map<String, Integer> createStoneAxeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 3);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 铁斧合成配方（3个铁锭+2个木头）
    private static Map<String, Integer> createIronAxeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 3);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 钻石斧合成配方（3个钻石+2个木头）
    private static Map<String, Integer> createDiamondAxeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_DIAMOND, 3);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 石镐合成配方（2个石头+3个木头）
    private static Map<String, Integer> createStonePickaxeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 2);
        req.put(ItemConstants.ITEM_WOOD, 3);
        return req;
    }

    // 铁镐合成配方（2个铁锭+3个木头）
    private static Map<String, Integer> createIronPickaxeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 2);
        req.put(ItemConstants.ITEM_WOOD, 3);
        return req;
    }

    // 钻石镐合成配方（2个钻石+3个木头）
    private static Map<String, Integer> createDiamondPickaxeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_DIAMOND, 2);
        req.put(ItemConstants.ITEM_WOOD, 3);
        return req;
    }

    // 石镰刀合成配方（2个石头+2个木头）
    private static Map<String, Integer> createStoneSickleReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 2);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 铁镰刀合成配方（2个铁锭+2个木头）
    private static Map<String, Integer> createIronSickleReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 2);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 钻石镰刀合成配方（2个钻石+2个木头）
    private static Map<String, Integer> createDiamondSickleReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_DIAMOND, 2);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 石质鱼竿合成配方（2个石头+2个木头）
    private static Map<String, Integer> createStoneFishingRodReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 2);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 铁质鱼竿合成配方（2个铁锭+2个木头）
    private static Map<String, Integer> createIronFishingRodReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 2);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 钻石鱼竿合成配方（2个钻石+2个木头）
    private static Map<String, Integer> createDiamondFishingRodReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_DIAMOND, 2);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 蜂蜜合成配方（1个蜂巢+2个石头）
    private static Map<String, Integer> createHoneyReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_HONEYCOMB, 1);
        req.put(ItemConstants.ITEM_STONE, 2);
        return req;
    }

    // 草质绳索合成配方（2个纤维+2个藤蔓）
    private static Map<String, Integer> createGrassRopeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_FIBER, 2);
        req.put(ItemConstants.ITEM_VINE, 2);
        return req;
    }

    // 加固绳索合成配方（2个铁锭+3个纤维+3个藤蔓）
    private static Map<String, Integer> createReinforcedRopeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 2);
        req.put(ItemConstants.ITEM_FIBER, 3);
        req.put(ItemConstants.ITEM_VINE, 3);
        return req;
    }

    // 硬质绳索合成配方（5个铁锭+5个纤维+5个藤蔓）
    private static Map<String, Integer> createHardRopeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 5);
        req.put(ItemConstants.ITEM_FIBER, 5);
        req.put(ItemConstants.ITEM_VINE, 5);
        return req;
    }

    // 木板合成配方（5个木头+4个草质绳索）
    private static Map<String, Integer> createWoodenPlankReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WOOD, 5);
        req.put(ItemConstants.ITEM_GRASS_ROPE, 4);
        return req;
    }

    // 钉子合成配方（1个铁锭+1个燧石）
    private static Map<String, Integer> createNailReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 1);
        req.put(ItemConstants.ITEM_FLINT, 1);
        return req;
    }

    // 火药合成配方（3个硫磺+3个树脂+3个煤炭+1个燧石）
    private static Map<String, Integer> createGunpowderReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_SULFUR, 3);
        req.put(ItemConstants.ITEM_RESIN, 3);
        req.put(ItemConstants.ITEM_COAL, 3);
        req.put(ItemConstants.ITEM_FLINT, 1);
        return req;
    }

    // 木船合成配方（20个木板+10个钉子）
    private static Map<String, Integer> createWoodenBoatReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WOODEN_PLANK, 20);
        req.put(ItemConstants.ITEM_NAIL, 10);
        return req;
    }

    // 石砖合成配方（10个石头+5个黏土）
    private static Map<String, Integer> createStoneBrickReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 10);
        req.put(ItemConstants.ITEM_CLAY, 5);
        return req;
    }

    // 水泥合成配方（5个石头+10个黏土）
    private static Map<String, Integer> createCementReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 5);
        req.put(ItemConstants.ITEM_CLAY, 10);
        return req;
    }

    // 砖块合成配方（20个黏土）
    private static Map<String, Integer> createBrickReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_CLAY, 20);
        return req;
    }

    // 铁板合成配方（10个铁锭）
    private static Map<String, Integer> createIronPlateReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 10);
        return req;
    }

    // 小石子合成石头配方（4个小石子合成1个石头）
    private static Map<String, Integer> createStoneFromSmallStonesReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_SMALL_STONE, 4);
        return req;
    }

    // 石铲合成配方（1个石头+2个木头）
    private static Map<String, Integer> createStoneShovelReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 1);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 铁铲合成配方（1个铁锭+2个木头）
    private static Map<String, Integer> createIronShovelReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 1);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 钻石铲合成配方（1个钻石+2个木头）
    private static Map<String, Integer> createDiamondShovelReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_DIAMOND, 1);
        req.put(ItemConstants.ITEM_WOOD, 2);
        return req;
    }

    // 石锤合成配方（3个石头+3个木头）
    private static Map<String, Integer> createStoneHammerReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 3);
        req.put(ItemConstants.ITEM_WOOD, 3);
        return req;
    }

    // 铁锤合成配方（3个铁锭+3个木头）
    private static Map<String, Integer> createIronHammerReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 3);
        req.put(ItemConstants.ITEM_WOOD, 3);
        return req;
    }

    // 钻石锤合成配方（3个钻石+3个木头）
    private static Map<String, Integer> createDiamondHammerReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_DIAMOND, 3);
        req.put(ItemConstants.ITEM_WOOD, 3);
        return req;
    }
}