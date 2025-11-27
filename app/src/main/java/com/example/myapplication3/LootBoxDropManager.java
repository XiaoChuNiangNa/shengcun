package com.example.myapplication3;

import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 战利品箱掉落管理器
 * 处理野生动物战斗后的战利品箱掉落逻辑
 */
public class LootBoxDropManager {
    private static final String TAG = "LootBoxDrop";
    
    private static LootBoxDropManager instance;
    private Random random;
    private LootBoxManager lootBoxManager;
    
    // 基础掉落概率（根据难度调整）
    private static final double BASE_DROP_PROBABILITY = 0.15; // 基础15%掉落率
    
    // 稀有度权重（越稀有权重越低）
    private static final Map<Rarity, Double> RARITY_WEIGHTS = new HashMap<>();
    static {
        RARITY_WEIGHTS.put(Rarity.COMMON, 0.60);    // 60%概率
        RARITY_WEIGHTS.put(Rarity.RARE, 0.25);       // 25%概率
        RARITY_WEIGHTS.put(Rarity.EPIC, 0.10);       // 10%概率
        RARITY_WEIGHTS.put(Rarity.LEGENDARY, 0.04);   // 4%概率
        RARITY_WEIGHTS.put(Rarity.MYTHICAL, 0.01);   // 1%概率
    }
    
    // 动物大小加成
    private static final Map<String, Double> SIZE_BONUS = new HashMap<>();
    static {
        SIZE_BONUS.put("小型", 1.0);     // 无加成
        SIZE_BONUS.put("中型", 1.5);     // 50%加成
        SIZE_BONUS.put("大型", 2.0);     // 100%加成
    }
    
    // 地形特殊加成
    private static final Map<String, Double> TERRAIN_BONUS = new HashMap<>();
    static {
        TERRAIN_BONUS.put("山地", 1.2);   // 山地地形+20%
        TERRAIN_BONUS.put("森林", 1.1);   // 森林地形+10%
        TERRAIN_BONUS.put("平原", 1.0);   // 平原地形无加成
        TERRAIN_BONUS.put("水域", 0.8);   // 水域地形-20%
        TERRAIN_BONUS.put("沙漠", 1.3);   // 沙漠地形+30%
    }
    
    private LootBoxDropManager() {
        random = new Random();
        lootBoxManager = LootBoxManager.getInstance();
    }
    
    public static synchronized LootBoxDropManager getInstance() {
        if (instance == null) {
            instance = new LootBoxDropManager();
        }
        return instance;
    }
    
    /**
     * 计算战斗胜利后是否掉落战利品箱
     * @param animalName 动物名称
     * @param animalSize 动物大小
     * @param terrainType 地形类型
     * @param difficulty 游戏难度
     * @return 如果掉落则返回LootBox对象，否则返回null
     */
    public LootBox calculateLootBoxDrop(String animalName, String animalSize, 
                                        String terrainType, String difficulty) {
        // 计算基础掉落概率
        double dropProbability = BASE_DROP_PROBABILITY;
        
        // 难度调整
        switch (difficulty.toLowerCase()) {
            case "easy":
                dropProbability *= 1.5; // 简单难度掉落率+50%
                break;
            case "hard":
                dropProbability *= 0.7; // 困难难度掉落率-30%
                break;
            default:
                // 普通难度保持不变
                break;
        }
        
        // 动物大小加成
        Double sizeBonus = SIZE_BONUS.get(animalSize);
        if (sizeBonus != null) {
            dropProbability *= sizeBonus;
        }
        
        // 地形加成
        Double terrainBonus = TERRAIN_BONUS.get(terrainType);
        if (terrainBonus != null) {
            dropProbability *= terrainBonus;
        }
        
        // 特殊动物加成
        dropProbability *= getSpecialAnimalBonus(animalName);
        
        // 限制最大掉落概率不超过50%
        dropProbability = Math.min(dropProbability, 0.50);
        
        Log.d(TAG, String.format("动物: %s, 大小: %s, 地形: %s, 难度: %s, 掉落概率: %.2f%%",
                animalName, animalSize, terrainType, difficulty, dropProbability * 100));
        
        // 判断是否掉落
        if (random.nextDouble() > dropProbability) {
            return null;
        }
        
        // 如果掉落，确定稀有度
        Rarity rarity = determineRarity(difficulty, animalSize, terrainType);
        
        // 根据稀有度获取战利品箱
        LootBox lootBox = getLootBoxByRarity(rarity);
        
        if (lootBox != null) {
            Log.i(TAG, String.format("掉落战利品箱: %s (%s)", lootBox.getName(), rarity.getDisplayName()));
        }
        
        return lootBox;
    }
    
    /**
     * 确定战利品箱稀有度
     * @param difficulty 游戏难度
     * @param animalSize 动物大小
     * @param terrainType 地形类型
     * @return 稀有度
     */
    private Rarity determineRarity(String difficulty, String animalSize, String terrainType) {
        // 基础稀有度权重
        Map<Rarity, Double> weights = new HashMap<>(RARITY_WEIGHTS);
        
        // 难度调整权重
        switch (difficulty.toLowerCase()) {
            case "easy":
                // 简单难度：提高低稀有度权重
                weights.put(Rarity.COMMON, weights.get(Rarity.COMMON) * 1.5);
                weights.put(Rarity.RARE, weights.get(Rarity.RARE) * 1.2);
                weights.put(Rarity.EPIC, weights.get(Rarity.EPIC) * 0.8);
                weights.put(Rarity.LEGENDARY, weights.get(Rarity.LEGENDARY) * 0.5);
                weights.put(Rarity.MYTHICAL, weights.get(Rarity.MYTHICAL) * 0.2);
                break;
            case "hard":
                // 困难难度：提高高稀有度权重
                weights.put(Rarity.COMMON, weights.get(Rarity.COMMON) * 0.7);
                weights.put(Rarity.RARE, weights.get(Rarity.RARE) * 0.9);
                weights.put(Rarity.EPIC, weights.get(Rarity.EPIC) * 1.3);
                weights.put(Rarity.LEGENDARY, weights.get(Rarity.LEGENDARY) * 1.5);
                weights.put(Rarity.MYTHICAL, weights.get(Rarity.MYTHICAL) * 2.0);
                break;
            default:
                // 普通难度：保持原权重
                break;
        }
        
        // 动物大小影响
        if ("中型".equals(animalSize)) {
            weights.put(Rarity.RARE, weights.get(Rarity.RARE) * 1.2);
            weights.put(Rarity.EPIC, weights.get(Rarity.EPIC) * 1.1);
        } else if ("大型".equals(animalSize)) {
            weights.put(Rarity.EPIC, weights.get(Rarity.EPIC) * 1.5);
            weights.put(Rarity.LEGENDARY, weights.get(Rarity.LEGENDARY) * 1.3);
        }
        
        // 地形影响
        if ("山地".equals(terrainType)) {
            weights.put(Rarity.LEGENDARY, weights.get(Rarity.LEGENDARY) * 1.2);
        } else if ("沙漠".equals(terrainType)) {
            weights.put(Rarity.EPIC, weights.get(Rarity.EPIC) * 1.1);
        }
        
        // 归一化权重
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0;
        
        for (Map.Entry<Rarity, Double> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }
        
        return Rarity.COMMON; // 默认返回普通
    }
    
    /**
     * 根据稀有度获取战利品箱
     * @param rarity 稀有度
     * @return 对应的战利品箱
     */
    private LootBox getLootBoxByRarity(Rarity rarity) {
        List<LootBox> boxes = lootBoxManager.getLootBoxesByRarity(rarity);
        if (boxes.isEmpty()) {
            return null;
        }
        
        // 如果有多个同稀有度的箱子，随机选择一个
        return boxes.get(random.nextInt(boxes.size()));
    }
    
    /**
     * 获取特殊动物掉落加成
     * @param animalName 动物名称
     * @return 加成倍数
     */
    private double getSpecialAnimalBonus(String animalName) {
        // 特殊动物有更高的掉落率
        if (animalName.contains("狼王") || animalName.contains("熊王")) {
            return 2.0; // 王级动物掉落率翻倍
        } else if (animalName.contains("精英") || animalName.contains("首领")) {
            return 1.5; // 精英动物掉落率+50%
        } else if (animalName.contains("稀有")) {
            return 1.3; // 稀有动物掉落率+30%
        }
        
        return 1.0; // 普通动物无加成
    }
    
    /**
     * 获取掉落概率详情（用于显示）
     * @param animalName 动物名称
     * @param animalSize 动物大小
     * @param terrainType 地形类型
     * @param difficulty 游戏难度
     * @return 掉落概率详情
     */
    public String getDropProbabilityDetails(String animalName, String animalSize, 
                                           String terrainType, String difficulty) {
        double dropProbability = BASE_DROP_PROBABILITY;
        
        // 难度调整
        switch (difficulty.toLowerCase()) {
            case "easy":
                dropProbability *= 1.5;
                break;
            case "hard":
                dropProbability *= 0.7;
                break;
        }
        
        // 动物大小加成
        Double sizeBonus = SIZE_BONUS.get(animalSize);
        if (sizeBonus != null) {
            dropProbability *= sizeBonus;
        }
        
        // 地形加成
        Double terrainBonus = TERRAIN_BONUS.get(terrainType);
        if (terrainBonus != null) {
            dropProbability *= terrainBonus;
        }
        
        // 特殊动物加成
        dropProbability *= getSpecialAnimalBonus(animalName);
        
        // 限制最大概率
        dropProbability = Math.min(dropProbability, 0.50);
        
        return String.format("%.1f%%", dropProbability * 100);
    }
    
    /**
     * 模拟多次战斗的掉落统计
     * @param animalName 动物名称
     * @param animalSize 动物大小
     * @param terrainType 地形类型
     * @param difficulty 游戏难度
     * @param simulationCount 模拟次数
     * @return 掉落统计
     */
    public Map<Rarity, Integer> simulateDrops(String animalName, String animalSize, 
                                            String terrainType, String difficulty, 
                                            int simulationCount) {
        Map<Rarity, Integer> dropStats = new HashMap<>();
        dropStats.put(Rarity.COMMON, 0);
        dropStats.put(Rarity.RARE, 0);
        dropStats.put(Rarity.EPIC, 0);
        dropStats.put(Rarity.LEGENDARY, 0);
        dropStats.put(Rarity.MYTHICAL, 0);
        
        int totalDrops = 0;
        
        for (int i = 0; i < simulationCount; i++) {
            LootBox lootBox = calculateLootBoxDrop(animalName, animalSize, terrainType, difficulty);
            if (lootBox != null) {
                totalDrops++;
                dropStats.put(lootBox.getRarity(), dropStats.get(lootBox.getRarity()) + 1);
            }
        }
        
        Log.d(TAG, String.format("模拟%d次战斗，总掉落%d次，掉落率%.2f%%", 
                simulationCount, totalDrops, (double) totalDrops / simulationCount * 100));
        
        return dropStats;
    }
}