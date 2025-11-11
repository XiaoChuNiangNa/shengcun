package com.example.myapplication3;

/**
 * 战斗结果类
 * 封装战斗的结果数据
 */
public class BattleResult {
    private int userId;
    private boolean battleResult; // true表示胜利，false表示失败
    private int goldEarned;
    private int expEarned;
    private int enemiesKilled;
    private boolean isBossBattle;
    private int battleRounds;
    private long timestamp;
    private boolean enemyEscaped; // true表示敌人逃跑，false表示敌人被击杀
    
    // 无参构造函数
    public BattleResult() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public BattleResult(int userId, boolean battleResult, int goldEarned, int expEarned, 
                       int enemiesKilled, boolean isBossBattle, int battleRounds) {
        this.userId = userId;
        this.battleResult = battleResult;
        this.goldEarned = goldEarned;
        this.expEarned = expEarned;
        this.enemiesKilled = enemiesKilled;
        this.isBossBattle = isBossBattle;
        this.battleRounds = battleRounds;
        this.timestamp = System.currentTimeMillis();
        this.enemyEscaped = false; // 默认敌人被击杀
    }
    
    public BattleResult(int userId, boolean battleResult, int goldEarned, int expEarned, 
                       int enemiesKilled, boolean isBossBattle, int battleRounds, boolean enemyEscaped) {
        this.userId = userId;
        this.battleResult = battleResult;
        this.goldEarned = goldEarned;
        this.expEarned = expEarned;
        this.enemiesKilled = enemiesKilled;
        this.isBossBattle = isBossBattle;
        this.battleRounds = battleRounds;
        this.timestamp = System.currentTimeMillis();
        this.enemyEscaped = enemyEscaped;
    }
    
    // Getters
    public int getUserId() {
        return userId;
    }
    
    public boolean isBattleResult() {
        return battleResult;
    }
    
    public int getGoldEarned() {
        return goldEarned;
    }
    
    public int getExpEarned() {
        return expEarned;
    }
    
    public int getEnemiesKilled() {
        return enemiesKilled;
    }
    
    public boolean isBossBattle() {
        return isBossBattle;
    }
    
    public int getBattleRounds() {
        return battleRounds;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setBattleResult(boolean battleResult) {
        this.battleResult = battleResult;
    }
    
    public void setGoldEarned(int goldEarned) {
        this.goldEarned = goldEarned;
    }
    
    public void setExpEarned(int expEarned) {
        this.expEarned = expEarned;
    }
    
    public void setEnemiesKilled(int enemiesKilled) {
        this.enemiesKilled = enemiesKilled;
    }
    
    public void setBossBattle(boolean bossBattle) {
        isBossBattle = bossBattle;
    }
    
    public void setBattleRounds(int battleRounds) {
        this.battleRounds = battleRounds;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isEnemyEscaped() {
        return enemyEscaped;
    }
    
    public void setEnemyEscaped(boolean enemyEscaped) {
        this.enemyEscaped = enemyEscaped;
    }
    
    @Override
    public String toString() {
        return "BattleResult{" +
                "userId=" + userId +
                ", battleResult=" + battleResult +
                ", goldEarned=" + goldEarned +
                ", expEarned=" + expEarned +
                ", enemiesKilled=" + enemiesKilled +
                ", isBossBattle=" + isBossBattle +
                ", battleRounds=" + battleRounds +
                ", enemyEscaped=" + enemyEscaped +
                ", timestamp=" + timestamp +
                '}';
    }
}