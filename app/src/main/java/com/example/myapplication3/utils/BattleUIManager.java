package com.example.myapplication3.utils;

import android.widget.TextView;

import com.example.myapplication3.BattleSkill;
import com.example.myapplication3.adapter.BattleCardAdapter;
import com.example.myapplication3.BattleUnit;
import java.util.List;

/**
 * 战斗UI管理工具类
 * 提供常用的UI更新操作，进一步简化代码
 */
public class BattleUIManager {
    
    private BattleCardAdapter cardAdapter;
    private TextView battleInfoText;
    private TextView resourcesText;
    private TextView battleLogText;
    private android.widget.ScrollView battleLogScroll;
    
    public BattleUIManager(BattleCardAdapter cardAdapter, 
                          TextView battleInfoText, 
                          TextView resourcesText,
                          TextView battleLogText,
                          android.widget.ScrollView battleLogScroll) {
        this.cardAdapter = cardAdapter;
        this.battleInfoText = battleInfoText;
        this.resourcesText = resourcesText;
        this.battleLogText = battleLogText;
        this.battleLogScroll = battleLogScroll;
    }
    
    /**
     * 一键更新整个战斗界面
     */
    public void updateBattleUI(BattleUnit[] playerUnits, 
                              BattleUnit[] enemyUnits,
                              int currentRound,
                              int gold,
                              int exp) {
        // 更新所有卡牌
        cardAdapter.updatePlayerUnits(playerUnits);
        cardAdapter.updateEnemyUnits(enemyUnits);
        
        // 更新信息显示
        updateBattleInfo(currentRound);
        updateResources(gold, exp);
    }
    
    /**
     * 更新所有技能进度
     */
    public void updateSkillProgress() {
        cardAdapter.updateAllSkillProgress();
    }
    
    /**
     * 更新战斗信息
     */
    public void updateBattleInfo(int currentRound) {
        if (battleInfoText != null) {
            battleInfoText.setText(String.format("第 %d 回合", currentRound));
        }
    }
    
    /**
     * 更新资源信息
     */
    public void updateResources(int gold, int exp) {
        if (resourcesText != null) {
            resourcesText.setText(String.format("金币: %d  经验: %d", gold, exp));
        }
    }
    
    /**
     * 添加战斗日志
     */
    public void addBattleLog(String message) {
        if (battleLogText != null) {
            String currentText = battleLogText.getText().toString();
            String newText = currentText + "\n" + message;
            battleLogText.setText(newText);
            
            // 滚动到底部
            if (battleLogScroll != null) {
                battleLogScroll.post(() -> battleLogScroll.fullScroll(android.view.View.FOCUS_DOWN));
            }
        }
    }
    
    /**
     * 获取所有存活的玩家单位
     */
    public List<BattleUnit> getAlivePlayers() {
        return cardAdapter.getAlivePlayerUnits();
    }
    
    /**
     * 获取所有存活的敌方单位
     */
    public List<BattleUnit> getAliveEnemies() {
        return cardAdapter.getAliveEnemyUnits();
    }
    
    /**
     * 获取可用的玩家技能
     */
    public List<BattleSkill> getAvailablePlayerSkills() {
        return cardAdapter.getAvailablePlayerSkills();
    }
    
    /**
     * 检查战斗是否结束
     */
    public boolean isBattleEnd() {
        return getAlivePlayers().isEmpty() || getAliveEnemies().isEmpty();
    }
    
    /**
     * 检查玩家是否胜利
     */
    public boolean isPlayerVictory() {
        return !getAlivePlayers().isEmpty() && getAliveEnemies().isEmpty();
    }
    
    /**
     * 处理回合开始时的UI更新
     */
    public void processRoundStart(BattleUnit[] playerUnits, 
                                 BattleUnit[] enemyUnits,
                                 int currentRound,
                                 int gold,
                                 int exp) {
        // 处理技能冷却
        processSkillCooldowns(playerUnits);
        processSkillCooldowns(enemyUnits);
        
        // 更新UI
        updateBattleUI(playerUnits, enemyUnits, currentRound, gold, exp);
    }
    
    /**
     * 处理技能冷却
     */
    private void processSkillCooldowns(BattleUnit[] units) {
        if (units == null) return;
        
        for (BattleUnit unit : units) {
            if (unit != null) {
                // 使用BattleUnit的updateCooldowns方法
                unit.updateCooldowns();
            }
        }
    }
}