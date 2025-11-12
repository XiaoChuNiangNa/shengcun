package com.example.myapplication3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 野外对战结算弹窗
 * 显示对战成功后的奖励信息
 */
public class WildBattleResultDialogFragment extends DialogFragment {

    private static final String ARG_IS_VICTORY = "is_victory";
    private static final String ARG_ANIMAL_NAME = "animal_name";
    private static final String ARG_TERRAIN_TYPE = "terrain_type";
    private static final String ARG_ORIGINAL_X = "original_x";
    private static final String ARG_ORIGINAL_Y = "original_y";

    private boolean isVictory;
    private String animalName;
    private String terrainType;
    private int originalX, originalY;
    private BattleActivity activity;

    private OnResultDialogListener listener;

    public interface OnResultDialogListener {
        void onReturnToGame();
        void onGameOver();
    }

    public static WildBattleResultDialogFragment newInstance(boolean isVictory, String animalName,
                                                             String terrainType, int originalX,
                                                             int originalY, BattleActivity activity) {
        WildBattleResultDialogFragment fragment = new WildBattleResultDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_VICTORY, isVictory);
        args.putString(ARG_ANIMAL_NAME, animalName);
        args.putString(ARG_TERRAIN_TYPE, terrainType);
        args.putInt(ARG_ORIGINAL_X, originalX);
        args.putInt(ARG_ORIGINAL_Y, originalY);
        fragment.setArguments(args);
        fragment.activity = activity;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            isVictory = getArguments().getBoolean(ARG_IS_VICTORY);
            animalName = getArguments().getString(ARG_ANIMAL_NAME);
            terrainType = getArguments().getString(ARG_TERRAIN_TYPE);
            originalX = getArguments().getInt(ARG_ORIGINAL_X);
            originalY = getArguments().getInt(ARG_ORIGINAL_Y);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        if (isVictory) {
            tvTitle.setText("战斗胜利！");
            tvResult.setText("成功击败了" + animalName + "！");
        } else {
            tvTitle.setText("战斗失败");
            tvResult.setText("被" + animalName + "击败了...");
        }

        // 获取并显示等级信息
        LevelExperienceManager levelExpManager = new LevelExperienceManager(getActivity());
        tvLevelInfo.setText(levelExpManager.getLevelInfo());

        // 设置奖励信息
        List<String> rewards = calculateRewards();
        StringBuilder rewardsText = new StringBuilder();
        for (String reward : rewards) {
            rewardsText.append(reward).append("\n");
        }
        tvRewards.setText(rewardsText.toString());

        // 记录战斗日志
        String logText = generateBattleLog(levelExpManager);
        tvLog.setText(logText);

        // 确认按钮
        btnConfirm.setOnClickListener(v -> {
            if (isVictory) {
                handleVictory();
            } else {
                handleDefeat();
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    /**
     * 计算奖励
     */
    private List<String> calculateRewards() {
        List<String> rewards = new ArrayList<>();
        Random random = new Random();

        if (isVictory) {
            // 胜利奖励
//            rewards.add("✓ 游戏时间 +1小时");

            // 根据动物类型和地形获得不同物品
            switch (animalName) {
                case "野兔":
                case "野鸡":
                case "小猪":
                case "蛇":
                    rewards.add("✓ 获得 肉 x" + (1 + random.nextInt(3)));
                    rewards.add("✓ 获得 皮革 x" + (1 + random.nextInt(2)));
                    break;
                case "山羊":
                    rewards.add("✓ 获得 肉 x" + (1 + random.nextInt(3)));
                    rewards.add("✓ 获得 皮革 x" + (1 + random.nextInt(2)));
                    rewards.add("✓ 获得 羊毛 x" + (1 + random.nextInt(2)));
                    break;
                case "食人鱼":
                    rewards.add("✓ 获得 鱼 x" + (1 + random.nextInt(3)));
                    break;
                case "狼":
                case "鹿":
                case "野猪":
                case "猴子":
                    rewards.add("✓ 获得 肉 x" + (2 + random.nextInt(3)));
                    rewards.add("✓ 获得 皮革 x" + (1 + random.nextInt(4)));
                    rewards.add("✓ 获得 兽骨 x" + (1 + random.nextInt(2)));
                    break;
                case "老虎":
                case "狮子":
                case "熊":
                case "猎豹":
                case "鲨鱼":
                    rewards.add("✓ 获得 肉 x" + (3 + random.nextInt(3)));
                    rewards.add("✓ 获得 皮革 x" + (2 + random.nextInt(4)));
                    rewards.add("✓ 获得 兽骨 x" + (1 + random.nextInt(4)));
                    break;
                default:
                    rewards.add("✓ 获得 肉 x" + (2 + random.nextInt(3)));
                    rewards.add("✓ 获得 皮革 x1");
            }

            // 经验奖励
            rewards.add("✓ 经验 +" + (50 + random.nextInt(50)));

        } else {
            // 失败惩罚
            rewards.add("✗ 体力消耗过多");
            rewards.add("✗ 部分物品丢失");
            rewards.add("✗ 需要重新挑战");
        }

        return rewards;
    }

    /**
     * 处理胜利结算
     */
    private void handleVictory() {
        // 将战斗胜利获得的物资添加到背包
        addBattleRewardsToBackpack();
        
        // 增加游戏时间1小时
        if (activity != null) {
            // 通过Intent返回胜利结果
            Intent resultIntent = new Intent();
            resultIntent.putExtra("battle_result", "victory");
            resultIntent.putExtra("animal_name", animalName);
            resultIntent.putExtra("terrain_type", terrainType);
            resultIntent.putExtra("original_x", originalX);
            resultIntent.putExtra("original_y", originalY);
            resultIntent.putExtra("time_increase", 1); // 增加1小时

            if (activity.getParent() == null) {
                activity.setResult(BattleActivity.RESULT_OK, resultIntent);
            } else {
                activity.getParent().setResult(BattleActivity.RESULT_OK, resultIntent);
            }

            // 显示结算信息并返回
            activity.finish();
        }
    }

    /**
     * 将战斗胜利获得的物资添加到背包
     */
    private void addBattleRewardsToBackpack() {
        if (activity == null) return;
        
        DBHelper dbHelper = DBHelper.getInstance(activity);
        Random random = new Random();
        
        // 根据动物类型添加不同数量的物资到背包
        switch (animalName) {
            case "野兔":
            case "野鸡":
            case "小猪":
            case "蛇":
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_MEAT, 1 + random.nextInt(3));
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_LEATHER, 1 + random.nextInt(2));
                break;
            case "山羊":
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_MEAT, 1 + random.nextInt(3));
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_LEATHER, 1 + random.nextInt(2));
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_WOOL, 1 + random.nextInt(2));
                break;
            case "食人鱼":
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_FISH, 1 + random.nextInt(3));
                break;
            case "狼":
            case "鹿":
            case "野猪":
            case "猴子":
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_MEAT, 2 + random.nextInt(3));
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_LEATHER, 1 + random.nextInt(4));
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_BONE, 1 + random.nextInt(2));
                break;
            case "老虎":
            case "狮子":
            case "熊":
            case "猎豹":
            case "鲨鱼":
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_MEAT, 3 + random.nextInt(3));
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_LEATHER, 2 + random.nextInt(4));
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_BONE, 1 + random.nextInt(4));
                break;
            default:
                // 默认掉落肉和皮
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_MEAT, 2 + random.nextInt(3));
                dbHelper.updateBackpackItem(MyApplication.currentUserId, ItemConstants.ITEM_LEATHER, 1);
        }
        
        Log.d("BattleRewards", "战斗胜利奖励已添加到背包：" + animalName);
    }

    /**
     * 处理失败结算
     */
    private void handleDefeat() {
        // 触发游戏失败逻辑
        if (listener != null) {
            listener.onGameOver();
        } else if (activity != null) {
            // 通过Intent返回失败结果
            Intent resultIntent = new Intent();
            resultIntent.putExtra("battle_result", "defeat");
            resultIntent.putExtra("animal_name", animalName);

            if (activity.getParent() == null) {
                activity.setResult(BattleActivity.RESULT_CANCELED, resultIntent);
            } else {
                activity.getParent().setResult(BattleActivity.RESULT_CANCELED, resultIntent);
            }

            // 关闭对战页面，触发游戏失败
            activity.finish();
        }
    }

    public void setOnResultDialogListener(OnResultDialogListener listener) {
        this.listener = listener;
    }

    /**
     * 生成战斗日志
     */
    private String generateBattleLog(LevelExperienceManager levelExpManager) {
        StringBuilder log = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        // 修正：用\n替换所有字符串中的实际换行
        log.append("战斗时间: ").append(currentTime).append("\n");

        if (isVictory) {
            log.append("战斗结果: 胜利\n");
            log.append("击败目标: ").append(animalName).append("\n");
            log.append("战斗地点: ").append(terrainType).append("\n");

            // 获取当前等级和经验信息
            int currentLevel = levelExpManager.getCurrentLevel();
            int currentExp = levelExpManager.getCurrentExp();
            int nextLevelExp = levelExpManager.getExpRequiredForNextLevel(currentLevel);

            log.append("当前等级: ").append(currentLevel).append("\n");
            log.append("当前经验: ").append(currentExp).append("/").append(nextLevelExp).append("\n");

            // 计算经验奖励（根据calculateRewards方法中的逻辑）
            Random random = new Random();
            int expReward = 50 + random.nextInt(50);
            int newExp = currentExp + expReward;

            log.append("获得经验: ").append(expReward).append("\n");
            log.append("新经验值: ").append(newExp).append("/").append(nextLevelExp).append("\n");

            // 检查是否升级
            if (newExp >= nextLevelExp) {
                log.append("状态: 即将升级！\n");
            }

            // 记录掉落物（根据calculateRewards方法中的逻辑）
            log.append("掉落物品: ");
            switch (animalName) {
                case "野兔":
                case "野鸡":
                case "小猪":
                case "蛇":
                    log.append("肉, 皮革\n");
                    break;
                case "山羊":
                    log.append("肉, 皮革, 羊毛\n");
                    break;
                case "食人鱼":
                    log.append("鱼\n");
                    break;
                case "狼":
                case "鹿":
                case "野猪":
                case "猴子":
                    log.append("肉, 皮革, 兽骨\n");
                    break;
                case "老虎":
                case "狮子":
                case "熊":
                case "猎豹":
                case "鲨鱼":
                    log.append("肉, 皮革, 兽骨\n");
                    break;
                default:
                    log.append("肉, 皮革\n");
            }
        } else {
            log.append("战斗结果: 失败\n");
            log.append("被击败: ").append(animalName).append("\n");
            log.append("战斗地点: ").append(terrainType).append("\n");
            log.append("状态: 需要重新挑战\n");
        }

        // 保存日志到SharedPreferences
        saveBattleLog(log.toString());

        return log.toString();
    }

    /**
     * 保存战斗日志到SharedPreferences
     */
    private void saveBattleLog(String logContent) {
        if (getActivity() != null) {
            SharedPreferences preferences = getActivity().getSharedPreferences("battle_logs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            // 获取当前日志数量
            int logCount = preferences.getInt("log_count", 0);

            // 修正：日志数量限制逻辑（避免越界）
            if (logCount >= 20) {
                // 删除最旧的日志（索引0），并将后续日志前移
                for (int i = 1; i < 20; i++) { // 只循环到19（因为i=20时已超出原最大索引）
                    String oldLog = preferences.getString("battle_log_" + i, "");
                    editor.putString("battle_log_" + (i - 1), oldLog);
                }
                // 新日志放在最后一个位置（索引19）
                editor.putString("battle_log_19", logContent);
                editor.putInt("log_count", 20); // 保持最大数量20
            } else {
                // 直接添加新日志
                editor.putString("battle_log_" + logCount, logContent);
                editor.putInt("log_count", logCount + 1);
            }

            editor.apply();
        }
    }
}