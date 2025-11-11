package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 对战页面 - 实现3v3布局中的1v1对战，支持召唤技能和自动/手动切换
 */
public class BattleActivity extends AppCompatActivity {
    
    // 界面控件
    private ImageButton btnBack;
    private TextView tvMapTitle, tvBattleInfo, tvResources;
    private Switch switchAutoBattle;
    private Button btnAttack, btnSkill, btnEndTurn;
    
    // 手动/自动文字控件
    private TextView tvManualText, tvAutoText;
    
    // 战斗信息滚动视图
    private ScrollView battleLogScroll;
    private TextView battleLogText;
    
    // 敌方单位控件（3个位置）
    private ProgressBar[] enemyAttackProgress = new ProgressBar[3];
    private ProgressBar[][] enemySkillProgress = new ProgressBar[3][2];
    private ProgressBar[] enemyHealth = new ProgressBar[3];
    private TextView[] enemyName = new TextView[3];
    
    // 玩家单位控件（3个位置）
    private ProgressBar[] playerAttackProgress = new ProgressBar[3];
    private ProgressBar[][] playerSkillProgress = new ProgressBar[3][2];
    private ProgressBar[] playerHealth = new ProgressBar[3];
    private TextView[] playerName = new TextView[3];
    
    // 对战状态
    private int currentRound = 1;
    private boolean isAutoBattle = true; // 默认自动战斗
    private boolean autoBattleMode = true; // 实际的自动战斗状态（下一回合生效）
    private int playerGold = 0;
    private int playerExp = 0;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());
    
    // 战斗统计
    private int totalDamageDealt = 0;
    private int totalDamageTaken = 0;
    private int totalHealing = 0;
    private int enemiesKilled = 0;
    private int skillsUsed = 0;
    private int summonsUsed = 0;
    private long battleStartTime;
    private List<String> battleLog = new ArrayList<>();
    private boolean battleEnded = false;
    
    // 结果管理器
    private BattleResultManager resultManager;
    private int userId;
    private boolean isBossBattle = false;
    
    // 单位数据（保留3v3布局，但默认1v1对战）
    private BattleUnit[] playerUnits = new BattleUnit[3]; // 玩家单位数组
    private BattleUnit[] enemyUnits = new BattleUnit[3];  // 敌方单位数组
    
    // 召唤技能相关
    private static final int SUMMON_SKILL_INDEX = 3; // 召唤技能索引
    private static final int SUMMON_DURATION = 5; // 召唤单位持续时间（回合数）
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        
        // 初始化用户ID和结果管理器
        userId = MyApplication.currentUserId;
        if (userId == -1) {
            Toast.makeText(this, "未登录用户，无法进行对战", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        resultManager = new BattleResultManager(this);
        
        // 初始化战斗统计
        battleStartTime = System.currentTimeMillis();
        
        initViews();
        initBattleUnits();
        updateUI();
        setupListeners();
        
        // 添加战斗开始日志（必须在initViews之后）
        addBattleLog("战斗开始 - 暗影森林");
        
        // 开始自动战斗
        if (autoBattleMode) {
            startAutoBattle();
        }
    }
    
    /**
     * 初始化视图控件
     */
    private void initViews() {
        // 标题和返回按钮
        btnBack = findViewById(R.id.btn_back);
        tvMapTitle = findViewById(R.id.tv_map_title);
        
        // 底部信息显示
        tvBattleInfo = findViewById(R.id.tv_battle_info);
        tvResources = findViewById(R.id.tv_resources);
        
        // 自动战斗开关
        switchAutoBattle = findViewById(R.id.switch_auto_battle);
        
        // 手动/自动文字控件
        tvManualText = findViewById(R.id.tv_manual_text);
        tvAutoText = findViewById(R.id.tv_auto_text);
        
        // 底部按钮
        btnAttack = findViewById(R.id.btn_attack);
        btnSkill = findViewById(R.id.btn_skill);
        btnEndTurn = findViewById(R.id.btn_end_turn);
        
        // 战斗信息滚动视图
        battleLogScroll = findViewById(R.id.battle_log_scroll);
        battleLogText = findViewById(R.id.battle_log_text);
        
        // 初始化3个位置的单位控件（适配1v1布局）
        for (int i = 0; i < 3; i++) {
            // 敌方单位控件 - 只有位置0有实际控件
            if (i == 0) {
                enemyName[i] = findViewById(R.id.enemy_name);
                enemyAttackProgress[i] = findViewById(R.id.enemy_attack_progress);
                enemyHealth[i] = findViewById(R.id.enemy_health);
                
                // 敌方技能进度条
                for (int j = 0; j < 2; j++) {
                    enemySkillProgress[i][j] = findViewById(getResources().getIdentifier(
                        "enemy_skill_" + (j + 1), "id", getPackageName()));
                }
            } else {
                // 位置1和位置2设为空
                enemyName[i] = null;
                enemyAttackProgress[i] = null;
                enemyHealth[i] = null;
                for (int j = 0; j < 2; j++) {
                    enemySkillProgress[i][j] = null;
                }
            }
            
            // 玩家单位控件 - 只有位置0有实际控件
            if (i == 0) {
                playerName[i] = findViewById(R.id.player_name);
                playerAttackProgress[i] = findViewById(R.id.player_attack_progress);
                playerHealth[i] = findViewById(R.id.player_health);
                
                // 玩家技能进度条
                for (int j = 0; j < 2; j++) {
                    playerSkillProgress[i][j] = findViewById(getResources().getIdentifier(
                        "player_skill_" + (j + 1), "id", getPackageName()));
                }
            } else {
                // 位置1和位置2设为空
                playerName[i] = null;
                playerAttackProgress[i] = null;
                playerHealth[i] = null;
                for (int j = 0; j < 2; j++) {
                    playerSkillProgress[i][j] = null;
                }
            }
        }
    }
    
    /**
     * 初始化战斗单位（默认1v1，但保留3v3布局）
     */
    private void initBattleUnits() {
        // 重置所有单位
        for (int i = 0; i < 3; i++) {
            playerUnits[i] = null;
            enemyUnits[i] = null;
        }
        
        // 创建主玩家单位（位置0）
        playerUnits[0] = MonsterManager.createPlayer();
        playerUnits[0].resetCooldowns();
        
        // 创建主敌方单位（位置0）
        Monster monster = MonsterManager.getRandomMonster();
        if (monster != null) {
            enemyUnits[0] = MonsterManager.convertToBattleUnit(monster);
        } else {
            // 如果没有怪物，使用默认敌人
            enemyUnits[0] = new BattleUnit("森林守护者", 100, 8, 5, 80, 
                new BattleSkill[0], BattleUnit.TYPE_ENEMY);
        }
        enemyUnits[0].resetCooldowns();
        
        // 初始化召唤技能
        initSummonSkill();
        
        // 记录对战开始时的属性状态
        addBattleLog("[开始战斗]");
        addBattleLog("玩家单位属性:");
        addBattleLog(getUnitAttributesString(playerUnits[0]));
        addBattleLog("敌方单位属性:");
        addBattleLog(getUnitAttributesString(enemyUnits[0]));
    }
    
    /**
     * 初始化召唤技能
     */
    private void initSummonSkill() {
        // 给玩家单位添加召唤技能
        if (playerUnits[0] != null) {
            BattleSkill[] skills = new BattleSkill[4]; // 包括召唤技能
            String[] skillNames = {"重击", "治疗", "暴风斩", "分身召唤"};
            int[] skillCooldowns = {3, 5, 7, 8}; // 召唤技能冷却较长
            
            for (int i = 0; i < skills.length; i++) {
                skills[i] = new BattleSkill(skillNames[i], skillCooldowns[i], 
                    playerUnits[0].getAttack() * (i == 3 ? 0 : 2)); // 召唤技能不造成伤害
            }
            
            // 替换原有技能
            playerUnits[0].setSkills(skills);
        }
    }
    
    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());
        
        // 自动战斗开关 - 点击后下一回合生效
        switchAutoBattle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAutoBattle = isChecked; // 界面显示状态
            // 实际战斗模式将在下一回合开始时切换
            addBattleLog("战斗模式将在下一回合切换为" + (isChecked ? "自动" : "手动"));
            updateSwitchTextColors();
        });
        
        // 底部按钮事件
        btnAttack.setOnClickListener(v -> executePlayerAttack());
        btnSkill.setOnClickListener(v -> showSkillSelectionDialog());
        btnEndTurn.setOnClickListener(v -> endPlayerTurn());
    }
    
    /**
     * 开始自动战斗
     */
    private void startAutoBattle() {
        handler.removeCallbacksAndMessages(null);
        updateButtonStates();
        autoBattleMode = true;
        
        // 自动战斗逻辑 - 每2秒执行一次
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (autoBattleMode && !checkBattleResult()) {
                    executeAutoBattleRound();
                    handler.postDelayed(this, 2000);
                } else if (autoBattleMode) {
                    // 战斗结束，停止自动战斗
                    stopAutoBattle();
                }
            }
        }, 2000);
        
        addBattleLog("自动战斗模式开启");
    }
    
    /**
     * 停止自动战斗
     */
    private void stopAutoBattle() {
        // 清除所有自动战斗的回调
        handler.removeCallbacksAndMessages(null);
        
        // 更新按钮状态为可点击
        updateButtonStates();
        autoBattleMode = false;
        
        // 重置玩家单位的攻击冷却，为手动战斗做准备
        for (int i = 0; i < 3; i++) {
            if (playerUnits[i] != null) {
                playerUnits[i].resetCooldowns();
            }
        }
        
        addBattleLog("手动战斗模式开启");
    }
    
    /**
     * 执行玩家攻击
     */
    private void executePlayerAttack() {
        if (!autoBattleMode && playerUnits[0] != null && playerUnits[0].isAlive() && 
            playerUnits[0].isReadyToAttack() && enemyUnits[0] != null && enemyUnits[0].isAlive()) {
            
            // 执行攻击
            BattleUnitAction action = new BattleUnitAction(BattleUnitAction.ACTION_ATTACK, -1, 0);
            executeAction(playerUnits[0], action, true);
            
            // 更新冷却
            playerUnits[0].updateCooldowns();
            updateSummonDuration();
            
            // 更新UI
            updateUI();
            
            // 检查战斗结果
            if (checkBattleResult()) return;
            
            // 自动执行敌方反击（如果敌方存活）
            if (enemyUnits[0].isAlive() && enemyUnits[0].isReadyToAttack()) {
                // 延迟执行敌方反击
                handler.postDelayed(() -> {
                    BattleUnitAction enemyAction = new BattleUnitAction(BattleUnitAction.ACTION_ATTACK, -1, 0);
                    executeAction(enemyUnits[0], enemyAction, false);
                    enemyUnits[0].updateCooldowns();
                    updateUI();
                    checkBattleResult();
                }, 1000);
            }
        } else if (!autoBattleMode) {
            addBattleLog("单位不可行动或战斗已结束");
        }
    }
    
    /**
     * 显示技能选择弹窗
     */
    private void showSkillSelectionDialog() {
        if (playerUnits[0] == null || !playerUnits[0].isAlive()) {
            addBattleLog("玩家单位无法行动");
            return;
        }
        
        // 创建自定义对话框
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_skill_selection, null);
        builder.setView(dialogView);
        
        final android.app.AlertDialog dialog = builder.create();
        
        // 设置技能槽位信息
        Button btnSkill1 = dialogView.findViewById(R.id.btn_select_skill_1);
        Button btnSkill2 = dialogView.findViewById(R.id.btn_select_skill_2);
        Button btnSkill3 = dialogView.findViewById(R.id.btn_select_skill_3);
        Button btnSkill4 = dialogView.findViewById(R.id.btn_select_skill_4); // 召唤技能
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_skill);
        
        // 根据玩家实际拥有的技能来设置
        TextView skillName1 = dialogView.findViewById(R.id.skill_name_1);
        TextView skillName2 = dialogView.findViewById(R.id.skill_name_2);
        TextView skillName3 = dialogView.findViewById(R.id.skill_name_3);
        TextView skillName4 = dialogView.findViewById(R.id.skill_name_4);
        
        skillName1.setText("重击");
        skillName2.setText("治疗");
        skillName3.setText("暴风斩");
        skillName4.setText("分身召唤");
        
        // 检查技能是否可用
        btnSkill1.setEnabled(playerUnits[0].canUseSkill(0));
        btnSkill2.setEnabled(playerUnits[0].canUseSkill(1));
        btnSkill3.setEnabled(playerUnits[0].canUseSkill(2));
        btnSkill4.setEnabled(playerUnits[0].canUseSkill(3) && hasEmptyPlayerSlot());
        
        // 技能选择事件
        btnSkill1.setOnClickListener(v -> {
            executePlayerSkill(0);
            dialog.dismiss();
        });
        
        btnSkill2.setOnClickListener(v -> {
            executePlayerSkill(1);
            dialog.dismiss();
        });
        
        btnSkill3.setOnClickListener(v -> {
            executePlayerSkill(2);
            dialog.dismiss();
        });
        
        btnSkill4.setOnClickListener(v -> {
            executeSummonSkill();
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 执行玩家技能
     */
    private void executePlayerSkill(int skillIndex) {
        if (!autoBattleMode && playerUnits[0] != null && playerUnits[0].isAlive() && 
            playerUnits[0].canUseSkill(skillIndex) && enemyUnits[0] != null && enemyUnits[0].isAlive()) {
            
            // 选择目标（治疗技能选择玩家，攻击技能选择敌人）
            int targetIndex = 0;
            BattleUnit target = (skillIndex == 1) ? playerUnits[0] : enemyUnits[0];
            
            String[] skillNames = {"重击", "治疗", "暴风斩", "分身召唤"};
            addBattleLog(String.format("回合 %d: %s 使用 %s", 
                currentRound, playerUnits[0].getName(), skillNames[skillIndex]));
            
            // 执行技能
            BattleUnitAction action = new BattleUnitAction(BattleUnitAction.ACTION_SKILL, skillIndex, targetIndex);
            executeAction(playerUnits[0], action, true);
            
            // 更新冷却
            playerUnits[0].updateCooldowns();
            
            // 更新UI
            updateUI();
            
            // 检查战斗结果
            if (checkBattleResult()) return;
            
            // 敌方反击（如果敌方存活）
            if (enemyUnits[0].isAlive() && enemyUnits[0].isReadyToAttack()) {
                handler.postDelayed(() -> {
                    BattleUnitAction enemyAction = new BattleUnitAction(BattleUnitAction.ACTION_ATTACK, -1, 0);
                    executeAction(enemyUnits[0], enemyAction, false);
                    enemyUnits[0].updateCooldowns();
                    updateUI();
                    checkBattleResult();
                }, 1000);
            }
        } else if (!autoBattleMode) {
            addBattleLog("技能不可用或战斗已结束");
        }
    }
    
    /**
     * 执行召唤技能
     */
    private void executeSummonSkill() {
        if (!autoBattleMode && playerUnits[0] != null && playerUnits[0].isAlive() && 
            playerUnits[0].canUseSkill(SUMMON_SKILL_INDEX) && hasEmptyPlayerSlot()) {
            
            // 召唤分身
            int summonSlot = findEmptyPlayerSlot();
            if (summonSlot != -1) {
                // 创建分身单位
                playerUnits[summonSlot] = createSummonUnit(playerUnits[0], SUMMON_DURATION);
                playerUnits[summonSlot].resetCooldowns();
                
                // 使用技能
                playerUnits[0].useSkill(SUMMON_SKILL_INDEX);
                summonsUsed++;
                
                addBattleLog(String.format("回合 %d: %s 召唤了分身到位置%d", 
                    currentRound, playerUnits[0].getName(), summonSlot + 1));
                
                // 更新UI
                updateUI();
                
                // 敌方回合（如果敌方存活）
                if (enemyUnits[0].isAlive() && enemyUnits[0].isReadyToAttack()) {
                    handler.postDelayed(() -> {
                        BattleUnitAction enemyAction = new BattleUnitAction(BattleUnitAction.ACTION_ATTACK, -1, 0);
                        executeAction(enemyUnits[0], enemyAction, false);
                        enemyUnits[0].updateCooldowns();
                        updateUI();
                        checkBattleResult();
                    }, 1000);
                }
            }
        } else if (!autoBattleMode) {
            addBattleLog("召唤技能不可用或没有空位");
        }
    }
    
    /**
     * 检查是否有空玩家槽位
     */
    private boolean hasEmptyPlayerSlot() {
        for (int i = 1; i < 3; i++) {
            if (playerUnits[i] == null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 找到空玩家槽位
     */
    private int findEmptyPlayerSlot() {
        for (int i = 1; i < 3; i++) {
            if (playerUnits[i] == null) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 创建召唤单位
     */
    private BattleUnit createSummonUnit(BattleUnit master, int duration) {
        // 分身继承主单位的部分属性
        BattleUnit summon = new BattleUnit(
            master.getName() + "的分身",
            master.getMaxHealth() / 2, // 分身生命值减半
            master.getAttack() / 2,     // 分身攻击力减半
            master.getDefense(),        // 分身防御力不变
            10,                        // 默认速度
            new BattleSkill[0],         // 分身没有技能
            BattleUnit.TYPE_SUMMON
        );
        
        // 设置召唤持续时间
        summon.setSummonDuration(duration);
        summon.setMaster(master);
        
        return summon;
    }
    
    /**
     * 更新召唤单位持续时间
     */
    private void updateSummonDuration() {
        for (int i = 1; i < 3; i++) {
            if (playerUnits[i] != null && playerUnits[i].getUnitType() == BattleUnit.TYPE_SUMMON) {
                int duration = playerUnits[i].getSummonDuration();
                if (duration > 0) {
                    playerUnits[i].setSummonDuration(duration - 1);
                    if (duration - 1 <= 0) {
                        addBattleLog(String.format("回合 %d: %s 的分身消失", 
                            currentRound, playerUnits[i].getName()));
                        playerUnits[i] = null;
                    }
                }
            }
        }
    }
    
    /**
     * 结束玩家回合
     */
    private void endPlayerTurn() {
        if (!autoBattleMode) {
            // 切换战斗模式（下一回合生效）
            autoBattleMode = isAutoBattle;
            
            // 增加回合数
            currentRound++;
            
            // 更新召唤持续时间
            updateSummonDuration();
            
            // 更新UI
            updateUI();
            
            addBattleLog("--- 回合结束 ---");
            
            // 如果是自动模式，开始自动战斗
            if (autoBattleMode) {
                startAutoBattle();
            }
        }
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean canAct = (playerUnits[0] != null && playerUnits[0].isAlive() && 
                         enemyUnits[0] != null && enemyUnits[0].isAlive());
        
        btnAttack.setEnabled(canAct && !autoBattleMode);
        btnSkill.setEnabled(canAct && !autoBattleMode);
        btnEndTurn.setEnabled(canAct && !autoBattleMode);
        
        // 根据自动战斗模式设置按钮背景色
        if (autoBattleMode) {
            btnAttack.setAlpha(0.5f);
            btnSkill.setAlpha(0.5f);
            btnEndTurn.setAlpha(0.5f);
        } else {
            btnAttack.setAlpha(1.0f);
            btnSkill.setAlpha(1.0f);
            btnEndTurn.setAlpha(1.0f);
        }
    }
    
    /**
     * 执行自动战斗回合
     */
    private void executeAutoBattleRound() {
        addBattleLog(String.format("[第%d回合]%s优先行动", currentRound, getPriorityUnitName()));
        
        // 玩家回合
        if (playerUnits[0] != null && playerUnits[0].isAlive() && playerUnits[0].isReadyToAttack()) {
            BattleUnitAction action = new BattleUnitAction(BattleUnitAction.ACTION_ATTACK, -1, 0);
            executeAction(playerUnits[0], action, false);
            playerUnits[0].updateCooldowns();
        }
        
        // 更新召唤持续时间
        updateSummonDuration();
        
        // 检查战斗结果
        if (!checkBattleResult()) {
            // 敌方回合
            if (enemyUnits[0] != null && enemyUnits[0].isAlive() && enemyUnits[0].isReadyToAttack()) {
                BattleUnitAction enemyAction = new BattleUnitAction(BattleUnitAction.ACTION_ATTACK, -1, 0);
                executeAction(enemyUnits[0], enemyAction, false);
                enemyUnits[0].updateCooldowns();
            }
            
            // 再次检查战斗结果
            checkBattleResult();
        }
        
        // 增加回合数
        currentRound++;
        
        // 更新UI
        updateUI();
    }
    
    /**
     * 获取优先行动的单位名称
     */
    private String getPriorityUnitName() {
        // 根据速度决定优先行动的单位
        if (playerUnits[0] != null && playerUnits[0].getStats().getSpeed() >= enemyUnits[0].getStats().getSpeed()) {
            return playerUnits[0].getName();
        } else {
            return enemyUnits[0].getName();
        }
    }
    
    /**
     * 执行行动
     */
    private void executeAction(BattleUnit unit, BattleUnitAction action, boolean showToast) {
        BattleUnit target = (action.getTargetType() == BattleUnitAction.TARGET_ENEMY) ? enemyUnits[0] : playerUnits[0];
        
        // 检查目标是否存活
        if (target == null || !target.isAlive()) {
            addBattleLog("目标已经死亡，无法行动");
            return;
        }
        
        if (action.getActionType() == BattleUnitAction.ACTION_ATTACK) {
            // 普通攻击
            int damage = unit.getAttack() - target.getDefense();
            if (damage < 1) damage = 1;
            
            int oldHealth = target.getCurrentHealth();
            target.takeDamage(damage);
            totalDamageDealt += damage;
            int newHealth = target.getCurrentHealth();
            
            String logMessage = String.format("%s对%s使用攻击/造成%d点伤害，生命值变化%d→%d", 
                unit.getName(), target.getName(), damage, oldHealth, newHealth);
            addBattleLog(logMessage);
            
            // 检查目标是否死亡
            if (!target.isAlive()) {
                if (target == enemyUnits[0]) {
                    enemiesKilled++;
                    String deathMessage = String.format("%s被击败！", target.getName());
                    addBattleLog(deathMessage);
                }
            }
            
        } else if (action.getActionType() == BattleUnitAction.ACTION_SKILL) {
            // 技能攻击
            int skillIndex = action.getSkillIndex();
            skillsUsed++;
            
            if (skillIndex == 1) {
                // 治疗技能（技能索引1）
                int healAmount = unit.getAttack() * 2; // 治疗量为攻击力的2倍
                int oldHealth = target.getCurrentHealth();
                target.heal(healAmount);
                totalHealing += healAmount;
                int newHealth = target.getCurrentHealth();
                
                String logMessage = String.format("%s对%s使用治疗技能/恢复%d点生命值，生命值变化%d→%d", 
                    unit.getName(), target.getName(), healAmount, oldHealth, newHealth);
                addBattleLog(logMessage);
                
            } else if (skillIndex == 3) {
                // 分身召唤技能
                String logMessage = String.format("%s使用分身召唤技能", unit.getName());
                addBattleLog(logMessage);
                
            } else {
                // 普通攻击技能
                int skillDamage = unit.getSkillDamage(skillIndex);
                int damage = skillDamage - target.getDefense();
                if (damage < 1) damage = 1;
                
                int oldHealth = target.getCurrentHealth();
                target.takeDamage(damage);
                totalDamageDealt += damage;
                int newHealth = target.getCurrentHealth();
                
                String[] skillNames = {"重击", "治疗", "暴风斩", "分身召唤"};
                String skillName = skillIndex < skillNames.length ? skillNames[skillIndex] : "技能";
                
                String logMessage = String.format("%s对%s使用%s/造成%d点伤害，生命值变化%d→%d", 
                    unit.getName(), target.getName(), skillName, damage, oldHealth, newHealth);
                addBattleLog(logMessage);
                
                // 检查目标是否死亡
                if (!target.isAlive() && target == enemyUnits[0]) {
                    enemiesKilled++;
                    String deathMessage = String.format("%s被技能击败！", target.getName());
                    addBattleLog(deathMessage);
                }
            }
        }
    }
    
    /**
     * 检查战斗结果
     */
    private boolean checkBattleResult() {
        boolean playerAlive = false;
        boolean enemyAlive = false;
        
        // 检查玩家是否存活
        for (int i = 0; i < 3; i++) {
            if (playerUnits[i] != null && playerUnits[i].isAlive()) {
                playerAlive = true;
                break;
            }
        }
        
        // 检查敌方是否存活
        for (int i = 0; i < 3; i++) {
            if (enemyUnits[i] != null && enemyUnits[i].isAlive()) {
                enemyAlive = true;
                break;
            }
        }
        
        if (!playerAlive) {
            // 玩家失败
            addBattleLog("战斗结束 - 玩家失败");
            saveBattleStatistics();
            showDefeatAnimation();
//            showBattleLogDialog(false); // 显示失败时的战斗日志弹窗
            return true;
        } else if (!enemyAlive) {
            // 玩家胜利
            addBattleLog("战斗结束 - 玩家胜利");
            saveBattleStatistics();
            showVictoryAnimation();
//            showBattleLogDialog(true); // 显示胜利时的战斗日志弹窗
            return true;
        }
        
        return false;
    }
    
    /**
     * 更新Switch文字颜色
     */
    private void updateSwitchTextColors() {
        if (tvManualText != null && tvAutoText != null) {
            if (autoBattleMode) {
                // 自动模式选中 - 橙色
                tvManualText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tvAutoText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                // 手动模式选中 - 橙色
                tvManualText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                tvAutoText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }

    /**
     * 更新UI
     */
    private void updateUI() {
        // 更新回合信息
        String battleStats = String.format("回合: %d | 自动: %s | 伤害: %d | 治疗: %d | 召唤: %d", 
            currentRound, autoBattleMode ? "开启" : "关闭", totalDamageDealt, totalHealing, summonsUsed);
        tvBattleInfo.setText(battleStats);
        
        // 更新所有单位UI
        for (int i = 0; i < 3; i++) {
            // 更新玩家单位UI
            if (playerUnits[i] != null && playerName[i] != null) {
                playerName[i].setText(playerUnits[i].getName());
                playerHealth[i].setProgress(playerUnits[i].getHealthPercent());
                playerAttackProgress[i].setProgress(playerUnits[i].getAttackCooldownPercent());
                
                // 更新技能冷却
                for (int j = 0; j < 2; j++) {
                    if (playerSkillProgress[i][j] != null) {
                        playerSkillProgress[i][j].setProgress(playerUnits[i].getSkillCooldownPercent(j));
                    }
                }
            } else if (playerName[i] != null) {
                playerName[i].setText("空位");
                playerHealth[i].setProgress(0);
                playerAttackProgress[i].setProgress(0);
                
                for (int j = 0; j < 2; j++) {
                    if (playerSkillProgress[i][j] != null) {
                        playerSkillProgress[i][j].setProgress(0);
                    }
                }
            }
            
            // 更新敌方单位UI
            if (enemyUnits[i] != null && enemyName[i] != null) {
                enemyName[i].setText(enemyUnits[i].getName());
                enemyHealth[i].setProgress(enemyUnits[i].getHealthPercent());
                enemyAttackProgress[i].setProgress(enemyUnits[i].getAttackCooldownPercent());
                
                // 更新技能冷却
                for (int j = 0; j < 2; j++) {
                    if (enemySkillProgress[i][j] != null) {
                        enemySkillProgress[i][j].setProgress(enemyUnits[i].getSkillCooldownPercent(j));
                    }
                }
            } else if (enemyName[i] != null) {
                enemyName[i].setText("空位");
                enemyHealth[i].setProgress(0);
                enemyAttackProgress[i].setProgress(0);
                
                for (int j = 0; j < 2; j++) {
                    if (enemySkillProgress[i][j] != null) {
                        enemySkillProgress[i][j].setProgress(0);
                    }
                }
            }
        }
        
        // 更新按钮状态
        updateButtonStates();
        
        // 更新Switch文字颜色
        updateSwitchTextColors();
        
        // 更新战斗日志显示
        updateBattleLogDisplay();
    }
    
    /**
     * 添加战斗日志
     */
    private void addBattleLog(String message) {
        battleLog.add(message);
        // 只保留最近的50条日志
        if (battleLog.size() > 50) {
            battleLog.remove(0);
        }
        updateBattleLogDisplay();
    }
    
    /**
     * 更新战斗日志显示
     */
    private void updateBattleLogDisplay() {
        StringBuilder logText = new StringBuilder();
        for (String log : battleLog) {
            logText.append(log).append("\n");
        }
        battleLogText.setText(logText.toString());
        
        // 自动滚动到底部
        battleLogScroll.post(() -> battleLogScroll.fullScroll(View.FOCUS_DOWN));
    }
    
    /**
     * 保存战斗统计
     */
    private void saveBattleStatistics() {
        long battleDuration = (System.currentTimeMillis() - battleStartTime) / 1000;
        
        // 创建战斗结果对象
        BattleResult result = new BattleResult(
            userId,
            (enemyUnits[0] != null && !enemyUnits[0].isAlive()), // 是否胜利
            totalDamageDealt / 10, // 金币奖励
            totalDamageDealt / 2,  // 经验奖励
            enemiesKilled,
            isBossBattle,
            currentRound,
            false // 敌人是否逃跑
        );
        
        // 保存到结果管理器
        if (resultManager != null) {
            resultManager.saveBattleResult(result);
        }
        
        // 显示详细统计
        showBattleSummary();
    }
    
    /**
     * 显示战斗总结
     */
    private void showBattleSummary() {
        long battleDuration = (System.currentTimeMillis() - battleStartTime) / 1000;
        
        String summary = String.format("战斗总结:\n回合数: %d\n战斗时间: %d秒\n总伤害: %d\n治疗量: %d\n技能使用: %d次\n召唤次数: %d次",
            currentRound, battleDuration, totalDamageDealt, totalHealing, skillsUsed, summonsUsed);
        
        addBattleLog(summary);
    }
    
    /**
     * 显示胜利动画
     */
    private void showVictoryAnimation() {
        addBattleLog("战斗胜利！");
        
        // 延迟后返回基地页面
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, BaseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("show_battle_log", true);
            intent.putExtra("battle_result", "胜利");
            intent.putExtra("battle_rounds", currentRound);
            intent.putExtra("battle_damage", totalDamageDealt);
            intent.putExtra("battle_kills", enemiesKilled);
            intent.putExtra("battle_healing", totalHealing);
            intent.putExtra("battle_skills_used", skillsUsed);
            intent.putExtra("battle_summons_used", summonsUsed);
            intent.putExtra("battle_log_data", battleLog.toArray(new String[0]));
            startActivity(intent);
            finish();
        }, 2000);
    }
    
    /**
     * 显示失败动画
     */
    private void showDefeatAnimation() {
        addBattleLog("战斗失败");
        
        // 延迟后返回基地页面
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, BaseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("show_battle_log", true);
            intent.putExtra("battle_result", "失败");
            intent.putExtra("battle_rounds", currentRound);
            intent.putExtra("battle_damage", totalDamageDealt);
            intent.putExtra("battle_kills", enemiesKilled);
            intent.putExtra("battle_healing", totalHealing);
            intent.putExtra("battle_skills_used", skillsUsed);
            intent.putExtra("battle_summons_used", summonsUsed);
            intent.putExtra("battle_log_data", battleLog.toArray(new String[0]));
            startActivity(intent);
            finish();
        }, 2000);
    }
    
    /**
     * 处理战斗结束（用于BattleSkillManager调用）
     */
    public void handleBattleEnded(boolean playerWon) {
        if (playerWon) {
            showVictoryAnimation();
        } else {
            showDefeatAnimation();
        }
    }
    
    /**
     * 添加总伤害（用于BattleSkillManager调用）
     */
    public void addTotalDamageDealt(int damage) {
        totalDamageDealt += damage;
    }
    
    /**
     * 获取单位属性字符串
     */
    private String getUnitAttributesString(BattleUnit unit) {
        if (unit == null) {
            return "单位不存在";
        }
        
        return String.format("%s - 生命: %d/%d | 攻击: %d | 防御: %d | 速度: %d", 
            unit.getName(), 
            unit.getCurrentHealth(), 
            unit.getMaxHealth(), 
            unit.getAttack(), 
            unit.getDefense(), 
            unit.getStats().getSpeed()
        );
    }
    
    /**
     * 记录单位属性变化
     */
    private void logAttributeChange(BattleUnit unit, String changeType, int oldValue, int newValue) {
        if (oldValue != newValue) {
            String message = String.format("回合 %d: %s %s变化 %d → %d", 
                currentRound, unit.getName(), changeType, oldValue, newValue);
            addBattleLog(message);
        }
    }
    
    /*
     * 显示战斗日志弹窗 - 已注释掉，战斗日志将在基地页面显示
     * @param isVictory 是否胜利
     */
    /*
    private void showBattleLogDialog(boolean isVictory) {
        // 延迟1秒后显示弹窗，让动画先播放
        handler.postDelayed(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(BattleActivity.this);

            // 设置弹窗标题
            String title = isVictory ? "战斗胜利 - 对战日志" : "战斗失败 - 对战日志";
            builder.setTitle(title);

            // 构建完整的战斗日志文本
            StringBuilder fullLog = new StringBuilder();
            fullLog.append("=== 本场对战完整日志 ===\n\n");

            // 添加战斗统计信息
            long battleDuration = (System.currentTimeMillis() - battleStartTime) / 1000;
            fullLog.append("战斗统计:\n");
            fullLog.append("• 结果: ").append(isVictory ? "胜利" : "失败").append("\n");
            fullLog.append("• 回合数: ").append(currentRound).append("\n");
            fullLog.append("• 战斗时长: ").append(battleDuration).append("秒\n");
            fullLog.append("• 总伤害: ").append(totalDamageDealt).append("\n");
            fullLog.append("• 承受伤害: ").append(totalDamageTaken).append("\n");
            fullLog.append("• 治疗量: ").append(totalHealing).append("\n");
            fullLog.append("• 击败敌人: ").append(enemiesKilled).append("\n");
            fullLog.append("• 使用技能: ").append(skillsUsed).append("\n");
            fullLog.append("• 召唤次数: ").append(summonsUsed).append("\n\n");

            // 添加战斗过程日志
            fullLog.append("战斗过程:\n");

            // 设置弹窗内容
            builder.setMessage(fullLog.toString());

            // 添加关闭按钮
            builder.setPositiveButton("关闭", (dialog, which) -> dialog.dismiss());

            // 显示弹窗
            builder.create().show();
        }, 1000); // 延迟1秒
    }
    */
    
    /**
     * 分享战斗日志
     */
    private void shareBattleLog(String logText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "战斗日志分享");
        shareIntent.putExtra(Intent.EXTRA_TEXT, logText);
        
        try {
            startActivity(Intent.createChooser(shareIntent, "分享战斗日志"));
        } catch (Exception e) {
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }
}