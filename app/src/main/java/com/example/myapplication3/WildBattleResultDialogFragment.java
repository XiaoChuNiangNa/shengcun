package com.example.myapplication3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.lang.ref.WeakReference;

/**
 * 野外对战结算弹窗
 * 显示对战成功后的奖励信息
 */
public class WildBattleResultDialogFragment extends DialogFragment {
    private static final String TAG = "WildBattleResultDialog";
    private static final String ARG_IS_VICTORY = "is_victory";
    private static final String ARG_ANIMAL_NAME = "animal_name";
    private static final String ARG_TERRAIN_TYPE = "terrain_type";
    private static final String ARG_ANIMAL_SIZE = "animal_size"; // 新增：统一参数
    private static final String ARG_ORIGINAL_X = "original_x";
    private static final String ARG_ORIGINAL_Y = "original_y";

    // 全局静态 Random 实例（解决随机数不均匀问题）
    private static final Random RANDOM = new Random();

    private boolean isVictory;
    private String animalName;
    private String terrainType;
    private String animalSize;
    private int originalX, originalY;
    private WeakReference<BattleActivity> activityWeakRef; // 弱引用：避免内存泄漏
    private WeakReference<OnResultDialogListener> listenerWeakRef; // 弱引用：避免内存泄漏
    private LootBox droppedLootBox;

    // 存储奖励数量（用于显示和实际添加一致）
    private int meatCount;
    private int leatherCount;
    private int woolCount;
    private int fishCount;
    private int boneCount;
    private int expReward;

    public interface OnResultDialogListener {
        void onReturnToGame();
        void onGameOver();
    }

    // 统一 newInstance 方法（避免重载导致的 animalSize 空指针）
    public static WildBattleResultDialogFragment newInstance(boolean isVictory, String animalName,
                                                             String terrainType, String animalSize,
                                                             int originalX, int originalY) {
        WildBattleResultDialogFragment fragment = new WildBattleResultDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_VICTORY, isVictory);
        args.putString(ARG_ANIMAL_NAME, animalName);
        args.putString(ARG_TERRAIN_TYPE, terrainType);
        args.putString(ARG_ANIMAL_SIZE, animalSize); // 必传参数，避免 null
        args.putInt(ARG_ORIGINAL_X, originalX);
        args.putInt(ARG_ORIGINAL_Y, originalY);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 从 Context 中获取 BattleActivity（避免直接传入 Activity）
        if (context instanceof BattleActivity) {
            activityWeakRef = new WeakReference<>((BattleActivity) context);
        }
        // 绑定 Listener（若 Activity 实现了接口）
        if (context instanceof OnResultDialogListener) {
            listenerWeakRef = new WeakReference<>((OnResultDialogListener) context);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        parseArguments(); // 解析参数（单独提取方法，优化可读性）

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_wild_battle_result, null);

        // 初始化视图
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvResult = view.findViewById(R.id.tv_result);
        TextView tvLevelInfo = view.findViewById(R.id.tv_level_info);
        TextView tvRewards = view.findViewById(R.id.tv_rewards);
        TextView tvLog = view.findViewById(R.id.tv_log);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        // 设置标题和结果
        setTitleAndResult(tvTitle, tvResult);

        // 获取等级信息
        LevelExperienceManager levelExpManager = new LevelExperienceManager(requireActivity());
        tvLevelInfo.setText(levelExpManager.getLevelInfo());

        // 计算战利品箱（胜利时）
        calculateLootBox();

        // 计算奖励（统一生成数量，用于显示和实际添加）
        List<String> rewards = calculateRewards();
        tvRewards.setText(buildRewardsText(rewards));

        // 生成并保存战斗日志
        String logText = generateBattleLog(levelExpManager);
        tvLog.setText(logText);

        // 确认按钮点击事件
        btnConfirm.setOnClickListener(v -> {
            handleDialogConfirm(levelExpManager);
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    /**
     * 解析参数（单独提取，优化代码结构）
     */
    private void parseArguments() {
        Bundle args = getArguments();
        if (args == null) {
            dismiss(); // 参数为空，关闭弹窗
            return;
        }
        isVictory = args.getBoolean(ARG_IS_VICTORY);
        animalName = args.getString(ARG_ANIMAL_NAME, "未知生物");
        terrainType = args.getString(ARG_TERRAIN_TYPE, "未知地形");
        animalSize = args.getString(ARG_ANIMAL_SIZE, "普通"); // 默认值，避免 null
        originalX = args.getInt(ARG_ORIGINAL_X, 0);
        originalY = args.getInt(ARG_ORIGINAL_Y, 0);
    }

    /**
     * 设置标题和战斗结果
     */
    private void setTitleAndResult(TextView tvTitle, TextView tvResult) {
        if (isVictory) {
            tvTitle.setText("战斗胜利！");
            tvResult.setText("成功击败了" + animalName + "！");
        } else {
            tvTitle.setText("战斗失败");
            tvResult.setText("被" + animalName + "击败了...");
        }
    }

    /**
     * 计算战利品箱（胜利时）
     */
    private void calculateLootBox() {
        if (isVictory) {
            String difficulty = getCurrentDifficulty();
            LootBoxDropManager dropManager = LootBoxDropManager.getInstance();
            droppedLootBox = dropManager.calculateLootBoxDrop(animalName, animalSize, terrainType, difficulty);
        }
    }

    /**
     * 计算奖励（统一生成数量，确保显示与实际一致）
     */
    private List<String> calculateRewards() {
        List<String> rewards = new ArrayList<>();
        if (isVictory) {
            // 1. 生成经验奖励（仅一次）
            expReward = 50 + RANDOM.nextInt(50);
            rewards.add("✓ 经验 +" + expReward);

            // 2. 生成物资奖励（仅一次，存储到成员变量）
            calculateMaterialRewards();

            // 3. 添加物资奖励到显示列表
            if (meatCount > 0) rewards.add("✓ 获得 肉 x" + meatCount);
            if (leatherCount > 0) rewards.add("✓ 获得 皮革 x" + leatherCount);
            if (woolCount > 0) rewards.add("✓ 获得 羊毛 x" + woolCount);
            if (fishCount > 0) rewards.add("✓ 获得 鱼 x" + fishCount);
            if (boneCount > 0) rewards.add("✓ 获得 兽骨 x" + boneCount);
        } else {
            // 失败惩罚
            rewards.add("✗ 体力消耗过多");
            rewards.add("✗ 部分物品丢失");
            rewards.add("✗ 需要重新挑战");
        }
        return rewards;
    }

    /**
     * 计算物资奖励数量（统一逻辑，存储到成员变量）
     */
    private void calculateMaterialRewards() {
        switch (animalName) {
            case "野兔":
            case "野鸡":
            case "小猪":
            case "蛇":
                meatCount = 1 + RANDOM.nextInt(3);
                leatherCount = 1 + RANDOM.nextInt(2);
                break;
            case "山羊":
                meatCount = 1 + RANDOM.nextInt(3);
                leatherCount = 1 + RANDOM.nextInt(2);
                woolCount = 1 + RANDOM.nextInt(2);
                break;
            case "食人鱼":
                fishCount = 1 + RANDOM.nextInt(3);
                break;
            case "狼":
            case "鹿":
            case "野猪":
            case "猴子":
                meatCount = 2 + RANDOM.nextInt(3);
                leatherCount = 1 + RANDOM.nextInt(4);
                boneCount = 1 + RANDOM.nextInt(2);
                break;
            case "老虎":
            case "狮子":
            case "熊":
            case "猎豹":
            case "鲨鱼":
                meatCount = 3 + RANDOM.nextInt(3);
                leatherCount = 2 + RANDOM.nextInt(4);
                boneCount = 1 + RANDOM.nextInt(4);
                break;
            default:
                meatCount = 2 + RANDOM.nextInt(3);
                leatherCount = 1;
        }
    }

    /**
     * 构建奖励文本（包含战利品箱）
     */
    private String buildRewardsText(List<String> rewards) {
        StringBuilder rewardsText = new StringBuilder();
        for (String reward : rewards) {
            rewardsText.append(reward).append("\n");
        }
        // 添加战利品箱信息
        if (droppedLootBox != null) {
            rewardsText.append("🎁 获得 ").append(droppedLootBox.getName())
                    .append(" (").append(droppedLootBox.getRarity().getDisplayName()).append(")");
        }
        return rewardsText.toString();
    }

    /**
     * 处理弹窗确认（胜利/失败逻辑）
     */
    private void handleDialogConfirm(LevelExperienceManager levelExpManager) {
        if (isVictory) {
            handleVictory(levelExpManager);
        } else {
            handleDefeat();
        }
    }

    /**
     * 处理胜利结算（更新经验+添加奖励+返回结果）
     */
    private void handleVictory(LevelExperienceManager levelExpManager) {
        BattleActivity activity = activityWeakRef.get();
        if (activity == null) return;

        // 1. 实际增加经验（修复：日志显示与实际同步）
        levelExpManager.addExperience(expReward);

        // 2. 添加物资奖励到背包
        addBattleRewardsToBackpack(activity);

        // 3. 添加战利品箱到背包
        addLootBoxToInventory(activity);

        // 4. 返回结果给 BattleActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("battle_result", "victory");
        resultIntent.putExtra("animal_name", animalName);
        resultIntent.putExtra("terrain_type", terrainType);
        resultIntent.putExtra("original_x", originalX);
        resultIntent.putExtra("original_y", originalY);
        resultIntent.putExtra("time_increase", 1); // 增加1小时

        activity.setResult(BattleActivity.RESULT_OK, resultIntent);
        activity.finish();
    }

    /**
     * 添加物资奖励到背包（使用统一计算的数量）
     */
    private void addBattleRewardsToBackpack(Context context) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        int userId = getCurrentUserId(); // 修复：获取int类型的用户ID

        try {
            if (meatCount > 0) {
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_MEAT, meatCount);
            }
            if (leatherCount > 0) {
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_LEATHER, leatherCount);
            }
            if (woolCount > 0) {
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_WOOL, woolCount);
            }
            if (fishCount > 0) {
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_FISH, fishCount);
            }
            if (boneCount > 0) {
                dbHelper.updateBackpackItem(userId, ItemConstants.ITEM_BONE, boneCount);
            }
            Log.d(TAG, "战斗奖励已添加到背包：" + animalName);
        } catch (Exception e) {
            Log.e(TAG, "添加背包奖励失败", e);
            Toast.makeText(context, "奖励发放失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加战利品箱到背包（增加用户提示）
     */
    private void addLootBoxToInventory(Context context) {
        if (droppedLootBox == null) return;

        try {
            LootBoxInventory inventory = LootBoxInventory.getInstance(context);
            boolean success = inventory.addLootBox(droppedLootBox, "击败" + animalName);

            if (!success) {
                Log.w(TAG, "背包已满，无法添加战利品箱");
                // 提示用户：背包已满
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("背包已满，无法接收" + droppedLootBox.getName() + "！")
                        .setPositiveButton("确认", null)
                        .show();
            } else {
                Log.i(TAG, "战利品箱已添加到背包: " + droppedLootBox.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "添加战利品箱失败", e);
            Toast.makeText(context, "战利品箱发放失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理失败结算
     */
    private void handleDefeat() {
        OnResultDialogListener listener = listenerWeakRef.get();
        BattleActivity activity = activityWeakRef.get();

        if (listener != null) {
            listener.onGameOver();
        } else if (activity != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("battle_result", "defeat");
            resultIntent.putExtra("animal_name", animalName);
            activity.setResult(BattleActivity.RESULT_CANCELED, resultIntent);
            activity.finish();
        }
    }

    /**
     * 生成战斗日志（修复换行符+同步经验值）
     */
    private String generateBattleLog(LevelExperienceManager levelExpManager) {
        StringBuilder log = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        log.append("战斗时间: ").append(currentTime).append("\n");
        log.append("战斗结果: ").append(isVictory ? "胜利" : "失败").append("\n");
        log.append("对战目标: ").append(animalName).append("\n");
        log.append("战斗地点: ").append(terrainType).append("\n");

        if (isVictory) {
            int currentLevel = levelExpManager.getCurrentLevel();
            int currentExp = levelExpManager.getCurrentExp();
            int nextLevelExp = levelExpManager.getExpRequiredForNextLevel(currentLevel);
            int newExp = currentExp + expReward; // 用统一的经验奖励

            log.append("当前等级: ").append(currentLevel).append("\n");
            log.append("当前经验: ").append(currentExp).append("/").append(nextLevelExp).append("\n");
            log.append("获得经验: ").append(expReward).append("\n");
            log.append("新经验值: ").append(newExp).append("/").append(nextLevelExp).append("\n");
            log.append("状态: ").append(newExp >= nextLevelExp ? "即将升级！" : "继续努力～").append("\n");
            log.append("掉落物品: ").append(getDroppedItemsText()).append("\n");
        } else {
            log.append("状态: 需要重新挑战").append("\n");
        }

        // 保存日志
        saveBattleLog(log.toString());
        return log.toString();
    }

    /**
     * 获取掉落物品文本（用于日志）
     */
    private String getDroppedItemsText() {
        List<String> items = new ArrayList<>();
        if (meatCount > 0) items.add("肉");
        if (leatherCount > 0) items.add("皮革");
        if (woolCount > 0) items.add("羊毛");
        if (fishCount > 0) items.add("鱼");
        if (boneCount > 0) items.add("兽骨");
        if (droppedLootBox != null) items.add(droppedLootBox.getName());
        return String.join("、", items);
    }

    /**
     * 保存战斗日志到 SharedPreferences
     */
    private void saveBattleLog(String logContent) {
        SharedPreferences preferences = requireActivity().getSharedPreferences("battle_logs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int logCount = preferences.getInt("log_count", 0);
        if (logCount >= 20) {
            // 删除最旧日志，后续日志前移
            for (int i = 1; i < 20; i++) {
                String oldLog = preferences.getString("battle_log_" + i, "");
                editor.putString("battle_log_" + (i - 1), oldLog);
            }
            editor.putString("battle_log_19", logContent);
        } else {
            editor.putString("battle_log_" + logCount, logContent);
            editor.putInt("log_count", logCount + 1);
        }
        editor.apply();
    }

    /**
     * 获取当前游戏难度
     */
    private String getCurrentDifficulty() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("game_settings", Context.MODE_PRIVATE);
        return prefs.getString("difficulty", "normal");
    }

    /**
     * 获取当前用户 ID
     */
    private int getCurrentUserId() {
        // 优先使用MyApplication中的全局用户ID
        if (MyApplication.currentUserId != -1) {
            return MyApplication.currentUserId;
        }
        
        // 备用方案：从SharedPreferences获取
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userIdStr = prefs.getString("current_user_id", "1");
        try {
            return Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            Log.w(TAG, "用户ID格式错误，使用默认值: " + userIdStr);
            return 1; // 默认用户ID
        }
    }

    /**
     * 外部设置 Listener（可选）
     */
    public void setOnResultDialogListener(OnResultDialogListener listener) {
        this.listenerWeakRef = new WeakReference<>(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放弱引用，避免内存泄漏
        activityWeakRef.clear();
        listenerWeakRef.clear();
    }
}