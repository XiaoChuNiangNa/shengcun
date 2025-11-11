package com.example.myapplication3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map;

public class ResourceIllustrationActivity extends AppCompatActivity {

    private ListView lvResourceList;
    private ImageView ivResourceImage;
    private TextView tvResourceName;
    private TextView tvResourceDescription;
    private TextView tvResourceUsageDetails;
    private ImageButton btnBack;

    private List<ResourceItem> resourceItems;
    private ResourceListAdapter adapter;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_illustration);

        // 获取用户ID
        userId = MyApplication.currentUserId;
        if (userId == -1) {
            SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
            userId = sp.getInt("user_id", -1);
        }

        initViews();
        loadResourceData();
        setupListeners();
    }

    private void initViews() {
        lvResourceList = findViewById(R.id.lv_resource_list);
        ivResourceImage = findViewById(R.id.iv_resource_image);
        tvResourceName = findViewById(R.id.tv_resource_name);
        tvResourceDescription = findViewById(R.id.tv_resource_description);
        tvResourceUsageDetails = findViewById(R.id.tv_resource_usage_details);
        btnBack = findViewById(R.id.btn_back);

        resourceItems = new ArrayList<>();
        adapter = new ResourceListAdapter(this, resourceItems);
        lvResourceList.setAdapter(adapter);
    }

    private void loadResourceData() {
        // 初始化资源数据
        resourceItems.clear();
        
        // 从游戏常量中获取所有资源数据
        addResourceFromConstants(ItemConstants.ITEM_WEED, "基础的植物资源，可用于制作绳索和燃料", "制作绳索、燃料、建筑材料");
        addResourceFromConstants(ItemConstants.ITEM_BERRY, "可食用的浆果，提供少量营养", "恢复饥饿度、制作食物");
        addResourceFromConstants(ItemConstants.ITEM_APPLE, "营养丰富的水果，可恢复体力", "恢复饥饿度、制作食物");
        addResourceFromConstants(ItemConstants.ITEM_HERB, "具有治疗效果的植物，可用于制作药剂", "恢复生命值、制作药剂");
        addResourceFromConstants(ItemConstants.ITEM_FIBER, "植物纤维，可用于制作绳索和布料", "制作绳索、布料、建筑材料");
        addResourceFromConstants(ItemConstants.ITEM_ICE, "冰块，可用于降温和制作饮品", "降温、制作饮品、保存食物");
        addResourceFromConstants(ItemConstants.ITEM_WOOD, "基础的建筑材料，可用于制作工具和搭建庇护所", "制作工具、搭建建筑、燃料");
        addResourceFromConstants(ItemConstants.ITEM_STONE, "坚固的矿物资源，可用于制作高级工具和建筑", "制作工具、建筑加固、防御工事");
        addResourceFromConstants(ItemConstants.ITEM_IRON_ORE, "重要的金属矿石，可冶炼成铁锭", "制作金属工具、武器、盔甲");
        addResourceFromConstants(ItemConstants.ITEM_GEM, "稀有的宝石，价值很高", "制作高级装备、交易货币");
        addResourceFromConstants(ItemConstants.ITEM_WATER, "生命之源，维持生存的基本需求", "解渴、烹饪、制作药剂");
        addResourceFromConstants(ItemConstants.ITEM_FISH, "水产品，提供蛋白质营养", "恢复饥饿度、制作食物");
        addResourceFromConstants(ItemConstants.ITEM_SAND, "基础的建筑材料，可用于制作玻璃和水泥", "制作玻璃、水泥、建筑材料");
        addResourceFromConstants(ItemConstants.ITEM_CLAY, "可塑性强的材料，可用于制作陶器", "制作陶罐、砖块、建筑材料");
        
        // 加载用户解锁状态（基于背包中是否拥有该资源）
        loadUnlockStatusFromBackpack();
        
        adapter.notifyDataSetChanged();
        
        // 默认显示第一个资源
        if (!resourceItems.isEmpty()) {
            showResourceDetails(resourceItems.get(0));
        }
    }
    
    private void addResourceFromConstants(String resourceName, String description, String usage) {
        resourceItems.add(new ResourceItem(
            resourceName,
            description,
            usage,
            getImageNameFromResourceName(resourceName),
            false  // 初始状态为未解锁
        ));
    }
    
    private String getImageNameFromResourceName(String resourceName) {
        // 根据资源名称获取对应的图片名称
        switch (resourceName) {
            case ItemConstants.ITEM_WEED: return "zacao";
            case ItemConstants.ITEM_BERRY: return "jiangguo";
            case ItemConstants.ITEM_APPLE: return "pingguo";
            case ItemConstants.ITEM_HERB: return "yaocao";
            case ItemConstants.ITEM_FIBER: return "xianwei";
            case ItemConstants.ITEM_ICE: return "bingkuai";
            case ItemConstants.ITEM_WOOD: return "mutou";
            case ItemConstants.ITEM_STONE: return "shitou";
            case ItemConstants.ITEM_IRON_ORE: return "tiekuang";
            case ItemConstants.ITEM_GEM: return "baoshi";
            case ItemConstants.ITEM_WATER: return "shui";
            case ItemConstants.ITEM_FISH: return "yu";
            case ItemConstants.ITEM_SAND: return "shazi";
            case ItemConstants.ITEM_CLAY: return "niantu";
            default: return "unknown";
        }
    }
    
    private void loadUnlockStatusFromBackpack() {
        // 从背包中获取用户实际拥有的资源，并设置解锁状态
        DBHelper dbHelper = DBHelper.getInstance(this);
        Map<String, Integer> backpack = dbHelper.getBackpack(userId);
        
        for (ResourceItem item : resourceItems) {
            // 如果背包中有该资源，则解锁
            boolean unlocked = backpack.containsKey(item.getName()) && backpack.get(item.getName()) > 0;
            item.setUnlocked(unlocked);
            
            // 保存解锁状态到SharedPreferences
            saveUnlockStatus(item.getName(), unlocked);
        }
    }

    private void loadUnlockStatus() {
        // 从SharedPreferences加载用户解锁状态
        SharedPreferences sp = getSharedPreferences("illustration_unlock_" + userId, MODE_PRIVATE);
        
        for (ResourceItem item : resourceItems) {
            boolean unlocked = sp.getBoolean(item.getName(), item.isUnlocked());
            item.setUnlocked(unlocked);
        }
    }

    private void saveUnlockStatus(String resourceName, boolean unlocked) {
        SharedPreferences sp = getSharedPreferences("illustration_unlock_" + userId, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(resourceName, unlocked);
        editor.apply();
    }

    private void setupListeners() {
        lvResourceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ResourceItem selectedItem = resourceItems.get(position);
                showResourceDetails(selectedItem);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showResourceDetails(ResourceItem item) {
        tvResourceName.setText(item.getName());
        tvResourceDescription.setText(item.getDescription());
        tvResourceUsageDetails.setText(item.getUsage());
        
        // 设置资源图片
        String imageName = item.getImageName();
        int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resourceId != 0) {
            ivResourceImage.setImageResource(resourceId);
        } else {
            // 使用默认图片
            ivResourceImage.setImageResource(R.drawable.ic_empty);
        }
        
        // 如果未解锁，显示问号
        if (!item.isUnlocked()) {
            ivResourceImage.setImageResource(R.drawable.ic_empty);
            tvResourceName.setText("???");
            tvResourceDescription.setText("该资源尚未解锁，请继续探索游戏");
            tvResourceUsageDetails.setText("未知用途");
        }
    }

    // 资源列表适配器
    private class ResourceListAdapter extends ArrayAdapter<ResourceItem> {
        
        public ResourceListAdapter(Context context, List<ResourceItem> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                    android.R.layout.simple_list_item_1, parent, false);
            }

            ResourceItem item = getItem(position);
            TextView textView = convertView.findViewById(android.R.id.text1);
            
            if (item != null) {
                if (item.isUnlocked()) {
                    textView.setText(item.getName());
                    textView.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    textView.setText("???");
                    textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
            }

            return convertView;
        }
    }

    // 解锁资源的方法（可以从其他Activity调用）
    public void unlockResource(String resourceName) {
        for (ResourceItem item : resourceItems) {
            if (item.getName().equals(resourceName)) {
                item.setUnlocked(true);
                saveUnlockStatus(resourceName, true);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }
}