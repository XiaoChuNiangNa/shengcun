package com.example.myapplication3;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class TechTreeActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TechPagerAdapter pagerAdapter;
    private TextView tvHopePoints;
    private TechManager techManager;
    private int currentHopePoints;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech_tree);

        // 初始化用户ID（从全局变量或SharedPreferences获取）
        userId = MyApplication.currentUserId;

        // 初始化科技管理器
        techManager = TechManager.getInstance(this);

        initViews();
        loadTechStatus();
        setupViewPager();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tvHopePoints = findViewById(R.id.tv_hope_points);
        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadTechStatus() {
        // 从数据库加载希望点数
        currentHopePoints = DBHelper.getInstance(this).getHopePoints(userId);
        tvHopePoints.setText("希望点数：" + currentHopePoints);

        // 通过TechManager加载用户科技等级
        techManager.loadUserTechLevels(userId);
    }

    private void setupViewPager() {
        // 通过TechManager按类型获取科技列表
        List<Tech> baseTechs = techManager.getTechsByType(Tech.TYPE_BASE);
        List<Tech> primaryTechs = techManager.getTechsByType(Tech.TYPE_PRIMARY);
        List<Tech> secondaryTechs = techManager.getTechsByType(Tech.TYPE_SECONDARY);

        pagerAdapter = new TechPagerAdapter(this,
                baseTechs, primaryTechs, secondaryTechs,
                this::canUpgradeTech, this::upgradeTech);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // 设置标签标题
        tabLayout.getTabAt(0).setText("基础科技");
        tabLayout.getTabAt(1).setText("一级科技");
        tabLayout.getTabAt(2).setText("二级科技");

        updateTabEnableStatus();
    }

    // 更新标签页启用状态
    private void updateTabEnableStatus() {
        // 检查基础科技是否全部满级（通过TechManager获取所有科技）
        boolean isBaseTechsMaxed = true;
        for (Tech tech : techManager.getTechsByType(Tech.TYPE_BASE)) {
            if (!tech.isMaxLevel()) {
                isBaseTechsMaxed = false;
                break;
            }
        }

        // 检查是否有已解锁的二级科技
        boolean hasUnlockedSecondary = false;
        for (Tech tech : techManager.getTechsByType(Tech.TYPE_SECONDARY)) {
            if (tech.isUnlocked()) {
                hasUnlockedSecondary = true;
                break;
            }
        }

        // 处理标签状态
        TabLayout.Tab primaryTab = tabLayout.getTabAt(1);
        if (primaryTab != null && primaryTab.view != null) {
            primaryTab.view.setEnabled(isBaseTechsMaxed);
            primaryTab.view.setClickable(isBaseTechsMaxed);
        }

        TabLayout.Tab secondaryTab = tabLayout.getTabAt(2);
        if (secondaryTab != null && secondaryTab.view != null) {
            secondaryTab.view.setEnabled(hasUnlockedSecondary);
            secondaryTab.view.setClickable(hasUnlockedSecondary);
        }

        pagerAdapter.notifyDataSetChanged();
    }

    // 通过TechManager检查是否可以升级
    private boolean canUpgradeTech(Tech tech) {
        return techManager.canUpgrade(tech, currentHopePoints);
    }

    // 通过TechManager执行升级操作
    private void upgradeTech(Tech tech) {
        // 先检查升级可行性，再执行升级（确保数据库操作优先）
        if (techManager.canUpgrade(tech, currentHopePoints)) {
            boolean success = techManager.upgradeTech(userId, tech, currentHopePoints);
            if (success) {
                // 从数据库重新获取希望点数，避免本地计算偏差
                currentHopePoints = dbHelper.getHopePoints(userId);
                tvHopePoints.setText("希望点数：" + currentHopePoints);
                pagerAdapter.notifyDataSetChanged();
                updateTabEnableStatus();
                Toast.makeText(this, tech.name + " 升级成功！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "无法升级该科技", Toast.LENGTH_SHORT).show();
        }
    }
}