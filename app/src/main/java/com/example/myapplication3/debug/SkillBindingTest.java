package com.example.myapplication3.debug;

import android.util.Log;
import com.example.myapplication3.BattleSkill;
import com.example.myapplication3.BattleSkillManager;
import com.example.myapplication3.BattleUnit;

/**
 * 技能绑定测试类
 * 用于调试技能显示和冷却绑定问题
 */
public class SkillBindingTest {
    
    private static final String TAG = "SkillBindingTest";
    
    /**
     * 测试技能创建和绑定
     */
    public static void testSkillBinding() {
        Log.d(TAG, "=== 开始技能绑定测试 ===");
        
        // 测试玩家技能创建
        BattleSkill[] playerSkills = new BattleSkill[6];
        playerSkills[0] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 2);
        playerSkills[1] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.POISON, 1);
        playerSkills[2] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1);
        playerSkills[3] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.SUMMON, 1);
        playerSkills[4] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.STUN, 2);
        playerSkills[5] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1);
        
        // 创建玩家单位
        BattleUnit playerUnit = new BattleUnit("测试玩家", 100, 10, 5, 8, playerSkills, BattleUnit.TYPE_PLAYER);
        
        // 打印技能信息
        Log.d(TAG, "玩家技能数量: " + playerSkills.length);
        for (int i = 0; i < playerSkills.length; i++) {
            BattleSkill skill = playerSkills[i];
            if (skill != null) {
                Log.d(TAG, String.format("技能[%d]: %s, 类型: %s, 冷却: %d, 当前冷却: %d", 
                    i, skill.getName(), skill.skillType, skill.getCooldown(), skill.getCurrentCooldown()));
            } else {
                Log.d(TAG, "技能[" + i + "]: null");
            }
        }
        
        // 测试技能冷却更新
        Log.d(TAG, "=== 测试技能冷却更新 ===");
        playerUnit.updateCooldowns();
        
        // 再次打印技能信息
        for (int i = 0; i < playerSkills.length; i++) {
            BattleSkill skill = playerSkills[i];
            if (skill != null) {
                Log.d(TAG, String.format("更新后技能[%d]: %s, 当前冷却: %d", 
                    i, skill.getName(), skill.getCurrentCooldown()));
            }
        }
        
        Log.d(TAG, "=== 技能绑定测试完成 ===");
    }
    
    /**
     * 测试技能可用性检查
     */
    public static void testSkillAvailability() {
        Log.d(TAG, "=== 开始技能可用性测试 ===");
        
        // 创建测试技能
        BattleSkill[] skills = new BattleSkill[3];
        skills[0] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 2);
        skills[1] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1);
        skills[2] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1);
        
        BattleUnit unit = new BattleUnit("测试单位", 100, 10, 5, 8, skills, BattleUnit.TYPE_PLAYER);
        
        // 模拟技能使用
        Log.d(TAG, "初始状态:");
        printSkillStatus(unit);
        
        // 使用第一个技能
        unit.useSkill(0);
        Log.d(TAG, "使用技能0后:");
        printSkillStatus(unit);
        
        // 更新冷却
        unit.updateCooldowns();
        Log.d(TAG, "更新冷却后:");
        printSkillStatus(unit);
        
        Log.d(TAG, "=== 技能可用性测试完成 ===");
    }
    
    /**
     * 打印技能状态
     */
    private static void printSkillStatus(BattleUnit unit) {
        BattleSkill[] skills = unit.getSkills();
        if (skills != null) {
            for (int i = 0; i < skills.length; i++) {
                BattleSkill skill = skills[i];
                if (skill != null) {
                    boolean canUse = unit.canUseSkill(i);
                    Log.d(TAG, String.format("  技能[%d]: %s - %s (冷却: %d/%d)", 
                        i, skill.getName(), canUse ? "可用" : "冷却中", 
                        skill.getCurrentCooldown(), skill.getCooldown()));
                }
            }
        }
    }
    
    /**
     * 测试技能类型识别
     */
    public static void testSkillTypeRecognition() {
        Log.d(TAG, "=== 开始技能类型识别测试 ===");
        
        // 测试SUMMON技能类型
        BattleSkill summonSkill = BattleSkillManager.createSkill(BattleSkillManager.SkillType.SUMMON, 1);
        if (summonSkill != null) {
            Log.d(TAG, String.format("群攻技能: %s, 类型: %s", 
                summonSkill.getName(), summonSkill.skillType));
            
            // 检查是否正确识别为SUMMON类型
            boolean isSummonType = (summonSkill.skillType == BattleSkillManager.SkillType.SUMMON);
            Log.d(TAG, "正确识别为群攻类型: " + isSummonType);
        }
        
        Log.d(TAG, "=== 技能类型识别测试完成 ===");
    }
}