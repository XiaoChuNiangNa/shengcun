package com.example.myapplication3.config;

import com.example.myapplication3.BattleSkillManager;
import java.util.Arrays;
import java.util.List;

/**
 * 技能配置管理类
 * 集中管理不同类型的技能配置，避免硬编码
 */
public class SkillConfig {
    
    /**
     * 玩家默认技能配置
     */
    public static class PlayerSkillConfig {
        public final BattleSkillManager.SkillType[] skillTypes;
        public final int[] skillLevels;
        
        public PlayerSkillConfig(BattleSkillManager.SkillType[] skillTypes, int[] skillLevels) {
            this.skillTypes = skillTypes;
            this.skillLevels = skillLevels;
        }
    }
    
    // 预定义的玩家技能配置
    public static final PlayerSkillConfig[] PLAYER_CONFIGS = {
        // 新手配置（基础技能）
        new PlayerSkillConfig(
            new BattleSkillManager.SkillType[]{
                BattleSkillManager.SkillType.CHARGE,
                BattleSkillManager.SkillType.ESCAPE
            },
            new int[]{1, 1}
        ),
        
        // 战斗配置（进阶技能）
        new PlayerSkillConfig(
            new BattleSkillManager.SkillType[]{
                BattleSkillManager.SkillType.CHARGE,
                BattleSkillManager.SkillType.BITE,
                BattleSkillManager.SkillType.SUMMON,
                BattleSkillManager.SkillType.ESCAPE
            },
            new int[]{2, 1, 1, 1}
        ),
        
        // 精英配置（高级技能）
        new PlayerSkillConfig(
            new BattleSkillManager.SkillType[]{
                BattleSkillManager.SkillType.CHARGE,
                BattleSkillManager.SkillType.POISON,
                BattleSkillManager.SkillType.BITE,
                BattleSkillManager.SkillType.SUMMON,
                BattleSkillManager.SkillType.STUN,
                BattleSkillManager.SkillType.ESCAPE
            },
            new int[]{3, 2, 2, 2, 2, 1}
        ),
        
        // 大师配置（全部技能）
        new PlayerSkillConfig(
            new BattleSkillManager.SkillType[]{
                BattleSkillManager.SkillType.CHARGE,
                BattleSkillManager.SkillType.POISON,
                BattleSkillManager.SkillType.BITE,
                BattleSkillManager.SkillType.SUMMON,
                BattleSkillManager.SkillType.STUN,
                BattleSkillManager.SkillType.ESCAPE,
                BattleSkillManager.SkillType.AOE
            },
            new int[]{3, 3, 3, 3, 3, 1, 2}
        )
    };
    
    /**
     * 获取指定等级的玩家技能配置
     */
    public static PlayerSkillConfig getPlayerConfig(int playerLevel) {
        if (playerLevel <= 3) {
            return PLAYER_CONFIGS[0]; // 新手
        } else if (playerLevel <= 7) {
            return PLAYER_CONFIGS[1]; // 战斗
        } else if (playerLevel <= 12) {
            return PLAYER_CONFIGS[2]; // 精英
        } else {
            return PLAYER_CONFIGS[3]; // 大师
        }
    }
    
    /**
     * 敌方技能配置
     */
    public static class EnemySkillConfig {
        public final String enemyName;
        public final BattleSkillManager.SkillType[] skillTypes;
        public final int[] skillLevels;
        public final double[] probability; // 技能出现概率
        
        public EnemySkillConfig(String enemyName, 
                           BattleSkillManager.SkillType[] skillTypes, 
                           int[] skillLevels, 
                           double[] probability) {
            this.enemyName = enemyName;
            this.skillTypes = skillTypes;
            this.skillLevels = skillLevels;
            this.probability = probability;
        }
    }
    
    // 预定义的敌方技能配置
    public static final EnemySkillConfig[] ENEMY_CONFIGS = {
        // 普通怪物
        new EnemySkillConfig(
            "野猪",
            new BattleSkillManager.SkillType[]{
                BattleSkillManager.SkillType.CHARGE,
                BattleSkillManager.SkillType.BITE
            },
            new int[]{1, 1},
            new double[]{0.8, 0.6}
        ),
        
        // 毒系怪物
        new EnemySkillConfig(
            "毒蛇",
            new BattleSkillManager.SkillType[]{
                BattleSkillManager.SkillType.POISON,
                BattleSkillManager.SkillType.BITE,
                BattleSkillManager.SkillType.ESCAPE
            },
            new int[]{1, 1, 1},
            new double[]{0.9, 0.5, 0.7}
        ),
        
        // Boss怪物
        new EnemySkillConfig(
            "暗影领主",
            new BattleSkillManager.SkillType[]{
                BattleSkillManager.SkillType.CHARGE,
                BattleSkillManager.SkillType.POISON,
                BattleSkillManager.SkillType.BITE,
                BattleSkillManager.SkillType.SUMMON,
                BattleSkillManager.SkillType.STUN,
                BattleSkillManager.SkillType.AOE
            },
            new int[]{3, 3, 3, 2, 2, 2},
            new double[]{0.9, 0.8, 0.9, 0.7, 0.8, 0.6}
        )
    };
    
    /**
     * 根据怪物名称获取敌方技能配置
     */
    public static EnemySkillConfig getEnemyConfig(String enemyName) {
        if (enemyName == null) return null;
        
        for (EnemySkillConfig config : ENEMY_CONFIGS) {
            if (enemyName.equals(config.enemyName)) {
                return config;
            }
        }
        
        // 默认返回普通怪物配置
        return ENEMY_CONFIGS[0];
    }
    
    /**
     * 获取所有可用的技能类型
     */
    public static List<BattleSkillManager.SkillType> getAllAvailableSkillTypes() {
        return Arrays.asList(
            BattleSkillManager.SkillType.CHARGE,
            BattleSkillManager.SkillType.POISON,
            BattleSkillManager.SkillType.BITE,
            BattleSkillManager.SkillType.SUMMON,
            BattleSkillManager.SkillType.STUN,
            BattleSkillManager.SkillType.ESCAPE,
            BattleSkillManager.SkillType.AOE
        );
    }
    
    /**
     * 技能描述
     */
    public static String getSkillDescription(BattleSkillManager.SkillType skillType) {
        switch (skillType) {
            case CHARGE: return "强力冲撞，造成额外伤害";
            case POISON: return "附毒攻击，持续造成伤害";
            case BITE: return "撕咬攻击，基础伤害较高";
            case SUMMON: return "召唤助手协助战斗";
            case STUN: return "震慑攻击，可能使敌人昏迷";
            case ESCAPE: return "快速脱离战斗";
            case AOE: return "范围攻击，对多个敌人造成伤害";
            default: return "未知技能";
        }
    }
}