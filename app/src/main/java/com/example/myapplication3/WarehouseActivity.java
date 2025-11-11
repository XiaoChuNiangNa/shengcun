package com.example.myapplication3;

import android.content.Intent;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarehouseActivity extends AppCompatActivity {
    
    private DBHelper dbHelper;
    private TextView tvWarehouseCap;
    private ListView lvBackpack, lvWarehouse;
    private Button btnMoveToWarehouse, btnMoveToBackpack;
    private ImageButton btnBack;
    
    private List<Map.Entry<String, Integer>> backpackItems;
    private List<Map.Entry<String, Integer>> warehouseItems;
    private WarehouseListAdapter backpackAdapter;
    private WarehouseListAdapter warehouseAdapter;
    
    private int selectedBackpackPosition = -1;
    private int selectedWarehousePosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_warehouse);
        
        dbHelper = DBHelper.getInstance(this);
        
        // 检查是否从基地进入，移除仓库使用限制
        if (isFromBase()) {
            // 从基地进入，无任何限制
        } else {
            // 从游戏进入，检查仓库使用权限
            if (!canUseWarehouse()) {
                Toast.makeText(this, "开始游戏后无法使用个人仓库，请先进行轮回", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        
        initView();
        loadData();
        setupListeners();
    }
    
    /**
     * 检查是否可以访问仓库
     * 开始游戏后不得使用，轮回后可以使用
     */
    private boolean canUseWarehouse() {
        // 使用GameStateManager检查游戏状态
        GameStateManager gameStateManager = GameStateManager.getInstance(this);
        if (gameStateManager.isUserInGame(MyApplication.currentUserId)) {
            return false; // 游戏进行中，不能使用仓库
        }
        
        // 检查用户状态，判断是否已经开始游戏
        Map<String, Object> userStatus = dbHelper.getUserStatus(MyApplication.currentUserId);
        if (userStatus != null) {
            // 如果有游戏进度（收集次数、操作次数等），说明已经开始游戏
            int globalCollectTimes = (int) userStatus.getOrDefault("global_collect_times", 0);
            int explorationTimes = (int) userStatus.getOrDefault("exploration_times", 0);
            int synthesisTimes = (int) userStatus.getOrDefault("synthesis_times", 0);
            
            // 如果用户已经开始游戏（有收集/探索/合成操作），则不能使用仓库
            if (globalCollectTimes > 0 || explorationTimes > 0 || synthesisTimes > 0) {
                return false; // 已经开始游戏，不能使用仓库
            }
        }
        return true; // 未开始游戏或刚轮回，可以使用仓库
    }

    private void initView() {
        tvWarehouseCap = findViewById(R.id.tv_warehouse_cap);
        lvBackpack = findViewById(R.id.lv_backpack);
        lvWarehouse = findViewById(R.id.lv_warehouse);
        btnMoveToWarehouse = findViewById(R.id.btn_move_to_warehouse);
        btnMoveToBackpack = findViewById(R.id.btn_move_to_backpack);
        btnBack = findViewById(R.id.btn_back);
        
        backpackItems = new ArrayList<>();
        warehouseItems = new ArrayList<>();
        
        backpackAdapter = new WarehouseListAdapter(this, backpackItems);
        warehouseAdapter = new WarehouseListAdapter(this, warehouseItems);
        
        lvBackpack.setAdapter(backpackAdapter);
        lvWarehouse.setAdapter(warehouseAdapter);
    }

    private void loadData() {
        // 加载背包数据
        Map<String, Integer> backpack = dbHelper.getBackpack(MyApplication.currentUserId);
        backpackItems.clear();
        backpackItems.addAll(backpack.entrySet());
        backpackAdapter.notifyDataSetChanged();
        
        // 加载仓库数据
        Map<String, Integer> warehouse = dbHelper.getWarehouse(MyApplication.currentUserId);
        warehouseItems.clear();
        warehouseItems.addAll(warehouse.entrySet());
        warehouseAdapter.notifyDataSetChanged();
        
        // 更新容量显示
        int warehouseCurrent = dbHelper.getWarehouseCurrentCount(MyApplication.currentUserId);
        int warehouseCap = dbHelper.getWarehouseCapacity(MyApplication.currentUserId);
        tvWarehouseCap.setText(String.format("仓库容量：%d/%d", warehouseCurrent, warehouseCap));
        
        // 重置选中状态
        selectedBackpackPosition = -1;
        selectedWarehousePosition = -1;
        btnMoveToWarehouse.setEnabled(false);
        btnMoveToBackpack.setEnabled(false);
    }

    private void setupListeners() {
        // 背包列表点击事件
        lvBackpack.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedBackpackPosition = position;
                selectedWarehousePosition = -1;
                btnMoveToWarehouse.setEnabled(true);
                btnMoveToBackpack.setEnabled(false);
                
                // 更新选中状态
                backpackAdapter.setSelectedPosition(position);
                warehouseAdapter.setSelectedPosition(-1);
            }
        });
        
        // 背包列表双击事件（移动所有该物品到仓库）
        lvBackpack.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < backpackItems.size()) {
                    Map.Entry<String, Integer> item = backpackItems.get(position);
                    String itemName = item.getKey();
                    int itemCount = item.getValue();
                    
                    // 移动所有该物品到仓库
                    moveAllItemToWarehouse(itemName, itemCount);
                    return true; // 消费事件
                }
                return false;
            }
        });
        
        // 仓库列表点击事件
        lvWarehouse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedWarehousePosition = position;
                selectedBackpackPosition = -1;
                btnMoveToWarehouse.setEnabled(false);
                btnMoveToBackpack.setEnabled(true);
                
                // 更新选中状态
                backpackAdapter.setSelectedPosition(-1);
                warehouseAdapter.setSelectedPosition(position);
            }
        });
        
        // 仓库列表双击事件（移动所有该物品到背包）
        lvWarehouse.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < warehouseItems.size()) {
                    Map.Entry<String, Integer> item = warehouseItems.get(position);
                    String itemName = item.getKey();
                    int itemCount = item.getValue();
                    
                    // 移动所有该物品到背包
                    moveAllItemToBackpack(itemName, itemCount);
                    return true; // 消费事件
                }
                return false;
            }
        });
        
        // 移动到仓库按钮
        btnMoveToWarehouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedBackpackPosition >= 0 && selectedBackpackPosition < backpackItems.size()) {
                    Map.Entry<String, Integer> item = backpackItems.get(selectedBackpackPosition);
                    showMoveDialog(item.getKey(), item.getValue(), true);
                }
            }
        });
        
        // 移动到背包按钮
        btnMoveToBackpack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedWarehousePosition >= 0 && selectedWarehousePosition < warehouseItems.size()) {
                    Map.Entry<String, Integer> item = warehouseItems.get(selectedWarehousePosition);
                    showMoveDialog(item.getKey(), item.getValue(), false);
                }
            }
        });
        
        // 返回按钮
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查是否从基地进入，如果是则返回基地
                if (isFromBase()) {
                    startActivity(new Intent(WarehouseActivity.this, BaseActivity.class));
                    finish();
                } else {
                    finish();
                }
            }
        });
    }
    
    private void showMoveDialog(final String itemName, final int maxCount, final boolean toWarehouse) {
        // 创建自定义对话框
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("移动物品数量");
        
        // 加载自定义布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_move_quantity, null);
        dialog.setView(dialogView);
        
        // 获取布局中的控件
        TextView tvItemName = dialogView.findViewById(R.id.tv_item_name);
        TextView tvItemDescription = dialogView.findViewById(R.id.tv_item_description);
        TextView tvMaxQuantity = dialogView.findViewById(R.id.tv_max_quantity);
        EditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        Button btnZero = dialogView.findViewById(R.id.btn_zero);
        Button btnMinusOne = dialogView.findViewById(R.id.btn_minus_one);
        Button btnPlusOne = dialogView.findViewById(R.id.btn_plus_one);
        Button btnAll = dialogView.findViewById(R.id.btn_all);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        
        // 设置物品信息
        tvItemName.setText(itemName);
        tvItemDescription.setText(getItemDescription(itemName));
        tvMaxQuantity.setText("最大数量：" + maxCount);
        
        // 设置数量输入框，默认值为1
        etQuantity.setText("1");
        
        // 按钮点击事件
        btnZero.setOnClickListener(v -> {
            etQuantity.setText("0");
        });
        
        btnMinusOne.setOnClickListener(v -> {
            try {
                int currentValue = Integer.parseInt(etQuantity.getText().toString());
                int newValue = Math.max(0, currentValue - 1);
                etQuantity.setText(String.valueOf(newValue));
            } catch (NumberFormatException e) {
                etQuantity.setText("0");
            }
        });
        
        btnPlusOne.setOnClickListener(v -> {
            try {
                int currentValue = Integer.parseInt(etQuantity.getText().toString());
                int newValue = Math.min(maxCount, currentValue + 1);
                etQuantity.setText(String.valueOf(newValue));
            } catch (NumberFormatException e) {
                etQuantity.setText("1");
            }
        });
        
        btnAll.setOnClickListener(v -> {
            etQuantity.setText(String.valueOf(maxCount));
        });
        
        btnConfirm.setOnClickListener(v -> {
            try {
                int moveCount = Integer.parseInt(etQuantity.getText().toString());
                
                // 验证数量
                if (moveCount <= 0) {
                    Toast.makeText(this, "请输入有效的数量", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (moveCount > maxCount) {
                    Toast.makeText(this, "物品数量不足，当前最多可移动 " + maxCount + " 个", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 执行移动操作
                moveItem(itemName, moveCount, toWarehouse);
                dialog.dismiss();
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    /**
     * 获取物品描述信息
     */
    private String getItemDescription(String itemName) {
        // 这里可以根据物品名称返回相应的描述信息
        // 暂时返回默认描述，后续可以扩展为从数据库或其他地方获取
        switch (itemName) {
            case "石头":
                return "基础建筑材料，可用于合成各种工具";
            case "木材":
                return "基础建筑材料，可用于制作工具和建筑";
            case "铁矿石":
                return "可冶炼成铁锭，用于制作高级工具";
            case "铜矿石":
                return "可冶炼成铜锭，用于制作导线和装饰品";
            case "草":
                return "随处可见的植物，可用于合成";
            default:
                return "物品描述信息";
        }
    }
    
    private void moveItem(String itemName, int count, boolean toWarehouse) {
        boolean success;
        if (toWarehouse) {
            // 从背包移动到仓库
            success = dbHelper.moveItemToWarehouse(MyApplication.currentUserId, itemName, count);
            if (success) {
                Toast.makeText(this, "成功移动 " + count + " 个 " + itemName + " 到仓库", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "移动失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 从仓库移动到背包
            success = dbHelper.moveItemToBackpack(MyApplication.currentUserId, itemName, count);
            if (success) {
                Toast.makeText(this, "成功移动 " + count + " 个 " + itemName + " 到背包", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "移动失败（可能背包容量不足）", Toast.LENGTH_SHORT).show();
            }
        }
        
        // 重新加载数据
        loadData();
    }
    
    /**
     * 移动所有该物品到仓库（双击功能）
     */
    private void moveAllItemToWarehouse(String itemName, int count) {
        boolean success = dbHelper.moveItemToWarehouse(MyApplication.currentUserId, itemName, count);
        if (success) {
            Toast.makeText(this, "成功移动所有 " + count + " 个 " + itemName + " 到仓库", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "移动失败（可能仓库容量不足）", Toast.LENGTH_SHORT).show();
        }
        
        // 重新加载数据
        loadData();
    }
    
    /**
     * 移动所有该物品到背包（双击功能）
     */
    private void moveAllItemToBackpack(String itemName, int count) {
        boolean success = dbHelper.moveItemToBackpack(MyApplication.currentUserId, itemName, count);
        if (success) {
            Toast.makeText(this, "成功移动所有 " + count + " 个 " + itemName + " 到背包", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "移动失败（可能背包容量不足）", Toast.LENGTH_SHORT).show();
        }
        
        // 重新加载数据
        loadData();
    }

    /**
     * 检查是否从基地进入
     */
    protected boolean isFromBase() {
        return getIntent().getBooleanExtra("from_base", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}