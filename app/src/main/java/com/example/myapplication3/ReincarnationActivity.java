package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication3.R;
import com.example.myapplication3.Constant;

import java.util.Map;

public class ReincarnationActivity extends BaseActivity {
    
    // 不同难度下的轮回天数要求
    private static final int EASY_MIN_DAYS = 5;    // 简单模式至少5天
    private static final int NORMAL_MIN_DAYS = 15;  // 普通模式至少15天
    private static final int HARD_MIN_DAYS = 30;    // 困难模式至少30天

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reincarnation);

        TextView tvDesc = findViewById(R.id.tv_desc);
        Button btnReincarnate = findViewById(R.id.btn_reincarnate);
        Button btnBack = findViewById(R.id.btn_back);

        // 更新轮回说明文本
        tvDesc.setText("轮回将完全重置所有游戏进度，包括：\n" +
                "- 背包内所有物品\n" +
                "- 已建造的所有建筑（包括传送门）\n" +
                "- 地图探索进度与当前位置\n" +
                "- 所有生存状态与资源\n\n" +
                "轮回后才可使用个人仓库。");

        btnBack.setOnClickListener(v -> finish());
        btnReincarnate.setOnClickListener(v -> {
            // 检查是否符合轮回条件
            if (checkReincarnationCondition()) {
                showConfirmDialog();
            }
        });
    }

    /**
     * 检查是否符合轮回条件
     */
    private boolean checkReincarnationCondition() {
        // 获取当前游戏天数
        int currentDay = getCurrentDay();
        // 获取当前游戏难度
        String currentDifficulty = getCurrentDifficulty();
        
        // 根据难度获取最小天数要求
        int minDaysRequired = getMinDaysRequired(currentDifficulty);
        
        // 检查天数是否满足要求
        if (currentDay < minDaysRequired) {
            int daysLeft = minDaysRequired - currentDay;
            String difficultyName = getDifficultyDisplayName(currentDifficulty);
            
            new AlertDialog.Builder(this)
                .setTitle("轮回条件未满足")
                .setMessage(String.format("当前为%s模式，需要至少%d天才能轮回。\n您还需要%d天才能进行轮回。", 
                    difficultyName, minDaysRequired, daysLeft))
                .setPositiveButton("确定", null)
                .show();
            
            return false;
        }
        
        return true;
    }

    /**
     * 获取当前游戏天数
     */
    private int getCurrentDay() {
        Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
        return getIntValue(userStatus.get("game_day"), Constant.GAME_DAY_DEFAULT);
    }

    /**
     * 获取当前游戏难度
     */
    private String getCurrentDifficulty() {
        Map<String, Object> userStatus = dbHelper.getUserStatus(userId);
        String difficulty = (String) userStatus.get("difficulty");
        return difficulty != null ? difficulty : Constant.DIFFICULTY_NORMAL;
    }

    /**
     * 根据难度获取最小天数要求
     */
    private int getMinDaysRequired(String difficulty) {
        switch (difficulty) {
            case Constant.DIFFICULTY_EASY:
                return EASY_MIN_DAYS;
            case Constant.DIFFICULTY_NORMAL:
                return NORMAL_MIN_DAYS;
            case Constant.DIFFICULTY_HARD:
                return HARD_MIN_DAYS;
            default:
                return NORMAL_MIN_DAYS; // 默认使用普通模式要求
        }
    }

    /**
     * 获取难度显示名称
     */
    private String getDifficultyDisplayName(String difficulty) {
        switch (difficulty) {
            case Constant.DIFFICULTY_EASY:
                return "简单模式";
            case Constant.DIFFICULTY_NORMAL:
                return "普通模式";
            case Constant.DIFFICULTY_HARD:
                return "困难模式";
            default:
                return "普通模式";
        }
    }

    /**
     * 安全获取整数值的方法
     */
    private int getIntValue(Object value, int defaultValue) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认轮回")
                .setMessage("即将进入轮回，是否开始？")
                .setPositiveButton("确认", (dialog, which) -> reincarnate())
                .setNegativeButton("取消", null)
                .show();
    }

    private void reincarnate() {
        // 1. 执行完全重置（包括背包、建筑、地图等所有数据）
        dbHelper.resetGame(userId);

        // 2. 新增：如果是测试账号，重新初始化其特权数据
        if (dbHelper.isTestAccount(userId)) {
            dbHelper.reinitTestAccountData(userId);
        }

        // 3. 更新轮回次数统计
        dbHelper.incrementReincarnationTimes(userId);
        
        // 4. 计算并更新所有成就进度（在轮回后）
        AchievementManager achievementManager = AchievementManager.getInstance(dbHelper);
        achievementManager.calculateAchievementProgressAfterReincarnation(userId);

        // 4. 设置游戏结束状态
        GameStateManager gameStateManager = GameStateManager.getInstance(this);
        gameStateManager.setGameEnded();
        
        android.util.Log.i("GameState", "ReincarnationActivity - 轮回完成，设置游戏结束状态");

        // 5. 提示信息及页面跳转（轮回后返回标题页）
        Toast.makeText(this,
                "轮回成功！请前往成就系统领取希望点数奖励",
                Toast.LENGTH_LONG).show();
        dbHelper.clearAllSaveSlots(userId);

        // 轮回后返回标题页，而不是直接进入游戏
        Intent intent = new Intent(this, TitleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}