package com.example.myapplication3;

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
    private static final double ENCOUNTER_PROBABILITY = 0.1; // 10%遭遇概率
    
    private static WildAnimalEncounterManager instance;
    private Random random = new Random();
    
    // 地形与动物的对应关系
    private Map<String, List<String>> terrainAnimals = new HashMap<>();
    
    public static WildAnimalEncounterManager getInstance() {
        if (instance == null) {
            instance = new WildAnimalEncounterManager();
        }
        return instance;
    }
    
    private WildAnimalEncounterManager() {
        initTerrainAnimalMapping();
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
     * 检查是否遭遇野生动物
     */
    public boolean checkForWildAnimalEncounter() {
        return random.nextDouble() < ENCOUNTER_PROBABILITY;
    }
    
    /**
     * 根据地形获取随机动物
     */
    public Monster getRandomAnimalForTerrain(String terrainType) {
        List<String> animalNames = terrainAnimals.get(terrainType);
        if (animalNames == null || animalNames.isEmpty()) {
            // 如果地形没有对应的动物，使用默认动物
            animalNames = terrainAnimals.get("草原");
            if (animalNames == null) {
                Log.w(TAG, "无法为地形" + terrainType + "找到合适的动物");
                return null;
            }
        }
        
        // 随机选择一个动物名称
        String animalName = animalNames.get(random.nextInt(animalNames.size()));
        
        // 从MonsterManager获取动物数据
        Monster animal = MonsterManager.getMonsterByName(animalName);
        if (animal == null) {
            Log.w(TAG, "未找到动物: " + animalName);
            // 如果找不到指定的动物，返回一个随机动物
            animal = MonsterManager.getRandomMonster();
        }
        
        return animal;
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
     * 获取遭遇概率
     */
    public double getEncounterProbability() {
        return ENCOUNTER_PROBABILITY;
    }
}