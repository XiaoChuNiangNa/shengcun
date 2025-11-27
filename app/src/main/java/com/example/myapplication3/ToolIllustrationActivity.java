package com.example.myapplication3;

import android.content.Context;
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

public class ToolIllustrationActivity extends AppCompatActivity {

    private ListView lvToolList;
    private ImageView ivToolImage;
    private TextView tvToolName;
    private TextView tvToolDescription;
    private TextView tvToolAttributesDetails;
    private ImageButton btnBack;

    private List<ToolItem> toolItems;
    private ToolListAdapter adapter;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_illustration);

        // 获取用户ID
        userId = MyApplication.currentUserId;
        if (userId == -1) {
            SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
            userId = sp.getInt("user_id", -1);
        }

        initViews();
        loadToolData();
        setupListeners();
    }

    private void initViews() {
        lvToolList = findViewById(R.id.lv_tool_list);
        ivToolImage = findViewById(R.id.iv_tool_image);
        tvToolName = findViewById(R.id.tv_tool_name);
        tvToolDescription = findViewById(R.id.tv_tool_description);
        tvToolAttributesDetails = findViewById(R.id.tv_tool_attributes_details);
        btnBack = findViewById(R.id.btn_back);

        toolItems = new ArrayList<>();
        adapter = new ToolListAdapter(this, toolItems);
        lvToolList.setAdapter(adapter);
    }

    private void loadToolData() {
        // 初始化工具数据
        toolItems.clear();
        
        // 添加游戏中的实际工具数据
        // 石质工具
        addToolFromConstants(ItemConstants.EQUIP_STONE_AXE, "基础的伐木工具，可以砍伐树木获取木材", "攻击力：5\n耐久度：50\n效率：中等");
        addToolFromConstants(ItemConstants.EQUIP_STONE_PICKAXE, "基础的采矿工具，可以开采各种矿石", "攻击力：4\n耐久度：45\n效率：中等");
        addToolFromConstants(ItemConstants.EQUIP_STONE_SICKLE, "基础的收割工具，可以收割农作物", "攻击力：3\n耐久度：40\n效率：中等");
        addToolFromConstants(ItemConstants.EQUIP_STONE_FISHING_ROD, "基础的钓鱼工具，可以在水域中钓鱼", "攻击力：2\n耐久度：35\n效率：中等");
        
        // 铲子工具
        addToolFromConstants(ItemConstants.EQUIP_STONE_SHOVEL, "基础的挖掘工具，适合采集植物类资源", "攻击力：3\n耐久度：30\n效率：中等\n草原/海滩/沙漠/雪原10%概率额外获得植物资源");
        addToolFromConstants(ItemConstants.EQUIP_IRON_SHOVEL, "进阶的挖掘工具，植物资源采集率更高", "攻击力：6\n耐久度：60\n效率：高\n草原/海滩/沙漠/雪原20%概率额外获得植物资源");
        addToolFromConstants(ItemConstants.EQUIP_DIAMOND_SHOVEL, "顶级的挖掘工具，植物资源采集率极高", "攻击力：9\n耐久度：90\n效率：极高\n草原/海滩/沙漠/雪原30%概率额外获得植物资源");
        
        // 锤子工具
        addToolFromConstants(ItemConstants.EQUIP_STONE_HAMMER, "基础的敲击工具，适合采集矿石类资源", "攻击力：4\n耐久度：40\n效率：中等\n岩石区/雪山10%概率额外获得矿石资源");
        addToolFromConstants(ItemConstants.EQUIP_IRON_HAMMER, "进阶的敲击工具，矿石资源采集率更高", "攻击力：7\n耐久度：80\n效率：高\n岩石区/雪山20%概率额外获得矿石资源");
        addToolFromConstants(ItemConstants.EQUIP_DIAMOND_HAMMER, "顶级的敲击工具，矿石资源采集率极高", "攻击力：11\n耐久度：120\n效率：极高\n岩石区/雪山30%概率额外获得矿石资源");
        
        // 铁质工具
        addToolFromConstants(ItemConstants.EQUIP_IRON_AXE, "进阶的伐木工具，砍伐效率更高", "攻击力：8\n耐久度：100\n效率：高");
        addToolFromConstants(ItemConstants.EQUIP_IRON_PICKAXE, "进阶的采矿工具，开采效率更高", "攻击力：7\n耐久度：90\n效率：高");
        addToolFromConstants(ItemConstants.EQUIP_IRON_SICKLE, "进阶的收割工具，收割效率更高", "攻击力：6\n耐久度：80\n效率：高");
        addToolFromConstants(ItemConstants.EQUIP_IRON_FISHING_ROD, "进阶的钓鱼工具，钓鱼效率更高", "攻击力：4\n耐久度：70\n效率：高");
        
        // 钻石工具
        addToolFromConstants(ItemConstants.EQUIP_DIAMOND_AXE, "顶级的伐木工具，砍伐效率极高", "攻击力：12\n耐久度：200\n效率：极高");
        addToolFromConstants(ItemConstants.EQUIP_DIAMOND_PICKAXE, "顶级的采矿工具，开采效率极高", "攻击力：10\n耐久度：180\n效率：极高");
        addToolFromConstants(ItemConstants.EQUIP_DIAMOND_SICKLE, "顶级的收割工具，收割效率极高", "攻击力：8\n耐久度：160\n效率：极高");
        addToolFromConstants(ItemConstants.EQUIP_DIAMOND_FISHING_ROD, "顶级的钓鱼工具，钓鱼效率极高", "攻击力：6\n耐久度：140\n效率：极高");
        
        // 加载用户解锁状态
        loadUnlockStatusFromBackpack();
        
        adapter.notifyDataSetChanged();
        
        // 默认显示第一个工具
        if (!toolItems.isEmpty()) {
            showToolDetails(toolItems.get(0));
        }
    }
    
    private void addToolFromConstants(String toolName, String description, String attributes) {
        toolItems.add(new ToolItem(
            toolName,
            description,
            attributes,
            getImageNameFromToolName(toolName),
            false  // 初始状态为未解锁
        ));
    }
    
    private String getImageNameFromToolName(String toolName) {
        // 根据工具名称获取对应的图片名称
        switch (toolName) {
            case ItemConstants.EQUIP_STONE_AXE: return "shifu";
            case ItemConstants.EQUIP_STONE_PICKAXE: return "shigao";
            case ItemConstants.EQUIP_STONE_SICKLE: return "shiliandao";
            case ItemConstants.EQUIP_STONE_FISHING_ROD: return "shizhiyugan";
            case ItemConstants.EQUIP_STONE_SHOVEL: return "shichan";
            case ItemConstants.EQUIP_STONE_HAMMER: return "shichui";
            case ItemConstants.EQUIP_IRON_AXE: return "tiefu";
            case ItemConstants.EQUIP_IRON_PICKAXE: return "tiegao";
            case ItemConstants.EQUIP_IRON_SICKLE: return "tieliandao";
            case ItemConstants.EQUIP_IRON_FISHING_ROD: return "tiezhiyugan";
            case ItemConstants.EQUIP_IRON_SHOVEL: return "tiechan";
            case ItemConstants.EQUIP_IRON_HAMMER: return "tiechui";
            case ItemConstants.EQUIP_DIAMOND_AXE: return "zuanshifu";
            case ItemConstants.EQUIP_DIAMOND_PICKAXE: return "zuanshigao";
            case ItemConstants.EQUIP_DIAMOND_SICKLE: return "zuanshiliandao";
            case ItemConstants.EQUIP_DIAMOND_FISHING_ROD: return "zuanshiyugan";
            case ItemConstants.EQUIP_DIAMOND_SHOVEL: return "zuanshichan";
            case ItemConstants.EQUIP_DIAMOND_HAMMER: return "zuanshichui";
            default: return "unknown";
        }
    }
    
    private void loadUnlockStatusFromBackpack() {
        // 从背包中获取用户实际拥有的工具，并设置解锁状态
        DBHelper dbHelper = DBHelper.getInstance(this);
        List<Equipment> backpackTools = dbHelper.getToolsFromBackpack(userId);
        
        for (ToolItem item : toolItems) {
            // 如果背包中有该工具，则解锁
            boolean unlocked = false;
            if (backpackTools != null) {
                for (Equipment tool : backpackTools) {
                    String toolType = tool.getType();
                    if (toolType != null && toolType.equals(item.getName())) {
                        unlocked = true;
                        break;
                    }
                }
            }
            item.setUnlocked(unlocked);
            
            // 保存解锁状态到SharedPreferences
            saveUnlockStatus(item.getName(), unlocked);
        }
    }

    private void loadUnlockStatus() {
        // 从SharedPreferences加载用户解锁状态
        SharedPreferences sp = getSharedPreferences("illustration_unlock_" + userId, MODE_PRIVATE);
        
        for (ToolItem item : toolItems) {
            boolean unlocked = sp.getBoolean(item.getName(), item.isUnlocked());
            item.setUnlocked(unlocked);
        }
    }

    private void saveUnlockStatus(String toolName, boolean unlocked) {
        SharedPreferences sp = getSharedPreferences("illustration_unlock_" + userId, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(toolName, unlocked);
        editor.apply();
    }

    private void setupListeners() {
        lvToolList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ToolItem selectedItem = toolItems.get(position);
                showToolDetails(selectedItem);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showToolDetails(ToolItem item) {
        tvToolName.setText(item.getName());
        tvToolDescription.setText(item.getDescription());
        tvToolAttributesDetails.setText(item.getAttributes());
        
        // 设置工具图片
        String imageName = item.getImageName();
        int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resourceId != 0) {
            ivToolImage.setImageResource(resourceId);
        } else {
            // 使用默认图片
            ivToolImage.setImageResource(R.drawable.ic_empty);
        }
        
        // 如果未解锁，显示问号
        if (!item.isUnlocked()) {
            ivToolImage.setImageResource(R.drawable.ic_empty);
            tvToolName.setText("???");
            tvToolDescription.setText("该工具尚未解锁，请继续探索游戏");
            tvToolAttributesDetails.setText("未知属性");
        }
    }

    // 工具列表适配器
    private class ToolListAdapter extends ArrayAdapter<ToolItem> {
        
        public ToolListAdapter(Context context, List<ToolItem> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                    android.R.layout.simple_list_item_1, parent, false);
            }

            ToolItem item = getItem(position);
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

    // 解锁工具的方法（可以从其他Activity调用）
    public void unlockTool(String toolName) {
        for (ToolItem item : toolItems) {
            if (item.getName().equals(toolName)) {
                item.setUnlocked(true);
                saveUnlockStatus(toolName, true);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }
}

// 工具图鉴数据模型
class ToolItem {
    private String name;
    private String description;
    private String attributes;
    private String imageName;
    private boolean unlocked;

    public ToolItem(String name, String description, String attributes, String imageName, boolean unlocked) {
        this.name = name;
        this.description = description;
        this.attributes = attributes;
        this.imageName = imageName;
        this.unlocked = unlocked;
    }

    // getter方法
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAttributes() {
        return attributes;
    }

    public String getImageName() {
        return imageName;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}