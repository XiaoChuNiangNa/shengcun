package com.example.myapplication3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.example.myapplication3.BattleSkill;
import com.example.myapplication3.BattleSkill;

/**
 * Buff效果管理器：统一管理战斗中的状态效果
 * 包括剧毒、流血、眩晕等效果
 */
public class BuffEffectManager {
    
    // 效果类型常量
    public static final String EFFECT_POISON = "POISON";      // 剧毒：每回合扣血
    public static final String EFFECT_BLEEDING = "BLEEDING";  // 流血：受击额外扣血
    public static final String EFFECT_STUN = "STUN";         // 眩晕：无法行动
    
    // 效果数据类
    public static class BuffEffect {
        public String type;         // 效果类型
        public int stacks;         // 层数（对于剧毒和流血）
        public int duration;       // 剩余持续时间（回合数）
        public String source;      // 效果来源（施法者名称）
        
        public BuffEffect(String type, int stacks, int duration, String source) {
            this.type = type;
            this.stacks = stacks;
            this.duration = duration;
            this.source = source;
        }
        
        @Override
        public String toString() {
            return String.format("%s(Lv%d) %d回合", getEffectDisplayName(), stacks, duration);
        }
        
        /**
         * 获取效果显示名称
         */
        public String getEffectDisplayName() {
            switch (type) {
                case EFFECT_POISON:
                    return "剧毒";
                case EFFECT_BLEEDING:
                    return "流血";
                case EFFECT_STUN:
                    return "眩晕";
                default:
                    return "未知效果";
            }
        }
    }
    
    // 存储单位的效果（key: 单位名称, value: 效果列表）
    private static final Map<String, List<BuffEffect>> UNIT_EFFECTS = new ConcurrentHashMap<>();
    
    /**
     * 添加效果到单位
     */
    public static void addEffect(String unitName, String effectType, int stacks, int duration, String source) {
        List<BuffEffect> effects = UNIT_EFFECTS.computeIfAbsent(unitName, k -> new ArrayList<>());
        
        // 检查是否已有相同类型的效果
        for (BuffEffect existingEffect : effects) {
            if (existingEffect.type.equals(effectType)) {
                // 已存在相同类型效果，叠加层数并刷新持续时间
                existingEffect.stacks += stacks;
                existingEffect.duration = Math.max(existingEffect.duration, duration);
                return;
            }
        }
        
        // 添加新效果
        effects.add(new BuffEffect(effectType, stacks, duration, source));
    }
    
    /**
     * 移除单位的所有效果
     */
    public static void removeAllEffects(String unitName) {
        UNIT_EFFECTS.remove(unitName);
    }
    
    /**
     * 移除特定类型的某个效果
     */
    public static void removeEffect(String unitName, String effectType) {
        List<BuffEffect> effects = UNIT_EFFECTS.get(unitName);
        if (effects != null) {
            effects.removeIf(effect -> effect.type.equals(effectType));
            if (effects.isEmpty()) {
                UNIT_EFFECTS.remove(unitName);
            }
        }
    }
    
    /**
     * 获取单位的所有效果
     */
    public static List<BuffEffect> getEffects(String unitName) {
        return UNIT_EFFECTS.getOrDefault(unitName, new ArrayList<>());
    }
    
    /**
     * 检查单位是否有特定类型的效果
     */
    public static boolean hasEffect(String unitName, String effectType) {
        List<BuffEffect> effects = UNIT_EFFECTS.get(unitName);
        if (effects != null) {
            for (BuffEffect effect : effects) {
                if (effect.type.equals(effectType)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 获取单位特定类型效果的层数
     */
    public static int getEffectStacks(String unitName, String effectType) {
        List<BuffEffect> effects = UNIT_EFFECTS.get(unitName);
        if (effects != null) {
            for (BuffEffect effect : effects) {
                if (effect.type.equals(effectType)) {
                    return effect.stacks;
                }
            }
        }
        return 0;
    }
    
    /**
     * 每个回合开始时处理效果
     * 需要在每个回合开始前调用
     */
    public static Map<String, Integer> processEffectsAtRoundStart(String unitName) {
        List<BuffEffect> effects = UNIT_EFFECTS.get(unitName);
        if (effects == null || effects.isEmpty()) {
            return null;
        }
        
        Map<String, Integer> damageResults = new HashMap<>();
        Iterator<BuffEffect> iterator = effects.iterator();
        
        while (iterator.hasNext()) {
            BuffEffect effect = iterator.next();
            
            // 处理剧毒效果：每回合扣血
            if (EFFECT_POISON.equals(effect.type)) {
                int poisonDamage = effect.stacks; // 每层造成1点伤害
                damageResults.put("POISON_DAMAGE", poisonDamage);
                
                // 添加日志信息
                System.out.println("回合开始: " + unitName + " 受到 " + poisonDamage + " 点剧毒伤害");
            }
            
            // 减少持续时间
            effect.duration--;
            
            // 持续时间结束，移除效果
            if (effect.duration <= 0) {
                iterator.remove();
            }
        }
        
        // 如果效果列表为空，移除单位的记录
        if (effects.isEmpty()) {
            UNIT_EFFECTS.remove(unitName);
        }
        
        return damageResults;
    }
    
    /**
     * 处理流血效果（在单位受到攻击时调用）
     * @return 额外流血伤害值
     */
    public static int processBleedingOnDamageTaken(String unitName) {
        if (hasEffect(unitName, EFFECT_BLEEDING)) {
            return getEffectStacks(unitName, EFFECT_BLEEDING); // 每层造成1点额外伤害
        }
        return 0;
    }
    
    /**
     * 检查单位是否被眩晕
     */
    public static boolean isUnitStunned(String unitName) {
        return hasEffect(unitName, EFFECT_STUN);
    }
    
    /**
     * 应用眩晕效果到单位（重置冷却）
     */
    public static void applyStunToUnit(String unitName, BattleUnit unit, String source, int duration) {
        // 添加眩晕效果
        addEffect(unitName, EFFECT_STUN, 1, duration, source);
        
        // 重置冷却进度
        if (unit != null) {
            // 重置攻击冷却
            unit.resetCooldowns();
            
            // 重置技能冷却
            BattleSkill[] skills = unit.getSkills();
            if (skills != null) {
                for (BattleSkill skill : skills) {
                    if (skill != null) {
                        skill.currentCooldown = skill.cooldown; // 重置为最大冷却值
                    }
                }
            }
        }
    }
    
    /**
     * 获取单位效果描述字符串
     */
    public static String getEffectsDescription(String unitName) {
        List<BuffEffect> effects = getEffects(unitName);
        if (effects.isEmpty()) {
            return "无效果";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < effects.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(effects.get(i).toString());
        }
        return sb.toString();
    }
    
    /**
     * 清除所有效果（用于战斗结束）
     */
    public static void clearAllEffects() {
        UNIT_EFFECTS.clear();
    }
}