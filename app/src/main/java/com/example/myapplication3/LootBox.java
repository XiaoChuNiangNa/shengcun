package com.example.myapplication3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 战利品箱类
 * 定义了不同等级的战利品箱及其包含的物品
 */
public class LootBox {
    private String name;                    // 战利品箱名称
    private Rarity rarity;                 // 稀有度
    private List<LootItemDrop> items;      // 物品掉落列表
    private int boxId;                      // 战利品箱ID
    private String description;             // 描述信息

    public LootBox(int boxId, String name, Rarity rarity, String description) {
        this.boxId = boxId;
        this.name = name;
        this.rarity = rarity;
        this.description = description;
        this.items = new ArrayList<>();
    }

    /**
     * 添加物品掉落信息
     * @param itemDrop 物品掉落信息
     */
    public void addItemDrop(LootItemDrop itemDrop) {
        items.add(itemDrop);
    }

    /**
     * 添加物品掉落信息（便捷方法）
     * @param itemName 物品名称
     * @param minAmount 最小数量
     * @param maxAmount 最大数量
     */
    public void addItemDrop(String itemName, int minAmount, int maxAmount) {
        addItemDrop(itemName, minAmount, maxAmount, 1.0);
    }

    /**
     * 添加物品掉落信息（带掉落概率）
     * @param itemName 物品名称
     * @param minAmount 最小数量
     * @param maxAmount 最大数量
     * @param dropChance 掉落概率
     */
    public void addItemDrop(String itemName, int minAmount, int maxAmount, double dropChance) {
        items.add(new LootItemDrop(itemName, minAmount, maxAmount, dropChance));
    }

    /**
     * 开启战利品箱，获取所有掉落的物品
     * @param difficulty 游戏难度
     * @return 掉落的物品列表
     */
    public List<Item> openBox(String difficulty) {
        List<Item> droppedItems = new ArrayList<>();
        
        for (LootItemDrop itemDrop : items) {
            Item dropItem = itemDrop.createDropItem(difficulty);
            if (dropItem != null) {
                droppedItems.add(dropItem);
            }
        }
        
        return droppedItems;
    }

    /**
     * 获取掉落统计信息
     * @param difficulty 游戏难度
     * @return 掉落统计Map（物品名称 -> 预期数量范围）
     */
    public Map<String, String> getDropStatistics(String difficulty) {
        Map<String, String> stats = new HashMap<>();
        
        for (LootItemDrop itemDrop : items) {
            int minAmount = itemDrop.getMinAmount();
            int maxAmount = itemDrop.getMaxAmount();
            
            // 根据难度调整数量范围
            switch (difficulty.toLowerCase()) {
                case "easy":
                    minAmount = (int) Math.ceil(minAmount * 1.2);
                    maxAmount = (int) Math.ceil(maxAmount * 1.2);
                    break;
                case "hard":
                    minAmount = (int) Math.floor(minAmount * 0.8);
                    maxAmount = (int) Math.floor(maxAmount * 0.8);
                    break;
                default:
                    break;
            }
            
            String dropInfo = String.format("%d-%d", minAmount, maxAmount);
            if (itemDrop.getDropChance() < 1.0) {
                dropInfo += String.format(" (%.0f%%)", itemDrop.getDropChance() * 100);
            }
            
            stats.put(itemDrop.getItemName(), dropInfo);
        }
        
        return stats;
    }

    /**
     * 获取战利品箱的总价值预估
     * @param difficulty 游戏难度
     * @return 预估总价值
     */
    public int estimateTotalValue(String difficulty) {
        int totalValue = 0;
        
        for (LootItemDrop itemDrop : items) {
            // 基础价值计算（可根据物品类型调整）
            int baseValue = getItemBaseValue(itemDrop.getItemName());
            double averageAmount = (itemDrop.getMinAmount() + itemDrop.getMaxAmount()) / 2.0;
            
            // 考虑掉落概率
            double expectedAmount = averageAmount * itemDrop.getDropChance();
            
            // 难度调整
            switch (difficulty.toLowerCase()) {
                case "easy":
                    expectedAmount *= 1.2;
                    break;
                case "hard":
                    expectedAmount *= 0.8;
                    break;
                default:
                    break;
            }
            
            totalValue += (int) Math.round(baseValue * expectedAmount);
        }
        
        return totalValue;
    }

    /**
     * 获取物品基础价值（简化计算）
     * @param itemName 物品名称
     * @return 基础价值
     */
    private int getItemBaseValue(String itemName) {
        // 根据物品类型设置基础价值
        if (itemName.contains("石头")) return 1;
        if (itemName.contains("沙子")) return 2;
        if (itemName.contains("燧石")) return 3;
        if (itemName.contains("煤炭")) return 5;
        if (itemName.contains("铁矿")) return 10;
        if (itemName.contains("硫磺")) return 15;
        if (itemName.contains("黑曜石")) return 25;
        if (itemName.contains("宝石")) return 50;
        if (itemName.contains("钻石")) return 100;
        
        return 1; // 默认值
    }

    /**
     * 模拟开启多次战利品箱，计算平均掉落
     * @param difficulty 游戏难度
     * @param simulationCount 模拟次数
     * @return 平均掉落统计
     */
    public Map<String, Double> simulateAverageDrops(String difficulty, int simulationCount) {
        Map<String, Double> totalDrops = new HashMap<>();
        
        for (int i = 0; i < simulationCount; i++) {
            List<Item> drops = openBox(difficulty);
            for (Item item : drops) {
                totalDrops.put(item.getName(), totalDrops.getOrDefault(item.getName(), 0.0) + item.getAmount());
            }
        }
        
        // 计算平均值
        for (String itemName : totalDrops.keySet()) {
            totalDrops.put(itemName, totalDrops.get(itemName) / simulationCount);
        }
        
        return totalDrops;
    }

    // Getters
    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public List<LootItemDrop> getItems() {
        return new ArrayList<>(items);
    }

    public int getBoxId() {
        return boxId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rarity.getDisplayName()).append(" - ").append(name).append("\n");
        sb.append("描述: ").append(description).append("\n");
        sb.append("包含物品:\n");
        
        for (LootItemDrop item : items) {
            sb.append("  ").append(item.getDisplayText()).append("\n");
        }
        
        return sb.toString();
    }
}