package com.example.myapplication3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 难度管理器
 * 负责处理不同难度下的游戏规则和效果
 */
public class DifficultyManager {
    
    private static volatile DifficultyManager instance;
    private Random random = new Random();
    
    // 难度解锁状态
    private Map<String, Boolean> difficultyUnlockStatus = new HashMap<>();
    
    private DifficultyManager() {
        // 默认解锁简单难度
        difficultyUnlockStatus.put("easy", true);
        difficultyUnlockStatus.put("normal", false);
        difficultyUnlockStatus.put("hard", false);
    }
    
    public static DifficultyManager getInstance() {
        if (instance == null) {
            synchronized (DifficultyManager.class) {
                if (instance == null) {
                    instance = new DifficultyManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 检查难度是否已解锁
     */
    public boolean isDifficultyUnlocked(String difficulty) {
        return difficultyUnlockStatus.getOrDefault(difficulty, false);
    }
    
    /**
     * 解锁难度
     */
    public void unlockDifficulty(String difficulty) {
        difficultyUnlockStatus.put(difficulty, true);
    }
    
    /**
     * 获取难度对应的资源获取概率
     */
    public double getRarityProbability(String difficulty, Rarity rarity) {
        switch (difficulty) {
            case "easy":
                switch (rarity) {
                    case COMMON: return 0.60;
                    case RARE: return 0.30;
                    case EPIC: return 0.10;
                    default: return 0.0; // 简单难度只有普通、稀有、史诗
                }
            case "normal":
                switch (rarity) {
                    case COMMON: return 0.60;
                    case RARE: return 0.25;
                    case EPIC: return 0.10;
                    case LEGENDARY: return 0.05;
                    default: return 0.0;
                }
            case "hard":
                switch (rarity) {
                    case COMMON: return 0.35;
                    case RARE: return 0.30;
                    case EPIC: return 0.20;
                    case LEGENDARY: return 0.10;
                    case MYTHICAL: return 0.05;
                    default: return 0.0;
                }
            default:
                return rarity.getBaseProbability();
        }
    }
    
    /**
     * 检查是否触发随机事件
     */
    public boolean shouldTriggerRandomEvent(String difficulty, String actionType) {
        if ("easy".equals(difficulty)) {
            return false; // 简单难度不触发随机事件
        }
        
        double probability = 0.0;
        if ("normal".equals(difficulty)) {
            probability = 0.10; // 普通难度10%概率
        } else if ("hard".equals(difficulty)) {
            probability = 0.20; // 困难难度20%概率
        }
        
        return random.nextDouble() <= probability;
    }
    
    /**
     * 获取工具耐久度额外消耗
     */
    public int getToolDurabilityCost(String difficulty) {
        switch (difficulty) {
            case "easy": return 0;
            case "normal": return 1;
            case "hard": return 2;
            default: return 0;
        }
    }
    
    /**
     * 获取移动额外消耗
     */
    public Map<String, Integer> getMoveCost(String difficulty) {
        Map<String, Integer> costs = new HashMap<>();
        
        switch (difficulty) {
            case "easy":
                costs.put("stamina", 0);
                costs.put("hunger", 0);
                costs.put("thirst", 0);
                break;
            case "normal":
                costs.put("stamina", 5);
                costs.put("hunger", 0);
                costs.put("thirst", 0);
                break;
            case "hard":
                costs.put("stamina", 5);
                costs.put("hunger", 5);
                costs.put("thirst", 5);
                break;
            default:
                costs.put("stamina", 0);
                costs.put("hunger", 0);
                costs.put("thirst", 0);
        }
        
        return costs;
    }
    
    /**
     * 获取生命值扣除规则
     */
    public int getLifeDeduction(String difficulty, int hunger, int thirst, int stamina) {
        switch (difficulty) {
            case "easy":
                // 简单难度：所有属性都为0才扣血，每次5点
                if (hunger <= 0 && thirst <= 0 && stamina <= 0) {
                    return 5;
                }
                return 0;
            case "normal":
                // 普通难度：任意一项归零就扣血，每次5点
                if (hunger <= 0 || thirst <= 0 || stamina <= 0) {
                    return 5;
                }
                return 0;
            case "hard":
                // 困难难度：任意一项归零就扣血，每次10点
                if (hunger <= 0 || thirst <= 0 || stamina <= 0) {
                    return 10;
                }
                return 0;
            default:
                return 0;
        }
    }
    
    /**
     * 获取夜间采集额外消耗
     */
    public int getNightCollectCost(String difficulty) {
        switch (difficulty) {
            case "easy": return 0;
            case "normal": return 5;
            case "hard": return 10;
            default: return 0;
        }
    }
    
    /**
     * 检查是否是夜间（18:00-次日6:00）
     */
    public boolean isNightTime(int gameHour) {
        return gameHour >= 18 || gameHour < 6;
    }
    
    /**
     * 获取资源获取数量调整系数
     */
    public double getResourceMultiplier(String difficulty) {
        switch (difficulty) {
            case "easy": return 0.8;   // 简单难度资源减少
            case "normal": return 1.0; // 普通难度保持不变
            case "hard": return 1.2;  // 困难难度资源增加
            default: return 1.0;
        }
    }
    
    /**
     * 处理随机事件
     */
    public String triggerRandomEvent(String difficulty, String actionType) {
        if (!shouldTriggerRandomEvent(difficulty, actionType)) {
            return null;
        }
        
        String[] events = {
            "你遇到了一只野狼，损失了一些生命值！",
            "突然下起了暴雨，你的衣服湿透了！",
            "你发现了一个隐藏的宝箱！",
            "你被毒蛇咬伤了！",
            "你发现了一些额外的资源！",
            "你的工具意外损坏了！",
            "你迷路了，花费了更多时间！"
        };
        
        return events[random.nextInt(events.length)];
    }
    
    /**
     * 获取难度描述
     */
    public String getDifficultyDescription(String difficulty) {
        switch (difficulty) {
            case "easy":
                return "简单难度：\n" +
                       "• 资源获取减少但无随机事件\n" +
                       "• 所有属性归零后才扣生命值\n" +
                       "• 资源概率：普通60%、稀有30%、史诗10%";
            case "normal":
                return "普通难度：\n" +
                       "• 标准资源获取，10%随机事件概率\n" +
                       "• 工具使用额外消耗1耐久\n" +
                       "• 移动额外消耗5体力\n" +
                       "• 任意属性归零扣生命值\n" +
                       "• 夜间采集额外消耗5体力\n" +
                       "• 资源概率：普通60%、稀有25%、史诗10%、传说5%";
            case "hard":
                return "困难难度：\n" +
                       "• 资源获取增加，20%随机事件概率\n" +
                       "• 工具使用额外消耗2耐久\n" +
                       "• 移动额外消耗饥饿、口渴、体力各5点\n" +
                       "• 任意属性归零扣10生命值\n" +
                       "• 夜间采集额外消耗10体力\n" +
                       "• 资源概率：普通35%、稀有30%、史诗20%、传说10%、神话5%";
            default:
                return "未知难度";
        }
    }
}