package com.example.myapplication3;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务类
 */
public class Quest {
    private int id;
    private String title;
    private String description;
    private QuestType type;
    private Map<String, Integer> requirements; // 任务需求：物品名称 -> 数量
    private Map<String, Integer> reward1;     // 奖励1：物品名称 -> 数量
    private Map<String, Integer> reward2;     // 奖励2：物品名称 -> 数量
    private int experienceReward;
    private int goldReward;
    private boolean isCompleted;
    private boolean isNewbieQuest; // 是否为新手任务
    private Map<String, Integer> currentProgress; // 当前进度

    public enum QuestType {
        COLLECTION,    // 收集任务
        BUILDING,      // 建造任务
        COMBAT,        // 战斗任务
        EXPLORATION,   // 探索任务
        STORY          // 剧情任务
    }

    public Quest(int id, String title, String description, QuestType type, 
                 Map<String, Integer> requirements, Map<String, Integer> reward1, 
                 Map<String, Integer> reward2, int experienceReward, int goldReward, 
                 boolean isNewbieQuest) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.requirements = requirements != null ? requirements : new HashMap<>();
        this.reward1 = reward1 != null ? reward1 : new HashMap<>();
        this.reward2 = reward2 != null ? reward2 : new HashMap<>();
        this.experienceReward = experienceReward;
        this.goldReward = goldReward;
        this.isCompleted = false;
        this.isNewbieQuest = isNewbieQuest;
        this.currentProgress = new HashMap<>();
        
        // 初始化进度
        for (String item : this.requirements.keySet()) {
            this.currentProgress.put(item, 0);
        }
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getName() { return title; } // 为兼容性添加getName方法
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public Map<String, Integer> getRequirements() { return requirements; }
    public Map<String, Integer> getReward1() { return reward1; }
    public Map<String, Integer> getReward2() { return reward2; }
    public int getExperienceReward() { return experienceReward; }
    public int getGoldReward() { return goldReward; }
    public boolean isCompleted() { return isCompleted; }
    public boolean isNewbieQuest() { return isNewbieQuest; }
    public Map<String, Integer> getCurrentProgress() { return currentProgress; }

    // Setters
    public void setCompleted(boolean completed) { isCompleted = completed; }

    /**
     * 更新任务进度
     * @param itemName 物品名称
     * @param amount 增加的数量
     * @return 是否完成任务
     */
    public boolean updateProgress(String itemName, int amount) {
        if (isCompleted) return true;
        
        if (requirements.containsKey(itemName)) {
            int current = currentProgress.getOrDefault(itemName, 0);
            int newAmount = Math.min(current + amount, requirements.get(itemName));
            currentProgress.put(itemName, newAmount);
            
            // 检查是否完成任务
            if (checkCompletion()) {
                isCompleted = true;
                return true;
            }
        }
        return false;
    }

    /**
     * 检查任务是否完成
     */
    public boolean checkCompletion() {
        for (Map.Entry<String, Integer> req : requirements.entrySet()) {
            int current = currentProgress.getOrDefault(req.getKey(), 0);
            if (current < req.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取任务进度百分比
     */
    public int getProgressPercentage() {
        if (requirements.isEmpty()) return 100;
        
        int totalRequired = 0;
        int totalCompleted = 0;
        
        for (Map.Entry<String, Integer> req : requirements.entrySet()) {
            totalRequired += req.getValue();
            totalCompleted += currentProgress.getOrDefault(req.getKey(), 0);
        }
        
        if (totalRequired == 0) return 100;
        return (int) ((totalCompleted * 100.0) / totalRequired);
    }

    /**
     * 获取进度描述
     */
    public String getProgressDescription() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> req : requirements.entrySet()) {
            int current = currentProgress.getOrDefault(req.getKey(), 0);
            int required = req.getValue();
            sb.append(req.getKey()).append(": ").append(current).append("/").append(required).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * 获取奖励描述
     */
    public String getRewardDescription() {
        StringBuilder sb = new StringBuilder();
        
        if (!reward1.isEmpty()) {
            for (Map.Entry<String, Integer> reward : reward1.entrySet()) {
                sb.append(reward.getKey()).append(" ×").append(reward.getValue()).append("  ");
            }
        }
        
        if (!reward2.isEmpty()) {
            for (Map.Entry<String, Integer> reward : reward2.entrySet()) {
                sb.append(reward.getKey()).append(" ×").append(reward.getValue()).append("  ");
            }
        }
        
        sb.append("\n经验值: ").append(experienceReward).append("  金币: ").append(goldReward);
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Quest{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isCompleted=" + isCompleted +
                ", progress=" + getProgressPercentage() + "%" +
                '}';
    }
}