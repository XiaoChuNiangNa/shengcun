package com.example.myapplication3;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏地图数据类
 * 包含所有地图配置和地形数据
 */
public class GameMapData {
    
    // 地图配置
    public static final int MAP_MIN = 1;
    public static final int MAP_MAX = 21;
    
    // 主世界地图数据（中文）
    public static final String[][] MAIN_WORLD_MAP = {
            {"海洋", "草原", "草原", "草原", "河流", "河流", "树林", "树林", "树林", "树林", "树林", "树林", "河流", "河流", "海滩", "海滩", "海滩", "沙漠", "沙漠", "沙漠", "沙漠"},
            {"海洋", "草原", "草原", "草原", "河流", "草原", "树林", "树林", "树林", "树林", "树林", "树林", "河流", "河流", "海滩", "海滩", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠"},
            {"海洋", "草原", "草原", "河流", "河流", "草原", "树林", "树林", "树林", "树林", "树林", "树林", "河流", "河流", "海滩", "海滩", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠"},
            {"海洋", "草原", "草原", "河流", "草原", "沼泽", "岩石区", "岩石区", "岩石区", "村落", "树林", "树林", "河流", "河流", "海滩", "海滩", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林"},
            {"海洋", "废弃营地", "河流", "河流", "沼泽", "沼泽", "岩石区", "岩石区", "岩石区", "树林", "树林", "树林", "河流", "河流", "海滩", "海滩", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林"},
            {"海洋", "河流", "河流", "沼泽", "沼泽", "沼泽", "沼泽", "沼泽", "树林", "树林", "树林", "树林", "河流", "河流", "海滩", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林"},
            {"海洋", "河流", "河流", "沼泽", "沼泽", "沼泽", "沼泽", "沼泽", "树林", "树林", "树林", "树林", "河流", "河流", "海滩", "针叶林", "针叶林", "针叶林", "岩石区", "岩石区", "岩石区"},
            {"海洋", "河流", "河流", "沼泽", "沼泽", "沼泽", "沼泽", "沼泽", "树林", "树林", "树林", "树林", "河流", "河流", "海滩", "针叶林", "针叶林", "针叶林", "废弃营地", "岩石区", "岩石区"},
            {"海洋", "河流", "河流", "沼泽", "沼泽", "沼泽", "草原", "草原", "树林", "树林", "树林", "树林", "河流", "河流", "针叶林", "针叶林", "针叶林", "针叶林", "岩石区", "岩石区", "岩石区"},
            {"海洋", "河流", "河流", "草原", "草原", "草原", "草原", "草原", "草原", "树林", "树林", "树林", "河流", "河流", "针叶林", "针叶林", "针叶林", "针叶林", "岩石区", "岩石区", "岩石区"},
            {"海洋", "河流", "河流", "草原", "村落", "草原", "草原", "草原", "草原", "树林", "树林", "树林", "河流", "河流", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林"},
            {"海洋", "河流", "河流", "草原", "草原", "草原", "草原", "草原", "草原", "树林", "树林", "树林", "河流", "河流", "河流", "河流", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林"},
            {"海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海滩", "海滩", "海滩", "海滩", "海滩", "海滩", "河流", "河流", "河流", "河流", "针叶林", "针叶林", "村落", "针叶林", "针叶林"},
            {"海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海滩", "海滩", "海滩", "河流", "河流", "针叶林", "针叶林", "针叶林", "针叶林", "针叶林"},
            {"海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋"},
            {"海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋"},
            {"海滩", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "深海", "深海", "深海", "深海", "深海", "深海", "海洋", "海洋", "雪原", "雪原", "雪原", "雪原", "雪原"},
            {"海滩", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋", "深海", "深海", "深海", "深海", "深海", "深海", "海洋", "雪原", "雪原", "雪原", "雪山", "雪山", "雪山"},
            {"海滩", "海滩", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "深海", "深海", "深海", "深海", "深海", "深海", "海洋", "雪原", "雪原", "岩石区", "雪山", "雪山", "雪山"},
            {"树林", "废弃营地", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "深海", "深海", "深海", "深海", "深海", "深海", "海洋", "雪原", "废弃营地", "岩石区", "雪山", "雪山", "雪山"},
            {"树林", "树林", "海滩", "海滩", "海滩", "海洋", "海洋", "海洋", "深海", "深海", "深海", "深海", "深海", "深海", "海洋", "雪原", "岩石区", "岩石区", "雪山", "雪山", "雪山"}
    };

    // 奇幻大陆地图数据（已启用）
    public static final String[][] FANTASY_CONTINENT_MAP = {
        {"传送门", "树林", "树林", "树林", "树林", "树林", "树林", "河流", "河流", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "雪山", "雪山", "雪山", "雪山", "雪山"},
        {"树林", "树林", "树林", "树林", "树林", "树林", "树林", "河流", "河流", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "雪山", "雪山", "雪山"},
        {"岩石区", "树林", "树林", "树林", "树林", "树林", "河流", "河流", "河流", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "雪山", "雪山"},
        {"岩石区", "岩石区", "岩石区", "废弃营地", "树林", "树林", "河流", "河流", "雪原", "雪原", "雪原", "雪原", "雪原", "村落", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "雪山", "雪山"},
        {"岩石区", "岩石区", "岩石区", "岩石区", "河流", "河流", "河流", "河流", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区"},
        {"岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "河流", "河流", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区"},
        {"岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "河流", "河流", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区"},
        {"岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "河流", "河流", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区"},
        {"岩石区", "岩石区", "岩石区", "岩石区", "岩石区", "河流", "河流", "河流", "海洋", "海洋", "海洋", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区", "岩石区"},
        {"岩石区", "岩石区", "岩石区", "岩石区", "河流", "河流", "河流", "海洋", "海洋", "海洋", "海洋", "海洋", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区", "岩石区"},
        {"岩石区", "岩石区", "河流", "河流", "河流", "河流", "河流", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区", "岩石区"},
        {"岩石区", "岩石区", "河流", "河流", "河流", "河流", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区", "岩石区"},
        {"岩石区", "岩石区", "河流", "河流", "河流", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "雪原", "雪原", "雪原", "雪原", "雪原", "岩石区"},
        {"岩石区", "岩石区", "河流", "河流", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "雪原", "雪原", "雪原", "雪原", "雪原"},
        {"岩石区", "河流", "河流", "河流", "海洋", "海洋", "海洋", "海洋", "海滩", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "雪原", "雪原", "雪原", "雪原"},
        {"岩石区", "河流", "河流", "河流", "海洋", "海洋", "海洋", "海滩", "海滩", "沙漠", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "雪原", "雪原", "雪原"},
        {"岩石区", "河流", "河流", "海洋", "海洋", "海洋", "海洋", "海滩", "沙漠", "沙漠", "沙漠", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "雪原", "雪原"},
        {"河流", "河流", "河流", "海洋", "海洋", "海洋", "海滩", "海滩", "沙漠", "沙漠", "沙漠", "沙漠", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋"},
        {"河流", "河流", "河流", "海洋", "海洋", "海滩", "海滩", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋", "海洋"},
        {"河流", "河流", "河流", "海洋", "海滩", "海滩", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋"},
        {"河流", "河流", "河流", "海洋", "海滩", "海滩", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "沙漠", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋"},
        {"河流", "河流", "河流", "海洋", "海滩", "海滩", "沙漠", "沙漠", "沙漠", "神殿", "沙漠", "沙漠", "沙漠", "沙漠", "海滩", "海滩", "海洋", "海洋", "海洋", "海洋", "海洋"}
    };
    
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
    public static final String TEMPLE = "temple"; // 神殿
    
//    // 新地形常量（奇幻大陆）
//    public static final String FANTASY_ENTRANCE = "fantasy_entrance"; // 奇幻入口
//    public static final String MAGIC_FOREST = "magic_forest"; // 魔法森林
//    public static final String ELF_VILLAGE = "elf_village"; // 精灵村落
//    public static final String CRYSTAL_VEIN = "crystal_vein"; // 水晶矿脉
//    public static final String MAGIC_ACADEMY = "magic_academy"; // 魔法学院
//    public static final String DRAGON_LAIR = "dragon_lair"; // 龙之巢穴
//    public static final String GIANT_MOUNTAIN = "giant_mountain"; // 巨人山脉
//    public static final String FANTASY_CASTLE = "fantasy_castle"; // 奇幻城堡
//    public static final String ROYAL_GARDEN = "royal_garden"; // 皇家花园
//    public static final String MAGIC_VAULT = "magic_vault"; // 魔法宝库
//    public static final String FANTASY_EXIT = "fantasy_exit"; // 奇幻出口
//    public static final String FANTASY_TEMPLE = "fantasy_temple"; // 奇幻神殿
//

    
    // 中文地形 → 英文地形映射（主世界）
    private static final Map<String, String> CHINESE_TO_ENGLISH_TERRAIN = new HashMap<>();
    
//    // 中文地形 → 英文地形映射（地下世界）
//    private static final Map<String, String> UNDERGROUND_CHINESE_TO_ENGLISH = new HashMap<>();
//
//    // 中文地形 → 英文地形映射（天空世界）
//    private static final Map<String, String> SKY_CHINESE_TO_ENGLISH = new HashMap<>();
//
//    // 中文地形 → 英文地形映射（奇幻大陆）
//    private static final Map<String, String> FANTASY_CHINESE_TO_ENGLISH = new HashMap<>();
//
    
    static {
        // 主世界地形映射
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
        CHINESE_TO_ENGLISH_TERRAIN.put("神殿", TEMPLE);
        
//        // 地下世界地形映射
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下入口", UNDERGROUND_ENTRANCE);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下通道", UNDERGROUND_PASSAGE);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下洞穴", UNDERGROUND_CAVE);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下矿脉", UNDERGROUND_VEIN);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下宝藏", UNDERGROUND_TREASURE);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下神殿", UNDERGROUND_TEMPLE);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下迷宫", UNDERGROUND_MAZE);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下核心", UNDERGROUND_CORE);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下祭坛", UNDERGROUND_ALTAR);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下宝库", UNDERGROUND_VAULT);
//        UNDERGROUND_CHINESE_TO_ENGLISH.put("地下出口", UNDERGROUND_EXIT);
//
//        // 天空世界地形映射
//        SKY_CHINESE_TO_ENGLISH.put("天空入口", SKY_ENTRANCE);
//        SKY_CHINESE_TO_ENGLISH.put("天空云层", SKY_CLOUD);
//        SKY_CHINESE_TO_ENGLISH.put("天空浮岛", SKY_ISLAND);
//        SKY_CHINESE_TO_ENGLISH.put("天空花园", SKY_GARDEN);
//        SKY_CHINESE_TO_ENGLISH.put("天空神殿", SKY_TEMPLE);
//        SKY_CHINESE_TO_ENGLISH.put("天空宫殿", SKY_PALACE);
//        SKY_CHINESE_TO_ENGLISH.put("天空庭院", SKY_COURTYARD);
//        SKY_CHINESE_TO_ENGLISH.put("天空核心", SKY_CORE);
//        SKY_CHINESE_TO_ENGLISH.put("天空祭坛", SKY_ALTAR);
//        SKY_CHINESE_TO_ENGLISH.put("天空宝库", SKY_VAULT);
//        SKY_CHINESE_TO_ENGLISH.put("天空出口", SKY_EXIT);
//
//        // 奇幻大陆地形映射
//        FANTASY_CHINESE_TO_ENGLISH.put("奇幻入口", FANTASY_ENTRANCE);
//        FANTASY_CHINESE_TO_ENGLISH.put("魔法森林", MAGIC_FOREST);
//        FANTASY_CHINESE_TO_ENGLISH.put("精灵村落", ELF_VILLAGE);
//        FANTASY_CHINESE_TO_ENGLISH.put("水晶矿脉", CRYSTAL_VEIN);
//        FANTASY_CHINESE_TO_ENGLISH.put("魔法学院", MAGIC_ACADEMY);
//        FANTASY_CHINESE_TO_ENGLISH.put("龙之巢穴", DRAGON_LAIR);
//        FANTASY_CHINESE_TO_ENGLISH.put("巨人山脉", GIANT_MOUNTAIN);
//        FANTASY_CHINESE_TO_ENGLISH.put("奇幻城堡", FANTASY_CASTLE);
//        FANTASY_CHINESE_TO_ENGLISH.put("皇家花园", ROYAL_GARDEN);
//        FANTASY_CHINESE_TO_ENGLISH.put("魔法宝库", MAGIC_VAULT);
//        FANTASY_CHINESE_TO_ENGLISH.put("奇幻出口", FANTASY_EXIT);

    }
    
    // 获取主世界区域英文类型
    public static String getMainWorldAreaTypeByCoord(int x, int y) {
        if (x < MAP_MIN || x > MAP_MAX || y < MAP_MIN || y > MAP_MAX) {
            return "未知区域（坐标错误）";
        }
        String chineseTerrain = MAIN_WORLD_MAP[y - 1][x - 1];
        return CHINESE_TO_ENGLISH_TERRAIN.getOrDefault(chineseTerrain, chineseTerrain);
    }
    
    // 获取主世界区域中文类型
    public static String getMainWorldAreaChineseTypeByCoord(int x, int y) {
        if (x < MAP_MIN || x > MAP_MAX || y < MAP_MIN || y > MAP_MAX) {
            return "未知区域";
        }
        return MAIN_WORLD_MAP[y - 1][x - 1];
    }
    
//    // 获取地下世界区域英文类型（暂时不引用）
//    public static String getUndergroundWorldAreaTypeByCoord(int x, int y) {
//        if (x < MAP_MIN || x > MAP_MAX || y < MAP_MIN || y > MAP_MAX) {
//            return "未知区域（坐标错误）";
//        }
//        String chineseTerrain = UNDERGROUND_WORLD_MAP[y - 1][x - 1];
//        return UNDERGROUND_CHINESE_TO_ENGLISH.getOrDefault(chineseTerrain, chineseTerrain);
//    }
//
//    // 获取天空世界区域英文类型（暂时不引用）
//    public static String getSkyWorldAreaTypeByCoord(int x, int y) {
//        if (x < MAP_MIN || x > MAP_MAX || y < MAP_MIN || y > MAP_MAX) {
//            return "未知区域（坐标错误）";
//        }
//        String chineseTerrain = SKY_WORLD_MAP[y - 1][x - 1];
//        return SKY_CHINESE_TO_ENGLISH.getOrDefault(chineseTerrain, chineseTerrain);
//    }
//
//    // 获取奇幻大陆区域英文类型（暂时不引用）
//    public static String getFantasyContinentAreaTypeByCoord(int x, int y) {
//        if (x < MAP_MIN || x > MAP_MAX || y < MAP_MIN || y > MAP_MAX) {
//            return "未知区域（坐标错误）";
//        }
//        String chineseTerrain = FANTASY_CONTINENT_MAP[y - 1][x - 1];
//        return FANTASY_CHINESE_TO_ENGLISH.getOrDefault(chineseTerrain, chineseTerrain);
//    }
}