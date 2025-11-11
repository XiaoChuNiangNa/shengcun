package com.example.myapplication3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
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
        TextView tvRewards = view.findViewById(R.id.tv_rewards);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        
        // 设置标题和结果
        if (isVictory) {
            tvTitle.setText("战斗胜利！");
            tvResult.setText("成功击败了" + animalName + "！");
        } else {
            tvTitle.setText("战斗失败");
            tvResult.setText("被" + animalName + "击败了...");
        }
        
        // 设置奖励信息
        List<String> rewards = calculateRewards();
        StringBuilder rewardsText = new StringBuilder();
        for (String reward : rewards) {
            rewardsText.append(reward).append("\n");
        }
        tvRewards.setText(rewardsText.toString());
        
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
            rewards.add("✓ 游戏时间 +1小时");
            
            // 根据动物类型和地形获得不同物品
            switch (animalName) {
                case "野兔":
                case "野鸡":
                    rewards.add("✓ 获得 兔肉 x" + (1 + random.nextInt(2)));
                    rewards.add("✓ 获得 生皮 x" + (1 + random.nextInt(2)));
                    break;
                case "小猪":
                case "野猪":
                    rewards.add("✓ 获得 猪肉 x" + (2 + random.nextInt(3)));
                    rewards.add("✓ 获得 野猪牙 x1");
                    break;
                case "狼":
                    rewards.add("✓ 获得 狼肉 x" + (2 + random.nextInt(2)));
                    rewards.add("✓ 获得 狼皮 x1");
                    rewards.add("✓ 获得 狼牙 x" + (1 + random.nextInt(2)));
                    break;
                case "鹿":
                    rewards.add("✓ 获得 鹿肉 x" + (3 + random.nextInt(2)));
                    rewards.add("✓ 获得 鹿角 x1");
                    rewards.add("✓ 获得 鹿皮 x1");
                    break;
                case "老虎":
                case "狮子":
                    rewards.add("✓ 获得 虎肉 x" + (3 + random.nextInt(2)));
                    rewards.add("✓ 获得 虎皮 x1");
                    rewards.add("✓ 获得 虎骨 x1");
                    rewards.add("✓ 获得 虎牙 x2");
                    break;
                case "熊":
                    rewards.add("✓ 获得 熊肉 x" + (4 + random.nextInt(2)));
                    rewards.add("✓ 获得 熊皮 x1");
                    rewards.add("✓ 获得 熊胆 x1");
                    rewards.add("✓ 获得 熊掌 x2");
                    break;
                case "鲨鱼":
                    rewards.add("✓ 获得 鲨鱼肉 x" + (3 + random.nextInt(3)));
                    rewards.add("✓ 获得 鲨鱼鳍 x1");
                    rewards.add("✓ 获得 鲨鱼牙齿 x" + (2 + random.nextInt(3)));
                    break;
                default:
                    rewards.add("✓ 获得 肉 x" + (2 + random.nextInt(3)));
                    rewards.add("✓ 获得 皮 x1");
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
}