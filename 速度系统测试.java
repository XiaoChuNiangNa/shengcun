/**
 * 速度系统测试 - 验证修复后的速度机制
 * 这个文件仅用于测试和验证，不会被编译到最终应用中
 */

public class 速度系统测试 {
    
    /**
     * 测试不同速度下的冷却减少逻辑
     */
    public static void testSpeedCooldown() {
        System.out.println("=== 速度系统测试 ===");
        
        // 测试不同速度
        int[] speeds = {50, 100, 200, 500};
        int maxCooldown = 100;
        
        for (int speed : speeds) {
            float cooldownReduction = speed / 100.0f;
            int roundsToAttack = (int) Math.ceil(maxCooldown / cooldownReduction);
            
            System.out.println(String.format("速度 %d: 每回合减少 %.1f 冷却, 需要 %d 回合攻击一次", 
                speed, cooldownReduction, roundsToAttack));
        }
        
        System.out.println("\n=== 速度对比测试 ===");
        System.out.println("速度200 vs 速度100: 当速度100攻击1次时，速度200攻击 " + (200/100) + " 次");
        System.out.println("速度500 vs 速度100: 当速度100攻击1次时，速度500攻击 " + (500/100) + " 次");
    }
    
    /**
     * 测试野生动物速度数据
     */
    public static void testWildAnimalSpeeds() {
        System.out.println("\n=== 野生动物速度测试 ===");
        
        // 模拟野生动物数据（来自MonsterManager.java）
        String[][] animals = {
            {"野兔", "200"},
            {"小猪", "100"}, 
            {"山羊", "100"},
            {"野鸡", "100"},
            {"蛇", "200"},
            {"食人鱼", "200"},
            {"狼", "100"},
            {"鹿", "100"},
            {"野猪", "100"},
            {"猴子", "200"},
            {"老虎", "200"},
            {"狮子", "200"},
            {"熊", "100"},
            {"猎豹", "500"},
            {"鲨鱼", "300"}
        };
        
        System.out.println("动物名称\t速度\t攻击频率对比(vs 玩家)");
        System.out.println("------------------------------------------------");
        
        for (String[] animal : animals) {
            String name = animal[0];
            int speed = Integer.parseInt(animal[1]);
            float attackRatio = speed / 100.0f;
            
            System.out.println(String.format("%s\t\t%d\t%.1fx", name, speed, attackRatio));
        }
    }
    
    public static void main(String[] args) {
        testSpeedCooldown();
        testWildAnimalSpeeds();
    }
}