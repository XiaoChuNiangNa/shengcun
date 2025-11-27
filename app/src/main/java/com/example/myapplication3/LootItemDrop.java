package com.example.myapplication3;

import java.util.Random;

/**
 * 物品掉落信息类
 * 定义了战利品箱中每种物品的掉落范围和数量
 */
public class LootItemDrop {
    private String itemName;      // 物品名称
    private int minAmount;         // 最小掉落数量
    private int maxAmount;         // 最大掉落数量
    private double dropChance;     // 掉落概率 (0.0-1.0)
    private Rarity rarity;        // 物品稀有度

    public LootItemDrop(String itemName, int minAmount, int maxAmount, double dropChance) {
        this.itemName = itemName;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.dropChance = dropChance;
        this.rarity = Rarity.getRarityByItem(itemName);
    }

    public LootItemDrop(String itemName, int minAmount, int maxAmount) {
        this(itemName, minAmount, maxAmount, 1.0); // 默认100%掉落
    }

    /**
     * 计算实际掉落数量
     * @return 掉落的物品数量
     */
    public int calculateDropAmount() {
        Random random = new Random();
        
        // 先判断是否掉落
        if (random.nextDouble() > dropChance) {
            return 0;
        }
        
        // 计算数量
        if (minAmount == maxAmount) {
            return minAmount;
        }
        
        return random.nextInt(maxAmount - minAmount + 1) + minAmount;
    }

    /**
     * 根据难度调整掉落数量
     * @param difficulty 游戏难度
     * @return 调整后的掉落数量
     */
    public int calculateDropAmountWithDifficulty(String difficulty) {
        int baseAmount = calculateDropAmount();
        
        if (baseAmount == 0) return 0;
        
        switch (difficulty.toLowerCase()) {
            case "easy":
                // 简单难度：掉落数量增加
                baseAmount = (int) Math.ceil(baseAmount * 1.2);
                break;
            case "hard":
                // 困难难度：掉落数量减少
                baseAmount = (int) Math.floor(baseAmount * 0.8);
                break;
            default:
                // 普通难度：不变
                break;
        }
        
        return Math.max(1, baseAmount);
    }

    /**
     * 创建包含实际掉落数量的Item对象
     * @param difficulty 游戏难度
     * @return Item对象，如果未掉落则返回null
     */
    public Item createDropItem(String difficulty) {
        int amount = calculateDropAmountWithDifficulty(difficulty);
        if (amount <= 0) {
            return null;
        }
        return new Item(itemName, amount);
    }

    // Getters
    public String getItemName() {
        return itemName;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public double getDropChance() {
        return dropChance;
    }

    public Rarity getRarity() {
        return rarity;
    }

    @Override
    public String toString() {
        String chanceStr = dropChance < 1.0 ? String.format(" (%.0f%%掉落)", dropChance * 100) : "";
        return String.format("%s (%d-%d)%s", itemName, minAmount, maxAmount, chanceStr);
    }

    /**
     * 获取显示文本，包含数量范围
     * @return 格式化的显示文本
     */
    public String getDisplayText() {
        return String.format("%s（%d-%d）", itemName, minAmount, maxAmount);
    }
}