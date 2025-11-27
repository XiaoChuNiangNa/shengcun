package com.example.myapplication3;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 战利品箱管理界面
 * 显示背包中的战利品箱，支持开启和查看详情
 */
public class LootBoxActivity extends AppCompatActivity {
    private static final String TAG = "LootBoxActivity";
    
    private ListView listViewLootBoxes;
    private TextView tvInventoryInfo;
    private Spinner spinnerSort;
    private LootBoxInventory inventory;
    private LootBoxArrayAdapter adapter;
    private List<LootBoxInventory.LootBoxItem> currentList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 创建简单的线性布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);
        
        // 添加一个文本视图作为占位符
        TextView placeholder = new TextView(this);
        placeholder.setText("战利品箱界面 - 暂未实现完整功能");
        placeholder.setTextSize(18);
        placeholder.setPadding(20, 20, 20, 20);
        layout.addView(placeholder);
        
        // 初始化ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("战利品箱背包");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    // 视图变量初始化已在onCreate中处理
    private void initInventory() {
        try {
            inventory = LootBoxInventory.getInstance(this);
        } catch (Exception e) {
            Log.e(TAG, "初始化战利品箱背包失败", e);
            Toast.makeText(this, "背包初始化失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    
    private void setupSpinner() {
        String[] sortOptions = {"按获得时间", "按稀有度", "按名称"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, sortOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);
        
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortLootBoxes(position);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void loadLootBoxes() {
        currentList = inventory.getAllLootBoxes();
        adapter = new LootBoxArrayAdapter(this, currentList);
        listViewLootBoxes.setAdapter(adapter);
        
        updateInventoryInfo();
        
        // 设置点击事件
        listViewLootBoxes.setOnItemClickListener((parent, view, position, id) -> {
            LootBoxInventory.LootBoxItem item = currentList.get(position);
            showLootBoxOptions(item);
        });
        
        listViewLootBoxes.setOnItemLongClickListener((parent, view, position, id) -> {
            LootBoxInventory.LootBoxItem item = currentList.get(position);
            showLootBoxDetails(item);
            return true;
        });
    }
    
    private void updateInventoryInfo() {
        Map<String, Object> stats = inventory.getInventoryStats();
        int usedSlots = (Integer) stats.get("usedSlots");
        int maxSlots = (Integer) stats.get("totalSlots");
        int totalLootBoxes = (Integer) stats.get("totalLootBoxes");
        
        String info = String.format("背包容量: %d/%d | 战利品箱: %d个", 
                usedSlots, maxSlots, totalLootBoxes);
        tvInventoryInfo.setText(info);
    }
    
    private void sortLootBoxes(int sortType) {
        if (currentList == null || currentList.isEmpty()) {
            return;
        }
        
        switch (sortType) {
            case 0: // 按获得时间
                currentList.sort((a, b) -> Long.compare(b.getObtainedTime(), a.getObtainedTime()));
                break;
            case 1: // 按稀有度
                currentList.sort((a, b) -> Integer.compare(b.getRarity().ordinal(), a.getRarity().ordinal()));
                break;
            case 2: // 按名称
                currentList.sort((a, b) -> a.getBoxName().compareTo(b.getBoxName()));
                break;
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    
    private void showLootBoxOptions(LootBoxInventory.LootBoxItem item) {
        String[] options = {"开启战利品箱", "查看详情", "查看掉落预览"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getBoxName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openLootBox(item);
                            break;
                        case 1:
                            showLootBoxDetails(item);
                            break;
                        case 2:
                            showDropPreview(item);
                            break;
                    }
                })
                .show();
    }
    
    private void openLootBox(LootBoxInventory.LootBoxItem item) {
        // 获取当前难度
        String difficulty = getCurrentDifficulty();
        
        // 开启战利品箱
        List<Item> droppedItems = inventory.useLootBox(item.getId(), difficulty);
        
        if (droppedItems == null) {
            Toast.makeText(this, "开启失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示开启结果
        showOpenResult(item, droppedItems, difficulty);
        
        // 刷新列表
        loadLootBoxes();
    }
    
    private void showOpenResult(LootBoxInventory.LootBoxItem item, List<Item> items, String difficulty) {
        StringBuilder result = new StringBuilder();
        result.append("成功开启 ").append(item.getBoxName()).append("！\n\n");
        result.append("难度: ").append(difficulty).append("\n");
        result.append("获得物品:\n");
        
        if (items.isEmpty()) {
            result.append("  (没有物品)\n");
        } else {
            for (Item dropItem : items) {
                result.append("  • ").append(dropItem.toString()).append("\n");
            }
        }
        
        int totalValue = LootBoxUtils.calculateItemsValue(items);
        result.append("\n总价值: ").append(totalValue);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("战利品箱开启结果")
                .setMessage(result.toString())
                .setPositiveButton("确定", null)
                .setNeutralButton("添加到背包", (dialog, which) -> {
                    addDroppedItemsToBackpack(items);
                })
                .show();
    }
    
    private void addDroppedItemsToBackpack(List<Item> items) {
        DBHelper dbHelper = DBHelper.getInstance(this);
        int userId = getCurrentUserId();
        boolean allAdded = true;
        
        for (Item item : items) {
            boolean success = dbHelper.updateBackpackItem(userId, item.getName(), item.getAmount());
            if (!success) {
                allAdded = false;
                Log.w(TAG, "添加物品失败: " + item.getName());
            }
        }
        
        if (allAdded) {
            Toast.makeText(this, "所有物品已添加到背包", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "部分物品添加失败，请检查背包容量", Toast.LENGTH_LONG).show();
        }
    }
    
    private int getCurrentUserId() {
        // 优先使用MyApplication中的全局用户ID
        if (MyApplication.currentUserId != -1) {
            return MyApplication.currentUserId;
        }
        
        // 备用方案：从SharedPreferences获取
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String userIdStr = prefs.getString("current_user_id", "1");
        try {
            return Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            Log.w(TAG, "用户ID格式错误，使用默认值: " + userIdStr);
            return 1; // 默认用户ID
        }
    }
    
    private void showLootBoxDetails(LootBoxInventory.LootBoxItem item) {
        LootBoxManager manager = LootBoxManager.getInstance();
        LootBox box = manager.getLootBox(item.getBoxId());
        
        if (box == null) {
            Toast.makeText(this, "战利品箱信息获取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("战利品箱详情\n\n");
        details.append("名称: ").append(item.getBoxName()).append("\n");
        details.append("稀有度: ").append(item.getRarity().getDisplayName()).append("\n");
        details.append("获得时间: ").append(item.getFormattedTime()).append("\n");
        details.append("获得来源: ").append(item.getSource()).append("\n\n");
        
        details.append("可能掉落:\n");
        for (var itemDrop : box.getItems()) {
            details.append("  • ").append(itemDrop.getDisplayText()).append("\n");
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("战利品箱详情")
                .setMessage(details.toString())
                .setPositiveButton("确定", null)
                .show();
    }
    
    private void showDropPreview(LootBoxInventory.LootBoxItem item) {
        String difficulty = getCurrentDifficulty();
        String preview = LootBoxUtils.getFullBoxInfo(item.getBoxId());
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("掉落预览 (" + difficulty + ")")
                .setMessage(preview)
                .setPositiveButton("确定", null)
                .show();
    }
    
    private String getCurrentDifficulty() {
        return getSharedPreferences("game_settings", MODE_PRIVATE)
                .getString("difficulty", "normal");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 不使用资源ID，直接使用整数ID
        menu.add(Menu.NONE, 1001, Menu.NONE, "扩展库存");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == 1001) {
            // 简单的扩展库存处理
            Toast.makeText(this, "库存扩展功能暂未实现", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showExpandInventoryDialog() {
        // 由于布局文件不存在，创建一个简单的对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("扩展背包容量");
        
        try {
            Map<String, Object> stats = inventory.getInventoryStats();
            int currentSlots = (Integer) stats.get("totalSlots");
            int freeSlots = (Integer) stats.get("freeSlots");
            
            // 计算扩展费用（每个格子100金币）
            int expandCost = 100;
            int totalCost = expandCost * 5;
            
            String message = String.format("当前容量: %d/%d\n扩展费用: %d 金币/格\n扩展5格总计: %d 金币", 
                    currentSlots - freeSlots, currentSlots, expandCost, totalCost);
            builder.setMessage(message)
                    .setPositiveButton("扩展5格", (dialog, which) -> {
                        expandInventory(5, totalCost);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "获取库存信息失败", e);
            Toast.makeText(this, "获取库存信息失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void expandInventory(int slots, int cost) {
        // TODO: 检查玩家金币是否足够
        // 现在先直接扩展
        
        if (inventory.expandInventory(slots)) {
            updateInventoryInfo();
            Toast.makeText(this, "背包容量扩展成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "扩展失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showStatisticsDialog() {
        Map<String, Object> stats = inventory.getInventoryStats();
        @SuppressWarnings("unchecked")
        Map<Rarity, Integer> rarityStats = (Map<Rarity, Integer>) stats.get("rarityStats");
        
        StringBuilder statistics = new StringBuilder();
        statistics.append("背包统计信息\n\n");
        statistics.append("总容量: ").append(stats.get("totalSlots")).append("\n");
        statistics.append("已使用: ").append(stats.get("usedSlots")).append("\n");
        statistics.append("剩余: ").append(stats.get("freeSlots")).append("\n");
        statistics.append("战利品箱总数: ").append(stats.get("totalLootBoxes")).append("\n\n");
        
        statistics.append("按稀有度分布:\n");
        for (Rarity rarity : Rarity.values()) {
            int count = rarityStats.getOrDefault(rarity, 0);
            if (count > 0) {
                statistics.append("  ").append(rarity.getDisplayName())
                        .append(": ").append(count).append("个\n");
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("统计信息")
                .setMessage(statistics.toString())
                .setPositiveButton("确定", null)
                .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 刷新背包数据
        if (inventory != null) {
            loadLootBoxes();
        }
    }
}