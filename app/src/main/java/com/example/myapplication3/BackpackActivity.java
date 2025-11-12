package com.example.myapplication3;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackpackActivity extends AppCompatActivity {
    private static final String TAG = "BackpackActivity";
    private DBHelper dbHelper;
    private TextView tvBackpackCap;
    private GridView gvBackpack;
    private ImageButton btnBack;
    private List<Map.Entry<String, Integer>> itemList;
    private BackpackGridAdapter adapter;

    private int backpackCap;
    private int life, hunger, thirst, stamina;
    
    // 移动物品相关变量
    private boolean isMovingItem = false;
    private String movingItemName = "";
    private int movingItemCount = 0;
    private TextView tvMovingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backpack);

        dbHelper = DBHelper.getInstance(this);
        initView();
        loadBackpackData();
    }

    private void initView() {
        tvBackpackCap = findViewById(R.id.tv_backpack_cap);
        gvBackpack = findViewById(R.id.gv_backpack);
        btnBack = findViewById(R.id.btn_back);
        tvMovingState = findViewById(R.id.tv_moving_state);

        // 网格设置（一行四格）
        gvBackpack.setNumColumns(4);

        // 物品点击事件：弹出自定义弹窗
        gvBackpack.setOnItemClickListener((parent, view, position, id) -> {
            if (itemList == null || position >= itemList.size()) {
                Log.d(TAG, "点击位置无效：" + position);
                return;
            }
            Map.Entry<String, Integer> item = itemList.get(position);
            String itemName = item.getKey();
            int count = item.getValue();
            Log.d(TAG, "点击物品：" + itemName + "，数量：" + count);
            showItemActionDialog(itemName, count);
        });

        // 点击空白处取消移动状态
        View rootLayout = findViewById(android.R.id.content);
        rootLayout.setOnClickListener(v -> {
            if (isMovingItem) {
                cancelMovingItem();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadBackpackData() {
        int currentPosition = gvBackpack.getFirstVisiblePosition();
        // 获取用户状态
        Map<String, Object> userStatus = dbHelper.getUserStatus(MyApplication.currentUserId);
        backpackCap = (int) userStatus.get("backpack_cap");
        int currentCount = dbHelper.getBackpackCurrentCount(MyApplication.currentUserId);
        tvBackpackCap.setText(String.format("背包容量：%d/%d", currentCount, backpackCap));

        life = (int) userStatus.get("life");
        hunger = (int) userStatus.get("hunger");
        thirst = (int) userStatus.get("thirst");
        stamina = (int) userStatus.get("stamina");

        // 获取背包物品
        Map<String, Integer> backpack = dbHelper.getBackpack(MyApplication.currentUserId);
        itemList = new ArrayList<>(backpack.entrySet());

        // 设置适配器
        adapter = new BackpackGridAdapter(this, itemList);
        gvBackpack.setAdapter(adapter);

        if (currentPosition >= 0) {
            gvBackpack.setSelection(currentPosition);
        }
    }

    /**
     * 显示物品操作弹窗
     */
    private void showItemActionDialog(String itemName, int count) {
        // 加载弹窗布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_item_action, null);
        if (dialogView == null) {
            Log.e(TAG, "弹窗布局加载失败");
            return;
        }

        // 初始化弹窗控件
        ImageView ivItem = dialogView.findViewById(R.id.iv_dialog_item);
        TextView tvName = dialogView.findViewById(R.id.tv_dialog_name);
        TextView tvEffect = dialogView.findViewById(R.id.tv_dialog_effect);
        Button btnUse = dialogView.findViewById(R.id.btn_use);
        Button btnMove = dialogView.findViewById(R.id.btn_move);
        Button btnDiscard = dialogView.findViewById(R.id.btn_discard);

        // 检查控件是否初始化成功
        if (ivItem == null || tvName == null || tvEffect == null || btnUse == null || btnMove == null || btnDiscard == null) {
            Log.e(TAG, "弹窗控件初始化失败");
            return;
        }

        // 设置物品图片
        int imgRes = ResourceImageManager.getItemImage(itemName);
        ivItem.setImageResource(imgRes);

        // 设置物品名称（带数量）
        tvName.setText(String.format("%s（×%d）", itemName, count));

        // 设置物品效果描述
        String effectDesc = getEffectDescription(itemName);
        tvEffect.setText(effectDesc);

        // 创建弹窗
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // 移动按钮点击事件
        btnMove.setOnClickListener(v -> {
            startMovingItem(itemName, count);
            dialog.dismiss();
        });

        // 使用按钮点击事件
        btnUse.setOnClickListener(v -> {
            useItem(itemName);
            dialog.dismiss();
        });

        // 丢弃按钮点击事件
        btnDiscard.setOnClickListener(v -> {
            discardItem(itemName);
            dialog.dismiss();
        });

        // 显示弹窗
        dialog.show();
        Log.d(TAG, "弹窗已显示：" + itemName);
    }

    /**
     * 获取物品效果描述
     */
    private String getEffectDescription(String itemName) {
        // 工具类物品
        if (isToolItem(itemName)) {
            int durability = dbHelper.getDurability(MyApplication.currentUserId, itemName);
            int maxDurability = ToolUtils.getToolInitialDurability(itemName, MyApplication.currentUserId);
            String desc = ToolUtils.getEquipDescription(itemName);
            return String.format("类型：工具\n耐久度：%d/%d\n效果：%s",
                    durability, maxDurability, desc);
        }

        // 消耗品（从FoodEffectManager获取效果）
        FoodEffectManager.FoodEffect effect = FoodEffectManager.getFoodEffect(itemName);
        if (effect != null) {
            StringBuilder sb = new StringBuilder("类型：消耗品\n效果：");
            if (effect.life != 0) sb.append("生命").append(effect.life > 0 ? "+" : "").append(effect.life).append(" ");
            if (effect.hunger != 0) sb.append("饥饿").append(effect.hunger > 0 ? "+" : "").append(effect.hunger).append(" ");
            if (effect.thirst != 0) sb.append("口渴").append(effect.thirst > 0 ? "+" : "").append(effect.thirst).append(" ");
            if (effect.stamina != 0) sb.append("体力").append(effect.stamina > 0 ? "+" : "").append(effect.stamina).append(" ");
            // 增加特殊效果显示
            if (!"无".equals(effect.special)) {
                sb.append("\n特殊效果：").append(effect.special);
            }
            return sb.toString();
        }

        // 其他物品
        return "无特殊效果";
    }

    /**
     * 判断是否为工具类物品
     */
    private boolean isToolItem(String itemName) {
        String[] tools = {
                ItemConstants.EQUIP_STONE_AXE, ItemConstants.EQUIP_IRON_AXE, ItemConstants.EQUIP_DIAMOND_AXE,
                ItemConstants.EQUIP_STONE_PICKAXE, ItemConstants.EQUIP_IRON_PICKAXE, ItemConstants.EQUIP_DIAMOND_PICKAXE,
                ItemConstants.EQUIP_STONE_SICKLE, ItemConstants.EQUIP_IRON_SICKLE, ItemConstants.EQUIP_DIAMOND_SICKLE,
                ItemConstants.EQUIP_STONE_FISHING_ROD, ItemConstants.EQUIP_IRON_FISHING_ROD, ItemConstants.EQUIP_DIAMOND_FISHING_ROD
        };
        for (String tool : tools) {
            if (tool.equals(itemName)) return true;
        }
        return false;
    }

    /**
     * 使用物品
     */
    public void useItem(String itemType) {
        if (isToolItem(itemType)) {
            Toast.makeText(this, "请在装备页面使用工具", Toast.LENGTH_SHORT).show();
            return;
        }

        FoodEffectManager.FoodEffect effect = FoodEffectManager.getFoodEffect(itemType);
        if (effect == null) {
            Toast.makeText(this, itemType + "无法使用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新基础属性（生命/饥饿/口渴/体力）
        int newLife = Math.max(0, Math.min(100, life + effect.life));
        int newHunger = Math.max(0, Math.min(100, hunger + effect.hunger));
        int newThirst = Math.max(0, Math.min(100, thirst + effect.thirst));
        int newStamina = Math.max(0, Math.min(100, stamina + effect.stamina));

        // 消耗物品并更新基础状态
        dbHelper.updateBackpackItem(MyApplication.currentUserId, itemType, -1);
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("life", newLife);
        updateData.put("hunger", newHunger);
        updateData.put("thirst", newThirst);
        updateData.put("stamina", newStamina);
        dbHelper.updateUserStatus(MyApplication.currentUserId, updateData);

        // 修复：重新获取最新状态值
        Map<String, Object> latestStatus = dbHelper.getUserStatus(MyApplication.currentUserId);
        life = (int) latestStatus.get("life");
        hunger = (int) latestStatus.get("hunger");
        thirst = (int) latestStatus.get("thirst");
        stamina = (int) latestStatus.get("stamina");

        // 处理特殊效果
        handleSpecialEffect(effect.special);

        Toast.makeText(this, "使用了1个" + itemType, Toast.LENGTH_SHORT).show();
        Log.d("FoodUse", "食物效果：" + effect.life + "，当前生命：" + life + "，新生命：" + newLife);
        Log.d("FoodUse", "食物效果：" + effect.hunger + "，当前饥饿：" + hunger + "，新饥饿：" + newHunger);
        Log.d("FoodUse", "食物效果：" + effect.thirst + "，当前口渴：" + thirst + "，新口渴：" + newThirst);
        Log.d("FoodUse", "食物效果：" + effect.stamina + "，当前体力：" + stamina + "，新体力：" + newStamina);

        loadBackpackData();
    }

    /**
     * 处理特殊效果
     */
    private void handleSpecialEffect(String specialEffect) {
        int userId = MyApplication.currentUserId;
        if ("无".equals(specialEffect)) return;

        // 处理体温相关效果
        if (specialEffect.startsWith("体温")) {
            handleTemperatureEffect(specialEffect, userId);
        }
        // 处理持续生命恢复效果
        else if (specialEffect.startsWith("每小时生命+5")) {
            handleLifeRegenEffect(specialEffect, userId);
        }
        // 处理其他持续效果
        else if (specialEffect.contains("持续")) {
            handlePersistentEffect(specialEffect, userId);
        }
    }

    /**
     * 处理体温效果（修正参数类型为int userId）
     */
    private void handleTemperatureEffect(String effect, int userId) {
        int currentTemp = dbHelper.getUserTemperature(userId);
        int newTemp = currentTemp;

        if (effect.contains("设置到默认体温")) {
            newTemp = Constant.TEMPERATURE_DEFAULT;
        } else if (effect.contains("+")) {
            int change = Integer.parseInt(effect.replaceAll("[^0-9]", ""));
            newTemp = currentTemp + change;
        } else if (effect.contains("-")) {
            int change = Integer.parseInt(effect.replaceAll("[^0-9]", ""));
            newTemp = currentTemp - change;
        }

        dbHelper.updateUserTemperature(userId, newTemp);
        Toast.makeText(this, "体温变化: " + effect, Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理持续生命恢复效果（修正参数类型为int userId）
     */
    private void handleLifeRegenEffect(String effect, int userId) {
        // 提取持续时间（如"持续5小时" -> 5）
        int duration = Integer.parseInt(effect.replaceAll("[^0-9]", ""));
        StatusEffectManager.addPersistentEffect(userId, "LIFE_REGEN", 5, duration);
        Toast.makeText(this, "获得效果: " + effect, Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理其他持续效果
     */
    private void handlePersistentEffect(String effect, int userId) {
        // 这里可以添加其他类型的持续效果处理逻辑
        // 例如：体力恢复、饥饿减缓等
        Toast.makeText(this, "获得持续效果: " + effect, Toast.LENGTH_SHORT).show();
    }

    /**
     * 丢弃物品
     */
    public void discardItem(String itemType) {
        dbHelper.updateBackpackItem(MyApplication.currentUserId, itemType, -1);
        Toast.makeText(this, "丢弃了1个" + itemType, Toast.LENGTH_SHORT).show();
        loadBackpackData(); // 刷新背包
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBackpackData();
    }

    /**
     * 开始移动物品
     */
    public void startMovingItem(String itemName, int count) {
        isMovingItem = true;
        movingItemName = itemName;
        movingItemCount = count;
        
        // 显示移动状态
        updateMovingStateDisplay();
        
        // 修改网格点击事件为移动模式
        gvBackpack.setOnItemClickListener((parent, view, position, id) -> {
            if (itemList == null || position >= itemList.size()) {
                Log.d(TAG, "点击位置无效：" + position);
                return;
            }
            
            Map.Entry<String, Integer> targetItem = itemList.get(position);
            String targetItemName = targetItem.getKey();
            
            // 移动物品到目标位置
            moveItemToPosition(targetItemName);
        });
        
        Toast.makeText(this, "开始移动物品：" + itemName + "，请点击目标位置", Toast.LENGTH_LONG).show();
        Log.d(TAG, "开始移动物品：" + itemName + "，数量：" + count);
    }

    /**
     * 移动物品到指定位置
     * 不同物品之间转移：相互调换位置和数量
     * 相同物品转移：叠加数量
     */
    public void moveItemToPosition(String targetItemName) {
        if (!isMovingItem) {
            Toast.makeText(this, "请先选择要移动的物品", Toast.LENGTH_LONG).show();
            return;
        }

        // 相同物品转移：叠加数量
        if (movingItemName.equals(targetItemName)) {
            try {
                // 获取目标物品当前数量
                int targetCount = dbHelper.getBackpackItemCount(MyApplication.currentUserId, targetItemName);
                // 叠加数量
                dbHelper.updateBackpackItem(MyApplication.currentUserId, targetItemName, movingItemCount);
                // 删除原物品（原位置已经叠加到目标位置）
                dbHelper.deleteBackpackItem(MyApplication.currentUserId, movingItemName);
                
                Toast.makeText(this, "成功叠加物品：" + movingItemName + " ×" + movingItemCount, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "叠加物品完成：" + movingItemName + " 数量：" + movingItemCount);
                
            } catch (Exception e) {
                Log.e(TAG, "叠加物品失败：", e);
                Toast.makeText(this, "叠加物品失败", Toast.LENGTH_LONG).show();
            }
        } 
        // 不同物品转移：相互调换位置
        else {
            try {
                // 获取目标物品当前数量
                int targetCount = dbHelper.getBackpackItemCount(MyApplication.currentUserId, targetItemName);
                
                // 保存目标物品信息
                String tempItemName = movingItemName;
                int tempItemCount = movingItemCount;
                
                // 交换物品：将源物品替换为目标物品
                if (targetCount > 0) {
                    // 删除源物品
                    dbHelper.deleteBackpackItem(MyApplication.currentUserId, movingItemName);
                    // 添加目标物品到源位置
                    dbHelper.addBackpackItem(MyApplication.currentUserId, tempItemName, targetCount);
                    // 删除原目标物品
                    dbHelper.deleteBackpackItem(MyApplication.currentUserId, targetItemName);
                    // 添加源物品到目标位置
                    dbHelper.addBackpackItem(MyApplication.currentUserId, targetItemName, tempItemCount);
                } else {
                    // 如果目标位置为空，直接移动物品
                    dbHelper.addBackpackItem(MyApplication.currentUserId, targetItemName, movingItemCount);
                    dbHelper.deleteBackpackItem(MyApplication.currentUserId, movingItemName);
                }
                
                Toast.makeText(this, "成功交换物品：" + movingItemName + " ↔ " + targetItemName, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "交换物品完成：" + movingItemName + " ↔ " + targetItemName);
                
            } catch (Exception e) {
                Log.e(TAG, "交换物品失败：", e);
                Toast.makeText(this, "交换物品失败", Toast.LENGTH_LONG).show();
            }
        }
        
        // 取消移动状态并刷新界面
        cancelMovingItem();
        loadBackpackData();
    }

    /**
     * 取消移动物品
     */
    public void cancelMovingItem() {
        if (isMovingItem) {
            isMovingItem = false;
            movingItemName = "";
            movingItemCount = 0;
            
            // 恢复正常的网格点击事件
            gvBackpack.setOnItemClickListener((parent, view, position, id) -> {
                if (itemList == null || position >= itemList.size()) {
                    Log.d(TAG, "点击位置无效：" + position);
                    return;
                }
                Map.Entry<String, Integer> item = itemList.get(position);
                String itemName = item.getKey();
                int count = item.getValue();
                Log.d(TAG, "点击物品：" + itemName + "，数量：" + count);
                showItemActionDialog(itemName, count);
            });
            
            // 隐藏移动状态显示
            tvMovingState.setVisibility(View.GONE);
            
            Toast.makeText(this, "已取消移动物品", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "取消移动物品");
            
            // 刷新界面以更新按钮状态
            loadBackpackData();
        }
    }

    /**
     * 更新移动状态显示
     */
    private void updateMovingStateDisplay() {
        if (isMovingItem && movingItemName != null && !movingItemName.isEmpty()) {
            tvMovingState.setText("正在移动：" + movingItemName + "（×" + movingItemCount + "）");
            tvMovingState.setVisibility(View.VISIBLE);
        } else {
            tvMovingState.setVisibility(View.GONE);
        }
    }
}