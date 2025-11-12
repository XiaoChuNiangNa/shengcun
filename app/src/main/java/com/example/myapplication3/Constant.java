package com.example.myapplication3;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.example.myapplication3.ItemConstants;

public class Constant {
    // 广播Action常量
    public static final String ACTION_UPDATE_AREA_INFO = "com.example.action.UPDATE_AREA_INFO";
    
    // 数据库配置
    public static final String DB_NAME = "MagicDomainDB";
    public static final int DB_VERSION = 29;

    // 生存指标初始值
    public static final int INIT_LIFE = 100;//生命
    public static final int INIT_HUNGER = 100;//饥饿
    public static final int INIT_THIRST = 100;//口渴
    public static final int INIT_STAMINA = 100;//体力

    // 背包配置
    public static final int BACKPACK_INIT_CAP = 50;//初始
    public static final int BACKPACK_EXPAND_CAP = 20;//奖励

    // 1. 修改全局每日最大采集次数为50
    public static final int COLLECT_MAX_TIMES = 50;

    // 2. 新增：各区域独立采集次数上限（key:区域名称，value:最大次数）
    public static final Map<String, Integer> AREA_MAX_COLLECT_TIMES = new HashMap<>();

    // 3. 刷新时间点（游戏内0点）
    public static final int REFRESH_HOUR = 0;

    // 难度配置
    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_NORMAL = "normal";
    public static final String DIFFICULTY_HARD = "hard";

    // 工具初始耐久度常量
    public static final int STONE_TOOL_DURABILITY = 10;
    public static final int IRON_TOOL_DURABILITY = 20;
    public static final int DIAMOND_TOOL_DURABILITY = 30;
    public static final int MEDIUM_STONE_TOOL_DURABILITY = 5;
    public static final int MEDIUM_IRON_TOOL_DURABILITY = 10;
    public static final int MEDIUM_DIAMOND_TOOL_DURABILITY = 15;
    public static final int DURABILITY_GRASS_ROPE = 1;    // 绳索耐久度
    public static final int DURABILITY_REINFORCED_ROPE = 5;
    public static final int DURABILITY_HARD_ROPE = 10;

    // 地形分组
    public static final String[] AREA_AXE_BOOST = {"树林", "针叶林"};
    public static final String[] AREA_PICKAXE_BOOST = {"岩石区", "雪山"};
    public static final String[] AREA_SICKLE_BOOST = {"草原", "沙漠", "沼泽"};
    public static final String[] AREA_FISHING_BOOST = {"河流"};

    // 地形默认高度配置（key:地形名称，value:默认高度）
    public static final Map<String, Integer> TERRAIN_DEFAULT_HEIGHT = new HashMap<>();
    // 特定坐标的高度覆盖（key:"x,y"，value:高度）
    public static final Map<String, Integer> COORD_SPECIFIC_HEIGHT = new HashMap<>();

    // 时间系统常量
    public static final int GAME_HOURS_PER_DAY = 24;
    public static final long REAL_MINUTES_PER_GAME_HOUR = 10 * 1000; // 现实10秒钟 = 游戏1小时(改成60即1分钟)
    public static final int GAME_HOUR_DEFAULT = 7; // 默认时间0点
    public static final int GAME_DAY_DEFAULT = 1;  // 默认天数0天

    // 昼夜时间常量
    public static final int DAY_START = 6;   // 早上6点
    public static final int DAY_END = 18;    // 晚上18点

    // 背景类型
    public static final String BG_DAY = "bg_day";
    public static final String BG_NIGHT = "bg_night";

    // 体温系统常量
    public static final int TEMPERATURE_MIN = 20;
    public static final int TEMPERATURE_MAX = 50;
    public static final int TEMPERATURE_NORMAL_MIN = 30;
    public static final int TEMPERATURE_NORMAL_MAX = 40;
    public static final int TEMPERATURE_DEFAULT = 37;   //默认体温
    public static final int TEMP_CHANGE_PER_UPDATE = 1; // 每次更新的体温变化量

    static {
        // 初始化默认地形高度
        TERRAIN_DEFAULT_HEIGHT.put("草原", 1);
        TERRAIN_DEFAULT_HEIGHT.put("河流", 1);
        TERRAIN_DEFAULT_HEIGHT.put("沙漠", 1);
        TERRAIN_DEFAULT_HEIGHT.put("沼泽", 1);
        TERRAIN_DEFAULT_HEIGHT.put("岩石区", 2);
        TERRAIN_DEFAULT_HEIGHT.put("针叶林", 1);
        TERRAIN_DEFAULT_HEIGHT.put("雪山", 3);
        TERRAIN_DEFAULT_HEIGHT.put("树林", 1);
        TERRAIN_DEFAULT_HEIGHT.put("废弃营地", 1);
        TERRAIN_DEFAULT_HEIGHT.put("海洋", 1);
        TERRAIN_DEFAULT_HEIGHT.put("海滩", 1);
        TERRAIN_DEFAULT_HEIGHT.put("深海", 1);
        TERRAIN_DEFAULT_HEIGHT.put("雪原", 2);
        TERRAIN_DEFAULT_HEIGHT.put("村落", 1); // 村落默认高度1
        TERRAIN_DEFAULT_HEIGHT.put("传送门", 1); // 传送门默认高度1

        // 单独设置坐标(19,13)的高度为2（覆盖默认）
        COORD_SPECIFIC_HEIGHT.put("19,13", 1);
    }

    // 区域等级
    public static final int AREA_LEVEL_0 = 0;
    public static final int AREA_LEVEL_1 = 1;
    public static final int AREA_LEVEL_2 = 2;
    public static final int AREA_LEVEL_3 = 3;
    public static final int AREA_LEVEL_4 = 4;

    // 工具等级
    public static final int TOOL_LEVEL_NONE = 0;
    public static final int TOOL_LEVEL_STONE = 1;
    public static final int TOOL_LEVEL_IRON = 2;
    public static final int TOOL_LEVEL_DIAMOND = 3;

    // 工具加成
    public static final int BOOST_LEVEL_1 = 1;
    public static final int BOOST_LEVEL_2 = 2;
    public static final int BOOST_LEVEL_3 = 3;

    // 建筑类型
    public static final String BUILDING_FIRE = "篝火";
    public static final String BUILDING_CAMPFIRE = "篝火";
    public static final String BUILDING_THATCH_HOUSE = "茅草屋";
    public static final String BUILDING_WOOD_HOUSE = "小木屋";
    public static final String BUILDING_STONE_HOUSE = "小石屋";
    public static final String BUILDING_BRICK_HOUSE = "砖瓦屋";

    public static final String BUILDING_FURNACE = "熔炉";
    public static final String BUILDING_STORAGE = "仓库";
    public static final String BUILDING_TRADING_POST = "交易所";
    public static final String BUILDING_PORTAL = "传送门";

    // 建筑分类
    public static final String BUILDING_CATEGORY_OUTDOOR = "野外建筑";
    public static final String BUILDING_CATEGORY_INDOOR = "室内建筑";

    // 地图配置（引用GameMapData类）
    public static final int MAP_MIN = GameMapData.MAP_MIN;
    public static final int MAP_MAX = GameMapData.MAP_MAX;

    // 移动条件常量
    public static final String MOVE_CONDITION_NONE = "NONE";
    public static final String MOVE_CONDITION_ROPE = "ROPE";
    public static final String MOVE_CONDITION_BOAT = "BOAT";

    // 地图数据（引用GameMapData类）
    public static final String[][] MAP_DATA = GameMapData.MAIN_WORLD_MAP;

    // 地形常量（英文）
    public static final String GRASSLAND = "grassland"; // 草原
    public static final String RIVER = "river";         // 河流
    public static final String DESERT = "desert";       // 沙漠
    public static final String SWAMP = "swamp";         // 沼泽
    public static final String ROCK = "rock";           // 岩石区
    public static final String CONIFEROUS_FOREST = "coniferous_forest"; // 针叶林
    public static final String SNOW_MOUNTAIN = "snow_mountain"; // 雪山
    public static final String OCEAN = "ocean"; // 海洋
    public static final String BEACH = "beach"; // 海滩
    public static final String DEEP_OCEAN = "deep_ocean"; // 深海
    public static final String SNOWFIELD = "snowfield"; // 雪原
    public static final String VILLAGE = "village"; // 村落
    public static final String FOREST = "forest"; // 树林
    public static final String ABANDONED_CAMP = "abandoned_camp"; // 废弃营地
    public static final String PORTAL = "portal"; // 传送门

    // 中文地形 → 英文地形映射
    private static final Map<String, String> CHINESE_TO_ENGLISH_TERRAIN = new HashMap<>();

    static {
        CHINESE_TO_ENGLISH_TERRAIN.put("草原", GRASSLAND);
        CHINESE_TO_ENGLISH_TERRAIN.put("河流", RIVER);
        CHINESE_TO_ENGLISH_TERRAIN.put("沙漠", DESERT);
        CHINESE_TO_ENGLISH_TERRAIN.put("沼泽", SWAMP);
        CHINESE_TO_ENGLISH_TERRAIN.put("岩石区", ROCK);
        CHINESE_TO_ENGLISH_TERRAIN.put("针叶林", CONIFEROUS_FOREST);
        CHINESE_TO_ENGLISH_TERRAIN.put("雪山", SNOW_MOUNTAIN);
        CHINESE_TO_ENGLISH_TERRAIN.put("海洋", OCEAN);
        CHINESE_TO_ENGLISH_TERRAIN.put("海滩", BEACH);
        CHINESE_TO_ENGLISH_TERRAIN.put("深海", DEEP_OCEAN);
        CHINESE_TO_ENGLISH_TERRAIN.put("雪原", SNOWFIELD);
        CHINESE_TO_ENGLISH_TERRAIN.put("村落", VILLAGE);
        CHINESE_TO_ENGLISH_TERRAIN.put("树林", FOREST);
        CHINESE_TO_ENGLISH_TERRAIN.put("废弃营地", ABANDONED_CAMP);
        CHINESE_TO_ENGLISH_TERRAIN.put("传送门", PORTAL);
    }

    // 地形 -> 背景资源ID 映射（英文 key）
    public static final Map<String, Integer> TERRAIN_BACKGROUND_RES = new HashMap<>();

    static {
        TERRAIN_BACKGROUND_RES.put(GRASSLAND, R.drawable.caoyuan);
        TERRAIN_BACKGROUND_RES.put(RIVER, R.drawable.heliu);
        TERRAIN_BACKGROUND_RES.put(DESERT, R.drawable.shamo);
        TERRAIN_BACKGROUND_RES.put(SWAMP, R.drawable.zhaoze);
        TERRAIN_BACKGROUND_RES.put(ROCK, R.drawable.yanshiqu);
        TERRAIN_BACKGROUND_RES.put(CONIFEROUS_FOREST, R.drawable.zhenyelin);
        TERRAIN_BACKGROUND_RES.put(SNOW_MOUNTAIN, R.drawable.xueshan);
        TERRAIN_BACKGROUND_RES.put(OCEAN, R.drawable.haiyang);
        TERRAIN_BACKGROUND_RES.put(BEACH, R.drawable.haitan);
        TERRAIN_BACKGROUND_RES.put(DEEP_OCEAN, R.drawable.shenhai);
        TERRAIN_BACKGROUND_RES.put(SNOWFIELD, R.drawable.xueyuan);
        TERRAIN_BACKGROUND_RES.put(VILLAGE, R.drawable.cunluo);
        TERRAIN_BACKGROUND_RES.put(FOREST, R.drawable.shuling);
        TERRAIN_BACKGROUND_RES.put(ABANDONED_CAMP, R.drawable.feiqiyingdi);
        TERRAIN_BACKGROUND_RES.put(PORTAL, R.drawable.chuansongmen);
    }

    // 获取区域英文类型（用于背景图）
    public static String getAreaTypeByCoord(int x, int y) {
        if (x < MAP_MIN || x > MAP_MAX || y < MAP_MIN || y > MAP_MAX) {
            return "未知区域（坐标错误）";
        }
        String chineseTerrain = MAP_DATA[y - 1][x - 1];
        return CHINESE_TO_ENGLISH_TERRAIN.getOrDefault(chineseTerrain, chineseTerrain);
    }

    // 获取区域中文类型（用于掉落表）
    public static String getAreaChineseTypeByCoord(int x, int y) {
        if (x < MAP_MIN || x > MAP_MAX || y < MAP_MIN || y > MAP_MAX) {
            return "未知区域";
        }
        return MAP_DATA[y - 1][x - 1];
    }

    // 掉落物数据结构
    public static class DropItem {
        public String itemName;
        public int minAmount;
        public int maxAmount;

        public DropItem(String itemName, int minAmount, int maxAmount) {
            this.itemName = itemName;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
    }

//    // 区域掉落配置（中文 key）
//    public static final Map<String, List<DropItem>> AREA_DROPS;
//
//    static {
//        AREA_DROPS = new HashMap<>();
//
//        // 草原
//        AREA_DROPS.put("草原", Arrays.asList(
//                new DropItem(ITEM_WEED, 1, 5),
//                new DropItem(ITEM_BERRY, 0, 2),
//                new DropItem(ITEM_HERB, 0, 2),
//                new DropItem(ITEM_FIBER, 0, 2)
//
//        ));
//
//        // 雪原
//        AREA_DROPS.put("雪原", Arrays.asList(
//                new DropItem(ITEM_ICE, 1, 3),
//                new DropItem(ITEM_STONE, 1, 3),
//                new DropItem(ITEM_WATER, 1, 2)
//        ));
//
//        // 树林
//        AREA_DROPS.put("树林", Arrays.asList(
//                new DropItem(ITEM_WOOD, 1, 4),
//                new DropItem(ITEM_APPLE, 0, 2),   // 苹果
//                new DropItem(ITEM_HERB, 0, 1),     // 药草
//                new DropItem(ITEM_VINE, 0, 1),     // 藤蔓
//                new DropItem("蜂巢", 0, 1)         // 新增蜂巢
//        ));
//
//        // 针叶林
//        AREA_DROPS.put("针叶林", Arrays.asList(
//                new DropItem(ITEM_WOOD, 2, 6),
//                new DropItem(ITEM_ACORN, 0, 2),
//                new DropItem(ITEM_HERB, 0, 1),
//                new DropItem(ITEM_VINE, 1, 2),
//                new DropItem(ITEM_RESIN, 0, 1),
//                new DropItem(ITEM_TRUFFLE, 0, 1)
//        ));
//
//        // 岩石区
//        AREA_DROPS.put("岩石区", Arrays.asList(
//                new DropItem(ITEM_STONE, 1, 5),
//                new DropItem(ITEM_IRON_ORE, 0, 2),
//                new DropItem(ITEM_GEM, 0, 1),
//                new DropItem(ITEM_FLINT, 0, 1),
//                new DropItem(ITEM_SULFUR, 0, 1),
//                new DropItem(ITEM_COAL, 0, 1)
//        ));
//
//        // 雪山
//        AREA_DROPS.put("雪山", Arrays.asList(
//                new DropItem(ITEM_ICE, 1, 3),
//                new DropItem(ITEM_STONE, 2, 6),
//                new DropItem(ITEM_IRON_ORE, 1, 3),
//                new DropItem(ITEM_GEM, 0, 2),
//                new DropItem(ITEM_SNOW_LOTUS, 0, 1),
//                new DropItem(ITEM_OBSIDIAN, 0, 1)
//        ));
//
//        // 河流
//        AREA_DROPS.put("河流", Arrays.asList(
//                new DropItem(ITEM_WATER, 1, 5),
//                new DropItem(ITEM_FISH, 0, 2),
//                new DropItem(ITEM_WEED, 1, 3)
//        ));
//
//        // 海洋
//        AREA_DROPS.put("海洋", Arrays.asList(
//                new DropItem(ITEM_WATER, 3, 15),
//                new DropItem(ITEM_FISH, 2, 4),
//                new DropItem(ITEM_WEED, 2, 4),
//                new DropItem(ITEM_GEM, 0, 1),
//                new DropItem(ITEM_KELP, 0, 2)
//        ));
//
//        // 深海
//        AREA_DROPS.put("深海", Arrays.asList(
//                new DropItem(ITEM_WATER, 4, 20),
//                new DropItem(ITEM_FISH, 3, 6),
//                new DropItem(ITEM_WEED, 3, 6),
//                new DropItem(ITEM_GEM, 0, 1),
//                new DropItem(ITEM_KELP, 1, 4)
//        ));
//
//        // 海滩
//        AREA_DROPS.put("海滩", Arrays.asList(
//                new DropItem(ITEM_WATER, 1, 3),
//                new DropItem(ITEM_SAND, 1, 3),
//                new DropItem(ITEM_FISH, 0, 1),
//                new DropItem(ITEM_SHELL, 0, 1),
//                new DropItem(ITEM_COCONUT, 0, 1),
//                new DropItem(ITEM_CRAWFISH, 0, 2)
//        ));
//
//        // 沙漠
//        AREA_DROPS.put("沙漠", Arrays.asList(
//                new DropItem(ITEM_WEED, 1, 2),
//                new DropItem(ITEM_CACTUS_FRUIT, 0, 2),
//                new DropItem(ITEM_SAND, 1, 5)
//        ));
//
//        // 沼泽
//        AREA_DROPS.put("沼泽", Arrays.asList(
//                new DropItem(ITEM_WATER, 1, 3),
//                new DropItem(ITEM_WEED, 1, 3),
//                new DropItem(ITEM_MUSHROOM, 0, 2),
//                new DropItem(ITEM_REED, 0, 2),
//                new DropItem(ITEM_CLAY, 0, 2)
//        ));
//
//        // 废弃营地
//        AREA_DROPS.put("废弃营地", Arrays.asList(
//                new DropItem(ITEM_STONE_PICKAXE, 0, 1),
//                new DropItem(ITEM_STONE_AXE, 0, 1),
//                new DropItem(ITEM_STONE_FISHING_ROD, 0, 1),
//                new DropItem(ITEM_DRIED_BREAD, 0, 2),
//                new DropItem(ITEM_HERB, 0, 2)
//        ));
//
//        // 村落
//        AREA_DROPS.put("村落", Arrays.asList(
//                new DropItem(ITEM_STONE_PICKAXE, 0, 1),
//                new DropItem(ITEM_STONE_AXE, 0, 1),
//                new DropItem(ITEM_STONE_FISHING_ROD, 0, 1),
//                new DropItem(ITEM_DRIED_BREAD, 1, 4),
//                new DropItem(ITEM_HERB, 1, 3),
//                new DropItem(ITEM_IRON_INGOT, 0, 1)
//        ));
//
//        // 树林
//        AREA_DROPS.put("树林", Arrays.asList(
//                new DropItem(ITEM_WOOD, 1, 4),
//                new DropItem(ITEM_BERRY, 0, 3),
//                new DropItem(ITEM_HERB, 0, 1)
//        ));
//    }

    //采集前检查次数和恢复时间
    public static boolean canCollect(String areaName, int collectedTimes, long lastCollectTime) {
        AreaConfig cfg = AREA_CONFIG.get(areaName);
        if (cfg == null) return false;

        // 检查是否达到采集次数上限
//        if (collectedTimes >= cfg.collectMaxTimes) {
//            return false;
//        }

        // 检查恢复时间
        long now = SystemClock.elapsedRealtime();
        long cd = cfg.recoveryMinutes * 60 * 1000;
        if (now - lastCollectTime < cd) {
            return false;
        }

        return true;
    }

    public static int getMoveStaminaCost(String fromArea, String toArea) {
        AreaConfig cfgFrom = AREA_CONFIG.get(fromArea);
        AreaConfig cfgTo = AREA_CONFIG.get(toArea);
        if (cfgFrom == null || cfgTo == null) return -1; // 未知区域

        int delta = Math.abs(cfgTo.height - cfgFrom.height);

        if (delta == 0) {
            return 5; // 同高度
        } else if (delta == 1) {
            return 10; // 高度差1，需要绳索
        } else {
            return -2; // 高度差>1，不能移动
        }
    }

    public static boolean canMove(String fromArea, String toArea, int stamina, Map<String, Integer> backpack) {
        AreaConfig cfgTo = AREA_CONFIG.get(toArea);
        if (cfgTo == null) return false;

        // 特殊移动条件
        if (cfgTo.moveCondition.equals(MOVE_CONDITION_BOAT)) {
            if (!backpack.containsKey(ItemConstants.ITEM_WOODEN_BOAT) || backpack.get(ItemConstants.ITEM_WOODEN_BOAT) <= 0) {
                return false; // 没有船
            }
        }

        int cost = getMoveStaminaCost(fromArea, toArea);
        if (cost < 0) return false; // 不能移动

        if (cost == 10) { // 需要绳索
            int ropeDurability = 0;
            if (backpack.containsKey(ItemConstants.ITEM_GRASS_ROPE)) ropeDurability += backpack.get(ItemConstants.ITEM_GRASS_ROPE) * DURABILITY_GRASS_ROPE;
            if (backpack.containsKey(ItemConstants.ITEM_REINFORCED_ROPE)) ropeDurability += backpack.get(ItemConstants.ITEM_REINFORCED_ROPE) * DURABILITY_REINFORCED_ROPE;
            if (backpack.containsKey(ItemConstants.ITEM_HARD_ROPE)) ropeDurability += backpack.get(ItemConstants.ITEM_HARD_ROPE) * DURABILITY_HARD_ROPE;

            if (ropeDurability <= 0) return false; // 没有可用绳索
        }

        return stamina >= cost;
    }

    public static void consumeMoveResources(String fromArea, String toArea, int[] stamina, Map<String, Integer> backpack) {
        int cost = getMoveStaminaCost(fromArea, toArea);
        if (cost < 0) return;

        // 扣体力
        stamina[0] -= cost;

        // 扣绳索耐久
        if (cost == 10) {
            if (backpack.containsKey(ItemConstants.ITEM_GRASS_ROPE) && backpack.get(ItemConstants.ITEM_GRASS_ROPE) > 0) {
                backpack.put(ItemConstants.ITEM_GRASS_ROPE, backpack.get(ItemConstants.ITEM_GRASS_ROPE) - 1);
            } else if (backpack.containsKey(ItemConstants.ITEM_REINFORCED_ROPE) && backpack.get(ItemConstants.ITEM_REINFORCED_ROPE) > 0) {
                backpack.put(ItemConstants.ITEM_REINFORCED_ROPE, backpack.get(ItemConstants.ITEM_REINFORCED_ROPE) - 1);
            } else if (backpack.containsKey(ItemConstants.ITEM_HARD_ROPE) && backpack.get(ItemConstants.ITEM_HARD_ROPE) > 0) {
                backpack.put(ItemConstants.ITEM_HARD_ROPE, backpack.get(ItemConstants.ITEM_HARD_ROPE) - 1);
            }
        }
    }

    private static final Random random = new Random();

    // 获取区域等级
    public static int getAreaLevel(String areaType) {
        switch (areaType) {
            // 等级0：草原、树林、沙漠、废弃营地、村落、雪原
            case "草原":
            case "树林":
            case "沙漠":
            case "废弃营地":
            case "村落":
            case "雪原":
                return AREA_LEVEL_0;
            
            // 等级1：针叶林、岩石区、河流、海洋、海滩、沼泽
            case "针叶林":
            case "岩石区":
            case "河流":
            case "海洋":
            case "海滩":
            case "沼泽":
                return AREA_LEVEL_1;
            
            // 等级2：深海
            case "深海":
                return AREA_LEVEL_2;
            
            // 等级3：雪山
            case "雪山":
                return AREA_LEVEL_3;
            
            default:
                return AREA_LEVEL_0;
        }
    }

    // 获取工具等级
    public static int getToolLevel(String toolType) {
        if (toolType == null || toolType.equals("无")) {
            return TOOL_LEVEL_NONE;
        }
        if (toolType.contains("石")) {
            return TOOL_LEVEL_STONE;
        } else if (toolType.contains("铁")) {
            return TOOL_LEVEL_IRON;
        } else if (toolType.contains("钻石")) {
            return TOOL_LEVEL_DIAMOND;
        }
        return TOOL_LEVEL_NONE;
    }

    // 检查工具是否适用于区域
    public static boolean isToolSuitableForArea(String toolType, String areaType) {
        // 先检查工具等级是否足够
        int toolLevel = getToolLevel(toolType);
        int areaLevel = getAreaLevel(areaType);
        
        Log.d("ToolDebug", "检查等级: toolLevel=" + toolLevel + ", areaLevel=" + areaLevel);
        
        if (areaLevel == AREA_LEVEL_0) {
            Log.d("ToolDebug", "等级0区域，无需工具限制");
            return true; // 等级0区域无需工具或任意工具都可以采集
        } else if (areaLevel == AREA_LEVEL_1) {
            boolean result = toolLevel >= TOOL_LEVEL_STONE; // 等级1区域需要石质或以上工具
            Log.d("ToolDebug", "等级1区域检查: " + result + " (需要工具等级 >= " + TOOL_LEVEL_STONE + ")");
            return result;
        }
        
        // 对于等级2、3区域，先检查工具等级是否足够
        boolean levelResult = toolLevel >= areaLevel;
        Log.d("ToolDebug", "等级" + areaLevel + "区域等级检查: " + levelResult + " (需要工具等级 >= " + areaLevel + ")");
        
        if (!levelResult) {
            return false; // 工具等级不足，无法采集
        }
        
        // 如果工具等级足够，再检查工具类型是否匹配（使用新的工具分类系统）
        boolean typeResult = ToolCategoryManager.isToolSuitableForArea(toolType, areaType);
        Log.d("ToolDebug", "工具类型匹配检查: " + typeResult + " (工具分类匹配)");
        
        return typeResult; // 工具等级足够且类型匹配才允许采集
    }

    // 检查工具是否匹配区域类型（已废弃，使用ToolCategoryManager替代）
    public static boolean isToolMatchingAreaType(String toolType, String areaType) {
        // 调用新的工具分类系统
        return ToolCategoryManager.isToolSuitableForArea(toolType, areaType);
    }

    // 获取移动体力消耗
    public static int getStaminaCost(String areaType) {
        switch (areaType) {
            case "岩石区":
            case "沙漠":
                return 10;
            case "沼泽":
            case "雪山":
                return 15;
            default:
                return 5;
        }
    }

    // 获取区域随机掉落物
    public static List<Item> getAreaLoot(String areaType) {

        List<Item> loot = new ArrayList<>();
        Log.d("CollectDebug", "开始生成区域[" + areaType + "]的掉落物"); // 新增
        // 1. 获取AreaResourceManager实例
        AreaResourceManager resourceManager = AreaResourceManager.getInstance();
        // 2. 获取区域资源配置（正确引用内部类AreaResource）
        AreaResourceManager.AreaResource areaResource = resourceManager.getAreaResource(areaType);

        // 3. 检查资源是否存在
        if (areaResource == null || areaResource.resources == null || areaResource.resources.isEmpty()) {
            Log.d("CollectDebug", "区域[" + areaType + "]无配置资源，返回空掉落物"); // 新增
            return loot;
        }

        // 4. 遍历资源（正确引用内部类ResourceItem）
        for (AreaResourceManager.ResourceItem item : areaResource.resources) {
            // 5. 访问maxCount和minCount（ResourceItem的成员变量）
            int amount = random.nextInt(item.maxCount - item.minCount + 1) + item.minCount;
            if (amount > 0) {
                loot.add(new Item(item.name, amount));
                Log.d("CollectDebug", "生成掉落物: " + item.name + " x" + amount); // 新增
            }
        }

        Log.d("CollectDebug", "区域[" + areaType + "]最终掉落物列表: " + loot.toString()); // 新增
        return loot;
    }

    // 食物恢复效果
    public static class FoodEffect {
        public int life;     // 生命
        public int hunger;   // 饥饿
        public int thirst;   // 口渴
        public int stamina;  // 体力

        public FoodEffect(int life, int hunger, int thirst, int stamina) {
            this.life = life;
            this.hunger = hunger;
            this.thirst = thirst;
            this.stamina = stamina;
        }
    }
    public static final Map<String, FoodEffect> FOOD_EFFECTS;
    static {
        FOOD_EFFECTS = new HashMap<>();
        // 水
        FOOD_EFFECTS.put(ItemConstants.ITEM_WATER, new FoodEffect(-5, 0, 5, 0));
        // 浆果
        FOOD_EFFECTS.put(ItemConstants.ITEM_BERRY, new FoodEffect(0, 5, 5, 0));
        // 药草
        FOOD_EFFECTS.put(ItemConstants.ITEM_HERB, new FoodEffect(10, 0, 0, 5));
        // 苹果
        FOOD_EFFECTS.put(ItemConstants.ITEM_APPLE, new FoodEffect(0, 10, 5, 0));
        // 橡果
        FOOD_EFFECTS.put(ItemConstants.ITEM_ACORN, new FoodEffect(0, 15, -5, 5));
        // 冰块
        FOOD_EFFECTS.put(ItemConstants.ITEM_ICE, new FoodEffect(-5, 0, 10, 0));
        // 雪莲
        FOOD_EFFECTS.put(ItemConstants.ITEM_SNOW_LOTUS, new FoodEffect(20, 0, 0, 0));
        // 鱼
        FOOD_EFFECTS.put(ItemConstants.ITEM_FISH, new FoodEffect(-10, 10, -5, 0));
        // 仙人掌果
        FOOD_EFFECTS.put(ItemConstants.ITEM_CACTUS_FRUIT, new FoodEffect(-5, 10, 5, 0));
        // 蘑菇
        FOOD_EFFECTS.put(ItemConstants.ITEM_MUSHROOM, new FoodEffect(-10, 5, 0, 0));
        // 干面包
        FOOD_EFFECTS.put(ItemConstants.ITEM_DRIED_BREAD, new FoodEffect(0, 20, -10, 10));
        // 草药
        FOOD_EFFECTS.put(ItemConstants.ITEM_MEDICINE, new FoodEffect(20, 0, 0, 0));
        // 烤鱼
        FOOD_EFFECTS.put(ItemConstants.ITEM_GRILLED_FISH, new FoodEffect(0, 30, -10, 30));
        // 鱼汤
        FOOD_EFFECTS.put(ItemConstants.ITEM_FISH_SOUP, new FoodEffect(0, 20, 20, 20));
        // 水果派
        FOOD_EFFECTS.put(ItemConstants.ITEM_FRUIT_PIE, new FoodEffect(0, 30, 10, 30));
        // 高级草药
        FOOD_EFFECTS.put(ItemConstants.ITEM_ADVANCED_HERB, new FoodEffect(40, 0, 0, 10));
        // 贝壳
        FOOD_EFFECTS.put(ItemConstants.ITEM_SHELL, new FoodEffect(-5, 5, -5, 0));
        // 松露
        FOOD_EFFECTS.put(ItemConstants.ITEM_TRUFFLE, new FoodEffect(-5, 10, 5, 5));
        // 蘑菇汤
        FOOD_EFFECTS.put(ItemConstants.ITEM_MUSHROOM_SOUP, new FoodEffect(10, 40, 20, 10));
        // 海带
        FOOD_EFFECTS.put(ItemConstants.ITEM_KELP, new FoodEffect(-5, 5, 5, 0));
        // 海带汤
        FOOD_EFFECTS.put(ItemConstants.ITEM_KELP_SOUP, new FoodEffect(0, 15, 20, 10));
        // 椰子
        FOOD_EFFECTS.put(ItemConstants.ITEM_COCONUT, new FoodEffect(10, 10, 10, 10));
        // 螃蟹
        FOOD_EFFECTS.put(ItemConstants.ITEM_CRAWFISH, new FoodEffect(-20, 5, 0, 0));
        // 烤螃蟹
        FOOD_EFFECTS.put(ItemConstants.ITEM_GRILLED_CRAWFISH, new FoodEffect(0, 20, 0, 10));
    }
    // 合成配方类
    public static class CraftRecipe {
        public String resultItem;
        public int resultAmount;
        public List<DropItem> materials;

        public CraftRecipe(String resultItem, int resultAmount, List<DropItem> materials) {
            this.resultItem = resultItem;
            this.resultAmount = resultAmount;
            this.materials = materials;
        }
    }

    // 熔炼配方类
    public static class SmeltRecipe {
        public String resultItem;
        public int resultAmount;
        public List<DropItem> materials;

        public SmeltRecipe(String resultItem, int resultAmount, List<DropItem> materials) {
            this.resultItem = resultItem;
            this.resultAmount = resultAmount;
            this.materials = materials;
        }
    }

    // 熔炼配方表
    public static final Map<String, SmeltRecipe> SMELT_RECIPES;
    static {
        SMELT_RECIPES = new HashMap<>();

        // 木炭
        SMELT_RECIPES.put(ItemConstants.ITEM_CHARCOAL, new SmeltRecipe(
                ItemConstants.ITEM_CHARCOAL,
                1,
                Arrays.asList(
                        new DropItem(ItemConstants.ITEM_WOOD, 2, 2),
                        new DropItem(ItemConstants.ITEM_WEED, 2, 2)
                )
        ));

        // 铁锭
        SMELT_RECIPES.put(ItemConstants.ITEM_IRON_INGOT, new SmeltRecipe(
                ItemConstants.ITEM_IRON_INGOT,
                1,
                Arrays.asList(
                        new DropItem(ItemConstants.ITEM_IRON_ORE, 2, 2),
                        new DropItem(ItemConstants.ITEM_CHARCOAL, 1, 1)
                )
        ));

        // 钻石
        SMELT_RECIPES.put(ItemConstants.ITEM_DIAMOND, new SmeltRecipe(
                ItemConstants.ITEM_DIAMOND,
                1,
                Arrays.asList(
                        new DropItem(ItemConstants.ITEM_GEM, 2, 2),
                        new DropItem(ItemConstants.ITEM_CHARCOAL, 1, 1)
                )
        ));

        // 陶罐
        SMELT_RECIPES.put(ItemConstants.ITEM_CLAY_POT, new SmeltRecipe(
                ItemConstants.ITEM_CLAY_POT,
                1,
                Arrays.asList(
                        new DropItem(ItemConstants.ITEM_CLAY, 5, 5),
                        new DropItem(ItemConstants.ITEM_CHARCOAL, 1, 1)
                )
        ));

        // 开水×5
        SMELT_RECIPES.put(ItemConstants.ITEM_BOILED_WATER, new SmeltRecipe(
                ItemConstants.ITEM_BOILED_WATER,
                5,
                Arrays.asList(
                        new DropItem(ItemConstants.ITEM_WATER, 5, 5),
                        new DropItem(ItemConstants.ITEM_CLAY_POT, 1, 1), // 不消耗
                        new DropItem(ItemConstants.ITEM_CHARCOAL, 1, 1)
                )
        ));
    }

    // 区域配置类
    public static class AreaConfig {
        public int collectMaxTimes;   // 采集次数上限
        public int recoveryMinutes;   // 恢复时间（分钟）
        public int height;            // 地形高度
        public String moveCondition;  // 移动条件：NONE, ROPE, BOAT
        public List<String> resources;

        public AreaConfig(int collectMaxTimes, int recoveryMinutes, int height, String moveCondition) {
            this.collectMaxTimes = collectMaxTimes;
            this.recoveryMinutes = recoveryMinutes;
            this.height = height;
            this.moveCondition = moveCondition;
            this.resources = resources;
        }
    }

    // 区域配置表
    public static final Map<String, AreaConfig> AREA_CONFIG;

    static {
        AREA_CONFIG = new HashMap<>();
        AREA_CONFIG.put("草原",     new AreaConfig(10, 1, 1, "NONE"));
        AREA_CONFIG.put("树林",     new AreaConfig(10, 1, 1, "NONE"));
        AREA_CONFIG.put("针叶林",   new AreaConfig(5, 2, 2, "NONE"));
        AREA_CONFIG.put("岩石区",   new AreaConfig(5, 2, 3, "ROPE"));
        AREA_CONFIG.put("雪山",     new AreaConfig(5, 5, 4, "ROPE"));
        AREA_CONFIG.put("河流",     new AreaConfig(10, 2, 1, "NONE"));
        AREA_CONFIG.put("海洋",     new AreaConfig(20, 5, 1, "BOAT"));
        AREA_CONFIG.put("深海",     new AreaConfig(30, 10, 1, "BOAT"));
        AREA_CONFIG.put("海滩",     new AreaConfig(5, 2, 1, "NONE"));
        AREA_CONFIG.put("沙漠",     new AreaConfig(10, 2, 1, "NONE"));
        AREA_CONFIG.put("沼泽",     new AreaConfig(10, 2, 1, "BOAT"));
        AREA_CONFIG.put("废弃营地", new AreaConfig(5, 10, 1, "NONE"));
        AREA_CONFIG.put("村落",     new AreaConfig(5, 10, 1, "NONE"));
        AREA_CONFIG.put("雪原",     new AreaConfig(10, 2, 2, "ROPE"));
        AREA_CONFIG.put("茅草屋", new AreaConfig(0, 0, 1, "NONE"));
        AREA_CONFIG.put("小木屋", new AreaConfig(0, 0, 1, "NONE"));
        AREA_CONFIG.put("小石屋", new AreaConfig(0, 0, 1, "NONE"));
        AREA_CONFIG.put("砖瓦屋", new AreaConfig(0, 0, 1, "NONE"));
        AREA_CONFIG.put("传送门", new AreaConfig(0, 0, 1, "NONE"));

    }

    //贸易
    // 贸易价格配置
    public static class TradePrice {
        public double buyPrice;   // 购买价格（金币）
        public double sellPrice;  // 出售价格（金币）

        public TradePrice(double buyPrice, double sellPrice) {
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }
    }

    //交易物品价格
    public static final Map<String, TradePrice> TRADE_PRICES;

    static {
        TRADE_PRICES = new HashMap<>();
        TRADE_PRICES.put(ItemConstants.ITEM_WEED,        new TradePrice(1, 0.5));
        TRADE_PRICES.put(ItemConstants.ITEM_BERRY,       new TradePrice(2, 1));
        TRADE_PRICES.put(ItemConstants.ITEM_HERB,        new TradePrice(8, 4));
        TRADE_PRICES.put(ItemConstants.ITEM_FIBER,       new TradePrice(1, 0.5));
        TRADE_PRICES.put(ItemConstants.ITEM_WOOD,        new TradePrice(2, 1));
        TRADE_PRICES.put(ItemConstants.ITEM_APPLE,       new TradePrice(2, 1));
        TRADE_PRICES.put(ItemConstants.ITEM_VINE,        new TradePrice(3, 1.5));
        TRADE_PRICES.put(ItemConstants.ITEM_HONEYCOMB,   new TradePrice(15, 7.5)); // 修正原7为7.5（保持出售价为购买价一半）
        TRADE_PRICES.put(ItemConstants.ITEM_ACORN,       new TradePrice(2, 1));
        TRADE_PRICES.put(ItemConstants.ITEM_RESIN,       new TradePrice(10, 5));
        TRADE_PRICES.put(ItemConstants.ITEM_TRUFFLE,     new TradePrice(18, 9));
        TRADE_PRICES.put(ItemConstants.ITEM_STONE,       new TradePrice(1, 0.5));
        TRADE_PRICES.put(ItemConstants.ITEM_IRON_ORE,    new TradePrice(12, 6));
        TRADE_PRICES.put(ItemConstants.ITEM_GEM,         new TradePrice(50, 25));
        TRADE_PRICES.put(ItemConstants.ITEM_FLINT,       new TradePrice(5, 2.5));
        TRADE_PRICES.put(ItemConstants.ITEM_SULFUR,      new TradePrice(15, 7.5)); // 修正原7为7.5
        TRADE_PRICES.put(ItemConstants.ITEM_COAL,        new TradePrice(8, 4));
        TRADE_PRICES.put(ItemConstants.ITEM_ICE,         new TradePrice(2, 1));
        TRADE_PRICES.put(ItemConstants.ITEM_OBSIDIAN,    new TradePrice(30, 15));
        TRADE_PRICES.put(ItemConstants.ITEM_SNOW_LOTUS,  new TradePrice(20, 10));
        TRADE_PRICES.put(ItemConstants.ITEM_WATER,       new TradePrice(1, 0.5));
        TRADE_PRICES.put(ItemConstants.ITEM_FISH,        new TradePrice(5, 2.5));
        TRADE_PRICES.put(ItemConstants.ITEM_KELP,        new TradePrice(3, 1.5));
        TRADE_PRICES.put(ItemConstants.ITEM_SAND,        new TradePrice(1, 0.5));
        TRADE_PRICES.put(ItemConstants.ITEM_SHELL,       new TradePrice(4, 2));
        TRADE_PRICES.put(ItemConstants.ITEM_COCONUT,     new TradePrice(4, 2));
        TRADE_PRICES.put(ItemConstants.ITEM_CRAWFISH,    new TradePrice(6, 3));
        TRADE_PRICES.put(ItemConstants.ITEM_CACTUS_FRUIT,new TradePrice(3, 1.5));
        TRADE_PRICES.put(ItemConstants.ITEM_MUSHROOM,    new TradePrice(4, 2));
        TRADE_PRICES.put(ItemConstants.ITEM_REED,        new TradePrice(3, 1.5));
        TRADE_PRICES.put(ItemConstants.ITEM_CLAY,        new TradePrice(2, 1));
        TRADE_PRICES.put(ItemConstants.ITEM_DRIED_BREAD, new TradePrice(10, 5));

        // 新增原料
        TRADE_PRICES.put(ItemConstants.ITEM_RICE,        new TradePrice(5, 2.5));          // 大米
        TRADE_PRICES.put(ItemConstants.ITEM_WINTER_MELON,new TradePrice(10, 5));          // 冬瓜
        TRADE_PRICES.put(ItemConstants.ITEM_CORN,        new TradePrice(5, 2.5));          // 玉米
        TRADE_PRICES.put(ItemConstants.ITEM_BEET,        new TradePrice(3, 1.5));          // 甜菜
        TRADE_PRICES.put(ItemConstants.ITEM_SPINACH,     new TradePrice(3, 1.5));          // 菠菜
        TRADE_PRICES.put(ItemConstants.ITEM_CARROT,      new TradePrice(2, 1));            // 胡萝卜
        TRADE_PRICES.put(ItemConstants.ITEM_POTATO,      new TradePrice(2, 1));            // 土豆
        TRADE_PRICES.put("黄瓜",           new TradePrice(3, 1.5));          // 黄瓜（假设常量为"黄瓜"，若有定义常量需替换）
        TRADE_PRICES.put(ItemConstants.ITEM_HONEY,       new TradePrice(10, 5));           // 蜂蜜

        // 新增烹饪食品食物
        TRADE_PRICES.put(ItemConstants.ITEM_ICY_SNOW_LOTUS_COCONUT,         new TradePrice(30, 15));   // 冰镇雪莲椰汁
        TRADE_PRICES.put(ItemConstants.ITEM_HONEY_APPLE_SLICE,              new TradePrice(15, 7.5));  // 蜂蜜苹果片
        TRADE_PRICES.put(ItemConstants.ITEM_RICE_PORRIDGE,                  new TradePrice(8, 4));     // 大米清粥
        TRADE_PRICES.put(ItemConstants.ITEM_KELP_WINTER_MELON_SOUP,         new TradePrice(18, 9));    // 海带冬瓜汤
        TRADE_PRICES.put(ItemConstants.ITEM_ROASTED_POTATO,                 new TradePrice(5, 2.5));   // 烤土豆
        TRADE_PRICES.put(ItemConstants.ITEM_FRUIT_SMOOTHIE,                 new TradePrice(15, 7.5));  // 水果冰沙
        TRADE_PRICES.put(ItemConstants.ITEM_CRAWFISH_SHELL_SOUP,            new TradePrice(15, 7.5));  // 螃蟹贝壳汤
        TRADE_PRICES.put(ItemConstants.ITEM_STEAMED_CORN,                   new TradePrice(8, 4));     // 蒸玉米
        TRADE_PRICES.put(ItemConstants.ITEM_MUSHROOM_TRUFFLE_FRY,           new TradePrice(30, 15));   // 蘑菇松露煎
        TRADE_PRICES.put(ItemConstants.ITEM_BERRY_HONEY_BREAD,              new TradePrice(18, 9));    // 浆果蜂蜜面包
        TRADE_PRICES.put(ItemConstants.ITEM_BEET_HONEY_DRINK,               new TradePrice(15, 7.5));  // 甜菜蜂蜜饮
        TRADE_PRICES.put(ItemConstants.ITEM_BOILED_SPINACH,                 new TradePrice(5, 2.5));   // 水煮菠菜
        TRADE_PRICES.put(ItemConstants.ITEM_ROASTED_ACORN,                  new TradePrice(5, 2.5));   // 烤橡果
        TRADE_PRICES.put(ItemConstants.ITEM_CACTUS_FRUIT_ICE_DRINK,         new TradePrice(8, 4));     // 仙人掌果冰饮
        TRADE_PRICES.put(ItemConstants.ITEM_CARROT_POTATO_SOUP,             new TradePrice(8, 4));     // 胡萝卜土豆汤
        TRADE_PRICES.put(ItemConstants.ITEM_COCONUT_BERRY_DRINK,            new TradePrice(10, 5));    // 椰汁浆果饮
        TRADE_PRICES.put(ItemConstants.ITEM_TRUFFLE_MUSHROOM_SOUP,          new TradePrice(30, 15));   // 松露蘑菇汤
        TRADE_PRICES.put(ItemConstants.ITEM_APPLE_HONEY_DRINK,              new TradePrice(15, 7.5));  // 苹果蜂蜜饮
        TRADE_PRICES.put(ItemConstants.ITEM_KELP_FISH_SOUP,                 new TradePrice(12, 6));    // 海带鱼鲜汤
        TRADE_PRICES.put(ItemConstants.ITEM_WINTER_MELON_CRAWFISH_SOUP,     new TradePrice(20, 10));   // 冬瓜螃蟹汤

    }

    // 兼容旧方法（随机返回一个资源）
    public static String getResourceByArea(String areaType) {
        List<Item> loot = getAreaLoot(areaType);
        if (loot.isEmpty()) return null;
        return loot.get(random.nextInt(loot.size())).getName();
    }
}