package com.example.myapplication3;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * 对战结果管理器
 * 处理战斗结算、奖励计算、时间管理等
 */
public class BattleResultManager {
    
    private Context context;
    private DBHelper dbHelper;
    private SharedPreferences sharedPreferences;
    
    // 奖励常量
    private static final int BASE_GOLD_REWARD = 50;
    private static final int BASE_EXP_REWARD = 100;
    private static final int HOURS_PER_BATTLE = 1; // 每次战斗增加1小时游戏时间
    
    public BattleResultManager(Context context) {
        this.context = context;
        this.dbHelper = DBHelper.getInstance(context);
        this.sharedPreferences = context.getSharedPreferences("battle_stats", Context.MODE_PRIVATE);
    }
    
    /**
     * 处理战斗胜利结算
     */
    public void handleVictory(int userId, int battleRounds, boolean isBossBattle) {
        // 计算奖励
        int goldReward = calculateGoldReward(battleRounds, isBossBattle);
        int expReward = calculateExpReward(battleRounds, isBossBattle);
        
        // 更新玩家数据
        updatePlayerResources(userId, goldReward, expReward);
        
        // 增加游戏时间
        addGameTime(userId, HOURS_PER_BATTLE);
        
        // 更新战斗统计
        updateBattleStats(userId, true, goldReward, expReward);
        
        // 显示结果
        showVictoryMessage(goldReward, expReward);
        
        // 检查升级
        checkLevelUp(userId);
    }
    
    /**
     * 处理战斗失败
     */
    public void handleDefeat(int userId) {
        // 减少玩家生命值（如果有后续影响）
        // 暂时只是简单处理
        
        // 更新战斗统计
        updateBattleStats(userId, false, 0, 0);
        
        // 显示失败消息
        showDefeatMessage();
        
        // 触发游戏结束流程
        triggerGameOver(userId);
    }
    
    /**
     * 计算金币奖励
     */
    private int calculateGoldReward(int battleRounds, boolean isBossBattle) {
        int reward = BASE_GOLD_REWARD + (battleRounds * 5);
        
        if (isBossBattle) {
            reward *= 3; // Boss战奖励翻倍
        }
        
        // 根据难度调整奖励
        String difficulty = getCurrentDifficulty();
        switch (difficulty) {
            case "hard":
                reward *= 1.5;
                break;
            case "normal":
                reward *= 1.0;
                break;
            case "easy":
                reward *= 0.7;
                break;
        }
        
        return Math.max(reward, 10); // 最低奖励10金币
    }
    
    /**
     * 计算经验奖励
     */
    private int calculateExpReward(int battleRounds, boolean isBossBattle) {
        int reward = BASE_EXP_REWARD + (battleRounds * 10);
        
        if (isBossBattle) {
            reward *= 3; // Boss战奖励翻倍
        }
        
        // 根据难度调整奖励
        String difficulty = getCurrentDifficulty();
        switch (difficulty) {
            case "hard":
                reward *= 1.5;
                break;
            case "normal":
                reward *= 1.0;
                break;
            case "easy":
                reward *= 0.7;
                break;
        }
        
        return Math.max(reward, 20); // 最低奖励20经验
    }
    
    /**
     * 更新玩家资源
     */
    private void updatePlayerResources(int userId, int goldReward, int expReward) {
        // 获取当前资源
        int currentGold = dbHelper.getUserGold(userId);
        int currentExp = dbHelper.getUserExp(userId);
        
        // 更新资源
        dbHelper.updateUserGold(userId, currentGold + goldReward);
        dbHelper.updateUserExp(userId, currentExp + expReward);
        
        // 记录获取的资源
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("last_gold_reward", goldReward);
        editor.putInt("last_exp_reward", expReward);
        editor.apply();
    }
    
    /**
     * 增加游戏时间
     */
    private void addGameTime(int userId, int hours) {
        // 这里需要集成到游戏的时间管理系统中
        // 暂时使用SharedPreferences记录
        int currentGameHours = sharedPreferences.getInt("game_hours_" + userId, 0);
        sharedPreferences.edit().putInt("game_hours_" + userId, currentGameHours + hours).apply();
        
        // 记录时间变化
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("last_time_added", hours);
        editor.apply();
    }
    
    /**
     * 更新战斗统计
     */
    private void updateBattleStats(int userId, boolean isVictory, int goldReward, int expReward) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        // 更新总战斗次数
        int totalBattles = sharedPreferences.getInt("total_battles_" + userId, 0);
        editor.putInt("total_battles_" + userId, totalBattles + 1);
        
        // 更新胜利次数
        if (isVictory) {
            int victories = sharedPreferences.getInt("victories_" + userId, 0);
            editor.putInt("victories_" + userId, victories + 1);
        }
        
        // 更新总获得金币和经验
        int totalGold = sharedPreferences.getInt("total_gold_earned_" + userId, 0);
        int totalExp = sharedPreferences.getInt("total_exp_earned_" + userId, 0);
        editor.putInt("total_gold_earned_" + userId, totalGold + goldReward);
        editor.putInt("total_exp_earned_" + userId, totalExp + expReward);
        
        editor.apply();
    }
    
    /**
     * 检查升级
     */
    private void checkLevelUp(int userId) {
        int currentExp = dbHelper.getUserExp(userId);
        int currentLevel = dbHelper.getUserLevel(userId);
        
        // 计算升级所需经验（简化公式：每级需要100 * 等级）
        int expNeeded = currentLevel * 100;
        
        if (currentExp >= expNeeded) {
            // 升级
            int newLevel = currentLevel + 1;
            dbHelper.updateUserLevel(userId, newLevel);
            
            // 显示升级消息
            showLevelUpMessage(newLevel);
            
            // 升级奖励（每级增加属性）
            handleLevelUpBonus(userId, newLevel);
        }
    }
    
    /**
     * 处理升级奖励
     */
    private void handleLevelUpBonus(int userId, int newLevel) {
        // 每级增加属性
        int attackBonus = newLevel * 2;
        int defenseBonus = newLevel;
        int speedBonus = newLevel;
        
        // 更新玩家属性（这里需要集成到玩家属性系统中）
        // 暂时记录到SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("level_up_attack_bonus", attackBonus);
        editor.putInt("level_up_defense_bonus", defenseBonus);
        editor.putInt("level_up_speed_bonus", speedBonus);
        editor.apply();
    }
    
    /**
     * 触发游戏结束
     */
    private void triggerGameOver(int userId) {
        // 设置游戏结束状态
        // 暂时简化游戏结束逻辑，直接记录到SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("game_ended", true);
        editor.putLong("game_end_time", System.currentTimeMillis());
        editor.apply();
        
        // 这里可以添加更多的游戏结束逻辑
        // 比如显示游戏结束界面、保存游戏数据等
    }
    
    /**
     * 获取当前难度
     */
    private String getCurrentDifficulty() {
        // 这里需要集成到游戏的难度系统中
        // 暂时返回默认难度
        return sharedPreferences.getString("current_difficulty", "normal");
    }
    
    /**
     * 显示胜利消息
     */
    private void showVictoryMessage(int goldReward, int expReward) {
        String message = String.format("战斗胜利！\n获得 %d 金币和 %d 经验\n游戏时间 +%d 小时", 
            goldReward, expReward, HOURS_PER_BATTLE);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 显示失败消息
     */
    private void showDefeatMessage() {
        Toast.makeText(context, "战斗失败！游戏结束", Toast.LENGTH_LONG).show();
    }
    
    /**
     * 显示升级消息
     */
    private void showLevelUpMessage(int newLevel) {
        String message = String.format("恭喜！等级提升到 %d 级！", newLevel);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 保存战斗结果
     */
    public void saveBattleResult(BattleResult result) {
        // 根据战斗结果处理不同的逻辑
        if (result.isBattleResult()) {
            // 胜利处理
            handleVictory(result.getUserId(), result.getBattleRounds(), result.isBossBattle());
        } else {
            // 失败处理
            handleDefeat(result.getUserId());
        }
        
        // 保存详细结果到SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("last_battle_user_id", result.getUserId());
        editor.putBoolean("last_battle_result", result.isBattleResult());
        editor.putInt("last_battle_gold", result.getGoldEarned());
        editor.putInt("last_battle_exp", result.getExpEarned());
        editor.putInt("last_battle_enemies", result.getEnemiesKilled());
        editor.putBoolean("last_battle_boss", result.isBossBattle());
        editor.putInt("last_battle_rounds", result.getBattleRounds());
        editor.putLong("last_battle_time", result.getTimestamp());
        editor.apply();
    }
    
    /**
     * 获取战斗统计
     */
    public BattleStats getBattleStats(int userId) {
        return new BattleStats(
            sharedPreferences.getInt("total_battles_" + userId, 0),
            sharedPreferences.getInt("victories_" + userId, 0),
            sharedPreferences.getInt("total_gold_earned_" + userId, 0),
            sharedPreferences.getInt("total_exp_earned_" + userId, 0)
        );
    }
}

/**
 * 战斗统计类
 */
class BattleStats {
    public int totalBattles;
    public int victories;
    public int totalGoldEarned;
    public int totalExpEarned;
    
    public BattleStats(int totalBattles, int victories, int totalGoldEarned, int totalExpEarned) {
        this.totalBattles = totalBattles;
        this.victories = victories;
        this.totalGoldEarned = totalGoldEarned;
        this.totalExpEarned = totalExpEarned;
    }
    
    public double getWinRate() {
        return totalBattles > 0 ? (victories * 100.0) / totalBattles : 0;
    }
}