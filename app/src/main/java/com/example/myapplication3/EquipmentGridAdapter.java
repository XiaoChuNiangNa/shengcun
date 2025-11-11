package com.example.myapplication3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class EquipmentGridAdapter extends BaseAdapter {
    private Context context;
    private List<Equipment> equipmentList;

    public EquipmentGridAdapter(Context context, List<Equipment> equipmentList) {
        this.context = context;
        this.equipmentList = equipmentList;
    }

    @Override
    public int getCount() {
        return equipmentList.size();
    }

    @Override
    public Object getItem(int position) {
        return equipmentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_equipment_grid, parent, false);
            holder = new ViewHolder();
            holder.ivEquip = convertView.findViewById(R.id.iv_equip);
            holder.tvName = convertView.findViewById(R.id.tv_equip_name);
            holder.tvDurability = convertView.findViewById(R.id.tv_equip_durability);
            holder.ivEquippedTag = convertView.findViewById(R.id.iv_equipped_tag);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 设置装备数据
        Equipment equip = equipmentList.get(position);
        holder.tvName.setText(equip.getType());
        holder.tvDurability.setText(String.format("耐久：%d/%d", equip.getDurability(), equip.getMaxDurability()));

        // 显示装备图片
        int imgRes = ResourceImageManager.getItemImage(equip.getType());
        holder.ivEquip.setImageResource(imgRes);

        // 显示已装备标记
        holder.ivEquippedTag.setVisibility(equip.isEquipped() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    static class ViewHolder {
        ImageView ivEquip;
        TextView tvName;
        TextView tvDurability;
        ImageView ivEquippedTag; // 已装备标记（如对勾图标）
    }
}