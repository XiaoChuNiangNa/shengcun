package com.example.myapplication3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {
    
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        
        progressBar = findViewById(R.id.progressBar);
        
        // 启动进度条动画
        startProgressAnimation();
    }
    
    private void startProgressAnimation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 2; // 每100毫秒增加2%，总共2秒完成
                    
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    
                    try {
                        Thread.sleep(40); // 40毫秒更新一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
                // 进度条完成后跳转到相应页面
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        navigateToNextActivity();
                    }
                });
            }
        }).start();
    }
    
    private void navigateToNextActivity() {
        // 检查是否记住登录状态
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        boolean isRemembered = sharedPreferences.getBoolean("is_remembered", false);
        
        Intent intent;
        if (userId != -1 && isRemembered) {
            // 如果记住我且有有效用户ID，直接跳转到标题页
            MyApplication.currentUserId = userId;
            intent = new Intent(LoadingActivity.this, TitleActivity.class);
        } else {
            // 否则跳转到登录页
            intent = new Intent(LoadingActivity.this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish(); // 关闭加载页面
    }
}