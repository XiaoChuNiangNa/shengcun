package com.example.myapplication3;

/**
 * 工具分类枚举
 */
public enum ToolCategory {
    AXE("斧", "用于砍伐树木和采集木质资源"),
    PICKAXE("镐", "用于挖掘矿石和岩石资源"),
    SICKLE("镰刀", "用于收割植物和农作物"),
    FISHING_ROD("鱼竿", "用于钓鱼和采集水生资源"),
    NONE("无", "没有装备工具");

    private final String name;
    private final String description;

    ToolCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}