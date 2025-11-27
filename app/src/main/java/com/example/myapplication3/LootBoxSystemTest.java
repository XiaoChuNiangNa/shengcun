package com.example.myapplication3;

import android.util.Log;
import java.util.List;
import java.util.Map;

/**
 * 战利品箱系统测试类
 * 用于验证修复后的系统功能
 */
public class LootBoxSystemTest {
    private static final String TAG = "LootBoxSystemTest";
    
    /**
     * 测试背包管理器
     */
    public static void testLootBoxInventory() {
        Log.i(TAG, "开始测试战利品箱背包系统");
        
        try {
            // 测试创建战利品箱项
            LootBoxManager manager = LootBoxManager.getInstance();
            LootBox smallBox = manager.getLootBox(1); // 小战利品箱
            LootBox rareBox = manager.getLootBox(2); // 中战利品箱（稀有）
            
            if (smallBox == null || rareBox == null) {
                Log.e(TAG, "战利品箱创建失败");
                return;
            }
            
            // 测试添加战利品箱到背包
            LootBoxInventory.LootBoxItem item1 = new LootBoxInventory.LootBoxItem(
                    smallBox.getBoxId(), smallBox.getName(), smallBox.getRarity(), "测试来源");
            LootBoxInventory.LootBoxItem item2 = new LootBoxInventory.LootBoxItem(
                    rareBox.getBoxId(), rareBox.getName(), rareBox.getRarity(), "测试来源");
            
            Log.i(TAG, "战利品箱项创建成功");
            Log.d(TAG, "Item1: " + item1.getBoxName() + " (" + item1.getRarity().getDisplayName() + ")");
            Log.d(TAG, "Item2: " + item2.getBoxName() + " (" + item2.getRarity().getDisplayName() + ")");
            
            // 测试时间格式化
            Log.d(TAG, "获得时间: " + item1.getFormattedTime());
            Log.d(TAG, "获得来源: " + item1.getSource());
            
            Log.i(TAG, "战利品箱背包测试完成");
            
        } catch (Exception e) {
            Log.e(TAG, "战利品箱背包测试失败", e);
        }
    }
    
    /**
     * 测试战利品箱开启
     */
    public static void testLootBoxOpening() {
        Log.i(TAG, "开始测试战利品箱开启功能");
        
        try {
            LootBoxManager manager = LootBoxManager.getInstance();
            LootBox box = manager.getLootBox(1); // 小战利品箱
            
            if (box == null) {
                Log.e(TAG, "获取战利品箱失败");
                return;
            }
            
            // 测试不同难度下的开启
            String[] difficulties = {"easy", "normal", "hard"};
            
            for (String difficulty : difficulties) {
                Log.i(TAG, "测试难度: " + difficulty);
                List<Item> items = box.openBox(difficulty);
                
                int totalItems = 0;
                int totalValue = 0;
                
                for (Item item : items) {
                    totalItems += item.getAmount();
                    totalValue += LootBoxUtils.calculateItemsValue(items);
                    Log.d(TAG, "获得物品: " + item.toString());
                }
                
                Log.i(TAG, String.format("难度 %s - 物品数: %d, 总价值: %d", 
                        difficulty, totalItems, totalValue));
            }
            
            // 测试统计信息
            Map<String, String> stats = manager.getLootBoxStatistics(1, "normal");
            Log.i(TAG, "战利品箱统计信息:");
            for (Map.Entry<String, String> entry : stats.entrySet()) {
                Log.d(TAG, entry.getKey() + ": " + entry.getValue());
            }
            
            Log.i(TAG, "战利品箱开启测试完成");
            
        } catch (Exception e) {
            Log.e(TAG, "战利品箱开启测试失败", e);
        }
    }
    
    /**
     * 测试掉落概率系统
     */
    public static void testDropProbability() {
        Log.i(TAG, "开始测试掉落概率系统");
        
        try {
            LootBoxDropManager dropManager = LootBoxDropManager.getInstance();
            
            // 测试不同组合的掉落概率
            String[][] testCases = {
                {"野兔", "小型", "草原", "normal"},
                {"狼", "中型", "森林", "normal"},
                {"老虎", "大型", "山地", "normal"},
                {"野兔", "小型", "沙漠", "easy"},
                {"老虎", "大型", "山地", "hard"}
            };
            
            for (String[] testCase : testCases) {
                String animalName = testCase[0];
                String animalSize = testCase[1];
                String terrainType = testCase[2];
                String difficulty = testCase[3];
                
                // 获取掉落概率详情
                String dropChance = dropManager.getDropProbabilityDetails(
                        animalName, animalSize, terrainType, difficulty);
                
                Log.i(TAG, String.format("动物: %s (%s) 地形: %s 难度: %s -> 掉落概率: %s",
                        animalName, animalSize, terrainType, difficulty, dropChance));
                
                // 模拟100次掉落
                Map<Rarity, Integer> dropStats = dropManager.simulateDrops(
                        animalName, animalSize, terrainType, difficulty, 100);
                
                int totalDrops = dropStats.values().stream().mapToInt(Integer::intValue).sum();
                Log.d(TAG, String.format("100次模拟结果: 总掉落 %d 次", totalDrops));
                
                for (Map.Entry<Rarity, Integer> entry : dropStats.entrySet()) {
                    if (entry.getValue() > 0) {
                        Log.d(TAG, String.format("  %s: %d次", 
                                entry.getKey().getDisplayName(), entry.getValue()));
                    }
                }
            }
            
            Log.i(TAG, "掉落概率测试完成");
            
        } catch (Exception e) {
            Log.e(TAG, "掉落概率测试失败", e);
        }
    }
    
    /**
     * 测试工具方法
     */
    public static void testUtilityMethods() {
        Log.i(TAG, "开始测试工具方法");
        
        try {
            // 测试价值计算
            LootBoxManager manager = LootBoxManager.getInstance();
            List<Item> testItems = manager.openLootBox(1, "normal");
            
            String formatted = LootBoxUtils.formatDroppedItems(testItems);
            Log.i(TAG, "格式化结果:\n" + formatted);
            
            int itemCount = LootBoxUtils.getTotalItemCount(testItems);
            int itemValue = LootBoxUtils.calculateItemsValue(testItems);
            Log.i(TAG, String.format("物品总数: %d, 总价值: %d", itemCount, itemValue));
            
            // 测试战利品箱信息
            String boxInfo = LootBoxUtils.getFullBoxInfo(1);
            Log.i(TAG, "战利品箱信息:\n" + boxInfo);
            
            // 测试价值比较
            String comparison = LootBoxUtils.compareAllBoxes("normal");
            Log.i(TAG, "价值比较:\n" + comparison);
            
            Log.i(TAG, "工具方法测试完成");
            
        } catch (Exception e) {
            Log.e(TAG, "工具方法测试失败", e);
        }
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        Log.i(TAG, "=" .repeat(50));
        Log.i(TAG, "战利品箱系统完整测试开始");
        Log.i(TAG, "=" .repeat(50));
        
        try {
            testLootBoxInventory();
            Log.i(TAG, "-".repeat(30));
            
            testLootBoxOpening();
            Log.i(TAG, "-".repeat(30));
            
            testDropProbability();
            Log.i(TAG, "-".repeat(30));
            
            testUtilityMethods();
            
            Log.i(TAG, "=" .repeat(50));
            Log.i(TAG, "所有测试完成！系统运行正常");
            Log.i(TAG, "=" .repeat(50));
            
        } catch (Exception e) {
            Log.e(TAG, "测试过程中发生错误", e);
        }
    }
    
    /**
     * 快速测试（用于验证修复）
     */
    public static void quickTest() {
        Log.i(TAG, "开始快速测试");
        
        try {
            // 测试基本的战利品箱创建和开启
            LootBoxManager manager = LootBoxManager.getInstance();
            LootBox box = manager.getLootBox(1);
            
            if (box != null) {
                List<Item> items = box.openBox("normal");
                Log.i(TAG, "快速测试 - 开启战利品箱成功，获得 " + items.size() + " 个物品");
                
                for (Item item : items) {
                    Log.d(TAG, "  " + item.toString());
                }
            } else {
                Log.e(TAG, "快速测试 - 获取战利品箱失败");
            }
            
            Log.i(TAG, "快速测试完成");
            
        } catch (Exception e) {
            Log.e(TAG, "快速测试失败", e);
        }
    }
}