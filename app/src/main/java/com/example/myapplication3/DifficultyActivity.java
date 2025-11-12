package com.example.myapplication3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class DifficultyActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvDifficulty;
    private Button btnEasy, btnNormal, btnHard, btnSave, btnBack;
    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        initView();
        loadDifficultyData();
    }

    private void initView() {
        tvDifficulty = findViewById(R.id.tv_difficulty);
        btnEasy = findViewById(R.id.btn_normal);
        btnNormal = findViewById(R.id.btn_medium);
        btnHard = findViewById(R.id.btn_hard);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);

        btnEasy.setOnClickListener(this);
        btnNormal.setOnClickListener(this);
        btnHard.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnBack.setOnClickListener(v -> finish());

        // 初始化难度解锁检查
        checkDifficultyUnlocks();
    }

    private void loadDifficultyData() {
        // 已通过登录检查，确保 currentUserId 有效
        Map<String, Object> userStatus = dbHelper.getUserStatus(MyApplication.currentUserId);
        difficulty = (String) userStatus.get("difficulty");

        // 设置默认难度为普通
        if (difficulty == null || difficulty.isEmpty()) {
            difficulty = Constant.DIFFICULTY_NORMAL;
        }

        updateDifficultyDisplay();
    }

    private void updateDifficultyDisplay() {
        String displayText = "当前难度：";
        switch (difficulty) {
            case Constant.DIFFICULTY_EASY:
                displayText += "简单";
                btnEasy.setEnabled(false);
                btnNormal.setEnabled(true);
                btnHard.setEnabled(true);
                break;
            case Constant.DIFFICULTY_NORMAL:
                displayText += "普通";
                btnEasy.setEnabled(true);
                btnNormal.setEnabled(false);
                btnHard.setEnabled(true);
                break;
            case Constant.DIFFICULTY_HARD:
                displayText += "困难";
                btnEasy.setEnabled(true);
                btnNormal.setEnabled(true);
                btnHard.setEnabled(false);
                break;
            default:
                displayText += "未知";
                break;
        }
        tvDifficulty.setText(displayText);
    }

    private void saveDifficulty() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("difficulty", difficulty);
        dbHelper.updateUserStatus(MyApplication.currentUserId, updateData);

        // 提示难度变化影响
        if (difficulty.equals(Constant.DIFFICULTY_HARD)) {
            Toast.makeText(this, "困难难度已保存，游戏难度大幅提升", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "难度已保存", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_normal) {
            difficulty = Constant.DIFFICULTY_EASY; // 简单模式
            updateDifficultyDisplay();
        } else if (id == R.id.btn_medium) {
            difficulty = Constant.DIFFICULTY_NORMAL; // 普通模式
            updateDifficultyDisplay();
        } else if (id == R.id.btn_hard) {
            difficulty = Constant.DIFFICULTY_HARD; // 困难模式
            updateDifficultyDisplay();
        } else if (id == R.id.btn_save) {
            saveDifficulty();
        }
    }

    // 检查难度解锁状态
    private void checkDifficultyUnlocks() {
        // 确保userId有效（继承自BaseActivity，需先初始化）
        if (userId <= 0) {
            btnNormal.setEnabled(false);
            btnNormal.setAlpha(0.5f);
            btnHard.setEnabled(false);
            btnHard.setAlpha(0.5f);
            btnNormal.setOnClickListener(v -> showToast("用户未登录"));
            btnHard.setOnClickListener(v -> showToast("用户未登录"));
            return;
        }

        // 检查普通难度解锁：需要通关简单难度
        boolean isEasyCleared = dbHelper.isEasyDifficultyCleared(userId);
        if (!isEasyCleared) {
            btnNormal.setEnabled(false);
            btnNormal.setAlpha(0.5f);
            btnNormal.setOnClickListener(v ->
                    showToast("需先通关简单难度才能解锁普通难度")
            );
        }

        // 检查困难难度解锁：需要通关普通难度
        boolean isNormalCleared = dbHelper.isNormalDifficultyCleared(userId);
        if (!isNormalCleared) {
            btnHard.setEnabled(false);
            btnHard.setAlpha(0.5f);
            btnHard.setOnClickListener(v ->
                    showToast("需先通关普通难度才能解锁困难难度")
            );
        }
    }
}