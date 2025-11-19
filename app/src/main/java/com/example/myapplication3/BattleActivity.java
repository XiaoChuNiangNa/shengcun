package com.example.myapplication3;

import com.example.myapplication3.BattleSkill;
import com.example.myapplication3.adapter.BattleCardAdapter;
import com.example.myapplication3.utils.BattleUIManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;



/**
 * 对战页面 - 实现3v3布局中的1v1对战，支持召唤技能和自动/手动切换
 * 支持野外对战模式
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

    // 新的动态卡牌系统
    private BattleCardAdapter cardAdapter;
    private BattleUIManager uiManager;
    private LinearLayout playerContainer;
    private LinearLayout enemyContainer;

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
    private LevelExperienceManager levelExpManager;
    private int userId;
    private boolean isBossBattle = false;

    // 野外对战相关
    private boolean isWildEncounter = false; // 是否为野外遭遇战
    private Monster wildAnimal; // 野生动物
    private String terrainType; // 地形类型
    private int originalX, originalY; // 原始位置

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
        levelExpManager = new LevelExperienceManager(this);
        levelExpManager = new LevelExperienceManager(this);

        // 检查是否为野外遭遇战
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("wild_encounter", false)) {
            isWildEncounter = true;
            terrainType = intent.getStringExtra("terrain_type");
            originalX = intent.getIntExtra("original_x", 0);
            originalY = intent.getIntExtra("original_y", 0);

            // 获取野生动物数据
            String animalName = intent.getStringExtra("animal_name");
            if (animalName != null) {
                wildAnimal = MonsterManager.getMonsterByName(animalName);
            }
        }

        // 初始化战斗统计
        battleStartTime = System.currentTimeMillis();

        initViews();
        initBattleUnits();
        updateUI();
        setupListeners();

        // 根据对战类型设置不同的战斗标题
        if (isWildEncounter) {
            addBattleLog("战斗开始 - 野外遭遇（" + terrainType + "）");
            tvMapTitle.setText("野外遭遇 - " + terrainType);
        } else {
            addBattleLog("战斗开始 - 暗影森林");
            tvMapTitle.setText("暗影森林");
        }

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

        // 初始化新的动态卡牌系统
        playerContainer = findViewById(R.id.player_units_row);
        enemyContainer = findViewById(R.id.enemy_units_row);
        
        // 创建卡牌适配器
        cardAdapter = new BattleCardAdapter(this, playerContainer, enemyContainer);
        
        // 创建UI管理器
        uiManager = new BattleUIManager(cardAdapter, tvBattleInfo, tvResources, battleLogText, battleLogScroll);
    }

    /**
     * 初始化战斗单位（支持野外对战模式）
     */
    private void initBattleUnits() {
        // 重置所有单位
        for (int i = 0; i < 3; i++) {
            playerUnits[i] = null;
            enemyUnits[i] = null;
        }

        // 初始化1号位和3号位为隐藏状态，2号位保持默认状态
        int[] sideSlots = {0, 2}; // 1号位和3号位（索引0和2）
        for (int i : sideSlots) {
            int playerCardId = getResources().getIdentifier("player_card_" + (i + 1), "id", getPackageName());
            int enemyCardId = getResources().getIdentifier("enemy_card_" + (i + 1), "id", getPackageName());
            
            View playerCard = findViewById(playerCardId);
            View enemyCard = findViewById(enemyCardId);
            
            if (playerCard != null) {
                playerCard.setVisibility(View.GONE);
            }
            if (enemyCard != null) {
                enemyCard.setVisibility(View.GONE);
            }
        }

        // 创建主玩家单位（位置2，即索引1）
        playerUnits[1] = MonsterManager.createPlayer();
        playerUnits[1].resetCooldowns();

        // 创建敌方单位（位置2，即索引1）
        if (isWildEncounter && wildAnimal != null) {
            // 野外遭遇战：使用指定的野生动物
            enemyUnits[1] = MonsterManager.convertToBattleUnit(wildAnimal);
            
            // 为野外动物创建动态技能
            String animalName = wildAnimal.getName();
            BattleSkill[] animalSkills = com.example.myapplication3.factory.SkillFactory.createEnemySkills(animalName);
            enemyUnits[1].setSkills(animalSkills);
            
            addBattleLog("遭遇野生动物：" + wildAnimal.getName());
            int skillCount = com.example.myapplication3.factory.SkillFactory.getValidSkillCount(animalSkills);
            addBattleLog("野生动物拥有 " + skillCount + " 个技能");
        } else {
            // 普通对战：随机敌人
            Monster monster = MonsterManager.getRandomMonster();
            if (monster != null) {
                enemyUnits[1] = MonsterManager.convertToBattleUnit(monster);
                
                // 为怪物创建动态技能
                String enemyName = monster.getName();
                BattleSkill[] enemySkills = com.example.myapplication3.factory.SkillFactory.createEnemySkills(enemyName);
                enemyUnits[1].setSkills(enemySkills);
                
                int skillCount = com.example.myapplication3.factory.SkillFactory.getValidSkillCount(enemySkills);
                addBattleLog("敌人拥有 " + skillCount + " 个技能");
            } else {
                // 如果没有怪物，使用默认敌人
                BattleSkill[] defaultSkills = new BattleSkill[]{
                    BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1)
                };
                enemyUnits[1] = new BattleUnit("森林守护者", 100, 8, 5, 80,
                    defaultSkills, BattleUnit.TYPE_ENEMY);
            }
        }
        enemyUnits[1].resetCooldowns();

        // 初始化技能
        initPlayerSkills();

        // 记录对战开始时的属性状态
        addBattleLog("[开始战斗]");
        addBattleLog("玩家单位属性:");
        addBattleLog(getUnitAttributesString(playerUnits[1]));
        addBattleLog("敌方单位属性:");
        addBattleLog(getUnitAttributesString(enemyUnits[1]));
        
        // 初始化进度条上限和显示
        if (cardAdapter != null) {
            cardAdapter.updateAllSkillProgress();
        }
    }

    /**
     * 初始化技能 - 使用工厂模式和配置管理系统
     */
    private void initPlayerSkills() {
        // 给玩家单位添加技能
        if (playerUnits[1] != null) {
            // 使用技能工厂动态创建技能
            int playerLevel = getCurrentPlayerLevel();
            BattleSkill[] skills = com.example.myapplication3.factory.SkillFactory.createPlayerSkills(playerLevel);
            
            // 验证技能数组
            if (!com.example.myapplication3.factory.SkillFactory.validateSkillArray(skills)) {
                addBattleLog("警告：技能创建失败，使用默认技能");
                skills = new BattleSkill[]{
                    BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1)
                };
            }
            
            playerUnits[1].setSkills(skills);
            
            // 记录技能信息
            int skillCount = com.example.myapplication3.factory.SkillFactory.getValidSkillCount(skills);
            addBattleLog("玩家技能已加载（共 " + skillCount + " 个）：");
            
            for (BattleSkill skill : skills) {
                if (skill != null) {
                    String description = com.example.myapplication3.config.SkillConfig.getSkillDescription(skill.skillType);
                    addBattleLog("- " + skill.getName() + " (冷却：" + skill.getCooldown() + "回合) - " + description);
                }
            }
        }
    }
    
    /**
     * 获取当前玩家等级（临时实现，应从数据库获取）
     */
    private int getCurrentPlayerLevel() {
        // 临时返回固定等级，实际应该从用户数据或数据库获取
        return 5; // 假设玩家等级5，对应战斗配置
    }

    /**
     * 设置监听器
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        switchAutoBattle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            autoBattleMode = isChecked;
            updateManualAutoText();

            if (autoBattleMode && !battleEnded) {
                startAutoBattle();
            }
        });

        btnAttack.setOnClickListener(v -> {
            if (!battleEnded && !autoBattleMode) {
                performPlayerAttack();
            }
        });

        btnSkill.setOnClickListener(v -> {
            if (!battleEnded && !autoBattleMode) {
                showSkillSelectionDialog();
            }
        });

        btnEndTurn.setOnClickListener(v -> {
            if (!battleEnded && !autoBattleMode) {
                endPlayerTurn();
            }
        });
    }

    /**
     * 更新UI显示
     */
    private void updateUI() {
        updateManualAutoText();
        
        // 根据自动/手动模式显示或隐藏底部按钮
        if (autoBattleMode) {
            // 自动模式下隐藏底部按钮
            btnAttack.setVisibility(View.GONE);
            btnSkill.setVisibility(View.GONE);
            btnEndTurn.setVisibility(View.GONE);
        } else {
            // 手动模式下显示底部按钮
            btnAttack.setVisibility(View.VISIBLE);
            btnSkill.setVisibility(View.VISIBLE);
            btnEndTurn.setVisibility(View.VISIBLE);
        }

        // 使用新的适配器系统更新所有卡牌
        if (cardAdapter != null) {
            // 更新所有玩家卡牌
            cardAdapter.updatePlayerUnits(playerUnits);
            
            // 更新所有敌方卡牌
            cardAdapter.updateEnemyUnits(enemyUnits);
            
            // 更新所有技能和攻击进度条
            cardAdapter.updateAllSkillProgress();
        }

        // 更新资源显示
        updateResourceDisplay();
    }

    /**
     * 更新资源显示
     */
    private void updateResourceDisplay() {
        // 更新回合信息
        tvBattleInfo.setText(String.format("第 %d 回合", currentRound));
        
        // 显示等级和进度信息（外置系统）
        if (levelExpManager != null) {
            String levelInfo = levelExpManager.getLevelInfo();
            tvResources.setText(String.format("金币: %d  %s", playerGold, levelInfo));
        } else {
            tvResources.setText(String.format("金币: %d  经验: %d", playerGold, playerExp));
        }
    }

    /**
     * 更新手动/自动文字显示
     */
    private void updateManualAutoText() {
        if (autoBattleMode) {
            tvAutoText.setVisibility(View.VISIBLE);
            tvManualText.setVisibility(View.GONE);
        } else {
            tvAutoText.setVisibility(View.GONE);
            tvManualText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 开始自动战斗
     */
    private void startAutoBattle() {
        if (battleEnded) return;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!autoBattleMode || battleEnded) return;

                performAutoBattleTurn();

                if (!battleEnded) {
                    handler.postDelayed(this, 1500); // 1.5秒后执行下一回合
                }
            }
        }, 1000); // 1秒后开始第一回合
    }

    /**
     * 执行自动战斗回合
     */
    private void performAutoBattleTurn() {
        // 回合开始：处理效果和冷却
        processRoundStart();
        
        // 玩家自动行动
        performPlayerAutoAction();

        // 检查战斗是否结束
        if (checkBattleEnd()) return;

        // 敌方自动行动
        performEnemyAutoAction();

        // 检查战斗是否结束
        if (checkBattleEnd()) return;

        // 增加回合数
        currentRound++;
        updateUI();
    }

    /**
     * 玩家自动行动
     */
    private void performPlayerAutoAction() {
        if (playerUnits[1] == null || playerUnits[1].getCurrentHealth() <= 0) return;

        BattleSkill[] skills = playerUnits[1].getSkills();
        if (skills == null) {
            performPlayerAttack();
            return;
        }

        boolean actionPerformed = false;

        // 优先检查召唤技能是否可用
        if (skills.length > SUMMON_SKILL_INDEX && skills[SUMMON_SKILL_INDEX] != null &&
            skills[SUMMON_SKILL_INDEX].getCurrentCooldown() <= 0) {
            useSummonSkill(playerUnits[1]);
            actionPerformed = true;
            addBattleLog("[自动] 使用召唤技能");
        }

        // 如果没有使用召唤技能，检查其他技能是否可用
        if (!actionPerformed) {
            // 随机选择一个可用技能
            List<Integer> availableSkills = new ArrayList<>();
            for (int i = 0; i < skills.length; i++) {
                if (i != SUMMON_SKILL_INDEX && skills[i] != null && skills[i].getCurrentCooldown() <= 0) {
                    availableSkills.add(i);
                }
            }
            
            if (!availableSkills.isEmpty()) {
                int selectedSkillIndex = availableSkills.get(random.nextInt(availableSkills.size()));
                usePlayerSkill(selectedSkillIndex);
                actionPerformed = true;
                addBattleLog("[自动] 使用技能: " + skills[selectedSkillIndex].getName());
            }
        }

        // 如果没有可用技能，使用普通攻击
        if (!actionPerformed) {
            performPlayerAttack();
            addBattleLog("[自动] 使用普通攻击");
        }
    }

    /**
     * 显示技能选择对话框
     */
    private void showSkillSelectionDialog() {
        if (playerUnits[1] == null) return;
        
        BattleSkill[] skills = playerUnits[1].getSkills();
        if (skills == null) return;
        
        // 创建可用技能列表
        List<String> skillOptions = new ArrayList<>();
        final List<Integer> skillIndices = new ArrayList<>();
        
        // 优先添加召唤技能
        if (skills.length > SUMMON_SKILL_INDEX && skills[SUMMON_SKILL_INDEX] != null) {
            BattleSkill summonSkill = skills[SUMMON_SKILL_INDEX];
            String status = summonSkill.getCurrentCooldown() <= 0 ? "可用" : "冷却(" + summonSkill.getCurrentCooldown() + ")";
            skillOptions.add(summonSkill.getName() + " - " + status);
            skillIndices.add(SUMMON_SKILL_INDEX);
        }
        
        // 添加其他技能（跳过召唤技能）
        for (int i = 0; i < skills.length; i++) {
            if (i == SUMMON_SKILL_INDEX) continue;
            
            if (skills[i] != null) {
                String status = skills[i].getCurrentCooldown() <= 0 ? "可用" : "冷却(" + skills[i].getCurrentCooldown() + ")";
                skillOptions.add(skills[i].getName() + " - " + status);
                skillIndices.add(i);
            }
        }
        
        if (skillOptions.isEmpty()) {
            Toast.makeText(this, "没有可用技能", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查Activity是否还在运行
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        try {
            // 显示技能选择对话框
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("选择技能");
            builder.setItems(skillOptions.toArray(new String[0]), (dialog, which) -> {
                int skillIndex = skillIndices.get(which);
                usePlayerSkill(skillIndex);
            });
            
            builder.setNegativeButton("取消", null);
            builder.show();
        } catch (Exception e) {
            // 如果显示对话框失败，不执行任何操作
        }
    }

    /**
     * 使用玩家技能 - 使用新技能系统
     */
    private void usePlayerSkill(int skillIndex) {
        if (playerUnits[1] == null || enemyUnits[1] == null) return;

        BattleSkill[] skills = playerUnits[1].getSkills();
        if (skills == null || skillIndex < 0 || skillIndex >= skills.length) return;

        BattleSkill skill = skills[skillIndex];
        if (skill.getCurrentCooldown() > 0) {
            Toast.makeText(this, skill.getName() + " 技能冷却中", Toast.LENGTH_SHORT).show();
            return;
        }

        // 处理特殊技能（召唤技能）
        if (skillIndex == SUMMON_SKILL_INDEX) {
            useSummonSkill(playerUnits[1]);
            return;
        }

        // 使用新技能系统的执行逻辑
        String skillResult = BattleSkillManager.executeSkill(
            skill.getSkillType(), skill.getLevel(), playerUnits[1], enemyUnits[1], this);

        // 记录技能使用
        skillsUsed++;

        // 设置技能冷却
        skill.resetCooldown();

        // 添加战斗日志
        addBattleLog(skillResult);

        updateUI();

        // 检查是否击杀敌人
        if (enemyUnits[1].getCurrentHealth() <= 0) {
            handleEnemyDefeated();
        }
    }

    /**
     * 使用召唤技能
     */
    private void useSummonSkill(BattleUnit caster) {
        // 查找空位放置召唤单位（优先使用1、3号位）
        int emptySlot = -1;
        int[] preferredSlots = {0, 2}; // 优先使用1号和3号位
        
        for (int slot : preferredSlots) {
            if (playerUnits[slot] == null) {
                emptySlot = slot;
                break;
            }
        }
        
        // 如果没有1、3号位，使用其他空位
        if (emptySlot == -1) {
            for (int i = 0; i < 3; i++) {
                if (playerUnits[i] == null) {
                    emptySlot = i;
                    break;
                }
            }
        }

        if (emptySlot == -1) {
            Toast.makeText(this, "没有空位可以召唤", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取召唤技能等级来决定分身属性
        BattleSkill[] skills = caster.getSkills();
        int skillLevel = 1; // 默认等级
        if (skills != null && skills.length > SUMMON_SKILL_INDEX && skills[SUMMON_SKILL_INDEX] != null) {
            skillLevel = skills[SUMMON_SKILL_INDEX].getLevel();
        }

        // 根据技能等级计算分身生命值比例
        double healthRatio = 0.5; // Lv1: 50%
        if (skillLevel == 2) healthRatio = 0.75; // Lv2: 75%
        else if (skillLevel == 3) healthRatio = 1.0; // Lv3: 100%

        // 创建召唤单位
        // 为分身添加基础攻击技能
        BattleSkill basicAttackSkill = new BattleSkill("攻击", 0, 0); // 无冷却的攻击技能
        BattleSkill[] summonSkills = new BattleSkill[]{basicAttackSkill};
        
        BattleUnit summonedUnit = new BattleUnit(
            "分身",                    // 改名为"分身"
            (int)(caster.getMaxHealth() * healthRatio), // 根据等级调整生命值
            caster.getAttack(),         // 继承完整攻击力
            caster.getDefense() / 2,    // 一半的防御力
            caster.getSpeed(),          // 相同的速度
            summonSkills,              // 添加攻击技能
            BattleUnit.TYPE_SUMMON     // 召唤单位类型
        );

        // 设置召唤单位的主单位和持续时间
        summonedUnit.setMaster(caster);
        summonedUnit.setSummonDuration(SUMMON_DURATION); // 设置持续时间为5回合

        playerUnits[emptySlot] = summonedUnit;
        summonsUsed++;

        // 设置召唤技能冷却
        if (skills != null && skills.length > SUMMON_SKILL_INDEX) {
            skills[SUMMON_SKILL_INDEX].resetCooldown();
        }

        addBattleLog("玩家召唤了 " + summonedUnit.getName() + " 在位置 " + (emptySlot + 1) + 
                     " (生命值: " + summonedUnit.getMaxHealth() + ", 持续" + SUMMON_DURATION + "回合)");
        updateUI();
    }

    /**
     * 玩家普通攻击
     */
    private void performPlayerAttack() {
        if (playerUnits[1] == null || enemyUnits[1] == null) return;

        int damage = calculateBasicDamage(playerUnits[1], enemyUnits[1]);
        enemyUnits[1].takeDamage(damage);
        totalDamageDealt += damage;

        String enemyName = enemyUnits[1].getName() != null ? enemyUnits[1].getName() : "未知敌人";
        addBattleLog("玩家对 " + enemyName + " 造成 " + damage + " 点伤害");
        updateUI();

        // 检查是否击杀敌人
        if (enemyUnits[1].getCurrentHealth() <= 0) {
            handleEnemyDefeated();
        }
    }

    /**
     * 敌方自动行动
     */
    private void performEnemyAutoAction() {
        if (enemyUnits[1] == null || enemyUnits[1].getCurrentHealth() <= 0) return;

        String enemyName = enemyUnits[1].getName() != null ? enemyUnits[1].getName() : "未知敌人";
        
        // 检查目标是否被眩晕
        if (BuffEffectManager.isUnitStunned(enemyName)) {
            addBattleLog(enemyName + " 被眩晕，无法行动！");
            updateUI();
            return;
        }

        // 简单的AI：随机选择攻击目标
        BattleUnit target = selectEnemyTarget();
        if (target == null) return;

        // 敌方随机选择行动：普通攻击或技能
        BattleSkill[] skills = enemyUnits[1].getSkills();
        boolean useSkill = false;
        
        if (skills != null && skills.length > 0) {
            // 检查是否有可用技能
            for (BattleSkill skill : skills) {
                if (skill != null && skill.isReady()) {
                    useSkill = true;
                    break;
                }
            }
            
            // 50%概率使用技能
            if (useSkill && random.nextBoolean()) {
                // 随机选择一个可用技能
                List<BattleSkill> availableSkills = new ArrayList<>();
                for (BattleSkill skill : skills) {
                    if (skill != null && skill.isReady()) {
                        availableSkills.add(skill);
                    }
                }
                
                if (!availableSkills.isEmpty()) {
                    BattleSkill selectedSkill = availableSkills.get(random.nextInt(availableSkills.size()));
                    
                    // 使用技能
                    String skillResult = BattleSkillManager.executeSkill(
                        selectedSkill.getSkillType(), selectedSkill.getLevel(), 
                        enemyUnits[1], target, this);
                    
                    // 设置技能冷却
                    selectedSkill.resetCooldown();
                    
                    addBattleLog(skillResult);
                    skillsUsed++;
                    
                    updateUI();
                    return;
                }
            }
        }

        // 使用普通攻击
        int damage = calculateBasicDamage(enemyUnits[1], target);
        
        // 处理流血效果额外伤害
        int bleedingDamage = BuffEffectManager.processBleedingOnDamageTaken(target.getName());
        if (bleedingDamage > 0) {
            damage += bleedingDamage;
            addBattleLog(target.getName() + " 的流血效果造成额外 " + bleedingDamage + " 点伤害！");
        }
        
        target.takeDamage(damage);
        totalDamageTaken += damage;

        String targetName = target.getName() != null ? target.getName() : "未知目标";
        addBattleLog(enemyName + " 对 " + targetName + " 造成 " + damage + " 点伤害");
        updateUI();

        // 检查是否击杀玩家单位
        if (target.getCurrentHealth() <= 0) {
            handlePlayerUnitDefeated(target);
        }
    }

    /**
     * 选择敌方攻击目标
     * 修改为优先攻击分身单位
     */
    private BattleUnit selectEnemyTarget() {
        // 优先攻击召唤单位（分身）
        for (int i = 0; i < 3; i++) {
            if (playerUnits[i] != null && playerUnits[i].getCurrentHealth() > 0 && 
                playerUnits[i].getType() == BattleUnit.TYPE_SUMMON) {
                addBattleLog("敌人优先攻击分身: " + playerUnits[i].getName());
                return playerUnits[i];
            }
        }
        
        // 如果没有召唤单位，攻击玩家主单位（2号位，索引1）
        if (playerUnits[1] != null && playerUnits[1].getCurrentHealth() > 0) {
            return playerUnits[1];
        }

        // 最后攻击其他存活的玩家单位
        for (int i = 0; i < 3; i++) {
            if (playerUnits[i] != null && playerUnits[i].getCurrentHealth() > 0) {
                return playerUnits[i];
            }
        }

        return null;
    }

    /**
     * 结束玩家回合
     */
    private void endPlayerTurn() {
        // 处理回合开始时的效果和冷却（包括进度更新）
        processRoundStart();
        
        // 敌方行动
        performEnemyAutoAction();

        // 检查战斗是否结束
        if (checkBattleEnd()) return;

        // 增加回合数
        currentRound++;
        updateUI();
    }

    /**
     * 检查战斗是否结束
     */
    private boolean checkBattleEnd() {
        // 检查玩家是否全部阵亡
        boolean playerAlive = false;
        for (int i = 0; i < 3; i++) {
            if (playerUnits[i] != null && playerUnits[i].getCurrentHealth() > 0) {
                playerAlive = true;
                break;
            }
        }

        if (!playerAlive) {
            handleBattleDefeat();
            return true;
        }

        // 检查敌方是否全部阵亡
        boolean enemyAlive = false;
        for (int i = 0; i < 3; i++) {
            if (enemyUnits[i] != null && enemyUnits[i].getCurrentHealth() > 0) {
                enemyAlive = true;
                break;
            }
        }

        if (!enemyAlive) {
            handleBattleVictory();
            return true;
        }

        return false;
    }

    /**
     * 处理敌方被击败
     */
    private void handleEnemyDefeated() {
        enemiesKilled++;
        
        // 检查敌方单位是否存在后再获取名称
        if (enemyUnits[1] != null && enemyUnits[1].getName() != null) {
            addBattleLog("击败了 " + enemyUnits[1].getName());
        } else {
            addBattleLog("击败了未知敌人");
        }

        // 处理怪物掉落物
        handleMonsterDrops();

        // 检查战斗胜利
        checkBattleEnd();
    }

    /**
     * 处理玩家单位被击败
     */
    private void handlePlayerUnitDefeated(BattleUnit unit) {
        addBattleLog(unit.getName() + " 被击败");

        // 如果是召唤单位，移除它
        if (unit.getType() == BattleUnit.TYPE_SUMMON) {
            for (int i = 0; i < 3; i++) {
                if (playerUnits[i] == unit) {
                    playerUnits[i] = null;
                    break;
                }
            }
        }

        // 检查战斗失败
        checkBattleEnd();
    }

    /**
     * 处理战斗胜利
     */
    private void handleBattleVictory() {
        battleEnded = true;
        addBattleLog("[战斗胜利]");

        // 显示结果对话框
        if (isWildEncounter) {
            showWildBattleResultDialog(true);
        } else {
            // 普通战斗胜利处理
            BattleResult battleResult = new BattleResult(userId, true, playerGold, playerExp,
                enemiesKilled, isBossBattle, currentRound);
            resultManager.saveBattleResult(battleResult);

            // 显示胜利对话框
            showVictoryDialog();
        }
    }

    /**
     * 处理战斗失败
     */
    private void handleBattleDefeat() {
        battleEnded = true;
        addBattleLog("[战斗失败]");

        // 显示结果对话框
        if (isWildEncounter) {
            showWildBattleResultDialog(false);
        } else {
            // 普通战斗失败处理
            BattleResult battleResult = new BattleResult(userId, false, playerGold, playerExp,
                enemiesKilled, isBossBattle, currentRound);
            resultManager.saveBattleResult(battleResult);

            // 显示失败对话框
            showDefeatDialog();
        }
    }

    /**
     * 显示野外对战结算弹窗
     */
    private void showWildBattleResultDialog(boolean isVictory) {
        String animalName = wildAnimal != null ? wildAnimal.getName() : "未知动物";

        WildBattleResultDialogFragment dialog =
            WildBattleResultDialogFragment.newInstance(isVictory, animalName,
                terrainType, originalX, originalY, this);

        dialog.setOnResultDialogListener(new WildBattleResultDialogFragment.OnResultDialogListener() {
            @Override
            public void onReturnToGame() {
                // 让对话框自己处理掉落物添加到背包的逻辑
                // 通过按钮点击触发对话框内部的handleVictory方法
                
                // 返回游戏逻辑
                Intent resultIntent = new Intent();
                resultIntent.putExtra("battle_result", "victory");
                resultIntent.putExtra("animal_name", animalName);
                resultIntent.putExtra("terrain_type", terrainType);
                resultIntent.putExtra("original_x", originalX);
                resultIntent.putExtra("original_y", originalY);
                resultIntent.putExtra("time_increase", 1);

                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onGameOver() {
                // 游戏失败逻辑
                Intent resultIntent = new Intent();
                resultIntent.putExtra("battle_result", "defeat");
                resultIntent.putExtra("animal_name", animalName);

                // 重置任务进度（战斗失败时重置）
                QuestManager questManager = QuestManager.getInstance(BattleActivity.this);
                questManager.resetQuestProgressOnGameFailure(MyApplication.currentUserId);

                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        });

        dialog.show(getSupportFragmentManager(), "wild_battle_result");
    }

    /**
     * 显示胜利对话框
     */
    private void showVictoryDialog() {
        // 检查Activity是否还在运行
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        try {
            // 简化实现，实际项目中需要完整的UI
            new android.app.AlertDialog.Builder(this)
                .setTitle("战斗胜利")
                .setMessage(String.format("获得 %d 金币和 %d 经验", playerGold, playerExp))
                .setPositiveButton("确定", (dialog, which) -> finish())
                .show();
        } catch (Exception e) {
            // 如果显示对话框失败，直接结束Activity
            finish();
        }
    }

    /**
     * 显示失败对话框
     */
    private void showDefeatDialog() {
        // 检查Activity是否还在运行
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        try {
            // 简化实现，实际项目中需要完整的UI
            new android.app.AlertDialog.Builder(this)
                .setTitle("战斗失败")
                .setMessage("您被击败了")
                .setPositiveButton("确定", (dialog, which) -> finish())
                .show();
        } catch (Exception e) {
            // 如果显示对话框失败，直接结束Activity
            finish();
        }
    }

    /**
     * 添加战斗日志
     */
    private void addBattleLog(String message) {
        battleLog.add(message);

        // 更新显示
        StringBuilder logText = new StringBuilder();
        for (String log : battleLog) {
            logText.append(log).append("\n");
        }

        battleLogText.setText(logText.toString());

        // 滚动到底部
        battleLogScroll.post(() -> battleLogScroll.fullScroll(View.FOCUS_DOWN));
    }

    /**
     * 计算基础伤害
     */
    private int calculateBasicDamage(BattleUnit attacker, BattleUnit defender) {
        int baseDamage = attacker.getAttack();
        int defense = defender.getDefense();

        // 简单伤害计算：攻击 - 防御，最小为1
        int damage = Math.max(1, baseDamage - defense);

        // 随机波动 ±10%
        int variation = (int)(damage * 0.1);
        damage += random.nextInt(variation * 2 + 1) - variation;

        return Math.max(1, damage);
    }

    /**
     * 计算技能伤害
     */
    private int calculateSkillDamage(BattleUnit attacker, BattleUnit defender, BattleSkill skill) {
        int baseDamage = attacker.getAttack() * 2; // 技能伤害是普通攻击的2倍
        int defense = defender.getDefense();

        int damage = Math.max(1, baseDamage - defense);

        // 随机波动 ±20%
        int variation = (int)(damage * 0.2);
        damage += random.nextInt(variation * 2 + 1) - variation;

        return Math.max(1, damage);
    }

    /**
     * 处理怪物掉落物
     */
    private void handleMonsterDrops() {
        // 检查敌方单位是否存在
        if (enemyUnits[1] == null) {
            return; // 敌方单位已被清理，不处理掉落物
        }
        
        // 获取当前战斗的怪物信息
        String enemyName = enemyUnits[1].getName();
        if (enemyName == null) {
            return; // 敌方名称为空，不处理掉落物
        }
        
        Monster monster = MonsterManager.getMonsterByName(enemyName);
        
        if (monster != null) {
            // 获取随机掉落物
            String[] drops = monster.getRandomDrops();
            
            // 显示掉落物信息
            if (drops.length > 0) {
                StringBuilder dropText = new StringBuilder();
                for (int i = 0; i < drops.length; i++) {
                    if (i > 0) dropText.append(", ");
                    dropText.append(drops[i]);
                }
                addBattleLog("获得：" + dropText.toString());
            } else {
                addBattleLog("没有获得任何掉落物");
            }
            
            // 这里可以添加背包系统集成
            // updatePlayerInventory(drops);
        }
    }
    
    /**
     * 计算金币奖励（已废弃）
     */
    private int calculateGoldReward(BattleUnit enemy) {
        return 0;
    }

    /**
     * 计算经验奖励（已废弃）
     */
    private int calculateExpReward(BattleUnit enemy) {
        return 0;
    }

    /**
     * 获取单位属性字符串
     */
    private String getUnitAttributesString(BattleUnit unit) {
        if (unit == null) return "无";

        return String.format("%s: HP %d/%d, ATK %d, DEF %d, SPD %d",
            unit.getName(), unit.getCurrentHealth(), unit.getMaxHealth(),
            unit.getAttack(), unit.getDefense(), unit.getSpeed());
    }



    /**
     * 公共方法：结束战斗
     * @param victory 是否胜利
     */
    public void endBattle(boolean victory) {
        if (victory) {
            handleBattleVictory();
        } else {
            handleBattleDefeat();
        }
    }

    /**
     * 公共方法：添加伤害统计
     * @param damage 造成的伤害值
     */
    public void addDamageDealt(int damage) {
        totalDamageDealt += damage;
    }



    /**
     * 处理后退按钮按下事件
     */
    private void handleBackPressed() {
        // 添加返回按钮跳转日志
        Log.d("Navigation", "从 BattleActivity 返回到上一个页面");
        
        // 检查Activity是否还在运行
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        try {
            // 确认退出对话框
            new android.app.AlertDialog.Builder(this)
                .setTitle("退出战斗")
                .setMessage("确定要退出战斗吗？这将视为战斗失败。")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 处理战斗失败
                    if (!battleEnded) {
                        handleBattleDefeat();
                    } else {
                        finish();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        } catch (Exception e) {
            // 如果显示对话框失败，直接结束Activity
            finish();
        }
    }
    
    /**
     * 处理回合开始时的效果和冷却
     */
    private void processRoundStart() {
        addBattleLog("=== 第 " + currentRound + " 回合开始 ===");
        
        // 处理召唤单位持续时间
        processSummonDuration();
        
        // 处理所有单位的技能冷却
        for (int i = 0; i < 3; i++) {
            if (playerUnits[i] != null) {
                processUnitSkills(playerUnits[i]);
            }
            if (enemyUnits[i] != null) {
                processUnitSkills(enemyUnits[i]);
            }
        }
        
        // 处理所有单位的效果
        processAllUnitEffects();
        
        // 重要：更新所有进度条UI显示
        if (cardAdapter != null) {
            cardAdapter.updateAllSkillProgress();
        }
    }
    
    /**
     * 处理单位技能冷却
     */
    private void processUnitSkills(BattleUnit unit) {
        // 直接使用BattleUnit的updateCooldowns方法
        unit.updateCooldowns();
        
        // 记录玩家技能冷却信息
        if (unit == playerUnits[1]) { // 修正为使用索引1（2号位）
            BattleSkill[] skills = unit.getSkills();
            if (skills != null) {
                for (BattleSkill skill : skills) {
                    if (skill != null && skill.getCurrentCooldown() > 0) {
                        addBattleLog("玩家技能 " + skill.getName() + " 冷却剩余: " + skill.getCurrentCooldown() + " 回合");
                    }
                }
            }
        }
    }
    
    /**
     * 处理召唤单位持续时间
     */
    private void processSummonDuration() {
        boolean needUIUpdate = false;
        
        // 处理玩家召唤单位的持续时间
        for (int i = 0; i < 3; i++) {
            if (playerUnits[i] != null && playerUnits[i].getType() == BattleUnit.TYPE_SUMMON) {
                int remainingDuration = playerUnits[i].getSummonDuration();
                if (remainingDuration > 0) {
                    remainingDuration--;
                    playerUnits[i].setSummonDuration(remainingDuration);
                    
                    if (remainingDuration <= 0) {
                        addBattleLog(playerUnits[i].getName() + " 持续时间结束，消失了");
                        playerUnits[i] = null; // 移除召唤单位
                        needUIUpdate = true;
                    } else {
                        addBattleLog(playerUnits[i].getName() + " 剩余持续时间: " + remainingDuration + " 回合");
                    }
                }
            }
        }
        
        // 处理敌方召唤单位的持续时间（如果有的话）
        for (int i = 0; i < 3; i++) {
            if (enemyUnits[i] != null && enemyUnits[i].getType() == BattleUnit.TYPE_SUMMON) {
                int remainingDuration = enemyUnits[i].getSummonDuration();
                if (remainingDuration > 0) {
                    remainingDuration--;
                    enemyUnits[i].setSummonDuration(remainingDuration);
                    
                    if (remainingDuration <= 0) {
                        addBattleLog(enemyUnits[i].getName() + " 持续时间结束，消失了");
                        enemyUnits[i] = null; // 移除召唤单位
                        needUIUpdate = true;
                    }
                }
            }
        }
        
        // 如果有召唤单位消失，更新UI
        if (needUIUpdate) {
            updateUI();
        }
    }

    /**
     * 处理所有单位的效果
     */
    private void processAllUnitEffects() {
        // 处理玩家单位效果
        for (int i = 0; i < 3; i++) {
            if (playerUnits[i] != null) {
                // 处理中毒效果
                if (BuffEffectManager.hasEffect(playerUnits[i].getName(), BuffEffectManager.EFFECT_POISON)) {
                    Map<String, Integer> damageResults = BuffEffectManager.processEffectsAtRoundStart(playerUnits[i].getName());
                    if (damageResults != null && damageResults.containsKey("POISON_DAMAGE")) {
                        int poisonDamage = damageResults.get("POISON_DAMAGE");
                        if (poisonDamage > 0) {
                            playerUnits[i].takeDamage(poisonDamage);
                            addBattleLog("玩家受到 " + poisonDamage + " 点剧毒伤害！");
                            
                            // 检查是否死亡
                            if (playerUnits[i].getCurrentHealth() <= 0) {
                                handlePlayerUnitDefeated(playerUnits[i]);
                            }
                        }
                    }
                }
            }
        }
        
        // 处理敌方单位效果
        for (int i = 0; i < 3; i++) {
            if (enemyUnits[i] != null) {
                // 处理中毒效果
                if (BuffEffectManager.hasEffect(enemyUnits[i].getName(), BuffEffectManager.EFFECT_POISON)) {
                    Map<String, Integer> damageResults = BuffEffectManager.processEffectsAtRoundStart(enemyUnits[i].getName());
                    if (damageResults != null && damageResults.containsKey("POISON_DAMAGE")) {
                        int poisonDamage = damageResults.get("POISON_DAMAGE");
                        if (poisonDamage > 0) {
                            enemyUnits[i].takeDamage(poisonDamage);
                            addBattleLog(enemyUnits[i].getName() + " 受到 " + poisonDamage + " 点剧毒伤害！");
                            
                            // 检查是否死亡
                            if (enemyUnits[i].getCurrentHealth() <= 0) {
                                if (enemyUnits[i] == enemyUnits[1]) { // 修正为使用索引1（2号位）
                                    handleEnemyDefeated();
                                } else {
                                    handlePlayerUnitDefeated(enemyUnits[i]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackPressed();
    }
}