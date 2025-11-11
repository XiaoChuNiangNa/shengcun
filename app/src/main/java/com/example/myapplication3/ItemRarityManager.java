package com.example.myapplication3;

import java.util.HashMap;
import java.util.Map;

/**
 * 物品稀有度管理器
 * 管理所有基础资源的稀有度配置
 */
public class ItemRarityManager {
    
    // 物品稀有度映射表
    private static final Map<String, Rarity> ITEM_RARITY_MAP = new HashMap<>();
    
    static {
        // 初始化物品稀有度配置
        initializeItemRarities();
    }
    
    /**
     * 初始化物品稀有度配置
     */
    private static void initializeItemRarities() {
        // 普通稀有度物品
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_WEED, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_BERRY, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_APPLE, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_CACTUS_FRUIT, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_MUSHROOM, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_REED, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_VINE, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_WOOD, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_ACORN, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_STONE, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_WATER, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_ICE, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_SAND, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_SHELL, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_COCONUT, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_KELP, Rarity.COMMON);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_CLAY, Rarity.COMMON);
        
        // 稀有稀有度物品
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_HERB, Rarity.RARE);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_FLINT, Rarity.RARE);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_COAL, Rarity.RARE);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_FISH, Rarity.RARE);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_CRAWFISH, Rarity.RARE);
        
        // 史诗稀有度物品
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_RESIN, Rarity.EPIC);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_IRON_ORE, Rarity.EPIC);
        
        // 传说稀有度物品
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_TRUFFLE, Rarity.LEGENDARY);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_SULFUR, Rarity.LEGENDARY);
        
        // 神话稀有度物品
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_SNOW_LOTUS, Rarity.MYTHICAL);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_OBSIDIAN, Rarity.MYTHICAL);
        ITEM_RARITY_MAP.put(ItemConstants.ITEM_GEM, Rarity.MYTHICAL);
    }
    
    /**
     * 根据物品名称获取稀有度
     * @param itemName 物品名称
     * @return 对应的稀有度，如果未找到则返回普通稀有度
     */
    public static Rarity getItemRarity(String itemName) {
        return ITEM_RARITY_MAP.getOrDefault(itemName, Rarity.COMMON);
    }
    
    /**
     * 检查物品是否具有指定的稀有度
     * @param itemName 物品名称
     * @param rarity 要检查的稀有度
     * @return 是否匹配
     */
    public static boolean isItemRarity(String itemName, Rarity rarity) {
        return getItemRarity(itemName) == rarity;
    }
    
    /**
     * 获取物品的稀有度显示名称
     * @param itemName 物品名称
     * @return 稀有度显示名称
     */
    public static String getItemRarityDisplayName(String itemName) {
        return getItemRarity(itemName).getDisplayName();
    }
    
    /**
     * 获取物品的稀有度颜色
     * @param itemName 物品名称
     * @return 颜色代码
     */
    public static String getItemRarityColor(String itemName) {
        return getItemRarity(itemName).getColor();
    }
    
    /**
     * 获取物品的基础掉落概率
     * @param itemName 物品名称
     * @return 基础掉落概率
     */
    public static double getItemBaseProbability(String itemName) {
        return getItemRarity(itemName).getBaseProbability();
    }
    
    /**
     * 获取物品根据难度调整后的掉落概率
     * @param itemName 物品名称
     * @param difficulty 游戏难度
     * @return 调整后的掉落概率
     */
    public static double getItemAdjustedProbability(String itemName, String difficulty) {
        return getItemRarity(itemName).getAdjustedProbability(difficulty);
    }
    
    /**
     * 获取物品的掉落数量范围
     * @param itemName 物品名称
     * @return 最小和最大掉落数量
     */
    public static int[] getItemDropAmountRange(String itemName) {
        return getItemRarity(itemName).getDropAmountRange();
    }
    
    /**
     * 获取所有具有指定稀有度的物品列表
     * @param rarity 稀有度
     * @return 物品名称数组
     */
    public static String[] getItemsByRarity(Rarity rarity) {
        return ITEM_RARITY_MAP.entrySet().stream()
                .filter(entry -> entry.getValue() == rarity)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }
    
    /**
     * 获取稀有度统计信息
     * @return 各稀有度物品数量的映射
     */
    public static Map<Rarity, Integer> getRarityStatistics() {
        Map<Rarity, Integer> stats = new HashMap<>();
        for (Rarity rarity : Rarity.values()) {
            stats.put(rarity, 0);
        }
        
        for (Rarity rarity : ITEM_RARITY_MAP.values()) {
            stats.put(rarity, stats.get(rarity) + 1);
        }
        
        return stats;
    }
}