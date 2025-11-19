package com.example.myapplication3;

import com.example.myapplication3.ItemConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * 建筑资源需求工具类
 * 统一管理所有建筑和升级的资源需求配置
 */
public class BuildingRequirements {

    // -------------- 基础建筑资源需求 --------------
    public static Map<String, Integer> createFireReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WOOD, 5);
        req.put(ItemConstants.ITEM_WEED, 5);
        return req;
    }

    public static Map<String, Integer> createFurnaceReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE, 10);
        req.put(ItemConstants.ITEM_WEED, 10);
        return req;
    }

    public static Map<String, Integer> createStorageReq(int currentWarehouseCount) {
        // 基础成本
        int baseWood = 5;
        int baseStone = 5;
        int baseWeed = 5;
        
        // 每建造一个仓库，成本增加50%（第N个仓库成本 = 基础成本 * (1 + (N-1) * 0.5)）
        double costMultiplier = 1.0 + currentWarehouseCount * 0.5;
        
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WOOD, (int) Math.ceil(baseWood * costMultiplier));
        req.put(ItemConstants.ITEM_STONE, (int) Math.ceil(baseStone * costMultiplier));
        req.put(ItemConstants.ITEM_WEED, (int) Math.ceil(baseWeed * costMultiplier));
        return req;
    }
    
    // 兼容旧方法（默认为第0个仓库）
    public static Map<String, Integer> createStorageReq() {
        return createStorageReq(0);
    }

    public static Map<String, Integer> createThatchHouseReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WOOD, 10);
        req.put(ItemConstants.ITEM_WEED, 10);
        return req;
    }

    public static Map<String, Integer> createPortalReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_INGOT, 20);
        req.put(ItemConstants.ITEM_DIAMOND, 10);
        req.put(ItemConstants.ITEM_SNOW_LOTUS, 10);
        return req;
    }

    // -------------- 房屋升级资源需求 --------------
    
    /**
     * 小木屋建造需求
     * 消耗：木板20 + 木头10
     * 说明：用木板和原木搭建的稳固木屋，比茅草屋更坚固耐用
     */
    public static Map<String, Integer> createSmallWoodenHouseReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WOODEN_PLANK, 20);  // 木板：主要结构材料
        req.put(ItemConstants.ITEM_WOOD, 10);           // 木头：辅助支撑材料
        return req;
    }

    /**
     * 小石屋建造需求
     * 消耗：石砖20 + 木板20 + 钉子10
     * 说明：用石砖和木板建造的坚固石屋，具有良好的保温性能
     */
    public static Map<String, Integer> createSmallStoneHouseReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE_BRICK, 20);    // 石砖：墙体主要材料
        req.put(ItemConstants.ITEM_WOODEN_PLANK, 20);   // 木板：屋顶和内部结构
        req.put(ItemConstants.ITEM_NAIL, 10);           // 钉子：固定连接材料
        return req;
    }

    /**
     * 砖瓦屋建造需求
     * 消耗：石砖30 + 水泥30 + 砖块50 + 铁板10 + 玻璃10 + 钉子20
     * 说明：精心建造的高级房屋，设施齐全，居住舒适度最高
     */
    public static Map<String, Integer> createBrickHouseReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_STONE_BRICK, 30);    // 石砖：地基和承重结构
        req.put(ItemConstants.ITEM_CEMENT, 30);         // 水泥：粘合材料
        req.put(ItemConstants.ITEM_BRICK, 50);         // 砖块：墙体主要材料
        req.put(ItemConstants.ITEM_IRON_PLATE, 10);     // 铁板：加固材料
        req.put(ItemConstants.ITEM_GLASS, 10);          // 玻璃：窗户材料
        req.put(ItemConstants.ITEM_NAIL, 20);           // 钉子：固定连接材料
        return req;
    }
}