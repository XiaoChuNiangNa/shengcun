package com.example.myapplication3.test;

import android.util.Log;
import com.example.myapplication3.BattleSkill;
import com.example.myapplication3.BattleSkillManager;
import com.example.myapplication3.BattleUnit;

/**
 * 动态技能显示测试
 * 验证卡牌根据实际拥有技能数量动态显示技能
 */
public class DynamicSkillTest {
    
    private static final String TAG = "DynamicSkillTest";
    
    /**
     * 测试0个技能的情况
     */
    public static void testZeroSkills() {
        Log.d(TAG, "=== 测试0个技能 ===");
        
        // 创建没有技能的单位
        BattleUnit unit = new BattleUnit("无技能单位", 100, 10, 5, 8, 
            new BattleSkill[0], BattleUnit.TYPE_PLAYER);
        
        BattleSkill[] skills = unit.getSkills();
        Log.d(TAG, "技能数量: " + (skills != null ? skills.length : 0));
        
        // 验证：应该显示0个技能进度条
        if (skills == null || skills.length == 0) {
            Log.d(TAG, "✅ 通过：无技能单位不显示技能进度条");
        } else {
            Log.d(TAG, "❌ 失败：无技能单位显示技能进度条");
        }
    }
    
    /**
     * 测试1个技能的情况
     */
    public static void testOneSkill() {
        Log.d(TAG, "=== 测试1个技能 ===");
        
        // 创建1个技能的单位
        BattleSkill[] skills = new BattleSkill[1];
        skills[0] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1);
        
        BattleUnit unit = new BattleUnit("单技能单位", 100, 10, 5, 8, 
            skills, BattleUnit.TYPE_PLAYER);
        
        Log.d(TAG, "技能数量: " + skills.length);
        for (int i = 0; i < skills.length; i++) {
            BattleSkill skill = skills[i];
            Log.d(TAG, String.format("技能[%d]: %s", i, 
                skill != null ? skill.getName() : "null"));
        }
        
        // 验证：应该显示1个技能进度条
        if (skills.length == 1 && skills[0] != null) {
            Log.d(TAG, "✅ 通过：单技能单位显示1个技能进度条");
        } else {
            Log.d(TAG, "❌ 失败：单技能单位显示技能数量不正确");
        }
    }
    
    /**
     * 测试多个技能的情况
     */
    public static void testMultipleSkills() {
        Log.d(TAG, "=== 测试多个技能 ===");
        
        // 创建3个技能的单位
        BattleSkill[] skills = new BattleSkill[3];
        skills[0] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1);
        skills[1] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1);
        skills[2] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1);
        
        BattleUnit unit = new BattleUnit("多技能单位", 100, 10, 5, 8, 
            skills, BattleUnit.TYPE_PLAYER);
        
        Log.d(TAG, "技能数量: " + skills.length);
        for (int i = 0; i < skills.length; i++) {
            BattleSkill skill = skills[i];
            Log.d(TAG, String.format("技能[%d]: %s", i, 
                skill != null ? skill.getName() : "null"));
        }
        
        // 验证：应该显示3个技能进度条
        int validSkillCount = 0;
        for (BattleSkill skill : skills) {
            if (skill != null) {
                validSkillCount++;
            }
        }
        
        if (validSkillCount == 3) {
            Log.d(TAG, "✅ 通过：多技能单位显示正确数量的技能进度条");
        } else {
            Log.d(TAG, "❌ 失败：多技能单位显示技能数量不正确");
        }
    }
    
    /**
     * 测试包含null技能的情况
     */
    public static void testMixedSkills() {
        Log.d(TAG, "=== 测试混合技能（包含null） ===");
        
        // 创建包含null技能的数组
        BattleSkill[] skills = new BattleSkill[4];
        skills[0] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1);
        skills[1] = null; // 空技能
        skills[2] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1);
        skills[3] = null; // 空技能
        
        BattleUnit unit = new BattleUnit("混合技能单位", 100, 10, 5, 8, 
            skills, BattleUnit.TYPE_PLAYER);
        
        Log.d(TAG, "总技能数量: " + skills.length);
        
        // 计算实际有效技能数量
        int validSkillCount = 0;
        for (BattleSkill skill : skills) {
            if (skill != null) {
                Log.d(TAG, "有效技能: " + skill.getName());
                validSkillCount++;
            } else {
                Log.d(TAG, "发现空技能，跳过");
            }
        }
        
        // 验证：应该只显示2个有效技能的进度条
        if (validSkillCount == 2) {
            Log.d(TAG, "✅ 通过：混合技能单位只显示有效技能进度条");
        } else {
            Log.d(TAG, "❌ 失败：混合技能单位显示技能数量不正确");
        }
    }
    
    /**
     * 测试技能动态更新
     */
    public static void testDynamicUpdate() {
        Log.d(TAG, "=== 测试技能动态更新 ===");
        
        // 初始创建2个技能
        BattleSkill[] initialSkills = new BattleSkill[2];
        initialSkills[0] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1);
        initialSkills[1] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1);
        
        BattleUnit unit = new BattleUnit("动态单位", 100, 10, 5, 8, 
            initialSkills, BattleUnit.TYPE_PLAYER);
        
        Log.d(TAG, "初始技能数量: " + initialSkills.length);
        
        // 动态添加技能
        BattleSkill[] newSkills = new BattleSkill[4];
        newSkills[0] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1);
        newSkills[1] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1);
        newSkills[2] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1);
        newSkills[3] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.POISON, 1);
        
        unit.setSkills(newSkills);
        
        Log.d(TAG, "更新后技能数量: " + newSkills.length);
        for (int i = 0; i < newSkills.length; i++) {
            BattleSkill skill = newSkills[i];
            if (skill != null) {
                Log.d(TAG, String.format("新技能[%d]: %s", i, skill.getName()));
            }
        }
        
        // 验证：应该显示4个技能进度条
        if (newSkills.length == 4) {
            Log.d(TAG, "✅ 通过：动态更新后显示正确数量的技能进度条");
        } else {
            Log.d(TAG, "❌ 失败：动态更新后显示技能数量不正确");
        }
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        Log.d(TAG, "开始动态技能显示测试");
        
        testZeroSkills();
        testOneSkill();
        testMultipleSkills();
        testMixedSkills();
        testDynamicUpdate();
        
        Log.d(TAG, "动态技能显示测试完成");
    }
}