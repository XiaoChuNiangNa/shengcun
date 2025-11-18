package com.example.myapplication3;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务管理器（重构版本）
 * 修复问题：
 * 1. 任务进度与用户ID绑定，避免共享
 * 2. 任务独立计算进度，前一个任务完成后才开始计算下一个任务
 */
public class QuestManager {
    private static QuestManager instance;
    
    // 任务类型常量
    public static final String QUEST_TYPE_SURVIVAL_TIME = "survival_time";
    public static final String QUEST_TYPE_COLLECT_GRASS = "collect_grass";
    public static final String QUEST_TYPE_COLLECT_WOOD = "collect_wood";
    public static final String QUEST_TYPE_COLLECT_STONE = "collect_stone";
    public static final String QUEST_TYPE_CRAFT_TOOLS = "craft_tools";
    public static final String QUEST_TYPE_BUILD_STRUCTURE = "build_structure";
    public static final String QUEST_TYPE_BUILD_THATCHED_HUT = "build_thatched_hut";
    public static final String QUEST_TYPE_BUILD_CAMPFIRE = "build_campfire";
    public static final String QUEST_TYPE_EXPLORE_TERRAIN = "explore_terrain";
    
    // 任务状态常量
    public static final int QUEST_STATUS_HIDDEN = 0;       // 隐藏状态（未解锁）
    public static final int QUEST_STATUS_ACTIVE = 1;       // 活动状态（可完成）
    public static final int QUEST_STATUS_COMPLETED = 2;   // 已完成状态
    public static final int QUEST_STATUS_CLAIMABLE = 3;   // 可领取状态
    
    private Context context;
    private DBHelper dbHelper;
    private List<Quest> allQuests;
    
    // 用户数据映射：用户ID -> 用户任务数据
    private Map<Integer, UserQuestData> userQuestDataMap;
    
    // 用户任务数据结构
    private static class UserQuestData {
        List<Quest> activeQuests = new ArrayList<>();
        List<Quest> completedQuests = new ArrayList<>();
        Quest currentActiveQuest = null;
        int currentQuestIndex = 0;
        boolean hasCompletedNewbieCycle = false;
    }

    private QuestManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = DBHelper.getInstance(context);
        this.allQuests = new ArrayList<>();
        this.userQuestDataMap = new HashMap<>();
        
        initializeQuests();
    }

    public static QuestManager getInstance(Context context) {
        if (instance == null) {
            instance = new QuestManager(context);
        }
        return instance;
    }

    /**
     * 获取指定用户的任务数据，如果不存在则创建
     */
    private UserQuestData getUserQuestData(int userId) {
        if (!userQuestDataMap.containsKey(userId)) {
            UserQuestData data = new UserQuestData();
            userQuestDataMap.put(userId, data);
            loadQuestProgress(userId, data);
        }
        return userQuestDataMap.get(userId);
    }

    /**
     * 初始化所有任务
     */
    private void initializeQuests() {
        allQuests.clear();
        
        // 新手任务列表
        Quest quest1 = new Quest(1, "首次进入游戏", "欢迎来到生存世界！", Quest.QuestType.STORY,
                new HashMap<>(), // 无需求
                createRewardMap(ItemConstants.EQUIP_STONE_SICKLE, 1), // 石镰刀
                createRewardMap(ItemConstants.ITEM_DRIED_BREAD, 3), // 干面包
                50, 50, true);

        Quest quest2 = new Quest(2, "收集杂草", "收集10株杂草来熟悉环境", Quest.QuestType.COLLECTION,
                createRewardMap(ItemConstants.ITEM_WEED, 10),
                createRewardMap(ItemConstants.EQUIP_STONE_AXE, 1), // 石斧
                createRewardMap(ItemConstants.ITEM_DRIED_BREAD, 3),
                50, 50, true);

        Quest quest3 = new Quest(3, "收集木头", "收集10个木头用于建造", Quest.QuestType.COLLECTION,
                createRewardMap(ItemConstants.ITEM_WOOD, 10),
                createRewardMap(ItemConstants.EQUIP_STONE_PICKAXE, 1), // 石镐
                createRewardMap(ItemConstants.ITEM_DRIED_BREAD, 3),
                50, 50, true);

        Quest quest4 = new Quest(4, "建造茅草屋", "建造你的第一个庇护所", Quest.QuestType.BUILDING,
                new HashMap<>(), // 建造需求在游戏逻辑中处理
                createRewardMap(ItemConstants.ITEM_WOOD, 5),
                createRewardMap(ItemConstants.ITEM_STONE, 5),
                50, 50, true);

        Quest quest5 = new Quest(5, "建造篝火", "建造篝火用于烹饪和取暖", Quest.QuestType.BUILDING,
                new HashMap<>(),
                createRewardMap(ItemConstants.EQUIP_STONE_FISHING_ROD, 1), // 石质鱼竿
                createRewardMap(ItemConstants.ITEM_CHARCOAL, 3),
                50, 50, true);

        Quest quest6 = new Quest(6, "捕获鱼", "捕获5条鱼作为食物来源", Quest.QuestType.COLLECTION,
                createRewardMap(ItemConstants.ITEM_FISH, 5),
                createRewardMap(ItemConstants.ITEM_SMALL_STONE, 10),
                new HashMap<>(),
                50, 50, true);

        Quest quest7 = new Quest(7, "收集椰子", "收集5个椰子获取食物和水分", Quest.QuestType.COLLECTION,
                createRewardMap(ItemConstants.ITEM_COCONUT, 5),
                createRewardMap(ItemConstants.ITEM_SMALL_STONE, 10),
                new HashMap<>(),
                50, 50, true);

        Quest quest8 = new Quest(8, "收集浆果", "收集10个浆果补充营养", Quest.QuestType.COLLECTION,
                createRewardMap(ItemConstants.ITEM_BERRY, 10),
                createRewardMap(ItemConstants.ITEM_DRIED_BREAD, 3),
                new HashMap<>(),
                50, 50, true);

        Quest quest9 = new Quest(9, "收集水", "收集10个单位的水确保生存", Quest.QuestType.COLLECTION,
                createRewardMap(ItemConstants.ITEM_WATER, 10),
                createRewardMap(ItemConstants.ITEM_BOILED_WATER, 5),
                new HashMap<>(),
                50, 50, true);

        Quest quest10 = new Quest(10, "收集药草", "收集10个药草用于制作药品", Quest.QuestType.COLLECTION,
                createRewardMap(ItemConstants.ITEM_HERB, 10),
                createRewardMap(ItemConstants.ITEM_ADVANCED_HERB, 1),
                createRewardMap(ItemConstants.ITEM_BERRY_HONEY_BREAD, 1),
                50, 50, true);

        allQuests.add(quest1);
        allQuests.add(quest2);
        allQuests.add(quest3);
        allQuests.add(quest4);
        allQuests.add(quest5);
        allQuests.add(quest6);
        allQuests.add(quest7);
        allQuests.add(quest8);
        allQuests.add(quest9);
        allQuests.add(quest10);
    }

    /**
     * 创建奖励Map的辅助方法
     */
    private Map<String, Integer> createRewardMap(String itemName, int amount) {
        Map<String, Integer> map = new HashMap<>();
        map.put(itemName, amount);
        return map;
    }

    /**
     * 加载指定用户的任务进度
     */
    private void loadQuestProgress(int userId, UserQuestData userData) {
        // 从数据库加载已完成的任务
        List<Integer> completedQuestIds = dbHelper.getCompletedQuests(userId);
        userData.hasCompletedNewbieCycle = dbHelper.hasCompletedNewbieCycle(userId);
        
        // 根据用户状态激活任务
        updateActiveQuestsForUser(userId, userData);
        
        // 加载各个任务的进度
        for (Quest quest : allQuests) {
            // 深拷贝Quest对象，避免不同用户之间的数据共享
            Quest userQuest = createQuestCopy(quest);
            
            if (completedQuestIds.contains(quest.getId())) {
                userQuest.setCompleted(true);
                userData.completedQuests.add(userQuest);
            } else if (userData.activeQuests.contains(userQuest)) {
                // 加载进度数据
                Map<String, Integer> progress = dbHelper.getQuestProgress(userId, quest.getId());
                if (progress != null) {
                    for (Map.Entry<String, Integer> entry : progress.entrySet()) {
                        userQuest.updateProgress(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        
        Log.d("QuestManager", "用户" + userId + "任务进度加载完成，活跃任务数: " + userData.activeQuests.size() + 
                ", 已完成任务数: " + userData.completedQuests.size());
    }

    /**
     * 创建Quest对象的深拷贝
     */
    private Quest createQuestCopy(Quest original) {
        Quest copy = new Quest(
                original.getId(),
                original.getTitle(),
                original.getDescription(),
                original.getType(),
                new HashMap<>(original.getRequirements()),
                new HashMap<>(original.getReward1()),
                new HashMap<>(original.getReward2()),
                original.getExperienceReward(),
                original.getGoldReward(),
                original.isNewbieQuest()
        );
        
        // 复制进度数据
        copy.setCompleted(original.isCompleted());
        
        return copy;
    }

    /**
     * 更新指定用户的活跃任务列表
     */
    private void updateActiveQuestsForUser(int userId, UserQuestData userData) {
        userData.activeQuests.clear();
        
        // 只有在前一个任务完成后，下一个任务才能激活
        for (Quest quest : allQuests) {
            // 如果已完成新手轮回，不再激活新手任务
            if (userData.hasCompletedNewbieCycle && quest.isNewbieQuest()) {
                continue;
            }
            
            // 检查是否应该激活这个任务
            if (shouldActivateQuest(userData, quest)) {
                userData.activeQuests.add(createQuestCopy(quest));
            }
        }
        
        // 设置当前活跃任务
        updateCurrentActiveQuestForUser(userData);
    }

    /**
     * 检查是否应该激活任务（只有前一个任务完成后才能激活）
     */
    private boolean shouldActivateQuest(UserQuestData userData, Quest quest) {
        int questIndex = allQuests.indexOf(quest);
        
        // 第一个任务总是激活
        if (questIndex == 0) {
            return true;
        }
        
        // 检查前一个任务是否已完成
        Quest previousQuest = null;
        if (questIndex > 0) {
            previousQuest = getQuestByIdForUser(userData, allQuests.get(questIndex - 1).getId());
        }
        
        // 前一个任务已完成，则激活当前任务
        return previousQuest != null && previousQuest.isCompleted();
    }

    /**
     * 更新指定用户的当前活跃任务
     */
    private void updateCurrentActiveQuestForUser(UserQuestData userData) {
        // 找到第一个未完成的任务作为当前活跃任务
        for (Quest quest : userData.activeQuests) {
            if (!quest.isCompleted()) {
                userData.currentActiveQuest = quest;
                userData.currentQuestIndex = getQuestIndex(quest.getId());
                break;
            }
        }
        
        // 如果所有任务都完成了，设置currentActiveQuest为null
        if (userData.currentQuestIndex >= allQuests.size() || 
            getQuestByIdForUser(userData, allQuests.get(userData.currentQuestIndex).getId()).isCompleted()) {
            userData.currentActiveQuest = null;
        }
    }

    /**
     * 获取任务在列表中的索引
     */
    private int getQuestIndex(int questId) {
        for (int i = 0; i < allQuests.size(); i++) {
            if (allQuests.get(i).getId() == questId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ID获取用户的任务
     */
    private Quest getQuestByIdForUser(UserQuestData userData, int questId) {
        for (Quest quest : userData.activeQuests) {
            if (quest.getId() == questId) {
                return quest;
            }
        }
        for (Quest quest : userData.completedQuests) {
            if (quest.getId() == questId) {
                return quest;
            }
        }
        return null;
    }

    /**
     * 玩家完成新手轮回
     */
    public void completeNewbieCycle(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        userData.hasCompletedNewbieCycle = true;
        dbHelper.setNewbieCycleCompleted(userId, true);
        
        // 重新加载活跃任务列表
        updateActiveQuestsForUser(userId, userData);
        
        // 保存进度到数据库
        saveQuestProgress(userId, userData);
        
        Log.d("QuestManager", "用户" + userId + "新手轮回已完成，新手任务将不再出现");
    }

    /**
     * 更新任务进度（关键修复：只更新当前活跃任务）
     */
    public void updateQuestProgress(int userId, String itemName, int amount) {
        UserQuestData userData = getUserQuestData(userId);
        
        // 只更新当前活跃任务的进度
        if (userData.currentActiveQuest != null && !userData.currentActiveQuest.isCompleted()) {
            boolean completed = userData.currentActiveQuest.updateProgress(itemName, amount);
            
            if (completed) {
                // 完成任务
                completeQuestForUser(userId, userData, userData.currentActiveQuest);
                
                Log.d("QuestProgress", "用户" + userId + "任务完成: " + userData.currentActiveQuest.getTitle() + 
                        ", 物品: " + itemName + " ×" + amount);
            } else {
                // 保存进度
                saveQuestProgress(userId, userData);
                
                Log.d("QuestProgress", "用户" + userId + "任务进度更新: " + userData.currentActiveQuest.getTitle() + 
                        ", 物品: " + itemName + " ×" + amount);
            }
        }
    }

    /**
     * 完成任务并发放奖励
     */
    private void completeQuestForUser(int userId, UserQuestData userData, Quest quest) {
        quest.setCompleted(true);
        userData.activeQuests.remove(quest);
        userData.completedQuests.add(quest);
        
        // 发放奖励
        giveQuestRewards(userId, quest);
        
        // 通知数据库
        dbHelper.markQuestAsCompleted(userId, quest.getId());
        
        // 显示完成提示
        showQuestCompletionToast(quest);
        
        // 激活下一个任务
        activateNextQuest(userId, userData);
        
        // 保存进度
        saveQuestProgress(userId, userData);
        
        Log.d("QuestManager", "用户" + userId + "任务完成: " + quest.getTitle());
    }

    /**
     * 激活下一个任务
     */
    private void activateNextQuest(int userId, UserQuestData userData) {
        // 更新活跃任务列表，激活下一个任务
        updateActiveQuestsForUser(userId, userData);
        
        if (userData.currentActiveQuest != null) {
            Log.d("QuestManager", "用户" + userId + "已激活下一个任务: " + userData.currentActiveQuest.getTitle());
        }
    }

    /**
     * 发放任务奖励
     */
    private void giveQuestRewards(int userId, Quest quest) {
        // 发放物品奖励
        giveItemRewards(userId, quest.getReward1());
        giveItemRewards(userId, quest.getReward2());
        
        // 发放经验值和金币
        if (quest.getExperienceReward() > 0) {
            LevelExperienceManager.getInstance(context).addExperience(quest.getExperienceReward());
        }
        
        if (quest.getGoldReward() > 0) {
            dbHelper.addGold(userId, quest.getGoldReward());
        }
    }

    /**
     * 发放物品奖励
     */
    private void giveItemRewards(int userId, Map<String, Integer> rewards) {
        for (Map.Entry<String, Integer> reward : rewards.entrySet()) {
            String itemName = reward.getKey();
            int amount = reward.getValue();
            
            if (amount > 0) {
                dbHelper.addItemToBackpack(userId, itemName, amount);
                Log.d("QuestReward", "用户" + userId + "发放奖励: " + itemName + " ×" + amount);
            }
        }
    }

    /**
     * 显示任务完成提示
     */
    private void showQuestCompletionToast(Quest quest) {
        String message = "任务完成: " + quest.getTitle() + "\n获得奖励: " + quest.getRewardDescription();
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 保存任务进度
     */
    private void saveQuestProgress(int userId, UserQuestData userData) {
        Map<String, Object> progressData = new HashMap<>();
        
        // 存储所有活跃任务的进度
        for (Quest quest : userData.activeQuests) {
            progressData.put("quest_" + quest.getId() + "_progress", quest.getCurrentProgress());
        }
        
        // 存储已完成任务
        List<Integer> completedIds = new ArrayList<>();
        for (Quest quest : userData.completedQuests) {
            completedIds.add(quest.getId());
        }
        progressData.put("completed_quests", completedIds);
        
        progressData.put("has_completed_newbie_cycle", userData.hasCompletedNewbieCycle);
        
        dbHelper.saveQuestProgress(userId, progressData);
    }

    // ========== 公共方法（保持兼容性） ==========

    public List<Quest> getActiveQuests(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        return new ArrayList<>(userData.activeQuests);
    }

    public List<Quest> getCompletedQuests(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        return new ArrayList<>(userData.completedQuests);
    }

    public List<Quest> getAllQuests() {
        return new ArrayList<>(allQuests);
    }

    public boolean hasActiveQuests(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        return !userData.activeQuests.isEmpty();
    }

    public boolean hasCompletedNewbieCycle(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        return userData.hasCompletedNewbieCycle;
    }

    /**
     * 获取当前活跃任务
     */
    public Quest getCurrentActiveQuest(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        return userData.currentActiveQuest;
    }
    
    /**
     * 获取当前任务状态
     */
    public int getCurrentQuestStatus(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        Quest currentQuest = userData.currentActiveQuest;
        
        if (currentQuest == null) {
            return QUEST_STATUS_HIDDEN;
        }
        
        if (currentQuest.isCompleted()) {
            return QUEST_STATUS_COMPLETED;
        }
        
        // 检查任务是否完成
        if (currentQuest.checkCompletion()) {
            return QUEST_STATUS_CLAIMABLE;
        }
        
        return QUEST_STATUS_ACTIVE;
    }
    
    /**
     * 提交并领取当前任务奖励
     */
    public boolean claimCurrentQuest(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        Quest currentQuest = userData.currentActiveQuest;
        
        if (currentQuest == null || !currentQuest.checkCompletion()) {
            return false;
        }
        
        // 完成任务并发放奖励
        completeQuestForUser(userId, userData, currentQuest);
        return true;
    }
    
    /**
     * 更新任务进度（按任务类型）
     */
    public void updateProgress(int userId, String questType, int value) {
        // 根据任务类型更新对应的任务进度
        switch (questType) {
            case QUEST_TYPE_SURVIVAL_TIME:
                // 生存时间相关的任务，暂时不实现具体逻辑
                break;
            case QUEST_TYPE_COLLECT_GRASS:
                updateQuestProgress(userId, ItemConstants.ITEM_WEED, value);
                break;
            case QUEST_TYPE_COLLECT_WOOD:
                updateQuestProgress(userId, ItemConstants.ITEM_WOOD, value);
                break;
            case QUEST_TYPE_COLLECT_STONE:
                updateQuestProgress(userId, ItemConstants.ITEM_STONE, value);
                break;
        }
    }

    /**
     * 获取任务进度文本
     */
    public String getProgressText(int userId, int questId) {
        UserQuestData userData = getUserQuestData(userId);
        Quest quest = getQuestByIdForUser(userData, questId);
        
        if (quest == null) return "0/0";
        
        Map<String, Integer> progress = quest.getCurrentProgress();
        Map<String, Integer> requirements = quest.getRequirements();
        
        if (progress.isEmpty() || requirements.isEmpty()) {
            return quest.isCompleted() ? "已完成" : "未开始";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> req : requirements.entrySet()) {
            String itemName = req.getKey();
            int required = req.getValue();
            int current = progress.getOrDefault(itemName, 0);
            sb.append(current).append("/").append(required);
            break;
        }
        
        return sb.toString();
    }

    /**
     * 获取单例实例（无Context版本）
     */
    public static QuestManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("QuestManager未初始化，请先调用getInstance(Context)方法");
        }
        return instance;
    }
    
    /**
     * 重置任务进度（用于调试）
     */
    /**
     * 获取当前任务的奖励描述
     */
    public String getCurrentQuestRewardDescription(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        Quest currentQuest = userData.currentActiveQuest;
        
        if (currentQuest == null) {
            return "暂无奖励";
        }
        
        return currentQuest.getRewardDescription();
    }

    /**
     * 检查是否是第一次玩游戏
     */
    public boolean isFirstTimePlaying(int userId) {
        UserQuestData userData = getUserQuestData(userId);
        return userData.completedQuests.isEmpty();
    }

    /**
     * 完成任务
     */
    public void completeQuest(int userId, int questId) {
        UserQuestData userData = getUserQuestData(userId);
        
        // 查找指定任务
        Quest questToComplete = null;
        for (Quest quest : userData.activeQuests) {
            if (quest.getId() == questId) {
                questToComplete = quest;
                break;
            }
        }
        
        if (questToComplete != null) {
            completeQuestForUser(userId, userData, questToComplete);
        }
    }

    /**
     * 游戏失败时重置任务进度
     */
    public void resetQuestProgressOnGameFailure(int userId) {
        Log.d("QuestManager", "游戏失败，重置用户" + userId + "的任务进度");
        
        // 获取用户数据
        UserQuestData userData = getUserQuestData(userId);
        
        // 清空已完成任务列表
        userData.completedQuests.clear();
        
        // 重新加载活跃任务列表（从头开始）
        updateActiveQuestsForUser(userId, userData);
        
        // 清除数据库中的任务进度
        dbHelper.clearQuestProgress(userId);
        
        // 重置新手轮回状态
        userData.hasCompletedNewbieCycle = false;
        dbHelper.setNewbieCycleCompleted(userId, false);
        
        // 保存重置后的进度
        saveQuestProgress(userId, userData);
        
        Log.d("QuestManager", "用户" + userId + "任务进度已重置（游戏失败）");
    }

    public void resetAllQuests(int userId) {
        // 清空用户数据
        userQuestDataMap.remove(userId);
        
        // 清除数据库中的任务进度
        dbHelper.clearQuestProgress(userId);
        
        // 重新加载进度
        getUserQuestData(userId);
        
        Log.d("QuestManager", "用户" + userId + "所有任务进度已重置");
    }
}