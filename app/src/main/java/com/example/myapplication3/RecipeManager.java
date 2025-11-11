package com.example.myapplication3;

import com.example.myapplication3.Constant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 配方管理类，集中管理所有烹饪配方
public class RecipeManager {

    // 获取所有配方列表
    public static List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        // 添加所有配方
        recipes.add(new Recipe(ItemConstants.ITEM_GRILLED_FISH, createGrilledFishReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_GRILLED_CRAWFISH, createGrilledCrawfishReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_FISH_SOUP, createFishSoupReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_MUSHROOM_SOUP, createMushroomSoupReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_KELP_SOUP, createKelpSoupReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_FRUIT_PIE, createFruitPieReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_ADVANCED_HERB, createAdvancedHerbReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_ICY_SNOW_LOTUS_COCONUT, createIcySnowLotusCoconutReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_HONEY_APPLE_SLICE, createHoneyAppleSliceReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_RICE_PORRIDGE, createRicePorridgeReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_KELP_WINTER_MELON_SOUP, createKelpWinterMelonSoupReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_ROASTED_POTATO, createRoastedPotatoReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_FRUIT_SMOOTHIE, createFruitSmoothieReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_CRAWFISH_SHELL_SOUP, createCrawfishShellSoupReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_STEAMED_CORN, createSteamedCornReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_MUSHROOM_TRUFFLE_FRY, createMushroomTruffleFryReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_BERRY_HONEY_BREAD, createBerryHoneyBreadReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_BEET_HONEY_DRINK, createBeetHoneyDrinkReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_BOILED_SPINACH, createBoiledSpinachReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_ROASTED_ACORN, createRoastedAcornReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_CACTUS_FRUIT_ICE_DRINK, createCactusFruitIceDrinkReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_CARROT_POTATO_SOUP, createCarrotPotatoSoupReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_COCONUT_BERRY_DRINK, createCoconutBerryDrinkReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_TRUFFLE_MUSHROOM_SOUP, createTruffleMushroomSoupReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_APPLE_HONEY_DRINK, createAppleHoneyDrinkReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_KELP_FISH_SOUP, createKelpFishSoupReq()));
        recipes.add(new Recipe(ItemConstants.ITEM_WINTER_MELON_CRAWFISH_SOUP, createWinterMelonCrawfishSoupReq()));

        return recipes;
    }

    // 所有配方的具体定义（迁移自CookingActivity）
    private static Map<String, Integer> createGrilledFishReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_FISH, 3);
        return req;
    }

    private static Map<String, Integer> createGrilledCrawfishReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_CRAWFISH, 3);
        return req;
    }

    private static Map<String, Integer> createFishSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_FISH, 1);
        req.put(ItemConstants.ITEM_WATER, 2);
        return req;
    }

    private static Map<String, Integer> createMushroomSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_MUSHROOM, 1);
        req.put(ItemConstants.ITEM_TRUFFLE, 1);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createKelpSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_KELP, 1);
        req.put(ItemConstants.ITEM_WATER, 2);
        return req;
    }

    private static Map<String, Integer> createFruitPieReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_BERRY, 1);
        req.put(ItemConstants.ITEM_CACTUS_FRUIT, 1);
        req.put(ItemConstants.ITEM_APPLE, 1);
        return req;
    }

    private static Map<String, Integer> createAdvancedHerbReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_MEDICINE, 1);
        req.put(ItemConstants.ITEM_ACORN, 1);
        req.put(ItemConstants.ITEM_SNOW_LOTUS, 1);
        return req;
    }

    private static Map<String, Integer> createIcySnowLotusCoconutReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_SNOW_LOTUS, 1);
        req.put(ItemConstants.ITEM_COCONUT, 1);
        req.put(ItemConstants.ITEM_ICE, 1);
        return req;
    }

    private static Map<String, Integer> createHoneyAppleSliceReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_APPLE, 2);
        req.put(ItemConstants.ITEM_HONEY, 1);
        return req;
    }

    private static Map<String, Integer> createRicePorridgeReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_RICE, 2);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createKelpWinterMelonSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_KELP, 1);
        req.put(ItemConstants.ITEM_WINTER_MELON, 1);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createRoastedPotatoReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_POTATO, 3);
        return req;
    }

    private static Map<String, Integer> createFruitSmoothieReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_BERRY, 1);
        req.put(ItemConstants.ITEM_ICE, 2);
        return req;
    }

    private static Map<String, Integer> createCrawfishShellSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_CRAWFISH, 1);
        req.put(ItemConstants.ITEM_SHELL, 1);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createSteamedCornReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_CORN, 3);
        return req;
    }

    private static Map<String, Integer> createMushroomTruffleFryReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_MUSHROOM, 1);
        req.put(ItemConstants.ITEM_TRUFFLE, 1);
        req.put(ItemConstants.ITEM_MEDICINE, 1);
        return req;
    }

    private static Map<String, Integer> createBerryHoneyBreadReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_DRIED_BREAD, 1);
        req.put(ItemConstants.ITEM_BERRY, 1);
        req.put(ItemConstants.ITEM_HONEY, 1);
        return req;
    }

    private static Map<String, Integer> createBeetHoneyDrinkReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_BEET, 2);
        req.put(ItemConstants.ITEM_HONEY, 1);
        return req;
    }

    private static Map<String, Integer> createBoiledSpinachReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_SPINACH, 2);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createRoastedAcornReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_ACORN, 3);
        return req;
    }

    private static Map<String, Integer> createCactusFruitIceDrinkReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_CACTUS_FRUIT, 2);
        req.put(ItemConstants.ITEM_ICE, 1);
        return req;
    }

    private static Map<String, Integer> createCarrotPotatoSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_CARROT, 1);
        req.put(ItemConstants.ITEM_POTATO, 1);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createCoconutBerryDrinkReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_COCONUT, 1);
        req.put(ItemConstants.ITEM_BERRY, 1);
        req.put(ItemConstants.ITEM_ICE, 1);
        return req;
    }

    private static Map<String, Integer> createTruffleMushroomSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_TRUFFLE, 1);
        req.put(ItemConstants.ITEM_MUSHROOM, 1);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createAppleHoneyDrinkReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_APPLE, 1);
        req.put(ItemConstants.ITEM_HONEY, 1);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createKelpFishSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_KELP, 1);
        req.put(ItemConstants.ITEM_FISH, 1);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }

    private static Map<String, Integer> createWinterMelonCrawfishSoupReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WINTER_MELON, 1);
        req.put(ItemConstants.ITEM_CRAWFISH, 1);
        req.put(ItemConstants.ITEM_WATER, 1);
        return req;
    }
}