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

        // 检查是否已登录（只有在记住我的情况下才自动登录）
        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
        int userId = sp.getInt("user_id", -1);
        boolean isRemembered = sp.getBoolean("is_remembered", false);

        if (userId != -1 && isRemembered) {
            MyApplication.currentUserId = userId;
            startActivity(new Intent(this, TitleActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        dbHelper = DBHelper.getInstance(this);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLoginRegister = findViewById(R.id.btn_login_register);
        cbRememberMe = findViewById(R.id.cb_remember_me); // 初始化复选框

        btnLoginRegister.setOnClickListener(v -> handleLoginOrRegister());
    }

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
        final String finalUsername = username;
        final String finalEncryptedPassword = encryptedPassword;

        // 数据库操作移至子线程
        new AsyncTask<Void, Void, Integer>() {
            private String resultMsg;

            @Override
            protected Integer doInBackground(Void... voids) {
                // 检查用户是否存在（数据库操作）
                int userId = dbHelper.checkUserExists(finalUsername);
                if (userId != -1) {
                    // 执行登录逻辑
                    int loginUserId = dbHelper.login(finalUsername, finalEncryptedPassword);
                    if (loginUserId != -1) {
                        // 如果是admin账号，返回特殊UID -100
                        if ("admin".equals(finalUsername)) {
                            Log.d("LoginActivity", "检测到admin账号登录，使用特殊UID: " + MyApplication.TEST_ACCOUNT_UID);
                            return MyApplication.TEST_ACCOUNT_UID;
                        }
                        return loginUserId; // 登录成功，返回用户ID
                    } else {
                        resultMsg = "密码错误";
                        return -1;
                    }
                } else {
                    // 执行注册逻辑
                    boolean registerSuccess = dbHelper.register(finalUsername, finalEncryptedPassword);
                    if (registerSuccess) {
                        int newUserId = dbHelper.login(finalUsername, finalEncryptedPassword);
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
                if (result != -1) {
                    handleLoginSuccess(result); // 主线程处理登录成功逻辑
                } else {
                    Toast.makeText(LoginActivity.this, resultMsg, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    // 登录成功处理
    private void handleLoginSuccess(int userId) {
        MyApplication.currentUserId = userId;
        
        // 新增：同步游戏状态管理器中的用户ID
        GameStateManager gameStateManager = GameStateManager.getInstance(this);
        gameStateManager.setCurrentUserId(userId);
        // 首次登录时游戏状态为未开始，只有在标题页点击"开始游戏"后才开始
        gameStateManager.setGameStarted(false);

        // 新增：检查是否为测试账号，如果是则初始化特权数据
        boolean isTest = dbHelper.isTestAccount(userId);
        Log.d("LoginActivity", "handleLoginSuccess: 用户 " + userId + " 是否为测试账号 = " + isTest);
        
        if (isTest) {
            Log.d("LoginActivity", "handleLoginSuccess: 开始初始化测试账号 " + userId + " 的特权数据");
            dbHelper.reinitTestAccountData(userId);
            
            // 验证特权数据是否应用成功
            Map<String, Object> status = dbHelper.getUserStatus(userId);
            Integer backpackCap = (Integer) status.get("backpack_cap");
            Integer hopePoints = (Integer) status.get("hope_points");
            Integer gold = (Integer) status.get("gold");
            Log.d("LoginActivity", "handleLoginSuccess: 特权数据验证 - 背包容量=" + backpackCap + ", 希望点数=" + hopePoints + ", 金币=" + gold);
        }

        // 新增：检查是否为新用户（首次注册），若是则初始化坐标
        boolean isNewUser = dbHelper.isNewUser(userId); // 需要在DBHelper中实现该方法
        if (isNewUser) {
            // 生成随机合法坐标（复用MainActivity的chooseRandomSpawnPoint逻辑）
            int[] spawnPoint = chooseRandomSpawnPoint();
            // 初始化数据库中的坐标
            dbHelper.initUserCoordinates(userId, spawnPoint[0], spawnPoint[1]);
        }

        // 根据记住我选项决定是否保存登录状态
        SharedPreferences.Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
        editor.putInt("user_id", userId);
        editor.putBoolean("is_remembered", cbRememberMe.isChecked());
        editor.apply();

        startActivity(new Intent(this, TitleActivity.class));
        finish();
        Toast.makeText(this, "感谢游玩小厨娘的小游戏", Toast.LENGTH_SHORT).show();
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