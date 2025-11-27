package com.example.myapplication3;

import java.util.List;

/**
 * 战利品箱系统测试类
 * 演示战利品箱系统的各种功能
 */
public class LootBoxTest {
    
    /**
     * 测试所有战利品箱
     */
    public static void testAllLootBoxes() {
        System.out.println("=" .repeat(60));
        System.out.println("战利品箱系统测试");
        System.out.println("=" .repeat(60));
        
        // 显示所有战利品箱
        System.out.println(LootBoxUtils.showAllLootBoxes());
        System.out.println();
        
        // 测试每种难度的战利品箱
        String[] difficulties = {"easy", "normal", "hard"};
        
        for (String difficulty : difficulties) {
            System.out.println("=".repeat(40));
            System.out.println("难度: " + difficulty.toUpperCase());
            System.out.println("=".repeat(40));
            
            // 测试小战利品箱
            testLootBox(1, "小战利品箱", difficulty);
            
            // 测试中战利品箱
            testLootBox(2, "中战利品箱", difficulty);
            
            // 测试大战利品箱
            testLootBox(3, "大战利品箱", difficulty);
            
            // 测试巨型战利品箱
            testLootBox(4, "巨型战利品箱", difficulty);
            
            // 测试终极战利品箱
            testLootBox(5, "终极战利品箱", difficulty);
            
            System.out.println();
        }
        
        // 显示价值比较
        System.out.println("=".repeat(40));
        System.out.println("价值比较");
        System.out.println("=".repeat(40));
        System.out.println(LootBoxUtils.compareAllBoxes("normal"));
        
        // 模拟批量开启
        System.out.println("\n".repeat(2));
        System.out.println("=".repeat(40));
        System.out.println("批量开启模拟");
        System.out.println("=".repeat(40));
        System.out.println(LootBoxUtils.simulateBoxOpening(3, 100, "normal"));
    }
    
    /**
     * 测试单个战利品箱
     * @param boxId 战利品箱ID
     * @param boxName 战利品箱名称
     * @param difficulty 难度
     */
    private static void testLootBox(int boxId, String boxName, String difficulty) {
        System.out.println("-".repeat(30));
        System.out.println(boxName + " (" + difficulty + ")");
        System.out.println("-".repeat(30));
        
        // 显示战利品箱详情
        System.out.println(LootBoxUtils.getFullBoxInfo(boxId));
        
        // 开启战利品箱
        List<Item> items;
        try {
            // 由于LootBoxUtils没有getLootBoxManager方法，直接使用LootBoxManager实例
            items = LootBoxManager.getInstance().openLootBox(boxId, difficulty);
        } catch (Exception e) {
            System.out.println("开启战利品箱失败: " + e.getMessage());
            items = java.util.Collections.emptyList();
        }
        System.out.println("\n实际掉落:");
        System.out.println(LootBoxUtils.formatDroppedItems(items));
        
        // 统计信息
        int totalItems = LootBoxUtils.getTotalItemCount(items);
        int totalValue = LootBoxUtils.calculateItemsValue(items);
        System.out.println("总数量: " + totalItems);
        System.out.println("总价值: " + totalValue);
    }
    
    /**
     * 测试等级推荐系统
     */
    public static void testLevelRecommendation() {
        System.out.println("=" .repeat(50));
        System.out.println("等级推荐系统测试");
        System.out.println("=" .repeat(50));
        
        int[] testLevels = {5, 15, 25, 35, 45};
        
        for (int level : testLevels) {
            int recommendedId = LootBoxUtils.getRecommendedBoxId(level);
            List<Item> items = LootBoxUtils.openRecommendedBox(level, "normal");
            
            System.out.println("等级 " + level + " -> 推荐战利品箱ID: " + recommendedId);
            System.out.println("掉落结果:");
            System.out.println(LootBoxUtils.formatDroppedItems(items));
            System.out.println();
        }
    }
    
    /**
     * 测试随机战利品箱系统
     */
    public static void testRandomLootBoxes() {
        System.out.println("=" .repeat(50));
        System.out.println("随机战利品箱测试");
        System.out.println("=" .repeat(50));
        
        LootBoxManager manager = LootBoxManager.getInstance();
        
        // 测试不同最大稀有度的随机选择
        Rarity[] maxRarities = {Rarity.COMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY, Rarity.MYTHICAL};
        
        for (Rarity maxRarity : maxRarities) {
            System.out.println("最大稀有度: " + maxRarity.getDisplayName());
            System.out.println("-".repeat(30));
            
            // 随机选择10次
            for (int i = 0; i < 10; i++) {
                LootBox box = manager.getRandomLootBox(maxRarity);
                if (box != null) {
                    System.out.println("  " + box.getName() + " (" + box.getRarity().getDisplayName() + ")");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * 性能测试
     */
    public static void performanceTest() {
        System.out.println("=" .repeat(50));
        System.out.println("性能测试");
        System.out.println("=" .repeat(50));
        
        long startTime = System.currentTimeMillis();
        
        // 开启10000个战利品箱
        for (int i = 0; i < 10000; i++) {
            LootBoxUtils.openSmallBox("normal");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("开启10000个小战利品箱耗时: " + duration + "ms");
        System.out.println("平均每个战利品箱: " + (duration / 10000.0) + "ms");
    }
    
    /**
     * 边界测试
     */
    public static void boundaryTest() {
        System.out.println("=" .repeat(50));
        System.out.println("边界测试");
        System.out.println("=" .repeat(50));
        
        // 测试不存在的战利品箱
        List<Item> items;
        try {
            items = LootBoxManager.getInstance().openLootBox(999, "normal");
        } catch (Exception e) {
            System.out.println("开启不存在的战利品箱失败: " + e.getMessage());
            items = java.util.Collections.emptyList();
        }
        System.out.println("开启不存在的战利品箱: " + items.size() + " 个物品");
        
        // 测试极端难度
        items = LootBoxUtils.openSmallBox("extreme"); // 不存在的难度
        System.out.println("极端难度测试: " + LootBoxUtils.formatDroppedItems(items));
        
        // 测试极端等级
        int boxId = LootBoxUtils.getRecommendedBoxId(-1);
        System.out.println("等级-1推荐战利品箱: " + boxId);
        
        boxId = LootBoxUtils.getRecommendedBoxId(999);
        System.out.println("等级999推荐战利品箱: " + boxId);
    }
    
    /**
     * 主测试方法
     */
    public static void runAllTests() {
        try {
            testAllLootBoxes();
            testLevelRecommendation();
            testRandomLootBoxes();
            performanceTest();
            boundaryTest();
            
            System.out.println("=" .repeat(60));
            System.out.println("所有测试完成！");
            System.out.println("=" .repeat(60));
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 快速测试方法
     */
    public static void quickTest() {
        System.out.println("快速战利品箱测试");
        System.out.println("-".repeat(30));
        
        // 开启一个中战利品箱
        List<Item> items = LootBoxUtils.openMediumBox("normal");
        System.out.println(LootBoxUtils.formatDroppedItems(items));
        
        // 显示价值比较
        System.out.println("\n" + LootBoxUtils.compareAllBoxes("normal"));
    }
    
    // Getter for LootBoxManager (用于工具类)
    public static class LootBoxUtilsAccess {
        public static LootBoxManager getLootBoxManager() {
            return LootBoxManager.getInstance();
        }
    }
}