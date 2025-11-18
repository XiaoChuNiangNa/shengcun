package com.example.myapplication3.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.myapplication3.BattleSkill;
import com.example.myapplication3.BattleUnit;
import com.example.myapplication3.R;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义战斗卡牌组件
 * 封装整个战斗单位的UI显示逻辑
 */
public class BattleCardView extends FrameLayout {
    
    // 基础控件
    private TextView nameText;
    private TextView attackText;
    private TextView speedText;
    private ProgressBar attackProgress;
    private ProgressBar healthBar;
    
    // 技能进度条列表
    private List<SkillProgressBar> skillProgressBars = new ArrayList<>();
    private LinearLayout skillContainer;
    
    // 数据
    private BattleUnit battleUnit;
    private boolean isPlayer = false;
    
    public BattleCardView(Context context) {
        super(context);
        initView();
    }
    
    public BattleCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    
    public BattleCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.battle_card_view, this, true);
        
        // 初始化基础控件
        nameText = findViewById(R.id.card_name);
        attackText = findViewById(R.id.attack_text);
        speedText = findViewById(R.id.speed_text);
        attackProgress = findViewById(R.id.attack_progress);
        healthBar = findViewById(R.id.health_bar);
        skillContainer = findViewById(R.id.skill_container);
        
        // 初始不创建任何技能条，按需动态创建
    }
    
    /**
     * 绑定战斗单位数据
     * @param unit 战斗单位
     * @param isPlayer 是否为玩家单位
     */
    public void bindBattleUnit(BattleUnit unit, boolean isPlayer) {
        this.battleUnit = unit;
        this.isPlayer = isPlayer;
        
        if (unit == null) {
            setVisibility(View.GONE);
            return;
        }
        
        setVisibility(View.VISIBLE);
        updateDisplay();
    }
    
    /**
     * 更新显示
     */
    public void updateDisplay() {
        if (battleUnit == null) {
            setVisibility(View.GONE);
            return;
        }
        
        // 更新基础信息
        nameText.setText(battleUnit.getName());
        
        // 更新生命值
        healthBar.setMax(battleUnit.getMaxHealth());
        healthBar.setProgress(battleUnit.getCurrentHealth());
        
        // 更新速度显示
        int speed = battleUnit.getSpeed();
        speedText.setText("速度: " + speed);
        
        // 更新攻击进度条
        updateAttackProgress();
        
        // 更新技能显示
        updateSkills();
        
        // 根据玩家/敌方设置不同的样式
        updateCardStyle();
    }
    
    /**
     * 更新技能显示 - 完全动态，无硬编码
     */
    private void updateSkills() {
        BattleSkill[] skills = battleUnit.getSkills();
        
        // 清空现有的技能条
        clearAllSkillBars();
        
        // 如果没有技能，直接返回
        if (skills == null || skills.length == 0) {
            return;
        }
        
        // 动态创建技能条，一个技能对应一个进度条
        for (BattleSkill skill : skills) {
            if (skill != null) {
                createAndBindSkillBar(skill);
            }
        }
    }
    
    /**
     * 清空所有技能条
     */
    private void clearAllSkillBars() {
        // 移除所有现有技能条
        skillContainer.removeAllViews();
        skillProgressBars.clear();
    }
    
    /**
     * 创建并绑定技能条
     */
    private void createAndBindSkillBar(BattleSkill skill) {
        // 创建新的技能进度条
        SkillProgressBar skillBar = new SkillProgressBar(getContext());
        
        // 设置布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 2, 0, 2); // 技能条间距
        skillBar.setLayoutParams(params);
        
        // 绑定技能数据
        skillBar.bindSkill(skill);
        
        // 添加到容器和列表
        skillContainer.addView(skillBar);
        skillProgressBars.add(skillBar);
    }
    
    /**
     * 更新卡牌样式
     */
    private void updateCardStyle() {
        if (isPlayer) {
            // 玩家卡牌样式
            nameText.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            setBackgroundResource(R.drawable.player_card_bg);
        } else {
            // 敌方卡牌样式
            nameText.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            setBackgroundResource(R.drawable.enemy_card_bg_boss);
        }
    }
    
    /**
     * 获取可用的技能列表 - 直接从战斗单位获取，不依赖UI组件
     */
    public List<BattleSkill> getAvailableSkills() {
        List<BattleSkill> availableSkills = new ArrayList<>();
        
        if (battleUnit == null) {
            return availableSkills;
        }
        
        BattleSkill[] skills = battleUnit.getSkills();
        if (skills != null) {
            for (BattleSkill skill : skills) {
                if (skill != null && skill.getCurrentCooldown() <= 0) {
                    availableSkills.add(skill);
                }
            }
        }
        
        return availableSkills;
    }
    
    /**
     * 获取战斗单位
     */
    public BattleUnit getBattleUnit() {
        return battleUnit;
    }
    
    /**
     * 检查是否为玩家单位
     */
    public boolean isPlayer() {
        return isPlayer;
    }
    
    /**
     * 更新所有技能的进度显示
     */
    public void updateSkillProgress() {
        // 确保技能条列表不为空
        if (skillProgressBars != null) {
            for (SkillProgressBar skillBar : skillProgressBars) {
                if (skillBar != null) {
                    skillBar.updateProgress();
                }
            }
        }
        
        // 同时更新攻击进度
        updateAttackProgress();
    }
    
    /**
     * 更新攻击进度条
     */
    public void updateAttackProgress() {
        if (battleUnit == null) {
            attackProgress.setProgress(0);
            return;
        }
        
        // 攻击进度条最大值 = 攻击进度上限
        // 进度条显示当前进度值
        int maxProgress = battleUnit.getMaxAttackCooldown();
        float currentProgress = battleUnit.getCurrentAttackCooldown();
        
        // 进度条直接显示当前进度值
        attackProgress.setMax(maxProgress);
        attackProgress.setProgress(Math.round(currentProgress));
    }
    
    /**
     * 设置攻击进度条的最大值（基于双方最高速度）
     */
    public void setAttackProgressMax(int maxSpeed) {
        // 设置进度条最大值为双方最高速度值
        attackProgress.setMax(maxSpeed);
        
        // 同时更新BattleUnit的进度上限
        if (battleUnit != null) {
            battleUnit.setMaxAttackCooldown(maxSpeed);
        }
    }
}