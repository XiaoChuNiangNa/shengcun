package com.example.myapplication3;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

// 导入BattleSkill类
import com.example.myapplication3.BattleSkill;

/**
 * 怪物数据类
 * 包含怪物属性、技能、掉落物等信息
 * 集成技能系统：怪物调用技能类中的技能
 */
public class Monster {
    private String name;
    private String type;
    private String spawnLocation;
    private int maxHealth;
    private int attack;
    private int defense;
    private int speed;
    private List<BattleSkill> skills; // 使用BattleSkillManager的技能
    private DropItem[] dropItems;
    
    public Monster(String name, String type, String spawnLocation, 
                  int maxHealth, int attack, int defense, int speed,
                  List<BattleSkill> skills, DropItem[] dropItems) {
        this.name = name;
        this.type = type;
        this.spawnLocation = spawnLocation;
        this.maxHealth = maxHealth;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.dropItems = dropItems;
    }
    
    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public String getSpawnLocation() { return spawnLocation; }
    public int getMaxHealth() { return maxHealth; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public List<BattleSkill> getSkills() { return skills; }
    public DropItem[] getDropItems() { return dropItems; }
    
    /**
     * 添加技能
     */
    public void addSkill(BattleSkill skill) {
        if (skill != null) {
            skills.add(skill);
        }
    }
    
    /**
     * 获取可用的技能（冷却结束的技能）
     */
    public List<BattleSkill> getAvailableSkills() {
        List<BattleSkill> availableSkills = new ArrayList<>();
        for (BattleSkill skill : skills) {
            if (skill.isReady()) {
                availableSkills.add(skill);
            }
        }
        return availableSkills;
    }
    
    /**
     * 随机选择可用的技能
     */
    public BattleSkill getRandomAvailableSkill() {
        List<BattleSkill> availableSkills = getAvailableSkills();
        if (availableSkills.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return availableSkills.get(random.nextInt(availableSkills.size()));
    }
    
    /**
     * 检查怪物是否可以逃跑
     */
    public boolean canEscape() {
        for (BattleSkill skill : skills) {
            if (skill.getName() != null && skill.getName().contains("逃跑")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 尝试逃跑
     */
    public boolean tryEscape() {
        for (BattleSkill skill : skills) {
            if (skill.getName() != null && skill.getName().contains("逃跑")) {
                if (skill.isReady()) {
                    // 根据技能等级判断逃跑概率
                    int level = skill.getLevel();
                    double escapeChance = 0.5; // Lv1: 50%
                    if (level == 2) escapeChance = 0.75; // Lv2: 75%
                    else if (level == 3) escapeChance = 1.0; // Lv3: 100%
                    
                    if (Math.random() < escapeChance) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }
    
    /**
     * 回合结束时更新技能冷却
     */
    public void updateSkillCooldowns() {
        for (BattleSkill skill : skills) {
            skill.reduceCooldown();
        }
    }
    
    /**
     * 随机获取掉落物
     */
    public String[] getRandomDrops() {
        Random random = new Random();
        java.util.ArrayList<String> drops = new java.util.ArrayList<>();
        
        for (DropItem item : dropItems) {
            int count = 0;
            for (int i = 0; i < item.maxCount; i++) {
                if (random.nextDouble() <= item.dropRate) {
                    count++;
                }
            }
            if (count > 0) {
                drops.add(item.name + " x" + count);
            }
        }
        
        return drops.toArray(new String[0]);
    }
    
    /**
     * 获取怪物描述
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("名称: ").append(name).append("\n");
        sb.append("类型: ").append(type).append("\n");
        sb.append("生成地: ").append(spawnLocation).append("\n");
        sb.append("生命: ").append(maxHealth).append("\n");
        sb.append("攻击: ").append(attack).append("\n");
        sb.append("防御: ").append(defense).append("\n");
        sb.append("速度: ").append(speed).append("\n");
        
        if (!skills.isEmpty()) {
            sb.append("技能: ");
            for (int i = 0; i < skills.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(skills.get(i).getName());
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}

/**
 * 掉落物类
 */
class DropItem {
    String name;
    double dropRate; // 掉落概率（0.0-1.0）
    int maxCount;   // 最大掉落数量
    
    public DropItem(String name, double dropRate, int maxCount) {
        this.name = name;
        this.dropRate = dropRate;
        this.maxCount = maxCount;
    }
}