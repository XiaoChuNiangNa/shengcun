package com.example.myapplication3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandler implements View.OnClickListener {
    // 使用WeakReference防止内存泄漏
    private final WeakReference<MainActivity> activityRef;
    private Runnable cdRefreshRunnable;
    private final Random random;

    // 替代AsyncTask的执行器和Handler
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public EventHandler(MainActivity activity) {
        this.activityRef = new WeakReference<>(activity);
        this.random = new Random();

        // 初始化线程池和主线程Handler
        this.executorService = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 获取MainActivity实例，如果活动已销毁则返回null
     */
    private MainActivity getActivity() {
        return activityRef.get();
    }

    // 初始化点击监听
    public void initClickListeners() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        try {
            // 确保btnCollect字段被初始化（解决可能的符号解析问题）
            activity.btnCollect = activity.findViewById(R.id.btn_collect);
            
            View btnCollect = activity.findViewById(R.id.btn_collect);
            View btnUp = activity.findViewById(R.id.btn_up);
            View btnDown = activity.findViewById(R.id.btn_down);
            View btnLeft = activity.findViewById(R.id.btn_left);
            View btnRight = activity.findViewById(R.id.btn_right);
            View btnQuestPanel = activity.findViewById(R.id.btn_quest_panel);

            if (btnCollect != null) btnCollect.setOnClickListener(this);
            if (btnUp != null) btnUp.setOnClickListener(v -> move(0, -1));
            if (btnDown != null) btnDown.setOnClickListener(v -> move(0, 1));
            if (btnLeft != null) btnLeft.setOnClickListener(v -> move(-1, 0));
            if (btnRight != null) btnRight.setOnClickListener(v -> move(1, 0));
            if (activity.ivSetting != null) activity.ivSetting.setOnClickListener(v -> {
                MainActivity currentActivity = getActivity();
                if (currentActivity != null) {
                    new SettingDialogFragment().show(currentActivity.getSupportFragmentManager(), "setting_dialog");
                }
            });
            if (activity.btnBackpack != null) activity.btnBackpack.setOnClickListener(this);
            if (activity.btnEquipment != null) activity.btnEquipment.setOnClickListener(this);
            if (activity.btnFunctions != null) activity.btnFunctions.setOnClickListener(this);
            if (btnQuestPanel != null) btnQuestPanel.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("EventHandler", "初始化点击监听器失败: " + e.getMessage(), e);
        }
    }

    // 移动处理
    public void move(int dx, int dy) {
        MainActivity activity = getActivity();
        if (activity == null) return;

        int newX = activity.currentX + dx;
        int newY = activity.currentY + dy;

        // 边界检查
        if (newX < Constant.MAP_MIN || newX > Constant.MAP_MAX || newY < Constant.MAP_MIN || newY > Constant.MAP_MAX) {
            Log.w("MoveDebug", "地图边界限制 - 当前位置(" + activity.currentX + "," + activity.currentY + "), 目标位置(" + newX + "," + newY + ")超出地图边界(" + Constant.MAP_MIN + "-" + Constant.MAP_MAX + ")");
            if (activity.tvAreaDescription != null) {
                activity.tvAreaDescription.setText("已到达地图边界");
            }
            return;
        }

        // 地形检查
        String fromArea = activity.gameMap.getTerrainType(activity.currentX, activity.currentY);
        String toArea = activity.gameMap.getTerrainType(newX, newY);
        Constant.AreaConfig cfgFrom = Constant.AREA_CONFIG.get(fromArea);
        Constant.AreaConfig cfgTo = Constant.AREA_CONFIG.get(toArea);

        if (cfgFrom == null || cfgTo == null) {
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
            if (activity.tvAreaDescription != null) {
                activity.tvAreaDescription.setText("未知区域，无法移动");
            }
            return;
        }

        // 高度差检查
        int fromHeight = activity.gameMap.getTerrainHeight(activity.currentX, activity.currentY);
        int toHeight = activity.gameMap.getTerrainHeight(newX, newY);
        int deltaHeight = Math.abs(toHeight - fromHeight);
        if (deltaHeight > 1) {
            Log.w("MoveDebug", "高度差过大无法移动 - 当前位置(" + activity.currentX + "," + activity.currentY + ")高度: " + fromHeight +
                    ", 目标位置(" + newX + "," + newY + ")高度: " + toHeight + ", 高度差: " + deltaHeight + " (最大允许: 1)");
            if (activity.tvAreaDescription != null) {
                activity.tvAreaDescription.setText("高度差过大，无法移动");
            }
            return;
        }

        // 体力/生命消耗
        int staminaCost = (deltaHeight == 0) ? 5 : 10;
        if (activity.stamina < staminaCost) {
            int lifeCost = staminaCost - activity.stamina;
            if (activity.life < lifeCost) {
                Log.w("MoveDebug", "生命不足无法移动 - 当前位置(" + activity.currentX + "," + activity.currentY + "), 目标位置(" + newX + "," + newY +
                        "), 体力需求: " + staminaCost + ", 当前体力: " + activity.stamina + ", 生命需求: " + lifeCost + ", 当前生命: " + activity.life);
                if (activity.tvAreaDescription != null) {
                    activity.tvAreaDescription.setText("生命不足，无法移动");
                }
                return;
            }
            activity.life = Math.max(0, activity.life - lifeCost);
            activity.stamina = 0;
            if (activity.tvAreaDescription != null) {
                activity.tvAreaDescription.setText("体力不足，移动消耗 " + lifeCost + " 点生命");
            }
        } else {
            activity.stamina -= staminaCost;
            if (activity.tvAreaDescription != null) {
                activity.tvAreaDescription.setText("");
            }
        }

        // 难度系统：移动额外消耗
        DifficultyManager difficultyManager = DifficultyManager.getInstance();
        Map<String, Integer> moveCosts = difficultyManager.getMoveCost(activity.difficulty);

        // 基础消耗 + 难度额外消耗
        activity.hunger = Math.max(0, activity.hunger - 5 - moveCosts.getOrDefault("hunger", 0));
        activity.thirst = Math.max(0, activity.thirst - 5 - moveCosts.getOrDefault("thirst", 0));
        activity.stamina = Math.max(0, activity.stamina - moveCosts.getOrDefault("stamina", 0));

        // 特殊地形消耗
        if (toArea.equals("海洋")) {
            activity.stamina = Math.max(0, activity.stamina - 5);
            activity.hunger = Math.max(0, activity.hunger - 5);
            if (activity.tvAreaDescription != null) {
                activity.tvAreaDescription.append("，游泳额外消耗5体力和5饥饿");
            }
        } else if (toArea.equals("深海")) {
            activity.stamina = Math.max(0, activity.stamina - 10);
            activity.hunger = Math.max(0, activity.hunger - 10);
            if (activity.tvAreaDescription != null) {
                activity.tvAreaDescription.append("，深海游泳额外消耗10体力和10饥饿");
            }
        }

        // 体温变化
        int tempChange = 0;
        if (toArea.equals("雪原") || toArea.equals("海洋")) tempChange = -1;
        else if (toArea.equals("雪山") || toArea.equals("深海")) tempChange = -2;

        if (tempChange != 0) {
            activity.temperature = Math.max(Constant.TEMPERATURE_MIN, activity.temperature + tempChange);
            if (activity.uiUpdater != null) {
                activity.uiUpdater.updateTemperatureDisplay();
            }
        }

        // 高度差1时绳索检查
        if (deltaHeight == 1) {
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
                Log.w("MoveDebug", "需要绳索才能移动 - 当前位置(" + activity.currentX + "," + activity.currentY + ")高度: " + fromHeight +
                        ", 目标位置(" + newX + "," + newY + ")高度: " + toHeight + ", 高度差: " + deltaHeight +
                        ", 背包中无可用绳索");
                if (activity.tvAreaDescription != null) {
                    activity.tvAreaDescription.setText("需要绳索才能移动到该区域");
                }
                return;
            } else {
                Log.i("MoveDebug", "使用绳索移动 - 当前位置(" + activity.currentX + "," + activity.currentY + ")高度: " + fromHeight +
                        ", 目标位置(" + newX + "," + newY + ")高度: " + toHeight + ", 高度差: " + deltaHeight +
                        ", 使用绳索: " + ropeType);
                // 更新数据库和内存中的背包数据
                if (activity.dataManager != null && activity.dataManager.getDbHelper() != null) {
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
        }

        // 执行移动
        activity.currentX = newX;
        activity.currentY = newY;
        if (activity.backgroundView != null) {
            activity.backgroundView.setCurrentCoord(activity.currentX, activity.currentY);
        }

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

                if (activity.uiUpdater != null) {
                    activity.uiUpdater.updateTimeDisplay();
                }
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

        // 保存数据和更新UI
        if (activity.dataManager != null) {
            activity.dataManager.saveAllCriticalData();
        }
        if (activity.uiUpdater != null) {
            activity.uiUpdater.updateAreaInfo();
            activity.uiUpdater.updateStatusDisplays();
        }
    }

    // 采集处理
    private void handleCollect() {
        MainActivity uiActivity = getActivity();
        if (uiActivity == null) return;

        String areaType = uiActivity.gameMap.getTerrainType(uiActivity.currentX, uiActivity.currentY);

        // 检查野生动物遭遇
        if (checkWildAnimalEncounter(areaType)) {
            return; // 遭遇野生动物后不执行普通采集
        }

        // 获取科技加成信息
        List<String> bonusTips = getTechGatheringBonuses();

        // 显示采集提示
        displayGatheringTips(bonusTips);

        // 执行实际采集
        performCollect(areaType, bonusTips);
    }

    /**
     * 检查是否遭遇野生动物
     * @param areaType 当前区域类型
     * @return 是否遭遇野生动物
     */
    private boolean checkWildAnimalEncounter(String areaType) {
        MainActivity uiActivity = getActivity();
        if (uiActivity == null) return false;

        WildAnimalEncounterManager encounterManager = WildAnimalEncounterManager.getInstance();
        if (encounterManager.checkForWildAnimalEncounter()) {
            Log.d("WildAnimalEncounter", "在" + areaType + "遭遇野生动物");
            encounterManager.handleWildAnimalEncounter(areaType, uiActivity);
            return true;
        }
        return false;
    }

    /**
     * 获取采集相关的科技加成
     * @return 科技加成提示列表
     */
    private List<String> getTechGatheringBonuses() {
        MainActivity uiActivity = getActivity();
        if (uiActivity == null) return new ArrayList<>();

        List<String> bonusTips = new ArrayList<>();
        TechManager techManager = TechManager.getInstance(uiActivity);

        // 获取基础采集术加成
        Tech baseGathering = techManager.getTechById("base_gathering");
        if (baseGathering != null && baseGathering.level > 0) {
            String[] baseBonuses = {"杂草、木头、石头", "浆果、苹果、橡果", "藤蔓、树脂、木炭"};
            int levelIndex = baseGathering.level - 1;
            if (levelIndex < baseBonuses.length) {
                bonusTips.add("基础采集术 Lv" + baseGathering.level + "：额外获得1个" + baseBonuses[levelIndex]);
            }
        }

        // 获取荒野采集术加成
        Tech wildGathering = techManager.getTechById("wild_gathering");
        if (wildGathering != null && wildGathering.level > 0) {
            bonusTips.add("荒野采集术 Lv" + wildGathering.level + "：额外获得" + wildGathering.level + "个资源");
        }

        // 获取高级采集术加成
        Tech advancedGathering = techManager.getTechById("advanced_gathering");
        if (advancedGathering != null && advancedGathering.level > 0) {
            bonusTips.add("高级采集术 Lv" + advancedGathering.level + "：额外获得" + advancedGathering.level + "个资源");
        }

        return bonusTips;
    }

    /**
     * 显示采集提示信息
     * @param bonusTips 科技加成提示列表
     */
    private void displayGatheringTips(List<String> bonusTips) {
        MainActivity uiActivity = getActivity();
        if (uiActivity == null) return;

        if (uiActivity.tvTip != null) {
            if (!bonusTips.isEmpty()) {
                uiActivity.tvTip.setText("科技加成：\n" + String.join("\n", bonusTips));
            } else {
                uiActivity.tvTip.setText("普通采集");
            }
        }
    }

    // 执行采集
    private void performCollect(String areaType, List<String> bonusTipsList) {
        MainActivity activity = getActivity();
        if (activity == null) return;

        AreaResourceManager.AreaResource areaResource = AreaResourceManager.getInstance().getAreaResource(areaType);
        if (areaResource == null) {
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText("未知区域，无法采集");
            }
            return;
        }

        // 关键修改：通过getDbHelper()访问dbHelper，增加非空判断
        if (activity.dataManager == null || activity.dataManager.getDbHelper() == null) {
            Log.e("EventHandler", "数据管理器或数据库帮助类为空，无法执行采集");
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText("采集失败：数据初始化异常");
            }
            return;
        }

        Map<String, Object> cdInfo = activity.dataManager.getDbHelper().getResourceCDInfo(MyApplication.currentUserId, activity.currentX, activity.currentY);
        int dbCollectCount = activity.dataManager.getIntValue(cdInfo.get("collect_count"), 0);
        long lastCollectTime = activity.dataManager.getLongValue(cdInfo.get("last_collect_time"), 0);

        if (dbCollectCount >= areaResource.maxCollectTimes) {
            long remainingCD = (lastCollectTime + areaResource.recoveryMinutes * 60 * 1000) - System.currentTimeMillis();
            String msg = remainingCD > 0 ?
                    String.format("该区域采集次数已达上限，剩余冷却：%d分%d秒", remainingCD / 60000, (remainingCD % 60000) / 1000) :
                    "该区域采集次数已达上限，请等待刷新";
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText(msg);
            }
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
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText("工具等级不足，无法采集该区域");
            }
            return;
        }

        // 检查工具类型是否匹配（等级0区域允许任意工具，其他区域检查类型匹配）
        boolean isToolTypeMatched = Constant.isToolSuitableForArea(activity.currentEquip, areaType);
        if (!isToolTypeMatched) {
            Log.d("ToolDebug", "工具类型不匹配: " + activity.currentEquip + " -> " + areaType + "（但允许采集，无加成）");
        } else {
            Log.d("ToolDebug", "工具类型匹配: " + activity.currentEquip + " -> " + areaType + "（有加成）");
        }

        if (activity.stamina < 3) {
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText("体力不足，无法采集");
            }
            return;
        }

        // 消耗体力
        int baseStaminaCost = 5;
        int finalStaminaCost = calculateStaminaCostWithBonuses(baseStaminaCost, bonusTipsList);
        activity.stamina = Math.max(0, activity.stamina - finalStaminaCost);

        if (activity.uiUpdater != null) {
            activity.uiUpdater.updateStatusDisplays();
        }

        final int newStamina = activity.stamina;
        // 替换AsyncTask为ExecutorService
        executorService.execute(() -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("stamina", newStamina);
            try {
                MainActivity currentActivity = getActivity();
                if (currentActivity != null && currentActivity.dataManager != null && currentActivity.dataManager.getDbHelper() != null) {
                    currentActivity.dataManager.getDbHelper().updateUserStatus(MyApplication.currentUserId, updates);
                }
            } catch (Exception e) {
                Log.e("EventHandler", "更新体力失败", e);
            }
        });

        // 处理装备耐久和加成
        int toolBonus = 0;
        if (activity.currentEquip != null && !activity.currentEquip.isEmpty() && !activity.currentEquip.equals("无")) {
            int currentDurability = activity.dataManager.getDbHelper().getDurability(MyApplication.currentUserId, activity.currentEquip);

            // 只有工具类型匹配时才给予加成
            if (isToolTypeMatched) {
                toolBonus = getToolResourceBonus(activity.currentEquip);
            }

            Log.d("ToolDebug", "当前装备: " + activity.currentEquip + ", 耐久度: " + currentDurability + ", 工具加成: +" + toolBonus + ", 类型匹配: " + isToolTypeMatched);

            if (currentDurability <= 0) {
                Log.w("ToolDebug", "工具已损坏: " + activity.currentEquip + "，无法采集");
                activity.runOnUiThread(() -> {
                    if (activity.tvScrollTip != null) {
                        activity.tvScrollTip.setText(activity.currentEquip + "已损坏，无法采集");
                    }
                });
                return;
            }

            // 只有工具类型匹配且有实际加成时才消耗耐久度
            if (isToolTypeMatched && toolBonus > 0) {
                // 立即更新耐久度，不等待异步任务
                final String currentTool = activity.currentEquip;
                // 替换AsyncTask为ExecutorService和Handler
                executorService.execute(() -> {
                    try {
                        MainActivity currentActivity = getActivity();
                        if (currentActivity != null && currentActivity.dataManager != null && currentActivity.dataManager.getDbHelper() != null) {
                            // 直接调用useTool方法，确保耐久度正确减少
                            currentActivity.dataManager.getDbHelper().useTool(MyApplication.currentUserId, currentTool);

                            // 在后台计算后，通过Handler更新UI
                            final int updatedDurability = currentActivity.dataManager.getDbHelper().getDurability(MyApplication.currentUserId, currentTool);
                            mainHandler.post(() -> {
                                MainActivity uiActivity = getActivity();
                                if (uiActivity != null) {
                                    if (updatedDurability <= 0) {
                                        uiActivity.currentEquip = "无";
                                        uiActivity.dataManager.getDbHelper().updateToolEquipStatus(MyApplication.currentUserId, uiActivity.currentEquip, false);
                                    }
                                    if (uiActivity.uiUpdater != null) {
                                        uiActivity.uiUpdater.refreshEquipStatus();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e("EventHandler", "更新工具耐久度失败", e);
                    }
                });
            } else if (isToolTypeMatched && toolBonus == 0) {
                Log.d("ToolDebug", "工具类型匹配但无加成，不消耗耐久度: " + activity.currentEquip + " -> " + areaType);
            } else {
                Log.d("ToolDebug", "工具类型不匹配，不消耗耐久度: " + activity.currentEquip + " -> " + areaType);
            }
        }

        // 生成采集物品 - 使用稀有度系统
        List<AreaResourceManager.CollectedItem> baseItems = AreaResourceManager.getInstance().generateRarityBasedResources(areaType, activity.difficulty);

        // 记录基础采集物品（无加成）
        List<AreaResourceManager.CollectedItem> noBonusItems = new ArrayList<>();
        for (AreaResourceManager.CollectedItem item : baseItems) {
            noBonusItems.add(new AreaResourceManager.CollectedItem(item.name, item.count));
        }

        // 应用连锁采集倍数
        for (AreaResourceManager.CollectedItem item : baseItems) {
            item.count *= collectionMultiplier;
        }

        // 计算工具等级和区域等级匹配的额外加成
        int levelBonus = calculateLevelBonus(activity.currentEquip, areaType);
        int totalBonus = toolBonus + levelBonus;

        // 记录工具和等级加成物品
        List<AreaResourceManager.CollectedItem> toolLevelBonusItems = new ArrayList<>();

        if (totalBonus > 0 && !baseItems.isEmpty()) {
            for (AreaResourceManager.CollectedItem item : baseItems) {
                item.count += totalBonus;
                toolLevelBonusItems.add(new AreaResourceManager.CollectedItem(item.name, totalBonus));
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
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText(areaType + "没有可采集的物品");
            }
            return;
        }

        // 处理科技加成
        List<AreaResourceManager.CollectedItem> bonusItems = handleTechBonuses(areaType, bonusTipsList);
        List<AreaResourceManager.CollectedItem> allItems = new ArrayList<>(baseItems);
        allItems.addAll(bonusItems);

        // 在采集前检查背包容量
        int totalItemsToAdd = allItems.stream().mapToInt(item -> item.count).sum();
        int currentBackpackCount = activity.dataManager.getDbHelper().getBackpackCurrentCount(MyApplication.currentUserId);
        int backpackCapacity = activity.dataManager.getDbHelper().getBackpackCapacity(MyApplication.currentUserId);

        if (currentBackpackCount + totalItemsToAdd > backpackCapacity) {
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText("背包容量不足，无法采集物品");
            }
            Log.w("EventHandler", "背包容量不足！需要容量: " + (currentBackpackCount + totalItemsToAdd) + ", 当前容量: " + backpackCapacity);
            return;
        }

        // 记录详细采集日志（只有容量足够时才记录）
        logDetailedCollectionInfo(areaType, noBonusItems, toolLevelBonusItems, bonusItems, allItems, collectionMultiplier);

        // 在开始采集操作之前获取采集前的背包数据
        Map<String, Integer> backpackBefore = activity.dataManager.getDbHelper().getBackpack(MyApplication.currentUserId);

        // 保存到数据库
        final List<AreaResourceManager.CollectedItem> itemsToSave = new ArrayList<>(allItems);
        final int collectedX = activity.currentX;
        final int collectedY = activity.currentY;
        final int countToUpdate = dbCollectCount + 1;

        // 替换AsyncTask为ExecutorService和Handler
        executorService.execute(() -> {
            Integer newGlobalCount = null;
            int explorationCount = 0; // 初始化变量
            try {
                MainActivity currentActivity = getActivity();
                if (currentActivity != null && currentActivity.dataManager != null && currentActivity.dataManager.getDbHelper() != null) {
                    // 关键修改：通过getDbHelper()访问dbHelper
                    currentActivity.dataManager.getDbHelper().updateAreaCollectCount(
                            MyApplication.currentUserId, collectedX, collectedY, countToUpdate, System.currentTimeMillis()
                    );
                    // 关键修改：通过getDbHelper()访问dbHelper
                    newGlobalCount = currentActivity.dataManager.getDbHelper().incrementGlobalCollectTimes(MyApplication.currentUserId);

                    // 增加寻宝探险次数
                    currentActivity.dataManager.getDbHelper().incrementExplorationTimes(MyApplication.currentUserId);
                    // 获取更新后的探险次数
                    explorationCount = currentActivity.dataManager.getDbHelper().getExplorationTimes(MyApplication.currentUserId);

                    // 添加物品到背包
                    boolean allItemsAdded = true;
                    for (AreaResourceManager.CollectedItem item : itemsToSave) {
                        // 关键修改：通过getDbHelper()访问dbHelper
                        boolean success = currentActivity.dataManager.getDbHelper().updateBackpackItem(MyApplication.currentUserId, item.name, item.count);
                        if (!success) {
                            Log.e("EventHandler", "添加物品到背包失败: " + item.name + " ×" + item.count);
                            allItemsAdded = false;
                        }
                    }

                    if (!allItemsAdded) {
                        Log.e("EventHandler", "部分物品添加失败，但继续执行其他操作");
                    }
                }
            } catch (Exception e) {
                Log.e("EventHandler", "保存采集数据失败", e);
            }

            // 使用Handler在主线程更新UI
            final Integer resultCount = newGlobalCount;
            final int finalExplorationCount = explorationCount;
            mainHandler.post(() -> {
                MainActivity uiActivity = getActivity();
                if (uiActivity != null) {
                    if (resultCount == null) {
                        if (uiActivity.tvScrollTip != null) {
                            uiActivity.tvScrollTip.setText("采集失败");
                        }
                        return;
                    }

                    // 处理容量不足的情况
                    if (resultCount == -1) {
                        if (uiActivity.tvScrollTip != null) {
                            uiActivity.tvScrollTip.setText("背包容量不足，无法采集物品");
                        }
                        Log.w("EventHandler", "采集失败：背包容量不足");
                        return;
                    }

                    uiActivity.currentCollectTimes = resultCount;

                    // 采集成功时增加游戏时间1小时
                    increaseGameTimeAfterAction();

                    StringBuilder tip = new StringBuilder("采集成功！获得：\n");
                    for (AreaResourceManager.CollectedItem item : itemsToSave) {
                        Rarity rarity = ItemRarityManager.getItemRarity(item.name);
                        tip.append("- ").append(item.name).append(" ×").append(item.count)
                                .append(" [").append(rarity.getDisplayName()).append("]\n");

                        // 更新任务进度 - 每种资源采集到的数量
                        onResourceCollected(item.name, item.count);
                    }

                    // 添加日志到Logcat
                    Log.d("EventHandler", "寻宝探险成就次数已更新: " + finalExplorationCount);

                    // 记录背包内物资增长情况，使用采集前获取的背包数据
                    logBackpackGrowthComparisonWithBefore(areaType, allItems, backpackBefore);

                    if (uiActivity.tvScrollTip != null) {
                        uiActivity.tvScrollTip.setText(tip.toString().trim());
                    }
                }
            });
        });
    }

    /**
     * 记录详细的采集信息日志
     */
    private void logDetailedCollectionInfo(String areaType,
                                           List<AreaResourceManager.CollectedItem> noBonusItems,
                                           List<AreaResourceManager.CollectedItem> toolLevelBonusItems,
                                           List<AreaResourceManager.CollectedItem> techBonusItems,
                                           List<AreaResourceManager.CollectedItem> allItems,
                                           int collectionMultiplier) {

        StringBuilder log = new StringBuilder("\n=== 采集详情日志 ===\n");
        log.append("区域: ").append(areaType).append("\n");
        log.append("连锁采集倍数: ").append(collectionMultiplier).append("\n\n");

        // 基础采集物品
        if (!noBonusItems.isEmpty()) {
            log.append("基础采集 (无加成):\n");
            for (AreaResourceManager.CollectedItem item : noBonusItems) {
                log.append("  - ").append(item.name).append(" ×").append(item.count).append("\n");
            }
            log.append("\n");
        }

        // 工具和等级加成物品
        if (!toolLevelBonusItems.isEmpty()) {
            log.append("工具&等级加成:\n");
            for (AreaResourceManager.CollectedItem item : toolLevelBonusItems) {
                log.append("  - ").append(item.name).append(" ×").append(item.count).append("\n");
            }
            log.append("\n");
        }

        // 科技加成物品
        if (!techBonusItems.isEmpty()) {
            log.append("科技加成:\n");
            for (AreaResourceManager.CollectedItem item : techBonusItems) {
                log.append("  - ").append(item.name).append(" ×").append(item.count).append("\n");
            }
            log.append("\n");
        }

        // 汇总物品
        log.append("最终获得物品:\n");
        Map<String, Integer> totalItems = new HashMap<>();
        for (AreaResourceManager.CollectedItem item : allItems) {
            totalItems.put(item.name, totalItems.getOrDefault(item.name, 0) + item.count);
        }
        for (Map.Entry<String, Integer> entry : totalItems.entrySet()) {
            log.append("  - ").append(entry.getKey()).append(" ×").append(entry.getValue()).append("\n");
        }

        Log.i("CollectionLog", log.toString());
    }

    /**
     * 记录背包内物资增长比较日志
     */
    private void logBackpackGrowthComparison(String areaType, List<AreaResourceManager.CollectedItem> collectedItems) {
        MainActivity uiActivity = getActivity();
        if (uiActivity == null || collectedItems == null) {
            Log.w("EventHandler", "logBackpackGrowthComparison: activity或collectedItems为null");
            return;
        }

        executorService.execute(() -> {
            try {
                // 获取采集前的背包数据
                Map<String, Integer> backpackBefore = null;
                if (uiActivity.dataManager != null && uiActivity.dataManager.getDbHelper() != null) {
                    backpackBefore = uiActivity.dataManager.getDbHelper().getBackpack(MyApplication.currentUserId);
                }

                if (backpackBefore == null) {
                    Log.w("EventHandler", "logBackpackGrowthComparison: 无法获取背包数据");
                    return;
                }

                // 添加短暂延迟，确保数据库写入完成
                try {
                    Thread.sleep(100); // 等待100毫秒
                } catch (InterruptedException e) {
                    Log.e("EventHandler", "延迟等待被中断", e);
                    Thread.currentThread().interrupt();
                }

                // 重新获取采集后的背包数据
                Map<String, Integer> backpackAfter = null;
                MainActivity updatedActivity = getActivity();
                if (updatedActivity != null && updatedActivity.dataManager != null && updatedActivity.dataManager.getDbHelper() != null) {
                    backpackAfter = updatedActivity.dataManager.getDbHelper().getBackpack(MyApplication.currentUserId);
                }

                if (backpackAfter == null) {
                    Log.w("EventHandler", "logBackpackGrowthComparison: 无法获取更新后的背包数据");
                    return;
                }

                StringBuilder log = new StringBuilder("\n=== 背包物资增长比对 ===\n");
                log.append("区域: ").append(areaType).append("\n\n");

                // 比较每个物品的增长
                Map<String, Integer> expectedGrowth = new HashMap<>();
                for (AreaResourceManager.CollectedItem item : collectedItems) {
                    if (item != null && item.name != null) {
                        expectedGrowth.put(item.name, expectedGrowth.getOrDefault(item.name, 0) + item.count);
                    }
                }

                boolean allMatch = true;
                log.append("采集物资与背包增长比对:\n");
                for (Map.Entry<String, Integer> expected : expectedGrowth.entrySet()) {
                    String itemName = expected.getKey();
                    int expectedCount = expected.getValue();
                    int beforeCount = backpackBefore.getOrDefault(itemName, 0);
                    int afterCount = backpackAfter.getOrDefault(itemName, 0);
                    int actualGrowth = afterCount - beforeCount;

                    String status = (expectedCount == actualGrowth) ? "✓" : "✗";
                    if (expectedCount != actualGrowth) allMatch = false;

                    log.append(String.format("  %s %s: 预期+%d, 实际+%d (采集前:%d, 采集后:%d)\n",
                            status, itemName, expectedCount, actualGrowth, beforeCount, afterCount));
                }

                // 检查是否有意外增长的物品
                log.append("\n其他物品变化:\n");
                for (Map.Entry<String, Integer> after : backpackAfter.entrySet()) {
                    String itemName = after.getKey();
                    int beforeCount = backpackBefore.getOrDefault(itemName, 0);
                    int afterCount = after.getValue();
                    int growth = afterCount - beforeCount;

                    if (growth > 0 && !expectedGrowth.containsKey(itemName)) {
                        log.append(String.format("  ⚠ %s: 意外增长+%d (采集前:%d, 采集后:%d)\n",
                                itemName, growth, beforeCount, afterCount));
                    }
                }

                log.append("\n比对结果: ").append(allMatch ? "✓ 所有物品增长正确" : "✗ 存在物品增长异常");

                Log.i("BackpackGrowthLog", log.toString());
            } catch (Exception e) {
                Log.e("EventHandler", "记录背包增长日志时出错", e);
            }
        });
    }

    /**
     * 记录背包内物资增长比较日志（使用采集前获取的背包数据）
     */
    private void logBackpackGrowthComparisonWithBefore(String areaType, List<AreaResourceManager.CollectedItem> collectedItems, Map<String, Integer> backpackBefore) {
        // 输入参数检查
        if (collectedItems == null || backpackBefore == null) {
            Log.w("EventHandler", "logBackpackGrowthComparisonWithBefore: 参数为null");
            return;
        }

        executorService.execute(() -> {
            try {
                // 获取采集后的背包数据
                Map<String, Integer> backpackAfter = null;
                MainActivity uiActivity = getActivity();
                if (uiActivity != null && uiActivity.dataManager != null && uiActivity.dataManager.getDbHelper() != null) {
                    backpackAfter = uiActivity.dataManager.getDbHelper().getBackpack(MyApplication.currentUserId);
                }

                if (backpackAfter == null) {
                    Log.w("EventHandler", "logBackpackGrowthComparisonWithBefore: 无法获取更新后的背包数据");
                    return;
                }

                StringBuilder log = new StringBuilder("\n=== 背包物资增长比对（正确时序）===\n");
                log.append("区域: ").append(areaType).append("\n\n");

                // 比较每个物品的增长
                Map<String, Integer> expectedGrowth = new HashMap<>();
                for (AreaResourceManager.CollectedItem item : collectedItems) {
                    if (item != null && item.name != null) {
                        expectedGrowth.put(item.name, expectedGrowth.getOrDefault(item.name, 0) + item.count);
                    }
                }

                boolean allMatch = true;
                log.append("采集物资与背包增长比对:\n");
                for (Map.Entry<String, Integer> expected : expectedGrowth.entrySet()) {
                    String itemName = expected.getKey();
                    if (itemName != null) {
                        int expectedCount = expected.getValue();
                        int beforeCount = backpackBefore.getOrDefault(itemName, 0);
                        int afterCount = backpackAfter.getOrDefault(itemName, 0);
                        int actualGrowth = afterCount - beforeCount;

                        String status = (expectedCount == actualGrowth) ? "✓" : "✗";
                        if (expectedCount != actualGrowth) allMatch = false;

                        log.append(String.format("  %s %s: 预期+%d, 实际+%d (采集前:%d, 采集后:%d)\n",
                                status, itemName, expectedCount, actualGrowth, beforeCount, afterCount));
                    }
                }

                // 检查是否有意外增长的物品
                log.append("\n其他物品变化:\n");
                for (Map.Entry<String, Integer> after : backpackAfter.entrySet()) {
                    String itemName = after.getKey();
                    if (itemName != null) {
                        int beforeCount = backpackBefore.getOrDefault(itemName, 0);
                        int afterCount = after.getValue();
                        int growth = afterCount - beforeCount;

                        if (growth > 0 && !expectedGrowth.containsKey(itemName)) {
                            log.append(String.format("  ⚠ %s: 意外增长+%d (采集前:%d, 采集后:%d)\n",
                                    itemName, growth, beforeCount, afterCount));
                        }
                    }
                }

                log.append("\n比对结果: ").append(allMatch ? "✓ 所有物品增长正确" : "✗ 存在物品增长异常");
                log.append("\n说明：采集前数据在实际采集操作开始前获取，确保时序正确");

                Log.i("BackpackGrowthLog", log.toString());
            } catch (Exception e) {
                Log.e("EventHandler", "记录背包增长日志（正确时序）时出错", e);
            }
        });
    }

    // 计算工具加成
    private int getToolResourceBonus(String toolType) {
        if (toolType == null) return 0;
        if (toolType.contains("石质")) return 1;
        if (toolType.contains("铁质")) return 2;
        if (toolType.contains("钻石")) return 3;
        return 0;
    }

    // 计算体力消耗
    private int calculateStaminaCostWithBonuses(int baseCost, List<String> bonuses) {
        int cost = baseCost;
        if (bonuses == null) return cost;

        for (String bonus : bonuses) {
            if (bonus.contains("体力消耗减少20%")) cost = (int) (cost * 0.8);
            else if (bonus.contains("体力消耗减少50%")) cost = (int) (cost * 0.5);
        }
        return Math.max(1, cost);
    }

    // 处理科技加成物品
    private List<AreaResourceManager.CollectedItem> handleTechBonuses(String areaType, List<String> bonuses) {
        List<AreaResourceManager.CollectedItem> bonusItems = new ArrayList<>();
        if (bonuses == null || bonuses.isEmpty()) return bonusItems;

        AreaResourceManager.AreaResource areaResource = AreaResourceManager.getInstance().getAreaResource(areaType);
        if (areaResource == null) return bonusItems;

        StringBuilder techLog = new StringBuilder("科技加成分析:\n");

        for (String bonus : bonuses) {
            if (bonus.contains("额外获得1个")) {
                String resStr = bonus.split("额外获得1个")[1];
                String[] resArray = resStr.split("、");
                if (resArray.length > 0) {
                    String randomRes = resArray[random.nextInt(resArray.length)];
                    bonusItems.add(new AreaResourceManager.CollectedItem(randomRes, 1));
                    techLog.append("  - ").append(bonus).append(" -> 实际获得: ").append(randomRes).append(" ×1\n");
                }
            } else if (bonus.contains("额外获得") && bonus.contains("个资源")) {
                try {
                    int count = Integer.parseInt(bonus.replaceAll("[^0-9]", ""));
                    if (!areaResource.resources.isEmpty()) {
                        AreaResourceManager.ResourceItem randomItem = areaResource.resources.get(
                                random.nextInt(areaResource.resources.size())
                        );
                        bonusItems.add(new AreaResourceManager.CollectedItem(randomItem.name, count));
                        techLog.append("  - ").append(bonus).append(" -> 实际获得: ").append(randomItem.name).append(" ×").append(count).append("\n");
                    }
                } catch (NumberFormatException e) {
                    Log.e("EventHandler", "解析科技加成数量失败", e);
                }
            }
        }

        if (!bonusItems.isEmpty()) {
            techLog.append("科技加成总计: ").append(bonusItems.size()).append("种物品，")
                    .append(bonusItems.stream().mapToInt(item -> item.count).sum()).append("个资源");
            Log.i("TechBonusLog", techLog.toString());
        }

        return bonusItems;
    }

    // 返回标题页
    public void backToTitlePage() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        activity.startActivity(new Intent(activity, TitleActivity.class));
        activity.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        MainActivity activity = getActivity();
        if (activity == null) return;

        if (id == R.id.btn_collect) {
            // 检查当前位置的地形类型
            String currentTerrain = activity.gameMap.getTerrainType(activity.currentX, activity.currentY);

            if (activity.gameMap.hasPortalAtCurrentPosition(MyApplication.currentUserId)) {
                handleTeleport();
            } else if ("神殿".equals(currentTerrain)) {
                handleTempleChallenge();
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
        } else if (id == R.id.btn_quest_panel) {
            handleQuestButtonClick();
        }
    }

    /**
     * 处理任务按钮点击
     */
    private void handleQuestButtonClick() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        // 获取任务管理器
        QuestManager questManager = QuestManager.getInstance(activity);

        // 获取当前任务状态
        int questStatus = questManager.getCurrentQuestStatus(MyApplication.currentUserId);
        Quest currentQuest = questManager.getCurrentActiveQuest(MyApplication.currentUserId);

        if (currentQuest == null) {
            // 没有任务时显示提示
            Toast.makeText(activity, "当前没有任务", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questStatus == QuestManager.QUEST_STATUS_CLAIMABLE) {
            // 可领取状态：点击领取奖励
            boolean success = questManager.claimCurrentQuest(MyApplication.currentUserId);
            if (success) {
                // 领取成功，显示奖励信息
                String rewardDescription = questManager.getCurrentQuestRewardDescription(MyApplication.currentUserId);
                showRewardDialog(rewardDescription);

                // 更新任务按钮显示
                updateQuestButtonDisplay();

                Log.d("QuestHandler", "任务领取成功: " + currentQuest.getTitle());
            } else {
                Toast.makeText(activity, "领取失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } else if (questStatus == QuestManager.QUEST_STATUS_ACTIVE) {
            // 活动状态：显示任务详情
            showQuestDetailsDialog();
        } else {
            // 已完成或无任务状态
            Toast.makeText(activity, "任务已完成", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示奖励对话框
     */
    private void showRewardDialog(String rewardDescription) {
        MainActivity activity = getActivity();
        if (activity == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("任务奖励")
                .setMessage("恭喜您完成任务！获得奖励：\n\n" + rewardDescription)
                .setPositiveButton("确定", null)
                .show();
    }

    /**
     * 显示任务详情对话框
     */
    private void showQuestDetailsDialog() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        QuestManager questManager = QuestManager.getInstance(activity);
        Quest currentQuest = questManager.getCurrentActiveQuest(MyApplication.currentUserId);

        if (currentQuest == null) {
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append(currentQuest.getTitle()).append("\n\n");
        message.append("任务描述：").append(currentQuest.getDescription()).append("\n\n");
        message.append("任务进度：\n").append(currentQuest.getProgressDescription()).append("\n\n");
        message.append("奖励：\n").append(questManager.getCurrentQuestRewardDescription(MyApplication.currentUserId));

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("任务详情")
                .setMessage(message.toString())
                .setPositiveButton("确定", null)
                .show();
    }

    /**
     * 更新任务按钮显示
     */
    public void updateQuestButtonDisplay() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        // 获取任务管理器
        QuestManager questManager = QuestManager.getInstance(activity);

        // 获取任务按钮
        Button btnQuest = activity.findViewById(R.id.btn_quest_panel);
        if (btnQuest == null) {
            return;
        }

        // 检查新手任务
        boolean hasNewbieQuest = questManager.hasUnfinishedNewbieQuest(MyApplication.currentUserId);
        int currentQuestStatus = questManager.getCurrentQuestStatus(MyApplication.currentUserId);

        if (hasNewbieQuest) {
            btnQuest.setText("新手任务");
            // 新手任务未完成时，显示提示色
            btnQuest.setTextColor(activity.getResources().getColor(R.color.quest_newbie_color));
        } else {
            if (currentQuestStatus == QuestManager.QUEST_STATUS_CLAIMABLE) {
                // 可领取状态：显示领取提示
                btnQuest.setText("任务可领取");
                btnQuest.setTextColor(activity.getResources().getColor(R.color.quest_claimable_color));
                // 显示红点提示
                showQuestRedDot(true);
            } else if (currentQuestStatus == QuestManager.QUEST_STATUS_ACTIVE) {
                // 活动状态：显示任务名称
                Quest currentQuest = questManager.getCurrentActiveQuest(MyApplication.currentUserId);
                if (currentQuest != null) {
                    btnQuest.setText(currentQuest.getTitle().length() > 6
                            ? currentQuest.getTitle().substring(0, 6) + "..."
                            : currentQuest.getTitle());
                    btnQuest.setTextColor(activity.getResources().getColor(R.color.quest_active_color));
                }
                showQuestRedDot(false);
            } else {
                // 无活跃任务
                btnQuest.setText("任务面板");
                btnQuest.setTextColor(activity.getResources().getColor(R.color.default_text_color));
                showQuestRedDot(false);
            }
        }
    }

    /**
     * 显示/隐藏任务按钮红点提示
     */
    private void showQuestRedDot(boolean show) {
        MainActivity activity = getActivity();
        if (activity == null) return;

        View redDot = activity.findViewById(R.id.quest_red_dot);
        if (redDot != null) {
            redDot.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 检查工具等级是否足够采集当前区域
     * @param toolType 当前装备的工具类型
     * @param areaType 当前区域类型
     * @return 是否满足等级要求
     */
    private boolean isToolLevelSufficient(String toolType, String areaType) {
        if (toolType == null || toolType.equals("无") || areaType == null) {
            return false;
        }

        // 获取区域等级（假设Constant中定义了区域等级映射）
        int areaLevel = Constant.getAreaLevel(areaType);
        // 获取工具等级（通过工具名称解析，如"石质斧头"为1级，"铁质斧头"为2级等）
        int toolLevel = getToolLevel(toolType);

        Log.d("ToolLevelCheck", "区域[" + areaType + "]等级: " + areaLevel +
                ", 工具[" + toolType + "]等级: " + toolLevel);

        // 工具等级 >= 区域等级 则满足要求
        return toolLevel >= areaLevel;
    }

    /**
     * 解析工具等级（从工具名称中提取）
     * @param toolType 工具类型名称
     * @return 工具等级（默认1级，无工具返回0）
     */
    private int getToolLevel(String toolType) {
        if (toolType == null || toolType.equals("无")) {
            return 0;
        }
        if (toolType.contains("石质")) {
            return 1;
        } else if (toolType.contains("铁质")) {
            return 2;
        } else if (toolType.contains("钻石")) {
            return 3;
        } else if (toolType.contains("传说")) {
            return 4;
        }
        // 默认1级（基础工具）
        return 1;
    }

    /**
     * 计算工具等级与区域等级匹配的资源加成
     * @param toolType 工具类型
     * @param areaType 区域类型
     * @return 等级匹配加成数量
     */
    private int calculateLevelBonus(String toolType, String areaType) {
        if (toolType == null || toolType.equals("无") || areaType == null) {
            return 0;
        }

        int toolLevel = getToolLevel(toolType);
        int areaLevel = Constant.getAreaLevel(areaType);
        int bonus = 0;

        // 工具等级高于区域等级时，每高1级增加1个加成
        if (toolLevel > areaLevel) {
            bonus = toolLevel - areaLevel;
            Log.d("LevelBonus", "工具等级高于区域等级，获得加成: " + bonus +
                    "(工具等级:" + toolLevel + ", 区域等级:" + areaLevel + ")");
        }

        return bonus;
    }

    /**
     * 资源采集完成后更新任务进度
     * @param resourceName 采集的资源名称
     * @param count 采集数量
     */
    private void onResourceCollected(String resourceName, int count) {
        MainActivity activity = getActivity();
        if (activity == null) return;

        // 调用任务管理器更新进度
        QuestManager questManager = QuestManager.getInstance(activity);
        boolean progressUpdated = questManager.updateResourceCollectionProgress(
                MyApplication.currentUserId, resourceName, count);

        if (progressUpdated) {
            Log.d("QuestProgress", "任务进度更新：资源[" + resourceName + "] +" + count);
            // 刷新任务按钮显示
            updateQuestButtonDisplay();
        }
    }

    /**
     * 获取玩家拥有的工具总数（背包中所有工具类物品数量之和）
     * @return 工具总数
     */
    public int getToolCount() {
        MainActivity activity = getActivity();
        if (activity == null || activity.dataManager == null || activity.dataManager.getDbHelper() == null) {
            Log.e("EventHandler", "获取工具数量失败：依赖组件为空");
            return 0;
        }

        try {
            // 查询背包中所有工具类物品（通过ItemConstants定义的工具列表筛选）
            Map<String, Integer> backpack = activity.dataManager.getDbHelper().getBackpack(MyApplication.currentUserId);
            int toolCount = 0;

            for (String itemName : ItemConstants.TOOL_ITEMS) {
                if (backpack.containsKey(itemName)) {
                    toolCount += backpack.get(itemName);
                }
            }

            Log.d("ToolCount", "玩家当前拥有工具总数: " + toolCount);
            return toolCount;
        } catch (Exception e) {
            Log.e("EventHandler", "计算工具数量异常", e);
            return 0;
        }
    }

    /**
     * 获取玩家已建造的建筑总数
     * @return 建筑总数
     */
    public int getBuildingCount() {
        MainActivity activity = getActivity();
        if (activity == null || activity.dataManager == null || activity.dataManager.getDbHelper() == null) {
            Log.e("EventHandler", "获取建筑数量失败：依赖组件为空");
            return 0;
        }

        try {
            // 从数据库查询当前用户的已建造建筑数量
            int buildingCount = activity.dataManager.getDbHelper().getBuiltBuildingCount(MyApplication.currentUserId);
            Log.d("BuildingCount", "玩家当前已建造建筑总数: " + buildingCount);
            return buildingCount;
        } catch (Exception e) {
            Log.e("EventHandler", "计算建筑数量异常", e);
            return 0;
        }
    }

    /**
     * 获取玩家已探索的地形类型数量（去重）
     * @return 已探索地形类型数
     */
    public int getExploredTerrainsCount() {
        MainActivity activity = getActivity();
        if (activity == null || activity.dataManager == null || activity.dataManager.getDbHelper() == null) {
            Log.e("EventHandler", "获取已探索地形数量失败：依赖组件为空");
            return 0;
        }

        try {
            // 从数据库查询玩家探索过的所有地形类型（去重）
            List<String> exploredTerrains = activity.dataManager.getDbHelper().getExploredTerrainTypes(MyApplication.currentUserId);
            int count = exploredTerrains != null ? exploredTerrains.size() : 0;
            Log.d("ExploredTerrains", "玩家已探索地形类型数: " + count + ", 类型: " + exploredTerrains);
            return count;
        } catch (Exception e) {
            Log.e("EventHandler", "计算已探索地形数量异常", e);
            return 0;
        }
    }

    /**
     * 处理传送门传送逻辑
     */
    private void handleTeleport() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        // 获取当前位置的传送门配置
        String portalTarget = activity.gameMap.getPortalTarget(MyApplication.currentUserId, activity.currentX, activity.currentY);
        if (portalTarget == null || portalTarget.isEmpty()) {
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText("传送门异常，无法传送");
            }
            Log.w("Teleport", "传送门目标为空，当前坐标: (" + activity.currentX + "," + activity.currentY + ")");
            return;
        }

        // 传送门目标格式："map:主世界,x:10,y:15" 或 "coord:5,8"（同地图传送）
        try {
            String[] targetParts = portalTarget.split(",");
            String targetMap = activity.gameMap.getCurrentMapType(); // 默认当前地图
            int targetX = activity.currentX;
            int targetY = activity.currentY;

            for (String part : targetParts) {
                String[] keyValue = part.split(":");
                if (keyValue.length == 2) {
                    switch (keyValue[0].trim()) {
                        case "map":
                            targetMap = keyValue[1].trim();
                            break;
                        case "x":
                            targetX = Integer.parseInt(keyValue[1].trim());
                            break;
                        case "y":
                            targetY = Integer.parseInt(keyValue[1].trim());
                            break;
                    }
                }
            }

            // 检查目标坐标是否在地图边界内
            if (targetX < Constant.MAP_MIN || targetX > Constant.MAP_MAX ||
                    targetY < Constant.MAP_MIN || targetY > Constant.MAP_MAX) {
                if (activity.tvScrollTip != null) {
                    activity.tvScrollTip.setText("目标位置超出地图边界，无法传送");
                }
                Log.w("Teleport", "目标坐标超出边界: (" + targetX + "," + targetY + ")");
                return;
            }

            // 消耗传送所需体力（固定10点）
            if (activity.stamina < 10) {
                if (activity.tvScrollTip != null) {
                    activity.tvScrollTip.setText("体力不足10点，无法激活传送门");
                }
                return;
            }
            activity.stamina -= 10;

            // 更新地图和位置
            activity.gameMap.switchMapType(targetMap);
            activity.currentX = targetX;
            activity.currentY = targetY;

            // 保存数据和更新UI
            if (activity.backgroundView != null) {
                activity.backgroundView.setCurrentCoord(targetX, targetY);
            }
            if (activity.dataManager != null) {
                activity.dataManager.saveAllCriticalData();
            }
            if (activity.uiUpdater != null) {
                activity.uiUpdater.updateAreaInfo();
                activity.uiUpdater.updateStatusDisplays();
            }

            // 记录传送日志
            String mapName = "main_world".equals(targetMap) ? "主世界" : "奇幻大陆";
            Log.i("Teleport", "传送成功：从" + activity.gameMap.getCurrentMapType() +
                    "(" + (activity.currentX - targetX) + "," + (activity.currentY - targetY) + ")" +
                    "传送到" + mapName + "(" + targetX + "," + targetY + ")");

            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText("传送门激活！已传送到" + mapName + "(" + targetX + "," + targetY + ")");
            }

            // 传送后增加游戏时间2小时
            activity.gameHour += 2;
            if (activity.gameHour >= 24) {
                activity.gameHour -= 24;
                activity.gameDay++;
            }
            if (activity.uiUpdater != null) {
                activity.uiUpdater.updateTimeDisplay();
            }

        } catch (Exception e) {
            Log.e("Teleport", "传送门解析或执行失败: " + portalTarget, e);
            if (activity.tvScrollTip != null) {
                activity.tvScrollTip.setText("传送失败，请稍后重试");
            }
        }
    }

    /**
     * 处理神殿挑战逻辑
     */
    private void handleTempleChallenge() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        // 检查是否已完成今日挑战
        boolean hasCompletedToday = activity.dataManager.getDbHelper().hasCompletedTempleChallengeToday(MyApplication.currentUserId);
        if (hasCompletedToday) {
            new AlertDialog.Builder(activity)
                    .setTitle("神殿挑战")
                    .setMessage("今日神殿挑战已完成，明日可再次挑战！")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        // 检查挑战条件（等级>=5）
        int playerLevel = activity.dataManager.getDbHelper().getPlayerLevel(MyApplication.currentUserId);
        if (playerLevel < 5) {
            new AlertDialog.Builder(activity)
                    .setTitle("挑战条件不足")
                    .setMessage("需要玩家等级达到5级才能挑战神殿！当前等级：" + playerLevel)
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        // 显示挑战确认对话框
        new AlertDialog.Builder(activity)
                .setTitle("神殿挑战")
                .setMessage("是否进入神殿挑战？\n挑战成功可获得稀有资源和装备！\n挑战失败将扣除10点生命。")
                .setPositiveButton("确认挑战", (dialog, which) -> startTempleChallenge())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 开始神殿挑战
     */
    private void startTempleChallenge() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        // 随机生成挑战结果（70%成功率）
        boolean challengeSuccess = random.nextDouble() < 0.7;

        executorService.execute(() -> {
            try {
                // 模拟挑战耗时（1秒）
                Thread.sleep(1000);

                // 标记今日已挑战
                activity.dataManager.getDbHelper().markTempleChallengeCompleted(MyApplication.currentUserId);

                mainHandler.post(() -> {
                    MainActivity uiActivity = getActivity();
                    if (uiActivity == null) return;

                    if (challengeSuccess) {
                        // 挑战成功：发放奖励
                        rewardTempleChallengeSuccess();
                    } else {
                        // 挑战失败：扣除生命
                        uiActivity.life = Math.max(0, uiActivity.life - 10);
                        // 更新数据库生命
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("life", uiActivity.life);
                        uiActivity.dataManager.getDbHelper().updateUserStatus(MyApplication.currentUserId, updates);

                        new AlertDialog.Builder(uiActivity)
                                .setTitle("挑战失败")
                                .setMessage("神殿挑战失败！扣除10点生命，当前生命：" + uiActivity.life)
                                .setPositiveButton("确定", null)
                                .show();

                        if (uiActivity.uiUpdater != null) {
                            uiActivity.uiUpdater.updateStatusDisplays();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("TempleChallenge", "挑战执行异常", e);
                mainHandler.post(() -> {
                    MainActivity uiActivity = getActivity();
                    if (uiActivity != null) {
                        Toast.makeText(uiActivity, "挑战异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 发放神殿挑战成功奖励
     */
    private void rewardTempleChallengeSuccess() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        // 生成稀有奖励（随机1种稀有资源+1件高级工具）
        String[] rareResources = {ItemConstants.ITEM_MYTHRIL_ORE, ItemConstants.ITEM_DRAGON_SCALE, ItemConstants.ITEM_ELF_CRYSTAL};
        String[] advancedTools = {ItemConstants.ITEM_DIAMOND_AXE, ItemConstants.ITEM_DIAMOND_PICKAXE, ItemConstants.ITEM_LEGENDARY_SHOVEL};

        String rewardResource = rareResources[random.nextInt(rareResources.length)];
        int resourceCount = 1 + random.nextInt(3); // 1-3个
        String rewardTool = advancedTools[random.nextInt(advancedTools.length)];

        // 发放奖励到背包
        activity.dataManager.getDbHelper().updateBackpackItem(MyApplication.currentUserId, rewardResource, resourceCount);
        activity.dataManager.getDbHelper().updateBackpackItem(MyApplication.currentUserId, rewardTool, 1);

        // 更新内存背包数据
        if (activity.backpack.containsKey(rewardResource)) {
            activity.backpack.put(rewardResource, activity.backpack.get(rewardResource) + resourceCount);
        } else {
            activity.backpack.put(rewardResource, resourceCount);
        }
        if (activity.backpack.containsKey(rewardTool)) {
            activity.backpack.put(rewardTool, activity.backpack.get(rewardTool) + 1);
        } else {
            activity.backpack.put(rewardTool, 1);
        }

        // 显示奖励对话框
        StringBuilder rewardMsg = new StringBuilder("挑战成功！获得以下奖励：\n");
        rewardMsg.append("- ").append(rewardResource).append(" ×").append(resourceCount).append("\n");
        rewardMsg.append("- ").append(rewardTool).append(" ×1");

        new AlertDialog.Builder(activity)
                .setTitle("神殿嘉奖")
                .setMessage(rewardMsg.toString())
                .setPositiveButton("确定", null)
                .show();

        Log.i("TempleChallenge", "挑战成功，发放奖励: " + rewardResource + "×" + resourceCount + ", " + rewardTool + "×1");

        // 刷新UI
        if (activity.uiUpdater != null) {
            activity.uiUpdater.updateBackpackDisplay();
        }
    }

    /**
     * 执行动作后增加游戏时间（默认1小时）
     */
    private void increaseGameTimeAfterAction() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        activity.gameHour += 1;
        // 处理跨天逻辑
        if (activity.gameHour >= 24) {
            activity.gameHour -= 24;
            activity.gameDay++;
            // 跨天触发状态刷新（如体力、饥饿度重置等）
            refreshStatusOnNewDay();
        }

        if (activity.uiUpdater != null) {
            activity.uiUpdater.updateTimeDisplay();
        }
    }

    /**
     * 跨天刷新玩家状态
     */
    private void refreshStatusOnNewDay() {
        MainActivity activity = getActivity();
        if (activity == null) return;

        // 跨天恢复50%体力和生命
        activity.stamina = Math.min(100, (int) (activity.stamina * 1.5));
        activity.life = Math.min(100, (int) (activity.life * 1.5));
        // 重置饥饿度和口渴度
        activity.hunger = 80;
        activity.thirst = 80;

        // 更新数据库状态
        Map<String, Object> updates = new HashMap<>();
        updates.put("stamina", activity.stamina);
        updates.put("life", activity.life);
        updates.put("hunger", activity.hunger);
        updates.put("thirst", activity.thirst);
        updates.put("game_day", activity.gameDay);
        updates.put("game_hour", activity.gameHour);

        executorService.execute(() -> {
            try {
                MainActivity currentActivity = getActivity();
                if (currentActivity != null && currentActivity.dataManager != null && currentActivity.dataManager.getDbHelper() != null) {
                    currentActivity.dataManager.getDbHelper().updateUserStatus(MyApplication.currentUserId, updates);
                }
            } catch (Exception e) {
                Log.e("EventHandler", "跨天状态刷新失败", e);
            }
        });

        // 显示跨天提示
        if (activity.tvScrollTip != null) {
            activity.tvScrollTip.setText("新的一天到来！体力、生命恢复50%，饥饿口渴已重置");
        }

        Log.i("GameTime", "跨天成功：当前天数" + activity.gameDay + "，状态已刷新");
    }

    /**
     * 更新采集按钮文本和状态
     */
    public void updateCollectButtonText() {
        MainActivity activity = getActivity();
        if (activity == null || activity.findViewById(R.id.btn_collect) == null) {
            return;
        }

        activity.runOnUiThread(() -> {
            Button btnCollect = activity.findViewById(R.id.btn_collect);
            if (btnCollect == null) return;
            
            try {

                // 检查当前位置是否可以采集
                String areaType = Constant.getAreaChineseTypeByCoord(activity.currentX, activity.currentY);
                if (areaType == null || areaType.isEmpty()) {
                    btnCollect.setText("无法采集");
                    btnCollect.setEnabled(false);
                    return;
                }

                // 检查采集次数和恢复时间
                DBHelper dbHelper = activity.dataManager.getDbHelper();
                Map<String, Object> areaData = dbHelper.getAreaCollectData(MyApplication.currentUserId, activity.currentX, activity.currentY);
                
                if (areaData != null) {
                    int collectedTimes = (int) areaData.getOrDefault("collect_count", 0);
                    long lastCollectTime = (long) areaData.getOrDefault("last_collect_time", 0L);
                    
                    if (!Constant.canCollect(areaType, collectedTimes, lastCollectTime)) {
                        // 计算剩余恢复时间
                        long now = SystemClock.elapsedRealtime();
                        Constant.AreaConfig config = Constant.AREA_CONFIG.get(areaType);
                        if (config != null) {
                            long cd = config.recoveryMinutes * 60 * 1000;
                            long remaining = cd - (now - lastCollectTime);
                            
                            if (remaining > 0) {
                                int minutes = (int) (remaining / (60 * 1000));
                                int seconds = (int) ((remaining % (60 * 1000)) / 1000);
                                btnCollect.setText("恢复中 " + minutes + ":" + String.format("%02d", seconds));
                                btnCollect.setEnabled(false);
                                return;
                            }
                        }
                    }
                }

                // 检查体力是否足够
                int staminaCost = Constant.getStaminaCost(areaType);
                if (activity.stamina < staminaCost) {
                    btnCollect.setText("体力不足");
                    btnCollect.setEnabled(false);
                    return;
                }

                // 检查工具是否匹配
                String toolType = activity.currentEquip;
                if (!Constant.isToolSuitableForArea(toolType, areaType)) {
                    btnCollect.setText("工具不足");
                    btnCollect.setEnabled(false);
                    return;
                }

                // 检查背包空间
                Map<String, Integer> backpack = dbHelper.getBackpack(MyApplication.currentUserId);
                int backpackCap = dbHelper.getBackpackCapacity(MyApplication.currentUserId);
                int currentSize = backpack.values().stream().mapToInt(Integer::intValue).sum();
                
                if (currentSize >= backpackCap) {
                    btnCollect.setText("背包已满");
                    btnCollect.setEnabled(false);
                    return;
                }

                // 可以采集
                btnCollect.setText("采集");
                btnCollect.setEnabled(true);

            } catch (Exception e) {
                Log.e("EventHandler", "更新采集按钮状态失败", e);
                // 出错时显示默认状态
                btnCollect.setText("采集");
                btnCollect.setEnabled(false);
            }
        });
    }

    /**
     * 设置冷却刷新Runnable
     */
    public void setCdRefreshRunnable(Runnable runnable) {
        this.cdRefreshRunnable = runnable;
    }

    /**
     * 获取冷却刷新Runnable
     */
    public Runnable getCdRefreshRunnable() {
        return cdRefreshRunnable;
    }

    /**
     * 资源释放：关闭线程池、移除Runnable，防止内存泄漏
     */
    public void cleanup() {
        Log.d("EventHandler", "执行cleanup，释放资源");

        // 移除循环Runnable
        if (cdRefreshRunnable != null && mainHandler != null) {
            mainHandler.removeCallbacks(cdRefreshRunnable);
            cdRefreshRunnable = null;
        }

        // 关闭线程池（等待已提交任务完成，不接受新任务）
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                // 等待60秒，若仍未关闭则强制终止
                if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 清空WeakReference引用（辅助GC）
        activityRef.clear();
    }
}