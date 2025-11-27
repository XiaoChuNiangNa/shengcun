package com.example.myapplication3;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class GameMap {
    private DBHelper dbHelper;
    private static GameMap instance;
    private String[][] mapData; // 地形数据（改为非final，支持动态切换）
    private final int minCoord; // 最小坐标
    private final int maxCoord; // 最大坐标
    private final Set<String> shelterCoords = new HashSet<>();
    private Map<String, Integer> coordHeights = new HashMap<>();
    private Map<String, Integer> terrainHeightMap = new HashMap<>();
    private String currentMapType = "main_world"; // 当前地图类型

    // 基础坐标边界校验
    private boolean isCoordInBoundary(int x, int y) {
        return x >= minCoord && x <= maxCoord && y >= minCoord && y <= maxCoord;
    }

    // 单例模式初始化
    private GameMap(Context context) {
        this.minCoord = Constant.MAP_MIN;
        this.maxCoord = Constant.MAP_MAX;
        this.dbHelper = DBHelper.getInstance(context);
        // 关键：使用全局当前用户ID，而非默认0
        int currentUserId = MyApplication.currentUserId;
        Log.d("GameMapInit", "初始化地形，当前用户ID=" + currentUserId); // 日志验证
        
        // 根据用户当前地图类型加载对应的地图数据
        loadCurrentMapData(currentUserId);
        loadUserTerrains(currentUserId); // 传入正确的用户ID
    }

    public static synchronized GameMap getInstance(Context context) {
        if (instance == null) {
            instance = new GameMap(context.getApplicationContext());
        }
        return instance;
    }



    // 验证坐标是否合法（可复活）
    public boolean isValidCoord(int x, int y) {
        // 1. 检查是否在地图边界内
        if (!isCoordInBoundary(x, y)) {
            return false;
        }
        // 2. 获取地形类型
        String terrain = getTerrainType(x, y);
        // 3. 校验：非未知区域 + 属于合法复活地形
        return terrain != null
                && !Objects.equals(terrain, "未知区域")
                && (isValidSpawnTerrain(terrain) || isShelterBuilding(terrain)); // 新增：庇护所建筑也允许复活
    }

    // 合法复活地形白名单（常量定义，方便维护）
    private static final Set<String> VALID_SPAWN_TERRAINS = new HashSet<>(Arrays.asList(
            "草原", "树林", "针叶林", "海滩", "沙漠", "村落", "废弃营地"
    ));

    // 新增：判断地形是否为合法复活地形
    public boolean isValidSpawnTerrain(String terrain) {
        return VALID_SPAWN_TERRAINS.contains(terrain);
    }

    /**
     * 修正地形获取逻辑：建筑地形优先于庇护所标记
     */
    public String getTerrainType(int x, int y) {
        // 1. 先检查基础地形（含建筑更新后的地形）
        if (!isCoordInBoundary(x, y)) {
            return "未知区域";
        }
        int arrayX = x - minCoord;
        int arrayY = y - minCoord;
        String baseTerrain = "未知区域";
        if (arrayY >= 0 && arrayY < mapData.length && arrayX >= 0 && arrayX < mapData[arrayY].length) {
            baseTerrain = mapData[arrayY][arrayX];
        }

        // 2. 若基础地形是庇护所类建筑，直接返回建筑类型（优先显示建筑）
        if (isShelterBuilding(baseTerrain)) {
            return baseTerrain;
        }

        // 3. 非建筑地形时，再判断是否为庇护所标记
        if (isShelter(x, y)) {
            return "庇护所";
        }

        return baseTerrain;
    }

    // 判断是否为庇护所类建筑地形
    private boolean isShelterBuilding(String terrain) {
        return terrain.equals("茅草屋")
                || terrain.equals("小木屋")
                || terrain.equals("小石屋")
                || terrain.equals("砖瓦屋")
                || terrain.equals("传送门");
    }

    // 获取地形高度
    public int getTerrainHeight(int x, int y) {
        if (!isCoordInBoundary(x, y)) {
            return 1;
        }
        String coordKey = x + "," + y;
        if (Constant.COORD_SPECIFIC_HEIGHT.containsKey(coordKey)) {
            return Constant.COORD_SPECIFIC_HEIGHT.get(coordKey);
        }
        String terrain = getTerrainType(x, y);
        return Constant.TERRAIN_DEFAULT_HEIGHT.getOrDefault(terrain, 1);
    }

    public int getWidth() {
        return maxCoord - minCoord + 1;
    }

    public int getHeight() {
        return maxCoord - minCoord + 1;
    }

    /**
     * 更新指定坐标的地形类型（核心：建造/升级建筑后调用）
     */
    public boolean updateTerrain(int x, int y, String newTerrain) {
        if (mapData == null || !isCoordInBoundary(x, y)) {
            Log.e("updateTerrain", "更新失败：地图数据为空或坐标超出边界 (" + x + "," + y + ")");
            return false;
        }
        int arrayX = x - minCoord;
        int arrayY = y - minCoord;
        if (arrayY >= 0 && arrayY < mapData.length && arrayX >= 0 && arrayX < mapData[arrayY].length) {
            String oldTerrain = mapData[arrayY][arrayX];
            mapData[arrayY][arrayX] = newTerrain;
            Log.d("GameMap", "地形更新：(" + x + "," + y + ") 从 " + oldTerrain + " 变为 " + newTerrain);
            return true;
        }
        Log.e("updateTerrain", "更新失败：数组索引越界 (" + x + "," + y + ") -> (" + arrayX + "," + arrayY + ")");
        return false;
    }

    // 标记/判断庇护所（用于非建筑类庇护所标记）
    public boolean setShelter(int x, int y) {
        if (isCoordInBoundary(x, y)) {
            shelterCoords.add(getCoordKey(x, y));
            return true;
        }
        return false;
    }

    public boolean isShelter(int x, int y) {
        return shelterCoords.contains(getCoordKey(x, y));
    }

    private String getCoordKey(int x, int y) {
        return x + "," + y;
    }

    public List<String> getCollectableItems(int x, int y) {
        List<String> items = new ArrayList<>();
        String areaType = getTerrainType(x, y); // 获取当前区域类型（如"森林"、"草原"）

        // 调用 AreaResourceManager 生成该区域的基础物品
        List<AreaResourceManager.CollectedItem> baseItems =
                AreaResourceManager.getInstance().generateBaseResources(areaType);

        // 提取物品名称到列表
        for (AreaResourceManager.CollectedItem item : baseItems) {
            items.add(item.name);
        }
        return items;
    }

    public void setHeight(int x, int y, int height) {
        String key = x + "," + y;
        terrainHeightMap.put(key, height);
    }

    public int getHeight(int x, int y) {
        String key = x + "," + y;
        return terrainHeightMap.getOrDefault(
                key,
                Constant.TERRAIN_DEFAULT_HEIGHT.getOrDefault(getTerrainType(x, y), 100)
        );
    }

    private void loadCustomTerrains(Context context) {
        // 移除局部变量定义：DBHelper dbHelper = DBHelper.getInstance(context);
        // 直接使用成员变量dbHelper
        int userId = MyApplication.currentUserId;
        if (userId == -1) return;

        // 使用成员变量dbHelper查询数据
        List<Map<String, Object>> customTerrains = dbHelper.getUserCustomTerrains(userId);
        for (Map<String, Object> terrain : customTerrains) {
            int x = (int) terrain.get("area_x");
            int y = (int) terrain.get("area_y");
            String terrainType = (String) terrain.get("terrain_type");
            updateTerrain(x, y, terrainType);
        }
    }

    /**
     * 判断地形是否为障碍物
     * 可根据实际游戏需求修改障碍物类型列表
     */
    public boolean isObstacle(int areaX, int areaY) {
        // 先校验坐标是否在边界内（复用已有方法）
        if (!isCoordInBoundary(areaX, areaY)) {
            return true; // 边界外视为障碍物
        }
        // 实际地形判断逻辑（示例）
        String terrainType = getTerrainType(areaX, areaY); // 假设有获取地形类型的方法
        List<String> obstacleTypes = Arrays.asList("岩石", "河流", "山脉");
        return obstacleTypes.contains(terrainType);
    }

    public void loadUserTerrains(int userId) {
        Log.d("GameMapLoad", "开始加载用户地形，userId=" + userId);
        if (dbHelper == null || userId == -1) {
            Log.e("GameMap", "无法加载用户地形：dbHelper为空或用户ID无效");
            return;
        }
        // 1. 从数据库查询用户自定义地形数据
        List<TerrainData> userTerrains = dbHelper.getUserTerrains(userId);
        Log.d("GameMapLoad", "从数据库查询到 " + userTerrains.size() + " 条地形数据（userId=" + userId + "）");
        if (userTerrains.isEmpty()) {
            Log.d("GameMap", "用户 " + userId + " 没有自定义地形数据");
            return;
        }

        // 2. 遍历地形数据，更新到内存中的 mapData
        for (TerrainData data : userTerrains) {
            // 打印从数据库读取的地形类型，确认是"茅草屋"而非常量
            Log.d("GameMap", "加载地形：(" + data.x + "," + data.y + ")，类型=" + data.terrainType);
            boolean updateSuccess = updateTerrain(data.x, data.y, data.terrainType);
            Log.d("GameMap", "加载结果：" + (updateSuccess ? "成功" : "失败"));
        }
    }

    // 辅助类：存储地形坐标和类型（之前定义过可保留）
    public static class TerrainData {
        public int x;
        public int y;
        public String terrainType;

        public TerrainData(int x, int y, String terrainType) {
            this.x = x;
            this.y = y;
            this.terrainType = terrainType;
        }
    }
    /**
     * 随机选择一个有效的复活点
     * 基于 isValidCoord 筛选合法坐标（边界内 + 合法复活地形）
     * @return 复活点坐标 [x, y]，若无有效坐标则返回默认值
     */
    public int[] chooseRandomSpawnPoint() {
        List<int[]> validSpawnPoints = new ArrayList<>();

        // 遍历地图所有坐标，筛选有效复活点
        for (int x = minCoord; x <= maxCoord; x++) {
            for (int y = minCoord; y <= maxCoord; y++) {
                if (isValidCoord(x, y)) { // 复用已有校验逻辑（边界 + 合法地形）
                    validSpawnPoints.add(new int[]{x, y});
                }
            }
        }

        // 随机选择一个有效坐标
        if (!validSpawnPoints.isEmpty()) {
            Random random = new Random();
            return validSpawnPoints.get(random.nextInt(validSpawnPoints.size()));
        } else {
            // 极端情况：无有效复活点时，返回地图中心或默认坐标（确保在边界内）
            Log.w("GameMap", "未找到有效复活点，使用默认坐标");
            int defaultX = (minCoord + maxCoord) / 2; // 中心X
            int defaultY = (minCoord + maxCoord) / 2; // 中心Y
            return new int[]{defaultX, defaultY};
        }
    }

    /**
     * 加载当前用户的地图数据
     */
    private void loadCurrentMapData(int userId) {
        String currentMap = dbHelper.getCurrentMap(userId);
        this.currentMapType = currentMap;
        
        if ("fantasy_continent".equals(currentMap)) {
            this.mapData = GameMapData.FANTASY_CONTINENT_MAP;
            Log.d("GameMap", "加载奇幻大陆地图");
        } else {
            this.mapData = Constant.MAP_DATA;
            Log.d("GameMap", "加载主世界地图");
        }
    }

    /**
     * 切换地图
     */
    public boolean switchMap(int userId, String targetMap) {
        if (!"main_world".equals(targetMap) && !"fantasy_continent".equals(targetMap)) {
            Log.e("GameMap", "无效的地图类型: " + targetMap);
            return false;
        }
        
        // 根据目标地图调用相应的传送方法
        boolean success = false;
        if ("fantasy_continent".equals(targetMap)) {
            success = dbHelper.teleportToFantasyContinent(userId);
        } else {
            success = dbHelper.teleportToMainWorld(userId);
        }
        
        if (success) {
            this.currentMapType = targetMap;
            
            // 重新加载地图数据
            if ("fantasy_continent".equals(targetMap)) {
                this.mapData = GameMapData.FANTASY_CONTINENT_MAP;
            } else {
                this.mapData = Constant.MAP_DATA;
            }
            
            Log.d("GameMap", "成功切换到地图: " + targetMap);
            return true;
        }
        
        Log.e("GameMap", "切换地图失败");
        return false;
    }

    /**
     * 获取当前地图类型
     */
    public String getCurrentMapType() {
        return currentMapType;
    }

    /**
     * 检查当前位置是否有传送门
     */
    public boolean hasPortalAtCurrentPosition(int userId) {
        return dbHelper.checkPortalAtCurrentPosition(userId);
    }

    /**
     * 获取传送门目标位置
     */
    public String getPortalTarget(int userId, int currentX, int currentY) {
        if (dbHelper == null) {
            Log.e("GameMap", "dbHelper为空，无法获取传送门目标");
            return null;
        }
        
        try {
            // 从数据库查询当前位置的传送门配置
            return dbHelper.getPortalTarget(userId, currentX, currentY);
        } catch (Exception e) {
            Log.e("GameMap", "获取传送门目标失败", e);
            return null;
        }
    }

    /**
     * 切换地图类型
     */
    public void switchMapType(String newMapType) {
        if (newMapType == null || newMapType.isEmpty()) {
            Log.e("GameMap", "无效的地图类型");
            return;
        }
        
        this.currentMapType = newMapType;
        Log.d("GameMap", "地图类型已切换为: " + newMapType);
        
        // 如果需要，可以重新加载地图数据
        // resetMap();
    }

}