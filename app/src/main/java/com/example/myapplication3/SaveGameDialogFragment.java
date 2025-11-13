package com.example.myapplication3;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveGameDialogFragment extends DialogFragment {
    private DBHelper dbHelper;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_save_game, container, false);
        dbHelper = DBHelper.getInstance(getContext());
        userId = MyApplication.currentUserId;

        // 初始化3个存档位按钮
        for (int i = 1; i <= 3; i++) {
            int slotId = i;
            Button btnSlot = view.findViewById(getResources().getIdentifier(
                    "btn_slot_" + i, "id", getContext().getPackageName()));
            if (btnSlot != null) { // 防止空指针
                updateSlotButton(btnSlot, slotId);
                btnSlot.setOnClickListener(v -> saveToSlot(slotId));
            }
        }
        return view;
    }

    // 修正：getSaveSlots只需要userId（解决Expected 1 argument but found 2）
    private void updateSlotButton(Button btn, int slotId) {
        List<Map<String, Object>> slots = dbHelper.getSaveSlots(userId); // 这里改为1个参数
        for (Map<String, Object> slot : slots) {
            if (slot.containsKey("slotId") && (int) slot.get("slotId") == slotId) {
                long time = slot.containsKey("saveTime") ? (long) slot.get("saveTime") : 0;
                btn.setText("存档位" + slotId + "\n" + new SimpleDateFormat("MM-dd HH:mm").format(new Date(time)));
                return;
            }
        }
        btn.setText("存档位" + slotId + "\n(空)");
    }

    // 修正：saveGameSlot传入4个参数（解决Expected 4 arguments but found 1和类型不匹配）
    private void saveToSlot(int slotId) {
        if (getContext() == null) return;

        // 检查该存档位是否已有数据
        boolean hasExistingData = checkSlotHasData(slotId);

        // 如果已有数据，显示确认覆盖对话框
        if (hasExistingData) {
            new AlertDialog.Builder(getContext())
                    .setTitle("覆盖存档")
                    .setMessage("存档位" + slotId + "已有数据，确定要覆盖吗？")
                    .setPositiveButton("确认", (dialog, which) -> doSave(slotId)) // 执行保存
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            // 无数据则直接保存
            doSave(slotId);
        }
    }

    // 检查存档位是否有数据
    private boolean checkSlotHasData(int slotId) {
        List<Map<String, Object>> slots = dbHelper.getSaveSlots(userId);
        for (Map<String, Object> slot : slots) {
            Object slotIdObj = slot.get("slotId");
            // 安全转换：先判断类型再转换
            if (slotIdObj instanceof Number) {
                if (((Number) slotIdObj).intValue() == slotId) {
                    return true;
                }
            }
        }
        return false;
    }

    // 实际执行保存的逻辑（提取原saveToSlot中的保存代码）
    private void doSave(int slotId) {
        try {
            // 获取当前游戏状态（用户基础状态）
            Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
            if (userStatus == null) {
                Toast.makeText(getContext(), "获取游戏状态失败", Toast.LENGTH_SHORT).show();
                return;
            }

            // 补充获取其他必要数据（背包、耐久度、已装备物品）
            Map<String, Integer> backpack = dbHelper.getBackpack(userId);
            Map<String, Integer> durability = dbHelper.getAllItemDurability(userId);
            List<String> equipped = dbHelper.getEquippedItems(userId);

            // 处理可能的空值
            if (backpack == null) backpack = new HashMap<>();
            if (durability == null) durability = new HashMap<>();
            if (equipped == null) equipped = new ArrayList<>();

            // 序列化游戏数据
            SaveData saveData = SaveData.fromUserStatus(userStatus, backpack, durability, equipped);
            String saveDataStr = new Gson().toJson(saveData);
            long saveTime = System.currentTimeMillis();

            // 保存到数据库
            dbHelper.saveGameSlot(userId, slotId, saveDataStr, saveTime);

            // 刷新当前存档位按钮显示
            View rootView = getView();
            if (rootView != null) {
                Button currentBtn = rootView.findViewById(getResources().getIdentifier(
                        "btn_slot_" + slotId, "id", getContext().getPackageName()));
                if (currentBtn != null) {
                    updateSlotButton(currentBtn, slotId);
                }
            }

            Toast.makeText(getContext(), "已保存到存档位" + slotId + "，即将退出游戏", Toast.LENGTH_SHORT).show();

            // 延迟关闭并退出游戏，让用户看到保存成功提示
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                dismiss();
                // 保存后正常退出游戏，保持游戏状态为"游戏中"
                if (getActivity() != null) {
                    // 设置游戏状态为已开始，确保下次登录时直接进入游戏
                    GameStateManager gameStateManager = GameStateManager.getInstance(getContext());
                    gameStateManager.setGameStarted(true);
                    
                    // 保存并退出到标题页
                    getActivity().finish();
                }
            }, 1000);

        } catch (Exception e) {
            Toast.makeText(getContext(), "存档失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // 完善对话框样式
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 去除标题栏
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // 设置宽度为屏幕80%，高度自适应
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}