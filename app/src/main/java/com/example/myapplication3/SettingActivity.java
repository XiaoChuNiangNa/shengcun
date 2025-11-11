//package com.example.myapplication3;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Switch;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
//    private DBHelper dbHelper;
//    private TextView tvDifficulty;
//    private Switch swSound;
//    // 移除 btnEasy 变量（布局中不存在）
//    private Button btnNormal, btnMedium, btnHard, btnReset, btnSave, btnBack, btnLogout;
//
//    private int soundStatus;
//    private String difficulty;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_setting);
//
//        dbHelper = DBHelper.getInstance(this);
//        initView();
//        loadSettingData();
//    }
//
//    private void initView() {
//        tvDifficulty = findViewById(R.id.tv_difficulty);
//        swSound = findViewById(R.id.sw_sound);
//        // 初始化布局中存在的三个难度按钮
//        btnNormal = findViewById(R.id.btn_normal);
//        btnMedium = findViewById(R.id.btn_medium);
//        btnHard = findViewById(R.id.btn_hard);
//        btnReset = findViewById(R.id.btn_reset);
//        btnSave = findViewById(R.id.btn_save);
//        btnBack = findViewById(R.id.btn_back);
//        btnLogout = findViewById(R.id.btn_logout);
//
//        // 为所有难度按钮设置点击监听
//        btnNormal.setOnClickListener(this);
//        btnMedium.setOnClickListener(this);
//        btnHard.setOnClickListener(this);
//        btnReset.setOnClickListener(this);
//        btnSave.setOnClickListener(this);
//        btnBack.setOnClickListener(v -> finish());
//        btnLogout.setOnClickListener(v -> logout());
//    }
//
//    private void loadSettingData() {
//        Map<String, Object> userStatus = dbHelper.getUserStatus(MyApplication.currentUserId);
//        soundStatus = (int) userStatus.get("sound_status");
//        difficulty = (String) userStatus.get("difficulty");
//
//        // 更新音效开关状态
//        swSound.setChecked(soundStatus == 1);
//
//        // 更新难度显示文本
//        String difficultyText;
//        switch (difficulty) {
//            case Constant.DIFFICULTY_NORMAL:
//                difficultyText = "普通";
//                break;
//            case Constant.DIFFICULTY_MEDIUM:
//                difficultyText = "中等";
//                break;
//            case Constant.DIFFICULTY_HARD:
//                difficultyText = "困难";
//                break;
//            default:
//                difficultyText = "未知";
//        }
//        tvDifficulty.setText("当前难度：" + difficultyText);
//
//        // 根据当前难度更新按钮启用状态（当前选中的难度按钮禁用，其他启用）
//        btnNormal.setEnabled(!difficulty.equals(Constant.DIFFICULTY_NORMAL));
//        btnMedium.setEnabled(!difficulty.equals(Constant.DIFFICULTY_MEDIUM));
//        btnHard.setEnabled(!difficulty.equals(Constant.DIFFICULTY_HARD) &&
//                !difficulty.equals(Constant.DIFFICULTY_HARD)); // 保持困难难度禁用（如果需要）
//    }
//
//    private void saveSettings() {
//        int newSoundStatus = swSound.isChecked() ? 1 : 0;
//
//        // 检查设置是否有变更
//        if (newSoundStatus == soundStatus &&
//                difficulty.equals(dbHelper.getUserStatus(MyApplication.currentUserId).get("difficulty"))) {
//            Toast.makeText(this, "设置未变更", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 保存更新后的设置
//        Map<String, Object> updateData = new HashMap<>();
//        updateData.put("sound_status", newSoundStatus);
//        updateData.put("difficulty", difficulty);
//        dbHelper.updateUserStatus(MyApplication.currentUserId, updateData);
//
//        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
//        finish();
//    }
//
//    private void resetGame() {
//        new AlertDialog.Builder(this)
//                .setTitle("重置游戏")
//                .setMessage("确定要重置游戏吗？所有进度将丢失！")
//                .setPositiveButton("确定", (dialog, which) -> {
//                    dbHelper.resetGame(MyApplication.currentUserId);
//                    Toast.makeText(this, "游戏已重置", Toast.LENGTH_SHORT).show();
//                    finish();
//                })
//                .setNegativeButton("取消", null)
//                .show();
//    }
//
//    private void logout() {
//        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
//        sp.edit().remove("user_id").apply();
//        MyApplication.currentUserId = -1;
//        startActivity(new Intent(this, LoginActivity.class));
//        finish();
//    }
//
//    @Override
//    public void onClick(View v) {
//        int id = v.getId();
//        if (id == R.id.btn_normal) {
//            difficulty = Constant.DIFFICULTY_NORMAL;
//            tvDifficulty.setText("当前难度：普通");
//            // 更新按钮状态：当前选中的按钮禁用，其他启用
//            btnNormal.setEnabled(false);
//            btnMedium.setEnabled(true);
//            btnHard.setEnabled(true);
//        } else if (id == R.id.btn_medium) {
//            difficulty = Constant.DIFFICULTY_MEDIUM;
//            tvDifficulty.setText("当前难度：中等");
//            btnNormal.setEnabled(true);
//            btnMedium.setEnabled(false);
//            btnHard.setEnabled(true);
//        } else if (id == R.id.btn_hard) {
//            // 困难难度如果暂未开放，可添加提示
//            Toast.makeText(this, "困难难度暂未开放", Toast.LENGTH_SHORT).show();
//            return;
//            // 如果开放，取消上面的提示并启用下面的逻辑
//            // difficulty = Constant.DIFFICULTY_HARD;
//            // tvDifficulty.setText("当前难度：困难");
//            // btnNormal.setEnabled(true);
//            // btnMedium.setEnabled(true);
//            // btnHard.setEnabled(false);
//        } else if (id == R.id.btn_reset) {
//            resetGame();
//        } else if (id == R.id.btn_save) {
//            saveSettings();
//        }
//    }
//}