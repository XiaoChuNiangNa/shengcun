package com.example.myapplication3;

// 导入BattleSkill类
import com.example.myapplication3.BattleSkill;

/**
 * 技能演示类：展示如何使用新的技能系统
 */
public class SkillDemo {
    
    public static void main(String[] args) {
        System.out.println("=== 新技能系统演示 ===\n");
        
        // 1. 显示所有技能信息
        displayAllSkills();
        
        System.out.println("\n=== 战斗演示 ===");
        
        // 2. 创建战斗单位
        BattleUnit player = new BattleUnit("玩家", 50, 10, 5, 100, 
            new BattleSkill[0], BattleUnit.TYPE_PLAYER);
        
        BattleUnit enemy = new BattleUnit("野兔", 20, 5, 2, 120, 
            new BattleSkill[]{
                BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1)
            }, BattleUnit.TYPE_ENEMY);
        
        // 3. 技能演示
        demonstrateSkills(player, enemy);
    }
    
    /**
     * 显示所有技能信息
     */
    private static void displayAllSkills() {
        System.out.println("可用技能列表：");
        for (BattleSkillManager.SkillType skillType : BattleSkillManager.getAllSkillTypes()) {
            int maxLevel = BattleSkillManager.getMaxLevel(skillType);
            for (int level = 1; level <= maxLevel; level++) {
                String description = BattleSkillManager.getSkillDescription(skillType, level);
                System.out.println("- " + description);
            }
        }
    }
    
    /**
     * 演示技能效果
     */
    private static void demonstrateSkills(BattleUnit player, BattleUnit enemy) {
        System.out.println("\n1. 冲撞技能演示：");
        String result = BattleSkillManager.executeSkill(
            BattleSkillManager.SkillType.CHARGE, 2, player, enemy, null);
        System.out.println(result);
        
        System.out.println("\n2. 附毒技能演示：");
        result = BattleSkillManager.executeSkill(
            BattleSkillManager.SkillType.POISON, 1, player, enemy, null);
        System.out.println(result);
        
        System.out.println("\n3. 撕咬技能演示：");
        result = BattleSkillManager.executeSkill(
            BattleSkillManager.SkillType.BITE, 2, enemy, player, null);
        System.out.println(result);
        
        System.out.println("\n4. 震慑技能演示：");
        result = BattleSkillManager.executeSkill(
            BattleSkillManager.SkillType.STUN, 3, player, enemy, null);
        System.out.println(result);
        
        // 检查效果
        System.out.println("\n=== 效果检查 ===");
        checkEffects(player, enemy);
    }
    
    /**
     * 检查单位效果
     */
    private static void checkEffects(BattleUnit player, BattleUnit enemy) {
        String playerEffects = BuffEffectManager.getEffectsDescription(player.getName());
        String enemyEffects = BuffEffectManager.getEffectsDescription(enemy.getName());
        
        System.out.println("玩家当前效果: " + playerEffects);
        System.out.println("敌人当前效果: " + enemyEffects);
        
        // 模拟回合开始
        System.out.println("\n=== 回合开始处理 ===");
        BuffEffectManager.processEffectsAtRoundStart(player.getName());
        BuffEffectManager.processEffectsAtRoundStart(enemy.getName());
        
        System.out.println("回合结束后的效果状态：");
        playerEffects = BuffEffectManager.getEffectsDescription(player.getName());
        enemyEffects = BuffEffectManager.getEffectsDescription(enemy.getName());
        
        System.out.println("玩家当前效果: " + playerEffects);
        System.out.println("敌人当前效果: " + enemyEffects);
    }
}