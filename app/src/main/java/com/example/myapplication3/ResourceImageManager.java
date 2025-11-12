package com.example.myapplication3;

import android.content.res.Resources;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一管理所有资源图片映射关系（物品图片使用拼音命名）
 */
public class ResourceImageManager {

    public enum Type {
        ITEM,       // 物品资源
        TERRAIN,    // 地形资源
        UI          // UI元素
    }

    private static final Map<String, Integer> itemImageMap = new HashMap<>();
    private static final Map<String, Integer> terrainImageMap = new HashMap<>();
    private static final Map<String, Integer> uiImageMap = new HashMap<>();

    static {
        initItemImages();
        initTerrainImages();
        initUiImages();
    }

    /**
     * 初始化物品图片映射（使用拼音命名资源，与Constant中的物品常量对应）
     */
    private static void initItemImages() {
        // 基础资源类（41种）
        itemImageMap.put(ItemConstants.ITEM_MEAT, R.drawable.rou);                // 肉
        itemImageMap.put(ItemConstants.ITEM_LEATHER, R.drawable.pige);            // 皮革
        itemImageMap.put(ItemConstants.ITEM_BONE, R.drawable.shougu);              // 兽骨
        itemImageMap.put(ItemConstants.ITEM_WOOL, R.drawable.yangmao);            // 羊毛
        itemImageMap.put(ItemConstants.ITEM_WEED, R.drawable.zacao);               // 杂草
        itemImageMap.put(ItemConstants.ITEM_BERRY, R.drawable.jiangguo);            // 浆果
        itemImageMap.put(ItemConstants.ITEM_APPLE, R.drawable.pingguo);             // 苹果
        itemImageMap.put(ItemConstants.ITEM_DRIED_BREAD,R.drawable.ganmianbao);      //干面包
        itemImageMap.put(ItemConstants.ITEM_HERB, R.drawable.yaocao);               // 药草
        itemImageMap.put(ItemConstants.ITEM_FIBER, R.drawable.xianwei);             // 纤维
        itemImageMap.put(ItemConstants.ITEM_ICE, R.drawable.bingkuai);              // 冰块
        itemImageMap.put(ItemConstants.ITEM_WOOD, R.drawable.mutou);                // 木头
        itemImageMap.put(ItemConstants.ITEM_HONEYCOMB, R.drawable.fengchao);        // 蜂巢
        itemImageMap.put(ItemConstants.ITEM_ACORN, R.drawable.xiangguo);            // 橡果
        itemImageMap.put(ItemConstants.ITEM_VINE, R.drawable.tengwan);              // 藤蔓
        itemImageMap.put(ItemConstants.ITEM_RESIN, R.drawable.shuzhi);              // 树脂
        itemImageMap.put(ItemConstants.ITEM_TRUFFLE, R.drawable.songlu);            // 松露
        itemImageMap.put(ItemConstants.ITEM_STONE, R.drawable.shitou);              // 石头
        itemImageMap.put(ItemConstants.ITEM_IRON_ORE, R.drawable.tiekuang);         // 铁矿
        itemImageMap.put(ItemConstants.ITEM_GEM, R.drawable.baoshi);                // 宝石
        itemImageMap.put(ItemConstants.ITEM_FLINT, R.drawable.suishi);            // 燧石
        itemImageMap.put(ItemConstants.ITEM_SULFUR, R.drawable.liuhuang);           // 硫磺
        itemImageMap.put(ItemConstants.ITEM_COAL, R.drawable.meitan);               // 煤炭
        itemImageMap.put(ItemConstants.ITEM_SNOW_LOTUS, R.drawable.xuelian);        // 雪莲
        itemImageMap.put(ItemConstants.ITEM_OBSIDIAN, R.drawable.heiyaoshi);           // 黑曜石
        itemImageMap.put(ItemConstants.ITEM_WATER, R.drawable.shui);                // 水
        itemImageMap.put(ItemConstants.ITEM_FISH, R.drawable.yu);                   // 鱼
        itemImageMap.put(ItemConstants.ITEM_KELP, R.drawable.haidai);               // 海带
        itemImageMap.put(ItemConstants.ITEM_SAND, R.drawable.shazi);                // 沙子
        itemImageMap.put(ItemConstants.ITEM_SHELL, R.drawable.beike);               // 贝壳
        itemImageMap.put(ItemConstants.ITEM_COCONUT, R.drawable.yezi);            // 椰子
        itemImageMap.put(ItemConstants.ITEM_CRAWFISH, R.drawable.pangxie);          // 螃蟹
        itemImageMap.put(ItemConstants.ITEM_CACTUS_FRUIT, R.drawable.xianrenzhangguo); // 仙人掌果
        itemImageMap.put(ItemConstants.ITEM_MUSHROOM, R.drawable.mogu);             // 蘑菇
        itemImageMap.put(ItemConstants.ITEM_REED, R.drawable.luwei);                // 芦苇
        itemImageMap.put(ItemConstants.ITEM_CLAY, R.drawable.niantu);               // 粘土
        itemImageMap.put(ItemConstants.ITEM_RICE, R.drawable.dami);                 // 大米
        itemImageMap.put(ItemConstants.ITEM_WINTER_MELON, R.drawable.donggua);      // 冬瓜
        itemImageMap.put(ItemConstants.ITEM_CORN, R.drawable.yumi);                 // 玉米
        itemImageMap.put(ItemConstants.ITEM_BEET, R.drawable.tiancai);              // 甜菜
        itemImageMap.put(ItemConstants.ITEM_SPINACH, R.drawable.bocai);             // 菠菜
        itemImageMap.put(ItemConstants.ITEM_CARROT, R.drawable.huluobo);            // 胡萝卜
        itemImageMap.put(ItemConstants.ITEM_POTATO, R.drawable.tudou);              // 土豆
        itemImageMap.put(ItemConstants.ITEM_IRON_INGOT, R.drawable.tieding);           // 铁锭
        itemImageMap.put(ItemConstants.ITEM_GOLD, R.drawable.huangjin);                 // 黄金

        // 工具装备类（14种）
        itemImageMap.put(ItemConstants.EQUIP_STONE_PICKAXE, R.drawable.shigao);         // 石镐
        itemImageMap.put(ItemConstants.EQUIP_STONE_AXE, R.drawable.shifu);              // 石斧
        itemImageMap.put(ItemConstants.EQUIP_STONE_FISHING_ROD, R.drawable.shizhiyugan); // 石质鱼竿
        itemImageMap.put(ItemConstants.EQUIP_STONE_SICKLE, R.drawable.shiliandao);      // 石镰刀
        itemImageMap.put(ItemConstants.EQUIP_IRON_PICKAXE, R.drawable.tiegao);          // 铁镐
        itemImageMap.put(ItemConstants.EQUIP_IRON_AXE, R.drawable.tiefu);               // 铁斧
        itemImageMap.put(ItemConstants.EQUIP_IRON_SICKLE, R.drawable.tieliandao);      // 铁镰刀
        itemImageMap.put(ItemConstants.EQUIP_IRON_FISHING_ROD, R.drawable.tiezhiyugan); // 铁质鱼竿
        itemImageMap.put(ItemConstants.EQUIP_DIAMOND_SICKLE, R.drawable.zuanshiliandao); // 钻石镰刀
        itemImageMap.put(ItemConstants.EQUIP_DIAMOND_FISHING_ROD, R.drawable.zuanshiyugan); // 钻石鱼竿
        itemImageMap.put(ItemConstants.EQUIP_DIAMOND_AXE, R.drawable.zuanshifu);        // 钻石斧
        itemImageMap.put(ItemConstants.EQUIP_DIAMOND_PICKAXE, R.drawable.zuanshigao);   // 钻石镐


        // 合成物品类（18种）
        itemImageMap.put(ItemConstants.ITEM_MEDICINE, R.drawable.caoyao);              // 草药
        itemImageMap.put(ItemConstants.ITEM_HONEY, R.drawable.fengmi);                 // 蜂蜜
        itemImageMap.put(ItemConstants.ITEM_GRASS_ROPE, R.drawable.caozhishengsuo);    // 草质绳索
        itemImageMap.put(ItemConstants.ITEM_REINFORCED_ROPE, R.drawable.jiagushengsuo); // 加固绳索
        itemImageMap.put(ItemConstants.ITEM_HARD_ROPE, R.drawable.yingzhishengsuo);    // 硬质绳索
        itemImageMap.put(ItemConstants.ITEM_WOODEN_PLANK, R.drawable.muban);           // 木板
        itemImageMap.put(ItemConstants.ITEM_NAIL, R.drawable.dingzi);                  // 钉子
        itemImageMap.put(ItemConstants.ITEM_GUNPOWDER, R.drawable.huoyao);             // 火药
        itemImageMap.put(ItemConstants.ITEM_WOODEN_BOAT, R.drawable.muchuan);          // 木船
        itemImageMap.put(ItemConstants.ITEM_STONE_BRICK, R.drawable.shizhuan);         // 石砖
        itemImageMap.put(ItemConstants.ITEM_CEMENT, R.drawable.shuini);              // 水泥
        itemImageMap.put(ItemConstants.ITEM_BRICK, R.drawable.zuankuai);              // 砖块
        itemImageMap.put(ItemConstants.ITEM_IRON_PLATE, R.drawable.tieban);            // 铁板
        itemImageMap.put(ItemConstants.ITEM_GLASS, R.drawable.boli);                   // 玻璃
        itemImageMap.put(ItemConstants.ITEM_DIAMOND, R.drawable.zuanshi);              // 钻石
        itemImageMap.put(ItemConstants.ITEM_CHARCOAL, R.drawable.mutan);               // 木炭
        itemImageMap.put(ItemConstants.ITEM_CLAY_POT, R.drawable.taoguan);             // 陶罐
        itemImageMap.put(ItemConstants.ITEM_BOILED_WATER, R.drawable.kaishui);         // 开水

        // 烹饪食品类（28种）
        itemImageMap.put(ItemConstants.ITEM_GRILLED_FISH, R.drawable.kaoyu);                  // 烤鱼
        itemImageMap.put(ItemConstants.ITEM_GRILLED_CRAWFISH, R.drawable.kaopangxie);         // 烤螃蟹
        itemImageMap.put(ItemConstants.ITEM_FISH_SOUP, R.drawable.yutang);                    // 鱼汤
        itemImageMap.put(ItemConstants.ITEM_MUSHROOM_SOUP, R.drawable.mogutang);              // 蘑菇汤
        itemImageMap.put(ItemConstants.ITEM_KELP_SOUP, R.drawable.haidaitang);                // 海带汤
        itemImageMap.put(ItemConstants.ITEM_FRUIT_PIE, R.drawable.shuiguopai);                // 水果派
        itemImageMap.put(ItemConstants.ITEM_ADVANCED_HERB, R.drawable.gaojicaoyao);           // 高级草药
        itemImageMap.put(ItemConstants.ITEM_ICY_SNOW_LOTUS_COCONUT, R.drawable.bingzhenxuelianyezhi); // 冰镇雪莲椰汁
        itemImageMap.put(ItemConstants.ITEM_HONEY_APPLE_SLICE, R.drawable.fengmipingguopian);  // 蜂蜜苹果片
        itemImageMap.put(ItemConstants.ITEM_RICE_PORRIDGE, R.drawable.damiqingzhou);          // 大米清粥
        itemImageMap.put(ItemConstants.ITEM_KELP_WINTER_MELON_SOUP, R.drawable.haidaidongguatang); // 海带冬瓜汤
        itemImageMap.put(ItemConstants.ITEM_ROASTED_POTATO, R.drawable.kaotudou);             // 烤土豆
        itemImageMap.put(ItemConstants.ITEM_FRUIT_SMOOTHIE, R.drawable.shuiguobingsha);       // 水果冰沙
        itemImageMap.put(ItemConstants.ITEM_CRAWFISH_SHELL_SOUP, R.drawable.pangxiebeiketang); // 螃蟹贝壳汤
        itemImageMap.put(ItemConstants.ITEM_STEAMED_CORN, R.drawable.zhengyumi);             // 蒸玉米
        itemImageMap.put(ItemConstants.ITEM_MUSHROOM_TRUFFLE_FRY, R.drawable.mogusonglujian); // 蘑菇松露煎
        itemImageMap.put(ItemConstants.ITEM_BERRY_HONEY_BREAD, R.drawable.jiangguofengmimianbao); // 浆果蜂蜜面包
        itemImageMap.put(ItemConstants.ITEM_BEET_HONEY_DRINK, R.drawable.tiancaifengmiyin);   // 甜菜蜂蜜饮
        itemImageMap.put(ItemConstants.ITEM_BOILED_SPINACH, R.drawable.shuizhubocai);            // 水煮菠菜
        itemImageMap.put(ItemConstants.ITEM_ROASTED_ACORN, R.drawable.kaoxiangguo);           // 烤橡果
        itemImageMap.put(ItemConstants.ITEM_CACTUS_FRUIT_ICE_DRINK, R.drawable.xianrenzhangguobingyin); // 仙人掌果冰饮
        itemImageMap.put(ItemConstants.ITEM_CARROT_POTATO_SOUP, R.drawable.huluobotudoutang); // 胡萝卜土豆汤
        itemImageMap.put(ItemConstants.ITEM_COCONUT_BERRY_DRINK, R.drawable.yezhijiangguoyin); // 椰汁浆果饮
        itemImageMap.put(ItemConstants.ITEM_TRUFFLE_MUSHROOM_SOUP, R.drawable.songlumogutang); // 松露蘑菇汤
        itemImageMap.put(ItemConstants.ITEM_APPLE_HONEY_DRINK, R.drawable.pingguofengmiyin);  // 苹果蜂蜜饮
        itemImageMap.put(ItemConstants.ITEM_KELP_FISH_SOUP, R.drawable.haidaiyuxiantang);     // 海带鱼鲜汤
        itemImageMap.put(ItemConstants.ITEM_WINTER_MELON_CRAWFISH_SOUP, R.drawable.dongguapangxietang); // 冬瓜螃蟹汤
        itemImageMap.put(ItemConstants.ITEM_DARK_FOOD, R.drawable.heianliaoli);               // 黑暗料理
    }

    /**
     * 初始化地形图片映射
     */
    private static void initTerrainImages() {
        terrainImageMap.put("草原", R.drawable.caoyuan);
        terrainImageMap.put("河流", R.drawable.heliu);
        terrainImageMap.put("沙漠", R.drawable.shamo);
        terrainImageMap.put("沼泽", R.drawable.zhaoze);
        terrainImageMap.put("岩石", R.drawable.yanshiqu);
        terrainImageMap.put("岩石区", R.drawable.yanshiqu);
        terrainImageMap.put("针叶林", R.drawable.zhenyelin);
        terrainImageMap.put("雪山", R.drawable.xueshan);
        terrainImageMap.put("树林", R.drawable.shuling);
        terrainImageMap.put("废弃营地", R.drawable.feiqiyingdi);
        terrainImageMap.put("海洋", R.drawable.haiyang);
        terrainImageMap.put("海滩", R.drawable.haitan);
        terrainImageMap.put("深海", R.drawable.shenhai);
        terrainImageMap.put("雪原", R.drawable.xueyuan);
        terrainImageMap.put("村落", R.drawable.cunluo);
    }

    /**
     * 初始化UI元素图片映射
     */
    private static void initUiImages() {
        uiImageMap.put("ic_load", R.drawable.ic_load);
        uiImageMap.put("ic_reset", R.drawable.ic_reset);
        uiImageMap.put("ic_functions", R.drawable.ic_functions);
        uiImageMap.put("ic_setting", R.drawable.ic_setting);
        uiImageMap.put("ic_building", R.drawable.ic_building);
        uiImageMap.put("ic_synthesis", R.drawable.ic_synthesis);
        uiImageMap.put("ic_equipment", R.drawable.ic_equipment);
        uiImageMap.put("ic_reincarnation", R.drawable.ic_reincarnation);
    }

    /**
     * 获取资源对应的图片ID
     * @param resourceName 资源名称
     * @param type 资源类型
     * @return 图片资源ID，找不到时返回默认图ID
     */
    public static int getImageResId(String resourceName, Type type) {
        if (resourceName == null) return getDefaultResId(type);

        switch (type) {
            case ITEM:
                return itemImageMap.getOrDefault(resourceName, getDefaultResId(type));
            case TERRAIN:
                return terrainImageMap.getOrDefault(resourceName, getDefaultResId(type));
            case UI:
                return uiImageMap.getOrDefault(resourceName, getDefaultResId(type));
            default:
                return R.drawable.ic_empty;
        }
    }

    /**
     * 获取对应类型的默认图片ID
     */
    private static int getDefaultResId(Type type) {
        switch (type) {
            case ITEM:
                return R.drawable.unknown; // 物品默认图（拼音命名风格）
            case TERRAIN:
                return R.drawable.weizhiquyu;   // 地形默认图
            case UI:
                return R.drawable.ic_empty;     // UI默认图
            default:
                return R.drawable.ic_empty;
        }
    }

    /**
     * 安全加载图片（处理资源不存在的情况）
     */
    public static int safeGetImageResId(String resourceName, Type type, Resources resources) {
        int resId = getImageResId(resourceName, type);
        try {
            resources.getResourceName(resId);
            return resId;
        } catch (Resources.NotFoundException e) {
            return getDefaultResId(type);
        }
    }

    public static int getItemImage(String itemType) {
        return itemImageMap.getOrDefault(itemType, 0);
    }
}