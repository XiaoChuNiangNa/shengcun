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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.lang.ref.WeakReference;

public class EquipmentActivity extends AppCompatActivity {
    private static final String TAG = "EquipmentActivity";
    private DBHelper dbHelper;
    private GridView gvEquipment;
    private ImageButton btnBack;
    private ImageView ivCurrentEquip;
    private TextView tvCurrentEquipName;
    private TextView tvCurrentEquipDurability;

    private List<Equipment> equipmentList; // 装备列表（支持相同装备分开显示）
    private EquipmentGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);

        dbHelper = DBHelper.getInstance(this);
        initView();
        loadEquipmentData();
    }

    private void initView() {
        // 顶部当前装备控件
        ivCurrentEquip = findViewById(R.id.iv_current_equip);
        tvCurrentEquipName = findViewById(R.id.tv_current_equip_name);
        tvCurrentEquipDurability = findViewById(R.id.tv_current_equip_durability);

        // 装备列表GridView（四格一行）
        gvEquipment = findViewById(R.id.gv_equipment);
        gvEquipment.setNumColumns(4);

        // 返回按钮
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 装备项点击事件
        gvEquipment.setOnItemClickListener((parent, view, position, id) -> {
            if (equipmentList == null || position >= equipmentList.size()) return;
            Equipment equip = equipmentList.get(position);
            showEquipActionDialog(equip, position);
        });
    }

    /**
     * 加载装备数据（支持相同装备分开显示）
     */
    private void loadEquipmentData() {
        equipmentList = new ArrayList<>();
        try {
            // 核心：从背包同步所有工具类物品到装备列表
            List<Equipment> backpackTools = dbHelper.getToolsFromBackpack(MyApplication.currentUserId);
            if (backpackTools != null && !backpackTools.isEmpty()) {
                // 直接使用返回的Equipment对象列表
                equipmentList.addAll(backpackTools);
            } else {
                initTestEquipments(); // 异常时加载测试数据
            }

//            // 确保默认均为未装备（覆盖可能的异常状态）
//            for (Equipment equip : equipmentList) {
//                equip.setEquipped(false);
//            }

            // 确保最多只有一个装备处于激活状态
            ensureSingleEquipped();
            updateCurrentEquipDisplay();

            // 设置适配器
            adapter = new EquipmentGridAdapter(this, equipmentList);
            gvEquipment.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "加载装备数据失败", e);
            Toast.makeText(this, "装备数据加载失败", Toast.LENGTH_SHORT).show();
            initTestEquipments();
            adapter = new EquipmentGridAdapter(this, equipmentList);
            gvEquipment.setAdapter(adapter);
        }
    }

    /**
     * 初始化测试装备数据（补充5个参数，解决参数不匹配问题）
     */
    private void initTestEquipments() {
        // 石质斧头（2个不同耐久）
        equipmentList.add(new Equipment(
                1, // 第1个参数：装备ID
                ItemConstants.EQUIP_STONE_AXE, // 第2个参数：装备类型
                40, // 第3个参数：当前耐久
                ToolUtils.getToolInitialDurability(ItemConstants.EQUIP_STONE_AXE), // 第4个参数：最大耐久
                false // 第5个参数：是否装备
        ));
        equipmentList.add(new Equipment(
                2,
                ItemConstants.EQUIP_STONE_AXE,
                50,
                ToolUtils.getToolInitialDurability(ItemConstants.EQUIP_STONE_AXE),
                false
        ));
        // 铁质镐子
        equipmentList.add(new Equipment(
                3,
                ItemConstants.EQUIP_IRON_PICKAXE,
                80,
                ToolUtils.getToolInitialDurability(ItemConstants.EQUIP_IRON_PICKAXE),
                false
        ));
        // 石质鱼竿
        equipmentList.add(new Equipment(
                4,
                ItemConstants.EQUIP_STONE_FISHING_ROD,
                30,
                ToolUtils.getToolInitialDurability(ItemConstants.EQUIP_STONE_FISHING_ROD),
                false
        ));
        // 钻石镰刀
        equipmentList.add(new Equipment(
                5,
                ItemConstants.EQUIP_DIAMOND_SICKLE,
                90,
                ToolUtils.getToolInitialDurability(ItemConstants.EQUIP_DIAMOND_SICKLE),
                false
        ));
    }

    /**
     * 确保列表中最多只有一个装备处于已装备状态（处理数据异常）
     */
    private void ensureSingleEquipped() {
        int equippedCount = 0;
        Equipment lastEquipped = null;
        for (Equipment equip : equipmentList) {
            if (equip.isEquipped()) {
                equippedCount++;
                lastEquipped = equip;
            }
        }
        // 若超过一个已装备，只保留最后一个，其余设为未装备
        if (equippedCount > 1) {
            for (Equipment equip : equipmentList) {
                if (equip != lastEquipped) {
                    equip.setEquipped(false);
                }
            }
            // 同步到数据库（实际项目中需要）
            if (lastEquipped != null) {
                updateEquipStatusInDB(lastEquipped.getType(), true);
            }
        }
    }

    /**
     * 更新顶部当前装备显示
     */
    private void updateCurrentEquipDisplay() {
        Equipment currentEquip = null;
        // List<Equipment> freshEquips = dbHelper.getToolsFromBackpack(MyApplication.currentUserId); // 暂时注释掉
        for (Equipment equip : equipmentList) {
            if (equip.isEquipped()) {
                currentEquip = equip;
                break;
            }
        }

        if (currentEquip != null) {
            // 显示当前装备信息
            ivCurrentEquip.setImageResource(ResourceImageManager.getItemImage(currentEquip.getType()));
            tvCurrentEquipName.setText(currentEquip.getType());
            tvCurrentEquipDurability.setText(String.format("耐久度：%d/%d",
                    currentEquip.getDurability(), currentEquip.getMaxDurability()));
        } else {
            // 无当前装备
            ivCurrentEquip.setImageResource(android.R.color.transparent);
            tvCurrentEquipName.setText("未装备任何工具");
            tvCurrentEquipDurability.setText("耐久度：0/0");
        }
    }

    /**
     * 显示装备操作弹窗
     */
    private void showEquipActionDialog(Equipment equip, int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_equipment_action, null);
        if (dialogView == null) return;

        // 初始化弹窗控件
        ImageView ivEquip = dialogView.findViewById(R.id.iv_dialog_equip);
        TextView tvName = dialogView.findViewById(R.id.tv_dialog_equip_name);
        TextView tvInfo = dialogView.findViewById(R.id.tv_dialog_equip_info);
        Button btnUse = dialogView.findViewById(R.id.btn_equip_use);
        Button btnRemove = dialogView.findViewById(R.id.btn_equip_remove);
        Button btnDiscard = dialogView.findViewById(R.id.btn_equip_discard);

        // 设置弹窗内容
        ivEquip.setImageResource(ResourceImageManager.getItemImage(equip.getType()));
        tvName.setText(equip.getType());
        String info = String.format(
                "类型：工具\n效果：%s\n耐久度：%d/%d\n%s",
                ToolUtils.getEquipDescription(equip.getType()),
                equip.getDurability(),
                equip.getMaxDurability(),
                equip.isEquipped() ? "状态：已装备" : "状态：未装备"
        );
        tvInfo.setText(info);

        // 控制"卸下"按钮显示（仅已装备的可见）
        btnRemove.setVisibility(equip.isEquipped() ? View.VISIBLE : View.GONE);

        // 创建弹窗
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // 装备按钮点击（设置为当前装备）
        btnUse.setOnClickListener(v -> {
            if (equip.isEquipped()) {
                Log.i("EquipmentLog", "装备操作: 工具" + equip.getType() + "已装备，无需重复装备");
                Toast.makeText(this, "已装备该工具", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("EquipmentLog", "开始装备工具: " + equip.getType() + "，耐久度: " + equip.getDurability() + "/" + equip.getMaxDurability());
                
                // 先取消其他所有装备的状态
                for (Equipment e : equipmentList) {
                    if (e.isEquipped()) {
                        Log.d("EquipmentLog", "卸下原有装备: " + e.getType());
                        e.setEquipped(false);
                        // 同步到数据库（更新为未装备）
                        dbHelper.updateToolEquipStatus(MyApplication.currentUserId, e.getType(), false);
                    }
                }
                
                // 设置当前装备为已装备
                equip.setEquipped(true);
                // 同步到数据库（更新为已装备）
                dbHelper.updateToolEquipStatus(MyApplication.currentUserId, equip.getType(), true);
                // 更新主页面当前装备显示
                MyApplication.currentEquip = equip.getType();
                
                Log.i("EquipmentLog", "装备成功: " + equip.getType() + "，工具效果: " + ToolUtils.getEquipDescription(equip.getType()));
                Toast.makeText(this, "已装备" + equip.getType(), Toast.LENGTH_SHORT).show();
                updateCurrentEquipDisplay();
                adapter.notifyDataSetChanged();
            }
            dialog.dismiss();
        });

        // 卸下按钮点击事件（修改后）
        btnRemove.setOnClickListener(v -> {
            Log.i("EquipmentLog", "开始卸下工具: " + equip.getType() + "，当前耐久度: " + equip.getDurability() + "/" + equip.getMaxDurability());
            
            equip.setEquipped(false);
            // 同步到数据库
            dbHelper.updateToolEquipStatus(MyApplication.currentUserId, equip.getType(), false);
            
            Log.i("EquipmentLog", "卸下成功: " + equip.getType() + "，当前无装备工具");
            Toast.makeText(this, "已卸下" + equip.getType(), Toast.LENGTH_SHORT).show();
            
            // 立即刷新装备列表
            loadEquipmentData();
            updateCurrentEquipDisplay();
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        // 丢弃按钮点击
        btnDiscard.setOnClickListener(v -> {
            Log.i("EquipmentLog", "开始丢弃工具: " + equip.getType() + "，耐久度: " + equip.getDurability() + "/" + equip.getMaxDurability() + 
                    "，是否已装备: " + equip.isEquipped());
            
            // 若丢弃的是当前装备，需先取消装备状态
            boolean wasEquipped = equip.isEquipped();
            equipmentList.remove(position);
            if (wasEquipped) {
                Log.d("EquipmentLog", "丢弃已装备工具，先取消装备状态");
                // 同步到数据库（实际项目中使用）
                updateEquipStatusInDB(equip.getType(), false);
                updateCurrentEquipDisplay();
            }
            // 从数据库删除该装备（修复：从背包中删除，而不是独立的装备表）
            dbHelper.updateBackpackItem(MyApplication.currentUserId, equip.getType(), -1);
            
            Log.i("EquipmentLog", "丢弃成功: " + equip.getType() + "已从背包中移除");
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "已丢弃" + equip.getType(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * 同步装备状态到数据库
     */
    private void updateEquipStatusInDB(String equipType, boolean isEquipped) {
        // 使用线程池执行数据库操作，避免AsyncTask的潜在内存泄漏
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 先将所有装备设为未装备
                dbHelper.updateAllEquipmentStatus(MyApplication.currentUserId);
                // 再将目标装备设为已装备（如果需要）
                if (isEquipped) {
                    dbHelper.updateEquipmentStatus(MyApplication.currentUserId, equipType, true);
                }
                // 操作成功后在主线程提示
                runOnUiThread(() ->
                        Toast.makeText(this, "装备状态更新成功", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                Log.e(TAG, "更新装备状态到数据库失败", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "装备状态更新失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}