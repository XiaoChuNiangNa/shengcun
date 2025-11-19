package com.example.myapplication3;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private LinearLayout achievementsContainer;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        dbHelper = DBHelper.getInstance(this);
        achievementsContainer = findViewById(R.id.achievements_container);

        // 初始化返回按钮
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 加载成就数据
        loadAchievements();
    }

    private void loadAchievements() {
        int userId = MyApplication.currentUserId;
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化用户成就数据（如果不存在）
        dbHelper.initUserAchievements(userId);

        // 获取用户所有成就
        List<AchievementItem> achievements = dbHelper.getUserAchievements(userId);

        // 添加调试日志：显示每个成就的数值
        Log.d("AchievementsActivity", "=== 成就数据调试信息 ===");
        Log.d("AchievementsActivity", "用户ID: " + userId + ", 成就总数: " + achievements.size());
        
        for (AchievementItem achievement : achievements) {
            int target = getAchievementTarget(achievement.getCategory(), achievement.getLevel());
            Log.d("AchievementsActivity", 
                "成就类型: " + achievement.getCategory() + 
                " Lv" + achievement.getLevel() + 
                " | 当前进度: " + achievement.getCurrent() + 
                " | 目标值: " + target +
                " | 完成状态: " + achievement.isCompleted() + 
                " | 领取状态: " + achievement.isClaimed());
        }
        Log.d("AchievementsActivity", "=== 调试信息结束 ===");

        // 清空容器
        achievementsContainer.removeAllViews();

        // 按成就类型分组显示
        displayAchievementGroup("资源收集", achievements, "resource_collect");
        displayAchievementGroup("寻宝探险", achievements, "exploration");
        displayAchievementGroup("合成物品", achievements, "synthesis");
        displayAchievementGroup("解锁建筑", achievements, "building");
        displayAchievementGroup("烹饪料理", achievements, "cooking");
        displayAchievementGroup("熔炼物品", achievements, "smelting");
        displayAchievementGroup("贸易大师", achievements, "trading");
        displayAchievementGroup("轮回之路", achievements, "reincarnation");
        displayAchievementGroup("生存之路", achievements, "survival");
    }

    private void displayAchievementGroup(String groupTitle, List<AchievementItem> achievements, String achievementType) {
        // 创建分组标题
        TextView groupTitleView = new TextView(this);
        groupTitleView.setText(groupTitle);
        groupTitleView.setTextColor(getResources().getColor(R.color.text_primary));
        groupTitleView.setTextSize(18);
        groupTitleView.setPadding(0, 16, 0, 16);
        groupTitleView.setTypeface(groupTitleView.getTypeface(), android.graphics.Typeface.BOLD);
        achievementsContainer.addView(groupTitleView);

        // 过滤该类型的成就并按等级排序
        List<AchievementItem> filteredAchievements = new ArrayList<>();
        for (AchievementItem achievement : achievements) {
            if (achievement.getCategory().equals(achievementType)) {
                filteredAchievements.add(achievement);
            }
        }

        // 按等级排序
        filteredAchievements.sort((a1, a2) -> Integer.compare(a1.getLevel(), a2.getLevel()));

        if (!filteredAchievements.isEmpty()) {
            // 只显示用户当前应该看到的成就等级
            AchievementItem nextAchievement = findNextAchievementToShow(filteredAchievements);
            if (nextAchievement != null) {
                addAchievementItem(nextAchievement, filteredAchievements);
            }
        } else {
            // 如果没有该类型的成就，显示提示信息
            TextView emptyText = new TextView(this);
            emptyText.setText("暂无相关成就数据");
            emptyText.setTextColor(getResources().getColor(R.color.text_secondary));
            emptyText.setTextSize(14);
            emptyText.setPadding(0, 8, 0, 16);
            achievementsContainer.addView(emptyText);
        }
    }
    
    private AchievementItem findNextAchievementToShow(List<AchievementItem> achievements) {
        if (achievements.isEmpty()) {
            return null;
        }
        
        // 找出用户应该看到的下一个成就
        // 逻辑：
        // 1. 从等级1开始检查
        // 2. 如果找到一个未完成的成就，直接返回
        // 3. 如果当前等级已完成且已领取，继续查找下一个等级
        // 4. 如果当前等级已完成但未领取，则只显示这个成就（不显示下一个）
        
        for (int i = 0; i < achievements.size(); i++) {
            AchievementItem current = achievements.get(i);
            
            // 如果当前成就未完成，显示它
            if (!current.isCompleted()) {
                return current;
            } 
            // 如果当前成就是最后一个，直接返回
            else if (i == achievements.size() - 1) {
                return current;
            } 
            // 如果当前成就已完成但未领取，只显示这个成就
            else if (current.isCompleted() && !current.isClaimed()) {
                return current;
            } 
            // 如果当前成就已完成且已领取，继续查找下一个等级
        }
        
        // 如果所有成就都已完成且已领取，显示最高等级成就
        return achievements.get(achievements.size() - 1);
    }

    private AchievementItem getNextAchievementToComplete(List<AchievementItem> achievements) {
        if (achievements.isEmpty()) {
            return null;
        }

        // 找到第一个未完成的成就
        for (AchievementItem achievement : achievements) {
            if (!achievement.isCompleted()) {
                return achievement;
            }
        }

        // 如果所有成就都已完成，显示最高等级的成就
        return achievements.get(achievements.size() - 1);
    }

    private void addAchievementItem(AchievementItem achievement, List<AchievementItem> allAchievements) {
        // 加载成就条目布局
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_achievement, achievementsContainer, false);

        // 获取视图组件
        ImageView ivIcon = itemView.findViewById(R.id.iv_icon);
        TextView tvTitle = itemView.findViewById(R.id.tv_title);
        ProgressBar progressBar = itemView.findViewById(R.id.progress_bar);
        TextView tvProgress = itemView.findViewById(R.id.tv_progress);
        Button btnClaim = itemView.findViewById(R.id.btn_claim);

        // 设置成就标题 - 只显示名称和等级
        String title = getAchievementTitle(achievement.getCategory(), achievement.getLevel());
        tvTitle.setText(title);

        // 获取目标值
        int target = getAchievementTarget(achievement.getCategory(), achievement.getLevel());
        int progress = achievement.getCurrent();

        // 设置进度条
        progressBar.setMax(target);
        progressBar.setProgress(progress);

        // 设置进度文本
        tvProgress.setText(progress + "/" + target);

        // 设置领取按钮状态
        if (achievement.isCompleted() && !achievement.isClaimed()) {
            // 可领取状态
            btnClaim.setEnabled(true);
            btnClaim.setBackgroundResource(R.drawable.button_green);
            btnClaim.setTextColor(getResources().getColor(R.color.white));
            btnClaim.setOnClickListener(v -> claimAchievement(achievement));
        } else if (achievement.isClaimed()) {
            // 已领取状态
            btnClaim.setEnabled(false);
            btnClaim.setText("已领取");
            btnClaim.setBackgroundResource(R.drawable.button_gray);
            btnClaim.setTextColor(getResources().getColor(R.color.text_secondary));
        } else {
            // 不可领取状态
            btnClaim.setEnabled(false);
            btnClaim.setBackgroundResource(R.drawable.button_gray);
            btnClaim.setTextColor(getResources().getColor(R.color.text_secondary));
        }

        // 添加成就条目到容器
        achievementsContainer.addView(itemView);

        // 显示成就数值和进度条日志
        logAchievementProgress(achievement, target);
    }

    private AchievementItem getNextLevelAchievement(AchievementItem currentAchievement, List<AchievementItem> allAchievements) {
        int nextLevel = currentAchievement.getLevel() + 1;
        for (AchievementItem achievement : allAchievements) {
            if (achievement.getCategory().equals(currentAchievement.getCategory()) && 
                achievement.getLevel() == nextLevel) {
                return achievement;
            }
        }
        return null;
    }

    private String getAchievementTitleWithNextLevel(AchievementItem currentAchievement, AchievementItem nextAchievement) {
        String baseTitle = getAchievementTitle(currentAchievement.getCategory(), currentAchievement.getLevel());
        
        if (nextAchievement != null) {
            return baseTitle + " → Lv" + nextAchievement.getLevel() + " (" + nextAchievement.getTarget() + ")";
        } else {
            return baseTitle + " (最高等级)";
        }
    }

    private String getAchievementTitle(String achievementType, int level) {
        String baseTitle = "";
        String levelText = "Lv" + level;
        
        switch (achievementType) {
            case "resource_collect":
                baseTitle = "资源收集";
                break;
            case "exploration":
                baseTitle = "寻宝探险";
                break;
            case "synthesis":
                baseTitle = "合成物品";
                break;
            case "building":
                baseTitle = "解锁建筑";
                break;
            case "cooking":
                baseTitle = "烹饪料理";
                break;
            case "smelting":
                baseTitle = "熔炼物品";
                break;
            case "trading":
                baseTitle = "贸易大师";
                break;
            case "reincarnation":
                baseTitle = "轮回之路";
                break;
            case "survival":
                baseTitle = "生存之路";
                break;
        }
        
        return baseTitle + " " + levelText;
    }

    private int getAchievementTarget(String achievementType, int level) {
        switch (achievementType) {
            case "resource_collect":
                return 100 * level; // Lv1:100, Lv2:200, ..., Lv5:500
            case "exploration":
            case "synthesis":
            case "smelting":
            case "trading":
            case "reincarnation":
                switch (level) {
                    case 1: return 100;
                    case 2: return 500;
                    case 3: return 1000;
                    case 4: return 2000;
                    case 5: return 5000;
                    case 6: return 10000;
                    case 7: return 20000;
                    case 8: return 50000;
                    case 9: return 100000;
                    case 10: return 200000;
                    default: return 0;
                }
            case "building":
                return 1; // 每个建筑只需要解锁一次
            case "cooking":
                return level * 5; // Lv1:5, Lv2:10, ..., Lv5:25
            case "survival":
                switch (level) {
                    case 1: return 1;  // Lv1: 首次简单模式成功轮回
                    case 2: return 1;  // Lv2: 首次普通模式成功轮回
                    case 3: return 1;  // Lv3: 首次困难模式成功轮回
                    case 4: return 100; // Lv4: 简单模式最高生存100天
                    case 5: return 100; // Lv5: 普通模式最高生存100天
                    case 6: return 100; // Lv6: 困难模式最高生存100天
                    case 7: return 500; // Lv7: 简单模式最高生存500天
                    case 8: return 500; // Lv8: 普通模式最高生存500天
                    case 9: return 500; // Lv9: 困难模式最高生存500天
                    case 10: return 1000; // Lv10: 困难模式最高生存1000天
                    default: return 0;
                }
            default:
                return 0;
        }
    }

    private void claimAchievement(AchievementItem achievement) {
        int userId = MyApplication.currentUserId;
        boolean success = dbHelper.claimAchievementReward(userId, achievement.getCategory(), achievement.getLevel());
        
        if (success) {
            Toast.makeText(this, "成功领取 " + getAchievementReward(achievement.getLevel()) + " 点希望点数", Toast.LENGTH_SHORT).show();
            // 重新加载成就列表
            loadAchievements();
        } else {
            Toast.makeText(this, "领取失败", Toast.LENGTH_SHORT).show();
        }
    }

    private int getAchievementReward(int level) {
        return level * 10; // Lv1:10, Lv2:20, ..., Lv10:100
    }

    // 显示成就数值和进度条日志
    private void logAchievementProgress(AchievementItem achievement, int target) {
        String categoryName = getCategoryDisplayName(achievement.getCategory());
        int progress = achievement.getCurrent();
        int progressPercent = (int) ((float) progress / target * 100);
        
        String logMessage = String.format("成就页面显示 - 分类: %s, 等级: Lv%d, 进度: %d/%d (%d%%), 完成状态: %s, 领取状态: %s",
                categoryName, achievement.getLevel(), progress, target, progressPercent,
                achievement.isCompleted() ? "已完成" : "未完成",
                achievement.isClaimed() ? "已领取" : "未领取");
        
        // 输出到Logcat
        android.util.Log.d("AchievementsActivity", logMessage);
        
        // 可选：在界面上显示Toast提示（如果需要更直观的显示）
        // Toast.makeText(this, logMessage, Toast.LENGTH_SHORT).show();
    }

    // 获取分类显示名称
    private String getCategoryDisplayName(String category) {
        switch (category) {
            case "resource_collect":
                return "资源收集";
            case "exploration":
                return "寻宝探险";
            case "synthesis":
                return "合成物品";
            case "building":
                return "解锁建筑";
            case "cooking":
                return "烹饪料理";
            case "smelting":
                return "熔炼物品";
            case "trading":
                return "贸易大师";
            case "reincarnation":
                return "轮回之路";
            case "survival":
                return "生存之路";
            default:
                return category;
        }
    }
}