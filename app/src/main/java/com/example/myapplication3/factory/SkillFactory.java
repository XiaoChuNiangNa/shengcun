package com.example.myapplication3.factory;

import com.example.myapplication3.BattleSkill;
import com.example.myapplication3.BattleSkillManager;
import com.example.myapplication3.config.SkillConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 技能工厂类
 * 负责动态创建技能，避免硬编码
 */
public class SkillFactory {
    
    private static final Random random = new Random();
    
    /**
     * 根据技能配置创建技能数组
     * 确保召唤技能放在固定位置(SUMMON_SKILL_INDEX = 3)
     */
    public static BattleSkill[] createSkillsFromConfig(
            BattleSkillManager.SkillType[] skillTypes, 
            int[] skillLevels) {
        
        if (skillTypes == null || skillLevels == null) {
            return new BattleSkill[0];
        }
        
        // 创建技能数组，确保长度至少为4以容纳召唤技能在索引3位置
        // 但最多不超过技能类型数组长度+1
        int maxLength = Math.max(skillTypes.length + 1, 4);
        BattleSkill[] skills = new BattleSkill[maxLength];
        
        // 查找并处理召唤技能
        BattleSkill summonSkill = null;
        int summonLevel = 1;
        int nonSummonSkillCount = 0;
        
        for (int i = 0; i < skillTypes.length && i < skillLevels.length; i++) {
            BattleSkillManager.SkillType skillType = skillTypes[i];
            int skillLevel = skillLevels[i];
            
            // 如果是召唤技能，暂时保存
            if (skillType == BattleSkillManager.SkillType.SUMMON) {
                summonSkill = BattleSkillManager.createSkill(skillType, skillLevel);
                summonLevel = skillLevel;
                // 确保将召唤技能放在固定位置（索引3）
                skills[3] = summonSkill;
            } else {
                // 为非召唤技能创建并放置
                BattleSkill skill = BattleSkillManager.createSkill(skillType, skillLevel);
                if (skill != null) {
                    // 为非召唤技能找到合适的位置
                    if (nonSummonSkillCount < 3) {
                        // 前三个非召唤技能放在索引0、1、2
                        skills[nonSummonSkillCount] = skill;
                    } else {
                        // 后续非召唤技能从索引4开始放置
                        skills[4 + (nonSummonSkillCount - 3)] = skill;
                    }
                    nonSummonSkillCount++;
                }
            }
        }
        
        // 检查是否有至少一个有效技能
        boolean hasValidSkills = false;
        for (BattleSkill skill : skills) {
            if (skill != null) {
                hasValidSkills = true;
                break;
            }
        }
        
        // 如果没有有效技能，返回空数组
        if (!hasValidSkills) {
            return new BattleSkill[0];
        }
        
        // 保持数组结构不变，直接返回
        return skills;
    }
    
    /**
     * 为玩家创建技能（基于等级）
     */
    public static BattleSkill[] createPlayerSkills(int playerLevel) {
        SkillConfig.PlayerSkillConfig config = SkillConfig.getPlayerConfig(playerLevel);
        return createSkillsFromConfig(config.skillTypes, config.skillLevels);
    }
    
    /**
     * 为怪物创建技能（基于怪物名称）
     */
    public static BattleSkill[] createEnemySkills(String enemyName) {
        SkillConfig.EnemySkillConfig config = SkillConfig.getEnemyConfig(enemyName);
        return createEnemySkillsFromConfig(config);
    }
    
    /**
     * 根据敌方配置创建技能（考虑概率）
     */
    private static BattleSkill[] createEnemySkillsFromConfig(
            SkillConfig.EnemySkillConfig config) {
        
        if (config == null) {
            // 默认给予1个基础技能
            return new BattleSkill[]{
                BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1)
            };
        }
        
        List<BattleSkill> skillList = new ArrayList<>();
        
        // 根据概率决定是否包含每个技能
        for (int i = 0; i < config.skillTypes.length; i++) {
            if (random.nextDouble() < config.probability[i]) {
                BattleSkill skill = BattleSkillManager.createSkill(
                    config.skillTypes[i], config.skillLevels[i]);
                if (skill != null) {
                    skillList.add(skill);
                }
            }
        }
        
        // 确保至少有一个技能
        if (skillList.isEmpty()) {
            BattleSkill fallbackSkill = BattleSkillManager.createSkill(
                BattleSkillManager.SkillType.BITE, 1);
            if (fallbackSkill != null) {
                skillList.add(fallbackSkill);
            }
        }
        
        return skillList.toArray(new BattleSkill[0]);
    }
    
    /**
     * 创建随机技能组合
     */
    public static BattleSkill[] createRandomSkills(int maxSkills, int maxLevel) {
        List<BattleSkillManager.SkillType> availableTypes = SkillConfig.getAllAvailableSkillTypes();
        List<BattleSkill> skillList = new ArrayList<>();
        
        // 随机选择技能类型和等级
        int skillCount = Math.min(maxSkills, availableTypes.size());
        
        while (skillList.size() < skillCount && !availableTypes.isEmpty()) {
            int randomIndex = random.nextInt(availableTypes.size());
            BattleSkillManager.SkillType skillType = availableTypes.get(randomIndex);
            
            int level = random.nextInt(maxLevel) + 1;
            BattleSkill skill = BattleSkillManager.createSkill(skillType, level);
            
            if (skill != null) {
                skillList.add(skill);
                availableTypes.remove(randomIndex); // 避免重复
            }
        }
        
        return skillList.toArray(new BattleSkill[0]);
    }
    
    /**
     * 创建自定义技能组合
     */
    public static BattleSkill[] createCustomSkills(
            BattleSkillManager.SkillType[] skillTypes, 
            int level) {
        
        if (skillTypes == null) {
            return new BattleSkill[0];
        }
        
        List<BattleSkill> skillList = new ArrayList<>();
        
        for (BattleSkillManager.SkillType skillType : skillTypes) {
            BattleSkill skill = BattleSkillManager.createSkill(skillType, level);
            if (skill != null) {
                skillList.add(skill);
            }
        }
        
        return skillList.toArray(new BattleSkill[0]);
    }
    
    /**
     * 检查技能数组的有效性
     */
    public static boolean validateSkillArray(BattleSkill[] skills) {
        if (skills == null) return false;
        
        int validCount = 0;
        for (BattleSkill skill : skills) {
            if (skill != null) {
                validCount++;
            }
        }
        
        return validCount > 0;
    }
    
    /**
     * 获取技能数组中有效技能的数量
     */
    public static int getValidSkillCount(BattleSkill[] skills) {
        if (skills == null) return 0;
        
        int count = 0;
        for (BattleSkill skill : skills) {
            if (skill != null) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * 过滤掉无效技能
     */
    public static BattleSkill[] filterValidSkills(BattleSkill[] skills) {
        if (skills == null) return new BattleSkill[0];
        
        List<BattleSkill> validSkills = new ArrayList<>();
        for (BattleSkill skill : skills) {
            if (skill != null) {
                validSkills.add(skill);
            }
        }
        
        return validSkills.toArray(new BattleSkill[0]);
    }
}