package com.example.myapplication3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 区域资源产出管理器：专门负责管理所有区域的资源配置、产出规则
 */
public class AreaResourceManager {
    // 单例实例
    private static volatile AreaResourceManager instance;

    // 存储所有区域的资源配置（区域名称 -> 区域资源配置）
    private final Map<String, AreaResource> areaResourceMap;

    // 私有构造方法：初始化所有区域的资源配置
    private AreaResourceManager() {
        areaResourceMap = new HashMap<>();
        initAreaResources(); // 初始化区域资源
    }

    // 单例获取方法
    public static AreaResourceManager getInstance() {
        if (instance == null) {
            synchronized (AreaResourceManager.class) {
                if (instance == null) {
                    instance = new AreaResourceManager();
                }
            }
        }
        return instance;
    }

    // 初始化所有区域的资源配置（根据表格数据）
    private void initAreaResources() {
        // 草原
        areaResourceMap.put("草原", new AreaResource(
                8, // 最大采集次数
                10, // 恢复时间（分钟）
                Arrays.asList(
                        new ResourceItem("杂草", 1, 5),
                        new ResourceItem("浆果", 0, 2),
                        new ResourceItem("药草", 0, 2),
                        new ResourceItem("纤维", 0, 2),
                        new ResourceItem("土豆", 0, 2),
                        new ResourceItem("胡萝卜", 0, 2),
                        new ResourceItem("甜菜", 0, 2),
                        new ResourceItem("菠菜", 0, 2),
                        new ResourceItem("小石子", 0, 3)
                )
        ));

        // 雪原
        areaResourceMap.put("雪原", new AreaResource(
                5, 15,
                Arrays.asList(
                        new ResourceItem("冰块", 1, 3),
                        new ResourceItem("石头", 1, 3),
                        new ResourceItem("水", 1, 2),
                        new ResourceItem("蓝莓", 0, 2),
                        new ResourceItem("玉米", 0, 1),
                        new ResourceItem("小石子", 0, 3)
                )
        ));

        // 树林
        areaResourceMap.put("树林", new AreaResource(
                6, 12,
                Arrays.asList(
                        new ResourceItem("木头", 1, 4),
                        new ResourceItem("苹果", 0, 2),
                        new ResourceItem("药草", 0, 1),
                        new ResourceItem("藤蔓", 0, 1),
                        new ResourceItem("蜂巢", 0, 1),
                        new ResourceItem("蘑菇", 0, 1),
                        new ResourceItem("小石子", 0, 3)
                )
        ));

        // 针叶林
        areaResourceMap.put("针叶林", new AreaResource(
                7, 14,
                Arrays.asList(
                        new ResourceItem("木头", 2, 6),
                        new ResourceItem("橡果", 0, 2),
                        new ResourceItem("药草", 0, 1),
                        new ResourceItem("藤蔓", 1, 2),
                        new ResourceItem("树脂", 0, 1),
                        new ResourceItem("松露", 0, 1),
                        new ResourceItem("蘑菇", 0, 1),
                        new ResourceItem("小石子", 0, 3)  // 新增小石子产出，数量0-3
                )
        ));

        // 岩石区
        areaResourceMap.put("岩石区", new AreaResource(
                5, 18,
                Arrays.asList(
                        new ResourceItem("石头", 1, 5),
                        new ResourceItem("铁矿", 0, 2),
                        new ResourceItem("宝石", 0, 1),
                        new ResourceItem("燧石", 0, 1),
                        new ResourceItem("硫磺", 0, 1),
                        new ResourceItem("煤炭", 0, 1),
                        new ResourceItem("小石子", 2, 5)  // 新增小石子产出，数量2-5
                )
        ));

        // 雪山
        areaResourceMap.put("雪山", new AreaResource(
                4, 20,
                Arrays.asList(
                        new ResourceItem("冰块", 1, 3),
                        new ResourceItem("石头", 2, 6),
                        new ResourceItem("铁矿", 1, 3),
                        new ResourceItem("宝石", 0, 2),
                        new ResourceItem("雪莲", 0, 1),
                        new ResourceItem("黑曜石", 0, 1)
                )
        ));

        // 河流
        areaResourceMap.put("河流", new AreaResource(
                6, 8,
                Arrays.asList(
                        new ResourceItem("水", 1, 5),
                        new ResourceItem("鱼", 0, 2),
                        new ResourceItem("杂草", 1, 3)
                )
        ));

        // 海洋
        areaResourceMap.put("海洋", new AreaResource(
                7, 10,
                Arrays.asList(
                        new ResourceItem("水", 3, 15),
                        new ResourceItem("鱼", 2, 4),
                        new ResourceItem("杂草", 2, 4),
                        new ResourceItem("宝石", 0, 1),
                        new ResourceItem("海带", 0, 2)
                )
        ));

        // 深海
        areaResourceMap.put("深海", new AreaResource(
                5, 25,
                Arrays.asList(
                        new ResourceItem("水", 4, 20),
                        new ResourceItem("鱼", 3, 6),
                        new ResourceItem("杂草", 3, 6),
                        new ResourceItem("宝石", 0, 1),
                        new ResourceItem("海带", 1, 4)
                )
        ));

        // 海滩
        areaResourceMap.put("海滩", new AreaResource(
                7, 12,
                Arrays.asList(
                        new ResourceItem("水", 1, 3),
                        new ResourceItem("沙子", 1, 3),
                        new ResourceItem("鱼", 0, 1),
                        new ResourceItem("贝壳", 0, 1),
                        new ResourceItem("椰子", 0, 1),
                        new ResourceItem("螃蟹", 0, 2)
                )
        ));

        // 沙漠
        areaResourceMap.put("沙漠", new AreaResource(
                4, 30,
                Arrays.asList(
                        new ResourceItem("杂草", 1, 2),
                        new ResourceItem("仙人掌果", 0, 2),
                        new ResourceItem("沙子", 1, 5)
                )
        ));

        // 沼泽
        areaResourceMap.put("沼泽", new AreaResource(
                5, 16,
                Arrays.asList(
                        new ResourceItem("水", 1, 3),
                        new ResourceItem("杂草", 1, 3),
                        new ResourceItem("蘑菇", 0, 2),
                        new ResourceItem("芦苇", 0, 2),
                        new ResourceItem("粘土", 0, 2),
                        new ResourceItem("冬瓜", 0, 1)
                )
        ));

        // 废弃营地
        areaResourceMap.put("废弃营地", new AreaResource(
                3, 20,
                Arrays.asList(
                        new ResourceItem("石镐", 0, 1),
                        new ResourceItem("石斧", 0, 1),
                        new ResourceItem("石质鱼竿", 0, 1),
                        new ResourceItem("干面包", 0, 2),
                        new ResourceItem("草药", 0, 2),
                        new ResourceItem("黄瓜", 0, 1)
                )
        ));

        // 村落
        areaResourceMap.put("村落", new AreaResource(
                6, 15,
                Arrays.asList(
                        new ResourceItem("石镐", 0, 1),
                        new ResourceItem("石斧", 0, 1),
                        new ResourceItem("石质鱼竿", 0, 1),
                        new ResourceItem("干面包", 1, 4),
                        new ResourceItem("草药", 1, 3),
                        new ResourceItem("铁锭", 0, 1),
                        new ResourceItem("大米", 1, 3)
                )
        ));
    }

    // 根据区域名称获取资源配置
    public AreaResource getAreaResource(String areaType) {
        return areaResourceMap.get(areaType);
    }

    // 随机生成区域的基础资源（工具方法：直接在管理器中封装产出逻辑）
    public List<CollectedItem> generateBaseResources(String areaType) {
        AreaResource areaResource = getAreaResource(areaType);
        if (areaResource == null || areaResource.resources.isEmpty()) {
            return new ArrayList<>();
        }

        List<CollectedItem> collectedItems = new ArrayList<>();
        Random random = new Random();

        // 遍历区域所有资源，根据数量范围随机生成（0数量的资源可能不产出）
        for (ResourceItem item : areaResource.resources) {
            int count = random.nextInt(item.maxCount - item.minCount + 1) + item.minCount;
            if (count > 0) { // 只添加数量>0的资源
                collectedItems.add(new CollectedItem(item.name, count));
            }
        }

        return collectedItems;
    }

    // 基于稀有度系统的资源生成方法
    public List<CollectedItem> generateRarityBasedResources(String areaType, String difficulty) {
        AreaResource areaResource = getAreaResource(areaType);
        if (areaResource == null || areaResource.resources.isEmpty()) {
            return new ArrayList<>();
        }

        List<CollectedItem> collectedItems = new ArrayList<>();
        Random random = new Random();
        DifficultyManager difficultyManager = DifficultyManager.getInstance();

        // 遍历区域所有资源，根据稀有度概率决定是否掉落
        for (ResourceItem item : areaResource.resources) {
            // 获取物品的稀有度
            Rarity rarity = ItemRarityManager.getItemRarity(item.name);
            
            // 根据难度获取掉落概率
            double probability = difficultyManager.getRarityProbability(difficulty, rarity);
            
            // 随机决定是否掉落
            if (random.nextDouble() <= probability) {
                // 根据稀有度决定掉落数量范围
                int[] dropRange = rarity.getDropAmountRange();
                int count = random.nextInt(dropRange[1] - dropRange[0] + 1) + dropRange[0];
                
                // 应用难度资源系数
                double multiplier = difficultyManager.getResourceMultiplier(difficulty);
                count = (int) Math.round(count * multiplier);
                
                if (count > 0) {
                    collectedItems.add(new CollectedItem(item.name, count));
                }
            }
        }

        return collectedItems;
    }

    // 获取区域资源的稀有度统计信息
    public Map<Rarity, Integer> getAreaRarityStatistics(String areaType) {
        AreaResource areaResource = getAreaResource(areaType);
        if (areaResource == null) {
            return new HashMap<>();
        }

        Map<Rarity, Integer> stats = new HashMap<>();
        for (Rarity rarity : Rarity.values()) {
            stats.put(rarity, 0);
        }

        for (ResourceItem item : areaResource.resources) {
            Rarity rarity = ItemRarityManager.getItemRarity(item.name);
            stats.put(rarity, stats.get(rarity) + 1);
        }

        return stats;
    }


    // 内部类：单个资源项（名称+数量范围）
    public static class ResourceItem {
        public String name;       // 资源名称
        public int minCount;      // 最小数量
        public int maxCount;      // 最大数量

        public ResourceItem(String name, int minCount, int maxCount) {
            this.name = name;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }

    // 内部类：区域资源配置（包含该区域的所有资源信息）
    public static class AreaResource {
        public int maxCollectTimes;   // 最大采集次数
        public int recoveryMinutes;   // 恢复时间（分钟）
        public List<ResourceItem> resources; // 资源列表

        public AreaResource(int maxCollectTimes, int recoveryMinutes, List<ResourceItem> resources) {
            this.maxCollectTimes = maxCollectTimes;
            this.recoveryMinutes = recoveryMinutes;
            this.resources = resources;
        }
    }

    // 内部类：采集结果项（名称+实际数量）
    public static class CollectedItem {
        public String name;
        public int count;

        public CollectedItem(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }
}