package com.example.myapplication3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private EditText etUsername, etPassword;
    private Button btnLoginRegister;
    private CheckBox cbRememberMe; // 记住我复选框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化数据库
        dbHelper = DBHelper.getInstance(this);

        // 初始化视图
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLoginRegister = findViewById(R.id.btn_login_register);
        cbRememberMe = findViewById(R.id.cb_remember_me);

        // 设置点击事件
        btnLoginRegister.setOnClickListener(v -> handleLoginOrRegister());

        // 自动填充上次登录的用户名
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String lastUsername = sharedPreferences.getString("username", "");
        if (!lastUsername.isEmpty()) {
            etUsername.setText(lastUsername);
            cbRememberMe.setChecked(true);
        }

        // 检查自动登录（非强制，仅作为快速登录选项）
        int rememberedUserId = sharedPreferences.getInt("user_id", -1);
        if (rememberedUserId != -1 && !isFinishing() && !isDestroyed()) {
            // 自动登录处理
            Log.d("LoginActivity", "检测到记住的用户ID: " + rememberedUserId + "，尝试自动登录");
            
            // 保存用户ID到应用全局变量
            MyApplication.currentUserId = rememberedUserId;
            GameStateManager.getInstance(this).setCurrentUserId(rememberedUserId);
            
            // 使用FLAG_ACTIVITY_CLEAR_TASK和FLAG_ACTIVITY_NEW_TASK确保不会出现多个任务栈
            Intent intent;
            if (rememberedUserId == MyApplication.TEST_ACCOUNT_UID) {
                // 测试账号直接进入标题页
                intent = new Intent(LoginActivity.this, TitleActivity.class);
            } else {
                // 普通账号检查游戏状态
                GameStateManager gameStateManager = GameStateManager.getInstance(this);
                if (gameStateManager.isGameStarted() && !gameStateManager.isGameEnded()) {
                    // 游戏进行中，直接进入游戏
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                } else {
                    // 游戏未开始或已结束，进入标题页
                    intent = new Intent(LoginActivity.this, TitleActivity.class);
                }
            }
            
            // 设置Intent标志，清除当前任务栈并创建新任务
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // 确保LoginActivity被销毁
            return; // 不再执行后续代码
        }
    }

    // 定义一个静态成员变量来保存当前运行的AsyncTask
    private LoginAsyncTask loginAsyncTask;

    // 处理登录或注册逻辑
    private void handleLoginOrRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 前置校验（主线程执行）
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 3 || username.length() > 15) {
            Toast.makeText(this, "用户名长度需在3-15之间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show();
            return;
        }

        String encryptedPassword = encryptPassword(password);
        
        // 取消之前可能还在运行的任务
        if (loginAsyncTask != null) {
            loginAsyncTask.cancel(true);
        }
        
        // 创建并执行新的AsyncTask
        loginAsyncTask = new LoginAsyncTask(this, username, encryptedPassword);
        loginAsyncTask.execute();
    }

    // 将AsyncTask改为静态内部类，避免内存泄漏
    private static class LoginAsyncTask extends AsyncTask<Void, Void, Integer> {
        // 使用WeakReference持有Activity引用，防止内存泄漏
        private WeakReference<LoginActivity> activityRef;
        private String username;
        private String encryptedPassword;
        private String resultMsg;
        private DBHelper dbHelper;

        public LoginAsyncTask(LoginActivity activity, String username, String encryptedPassword) {
            this.activityRef = new WeakReference<>(activity);
            this.username = username;
            this.encryptedPassword = encryptedPassword;
            // 获取DBHelper实例（单例模式，不会造成内存泄漏）
            if (activity != null) {
                this.dbHelper = DBHelper.getInstance(activity.getApplicationContext());
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            // 检查是否已取消
            if (isCancelled()) {
                return -1;
            }
            
            // 检查用户是否存在（数据库操作）
            int userId = dbHelper.checkUserExists(username);
            if (userId != -1) {
                // 检查是否已取消
                if (isCancelled()) {
                    return -1;
                }
                
                // 执行登录逻辑
                int loginUserId = dbHelper.login(username, encryptedPassword);
                if (loginUserId != -1) {
                    // 如果是admin账号，返回特殊UID -100
                    if ("admin".equals(username)) {
                        Log.d("LoginActivity", "检测到admin账号登录，使用特殊UID: " + MyApplication.TEST_ACCOUNT_UID);
                        return MyApplication.TEST_ACCOUNT_UID;
                    }
                    return loginUserId; // 登录成功，返回用户ID
                } else {
                    resultMsg = "密码错误";
                    return -1;
                }
            } else {
                // 检查是否已取消
                if (isCancelled()) {
                    return -1;
                }
                
                // 执行注册逻辑
                boolean registerSuccess = dbHelper.register(username, encryptedPassword);
                if (registerSuccess) {
                    int newUserId = dbHelper.login(username, encryptedPassword);
                    if (newUserId != -1) {
                        return newUserId; // 注册并登录成功
                    } else {
                        resultMsg = "注册成功，但登录失败";
                        return -1;
                    }
                } else {
                    resultMsg = "注册失败";
                    return -1;
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 获取Activity引用，并检查是否还存在
            LoginActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                Log.d("LoginActivity", "Activity已不存在，取消回调处理");
                return;
            }
            
            // 清除任务引用
            activity.loginAsyncTask = null;
            
            if (result != -1) {
                activity.handleLoginSuccess(result); // 主线程处理登录成功逻辑
            } else {
                Toast.makeText(activity, resultMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 重要：取消可能正在运行的异步任务
        if (loginAsyncTask != null) {
            loginAsyncTask.cancel(true);
            loginAsyncTask = null;
        }
        
        // 清理引用，避免内存泄漏
        etUsername = null;
        etPassword = null;
        btnLoginRegister = null;
        cbRememberMe = null;
        
        Log.d("LoginActivity", "onDestroy: 资源已清理");
    }

    // 处理登录成功逻辑
    private void handleLoginSuccess(int userId) {
        // 检查Activity状态，确保在有效状态下执行操作
        if (isFinishing() || isDestroyed()) {
            Log.e("LoginActivity", "handleLoginSuccess: Activity已销毁，取消操作");
            return;
        }
        
        // 保存用户ID到应用全局变量和GameStateManager
        MyApplication.currentUserId = userId;
        GameStateManager.getInstance(this).setCurrentUserId(userId);
        
        // 处理记住登录状态
        if (cbRememberMe.isChecked()) {
            // 检查Activity状态
            if (isFinishing() || isDestroyed()) {
                return;
            }
            
            // 保存登录信息到SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", userId);
            editor.putString("username", etUsername.getText().toString());
            editor.apply();
        }
        
        // 检查Activity状态
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        // 创建Intent并设置合适的标志
        Intent intent;
        
        // 测试账号特殊处理
        if (userId == MyApplication.TEST_ACCOUNT_UID) {
            Toast.makeText(this, "管理员账号登录成功", Toast.LENGTH_SHORT).show();
            intent = new Intent(LoginActivity.this, TitleActivity.class);
        } else {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            
            // 生成随机出生点（非测试账号）
            int[] spawnPoint = chooseRandomSpawnPoint();
            
            // 检查Activity状态
            if (isFinishing() || isDestroyed()) {
                return;
            }
            
            // 设置游戏开始状态
            GameStateManager gameStateManager = GameStateManager.getInstance(this);
            gameStateManager.setGameStarted(true);
            gameStateManager.setCurrentUserId(userId);
            
            // 检查Activity状态
            if (isFinishing() || isDestroyed()) {
                return;
            }
            
            // 跳转到MainActivity并传递出生点坐标
            intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("SPAWN_X", spawnPoint[0]);
            intent.putExtra("SPAWN_Y", spawnPoint[1]);
        }
        
        // 重要：设置Intent标志，清除当前任务栈并创建新任务
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        // 检查Activity状态
        if (!isFinishing() && !isDestroyed()) {
            startActivity(intent);
            // 重要：确保在startActivity后立即调用finish()
            finish();
        }
    }

//    private String encryptPassword(String password) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            md.update(password.getBytes());
//            byte[] bytes = md.digest();
//            StringBuilder sb = new StringBuilder();
//            for (byte b : bytes) {
//                sb.append(String.format("%02x", b));
//            }
//            return sb.toString();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return password;
//        }
//    }

    private String encryptPassword(String password) {
        // 调用EncryptionUtils的静态方法，确保加密逻辑统一
        return EncryptionUtils.encryptPassword(password);
    }
    private int[] chooseRandomSpawnPoint() {
            GameMap gameMap = GameMap.getInstance(this);
            List<int[]> validSpawnPoints = new ArrayList<>();
            // 遍历地图范围内所有坐标，通过统一校验筛选
            for (int y = Constant.MAP_MIN; y <= Constant.MAP_MAX; y++) {
                for (int x = Constant.MAP_MIN; x <= Constant.MAP_MAX; x++) {
                    if (gameMap.isValidCoord(x, y)) { // 直接使用统一校验
                        validSpawnPoints.add(new int[]{x, y});
                    }
                }
            }
            // 随机选择（兜底逻辑不变）
            if (!validSpawnPoints.isEmpty()) {
                Random random = new Random();
                return validSpawnPoints.get(random.nextInt(validSpawnPoints.size()));
            } else {
                return new int[]{1, 1}; // 极端情况兜底，没有找到复活点时默认（1，1）
            }
    }
}