package com.example.myapplication3;

/**
 * 物品稀有度枚举
 * 定义了5个稀有度级别，每个级别有不同的掉落概率和颜色标识
 */
public enum Rarity {
    COMMON("普通", 0.60, "#808080"),      // 灰色，60%概率
    RARE("稀有", 0.25, "#1E90FF"),        // 蓝色，25%概率
    EPIC("史诗", 0.10, "#9370DB"),        // 紫色，10%概率
    LEGENDARY("传说", 0.04, "#FF8C00"),   // 橙色，4%概率
    MYTHICAL("神话", 0.01, "#FF4500");    // 红色，1%概率

    private final String displayName;
    private final double baseProbability; // 基础掉落概率
    private final String color;           // 颜色代码

    Rarity(String displayName, double baseProbability, String color) {
        this.displayName = displayName;
        this.baseProbability = baseProbability;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseProbability() {
        return baseProbability;
    }

    public String getColor() {
        return color;
    }

    /**
     * 根据难度调整掉落概率
     * @param difficulty 游戏难度
     * @return 调整后的概率
     */
    public double getAdjustedProbability(String difficulty) {
        double adjusted = baseProbability;
        
        switch (difficulty) {
            case "easy":
                // 简单难度：稀有物品概率提高
                if (this == RARE) adjusted *= 1.5;
                else if (this == EPIC) adjusted *= 2.0;
                else if (this == LEGENDARY) adjusted *= 3.0;
                else if (this == MYTHICAL) adjusted *= 4.0;
                break;
            case "hard":
                // 困难难度：稀有物品概率降低
                if (this == RARE) adjusted *= 0.7;
                else if (this == EPIC) adjusted *= 0.5;
                else if (this == LEGENDARY) adjusted *= 0.3;
                else if (this == MYTHICAL) adjusted *= 0.2;
                break;
            default:
                // 普通难度：保持原概率
                break;
        }
        
        return Math.min(adjusted, 1.0); // 确保概率不超过100%
    }

    /**
     * 获取稀有度对应的掉落数量范围
     * @return 最小和最大掉落数量
     */
    public int[] getDropAmountRange() {
        switch (this) {
            case COMMON:
                return new int[]{1, 5};      // 普通物品：1-5个
            case RARE:
                return new int[]{1, 3};      // 稀有物品：1-3个
            case EPIC:
                return new int[]{1, 2};      // 史诗物品：1-2个
            case LEGENDARY:
                return new int[]{1, 1};      // 传说物品：1个
            case MYTHICAL:
                return new int[]{1, 1};      // 神话物品：1个
            default:
                return new int[]{1, 1};
        }
    }

    /**
     * 根据物品名称获取稀有度
     * @param itemName 物品名称
     * @return 对应的稀有度
     */
    public static Rarity getRarityByItem(String itemName) {
        return ItemRarityManager.getItemRarity(itemName);
    }
}