package com.example.myapplication3;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * 战利品箱列表适配器
 * 用于在ListView中显示战利品箱
 */
public class LootBoxArrayAdapter extends BaseAdapter {
    private Context context;
    private List<LootBoxInventory.LootBoxItem> lootBoxItems;
    private LayoutInflater inflater;

    public LootBoxArrayAdapter(Context context, List<LootBoxInventory.LootBoxItem> lootBoxItems) {
        this.context = context;
        this.lootBoxItems = lootBoxItems;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lootBoxItems.size();
    }

    @Override
    public Object getItem(int position) {
        return lootBoxItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 由于布局文件不存在，创建一个简单的TextView作为item视图
        TextView textView = new TextView(parent.getContext());
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(16);
        
        // 获取当前位置的战利品箱
        LootBoxInventory.LootBoxItem item = lootBoxItems.get(position);
        if (item != null) {
            textView.setText(item.getBoxName());
            textView.setTextColor(getRarityColor(item.getRarity()));
        }
        
        return textView;
    }

    /**
     * 根据稀有度获取颜色
     */
    private int getRarityColor(Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return Color.GRAY;
            case RARE:
                return Color.BLUE;
            case EPIC:
                return Color.parseColor("#9370DB"); // 紫色
            case LEGENDARY:
                return Color.parseColor("#FF8C00"); // 橙色
            case MYTHICAL:
                return Color.RED;
            default:
                return Color.BLACK;
        }
    }

    // ViewHolder类已移除，因为现在直接使用TextView
}