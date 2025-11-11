package com.example.myapplication3;

/**
 * Buff效果系统演示类
 * 展示如何使用BuffEffectManager来应用和管理各种状态效果
 */
public class BuffEffectDemo {
    
    /**
     * 演示如何使用Buff效果系统
     */
    public static void demonstrateBuffEffects() {
        // 创建一些示例单位
        BattleUnit player = new BattleUnit("玩家英雄", 100, 20, 10, new int[]{3, 5, 7});
        BattleUnit enemy = new BattleUnit("剧毒怪物", 80, 15, 8, new int[]{2, 4, 6});
        
        System.out.println("=== Buff效果系统演示 ===");
        System.out.println("初始状态:");
        System.out.println(player.getName() + " - 生命: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
        System.out.println(enemy.getName() + " - 生命: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
        
        // 演示1: 应用剧毒效果到敌人
        System.out.println("\n=== 演示1: 剧毒效果 ===");
        applyPoisonToEnemy(player, enemy);
        
        // 演示2: 应用流血效果到玩家
        System.out.println("\n=== 演示2: 流血效果 ===");
        applyBleedingToPlayer(enemy, player);
        
        // 演示3: 应用眩晕效果到敌人
        System.out.println("\n=== 演示3: 眩晕效果 ===");
        applyStunToEnemy(player, enemy);
        
        // 演示4: 回合处理效果
        System.out.println("\n=== 演示4: 回合效果处理 ===");
        simulateRoundEffects(player, enemy);
        
        System.out.println("\n=== 演示完成 ===");
    }
    
    /**
     * 演示剧毒效果
     */
    private static void applyPoisonToEnemy(BattleUnit player, BattleUnit enemy) {
        // 应用剧毒效果：3层，持续5回合
        BuffEffectManager.addEffect(enemy.getName(), 
            BuffEffectManager.EFFECT_POISON, 3, 5, player.getName());
        
        System.out.println(player.getName() + " 对 " + enemy.getName() + " 施加剧毒效果");
        System.out.println("当前效果: " + BuffEffectManager.getEffectsDescription(enemy.getName()));
    }
    
    /**
     * 演示流血效果
     */
    private static void applyBleedingToPlayer(BattleUnit enemy, BattleUnit player) {
        // 应用流血效果：2层，持续3回合
        BuffEffectManager.addEffect(player.getName(), 
            BuffEffectManager.EFFECT_BLEEDING, 2, 3, enemy.getName());
        
        System.out.println(enemy.getName() + " 对 " + player.getName() + " 施加流血效果");
        System.out.println("当前效果: " + BuffEffectManager.getEffectsDescription(player.getName()));
        
        // 演示流血伤害
        int bleedingDamage = BuffEffectManager.processBleedingOnDamageTaken(player.getName());
        System.out.println("下次攻击时流血伤害: " + bleedingDamage + " 点");
    }
    
    /**
     * 演示眩晕效果
     */
    private static void applyStunToEnemy(BattleUnit player, BattleUnit enemy) {
        // 应用眩晕效果：1层，持续2回合
        BuffEffectManager.applyStunToUnit(enemy.getName(), enemy, player.getName(), 2);
        
        System.out.println(player.getName() + " 对 " + enemy.getName() + " 施加眩晕效果");
        System.out.println("当前效果: " + BuffEffectManager.getEffectsDescription(enemy.getName()));
        
        // 检查是否被眩晕
        boolean isStunned = BuffEffectManager.isUnitStunned(enemy.getName());
        System.out.println(enemy.getName() + " 是否被眩晕: " + isStunned);
    }
    
    /**
     * 模拟回合效果处理
     */
    private static void simulateRoundEffects(BattleUnit player, BattleUnit enemy) {
        System.out.println("--- 第1回合开始 ---");
        
        // 处理玩家单位的剧毒效果
        processUnitEffects(player, "第1回合");
        
        // 处理敌人单位的剧毒效果
        processUnitEffects(enemy, "第1回合");
        
        System.out.println("--- 第2回合开始 ---");
        
        // 处理玩家单位的剧毒效果
        processUnitEffects(player, "第2回合");
        
        // 处理敌人单位的剧毒效果
        processUnitEffects(enemy, "第2回合");
        
        System.out.println("战斗结束后的状态:");
        System.out.println(player.getName() + " - 生命: " + player.getCurrentHealth() + "/" + player.getMaxHealth());
        System.out.println(enemy.getName() + " - 生命: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
        System.out.println("玩家剩余效果: " + BuffEffectManager.getEffectsDescription(player.getName()));
        System.out.println("敌人剩余效果: " + BuffEffectManager.getEffectsDescription(enemy.getName()));
        
        // 清除所有效果
        BuffEffectManager.clearAllEffects();
        System.out.println("清除所有效果后:");
        System.out.println("玩家剩余效果: " + BuffEffectManager.getEffectsDescription(player.getName()));
        System.out.println("敌人剩余效果: " + BuffEffectManager.getEffectsDescription(enemy.getName()));
    }
    
    /**
     * 处理单个单位的回合效果
     */
    private static void processUnitEffects(BattleUnit unit, String roundLabel) {
        String unitName = unit.getName();
        
        System.out.println("处理 " + unitName + " 的效果:");
        System.out.println("当前效果: " + BuffEffectManager.getEffectsDescription(unitName));
        
        // 处理回合开始时的效果（如剧毒伤害）
        java.util.Map<String, Integer> damageResults = BuffEffectManager.processEffectsAtRoundStart(unitName);
        if (damageResults != null && damageResults.containsKey("POISON_DAMAGE")) {
            int poisonDamage = damageResults.get("POISON_DAMAGE");
            if (poisonDamage > 0) {
                unit.takeDamage(poisonDamage);
                System.out.println(unitName + " 受到 " + poisonDamage + " 点剧毒伤害");
            }
        }
        
        System.out.println(unitName + " 剩余生命: " + unit.getCurrentHealth());
    }
}