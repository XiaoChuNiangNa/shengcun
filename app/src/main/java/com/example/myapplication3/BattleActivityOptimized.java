package com.example.myapplication3;

import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication3.adapter.BattleCardAdapter;
import java.util.List;

/**
 * 优化后的对战页面示例
 * 使用自定义组件和适配器模式大幅简化代码
 */
public class BattleActivityOptimized extends AppCompatActivity {
    
    // 使用适配器管理所有卡牌
    private BattleCardAdapter cardAdapter;
    
    // 战斗数据
    private BattleUnit[] playerUnits = new BattleUnit[3];
    private BattleUnit[] enemyUnits = new BattleUnit[3];
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_optimized);
        
        // 初始化适配器
        LinearLayout playerContainer = findViewById(R.id.player_units_row);
        LinearLayout enemyContainer = findViewById(R.id.enemy_units_row);
        cardAdapter = new BattleCardAdapter(this, playerContainer, enemyContainer);
        
        // 初始化战斗单位
        initBattleUnits();
        
        // 更新显示
        updateAllCards();
    }
    
    /**
     * 初始化战斗单位（极大简化的版本）
     */
    private void initBattleUnits() {
        // 创建玩家单位（只在中间位置）
        playerUnits[1] = MonsterManager.createPlayer();
        
        // 创建敌方单位（只在中间位置）
        Monster monster = MonsterManager.getRandomMonster();
        if (monster != null) {
            enemyUnits[1] = MonsterManager.convertToBattleUnit(monster);
        }
    }
    
    /**
     * 更新所有卡牌显示
     */
    private void updateAllCards() {
        // 使用适配器一键更新所有显示
        cardAdapter.updatePlayerUnits(playerUnits);
        cardAdapter.updateEnemyUnits(enemyUnits);
    }
    
    /**
     * 更新技能进度
     */
    private void updateSkillProgress() {
        // 一行代码更新所有技能进度
        cardAdapter.updateAllSkillProgress();
    }
    
    /**
     * 获取可用技能（示例）
     */
    private void showAvailableSkills() {
        List<BattleSkill> availableSkills = cardAdapter.getAvailablePlayerSkills();
        
        // 简单的技能选择逻辑
        if (!availableSkills.isEmpty()) {
            BattleSkill selectedSkill = availableSkills.get(0); // 选择第一个可用技能
            useSkill(selectedSkill);
        }
    }
    
    /**
     * 使用技能（示例）
     */
    private void useSkill(BattleSkill skill) {
        // 技能使用逻辑...
        
        // 使用后更新显示
        updateSkillProgress();
    }
    
    /**
     * 处理战斗回合
     */
    private void processBattleTurn() {
        // 处理技能冷却
        processSkillCooldowns();
        
        // 更新显示
        updateAllCards();
    }
    
    /**
     * 处理技能冷却
     */
    private void processSkillCooldowns() {
        // 处理所有单位的技能冷却
        for (BattleUnit unit : playerUnits) {
            if (unit != null) {
                unit.updateCooldowns();
            }
        }
        
        for (BattleUnit unit : enemyUnits) {
            if (unit != null) {
                unit.updateCooldowns();
            }
        }
    }
    
    /**
     * 获取存活的敌方目标（示例）
     */
    private BattleUnit getAliveEnemyTarget() {
        List<BattleUnit> aliveEnemies = cardAdapter.getAliveEnemyUnits();
        return aliveEnemies.isEmpty() ? null : aliveEnemies.get(0);
    }
    
    /**
     * 检查战斗是否结束（示例）
     */
    private boolean checkBattleEnd() {
        List<BattleUnit> alivePlayers = cardAdapter.getAlivePlayerUnits();
        List<BattleUnit> aliveEnemies = cardAdapter.getAliveEnemyUnits();
        
        return alivePlayers.isEmpty() || aliveEnemies.isEmpty();
    }
}