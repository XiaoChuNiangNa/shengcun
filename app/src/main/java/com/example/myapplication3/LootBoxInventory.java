package com.example.myapplication3;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 战利品箱背包管理类
 * 处理战利品箱在背包中的存储、管理和使用
 */
public class LootBoxInventory {
    private static final String TAG = "LootBoxInventory";
    private static final String PREFS_NAME = "lootbox_inventory";
    private static final String KEY_LOOTBOX_LIST = "lootbox_list";
    private static final String KEY_TOTAL_SLOTS = "total_slots";
    private static final String KEY_USED_SLOTS = "used_slots";
    private static final String KEY_MAX_SLOTS = "max_slots";
    
    private static LootBoxInventory instance;
    private Context context;
    private SharedPreferences preferences;
    
    // 默认背包容量
    private static final int DEFAULT_MAX_SLOTS = 50;
    
    // 战利品箱列表
    private List<LootBoxItem> lootBoxItems;
    
    // 背包容量
    private int maxSlots;
    private int usedSlots;
    
    public LootBoxInventory(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.lootBoxItems = new ArrayList<>();
        loadInventory();
    }
    
    public static synchronized LootBoxInventory getInstance(Context context) {
        if (instance == null) {
            instance = new LootBoxInventory(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 战利品箱物品项
     */
    public static class LootBoxItem {
        private String id;              // 唯一ID
        private int boxId;              // 战利品箱类型ID
        private String boxName;         // 战利品箱名称
        private Rarity rarity;          // 稀有度
        private long obtainedTime;      // 获得时间
        private String source;          // 获得来源
        
        public LootBoxItem(int boxId, String boxName, Rarity rarity, String source) {
            this.id = UUID.randomUUID().toString();
            this.boxId = boxId;
            this.boxName = boxName;
            this.rarity = rarity;
            this.source = source;
            this.obtainedTime = System.currentTimeMillis();
        }
        
        // 用于从保存数据恢复的构造函数
        public LootBoxItem(String id, int boxId, String boxName, Rarity rarity, long obtainedTime, String source) {
            this.id = id;
            this.boxId = boxId;
            this.boxName = boxName;
            this.rarity = rarity;
            this.obtainedTime = obtainedTime;
            this.source = source;
        }
        
        // Getters
        public String getId() { return id; }
        public int getBoxId() { return boxId; }
        public String getBoxName() { return boxName; }
        public Rarity getRarity() { return rarity; }
        public long getObtainedTime() { return obtainedTime; }
        public String getSource() { return source; }
        
        /**
         * 格式化获得时间
         */
        public String getFormattedTime() {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(obtainedTime));
        }
    }
    
    /**
     * 添加战利品箱到背包
     * @param lootBox 战利品箱对象
     * @param source 获得来源
     * @return 是否添加成功
     */
    public boolean addLootBox(LootBox lootBox, String source) {
        if (usedSlots >= maxSlots) {
            Log.w(TAG, "背包已满，无法添加战利品箱");
            return false;
        }
        
        LootBoxItem item = new LootBoxItem(lootBox.getBoxId(), lootBox.getName(), 
                                         lootBox.getRarity(), source);
        lootBoxItems.add(item);
        usedSlots++;
        
        saveInventory();
        Log.i(TAG, String.format("添加战利品箱: %s (%s) 来源: %s", 
                lootBox.getName(), lootBox.getRarity().getDisplayName(), source));
        
        return true;
    }
    
    /**
     * 使用战利品箱
     * @param itemId 战利品箱ID
     * @param difficulty 游戏难度
     * @return 开启的物品列表，如果失败返回null
     */
    public List<Item> useLootBox(String itemId, String difficulty) {
        LootBoxItem item = findLootBoxItem(itemId);
        if (item == null) {
            Log.w(TAG, "找不到战利品箱: " + itemId);
            return null;
        }
        
        // 开启战利品箱
        LootBoxManager manager = LootBoxManager.getInstance();
        LootBox lootBox = manager.getLootBox(item.getBoxId());
        
        if (lootBox == null) {
            Log.w(TAG, "战利品箱不存在: " + item.getBoxId());
            return null;
        }
        
        List<Item> droppedItems = lootBox.openBox(difficulty);
        
        // 从背包中移除
        removeLootBoxItem(itemId);
        
        Log.i(TAG, String.format("使用战利品箱: %s, 获得 %d 个物品", 
                item.getBoxName(), droppedItems.size()));
        
        return droppedItems;
    }
    
    /**
     * 移除战利品箱
     * @param itemId 战利品箱ID
     * @return 是否移除成功
     */
    public boolean removeLootBox(String itemId) {
        return removeLootBoxItem(itemId) != null;
    }
    
    /**
     * 获取背包中所有战利品箱
     * @return 战利品箱列表
     */
    public List<LootBoxItem> getAllLootBoxes() {
        return new ArrayList<>(lootBoxItems);
    }
    
    /**
     * 根据稀有度获取战利品箱
     * @param rarity 稀有度
     * @return 对应稀有度的战利品箱列表
     */
    public List<LootBoxItem> getLootBoxesByRarity(Rarity rarity) {
        List<LootBoxItem> result = new ArrayList<>();
        for (LootBoxItem item : lootBoxItems) {
            if (item.getRarity() == rarity) {
                result.add(item);
            }
        }
        return result;
    }
    
    /**
     * 获取背包统计信息
     * @return 统计信息Map
     */
    public Map<String, Object> getInventoryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSlots", maxSlots);
        stats.put("usedSlots", usedSlots);
        stats.put("freeSlots", maxSlots - usedSlots);
        stats.put("totalLootBoxes", lootBoxItems.size());
        
        // 按稀有度统计
        Map<Rarity, Integer> rarityCount = new HashMap<>();
        for (LootBoxItem item : lootBoxItems) {
            rarityCount.put(item.getRarity(), rarityCount.getOrDefault(item.getRarity(), 0) + 1);
        }
        stats.put("rarityStats", rarityCount);
        
        return stats;
    }
    
    /**
     * 扩展背包容量
     * @param additionalSlots 增加的格子数
     * @return 是否扩展成功
     */
    public boolean expandInventory(int additionalSlots) {
        if (additionalSlots <= 0) {
            return false;
        }
        
        maxSlots += additionalSlots;
        saveInventory();
        Log.i(TAG, String.format("背包容量扩展: %d -> %d", maxSlots - additionalSlots, maxSlots));
        
        return true;
    }
    
    /**
     * 检查背包是否已满
     * @return 是否已满
     */
    public boolean isFull() {
        return usedSlots >= maxSlots;
    }
    
    /**
     * 获取剩余格子数
     * @return 剩余格子数
     */
    public int getFreeSlots() {
        return maxSlots - usedSlots;
    }
    
    /**
     * 清空背包
     */
    public void clearInventory() {
        lootBoxItems.clear();
        usedSlots = 0;
        saveInventory();
        Log.i(TAG, "战利品箱背包已清空");
    }
    
    /**
     * 格式化背包信息显示
     * @return 格式化的背包信息
     */
    public String getInventoryInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 战利品箱背包 ===\n");
        sb.append(String.format("容量: %d/%d\n", usedSlots, maxSlots));
        sb.append("包含物品:\n");
        
        if (lootBoxItems.isEmpty()) {
            sb.append("  (空)\n");
        } else {
            // 按稀有度分组显示
            Map<Rarity, List<LootBoxItem>> grouped = new HashMap<>();
            for (LootBoxItem item : lootBoxItems) {
                grouped.computeIfAbsent(item.getRarity(), k -> new ArrayList<>()).add(item);
            }
            
            for (Rarity rarity : Rarity.values()) {
                List<LootBoxItem> items = grouped.get(rarity);
                if (items != null && !items.isEmpty()) {
                    sb.append(String.format("  %s (%s): %d个\n", 
                            rarity.getDisplayName(), rarity.getColor(), items.size()));
                    
                    for (LootBoxItem item : items) {
                        sb.append(String.format("    • %s (来源: %s, 获得时间: %s)\n",
                                item.getBoxName(), item.getSource(), item.getFormattedTime()));
                    }
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 保存背包数据
     */
    private void saveInventory() {
        SharedPreferences.Editor editor = preferences.edit();
        
        // 保存容量信息
        editor.putInt(KEY_MAX_SLOTS, maxSlots);
        editor.putInt(KEY_USED_SLOTS, usedSlots);
        
        // 保存战利品箱列表
        StringBuilder sb = new StringBuilder();
        for (LootBoxItem item : lootBoxItems) {
            if (sb.length() > 0) sb.append(";");
            sb.append(String.format("%s|%d|%s|%s|%d|%s",
                    item.getId(),
                    item.getBoxId(),
                    item.getBoxName(),
                    item.getRarity().name(),
                    item.getObtainedTime(),
                    item.getSource()));
        }
        
        editor.putString(KEY_LOOTBOX_LIST, sb.toString());
        editor.apply();
        
        Log.d(TAG, "背包数据已保存");
    }
    
    /**
     * 加载背包数据
     */
    private void loadInventory() {
        // 加载容量信息
        maxSlots = preferences.getInt(KEY_MAX_SLOTS, DEFAULT_MAX_SLOTS);
        usedSlots = preferences.getInt(KEY_USED_SLOTS, 0);
        
        // 加载战利品箱列表
        String data = preferences.getString(KEY_LOOTBOX_LIST, "");
        if (data != null && !data.isEmpty()) {
            String[] items = data.split(";");
            for (String itemData : items) {
                if (itemData.isEmpty()) continue;
                
                String[] parts = itemData.split("\\|");
                if (parts.length >= 6) {
                    try {
                        LootBoxItem item = new LootBoxItem(
                                parts[0],                                    // id
                                Integer.parseInt(parts[1]),                  // boxId
                                parts[2],                                   // boxName
                                Rarity.valueOf(parts[3]),                    // rarity
                                Long.parseLong(parts[4]),                    // obtainedTime
                                parts[5]                                    // source
                        );
                        
                        lootBoxItems.add(item);
                    } catch (Exception e) {
                        Log.e(TAG, "加载战利品箱数据失败: " + itemData, e);
                    }
                }
            }
        }
        
        Log.d(TAG, String.format("背包数据加载完成: %d/%d", usedSlots, maxSlots));
    }
    
    /**
     * 查找战利品箱项
     * @param itemId 战利品箱ID
     * @return 找到的战利品箱项，未找到返回null
     */
    private LootBoxItem findLootBoxItem(String itemId) {
        for (LootBoxItem item : lootBoxItems) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * 移除战利品箱项（内部方法）
     * @param itemId 战利品箱ID
     * @return 被移除的战利品箱项，未找到返回null
     */
    private LootBoxItem removeLootBoxItem(String itemId) {
        for (int i = 0; i < lootBoxItems.size(); i++) {
            if (lootBoxItems.get(i).getId().equals(itemId)) {
                LootBoxItem removed = lootBoxItems.remove(i);
                usedSlots--;
                saveInventory();
                return removed;
            }
        }
        return null;
    }
    
    // Getters
    public int getMaxSlots() { return maxSlots; }
    public int getUsedSlots() { return usedSlots; }
    public List<LootBoxItem> getLootBoxItems() { return new ArrayList<>(lootBoxItems); }
}