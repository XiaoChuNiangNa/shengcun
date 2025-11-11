package com.example.myapplication3;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementManager {
    private static AchievementManager instance;
    private DBHelper dbHelper;
    
    // 成就分类（与数据库中的achievement_type字段保持一致）
    public static final String CATEGORY_RESOURCE = "resource_collect";
    public static final String CATEGORY_EXPLORE = "exploration";
    public static final String CATEGORY_SYNTHESIS = "synthesis";
    public static final String CATEGORY_BUILDING = "building";
    public static final String CATEGORY_COOKING = "cooking";
    public static final String CATEGORY_SMELTING = "smelting";
    public static final String CATEGORY_TRADING = "trading";
    public static final String CATEGORY_REINCARNATION = "reincarnation";
    
    private AchievementManager(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    public static synchronized AchievementManager getInstance(DBHelper dbHelper) {
        if (instance == null) {
            instance = new AchievementManager(dbHelper);
        }
        return instance;
    }
    
    // 初始化成就数据
    public void initAchievements(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // 检查是否已经初始化过
        Cursor cursor = db.query("achievements", null, "user_id=?", 
                new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return;
        }
        cursor.close();
        
        // 创建成就数据
        List<AchievementItem> achievements = createDefaultAchievements();
        
        for (AchievementItem achievement : achievements) {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("achievement_type", achievement.getCategory());
            values.put("level", achievement.getLevel());
            values.put("progress", achievement.getCurrent());
            values.put("is_claimed", achievement.isClaimed() ? 1 : 0);
            values.put("is_completed", achievement.isCompleted() ? 1 : 0);
            
            db.insert("achievements", null, values);
        }
    }
    
    // 创建默认成就数据
    private List<AchievementItem> createDefaultAchievements() {
        List<AchievementItem> achievements = new ArrayList<>();
        
        // 资源收集成就
        achievements.add(new AchievementItem("", "", "", CATEGORY_RESOURCE, "普通资源收集者", 1, 100, 10));
        achievements.add(new AchievementItem("", "", "", CATEGORY_RESOURCE, "稀有资源收集者", 2, 100, 20));
        achievements.add(new AchievementItem("", "", "", CATEGORY_RESOURCE, "史诗资源收集者", 3, 100, 30));
        achievements.add(new AchievementItem("", "", "", CATEGORY_RESOURCE, "传说资源收集者", 4, 100, 40));
        achievements.add(new AchievementItem("", "", "", CATEGORY_RESOURCE, "神话资源收集者", 5, 100, 50));
        
        // 寻宝探险成就
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "初级探险家", 1, 100, 10));
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "中级探险家", 2, 500, 20));
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "高级探险家", 3, 1000, 30));
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "资深探险家", 4, 2000, 40));
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "专家探险家", 5, 5000, 50));
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "大师探险家", 6, 10000, 60));
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "传奇探险家", 7, 20000, 70));
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "史诗探险家", 8, 50000, 80));
        achievements.add(new AchievementItem("", "", "", CATEGORY_EXPLORE, "神话探险家", 9, 100000, 90));
        
        // 合成物品成就
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "初级合成师", 1, 100, 10));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "中级合成师", 2, 500, 20));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "高级合成师", 3, 1000, 30));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "资深合成师", 4, 2000, 40));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "专家合成师", 5, 5000, 50));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "大师合成师", 6, 10000, 60));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "传奇合成师", 7, 20000, 70));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "史诗合成师", 8, 50000, 80));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SYNTHESIS, "神话合成师", 9, 100000, 90));
        
        // 解锁建筑成就
        achievements.add(new AchievementItem("", "", "", CATEGORY_BUILDING, "篝火建造者", 1, 1, 10));
        achievements.add(new AchievementItem("", "", "", CATEGORY_BUILDING, "熔炉建造者", 2, 1, 20));
        achievements.add(new AchievementItem("", "", "", CATEGORY_BUILDING, "仓库建造者", 3, 1, 30));
        achievements.add(new AchievementItem("", "", "", CATEGORY_BUILDING, "茅草屋建造者", 4, 1, 40));
        achievements.add(new AchievementItem("", "", "", CATEGORY_BUILDING, "小木屋建造者", 5, 1, 50));
        achievements.add(new AchievementItem("", "", "", CATEGORY_BUILDING, "传送门建造者", 6, 1, 60));
        achievements.add(new AchievementItem("", "", "", CATEGORY_BUILDING, "小石屋建造者", 7, 1, 70));
        achievements.add(new AchievementItem("", "", "", CATEGORY_BUILDING, "砖瓦屋建造者", 8, 1, 80));
        
        // 烹饪料理成就
        achievements.add(new AchievementItem("", "", "", CATEGORY_COOKING, "初级厨师", 1, 5, 10));
        achievements.add(new AchievementItem("", "", "", CATEGORY_COOKING, "中级厨师", 2, 10, 20));
        achievements.add(new AchievementItem("", "", "", CATEGORY_COOKING, "高级厨师", 3, 15, 30));
        achievements.add(new AchievementItem("", "", "", CATEGORY_COOKING, "资深厨师", 4, 20, 40));
        achievements.add(new AchievementItem("", "", "", CATEGORY_COOKING, "专家厨师", 5, 25, 50));
        
        // 熔炼物品成就
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "初级熔炼师", 1, 100, 10));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "中级熔炼师", 2, 500, 20));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "高级熔炼师", 3, 1000, 30));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "资深熔炼师", 4, 2000, 40));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "专家熔炼师", 5, 5000, 50));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "大师熔炼师", 6, 10000, 60));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "传奇熔炼师", 7, 20000, 70));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "史诗熔炼师", 8, 50000, 80));
        achievements.add(new AchievementItem("", "", "", CATEGORY_SMELTING, "神话熔炼师", 9, 100000, 90));
        
        // 贸易大师成就
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "初级商人", 1, 100, 10));
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "中级商人", 2, 500, 20));
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "高级商人", 3, 1000, 30));
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "资深商人", 4, 2000, 40));
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "专家商人", 5, 5000, 50));
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "大师商人", 6, 10000, 60));
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "传奇商人", 7, 20000, 70));
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "史诗商人", 8, 50000, 80));
        achievements.add(new AchievementItem("", "", "", CATEGORY_TRADING, "神话商人", 9, 100000, 90));
        
        // 轮回之路成就
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "初级轮回者", 1, 100, 10));
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "中级轮回者", 2, 500, 20));
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "高级轮回者", 3, 1000, 30));
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "资深轮回者", 4, 2000, 40));
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "专家轮回者", 5, 5000, 50));
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "大师轮回者", 6, 10000, 60));
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "传奇轮回者", 7, 20000, 70));
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "史诗轮回者", 8, 50000, 80));
        achievements.add(new AchievementItem("", "", "", CATEGORY_REINCARNATION, "神话轮回者", 9, 100000, 90));
        
        return achievements;
    }
    
    // 获取用户的所有成就
    public List<AchievementItem> getUserAchievements(int userId) {
        List<AchievementItem> achievements = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query("achievements", null, "user_id=?", 
                new String[]{String.valueOf(userId)}, null, null, "achievement_type, level");
        
        while (cursor.moveToNext()) {
            AchievementItem achievement = new AchievementItem(
                "", // id
                "", // userId
                "", // achievementType
                cursor.getString(cursor.getColumnIndexOrThrow("achievement_type")),
                "", // name (数据库中没有name字段)
                cursor.getInt(cursor.getColumnIndexOrThrow("level")),
                0, // target (动态计算)
                0  // reward (动态计算)
            );
            achievement.setCurrent(cursor.getInt(cursor.getColumnIndexOrThrow("progress")));
            achievement.setClaimed(cursor.getInt(cursor.getColumnIndexOrThrow("is_claimed")) == 1);
            achievement.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
            
            achievements.add(achievement);
        }
        cursor.close();
        
        return achievements;
    }
    
    // 更新成就进度
    public void updateAchievementProgress(int userId, String category, int newProgress) {
        // 直接调用 DBHelper 的方法，确保使用正确的字段名
        dbHelper.updateAchievementProgress(userId, category, newProgress);
    }
    
    // 领取成就奖励
    public boolean claimAchievementReward(int userId, String category, int level) {
        // 直接调用 DBHelper 的方法，避免重复代码
        return dbHelper.claimAchievementReward(userId, category, level);
    }
    
    // 增加希望点数
    private void addHopePoints(int userId, int points) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // 获取当前希望点数
        Cursor cursor = db.query("user_status", new String[]{"hope_points"}, 
                "user_id=?", new String[]{String.valueOf(userId)}, null, null, null);
        
        int currentPoints = 0;
        if (cursor.moveToFirst()) {
            currentPoints = cursor.getInt(cursor.getColumnIndexOrThrow("hope_points"));
        }
        cursor.close();
        
        // 更新希望点数
        ContentValues values = new ContentValues();
        values.put("hope_points", currentPoints + points);
        db.update("user_status", values, "user_id=?", new String[]{String.valueOf(userId)});
    }
    
    // 获取按分类分组的成就
    public Map<String, List<AchievementItem>> getAchievementsByCategory(int userId) {
        List<AchievementItem> allAchievements = getUserAchievements(userId);
        Map<String, List<AchievementItem>> achievementsByCategory = new HashMap<>();
        
        for (AchievementItem achievement : allAchievements) {
            String category = achievement.getCategory();
            if (!achievementsByCategory.containsKey(category)) {
                achievementsByCategory.put(category, new ArrayList<>());
            }
            achievementsByCategory.get(category).add(achievement);
        }
        
        return achievementsByCategory;
    }
    
    // 计算并更新成就进度（在轮回后调用）
    public void calculateAchievementProgressAfterReincarnation(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // 获取用户状态数据用于计算成就进度
        Map<String, Integer> userStats = getUserStatistics(userId);
        
        // 计算并更新每个成就分类的进度
        updateResourceCollectionProgress(userId, userStats, db);
        updateExplorationProgress(userId, userStats, db);
        updateSynthesisProgress(userId, userStats, db);
        updateBuildingProgress(userId, userStats, db);
        updateCookingProgress(userId, userStats, db);
        updateSmeltingProgress(userId, userStats, db);
        updateTradingProgress(userId, userStats, db);
        updateReincarnationProgress(userId, userStats, db);
        
        Log.d("AchievementManager", "轮回后成就进度计算完成");
    }
    
    // 获取用户统计数据
    private Map<String, Integer> getUserStatistics(int userId) {
        Map<String, Integer> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // 获取用户状态数据
        Cursor cursor = db.query("user_status", 
                new String[]{"global_collect_times", "exploration_times", "synthesis_times", "smelting_times", "trading_times", "gold", "game_day"}, 
                "user_id=?", new String[]{String.valueOf(userId)}, null, null, null);
        
        if (cursor.moveToFirst()) {
            stats.put("global_collect_times", cursor.getInt(cursor.getColumnIndexOrThrow("global_collect_times")));
            stats.put("exploration_times", cursor.getInt(cursor.getColumnIndexOrThrow("exploration_times")));
            stats.put("synthesis_times", cursor.getInt(cursor.getColumnIndexOrThrow("synthesis_times")));
            stats.put("smelting_times", cursor.getInt(cursor.getColumnIndexOrThrow("smelting_times")));
            stats.put("trading_times", cursor.getInt(cursor.getColumnIndexOrThrow("trading_times")));
            stats.put("gold", cursor.getInt(cursor.getColumnIndexOrThrow("gold")));
            stats.put("game_day", cursor.getInt(cursor.getColumnIndexOrThrow("game_day")));
        }
        cursor.close();
        
        return stats;
    }
    
    // 更新资源收集成就进度
    private void updateResourceCollectionProgress(int userId, Map<String, Integer> stats, SQLiteDatabase db) {
        int collectTimes = stats.getOrDefault("global_collect_times", 0);
        updateAchievementProgress(userId, CATEGORY_RESOURCE, collectTimes);
        Log.d("AchievementManager", "资源收集成就进度更新: " + collectTimes);
    }
    
    // 更新寻宝探险成就进度
    private void updateExplorationProgress(int userId, Map<String, Integer> stats, SQLiteDatabase db) {
        int explorationTimes = stats.getOrDefault("exploration_times", 0);
        updateAchievementProgress(userId, CATEGORY_EXPLORE, explorationTimes);
        Log.d("AchievementManager", "寻宝探险成就进度更新: " + explorationTimes);
    }
    
    // 更新合成物品成就进度
    private void updateSynthesisProgress(int userId, Map<String, Integer> stats, SQLiteDatabase db) {
        int synthesisTimes = stats.getOrDefault("synthesis_times", 0);
        updateAchievementProgress(userId, CATEGORY_SYNTHESIS, synthesisTimes);
        Log.d("AchievementManager", "合成物品成就进度更新: " + synthesisTimes);
    }
    
    // 更新解锁建筑成就进度
    private void updateBuildingProgress(int userId, Map<String, Integer> stats, SQLiteDatabase db) {
        // 检查已解锁的建筑数量
        int unlockedBuildings = getUnlockedBuildingsCount(userId);
        updateAchievementProgress(userId, CATEGORY_BUILDING, unlockedBuildings);
        Log.d("AchievementManager", "解锁建筑成就进度更新: " + unlockedBuildings);
    }
    
    // 更新烹饪料理成就进度
    private void updateCookingProgress(int userId, Map<String, Integer> stats, SQLiteDatabase db) {
        // 改为基于料理图鉴的解锁数量来计算进度
        int unlockedFoodCount = getUnlockedFoodCount(userId);
        updateAchievementProgress(userId, CATEGORY_COOKING, unlockedFoodCount);
        Log.d("AchievementManager", "烹饪料理成就进度更新: 已解锁料理数量=" + unlockedFoodCount);
    }
    
    // 获取已解锁的料理数量（从背包中统计）
    private int getUnlockedFoodCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int unlockedCount = 0;
        
        try {
            // 定义所有料理物品的常量列表
            String[] foodItems = {
                ItemConstants.ITEM_GRILLED_FISH,
                ItemConstants.ITEM_GRILLED_CRAWFISH,
                ItemConstants.ITEM_FISH_SOUP,
                ItemConstants.ITEM_MUSHROOM_SOUP,
                ItemConstants.ITEM_KELP_SOUP,
                ItemConstants.ITEM_FRUIT_PIE,
                ItemConstants.ITEM_ADVANCED_HERB,
                ItemConstants.ITEM_ICY_SNOW_LOTUS_COCONUT,
                ItemConstants.ITEM_HONEY_APPLE_SLICE,
                ItemConstants.ITEM_RICE_PORRIDGE,
                ItemConstants.ITEM_KELP_WINTER_MELON_SOUP,
                ItemConstants.ITEM_ROASTED_POTATO,
                ItemConstants.ITEM_FRUIT_SMOOTHIE,
                ItemConstants.ITEM_CRAWFISH_SHELL_SOUP,
                ItemConstants.ITEM_STEAMED_CORN,
                ItemConstants.ITEM_MUSHROOM_TRUFFLE_FRY,
                ItemConstants.ITEM_BERRY_HONEY_BREAD,
                ItemConstants.ITEM_BEET_HONEY_DRINK,
                ItemConstants.ITEM_BOILED_SPINACH,
                ItemConstants.ITEM_ROASTED_ACORN,
                ItemConstants.ITEM_CACTUS_FRUIT_ICE_DRINK,
                ItemConstants.ITEM_CARROT_POTATO_SOUP,
                ItemConstants.ITEM_COCONUT_BERRY_DRINK,
                ItemConstants.ITEM_TRUFFLE_MUSHROOM_SOUP,
                ItemConstants.ITEM_APPLE_HONEY_DRINK,
                ItemConstants.ITEM_KELP_FISH_SOUP,
                ItemConstants.ITEM_WINTER_MELON_CRAWFISH_SOUP,
                ItemConstants.ITEM_DARK_FOOD
            };
            
            // 统计背包中存在的料理数量
            for (String foodItem : foodItems) {
                Cursor cursor = db.query("backpack", 
                        new String[]{"item_count"}, 
                        "user_id = ? AND item_type = ?", 
                        new String[]{String.valueOf(userId), foodItem}, 
                        null, null, null);
                
                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow("item_count"));
                    if (count > 0) {
                        unlockedCount++;
                    }
                }
                cursor.close();
            }
            
            Log.d("AchievementManager", "用户 " + userId + " 已解锁的料理数量: " + unlockedCount);
        } catch (Exception e) {
            Log.e("AchievementManager", "获取已解锁料理数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return unlockedCount;
    }
    
    // 更新熔炼物品成就进度
    private void updateSmeltingProgress(int userId, Map<String, Integer> stats, SQLiteDatabase db) {
        int smeltingTimes = stats.getOrDefault("smelting_times", 0);
        updateAchievementProgress(userId, CATEGORY_SMELTING, smeltingTimes);
        Log.d("AchievementManager", "熔炼物品成就进度更新: " + smeltingTimes);
    }
    
    // 更新贸易大师成就进度
    private void updateTradingProgress(int userId, Map<String, Integer> stats, SQLiteDatabase db) {
        int tradingTimes = stats.getOrDefault("trading_times", 0);
        updateAchievementProgress(userId, CATEGORY_TRADING, tradingTimes);
        Log.d("AchievementManager", "贸易大师成就进度更新: " + tradingTimes);
    }
    
    // 更新轮回之路成就进度
    private void updateReincarnationProgress(int userId, Map<String, Integer> stats, SQLiteDatabase db) {
        // 这里需要根据实际的轮回次数来更新
        int reincarnationCount = getReincarnationCount(userId);
        updateAchievementProgress(userId, CATEGORY_REINCARNATION, reincarnationCount);
        Log.d("AchievementManager", "轮回之路成就进度更新: " + reincarnationCount);
    }
    
    // 获取已解锁的建筑数量（从数据库中查询实际的建筑解锁情况）
    private int getUnlockedBuildingsCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int buildingCount = 0;
        
        try {
            // 查询building表，统计该用户已解锁的不同建筑类型的数量
            Cursor cursor = db.query("building", 
                    new String[]{"COUNT(DISTINCT building_type) as unique_building_count"}, 
                    "user_id = ?", 
                    new String[]{String.valueOf(userId)}, 
                    null, null, null);
            
            if (cursor.moveToFirst()) {
                buildingCount = cursor.getInt(cursor.getColumnIndexOrThrow("unique_building_count"));
            }
            cursor.close();
            
            Log.d("AchievementManager", "用户 " + userId + " 已解锁的建筑类型数量: " + buildingCount);
        } catch (Exception e) {
            Log.e("AchievementManager", "查询建筑解锁数量失败: " + e.getMessage());
            e.printStackTrace();
            // 如果查询失败，返回0避免错误数据
            buildingCount = 0;
        }
        
        return buildingCount;
    }
    
    // 检查表中是否存在指定列
    private boolean isColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    if (columnName.equals(name)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("AchievementManager", "检查列是否存在失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    // 获取轮回次数（从数据库中获取实际的轮回次数）
    private int getReincarnationCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            // 首先检查reincarnation_times列是否存在
            if (!isColumnExists(db, "user_status", "reincarnation_times")) {
                // 如果列不存在，先创建列
                try {
                    db.execSQL("ALTER TABLE user_status ADD COLUMN reincarnation_times INTEGER DEFAULT 0");
                    Log.d("AchievementManager", "已添加reincarnation_times列");
                } catch (Exception e) {
                    Log.e("AchievementManager", "添加reincarnation_times列失败", e);
                    return 0;
                }
            }
            
            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"reincarnation_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            int currentCount = 0;
            if (cursor.moveToFirst()) {
                currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("reincarnation_times"));
            }
            cursor.close();
            
            Log.d("AchievementManager", "获取轮回次数: " + currentCount);
            return currentCount;
        } catch (Exception e) {
            Log.e("AchievementManager", "获取轮回次数失败", e);
            return 0;
        }
    }
    
    // 获取成就目标值
    private int getAchievementTarget(String achievementType, int level) {
        switch (achievementType) {
            case "resource_collect":
                return 100 * level; // Lv1:100, Lv2:200, ..., Lv5:500
            case "exploration":
            case "synthesis":
            case "smelting":
            case "trading":
            case "reincarnation":
                switch (level) {
                    case 1: return 100;
                    case 2: return 500;
                    case 3: return 1000;
                    case 4: return 2000;
                    case 5: return 5000;
                    case 6: return 10000;
                    case 7: return 20000;
                    case 8: return 50000;
                    case 9: return 100000;
                    case 10: return 200000;
                    default: return 0;
                }
            case "building":
                return level; // Lv1:1, Lv2:2, ..., Lv8:8
            case "cooking":
                return level * 5; // Lv1:5, Lv2:10, ..., Lv5:25
            default:
                return 0;
        }
    }
    
    // 获取成就奖励点数
    private int getAchievementReward(int level) {
        return level * 10; // Lv1:10, Lv2:20, ..., Lv10:100
    }
}