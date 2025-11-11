package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FunctionListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FunctionAdapter adapter;

    private List<FunctionItem> functionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_list);

        initData();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FunctionAdapter(functionList);
        recyclerView.setAdapter(adapter);
    }

    private void initData() {
        //functionList.add(new FunctionItem("背包", R.drawable.ic_backpack, BackpackActivity.class));
        //functionList.add(new FunctionItem("装备", R.drawable.ic_equipment, EquipmentActivity.class));
        functionList.add(new FunctionItem("合成", R.drawable.ic_synthesis, SynthesisActivity.class));
        functionList.add(new FunctionItem("建筑", R.drawable.ic_building, BuildingActivity.class));
        functionList.add(new FunctionItem("烹饪", R.drawable.ic_cooking, CookingActivity.class));
        functionList.add(new FunctionItem("睡眠", R.drawable.ic_sleep, RestActivity.class));
        functionList.add(new FunctionItem("熔炼", R.drawable.ic_smelting, SmeltingActivity.class));
        functionList.add(new FunctionItem("贸易", R.drawable.ic_trading, TradingActivity.class));
        functionList.add(new FunctionItem("轮回", R.drawable.ic_reincarnation, ReincarnationActivity.class));
        functionList.add(new FunctionItem("读档", R.drawable.ic_load, null)); // 读档逻辑特殊
//        functionList.add(new FunctionItem("重置", R.drawable.ic_reset, null)); // 重置逻辑特殊
//        functionList.add(new FunctionItem("设置", R.drawable.ic_setting, SettingActivity.class));
    }

    class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.ViewHolder> {

        private List<FunctionItem> dataList;

        public FunctionAdapter(List<FunctionItem> list) {
            this.dataList = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_function, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FunctionItem item = dataList.get(position);
            holder.tvName.setText(item.name);
            holder.ivIcon.setImageResource(item.iconRes);

            holder.itemView.setOnClickListener(v -> {
                if (item.targetClass != null) {
                    Intent intent = new Intent(FunctionListActivity.this, item.targetClass);
                    // 如果是烹饪、熔炼、贸易活动，设置需要检测建筑标记
                    if (item.targetClass == CookingActivity.class || 
                        item.targetClass == SmeltingActivity.class || 
                        item.targetClass == TradingActivity.class) {
                        intent.putExtra("check_building", true);
                    }
                    startActivity(intent);
                } else {
                    // 特殊功能：读档/重置
                    if (item.name.equals("读档")) {
                        new LoadGameDialogFragment().show(getSupportFragmentManager(), "load_game");
                    } else if (item.name.equals("重置")) {
                        showResetConfirmDialog(); // 显示重置确认弹窗
                        // 调用 MainActivity 的重置逻辑

                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvName;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_icon);
                tvName = itemView.findViewById(R.id.tv_name);
            }
        }
    }

    class FunctionItem {
        String name;
        int iconRes;
        Class<?> targetClass;

        public FunctionItem(String name, int iconRes, Class<?> targetClass) {
            this.name = name;
            this.iconRes = iconRes;
            this.targetClass = targetClass;
        }
    }
    private void showResetConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认重置")
                .setMessage("确定要重置所有游戏状态吗？\n" +
                        "包括：背包清空、所有进度重置、随机复活到初始区域")
                .setPositiveButton("确认", (dialog, which) -> performReset())
                .setNegativeButton("取消", null)
                .show();
    }
    private void performReset() {
        DBHelper dbHelper = DBHelper.getInstance(this);
        int userId = MyApplication.currentUserId;

        // 1. 重置游戏状态
        dbHelper.resetGame(userId);

        // 2. 新增：如果是测试账号，重新初始化其特权数据
        if (dbHelper.isTestAccount(userId)) {
            dbHelper.reinitTestAccountData(userId);
        } else {
            // 非测试账号才清空背包（测试账号的背包由reinitTestAccountData处理）
            dbHelper.clearBackpack(userId);
        }

        // 3. 生成随机坐标（保持原有逻辑）
        Random random = new Random();
        int randomX = random.nextInt(Constant.MAP_MAX - Constant.MAP_MIN + 1) + Constant.MAP_MIN;
        int randomY = random.nextInt(Constant.MAP_MAX - Constant.MAP_MIN + 1) + Constant.MAP_MIN;

        Map<String, Object> coordData = new HashMap<>();
        coordData.put("current_x", randomX);
        coordData.put("current_y", randomY);
        dbHelper.updateUserStatus(userId, coordData);

        // 4. 页面跳转（保持原有逻辑）
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("needReloadData", true);
        startActivity(intent);
        finish();
        Toast.makeText(this, "游戏已重置", Toast.LENGTH_LONG).show();
    }
}