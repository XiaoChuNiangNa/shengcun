package com.example.myapplication3;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private final MainActivity activity;
    private final DBHelper dbHelper;

    public DataManager(MainActivity activity) {
        this.activity = activity;
        this.dbHelper = DBHelper.getInstance(activity);
    }

    // 新增：提供dbHelper的getter方法，供外部访问
    public DBHelper getDbHelper() {
        return dbHelper;
    }

    // 加载游戏数据
    public void loadGameData() {
        new LoadGameTask(activity).execute();
    }

    // 安全的异步任务类
    private static class LoadGameTask extends AsyncTask<Void, Void, Map<String, Object>> {
        private WeakReference<MainActivity> activityRef;
        private DBHelper dbHelper;

        LoadGameTask(MainActivity activity) {
            this.activityRef = new WeakReference<>(activity);
            this.dbHelper = DBHelper.getInstance(activity);
        }

        @Override
        protected Map<String, Object> doInBackground(Void... voids) {
            return dbHelper.getUserStatus(MyApplication.currentUserId);
        }

        @Override
        protected void onPostExecute(Map<String, Object> userStatus) {
            MainActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return;

            if (userStatus == null || userStatus.isEmpty()) {
                initDefaultGameStatus(activity);
                activity.uiUpdater.updateAreaInfo();
                return;
            }

            // 加载坐标
            int currentXFromDB = getIntValue(userStatus.get("current_x"), 0);
            int currentYFromDB = getIntValue(userStatus.get("current_y"), 0);
            boolean isCoordValid = activity.gameMap.isValidCoord(currentXFromDB, currentYFromDB);

            if (!isCoordValid) {
                int[] spawnPoint = chooseRandomSpawnPoint(activity);
                activity.currentX = spawnPoint[0];
                activity.currentY = spawnPoint[1];
                activity.gameHour = Constant.GAME_HOUR_DEFAULT;
                activity.gameDay = Constant.GAME_DAY_DEFAULT;
                activity.temperature = Constant.TEMPERATURE_DEFAULT;

                saveInitialDataToDB(activity);
            } else {
                activity.currentX = currentXFromDB;
                activity.currentY = currentYFromDB;
            }

            // 加载状态数据
            int lifeFromDB = getIntValue(userStatus.get("life"), Constant.INIT_LIFE);
            
            // 关键修复：只在游戏已开始且生命值<=0时重置，避免新玩家被误判为死亡
            GameStateManager gameStateManager = GameStateManager.getInstance(activity);
            boolean isGameStarted = gameStateManager.isGameStarted();
            
            // 添加详细的生命值检查日志
            Log.d("DataManager", "详细检查生命值:");
            Log.d("DataManager", "  - 从数据库读取的生命值: " + lifeFromDB);
            Log.d("DataManager", "  - 游戏开始状态: " + isGameStarted);
            Log.d("DataManager", "  - 生命值<=0: " + (lifeFromDB <= 0));
            Log.d("DataManager", "  - 综合条件 (isGameStarted && lifeFromDB<=0): " + (isGameStarted && lifeFromDB <= 0));
            
            if (isGameStarted && lifeFromDB <= 0) {
                // 游戏已开始且生命值为0，才是真正的死亡状态
                Log.i("DataManager", "检测到死亡状态，自动重置游戏数据");
                // 直接调用重置方法，确保所有状态都正确重置
                DataManager dataManager = new DataManager(activity);
                dataManager.resetGameData(MyApplication.currentUserId);
                
                // 同时重置游戏状态管理器，确保游戏状态一致
                gameStateManager.resetGame();
                Log.i("DataManager", "游戏状态管理器已重置");
            } else {
                // 新玩家或者生命值正常，设置生命值并考虑等级加成
                LevelExperienceManager levelManager = LevelExperienceManager.getInstance(activity);
                int lifeBonus = levelManager.getTotalHpBonus();
                int maxLife = 100 + lifeBonus; // 基础100 + 等级加成
                
                // 确保当前生命值不超过新的最大值
                activity.life = Math.min(lifeFromDB, maxLife);
                Log.i("DataManager", "设置生命值: " + activity.life + " (最大值: " + maxLife + ", 加成: " + lifeBonus + ")");
                
                // 检查设置后的状态
                Log.d("DataManager", "设置后活动中的生命值: " + activity.life);
            }
            
            activity.hunger = getIntValue(userStatus.get("hunger"), Constant.INIT_HUNGER);
            activity.thirst = getIntValue(userStatus.get("thirst"), Constant.INIT_THIRST);
            activity.stamina = getIntValue(userStatus.get("stamina"), Constant.INIT_STAMINA);
            activity.gold = getIntValue(userStatus.get("gold"), 0);
            activity.backpackCap = getIntValue(userStatus.get("backpack_cap"), 10);
            // 加载难度设置，如果不存在则使用默认值
            activity.difficulty = (String) userStatus.getOrDefault("difficulty", Constant.DIFFICULTY_NORMAL);
            
            // 添加日志显示当前游戏难度
            Log.d("GameLoading", "从数据库加载的难度: " + activity.difficulty);
            Log.d("GameLoading", "当前游戏难度: " + activity.difficulty);
            activity.firstCollectTime = getLongValue(userStatus.get("first_collect_time"), 0);

            // 加载时间和温度
            activity.gameHour = getIntValue(userStatus.get("game_hour"), Constant.GAME_HOUR_DEFAULT);
            activity.gameDay = getIntValue(userStatus.get("game_day"), Constant.GAME_DAY_DEFAULT);
            activity.temperature = getIntValue(userStatus.get("temperature"), Constant.TEMPERATURE_DEFAULT);

            // 加载采集数据
            activity.currentCollectTimes = getIntValue(userStatus.get("global_collect_times"), 0);
            activity.lastRefreshDay = getIntValue(userStatus.get("last_refresh_day"), 1);

            // 刷新UI
            activity.uiUpdater.updateAreaInfo();
            activity.uiUpdater.updateStatusDisplays();
            activity.uiUpdater.updateTimeDisplay();
            activity.uiUpdater.updateTemperatureDisplay();
            activity.backgroundView.setCurrentCoord(activity.currentX, activity.currentY);

            // 加载装备
            loadCurrentEquip(activity);
        }

        private int getIntValue(Object obj, int defaultValue) {
            if (obj == null) return defaultValue;
            if (obj instanceof Integer) return (Integer) obj;
            if (obj instanceof String) {
                try { return Integer.parseInt((String) obj); }
                catch (NumberFormatException e) { return defaultValue; }
            }
            return defaultValue;
        }

        private long getLongValue(Object obj, long defaultValue) {
            if (obj instanceof Number) return ((Number) obj).longValue();
            return defaultValue;
        }

        private void initDefaultGameStatus(MainActivity activity) {
            int[] spawnPoint = chooseRandomSpawnPoint(activity);
            activity.currentX = spawnPoint[0];
            activity.currentY = spawnPoint[1];

            activity.life = Constant.INIT_LIFE;
            activity.hunger = Constant.INIT_HUNGER;
            activity.thirst = Constant.INIT_THIRST;
            activity.stamina = Constant.INIT_STAMINA;
            activity.gold = 0;
            activity.backpackCap = 10;
            
            // 从SharedPreferences读取正确的难度设置
            android.content.SharedPreferences sp = activity.getSharedPreferences("game_settings", android.content.Context.MODE_PRIVATE);
            String difficultyFromPrefs = sp.getString("difficulty", Constant.DIFFICULTY_NORMAL);
            
            // 添加日志用于调试难度设置
            Log.d("GameLoading", "从SharedPreferences读取的难度: " + difficultyFromPrefs);
            
            // 直接使用从SharedPreferences读取的值，确保与TitleActivity中保存的值一致
            activity.difficulty = difficultyFromPrefs;
            Log.d("GameLoading", "初始化后设置的难度: " + activity.difficulty);
            
            activity.firstCollectTime = 0;
            activity.currentEquip = "";

            activity.gameHour = Constant.GAME_HOUR_DEFAULT;
            activity.gameDay = Constant.GAME_DAY_DEFAULT;
            activity.temperature = Constant.TEMPERATURE_DEFAULT;

            // 保存默认数据
            Map<String, Object> defaultStatus = new HashMap<>();
            defaultStatus.put("current_x", activity.currentX);
            defaultStatus.put("current_y", activity.currentY);
            defaultStatus.put("life", activity.life);
            defaultStatus.put("hunger", activity.hunger);
            defaultStatus.put("thirst", activity.thirst);
            defaultStatus.put("stamina", activity.stamina);
            defaultStatus.put("gold", activity.gold);
            defaultStatus.put("backpack_cap", activity.backpackCap);
            defaultStatus.put("difficulty", activity.difficulty);
            defaultStatus.put("first_collect_time", activity.firstCollectTime);
            defaultStatus.put("game_hour", activity.gameHour);
            defaultStatus.put("game_day", activity.gameDay);
            defaultStatus.put("temperature", activity.temperature);
            dbHelper.updateUserStatus(MyApplication.currentUserId, defaultStatus);
        }

        private void saveInitialDataToDB(MainActivity activity) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    Map<String, Object> coordUpdate = new HashMap<>();
                    coordUpdate.put("current_x", activity.currentX);
                    coordUpdate.put("current_y", activity.currentY);
                    coordUpdate.put("game_hour", activity.gameHour);
                    coordUpdate.put("game_day", activity.gameDay);
                    coordUpdate.put("temperature", activity.temperature);
                    dbHelper.updateUserStatus(MyApplication.currentUserId, coordUpdate);
                    return null;
                }
            }.execute();
        }

        private int[] chooseRandomSpawnPoint(MainActivity activity) {
            return activity.gameMap.chooseRandomSpawnPoint();
        }

        private void loadCurrentEquip(MainActivity activity) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    return dbHelper.getCurrentEquip(MyApplication.currentUserId);
                }

                @Override
                protected void onPostExecute(String equip) {
                    if (activity != null && !activity.isFinishing()) {
                        activity.currentEquip = equip;
                        activity.uiUpdater.refreshEquipStatus();
                    }
                }
            }.execute();
        }
    }

    // 初始化默认状态
    private void initDefaultGameStatus() {
        int[] spawnPoint = chooseRandomSpawnPoint();
        activity.currentX = spawnPoint[0];
        activity.currentY = spawnPoint[1];

        activity.life = Constant.INIT_LIFE;
        activity.hunger = Constant.INIT_HUNGER;
        activity.thirst = Constant.INIT_THIRST;
        activity.stamina = Constant.INIT_STAMINA;
        activity.gold = 0;
        activity.backpackCap = 10;
        
        // 从SharedPreferences读取正确的难度设置
        android.content.SharedPreferences sp = activity.getSharedPreferences("game_settings", android.content.Context.MODE_PRIVATE);
        String difficultyFromPrefs = sp.getString("difficulty", Constant.DIFFICULTY_NORMAL);
        
        // 添加日志用于调试难度设置
        Log.d("GameLoading", "从SharedPreferences读取的难度: " + difficultyFromPrefs);
        
        // 直接使用从SharedPreferences读取的值，确保与TitleActivity中保存的值一致
        activity.difficulty = difficultyFromPrefs;
        Log.d("GameLoading", "初始化后设置的难度: " + activity.difficulty);
        
        activity.firstCollectTime = 0;
        activity.currentEquip = "";

        activity.gameHour = Constant.GAME_HOUR_DEFAULT;
        activity.gameDay = Constant.GAME_DAY_DEFAULT;
        activity.temperature = Constant.TEMPERATURE_DEFAULT;

        // 保存默认数据
        Map<String, Object> defaultStatus = new HashMap<>();
        defaultStatus.put("current_x", activity.currentX);
        defaultStatus.put("current_y", activity.currentY);
        defaultStatus.put("life", activity.life);
        defaultStatus.put("hunger", activity.hunger);
        defaultStatus.put("thirst", activity.thirst);
        defaultStatus.put("stamina", activity.stamina);
        defaultStatus.put("gold", activity.gold);
        defaultStatus.put("backpack_cap", activity.backpackCap);
        defaultStatus.put("difficulty", activity.difficulty);
        defaultStatus.put("first_collect_time", activity.firstCollectTime);
        defaultStatus.put("game_hour", activity.gameHour);
        defaultStatus.put("game_day", activity.gameDay);
        defaultStatus.put("temperature", activity.temperature);
        dbHelper.updateUserStatus(MyApplication.currentUserId, defaultStatus);
    }

    // 保存初始坐标数据
    private void saveInitialDataToDB() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Map<String, Object> coordUpdate = new HashMap<>();
                coordUpdate.put("current_x", activity.currentX);
                coordUpdate.put("current_y", activity.currentY);
                coordUpdate.put("game_hour", activity.gameHour);
                coordUpdate.put("game_day", activity.gameDay);
                coordUpdate.put("temperature", activity.temperature);
                dbHelper.updateUserStatus(MyApplication.currentUserId, coordUpdate);
                return null;
            }
        }.execute();
    }

    // 加载当前装备
    private void loadCurrentEquip() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return dbHelper.getCurrentEquip(MyApplication.currentUserId);
            }

            @Override
            protected void onPostExecute(String equip) {
                activity.currentEquip = equip;
                activity.uiUpdater.refreshEquipStatus();
            }
        }.execute();
    }

    // 保存坐标到数据库
    public void saveCoordToDB(int x, int y) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Map<String, Object> update = new HashMap<>();
                update.put("current_x", x);
                update.put("current_y", y);
                dbHelper.updateUserStatus(MyApplication.currentUserId, update);
                return null;
            }
        }.execute();
    }

    // 保存所有关键数据
    public void saveAllCriticalData() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("life", activity.life);
        updateData.put("hunger", activity.hunger);
        updateData.put("thirst", activity.thirst);
        updateData.put("stamina", activity.stamina);
        updateData.put("current_x", activity.currentX);
        updateData.put("current_y", activity.currentY);
        updateData.put("game_hour", activity.gameHour);
        updateData.put("game_day", activity.gameDay);
        updateData.put("temperature", activity.temperature);
        updateData.put("difficulty", activity.difficulty);
        
        // 添加日志记录保存的难度值
        Log.d("GameSaving", "保存的难度常量: " + activity.difficulty);

        new Thread(() -> dbHelper.updateUserStatus(MyApplication.currentUserId, updateData)).start();
    }

    // 随机选择复活点
    private int[] chooseRandomSpawnPoint() {
        return activity.gameMap.chooseRandomSpawnPoint();
    }

    // 工具方法：安全获取int
    public int getIntValue(Object obj, int defaultValue) {
        if (obj == null) return defaultValue;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof String) {
            try { return Integer.parseInt((String) obj); }
            catch (NumberFormatException e) { return defaultValue; }
        }
        return defaultValue;
    }

    // 工具方法：安全获取long
    public long getLongValue(Object obj, long defaultValue) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        return defaultValue;
    }

    /**
     * 更新当前坐标（用于读档后同步坐标）
     */
    public void updateCurrentCoord(int newX, int newY) {
        if (activity != null) {
            activity.currentX = newX;
            activity.currentY = newY;
            
            // 立即更新数据库中的坐标
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("current_x", newX);
            updateData.put("current_y", newY);
            dbHelper.updateUserStatus(MyApplication.currentUserId, updateData);
            
            Log.d("DataManager", "坐标已更新: (" + newX + ", " + newY + ")");
        }
    }

    /**
     * 重置游戏数据（死亡后重置）
     * 只重置生存状态、背包物品等，但保留建筑
     */
    public void resetGameData(int userId) {
        try {
            // 从SharedPreferences读取难度设置
            android.content.SharedPreferences sp = activity.getSharedPreferences("game_settings", android.content.Context.MODE_PRIVATE);
            String difficultyFromPrefs = sp.getString("difficulty", Constant.DIFFICULTY_NORMAL);
            
            // 调用接受难度参数的resetUserData方法
            dbHelper.resetUserData(userId, difficultyFromPrefs);
            
            // 更新当前活动中的状态变量，确保立即生效
            activity.life = Constant.INIT_LIFE;
            activity.hunger = Constant.INIT_HUNGER;
            activity.thirst = Constant.INIT_THIRST;
            activity.stamina = Constant.INIT_STAMINA;
            
            // 设置默认出生点
            int[] spawnPoint = activity.gameMap.chooseRandomSpawnPoint();
            activity.currentX = spawnPoint[0];
            activity.currentY = spawnPoint[1];
            activity.gameHour = Constant.GAME_HOUR_DEFAULT;
            activity.gameDay = Constant.GAME_DAY_DEFAULT;
            activity.currentCollectTimes = 0;
            activity.lastRefreshDay = 0;
            
            Log.i("DataManager", "游戏数据重置成功（保留建筑），难度: " + difficultyFromPrefs);
        } catch (Exception e) {
            Log.e("DataManager", "重置游戏数据失败", e);
        }
    }

    /**
     * 初始化新用户数据（新玩家第一次开始游戏时调用）
     */
    public void initializeNewUserData(int userId) {
        try {
            // 1. 确保用户数据存在，如果不存在则创建
            // 从SharedPreferences读取难度设置
              android.content.SharedPreferences sp = activity.getSharedPreferences("game_settings", android.content.Context.MODE_PRIVATE);
              String difficultyFromPrefs = sp.getString("difficulty", Constant.DIFFICULTY_NORMAL);
              
              Log.d("DataManager", "初始化新用户数据，从SharedPreferences读取的难度: " + difficultyFromPrefs + ", 用户ID: " + userId);
              
              if (!dbHelper.hasUserData(userId)) {
                 // 调用接受难度参数的initUserData方法，确保使用用户选择的难度
                 dbHelper.initUserData(userId, difficultyFromPrefs);
                 Log.d("DataManager", "调用initUserData方法，使用难度: " + difficultyFromPrefs);
              } else {
                 // 如果用户数据已存在，更新难度设置以确保与SharedPreferences一致
                 Map<String, Object> difficultyUpdate = new HashMap<>();
                 difficultyUpdate.put("difficulty", difficultyFromPrefs);
                 dbHelper.updateUserStatus(userId, difficultyUpdate);
                 Log.d("DataManager", "用户数据已存在，更新难度为: " + difficultyFromPrefs);
              }
            
            // 2. 初始化用户状态为默认值（考虑等级加成）
            Map<String, Object> defaultStatus = new HashMap<>();
            LevelExperienceManager levelManager = LevelExperienceManager.getInstance(activity);
            int lifeBonus = levelManager.getTotalHpBonus();
            int initLifeWithBonus = Constant.INIT_LIFE + lifeBonus;
            defaultStatus.put("life", initLifeWithBonus);
            defaultStatus.put("hunger", Constant.INIT_HUNGER);
            defaultStatus.put("thirst", Constant.INIT_THIRST);
            defaultStatus.put("stamina", Constant.INIT_STAMINA);
            defaultStatus.put("gold", 0);
            defaultStatus.put("backpack_cap", 10);
            // 使用从SharedPreferences读取的难度设置
            Log.d("GameInit", "初始化新用户数据，从SharedPreferences读取的难度: " + difficultyFromPrefs + ", 用户ID: " + userId);
            defaultStatus.put("difficulty", difficultyFromPrefs);
            defaultStatus.put("first_collect_time", 0);
            defaultStatus.put("game_hour", Constant.GAME_HOUR_DEFAULT);
            defaultStatus.put("game_day", Constant.GAME_DAY_DEFAULT);
            defaultStatus.put("temperature", Constant.TEMPERATURE_DEFAULT);
            
            // 3. 初始化操作次数记录
            defaultStatus.put("global_collect_times", 0);
            defaultStatus.put("exploration_times", 0);
            defaultStatus.put("synthesis_times", 0);
            defaultStatus.put("last_refresh_day", 0);
            
            // 4. 设置默认出生点
            int[] spawnPoint = activity.gameMap.chooseRandomSpawnPoint();
            defaultStatus.put("current_x", spawnPoint[0]);
            defaultStatus.put("current_y", spawnPoint[1]);
            
            dbHelper.updateUserStatus(userId, defaultStatus);
            
            // 5. 更新当前活动中的状态变量，确保立即生效
            activity.life = Constant.INIT_LIFE;
            activity.hunger = Constant.INIT_HUNGER;
            activity.thirst = Constant.INIT_THIRST;
            activity.stamina = Constant.INIT_STAMINA;
            activity.currentX = spawnPoint[0];
            activity.currentY = spawnPoint[1];
            activity.gameHour = Constant.GAME_HOUR_DEFAULT;
            activity.gameDay = Constant.GAME_DAY_DEFAULT;
            activity.currentCollectTimes = 0;
            activity.lastRefreshDay = 0;
            
            Log.i("DataManager", "新用户数据初始化成功，难度: " + difficultyFromPrefs);
        } catch (Exception e) {
            Log.e("DataManager", "新用户数据初始化失败", e);
        }
    }
}