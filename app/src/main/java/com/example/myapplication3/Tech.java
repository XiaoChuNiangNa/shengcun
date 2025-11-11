package com.example.myapplication3;

public class Tech {
    public static final int TYPE_BASE = 0;      // 基础科技
    public static final int TYPE_PRIMARY = 1;   // 一级科技
    public static final int TYPE_SECONDARY = 2; // 二级科技

    public String techId;
    public String name;
    public int type; // 科技类型（基础/一级/二级）
    public int level;
    public int maxLevel;
    public int[] upgradeCosts;
    public String[] descriptions;
    public String preTechId; // 前置科技ID
    public int preTechMinLevel; // 前置科技所需最低等级
    public String parentTechId; // 对应上级科技ID（仅二级科技使用）

    public Tech(String techId, String name, int type, int maxLevel, int[] upgradeCosts,
                String[] descriptions, String preTechId, int preTechMinLevel, String parentTechId) {
        this.techId = techId;
        this.name = name;
        this.type = type;
        this.maxLevel = maxLevel;
        this.upgradeCosts = upgradeCosts;
        this.descriptions = descriptions;
        this.preTechId = preTechId;
        this.preTechMinLevel = preTechMinLevel;
        this.parentTechId = parentTechId;
        this.level = 0;
    }

    // 判断是否已解锁（等级>0）
    public boolean isUnlocked() {
        return level > 0;
    }

    // 判断是否已达满级
    public boolean isMaxLevel() {
        return level >= maxLevel;
    }

    // 获取当前等级升级所需消耗
    public int getCurrentUpgradeCost() {
        // 检查等级是否超出数组范围或已达最大等级
        if (isMaxLevel() || level < 0 || level >= upgradeCosts.length) {
            return 0;
        }
        return upgradeCosts[level];
    }

    // 获取当前等级描述
    public String getCurrentDescription() {
        if (level == 0) return "未解锁";
        return descriptions[level - 1];
    }
}
