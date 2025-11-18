package com.example.myapplication3.adapter;

import android.content.Context;
import android.widget.LinearLayout;
import com.example.myapplication3.BattleUnit;
import com.example.myapplication3.ui.BattleCardView;
import java.util.ArrayList;
import java.util.List;

/**
 * 战斗卡牌适配器
 * 管理玩家和敌方卡牌的显示和更新
 */
public class BattleCardAdapter {
    
    private Context context;
    private LinearLayout playerContainer;
    private LinearLayout enemyContainer;
    private List<BattleCardView> playerCards = new ArrayList<>();
    private List<BattleCardView> enemyCards = new ArrayList<>();
    
    public BattleCardAdapter(Context context, LinearLayout playerContainer, LinearLayout enemyContainer) {
        this.context = context;
        this.playerContainer = playerContainer;
        this.enemyContainer = enemyContainer;
        initCards();
    }
    
    /**
     * 初始化卡牌（每个位置最多3个）
     */
    private void initCards() {
        // 清除现有卡牌
        playerCards.clear();
        enemyCards.clear();
        playerContainer.removeAllViews();
        enemyContainer.removeAllViews();
        
        // 创建3个玩家卡牌位置
        for (int i = 0; i < 3; i++) {
            BattleCardView card = new BattleCardView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 4, 4, 4);
            card.setLayoutParams(params);
            playerContainer.addView(card);
            playerCards.add(card);
        }
        
        // 创建3个敌方卡牌位置
        for (int i = 0; i < 3; i++) {
            BattleCardView card = new BattleCardView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 4, 4, 4);
            card.setLayoutParams(params);
            enemyContainer.addView(card);
            enemyCards.add(card);
        }
    }
    
    /**
     * 更新玩家单位显示
     * @param playerUnits 玩家单位数组
     */
    public void updatePlayerUnits(BattleUnit[] playerUnits) {
        if (playerUnits == null) return;
        
        for (int i = 0; i < Math.min(playerUnits.length, playerCards.size()); i++) {
            playerCards.get(i).bindBattleUnit(playerUnits[i], true);
        }
        
        // 隐藏多余的位置
        for (int i = playerUnits.length; i < playerCards.size(); i++) {
            playerCards.get(i).bindBattleUnit(null, true);
        }
    }
    
    /**
     * 更新敌方单位显示
     * @param enemyUnits 敌方单位数组
     */
    public void updateEnemyUnits(BattleUnit[] enemyUnits) {
        if (enemyUnits == null) return;
        
        for (int i = 0; i < Math.min(enemyUnits.length, enemyCards.size()); i++) {
            enemyCards.get(i).bindBattleUnit(enemyUnits[i], false);
        }
        
        // 隐藏多余的位置
        for (int i = enemyUnits.length; i < enemyCards.size(); i++) {
            enemyCards.get(i).bindBattleUnit(null, false);
        }
    }
    
    /**
     * 更新所有卡牌的技能进度
     */
    public void updateAllSkillProgress() {
        // 首先计算双方最高速度
        int maxSpeed = calculateMaxSpeed();
        
        // 设置所有卡牌的攻击进度条上限
        for (BattleCardView card : playerCards) {
            card.setAttackProgressMax(maxSpeed);
        }
        for (BattleCardView card : enemyCards) {
            card.setAttackProgressMax(maxSpeed);
        }
        
        // 然后更新所有进度显示
        for (BattleCardView card : playerCards) {
            card.updateSkillProgress();
        }
        for (BattleCardView card : enemyCards) {
            card.updateSkillProgress();
        }
    }
    
    /**
     * 计算战场上所有单位的最高速度
     * 这个值将作为所有单位的攻击进度上限
     */
    private int calculateMaxSpeed() {
        int maxSpeed = 100; // 默认最低速度
        
        for (BattleCardView card : playerCards) {
            BattleUnit unit = card.getBattleUnit();
            if (unit != null && unit.isAlive()) {
                maxSpeed = Math.max(maxSpeed, unit.getSpeed());
            }
        }
        
        for (BattleCardView card : enemyCards) {
            BattleUnit unit = card.getBattleUnit();
            if (unit != null && unit.isAlive()) {
                maxSpeed = Math.max(maxSpeed, unit.getSpeed());
            }
        }
        
        return maxSpeed;
    }
    
    /**
     * 获取指定位置的玩家卡牌
     */
    public BattleCardView getPlayerCard(int position) {
        if (position >= 0 && position < playerCards.size()) {
            return playerCards.get(position);
        }
        return null;
    }
    
    /**
     * 获取指定位置的敌方卡牌
     */
    public BattleCardView getEnemyCard(int position) {
        if (position >= 0 && position < enemyCards.size()) {
            return enemyCards.get(position);
        }
        return null;
    }
    
    /**
     * 获取所有可用的玩家技能
     */
    public List<com.example.myapplication3.BattleSkill> getAvailablePlayerSkills() {
        List<com.example.myapplication3.BattleSkill> availableSkills = new ArrayList<>();
        
        for (BattleCardView card : playerCards) {
            availableSkills.addAll(card.getAvailableSkills());
        }
        
        return availableSkills;
    }
    
    /**
     * 获取所有存活的玩家单位
     */
    public List<BattleUnit> getAlivePlayerUnits() {
        List<BattleUnit> aliveUnits = new ArrayList<>();
        
        for (BattleCardView card : playerCards) {
            BattleUnit unit = card.getBattleUnit();
            if (unit != null && unit.getCurrentHealth() > 0) {
                aliveUnits.add(unit);
            }
        }
        
        return aliveUnits;
    }
    
    /**
     * 获取所有存活的敌方单位
     */
    public List<BattleUnit> getAliveEnemyUnits() {
        List<BattleUnit> aliveUnits = new ArrayList<>();
        
        for (BattleCardView card : enemyCards) {
            BattleUnit unit = card.getBattleUnit();
            if (unit != null && unit.getCurrentHealth() > 0) {
                aliveUnits.add(unit);
            }
        }
        
        return aliveUnits;
    }
    
    /**
     * 刷新所有卡牌显示
     */
    public void refreshAllCards() {
        for (BattleCardView card : playerCards) {
            card.updateDisplay();
        }
        for (BattleCardView card : enemyCards) {
            card.updateDisplay();
        }
    }
}