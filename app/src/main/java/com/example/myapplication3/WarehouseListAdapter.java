package com.example.myapplication3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class WarehouseListAdapter extends BaseAdapter {
    
    private Context context;
    private List<Map.Entry<String, Integer>> items;
    private LayoutInflater inflater;
    private int selectedPosition = -1;

    public WarehouseListAdapter(Context context, List<Map.Entry<String, Integer>> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
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
            convertView = inflater.inflate(R.layout.item_warehouse, parent, false);
            holder = new ViewHolder();
            holder.tvItemName = convertView.findViewById(R.id.tv_item_name);
            holder.tvItemCount = convertView.findViewById(R.id.tv_item_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Map.Entry<String, Integer> item = items.get(position);
        holder.tvItemName.setText(item.getKey());
        holder.tvItemCount.setText("×" + item.getValue());
        
        // 设置选中状态
        if (position == selectedPosition) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.selected_item_color));
        } else {
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
        
        return convertView;
    }
    
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }
    
    private static class ViewHolder {
        TextView tvItemName;
        TextView tvItemCount;
    }
}