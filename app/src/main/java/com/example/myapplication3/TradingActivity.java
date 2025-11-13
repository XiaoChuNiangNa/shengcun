package com.example.myapplication3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    private SharedPreferences preferences;
    private CountDownTimer refreshTimer;
    private TextView tvRefreshTimer;
    private LinearLayout container;
    private TextView tvCurrentGold;
    private static final long REFRESH_INTERVAL = 1 * 60 * 1000; // 1分钟（方便测试）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化用户ID
        userId = MyApplication.currentUserId;
        
        // 初始化SharedPreferences
        preferences = getSharedPreferences("trade_timer", MODE_PRIVATE);
        
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

        container = findViewById(R.id.container);
        tvRefreshTimer = findViewById(R.id.tv_refresh_timer);
        tvCurrentGold = findViewById(R.id.tv_current_gold);
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

        // 更新金币显示
        updateGoldDisplay();
        
        // 添加调试信息
        long lastRefreshTime = preferences.getLong("last_refresh_time", 0);
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRefresh = currentTime - lastRefreshTime;
        
        // 检查是否需要刷新贸易选项
        if (lastRefreshTime == 0 || timeSinceLastRefresh >= REFRESH_INTERVAL) {
            // 需要刷新，重新生成贸易选项
            addTradeOptions();
            
            // 保存刷新时间
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("last_refresh_time", currentTime);
            editor.apply();
            
            // 调试信息
//            showToast("首次进入或刷新间隔已到，重新生成贸易选项");
        } else {
            // 如果时间未到，尝试加载已保存的贸易选项
            loadSavedTradeOptions();
            
            // 调试信息
            long remainingTime = REFRESH_INTERVAL - timeSinceLastRefresh;
//            showToast("剩余时间: " + (remainingTime / 1000) + "秒，加载保存的贸易选项");
        }
        
        // 启动计时器
        startRefreshTimer();
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

        // 获取物品中文名称
        String itemName = getItemChineseName(itemKey);
        
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
                
                showToast("成功出售 " + quantity + " 个 " + itemName + "，获得 " + price + " 金币！");
                // 刷新数据并更新金币显示
                backpack = dbHelper.getBackpack(userId);
                updateGoldDisplay();
            } else {
                showToast("物品不足，需要 " + quantity + " 个 " + itemName);
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
                
                showToast("成功购买 " + quantity + " 个 " + itemName + "，花费 " + price + " 金币！");
                // 刷新数据并更新金币显示
                backpack = dbHelper.getBackpack(userId);
                updateGoldDisplay();
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

    /**
     * 禁用系统返回键，只允许使用按钮返回
     */
    @Override
    public void onBackPressed() {
        // 空实现，禁用系统返回功能
        // 用户只能通过页面上的返回按钮返回
    }
    
    /**
     * 更新金币显示
     */
    private void updateGoldDisplay() {
        Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
        int userGold = (int) userStatus.getOrDefault("gold", 0);
        tvCurrentGold.setText("当前金币：" + userGold);
    }
    
    /**
     * 获取物品的中文名称
     */
    private String getItemChineseName(String itemKey) {
        switch (itemKey) {
            case "stone": return "石头";
            case "wood": return "木头";
            case "iron_ore": return "铁矿";
            case "weed": return "杂草";
            case "water": return "水";
            case "wool": return "羊毛";
            case "bone": return "兽骨";
            case "leather": return "皮革";
            case "meat": return "肉";
            case "fish": return "鱼";
            case "berry": return "浆果";
            case "herb": return "药草";
            case "fiber": return "纤维";
            case "apple": return "苹果";
            case "vine": return "藤蔓";
            case "honeycomb": return "蜂巢";
            case "acorn": return "橡果";
            case "resin": return "树脂";
            case "truffle": return "松露";
            case "gem": return "宝石";
            case "flint": return "燧石";
            case "sulfur": return "硫磺";
            case "coal": return "煤炭";
            case "ice": return "冰块";
            case "obsidian": return "黑曜石";
            case "snow_lotus": return "雪莲";
            case "kelp": return "海带";
            case "sand": return "沙子";
            case "shell": return "贝壳";
            case "coconut": return "椰子";
            case "crawfish": return "螃蟹";
            case "cactus_fruit": return "仙人掌果";
            case "mushroom": return "蘑菇";
            case "reed": return "芦苇";
            case "clay": return "粘土";
            case "dried_bread": return "干面包";
            case "rice": return "大米";
            case "carrot": return "胡萝卜";
            case "potato": return "土豆";
            case "beet": return "甜菜";
            case "spinach": return "菠菜";
            case "corn": return "玉米";
            case "honey": return "蜂蜜";
            case "cucumber": return "黄瓜";
            case "winter_melon": return "冬瓜";
            default: return itemKey;
        }
    }
    
    /**
     * 保存当前贸易选项
     */
    private void saveTradeOptions(String[][] tradeOptions) {
        SharedPreferences.Editor editor = preferences.edit();
        
        // 保存贸易选项数量
        editor.putInt("trade_options_count", tradeOptions.length);
        
        // 保存每个贸易选项
        for (int i = 0; i < tradeOptions.length; i++) {
            String[] option = tradeOptions[i];
            editor.putString("trade_option_" + i + "_name", option[0]);
            editor.putString("trade_option_" + i + "_item", option[1]);
            editor.putInt("trade_option_" + i + "_price", Integer.parseInt(option[2]));
            editor.putInt("trade_option_" + i + "_quantity", Integer.parseInt(option[3]));
        }
        
        editor.apply();
    }
    
    /**
     * 加载已保存的贸易选项
     */
    private void loadSavedTradeOptions() {
        int count = preferences.getInt("trade_options_count", 0);
        
        if (count > 0) {
            // 清除当前容器
            container.removeAllViews();
            
            // 加载每个贸易选项
            for (int i = 0; i < count; i++) {
                String name = preferences.getString("trade_option_" + i + "_name", "");
                String itemKey = preferences.getString("trade_option_" + i + "_item", "");
                int price = preferences.getInt("trade_option_" + i + "_price", 0);
                int quantity = preferences.getInt("trade_option_" + i + "_quantity", 0);
                
                if (!name.isEmpty() && !itemKey.isEmpty()) {
                    addTradeOption(container, name, itemKey, price, quantity);
                }
            }
        } else {
            // 如果没有保存的选项，重新生成
            addTradeOptions();
        }
    }

    /**
     * 启动刷新计时器
     */
    private void startRefreshTimer() {
        long lastRefreshTime = preferences.getLong("last_refresh_time", 0);
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRefresh = currentTime - lastRefreshTime;
        
        if (timeSinceLastRefresh >= REFRESH_INTERVAL) {
            // 需要刷新
            setupRefreshTimer(0);
        } else {
            // 继续倒计时
            long remainingTime = REFRESH_INTERVAL - timeSinceLastRefresh;
            setupRefreshTimer(remainingTime);
        }
    }

    /**
     * 设置刷新计时器
     */
    private void setupRefreshTimer(long remainingTime) {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }

        //商店刷新时间
        refreshTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000; // 正确的分钟计算
                long seconds = (millisUntilFinished % 60000) / 1000;
                String timeText = String.format("下次刷新: %02d:%02d", minutes, seconds);
                tvRefreshTimer.setText(timeText);
            }

            @Override
            public void onFinish() {
                // 刷新贸易选项
                refreshTradeOptions();
                tvRefreshTimer.setText("已刷新");
                
                // 保存刷新时间
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong("last_refresh_time", System.currentTimeMillis());
                editor.apply();
                
                // 重新启动计时器
                startRefreshTimer();
            }
        }.start();
    }

    /**
     * 添加贸易选项
     */
    private void addTradeOptions() {
        container.removeAllViews();
        
        // 新的物品价格映射（根据提供的表格）
        java.util.Map<String, int[]> itemPrices = new java.util.HashMap<>();
        itemPrices.put("weed", new int[]{10, 5});       // 杂草: 购买10, 出售5
        itemPrices.put("berry", new int[]{20, 10});      // 浆果: 购买20, 出售10
        itemPrices.put("herb", new int[]{80, 40});      // 药草: 购买80, 出售40
        itemPrices.put("fiber", new int[]{10, 5});      // 纤维: 购买10, 出售5
        itemPrices.put("wood", new int[]{20, 10});      // 木头: 购买20, 出售10
        itemPrices.put("apple", new int[]{20, 10});     // 苹果: 购买20, 出售10
        itemPrices.put("vine", new int[]{30, 15});      // 藤蔓: 购买30, 出售15
        itemPrices.put("honeycomb", new int[]{150, 75}); // 蜂巢: 购买150, 出售75
        itemPrices.put("acorn", new int[]{20, 10});      // 橡果: 购买20, 出售10
        itemPrices.put("resin", new int[]{100, 50});    // 树脂: 购买100, 出售50
        itemPrices.put("truffle", new int[]{180, 90});   // 松露: 购买180, 出售90
        itemPrices.put("stone", new int[]{10, 5});      // 石头: 购买10, 出售5
        itemPrices.put("iron_ore", new int[]{120, 60}); // 铁矿: 购买120, 出售60
        itemPrices.put("gem", new int[]{500, 250});     // 宝石: 购买500, 出售250
        itemPrices.put("flint", new int[]{50, 25});     // 燧石: 购买50, 出售25
        itemPrices.put("sulfur", new int[]{150, 75});   // 硫磺: 购买150, 出售75
        itemPrices.put("coal", new int[]{80, 40});       // 煤炭: 购买80, 出售40
        itemPrices.put("ice", new int[]{20, 10});       // 冰块: 购买20, 出售10
        itemPrices.put("obsidian", new int[]{300, 150}); // 黑曜石: 购买300, 出售150
        itemPrices.put("snow_lotus", new int[]{200, 100}); // 雪莲: 购买200, 出售100
        itemPrices.put("water", new int[]{5, 2});       // 水: 购买5, 出售2（调整为整数）
        itemPrices.put("fish", new int[]{50, 25});      // 鱼: 购买50, 出售25
        itemPrices.put("kelp", new int[]{30, 15});      // 海带: 购买30, 出售15
        itemPrices.put("sand", new int[]{10, 5});       // 沙子: 购买10, 出售5
        itemPrices.put("shell", new int[]{40, 20});     // 贝壳: 购买40, 出售20
        itemPrices.put("coconut", new int[]{40, 20});   // 椰子: 购买40, 出售20
        itemPrices.put("crawfish", new int[]{60, 30});  // 螃蟹: 购买60, 出售30
        itemPrices.put("cactus_fruit", new int[]{30, 15}); // 仙人掌果: 购买30, 出售15
        itemPrices.put("mushroom", new int[]{40, 20});  // 蘑菇: 购买40, 出售20
        itemPrices.put("reed", new int[]{30, 15});      // 芦苇: 购买30, 出售15
        itemPrices.put("clay", new int[]{20, 10});      // 粘土: 购买20, 出售10
        itemPrices.put("dried_bread", new int[]{100, 50}); // 干面包: 购买100, 出售50
        itemPrices.put("rice", new int[]{50, 25});      // 大米: 购买50, 出售25
        itemPrices.put("carrot", new int[]{20, 10});   // 胡萝卜: 购买20, 出售10
        itemPrices.put("potato", new int[]{20, 10});    // 土豆: 购买20, 出售10
        itemPrices.put("beet", new int[]{30, 15});      // 甜菜: 购买30, 出售15
        itemPrices.put("spinach", new int[]{30, 15});   // 菠菜: 购买30, 出售15
        itemPrices.put("corn", new int[]{50, 25});      // 玉米: 购买50, 出售25
        itemPrices.put("honey", new int[]{100, 50});     // 蜂蜜: 购买100, 出售50
        itemPrices.put("cucumber", new int[]{30, 15});   // 黄瓜: 购买30, 出售15
        itemPrices.put("winter_melon", new int[]{100, 50}); // 冬瓜: 购买100, 出售50
        itemPrices.put("wool", new int[]{200, 100});    // 羊毛: 购买200, 出售100
        itemPrices.put("leather", new int[]{300, 150}); // 皮革: 购买300, 出售150
        itemPrices.put("bone", new int[]{600, 300});    // 兽骨: 购买600, 出售300
        itemPrices.put("meat", new int[]{100, 50});     // 肉: 购买100, 出售50
        
        // 随机选择5个贸易选项
        java.util.Random random = new java.util.Random();
        String[][] selectedOptions = new String[5][4];
        
        // 获取所有物品键的列表
        java.util.List<String> itemKeys = new java.util.ArrayList<>(itemPrices.keySet());
        
        for (int i = 0; i < 5; i++) {
            // 随机选择一个物品
            String itemKey = itemKeys.get(random.nextInt(itemKeys.size()));
            
            // 随机决定是购买还是出售（50%概率）
            boolean isBuy = random.nextBoolean();
            int[] prices = itemPrices.get(itemKey);
            int price = isBuy ? prices[0] : prices[1]; // 购买价格或出售价格
            
            // 随机生成数量（1-20之间，根据价格调整）
            int maxQuantity = Math.max(1, 100 / Math.max(1, price));
            int quantity = random.nextInt(maxQuantity) + 1;
            
            // 计算总价格（单价 × 数量）
            int totalPrice = price * quantity;
            
            // 创建贸易选项
            String tradeType = isBuy ? "购买" : "出售";
            String itemName = getItemChineseName(itemKey);
            String tradeName = tradeType + itemName;
            
            selectedOptions[i] = new String[]{tradeName, itemKey, String.valueOf(totalPrice), String.valueOf(quantity)};
            addTradeOption(container, tradeName, itemKey, totalPrice, quantity);
        }
        
        // 保存生成的贸易选项
        saveTradeOptions(selectedOptions);
    }

    /**
     * 刷新贸易选项
     */
    private void refreshTradeOptions() {
        addTradeOptions();
        showToast("商店已刷新！");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }

}