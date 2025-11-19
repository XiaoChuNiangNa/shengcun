package com.example.myapplication3;

/**
 * 玩家属性类
 * 管理攻击、防御、速度等战斗属性
 */
public class PlayerStats {
    private int attack;      // 攻击力
    private int defense;     // 防御力
    private int speed;       // 速度
    
    // 默认属性值
    private static final int DEFAULT_ATTACK = 10;
    private static final int DEFAULT_DEFENSE = 5;
    private static final int DEFAULT_SPEED = 100;
    
    public PlayerStats() {
        this.attack = DEFAULT_ATTACK;
        this.defense = DEFAULT_DEFENSE;
        this.speed = DEFAULT_SPEED;
    }
    
    public PlayerStats(int attack, int defense, int speed) {
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
    }
    
    // Getters
    public int getAttack() {
        return attack;
    }
    
    public int getDefense() {
        return defense;
    }
    
    public int getSpeed() {
        return speed;
    }
    
    // Setters
    public void setAttack(int attack) {
        this.attack = attack;
    }
    
    public void setDefense(int defense) {
        this.defense = defense;
    }
    
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    
    /**
     * 根据速度计算攻击间隔（秒）
     * 公式：速度100，间隔2秒；速度每增加100，间隔减0.1秒；下限1秒
     * @return 攻击间隔（秒）
     */
    public double getAttackInterval() {
        // 基础攻击间隔：速度100 = 2秒
        double baseInterval = 2.0;
        
        // 速度修正：速度每增加100，间隔减少0.1秒
        double speedBonus = (speed - 100) / 100.0 * 0.1;
        
        // 计算最终间隔
        double interval = baseInterval - speedBonus;
        
        // 下限1秒
        return Math.max(interval, 1.0);
    }
    
    /**
     * 计算攻击间隔的显示进度（百分比）
     * @param currentCooldown 当前冷却值
     * @param maxCooldown 最大冷却值
     * @return 进度百分比
     */
    public int getAttackCooldownPercentage(float currentCooldown, int maxCooldown) {
        if (maxCooldown <= 0) return 0;
        return (int) (((maxCooldown - currentCooldown) * 100.0) / maxCooldown);
    }
    
    /**
     * 计算伤害
     * 公式：实际伤害 = 攻击者的攻击力 - 目标的防御力
     * 如果防御力大于攻击力，则造成0伤害
     * @param attackerAttack 攻击者的攻击力
     * @param targetDefense 目标的防御力
     * @return 实际伤害值
     */
    public static int calculateDamage(int attackerAttack, int targetDefense) {
        int damage = attackerAttack - targetDefense;
        return Math.max(damage, 0); // 伤害不能为负数
    }
    
    /**
     * 获取最大速度值（对应攻击间隔下限1秒）
     * 当速度达到1100时，攻击间隔为1秒
     */
    public static int getMaxSpeed() {
        return 1100;
    }
    
    /**
     * 检查速度是否达到上限
     */
    public boolean isMaxSpeed() {
        return speed >= getMaxSpeed();
    }
    
    /**
     * 增加属性值
     */
    public void increaseAttack(int amount) {
        this.attack += amount;
    }
    
    public void increaseDefense(int amount) {
        this.defense += amount;
    }
    
    public void increaseSpeed(int amount) {
        this.speed = Math.min(this.speed + amount, getMaxSpeed());
    }
    
    /**
     * 重置为默认值
     */
    public void reset() {
        this.attack = DEFAULT_ATTACK;
        this.defense = DEFAULT_DEFENSE;
        this.speed = DEFAULT_SPEED;
    }
    
    @Override
    public String toString() {
        return String.format("攻击:%d 防御:%d 速度:%d", attack, defense, speed);
    }
}