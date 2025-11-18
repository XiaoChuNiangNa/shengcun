package com.example.myapplication3.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.myapplication3.BattleSkill;
import com.example.myapplication3.R;

/**
 * 自定义技能进度条组件
 * 封装技能进度条和技能名称的显示逻辑
 */
public class SkillProgressBar extends FrameLayout {
    
    private ProgressBar progressBar;
    private TextView skillNameText;
    private BattleSkill currentSkill;
    
    public SkillProgressBar(Context context) {
        super(context);
        initView();
    }
    
    public SkillProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    
    public SkillProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.skill_progress_bar, this, true);
        progressBar = findViewById(R.id.skill_progress);
        skillNameText = findViewById(R.id.skill_name);
    }
    
    /**
     * 绑定技能数据
     * @param skill 技能对象
     */
    public void bindSkill(BattleSkill skill) {
        this.currentSkill = skill;
        
        if (skill == null) {
            setVisibility(View.GONE);
            return;
        }
        
        setVisibility(View.VISIBLE);
        updateProgress();
    }
    
    /**
     * 更新进度显示
     */
    public void updateProgress() {
        if (currentSkill == null) {
            setVisibility(View.GONE);
            return;
        }
        
        // 确保技能冷却值不为负数
        int currentCooldown = Math.max(0, currentSkill.getCurrentCooldown());
        int maxCooldown = Math.max(1, currentSkill.getCooldown());
        
        // 设置进度条最大值和当前进度
        progressBar.setMax(maxCooldown);
        progressBar.setProgress(maxCooldown - currentCooldown);
        
        // 更新技能名称显示（包含冷却状态）
        String skillName = currentSkill.getName() != null ? currentSkill.getName() : "未知技能";
        String displayText;
        
        if (currentCooldown <= 0) {
            displayText = skillName;
        } else {
            displayText = skillName + " (" + currentCooldown + ")";
        }
        
        skillNameText.setText(displayText);
        
        // 根据技能可用性设置颜色
        if (currentCooldown <= 0) {
            skillNameText.setTextColor(0xFF4CAF50); // 绿色表示可用
        } else {
            skillNameText.setTextColor(0xFFFF5722); // 红色表示冷却中
        }
    }
    
    /**
     * 获取当前绑定的技能
     */
    public BattleSkill getCurrentSkill() {
        return currentSkill;
    }
    
    /**
     * 检查技能是否可用
     */
    public boolean isSkillReady() {
        return currentSkill != null && currentSkill.getCurrentCooldown() <= 0;
    }
}