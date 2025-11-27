package com.example.myapplication3;

import java.util.List;
import java.util.Map;

/**
 * 战利品箱工具类
 * 提供便捷的战利品箱操作方法
 */
public class LootBoxUtils {
    private static final LootBoxManager manager = LootBoxManager.getInstance();

    /**
     * 快速开启小战利品箱
     * @param difficulty 游戏难度
     * @return 掉落物品列表
     */
    public static List<Item> openSmallBox(String difficulty) {
        return manager.openLootBox(1, difficulty);
    }

    /**
     * 快速开启中战利品箱
     * @param difficulty 游戏难度
     * @return 掉落物品列表
     */
    public static List<Item> openMediumBox(String difficulty) {
        return manager.openLootBox(2, difficulty);
    }

    /**
     * 快速开启大战利品箱
     * @param difficulty 游戏难度
     * @return 掉落物品列表
     */
    public static List<Item> openLargeBox(String difficulty) {
        return manager.openLootBox(3, difficulty);
    }

    /**
     * 快速开启巨型战利品箱
     * @param difficulty 游戏难度
     * @return 掉落物品列表
     */
    public static List<Item> openGiantBox(String difficulty) {
        return manager.openLootBox(4, difficulty);
    }

    /**
     * 快速开启终极战利品箱
     * @param difficulty 游戏难度
     * @return 掉落物品列表
     */
    public static List<Item> openUltimateBox(String difficulty) {
        return manager.openLootBox(5, difficulty);
    }

    /**
     * 根据等级获取合适的战利品箱
     * @param playerLevel 玩家等级
     * @return 推荐的战利品箱ID
     */
    public static int getRecommendedBoxId(int playerLevel) {
        if (playerLevel < 10) {
            return 1; // 小战利品箱
        } else if (playerLevel < 20) {
            return 2; // 中战利品箱
        } else if (playerLevel < 30) {
            return 3; // 大战利品箱
        } else if (playerLevel < 40) {
            return 4; // 巨型战利品箱
        } else {
            return 5; // 终极战利品箱
        }
    }

    /**
     * 根据等级开启推荐战利品箱
     * @param playerLevel 玩家等级
     * @param difficulty 游戏难度
     * @return 掉落物品列表
     */
    public static List<Item> openRecommendedBox(int playerLevel, String difficulty) {
        int boxId = getRecommendedBoxId(playerLevel);
        return manager.openLootBox(boxId, difficulty);
    }

    /**
     * 格式化掉落物品显示
     * @param items 物品列表
     * @return 格式化的字符串
     */
    public static String formatDroppedItems(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return "没有获得任何物品";
        }

        StringBuilder sb = new StringBuilder("获得物品：\n");
        for (Item item : items) {
            sb.append("  • ").append(item.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 计算掉落物品总数
     * @param items 物品列表
     * @return 物品总数量
     */
    public static int getTotalItemCount(List<Item> items) {
        int total = 0;
        if (items != null) {
            for (Item item : items) {
                total += item.getAmount();
            }
        }
        return total;
    }

    /**
     * 计算掉落物品价值
     * @param items 物品列表
     * @return 总价值
     */
    public static int calculateItemsValue(List<Item> items) {
        int totalValue = 0;
        if (items != null) {
            for (Item item : items) {
                totalValue += getItemValue(item.getName()) * item.getAmount();
            }
        }
        return totalValue;
    }

    /**
     * 获取物品单位价值
     * @param itemName 物品名称
     * @return 单位价值
     */
    private static int getItemValue(String itemName) {
        if (itemName.contains("石头")) return 1;
        if (itemName.contains("沙子")) return 2;
        if (itemName.contains("燧石")) return 3;
        if (itemName.contains("煤炭")) return 5;
        if (itemName.contains("铁矿")) return 10;
        if (itemName.contains("硫磺")) return 15;
        if (itemName.contains("黑曜石")) return 25;
        if (itemName.contains("宝石")) return 50;
        if (itemName.contains("钻石")) return 100;
        return 1;
    }

    /**
     * 获取战利品箱的完整信息
     * @param boxId 战利品箱ID
     * @return 完整信息字符串
     */
    public static String getFullBoxInfo(int boxId) {
        LootBox box = manager.getLootBox(boxId);
        if (box == null) {
            return "战利品箱不存在";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=" .repeat(40)).append("\n");
        sb.append(" ").append(box.getName()).append("\n");
        sb.append("=" .repeat(40)).append("\n");
        sb.append("稀有度: ").append(box.getRarity().getDisplayName()).append("\n");
        sb.append("描述: ").append(box.getDescription()).append("\n");
        sb.append("物品详情:\n");
        
        for (var itemDrop : box.getItems()) {
            sb.append("  • ").append(itemDrop.getDisplayText()).append("\n");
        }
        
        // 普通难度统计
        sb.append("\n普通难度掉落范围:\n");
        Map<String, String> stats = manager.getLootBoxStatistics(boxId, "normal");
        for (Map.Entry<String, String> entry : stats.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // 估算价值
        sb.append("\n预估价值: ").append(manager.estimateLootBoxValue(boxId, "normal"));
        
        return sb.toString();
    }

    /**
     * 显示所有战利品箱信息
     * @return 所有战利品箱的简要信息
     */
    public static String showAllLootBoxes() {
        StringBuilder sb = new StringBuilder("所有可用战利品箱:\n");
        sb.append("-".repeat(50)).append("\n");
        
        for (LootBox box : manager.getAllLootBoxes()) {
            sb.append(String.format("ID:%d | %s | %s\n", 
                box.getBoxId(), 
                box.getName(), 
                box.getRarity().getDisplayName()));
        }
        
        return sb.toString();
    }

    /**
     * 模拟批量开启战利品箱
     * @param boxId 战利品箱ID
     * @param count 开启次数
     * @param difficulty 游戏难度
     * @return 模拟结果
     */
    public static String simulateBoxOpening(int boxId, int count, String difficulty) {
        Map<String, Double> averageDrops = manager.simulateLootBoxOpens(boxId, difficulty, count);
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("模拟开启%d次 %s (难度:%s)\n", 
            count, 
            manager.getLootBox(boxId).getName(), 
            difficulty));
        sb.append("-".repeat(40)).append("\n");
        
        for (Map.Entry<String, Double> entry : averageDrops.entrySet()) {
            sb.append(String.format("%s: %.1f\n", entry.getKey(), entry.getValue()));
        }
        
        return sb.toString();
    }

    /**
     * 比较不同战利品箱的价值
     * @param difficulty 游戏难度
     * @return 比较结果
     */
    public static String compareAllBoxes(String difficulty) {
        StringBuilder sb = new StringBuilder("战利品箱价值比较 (").append(difficulty).append("):\n");
        sb.append("-".repeat(50)).append("\n");
        
        for (LootBox box : manager.getAllLootBoxes()) {
            int value = manager.estimateLootBoxValue(box.getBoxId(), difficulty);
            sb.append(String.format("%-15s: %d 价值\n", box.getName(), value));
        }
        
        return sb.toString();
    }
}