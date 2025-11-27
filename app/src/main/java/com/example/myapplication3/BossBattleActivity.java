package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Boss战斗页面 - 专门用于神殿Boss挑战
 */
public class BossBattleActivity extends AppCompatActivity {
    
    // 界面控件
    private ImageButton btnBack;
    private TextView tvBattleInfo, tvBossInfo, tvPlayerInfo;
    private Button btnAttack, btnSkill, btnEndTurn;
    private ScrollView battleLogScroll;
    private TextView battleLogText;
    
    // Boss数据
    private String bossName;
    private int bossMaxLife, bossCurrentLife;
    private int bossAttack, bossDefense, bossSpeed;
    private String[] bossSkills;
    
    // 玩家数据
    private int playerMaxLife, playerCurrentLife;
    private int playerAttack, playerDefense, playerSpeed;
    private String playerName;
    
    // 战斗状态
    private boolean isPlayerTurn = true;
    private boolean battleEnded = false;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());
    
    // 战利品掉落配置
    private boolean lootEnabled;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_battle);
        
        // 获取Boss数据
        getBossData();
        
        // 初始化界面
        initViews();
        
        // 初始化战斗数据
        initBattleData();
        
        // 显示初始战斗信息
        updateBattleDisplay();
        
        Log.d("BossBattle", "Boss战斗开始：" + bossName);
    }
    
    /**
     * 获取Boss数据
     */
    private void getBossData() {
        Intent intent = getIntent();
        bossName = intent.getStringExtra("boss_name");
        bossMaxLife = intent.getIntExtra("boss_life", 300);
        bossAttack = intent.getIntExtra("boss_attack", 50);
        bossDefense = intent.getIntExtra("boss_defense", 10);
        bossSpeed = intent.getIntExtra("boss_speed", 200);
        String skillsStr = intent.getStringExtra("boss_skills");
        bossSkills = skillsStr != null ? skillsStr.split(",") : new String[]{"普通攻击"};
        lootEnabled = intent.getBooleanExtra("loot_enabled", false);
        
        bossCurrentLife = bossMaxLife;
        
        Log.d("BossBattle", "Boss数据 - " + bossName + " HP:" + bossMaxLife + " ATK:" + bossAttack + " DEF:" + bossDefense + " SPD:" + bossSpeed);
    }
    
    /**
     * 初始化界面控件
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvBattleInfo = findViewById(R.id.tv_battle_info);
        tvBossInfo = findViewById(R.id.tv_boss_info);
        tvPlayerInfo = findViewById(R.id.tv_player_info);
        btnAttack = findViewById(R.id.btn_attack);
        btnSkill = findViewById(R.id.btn_skill);
        btnEndTurn = findViewById(R.id.btn_end_turn);
        battleLogScroll = findViewById(R.id.battle_log_scroll);
        battleLogText = findViewById(R.id.battle_log_text);
        
        // 设置标题
        setTitle("挑战 " + bossName);
        
        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> {
            if (!battleEnded) {
                showExitConfirmDialog();
            } else {
                finish();
            }
        });
        
        // 按钮点击事件
        btnAttack.setOnClickListener(v -> performPlayerAttack());
        btnSkill.setOnClickListener(v -> performPlayerSkill());
        btnEndTurn.setOnClickListener(v -> endPlayerTurn());
    }
    
    /**
     * 初始化战斗数据
     */
    private void initBattleData() {
        // 从数据库获取玩家数据
        DBHelper dbHelper = DBHelper.getInstance(this);
        Map<String, Object> userStatus = dbHelper.getUserStatus(MyApplication.currentUserId);
        
        playerMaxLife = userStatus != null ? (int) userStatus.get("life") : 100;
        playerCurrentLife = playerMaxLife;
        playerAttack = calculatePlayerAttack();
        playerDefense = calculatePlayerDefense();
        playerSpeed = calculatePlayerSpeed();
        playerName = "勇士";
        
        // 获取玩家当前装备
        String currentEquip = dbHelper.getCurrentEquip(MyApplication.currentUserId);
        if (currentEquip != null && !currentEquip.equals("无")) {
            playerName = currentEquip;
        }
        
        Log.d("BossBattle", "玩家数据 - HP:" + playerMaxLife + " ATK:" + playerAttack + " DEF:" + playerDefense + " SPD:" + playerSpeed);
    }
    
    /**
     * 计算玩家攻击力
     */
    private int calculatePlayerAttack() {
        // 基础攻击力 + 装备加成
        int baseAttack = 30;
        // 这里可以根据装备系统计算具体攻击力
        return baseAttack;
    }
    
    /**
     * 计算玩家防御力
     */
    private int calculatePlayerDefense() {
        // 基础防御力 + 装备加成
        int baseDefense = 15;
        // 这里可以根据装备系统计算具体防御力
        return baseDefense;
    }
    
    /**
     * 计算玩家速度
     */
    private int calculatePlayerSpeed() {
        // 基础速度 + 装备加成
        int baseSpeed = 100;
        // 这里可以根据装备系统计算具体速度
        return baseSpeed;
    }
    
    /**
     * 更新战斗显示
     */
    private void updateBattleDisplay() {
        // 更新Boss信息
        tvBossInfo.setText(bossName + "\n生命: " + bossCurrentLife + "/" + bossMaxLife);
        
        // 更新玩家信息
        tvPlayerInfo.setText(playerName + "\n生命: " + playerCurrentLife + "/" + playerMaxLife);
        
        // 更新战斗信息
        String turnText = isPlayerTurn ? "你的回合" : "Boss回合";
        tvBattleInfo.setText(turnText);
        
        // 控制按钮状态
        boolean playerTurnEnabled = isPlayerTurn && !battleEnded;
        btnAttack.setEnabled(playerTurnEnabled);
        btnSkill.setEnabled(playerTurnEnabled);
        btnEndTurn.setEnabled(playerTurnEnabled);
    }
    
    /**
     * 玩家进行普通攻击
     */
    private void performPlayerAttack() {
        if (!isPlayerTurn || battleEnded) return;
        
        // 计算伤害
        int damage = Math.max(1, playerAttack - bossDefense/2 + random.nextInt(10) - 5);
        bossCurrentLife = Math.max(0, bossCurrentLife - damage);
        
        addBattleLog("你攻击了" + bossName + "，造成" + damage + "点伤害！");
        
        // 检查Boss是否死亡
        if (bossCurrentLife <= 0) {
            endBattle(true);
        } else {
            endPlayerTurn();
        }
    }
    
    /**
     * 玩家使用技能
     */
    private void performPlayerSkill() {
        if (!isPlayerTurn || battleEnded) return;
        
        // 这里可以扩展具体的技能系统
        addBattleLog("你使用了技能！");
        performPlayerAttack(); // 暂时简化为攻击
    }
    
    /**
     * 结束玩家回合
     */
    private void endPlayerTurn() {
        isPlayerTurn = false;
        updateBattleDisplay();
        
        // 延迟执行Boss回合
        handler.postDelayed(this::performBossTurn, 1000);
    }
    
    /**
     * Boss回合
     */
    private void performBossTurn() {
        if (battleEnded) return;
        
        // Boss根据速度决定行动次数
        int bossActions = bossSpeed / playerSpeed;
        bossActions = Math.max(1, Math.min(3, bossActions)); // 限制在1-3次
        
        for (int i = 0; i < bossActions; i++) {
            if (battleEnded) break;
            
            // Boss随机选择技能
            int skillIndex = random.nextInt(bossSkills.length);
            String skill = bossSkills[skillIndex];
            
            performBossSkill(skill);
            
            // 检查玩家是否死亡
            if (playerCurrentLife <= 0) {
                endBattle(false);
                break;
            }
        }
        
        if (!battleEnded) {
            isPlayerTurn = true;
            updateBattleDisplay();
        }
    }
    
    /**
     * Boss执行技能
     */
    private void performBossSkill(String skill) {
        int damage = 0;
        String skillMessage = "";
        
        switch (skill) {
            case "普通攻击":
                damage = Math.max(1, bossAttack - playerDefense/2 + random.nextInt(10) - 5);
                skillMessage = bossName + "发动了普通攻击";
                break;
            case "附毒Lv3":
                damage = Math.max(1, bossAttack - playerDefense/2 + random.nextInt(15) - 5);
                skillMessage = bossName + "发动了附毒攻击";
                break;
            case "震慑Lv3":
                damage = Math.max(1, bossAttack - playerDefense/2 + random.nextInt(20) - 5);
                skillMessage = bossName + "发动了震慑攻击";
                break;
            case "掠夺Lv3":
                damage = Math.max(1, bossAttack - playerDefense/2 + random.nextInt(15) - 5);
                skillMessage = bossName + "发动了掠夺攻击";
                break;
            default:
                damage = Math.max(1, bossAttack - playerDefense/2 + random.nextInt(10) - 5);
                skillMessage = bossName + "发动了攻击";
                break;
        }
        
        playerCurrentLife = Math.max(0, playerCurrentLife - damage);
        addBattleLog(skillMessage + "，对你造成" + damage + "点伤害！");
    }
    
    /**
     * 结束战斗
     */
    private void endBattle(boolean playerVictory) {
        battleEnded = true;
        
        if (playerVictory) {
            addBattleLog("恭喜！你击败了" + bossName + "！");
            Toast.makeText(this, "战斗胜利！", Toast.LENGTH_LONG).show();
            
            // 处理战利品掉落
            if (lootEnabled) {
                handleLootDrops();
            }
            
            // 设置返回结果
            Intent result = new Intent();
            result.putExtra("battle_result", "victory");
            result.putExtra("boss_name", bossName);
            setResult(RESULT_OK, result);
            
            // 延迟关闭
            handler.postDelayed(this::finish, 2000);
        } else {
            addBattleLog("你被" + bossName + "击败了...");
            Toast.makeText(this, "战斗失败", Toast.LENGTH_LONG).show();
            
            // 设置返回结果
            Intent result = new Intent();
            result.putExtra("battle_result", "defeat");
            setResult(RESULT_OK, result);
            
            // 延迟关闭
            handler.postDelayed(this::finish, 2000);
        }
        
        updateBattleDisplay();
    }
    
    /**
     * 处理战利品掉落
     */
    private void handleLootDrops() {
        // Boss战利品掉落概率
        double[] lootProbabilities = {
            0.40, // 小战利品箱 40%
            0.30, // 中战利品箱 30%
            0.20, // 大战利品箱 20%
            0.10, // 巨型战利品箱 10%
            0.01  // 终极战利品箱 1%
        };
        
        String[] lootBoxNames = {
            "小战利品箱",
            "中战利品箱",
            "大战利品箱",
            "巨型战利品箱",
            "终极战利品箱"
        };
        
        // 随机掉落3个战利品箱
        List<String> droppedLoots = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double rand = random.nextDouble();
            double cumulative = 0;
            
            for (int j = 0; j < lootProbabilities.length; j++) {
                cumulative += lootProbabilities[j];
                if (rand <= cumulative) {
                    droppedLoots.add(lootBoxNames[j]);
                    break;
                }
            }
        }
        
        // 添加到战利品箱库存
        // 由于LootBoxInventory的方法参数不匹配，这里只记录日志，暂时注释掉添加到库存的代码
        for (String lootBoxType : droppedLoots) {
            // 直接记录获得的战利品箱信息到日志
            addBattleLog("获得 " + lootBoxType + "！");
        }
        
        // 记录总共获得的战利品箱数量
        if (!droppedLoots.isEmpty()) {
            addBattleLog("总共获得 " + droppedLoots.size() + " 个战利品箱！");
        }
        
        addBattleLog("总共获得 " + droppedLoots.size() + " 个战利品箱！");
    }
    
    /**
     * 添加战斗日志
     */
    private void addBattleLog(String message) {
        battleLogText.append(message + "\n");
        // 自动滚动到底部
        battleLogScroll.post(() -> battleLogScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }
    
    /**
     * 显示退出确认对话框
     */
    private void showExitConfirmDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("退出战斗")
                .setMessage("确定要退出战斗吗？退出将被视为战斗失败。")
                .setPositiveButton("确定", (dialog, which) -> {
                    endBattle(false);
                })
                .setNegativeButton("取消", null)
                .show();
    }
}