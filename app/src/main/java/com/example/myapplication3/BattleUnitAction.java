package com.example.myapplication3;

/**
 * 战斗单位行动类
 * 包含攻击、技能等行动信息
 */
public class BattleUnitAction {
    
    // 行动类型常量
    public static final int ACTION_ATTACK = 0;
    public static final int ACTION_SKILL = 1;
    
    // 目标类型常量
    public static final int TARGET_ENEMY = 0;
    public static final int TARGET_PLAYER = 1;
    
    private int actionType;      // 行动类型
    private int skillIndex;      // 技能索引（-1表示攻击）
    private int targetType;       // 目标类型
    private int targetIndex;      // 目标索引
    
    public BattleUnitAction(int actionType, int skillIndex, int targetIndex) {
        this.actionType = actionType;
        this.skillIndex = skillIndex;
        
        // 简化的目标类型逻辑，适配1v1布局
        if (targetIndex == 0) {
            this.targetType = TARGET_PLAYER;
            this.targetIndex = 0;
        } else {
            this.targetType = TARGET_ENEMY;
            this.targetIndex = 0;
        }
    }
    
    // Getter和Setter方法
    public int getActionType() {
        return actionType;
    }
    
    public void setActionType(int actionType) {
        this.actionType = actionType;
    }
    
    public int getSkillIndex() {
        return skillIndex;
    }
    
    public void setSkillIndex(int skillIndex) {
        this.skillIndex = skillIndex;
    }
    
    public int getTargetType() {
        return targetType;
    }
    
    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }
    
    public int getTargetIndex() {
        return targetIndex;
    }
    
    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }
    
    @Override
    public String toString() {
        String actionName = actionType == ACTION_ATTACK ? "攻击" : "技能";
        String targetName = targetType == TARGET_PLAYER ? "玩家" : "敌方";
        return "行动类型: " + actionName + ", 技能索引: " + skillIndex + 
               ", 目标类型: " + targetName + ", 目标索引: " + targetIndex;
    }
}