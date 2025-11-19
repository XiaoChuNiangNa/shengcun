package com.example.myapplication3;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 等级和经验管理系统
 * 外置等级系统：等级轮回、游戏失败等不重置，保存和读档时也不会记录经验和等级
 */
public class LevelExperienceManager {
    
    private static LevelExperienceManager instance;
    private Context context;
    private SharedPreferences sharedPreferences;
    private Random random;

    public static synchronized LevelExperienceManager getInstance(Context context) {
        if (instance == null) {
            instance = new LevelExperienceManager(context.getApplicationContext());
        }
        return instance;
    }
    
    // 等级和经验常量
    private static final int MAX_LEVEL = 100;
    private static final int BASE_EXP_REQUIRED = 500; // 升级到2级需要500经验
    private static final int EXP_INCREMENT_PER_LEVEL = 500; // 每级增加500经验需求
    
    public LevelExperienceManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("level_exp", Context.MODE_PRIVATE);
        this.random = new Random();
        
        // 初始化外置等级系统（如果不存在）
        if (!sharedPreferences.contains("current_level")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("current_level", 1);
            editor.putInt("current_exp", 0);
            editor.putInt("total_hp_bonus", 0);
            editor.apply();
        }
    }
    
    /**
     * 获取当前等级（外置系统，不依赖用户ID）
     */
    public int getCurrentLevel() {
        return sharedPreferences.getInt("current_level", 1);
    }
    
    /**
     * 获取当前经验值（外置系统，不依赖用户ID）
     */
    public int getCurrentExp() {
        return sharedPreferences.getInt("current_exp", 0);
    }
    
    /**
     * 获取总生命值加成
     */
    public int getTotalHpBonus() {
        return sharedPreferences.getInt("total_hp_bonus", 0);
    }
    
    /**
     * 获取升级到下一级所需经验
     */
    public int getExpRequiredForNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) {
            return 0; // 满级后不需要经验
        }
        
        // 计算升级所需经验
        // 1级升2级: 500
        // 2级升3级: 1000  
        // 3级升4级: 1500
        // 以此类推: BASE_EXP_REQUIRED + (currentLevel - 1) * EXP_INCREMENT_PER_LEVEL
        return BASE_EXP_REQUIRED + (currentLevel - 1) * EXP_INCREMENT_PER_LEVEL;
    }
    
    /**
     * 添加经验值（外置系统，不依赖用户ID）
     */
    public void addExp(int expAmount) {
        if (expAmount <= 0) return;
        
        int currentLevel = getCurrentLevel();
        int currentExp = getCurrentExp();
        
        // 如果已经满级，不增加经验
        if (currentLevel >= MAX_LEVEL) {
            Toast.makeText(context, "已达到满级，无法获得更多经验", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 增加经验值
        int newExp = currentExp + expAmount;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("current_exp", newExp);
        editor.putInt("last_exp_gained", expAmount);
        editor.apply();
        
        // 检查是否升级
        checkLevelUp();
    }

    /**
     * 添加经验值（别名方法，与QuestManager调用保持一致）
     */
    public void addExperience(int experienceAmount) {
        addExp(experienceAmount);
    }
    
    /**
     * 检查是否升级（外置系统）
     */
    private void checkLevelUp() {
        int currentLevel = getCurrentLevel();
        int currentExp = getCurrentExp();
        
        // 如果已经满级，不检查升级
        if (currentLevel >= MAX_LEVEL) return;
        
        // 计算升级所需经验
        int expRequired = getExpRequiredForNextLevel(currentLevel);
        
        // 检查是否达到升级条件
        if (currentExp >= expRequired) {
            // 升级
            int newLevel = currentLevel + 1;
            
            // 更新等级
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("current_level", newLevel);
            
            // 显示升级消息
            showLevelUpMessage(newLevel);
            
            // 处理升级奖励
            handleLevelUpBonus(newLevel);
            
            // 应用更改
            editor.apply();
            
            // 递归检查是否连续升级
            checkLevelUp();
        }
    }
    
    /**
     * 处理升级奖励（外置系统）
     */
    private void handleLevelUpBonus(int newLevel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        // 1. 每升一级增加生命值上限5点
        int currentHpBonus = getTotalHpBonus();
        int newHpBonus = currentHpBonus + 5;
        editor.putInt("total_hp_bonus", newHpBonus);
        
        // 2. 增加当前生命值5点（关键修复）
        DBHelper dbHelper = DBHelper.getInstance(context);
        int userId = MyApplication.currentUserId;
        if (userId != -1) {
            // 获取当前生命值
            Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
            int currentLife = userStatus != null ? (int) userStatus.get("life") : 100;
            int maxLife = 100 + newHpBonus; // 基础100 + 新的加成
            
            // 增加当前生命值，但不超过新的上限
            int newCurrentLife = Math.min(currentLife + 5, maxLife);
            
            // 更新数据库中的当前生命值
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("life", newCurrentLife);
            dbHelper.updateUserStatus(userId, updateData);
            
            Log.d("LevelUp", "升级后生命值更新: " + currentLife + " -> " + newCurrentLife + " (上限: " + maxLife + ")");
        }
        
        // 3. 随机给予5-20金币奖励
        int randomGold = 5 + random.nextInt(16); // 5-20随机数
        
        // 记录升级奖励信息
        editor.putInt("last_level_up_level", newLevel);
        editor.putInt("last_level_up_gold", randomGold);
        editor.putInt("last_level_up_hp", 5);
        
        // 应用更改
        editor.apply();
        
        // 显示升级奖励消息
        showLevelUpRewardMessage(newLevel, randomGold);
    }
    
    /**
     * 显示升级奖励消息
     */
    private void showLevelUpRewardMessage(int newLevel, int goldBonus) {
        StringBuilder message = new StringBuilder();
        message.append("恭喜升到 ").append(newLevel).append(" 级！\n"); // 去掉字符串中的实际换行，保留\n
        message.append("生命值上限 +5\n"); // 用\n表示换行，去掉非法的\和实际换行
        message.append("获得金币：").append(goldBonus);

        Toast.makeText(context, message.toString(), Toast.LENGTH_LONG).show();
    }
    
    /**
     * 显示升级消息
     */
    private void showLevelUpMessage(int newLevel) {
        String message = String.format("恭喜！等级提升到 %d 级！", newLevel);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 获取等级进度（百分比）- 外置系统
     */
    public float getLevelProgress() {
        int currentLevel = getCurrentLevel();
        int currentExp = getCurrentExp();
        
        if (currentLevel >= MAX_LEVEL) {
            return 1.0f; // 满级
        }
        
        int expRequired = getExpRequiredForNextLevel(currentLevel);
        int expForCurrentLevel = getExpRequiredForNextLevel(currentLevel - 1);
        
        if (expRequired == expForCurrentLevel) {
            return 1.0f; // 已经是最大等级
        }
        
        return (float)(currentExp - expForCurrentLevel) / (expRequired - expForCurrentLevel);
    }
    
    /**
     * 获取当前等级的经验信息 - 外置系统
     */
    public String getLevelInfo() {
        int currentLevel = getCurrentLevel();
        int currentExp = getCurrentExp();
        
        if (currentLevel >= MAX_LEVEL) {
            return String.format("等级: %d (满级) - 经验: %d", currentLevel, currentExp);
        }
        
        int expRequired = getExpRequiredForNextLevel(currentLevel);
        return String.format("等级: %d - 经验: %d/%d (%.1f%%)", 
            currentLevel, currentExp, expRequired, getLevelProgress() * 100);
    }
    
    /**
     * 获取等级统计信息 - 外置系统
     */
    public LevelStats getLevelStats() {
        int currentLevel = getCurrentLevel();
        int totalExp = getCurrentExp();
        
        // 计算总获得经验（包括已消耗的升级经验）
        int totalExpEarned = totalExp;
        for (int i = 1; i < currentLevel; i++) {
            totalExpEarned += getExpRequiredForNextLevel(i);
        }
        
        return new LevelStats(currentLevel, totalExp, totalExpEarned);
    }
    
    /**
     * 重置等级和经验（用于测试或重新开始）- 外置系统
     */
    public void resetLevelAndExp() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("current_level", 1);
        editor.putInt("current_exp", 0);
        editor.putInt("total_hp_bonus", 0);
        editor.apply();
        
        Toast.makeText(context, "等级和经验已重置", Toast.LENGTH_SHORT).show();
    }
}

/**
 * 等级统计类
 */
class LevelStats {
    public int currentLevel;
    public int currentExp;
    public int totalExpEarned;
    
    public LevelStats(int currentLevel, int currentExp, int totalExpEarned) {
        this.currentLevel = currentLevel;
        this.currentExp = currentExp;
        this.totalExpEarned = totalExpEarned;
    }
    
    public int getExpToNextLevel(int currentLevel) {
        if (currentLevel >= 100) return 0;
        return 500 + (currentLevel - 1) * 500;
    }
}