package com.example.myapplication3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class TechPagerAdapter extends PagerAdapter {

    private Context context;
    private List<Tech> baseTechs;
    private List<Tech> primaryTechs;
    private List<Tech> secondaryTechs;
    private CanUpgradeChecker canUpgradeChecker;
    private UpgradeTechListener upgradeListener;

    // 接口：检查是否可升级
    public interface CanUpgradeChecker {
        boolean canUpgrade(Tech tech);
    }

    // 接口：升级科技回调
    public interface UpgradeTechListener {
        void upgrade(Tech tech);
    }

    public TechPagerAdapter(Context context, List<Tech> baseTechs, List<Tech> primaryTechs,
                            List<Tech> secondaryTechs, CanUpgradeChecker checker,
                            UpgradeTechListener listener) {
        this.context = context;
        this.baseTechs = baseTechs;
        this.primaryTechs = primaryTechs;
        this.secondaryTechs = secondaryTechs;
        this.canUpgradeChecker = checker;
        this.upgradeListener = listener;
    }

    @Override
    public int getCount() {
        return 3; // 三个页面：基础、一级、二级
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // 1. 创建ScrollView作为可滚动容器
        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // 2. 创建内部线性布局（保持原有垂直排列）
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        // 添加间距，避免内容贴边
        layout.setPadding(0, 0, 0, 40);  // 底部留白，防止最后一项被遮挡

        // 3. 根据页面位置加载对应科技列表
        List<Tech> techsToShow = getTechsForPosition(position);

        // 4. 添加科技项到布局
        for (Tech tech : techsToShow) {
            View techView = createTechItemView(tech);
            // 添加科技项之间的间隔
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = 16;  // 每项之间的间距
            layout.addView(techView, params);
        }

        // 5. 将线性布局放入ScrollView
        scrollView.addView(layout);
        // 6. 将ScrollView添加到容器
        container.addView(scrollView);
        return scrollView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    // 根据位置获取对应科技列表
    private List<Tech> getTechsForPosition(int position) {
        switch (position) {
            case 0: return baseTechs;
            case 1: return primaryTechs;
            case 2: return secondaryTechs;
            default: return baseTechs;
        }
    }

    // 创建单个科技项视图
    private View createTechItemView(Tech tech) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_tech, null);

        TextView tvName = view.findViewById(R.id.tv_tech_name);
        TextView tvLevel = view.findViewById(R.id.tv_tech_level);
        TextView tvDesc = view.findViewById(R.id.tv_tech_desc);
        TextView tvCost = view.findViewById(R.id.tv_tech_cost);
        Button btnUpgrade = view.findViewById(R.id.btn_upgrade);

        // 设置科技信息
        tvName.setText(tech.name);
        tvLevel.setText(String.format("等级：%d/%d", tech.level, tech.maxLevel));
        tvDesc.setText(tech.getCurrentDescription());
        tvCost.setText("消耗：" + tech.getCurrentUpgradeCost() + " 希望点数");

        // 设置升级按钮状态
        boolean canUpgrade = canUpgradeChecker.canUpgrade(tech);
        btnUpgrade.setEnabled(canUpgrade);
        btnUpgrade.setText(canUpgrade ? "升级" : getUpgradeButtonText(tech));

        // 升级按钮点击事件
        btnUpgrade.setOnClickListener(v -> {
            if (canUpgrade) {
                upgradeListener.upgrade(tech);
            }
        });

        return view;
    }

    // 获取升级按钮显示文本
    private String getUpgradeButtonText(Tech tech) {
        if (tech.isMaxLevel()) {
            return "已满级";
        }

        // 复用接口的判断结果（已包含前置条件和希望点数检查）
        boolean canUpgrade = canUpgradeChecker.canUpgrade(tech);
        if (canUpgrade) {
            return "升级（消耗：" + tech.getCurrentUpgradeCost() + "希望点）";
        }

        // 未满足条件时，更精准地提示原因
        if (!tech.preTechId.isEmpty()) {
            Tech preTech = findTechById(tech.preTechId);
            if (preTech == null || preTech.level < tech.preTechMinLevel) {
                return "前置科技未达标";
            }
        }

        // 排除前置条件后，剩余原因就是希望点数不足
        return "希望点不足";
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // 保持当前位置，不强制刷新所有页面
        return POSITION_UNCHANGED;
    }

    // 根据techId查找科技
    public Tech findTechById(String techId) {
        return TechManager.getInstance(context).getTechById(techId);
    }
}
