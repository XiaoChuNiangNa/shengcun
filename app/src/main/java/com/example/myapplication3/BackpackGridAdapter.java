package com.example.myapplication3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class BackpackGridAdapter extends BaseAdapter {
    private Context context;
    private List<Map.Entry<String, Integer>> items; // 物品列表（名称+数量）

    public BackpackGridAdapter(Context context, List<Map.Entry<String, Integer>> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // 加载网格项布局
            convertView = LayoutInflater.from(context).inflate(R.layout.item_backpack_grid, parent, false);
            holder = new ViewHolder();
            holder.ivItem = convertView.findViewById(R.id.iv_item);
            holder.tvCount = convertView.findViewById(R.id.tv_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 设置物品数据
        Map.Entry<String, Integer> item = items.get(position);
        String itemName = item.getKey();
        int count = item.getValue();

        // 显示物品图片
        int imgRes = ResourceImageManager.getItemImage(itemName);
        holder.ivItem.setImageResource(imgRes);

        // 显示物品数量（右下角）
        holder.tvCount.setText(String.valueOf(count));

        // 注意：此处不要设置点击事件，避免覆盖GridView的点击事件
        return convertView;
    }

    static class ViewHolder {
        ImageView ivItem; // 物品图片
        TextView tvCount; // 数量文本（右下角）
    }
}