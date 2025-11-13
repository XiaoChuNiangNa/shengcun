package com.example.myapplication3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import com.example.myapplication3.R;
import com.example.myapplication3.Constant;
import com.example.myapplication3.ItemConstants;

import java.util.HashMap;
import java.util.Map;

public class BuildingActivity extends BaseActivity {

    private GameMap gameMap;
    private int currentX, currentY;
    private GameBackgroundView backgroundView;
    private static final String[] BUILDING_ORDER = {
            Constant.BUILDING_FIRE,
            Constant.BUILDING_FURNACE,
            Constant.BUILDING_STORAGE,
            Constant.BUILDING_THATCH_HOUSE,
            Constant.BUILDING_PORTAL
    };
    private static final Map<String, String> HOUSE_UPGRADE_CHAIN = new HashMap<String, String>() {{
        put(Constant.BUILDING_THATCH_HOUSE, "小木屋");
        put("小木屋", "小石屋");
        put("小石屋", "砖瓦屋");
    }};
    private static final String[] HOUSE_LEVELS = {
            Constant.BUILDING_THATCH_HOUSE, "小木屋", "小石屋", "砖瓦屋"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

        gameMap = GameMap.getInstance(this);
        initCurrentCoordinates();
        backgroundView = findViewById(R.id.backgroundView);
        backgroundView.setCurrentCoord(currentX, currentY);

        LinearLayout container = findViewById(R.id.container);
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // 检查是否从基地进入，如果是则返回基地
            if (isFromBase()) {
                startActivity(new Intent(BuildingActivity.this, BaseActivity.class));
                finish();
            } else {
                finish();
            }
        });

        initBuildingList(container);
        initHouseUpgradeOptions(container);
    }

    private void initCurrentCoordinates() {
        Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
        // 确保坐标有默认值（避免NULL）
        currentX = (int) userStatus.getOrDefault("current_x", 0);
        currentY = (int) userStatus.getOrDefault("current_y", 0);
        // 日志验证坐标是否有效
        Log.d("BuildingCoord", "当前建造坐标：(" + currentX + "," + currentY + ")");
        // 若坐标超出地图边界，强制修正（避免无效值）
        if (currentX < Constant.MAP_MIN || currentX > Constant.MAP_MAX) {
            currentX = Constant.MAP_MIN;
        }
        if (currentY < Constant.MAP_MIN || currentY > Constant.MAP_MAX) {
            currentY = Constant.MAP_MIN;
        }
    }

    /**
     * 修复：初始化建造列表时过滤低级房屋选项
     */
    private void initBuildingList(LinearLayout container) {
        // 1. 获取当前位置的最高级房屋（用于过滤低级选项）
        String currentMaxHouse = getCurrentMaxHouseAtPos();
        // 2. 遍历所有建筑类型
        for (String buildingType : BUILDING_ORDER) {
            // 3. 只对房屋类建筑进行等级过滤（非房屋类正常显示）
            boolean isHouse = isShelterBuilding(buildingType);
            if (isHouse && currentMaxHouse != null) {
                // 3.1 计算当前遍历的房屋等级和最高房屋等级
                int currentHouseLevel = getHouseLevel(buildingType);
                int maxHouseLevel = getHouseLevel(currentMaxHouse);
                // 3.2 跳过低于或等于当前最高等级的房屋（避免重复显示低级选项）
                if (currentHouseLevel <= maxHouseLevel) {
                    continue;
                }
            }

            // 4. 原有逻辑：检查当前位置是否已有该类型建筑
            boolean hasBuiltAtCurrentPos = dbHelper.hasBuildingOfTypeAt(userId, currentX, currentY, buildingType);
            String name = getBuildingName(buildingType);
            Map<String, Integer> req = getBuildingRequirements(buildingType);
            addBuildingItem(container, buildingType, name, req, hasBuiltAtCurrentPos ? 1 : 0);
        }
    }

    private void initHouseUpgradeOptions(LinearLayout container) {
        String currentHouse = getCurrentMaxHouseAtPos(); // 检查当前位置的最高级房屋
        if (currentHouse == null) return;

        String nextHouse = HOUSE_UPGRADE_CHAIN.get(currentHouse);
        if (nextHouse == null) return;

        String currentTerrain = gameMap.getTerrainType(currentX, currentY);
        boolean isOnUpgradeableTerrain = currentTerrain.equals(currentHouse);

        if (isOnUpgradeableTerrain) {
            Map<String, Integer> req = getHouseUpgradeRequirements(nextHouse);
            String desc = nextHouse + "（升级自" + currentHouse + "）";
            addUpgradeOption(container, nextHouse, desc, req, currentHouse);
        } else {
            addTerrainRestrictionHint(container);
        }
    }

    // 修复：检查当前位置的最高级房屋（而非全局计数）
    private String getCurrentMaxHouseAtPos() {
        if (dbHelper.hasBuildingOfTypeAt(userId, currentX, currentY, "砖瓦屋")) {
            return "砖瓦屋";
        } else if (dbHelper.hasBuildingOfTypeAt(userId, currentX, currentY, "小石屋")) {
            return "小石屋";
        } else if (dbHelper.hasBuildingOfTypeAt(userId, currentX, currentY, "小木屋")) {
            return "小木屋";
        } else if (dbHelper.hasBuildingOfTypeAt(userId, currentX, currentY, Constant.BUILDING_THATCH_HOUSE)) {
            return Constant.BUILDING_THATCH_HOUSE;
        }
        return null;
    }

    /**
     * 新增：根据房屋类型获取等级（用于过滤逻辑）
     */
    private int getHouseLevel(String houseType) {
        for (int i = 0; i < HOUSE_LEVELS.length; i++) {
            if (HOUSE_LEVELS[i].equals(houseType)) {
                return i; // 返回等级索引（0=最低，3=最高）
            }
        }
        return -1; // 非房屋类型返回-1
    }

    private void addTerrainRestrictionHint(LinearLayout container) {
        LinearLayout item = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.item_building, container, false);
        TextView tvName = item.findViewById(R.id.tv_name);
        TextView tvReq = item.findViewById(R.id.tv_requirements);
        Button btnBuild = item.findViewById(R.id.btn_build);

        tvName.setText("无法升级房屋");
        tvReq.setText("提示：需在当前房屋地形上才能升级");
        btnBuild.setVisibility(View.GONE);
        container.addView(item);
    }

    private String getBuildingName(String type) {
        switch (type) {
            case Constant.BUILDING_FIRE: return "篝火";
            case Constant.BUILDING_FURNACE: return "熔炉";
            case Constant.BUILDING_STORAGE: return "仓库";
            case Constant.BUILDING_THATCH_HOUSE: return "茅草屋";
            case Constant.BUILDING_PORTAL: return "传送门";
            case "小木屋": return "小木屋";
            case "小石屋": return "小石屋";
            case "砖瓦屋": return "砖瓦屋";
            default: return type;
        }
    }

    private Map<String, Integer> getBuildingRequirements(String type) {
        switch (type) {
            case Constant.BUILDING_FIRE:
                return BuildingRequirements.createFireReq(); // 调用新类方法
            case Constant.BUILDING_FURNACE:
                return BuildingRequirements.createFurnaceReq(); // 调用新类方法
            case Constant.BUILDING_STORAGE:
                return BuildingRequirements.createStorageReq(); // 调用新类方法
            case Constant.BUILDING_THATCH_HOUSE:
                return BuildingRequirements.createThatchHouseReq(); // 调用新类方法
            case Constant.BUILDING_PORTAL:
                return BuildingRequirements.createPortalReq(); // 调用新类方法
            default:
                return new HashMap<>();
        }
    }

    private Map<String, Integer> getHouseUpgradeRequirements(String type) {
        switch (type) {
            case "小木屋":
                return BuildingRequirements.createSmallWoodenHouseReq(); // 调用新类方法
            case "小石屋":
                return BuildingRequirements.createSmallStoneHouseReq(); // 调用新类方法
            case "砖瓦屋":
                return BuildingRequirements.createBrickHouseReq(); // 调用新类方法
            default:
                return new HashMap<>();
        }
    }

    private void addBuildingItem(LinearLayout container, String type, String name,
                                 Map<String, Integer> requirements, int count) {
        LinearLayout item = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.item_building, container, false);
        TextView tvName = item.findViewById(R.id.tv_name);
        TextView tvReq = item.findViewById(R.id.tv_requirements);
        TextView tvCount = item.findViewById(R.id.tv_count);
        Button btnBuild = item.findViewById(R.id.btn_build);

        tvName.setText(name);
        tvReq.setText(getRequirementsText(requirements));
        tvCount.setText("当前：" + count);

        if (count > 0) {
            btnBuild.setText("已建造");
            btnBuild.setClickable(false);
            btnBuild.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            btnBuild.setText("建造");
            btnBuild.setOnClickListener(v -> build(type, requirements, tvCount, btnBuild));
        }

        container.addView(item);
    }

    /**
     * 建造建筑的核心逻辑（修复重复建造判断逻辑）
     */
    private void build(String type, Map<String, Integer> requirements, TextView tvCount, Button btnBuild) {
        // 检查资源是否满足
        if (!checkResources(requirements)) {
            Toast.makeText(this, "资源不足，无法建造", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查地形限制
        String currentTerrain = gameMap.getTerrainType(currentX, currentY);
        if (!canBuildOnTerrain(type, currentTerrain)) {
            String buildingName = getBuildingName(type);
            String terrainName = getTerrainDisplayName(currentTerrain);
            
            // 根据建筑分类显示不同的错误提示
            if (isIndoorBuilding(type)) {
                Toast.makeText(this, buildingName + "是室内建筑，只能建造在庇护所地形内（茅草屋、小木屋、小石屋、砖瓦屋）", Toast.LENGTH_LONG).show();
            } else if (isOutdoorBuilding(type)) {
                Toast.makeText(this, buildingName + "是野外建筑，只能建造在非庇护所地形", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, buildingName + "无法在当前地形建造", Toast.LENGTH_LONG).show();
            }
            return;
        }

        // 检查当前位置是否允许建造
        if (!canBuildAtCurrentPosition(type)) {
            String buildingName = getBuildingName(type);
            String existingBuildingType = dbHelper.getBuildingTypeAt(userId, currentX, currentY);
            String existingBuildingName = getBuildingName(existingBuildingType);
            
            // 根据建筑分类显示不同的错误提示
            if (isOutdoorBuilding(type)) {
                Toast.makeText(this, "野外建筑" + buildingName + "每个地形只能建造一个，该位置已有" + existingBuildingName, Toast.LENGTH_LONG).show();
            } else if (isIndoorBuilding(type)) {
                Toast.makeText(this, "室内建筑" + buildingName + "只能在庇护所地形内建造，且该位置已有" + existingBuildingName, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "该位置已建造" + existingBuildingName + "，无法建造" + buildingName, Toast.LENGTH_LONG).show();
            }
            return;
        }

        // 开始建造日志
        Log.i("BuildingLog", "=== 开始建造建筑 ===");
        Log.i("BuildingLog", "建筑类型: " + type + ", 坐标: (" + currentX + ", " + currentY + "), 用户ID: " + userId);
        Log.i("BuildingLog", "资源需求: " + requirements.toString());
        Log.i("BuildingLog", "当前地形: " + currentTerrain);

        new AsyncTask<Void, Void, Boolean>() {
            private String errorMsg; // 用于记录后台任务中的错误信息

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    // 修复：根据建筑分类检查是否允许建造
                    Log.i("BuildingLog", "检查当前位置是否允许建造建筑...");
                    boolean hasAnyBuilding = dbHelper.hasAnyBuildingAt(userId, currentX, currentY);
                    Log.d("BuildingLog", "当前位置建筑检查结果: " + (hasAnyBuilding ? "已存在建筑" : "可建造"));
                    
                    if (hasAnyBuilding) {
                        // 获取当前位置的建筑类型
                        String existingBuildingType = dbHelper.getBuildingTypeAt(userId, currentX, currentY);
                        
                        // 根据建筑分类进行判断
                        if (isOutdoorBuilding(type)) {
                            // 野外建筑（庇护所、传送门）：每个地形只能建造一个
                            errorMsg = "野外建筑" + getBuildingName(type) + "每个地形只能建造一个，该位置已有" + getBuildingName(existingBuildingType);
                            Log.e("BuildingLog", "野外建筑重复建造错误: " + errorMsg);
                            return false;
                        } else if (isIndoorBuilding(type)) {
                            // 室内建筑（篝火、熔炉、仓库）：每个庇护所内每种类型可以建造一个
                            boolean hasSameTypeBuilding = dbHelper.hasBuildingOfTypeAt(userId, currentX, currentY, type);
                            if (hasSameTypeBuilding) {
                                errorMsg = "室内建筑" + getBuildingName(type) + "每个庇护所内只能建造一个，该位置已有相同类型建筑";
                                Log.e("BuildingLog", "室内建筑重复建造错误: " + errorMsg);
                                return false;
                            }
                            // 检查当前位置是否为庇护所地形
                            String currentTerrain = gameMap.getTerrainType(currentX, currentY);
                            if (!isShelterTerrain(currentTerrain)) {
                                errorMsg = "室内建筑" + getBuildingName(type) + "只能在庇护所地形内建造";
                                Log.e("BuildingLog", "室内建筑地形错误: " + errorMsg);
                                return false;
                            }
                            // 检查现有建筑是否为庇护所建筑
                            if (!isShelterBuilding(existingBuildingType)) {
                                errorMsg = "室内建筑" + getBuildingName(type) + "只能在庇护所建筑内建造，该位置已有" + getBuildingName(existingBuildingType);
                                Log.e("BuildingLog", "室内建筑庇护所错误: " + errorMsg);
                                return false;
                            }
                            // 允许建造：庇护所内可以建造不同类型的室内建筑
                            Log.d("BuildingLog", "允许在庇护所内建造不同类型的室内建筑: " + type);
                        } else {
                            // 其他建筑类型不允许重复建造
                            errorMsg = "该位置已建造" + getBuildingName(existingBuildingType) + "，无法重复建造";
                            Log.e("BuildingLog", "重复建造错误: " + errorMsg);
                            return false;
                        }
                    }

                    // 检查资源是否足够（但不扣除）
                    Log.i("BuildingLog", "检查资源是否足够...");
                    for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
                        String itemType = entry.getKey();
                        int requiredCount = entry.getValue();
                        int currentCount = dbHelper.getItemCount(userId, itemType);
                        Log.d("BuildingLog", "检查资源: " + itemType + ", 当前数量: " + currentCount + ", 需求数量: " + requiredCount);
                        
                        if (currentCount < requiredCount) {
                            errorMsg = "资源不足：" + itemType + "数量不足";
                            Log.e("BuildingLog", "资源不足: " + itemType + " (当前: " + currentCount + ", 需求: " + requiredCount + ")");
                            return false;
                        }
                    }

                    // 扣除资源（在位置检查通过后）
                    Log.i("BuildingLog", "开始扣除资源...");
                    for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
                        String itemType = entry.getKey();
                        int requiredCount = entry.getValue();
                        
                        // 扣除资源
                        dbHelper.updateBackpackItem(userId, itemType, -requiredCount);
                        Log.i("BuildingLog", "资源扣除成功: " + itemType + " -" + requiredCount);
                    }
                    Log.i("BuildingLog", "所有资源扣除完成");

                    // 记录建筑到数据库
                    Log.i("BuildingLog", "开始记录建筑到数据库...");
                    dbHelper.addBuilding(userId, type, currentX, currentY);
                    Log.i("BuildingLog", "建筑数据库记录成功: " + type + " 坐标(" + currentX + ", " + currentY + ")");
                    
                    return true;
                } catch (Exception e) {
                    Log.e("BuildError", "建造过程出错", e);
                    Log.e("BuildingLog", "建造过程异常: " + e.getMessage());
                    errorMsg = "建造失败：" + e.getMessage();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Log.i("BuildingLog", "=== 建筑建造成功 ===");
                    
                    // 若为庇护所类型建筑或传送门，更新地形
                    if (isShelterBuilding(type) || type.equals(Constant.BUILDING_PORTAL)) {
                        String buildingName = getBuildingName(type);
                        String newTerrainType = buildingName;
                        
                        // 传送门建筑特殊处理：地形改为"传送门"
                        if (type.equals(Constant.BUILDING_PORTAL)) {
                            newTerrainType = "传送门";
                        }
                        
                        Log.i("BuildingLog", "开始更新地形...");
                        
                        String oldTerrain = gameMap.getTerrainType(currentX, currentY);
                        Log.d("TerrainUpdate", "更新前地形：" + oldTerrain + " (" + currentX + "," + currentY + ")");

                        boolean updateSuccess = gameMap.updateTerrain(currentX, currentY, newTerrainType);
                        String newTerrain = gameMap.getTerrainType(currentX, currentY);
                        Log.d("TerrainUpdate", "更新结果：" + (updateSuccess ? "成功" : "失败") + "，更新后地形：" + newTerrain);

                        if (updateSuccess) {
                            Log.i("BuildingLog", "内存地形更新成功: " + newTerrainType);
                            backgroundView.invalidate(); // 刷新背景视图
                            Toast.makeText(BuildingActivity.this,
                                    buildingName + "建造成功，地形已更新",
                                    Toast.LENGTH_SHORT).show();
                            
                            // 关键：使用全局用户ID保存地形，确保与加载逻辑一致
                            Log.i("BuildingLog", "开始保存地形到数据库...");
                            dbHelper.saveUserTerrain(MyApplication.currentUserId, currentX, currentY, newTerrainType);
                            Log.i("BuildingLog", "地形数据库保存完成");
                            
                            // 二次确认内存地形更新
                            gameMap.updateTerrain(currentX, currentY, newTerrainType);
                            Log.i("BuildingLog", "地形二次确认更新完成");
                        } else {
                            Log.e("BuildingLog", "内存地形更新失败");
                            Toast.makeText(BuildingActivity.this,
                                    buildingName + "建造成功，但地形更新失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 非庇护所建筑直接提示成功
                        Log.i("BuildingLog", "非庇护所建筑建造完成");
                        Toast.makeText(BuildingActivity.this,
                                getBuildingName(type) + "建造成功",
                                Toast.LENGTH_SHORT).show();
                    }

                    // 更新UI显示
                    Log.i("BuildingLog", "开始更新UI显示...");
                    tvCount.setText("当前：1");
                    btnBuild.setText("已建造");
                    btnBuild.setClickable(false);
                    btnBuild.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    Log.i("BuildingLog", "UI更新完成");

                    // 发送广播通知其他页面更新数据
                    Log.i("BuildingLog", "发送广播通知其他页面更新...");
                    Intent buildIntent = new Intent("com.example.action.UPDATE_BUILDINGS");
                    sendBroadcast(buildIntent);
                    // 发送广播通知主页面更新区域信息
                    Intent areaIntent = new Intent("com.example.action.UPDATE_AREA_INFO");
                    sendBroadcast(areaIntent);
                    Log.i("BuildingLog", "广播发送完成");

                    Log.i("BuildingLog", "=== 建筑建造流程全部完成 ===");

                } else {
                    // 处理失败场景
                    Log.e("BuildingLog", "=== 建筑建造失败 ===");
                    Log.e("BuildingLog", "失败原因: " + errorMsg);
                    String msg = TextUtils.isEmpty(errorMsg) ? "建造失败，请重试" : errorMsg;
                    Toast.makeText(BuildingActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    /**
     * 添加升级选项（完善升级逻辑，确保地形同步）
     */
    private void addUpgradeOption(LinearLayout container, String nextHouse, String desc,
                                  Map<String, Integer> requirements, String currentHouse) {
        LinearLayout item = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.item_building, container, false);
        TextView tvName = item.findViewById(R.id.tv_name);
        TextView tvReq = item.findViewById(R.id.tv_requirements);
        Button btnUpgrade = item.findViewById(R.id.btn_build);

        tvName.setText(desc);
        tvReq.setText(getRequirementsText(requirements));
        btnUpgrade.setText("升级");

        btnUpgrade.setOnClickListener(v -> {
            if (!checkResources(requirements)) {
                Toast.makeText(this, "资源不足，无法升级", Toast.LENGTH_SHORT).show();
                return;
            }

            // 开始升级日志
            Log.i("BuildingLog", "=== 开始升级建筑 ===");
            Log.i("BuildingLog", "升级信息: " + currentHouse + " -> " + nextHouse + ", 坐标: (" + currentX + ", " + currentY + "), 用户ID: " + userId);
            Log.i("BuildingLog", "升级资源需求: " + requirements.toString());

            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        // 1. 扣除升级资源（检查资源充足性）
                        Log.i("BuildingLog", "开始扣除升级资源...");
                        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
                            String itemType = entry.getKey();
                            int requiredCount = entry.getValue();
                            int currentCount = dbHelper.getItemCount(userId, itemType);
                            Log.d("BuildingLog", "检查升级资源: " + itemType + ", 当前数量: " + currentCount + ", 需求数量: " + requiredCount);
                            
                            if (currentCount < requiredCount) {
                                Log.e("BuildingLog", "升级资源不足: " + itemType + " (当前: " + currentCount + ", 需求: " + requiredCount + ")");
                                return false;
                            }
                            dbHelper.updateBackpackItem(userId, itemType, -requiredCount);
                            Log.i("BuildingLog", "升级资源扣除成功: " + itemType + " -" + requiredCount);
                        }
                        Log.i("BuildingLog", "所有升级资源扣除完成");

                        // 2. 删除当前位置的旧房屋（确保只删除当前坐标的建筑）
                        Log.i("BuildingLog", "开始删除旧建筑...");
                        boolean isOldRemoved = dbHelper.removeBuildingAt(userId, currentX, currentY, currentHouse);
                        if (!isOldRemoved) {
                            Log.e("UpgradeError", "旧建筑删除失败：" + currentHouse + " (" + currentX + "," + currentY + ")");
                            Log.e("BuildingLog", "旧建筑删除失败: " + currentHouse + " 坐标(" + currentX + ", " + currentY + ")");
                            return false;
                        }
                        Log.i("BuildingLog", "旧建筑删除成功: " + currentHouse);

                        // 3. 添加新房屋到当前位置
                        Log.i("BuildingLog", "开始添加新建筑到数据库...");
                        dbHelper.addBuilding(userId, nextHouse, currentX, currentY);
                        Log.i("BuildingLog", "新建筑数据库记录成功: " + nextHouse + " 坐标(" + currentX + ", " + currentY + ")");

                        // 4. 同步更新数据库地形（使用全局用户ID）
                        Log.i("BuildingLog", "开始更新数据库地形...");
                        dbHelper.saveUserTerrain(MyApplication.currentUserId, currentX, currentY, nextHouse);
                        Log.i("BuildingLog", "数据库地形更新完成");

                        Log.i("BuildingLog", "=== 建筑升级数据库操作全部完成 ===");
                        return true;
                    } catch (Exception e) {
                        Log.e("UpgradeError", "升级失败", e);
                        Log.e("BuildingLog", "建筑升级过程异常: " + e.getMessage());
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    if (success) {
                        Log.i("BuildingLog", "=== 建筑升级成功 ===");
                        
                        // 1. 更新内存地形
                        Log.i("BuildingLog", "开始更新内存地形...");
                        boolean updateSuccess = gameMap.updateTerrain(currentX, currentY, nextHouse);
                        String newTerrain = gameMap.getTerrainType(currentX, currentY);
                        Log.d("UpgradeTerrain", "升级后地形：" + newTerrain + "（更新结果：" + updateSuccess + "）");

                        // 2. 刷新背景和UI
                        Log.i("BuildingLog", "刷新背景和UI...");
                        backgroundView.invalidate();
                        Toast.makeText(BuildingActivity.this, nextHouse + "升级成功", Toast.LENGTH_SHORT).show();

                        // 3. 发送广播通知全局更新
                        Log.i("BuildingLog", "发送广播通知全局更新...");
                        Intent areaIntent = new Intent("com.example.action.UPDATE_AREA_INFO");
                        sendBroadcast(areaIntent);

                        // 4. 重新初始化建造/升级列表（确保选项同步）
                        Log.i("BuildingLog", "重新初始化建造/升级列表...");
                        container.removeAllViews();
                        initBuildingList(container);
                        initHouseUpgradeOptions(container);
                        Log.i("BuildingLog", "UI更新完成");

                        Log.i("BuildingLog", "=== 建筑升级流程全部完成 ===");
                    } else {
                        Log.e("BuildingLog", "=== 建筑升级失败 ===");
                        Toast.makeText(BuildingActivity.this, "升级失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        });

        container.addView(item);
    }

    // 辅助方法：检查资源是否满足
    private boolean checkResources(Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            int current = dbHelper.getItemCount(userId, entry.getKey());
            if (current < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    // 辅助方法：格式化需求文本
    private String getRequirementsText(Map<String, Integer> requirements) {
        StringBuilder sb = new StringBuilder("需求：");
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            sb.append(entry.getKey()).append(" x").append(entry.getValue()).append("，");
        }
        if (sb.length() > 2) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    // 判断是否为庇护所类型建筑（包含所有房屋等级）
    private boolean isShelterBuilding(String type) {
        return type.equals(Constant.BUILDING_THATCH_HOUSE)
                || type.equals("小木屋")
                || type.equals("小石屋")
                || type.equals("砖瓦屋");
    }

    // 判断是否为庇护所地形（统一庇护所类）
    private boolean isShelterTerrain(String terrainType) {
        return isShelterBuilding(terrainType);
    }

    // 判断建筑是否为室内建筑
    private boolean isIndoorBuilding(String buildingType) {
        return buildingType.equals(Constant.BUILDING_FIRE) || 
               buildingType.equals(Constant.BUILDING_FURNACE) || 
               buildingType.equals(Constant.BUILDING_STORAGE);
    }

    // 判断建筑是否为野外建筑
    private boolean isOutdoorBuilding(String buildingType) {
        return buildingType.equals(Constant.BUILDING_PORTAL) || 
               isShelterBuilding(buildingType);
    }

    // 判断是否为不允许建造的地形（村落、水域、沼泽等）
    private boolean isForbiddenTerrain(String terrainType) {
        return terrainType.equals("村落") || 
               terrainType.equals("河流") || 
               terrainType.equals("海洋") || 
               terrainType.equals("深海") || 
               terrainType.equals("沼泽");
    }


    // 检查建筑是否可以在指定地形上建造（新分类逻辑）
    private boolean canBuildOnTerrain(String buildingType, String terrainType) {
        // 首先检查是否为不允许建造的地形（村落、水域、沼泽等）
        if (isForbiddenTerrain(terrainType)) {
            return false;
        }
        
        // 室内建筑（仓库、熔炉、篝火）只能建造在庇护所地形内
        if (isIndoorBuilding(buildingType)) {
            return isShelterTerrain(terrainType);
        }
        
        // 野外建筑（传送门、庇护所）只能建造在非庇护所地形
        if (isOutdoorBuilding(buildingType)) {
            return !isShelterTerrain(terrainType);
        }
        
        // 其他建筑默认允许建造
        return true;
    }

    // 检查是否允许在当前位置建造（根据建筑分类：野外建筑每个地形只能一个，室内建筑庇护所内每种类型可建造一个）
    private boolean canBuildAtCurrentPosition(String buildingType) {
        // 检查当前位置是否有建筑
        boolean hasAnyBuilding = dbHelper.hasAnyBuildingAt(userId, currentX, currentY);
        
        if (!hasAnyBuilding) {
            // 当前位置没有建筑，可以建造
            return true;
        }
        
        // 当前位置已有建筑，根据建筑分类进行判断
        String currentTerrain = gameMap.getTerrainType(currentX, currentY);
        String existingBuildingType = dbHelper.getBuildingTypeAt(userId, currentX, currentY);
        
        // 1. 如果是野外建筑（庇护所、传送门），每个地形只能建造一个
        if (isOutdoorBuilding(buildingType)) {
            // 野外建筑不允许在已有建筑的地形上建造
            return false;
        }
        
        // 2. 如果是室内建筑（篝火、熔炉、仓库），每个庇护所内每种类型可以建造一个
        if (isIndoorBuilding(buildingType)) {
            // 检查当前位置是否为庇护所地形
            if (isShelterTerrain(currentTerrain)) {
                // 进一步检查：当前位置的现有建筑必须是庇护所建筑
                if (isShelterBuilding(existingBuildingType)) {
                    // 检查是否已存在相同类型的室内建筑
                    boolean hasSameTypeBuilding = dbHelper.hasBuildingOfTypeAt(userId, currentX, currentY, buildingType);
                    if (hasSameTypeBuilding) {
                        // 已存在相同类型的室内建筑，不允许重复建造
                        return false;
                    }
                    // 庇护所内允许建造不同类型的室内建筑
                    return true;
                }
            }
        }
        
        // 其他情况不允许建造
        return false;
    }

    // 获取地形显示名称
    private String getTerrainDisplayName(String terrainType) {
        if (isShelterBuilding(terrainType)) {
            return "庇护所地形（" + terrainType + "）";
        }
        return terrainType;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 保存当前状态
        saveCurrentState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        cleanupResources();
    }

    private void saveCurrentState() {
        // 保存当前建筑状态到数据库
        new BuildingStateSaveTask(this).execute();
    }

    private void cleanupResources() {
        // 清理资源，GameBackgroundView 没有 cleanup() 方法
        // 如果需要清理背景视图资源，可以调用 invalidate() 或直接设置为 null
        if (backgroundView != null) {
            backgroundView.invalidate();
        }
    }

    /**
     * 检查是否从基地进入
     */
    protected boolean isFromBase() {
        return getIntent().getBooleanExtra("from_base", false);
    }

    /**
     * 禁用系统返回键，只允许使用按钮返回
     */
    @Override
    public void onBackPressed() {
        // 空实现，禁用系统返回功能
        // 用户只能通过页面上的返回按钮返回
    }

    // 安全的异步任务类 - 建筑状态保存
    private static class BuildingStateSaveTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<BuildingActivity> activityRef;

        BuildingStateSaveTask(BuildingActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            BuildingActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                try {
                    // 保存当前坐标和建筑状态
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("current_x", activity.currentX);
                    updates.put("current_y", activity.currentY);
                    return activity.dbHelper.updateUserStatus(MyApplication.currentUserId, updates);
                } catch (Exception e) {
                    Log.e("BuildingActivity", "保存状态失败: " + e.getMessage());
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            BuildingActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                if (!success) {
                    Log.w("BuildingActivity", "建筑状态保存失败");
                }
            }
        }
    }
}