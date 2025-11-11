package com.example.myapplication3;

public class Equipment {

    private int id;
    private String type; // 装备类型（如"石质斧头"）
    private int durability; // 当前耐久度
    private int maxDurability; // 最大耐久度
    private boolean isEquipped; // 是否为当前装备
    private int itemCount; // 物品数量

    public Equipment(int id, String type, int durability, int maxDurability, boolean isEquipped) {
        this.id = id;
        this.type = type;
        this.durability = durability;
        this.maxDurability = maxDurability;
        this.isEquipped = isEquipped;
        this.itemCount = 1; // 默认数量为1
    }
    
    // 新构造函数：用于从背包数据创建装备对象
    public Equipment(String type, int durability) {
        this.id = 0; // 使用默认ID
        this.type = type;
        this.durability = durability;
        this.maxDurability = 100; // 默认最大耐久度
        this.isEquipped = false; // 默认未装备
        this.itemCount = 1; // 默认数量为1
    }

    // Getter和Setter
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public boolean isEquipped() { return isEquipped; }
    public void setEquipped(boolean equipped) { isEquipped = equipped; }
    public void setDurability(int durability) { this.durability = durability; }
    
    // ItemCount相关方法
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}