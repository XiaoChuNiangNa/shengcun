package com.example.myapplication3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnWarehouse;  // 仓库按钮
    private Button btnSynthesis;  // 合成按钮
    private Button btnCooking;    // 烹饪按钮
    private Button btnSmelting;   // 熔炼按钮
    private Button btnTrade;      // 贸易按钮
    private Button btnBattle;     // 对战按钮
    private ImageButton btnBack;       // 返回按钮
    protected DBHelper dbHelper;  // 数据库助手
    protected int userId;          // 用户ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // 初始化数据库助手和用户ID
        dbHelper = DBHelper.getInstance(this);
        userId = MyApplication.currentUserId;

        initView();
        initClickListener();

        // 检查是否需要显示战斗日志弹窗
        checkAndShowBattleLog();

        // 设置返回按钮功能
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 添加返回按钮跳转日志
                Log.d("Navigation", "从 BaseActivity 返回到标题页");
                
                // 返回标题页
                Intent intent = new Intent(BaseActivity.this, TitleActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initView() {
        btnWarehouse = findViewById(R.id.btn_warehouse);
        btnSynthesis = findViewById(R.id.btn_synthesis);
        btnCooking = findViewById(R.id.btn_cooking);
        btnSmelting = findViewById(R.id.btn_smelting);
        btnTrade = findViewById(R.id.btn_trade);
        btnBattle = findViewById(R.id.btn_battle);
        btnBack = findViewById(R.id.btn_back);
    }

    private void initClickListener() {
        btnWarehouse.setOnClickListener(this);
        btnSynthesis.setOnClickListener(this);
        btnCooking.setOnClickListener(this);
        btnSmelting.setOnClickListener(this);
        btnTrade.setOnClickListener(this);
        btnBattle.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int currentUserId = MyApplication.currentUserId;
        if (currentUserId == -1) {
            SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
            currentUserId = sp.getInt("user_id", -1);
        }

        if (currentUserId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        MyApplication.currentUserId = currentUserId;

        // 设置来源标记，表示从基地进入
        Intent intent = new Intent();
        intent.putExtra("from_base", true);

        if (v.getId() == R.id.btn_warehouse) {
            // 跳转到仓库页面
            intent.setClass(this, WarehouseActivity.class);
        } else if (v.getId() == R.id.btn_synthesis) {
            // 跳转到合成页面
            intent.setClass(this, SynthesisActivity.class);
        } else if (v.getId() == R.id.btn_cooking) {
            // 跳转到烹饪页面
            intent.setClass(this, CookingActivity.class);
        } else if (v.getId() == R.id.btn_smelting) {
            // 跳转到熔炼页面
            intent.setClass(this, SmeltingActivity.class);
        } else if (v.getId() == R.id.btn_trade) {
            // 跳转到贸易页面
            intent.setClass(this, TradingActivity.class);
        } else if (v.getId() == R.id.btn_battle) {
            // 跳转到对战页面
            intent.setClass(this, BattleActivity.class);
        } else if (v.getId() == R.id.btn_back) {
            // 添加返回按钮跳转日志
            Log.d("Navigation", "从 BaseActivity 返回到标题页");
            
            // 返回标题页
            intent.setClass(this, TitleActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        if (v.getId() != R.id.btn_back) {
            startActivity(intent);
        } else {
            finish();
        }
    }

    /**
     * 检查是否从基地进入
     */
    public static boolean isFromBase(Intent intent) {
        return intent != null && intent.getBooleanExtra("from_base", false);
    }

    /**
     * 检查是否是夜间时间（18:00-次日6:00）
     */
    public boolean isNightTime() {
        // 这里需要根据游戏时间来判断，暂时返回false
        // 后续可以根据实际游戏时间系统实现
        return false;
    }

    /**
     * 显示Toast消息
     * @param message 要显示的消息
     */
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查并显示战斗日志弹窗
     */
    private void checkAndShowBattleLog() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("show_battle_log", false)) {
            // 延迟显示，确保基地页面加载完成
            new Handler().postDelayed(() -> {
                String battleResult = intent.getStringExtra("battle_result");
                int rounds = intent.getIntExtra("battle_rounds", 0);
                int damage = intent.getIntExtra("battle_damage", 0);
                int kills = intent.getIntExtra("battle_kills", 0);
                int healing = intent.getIntExtra("battle_healing", 0);
                int skillsUsed = intent.getIntExtra("battle_skills_used", 0);
                int summonsUsed = intent.getIntExtra("battle_summons_used", 0);
                String[] battleLogData = intent.getStringArrayExtra("battle_log_data");
                
                showBattleResultDialog(battleResult, rounds, damage, kills, healing, skillsUsed, summonsUsed, battleLogData);
            }, 500);
        }
    }

    /**
     * 显示战斗结果弹窗 - 使用详细的战斗日志
     */
    private void showBattleResultDialog(String result, int rounds, int damage, int kills, int healing, int skillsUsed, int summonsUsed, String[] battleLogData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 设置弹窗标题
        String title = result.equals("胜利") ? "战斗胜利 - 对战日志" : "战斗失败 - 对战日志";
        builder.setTitle(title);

        // 构建完整的战斗日志文本
        StringBuilder fullLog = new StringBuilder();
        fullLog.append("=== 本场对战完整日志 ===\n\n");

        // 添加战斗统计信息
        fullLog.append("战斗统计:\n");
        fullLog.append("• 结果: ").append(result).append("\n");
        fullLog.append("• 回合数: ").append(rounds).append("\n");
        fullLog.append("• 总伤害: ").append(damage).append("\n");
        fullLog.append("• 承受伤害: ").append(0).append("\n"); // 这里可以后续添加实际数据
        fullLog.append("• 治疗量: ").append(healing).append("\n");
        fullLog.append("• 击败敌人: ").append(kills).append("\n");
        fullLog.append("• 使用技能: ").append(skillsUsed).append("\n");
        fullLog.append("• 召唤次数: ").append(summonsUsed).append("\n\n");

        // 添加战斗过程日志
        fullLog.append("战斗过程:\n");
        if (battleLogData != null && battleLogData.length > 0) {
            // 添加每个回合的详细日志
            for (String logEntry : battleLogData) {
                fullLog.append("• ").append(logEntry).append("\n");
            }
        } else {
            // 如果没有日志数据，显示默认信息
            fullLog.append("• 战斗开始时双方单位属性已记录\n");
            fullLog.append("• 对战过程中进行了多次攻击和技能使用\n");
            fullLog.append("• 详细对战过程可在对战页面查看\n");
        }
        fullLog.append("\n");

        // 添加战斗结果分析
//        fullLog.append("战斗结果分析:\n");
//        if (result.equals("胜利")) {
//            fullLog.append("• 恭喜你获得胜利！\n");
//            if (rounds <= 3) {
//                fullLog.append("• 战斗非常迅速，说明你的实力强大\n");
//            } else if (rounds <= 6) {
//                fullLog.append("• 战斗较为激烈，展现了良好的战术配合\n");
//            } else {
//                fullLog.append("• 这是一场持久战，展现了你的坚韧\n");
//            }
//
//            if (damage > 100) {
//                fullLog.append("• 输出伤害非常高，攻击力出色\n");
//            }
//
//            if (healing > 0) {
//                fullLog.append("• 合理使用了治疗技能，生存能力良好\n");
//            }
//
//            if (skillsUsed > 5) {
//                fullLog.append("• 技能使用频繁，战术多样性较好\n");
//            }
//
//            fullLog.append("• 可以考虑挑战更高难度的敌人\n");
//        } else {
//            fullLog.append("• 战斗失败，不要气馁\n");
//            if (damage < 50) {
//                fullLog.append("• 输出伤害较低，建议提升攻击力\n");
//            }
//
//            if (healing == 0) {
//                fullLog.append("• 未使用治疗技能，生存能力有待提高\n");
//            }
//
//            if (skillsUsed < 3) {
//                fullLog.append("• 技能使用较少，建议多使用技能\n");
//            }
//
//            fullLog.append("• 建议提升角色等级和装备\n");
//            fullLog.append("• 合理搭配技能和战术策略\n");
//        }

        // 设置弹窗内容
        builder.setMessage(fullLog.toString());

        // 添加关闭按钮
        builder.setPositiveButton("关闭", (dialog, which) -> dialog.dismiss());

        // 显示弹窗
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}