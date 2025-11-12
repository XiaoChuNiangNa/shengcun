package com.example.myapplication3;

import java.util.*;

/**
 * 战斗技能管理器：统一管理所有战斗技能
 * 包括逃跑、冲撞、附毒、撕咬、群攻、震慑、掠夺等技能
 */
public class BattleSkillManager {
    
    // 技能类型枚举
    public enum SkillType {
        ESCAPE(1, "逃跑"),        // 逃跑
        CHARGE(2, "冲撞"),        // 冲撞
        POISON(3, "附毒"),        // 附毒
        BITE(4, "撕咬"),         // 撕咬
        SUMMON(5, "群攻"),       // 群攻
        STUN(6, "震慑"),         // 震慑
        PLUNDER(7, "掠夺"),       // 掠夺
        AOE(8, "范围攻击"),      // 范围攻击
        LOOT(9, "拾取")         // 拾取（掉落物增强）
        ;
        
        private final int id;
        private final String name;
        
        SkillType(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        
        public static SkillType getById(int id) {
            for (SkillType type : values()) {
                if (type.id == id) return type;
            }
            return null;
        }
    }
    
    // 技能等级配置
    public static class SkillLevel {
        public int level;
        public int cooldown;      // 冷却时间（回合数）
        public String effect;      // 技能效果描述
        
        public SkillLevel(int level, int cooldown, String effect) {
            this.level = level;
            this.cooldown = cooldown;
            this.effect = effect;
        }
    }
    
    // 技能配置数据
    private static final Map<SkillType, List<SkillLevel>> SKILL_CONFIGS = new HashMap<>();
    
    static {
        // 初始化技能配置
        initSkillConfigs();
    }
    
    /**
     * 初始化技能配置（回合制冷却）
     */
    private static void initSkillConfigs() {
        // 逃跑技能 - 冷却改为回合数
        SKILL_CONFIGS.put(SkillType.ESCAPE, Arrays.asList(
            new SkillLevel(1, 6, "50%概率逃跑"),
            new SkillLevel(2, 5, "75%概率逃跑"),
            new SkillLevel(3, 4, "100%概率逃跑")
        ));
        
        // 冲撞技能 - 冷却改为回合数
        SKILL_CONFIGS.put(SkillType.CHARGE, Arrays.asList(
            new SkillLevel(1, 3, "对敌军单体造成攻击*1.5的伤害"),
            new SkillLevel(2, 2, "对敌军单体造成攻击*2的伤害"),
            new SkillLevel(3, 1, "对敌军单体造成攻击*2.5的伤害")
        ));
        
        // 附毒技能 - 冷却改为回合数
        SKILL_CONFIGS.put(SkillType.POISON, Arrays.asList(
            new SkillLevel(1, 4, "使敌军单体附加1层剧毒效果"),
            new SkillLevel(2, 3, "使敌军单体附加2层剧毒效果"),
            new SkillLevel(3, 2, "使敌军单体附加3层剧毒效果")
        ));
        
        // 撕咬技能 - 冷却改为回合数
        SKILL_CONFIGS.put(SkillType.BITE, Arrays.asList(
            new SkillLevel(1, 3, "使敌军单体附加1层流血效果"),
            new SkillLevel(2, 2, "使敌军单体附加2层流血效果"),
            new SkillLevel(3, 1, "使敌军单体附加3层流血效果")
        ));
        
        // 群攻技能 - 冷却改为回合数
        SKILL_CONFIGS.put(SkillType.SUMMON, Arrays.asList(
            new SkillLevel(1, 6, "召唤一个无技能，召唤者50%生命值的分身"),
            new SkillLevel(2, 5, "召唤一个无技能，召唤者75%生命值的分身"),
            new SkillLevel(3, 4, "召唤一个无技能，召唤者100%生命值的分身")
        ));
        
        // 震慑技能 - 冷却改为回合数，持续时间改为回合数
        SKILL_CONFIGS.put(SkillType.STUN, Arrays.asList(
            new SkillLevel(1, 4, "使敌人眩晕，持续1回合"),
            new SkillLevel(2, 3, "使敌人眩晕，持续2回合"),
            new SkillLevel(3, 2, "使敌人眩晕，持续3回合")
        ));
        
        // 掠夺技能 - 按照需求配置
        SKILL_CONFIGS.put(SkillType.PLUNDER, Arrays.asList(
            new SkillLevel(1, 6, "若为敌人则随机抢夺玩家背包1个物资，若为玩家则随机获得2个普通资源"),
            new SkillLevel(2, 5, "若为敌人则随机抢夺玩家背包2个物资，若为玩家则随机获得2个普通资源"),
            new SkillLevel(3, 4, "若为敌人则随机抢夺玩家背包3个物资，若为玩家则随机获得3个普通资源")
        ));
        
        // 范围攻击技能
        SKILL_CONFIGS.put(SkillType.AOE, Arrays.asList(
            new SkillLevel(1, 5, "对全体敌人造成50%攻击力的伤害"),
            new SkillLevel(2, 4, "对全体敌人造成75%攻击力的伤害"),
            new SkillLevel(3, 3, "对全体敌人造成100%攻击力的伤害")
        ));
        
        // 掠夺技能（重命名，避免与PLUNDER冲突）
        SKILL_CONFIGS.put(SkillType.LOOT, Arrays.asList(
            new SkillLevel(1, 4, "使用技能后获得额外掉落物"),
            new SkillLevel(2, 3, "使用技能后获得更多额外掉落物"),
            new SkillLevel(3, 2, "使用技能后获得大量额外掉落物")
        ));
    }
    
    /**
     * 创建技能实例
     */
    public static BattleSkill createSkill(SkillType skillType, int level) {
        List<SkillLevel> levels = SKILL_CONFIGS.get(skillType);
        if (levels == null || level < 1 || level > levels.size()) {
            return null;
        }
        
        SkillLevel skillLevel = levels.get(level - 1);
        BattleSkill skill = new BattleSkill(skillType.getName(), skillLevel.cooldown);
        skill.skillType = skillType;
        skill.level = level;
        skill.description = skillLevel.effect;
        
        return skill;
    }
    
    /**
     * 获取技能的所有等级配置
     */
    public static List<SkillLevel> getSkillLevels(SkillType skillType) {
        return SKILL_CONFIGS.get(skillType);
    }
    
    /**
     * 获取技能的最大等级
     */
    public static int getMaxLevel(SkillType skillType) {
        List<SkillLevel> levels = SKILL_CONFIGS.get(skillType);
        return levels != null ? levels.size() : 0;
    }
    
    /**
     * 执行技能效果
     */
    public static String executeSkill(SkillType skillType, int level, BattleUnit caster, BattleUnit target, BattleActivity battleActivity) {
        List<SkillLevel> levels = SKILL_CONFIGS.get(skillType);
        if (levels == null || level < 1 || level > levels.size()) {
            return "技能执行失败";
        }
        
        SkillLevel skillLevel = levels.get(level - 1);
        StringBuilder result = new StringBuilder();
        
        switch (skillType) {
            case ESCAPE:
                // 逃跑技能
                double escapeChance = 0.5; // Lv1: 50%
                if (level == 2) escapeChance = 0.75; // Lv2: 75%
                else if (level == 3) escapeChance = 1.0; // Lv3: 100%
                
                if (Math.random() < escapeChance) {
                    if (battleActivity != null) {
                        // 通过公共方法结束战斗
                        battleActivity.endBattle(true);
                    }
                    result.append(caster.getName()).append(" 成功逃跑！");
                } else {
                    result.append(caster.getName()).append(" 逃跑失败！");
                }
                break;
                
            case CHARGE:
                // 冲撞技能
                double damageMultiplier = 1.5; // Lv1: 1.5x
                if (level == 2) damageMultiplier = 2.0; // Lv2: 2x
                else if (level == 3) damageMultiplier = 2.5; // Lv3: 2.5x
                
                int chargeDamage = (int)(caster.getAttack() * damageMultiplier);
                if (target != null) {
                    target.takeDamage(chargeDamage);
                    if (battleActivity != null) {
                        battleActivity.addDamageDealt(chargeDamage);
                    }
                    result.append(caster.getName()).append(" 使用冲撞对 ").append(target.getName())
                          .append(" 造成 ").append(chargeDamage).append(" 点伤害！");
                }
                break;
                
            case POISON:
                // 附毒技能
                int poisonStacks = level; // Lv1: 1层, Lv2: 2层, Lv3: 3层
                if (target != null) {
                    BuffEffectManager.addEffect(target.getName(), BuffEffectManager.EFFECT_POISON, 
                        poisonStacks, 5, caster.getName());
                    result.append(caster.getName()).append(" 对 ").append(target.getName())
                          .append(" 施加了 ").append(poisonStacks).append(" 层剧毒效果！");
                }
                break;
                
            case BITE:
                // 撕咬技能
                int bleedingStacks = level; // Lv1: 1层, Lv2: 2层, Lv3: 3层
                if (target != null) {
                    BuffEffectManager.addEffect(target.getName(), BuffEffectManager.EFFECT_BLEEDING, 
                        bleedingStacks, 3, caster.getName());
                    result.append(caster.getName()).append(" 对 ").append(target.getName())
                          .append(" 施加了 ").append(bleedingStacks).append(" 层流血效果！");
                }
                break;
                
            case SUMMON:
                // 群攻技能
                double summonHealthRatio = 0.5; // Lv1: 50%
                if (level == 2) summonHealthRatio = 0.75; // Lv2: 75%
                else if (level == 3) summonHealthRatio = 1.0; // Lv3: 100%
                
                if (battleActivity != null) {
                    // 创建分身 - 使用完整的构造函数
                    int[] defaultSkillCooldowns = {3, 5, 7}; // 默认技能冷却时间
                    BattleUnit clone = new BattleUnit(caster.getName() + "的分身", 
                        (int)(caster.getMaxHealth() * summonHealthRatio), 
                        caster.getAttack() / 2, caster.getDefense() / 2, 
                        defaultSkillCooldowns);
                    
                    // 添加到战场（简化实现）
                    result.append(caster.getName()).append(" 召唤了分身！");
                }
                break;
                
            case STUN:
                // 震慑技能（回合制持续时间）
                int stunDuration = 1; // Lv1: 1回合
                if (level == 2) stunDuration = 2; // Lv2: 2回合
                else if (level == 3) stunDuration = 3; // Lv3: 3回合
                
                if (target != null) {
                    // 眩晕效果改为回合制
                    BuffEffectManager.addEffect(target.getName(), BuffEffectManager.EFFECT_STUN, 
                        1, stunDuration, caster.getName());
                    
                    // 应用眩晕效果到目标单位
                    BuffEffectManager.applyStunToUnit(target.getName(), target, caster.getName(), stunDuration);
                    
                    result.append(caster.getName()).append(" 震慑了 ").append(target.getName())
                          .append(" ，使其眩晕 ").append(stunDuration).append(" 回合！");
                }
                break;
                
            case PLUNDER:
                // 掠夺技能
                int itemsToPlunder = level; // Lv1: 1个, Lv2: 2个, Lv3: 3个
                int itemsToGain = (level == 1 || level == 2) ? 2 : 3; // Lv1-2: 2个, Lv3: 3个
                
                // 判断施法者是敌人还是玩家
                boolean isEnemy = caster.getName().contains("怪物") || caster.getName().contains("敌人");
                
                if (isEnemy) {
                    // 敌人使用掠夺技能：抢夺玩家物资
                    result.append(caster.getName()).append(" 掠夺技能发动！");
                    result.append(" 成功抢夺玩家 ").append(itemsToPlunder).append(" 个物资！");
                    
                    // 实际实现需要与背包系统集成，这里模拟效果
                    if (battleActivity != null) {
                        // 调用BattleActivity的方法处理玩家物资损失
                        // battleActivity.onPlayerItemsPlundered(itemsToPlunder);
                    }
                } else {
                    // 玩家使用掠夺技能：获得普通资源
                    result.append(caster.getName()).append(" 掠夺技能发动！");
                    result.append(" 成功获得 ").append(itemsToGain).append(" 个普通资源！");
                    
                    // 实际实现需要与背包系统集成，这里模拟效果
                    if (battleActivity != null) {
                        // 调用BattleActivity的方法处理玩家资源获得
                        // battleActivity.onPlayerItemsGained(itemsToGain);
                    }
                }
                break;
                
            case AOE:
                // 范围攻击技能
                double aoeDamageMultiplier = 0.5; // Lv1: 50%
                if (level == 2) aoeDamageMultiplier = 0.75; // Lv2: 75%
                else if (level == 3) aoeDamageMultiplier = 1.0; // Lv3: 100%
                
                result.append(caster.getName()).append(" 发动范围攻击！");
                // 在实际实现中，这里应该对战场上的所有敌人造成伤害
                break;
                
            case LOOT:
                // 掠夺技能（掉落物增强）
                int lootBonus = level * 2; // Lv1: +2, Lv2: +4, Lv3: +6
                result.append(caster.getName()).append(" 使用技能，增加掉落物获得量！");
                // 在实际实现中，这里应该增加掉落物数量
                break;
        }
        
        return result.toString();
    }
    
    /**
     * 获取所有技能类型
     */
    public static List<SkillType> getAllSkillTypes() {
        return Arrays.asList(SkillType.values());
    }
    
    /**
     * 获取技能描述
     */
    public static String getSkillDescription(SkillType skillType, int level) {
        List<SkillLevel> levels = SKILL_CONFIGS.get(skillType);
        if (levels == null || level < 1 || level > levels.size()) {
            return "技能描述不可用";
        }
        
        SkillLevel skillLevel = levels.get(level - 1);
        return String.format("%s Lv%d (冷却:%d回合) - %s", 
            skillType.getName(), level, skillLevel.cooldown, skillLevel.effect);
    }
    

}