package com.example.myapplication3;

import java.util.Map;

public class MaterialChecker {
    // 检查背包是否满足材料需求（通用逻辑）
    public static boolean checkMaterials(Map<String, Integer> backpack, Map<String, Integer> requirements, OnCheckFailedListener listener) {
        if (backpack == null) {
            listener.onFailed("背包数据加载失败");
            return false;
        }

        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String item = entry.getKey();
            int required = entry.getValue();

            // 燃料特殊处理（如果有需要）
            if (item.equals("FUEL")) continue;

            int count = backpack.getOrDefault(item, 0);
            if (count < required) {
                listener.onFailed("缺少" + (required - count) + "个" + item);
                return false;
            }
        }

        // 燃料检查（木炭或煤炭）
        if (requirements.containsKey("FUEL")) {
            int fuelNeed = requirements.get("FUEL");
            int charcoalCount = backpack.getOrDefault(ItemConstants.ITEM_CHARCOAL, 0);
            int coalCount = backpack.getOrDefault(ItemConstants.ITEM_COAL, 0);

            if (charcoalCount + coalCount < fuelNeed) {
                listener.onFailed("缺少" + fuelNeed + "个木炭或煤炭（当前共" + (charcoalCount + coalCount) + "个）");
                return false;
            }
        }

        return true;
    }

    // 回调接口用于提示错误信息
    public interface OnCheckFailedListener {
        void onFailed(String message);
    }
}
