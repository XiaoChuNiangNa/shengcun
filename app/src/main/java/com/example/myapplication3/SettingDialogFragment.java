package com.example.myapplication3;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.HashMap;
import java.util.Map;

public class SettingDialogFragment extends DialogFragment implements View.OnClickListener {
    private DBHelper dbHelper;
    private Switch swSound;
    private Button btnSaveGame, btnLogout, btnExitGame; // 新增退出游戏按钮引用
    private int soundStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting, container, false);
        dbHelper = DBHelper.getInstance(getContext());
        initView(view);
        loadSoundSetting();
        return view;
    }

    private void initView(View view) {
        swSound = view.findViewById(R.id.sw_sound);
        btnSaveGame = view.findViewById(R.id.btn_save_game);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnExitGame = view.findViewById(R.id.btn_exit_game); // 初始化退出游戏按钮

        // 绑定点击事件
        //btnBack.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        // 注释掉保存游戏按钮的点击事件
        // btnSaveGame.setOnClickListener(this);
        btnExitGame.setOnClickListener(this); // 退出游戏按钮事件

        // 隐藏保存游戏按钮
        btnSaveGame.setVisibility(View.GONE);
    }

    private void loadSoundSetting() {
        Map<String, Object> userStatus = dbHelper.getUserStatus(MyApplication.currentUserId);
        soundStatus = (int) userStatus.get("sound_status");
        swSound.setChecked(soundStatus == 1);
    }

    private void saveSoundSetting() {
        int newSoundStatus = swSound.isChecked() ? 1 : 0;
        if (newSoundStatus == soundStatus) {
            dismiss();
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("sound_status", newSoundStatus);
        dbHelper.updateUserStatus(MyApplication.currentUserId, updateData);

        Toast.makeText(getContext(), "设置已保存", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    // 退出登录逻辑
    private void logout() {
        if (getContext() == null) return;

        MyApplication.currentUserId = -1;
        getContext().getSharedPreferences("user_info", 0).edit()
                .remove("user_id")
                .apply();

        // 检查Activity是否存在
        if (getActivity() != null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            // 添加栈清理标志，确保登录页为栈顶且无法返回
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
        dismiss();
    }

    // 新增退出游戏逻辑
    private void exitGame() {
        if (getActivity() == null) return;
        // 关闭所有活动并退出应用
        getActivity().finishAffinity(); // 关闭当前应用的所有Activity
        System.exit(0); // 退出应用进程
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            saveSoundSetting();
        } else if (id == R.id.btn_logout) {
            logout();
        } else if (id == R.id.btn_exit_game) {
            exitGame();
        }
        // 注释掉保存游戏按钮的点击事件
        // else if (id == R.id.btn_save_game) {
        //     new SaveGameDialogFragment().show(getParentFragmentManager(), "save_game");
        // }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}