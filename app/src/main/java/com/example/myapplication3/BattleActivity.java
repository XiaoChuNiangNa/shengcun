package com.example.myapplication3;

import com.example.myapplication3.BattleSkill;
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

        // 初始化3个位置的单位控件（适配1v1布局）
        for (int i = 0; i < 3; i++) {
            // 敌方单位控件 - 使用带编号的ID
            enemyName[i] = findViewById(getResources().getIdentifier(
                "enemy_name_" + (i + 1), "id", getPackageName()));
            enemyAttackProgress[i] = findViewById(getResources().getIdentifier(
                "enemy_attack_progress_" + (i + 1), "id", getPackageName()));
            enemyHealth[i] = findViewById(getResources().getIdentifier(
                "enemy_health_" + (i + 1), "id", getPackageName()));

            // 敌方技能进度条
            for (int j = 0; j < 2; j++) {
                enemySkillProgress[i][j] = findViewById(getResources().getIdentifier(
                    "enemy_skill_" + (j + 1), "id", getPackageName()));
            }

            // 玩家单位控件 - 使用带编号的ID
            playerName[i] = findViewById(getResources().getIdentifier(
                "player_name_" + (i + 1), "id", getPackageName()));
            playerAttackProgress[i] = findViewById(getResources().getIdentifier(
                "player_attack_progress_" + (i + 1), "id", getPackageName()));
            playerHealth[i] = findViewById(getResources().getIdentifier(
                "player_health_" + (i + 1), "id", getPackageName()));

            // 玩家技能进度条
            for (int j = 0; j < 2; j++) {
                playerSkillProgress[i][j] = findViewById(getResources().getIdentifier(
                    "player_skill_" + (j + 1), "id", getPackageName()));
            }
        }
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

        // 创建主玩家单位（位置0）
        playerUnits[0] = MonsterManager.createPlayer();
        playerUnits[0].resetCooldowns();

        // 创建敌方单位
        if (isWildEncounter && wildAnimal != null) {
            // 野外遭遇战：使用指定的野生动物
            enemyUnits[0] = MonsterManager.convertToBattleUnit(wildAnimal);
            addBattleLog("遭遇野生动物：" + wildAnimal.getName());
        } else {
            // 普通对战：随机敌人
            Monster monster = MonsterManager.getRandomMonster();
            if (monster != null) {
                enemyUnits[0] = MonsterManager.convertToBattleUnit(monster);
            } else {
                // 如果没有怪物，使用默认敌人
                enemyUnits[0] = new BattleUnit("森林守护者", 100, 8, 5, 80,
                    new BattleSkill[0], BattleUnit.TYPE_ENEMY);
            }
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
     * 初始化召唤技能 - 使用新的技能系统
     */
    private void initSummonSkill() {
        // 给玩家单位添加使用新技能系统的技能
        if (playerUnits[0] != null) {
            BattleSkill[] skills = new BattleSkill[6]; // 包括多种技能类型
            
            // 使用新技能系统创建技能
            skills[0] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.CHARGE, 2);
            skills[1] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.POISON, 1);
            skills[2] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.BITE, 1);
            skills[3] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.SUMMON, 1);
            skills[4] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.STUN, 2);
            skills[5] = BattleSkillManager.createSkill(BattleSkillManager.SkillType.ESCAPE, 1);

            playerUnits[0].setSkills(skills);
            
            // 记录技能信息
            addBattleLog("玩家技能已加载：");
            for (BattleSkill skill : skills) {
                if (skill != null) {
                    addBattleLog("- " + skill.getName() + " (冷却：" + skill.getCooldown() + "回合)");
                }
            }
        }
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

        // 更新玩家单位显示
        if (playerUnits[0] != null && playerName[0] != null) {
            playerName[0].setText(playerUnits[0].getName());
            playerHealth[0].setMax(playerUnits[0].getMaxHealth());
            playerHealth[0].setProgress(playerUnits[0].getCurrentHealth());
            playerAttackProgress[0].setProgress(0);

            // 更新技能进度条
            BattleSkill[] skills = playerUnits[0].getSkills();
            if (skills != null) {
                for (int i = 0; i < Math.min(skills.length, 2); i++) {
                    if (playerSkillProgress[0][i] != null) {
                        playerSkillProgress[0][i].setMax(skills[i].getCooldown());
                        playerSkillProgress[0][i].setProgress(skills[i].getCurrentCooldown());
                    }
                }
            }
        }

        // 更新敌方单位显示
        if (enemyUnits[0] != null && enemyName[0] != null) {
            enemyName[0].setText(enemyUnits[0].getName());
            enemyHealth[0].setMax(enemyUnits[0].getMaxHealth());
            enemyHealth[0].setProgress(enemyUnits[0].getCurrentHealth());
            enemyAttackProgress[0].setProgress(0);

            // 更新技能进度条
            BattleSkill[] skills = enemyUnits[0].getSkills();
            if (skills != null) {
                for (int i = 0; i < Math.min(skills.length, 2); i++) {
                    if (enemySkillProgress[0][i] != null) {
                        enemySkillProgress[0][i].setMax(skills[i].getCooldown());
                        enemySkillProgress[0][i].setProgress(skills[i].getCurrentCooldown());
                    }
                }
            }
        }

        // 更新回合信息和等级信息
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
        if (playerUnits[0] == null || playerUnits[0].getCurrentHealth() <= 0) return;

        // 简单的AI逻辑：优先使用召唤技能，然后使用其他技能，最后普通攻击
        BattleSkill[] skills = playerUnits[0].getSkills();
        boolean actionPerformed = false;

        // 检查召唤技能是否可用
        if (skills != null && skills.length > SUMMON_SKILL_INDEX &&
            skills[SUMMON_SKILL_INDEX].getCurrentCooldown() <= 0) {
            useSummonSkill(playerUnits[0]);
            actionPerformed = true;
        }

        // 检查其他技能是否可用
        if (!actionPerformed && skills != null) {
            for (int i = 0; i < skills.length; i++) {
                if (i != SUMMON_SKILL_INDEX && skills[i].getCurrentCooldown() <= 0) {
                    usePlayerSkill(i);
                    actionPerformed = true;
                    break;
                }
            }
        }

        // 如果没有可用技能，使用普通攻击
        if (!actionPerformed) {
            performPlayerAttack();
        }
    }

    /**
     * 显示技能选择对话框
     */
    private void showSkillSelectionDialog() {
        // 技能选择对话框实现
        // 这里简化实现，实际项目中需要完整的UI

        if (playerUnits[0] != null) {
            BattleSkill[] skills = playerUnits[0].getSkills();
            if (skills != null) {
                // 简单的技能选择逻辑
                int usableSkillIndex = -1;
                for (int i = 0; i < skills.length; i++) {
                    if (skills[i] != null && skills[i].getCurrentCooldown() <= 0) {
                        usableSkillIndex = i;
                        break;
                    }
                }

                if (usableSkillIndex != -1) {
                    usePlayerSkill(usableSkillIndex);
                } else {
                    Toast.makeText(this, "所有技能都在冷却中", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 使用玩家技能 - 使用新技能系统
     */
    private void usePlayerSkill(int skillIndex) {
        if (playerUnits[0] == null || enemyUnits[0] == null) return;

        BattleSkill[] skills = playerUnits[0].getSkills();
        if (skills == null || skillIndex < 0 || skillIndex >= skills.length) return;

        BattleSkill skill = skills[skillIndex];
        if (skill.getCurrentCooldown() > 0) {
            Toast.makeText(this, skill.getName() + " 技能冷却中", Toast.LENGTH_SHORT).show();
            return;
        }

        // 处理特殊技能（召唤技能）
        if (skillIndex == SUMMON_SKILL_INDEX) {
            useSummonSkill(playerUnits[0]);
            return;
        }

        // 使用新技能系统的执行逻辑
        String skillResult = BattleSkillManager.executeSkill(
            skill.getSkillType(), skill.getLevel(), playerUnits[0], enemyUnits[0], this);

        // 记录技能使用
        skillsUsed++;

        // 设置技能冷却
        skill.resetCooldown();

        // 添加战斗日志
        addBattleLog(skillResult);

        updateUI();

        // 检查是否击杀敌人
        if (enemyUnits[0].getCurrentHealth() <= 0) {
            handleEnemyDefeated();
        }
    }

    /**
     * 使用召唤技能
     */
    private void useSummonSkill(BattleUnit caster) {
        // 查找空位放置召唤单位
        int emptySlot = -1;
        for (int i = 1; i < 3; i++) { // 从位置1开始（位置0是玩家）
            if (playerUnits[i] == null) {
                emptySlot = i;
                break;
            }
        }

        if (emptySlot == -1) {
            Toast.makeText(this, "没有空位可以召唤", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建召唤单位
        BattleUnit summonedUnit = new BattleUnit(
            "召唤分身",
            caster.getMaxHealth() / 2, // 一半的生命值
            caster.getAttack() / 2,     // 一半的攻击力
            caster.getDefense() / 2,    // 一半的防御力
            caster.getSpeed(),          // 相同的速度
            new BattleSkill[0],         // 无技能
            BattleUnit.TYPE_SUMMON     // 召唤单位类型
        );

        playerUnits[emptySlot] = summonedUnit;
        summonsUsed++;

        // 设置召唤技能冷却
        BattleSkill[] skills = caster.getSkills();
        if (skills != null && skills.length > SUMMON_SKILL_INDEX) {
            skills[SUMMON_SKILL_INDEX].resetCooldown();
        }

        addBattleLog("玩家召唤了 " + summonedUnit.getName() + " 在位置 " + (emptySlot + 1));
        updateUI();
    }

    /**
     * 玩家普通攻击
     */
    private void performPlayerAttack() {
        if (playerUnits[0] == null || enemyUnits[0] == null) return;

        int damage = calculateBasicDamage(playerUnits[0], enemyUnits[0]);
        enemyUnits[0].takeDamage(damage);
        totalDamageDealt += damage;

        addBattleLog("玩家对 " + enemyUnits[0].getName() + " 造成 " + damage + " 点伤害");
        updateUI();

        // 检查是否击杀敌人
        if (enemyUnits[0].getCurrentHealth() <= 0) {
            handleEnemyDefeated();
        }
    }

    /**
     * 敌方自动行动
     */
    private void performEnemyAutoAction() {
        if (enemyUnits[0] == null || enemyUnits[0].getCurrentHealth() <= 0) return;

        // 检查目标是否被眩晕
        if (BuffEffectManager.isUnitStunned(enemyUnits[0].getName())) {
            addBattleLog(enemyUnits[0].getName() + " 被眩晕，无法行动！");
            updateUI();
            return;
        }

        // 简单的AI：随机选择攻击目标
        BattleUnit target = selectEnemyTarget();
        if (target == null) return;

        // 敌方随机选择行动：普通攻击或技能
        BattleSkill[] skills = enemyUnits[0].getSkills();
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
                        enemyUnits[0], target, this);
                    
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
        int damage = calculateBasicDamage(enemyUnits[0], target);
        
        // 处理流血效果额外伤害
        int bleedingDamage = BuffEffectManager.processBleedingOnDamageTaken(target.getName());
        if (bleedingDamage > 0) {
            damage += bleedingDamage;
            addBattleLog(target.getName() + " 的流血效果造成额外 " + bleedingDamage + " 点伤害！");
        }
        
        target.takeDamage(damage);
        totalDamageTaken += damage;

        addBattleLog(enemyUnits[0].getName() + " 对 " + target.getName() + " 造成 " + damage + " 点伤害");
        updateUI();

        // 检查是否击杀玩家单位
        if (target.getCurrentHealth() <= 0) {
            handlePlayerUnitDefeated(target);
        }
    }

    /**
     * 选择敌方攻击目标
     */
    private BattleUnit selectEnemyTarget() {
        // 优先攻击玩家主单位
        if (playerUnits[0] != null && playerUnits[0].getCurrentHealth() > 0) {
            return playerUnits[0];
        }

        // 如果没有主单位，攻击召唤单位
        for (int i = 1; i < 3; i++) {
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
        addBattleLog("击败了 " + enemyUnits[0].getName());

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
        // 简化实现，实际项目中需要完整的UI
        new android.app.AlertDialog.Builder(this)
            .setTitle("战斗胜利")
            .setMessage(String.format("获得 %d 金币和 %d 经验", playerGold, playerExp))
            .setPositiveButton("确定", (dialog, which) -> finish())
            .show();
    }

    /**
     * 显示失败对话框
     */
    private void showDefeatDialog() {
        // 简化实现，实际项目中需要完整的UI
        new android.app.AlertDialog.Builder(this)
            .setTitle("战斗失败")
            .setMessage("您被击败了")
            .setPositiveButton("确定", (dialog, which) -> finish())
            .show();
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
        // 获取当前战斗的怪物信息
        String enemyName = enemyUnits[0].getName();
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
    }
    
    /**
     * 处理回合开始时的效果和冷却
     */
    private void processRoundStart() {
        addBattleLog("=== 第 " + currentRound + " 回合开始 ===");
        
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
    }
    
    /**
     * 处理单位技能冷却
     */
    private void processUnitSkills(BattleUnit unit) {
        BattleSkill[] skills = unit.getSkills();
        if (skills != null) {
            for (BattleSkill skill : skills) {
                if (skill != null && skill.getCurrentCooldown() > 0) {
                    skill.reduceCooldown();
                    if (unit == playerUnits[0]) {
                        addBattleLog("玩家技能 " + skill.getName() + " 冷却剩余: " + skill.getCurrentCooldown() + " 回合");
                    }
                }
            }
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
                                if (enemyUnits[i] == enemyUnits[0]) {
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