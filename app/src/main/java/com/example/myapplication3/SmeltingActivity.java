package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class SmeltingActivity extends BaseActivity {
    protected Map<String, Integer> backpack; // 添加背包变量声明

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化背包数据
        backpack = dbHelper.getBackpack(userId);

        // 直接初始化活动，无需建筑检查
        initActivity();
    }

    /**
     * 初始化活动界面
     */
    private void initActivity() {
        setContentView(R.layout.activity_smelting);

        LinearLayout container = findViewById(R.id.container);
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // 检查是否从基地进入，如果是则返回基地
            if (isFromBase()) {
                startActivity(new Intent(SmeltingActivity.this, BaseActivity.class));
                finish();
            } else {
                finish();
            }
        });

        addSmeltItem(container, ItemConstants.ITEM_CHARCOAL, createCharcoalReq());
        addSmeltItem(container, ItemConstants.ITEM_IRON_INGOT, createIronIngotReq());
        addSmeltItem(container, ItemConstants.ITEM_DIAMOND, createDiamondReq());
        addSmeltItem(container, ItemConstants.ITEM_CLAY_POT, createClayPotReq());
        addSmeltItem(container, ItemConstants.ITEM_BOILED_WATER, createBoiledWaterReq());
        addSmeltItem(container, ItemConstants.ITEM_GLASS, createGlassReq()); // 新增玻璃配方
    }

    // 木炭
    private Map<String, Integer> createCharcoalReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WOOD, 2);
        req.put(ItemConstants.ITEM_WEED, 2);
        return req;
    }

    // 铁锭
    private Map<String, Integer> createIronIngotReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_IRON_ORE, 2);
        req.put("FUEL", 1); // 用特殊标记表示需要燃料（木炭/煤炭）
        return req;
    }

    // 钻石
    private Map<String, Integer> createDiamondReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_GEM, 2);
        req.put("FUEL", 1);
        return req;
    }

    // 陶罐
    private Map<String, Integer> createClayPotReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_CLAY, 5);
        req.put("FUEL", 1);
        return req;
    }

    // 开水×5
    private Map<String, Integer> createBoiledWaterReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_WATER, 5);
        req.put(ItemConstants.ITEM_CLAY_POT, 1); // 不消耗
        req.put("FUEL", 1);
        return req;
    }

    // 玻璃
    private Map<String, Integer> createGlassReq() {
        Map<String, Integer> req = new HashMap<>();
        req.put(ItemConstants.ITEM_SAND, 2);
        req.put("FUEL", 1);
        return req;
    }

    private void addSmeltItem(LinearLayout container, String name, Map<String, Integer> requirements) {
        LinearLayout item = (LinearLayout) getLayoutInflater().inflate(R.layout.item_smelting, container, false);
        TextView tvName = item.findViewById(R.id.tv_name);
        TextView tvReq = item.findViewById(R.id.tv_requirements);
        Button btnSmelt = item.findViewById(R.id.btn_smelt);

        tvName.setText(name);
        tvReq.setText(getRequirementsText(requirements));

        btnSmelt.setOnClickListener(v -> smelt(name, requirements));
        container.addView(item);
    }

    private String getRequirementsText(Map<String, Integer> req) {
        StringBuilder sb = new StringBuilder("需求：");
        for (Map.Entry<String, Integer> entry : req.entrySet()) {
            if (entry.getKey().equals("FUEL")) {
                sb.append(entry.getValue()).append("个木炭或煤炭，");
            } else {
                sb.append(entry.getValue()).append("个").append(entry.getKey()).append("，");
            }
        }
        // 移除最后一个逗号
        if (sb.length() > 2) {
            return sb.substring(0, sb.length() - 1);
        }
        return sb.toString();
    }

    private void smelt(String name, Map<String, Integer> requirements) {
        if (!checkMaterials(requirements)) return;

        deductMaterials(requirements);
        // 检查是否从基地进入，如果是则放入仓库
        if (isFromBase()) {
            // 基地中熔炼物品直接放入仓库
            if (name.equals(ItemConstants.ITEM_BOILED_WATER)) {
                // 陶罐不消耗，手动加回
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_CLAY_POT, 1);
                dbHelper.updateWarehouseItem(userId, name, 5);
            } else {
                dbHelper.updateWarehouseItem(userId, name, 1);
            }
            showToast(name + "熔炼成功！物品已放入仓库。");
        } else {
            // 游戏中熔炼物品放入背包
            if (name.equals(ItemConstants.ITEM_BOILED_WATER)) {
                // 陶罐不消耗，手动加回
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_CLAY_POT, 1);
                dbHelper.updateBackpackItem(userId, name, 5);
            } else {
                dbHelper.updateBackpackItem(userId, name, 1);
            }
            
            // 增加熔炼次数
            int newSmeltingCount = dbHelper.incrementSmeltingTimes(userId);
            
            showToast(name + "熔炼成功！\n熔炼次数: " + newSmeltingCount);
            
            // 添加日志到Logcat
            Log.d("SmeltingActivity", "熔炼次数已更新: " + newSmeltingCount);
        }
    }

    // 检查材料是否足够
    protected boolean checkMaterials(Map<String, Integer> requirements) {
        // 新增：检查背包是否为空
        if (backpack == null) {
            showToast("背包数据加载失败");
            return false;
        }

        // 检查普通材料
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String item = entry.getKey();
            int required = entry.getValue();

            // 跳过燃料检查，后面单独处理
            if (item.equals("FUEL")) continue;

            // 使用backpack map获取数量
            int count = backpack.getOrDefault(item, 0);
            if (count < required) {
                showToast("缺少" + (required - count) + "个" + item);
                return false;
            }
        }

        // 检查燃料（木炭或煤炭，修复总和判断逻辑）
        if (requirements.containsKey("FUEL")) {
            int fuelNeed = requirements.get("FUEL");
            int charcoalCount = backpack.getOrDefault(ItemConstants.ITEM_CHARCOAL, 0);
            int coalCount = backpack.getOrDefault(ItemConstants.ITEM_COAL, 0);

            // 修复：改为检查总和是否满足需求
            if (charcoalCount + coalCount < fuelNeed) {
                showToast("缺少" + fuelNeed + "个木炭或煤炭（当前共" + (charcoalCount + coalCount) + "个）");
                return false;
            }
        }

        return true;
    }

    // 扣除材料
    protected void deductMaterials(Map<String, Integer> requirements) {
        // 扣除普通材料
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String item = entry.getKey();
            int count = entry.getValue();

            // 跳过燃料处理
            if (item.equals("FUEL")) continue;

            dbHelper.updateBackpackItem(userId, item, -count);
        }

        // 处理燃料扣除（优先使用数量较少的燃料）
        if (requirements.containsKey("FUEL")) {
            int fuelNeed = requirements.get("FUEL");
            // 修正：使用backpack map获取数量
            int charcoalCount = backpack.getOrDefault(ItemConstants.ITEM_CHARCOAL, 0);
            int coalCount = backpack.getOrDefault(ItemConstants.ITEM_COAL, 0);

            if (charcoalCount <= coalCount && charcoalCount >= fuelNeed) {
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_CHARCOAL, -fuelNeed);
            } else {
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_COAL, -fuelNeed);
            }
        }
        // 同步更新内存中的背包数据
        backpack = dbHelper.getBackpack(userId);
    }

    /**
     * 检查是否从基地进入
     */
    protected boolean isFromBase() {
        return getIntent().getBooleanExtra("from_base", false);
    }


}