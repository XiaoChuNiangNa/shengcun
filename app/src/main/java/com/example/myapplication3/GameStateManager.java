package com.example.myapplication3;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 游戏状态管理器
 * 管理游戏的整体状态，包括游戏是否结束等
 */
public class GameStateManager {
    private static GameStateManager instance;
    private Context context;
    private SharedPreferences sharedPreferences;
    
    private GameStateManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("game_state", Context.MODE_PRIVATE);
    }
    
    public static synchronized GameStateManager getInstance(Context context) {
        if (instance == null) {
            instance = new GameStateManager(context);
        }
        return instance;
    }
    
    /**
     * 设置游戏结束状态
     */
    public void setGameEnded() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("game_ended", true);
        editor.putLong("game_end_time", System.currentTimeMillis());
        editor.apply();
    }
    
    /**
     * 检查游戏是否结束
     */
    public boolean isGameEnded() {
        return sharedPreferences.getBoolean("game_ended", false);
    }
    
    /**
     * 重置游戏状态
     */
    public void resetGame() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("game_ended", false);
        editor.putBoolean("game_started", false); // 重置游戏开始状态
        editor.remove("game_end_time");
        editor.apply();
    }
    
    /**
     * 获取游戏结束时间
     */
    public long getGameEndTime() {
        return sharedPreferences.getLong("game_end_time", 0);
    }
    
    /**
     * 检查是否可以重新开始游戏
     */
    public boolean canRestartGame() {
        // 可以添加一些条件，比如冷却时间等
        return isGameEnded();
    }
    
    /**
     * 检查游戏是否已开始
     */
    public boolean isGameStarted() {
        // 游戏开始的判断逻辑：
        // 1. 有明确的游戏开始标志
        // 2. 游戏未结束
        // 3. 有有效的用户ID
        return sharedPreferences.getBoolean("game_started", false) && 
               !isGameEnded() && 
               getCurrentUserId() != -1;
    }
    
    /**
     * 设置游戏开始状态
     */
    public void setGameStarted(boolean started) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("game_started", started);
        editor.apply();
    }
    
    /**
     * 获取当前用户ID
     */
    public int getCurrentUserId() {
        // 从SharedPreferences获取当前用户ID
        return sharedPreferences.getInt("current_user_id", -1);
    }
    
    /**
     * 设置当前用户ID
     */
    public void setCurrentUserId(int userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("current_user_id", userId);
        editor.apply();
    }
    
    /**
     * 设置游戏开始状态
     */
//    public void setGameStarted(boolean started) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean("game_started", started);
//        editor.apply();
//    }
    
    /**
     * 检查指定用户是否在游戏中
     */
    public boolean isUserInGame(int userId) {
        // 检查游戏是否已开始，并且当前用户ID匹配传入的用户ID
        return isGameStarted() && getCurrentUserId() == userId;
    }
}