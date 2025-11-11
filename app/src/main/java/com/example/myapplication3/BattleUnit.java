package com.example.myapplication3;

import java.util.Random;

/**
 * 战斗单位类
 * 管理角色的生命值、属性、技能等战斗相关数据
 */
public class BattleUnit {
    private String name;
    private int maxHealth;
    private int currentHealth;
    private PlayerStats stats; // 属性系统
    
    // 攻击冷却系统
    private int attackCooldown;
    private int maxAttackCooldown;
    private boolean isReadyToAttack = false;
    
    // 技能系统
    private BattleSkill[] skills;
    private Random random = new Random();
    
    // 单位类型
    public static final int TYPE_PLAYER = 1;
    public static final int TYPE_ENEMY = 2;
    public static final int TYPE_BOSS = 3;
    public static final int TYPE_SUMMON = 4; // 召唤单位
    private int unitType;
    
    // 召唤系统
    private BattleUnit master; // 召唤单位的主单位
    private int summonDuration = -1; // 召唤持续时间（回合数），-1表示永久存在
    
    public BattleUnit(String name, int maxHealth, int attack, int defense, int speed, 
                     BattleSkill[] skills, int unitType) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.stats = new PlayerStats(attack, defense, speed);
        this.skills = skills;
        this.unitType = unitType;
        
        // 根据速度计算攻击冷却时间
        initAttackCooldown();
    }
    
    /**
     * 简化构造函数（用于BattleActivity）
     */
    public BattleUnit(String name, int maxHealth, int attack, int defense, int[] skillCooldowns) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.stats = new PlayerStats(attack, defense, 10); // 默认速度
        
        // 创建默认技能
        this.skills = new BattleSkill[skillCooldowns.length];
        for (int i = 0; i < skillCooldowns.length; i++) {
            String[] skillNames = {"重击", "治疗", "暴风斩", "火焰冲击", "冰霜箭"};
            String skillName = i < skillNames.length ? skillNames[i] : "技能" + (i + 1);
            this.skills[i] = new BattleSkill(skillName, skillCooldowns[i], attack * 2);
        }
        
        // 默认类型为玩家
        this.unitType = TYPE_PLAYER;
        
        // 根据速度计算攻击冷却时间
        initAttackCooldown();
    }
    
    /**
     * 根据速度初始化攻击冷却系统
     */
    private void initAttackCooldown() {
        // 将攻击间隔（秒）转换为游戏内的冷却回合数
        // 假设1回合 = 1秒，这里使用10个时间单位代表1秒，便于游戏内显示
        double attackInterval = stats.getAttackInterval();
        this.maxAttackCooldown = (int) (attackInterval * 10);
        this.attackCooldown = 0;
        this.isReadyToAttack = false;
    }
    
    /**
     * 执行攻击
     * @return 造成的伤害值
     */
    public int attack() {
        if (isReadyToAttack) {
            isReadyToAttack = false;
            attackCooldown = maxAttackCooldown;
            
            // 基础攻击力 + 随机浮动（±20%）
            int baseAttack = stats.getAttack();
            int minDamage = (int) (baseAttack * 0.8);
            int maxDamage = (int) (baseAttack * 1.2);
            return random.nextInt(maxDamage - minDamage + 1) + minDamage;
        }
        return 0;
    }
    
    /**
     * 承受伤害
     * @param rawDamage 原始伤害值
     * @return 实际承受的伤害
     */
    public int takeDamage(int rawDamage) {
        // 计算实际伤害：原始伤害 - 防御力
        int actualDamage = PlayerStats.calculateDamage(rawDamage, stats.getDefense());
        currentHealth = Math.max(0, currentHealth - actualDamage);
        return actualDamage;
    }
    
    /**
     * 治疗
     * @param amount 治疗量
     */
    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
    
    /**
     * 更新冷却系统（每个游戏回合调用）
     */
    public void updateCooldowns() {
        // 更新攻击冷却
        if (attackCooldown > 0) {
            attackCooldown--;
            if (attackCooldown <= 0) {
                isReadyToAttack = true;
            }
        }
        
        // 更新技能冷却
        for (BattleSkill skill : skills) {
            if (skill.currentCooldown > 0) {
                skill.currentCooldown--;
            }
        }
    }
    
    /**
     * 检查是否可以攻击
     */
    public boolean isReadyToAttack() {
        return isReadyToAttack;
    }
    
    /**
     * 检查单位是否存活
     */
    public boolean isAlive() {
        return currentHealth > 0;
    }
    
    /**
     * 检查是否可以释放技能
     */
    public boolean canUseSkill(int skillIndex) {
        return skillIndex < skills.length && skills[skillIndex].currentCooldown <= 0;
    }
    
    /**
     * 使用技能
     */
    public void useSkill(int skillIndex) {
        if (skillIndex < skills.length) {
            skills[skillIndex].currentCooldown = skills[skillIndex].cooldown;
        }
    }
    
    /**
     * 获取技能伤害
     */
    public int getSkillDamage(int skillIndex) {
        if (skillIndex < skills.length) {
            return skills[skillIndex].damage;
        }
        return 0;
    }
    
    // Getters
    public String getName() { return name; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public PlayerStats getStats() { return stats; }
    public BattleSkill[] getSkills() { return skills; }
    public int getUnitType() { return unitType; }
    
    // Setters
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }
    

    
    // 显示相关方法
    public int getHealthPercentage() {
        return (int) ((currentHealth * 100.0) / maxHealth);
    }
    
    // 添加BattleActivity需要的方法
    public int getHealthPercent() {
        return getHealthPercentage();
    }
    
    public int getAttackCooldownPercentage() {
        return stats.getAttackCooldownPercentage(attackCooldown, maxAttackCooldown);
    }
    
    public int getAttackCooldownPercent() {
        return getAttackCooldownPercentage();
    }
    
    public int getSkillCooldownPercentage(int skillIndex) {
        if (skillIndex >= skills.length) return 0;
        BattleSkill skill = skills[skillIndex];
        return (int) (((skill.cooldown - skill.currentCooldown) * 100.0) / skill.cooldown);
    }
    
    public int getSkillCooldownPercent(int skillIndex) {
        return getSkillCooldownPercentage(skillIndex);
    }
    
    // 添加缺失的方法
    public void resetCooldowns() {
        attackCooldown = 0;
        isReadyToAttack = true;
        for (BattleSkill skill : skills) {
            if (skill != null) {
                skill.currentCooldown = 0;
            }
        }
    }
    
    public int getAttack() {
        return stats.getAttack();
    }
    
    public int getDefense() {
        return stats.getDefense();
    }
    
    // 召唤系统相关方法
    public BattleUnit getMaster() {
        return master;
    }
    
    public void setMaster(BattleUnit master) {
        this.master = master;
    }
    
    public int getSummonDuration() {
        return summonDuration;
    }
    
    public void setSummonDuration(int summonDuration) {
        this.summonDuration = summonDuration;
    }
    
    public void setSkills(BattleSkill[] skills) {
        this.skills = skills;
    }
    
    /**
     * 获取单位描述（包含属性信息）
     */
    public String getDescription() {
        return String.format("%s\n生命:%d/%d\n%s", 
            name, currentHealth, maxHealth, stats.toString());
    }
    
    /**
     * 检查是否为Boss单位
     */
    public boolean isBoss() {
        return unitType == TYPE_BOSS;
    }
    
    /**
     * 检查是否为精英单位
     */
    public boolean isElite() {
        return unitType == TYPE_ENEMY && stats.getAttack() > 15;
    }
}

/**
 * 战斗技能类（完整版）
 */
class BattleSkill {
    String name;
    int cooldown;
    int currentCooldown;
    int damage;
    BattleSkillManager.SkillType skillType;
    int level;
    String description;
    
    BattleSkill(String name, int cooldown) {
        this.name = name;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.damage = 0;
    }
    
    BattleSkill(String name, int cooldown, int damage) {
        this.name = name;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.damage = damage;
    }
    
    /**
     * 检查技能是否可用
     */
    public boolean isReady() {
        return currentCooldown <= 0;
    }
    
    /**
     * 减少冷却时间
     */
    public void reduceCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }
    
    /**
     * 获取技能名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取技能类型
     */
    public BattleSkillManager.SkillType getSkillType() {
        return skillType;
    }
    
    /**
     * 获取技能等级
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * 获取技能描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取技能伤害
     */
    public int getDamage() {
        return damage;
    }
}