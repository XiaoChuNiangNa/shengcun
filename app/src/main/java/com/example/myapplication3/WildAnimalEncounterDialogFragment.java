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

import java.util.Random;

/**
 * 野生动物遭遇选择对话框
 * 当玩家在采集时遇到野生动物时弹出
 */
public class WildAnimalEncounterDialogFragment extends DialogFragment {
    
    private Monster wildAnimal;
    private String currentArea;
    private MainActivity mainActivity;
    private Random random = new Random();
    
    // 遭遇动物时的回调接口
    public interface OnEncounterChoiceListener {
        void onBattleChoice(Monster animal);
        void onEscapeChoice(int staminaCost, int timeCost);
    }
    
    private OnEncounterChoiceListener listener;
    
    public WildAnimalEncounterDialogFragment() {
        // 空的构造函数
    }
    
    public static WildAnimalEncounterDialogFragment newInstance(Monster animal, String area, MainActivity activity) {
        WildAnimalEncounterDialogFragment fragment = new WildAnimalEncounterDialogFragment();
        fragment.wildAnimal = animal;
        fragment.currentArea = area;
        fragment.mainActivity = activity;
        return fragment;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // 加载自定义布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_wild_animal_encounter, null);
        
        // 设置对话框内容
        setupDialogContent(dialogView);
        
        builder.setView(dialogView)
                .setTitle("野生动物遭遇")
                .setNegativeButton("逃跑", (dialog, which) -> {
                    handleEscapeChoice();
                })
                .setPositiveButton("迎战", (dialog, which) -> {
                    handleBattleChoice();
                });
        
        return builder.create();
    }
    
    private void setupDialogContent(View dialogView) {
        if (wildAnimal == null) return;
        
        // 设置动物信息
        TextView tvAnimalName = dialogView.findViewById(R.id.tv_animal_name);
        TextView tvAnimalType = dialogView.findViewById(R.id.tv_animal_type);
        TextView tvAnimalStats = dialogView.findViewById(R.id.tv_animal_stats);
        TextView tvAnimalSkills = dialogView.findViewById(R.id.tv_animal_skills);
        TextView tvAreaInfo = dialogView.findViewById(R.id.tv_area_info);
        TextView tvEscapeCost = dialogView.findViewById(R.id.tv_escape_cost);
        
        // 计算逃跑的体力消耗（最多50点）
        int staminaCost = calculateStaminaCost();
        int timeCost = 5; // 逃跑时间成本固定为5小时
        
        tvAnimalName.setText("遭遇：" + wildAnimal.getName());
        tvAnimalType.setText("类型：" + wildAnimal.getType());
        tvAnimalStats.setText(String.format("属性：生命 %d | 攻击 %d | 防御 %d | 速度 %d", 
            wildAnimal.getMaxHealth(), wildAnimal.getAttack(), 
            wildAnimal.getDefense(), wildAnimal.getSpeed()));
        
        // 显示技能信息
        StringBuilder skillsText = new StringBuilder("技能：");
        if (wildAnimal.getSkills() != null && !wildAnimal.getSkills().isEmpty()) {
            for (int i = 0; i < wildAnimal.getSkills().size(); i++) {
                if (i > 0) skillsText.append(", ");
                skillsText.append(wildAnimal.getSkills().get(i).getName());
            }
        } else {
            skillsText.append("无特殊技能");
        }
        tvAnimalSkills.setText(skillsText.toString());
        
        tvAreaInfo.setText("发现地点：" + currentArea);
        tvEscapeCost.setText(String.format("逃跑将消耗%d点体力，游戏时间增加%d小时", staminaCost, timeCost));
    }
    
    private int calculateStaminaCost() {
        // 根据动物类型和当前玩家状态计算体力消耗
        int baseCost = 10;
        
        // 根据动物大小调整基础消耗
        if (wildAnimal.getType().contains("大型")) {
            baseCost += 20;
        } else if (wildAnimal.getType().contains("中型")) {
            baseCost += 10;
        }
        
        // 根据动物速度调整消耗（速度越快逃跑越困难）
        int speedModifier = wildAnimal.getSpeed() / 50;
        baseCost += Math.min(speedModifier, 10);
        
        // 体力消耗上限为50点
        return Math.min(baseCost, 50);
    }
    
    private void handleBattleChoice() {
        if (listener != null) {
            listener.onBattleChoice(wildAnimal);
        }
        
        // 启动野外对战
        if (mainActivity != null) {
            Intent battleIntent = new Intent(mainActivity, BattleActivity.class);
            battleIntent.putExtra("wild_encounter", true);
            battleIntent.putExtra("animal_name", wildAnimal.getName());
            battleIntent.putExtra("terrain_type", currentArea);
            battleIntent.putExtra("original_x", mainActivity.currentX);
            battleIntent.putExtra("original_y", mainActivity.currentY);
            mainActivity.startActivity(battleIntent);
        }
    }
    
    private void handleEscapeChoice() {
        int staminaCost = calculateStaminaCost();
        int timeCost = 5; // 逃跑时间固定增加5小时
        
        if (listener != null) {
            listener.onEscapeChoice(staminaCost, timeCost);
        }
        
        // 应用逃跑效果
        if (mainActivity != null) {
            // 扣除体力
            mainActivity.stamina = Math.max(0, mainActivity.stamina - staminaCost);
            
            // 增加游戏时间
            mainActivity.gameHour += timeCost;
            if (mainActivity.gameHour >= 24) {
                mainActivity.gameDay += mainActivity.gameHour / 24;
                mainActivity.gameHour %= 24;
            }
            
            // 保存数据
            mainActivity.dataManager.saveAllCriticalData();
            
            // 更新UI
            mainActivity.uiUpdater.updateStatusDisplays();
            mainActivity.uiUpdater.updateTimeDisplay();
            
            // 显示逃跑成功提示
            if (mainActivity.tvScrollTip != null) {
                mainActivity.tvScrollTip.setText(String.format(
                    "成功逃脱！消耗%d点体力，时间流逝%d小时", staminaCost, timeCost));
            }
        }
    }
    
    public void setOnEncounterChoiceListener(OnEncounterChoiceListener listener) {
        this.listener = listener;
    }
}