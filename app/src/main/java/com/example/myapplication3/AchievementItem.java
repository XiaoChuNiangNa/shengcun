package com.example.myapplication3;

public class AchievementItem {
    private String id;          // 成就ID
    private String userId;      // 用户ID
    private String achievementType; // 成就类型
    private String category;    // 成就分类
    private String name;        // 成就名称
    private int level;          // 成就等级
    private int target;         // 目标值
    private int current;        // 当前进度
    private int reward;         // 奖励点数
    private boolean claimed;    // 是否已领取
    private boolean completed;  // 是否已完成
    
    public AchievementItem(String id, String userId, String achievementType, String category, String name, int level, int target, int reward) {
        this.id = id;
        this.userId = userId;
        this.achievementType = achievementType;
        this.category = category;
        this.name = name;
        this.level = level;
        this.target = target;
        this.reward = reward;
        this.current = 0;
        this.claimed = false;
        this.completed = false;
    }
    
    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }
    
    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }
    
    public int getReward() { return reward; }
    public void setReward(int reward) { this.reward = reward; }
    
    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    // 新增方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getAchievementType() { return achievementType; }
    public void setAchievementType(String achievementType) { this.achievementType = achievementType; }
    
    public int getProgress() { return current; }
    public void setProgress(int progress) { this.current = progress; }
    
    // 获取进度百分比
    public int getProgressPercent() {
        if (target == 0) return 0;
        return (int) ((float) current / target * 100);
    }
    
    // 检查是否可以领取
    public boolean canClaim() {
        return completed && !claimed;
    }
}