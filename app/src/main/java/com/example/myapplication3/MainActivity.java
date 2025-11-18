package com.example.myapplication3;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import java.util.Map;
import java.util.Random;
import java.util.Collections;

public class MainActivity extends BaseActivity {
    // 核心成员变量
    public int currentX, currentY;
    public int life, hunger, thirst, stamina;
    public int gold;
    public int backpackCap;
    public String currentEquip;
    public String difficulty;
    public long firstCollectTime;
    public int gameHour, gameDay, temperature;
    public int currentCollectTimes = 0;
    public int lastRefreshDay = 0;
    public boolean isNeedReloadData = false;
    public boolean isBackPressedEnabled = true;

    // 视图控件
    public TextView tvAreaInfo, tvLife, tvHunger, tvThirst, tvStamina, tvTip;
    public TextView tvAreaDescription, tvScrollTip, tvEquipStatus;
    public TextView tvTime, tvDay, tvTemperature;
    public ImageView ivSetting, ivClock, ivThermometer;
    public ImageButton btnBackpack, btnEquipment, btnFunctions;
    public android.widget.Button btnCollect;
    public GameBackgroundView backgroundView;
    public ScrollView scrollView;

    // 管理器实例
    public DataManager dataManager;
    public UIUpdater uiUpdater;
    public EventHandler eventHandler;
    public TimeManager timeManager;
    public AreaInfoReceiver areaInfoReceiver;

    // 其他工具
    public Handler handler = new Handler(Looper.getMainLooper());
    public Random random = new Random();
    public GameMap gameMap;
    public AlertDialog deathDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCurrentUserId();
        if (MyApplication.currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 先检查死亡状态，如果死亡则重置游戏状态
        DataManager tempDataManager = new DataManager(this);
        Map<String, Object> userStatus = tempDataManager.getDbHelper().getUserStatus(MyApplication.currentUserId);
        int lifeFromDB = userStatus != null && userStatus.containsKey("life") ? (int) userStatus.get("life") : Constant.INIT_LIFE;
        
        // 如果生命值为0（死亡状态），自动重置游戏状态
        if (lifeFromDB <= 0) {
            Log.i("MainActivity", "检测到死亡状态，重置游戏状态");
            
            // 重置游戏数据
            tempDataManager.resetGameData(MyApplication.currentUserId);
            
            // 重置游戏状态管理器
            GameStateManager gameStateManager = GameStateManager.getInstance(this);
            gameStateManager.resetGame();
            
            // 设置游戏开始状态为true，允许重新开始游戏
            gameStateManager.setGameStarted(true);
            gameStateManager.setCurrentUserId(MyApplication.currentUserId);
            
            Log.i("MainActivity", "死亡状态处理完成，游戏状态已重置");
        }
        
        // 然后验证游戏状态一致性
        GameStateManager gameStateManager = GameStateManager.getInstance(this);
        boolean isGameStarted = gameStateManager.isGameStarted();
        int currentUserId = gameStateManager.getCurrentUserId();
        
        if (!isGameStarted || currentUserId != MyApplication.currentUserId) {
            Toast.makeText(this, "游戏状态异常，返回标题页", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TitleActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);

        // 初始化管理器
        dataManager = new DataManager(this);
        
        // 检查是否处于死亡状态，如果是则自动重置游戏
        Map<String, Object> currentUserStatus = dataManager.getDbHelper().getUserStatus(currentUserId);
        uiUpdater = new UIUpdater(this);
        eventHandler = new EventHandler(this);
        timeManager = new TimeManager(this);
        areaInfoReceiver = new AreaInfoReceiver(this);
        gameMap = GameMap.getInstance(this);
        
        // 初始化视图
        uiUpdater.initViews();

        // 注册广播
        IntentFilter areaFilter = new IntentFilter(Constant.ACTION_UPDATE_AREA_INFO);
        ContextCompat.registerReceiver(this, areaInfoReceiver, areaFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // 初始化事件监听
        eventHandler.initClickListeners();

        // 加载数据
        dataManager.loadGameData();

        // 启动定时器
        timeManager.startCDRefresh();
        timeManager.startTimeUpdates();
        timeManager.startTemperatureUpdates();

        // 处理返回键
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isBackPressedEnabled) {
                    showExitDialog();
                }
                // 如果isBackPressedEnabled为false，则不处理返回键（静默忽略）
            }
        });

        // 坐标变化监听
        backgroundView.setOnCoordChangeListener((newX, newY) -> {
            currentX = newX;
            currentY = newY;
            uiUpdater.updateAreaInfo();
            dataManager.saveCoordToDB(newX, newY);
            
            // 坐标变化后检查传送门
            checkPortalInteraction();
            
            // 更新采集按钮文本
            eventHandler.updateCollectButtonText();
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        timeManager.handleOnResume();
        
        // 延迟加载装备状态（从装备页面返回时更新）
        handler.postDelayed(() -> {
            reloadEquipStatus();
        }, 100);
        
        // 延迟加载用户状态（从背包页面返回时更新）
        handler.postDelayed(() -> {
            reloadUserStatus();
        }, 150);
        
        // 延迟更新采集按钮文本
        handler.postDelayed(() -> {
            eventHandler.updateCollectButtonText();
        }, 200);
    }
    
    /**
     * 重新加载装备状态
     */
    private void reloadEquipStatus() {
        // 从数据库重新加载当前装备状态
        String currentEquipFromDB = dataManager.getDbHelper().getCurrentEquip(MyApplication.currentUserId);
        if (currentEquipFromDB != null && !currentEquipFromDB.isEmpty()) {
            currentEquip = currentEquipFromDB;
            MyApplication.currentEquip = currentEquipFromDB;
            Log.d("EquipSync", "重新加载装备状态: " + currentEquip);
        } else {
            currentEquip = "无";
            MyApplication.currentEquip = "无";
            Log.d("EquipSync", "重新加载装备状态: 无装备");
        }
        
        // 刷新装备状态显示
        if (uiUpdater != null) {
            uiUpdater.refreshEquipStatus();
        }
    }

    /**
     * 重新加载用户状态（生命、饥饿、口渴、体力等）
     */
    private void reloadUserStatus() {
        // 从数据库重新加载用户状态
        Map<String, Object> userStatus = dataManager.getDbHelper().getUserStatus(MyApplication.currentUserId);
        if (userStatus != null) {
            life = (int) userStatus.get("life");
            hunger = (int) userStatus.get("hunger");
            thirst = (int) userStatus.get("thirst");
            stamina = (int) userStatus.get("stamina");
            
            Log.d("StatusSync", "重新加载用户状态: 生命=" + life + ", 饥饿=" + hunger + ", 口渴=" + thirst + ", 体力=" + stamina);
            
            // 刷新状态显示 - 使用正确的方法名
            if (uiUpdater != null) {
                uiUpdater.updateStatusDisplays();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timeManager.handleOnPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataManager.saveAllCriticalData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(areaInfoReceiver);
        } catch (IllegalArgumentException e) {
            // 接收器未注册，忽略错误
        }
        if (deathDialog != null && deathDialog.isShowing()) {
            deathDialog.dismiss();
        }
    }

    public static int currentUserId = -1;
    
    private void initCurrentUserId() {
        currentUserId = MyApplication.currentUserId;
    }

    /**
     * 显示游戏结束界面
     */
    public void showGameOverScreen() {
        if (isFinishing()) return;
        
        Log.i("GameOver", "生命值为0，触发游戏结束");
        
        // 禁用所有交互
        disableAllInteractions();
        
        // 添加游戏结束覆盖层
        View gameOverOverlay = LayoutInflater.from(this).inflate(R.layout.game_over_overlay, null);
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(gameOverOverlay);
        
        // 获取游戏结束文字视图
        TextView tvGameOver = gameOverOverlay.findViewById(R.id.tvGameOver);
        
        // 开始渐黑动画
        startFadeToBlackAnimation(gameOverOverlay, tvGameOver);
    }
    
    /**
     * 渐黑动画
     */
    private void startFadeToBlackAnimation(View overlay, TextView tvGameOver) {
        // 渐黑动画
        AlphaAnimation fadeAnimation = new AlphaAnimation(0f, 1f);
        fadeAnimation.setDuration(2000); // 2秒渐黑
        fadeAnimation.setFillAfter(true);
        
        fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d("GameOver", "开始渐黑动画");
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d("GameOver", "渐黑动画结束，显示游戏结束文字");
                // 显示游戏结束文字
                tvGameOver.setVisibility(View.VISIBLE);
                
                // 文字淡入动画
                AlphaAnimation textAnimation = new AlphaAnimation(0f, 1f);
                textAnimation.setDuration(1000);
                textAnimation.setFillAfter(true);
                
                textAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Log.d("GameOver", "游戏结束文字显示完成，显示选择对话框");
                        // 延迟1秒后显示选择对话框
                        handler.postDelayed(() -> {
                            if (!isFinishing()) {
                                showGameOverDialog();
                            }
                        }, 1000);
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                
                tvGameOver.startAnimation(textAnimation);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        overlay.startAnimation(fadeAnimation);
    }
    
    /**
     * 显示游戏结束选择对话框
     */
    private void showGameOverDialog() {
        if (isFinishing()) return;
        
        // 检查是否有存档
        boolean hasSaveData = dataManager.getDbHelper().hasSaveData(currentUserId);
        
        if (!hasSaveData) {
            Log.i("GameOver", "没有存档，直接重置游戏");
            // 没有存档，直接重置游戏并返回标题页
            resetGameAndReturnToTitle();
            return;
        }
        
        Log.i("GameOver", "有存档，显示选择对话框");
        
        // 创建选择对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("游戏结束")
                .setMessage("生命值已归零，游戏结束。请选择操作：")
                .setCancelable(false)
                .setPositiveButton("读档", (dialog, which) -> {
                    Log.i("GameOver", "用户选择读档");
                    loadGame();
                })
                .setNegativeButton("重开", (dialog, which) -> {
                    Log.i("GameOver", "用户选择重开");
                    resetGameAndReturnToTitle();
                });
        
        deathDialog = builder.create();
        deathDialog.show();
    }
    
    /**
     * 读档操作
     */
    private void loadGame() {
        Log.i("GameOver", "开始读档操作");
        
        // 从数据库重新加载用户状态
        Map<String, Object> userStatus = dataManager.getDbHelper().getUserStatus(currentUserId);
        if (userStatus != null) {
            // 恢复生命值到存档时的状态
            int savedLife = (int) userStatus.get("life");
            if (savedLife > 0) {
                life = savedLife;
                
                // 更新数据库
                dataManager.getDbHelper().updateUserStatus(currentUserId, Collections.singletonMap("life", life));
                
                Log.i("GameOver", "读档成功，生命值恢复为：" + life);
                
                // 移除游戏结束覆盖层
                removeGameOverOverlay();
                
                // 重新启用交互
                enableAllInteractions();
                
                // 刷新UI显示
                if (uiUpdater != null) {
                    uiUpdater.updateStatusDisplays();
                }
                
                Toast.makeText(this, "读档成功，游戏继续", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // 如果读档失败，提示用户并重置游戏
        Log.w("GameOver", "读档失败，存档中生命值也为0");
        Toast.makeText(this, "读档失败，存档中生命值也为0，将重置游戏", Toast.LENGTH_LONG).show();
        resetGameAndReturnToTitle();
    }
    
    /**
     * 重置游戏并返回标题页
     */
    private void resetGameAndReturnToTitle() {
        Log.i("GameOver", "开始重置游戏");
        
        // 重置游戏数据
        dataManager.resetGameData(currentUserId);
        
        // 重置游戏状态管理器，确保可以重新开始游戏
        GameStateManager gameStateManager = GameStateManager.getInstance(this);
        gameStateManager.resetGame();
        
        Log.i("GameOver", "游戏重置完成，游戏状态已重置，返回标题页");
        Log.i("GameState", "MainActivity - 重置游戏，游戏状态管理器已重置");
        
        // 返回标题页
        Intent intent = new Intent(this, TitleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * 移除游戏结束覆盖层
     */
    private void removeGameOverOverlay() {
        ViewGroup rootView = findViewById(android.R.id.content);
        View overlay = rootView.findViewById(R.id.gameOverOverlay);
        if (overlay != null) {
            rootView.removeView(overlay);
        }
    }
    
    /**
     * 禁用所有交互
     */
    private void disableAllInteractions() {
        if (btnBackpack != null) btnBackpack.setEnabled(false);
        if (btnEquipment != null) btnEquipment.setEnabled(false);
        if (btnFunctions != null) btnFunctions.setEnabled(false);
        if (backgroundView != null) backgroundView.setEnabled(false);
        
        // 禁用返回键 - 使用自定义标志
        isBackPressedEnabled = false;
    }
    
    /**
     * 启用所有交互
     */
    private void enableAllInteractions() {
        if (btnBackpack != null) btnBackpack.setEnabled(true);
        if (btnEquipment != null) btnEquipment.setEnabled(true);
        if (btnFunctions != null) btnFunctions.setEnabled(true);
        if (backgroundView != null) backgroundView.setEnabled(true);
        
        // 启用返回键
        isBackPressedEnabled = true;
    }

    /**
     * 显示退出游戏对话框
     */
    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出游戏")
                .setMessage("请选择退出方式：")
                .setPositiveButton("保存并退出", (dialog, which) -> {
                    // 显示存档选择对话框
                    new SaveGameDialogFragment().show(getSupportFragmentManager(), "save_game");
                })
                .setNegativeButton("直接退出", (dialog, which) -> {
                    // 直接退出游戏
                    finish();
                })
                .setNeutralButton("取消", null)
                .show();
    }

    /**
     * 检查当前位置是否有传送门，并更新按钮状态
     */
    public void checkPortalInteraction() {
        // 只更新按钮状态，不显示对话框
        eventHandler.updateCollectButtonText();
    }

    /**
     * 显示传送门交互对话框
     */
    private void showPortalDialog() {
        String currentMap = gameMap.getCurrentMapType();
        String targetMap = "main_world".equals(currentMap) ? "fantasy_continent" : "main_world";
        String targetMapName = "main_world".equals(currentMap) ? "奇幻大陆" : "主世界";
        
        new AlertDialog.Builder(this)
                .setTitle("传送门")
                .setMessage("你发现了一个传送门！是否要传送到" + targetMapName + "？")
                .setPositiveButton("传送", (dialog, which) -> {
                    teleportToMap(targetMap);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 传送到指定地图
     */
    private void teleportToMap(String targetMap) {
        boolean success = gameMap.switchMap(currentUserId, targetMap);
        if (success) {
            String targetMapName = "main_world".equals(targetMap) ? "主世界" : "奇幻大陆";
            Toast.makeText(this, "成功传送到" + targetMapName, Toast.LENGTH_SHORT).show();
            
            // 刷新界面显示
            uiUpdater.updateAreaInfo();
        } else {
            Toast.makeText(this, "传送失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 读档后刷新坐标显示
     */
    public void refreshCoordinatesAfterLoad() {
        // 重新从数据库加载坐标数据
        Map<String, Object> userStatus = dataManager.getDbHelper().getUserStatus(currentUserId);
        if (userStatus != null) {
            // 更新当前坐标
            int newX = (int) userStatus.get("current_x");
            int newY = (int) userStatus.get("current_y");
            
            Log.d("MainActivity", "读档前坐标: (" + currentX + ", " + currentY + "), 读档后坐标: (" + newX + ", " + newY + ")");
            
            // 更新主活动坐标变量
            currentX = newX;
            currentY = newY;
            
            // 强制刷新背景视图显示新坐标
            if (backgroundView != null) {
                backgroundView.setCurrentCoord(newX, newY, true);
                // 确保界面立即重绘
                backgroundView.invalidate();
                backgroundView.postInvalidate();
            }
            
            // 刷新UI显示
            if (uiUpdater != null) {
                uiUpdater.updateAreaInfo();
            }
            
            // 确保数据管理器也更新坐标
            if (dataManager != null) {
                dataManager.updateCurrentCoord(newX, newY);
            }
            
            // 检查传送门交互
            checkPortalInteraction();
            
            Log.d("MainActivity", "读档后坐标已刷新: (" + currentX + ", " + currentY + ")");
        }
    }


}