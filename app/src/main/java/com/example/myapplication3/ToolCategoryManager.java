package com.example.myapplication3;

import java.util.*;

/**
 * 工具分类管理器
 * 管理工具分类和区域工具对应关系
 */
public class ToolCategoryManager {
    
    // 工具分类映射表
    private static final Map<String, ToolCategory> TOOL_CATEGORY_MAP = new HashMap<>();
    
    // 区域允许的工具类型映射表
    private static final Map<String, Set<ToolCategory>> AREA_TOOL_MAP = new HashMap<>();
    
    static {
        // 初始化工具分类映射
        initToolCategoryMap();
        
        // 初始化区域工具对应关系
        initAreaToolMap();
    }
    
    /**
     * 初始化工具分类映射
     */
    private static void initToolCategoryMap() {
        // 斧类工具
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_STONE_AXE, ToolCategory.AXE);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_IRON_AXE, ToolCategory.AXE);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_DIAMOND_AXE, ToolCategory.AXE);
        
        // 镐类工具
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_STONE_PICKAXE, ToolCategory.PICKAXE);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_IRON_PICKAXE, ToolCategory.PICKAXE);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_DIAMOND_PICKAXE, ToolCategory.PICKAXE);
        
        // 镰刀类工具
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_STONE_SICKLE, ToolCategory.SICKLE);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_IRON_SICKLE, ToolCategory.SICKLE);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_DIAMOND_SICKLE, ToolCategory.SICKLE);
        
        // 鱼竿类工具
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_STONE_FISHING_ROD, ToolCategory.FISHING_ROD);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_IRON_FISHING_ROD, ToolCategory.FISHING_ROD);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_DIAMOND_FISHING_ROD, ToolCategory.FISHING_ROD);
        
        // 铲子类工具
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_STONE_SHOVEL, ToolCategory.SHOVEL);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_IRON_SHOVEL, ToolCategory.SHOVEL);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_DIAMOND_SHOVEL, ToolCategory.SHOVEL);
        
        // 锤子类工具
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_STONE_HAMMER, ToolCategory.HAMMER);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_IRON_HAMMER, ToolCategory.HAMMER);
        TOOL_CATEGORY_MAP.put(ItemConstants.EQUIP_DIAMOND_HAMMER, ToolCategory.HAMMER);
    }
    
    /**
     * 初始化区域工具对应关系
     */
    private static void initAreaToolMap() {
        // 树林、针叶林：斧头
        Set<ToolCategory> forestTools = new HashSet<>(Arrays.asList(ToolCategory.AXE));
        AREA_TOOL_MAP.put("树林", forestTools);
        AREA_TOOL_MAP.put("针叶林", forestTools);
        
        // 岩石区、雪山：镐
        Set<ToolCategory> rockTools = new HashSet<>(Arrays.asList(ToolCategory.PICKAXE));
        AREA_TOOL_MAP.put("岩石区", rockTools);
        AREA_TOOL_MAP.put("雪山", rockTools);
        
        // 草原、沙漠、沼泽：镰刀
        Set<ToolCategory> plantTools = new HashSet<>(Arrays.asList(ToolCategory.SICKLE));
        AREA_TOOL_MAP.put("草原", plantTools);
        AREA_TOOL_MAP.put("沙漠", plantTools);
        AREA_TOOL_MAP.put("沼泽", plantTools);
        
        // 河流：鱼竿
        Set<ToolCategory> waterTools = new HashSet<>(Arrays.asList(ToolCategory.FISHING_ROD));
        AREA_TOOL_MAP.put("河流", waterTools);
        
        // 海洋、深海：鱼竿
        Set<ToolCategory> oceanTools = new HashSet<>(Arrays.asList(ToolCategory.FISHING_ROD));
        AREA_TOOL_MAP.put("海洋", oceanTools);
        AREA_TOOL_MAP.put("深海", oceanTools);
        
        // 海滩：斧头、镰刀
        Set<ToolCategory> beachTools = new HashSet<>(Arrays.asList(ToolCategory.AXE, ToolCategory.SICKLE));
        AREA_TOOL_MAP.put("海滩", beachTools);
        
        // 废弃营地、村落：所有工具都可以
        Set<ToolCategory> campTools = new HashSet<>(Arrays.asList(
            ToolCategory.AXE, ToolCategory.PICKAXE, ToolCategory.SICKLE, ToolCategory.FISHING_ROD));
        AREA_TOOL_MAP.put("废弃营地", campTools);
        AREA_TOOL_MAP.put("村落", campTools);
        
        // 雪原：镐
        Set<ToolCategory> snowTools = new HashSet<>(Arrays.asList(ToolCategory.PICKAXE));
        AREA_TOOL_MAP.put("雪原", snowTools);
        
        // 茅草屋：无特殊工具要求
        Set<ToolCategory> houseTools = new HashSet<>(Arrays.asList(
            ToolCategory.AXE, ToolCategory.PICKAXE, ToolCategory.SICKLE, ToolCategory.FISHING_ROD));
        AREA_TOOL_MAP.put("茅草屋", houseTools);
    }
    
    /**
     * 获取工具的分类
     */
    public static ToolCategory getToolCategory(String toolType) {
        if (toolType == null || toolType.equals("无")) {
            return ToolCategory.NONE;
        }
        return TOOL_CATEGORY_MAP.getOrDefault(toolType, ToolCategory.NONE);
    }
    
    /**
     * 检查工具是否适用于区域
     */
    public static boolean isToolSuitableForArea(String toolType, String areaType) {
        ToolCategory toolCategory = getToolCategory(toolType);
        Set<ToolCategory> allowedCategories = AREA_TOOL_MAP.get(areaType);
        
        if (allowedCategories == null) {
            // 未知区域，允许所有工具
            return true;
        }
        
        return allowedCategories.contains(toolCategory);
    }
    
    /**
     * 获取区域允许的工具类型
     */
    public static Set<ToolCategory> getAllowedToolCategories(String areaType) {
        return AREA_TOOL_MAP.getOrDefault(areaType, new HashSet<>());
    }
    
    /**
     * 获取工具分类的描述
     */
    public static String getToolCategoryDescription(String toolType) {
        ToolCategory category = getToolCategory(toolType);
        return category.getDescription();
    }
}