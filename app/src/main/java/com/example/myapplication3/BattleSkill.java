package com.example.myapplication3;

/**
 * 战斗技能类
 */
public class BattleSkill {
    public String name;
    public int cooldown;
    public int currentCooldown;
    public int damage;
    public BattleSkillManager.SkillType skillType;
    public int level;
    public String description;

    public BattleSkill(String name, int cooldown) {
        this.name = name;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.damage = 0;
    }

    public BattleSkill(String name, int cooldown, int damage) {
        this.name = name;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.damage = damage;
    }

    public BattleSkill(String name, String description, int cooldown, int damage) {
        this.name = name;
        this.description = description;
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

    /**
     * 获取技能冷却时间
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * 获取当前冷却时间
     */
    public int getCurrentCooldown() {
        return currentCooldown;
    }

    /**
     * 重置冷却时间
     */
    public void resetCooldown() {
        currentCooldown = cooldown;
    }

    // Setter方法
    public void setName(String name) {
        this.name = name;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setCurrentCooldown(int currentCooldown) {
        this.currentCooldown = currentCooldown;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setSkillType(BattleSkillManager.SkillType skillType) {
        this.skillType = skillType;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}