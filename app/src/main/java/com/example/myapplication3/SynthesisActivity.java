package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication3.R;
import com.example.myapplication3.Constant;
import com.example.myapplication3.Recipe;

import java.util.List;
import java.util.Map;

public class SynthesisActivity extends BaseActivity {
    protected Map<String, Integer> backpack; // 添加背包变量声明

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化背包数据
        backpack = dbHelper.getBackpack(userId);
        
        setContentView(R.layout.activity_synthesis);

        LinearLayout container = findViewById(R.id.container);
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // 检查是否从基地进入，如果是则返回基地
            if (isFromBase()) {
                startActivity(new Intent(SynthesisActivity.this, BaseActivity.class));
                finish();
            } else {
                finish();
            }
        });

        // 从配方管理类获取所有合成配方并添加到界面
        List<Recipe> allRecipes = SynthesisRecipeManager.getAllSynthesisRecipes();
        for (Recipe recipe : allRecipes) {
            addRecipe(container, recipe.getName(), recipe.getRequirements());
        }
    }

    /**
     * 向容器中添加合成配方项
     * @param container 父容器
     * @param name 合成物品名称
     * @param requirements 合成所需材料
     */
    private void addRecipe(LinearLayout container, String name, Map<String, Integer> requirements) {
        LinearLayout item = (LinearLayout) getLayoutInflater().inflate(R.layout.item_synthesis, container, false);
        TextView tvName = item.findViewById(R.id.tv_name);
        TextView tvReq = item.findViewById(R.id.tv_requirements);
        Button btnSynthesize = item.findViewById(R.id.btn_synthesize);

        tvName.setText(name);
        tvReq.setText(getRequirementsText(requirements));

        btnSynthesize.setOnClickListener(v -> synthesize(name, requirements));
        container.addView(item);
    }

    /**
     * 将材料要求转换为显示文本
     * @param req 材料要求Map
     * @return 格式化后的文本
     */
    private String getRequirementsText(Map<String, Integer> req) {
        if (req == null || req.isEmpty()) {
            return "无需求";
        }

        StringBuilder sb = new StringBuilder("需求：");
        for (Map.Entry<String, Integer> entry : req.entrySet()) {
            sb.append(entry.getValue()).append("个").append(entry.getKey()).append("，");
        }
        // 移除最后一个逗号
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 执行合成操作
     * @param name 合成物品名称
     * @param requirements 合成所需材料
     */
    private void synthesize(String name, Map<String, Integer> requirements) {
        // 检查材料是否充足
        if (!checkMaterials(requirements)) {
            return;
        }

        // 扣除材料
        deductMaterials(requirements);

        // 检查是否从基地进入，如果是则放入仓库
        boolean success;
        if (isFromBase()) {
            // 基地中合成物品直接放入仓库
            success = dbHelper.updateWarehouseItem(userId, name, 1);
            if (success) {
                showToast(name + "合成成功！物品已放入仓库。");
            } else {
                showToast(name + "仓库容量不足！");
                return;
            }
        } else {
            // 游戏中合成物品放入背包
            success = dbHelper.updateBackpackItem(userId, name, 1);
            if (success) {
                // 增加合成物品次数
                int newSynthesisCount = dbHelper.incrementSynthesisTimes(userId);
                
                showToast(name + "合成成功！");
                // 立即刷新背包数据
                backpack = dbHelper.getBackpack(userId);
                
                // 添加日志到Logcat
                Log.d("SynthesisActivity", "合成物品成就次数已更新: " + newSynthesisCount);
            } else {
                showToast(name + "背包容量不足！");
                return;
            }
        }
    }

    /**
     * 检查材料是否充足
     * @param requirements 所需材料
     * @return 是否充足
     */
    protected boolean checkMaterials(Map<String, Integer> requirements) {
        // 检查背包是否加载
        if (backpack == null) {
            showToast("背包数据加载失败");
            return false;
        }

        // 检查每种材料是否满足需求
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String item = entry.getKey();
            int required = entry.getValue();
            int have = backpack.getOrDefault(item, 0);

            if (have < required) {
                showToast("缺少" + (required - have) + "个" + item);
                return false;
            }
        }
        return true;
    }

    /**
     * 扣除合成所需材料
     * @param requirements 所需材料
     */
    protected void deductMaterials(Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String item = entry.getKey();
            int count = entry.getValue();
            dbHelper.updateBackpackItem(userId, item, -count);
        }
    }

    /**
     * 检查是否从基地进入
     */
    protected boolean isFromBase() {
        return getIntent().getBooleanExtra("from_base", false);
    }

    /**
     * 显示提示信息
     * @param message 提示内容
     */
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
}