package com.example.myapplication3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 战利品箱管理器
 * 管理所有战利品箱的定义和操作
 */
public class LootBoxManager {
    private static LootBoxManager instance;
    private Map<Integer, LootBox> lootBoxes;
    private Random random;

    private LootBoxManager() {
        lootBoxes = new HashMap<>();
        random = new Random();
        initializeLootBoxes();
    }

    public static synchronized LootBoxManager getInstance() {
        if (instance == null) {
            instance = new LootBoxManager();
        }
        return instance;
    }

    /**
     * 初始化所有战利品箱
     */
    private void initializeLootBoxes() {
        // 小战利品箱 - 普通
        LootBox smallBox = new LootBox(1, "小战利品箱", Rarity.COMMON, "基础的战利品箱，包含常见的基础资源");
        smallBox.addItemDrop("石头", 3, 5);
        smallBox.addItemDrop("沙子", 3, 5);
        smallBox.addItemDrop("燧石", 0, 3);
        smallBox.addItemDrop("煤炭", 0, 3);
        smallBox.addItemDrop("铁矿", 0, 3);
        lootBoxes.put(1, smallBox);

        // 中战利品箱 - 稀有
        LootBox mediumBox = new LootBox(2, "中战利品箱", Rarity.RARE, "品质较好的战利品箱，包含更多稀有资源");
        mediumBox.addItemDrop("沙子", 3, 5);
        mediumBox.addItemDrop("燧石", 3, 5);
        mediumBox.addItemDrop("煤炭", 1, 3);
        mediumBox.addItemDrop("铁矿", 0, 5);
        mediumBox.addItemDrop("硫磺", 0, 3);
        lootBoxes.put(2, mediumBox);

        // 大战利品箱 - 史诗
        LootBox largeBox = new LootBox(3, "大战利品箱", Rarity.EPIC, "高级战利品箱，包含珍贵资源");
        largeBox.addItemDrop("燧石", 3, 5);
        largeBox.addItemDrop("煤炭", 3, 5);
        largeBox.addItemDrop("铁矿", 1, 5);
        largeBox.addItemDrop("硫磺", 0, 3);
        largeBox.addItemDrop("黑曜石", 0, 1);
        lootBoxes.put(3, largeBox);

        // 巨型战利品箱 - 传说
        LootBox giantBox = new LootBox(4, "巨型战利品箱", Rarity.LEGENDARY, "传说中的战利品箱，包含大量稀有资源");
        giantBox.addItemDrop("煤炭", 3, 5);
        giantBox.addItemDrop("铁矿", 3, 5);
        giantBox.addItemDrop("硫磺", 3, 5);
        giantBox.addItemDrop("黑曜石", 0, 2);
        giantBox.addItemDrop("宝石", 0, 1);
        lootBoxes.put(4, giantBox);

        // 终极战利品箱 - 神话
        LootBox ultimateBox = new LootBox(5, "终极战利品箱", Rarity.MYTHICAL, "神话级别的战利品箱，可能包含最珍贵的宝物");
        ultimateBox.addItemDrop("铁矿", 3, 5);
        ultimateBox.addItemDrop("硫磺", 3, 5);
        ultimateBox.addItemDrop("黑曜石", 1, 3);
        ultimateBox.addItemDrop("宝石", 0, 2);
        ultimateBox.addItemDrop("钻石", 0, 1);
        lootBoxes.put(5, ultimateBox);
    }

    /**
     * 根据ID获取战利品箱
     * @param boxId 战利品箱ID
     * @return 战利品箱对象，如果不存在返回null
     */
    public LootBox getLootBox(int boxId) {
        return lootBoxes.get(boxId);
    }

    /**
     * 获取所有战利品箱
     * @return 所有战利品箱列表
     */
    public List<LootBox> getAllLootBoxes() {
        return new ArrayList<>(lootBoxes.values());
    }

    /**
     * 根据稀有度获取战利品箱
     * @param rarity 稀有度
     * @return 对应稀有度的战利品箱列表
     */
    public List<LootBox> getLootBoxesByRarity(Rarity rarity) {
        List<LootBox> result = new ArrayList<>();
        for (LootBox box : lootBoxes.values()) {
            if (box.getRarity() == rarity) {
                result.add(box);
            }
        }
        return result;
    }

    /**
     * 开启战利品箱
     * @param boxId 战利品箱ID
     * @param difficulty 游戏难度
     * @return 掉落的物品列表
     */
    public List<Item> openLootBox(int boxId, String difficulty) {
        LootBox box = getLootBox(boxId);
        if (box == null) {
            return new ArrayList<>();
        }
        return box.openBox(difficulty);
    }

    /**
     * 随机获取一个战利品箱
     * @param maxRarity 最大稀有度
     * @return 随机的战利品箱
     */
    public LootBox getRandomLootBox(Rarity maxRarity) {
        List<LootBox> availableBoxes = new ArrayList<>();
        
        for (LootBox box : lootBoxes.values()) {
            // 只选择不超过最大稀有度的箱子
            if (box.getRarity().ordinal() <= maxRarity.ordinal()) {
                availableBoxes.add(box);
            }
        }
        
        if (availableBoxes.isEmpty()) {
            return null;
        }
        
        // 根据稀有度权重选择
        return weightedRandomSelection(availableBoxes);
    }

    /**
     * 根据稀有度权重进行随机选择
     * @param boxes 可选的战利品箱列表
     * @return 选中的战利品箱
     */
    private LootBox weightedRandomSelection(List<LootBox> boxes) {
        // 计算总权重
        double totalWeight = 0;
        for (LootBox box : boxes) {
            // 稀有度越高，权重越低（越难获得）
            double weight = 1.0 / (box.getRarity().ordinal() + 1);
            totalWeight += weight;
        }
        
        // 随机选择
        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0;
        
        for (LootBox box : boxes) {
            double weight = 1.0 / (box.getRarity().ordinal() + 1);
            currentWeight += weight;
            if (randomValue <= currentWeight) {
                return box;
            }
        }
        
        return boxes.get(0); // 默认返回第一个
    }

    /**
     * 获取战利品箱的掉落统计
     * @param boxId 战利品箱ID
     * @param difficulty 游戏难度
     * @return 掉落统计信息
     */
    public Map<String, String> getLootBoxStatistics(int boxId, String difficulty) {
        LootBox box = getLootBox(boxId);
        if (box == null) {
            return new HashMap<>();
        }
        return box.getDropStatistics(difficulty);
    }

    /**
     * 估算战利品箱价值
     * @param boxId 战利品箱ID
     * @param difficulty 游戏难度
     * @return 预估价值
     */
    public int estimateLootBoxValue(int boxId, String difficulty) {
        LootBox box = getLootBox(boxId);
        if (box == null) {
            return 0;
        }
        return box.estimateTotalValue(difficulty);
    }

    /**
     * 模拟多次开启战利品箱
     * @param boxId 战利品箱ID
     * @param difficulty 游戏难度
     * @param times 模拟次数
     * @return 平均掉落统计
     */
    public Map<String, Double> simulateLootBoxOpens(int boxId, String difficulty, int times) {
        LootBox box = getLootBox(boxId);
        if (box == null) {
            return new HashMap<>();
        }
        return box.simulateAverageDrops(difficulty, times);
    }

    /**
     * 获取战利品箱的详细信息
     * @param boxId 战利品箱ID
     * @return 格式化的详细信息
     */
    public String getLootBoxDetails(int boxId) {
        LootBox box = getLootBox(boxId);
        if (box == null) {
            return "战利品箱不存在";
        }
        return box.toString();
    }

    /**
     * 添加自定义战利品箱
     * @param lootBox 自定义战利品箱
     */
    public void addCustomLootBox(LootBox lootBox) {
        if (lootBox != null) {
            lootBoxes.put(lootBox.getBoxId(), lootBox);
        }
    }

    /**
     * 移除战利品箱
     * @param boxId 战利品箱ID
     * @return 是否成功移除
     */
    public boolean removeLootBox(int boxId) {
        return lootBoxes.remove(boxId) != null;
    }

    /**
     * 检查战利品箱是否存在
     * @param boxId 战利品箱ID
     * @return 是否存在
     */
    public boolean hasLootBox(int boxId) {
        return lootBoxes.containsKey(boxId);
    }

    /**
     * 获取战利品箱总数
     * @return 总数
     */
    public int getLootBoxCount() {
        return lootBoxes.size();
    }
}