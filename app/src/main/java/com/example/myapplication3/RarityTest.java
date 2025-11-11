package com.example.myapplication3;

import android.util.Log;

import java.util.Map;

/**
 * 稀有度系统测试类
 * 用于验证稀有度系统是否正常工作
 */
public class RarityTest {
    
    private static final String TAG = "RarityTest";
    
    /**
     * 测试物品稀有度配置
     */
    public static void testItemRarities() {
        Log.d(TAG, "=== 物品稀有度配置测试 ===");
        
        // 测试普通物品
        testItemRarity(ItemConstants.ITEM_WEED, Rarity.COMMON);
        testItemRarity(ItemConstants.ITEM_BERRY, Rarity.COMMON);
        testItemRarity(ItemConstants.ITEM_APPLE, Rarity.COMMON);
        
        // 测试稀有物品
        testItemRarity(ItemConstants.ITEM_HERB, Rarity.RARE);
        testItemRarity(ItemConstants.ITEM_FLINT, Rarity.RARE);
        testItemRarity(ItemConstants.ITEM_COAL, Rarity.RARE);
        
        // 测试史诗物品
        testItemRarity(ItemConstants.ITEM_RESIN, Rarity.EPIC);
        testItemRarity(ItemConstants.ITEM_IRON_ORE, Rarity.EPIC);
        
        // 测试传说物品
        testItemRarity(ItemConstants.ITEM_TRUFFLE, Rarity.LEGENDARY);
        testItemRarity(ItemConstants.ITEM_SULFUR, Rarity.LEGENDARY);
        
        // 测试神话物品
        testItemRarity(ItemConstants.ITEM_SNOW_LOTUS, Rarity.MYTHICAL);
        testItemRarity(ItemConstants.ITEM_OBSIDIAN, Rarity.MYTHICAL);
        testItemRarity(ItemConstants.ITEM_GEM, Rarity.MYTHICAL);
        
        Log.d(TAG, "=== 稀有度统计信息 ===");
        Map<Rarity, Integer> stats = ItemRarityManager.getRarityStatistics();
        for (Map.Entry<Rarity, Integer> entry : stats.entrySet()) {
            Log.d(TAG, entry.getKey().getDisplayName() + ": " + entry.getValue() + " 个物品");
        }
    }
    
    /**
     * 测试难度对掉落概率的影响
     */
    public static void testDifficultyEffects() {
        Log.d(TAG, "=== 难度对掉落概率的影响测试 ===");
        
        String[] difficulties = {"easy", "normal", "hard"};
        String[] testItems = {
            ItemConstants.ITEM_WEED,      // 普通
            ItemConstants.ITEM_HERB,      // 稀有
            ItemConstants.ITEM_RESIN,     // 史诗
            ItemConstants.ITEM_TRUFFLE,   // 传说
            ItemConstants.ITEM_SNOW_LOTUS // 神话
        };
        
        for (String difficulty : difficulties) {
            Log.d(TAG, "难度: " + difficulty);
            for (String item : testItems) {
                double probability = ItemRarityManager.getItemAdjustedProbability(item, difficulty);
                Rarity rarity = ItemRarityManager.getItemRarity(item);
                Log.d(TAG, "  " + item + " [" + rarity.getDisplayName() + "]: " + 
                    String.format("%.1f%%", probability * 100));
            }
        }
    }
    
    /**
     * 测试区域稀有度统计
     */
    public static void testAreaRarityStatistics() {
        Log.d(TAG, "=== 区域稀有度统计测试 ===");
        
        String[] testAreas = {"草原", "树林", "岩石区", "雪山", "海洋"};
        
        for (String area : testAreas) {
            Map<Rarity, Integer> stats = AreaResourceManager.getInstance().getAreaRarityStatistics(area);
            Log.d(TAG, "区域: " + area);
            for (Map.Entry<Rarity, Integer> entry : stats.entrySet()) {
                if (entry.getValue() > 0) {
                    Log.d(TAG, "  " + entry.getKey().getDisplayName() + ": " + entry.getValue() + " 种物品");
                }
            }
        }
    }
    
    /**
     * 测试单个物品的稀有度
     */
    private static void testItemRarity(String itemName, Rarity expectedRarity) {
        Rarity actualRarity = ItemRarityManager.getItemRarity(itemName);
        boolean passed = (actualRarity == expectedRarity);
        String status = passed ? "✓" : "✗";
        
        Log.d(TAG, status + " " + itemName + " -> 期望: " + expectedRarity.getDisplayName() + 
            ", 实际: " + actualRarity.getDisplayName());
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        Log.d(TAG, "开始运行稀有度系统测试...");
        testItemRarities();
        testDifficultyEffects();
        testAreaRarityStatistics();
        Log.d(TAG, "稀有度系统测试完成!");
    }
}