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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LoadGameDialogFragment extends DialogFragment {
    private DBHelper dbHelper;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_save_game, container, false); // 使用和保存界面相同的布局
        dbHelper = DBHelper.getInstance(getContext());
        userId = MyApplication.currentUserId;

        // 修改标题
        View titleView = view.findViewById(android.R.id.title);
        if (titleView != null && titleView instanceof android.widget.TextView) {
            ((android.widget.TextView) titleView).setText("加载存档");
        }

        // 初始化3个存档位按钮
        for (int i = 1; i <= 3; i++) {
            int slotId = i;
            Button btnSlot = view.findViewById(getResources().getIdentifier(
                    "btn_slot_" + i, "id", getContext().getPackageName()));
            if (btnSlot != null) { // 防止空指针
                updateSlotButton(btnSlot, slotId);
                btnSlot.setOnClickListener(v -> loadFromSlot(slotId));
            }
        }
        return view;
    }

    // 更新存档位按钮显示
    private void updateSlotButton(Button btn, int slotId) {
        List<Map<String, Object>> slots = dbHelper.getSaveSlots(userId);
        for (Map<String, Object> slot : slots) {
            if (slot.containsKey("slotId") && (int) slot.get("slotId") == slotId) {
                long time = slot.containsKey("saveTime") ? (long) slot.get("saveTime") : 0;
                btn.setText("存档位" + slotId + "\n" + new SimpleDateFormat("MM-dd HH:mm").format(new Date(time)));
                return;
            }
        }
        btn.setText("存档位" + slotId + "\n(空)");
    }

    // 从存档位加载
    private void loadFromSlot(int slotId) {
        if (getContext() == null) return;

        // 检查该存档位是否有数据
        boolean hasData = checkSlotHasData(slotId);

        if (!hasData) {
            // 空存档位，显示提示
            new AlertDialog.Builder(getContext())
                    .setTitle("提示")
                    .setMessage("存档位" + slotId + "为空，无法加载。")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        // 有数据则加载
        SaveData data = dbHelper.loadSaveSlot(userId, slotId);
        if (data != null) {
            // 恢复游戏状态
            dbHelper.restoreGameStatus(userId, data);
            Toast.makeText(getContext(), "读档成功", Toast.LENGTH_SHORT).show();
            
            // 延迟关闭，让用户看到成功提示
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // 通知主界面刷新坐标显示
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.refreshCoordinatesAfterLoad();
                }
                
                if (getActivity() != null) {
                    getActivity().finish();
                }
                dismiss();
            }, 500);
        } else {
            Toast.makeText(getContext(), "读档失败", Toast.LENGTH_SHORT).show();
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