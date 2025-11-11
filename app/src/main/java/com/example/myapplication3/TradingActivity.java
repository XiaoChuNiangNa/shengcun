package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class TradingActivity extends BaseActivity {

    private int userId;
    protected Map<String, Integer> backpack; // 添加背包变量声明

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化用户ID
        userId = MyApplication.currentUserId;
        
        // 初始化背包数据
        backpack = dbHelper.getBackpack(userId);

        // 直接初始化活动，无需建筑检查
        initActivity();
    }

    /**
     * 初始化活动界面
     */
    private void initActivity() {
        setContentView(R.layout.activity_trade);

        LinearLayout container = findViewById(R.id.container);
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // 检查是否从基地进入，如果是则返回基地
            if (isFromBase()) {
                startActivity(new Intent(TradingActivity.this, BaseActivity.class));
                finish();
            } else {
                finish();
            }
        });

        // 添加贸易选项
        addTradeOption(container, "出售石头", "stone", 5, 10);
        addTradeOption(container, "出售木材", "wood", 3, 15);
        addTradeOption(container, "出售铁矿石", "iron_ore", 50, 2);
        addTradeOption(container, "购买草", "weed", 2, 25);
        addTradeOption(container, "购买水", "water", 1, 50);
    }

    private void addTradeOption(LinearLayout container, String name, String itemKey, int price, int quantity) {
        LinearLayout item = (LinearLayout) getLayoutInflater().inflate(R.layout.item_trade, container, false);
        TextView tvName = item.findViewById(R.id.tv_name);
        TextView tvDescription = item.findViewById(R.id.tv_description);
        Button btnTrade = item.findViewById(R.id.btn_trade);

        tvName.setText(name);
        tvDescription.setText("价格: " + price + " 金币, 数量: " + quantity);

        btnTrade.setOnClickListener(v -> performTrade(itemKey, price, quantity, name.contains("出售")));
        container.addView(item);
    }

    private void performTrade(String itemKey, int price, int quantity, boolean isSelling) {
        // 获取用户金币
        Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
        int userGold = (int) userStatus.getOrDefault("gold", 0);

        if (isSelling) {
            // 出售逻辑
            int itemCount = backpack.getOrDefault(itemKey, 0);
            if (itemCount >= quantity) {
                // 扣除物品
                dbHelper.updateBackpackItem(userId, itemKey, -quantity);
                // 增加金币 - 使用updateUserStatus方法
                Map<String, Object> updateData = new java.util.HashMap<>();
                updateData.put("gold", userGold + price);
                dbHelper.updateUserStatus(userId, updateData);
                
                showToast("成功出售 " + quantity + " 个 " + itemKey + "，获得 " + price + " 金币！");
                // 刷新数据
                backpack = dbHelper.getBackpack(userId);
            } else {
                showToast("物品不足，需要 " + quantity + " 个 " + itemKey);
            }
        } else {
            // 购买逻辑
            if (userGold >= price) {
                // 扣除金币 - 使用updateUserStatus方法
                Map<String, Object> updateData = new java.util.HashMap<>();
                updateData.put("gold", userGold - price);
                dbHelper.updateUserStatus(userId, updateData);
                // 添加物品到背包
                dbHelper.updateBackpackItem(userId, itemKey, quantity);
                
                showToast("成功购买 " + quantity + " 个 " + itemKey + "，花费 " + price + " 金币！");
                // 刷新数据
                backpack = dbHelper.getBackpack(userId);
            } else {
                showToast("金币不足，需要 " + price + " 金币");
            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected boolean isFromBase() {
        return getIntent().getBooleanExtra("from_base", false);
    }


}