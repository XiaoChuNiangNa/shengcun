package com.example.myapplication3;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 野生动物遭遇管理器
 * 处理野外对战系统的核心逻辑
 */
public class WildAnimalEncounterManager {
    private static final String TAG = "WildAnimalEncounter";
    
    // 基于难度的遭遇概率
    private static final double EASY_ENCOUNTER_PROBABILITY = 0.05;    // 简单模式：5%
    private static final double NORMAL_ENCOUNTER_PROBABILITY = 0.10;  // 普通模式：10%
    private static final double HARD_ENCOUNTER_PROBABILITY = 0.15;    // 困难模式：15%
    
    // 中型动物遭遇概率（仅在普通和困难模式下）
    private static final double MEDIUM_ANIMAL_PROBABILITY = 0.02;     // 2%概率遇到中型动物
    
    private static WildAnimalEncounterManager instance;
    private Random random = new Random();
    
    // 地形与动物的对应关系
    private Map<String, List<String>> terrainAnimals = new HashMap<>();
    
    // 动物分类列表
    private List<String> smallAnimals = new ArrayList<>();
    private List<String> mediumAnimals = new ArrayList<>();
    private List<String> largeAnimals = new ArrayList<>();
    
    public static WildAnimalEncounterManager getInstance() {
        if (instance == null) {
            instance = new WildAnimalEncounterManager();
        }
        return instance;
    }
    
    private WildAnimalEncounterManager() {
        initTerrainAnimalMapping();
        initAnimalCategoryMapping();
    }
    
    /**
     * 初始化地形与动物的对应关系
     */
    private void initTerrainAnimalMapping() {
        // 草原动物
        List<String> grasslandAnimals = new ArrayList<>();
        grasslandAnimals.add("野兔");
        grasslandAnimals.add("小猪");
        grasslandAnimals.add("野鸡");
        grasslandAnimals.add("狼");
        grasslandAnimals.add("鹿");
        grasslandAnimals.add("野猪");
        grasslandAnimals.add("老虎");
        grasslandAnimals.add("狮子");
        grasslandAnimals.add("猎豹");
        terrainAnimals.put("草原", grasslandAnimals);
        
        // 雪原动物
        List<String> snowfieldAnimals = new ArrayList<>();
        snowfieldAnimals.add("野兔");
        snowfieldAnimals.add("山羊");
        snowfieldAnimals.add("狼");
        snowfieldAnimals.add("鹿");
        snowfieldAnimals.add("老虎");
        snowfieldAnimals.add("熊");
        terrainAnimals.put("雪原", snowfieldAnimals);
        
        // 树林动物
        List<String> forestAnimals = new ArrayList<>();
        forestAnimals.add("野鸡");
        forestAnimals.add("蛇");
        forestAnimals.add("猴子");
        forestAnimals.add("野猪");
        forestAnimals.add("鹿");
        forestAnimals.add("熊");
        terrainAnimals.put("树林", forestAnimals);
        
        // 针叶林动物
        List<String> coniferousAnimals = new ArrayList<>();
        coniferousAnimals.add("蛇");
        coniferousAnimals.add("猴子");
        coniferousAnimals.add("熊");
        terrainAnimals.put("针叶林", coniferousAnimals);
        
        // 河流动物
        List<String> riverAnimals = new ArrayList<>();
        riverAnimals.add("野兔");
        riverAnimals.add("狼");
        riverAnimals.add("鹿");
        riverAnimals.add("野猪");
        terrainAnimals.put("河流", riverAnimals);
        
        // 岩石区动物
        List<String> rockyAnimals = new ArrayList<>();
        rockyAnimals.add("山羊");
        rockyAnimals.add("蛇");
        terrainAnimals.put("岩石区", rockyAnimals);
        
        // 雪山动物
        List<String> mountainAnimals = new ArrayList<>();
        mountainAnimals.add("山羊");
        mountainAnimals.add("熊");
        terrainAnimals.put("雪山", mountainAnimals);
        
        // 海洋动物
        List<String> oceanAnimals = new ArrayList<>();
        oceanAnimals.add("食人鱼");
        oceanAnimals.add("鲨鱼");
        terrainAnimals.put("海洋", oceanAnimals);
        
        // 深海动物
        List<String> deepOceanAnimals = new ArrayList<>();
        deepOceanAnimals.add("食人鱼");
        deepOceanAnimals.add("鲨鱼");
        terrainAnimals.put("深海", deepOceanAnimals);
        
        // 其他地形（默认一些常见动物）
        List<String> defaultAnimals = new ArrayList<>();
        defaultAnimals.add("野兔");
        defaultAnimals.add("小猪");
        defaultAnimals.add("野鸡");
        terrainAnimals.put("海滩", defaultAnimals);
        terrainAnimals.put("沙漠", defaultAnimals);
        terrainAnimals.put("沼泽", defaultAnimals);
        terrainAnimals.put("废弃营地", defaultAnimals);
        terrainAnimals.put("村落", defaultAnimals);
    }
    
    /**
     * 初始化动物分类映射
     */
    private void initAnimalCategoryMapping() {
        // 小型动物
        smallAnimals.add("野兔");
        smallAnimals.add("小猪");
        smallAnimals.add("山羊");
        smallAnimals.add("野鸡");
        smallAnimals.add("蛇");
        smallAnimals.add("食人鱼");
        
        // 中型动物
        mediumAnimals.add("狼");
        mediumAnimals.add("鹿");
        mediumAnimals.add("野猪");
        mediumAnimals.add("猴子");
        
        // 大型动物
        largeAnimals.add("老虎");
        largeAnimals.add("狮子");
        largeAnimals.add("熊");
        largeAnimals.add("猎豹");
        largeAnimals.add("鲨鱼");
    }
    
    /**
     * 检查是否遭遇野生动物（基于难度）
     */
    public boolean checkForWildAnimalEncounter() {
        String difficulty = getCurrentDifficulty();
        double encounterProbability = getDifficultyEncounterProbability(difficulty);
        
        Log.d(TAG, "当前难度: " + difficulty + ", 遭遇概率: " + (encounterProbability * 100) + "%");
        return random.nextDouble() < encounterProbability;
    }
    
    /**
     * 根据难度获取遭遇概率
     */
    private double getDifficultyEncounterProbability(String difficulty) {
        switch (difficulty) {
            case Constant.DIFFICULTY_EASY:
                return EASY_ENCOUNTER_PROBABILITY;
            case Constant.DIFFICULTY_NORMAL:
                return NORMAL_ENCOUNTER_PROBABILITY;
            case Constant.DIFFICULTY_HARD:
                return HARD_ENCOUNTER_PROBABILITY;
            default:
                return NORMAL_ENCOUNTER_PROBABILITY; // 默认使用普通模式概率
        }
    }
    
    /**
     * 获取当前游戏难度
     */
    private String getCurrentDifficulty() {
        try {
            // 获取当前用户的难度设置
            Context context = MyApplication.getAppContext();
            if (context != null) {
                Map<String, Object> userStatus = DBHelper.getInstance(context).getUserStatus(MyApplication.currentUserId);
                String difficulty = (String) userStatus.get("difficulty");
                
                if (difficulty == null || difficulty.isEmpty()) {
                    // 如果未设置难度，使用默认的普通难度
                    difficulty = Constant.DIFFICULTY_NORMAL;
                }
                
                return difficulty;
            } else {
                Log.w(TAG, "无法获取应用上下文，使用默认难度");
                return Constant.DIFFICULTY_NORMAL;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取难度设置失败: " + e.getMessage());
            return Constant.DIFFICULTY_NORMAL; // 错误时使用普通难度
        }
    }
    
    /**
     * 根据地形和难度获取随机动物
     */
    public Monster getRandomAnimalForTerrain(String terrainType) {
        String difficulty = getCurrentDifficulty();
        List<String> animalNames = terrainAnimals.get(terrainType);
        
        if (animalNames == null || animalNames.isEmpty()) {
            // 如果地形没有对应的动物，使用默认动物
            animalNames = terrainAnimals.get("草原");
            if (animalNames == null) {
                Log.w(TAG, "无法为地形" + terrainType + "找到合适的动物");
                return null;
            }
        }
        
        // 根据难度过滤动物列表
        List<String> filteredAnimals = getFilteredAnimalsByDifficulty(animalNames, difficulty);
        
        if (filteredAnimals.isEmpty()) {
            // 如果没有合适的动物，使用原始列表
            filteredAnimals = animalNames;
            Log.w(TAG, "难度" + difficulty + "下没有合适的动物，使用原始列表");
        }
        
        // 随机选择一个动物名称
        String animalName = filteredAnimals.get(random.nextInt(filteredAnimals.size()));
        
        // 从MonsterManager获取动物数据
        Monster animal = MonsterManager.getMonsterByName(animalName);
        if (animal == null) {
            Log.w(TAG, "未找到动物: " + animalName);
            // 如果找不到指定的动物，返回一个随机动物
            animal = MonsterManager.getRandomMonster();
        }
        
        Log.d(TAG, "难度: " + difficulty + ", 地形: " + terrainType + ", 遭遇动物: " + animalName);
        return animal;
    }
    
    /**
     * 根据难度过滤动物列表
     */
    private List<String> getFilteredAnimalsByDifficulty(List<String> allAnimals, String difficulty) {
        List<String> filteredAnimals = new ArrayList<>();
        
        for (String animalName : allAnimals) {
            Monster animal = MonsterManager.getMonsterByName(animalName);
            if (animal == null) continue;
            
            String animalType = animal.getType();
            
            switch (difficulty) {
                case Constant.DIFFICULTY_EASY:
                    // 简单模式：只允许小型动物，且在遇到时检查中型动物概率
                    if (smallAnimals.contains(animalName)) {
                        filteredAnimals.add(animalName);
                    }
                    break;
                    
                case Constant.DIFFICULTY_NORMAL:
                    // 普通模式：允许小型和中型动物，检查中型动物遭遇概率
                    if (smallAnimals.contains(animalName)) {
                        filteredAnimals.add(animalName);
                    } else if (mediumAnimals.contains(animalName)) {
                        // 2%概率遇到中型动物
                        if (random.nextDouble() < MEDIUM_ANIMAL_PROBABILITY) {
                            filteredAnimals.add(animalName);
                            Log.d(TAG, "普通模式下触发2%中型动物概率，遭遇: " + animalName);
                        }
                    }
                    break;
                    
                case Constant.DIFFICULTY_HARD:
                    // 困难模式：允许所有类型的动物
                    filteredAnimals.add(animalName);
                    break;
                    
                default:
                    // 默认情况：使用普通模式规则
                    if (smallAnimals.contains(animalName)) {
                        filteredAnimals.add(animalName);
                    } else if (mediumAnimals.contains(animalName)) {
                        if (random.nextDouble() < MEDIUM_ANIMAL_PROBABILITY) {
                            filteredAnimals.add(animalName);
                        }
                    }
                    break;
            }
        }
        
        return filteredAnimals;
    }
    
    /**
     * 处理野生动物遭遇事件
     */
    public void handleWildAnimalEncounter(String terrainType, MainActivity activity) {
        Log.d(TAG, "在" + terrainType + "遭遇野生动物");
        
        // 获取适合该地形的动物
        Monster animal = getRandomAnimalForTerrain(terrainType);
        if (animal == null) {
            Log.w(TAG, "无法生成野生动物");
            return;
        }
        
        // 显示遭遇对话框
        showEncounterDialog(animal, terrainType, activity);
    }
    
    /**
     * 显示遭遇对话框
     */
    private void showEncounterDialog(Monster animal, String terrainType, MainActivity activity) {
        try {
            WildAnimalEncounterDialogFragment dialog = 
                WildAnimalEncounterDialogFragment.newInstance(animal, terrainType, activity);
            
            // 设置回调
            dialog.setOnEncounterChoiceListener(new WildAnimalEncounterDialogFragment.OnEncounterChoiceListener() {
                @Override
                public void onBattleChoice(Monster animal) {
                    Log.d(TAG, "玩家选择与" + animal.getName() + "战斗");
                    // 战斗逻辑已经在对话框的handleBattleChoice中处理
                }
                
                @Override
                public void onEscapeChoice(int staminaCost, int timeCost) {
                    Log.d(TAG, "玩家选择逃跑，消耗体力: " + staminaCost + ", 时间增加: " + timeCost + "小时");
                    // 逃跑逻辑已经在对话框的handleEscapeChoice中处理
                }
            });
            
            // 显示对话框
            dialog.show(activity.getSupportFragmentManager(), "wild_animal_encounter");
            
        } catch (Exception e) {
            Log.e(TAG, "显示野生动物遭遇对话框失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取地形对应的动物列表
     */
    public List<String> getAnimalsForTerrain(String terrainType) {
        return terrainAnimals.get(terrainType);
    }
    
    /**
     * 获取所有地形类型
     */
    public List<String> getAllTerrainTypes() {
        return new ArrayList<>(terrainAnimals.keySet());
    }
    
    /**
     * 获取当前难度的遭遇概率
     */
    public double getEncounterProbability() {
        return getDifficultyEncounterProbability(getCurrentDifficulty());
    }
}