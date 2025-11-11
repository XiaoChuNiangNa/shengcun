package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class IllustrationSelectionActivity extends AppCompatActivity {

    private Button btnResourceIllustration;
    private Button btnToolIllustration;
    private Button btnFoodIllustration;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illustration_selection);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnResourceIllustration = findViewById(R.id.btn_resource_illustration);
        btnToolIllustration = findViewById(R.id.btn_tool_illustration);
        btnFoodIllustration = findViewById(R.id.btn_food_illustration);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        btnResourceIllustration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到资源图鉴页面
                Intent intent = new Intent(IllustrationSelectionActivity.this, ResourceIllustrationActivity.class);
                startActivity(intent);
            }
        });

        btnToolIllustration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到工具图鉴页面
                Intent intent = new Intent(IllustrationSelectionActivity.this, ToolIllustrationActivity.class);
                startActivity(intent);
            }
        });

        btnFoodIllustration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到料理图鉴页面
                Intent intent = new Intent(IllustrationSelectionActivity.this, FoodIllustrationActivity.class);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}