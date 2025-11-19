package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.ref.WeakReference;

import com.example.myapplication3.R;
import com.example.myapplication3.Constant;
import com.example.myapplication3.Recipe;
import com.example.myapplication3.RecipeManager;

public class CookingActivity extends BaseActivity {

    // 食材选择区域控件
    private LinearLayout ingredient1;
    private LinearLayout ingredient2;
    private LinearLayout ingredient3;
    private TextView tvIngredient1;
    private TextView tvIngredient2;
    private TextView tvIngredient3;
    private ImageView ivIngredient1;
    private ImageView ivIngredient2;
    private ImageView ivIngredient3;
    private Button btnCook;

    // 存储选中的食材（键：食材框索引，值：食材名称）
    private Map<Integer, String> selectedIngredients = new HashMap<>();
    
    // 背包数据
    protected Map<String, Integer> backpack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化背包数据
        backpack = dbHelper.getBackpack(userId);

        // 直接初始化活动，无需建筑检查
        initActivity();
    }

    private void initActivity() {
        setContentView(R.layout.activity_cooking);

        // 初始化控件
        initViews();

        // 设置返回按钮点击事件
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // 记录返回跳转日志
            android.util.Log.d("NavigationLog", "从 CookingActivity 返回到上一个页面");
            // 直接调用finish()返回上一个活动
            finish();
        });

        // 设置食材选择框点击事件
        setIngredientClickListeners();

        // 设置烹饪按钮点击事件
        btnCook.setOnClickListener(v -> startCooking());
    }

    /**
     * 初始化所有控件
     */
    private void initViews() {
        // 食材框1
        ingredient1 = findViewById(R.id.ingredient1);
        tvIngredient1 = findViewById(R.id.tv_ingredient1);
        ivIngredient1 = findViewById(R.id.iv_ingredient1);

        // 食材框2
        ingredient2 = findViewById(R.id.ingredient2);
        tvIngredient2 = findViewById(R.id.tv_ingredient2);
        ivIngredient2 = findViewById(R.id.iv_ingredient2);

        // 食材框3
        ingredient3 = findViewById(R.id.ingredient3);
        tvIngredient3 = findViewById(R.id.tv_ingredient3);
        ivIngredient3 = findViewById(R.id.iv_ingredient3);

        // 烹饪按钮
        btnCook = findViewById(R.id.btn_cook);

        // 初始显示默认图片
        setDefaultImage(ivIngredient1);
        setDefaultImage(ivIngredient2);
        setDefaultImage(ivIngredient3);
    }

    /**
     * 设置默认图片
     */
    private void setDefaultImage(ImageView imageView) {
        //imageView.setImageResource(R.drawable.unknown);
    }

    /**
     * 为三个食材选择框设置点击事件
     */
    private void setIngredientClickListeners() {
        // 食材框1点击事件
        ingredient1.setOnClickListener(v -> showIngredientSelectionDialog(1, tvIngredient1, ivIngredient1));

        // 食材框2点击事件
        ingredient2.setOnClickListener(v -> showIngredientSelectionDialog(2, tvIngredient2, ivIngredient2));

        // 食材框3点击事件
        ingredient3.setOnClickListener(v -> showIngredientSelectionDialog(3, tvIngredient3, ivIngredient3));
    }

    /**
     * 显示食材选择对话框
     * @param index 食材框索引
     * @param tv 显示文字的TextView
     * @param iv 显示图片的ImageView
     */
    private void showIngredientSelectionDialog(int index, TextView tv, ImageView iv) {
        // 检查背包是否有食材
        if (backpack.isEmpty() || isBackpackEmpty()) {
            showToast("食材不足");
            return;
        }

        // 获取背包中所有可用食材
        List<String> availableIngredients = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : backpack.entrySet()) {
            if (entry.getValue() > 0) {
                availableIngredients.add(entry.getKey());
            }
        }

        // 转换为数组用于对话框
        String[] ingredientsArray = availableIngredients.toArray(new String[0]);

        // 显示选择对话框
        new AlertDialog.Builder(this)
                .setTitle("选择食材")
                .setItems(ingredientsArray, (dialog, which) -> {
                    String selected = ingredientsArray[which];
                    selectIngredient(selected, tv, iv, index);
                })
                .show();
    }

    /**
     * 检查背包是否为空（所有食材数量为0）
     */
    private boolean isBackpackEmpty() {
        for (int count : backpack.values()) {
            if (count > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 处理食材选择逻辑
     * @param ingredientName 食材名称
     * @param tv 显示文字的TextView
     * @param iv 显示图片的ImageView
     * @param index 食材框索引
     */
    private void selectIngredient(String ingredientName, TextView tv, ImageView iv, int index) {
        // 检查背包中是否有该食材（双重保险）
        int haveCount = backpack.getOrDefault(ingredientName, 0);
        if (haveCount <= 0) {
            showToast("背包中没有" + ingredientName);
            return;
        }

        // 更新文字显示
        if (selectedIngredients.containsKey(index)) {
            // 取消选择
            selectedIngredients.remove(index);
            tv.setText("点击选择");
            setDefaultImage(iv);
        } else {
            // 选择食材
            selectedIngredients.put(index, ingredientName);
            tv.setText(ingredientName);
            // 设置食材图片
            int imgRes = ResourceImageManager.getItemImage(ingredientName);
            //iv.setImageResource(imgRes != 0 ? imgRes : R.drawable.unknown);
        }
    }

    /**
     * 开始烹饪逻辑
     */
    private void startCooking() {
        // 检查是否选择了至少一种食材
        if (selectedIngredients.isEmpty()) {
            showToast("请至少选择一种食材");
            return;
        }

        // 提取选中的食材（去重并统计数量）
        Map<String, Integer> actualIngredients = new HashMap<>();
        for (String name : selectedIngredients.values()) {
            actualIngredients.put(name, actualIngredients.getOrDefault(name, 0) + 1);
        }

        // 检查选中的食材是否足够
        if (!checkMaterials(actualIngredients)) {
            return;
        }

        // 检查燃料（木炭或煤炭）
        if (!checkFuel()) {
            return;
        }

        // 扣除材料和燃料
        deductMaterials(actualIngredients);
        deductFuel();

        // 查找匹配的配方
        Recipe matchedRecipe = findMatchedRecipe(actualIngredients);
        String resultName;

        // 确定烹饪结果
        if (matchedRecipe != null) {
            resultName = matchedRecipe.getName();
            showToast(resultName + "烹饪成功！");
        } else {
            resultName = "黑暗料理";
            showToast("烹饪失败，获得" + resultName + "！");
        }

        // 检查是否从基地进入，如果是则放入仓库
        if (isFromBase()) {
            // 基地中烹饪物品直接放入仓库
            dbHelper.updateWarehouseItem(userId, resultName, 1);
        } else {
            // 游戏中烹饪物品放入背包
            dbHelper.updateBackpackItem(userId, resultName, 1);
        }

        // 刷新背包数据
        backpack = dbHelper.getBackpack(userId);

        // 重置食材选择框
        resetIngredientSelection();
    }

    /**
     * 查找与选中食材匹配的配方
     */
    private Recipe findMatchedRecipe(Map<String, Integer> ingredients) {
        List<Recipe> allRecipes = RecipeManager.getAllRecipes();
        for (Recipe recipe : allRecipes) {
            if (ingredients.equals(recipe.getRequirements())) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * 检查是否有燃料（木炭或煤炭）
     */
    private boolean checkFuel() {
        int charcoalCount = backpack.getOrDefault(ItemConstants.ITEM_CHARCOAL, 0);
        int coalCount = backpack.getOrDefault(ItemConstants.ITEM_COAL, 0);
        if (charcoalCount < 1 && coalCount < 1) {
            showToast("缺少燃料");
            return false;
        }
        return true;
    }

    /**
     * 扣除燃料（优先扣除木炭）
     */
    private void deductFuel() {
        int charcoalCount = backpack.getOrDefault(ItemConstants.ITEM_CHARCOAL, 0);
        if (charcoalCount >= 1) {
            dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_CHARCOAL, -1);
        } else {
            dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_COAL, -1);
        }
    }

    /**
     * 重置食材选择框状态
     */
    private void resetIngredientSelection() {
        // 重置食材框文字和图片
        tvIngredient1.setText("点击选择");
        tvIngredient2.setText("点击选择");
        tvIngredient3.setText("点击选择");

        setDefaultImage(ivIngredient1);
        setDefaultImage(ivIngredient2);
        setDefaultImage(ivIngredient3);

        // 清空选中记录
        selectedIngredients.clear();
    }


    protected boolean checkMaterials(Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String item = entry.getKey();
            int need = entry.getValue();
            int have = backpack.getOrDefault(item, 0);
            if (have < need) {
                showToast("缺少" + (need - have) + "个" + item);
                return false;
            }
        }
        return true;
    }


    protected void deductMaterials(Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            dbHelper.updateBackpackItem(userId, entry.getKey(), -entry.getValue());
        }
    }

    /**
     * 检查是否从基地进入
     */
    protected boolean isFromBase() {
        return getIntent().getBooleanExtra("from_base", false);
    }



    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 禁用系统返回键，只允许使用按钮返回
     */
    @Override
    public void onBackPressed() {
        // 空实现，禁用系统返回功能
        // 用户只能通过页面上的返回按钮返回
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        cleanupResources();
    }

    private void cleanupResources() {
        // 清理图片缓存等资源
        if (selectedIngredients != null) {
            selectedIngredients.clear();
        }
    }



    // 安全的异步任务类 - 烹饪操作
    private static class CookingTask extends AsyncTask<Recipe, Void, Boolean> {
        private WeakReference<CookingActivity> activityRef;
        private String resultMessage;

        CookingTask(CookingActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Recipe... recipes) {
            CookingActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing() && recipes.length > 0) {
                Recipe recipe = recipes[0];
                try {
                    // 执行烹饪逻辑
                    return activity.performCookingOperation(recipe);
                } catch (Exception e) {
                    resultMessage = "烹饪失败: " + e.getMessage();
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            CookingActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                if (success) {
                    activity.showToast("烹饪成功！");
                    activity.resetIngredients();
                } else {
                    activity.showToast(resultMessage != null ? resultMessage : "烹饪失败");
                }
            }
        }
    }

    private boolean performCookingOperation(Recipe recipe) {
        // 实现具体的烹饪操作逻辑
        // 这里应该包含食材消耗、成品生成等数据库操作
        return true;
    }

    private void resetIngredients() {
        // 重置食材选择状态
        selectedIngredients.clear();
        // 更新UI显示
        updateIngredientDisplay();
    }

    private void updateIngredientDisplay() {
        // 更新食材显示逻辑
    }
}