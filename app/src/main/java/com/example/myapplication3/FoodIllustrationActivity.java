package com.example.myapplication3;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.content.SharedPreferences;
import java.util.Map;
import android.content.SharedPreferences;

public class FoodIllustrationActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private FoodIllustrationAdapter adapter;
    private List<FoodItem> foodList;
    private int userId;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_illustration);
        
        // 获取用户ID
        userId = MyApplication.currentUserId;
        if (userId == -1) {
            SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
            userId = sp.getInt("user_id", -1);
        }
        
        // 设置返回按钮
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());
        
        // 初始化数据
        initializeFoodData();
        
        // 设置RecyclerView
        recyclerView = findViewById(R.id.rv_food_grid);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2); // 2列网格布局
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FoodIllustrationAdapter(this, foodList);
        recyclerView.setAdapter(adapter);
        
        // 设置点击事件（需要在适配器中处理）
    }
    
    private void initializeFoodData() {
        foodList = new ArrayList<>();
        
        // 从游戏常量中获取所有料理数据
        addFoodFromConstants(ItemConstants.ITEM_GRILLED_FISH, "美味的烤鱼，提供丰富的蛋白质", "鱼 x1, 木炭 x1");
        addFoodFromConstants(ItemConstants.ITEM_GRILLED_CRAWFISH, "香喷喷的烤螃蟹，营养丰富", "螃蟹 x1, 木炭 x1");
        addFoodFromConstants(ItemConstants.ITEM_FISH_SOUP, "鲜美的鱼汤，温暖身心", "鱼 x1, 水 x1, 盐 x1");
        addFoodFromConstants(ItemConstants.ITEM_MUSHROOM_SOUP, "营养的蘑菇汤，恢复体力", "蘑菇 x2, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_KELP_SOUP, "清爽的海带汤，补充矿物质", "海带 x1, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_FRUIT_PIE, "甜美的水果派，提升心情", "水果 x2, 面粉 x1, 蜂蜜 x1");
        addFoodFromConstants(ItemConstants.ITEM_ADVANCED_HERB, "高级草药，治疗效果显著", "药草 x3, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_ICY_SNOW_LOTUS_COCONUT, "冰镇雪莲椰汁，清凉解暑", "雪莲 x1, 椰子 x1, 冰块 x2");
        addFoodFromConstants(ItemConstants.ITEM_HONEY_APPLE_SLICE, "蜂蜜苹果片，甜美可口", "苹果 x1, 蜂蜜 x1");
        addFoodFromConstants(ItemConstants.ITEM_RICE_PORRIDGE, "清淡的大米清粥，易消化", "大米 x1, 水 x2");
        addFoodFromConstants(ItemConstants.ITEM_KELP_WINTER_MELON_SOUP, "海带冬瓜汤，清热降火", "海带 x1, 冬瓜 x1, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_ROASTED_POTATO, "香烤土豆，饱腹感强", "土豆 x2, 木炭 x1");
        addFoodFromConstants(ItemConstants.ITEM_FRUIT_SMOOTHIE, "水果冰沙，清凉解渴", "水果 x2, 冰块 x1, 蜂蜜 x1");
        addFoodFromConstants(ItemConstants.ITEM_CRAWFISH_SHELL_SOUP, "螃蟹贝壳汤，鲜美滋补", "螃蟹 x1, 贝壳 x2, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_STEAMED_CORN, "蒸玉米，香甜可口", "玉米 x2, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_MUSHROOM_TRUFFLE_FRY, "蘑菇松露煎，高级料理", "蘑菇 x1, 松露 x1, 油 x1");
        addFoodFromConstants(ItemConstants.ITEM_BERRY_HONEY_BREAD, "浆果蜂蜜面包，营养丰富", "浆果 x2, 蜂蜜 x1, 面粉 x1");
        addFoodFromConstants(ItemConstants.ITEM_BEET_HONEY_DRINK, "甜菜蜂蜜饮，补充能量", "甜菜 x1, 蜂蜜 x1, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_BOILED_SPINACH, "水煮菠菜，健康蔬菜", "菠菜 x2, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_ROASTED_ACORN, "烤橡果，坚果风味", "橡果 x3, 木炭 x1");
        addFoodFromConstants(ItemConstants.ITEM_CACTUS_FRUIT_ICE_DRINK, "仙人掌果冰饮，解暑佳品", "仙人掌果 x1, 冰块 x2, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_CARROT_POTATO_SOUP, "胡萝卜土豆汤，营养均衡", "胡萝卜 x1, 土豆 x1, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_COCONUT_BERRY_DRINK, "椰汁浆果饮，热带风味", "椰子 x1, 浆果 x2, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_TRUFFLE_MUSHROOM_SOUP, "松露蘑菇汤，高级滋补", "松露 x1, 蘑菇 x2, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_APPLE_HONEY_DRINK, "苹果蜂蜜饮，甜美健康", "苹果 x1, 蜂蜜 x1, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_KELP_FISH_SOUP, "海带鱼鲜汤，海鲜风味", "海带 x1, 鱼 x1, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_WINTER_MELON_CRAWFISH_SOUP, "冬瓜螃蟹汤，清淡鲜美", "冬瓜 x1, 螃蟹 x1, 水 x1");
        addFoodFromConstants(ItemConstants.ITEM_DARK_FOOD, "黑暗料理，效果未知", "随机食材 x3");
        
        // 加载用户解锁状态（基于背包中是否拥有该料理）
        loadUnlockStatusFromBackpack();
    }
    
    private void addFoodFromConstants(String foodName, String description, String recipe) {
        foodList.add(new FoodItem(
            foodName,
            description,
            getImageResIdFromFoodName(foodName),
            recipe,
            false  // 初始状态为未解锁
        ));
    }
    
    private int getImageResIdFromFoodName(String foodName) {
        // 根据料理名称获取对应的图片资源ID
        switch (foodName) {
            case ItemConstants.ITEM_GRILLED_FISH: return R.drawable.kaoyu;
            case ItemConstants.ITEM_GRILLED_CRAWFISH: return R.drawable.kaopangxie;
            case ItemConstants.ITEM_FISH_SOUP: return R.drawable.yutang;
            case ItemConstants.ITEM_MUSHROOM_SOUP: return R.drawable.mogutang;
            case ItemConstants.ITEM_KELP_SOUP: return R.drawable.haidaitang;
            case ItemConstants.ITEM_FRUIT_PIE: return R.drawable.shuiguopai;
            case ItemConstants.ITEM_ADVANCED_HERB: return R.drawable.gaojicaoyao;
            case ItemConstants.ITEM_ICY_SNOW_LOTUS_COCONUT: return R.drawable.bingzhenxuelianyezhi;
            case ItemConstants.ITEM_HONEY_APPLE_SLICE: return R.drawable.fengmipingguopian;
            case ItemConstants.ITEM_RICE_PORRIDGE: return R.drawable.damiqingzhou;
            case ItemConstants.ITEM_KELP_WINTER_MELON_SOUP: return R.drawable.haidaidongguatang;
            case ItemConstants.ITEM_ROASTED_POTATO: return R.drawable.kaotudou;
            case ItemConstants.ITEM_FRUIT_SMOOTHIE: return R.drawable.shuiguobingsha;
            case ItemConstants.ITEM_CRAWFISH_SHELL_SOUP: return R.drawable.pangxiebeiketang;
            case ItemConstants.ITEM_STEAMED_CORN: return R.drawable.zhengyumi;
            case ItemConstants.ITEM_MUSHROOM_TRUFFLE_FRY: return R.drawable.mogusonglujian;
            case ItemConstants.ITEM_BERRY_HONEY_BREAD: return R.drawable.jiangguofengmimianbao;
            case ItemConstants.ITEM_BEET_HONEY_DRINK: return R.drawable.tiancaifengmiyin;
            case ItemConstants.ITEM_BOILED_SPINACH: return R.drawable.shuizhubocai;
            case ItemConstants.ITEM_ROASTED_ACORN: return R.drawable.kaoxiangguo;
            case ItemConstants.ITEM_CACTUS_FRUIT_ICE_DRINK: return R.drawable.xianrenzhangguobingyin;
            case ItemConstants.ITEM_CARROT_POTATO_SOUP: return R.drawable.huluobotudoutang;
            case ItemConstants.ITEM_COCONUT_BERRY_DRINK: return R.drawable.yezhijiangguoyin;
            case ItemConstants.ITEM_TRUFFLE_MUSHROOM_SOUP: return R.drawable.songlumogutang;
            case ItemConstants.ITEM_APPLE_HONEY_DRINK: return R.drawable.pingguofengmiyin;
            case ItemConstants.ITEM_KELP_FISH_SOUP: return R.drawable.haidaiyuxiantang;
            case ItemConstants.ITEM_WINTER_MELON_CRAWFISH_SOUP: return R.drawable.dongguapangxietang;
            case ItemConstants.ITEM_DARK_FOOD: return R.drawable.heianliaoli;
            default: return R.drawable.ic_cooking;
        }
    }
    
    private void loadUnlockStatusFromBackpack() {
        // 从背包中获取用户实际拥有的料理，并设置解锁状态
        DBHelper dbHelper = DBHelper.getInstance(this);
        Map<String, Integer> backpack = dbHelper.getBackpack(userId);
        
        for (FoodItem item : foodList) {
            // 如果背包中有该料理，则解锁
            boolean unlocked = backpack.containsKey(item.getName()) && backpack.get(item.getName()) > 0;
            item.setUnlocked(unlocked);
        }
    }
    
    public void showFoodDetailDialog(FoodItem foodItem) {
        Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.setContentView(R.layout.dialog_food_detail);
        
        ImageView foodImage = dialog.findViewById(R.id.dialog_food_image);
        TextView foodName = dialog.findViewById(R.id.dialog_food_name);
        TextView foodDescription = dialog.findViewById(R.id.dialog_food_description);
        TextView foodRecipe = dialog.findViewById(R.id.dialog_food_recipe);
        LinearLayout closeButton = dialog.findViewById(R.id.dialog_close_button);
        
        foodImage.setImageResource(foodItem.getImageResId());
        foodName.setText(foodItem.getName());
        foodDescription.setText(foodItem.getDescription());
        foodRecipe.setText("配方: " + foodItem.getRecipe());
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
}