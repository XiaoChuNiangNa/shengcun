package com.example.myapplication3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class TitleActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnStartGame;
    private Button btnDifficulty;
    private Button btnTechTree;  // 科技树按钮
    private Button btnBase;  // 基地按钮
    private Button btnIllustration;  // 图鉴按钮
    private Button btnAchievement;  // 成就按钮
    private ImageButton btnSettings;  // 设置按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 添加状态检测日志
        GameStateManager gameStateManager = GameStateManager.getInstance(this);
        boolean isGameStarted = gameStateManager.isGameStarted();
        int currentUserId = gameStateManager.getCurrentUserId();
        
        android.util.Log.i("GameState", "TitleActivity - 游戏状态检测:");
        android.util.Log.i("GameState", "  isGameStarted: " + isGameStarted);
        android.util.Log.i("GameState", "  currentUserId: " + currentUserId);
        android.util.Log.i("GameState", "  MyApplication.currentUserId: " + MyApplication.currentUserId);
        
        // 修复：简化状态检测逻辑，确保一致性
        // 只有游戏已开始且未结束时才直接进入游戏
        if (isGameStarted && !gameStateManager.isGameEnded()) {
            // 确保GameStateManager中的用户ID与MyApplication保持一致
            if (currentUserId != MyApplication.currentUserId && MyApplication.currentUserId != -1) {
                // 如果不一致，同步GameStateManager中的用户ID
                gameStateManager.setCurrentUserId(MyApplication.currentUserId);
                android.util.Log.i("GameState", "用户ID不一致，已同步修复");
            }
            
            android.util.Log.i("GameState", "检测到游戏已开始且未结束，直接进入游戏");
            startActivity(new Intent(TitleActivity.this, MainActivity.class));
            finish();
            return;
        } else {
            android.util.Log.i("GameState", "游戏未开始或已结束，显示标题页");
        }
        
        setContentView(R.layout.activity_title);

        initView();
        initClickListener();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 原onBackPressed中的逻辑
                finishAffinity();
                //System.exit(0);
            }
        });
    }

    private void initView() {
        btnStartGame = findViewById(R.id.btn_start_game);
        btnTechTree = findViewById(R.id.btn_tech_tree);  // 初始化科技树按钮
        btnBase = findViewById(R.id.btn_base);  // 初始化基地按钮
        btnIllustration = findViewById(R.id.btn_illustration);  // 初始化图鉴按钮
        btnAchievement = findViewById(R.id.btn_achievement);  // 初始化成就按钮
        btnSettings = findViewById(R.id.btn_settings);  // 初始化设置按钮
        // 难度设置按钮已从布局中移除，改为弹窗形式
    }

    private void initClickListener() {
        btnStartGame.setOnClickListener(this);
        btnTechTree.setOnClickListener(this);  // 设置科技树按钮点击监听
        btnBase.setOnClickListener(this);  // 设置基地按钮点击监听
        btnIllustration.setOnClickListener(this);  // 设置图鉴按钮点击监听
        btnAchievement.setOnClickListener(this);  // 设置成就按钮点击监听
        btnSettings.setOnClickListener(this);  // 设置按钮点击监听
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start_game) {
            // 开始游戏逻辑：检查登录状态
            int currentUserId = MyApplication.currentUserId;
            if (currentUserId == -1) {
                SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
                currentUserId = sp.getInt("user_id", -1);
            }

            if (currentUserId != -1) {
                MyApplication.currentUserId = currentUserId;
                
                // 关键修复：同步设置GameStateManager中的用户ID
                GameStateManager gameStateManager = GameStateManager.getInstance(this);
                gameStateManager.setCurrentUserId(currentUserId);
                
                // 检查是否是首次游戏（简单模式未通关）
                DBHelper dbHelper = DBHelper.getInstance(this);
                boolean isFirstGame = !dbHelper.isEasyDifficultyCleared(currentUserId);
                
                if (isFirstGame) {
                    // 首次游戏：自动进入简单模式，不显示难度选择弹窗
                    startGameWithDifficulty("简单");
                } else {
                    // 非首次游戏：显示难度选择弹窗
                    showDifficultyDialog();
                }
            } else {
                startActivity(new Intent(TitleActivity.this, LoginActivity.class));
            }
        } else if (v.getId() == R.id.btn_tech_tree) {
            // 科技树按钮点击事件：检查登录状态
            int currentUserId = MyApplication.currentUserId;
            if (currentUserId == -1) {
                SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
                currentUserId = sp.getInt("user_id", -1);
            }

            if (currentUserId != -1) {
                MyApplication.currentUserId = currentUserId;
                startActivity(new Intent(TitleActivity.this, TechTreeActivity.class));
            } else {
                Toast.makeText(this, "请先登录再查看科技树", Toast.LENGTH_LONG).show();
                startActivity(new Intent(TitleActivity.this, LoginActivity.class));
            }
        } else if (v.getId() == R.id.btn_base) {
            // 基地按钮点击事件：检查登录状态
            int currentUserId = MyApplication.currentUserId;
            if (currentUserId == -1) {
                SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
                currentUserId = sp.getInt("user_id", -1);
            }

            if (currentUserId != -1) {
                MyApplication.currentUserId = currentUserId;
                startActivity(new Intent(TitleActivity.this, BaseActivity.class));
            } else {
                Toast.makeText(this, "请先登录再进入基地", Toast.LENGTH_LONG).show();
                startActivity(new Intent(TitleActivity.this, LoginActivity.class));
            }
        } else if (v.getId() == R.id.btn_illustration) {
            // 图鉴按钮点击事件：检查登录状态
            int currentUserId = MyApplication.currentUserId;
            if (currentUserId == -1) {
                SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
                currentUserId = sp.getInt("user_id", -1);
            }

            if (currentUserId != -1) {
                MyApplication.currentUserId = currentUserId;
                // 跳转到选择图鉴页面
                startActivity(new Intent(TitleActivity.this, IllustrationSelectionActivity.class));
            } else {
                Toast.makeText(this, "请先登录再查看图鉴", Toast.LENGTH_LONG).show();
                startActivity(new Intent(TitleActivity.this, LoginActivity.class));
            }
        } else if (v.getId() == R.id.btn_achievement) {
            // 成就按钮点击事件：检查登录状态
            int currentUserId = MyApplication.currentUserId;
            if (currentUserId == -1) {
                SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
                currentUserId = sp.getInt("user_id", -1);
            }

            if (currentUserId != -1) {
                MyApplication.currentUserId = currentUserId;
                // 跳转到成就页面
                startActivity(new Intent(TitleActivity.this, AchievementsActivity.class));
            } else {
                Toast.makeText(this, "请先登录再查看成就", Toast.LENGTH_LONG).show();
                startActivity(new Intent(TitleActivity.this, LoginActivity.class));
            }
        } else if (v.getId() == R.id.btn_settings) {
            // 设置按钮点击事件：显示设置对话框
            showSettingsDialog();
        }
    }

    /**
     * 显示难度设置弹窗
     */
    private void showDifficultyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_difficulty, null);
        builder.setView(dialogView);

        // 获取弹窗中的控件
        final RadioButton rbEasy = dialogView.findViewById(R.id.rb_easy);
        final RadioButton rbNormal = dialogView.findViewById(R.id.rb_normal);
        final RadioButton rbHard = dialogView.findViewById(R.id.rb_hard);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // 检查用户通关状态，解锁相应难度
        DBHelper dbHelper = DBHelper.getInstance(this);
        int currentUserId = MyApplication.currentUserId;
        boolean isEasyCleared = dbHelper.isEasyDifficultyCleared(currentUserId);
        boolean isNormalCleared = dbHelper.isNormalDifficultyCleared(currentUserId);

        // 设置难度选项的可用性
        // 简单难度始终可用
        rbEasy.setEnabled(true);
        rbEasy.setText("简单");
        
        // 普通难度：需要简单模式通关后解锁
        if (isEasyCleared) {
            rbNormal.setEnabled(true);
            rbNormal.setText("普通");
        } else {
            rbNormal.setEnabled(false);
            rbNormal.setText("普通\n (未解锁)");
        }
        
        // 困难难度：需要普通模式通关后解锁
        if (isNormalCleared) {
            rbHard.setEnabled(true);
            rbHard.setText("困难");
        } else {
            rbHard.setEnabled(false);
            rbHard.setText("困难\n (未解锁)");
        }

        // 设置默认选中普通难度，与默认值保持一致
        rbNormal.setChecked(true);

        // 设置单选按钮的互斥逻辑（只允许选择已启用的选项）
        View.OnClickListener radioClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果按钮不可用，则不执行任何操作
                if (!((RadioButton) v).isEnabled()) {
                    return;
                }
                
                // 取消所有选中状态
                rbEasy.setChecked(false);
                rbNormal.setChecked(false);
                rbHard.setChecked(false);
                
                // 设置当前点击的按钮为选中状态
                ((RadioButton) v).setChecked(true);
            }
        };
        
        rbEasy.setOnClickListener(radioClickListener);
        rbNormal.setOnClickListener(radioClickListener);
        rbHard.setOnClickListener(radioClickListener);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // 取消按钮点击事件
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 确认按钮点击事件
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 默认值设为普通难度，与默认选中的rbNormal按钮保持一致
                String difficulty = Constant.DIFFICULTY_NORMAL;
                if (rbEasy.isChecked()) {
                    difficulty = Constant.DIFFICULTY_EASY;
                } else if (rbNormal.isChecked()) {
                    difficulty = Constant.DIFFICULTY_NORMAL;
                } else if (rbHard.isChecked()) {
                    difficulty = Constant.DIFFICULTY_HARD;
                }

                // 添加日志显示选择的难度
                android.util.Log.d("DifficultySelect", "用户选择的难度: " + difficulty);
                
                // 保存难度设置到SharedPreferences
                SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("difficulty", difficulty);
                editor.apply();

                // 设置游戏已开始状态
                GameStateManager gameStateManager = GameStateManager.getInstance(TitleActivity.this);
                gameStateManager.setGameStarted(true);

                // 显示友好的难度名称
                String difficultyName = "普通";
                if (Constant.DIFFICULTY_EASY.equals(difficulty)) {
                    difficultyName = "简单";
                } else if (Constant.DIFFICULTY_HARD.equals(difficulty)) {
                    difficultyName = "困难";
                }
                Toast.makeText(TitleActivity.this, "难度设置为：" + difficultyName, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                
                // 开始游戏
                startGameWithDifficulty(difficulty);
            }
        });

        dialog.show();
    }

    /**
     * 使用指定难度开始游戏
     * @param difficulty 已经是英文常量（如"easy", "normal", "hard"）
     */
    private void startGameWithDifficulty(String difficulty) {
        // 修复：传入的difficulty已经是英文常量，直接使用，无需再次转换
        String difficultyConstant = difficulty;
        
        // 如果是中文显示名称，转换为英文常量（兼容性处理）
        if ("简单".equals(difficulty)) {
            difficultyConstant = Constant.DIFFICULTY_EASY;
        } else if ("普通".equals(difficulty)) {
            difficultyConstant = Constant.DIFFICULTY_NORMAL;
        } else if ("困难".equals(difficulty)) {
            difficultyConstant = Constant.DIFFICULTY_HARD;
        } else {
            // 已经是英文常量，直接使用
            difficultyConstant = difficulty;
        }
        
        // 保存难度设置到SharedPreferences
        SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("difficulty", difficultyConstant);
        editor.apply();
        
        Log.d("DifficultySelect", "选择的难度: " + difficulty + " -> 保存的常量: " + difficultyConstant);

        // 设置游戏已开始状态
        GameStateManager gameStateManager = GameStateManager.getInstance(TitleActivity.this);
        gameStateManager.setGameStarted(true);

        // 显示友好的难度名称
        String difficultyDisplayName;
        if (Constant.DIFFICULTY_EASY.equals(difficultyConstant)) {
            difficultyDisplayName = "简单";
        } else if (Constant.DIFFICULTY_HARD.equals(difficultyConstant)) {
            difficultyDisplayName = "困难";
        } else {
            difficultyDisplayName = "普通";
        }
        Toast.makeText(TitleActivity.this, "难度设置为：" + difficultyDisplayName, Toast.LENGTH_SHORT).show();
        
        // 添加难度设置的日志
        Log.d("DifficultySelect", "开始游戏，保存的难度常量: " + difficultyConstant);
        
        // 开始游戏
        startActivity(new Intent(TitleActivity.this, MainActivity.class));
    }

    /**
     * 显示设置对话框
     */
    private void showSettingsDialog() {
        // 使用SettingDialogFragment显示设置对话框
        SettingDialogFragment settingDialog = new SettingDialogFragment();
        settingDialog.show(getSupportFragmentManager(), "settings");
    }

//    @Override
//    public void onBackPressed() {
//        finishAffinity();
//        System.exit(0);
//    }
}