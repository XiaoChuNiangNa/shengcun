package com.example.myapplication3;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * 科技管理类，负责所有科技的初始化、数据管理、升级逻辑等核心操作
 * 采用单例模式确保全局唯一实例，统一管理科技数据
 */
public class TechManager {
    private static TechManager instance;
    private List<Tech> allTechs = new ArrayList<>(); // 存储所有科技的列表
    private DBHelper dbHelper;

    /**
     * 单例模式获取实例
     * @param context 上下文
     * @return 唯一的TechManager实例
     */
    public static synchronized TechManager getInstance(Context context) {
        if (instance == null) {
            instance = new TechManager(context);
        }
        return instance;
    }

    /**
     * 私有构造方法，初始化数据库帮助类和科技数据
     * @param context 上下文
     */
    private TechManager(Context context) {
        dbHelper = DBHelper.getInstance(context);
        initAllTechs(); // 初始化所有科技数据
    }

    /**
     * 初始化所有科技数据，包含基础科技、一级科技和二级科技
     * 每个科技都配置了详细属性和升级信息
     */
    private void initAllTechs() {
        // 基础科技：无需前置条件，是所有后续科技的基础
        allTechs.add(new Tech(
                "base_gathering",
                "基础采集术",
                Tech.TYPE_BASE,
                3,
                new int[]{5, 5, 5}, // 各等级升级消耗的希望点数
                new String[]{
                        "采集时额外获取1个随机普通资源",
                        "采集时额外获取2个随机普通资源",
                        "采集时额外获取3个随机普通资源"
                },
                "", 0, "" // 无前置科技
        ));

        // 一级科技：需要基础科技达到满级（3级）才能解锁
        allTechs.add(new Tech(
                "wild_gathering",
                "荒野采集术",
                Tech.TYPE_PRIMARY,
                3,
                new int[]{10, 20, 30},
                new String[]{
                        "在区域等级1的地方采集时，额外随机获取1个普通资源",
                        "在区域等级2的地方采集时，额外随机获取1个稀有资源",
                        "在区域等级3的地方采集时，额外随机获取1个史诗资源"
                },
                "base_gathering", 3, "" // 前置科技为基础采集术，需达到3级
        ));

        allTechs.add(new Tech(
                "simple_toolcraft",
                "简易工具制作术",
                Tech.TYPE_PRIMARY,
                3,
                new int[]{10, 20, 40},
                new String[]{
                        "提升所有工具耐久度10点",
                        "提升所有工具耐久度20点",
                        "提升所有工具耐久度30点"
                },
                "base_gathering", 3, "" // 前置科技为基础采集术，需达到3级
        ));

        allTechs.add(new Tech(
                "basic_construction",
                "基础建筑建造技术",
                Tech.TYPE_PRIMARY,
                3,
                new int[]{10, 20, 40},
                new String[]{
                        "初始解锁烹饪功能",
                        "初始解锁熔炼功能",
                        "初始解锁贸易功能"
                },
                "base_gathering", 3, "" // 前置科技为基础采集术，需达到3级
        ));

        allTechs.add(new Tech(
                "trade_mastery",
                "贸易精通",
                Tech.TYPE_PRIMARY,
                3,
                new int[]{10, 20, 40},
                new String[]{
                        "购买消耗的黄金10%概率返还1黄金",
                        "购买消耗的黄金20%概率返还1黄金",
                        "购买消耗的黄金30%概率返还1黄金"
                },
                "base_gathering", 3, "" // 前置科技为基础采集术，需达到3级
        ));

        allTechs.add(new Tech(
                "hope_star",
                "希望之星",
                Tech.TYPE_PRIMARY,
                6,
                new int[]{10, 20, 40, 80, 160, 320},
                new String[]{
                        "轮回后额外获得1个希望点数",
                        "轮回后额外获得2个希望点数",
                        "轮回后额外获得3个希望点数",
                        "轮回后额外获得4个希望点数",
                        "轮回后额外获得5个希望点数",
                        "轮回后额外获得6个希望点数"
                },
                "base_gathering", 3, "" // 前置科技为基础采集术，需达到3级
        ));

        // 二级科技：需要对应一级科技达到满级（3级）才能解锁
        allTechs.add(new Tech(
                "advanced_gathering",
                "高级采集术",
                Tech.TYPE_SECONDARY,
                3,
                new int[]{20, 40, 60},
                new String[]{
                        "在区域等级1的地方采集时，额外随机获取1个稀有资源",
                        "在区域等级2的地方采集时，额外随机获取1个史诗资源",
                        "在区域等级3的地方采集时，额外随机获取1个传说资源"
                },
                "wild_gathering", 3, "wild_gathering" // 前置科技为荒野采集术，需达到3级
        ));

        allTechs.add(new Tech(
                "advanced_toolcraft",
                "高级工具制作术",
                Tech.TYPE_SECONDARY,
                3,
                new int[]{20, 40, 80},
                new String[]{
                        "提升所有工具耐久度30点",
                        "提升所有工具耐久度60点",
                        "提升所有工具耐久度90点"
                },
                "simple_toolcraft", 3, "simple_toolcraft" // 前置科技为简易工具制作术，需达到3级
        ));

        allTechs.add(new Tech(
                "advanced_construction",
                "高级建筑建造技术",
                Tech.TYPE_SECONDARY,
                3,
                new int[]{20, 40, 80},
                new String[]{
                        "初始背包容量+50",
                        "初始背包容量+100",
                        "初始背包容量+150"
                },
                "basic_construction", 3, "basic_construction" // 前置科技为基础建筑建造技术，需达到3级
        ));

        allTechs.add(new Tech(
                "trade_expert",
                "贸易大师",
                Tech.TYPE_SECONDARY,
                3,
                new int[]{30, 60, 90},
                new String[]{
                        "出售额外获得黄金1",
                        "出售额外获得黄金2",
                        "出售额外获得黄金3"
                },
                "trade_mastery", 3, "trade_mastery" // 前置科技为贸易精通，需达到3级
        ));
    }

    /**
     * 从数据库加载指定用户的科技等级
     * @param userId 用户ID
     */
    public void loadUserTechLevels(int userId) {
        // 移除Integer.parseInt()，直接使用int类型的userId
        for (Tech tech : allTechs) {
            tech.level = dbHelper.getTechLevel(userId, tech.techId);
        }
    }

    /**
     * 升级指定科技
     * @param userId 用户ID（String类型）
     * @param tech 要升级的科技
     * @param currentHopePoints 当前拥有的希望点数
     * @return 升级是否成功
     */
    public boolean upgradeTech(int userId, Tech tech, int currentHopePoints) {
        // 校验升级条件
        if (!canUpgrade(tech, currentHopePoints)) {
            return false;
        }

        // 移除Integer.parseInt()，直接使用int类型的userId
        int cost = tech.getCurrentUpgradeCost();
        tech.level++; // 等级+1
        dbHelper.updateTechLevel(userId, tech.techId, tech.level); // 直接传入int类型userId
        dbHelper.updateHopePoints(userId, currentHopePoints - cost); // 直接传入int类型userId
        
        // 修复：科技升级后更新所有现有工具的耐久度上限
        if (tech.techId.equals("simple_toolcraft")) {
            dbHelper.updateAllToolsMaxDurability(userId);
        }
        
        return true;
    }

    /**
     * 获取指定科技的当前效果值
     * @param techId 科技ID
     * @return 当前科技等级对应的效果值
     */
    public int getTechEffectValue(String techId) {
        Tech tech = getTechById(techId);
        if (tech == null) return 0;
        
        switch (techId) {
            case "simple_toolcraft":
                return tech.level * 10; // 每级增加10点耐久
            case "advanced_toolcraft":
                return tech.level * 30; // 每级增加30点耐久
            case "trade_mastery":
                return tech.level * 10; // 每级增加10%概率返还
            case "trade_expert":
                return tech.level; // 每级增加1点出售额外收益
            case "base_gathering":
                return tech.level; // 每级增加1个普通资源
            case "wild_gathering":
            case "advanced_gathering":
                return tech.level; // 每级增加1个采集资源（具体效果在采集逻辑中处理）
            case "advanced_construction":
                return tech.level * 50; // 每级增加50背包容量
            case "hope_star":
                return tech.level; // 每级增加1个希望点数
            default:
                return tech.level; // 默认每级增加1点效果
        }
    }


    /**
     * 检查科技是否可以升级
     * @param tech 要检查的科技
     * @param currentHopePoints 当前拥有的希望点数
     * @return 是否可以升级
     */
    public boolean canUpgrade(Tech tech, int currentHopePoints) {
        // 1. 检查是否已达满级
        if (tech.isMaxLevel()) {
            return false;
        }

        // 2. 检查希望点数是否充足
        if (currentHopePoints < tech.getCurrentUpgradeCost()) {
            return false;
        }

        // 3. 检查前置科技条件是否满足
        if (!checkPreTechCondition(tech)) {
            return false;
        }

        return true;
    }

    /**
     * 检查前置科技条件是否满足
     * @param tech 要检查的科技
     * @return 前置条件是否满足
     */
    private boolean checkPreTechCondition(Tech tech) {
        // 无前置科技则直接满足条件
        if (tech.preTechId.isEmpty()) {
            return true;
        }

        // 查找前置科技
        Tech preTech = getTechById(tech.preTechId);
        if (preTech == null) {
            return false; // 前置科技不存在（配置错误）
        }

        // 检查前置科技等级是否达到要求
        return preTech.level >= tech.preTechMinLevel;
    }

    /**
     * 按类型筛选科技
     * @param type 科技类型（基础/一级/二级）
     * @return 该类型的所有科技
     */
    public List<Tech> getTechsByType(int type) {
        List<Tech> result = new ArrayList<>();
        for (Tech tech : allTechs) {
            if (tech.type == type) {
                result.add(tech);
            }
        }
        return result;
    }

    /**
     * 通过科技ID查找科技
     * @param techId 科技唯一标识
     * @return 对应的科技对象，未找到则返回null
     */
    public Tech getTechById(String techId) {
        for (Tech tech : allTechs) {
            if (tech.techId.equals(techId)) {
                return tech;
            }
        }
        return null;
    }

    /**
     * 获取所有科技的列表
     * @return 包含所有科技的列表
     */
    public List<Tech> getAllTechs() {
        return allTechs;
    }

    /**
     * 检查所有基础科技是否已达满级
     * @return 所有基础科技是否都满级
     */
    public boolean checkAllBaseTechsMaxed() {
        for (Tech tech : allTechs) {
            if (tech.type == Tech.TYPE_BASE && !tech.isMaxLevel()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否有任何二级科技已解锁（等级>0）
     * @return 是否有已解锁的二级科技
     */
    public boolean checkAnySecondaryTechUnlocked() {
        for (Tech tech : allTechs) {
            if (tech.type == Tech.TYPE_SECONDARY && tech.isUnlocked()) {
                return true;
            }
        }
        return false;
    }


}