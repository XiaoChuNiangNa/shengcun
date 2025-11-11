package com.example.myapplication3;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

// 导入BattleSkill类
import com.example.myapplication3.BattleSkill;

/**
 * 怪物管理器
 * 管理所有怪物数据，提供随机怪物生成功能
 */
public class MonsterManager {
    private static Monster[] monsters;
    private static Random random = new Random();
    
    static {
        initializeMonsters();
    }
    
    /**
     * 初始化怪物数据
     */
    private static void initializeMonsters() {
        List<Monster> monsterList = new ArrayList<>();
        
        // 1. 野兔 - 小型动物
        List<BattleSkill> hareSkills = new ArrayList<>();
        hareSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1));
        
        DropItem[] hareDrops = {
            new DropItem("肉", 0.8, 2), // 80%概率掉落1-2个肉
            new DropItem("皮革", 0.4, 1) // 40%概率掉落0-1个皮革
        };
        
        Monster hare = new Monster(
            "野兔", "小型动物", "草原、雪原、雪山",
            20, 0, 0, 200, hareSkills, hareDrops
        );
        monsterList.add(hare);
        
        // 2. 小猪 - 小型动物
        List<BattleSkill> pigletSkills = new ArrayList<>();
        pigletSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1));
        
        DropItem[] pigletDrops = {
            new DropItem("肉", 0.9, 5), // 90%概率掉落3-5个肉
            new DropItem("皮革", 0.5, 2) // 50%概率掉落0-2个皮革
        };
        
        Monster piglet = new Monster(
            "小猪", "小型动物", "草原、废弃营地",
            30, 5, 0, 100, pigletSkills, pigletDrops
        );
        monsterList.add(piglet);
        
        // 3. 山羊 - 小型动物
        List<BattleSkill> goatSkills = new ArrayList<>();
        goatSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1));
        
        DropItem[] goatDrops = {
            new DropItem("肉", 0.8, 4), // 80%概率掉落1-4个肉
            new DropItem("皮革", 0.6, 2), // 60%概率掉落1-2个皮革
            new DropItem("羊毛", 0.7, 3) // 70%概率掉落1-3个羊毛
        };
        
        Monster goat = new Monster(
            "山羊", "小型动物", "岩石区、雪原、雪山",
            40, 5, 0, 100, goatSkills, goatDrops
        );
        monsterList.add(goat);
        
        // 4. 野鸡 - 小型动物
        List<BattleSkill> chickenSkills = new ArrayList<>();
        
        DropItem[] chickenDrops = {
            new DropItem("肉", 0.8, 2) // 80%概率掉落1-2个肉
        };
        
        Monster chicken = new Monster(
            "野鸡", "小型动物", "草原、树林",
            10, 5, 0, 100, chickenSkills, chickenDrops
        );
        monsterList.add(chicken);
        
        // 5. 蛇 - 小型动物
        List<BattleSkill> snakeSkills = new ArrayList<>();
        snakeSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.POISON, 1));
        
        DropItem[] snakeDrops = {
            new DropItem("肉", 0.7, 2) // 70%概率掉落1-2个肉
        };
        
        Monster snake = new Monster(
            "蛇", "小型动物", "树林、针叶林",
            20, 10, 0, 200, snakeSkills, snakeDrops
        );
        monsterList.add(snake);
        
        // 6. 食人鱼 - 小型动物
        List<BattleSkill> piranhaSkills = new ArrayList<>();
        piranhaSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1));
        
        DropItem[] piranhaDrops = {
            new DropItem("鱼", 0.6, 2) // 60%概率掉落0-2个鱼
        };
        
        Monster piranha = new Monster(
            "食人鱼", "小型动物", "海洋、深海",
            10, 10, 0, 200, piranhaSkills, piranhaDrops
        );
        monsterList.add(piranha);
        
        // 7. 狼 - 中型动物
        List<BattleSkill> wolfSkills = new ArrayList<>();
        wolfSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 2));
        wolfSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.AOE, 1));
        
        DropItem[] wolfDrops = {
            new DropItem("肉", 0.9, 5), // 90%概率掉落2-5个肉
            new DropItem("兽骨", 0.7, 3) // 70%概率掉落1-3个兽骨
        };
        
        Monster wolf = new Monster(
            "狼", "中型动物", "草原、雪原、河流",
            40, 15, 0, 100, wolfSkills, wolfDrops
        );
        monsterList.add(wolf);
        
        // 8. 鹿 - 中型动物
        List<BattleSkill> deerSkills = new ArrayList<>();
        deerSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1));
        
        DropItem[] deerDrops = {
            new DropItem("肉", 0.9, 5), // 90%概率掉落2-5个肉
            new DropItem("皮革", 0.8, 4), // 80%概率掉落2-4个皮革
            new DropItem("兽骨", 0.8, 4) // 80%概率掉落2-4个兽骨
        };
        
        Monster deer = new Monster(
            "鹿", "中型动物", "草原、雪原、河流",
            50, 5, 0, 100, deerSkills, deerDrops
        );
        monsterList.add(deer);
        
        // 9. 野猪 - 中型动物
        List<BattleSkill> boarSkills = new ArrayList<>();
        boarSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 2));
        
        DropItem[] boarDrops = {
            new DropItem("肉", 0.9, 6), // 90%概率掉落4-6个肉
            new DropItem("皮革", 0.8, 5) // 80%概率掉落3-5个皮革
        };
        
        Monster boar = new Monster(
            "野猪", "中型动物", "草原、树林、河流",
            80, 5, 5, 100, boarSkills, boarDrops
        );
        monsterList.add(boar);
        
        // 10. 猴子 - 中型动物
        List<BattleSkill> monkeySkills = new ArrayList<>();
        monkeySkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.LOOT, 1));
        monkeySkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.AOE, 1));
        monkeySkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1));
        
        DropItem[] monkeyDrops = {
            new DropItem("肉", 0.8, 5), // 80%概率掉落2-5个肉
            new DropItem("皮革", 0.7, 4), // 70%概率掉落2-4个皮革
            new DropItem("兽骨", 0.6, 2) // 60%概率掉落1-2个兽骨
        };
        
        Monster monkey = new Monster(
            "猴子", "中型动物", "树林、针叶林",
            30, 5, 0, 200, monkeySkills, monkeyDrops
        );
        monsterList.add(monkey);
        
        // 11. 老虎 - 大型动物
        List<BattleSkill> tigerSkills = new ArrayList<>();
        tigerSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 2));
        
        DropItem[] tigerDrops = {
            new DropItem("肉", 0.9, 6), // 90%概率掉落4-6个肉
            new DropItem("皮革", 0.8, 5), // 80%概率掉落3-5个皮革
            new DropItem("兽骨", 0.7, 4) // 70%概率掉落2-4个兽骨
        };
        
        Monster tiger = new Monster(
            "老虎", "大型动物", "草原、雪原",
            80, 25, 5, 200, tigerSkills, tigerDrops
        );
        monsterList.add(tiger);
        
        // 12. 狮子 - 大型动物
        List<BattleSkill> lionSkills = new ArrayList<>();
        lionSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 3));
        lionSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.STUN, 1));
        
        DropItem[] lionDrops = {
            new DropItem("肉", 0.9, 7), // 90%概率掉落5-7个肉
            new DropItem("皮革", 0.8, 6), // 80%概率掉落4-6个皮革
            new DropItem("兽骨", 0.8, 5) // 80%概率掉落3-5个兽骨
        };
        
        Monster lion = new Monster(
            "狮子", "大型动物", "草原",
            100, 20, 5, 200, lionSkills, lionDrops
        );
        monsterList.add(lion);
        
        // 13. 熊 - 大型动物
        List<BattleSkill> bearSkills = new ArrayList<>();
        bearSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 3));
        bearSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.STUN, 1));
        
        DropItem[] bearDrops = {
            new DropItem("肉", 0.9, 9), // 90%概率掉落6-9个肉
            new DropItem("皮革", 0.8, 8), // 80%概率掉落5-8个皮革
            new DropItem("兽骨", 0.8, 5) // 80%概率掉落3-5个兽骨
        };
        
        Monster bear = new Monster(
            "熊", "大型动物", "针叶林",
            150, 20, 10, 100, bearSkills, bearDrops
        );
        monsterList.add(bear);
        
        // 14. 猎豹 - 大型动物
        List<BattleSkill> cheetahSkills = new ArrayList<>();
        cheetahSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 3));
        cheetahSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1));
        
        DropItem[] cheetahDrops = {
            new DropItem("肉", 0.9, 6), // 90%概率掉落4-6个肉
            new DropItem("皮革", 0.8, 5), // 80%概率掉落3-5个皮革
            new DropItem("兽骨", 0.7, 4) // 70%概率掉落2-4个兽骨
        };
        
        Monster cheetah = new Monster(
            "猎豹", "大型动物", "草原",
            60, 30, 5, 500, cheetahSkills, cheetahDrops
        );
        monsterList.add(cheetah);
        
        // 15. 鲨鱼 - 大型动物
        List<BattleSkill> sharkSkills = new ArrayList<>();
        sharkSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 3));
        sharkSkills.add(BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 1));
        
        DropItem[] sharkDrops = {
            new DropItem("肉", 0.9, 6), // 90%概率掉落4-6个肉
            new DropItem("皮革", 0.8, 5), // 80%概率掉落3-5个皮革
            new DropItem("兽骨", 0.7, 4) // 70%概率掉落2-4个兽骨
        };
        
        Monster shark = new Monster(
            "鲨鱼", "大型动物", "海洋、深海",
            70, 40, 5, 300, sharkSkills, sharkDrops
        );
        monsterList.add(shark);
        
        monsters = monsterList.toArray(new Monster[0]);
    }
    
    /**
     * 随机获取一个怪物
     */
    public static Monster getRandomMonster() {
        if (monsters.length == 0) {
            return null;
        }
        return monsters[random.nextInt(monsters.length)];
    }
    
    /**
     * 获取所有怪物
     */
    public static Monster[] getAllMonsters() {
        return monsters;
    }
    
    /**
     * 根据名称获取怪物
     */
    public static Monster getMonsterByName(String name) {
        for (Monster monster : monsters) {
            if (monster.getName().equals(name)) {
                return monster;
            }
        }
        return null;
    }
    
    /**
     * 获取怪物数量
     */
    public static int getMonsterCount() {
        return monsters.length;
    }
    
    /**
     * 将怪物转换为战斗单位（使用新技能系统）
     */
    public static BattleUnit convertToBattleUnit(Monster monster) {
        // 直接使用怪物现有的技能列表，转换为数组
        List<BattleSkill> monsterSkills = monster.getSkills();
        BattleSkill[] battleSkills = monsterSkills.toArray(new BattleSkill[0]);
        
        return new BattleUnit(
            monster.getName(),
            monster.getMaxHealth(),
            monster.getAttack(),
            monster.getDefense(),
            monster.getSpeed(),
            battleSkills,
            BattleUnit.TYPE_ENEMY
        );
    }
    
    /**
     * 创建玩家角色
     */
    public static BattleUnit createPlayer() {
        // 玩家默认属性：攻击5，防御0，速度100，无技能
        return new BattleUnit(
            "玩家",
            50, // 玩家生命值
            5,  // 攻击力
            0,  // 防御力
            100, // 速度
            new BattleSkill[0], // 无技能
            BattleUnit.TYPE_PLAYER
        );
    }
    
    /**
     * 检查怪物是否尝试逃跑
     */
    public static boolean tryEscape(Monster monster) {
        if (!monster.canEscape()) {
            return false;
        }
        
        // 逃跑技能有50%概率成功
        return random.nextDouble() <= 0.5;
    }
    
    /**
     * 获取怪物掉落物描述
     */
    public static String getDropDescription(Monster monster) {
        String[] drops = monster.getRandomDrops();
        if (drops.length == 0) {
            return "无掉落物";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < drops.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(drops[i]);
        }
        return sb.toString();
    }
}