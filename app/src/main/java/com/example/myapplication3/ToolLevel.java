package com.example.myapplication3;

// 新建 ToolLevel.java
public enum ToolLevel {
    LEVEL_1(1, "石质"),
    LEVEL_2(2, "铁质"),
    LEVEL_3(3, "钻石"),
    UNKNOWN(0, "未知"); // 默认等级

    private final int level;
    private final String material;

    ToolLevel(int level, String material) {
        this.level = level;
        this.material = material;
    }

    public int getLevel() {
        return level;
    }

    public String getMaterial() {
        return material;
    }
}
