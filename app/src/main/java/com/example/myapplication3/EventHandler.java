package com.example.myapplication3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandler implements View.OnClickListener {
    private final MainActivity activity;
    private Runnable cdRefreshRunnable;

    public EventHandler(MainActivity activity) {
        this.activity = activity;
    }

    // 初始化点击监听
    public void initClickListeners() {
        activity.findViewById(R.id.btn_collect).setOnClickListener(this);
        activity.findViewById(R.id.btn_up).setOnClickListener(v -> move(0, -1));
        activity.findViewById(R.id.btn_down).setOnClickListener(v -> move(0, 1));
        activity.findViewById(R.id.btn_left).setOnClickListener(v -> move(-1, 0));
        activity.findViewById(R.id.btn_right).setOnClickListener(v -> move(1, 0));
        activity.ivSetting.setOnClickListener(v -> new SettingDialogFragment().show(activity.getSupportFragmentManager(), "setting_dialog"));
        activity.btnBackpack.setOnClickListener(this);
        activity.btnEquipment.setOnClickListener(this);
        activity.btnFunctions.setOnClickListener(this);
    }

    // 移动处理
    public void move(int dx, int dy) {
        int newX = activity.currentX + dx;
        int newY = activity.currentY + dy;

        // 边界检查
        if (newX < Constant.MAP_MIN || newX > Constant.MAP_MAX || newY < Constant.MAP_MIN || newY > Constant.MAP_MAX) {
            // 详细日志提示边界检查
            Log.w("MoveDebug", "地图边界限制 - 当前位置(" + activity.currentX + "," + activity.currentY + "), 目标位置(" + newX + "," + newY + ")超出地图边界(" + Constant.MAP_MIN + "-" + Constant.MAP_MAX + ")");
            activity.tvAreaDescription.setText("已到达地图边界");
            return;
        }

        // 地形检查
        String fromArea = activity.gameMap.getTerrainType(activity.currentX, activity.currentY);
        String toArea = activity.gameMap.getTerrainType(newX, newY);
        Constant.AreaConfig cfgFrom = Constant.AREA_CONFIG.get(fromArea);
        Constant.AreaConfig cfgTo = Constant.AREA_CONFIG.get(toArea);

        if (cfgFrom == null || cfgTo == null) {
            // 详细日志提示未知区域原因
            StringBuilder logMsg = new StringBuilder("未知区域无法移动 - 详细信息: ");
            logMsg.append("当前位置(").append(activity.currentX).append(",").append(activity.currentY).append(")地形: ").append(fromArea);
            logMsg.append(", 目标位置(").append(newX).append(",").append(newY).append(")地形: ").append(toArea);
            
            if (cfgFrom == null) {
                logMsg.append(", 当前地形配置缺失: ").append(fromArea);
            }
            if (cfgTo == null) {
                logMsg.append(", 目标地形配置缺失: ").append(toArea);
            }
            
            Log.w("MoveDebug", logMsg.toString());
            activity.tvAreaDescription.setText("未知区域，无法移动");
            return;
        }

        // 高度差检查
        int fromHeight = activity.gameMap.getTerrainHeight(activity.currentX, activity.currentY);
        int toHeight = activity.gameMap.getTerrainHeight(newX, newY);
        int deltaHeight = Math.abs(toHeight - fromHeight);
        if (deltaHeight > 1) {
            // 详细日志提示高度差限制
            Log.w("MoveDebug", "高度差过大无法移动 - 当前位置(" + activity.currentX + "," + activity.currentY + ")高度: " + fromHeight + 
                    ", 目标位置(" + newX + "," + newY + ")高度: " + toHeight + ", 高度差: " + deltaHeight + " (最大允许: 1)");
            activity.tvAreaDescription.setText("高度差过大，无法移动");
            return;
        }

        // 体力/生命消耗
        int staminaCost = (deltaHeight == 0) ? 5 : 10;
        if (activity.stamina < staminaCost) {
            int lifeCost = staminaCost - activity.stamina;
            if (activity.life < lifeCost) {
                // 详细日志提示生命不足
                Log.w("MoveDebug", "生命不足无法移动 - 当前位置(" + activity.currentX + "," + activity.currentY + "), 目标位置(" + newX + "," + newY + 
                        "), 体力需求: " + staminaCost + ", 当前体力: " + activity.stamina + ", 生命需求: " + lifeCost + ", 当前生命: " + activity.life);
                activity.tvAreaDescription.setText("生命不足，无法移动");
                return;
            }
            activity.life = Math.max(0, activity.life - lifeCost);
            activity.stamina = 0;
            activity.tvAreaDescription.setText("体力不足，移动消耗 " + lifeCost + " 点生命");
        } else {
            activity.stamina -= staminaCost;
            activity.tvAreaDescription.setText("");
        }

        // 难度系统：移动额外消耗
        DifficultyManager difficultyManager = DifficultyManager.getInstance();
        Map<String, Integer> moveCosts = difficultyManager.getMoveCost(activity.difficulty);
        
        // 基础消耗 + 难度额外消耗
        activity.hunger = Math.max(0, activity.hunger - 5 - moveCosts.get("hunger"));
        activity.thirst = Math.max(0, activity.thirst - 5 - moveCosts.get("thirst"));
        activity.stamina = Math.max(0, activity.stamina - moveCosts.get("stamina"));

        // 特殊地形消耗
        if (toArea.equals("海洋")) {
            activity.stamina = Math.max(0, activity.stamina - 5);
            activity.hunger = Math.max(0, activity.hunger - 5);
            activity.tvAreaDescription.append("，游泳额外消耗5体力和5饥饿");
        } else if (toArea.equals("深海")) {
            activity.stamina = Math.max(0, activity.stamina - 10);
            activity.hunger = Math.max(0, activity.hunger - 10);
            activity.tvAreaDescription.append("，深海游泳额外消耗10体力和10饥饿");
        }

        // 体温变化
        int tempChange = 0;
        if (toArea.equals("雪原") || toArea.equals("海洋")) tempChange = -1;
        else if (toArea.equals("雪山") || toArea.equals("深海")) tempChange = -2;

        if (tempChange != 0) {
            activity.temperature = Math.max(Constant.TEMPERATURE_MIN, activity.temperature + tempChange);
            activity.uiUpdater.updateTemperatureDisplay();
        }

        // 高度差1时绳索检查
        if (deltaHeight == 1) {
            // 使用MainActivity中的背包数据，而不是从数据库获取
            boolean hasRope = false;
            String ropeType = "";

            if (activity.backpack != null) {
                if (activity.backpack.containsKey(ItemConstants.ITEM_GRASS_ROPE) && activity.backpack.get(ItemConstants.ITEM_GRASS_ROPE) > 0) {
                    ropeType = ItemConstants.ITEM_GRASS_ROPE;
                    hasRope = true;
                } else if (activity.backpack.containsKey(ItemConstants.ITEM_REINFORCED_ROPE) && activity.backpack.get(ItemConstants.ITEM_REINFORCED_ROPE) > 0) {
                    ropeType = ItemConstants.ITEM_REINFORCED_ROPE;
                    hasRope = true;
                } else if (activity.backpack.containsKey(ItemConstants.ITEM_HARD_ROPE) && activity.backpack.get(ItemConstants.ITEM_HARD_ROPE) > 0) {
                    ropeType = ItemConstants.ITEM_HARD_ROPE;
                    hasRope = true;
                }
            }

            if (!hasRope) {
                // 详细日志提示绳索需求
                Log.w("MoveDebug", "需要绳索才能移动 - 当前位置(" + activity.currentX + "," + activity.currentY + ")高度: " + fromHeight + 
                        ", 目标位置(" + newX + "," + newY + ")高度: " + toHeight + ", 高度差: " + deltaHeight + 
                        ", 背包中无可用绳索");
                activity.tvAreaDescription.setText("需要绳索才能移动到该区域");
                return;
            } else {
                // 详细日志提示绳索使用
                Log.i("MoveDebug", "使用绳索移动 - 当前位置(" + activity.currentX + "," + activity.currentY + ")高度: " + fromHeight + 
                        ", 目标位置(" + newX + "," + newY + ")高度: " + toHeight + ", 高度差: " + deltaHeight + 
                        ", 使用绳索: " + ropeType);
                // 更新数据库和内存中的背包数据
                activity.dataManager.getDbHelper().updateBackpackItem(MyApplication.currentUserId, ropeType, -1);
                
                // 同步更新内存中的背包数据
                if (activity.backpack.containsKey(ropeType)) {
                    int currentCount = activity.backpack.get(ropeType);
                    if (currentCount > 0) {
                        activity.backpack.put(ropeType, currentCount - 1);
                    }
                }
            }
        }

        // 执行移动
        activity.currentX = newX;
        activity.currentY = newY;
        activity.backgroundView.setCurrentCoord(activity.currentX, activity.currentY);
        
        // 处理移动随机事件
        RandomEventManager.RandomEvent moveEvent = RandomEventManager.handleMoveEvent(MyApplication.currentUserId, toArea);
        
        // 应用随机事件效果
        if (moveEvent != null) {
            RandomEventManager.applyEventEffects(MyApplication.currentUserId, moveEvent);
            
            // 如果是迷路事件，增加额外时间消耗
            if (moveEvent.type == RandomEventManager.EventType.GET_LOST) {
                int timePenalty = RandomEventManager.getLostTimePenalty(MyApplication.currentUserId, moveEvent);
                activity.gameHour += timePenalty;
                
                // 处理跨天
                if (activity.gameHour >= 24) {
                    activity.gameHour -= 24;
                    activity.gameDay++;
                }
                
                activity.uiUpdater.updateTimeDisplay();
            }
        }
        
        // 详细日志记录移动成功，增加地图名称
        String currentMap = activity.gameMap.getCurrentMapType();
        String mapName = "main_world".equals(currentMap) ? "主世界" : "奇幻大陆";
        Log.i("MoveDebug", "移动成功 - " + mapName + " - 从位置(" + (activity.currentX - dx) + "," + (activity.currentY - dy) + ")地形: " + fromArea + 
                " 移动到位置(" + activity.currentX + "," + activity.currentY + ")地形: " + toArea + 
                ", 体力消耗: " + staminaCost + ", 剩余体力: " + activity.stamina);
        
        // 移动成功时增加游戏时间1小时
        increaseGameTimeAfterAction();
        
        activity.dataManager.saveAllCriticalData();
        activity.uiUpdater.updateAreaInfo();
        activity.uiUpdater.updateStatusDisplays();
    }

    // 采集处理
    private void handleCollect() {
        String areaType = activity.gameMap.getTerrainType(activity.currentX, activity.currentY);
        
        // 检查是否遭遇野生动物（10%概率）
        WildAnimalEncounterManager encounterManager = WildAnimalEncounterManager.getInstance();
        if (encounterManager.checkForWildAnimalEncounter()) {
            Log.d("WildAnimalEncounter", "在" + areaType + "遭遇野生动物");
            encounterManager.handleWildAnimalEncounter(areaType, activity);
            return; // 遭遇野生动物后不执行普通采集
        }
        
        int currentAreaLevel = Constant.getAreaLevel(areaType);
        TechManager techManager = TechManager.getInstance(activity);
        Tech baseGathering = techManager.getTechById("base_gathering");
        Tech wildGathering = techManager.getTechById("wild_gathering");
        Tech advancedGathering = techManager.getTechById("advanced_gathering");

        List<String> bonusTips = new ArrayList<>();
        if (baseGathering != null && baseGathering.level > 0) {
            String[] baseBonuses = {"杂草、木头、石头", "浆果、苹果、橡果", "藤蔓、树脂、木炭"};
            int levelIndex = baseGathering.level - 1;
            if (levelIndex < baseBonuses.length) {
                bonusTips.add("基础采集术 Lv" + baseGathering.level + "：额外获得1个" + baseBonuses[levelIndex]);
            }
        }

        if (wildGathering != null && wildGathering.level > 0) {
            bonusTips.add("荒野采集术 Lv" + wildGathering.level + "：额外获得" + wildGathering.level + "个资源");
        }

        if (advancedGathering != null && advancedGathering.level > 0) {
            bonusTips.add("高级采集术 Lv" + advancedGathering.level + "：额外获得" + advancedGathering.level + "个资源");
        }

        if (!bonusTips.isEmpty()) {
            activity.tvTip.setText("科技加成：\n" + String.join("\n", bonusTips));
        } else {
            activity.tvTip.setText("普通采集");
        }

        performCollect(areaType, bonusTips);
    }

    // 执行采集
    private void performCollect(String areaType, List<String> bonusTipsList) {
        AreaResourceManager.AreaResource areaResource = AreaResourceManager.getInstance().getAreaResource(areaType);
        if (areaResource == null) {
            activity.tvScrollTip.setText("未知区域，无法采集");
            return;
        }

        // 关键修改：通过getDbHelper()访问dbHelper
        Map<String, Object> cdInfo = activity.dataManager.getDbHelper().getResourceCDInfo(MyApplication.currentUserId, activity.currentX, activity.currentY);
        int dbCollectCount = activity.dataManager.getIntValue(cdInfo.get("collect_count"), 0);
        long lastCollectTime = activity.dataManager.getLongValue(cdInfo.get("last_collect_time"), 0);

        if (dbCollectCount >= areaResource.maxCollectTimes) {
            long remainingCD = (lastCollectTime + areaResource.recoveryMinutes * 60 * 1000) - System.currentTimeMillis();
            String msg = remainingCD > 0 ?
                    String.format("该区域采集次数已达上限，剩余冷却：%d分%d秒", remainingCD / 60000, (remainingCD % 60000) / 1000) :
                    "该区域采集次数已达上限，请等待刷新";
            activity.tvScrollTip.setText(msg);
            return;
        }

        // 处理随机事件
        RandomEventManager.RandomEvent collectEvent = RandomEventManager.handleCollectEvent(MyApplication.currentUserId, areaType);
        int collectionMultiplier = 1;
        
        // 应用随机事件效果
        if (collectEvent != null) {
            RandomEventManager.applyEventEffects(MyApplication.currentUserId, collectEvent);
            
            // 如果是连锁采集，获取资源倍数
            if (collectEvent.type == RandomEventManager.EventType.CHAIN_COLLECTION) {
                collectionMultiplier = RandomEventManager.getCollectionMultiplier(MyApplication.currentUserId, collectEvent);
            }
        }

        // 检查工具与地形匹配情况
        Log.d("ToolDebug", "检查工具匹配: toolType=" + activity.currentEquip + ", areaType=" + areaType);
        
        // 检查工具等级和区域等级匹配情况
        String matchResult = ToolUtils.checkToolAreaMatch(activity.currentEquip, areaType);
        Log.i("ToolDebug", "工具等级匹配结果: " + matchResult);
        
        // 检查工具等级是否足够（新增等级匹配验证）
        if (!isToolLevelSufficient(activity.currentEquip, areaType)) {
            Log.d("ToolDebug", "工具等级不足: " + activity.currentEquip + " -> " + areaType);
            activity.tvScrollTip.setText("工具等级不足，无法采集该区域");
            return;
        }
        
        // 检查工具类型是否匹配（等级0区域允许任意工具，其他区域检查类型匹配）
        boolean isToolTypeMatched = Constant.isToolSuitableForArea(activity.currentEquip, areaType);
        if (!isToolTypeMatched) {
            Log.d("ToolDebug", "工具类型不匹配: " + activity.currentEquip + " -> " + areaType + "（但允许采集，无加成）");
            // 不阻止采集，只是没有加成
        } else {
            Log.d("ToolDebug", "工具类型匹配: " + activity.currentEquip + " -> " + areaType + "（有加成）");
        }

        if (activity.stamina < 3) {
            activity.tvScrollTip.setText("体力不足，无法采集");
            return;
        }

        // 消耗体力
        int baseStaminaCost = 5;
        int finalStaminaCost = calculateStaminaCostWithBonuses(baseStaminaCost, bonusTipsList);
        activity.stamina = Math.max(0, activity.stamina - finalStaminaCost);
        activity.uiUpdater.updateStatusDisplays();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("stamina", activity.stamina);
                // 关键修改：通过getDbHelper()访问dbHelper
                activity.dataManager.getDbHelper().updateUserStatus(MyApplication.currentUserId, updates);
                return null;
            }
        }.execute();

        // 处理装备耐久和加成
        int toolBonus = 0;
        if (activity.currentEquip != null && !activity.currentEquip.isEmpty() && !activity.currentEquip.equals("无")) {
            // 关键修改：通过getDbHelper()访问dbHelper
            int currentDurability = activity.dataManager.getDbHelper().getDurability(MyApplication.currentUserId, activity.currentEquip);
            
            // 只有工具类型匹配时才给予加成
            if (isToolTypeMatched) {
                toolBonus = getToolResourceBonus(activity.currentEquip);
            }
            
            Log.d("ToolDebug", "当前装备: " + activity.currentEquip + ", 耐久度: " + currentDurability + ", 工具加成: +" + toolBonus + ", 类型匹配: " + isToolTypeMatched);
            
            if (currentDurability <= 0) {
                Log.w("ToolDebug", "工具已损坏: " + activity.currentEquip + "，无法采集");
                activity.runOnUiThread(() -> activity.tvScrollTip.setText(activity.currentEquip + "已损坏，无法采集"));
                return;
            }

            // 只有工具类型匹配且有实际加成时才消耗耐久度
            if (isToolTypeMatched && toolBonus > 0) {
                // 立即更新耐久度，不等待异步任务
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        // 直接调用useTool方法，确保耐久度正确减少
                        activity.dataManager.getDbHelper().useTool(MyApplication.currentUserId, activity.currentEquip);
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        // 检查耐久度是否已耗尽
                        int updatedDurability = activity.dataManager.getDbHelper().getDurability(MyApplication.currentUserId, activity.currentEquip);
                        if (updatedDurability <= 0) {
                            activity.currentEquip = "无";
                            activity.dataManager.getDbHelper().updateToolEquipStatus(MyApplication.currentUserId, activity.currentEquip, false);
                        }
                        activity.uiUpdater.refreshEquipStatus();
                    }
                }.execute();
            } else if (isToolTypeMatched && toolBonus == 0) {
                Log.d("ToolDebug", "工具类型匹配但无加成，不消耗耐久度: " + activity.currentEquip + " -> " + areaType);
            } else {
                Log.d("ToolDebug", "工具类型不匹配，不消耗耐久度: " + activity.currentEquip + " -> " + areaType);
            }
        }

        // 生成采集物品 - 使用稀有度系统
        List<AreaResourceManager.CollectedItem> baseItems = AreaResourceManager.getInstance().generateRarityBasedResources(areaType, activity.difficulty);
        
        // 应用连锁采集倍数
        for (AreaResourceManager.CollectedItem item : baseItems) {
            item.count *= collectionMultiplier;
        }
        
        // 计算工具等级和区域等级匹配的额外加成
        int levelBonus = calculateLevelBonus(activity.currentEquip, areaType);
        int totalBonus = toolBonus + levelBonus;
        
        if (totalBonus > 0 && !baseItems.isEmpty()) {
            for (AreaResourceManager.CollectedItem item : baseItems) {
                item.count += totalBonus;
            }
            
            if (toolBonus > 0 && levelBonus > 0) {
                bonusTipsList.add("工具加成：" + activity.currentEquip + "额外获得" + toolBonus + "个资源");
                bonusTipsList.add("额外获得" + levelBonus + "个资源");
                Log.i("ToolDebug", "资源加成计算: 工具加成=" + toolBonus + ", 等级匹配加成=" + levelBonus + ", 总加成=" + totalBonus);
            } else if (toolBonus > 0) {
                bonusTipsList.add("工具加成：" + activity.currentEquip + "额外获得" + toolBonus + "个资源");
                Log.i("ToolDebug", "资源加成计算: 工具加成=" + toolBonus + ", 总加成=" + totalBonus);
            } else if (levelBonus > 0) {
                bonusTipsList.add("额外获得" + levelBonus + "个资源");
                Log.i("ToolDebug", "资源加成计算: 等级匹配加成=" + levelBonus + ", 总加成=" + totalBonus);
            }
        }

        if (baseItems.isEmpty()) {
            activity.tvScrollTip.setText(areaType + "没有可采集的物品");
            return;
        }

        // 处理科技加成
        List<AreaResourceManager.CollectedItem> bonusItems = handleTechBonuses(areaType, bonusTipsList);
        List<AreaResourceManager.CollectedItem> allItems = new ArrayList<>(baseItems);
        allItems.addAll(bonusItems);

        // 保存到数据库
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                int newAreaCount = dbCollectCount + 1;
                // 关键修改：通过getDbHelper()访问dbHelper
                activity.dataManager.getDbHelper().updateAreaCollectCount(
                        MyApplication.currentUserId, activity.currentX, activity.currentY, newAreaCount, System.currentTimeMillis()
                );
                // 关键修改：通过getDbHelper()访问dbHelper
                int newGlobalCount = activity.dataManager.getDbHelper().incrementGlobalCollectTimes(MyApplication.currentUserId);
                
                // 增加寻宝探险次数
                int newExplorationCount = activity.dataManager.getDbHelper().incrementExplorationTimes(MyApplication.currentUserId);

                for (AreaResourceManager.CollectedItem item : allItems) {
                    // 关键修改：通过getDbHelper()访问dbHelper
                    activity.dataManager.getDbHelper().updateBackpackItem(MyApplication.currentUserId, item.name, item.count);
                }
                return newGlobalCount;
            }

            @Override
            protected void onPostExecute(Integer newGlobalCount) {
                if (newGlobalCount == null) {
                    activity.tvScrollTip.setText("采集失败");
                    return;
                }
                activity.currentCollectTimes = newGlobalCount;

                // 采集成功时增加游戏时间1小时
                increaseGameTimeAfterAction();

                StringBuilder tip = new StringBuilder("采集成功！获得：\n");
                for (AreaResourceManager.CollectedItem item : allItems) {
                    Rarity rarity = ItemRarityManager.getItemRarity(item.name);
                    tip.append("- ").append(item.name).append(" ×").append(item.count)
                      .append(" [").append(rarity.getDisplayName()).append("]\n");
                }
                
                // 添加寻宝探险成就次数日志 - 需要从数据库重新查询
                int explorationCount = activity.dataManager.getDbHelper().getExplorationTimes(MyApplication.currentUserId);
                //tip.append("\n寻宝探险成就次数: ").append(explorationCount);

                activity.tvScrollTip.setText(tip.toString().trim());
                
                // 添加日志到Logcat
                Log.d("EventHandler", "寻宝探险成就次数已更新: " + explorationCount);
            }
        }.execute();
    }

    // 计算工具加成
    private int getToolResourceBonus(String toolType) {
        if (toolType.contains("石质")) return 1;
        if (toolType.contains("铁质")) return 2;
        if (toolType.contains("钻石")) return 3;
        return 0;
    }

    // 计算体力消耗
    private int calculateStaminaCostWithBonuses(int baseCost, List<String> bonuses) {
        int cost = baseCost;
        for (String bonus : bonuses) {
            if (bonus.contains("体力消耗减少20%")) cost = (int) (cost * 0.8);
            else if (bonus.contains("体力消耗减少50%")) cost = (int) (cost * 0.5);
        }
        return Math.max(1, cost);
    }

    // 处理科技加成物品
    private List<AreaResourceManager.CollectedItem> handleTechBonuses(String areaType, List<String> bonuses) {
        List<AreaResourceManager.CollectedItem> bonusItems = new ArrayList<>();
        AreaResourceManager.AreaResource areaResource = AreaResourceManager.getInstance().getAreaResource(areaType);
        if (areaResource == null) return bonusItems;

        for (String bonus : bonuses) {
            if (bonus.contains("额外获得1个")) {
                String resStr = bonus.split("额外获得1个")[1];
                String[] resArray = resStr.split("、");
                String randomRes = resArray[activity.random.nextInt(resArray.length)];
                bonusItems.add(new AreaResourceManager.CollectedItem(randomRes, 1));
            } else if (bonus.contains("额外获得") && bonus.contains("个资源")) {
                int count = Integer.parseInt(bonus.replaceAll("[^0-9]", ""));
                AreaResourceManager.ResourceItem randomItem = areaResource.resources.get(
                        activity.random.nextInt(areaResource.resources.size())
                );
                bonusItems.add(new AreaResourceManager.CollectedItem(randomItem.name, count));
            }
        }
        return bonusItems;
    }

    // 返回标题页
    public void backToTitlePage() {
        activity.startActivity(new Intent(activity, TitleActivity.class));
        activity.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_collect) {
            // 检查当前位置是否有传送门
            if (activity.gameMap.hasPortalAtCurrentPosition(MyApplication.currentUserId)) {
                handleTeleport();
            } else {
                handleCollect();
            }
        } else if (id == R.id.btn_backpack) {
            activity.dataManager.saveAllCriticalData();
            activity.startActivity(new Intent(activity, BackpackActivity.class));
        } else if (id == R.id.btn_equipment) {
            activity.dataManager.saveAllCriticalData();
            activity.startActivity(new Intent(activity, EquipmentActivity.class));
        } else if (id == R.id.btn_functions) {
            activity.dataManager.saveAllCriticalData();
            activity.startActivity(new Intent(activity, FunctionListActivity.class));
        }
    }

    /**
     * 处理传送功能
     */
    private void handleTeleport() {
        // 获取当前地图
        String currentMap = activity.gameMap.getCurrentMapType();
        String targetMap = "main_world".equals(currentMap) ? "fantasy_continent" : "main_world";
        String targetMapName = "main_world".equals(currentMap) ? "奇幻大陆" : "主世界";
        
        // 直接传送，不显示确认对话框
        boolean success = activity.gameMap.switchMap(MyApplication.currentUserId, targetMap);
        if (success) {
            Toast.makeText(activity, "成功传送到" + targetMapName, Toast.LENGTH_SHORT).show();
            
            // 重新加载用户坐标数据
            activity.dataManager.loadGameData();
            
            // 强制刷新背景视图显示新坐标
            if (activity.backgroundView != null) {
                activity.backgroundView.setCurrentCoord(activity.currentX, activity.currentY, true);
                activity.backgroundView.invalidate();
                activity.backgroundView.postInvalidate();
            }
            
            // 刷新界面显示
            activity.uiUpdater.updateAreaInfo();
            
            // 延迟更新按钮文本，确保坐标数据已完全加载
            activity.backgroundView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateCollectButtonText();
                    Log.d("TeleportDebug", "传送后按钮状态更新 - 当前位置: (" + activity.currentX + ", " + activity.currentY + "), 是否有传送门: " + activity.gameMap.hasPortalAtCurrentPosition(MyApplication.currentUserId));
                }
            }, 100);
            
            Log.i("TeleportDebug", "传送成功 - 从" + currentMap + "传送到" + targetMap + ", 新坐标: (" + activity.currentX + ", " + activity.currentY + ")");
        } else {
            Toast.makeText(activity, "传送失败", Toast.LENGTH_SHORT).show();
            Log.e("TeleportDebug", "传送失败 - 从" + currentMap + "传送到" + targetMap);
        }
    }



    /**
     * 更新采集按钮文本
     */
    public void updateCollectButtonText() {
        android.widget.Button btnCollect = activity.findViewById(R.id.btn_collect);
        if (activity.gameMap.hasPortalAtCurrentPosition(MyApplication.currentUserId)) {
            // 在传送门地形，显示"传送"
            btnCollect.setText("传送");
        } else {
            // 不在传送门地形，显示"采集"
            btnCollect.setText("采集");
        }
    }
    // 新增：cdRefreshRunnable的getter方法
    public Runnable getCdRefreshRunnable() {
        return cdRefreshRunnable;
    }

    // 新增：cdRefreshRunnable的setter方法
    public void setCdRefreshRunnable(Runnable runnable) {
        this.cdRefreshRunnable = runnable;
    }

    /**
     * 计算工具等级和区域等级匹配的额外加成（修改方法）
     * @param equipType 工具类型
     * @param areaType 区域类型
     * @return 等级匹配加成数量（工具等级>区域等级时返回差值，否则返回0）
     */
    private int calculateLevelBonus(String equipType, String areaType) {
        ToolLevel toolLevel = ToolUtils.getToolLevel(equipType);
        
        // 获取区域等级（根据区域类型判断）
        int areaLevel = getAreaLevel(areaType);
        
        if (toolLevel == ToolLevel.UNKNOWN) {
            return 0;
        }
        
        int toolLevelValue = toolLevel.ordinal();
        
        // 工具等级高于区域等级时，给予额外加成
        if (toolLevelValue > areaLevel) {
            int levelDifference = toolLevelValue - areaLevel;
            // 每高一级额外获得1个资源
            return levelDifference;
        }
        
        return 0;
    }

    /**
     * 获取区域等级（根据区域类型判断）
     * @param areaType 区域类型
     * @return 区域等级
     */
    private int getAreaLevel(String areaType) {
        // 统一使用Constant中的区域等级定义
        return Constant.getAreaLevel(areaType);
    }

    /**
     * 检查工具等级是否足够采集该区域
     * @param equipType 工具类型
     * @param areaType 区域类型
     * @return 工具等级是否足够
     */
    private boolean isToolLevelSufficient(String equipType, String areaType) {
        ToolLevel toolLevel = ToolUtils.getToolLevel(equipType);
        
        // 获取区域等级
        int areaLevel = getAreaLevel(areaType);
        
        // 空手（UNKNOWN工具）可以采集等级0和等级1区域
        if (toolLevel == ToolLevel.UNKNOWN) {
            return areaLevel <= 1; // 等级0和等级1区域允许空手采集
        }
        
        int toolLevelValue = toolLevel.getLevel(); // 修复：使用getLevel()而不是ordinal()
        
        // 工具等级必须大于等于区域等级才能采集
        return toolLevelValue >= areaLevel;
    }

    /**
     * 动作成功后增加游戏时间1小时
     */
    private void increaseGameTimeAfterAction() {
        try {
            activity.gameHour++;
            
            // 状态效果管理器处理小时变化
            StatusEffectManager.onHourPassed(MyApplication.currentUserId);
            
            // 检查是否超过一天
            if (activity.gameHour >= Constant.GAME_HOURS_PER_DAY) {
                activity.gameHour = 0;
                activity.gameDay++;
            }
            
            // 刷新时间显示
            activity.uiUpdater.updateTimeDisplay();
            
            // 检查刷新时间
            if (activity.gameHour == Constant.REFRESH_HOUR && activity.gameDay > activity.lastRefreshDay) {
                activity.timeManager.resetCollectTimes();
                activity.lastRefreshDay = activity.gameDay;
            }
            
            // 保存时间数据
            activity.timeManager.saveTimeData();
            
            Log.i("GameTime", "动作成功，游戏时间增加1小时，当前时间: 第" + activity.gameDay + "天 " + activity.gameHour + "时");
        } catch (Exception e) {
            Log.e("GameTime", "增加游戏时间时出错: " + e.getMessage());
        }
    }
}