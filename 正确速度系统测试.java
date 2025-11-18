/**
 * 正确的速度系统测试 - 验证修复后的速度机制
 * 按照：进度上限=最高速度，进度增量=自身速度的逻辑
 */

public class 正确速度系统测试 {
    
    /**
     * 测试正确的速度系统逻辑
     */
    public static void testCorrectSpeedSystem() {
        System.out.println("=== 正确速度系统测试 ===");
        
        // 模拟战斗场景：速度100 vs 速度500
        int speed1 = 100;  // 玩家或普通动物
        int speed2 = 500;  // 猎豹等高速动物
        
        // 计算双方最高速度作为进度上限
        int maxProgress = Math.max(speed1, speed2);
        System.out.println("进度上限：" + maxProgress);
        
        // 模拟进度增长
        int progress1 = 0, progress2 = 0;
        int rounds = 1;
        
        System.out.println("\n回合\t进度1(" + speed1 + ")\t进度2(" + speed2 + ")\t动作");
        System.out.println("------------------------------------------------");
        
        while (rounds <= 10) {
            // 每回合进度增加 = 速度值
            progress1 += speed1;
            progress2 += speed2;
            
            String action1 = (progress1 >= maxProgress) ? "攻击!" : "-";
            String action2 = (progress2 >= maxProgress) ? "攻击!" : "-";
            
            System.out.println(String.format("%d\t%d/%d\t\t%d/%d\t\t%s\t%s", 
                rounds, progress1, maxProgress, progress2, maxProgress, action1, action2));
            
            // 重置已攻击的进度
            if (progress1 >= maxProgress) progress1 = progress1 % maxProgress;
            if (progress2 >= maxProgress) progress2 = progress2 % maxProgress;
            
            rounds++;
        }
    }
    
    /**
     * 计算攻击频率对比
     */
    public static void calculateAttackFrequency() {
        System.out.println("\n=== 攻击频率分析 ===");
        
        int[] speeds = {50, 100, 200, 500};
        
        for (int speed : speeds) {
            // 假设对手速度为500（猎豹），进度上限为500
            int maxProgress = 500;
            int roundsToAttack = (int) Math.ceil((double) maxProgress / speed);
            
            System.out.println(String.format("速度 %d: 需要 %d 回合攻击一次 (进度上限=%d)", 
                speed, roundsToAttack, maxProgress));
        }
    }
    
    /**
     * 测试野生动物速度对比
     */
    public static void testWildAnimalSpeeds() {
        System.out.println("\n=== 野生动物速度对比(对比玩家速度100) ===");
        
        // 来自MonsterManager的真实速度数据
        String[][] animals = {
            {"野兔", "200"},
            {"猎豹", "500"},
            {"熊", "100"},
            {"狮子", "200"},
            {"鲨鱼", "300"}
        };
        
        int playerSpeed = 100;
        
        for (String[] animal : animals) {
            String name = animal[0];
            int animalSpeed = Integer.parseInt(animal[1]);
            
            // 计算最高速度作为进度上限
            int maxProgress = Math.max(playerSpeed, animalSpeed);
            
            // 计算各自的攻击回合数
            int playerRounds = (int) Math.ceil((double) maxProgress / playerSpeed);
            int animalRounds = (int) Math.ceil((double) maxProgress / animalSpeed);
            
            System.out.println(String.format("%s(速度%d) vs 玩家(速度%d): " +
                "进度上限=%d, 玩家%d回合攻击, %s%d回合攻击", 
                name, animalSpeed, playerSpeed, maxProgress, 
                playerRounds, name, animalRounds));
        }
    }
    
    public static void main(String[] args) {
        testCorrectSpeedSystem();
        calculateAttackFrequency();
        testWildAnimalSpeeds();
    }
}