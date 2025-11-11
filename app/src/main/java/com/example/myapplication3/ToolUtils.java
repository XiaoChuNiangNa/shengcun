package com.example.myapplication3;

public class ToolUtils {

    /**
     * 单参数方法：兼容旧调用，使用当前登录用户ID
     * 用于不需要显式指定用户的场景
     */
    public static int getToolInitialDurability(String equipType) {
        // 调用双参数方法，传入全局当前用户ID
        return getToolInitialDurability(equipType, MyApplication.currentUserId);
    }

    /**
     * 双参数方法：支持传入用户ID，计算受科技影响的动态耐久上限
     * 核心修复：关联用户科技等级
     */
    public static int getToolInitialDurability(String equipType, int userId) {
        // 1. 获取基础耐久（原有逻辑）
        int baseDurability = getBaseDurability(equipType);
        if (baseDurability == 0) {
            return 0; // 未知工具类型，返回0
        }

        // 2. 获取科技加成（根据用户ID查询工具耐久升级科技）
        int techBonus = getTechDurabilityBonus(userId);

        // 3. 总耐久上限 = 基础耐久 + 科技加成
        return baseDurability + techBonus;
    }

    /**
     * 提取基础耐久计算逻辑（复用原有switch-case）
     */
    private static int getBaseDurability(String equipType) {
        switch (equipType) {
            // 石质工具
            case ItemConstants.EQUIP_STONE_AXE:
            case ItemConstants.EQUIP_STONE_PICKAXE:
            case ItemConstants.EQUIP_STONE_SICKLE:
            case ItemConstants.EQUIP_STONE_FISHING_ROD:
                return Constant.STONE_TOOL_DURABILITY; // 例如10
            // 铁质工具
            case ItemConstants.EQUIP_IRON_AXE:
            case ItemConstants.EQUIP_IRON_PICKAXE:
            case ItemConstants.EQUIP_IRON_SICKLE:
            case ItemConstants.EQUIP_IRON_FISHING_ROD:
                return Constant.IRON_TOOL_DURABILITY; // 例如20
            // 钻石工具
            case ItemConstants.EQUIP_DIAMOND_AXE:
            case ItemConstants.EQUIP_DIAMOND_PICKAXE:
            case ItemConstants.EQUIP_DIAMOND_SICKLE:
            case ItemConstants.EQUIP_DIAMOND_FISHING_ROD:
                return Constant.DIAMOND_TOOL_DURABILITY; // 例如30
            default:
                return 0;
        }
    }

    /**
     * 根据用户ID查询科技等级，计算耐久加成
     * 修复：使用正确的科技ID "simple_tool_making"（简易工具制作技术）
     */
    private static int getTechDurabilityBonus(int userId) {
        // 获取数据库实例
        DBHelper dbHelper = DBHelper.getInstance(null);
        // 查询用户该科技的等级（默认0级）
        int techLevel = dbHelper.getTechLevel(userId, "simple_tool_making");
        // 每级科技增加10点耐久（可根据需求调整）
        return techLevel * 10;
    }

    /**
     * 获取工具效果描述（更新为使用工具分类系统）
     */
    public static String getEquipDescription(String equipType) {
        ToolCategory category = ToolCategoryManager.getToolCategory(equipType);
        
        // 根据工具等级确定加成数值
        int bonus = 0;
        ToolLevel toolLevel = getToolLevel(equipType);
        if (toolLevel != ToolLevel.UNKNOWN) {
            bonus = toolLevel.getLevel(); // 等级1: +1, 等级2: +2, 等级3: +3
        }
        
        switch (category) {
            case AXE:
                return "树林、针叶林资源产出+" + bonus;
            case PICKAXE:
                return "岩石区、雪山资源产出+" + bonus;
            case SICKLE:
                return "草原、沙漠、沼泽资源产出+" + bonus;
            case FISHING_ROD:
                return "河流、海洋、深海资源产出+" + bonus;
            case NONE:
            default:
                return "无特殊效果";
        }
    }

    /**
     * 原有方法：获取工具等级（保持不变）
     */
    public static ToolLevel getToolLevel(String equipType) {
        // 处理"无"工具类型，返回UNKNOWN但等级为0，允许采集0级地形
        if (equipType == null || equipType.equals("无")) {
            return ToolLevel.UNKNOWN; // UNKNOWN的等级为0，可以采集0级地形
        }
        
        switch (equipType) {
            // 石质工具
            case ItemConstants.EQUIP_STONE_AXE:
            case ItemConstants.EQUIP_STONE_PICKAXE:
            case ItemConstants.EQUIP_STONE_SICKLE:
            case ItemConstants.EQUIP_STONE_FISHING_ROD:
                return ToolLevel.LEVEL_1;
            // 铁质工具
            case ItemConstants.EQUIP_IRON_AXE:
            case ItemConstants.EQUIP_IRON_PICKAXE:
            case ItemConstants.EQUIP_IRON_SICKLE:
            case ItemConstants.EQUIP_IRON_FISHING_ROD:
                return ToolLevel.LEVEL_2;
            // 钻石工具
            case ItemConstants.EQUIP_DIAMOND_AXE:
            case ItemConstants.EQUIP_DIAMOND_PICKAXE:
            case ItemConstants.EQUIP_DIAMOND_SICKLE:
            case ItemConstants.EQUIP_DIAMOND_FISHING_ROD:
                return ToolLevel.LEVEL_3;
            default:
                return ToolLevel.UNKNOWN;
        }
    }



    /**
     * 判断工具是否适用于当前地形
     * @param equipType 工具类型
     * @param terrain 地形类型
     * @return 是否适用
     */
    public static boolean isToolSuitableForTerrain(String equipType, String terrain) {
        String desc = getEquipDescription(equipType);
        // 根据工具效果描述判断是否匹配地形（例如"树林、针叶林"包含当前地形）
        return desc.contains(terrain);
    }

    /**
     * 检查工具等级和区域等级是否匹配（新增方法）
     * @param equipType 工具类型
     * @param areaType 区域类型
     * @return 匹配结果信息
     */
    public static String checkToolAreaMatch(String equipType, String areaType) {
        ToolLevel toolLevel = getToolLevel(equipType);
        
        // 获取区域等级（根据区域类型判断）
        int areaLevel = getAreaLevel(areaType);
        
        // 判断匹配情况
        int toolLevelValue = toolLevel.getLevel();
        
        // 特殊处理：0级地形（草原、树林、河流）允许空手采集
        if (areaLevel == 1 && toolLevelValue == 0) {
            return "空手可以采集" + areaType + "，获得基础资源";
        }
        
        if (toolLevelValue < areaLevel) {
            return "工具等级过低（" + toolLevel + "），无法采集" + areaType + "（需要等级" + areaLevel + "）";
        } else if (toolLevelValue == areaLevel) {
            return "工具等级匹配（" + toolLevel + "），可以采集" + areaType + "，获得基础资源";
        } else {
            return "工具等级较高（" + toolLevel + "），可以采集" + areaType + "，获得额外资源加成";
        }
    }

    /**
     * 获取区域等级（根据区域类型判断）
     * @param areaType 区域类型
     * @return 区域等级
     */
    private static int getAreaLevel(String areaType) {
        // 根据区域类型返回对应的等级
        switch (areaType) {
            case "树林":
            case "草原":
            case "河流":
                return 1; // 基础区域
            case "针叶林":
            case "沙漠":
            case "沼泽":
                return 2; // 中级区域
            case "岩石区":
            case "雪山":
                return 3; // 高级区域
            default:
                return 1; // 默认基础区域
        }
    }

    /**
     * 获取工具在对应地形的资源加成数量
     * @param equipType 工具类型
     * @return 加成数量
     */
    public static int getResourceBonus(String equipType) {
        String desc = getEquipDescription(equipType);
        if (desc.contains("+1")) return 1;
        if (desc.contains("+2")) return 2;
        if (desc.contains("+3")) return 3;
        return 0;
    }
}