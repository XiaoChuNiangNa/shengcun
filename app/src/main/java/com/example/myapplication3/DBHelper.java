package com.example.myapplication3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper instance;
    private SQLiteDatabase readableDb;
    private SQLiteDatabase writableDb;

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, Constant.DB_NAME, null, Constant.DB_VERSION);
        // 初始化数据库连接
        readableDb = getReadableDatabase();
        writableDb = getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (readableDb == null || !readableDb.isOpen()) {
            readableDb = super.getReadableDatabase();
        }
        return readableDb;
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (writableDb == null || !writableDb.isOpen()) {
            writableDb = super.getWritableDatabase();
        }
        return writableDb;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 用户表
        db.execSQL("CREATE TABLE IF NOT EXISTS user (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "is_test INTEGER NOT NULL DEFAULT 0," +  // 0:普通账号 1:测试账号
                "is_login INTEGER NOT NULL DEFAULT 0)"); // 新增：0=未登录，1=当前登录

        // 创建测试账号
        initAdminTestAccountOnCreate(db);


        // 用户状态表
        db.execSQL("CREATE TABLE IF NOT EXISTS user_status (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "life INTEGER NOT NULL," +
                "hunger INTEGER NOT NULL," +
                "thirst INTEGER NOT NULL," +
                "stamina INTEGER NOT NULL," +
                "current_x INTEGER NOT NULL," +
                "current_y INTEGER NOT NULL," +
                "current_map TEXT NOT NULL DEFAULT 'main_world'," +  // 新增：当前地图
                "backpack_cap INTEGER NOT NULL," +
                "gold INTEGER NOT NULL DEFAULT 0," +
                "sound_status INTEGER NOT NULL DEFAULT 1," +
                "difficulty TEXT NOT NULL DEFAULT 'normal'," +
                "first_collect_time LONG NOT NULL DEFAULT 0," +
                "is_synthesis_unlocked INTEGER NOT NULL DEFAULT 1," +
                "is_building_unlocked INTEGER NOT NULL DEFAULT 1," +
                "is_cooking_unlocked INTEGER NOT NULL DEFAULT 0," +
                "is_smelting_unlocked INTEGER NOT NULL DEFAULT 0," +
                "is_trading_unlocked INTEGER NOT NULL DEFAULT 0," +
                "is_sleep_unlocked INTEGER NOT NULL DEFAULT 0," +
                "last_rest_time INTEGER DEFAULT 0," +
                "is_easy_cleared INTEGER DEFAULT 0," +  // 新增：简单难度通关标记
                "is_normal_cleared INTEGER DEFAULT 0," +
                "is_hard_cleared INTEGER DEFAULT 0," +  // 新增：困难难度通关标记
                "hope_points INTEGER NOT NULL DEFAULT 0," +  // 希望点数
                "game_hour INTEGER NOT NULL DEFAULT " + Constant.GAME_HOUR_DEFAULT + "," +
                "game_day INTEGER NOT NULL DEFAULT " + Constant.GAME_DAY_DEFAULT + "," +
                "temperature INTEGER NOT NULL DEFAULT " + Constant.TEMPERATURE_DEFAULT + "," +  // 这里需要添加逗号
                "is_reincarnation_unlocked INTEGER NOT NULL DEFAULT 0," +
                "global_collect_times INTEGER DEFAULT 0," +
                "exploration_times INTEGER DEFAULT 0," +
                "synthesis_times INTEGER DEFAULT 0," +
                "smelting_times INTEGER DEFAULT 0," +
                "trading_times INTEGER DEFAULT 0," +
                "reincarnation_times INTEGER DEFAULT 0," +  // 添加轮回次数字段
                "last_refresh_day INTEGER DEFAULT 1," +
                "exp INTEGER NOT NULL DEFAULT 0," +  // 新增：经验值字段
                "level INTEGER NOT NULL DEFAULT 1," +  // 新增：用户等级字段，默认等级为1
                "is_test_initialized INTEGER NOT NULL DEFAULT 0)");

        // 背包表（含耐久字段）
        db.execSQL("CREATE TABLE IF NOT EXISTS backpack (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "item_type TEXT NOT NULL," +
                "item_count INTEGER NOT NULL DEFAULT 0," +
                "durability INTEGER NOT NULL DEFAULT 0," +
                "is_equipped INTEGER NOT NULL DEFAULT 0," + // 新增：0=未装备，1=已装备（仅工具有效）
                "UNIQUE(user_id, item_type))");

        // 装备表
        db.execSQL("CREATE TABLE IF NOT EXISTS equipment (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "equip_type TEXT NOT NULL DEFAULT '无'," +
                "durability INTEGER NOT NULL DEFAULT 0," + // 添加耐久度字段
                "is_equipped INTEGER NOT NULL DEFAULT 0" +  // 移除UNIQUE约束
                ")");

        // 资源冷却表
        db.execSQL("CREATE TABLE IF NOT EXISTS resource_cd (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "area_x INTEGER NOT NULL," +
                "area_y INTEGER NOT NULL," +
                "collect_count INTEGER NOT NULL DEFAULT 0," +
                "last_collect_time LONG NOT NULL DEFAULT 0," +
                "UNIQUE(user_id, area_x, area_y))");

        // 建筑表（使用新的约束：用户+坐标+建筑类型唯一）
        db.execSQL("CREATE TABLE IF NOT EXISTS building (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "building_type TEXT NOT NULL," +
                "area_x INTEGER NOT NULL," +  // 新增：x坐标
                "area_y INTEGER NOT NULL," +  // 新增：y坐标
                "count INTEGER NOT NULL DEFAULT 1," +
                "UNIQUE(user_id, area_x, area_y, building_type)" +  // 新的唯一约束：用户+坐标+建筑类型
                ")");

        //用户地形表
        db.execSQL("CREATE TABLE IF NOT EXISTS user_terrain (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "area_x INTEGER NOT NULL," + // 坐标x
                "area_y INTEGER NOT NULL," + // 坐标y
                "terrain_type TEXT NOT NULL," + // 地形类型（如"茅草屋"）
                "UNIQUE(user_id, area_x, area_y)" + // 同一位置地形唯一
                ")");

        //科技表
        db.execSQL("CREATE TABLE IF NOT EXISTS tech (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "tech_id TEXT NOT NULL," +  // 科技唯一标识（如base_collect）
                "level INTEGER NOT NULL DEFAULT 0," +  // 当前等级
                "UNIQUE(user_id, tech_id)," +  // 确保每个用户的科技唯一
                "FOREIGN KEY(user_id) REFERENCES user(id)" +
                ")");

        //存档表
        db.execSQL("CREATE TABLE IF NOT EXISTS save_slot (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "slot_id INTEGER NOT NULL," + // 存档位1-3
                "save_data TEXT NOT NULL," +  // 序列化的存档数据
                "save_time LONG NOT NULL," +  // 保存时间戳
                "UNIQUE(user_id, slot_id)" +  // 每个用户的存档位唯一
                ")");

        //随机事件表
        db.execSQL("CREATE TABLE IF NOT EXISTS event (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "event_type TEXT NOT NULL," +  // 事件类型
                "is_active INTEGER NOT NULL DEFAULT 1," +  // 是否活跃
                "create_time LONG NOT NULL," +  // 创建时间
                "end_time LONG NOT NULL," +  // 结束时间
                "FOREIGN KEY(user_id) REFERENCES user(id)" +
                ")");

        // 区域采集次数表（记录用户-区域坐标-次数）
        db.execSQL("CREATE TABLE IF NOT EXISTS area_collect (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +  // 必须有NOT NULL
                "area_x INTEGER NOT NULL," +   // 正确列名：area_x（不是x）
                "area_y INTEGER NOT NULL," +   // 正确列名：area_y（不是y）
                "collect_count INTEGER NOT NULL DEFAULT 0," +  // 正确列名：collect_count（不是collect_times）
                "last_collect_time LONG NOT NULL DEFAULT 0," +  // 补充时间列
                "UNIQUE(user_id, area_x, area_y)" +
                ")");
        //map表
        db.execSQL("CREATE TABLE IF NOT EXISTS map (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +  // 关联用户
                "area_x INTEGER NOT NULL," +        // 坐标X
                "area_y INTEGER NOT NULL," +        // 坐标Y
                "terrain_type TEXT NOT NULL," + // 地形类型
                "FOREIGN KEY(user_id) REFERENCES user(id)," +
                "UNIQUE(user_id, area_x, area_y)" +      // 确保用户的每个坐标仅存一条记录
                ")");

        // 仓库表
        db.execSQL("CREATE TABLE IF NOT EXISTS warehouse (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "item_type TEXT NOT NULL," +
                "item_count INTEGER NOT NULL DEFAULT 0," +
                "durability INTEGER NOT NULL DEFAULT 0," +
                "UNIQUE(user_id, item_type)" +
                ")");

        // 成就表
        db.execSQL("CREATE TABLE IF NOT EXISTS achievements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "achievement_type TEXT NOT NULL," +
                "level INTEGER NOT NULL DEFAULT 0," +
                "progress INTEGER NOT NULL DEFAULT 0," +
                "is_completed INTEGER NOT NULL DEFAULT 0," +
                "is_claimed INTEGER NOT NULL DEFAULT 0," +
                "UNIQUE(user_id, achievement_type, level)" +
                ")");
    }

    // achievements表在onCreate中已创建，此处移除重复方法

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE user_status ADD COLUMN user_id INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE backpack ADD COLUMN user_id INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE equipment ADD COLUMN user_id INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE resource_cd ADD COLUMN user_id INTEGER NOT NULL DEFAULT 1");
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE user_status ADD COLUMN thirst INTEGER NOT NULL DEFAULT 100");
            db.execSQL("ALTER TABLE user_status ADD COLUMN stamina INTEGER NOT NULL DEFAULT 100");
            db.execSQL("ALTER TABLE user_status ADD COLUMN gold INTEGER NOT NULL DEFAULT 0");

            db.execSQL("DROP TABLE IF EXISTS resource_cd");
            db.execSQL("CREATE TABLE IF NOT EXISTS resource_cd (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "area_x INTEGER NOT NULL," +
                    "area_y INTEGER NOT NULL," +
                    "collect_count INTEGER NOT NULL DEFAULT 0," +
                    "last_collect_time LONG NOT NULL DEFAULT 0," +
                    "UNIQUE(user_id, area_x, area_y))");

            db.execSQL("CREATE TABLE IF NOT EXISTS building (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "building_type TEXT NOT NULL," +
                    "area_x INTEGER NOT NULL," +  // 新增：x坐标
                    "area_y INTEGER NOT NULL," +  // 新增：y坐标
                    "count INTEGER NOT NULL DEFAULT 1," +
                    "UNIQUE(user_id, area_x, area_y, building_type)" +  // 新的唯一约束：用户+坐标+建筑类型
                    ")");
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_synthesis_unlocked INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_building_unlocked INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_cooking_unlocked INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_smelting_unlocked INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_trading_unlocked INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_sleep_unlocked INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_reincarnation_unlocked INTEGER NOT NULL DEFAULT 0");
        }

        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE backpack ADD COLUMN durability INTEGER NOT NULL DEFAULT 0");
        }

        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE user_status ADD COLUMN hope_points INTEGER NOT NULL DEFAULT 0");
        }

        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS tech (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "tech_id TEXT NOT NULL," +
                    "level INTEGER NOT NULL DEFAULT 0," +
                    "UNIQUE(user_id, tech_id)" +
                    ")");
        }

        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE user_status ADD COLUMN game_hour INTEGER NOT NULL DEFAULT " + Constant.GAME_HOUR_DEFAULT);
            db.execSQL("ALTER TABLE user_status ADD COLUMN game_day INTEGER NOT NULL DEFAULT " + Constant.GAME_DAY_DEFAULT);
            db.execSQL("ALTER TABLE user_status ADD COLUMN temperature INTEGER NOT NULL DEFAULT " + Constant.TEMPERATURE_DEFAULT);
        }

        if (oldVersion < 9) {
            db.execSQL("CREATE TABLE IF NOT EXISTS save_slot (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "slot_id INTEGER NOT NULL," +
                    "save_data TEXT NOT NULL," +
                    "save_time LONG NOT NULL," +
                    "UNIQUE(user_id, slot_id)" +
                    ")");
        }

        // 补全版本10的升级逻辑
        if (oldVersion < 10) {
            db.execSQL("ALTER TABLE user_status ADD COLUMN last_rest_time LONG NOT NULL DEFAULT 0");
        }

        if (oldVersion < 11) {
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_normal_cleared INTEGER NOT NULL DEFAULT 0");
        }

        // 版本12逻辑已合并到版本11中，移除重复代码

        if (oldVersion < 13) {
            // 先备份旧数据
            db.execSQL("ALTER TABLE equipment RENAME TO equipment_old");
            // 创建新表（无唯一约束）
            db.execSQL("CREATE TABLE IF NOT EXISTS equipment (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "equip_type TEXT NOT NULL DEFAULT '无'," +
                    "durability INTEGER NOT NULL DEFAULT 0," +
                    "is_equipped INTEGER NOT NULL DEFAULT 0" +
                    ")");
            // 迁移旧数据
            db.execSQL("INSERT INTO equipment (user_id, equip_type, is_equipped) " +
                    "SELECT user_id, equip_type, is_equipped FROM equipment_old");
            // 删除旧表
            db.execSQL("DROP TABLE equipment_old");
        }

        if (oldVersion < 14) {
            // 为旧表添加字段
            db.execSQL("ALTER TABLE user ADD COLUMN is_test INTEGER NOT NULL DEFAULT 0");
        }

        if (oldVersion < 15) {
            db.execSQL("ALTER TABLE user_status ADD COLUMN is_test_initialized INTEGER NOT NULL DEFAULT 0");
        }
        if (oldVersion < 16) {
            // 1. 先删除旧的area_collect表（如果存在，因为列名错误）
            db.execSQL("DROP TABLE IF EXISTS area_collect");

            // 2. 重新创建表，使用正确的列名和字段
            db.execSQL("CREATE TABLE IF NOT EXISTS area_collect (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +  // 补充NOT NULL约束，确保数据完整性
                    "area_x INTEGER NOT NULL," +   // 区域X坐标
                    "area_y INTEGER NOT NULL," +   // 区域Y坐标
                    "collect_count INTEGER NOT NULL DEFAULT 0," +  // 修复列名：collect_times → collect_count
                    "last_collect_time LONG NOT NULL DEFAULT 0," +  // 补充最后采集时间列
                    "UNIQUE(user_id, area_x, area_y)" +  // 唯一索引：用户+坐标
                    ")");

            // 3. 同步添加全局采集次数相关字段（如果之前逻辑需要）
            db.execSQL("ALTER TABLE user_status ADD COLUMN global_collect_times INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE user_status ADD COLUMN last_refresh_day INTEGER DEFAULT 1");
        }
        if (oldVersion < 17) { // 假设当前版本是16，新增版本17
            db.execSQL("ALTER TABLE backpack ADD COLUMN is_equipped INTEGER NOT NULL DEFAULT 0");
        }
        if (oldVersion < 18) {
            // 新增map表（避免重复创建）
            db.execSQL("CREATE TABLE IF NOT EXISTS map (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "area_x INTEGER NOT NULL," +
                    "area_y INTEGER NOT NULL," +
                    "terrain_type TEXT NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES user(id)," +
                    "UNIQUE(user_id, area_x, area_y)"+
                    ")");
        }

        if (oldVersion < 19) {
            // 1. 检查旧表是否存在（避免重复创建临时表）
            Cursor cursor = null;
            boolean hasOldTable = false;
            try {
                cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{"building"});
                hasOldTable = cursor.moveToFirst();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            if (hasOldTable) {
                // 2. 创建临时表备份数据
                db.execSQL("CREATE TABLE IF NOT EXISTS building_temp (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "building_type TEXT NOT NULL," +
                        "count INTEGER NOT NULL DEFAULT 1)");

                // 3. 复制旧表数据到临时表
                db.execSQL("INSERT INTO building_temp (user_id, building_type, count) " +
                        "SELECT user_id, building_type, count FROM building");

                // 4. 删除旧表
                db.execSQL("DROP TABLE IF EXISTS building");
            }

            // 5. 创建新表（无论是否有旧表，确保表结构正确）
            db.execSQL("CREATE TABLE IF NOT EXISTS building (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "building_type TEXT NOT NULL," +
                    "area_x INTEGER NOT NULL," +
                    "area_y INTEGER NOT NULL," +
                    "count INTEGER NOT NULL DEFAULT 1," +
                    "UNIQUE(user_id, area_x, area_y))");

            // 6. 恢复临时表数据（如果存在）
            if (hasOldTable) {
                db.execSQL("INSERT INTO building (user_id, building_type, count, area_x, area_y) " +
                        "SELECT user_id, building_type, count, 0, 0 FROM building_temp");
                db.execSQL("DROP TABLE IF EXISTS building_temp");
            }
        }
        if (oldVersion < 20) {
            // 创建 user_terrain 表（针对旧版本数据库）
            db.execSQL("CREATE TABLE IF NOT EXISTS user_terrain (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "area_x INTEGER NOT NULL," +
                    "area_y INTEGER NOT NULL," +
                    "terrain_type TEXT NOT NULL," +
                    "UNIQUE(user_id, area_x, area_y)" +
                    ")");
        }

        if (oldVersion < 21) {
            // 修改building表结构，移除坐标唯一约束，改为用户+坐标+建筑类型的唯一约束
            // 1. 创建临时表备份数据
            db.execSQL("CREATE TABLE IF NOT EXISTS building_temp (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "building_type TEXT NOT NULL," +
                    "area_x INTEGER NOT NULL," +
                    "area_y INTEGER NOT NULL," +
                    "count INTEGER NOT NULL DEFAULT 1)");

            // 2. 复制旧表数据到临时表
            db.execSQL("INSERT INTO building_temp (user_id, building_type, area_x, area_y, count) " +
                    "SELECT user_id, building_type, area_x, area_y, count FROM building");

            // 3. 删除旧表
            db.execSQL("DROP TABLE IF EXISTS building");

            // 4. 创建新表（新的约束：用户+坐标+建筑类型唯一）
            db.execSQL("CREATE TABLE IF NOT EXISTS building (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "building_type TEXT NOT NULL," +
                    "area_x INTEGER NOT NULL," +
                    "area_y INTEGER NOT NULL," +
                    "count INTEGER NOT NULL DEFAULT 1," +
                    "UNIQUE(user_id, area_x, area_y, building_type)" +  // 新的唯一约束：用户+坐标+建筑类型
                    ")");

            // 5. 恢复临时表数据
            db.execSQL("INSERT INTO building (user_id, building_type, area_x, area_y, count) " +
                    "SELECT user_id, building_type, area_x, area_y, count FROM building_temp");

            // 6. 删除临时表
            db.execSQL("DROP TABLE IF EXISTS building_temp");
        }

        if (oldVersion < 22) {
            // 添加current_map列到user_status表
            db.execSQL("ALTER TABLE user_status ADD COLUMN current_map TEXT NOT NULL DEFAULT 'main_world'");
        }

        if (oldVersion < 23) {
            // 版本23升级逻辑：确保current_map列存在
            // 如果之前版本22的升级没有执行，这里会再次添加
            try {
                db.execSQL("ALTER TABLE user_status ADD COLUMN current_map TEXT NOT NULL DEFAULT 'main_world'");
            } catch (Exception e) {
                // 如果列已经存在，忽略错误
                Log.d("DBHelper", "current_map列已存在，跳过添加");
            }
        }

        if (oldVersion < 24) {
            // 版本24升级逻辑：创建warehouse表和achievements表
            db.execSQL("CREATE TABLE IF NOT EXISTS warehouse (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "item_type TEXT NOT NULL," +
                    "item_count INTEGER NOT NULL DEFAULT 0," +
                    "durability INTEGER NOT NULL DEFAULT 0," +
                    "UNIQUE(user_id, item_type)" +
                    ")");

            // 创建成就表
            db.execSQL("CREATE TABLE IF NOT EXISTS achievements (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "achievement_type TEXT NOT NULL," +
                    "level INTEGER NOT NULL DEFAULT 0," +
                    "progress INTEGER NOT NULL DEFAULT 0," +
                    "is_completed INTEGER NOT NULL DEFAULT 0," +
                    "is_claimed INTEGER NOT NULL DEFAULT 0," +
                    "UNIQUE(user_id, achievement_type, level)" +
                    ")");
        }

        if (oldVersion < 25) {
            // 版本25升级逻辑：确保achievements表包含is_claimed列
            try {
                db.execSQL("ALTER TABLE achievements ADD COLUMN is_claimed INTEGER NOT NULL DEFAULT 0");
            } catch (Exception e) {
                // 如果列已经存在，忽略错误
                Log.d("DBHelper", "is_claimed列已存在，跳过添加");
            }
        }

        if (oldVersion < 26) {
            // 版本26升级逻辑：添加成就统计字段
            db.execSQL("ALTER TABLE user_status ADD COLUMN exploration_times INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE user_status ADD COLUMN synthesis_times INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE user_status ADD COLUMN smelting_times INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE user_status ADD COLUMN trading_times INTEGER DEFAULT 0");
        }

        if (oldVersion < 27) {
            // 版本27升级逻辑：添加轮回次数统计字段
            try {
                db.execSQL("ALTER TABLE user_status ADD COLUMN reincarnation_times INTEGER DEFAULT 0");
            } catch (Exception e) {
                // 如果列已经存在，忽略错误
                Log.d("DBHelper", "reincarnation_times列已存在，跳过添加");
            }
        }

        if (oldVersion < 28) {
            // 版本28升级逻辑：添加level列到user_status表
            try {
                db.execSQL("ALTER TABLE user_status ADD COLUMN level INTEGER NOT NULL DEFAULT 1");
            } catch (Exception e) {
                // 如果列已经存在，忽略错误
                Log.d("DBHelper", "level列已存在，跳过添加");
            }
        }

        if (oldVersion < 29) {
            // 版本29升级逻辑：添加难度解锁字段
            try {
                db.execSQL("ALTER TABLE user_status ADD COLUMN is_easy_cleared INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE user_status ADD COLUMN is_hard_cleared INTEGER DEFAULT 0");
            } catch (Exception e) {
                // 如果列已经存在，忽略错误
                Log.d("DBHelper", "难度解锁字段已存在，跳过添加");
            }
        }

    }

    /**
     * 检查表中是否存在指定列
     * @param db 数据库实例
     * @param tableName 表名
     * @param columnName 列名
     * @return 列是否存在
     */
    private boolean isColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    if (columnName.equals(name)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DBHelper", "检查列是否存在失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    // 获取物品耐久度
    public int getDurability(int userId, String itemType) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int durability = 0;
        try {
            cursor = db.query("backpack", new String[]{"durability"},
                    "user_id=? AND item_type=?",
                    new String[]{String.valueOf(userId), itemType},
                    null, null, null);
            if (cursor.moveToFirst()) {
                durability = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return durability;
    }

    /**
     * 更新工具的装备状态（仅对背包中的工具有效）
     */
    public void updateToolEquipStatus(int userId, String toolType, boolean isEquipped) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_equipped", isEquipped ? 1 : 0);
        db.update(
                "backpack",
                values,
                "user_id = ? AND item_type = ?",
                new String[]{String.valueOf(userId), toolType}
        );
    }

    public Map<String, Integer> getAllItemDurability(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        Map<String, Integer> durabilityMap = new HashMap<>();
        try {
            // 查询该用户背包中所有物品的类型和耐久度
            cursor = db.query(
                    "backpack",
                    new String[]{"item_type", "durability"}, // 需要查询的字段
                    "user_id=?", // 查询条件
                    new String[]{String.valueOf(userId)}, // 条件参数
                    null, null, null
            );
            // 遍历结果集，存入Map
            while (cursor.moveToNext()) {
                String itemType = cursor.getString(0); // 第0列是item_type
                int durability = cursor.getInt(1); // 第1列是durability
                durabilityMap.put(itemType, durability);
            }
        } finally {
            if (cursor != null) {
                cursor.close(); // 确保游标关闭
            }
        }
        return durabilityMap;
    }
    // 更新物品耐久度
    public Boolean updateDurability(int userId, String itemType, int delta) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // 1. 获取当前耐久
            int currentDurability = getDurability(userId, itemType);
            // 2. 计算新耐久（核心修复：传入userId获取科技加成后的初始耐久上限）
            int initialDurability = ToolUtils.getToolInitialDurability(itemType, userId);
            int newDurability = currentDurability + delta;

            // 3. 处理耐久为0的情况（移除物品）
            if (newDurability <= 0) {
                // 移除物品（返回是否成功）
                boolean isRemoved = updateBackpackItem(userId, itemType, -1);
                return isRemoved; // 直接返回移除结果，无需更新耐久
            }

            // 4. 确保耐久不超过上限（新增逻辑：防止耐久超过科技加成后的上限）
            if (newDurability > initialDurability) {
                newDurability = initialDurability;
            }

            // 5. 更新耐久到数据库
            ContentValues values = new ContentValues();
            values.put("durability", newDurability);
            int rowsAffected = db.update(
                    "backpack",
                    values,
                    "user_id=? AND item_type=?",
                    new String[]{String.valueOf(userId), itemType}
            );
            return rowsAffected > 0; // 返回是否更新成功

        } catch (Exception e) {
            e.printStackTrace();
            return false; // 异常时返回失败
        } finally {
            // 无需手动关闭db，系统会管理
        }
    }

    // 解锁功能
    public void unlockFunction(int userId, String functionName) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(functionName, 1);
            db.update("user_status", values, "user_id=?", new String[]{String.valueOf(userId)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 注册
    public boolean register(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password", password);
            long id = db.insert("user", null, values);
            return id != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int checkUserExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int userId = -1;
        try {
            cursor = db.query("user", new String[]{"id"},
                    "username=?",
                    new String[]{username},
                    null, null, null);
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return userId;
    }
    // 登录
    public int login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int userId = -1;
        try {
            cursor = db.query("user", new String[]{"id"},
                    "username=? AND password=?",
                    new String[]{username, password},
                    null, null, null);
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(0);

                // 如果是admin账号，返回特殊UID -100
                if ("admin".equals(username)) {
                    Log.d("DBHelper", "login: admin账号登录，返回特殊UID: " + MyApplication.TEST_ACCOUNT_UID);
                    return MyApplication.TEST_ACCOUNT_UID;
                }
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
        return userId;
    }

    // 获取工具初始耐久（修复：明确工具类型判断）
    // 获取工具初始耐久（修复：支持"石斧"、"石镐"等名称识别）
    public int getToolInitialDurability(String itemType) {
        // 获取当前用户难度
        String difficulty = (String) getUserStatus(MyApplication.currentUserId).get("difficulty");
        boolean isMedium = Constant.DIFFICULTY_NORMAL.equals(difficulty);

        // 与ToolUtils保持一致的精准匹配逻辑
        switch (itemType) {
            // 石质工具
            case ItemConstants.EQUIP_STONE_AXE:
            case ItemConstants.EQUIP_STONE_PICKAXE:
            case ItemConstants.EQUIP_STONE_SICKLE:
            case ItemConstants.EQUIP_STONE_FISHING_ROD:
                return isMedium ? Constant.MEDIUM_STONE_TOOL_DURABILITY : Constant.STONE_TOOL_DURABILITY;

            // 铁质工具
            case ItemConstants.EQUIP_IRON_AXE:
            case ItemConstants.EQUIP_IRON_PICKAXE:
            case ItemConstants.EQUIP_IRON_SICKLE:
            case ItemConstants.EQUIP_IRON_FISHING_ROD:
                return isMedium ? Constant.MEDIUM_IRON_TOOL_DURABILITY : Constant.IRON_TOOL_DURABILITY;

            // 钻石工具
            case ItemConstants.EQUIP_DIAMOND_AXE:
            case ItemConstants.EQUIP_DIAMOND_PICKAXE:
            case ItemConstants.EQUIP_DIAMOND_SICKLE:
            case ItemConstants.EQUIP_DIAMOND_FISHING_ROD:
                return isMedium ? Constant.MEDIUM_DIAMOND_TOOL_DURABILITY : Constant.DIAMOND_TOOL_DURABILITY;

            // 非工具类型或未匹配到的类型返回0
            default:
                return 0;
        }
    }

    // 初始化用户数据
    public void initUserData(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            Random random = new Random();
            int initX = random.nextInt(Constant.MAP_MAX) + 1;
            int initY = random.nextInt(Constant.MAP_MAX) + 1;

            ContentValues userStatus = new ContentValues();
            userStatus.put("user_id", userId);
            userStatus.put("life", Constant.INIT_LIFE);
            userStatus.put("hunger", Constant.INIT_HUNGER);
            userStatus.put("thirst", Constant.INIT_THIRST);
            userStatus.put("stamina", Constant.INIT_STAMINA);
            userStatus.put("current_x", initX);
            userStatus.put("current_y", initY);
            userStatus.put("backpack_cap", Constant.BACKPACK_INIT_CAP);
            userStatus.put("gold", 0);
            userStatus.put("sound_status", 1);
            userStatus.put("difficulty", Constant.DIFFICULTY_NORMAL);
            userStatus.put("first_collect_time", 0);
            db.insert("user_status", null, userStatus);

            // 初始装备：石斧（存入背包，工具类）
            ContentValues backpack = new ContentValues();
            backpack.put("user_id", userId);
            backpack.put("item_type", ItemConstants.EQUIP_STONE_AXE);
            backpack.put("item_count", 1); // 数量1
            backpack.put("durability", getToolInitialDurability(ItemConstants.EQUIP_STONE_AXE)); // 假设10
            db.insert("backpack", null, backpack);

            // 初始装备：石镐（存入背包，工具类）
            backpack.clear();
            backpack.put("user_id", userId);
            backpack.put("item_type", ItemConstants.EQUIP_STONE_PICKAXE);
            backpack.put("item_count", 1);
            backpack.put("durability", getToolInitialDurability(ItemConstants.EQUIP_STONE_PICKAXE)); // 假设10
            db.insert("backpack", null, backpack);

            // 干面包（存入背包，消耗品）
            backpack.clear();
            backpack.put("user_id", userId);
            backpack.put("item_type", ItemConstants.ITEM_DRIED_BREAD);
            backpack.put("item_count", 2);
            backpack.put("durability", 0);
            db.insert("backpack", null, backpack);
        } finally {

        }
    }

    /**
     * 从背包中获取所有工具类物品（用于装备页面显示）
     */
    public List<Equipment> getToolsFromBackpack(int userId) {
        List<Equipment> tools = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "backpack",
                new String[]{"id", "item_type", "durability", "is_equipped"},
                "user_id = ? AND item_type IN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", // 所有工具类型
                new String[]{String.valueOf(userId),
                        ItemConstants.EQUIP_STONE_AXE, ItemConstants.EQUIP_IRON_AXE, ItemConstants.EQUIP_DIAMOND_AXE,
                        ItemConstants.EQUIP_STONE_PICKAXE, ItemConstants.EQUIP_IRON_PICKAXE, ItemConstants.EQUIP_DIAMOND_PICKAXE,
                        ItemConstants.EQUIP_STONE_SICKLE, ItemConstants.EQUIP_IRON_SICKLE, ItemConstants.EQUIP_DIAMOND_SICKLE,
                        ItemConstants.EQUIP_STONE_FISHING_ROD, ItemConstants.EQUIP_IRON_FISHING_ROD, ItemConstants.EQUIP_DIAMOND_FISHING_ROD
                },
                null, null, null
        );
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("item_type"));
            int durability = cursor.getInt(cursor.getColumnIndexOrThrow("durability"));
            boolean isEquipped = cursor.getInt(cursor.getColumnIndexOrThrow("is_equipped")) == 1;
            tools.add(new Equipment(
                    id,
                    type,
                    durability,
                    ToolUtils.getToolInitialDurability(type, userId), // 传入userId获取动态耐久上限
                    isEquipped
            ));
        }
        cursor.close();
        return tools;
    }



    public int getCurrentX() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "user_status",
                new String[]{"current_x"},
                "user_id = ?",
                new String[]{String.valueOf(MyApplication.currentUserId)},
                null, null, null
        );
        int x = 0; // 默认值，实际会被数据库值覆盖
        if (cursor.moveToFirst()) {
            x = cursor.getInt(0);
        }
        cursor.close();
        return x;
    }

    /**
     * 获取当前用户的Y坐标
     */
    public int getCurrentY() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "user_status",
                new String[]{"current_y"},
                "user_id = ?",
                new String[]{String.valueOf(MyApplication.currentUserId)},
                null, null, null
        );
        int y = 0; // 默认值，实际会被数据库值覆盖
        if (cursor.moveToFirst()) {
            y = cursor.getInt(0);
        }
        cursor.close();
        return y;
    }


    public boolean isNewUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "user_status",
                new String[]{"current_x"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
        boolean isNew = true;
        if (cursor.moveToFirst()) {
            // 如果current_x存在且不为默认值（如0），则不是新用户
            int currentX = cursor.getInt(0);
            isNew = (currentX == 0); // 假设未初始化时为0
        }
        cursor.close();
        return isNew;
    }

    /**
     * 初始化用户坐标
     */
    public void initUserCoordinates(int userId, int x, int y) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_x", x);
        values.put("current_y", y);
        // 同时初始化时间和体温的默认值（与MainActivity保持一致）
        values.put("game_hour", Constant.GAME_HOUR_DEFAULT);
        values.put("game_day", Constant.GAME_DAY_DEFAULT);
        values.put("temperature", Constant.TEMPERATURE_DEFAULT);
        db.update(
                "user_status",
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    // 使用工具后减少耐久
    public void useTool(int userId, String toolType) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("backpack", new String[]{"durability"},
                    "user_id=? AND item_type=?",
                    new String[]{String.valueOf(userId), toolType}, null, null, null);

            if (cursor.moveToFirst()) {
                int durability = cursor.getInt(0);
                if (durability > 1) {
                    ContentValues values = new ContentValues();
                    values.put("durability", durability - 1);
                    db.update("backpack", values,
                            "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), toolType});
                } else {
                    // 耐久为0，删除物品

                    db.delete("backpack",
                            "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), toolType});
                    // 取消装备
                    ContentValues resetValues = new ContentValues();
                    resetValues.put("is_equipped", 0);
                    db.update("equipment", resetValues,
                            "user_id=? AND equip_type=?",
                            new String[]{String.valueOf(userId), toolType});
                }
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
    }

    // 获取用户状态
    public Map<String, Object> getUserStatus(int userId) {
        Map<String, Object> status = new HashMap<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query("user_status", null, "user_id=?",
                    new String[]{String.valueOf(userId)}, null, null, null);

            // 调试日志：检查查询结果
            Log.d("DBHelper", "getUserStatus: 查询用户 " + userId + " 的状态数据，cursor count=" + cursor.getCount());

            if (cursor.moveToFirst()) {
                status.put("life", cursor.getInt(cursor.getColumnIndexOrThrow("life")));
                status.put("hunger", cursor.getInt(cursor.getColumnIndexOrThrow("hunger")));
                status.put("thirst", cursor.getInt(cursor.getColumnIndexOrThrow("thirst")));
                status.put("stamina", cursor.getInt(cursor.getColumnIndexOrThrow("stamina")));
                status.put("current_x", cursor.getInt(cursor.getColumnIndexOrThrow("current_x")));
                status.put("current_y", cursor.getInt(cursor.getColumnIndexOrThrow("current_y")));
                int backpackCap = cursor.getInt(cursor.getColumnIndexOrThrow("backpack_cap"));
                int hopePoints = cursor.getInt(cursor.getColumnIndexOrThrow("hope_points"));
                int gold = cursor.getInt(cursor.getColumnIndexOrThrow("gold"));

                // 调试日志：显示关键特权数据
                Log.d("DBHelper", "getUserStatus: 用户 " + userId + " 特权数据 - 背包容量=" + backpackCap +
                        ", 希望点数=" + hopePoints + ", 金币=" + gold);

                status.put("backpack_cap", backpackCap);
                status.put("gold", gold);
                status.put("hope_points", hopePoints);
                status.put("sound_status", cursor.getInt(cursor.getColumnIndexOrThrow("sound_status")));
                status.put("difficulty", cursor.getString(cursor.getColumnIndexOrThrow("difficulty")));
                status.put("first_collect_time", cursor.getLong(cursor.getColumnIndexOrThrow("first_collect_time")));
                status.put("is_synthesis_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_synthesis_unlocked")));
                status.put("is_building_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_building_unlocked")));
                status.put("is_cooking_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_cooking_unlocked")));
                status.put("is_smelting_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_smelting_unlocked")));
                status.put("is_trading_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_trading_unlocked")));
                status.put("is_sleep_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_sleep_unlocked")));
                status.put("is_reincarnation_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_reincarnation_unlocked")));

                status.put("game_hour", cursor.getInt(cursor.getColumnIndexOrThrow("game_hour")));
                status.put("game_day", cursor.getInt(cursor.getColumnIndexOrThrow("game_day")));
                status.put("temperature", cursor.getInt(cursor.getColumnIndexOrThrow("temperature")));
            } else {
                Log.d("DBHelper", "getUserStatus: 用户 " + userId + " 数据不存在，执行初始化");
                initUserData(userId);
                // 重新查询初始化后的数据
                cursor = db.query("user_status", null, "user_id=?",
                        new String[]{String.valueOf(userId)}, null, null, null);

                Log.d("DBHelper", "getUserStatus: 重新查询用户 " + userId + " 的状态数据，cursor count=" + cursor.getCount());

                if (cursor.moveToFirst()) {
                    status.put("life", cursor.getInt(cursor.getColumnIndexOrThrow("life")));
                    status.put("hunger", cursor.getInt(cursor.getColumnIndexOrThrow("hunger")));
                    status.put("thirst", cursor.getInt(cursor.getColumnIndexOrThrow("thirst")));
                    status.put("stamina", cursor.getInt(cursor.getColumnIndexOrThrow("stamina")));
                    status.put("current_x", cursor.getInt(cursor.getColumnIndexOrThrow("current_x")));
                    status.put("current_y", cursor.getInt(cursor.getColumnIndexOrThrow("current_y")));
                    int backpackCap = cursor.getInt(cursor.getColumnIndexOrThrow("backpack_cap"));
                    int hopePoints = cursor.getInt(cursor.getColumnIndexOrThrow("hope_points"));
                    int gold = cursor.getInt(cursor.getColumnIndexOrThrow("gold"));

                    // 调试日志：显示初始化后的特权数据
                    Log.d("DBHelper", "getUserStatus: 初始化后用户 " + userId + " 特权数据 - 背包容量=" + backpackCap +
                            ", 希望点数=" + hopePoints + ", 金币=" + gold);

                    status.put("backpack_cap", backpackCap);
                    status.put("gold", gold);
                    status.put("hope_points", hopePoints);
                    status.put("sound_status", cursor.getInt(cursor.getColumnIndexOrThrow("sound_status")));
                    status.put("difficulty", cursor.getString(cursor.getColumnIndexOrThrow("difficulty")));
                    status.put("first_collect_time", cursor.getLong(cursor.getColumnIndexOrThrow("first_collect_time")));
                    status.put("is_synthesis_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_synthesis_unlocked")));
                    status.put("is_building_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_building_unlocked")));
                    status.put("is_cooking_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_cooking_unlocked")));
                    status.put("is_smelting_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_smelting_unlocked")));
                    status.put("is_trading_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_trading_unlocked")));
                    status.put("is_sleep_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_sleep_unlocked")));
                    status.put("is_reincarnation_unlocked", cursor.getInt(cursor.getColumnIndexOrThrow("is_reincarnation_unlocked")));
                    status.put("game_hour", cursor.getInt(cursor.getColumnIndexOrThrow("game_hour")));
                    status.put("game_day", cursor.getInt(cursor.getColumnIndexOrThrow("game_day")));
                    status.put("temperature", cursor.getInt(cursor.getColumnIndexOrThrow("temperature")));
                }
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
        return status;
    }

    // 更新用户状态
    public boolean updateUserStatus(int userId, Map<String, Object> updateData) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            for (String key : updateData.keySet()) {
                // 检查字段是否存在，如果不存在则跳过
                if (!isColumnExists(db, "user_status", key)) {
                    Log.w("DBHelper", "跳过不存在的字段: " + key);
                    continue;
                }

                Object value = updateData.get(key);
                if (value instanceof Integer) {
                    values.put(key, (Integer) value);
                } else if (value instanceof Long) {
                    values.put(key, (Long) value);
                } else if (value instanceof String) {
                    values.put(key, (String) value);
                }
            }
            // db.update()返回受影响的行数，大于0表示更新成功
            int rowsAffected = db.update("user_status", values, "user_id=?", new String[]{String.valueOf(userId)});
            return rowsAffected > 0; // 受影响行数>0则返回true（成功）
        } finally {

        }
    }

    // 获取背包
    public Map<String, Integer> getBackpack(int userId) {
        Map<String, Integer> backpack = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("backpack", null, "user_id=?",
                    new String[]{String.valueOf(userId)}, null, null, null);
            while (cursor.moveToNext()) {
                String itemType = cursor.getString(cursor.getColumnIndexOrThrow("item_type"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("item_count"));
                backpack.put(itemType, count);
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
        return backpack;
    }

    // 更新背包物品（含耐久初始化）
    public Boolean updateBackpackItem(int userId, String itemType, int count) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction(); // 开启事务
        try {
            // 检查背包容量（仅当增加物品时）
            if (count > 0) {
                int currentTotal = getBackpackCurrentCount(userId);
                int cap = getBackpackCapacity(userId);
                if (currentTotal + count > cap) {
                    return false; // 容量不足
                }
            }

            // 查询物品是否已存在
            Cursor cursor = db.query(
                    "backpack",
                    new String[]{"item_count", "durability"},
                    "user_id=? AND item_type=?",
                    new String[]{String.valueOf(userId), itemType},
                    null, null, null
            );

            ContentValues values = new ContentValues();
            if (cursor.moveToFirst()) {
                // 物品已存在：更新数量和耐久（工具类需处理耐久）
                int currentCount = cursor.getInt(0);
                int newCount = currentCount + count;
                if (newCount <= 0) {
                    // 数量为0：删除物品
                    db.delete("backpack", "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), itemType});
                } else {
                    values.put("item_count", newCount);
                    // 若为工具且是新物品，初始化耐久（修复：传入userId获取科技加成后的耐久）
                    if (count > 0 && isToolItem(itemType) && currentCount == 0) {
                        values.put("durability", ToolUtils.getToolInitialDurability(itemType, userId));
                    }
                    db.update("backpack", values, "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), itemType});
                }
            } else if (count > 0) {
                // 新物品：插入记录（工具类初始化耐久）
                values.put("user_id", userId);
                values.put("item_type", itemType);
                values.put("item_count", count);
                values.put("is_equipped", 0); // 默认未装备
                if (isToolItem(itemType)) {
                    values.put("durability", ToolUtils.getToolInitialDurability(itemType, userId));
                } else {
                    values.put("durability", 0); // 非工具无耐久
                }
                db.insert("backpack", null, values);
            }
            cursor.close();
            db.setTransactionSuccessful(); // 标记事务成功
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction(); // 结束事务（无论成功与否）
        }
    }

    // 获取背包当前物品总数
    public int getBackpackCurrentCount(int userId) {
        int total = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT SUM(item_count) FROM backpack WHERE user_id=?",
                    new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由调用方管理
            // if (db != null && db.isOpen()) db.close();
        }
        return total;
    }

    // 获取当前装备
    public String getCurrentEquip(int userId) {
        String equip = "无";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            // 修复：从backpack表查询装备状态，与updateToolEquipStatus保持一致
            cursor = db.query("backpack", new String[]{"item_type"},
                    "user_id=? AND is_equipped=?",
                    new String[]{String.valueOf(userId), "1"}, null, null, null);
            if (cursor.moveToFirst()) {
                equip = cursor.getString(cursor.getColumnIndexOrThrow("item_type"));
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
        return equip;
    }

    // 更新当前装备
    public void updateCurrentEquip(int userId, String equipType) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        try {
            // 先将所有装备设为未装备
            ContentValues values = new ContentValues();
            values.put("is_equipped", 0);
            db.update("equipment", values, "user_id=?", new String[]{String.valueOf(userId)});

            // 检查该装备是否已存在
            cursor = db.query("equipment", null,
                    "user_id=? AND equip_type=?",
                    new String[]{String.valueOf(userId), equipType}, null, null, null);

            if (cursor.moveToFirst()) {
                values.put("is_equipped", 1);
                db.update("equipment", values,
                        "user_id=? AND equip_type=?",
                        new String[]{String.valueOf(userId), equipType});
            } else {
                values.put("user_id", userId);
                values.put("equip_type", equipType);
                values.put("is_equipped", 1);
                db.insert("equipment", null, values);
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
    }

    public List<Equipment> getAllEquipments(int userId) {
        List<Equipment> equipments = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "equipment",
                new String[]{"id", "equip_type", "durability", "is_equipped"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String type = cursor.getString(1);
            int durability = cursor.getInt(2); // 读取耐久度
            boolean isEquipped = cursor.getInt(3) == 1;
            // 最大耐久度从ToolUtils获取
            int maxDurability = ToolUtils.getToolInitialDurability(type);
            equipments.add(new Equipment(id, type, durability, maxDurability, isEquipped));
        }
        cursor.close();
        return equipments;
    }

    public void syncBackpackToEquipment(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            // 清除现有装备
            db.delete("equipment", "user_id=?", new String[]{String.valueOf(userId)});

            // 从背包中添加所有工具到装备表
            Cursor cursor = db.query("backpack", null,
                    "user_id=? AND item_type IN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{
                            String.valueOf(userId),
                            ItemConstants.EQUIP_STONE_AXE, ItemConstants.EQUIP_IRON_AXE, ItemConstants.EQUIP_DIAMOND_AXE,
                            ItemConstants.EQUIP_STONE_PICKAXE, ItemConstants.EQUIP_IRON_PICKAXE, ItemConstants.EQUIP_DIAMOND_PICKAXE,
                            ItemConstants.EQUIP_STONE_SICKLE, ItemConstants.EQUIP_IRON_SICKLE, ItemConstants.EQUIP_DIAMOND_SICKLE,
                            ItemConstants.EQUIP_STONE_FISHING_ROD, ItemConstants.EQUIP_IRON_FISHING_ROD, ItemConstants.EQUIP_DIAMOND_FISHING_ROD
                    }, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("item_type"));
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow("item_count"));
                    int durability = cursor.getInt(cursor.getColumnIndexOrThrow("durability"));

                    // 为每个工具创建装备记录
                    for (int i = 0; i < count; i++) {
                        ContentValues values = new ContentValues();
                        values.put("user_id", userId);
                        values.put("equip_type", type);
                        values.put("durability", durability);
                        values.put("is_equipped", i == 0 ? 1 : 0); // 第一个装备默认装备
                        db.insert("equipment", null, values);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // 获取资源冷却信息（修复：不关闭数据库，由调用方管理）
    private Map<String, Object> getResourceCDInfoInternal(int userId, int x, int y, SQLiteDatabase db) {
        Map<String, Object> info = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = db.query("resource_cd", new String[]{"collect_count", "last_collect_time"},
                    "user_id=? AND area_x=? AND area_y=?",
                    new String[]{String.valueOf(userId), String.valueOf(x), String.valueOf(y)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                info.put("collect_count", cursor.getInt(0));
                info.put("last_collect_time", cursor.getLong(1));
            } else {
                info.put("collect_count", 0);
                info.put("last_collect_time", 0L);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return info;
    }

    // 公开方法：获取资源冷却信息
    public Map<String, Object> getResourceCDInfo(int userId, int x, int y) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            return getResourceCDInfoInternal(userId, x, y, db);
        } finally {

        }
    }

    // 更新资源冷却信息
    public void updateResourceCD(int userId, int x, int y, int collectCount, long lastCollectTime) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("area_x", x);
            values.put("area_y", y);
            values.put("collect_count", collectCount);
            values.put("last_collect_time", lastCollectTime);

            cursor = db.query("resource_cd", null,
                    "user_id=? AND area_x=? AND area_y=?",
                    new String[]{String.valueOf(userId), String.valueOf(x), String.valueOf(y)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                db.update("resource_cd", values,
                        "user_id=? AND area_x=? AND area_y=?",
                        new String[]{String.valueOf(userId), String.valueOf(x), String.valueOf(y)});
            } else {
                db.insert("resource_cd", null, values);
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
    }

    // 增加建筑
    public void addBuilding(int userId, String buildingType) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("building", null, "user_id=? AND building_type=?",
                    new String[]{String.valueOf(userId), buildingType}, null, null, null);

            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("building_type", buildingType);

            if (cursor.moveToFirst()) {
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                values.put("count", count + 1);
                db.update("building", values, "user_id=? AND building_type=?",
                        new String[]{String.valueOf(userId), buildingType});
            } else {
                values.put("count", 1);
                db.insert("building", null, values);
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
    }

    // 获取建筑数量
    public int getBuildingCount(int userId, String buildingType) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query("building", null, "user_id=? AND building_type=?",
                    new String[]{String.valueOf(userId), buildingType}, null, null, null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            }
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // if (db != null && db.isOpen()) db.close();
        }
        return count;
    }

    // 重置游戏
    public void resetGame(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. 先将背包中的资源转移到仓库
            Map<String, Integer> backpack = getBackpack(userId);
            android.util.Log.i("Reincarnation", "轮回前背包内容: " + backpack);

            int transferredItems = 0;
            for (Map.Entry<String, Integer> entry : backpack.entrySet()) {
                String itemName = entry.getKey();
                int itemCount = entry.getValue();
                // 将背包中的物品转移到仓库
                boolean success = moveItemToWarehouse(userId, itemName, itemCount);
                if (success) {
                    transferredItems++;
                    android.util.Log.i("Reincarnation", "成功转移物品到仓库: " + itemName + " x" + itemCount);
                } else {
                    android.util.Log.w("Reincarnation", "转移物品到仓库失败: " + itemName + " x" + itemCount);
                }
            }
            android.util.Log.i("Reincarnation", "总共转移了 " + transferredItems + " 种物品到仓库");

            // 2. 重置用户状态（位置、生存指标等）
            ContentValues statusValues = new ContentValues();
            statusValues.put("life", Constant.INIT_LIFE);
            statusValues.put("hunger", Constant.INIT_HUNGER);
            statusValues.put("thirst", Constant.INIT_THIRST);
            statusValues.put("stamina", Constant.INIT_STAMINA);
            statusValues.put("current_x", 0);  // 重置为初始坐标（后续会重新随机）
            statusValues.put("current_y", 0);
            statusValues.put("gold", 0);  // 金币也重置
            db.update("user_status", statusValues, "user_id=?", new String[]{String.valueOf(userId)});

            // 3. 清空背包（已经转移到仓库，这里清空剩余数据）
            db.delete("backpack", "user_id=?", new String[]{String.valueOf(userId)});

            // 4. 清空装备
            db.delete("equipment", "user_id=?", new String[]{String.valueOf(userId)});

            // 5. 清空资源冷却记录
            db.delete("resource_cd", "user_id=?", new String[]{String.valueOf(userId)});

            // 6. 清空所有建筑（包括传送门）
            db.delete("building", "user_id=?", new String[]{String.valueOf(userId)});

            // 7. 清空用户地形数据（修复轮回后建筑保留的问题）
            db.delete("user_terrain", "user_id=?", new String[]{String.valueOf(userId)});

            db.setTransactionSuccessful();
            android.util.Log.i("Reincarnation", "轮回重置完成，背包资源已转移到仓库，建筑和地形数据已清除");
        } finally {
            db.endTransaction();
        }
    }


    // 修复：更新采集记录（使用内部方法获取冷却信息，避免数据库重复关闭）
    public void updateCollectRecord(int currentUserId, int currentX, int currentY) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();

            // 使用内部方法获取冷却信息，共享同一个数据库连接
            Map<String, Object> cdInfo = getResourceCDInfoInternal(currentUserId, currentX, currentY, db);
            int collectCount = (int) cdInfo.get("collect_count");
            long lastCollectTime = (long) cdInfo.get("last_collect_time");

            values.put("collect_count", collectCount + 1);
            values.put("last_collect_time", System.currentTimeMillis());

            if (collectCount == 0) {
                values.put("user_id", currentUserId);
                values.put("area_x", currentX);
                values.put("area_y", currentY);
                db.insert("resource_cd", null, values);
            } else {
                db.update("resource_cd", values, "user_id=? AND area_x=? AND area_y=?",
                        new String[]{String.valueOf(currentUserId), String.valueOf(currentX), String.valueOf(currentY)});
            }
        } finally {

        }

    }

    // 新增更新希望点数的方法
    public void updateHopePoints(int userId, int hopePoints) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("hope_points", hopePoints);
        db.update(
                "user_status",
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)}
        );
    }


    /**
     * 获取指定用户的科技等级
     * @param userId 用户ID（int类型）
     * @param techId 科技ID
     * @return 科技等级
     */
    public int getTechLevel(int userId, String techId) {
        SQLiteDatabase db = getReadableDatabase();
        int level = 0;
        Cursor cursor = db.query(
                "tech",
                new String[]{"level"},
                "user_id = ? AND tech_id = ?",
                new String[]{String.valueOf(userId), techId},
                null, null, null
        );
        if (cursor.moveToFirst()) {
            level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
        }
        cursor.close();
        return level;
    }

    // 升级科技等级（等级+1）
    public void upgradeTechLevel(int userId, String techId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // 先查询当前等级
            int currentLevel = getTechLevel(userId, techId);

            ContentValues values = new ContentValues();
            values.put("level", currentLevel + 1);

            // 如果存在则更新，不存在则插入新记录
            int rowsAffected = db.update("tech", values,
                    "user_id=? AND tech_id=?",
                    new String[]{String.valueOf(userId), techId});

            if (rowsAffected == 0) {
                // 记录不存在，插入新记录（等级从1开始）
                values.put("user_id", userId);
                values.put("tech_id", techId);
                values.put("level", 1);
                db.insert("tech", null, values);
            }
        } finally {
            // 不关闭数据库，由SQLiteOpenHelper管理
        }
    }

    // 获取用户当前的希望点数（科技升级消耗）
    public int getHopePoints(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int hopePoints = 0;
        try {
            cursor = db.query("user_status", new String[]{"hope_points"},
                    "user_id=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                hopePoints = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return hopePoints;
    }

    // 扣除希望点数（升级科技时使用）
    public void reduceHopePoints(int userId, int cost) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            int currentPoints = getHopePoints(userId);
            if (currentPoints >= cost) {
                ContentValues values = new ContentValues();
                values.put("hope_points", currentPoints - cost);
                db.update("user_status", values,
                        "user_id=?",
                        new String[]{String.valueOf(userId)});
            }
        } finally {
            // 不关闭数据库，由SQLiteOpenHelper管理
        }
    }

    public void saveGameSlot(int userId, int slotId, String saveData, long saveTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("slot_id", slotId);
        values.put("save_data", saveData);
        values.put("save_time", saveTime);

        // 存在则更新，不存在则插入
        db.replace("save_slot", null, values);
    }

    // 获取所有存档
    public List<Map<String, Object>> getSaveSlots(int userId) {
        List<Map<String, Object>> slots = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("save_slot",
                    new String[]{"slot_id", "save_time"},
                    "user_id=?",
                    new String[]{String.valueOf(userId)},
                    null, null, "slot_id");

            while (cursor.moveToNext()) {
                Map<String, Object> slot = new HashMap<>();
                slot.put("slotId", cursor.getInt(0)); // slot_id
                slot.put("saveTime", cursor.getLong(1)); // save_time
                slots.add(slot);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return slots;
    }

    // 读取存档
    public SaveData loadSaveSlot(int userId, int slotId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("save_slot",
                new String[]{"save_data"},
                "user_id=? AND slot_id=?",
                new String[]{String.valueOf(userId), String.valueOf(slotId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            String json = cursor.getString(0);
            cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // db.close();
            return new Gson().fromJson(json, SaveData.class);
        }
        cursor.close();
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
        return null;
    }

    // 删除存档
    public void deleteSaveSlot(int userId, int slotId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("save_slot", "user_id=? AND slot_id=?",
                new String[]{String.valueOf(userId), String.valueOf(slotId)});
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
    }

    // 清空所有存档（用于轮回）
    public void clearAllSaveSlots(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("save_slot", "user_id=?", new String[]{String.valueOf(userId)});
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
    }

    public boolean restoreGameStatus(int userId, SaveData saveData) {
        if (saveData == null) {
            return false; // 存档数据为空，恢复失败
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction(); // 开启事务，确保数据一致性
        try {
            // 1. 更新用户状态表（user_status）
            ContentValues statusValues = new ContentValues();
            statusValues.put("life", saveData.life);
            statusValues.put("hunger", saveData.hunger);
            statusValues.put("thirst", saveData.thirst);
            statusValues.put("stamina", saveData.stamina);
            statusValues.put("current_x", saveData.currentX);
            statusValues.put("current_y", saveData.currentY);
            statusValues.put("gold", saveData.gold);
            statusValues.put("game_hour", saveData.gameHour);
            statusValues.put("game_day", saveData.gameDay);
            statusValues.put("temperature", saveData.temperature);
            // 其他需要恢复的状态字段（与 SaveData 类字段对应）
            db.update(
                    "user_status",
                    statusValues,
                    "user_id=?",
                    new String[]{String.valueOf(userId)}
            );

            // 2. 清空并恢复背包（backpack）
            db.delete("backpack", "user_id=?", new String[]{String.valueOf(userId)});
            for (Map.Entry<String, Integer> item : saveData.backpackItems.entrySet()) {
                ContentValues backpackValues = new ContentValues();
                backpackValues.put("user_id", userId);
                backpackValues.put("item_type", item.getKey());
                backpackValues.put("item_count", item.getValue());
                backpackValues.put("durability", saveData.itemDurability.getOrDefault(item.getKey(), 0));
                db.insert("backpack", null, backpackValues);
            }

            // 3. 恢复装备（equipment）
            db.delete("equipment", "user_id=?", new String[]{String.valueOf(userId)});
            for (String equipType : saveData.equippedItems) {
                ContentValues equipValues = new ContentValues();
                equipValues.put("user_id", userId);
                equipValues.put("equip_type", equipType);
                equipValues.put("is_equipped", 1);
                db.insert("equipment", null, equipValues);
            }

            // 4. 恢复其他需要的表（如建筑、科技等，根据 SaveData 字段补充）
            // ...

            db.setTransactionSuccessful(); // 事务成功
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // 定义存档数据实体类（用于Gson解析，放在DBHelper内部或单独文件）
    public static class SavedGameData {
        public int life;
        public int hunger;
        public int thirst;
        public int stamina;
        public int currentX;
        public int currentY;
        public int gold;
        public int gameHour;
        public int gameDay;
        public int temperature;
        public Map<String, Integer> backpackItems; // 物品类型 -> 数量
        public Map<String, Integer> itemDurability; // 物品类型 -> 耐久度
        public List<String> equippedItems; // 已装备的物品类型
        // 其他需要存档的字段...
    }
    public Map<String, Integer> getAllDurability(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Map<String, Integer> durabilityMap = new HashMap<>();
        Cursor cursor = db.query("backpack",
                new String[]{"item_type", "durability"},
                "user_id=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String itemType = cursor.getString(0);
                int durability = cursor.getInt(1);
                durabilityMap.put(itemType, durability);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return durabilityMap;
    }

    // DBHelper.java 中新增：获取用户已装备物品
    public List<String> getEquippedItems(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> equippedList = new ArrayList<>();
        Cursor cursor = db.query("equipment",
                new String[]{"equip_type"},
                "user_id=? AND is_equipped=1",
                new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                equippedList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return equippedList;
    }

    public void setNormalDifficultyCleared(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_normal_cleared", 1);
        db.update("user_status", values, "user_id=?", new String[]{String.valueOf(userId)});
    }

    // 新增：查询普通难度是否通关
    public boolean isNormalDifficultyCleared(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("user_status", new String[]{"is_normal_cleared"},
                "user_id=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor.moveToFirst()) {
            int cleared = cursor.getInt(0);
            cursor.close();
            return cleared == 1;
        }
        cursor.close();
        return false;
    }

    // 新增：设置简单难度通关
    public void setEasyDifficultyCleared(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_easy_cleared", 1);
        db.update("user_status", values, "user_id=?", new String[]{String.valueOf(userId)});
    }

    // 新增：查询简单难度是否通关
    public boolean isEasyDifficultyCleared(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("user_status", new String[]{"is_easy_cleared"},
                "user_id=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor.moveToFirst()) {
            int cleared = cursor.getInt(0);
            cursor.close();
            return cleared == 1;
        }
        cursor.close();
        return false;
    }

    // 新增：设置困难难度通关
    public void setHardDifficultyCleared(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_hard_cleared", 1);
        db.update("user_status", values, "user_id=?", new String[]{String.valueOf(userId)});
    }

    // 新增：查询困难难度是否通关
    public boolean isHardDifficultyCleared(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("user_status", new String[]{"is_hard_cleared"},
                "user_id=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor.moveToFirst()) {
            int cleared = cursor.getInt(0);
            cursor.close();
            return cleared == 1;
        }
        cursor.close();
        return false;
    }
    /**
     * 向背包添加指定数量的物品
     * @param userId 用户ID
     * @param itemType 物品类型
     * @param count 添加数量（正数）
     */
    public void addItem(int userId, String itemType, int count) {
        if (count <= 0) return; // 确保数量为正数
        updateBackpackItem(userId, itemType, count); // 正数表示增加数量
    }
    // 添加物品到背包
    public void addItemToBackpack(int userId, String itemType, int count) {
        updateBackpackItem(userId, itemType, count); // 复用现有更新方法
    }

    // 从背包移除物品
    /**
     * 从背包移除指定数量的物品
     * @param userId 用户ID
     * @param itemType 物品类型
     * @param count 移除数量（正数）
     */
    public void removeItem(int userId, String itemType, int count) {
        if (count <= 0) return; // 确保数量为正数
        updateBackpackItem(userId, itemType, -count); // 负数表示减少数量
    }

    // 获取资源冷却时间
    public long getResourceCD(int userId, int x, int y) {
        // 实现逻辑：查询resource_cd表的冷却时间
        return 0; // 替换为实际查询结果
    }

    public boolean hasSaveData(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "save_slot",
                new String[]{"id"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
        boolean hasData = cursor.getCount() > 0;
        cursor.close();
        return hasData;
    }

    // 记录事件
    public void recordEvent(int userId, String eventType, long endTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("event_type", eventType);
        values.put("is_active", 1);
        values.put("create_time", System.currentTimeMillis());
        values.put("end_time", endTime);
        db.insert("event", null, values);
    }

    // 获取活跃事件
    public Map<String, Object> getActiveEvent(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "event",
                new String[]{"id", "event_type", "end_time"},
                "user_id = ? AND is_active = 1 AND end_time > ?",
                new String[]{String.valueOf(userId), String.valueOf(System.currentTimeMillis())},
                null, null, "create_time DESC", "1"
        );

        Map<String, Object> event = null;
        if (cursor.moveToFirst()) {
            event = new HashMap<>();
            event.put("id", cursor.getInt(0));
            event.put("type", cursor.getString(1));
            event.put("end_time", cursor.getLong(2));
        }
        cursor.close();
        return event;
    }

    /**
     * 初始化测试账号(admin/123456)
     * 包含99个所有资源、4个所有工具、解锁所有建筑
     */
    public void initTestAccount() {
        int testUserId = getAdminUserId(); // 获取admin账号的ID
        if (testUserId == -1) return; // 若admin账号不存在则返回

        // 仅当测试账号未初始化时执行（传入userId参数）
        if (!isTestAccountInitialized(testUserId)) {
            // 1. 初始化用户基础状态（生存指标、金钱、背包容量等）
            Map<String, Object> initialStatus = TestAccountInitializer.getInitialStatus();
            updateUserStatus(testUserId, initialStatus);

            // 2. 初始化背包资源（所有非工具资源99个）
            Map<String, Integer> initialBackpack = TestAccountInitializer.getInitialBackpack();
            for (Map.Entry<String, Integer> entry : initialBackpack.entrySet()) {
                addItemToBackpack(testUserId, entry.getKey(), entry.getValue(), 0); // 4参数匹配
            }

            // 3. 初始化装备（补充4个参数：用户ID、类型、耐久度、是否装备）
            Map<String, Integer> initialEquipment = TestAccountInitializer.getInitialEquipment();
            for (Map.Entry<String, Integer> entry : initialEquipment.entrySet()) {
                String equipType = entry.getKey();
                int durability = entry.getValue();
                // 调用4参数的addEquipment方法（最后一个参数false表示初始未装备）
                addEquipment(testUserId, equipType, durability, false);
            }

            // 标记测试账号已初始化（传入userId参数）
            markTestAccountInitialized(testUserId);
        }
    }

    // 辅助方法：获取admin账号的ID（不存在则返回-1）
    private int getAdminUserId() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("user",
                new String[]{"id"},
                "username = ?",
                new String[]{"admin"}, // 测试账号固定为admin
                null, null, null);
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    // 新增：将指定用户的某类型装备全部设为未装备（解决286行错误）
    public void updateAllEquipmentStatus(int userId, String equipType, boolean isEquipped) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_equipped", isEquipped ? 1 : 0); // 1表示true，0表示false
        // 更新条件：用户ID匹配 + 装备类型匹配
        db.update("equipment", values, "user_id = ? AND equip_type = ?",
                new String[]{String.valueOf(userId), equipType});
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
    }
    
    // 新增：将所有装备设为未装备状态（重载方法，用于装备更换时取消所有装备）
    public void updateAllEquipmentStatus(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_equipped", 0); // 设为未装备
        // 更新条件：只匹配用户ID，不指定装备类型（更新所有装备）
        db.update("equipment", values, "user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    // 新增：更新装备状态（基于ID）
    public void updateEquipmentStatus(int userId, String equipType, boolean isEquipped) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_equipped", isEquipped ? 1 : 0);
        // 更新条件：用户ID匹配 + 装备类型匹配
        db.update("equipment", values, "user_id = ? AND equip_type = ?",
                new String[]{String.valueOf(userId), equipType});
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
    }

    // 新增：删除指定用户的某类型且指定耐久的装备（解决271行参数数量错误）
    public void deleteEquipment(int userId, String equipType, int durability) {
        SQLiteDatabase db = getWritableDatabase();
        // 删除条件：用户ID + 装备类型 + 耐久度匹配（确保删除唯一装备）
        db.delete("equipment", "user_id = ? AND equip_type = ? AND durability = ?",
                new String[]{String.valueOf(userId), equipType, String.valueOf(durability)});
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
    }

    /**
     * 更新所有现有工具的耐久度上限（科技升级后调用）
     * @param userId 用户ID
     */
    public void updateAllToolsMaxDurability(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // 获取所有工具类物品
            Cursor cursor = db.query(
                    "backpack",
                    new String[]{"item_type", "durability"},
                    "user_id = ? AND item_type IN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{String.valueOf(userId),
                            ItemConstants.EQUIP_STONE_AXE, ItemConstants.EQUIP_IRON_AXE, ItemConstants.EQUIP_DIAMOND_AXE,
                            ItemConstants.EQUIP_STONE_PICKAXE, ItemConstants.EQUIP_IRON_PICKAXE, ItemConstants.EQUIP_DIAMOND_PICKAXE,
                            ItemConstants.EQUIP_STONE_SICKLE, ItemConstants.EQUIP_IRON_SICKLE, ItemConstants.EQUIP_DIAMOND_SICKLE,
                            ItemConstants.EQUIP_STONE_FISHING_ROD, ItemConstants.EQUIP_IRON_FISHING_ROD, ItemConstants.EQUIP_DIAMOND_FISHING_ROD
                    },
                    null, null, null
            );

            while (cursor.moveToNext()) {
                String itemType = cursor.getString(cursor.getColumnIndexOrThrow("item_type"));
                int currentDurability = cursor.getInt(cursor.getColumnIndexOrThrow("durability"));

                // 获取新的耐久度上限（包含科技加成）
                int newMaxDurability = ToolUtils.getToolInitialDurability(itemType, userId);

                // 如果当前耐久度超过新的上限，则调整为上限值
                int adjustedDurability = Math.min(currentDurability, newMaxDurability);

                // 更新耐久度
                ContentValues values = new ContentValues();
                values.put("durability", adjustedDurability);
                db.update("backpack", values, "user_id=? AND item_type=?",
                        new String[]{String.valueOf(userId), itemType});
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // db.close();
        }
    }

    /**
     * 减少建筑数量（如拆除）
     * @param userId 用户ID
     * @param buildingType 建筑类型
     * @param count 减少的数量（默认1）
     */
    public void removeBuilding(int userId, String buildingType, int count) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        // 先查询当前数量
        Cursor cursor = db.query(
                "building",
                new String[]{"count"},
                "user_id = ? AND building_type = ?",
                new String[]{String.valueOf(userId), buildingType},
                null, null, null
        );

        int currentCount = 0;
        if (cursor.moveToFirst()) {
            currentCount = cursor.getInt(0);
        }
        cursor.close();

        // 计算新数量（不能为负）
        int newCount = Math.max(0, currentCount - count);
        if (newCount == 0) {
            // 数量为0时删除记录
            db.delete(
                    "building",
                    "user_id = ? AND building_type = ?",
                    new String[]{String.valueOf(userId), buildingType}
            );
        } else {
            // 否则更新数量
            values.put("count", newCount);
            db.update(
                    "building",
                    values,
                    "user_id = ? AND building_type = ?",
                    new String[]{String.valueOf(userId), buildingType}
            );
        }
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
    }

    public void removeBuilding(int userId, String buildingType) {
        removeBuilding(userId, buildingType, 1);
    }

    /**
     * 获取背包中指定物品的数量
     * @param userId 用户ID
     * @param itemType 物品类型
     * @return 物品数量（默认0）
     */
    public int getItemCount(int userId, String itemType) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "backpack",
                new String[]{"item_count"},
                "user_id = ? AND item_type = ?",
                new String[]{String.valueOf(userId), itemType},
                null, null, null
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
        return count;
    }

    /**
     * 扣除背包中的物品
     * @param userId 用户ID
     * @param itemType 物品类型
     * @param count 扣除数量
     */
    public void deductItem(int userId, String itemType, int count) {
        updateBackpackItem(userId, itemType, -count); // 复用现有方法
    }

    public void updateTerrain(int userId, int x, int y, String terrainType) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("terrain_type", terrainType);
        db.update("map", values, "user_id=? AND area_x=? AND area_y=?",
                new String[]{String.valueOf(userId), String.valueOf(x), String.valueOf(y)});
    }

    // 检查测试账号是否已初始化
    public boolean isTestAccountInitialized(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("user_status",
                new String[]{"is_test_initialized"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        boolean initialized = false;
        if (cursor.moveToFirst()) {
            initialized = cursor.getInt(0) == 1;
        }
        cursor.close();
        return initialized;
    }

    // 标记测试账号已初始化
    public void markTestAccountInitialized(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_test_initialized", 1);
        db.update("user_status", values, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    // 新增：带耐久度的背包物品添加方法（解决参数不匹配问题）
    public void addItemToBackpack(int userId, String itemType, int count, int durability) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("item_type", itemType);
        values.put("item_count", count);
        values.put("durability", durability);

        // 存在则更新，不存在则插入
        long rows = db.update("backpack", values,
                "user_id = ? AND item_type = ?",
                new String[]{String.valueOf(userId), itemType});
        if (rows == 0) {
            db.insert("backpack", null, values);
        }
    }

    // 新增：添加装备方法
    public void addEquipment(int userId, String equipType, int durability, boolean isEquipped) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("equip_type", equipType);
        values.put("durability", durability);
        values.put("is_equipped", isEquipped ? 1 : 0);
        db.insert("equipment", null, values);
    }

    // 新增：密码加密方法（SHA-256）
    private String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // 新增：在数据库创建时初始化测试账号（admin）
    private void initAdminTestAccountOnCreate(SQLiteDatabase db) {
        try {
            // 1. 检查admin账号是否存在
            Cursor cursor = db.query("user", new String[]{"id"},
                    "username = ?", new String[]{"admin"}, null, null, null);
            int adminUserId = -1;
            if (cursor.moveToFirst()) {
                adminUserId = cursor.getInt(0);
            } else {
                // 2. 创建admin账号（密码加密）- 使用特殊UID -100
                ContentValues userValues = new ContentValues();
                userValues.put("id", MyApplication.TEST_ACCOUNT_UID); // 设置特殊UID
                userValues.put("username", "admin");
                userValues.put("password", encryptPassword("123456")); // 加密密码
                userValues.put("is_test", 1); // 标记为测试账号
                adminUserId = (int) db.insert("user", null, userValues);

                // 3. 初始化用户状态（基础字段）- 使用特殊UID -100
                ContentValues statusValues = new ContentValues();
                statusValues.put("user_id", MyApplication.TEST_ACCOUNT_UID);
                statusValues.put("life", Constant.INIT_LIFE);
                statusValues.put("hunger", Constant.INIT_HUNGER);
                statusValues.put("thirst", Constant.INIT_THIRST);
                statusValues.put("stamina", Constant.INIT_STAMINA);
                statusValues.put("current_x", 0);
                statusValues.put("current_y", 0);
                statusValues.put("backpack_cap", 10); // 临时值，后续会被覆盖
                statusValues.put("gold", 0); // 临时值，后续会被覆盖
                statusValues.put("hope_points", 0); // 临时值，后续会被覆盖
                statusValues.put("is_test_initialized", 0); // 标记未初始化
                db.insert("user_status", null, statusValues);
            }
            cursor.close();

            // 4. 仅对未初始化的测试账号应用初始属性
            if (adminUserId != -1) {
                applyTestAccountInitialPropsOnCreate(db, adminUserId);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "初始化测试账号失败: " + e.getMessage());
        }
    }

    // 新增：初始化测试账号（admin）
    public void initAdminTestAccount() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. 检查admin账号是否存在
            Cursor cursor = db.query("user", new String[]{"id"},
                    "username = ?", new String[]{"admin"}, null, null, null);
            int adminUserId = -1;
            if (cursor.moveToFirst()) {
                adminUserId = cursor.getInt(0);
            } else {
                // 2. 创建admin账号（密码加密）
                ContentValues userValues = new ContentValues();
                userValues.put("username", "admin");
                userValues.put("password", encryptPassword("123456")); // 加密密码
                userValues.put("is_test", 1); // 标记为测试账号
                adminUserId = (int) db.insert("user", null, userValues);

                // 3. 初始化用户状态（基础字段）
                ContentValues statusValues = new ContentValues();
                statusValues.put("user_id", adminUserId);
                statusValues.put("life", Constant.INIT_LIFE);
                statusValues.put("hunger", Constant.INIT_HUNGER);
                statusValues.put("thirst", Constant.INIT_THIRST);
                statusValues.put("stamina", Constant.INIT_STAMINA);
                statusValues.put("current_x", 0);
                statusValues.put("current_y", 0);
                statusValues.put("backpack_cap", 10); // 临时值，后续会被覆盖
                statusValues.put("gold", 0); // 临时值，后续会被覆盖
                statusValues.put("hope_points", 0); // 临时值，后续会被覆盖
                statusValues.put("is_test_initialized", 0); // 标记未初始化
                db.insert("user_status", null, statusValues);
            }
            cursor.close();

            // 4. 仅对未初始化的测试账号应用初始属性
            if (adminUserId != -1 && !isTestAccountInitialized(adminUserId)) {
                applyTestAccountInitialProps(adminUserId);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    // 应用测试账号初始属性（在数据库创建时调用）
    private void applyTestAccountInitialPropsOnCreate(SQLiteDatabase db, int userId) {
        try {
            // 2. 更新用户状态（金钱、背包容量等）
            Map<String, Object> initialStatus = TestAccountInitializer.getInitialStatus();
            ContentValues statusValues = new ContentValues();
            for (Map.Entry<String, Object> entry : initialStatus.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Integer) {
                    statusValues.put(key, (Integer) value);
                } else if (value instanceof String) {
                    statusValues.put(key, (String) value);
                } else if (value instanceof Long) {
                    statusValues.put(key, (Long) value);
                }
            }
            db.update("user_status", statusValues, "user_id = ?", new String[]{String.valueOf(userId)});

            // 3. 添加初始背包资源（处理UNIQUE约束，已存在则更新数量）
            Map<String, Integer> initialBackpack = TestAccountInitializer.getInitialBackpack();
            for (Map.Entry<String, Integer> entry : initialBackpack.entrySet()) {
                String itemType = entry.getKey();
                int count = entry.getValue();

                ContentValues backpackValues = new ContentValues();
                backpackValues.put("user_id", userId);
                backpackValues.put("item_type", itemType);
                backpackValues.put("item_count", count);
                backpackValues.put("durability", 0);
                backpackValues.put("is_equipped", 0);

                try {
                    db.insert("backpack", null, backpackValues);
                } catch (SQLiteConstraintException e) {
                    // 如果已存在，则更新数量
                    db.update("backpack", backpackValues, "user_id = ? AND item_type = ?",
                            new String[]{String.valueOf(userId), itemType});
                }
            }

            // 4. 标记为已初始化（确保测试账号不会重复初始化）
            ContentValues values = new ContentValues();
            values.put("is_test_initialized", 1);
            db.update("user_status", values, "user_id = ?", new String[]{String.valueOf(userId)});

        } catch (Exception e) {
            Log.e("DBHelper", "应用测试账号初始属性失败: " + e.getMessage());
        }
    }

    // 应用测试账号初始属性（提取为单独方法）
    private void applyTestAccountInitialProps(int userId) {
        // 1. 获取可写数据库实例（解决 "Cannot resolve symbol 'db'" 错误）
        SQLiteDatabase db = getWritableDatabase();

        try {
            // 2. 更新用户状态（金钱、背包容量等）
            Map<String, Object> initialStatus = TestAccountInitializer.getInitialStatus();
            ContentValues statusValues = new ContentValues();

            // 安全类型转换（避免空指针或类型错误）
            statusValues.put("gold", getIntFromMap(initialStatus, "gold", 0));
            statusValues.put("hope_points", getIntFromMap(initialStatus, "hope_points", 0));
            statusValues.put("backpack_cap", getIntFromMap(initialStatus, "backpack_cap", 10));
            statusValues.put("is_synthesis_unlocked", getIntFromMap(initialStatus, "is_synthesis_unlocked", 1));
            statusValues.put("is_building_unlocked", getIntFromMap(initialStatus, "is_building_unlocked", 1));
            // 补充其他需要初始化的状态字段（如解锁状态）
            statusValues.put("is_cooking_unlocked", getIntFromMap(initialStatus, "is_cooking_unlocked", 1));
            statusValues.put("is_smelting_unlocked", getIntFromMap(initialStatus, "is_smelting_unlocked", 1));

            // 执行更新（where条件确保只更新当前用户）
            db.update("user_status", statusValues, "user_id = ?", new String[]{String.valueOf(userId)});

            // 3. 添加初始背包资源（处理UNIQUE约束，已存在则更新数量）
            Map<String, Integer> initialBackpack = TestAccountInitializer.getInitialBackpack();
            for (Map.Entry<String, Integer> entry : initialBackpack.entrySet()) {
                String itemType = entry.getKey();
                int count = entry.getValue();
                addOrUpdateBackpackItem(db, userId, itemType, count, 0); // 非工具耐久度为0
            }

            // 4. 标记为已初始化（确保测试账号不会重复初始化）
            markTestAccountInitialized(db, userId);

        } finally {
            // 不需要手动关闭db（系统会管理），但可根据需求处理
        }
    }

    // 辅助方法：安全从Map获取int值
    private int getIntFromMap(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }

    // 辅助方法：添加或更新背包物品（处理UNIQUE(user_id, item_type)约束）
    private void addOrUpdateBackpackItem(SQLiteDatabase db, int userId, String itemType, int count, int durability) {
        // 先查询物品是否已存在
        Cursor cursor = db.query(
                "backpack",
                new String[]{"item_count"},
                "user_id = ? AND item_type = ?",
                new String[]{String.valueOf(userId), itemType},
                null, null, null
        );

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("item_type", itemType);
        values.put("durability", durability);

        if (cursor.moveToFirst()) {
            // 已存在则更新数量（累加）
            int currentCount = cursor.getInt(0);
            values.put("item_count", currentCount + count);
            db.update(
                    "backpack",
                    values,
                    "user_id = ? AND item_type = ?",
                    new String[]{String.valueOf(userId), itemType}
            );
        } else {
            // 不存在则插入新记录
            values.put("item_count", count);
            db.insert("backpack", null, values);
        }
        cursor.close(); // 关闭游标释放资源
    }

    // 标记测试账号已初始化（使用独立字段避免与其他逻辑冲突）
    private void markTestAccountInitialized(SQLiteDatabase db, int userId) {
        ContentValues values = new ContentValues();
        // 建议在user_status表中新增is_test_initialized字段，若未新增可临时用现有字段
        values.put("is_synthesis_unlocked", 2); // 用2表示已初始化（区别于1的默认解锁）
        db.update(
                "user_status",
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean isTestAccount(int userId) {
        // 通过特殊UID判断是否为测试账号
        boolean isTest = (userId == MyApplication.TEST_ACCOUNT_UID);
        Log.d("DBHelper", "isTestAccount: 用户 " + userId + " 是否为测试账号 = " + isTest);
        return isTest;
    }

    /**
     * 重新初始化测试账号的数据（应用特权）
     */
    public void reinitTestAccountData(int userId) {
        Log.d("DBHelper", "reinitTestAccountData: 开始重新初始化测试账号 " + userId + " 的特权数据");

        // 先检查用户是否为测试账号
        boolean isTest = isTestAccount(userId);
        Log.d("DBHelper", "reinitTestAccountData: 用户 " + userId + " 是否为测试账号 = " + isTest);

        if (!isTest) {
            Log.d("DBHelper", "reinitTestAccountData: 用户 " + userId + " 不是测试账号，跳过特权初始化");
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. 重置用户状态为测试账号初始值
            Map<String, Object> initialStatus = TestAccountInitializer.getInitialStatus();

            // 调试日志：显示测试账号特权设置
            Log.d("DBHelper", "reinitTestAccountData: 测试账号特权设置 - " + initialStatus.toString());

            // 特别检查关键特权字段
            Integer backpackCap = (Integer) initialStatus.get("backpack_cap");
            Integer hopePoints = (Integer) initialStatus.get("hope_points");
            Integer gold = (Integer) initialStatus.get("gold");
            Log.d("DBHelper", "reinitTestAccountData: 关键特权字段 - 背包容量=" + backpackCap + ", 希望点数=" + hopePoints + ", 金币=" + gold);

            ContentValues statusValues = new ContentValues();
            for (Map.Entry<String, Object> entry : initialStatus.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Integer) {
                    statusValues.put(key, (Integer) value);
                } else if (value instanceof Long) {
                    statusValues.put(key, (Long) value);
                } else if (value instanceof String) {
                    statusValues.put(key, (String) value);
                }
            }

            // 先检查用户状态记录是否存在
            Cursor checkCursor = db.query("user_status", new String[]{"user_id"},
                    "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
            boolean recordExists = checkCursor.moveToFirst();
            checkCursor.close();

            Log.d("DBHelper", "reinitTestAccountData: 用户 " + userId + " 状态记录存在 = " + recordExists);

            int affectedRows;
            if (recordExists) {
                // 记录存在，执行更新
                affectedRows = db.update("user_status", statusValues, "user_id = ?",
                        new String[]{String.valueOf(userId)});
                Log.d("DBHelper", "reinitTestAccountData: 更新用户状态表，影响行数=" + affectedRows);

                // 验证更新结果
                if (affectedRows > 0) {
                    Log.d("DBHelper", "reinitTestAccountData: 用户状态更新成功");
                } else {
                    Log.e("DBHelper", "reinitTestAccountData: 用户状态更新失败，影响行数为0");
                }
            } else {
                // 记录不存在，先插入记录
                statusValues.put("user_id", userId);
                long insertResult = db.insert("user_status", null, statusValues);
                affectedRows = insertResult != -1 ? 1 : 0;
                Log.d("DBHelper", "reinitTestAccountData: 插入用户状态表，结果=" + insertResult + ", 影响行数=" + affectedRows);
            }

            // 2. 重置背包为测试账号初始资源
            Map<String, Integer> initialBackpack = TestAccountInitializer.getInitialBackpack();

            // 调试日志：显示背包资源设置
            Log.d("DBHelper", "reinitTestAccountData: 测试账号背包资源设置 - " + initialBackpack.toString());

            // 先清空现有背包
            int deletedRows = db.delete("backpack", "user_id = ?", new String[]{String.valueOf(userId)});
            Log.d("DBHelper", "reinitTestAccountData: 清空背包，删除行数=" + deletedRows);

            // 插入测试资源
            int insertedCount = 0;
            for (Map.Entry<String, Integer> entry : initialBackpack.entrySet()) {
                ContentValues backpackValues = new ContentValues();
                backpackValues.put("user_id", userId);
                backpackValues.put("item_type", entry.getKey());
                backpackValues.put("item_count", entry.getValue());
                backpackValues.put("durability", 0); // 非工具默认耐久0
                long result = db.insert("backpack", null, backpackValues);
                if (result != -1) insertedCount++;
            }
            Log.d("DBHelper", "reinitTestAccountData: 插入背包资源，成功插入=" + insertedCount + " 项");

            // 3. 清空装备（测试账号初始无装备）
            int deletedEquipRows = db.delete("equipment", "user_id = ?", new String[]{String.valueOf(userId)});
            Log.d("DBHelper", "reinitTestAccountData: 清空装备，删除行数=" + deletedEquipRows);

            db.setTransactionSuccessful();
            Log.d("DBHelper", "reinitTestAccountData: 测试账号 " + userId + " 特权数据重新初始化完成");
        } catch (Exception e) {
            Log.e("DBHelper", "reinitTestAccountData: 初始化测试账号数据失败", e);
        } finally {
            db.endTransaction();
        }
    }



    /**
     * 更新用户的科技等级
     * @param userId 用户ID（int类型）
     * @param techId 科技ID
     * @param level 新等级
     */
    public void updateTechLevel(int userId, String techId, int level) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("level", level);

        // 尝试更新现有记录
        int rowsAffected = db.update(
                "tech",
                values,
                "user_id = ? AND tech_id = ?",
                new String[]{String.valueOf(userId), techId}
        );

        // 如果没有现有记录，则插入新记录
        if (rowsAffected == 0) {
            values.put("user_id", userId);
            values.put("tech_id", techId);
            db.insert("tech", null, values);
        }
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
    }

    /**
     * 更新用户的希望点数
     * @param userId 用户ID（int类型）
     * @param hopePoints 新的希望点数
     */

    /**
     * 更新区域采集次数
     */
    public void updateAreaCollectTimes(int userId, int x, int y, int times) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("area_x", x);
        values.put("area_y", y);
        values.put("collect_times", times);
        // 存在则更新，不存在则插入
        db.replace("area_collect", null, values);
    }

    /**
     * 清空用户的所有区域采集次数（0点刷新时调用）
     */
    public void clearAreaCollectTimes(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("area_collect", "user_id=?", new String[]{String.valueOf(userId)});
    }

    /**
     * 加载用户的区域采集次数到内存（从数据库到areaCollectTimes）
     */
    public Map<String, Integer> loadAreaCollectTimes(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Map<String, Integer> map = new HashMap<>();
        // 将查询列名 collect_times 改为 collect_count（与表结构一致）
        Cursor cursor = db.query("area_collect",
                new String[]{"area_x", "area_y", "collect_count"},
                "user_id=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        while (cursor.moveToNext()) {
            int x = cursor.getInt(0);
            int y = cursor.getInt(1);
            int times = cursor.getInt(2); // 此处索引不变（第三列仍为采集次数）
            map.put(x + "," + y, times); // 用"x,y"作为key
        }
        cursor.close();
        return map;
    }

    /**
     * 获取用户的背包容量
     * @param userId 用户ID
     * @return 背包容量（默认返回初始容量）
     */
    public int getBackpackCapacity(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        int capacity = -1; // 初始设为-1，表示未获取到有效值
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "user_status",
                    new String[]{"backpack_cap"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );
            if (cursor.moveToFirst()) {
                capacity = cursor.getInt(cursor.getColumnIndexOrThrow("backpack_cap"));
            }
        } catch (Exception e) {
            Log.e("DBHelper", "获取背包容量失败", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        // 如果查询失败或未获取到有效值，返回默认容量
        if (capacity == -1) {
            return Constant.BACKPACK_INIT_CAP;
        }
        return capacity;
    }
    /**
     * 更新区域采集次数到数据库
     * @param userId 用户ID
     * @param x 区域X坐标
     * @param y 区域Y坐标
     * @param newCount 新的采集次数（collectCount + 1）
     * @param lastTime 最后采集时间
     */
    public void updateAreaCollectCount(int userId, int x, int y, int newCount, long lastTime) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("collect_count", newCount);
            values.put("last_collect_time", lastTime);

            // 关键修复：WHERE条件用area_x和area_y（与表结构一致）
            int rowsAffected = db.update(
                    "area_collect",
                    values,
                    "user_id = ? AND area_x = ? AND area_y = ?",  // 这里改为area_x和area_y
                    new String[]{String.valueOf(userId), String.valueOf(x), String.valueOf(y)}
            );

            if (rowsAffected == 0) {
                values.put("user_id", userId);
                values.put("area_x", x);  // 插入时用area_x
                values.put("area_y", y);  // 插入时用area_y
                db.insert("area_collect", null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 获取指定区域的采集数据（次数和最后采集时间）
     * @param userId 用户ID
     * @param areaX 区域X坐标
     * @param areaY 区域Y坐标
     * @return 包含collect_count和last_collect_time的Map，无数据则返回默认值
     */
    public Map<String, Object> getAreaCollectData(int userId, int areaX, int areaY) {
        SQLiteDatabase db = getReadableDatabase();
        Map<String, Object> result = new HashMap<>();
        // 默认值：采集次数0，最后采集时间0
        result.put("collect_count", 0);
        result.put("last_collect_time", 0L);

        Cursor cursor = null;
        try {
            // 查询指定用户、指定坐标的区域采集数据
            cursor = db.query(
                    "area_collect",
                    new String[]{"collect_count", "last_collect_time"},
                    "user_id = ? AND area_x = ? AND area_y = ?",
                    new String[]{String.valueOf(userId), String.valueOf(areaX), String.valueOf(areaY)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // 读取采集次数（默认0）
                int collectCount = cursor.getInt(cursor.getColumnIndexOrThrow("collect_count"));
                // 读取最后采集时间（默认0）
                long lastCollectTime = cursor.getLong(cursor.getColumnIndexOrThrow("last_collect_time"));

                result.put("collect_count", collectCount);
                result.put("last_collect_time", lastCollectTime);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "获取区域采集数据失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // db.close();
        }
        return result;
    }

    /**
     * 增加用户的全局采集次数（+1）
     * @param userId 用户ID
     */
    public Integer incrementGlobalCollectTimes(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // 1. 先查询当前全局采集次数
            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"global_collect_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            int currentCount = 0;
            if (cursor.moveToFirst()) {
                // 安全获取当前值（默认0）
                currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("global_collect_times"));
            }
            cursor.close();

            // 2. 自增1
            int newCount = currentCount + 1;

            // 3. 更新数据库
            ContentValues values = new ContentValues();
            values.put("global_collect_times", newCount);
            db.update(
                    "user_status",
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            // 4. 返回更新后的值
            return newCount;
        } catch (Exception e) {
            Log.e("DBHelper", "更新全局采集次数失败", e);
            return 0; // 异常时返回默认值
        } finally {
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // db.close();
        }
    }

    /**
     * 获取用户的探索次数
     * @param userId 用户ID
     * @return 探索次数
     */
    public int getExplorationTimes(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"exploration_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            int currentCount = 0;
            if (cursor.moveToFirst()) {
                currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("exploration_times"));
            }
            cursor.close();
            return currentCount;
        } catch (Exception e) {
            Log.e("DBHelper", "获取探索次数失败", e);
            return 0;
        }
    }

    /**
     * 增加用户的探索次数（+1）
     * @param userId 用户ID
     */
    public Integer incrementExplorationTimes(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"exploration_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            int currentCount = 0;
            if (cursor.moveToFirst()) {
                currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("exploration_times"));
            }
            cursor.close();

            int newCount = currentCount + 1;
            ContentValues values = new ContentValues();
            values.put("exploration_times", newCount);
            db.update("user_status", values, "user_id = ?", new String[]{String.valueOf(userId)});

            return newCount;
        } catch (Exception e) {
            Log.e("DBHelper", "更新探索次数失败", e);
            return 0;
        }
    }

    /**
     * 增加用户的合成次数（+1）
     * @param userId 用户ID
     */
    public Integer incrementSynthesisTimes(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"synthesis_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            int currentCount = 0;
            if (cursor.moveToFirst()) {
                currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("synthesis_times"));
            }
            cursor.close();

            int newCount = currentCount + 1;
            ContentValues values = new ContentValues();
            values.put("synthesis_times", newCount);
            db.update("user_status", values, "user_id = ?", new String[]{String.valueOf(userId)});

            return newCount;
        } catch (Exception e) {
            Log.e("DBHelper", "更新合成次数失败", e);
            return 0;
        }
    }

    /**
     * 增加用户的熔炼次数（+1）
     * @param userId 用户ID
     */
    public Integer incrementSmeltingTimes(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"smelting_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            int currentCount = 0;
            if (cursor.moveToFirst()) {
                currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("smelting_times"));
            }
            cursor.close();

            int newCount = currentCount + 1;
            ContentValues values = new ContentValues();
            values.put("smelting_times", newCount);
            db.update("user_status", values, "user_id = ?", new String[]{String.valueOf(userId)});

            return newCount;
        } catch (Exception e) {
            Log.e("DBHelper", "更新熔炼次数失败", e);
            return 0;
        }
    }

    /**
     * 增加用户的贸易次数（+1）
     * @param userId 用户ID
     */
    public Integer incrementTradingTimes(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"trading_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            int currentCount = 0;
            if (cursor.moveToFirst()) {
                currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("trading_times"));
            }
            cursor.close();

            int newCount = currentCount + 1;
            ContentValues values = new ContentValues();
            values.put("trading_times", newCount);
            db.update("user_status", values, "user_id = ?", new String[]{String.valueOf(userId)});

            return newCount;
        } catch (Exception e) {
            Log.e("DBHelper", "更新贸易次数失败", e);
            return 0;
        }
    }

    /**
     * 增加用户的轮回次数（+1）
     * @param userId 用户ID
     */
    public Integer incrementReincarnationTimes(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // 首先检查reincarnation_times列是否存在
            if (!isColumnExists(db, "user_status", "reincarnation_times")) {
                // 如果列不存在，先创建列
                try {
                    db.execSQL("ALTER TABLE user_status ADD COLUMN reincarnation_times INTEGER DEFAULT 0");
                    Log.d("DBHelper", "已添加reincarnation_times列");
                } catch (Exception e) {
                    Log.e("DBHelper", "添加reincarnation_times列失败", e);
                    return 0;
                }
            }

            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"reincarnation_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            int currentCount = 0;
            if (cursor.moveToFirst()) {
                currentCount = cursor.getInt(cursor.getColumnIndexOrThrow("reincarnation_times"));
            }
            cursor.close();

            int newCount = currentCount + 1;
            ContentValues values = new ContentValues();
            values.put("reincarnation_times", newCount);
            db.update("user_status", values, "user_id = ?", new String[]{String.valueOf(userId)});

            Log.d("DBHelper", "轮回次数已更新: " + newCount);
            return newCount;
        } catch (Exception e) {
            Log.e("DBHelper", "更新轮回次数失败", e);
            return 0;
        }
    }

    public int incrementAndGetGlobalCollectTimes(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. 查询当前值
            Cursor cursor = db.query(
                    "user_status",
                    new String[]{"global_collect_times"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );
            int current = 0;
            if (cursor.moveToFirst()) {
                current = cursor.getInt(0);
            }
            cursor.close();
            // 2. 递增并更新
            int newCount = current + 1;
            ContentValues values = new ContentValues();
            values.put("global_collect_times", newCount);
            db.update("user_status", values, "user_id = ?", new String[]{String.valueOf(userId)});
            db.setTransactionSuccessful();
            return newCount;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 为普通账号添加初始物资（石斧、石镐、干面包等）
     */
    private void addNormalInitialItems(SQLiteDatabase db, int userId) {
        // 初始装备：石斧（工具类）
        ContentValues axe = new ContentValues();
        axe.put("user_id", userId);
        axe.put("item_type", ItemConstants.EQUIP_STONE_AXE);
        axe.put("item_count", 1);
        axe.put("durability", getToolInitialDurability(ItemConstants.EQUIP_STONE_AXE)); // 假设初始耐久10
        axe.put("is_equipped", 0); // 默认未装备
        db.insert("backpack", null, axe);

        // 初始装备：石镐（工具类）
        ContentValues pickaxe = new ContentValues();
        pickaxe.put("user_id", userId);
        pickaxe.put("item_type", ItemConstants.EQUIP_STONE_PICKAXE);
        pickaxe.put("item_count", 1);
        pickaxe.put("durability", getToolInitialDurability(ItemConstants.EQUIP_STONE_PICKAXE)); // 假设初始耐久10
        pickaxe.put("is_equipped", 0);
        db.insert("backpack", null, pickaxe);

        // 基础生存物资：干面包
        ContentValues bread = new ContentValues();
        bread.put("user_id", userId);
        bread.put("item_type", ItemConstants.ITEM_DRIED_BREAD);
        bread.put("item_count", 2);
        bread.put("durability", 0);
        bread.put("is_equipped", 0);
        db.insert("backpack", null, bread);

        // 其他必要初始资源（如木材、石头等，根据需求添加）
        ContentValues wood = new ContentValues();
        wood.put("user_id", userId);
        wood.put("item_type", ItemConstants.ITEM_WOOD);
        wood.put("item_count", 10);
        wood.put("durability", 0);
        wood.put("is_equipped", 0);
        db.insert("backpack", null, wood);
    }

    /**
     * 为测试账号添加初始物资（复用TestAccountInitializer的配置）
     */
    private void addTestInitialItems(SQLiteDatabase db, int userId) {
        // 获取测试账号初始背包资源
        Map<String, Integer> initialBackpack = TestAccountInitializer.getInitialBackpack();
        for (Map.Entry<String, Integer> entry : initialBackpack.entrySet()) {
            String itemType = entry.getKey();
            int count = entry.getValue();

            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("item_type", itemType);
            values.put("item_count", count);
            // 工具类物品设置初始耐久，非工具类为0
            values.put("durability", isToolItem(itemType) ? getToolInitialDurability(itemType) : 0);
            values.put("is_equipped", 0); // 默认未装备
            db.insert("backpack", null, values);
        }

        // 测试账号额外添加全类型工具（可选，根据测试需求）
        String[] testTools = {
                ItemConstants.EQUIP_STONE_AXE, ItemConstants.EQUIP_IRON_AXE, ItemConstants.EQUIP_DIAMOND_AXE,
                ItemConstants.EQUIP_STONE_PICKAXE, ItemConstants.EQUIP_IRON_PICKAXE, ItemConstants.EQUIP_DIAMOND_PICKAXE
        };
        for (String tool : testTools) {
            ContentValues toolValues = new ContentValues();
            toolValues.put("user_id", userId);
            toolValues.put("item_type", tool);
            toolValues.put("item_count", 1);
            toolValues.put("durability", getToolInitialDurability(tool));
            toolValues.put("is_equipped", 0);
            db.insert("backpack", null, toolValues);
        }
    }

    /**
     * 重置用户数据（清空背包/装备，重新初始化初始物资）
     * @param userId 用户ID
     */
//    public void resetUserData(int userId) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.beginTransaction(); // 开启事务，确保数据一致性
//        try {
//            // 1. 清空现有背包数据
//            db.delete("backpack", "user_id = ?", new String[]{String.valueOf(userId)});
//            // 2. 清空现有装备数据（如果还存在独立装备表）
//            db.delete("equipment", "user_id = ?", new String[]{String.valueOf(userId)});
//            // 3. 重置用户状态（如位置、生存指标等，复用初始化逻辑）
//            Map<String, Object> initialStatus = new HashMap<>();
//            initialStatus.put("life", Constant.INIT_LIFE);
//            initialStatus.put("hunger", Constant.INIT_HUNGER);
//            initialStatus.put("thirst", Constant.INIT_THIRST);
//            initialStatus.put("stamina", Constant.INIT_STAMINA);
//            int[] spawnPoint = TestAccountInitializer.chooseRandomSpawnPoint(); // 复用随机出生点
//            initialStatus.put("current_x", spawnPoint[0]);
//            initialStatus.put("current_y", spawnPoint[1]);
//            updateUserStatus(userId, initialStatus);
//
//            // 4. 根据账号类型重新添加初始物资
//            // 判断是否为测试账号（假设user表有is_test字段）
//            Cursor userCursor = db.query("user", new String[]{"is_test"}, "id = ?", new String[]{String.valueOf(userId)}, null, null, null);
//            boolean isTestAccount = false;
//            if (userCursor.moveToFirst()) {
//                isTestAccount = userCursor.getInt(0) == 1;
//            }
//            userCursor.close();
//
//            if (isTestAccount) {
//                addTestInitialItems(db, userId); // 测试账号初始物资
//            } else {
//                addNormalInitialItems(db, userId); // 普通账号初始物资
//            }
//
//            db.setTransactionSuccessful(); // 事务成功
//        } finally {
//            db.endTransaction(); // 结束事务
//        }
//    }
    /**
     * 处理用户轮回逻辑（保留关键数据，重置背包和基础状态）
     * @param userId 用户ID
     */
//    public void reincarnateUser(int userId) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.beginTransaction();
//        try {
//            // 1. 保留需要继承的数据（如希望点数、科技等级等）
//            Map<String, Object> userStatus = getUserStatus(userId);
//            int hopePoints = getIntValue(userStatus.get("hope_points"), 0); // 保留希望点数
//            String difficulty = (String) userStatus.get("difficulty"); // 保留难度
//
//            // 2. 清空背包和装备（同重置逻辑）
//            db.delete("backpack", "user_id = ?", new String[]{String.valueOf(userId)});
//            db.delete("equipment", "user_id = ?", new String[]{String.valueOf(userId)});
//
//            // 3. 重置基础状态（位置、生存指标等）
//            int[] spawnPoint = TestAccountInitializer.chooseRandomSpawnPoint();
//            Map<String, Object> newStatus = new HashMap<>();
//            newStatus.put("life", Constant.INIT_LIFE);
//            newStatus.put("hunger", Constant.INIT_HUNGER);
//            newStatus.put("thirst", Constant.INIT_THIRST);
//            newStatus.put("stamina", Constant.INIT_STAMINA);
//            newStatus.put("current_x", spawnPoint[0]);
//            newStatus.put("current_y", spawnPoint[1]);
//            newStatus.put("hope_points", hopePoints); // 恢复希望点数
//            newStatus.put("difficulty", difficulty); // 恢复难度
//            newStatus.put("game_day", 0); // 重置天数
//            updateUserStatus(userId, newStatus);
//
//            // 4. 重新添加初始物资（同重置逻辑）
//            Cursor userCursor = db.query("user", new String[]{"is_test"}, "id = ?", new String[]{String.valueOf(userId)}, null, null, null);
//            boolean isTestAccount = false;
//            if (userCursor.moveToFirst()) {
//                isTestAccount = userCursor.getInt(0) == 1;
//            }
//            userCursor.close();
//
//            if (isTestAccount) {
//                addTestInitialItems(db, userId);
//            } else {
//                addNormalInitialItems(db, userId);
//            }
//
//            db.setTransactionSuccessful();
//        } finally {
//            db.endTransaction();
//        }
//    }

    public List<Map<String, Object>> getUserCustomTerrains(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Map<String, Object>> result = new ArrayList<>();

        Cursor cursor = db.query("map", new String[]{"area_x", "area_y", "terrain_type"},
                "user_id=?", new String[]{String.valueOf(userId)},
                null, null, null);

        while (cursor.moveToNext()) {
            Map<String, Object> terrain = new HashMap<>();
            terrain.put("x", cursor.getInt(cursor.getColumnIndexOrThrow("x")));
            terrain.put("y", cursor.getInt(cursor.getColumnIndexOrThrow("y")));
            terrain.put("terrain_type", cursor.getString(cursor.getColumnIndexOrThrow("terrain_type")));
            result.add(terrain);
        }
        cursor.close();
        return result;
    }

    /**
     * 检查指定位置是否有建筑
     */
    public boolean hasBuildingAt(int userId, int areaX, int areaY) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            // 使用area_x和area_y匹配数据库字段
            cursor = db.query("building", null,
                    "user_id=? AND area_x=? AND area_y=?",
                    new String[]{String.valueOf(userId), String.valueOf(areaX), String.valueOf(areaY)},
                    null, null, null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
            //db.close(); // 保持与getAreaCollectData一致的关闭逻辑
        }
    }

    /**
     * 检查指定区域坐标是否有特定类型的建筑
     */
    public boolean hasBuildingOfTypeAt(int userId, int areaX, int areaY, String buildingType) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("building", null,
                    "user_id=? AND area_x=? AND area_y=? AND building_type=?",
                    new String[]{String.valueOf(userId), String.valueOf(areaX), String.valueOf(areaY), buildingType},
                    null, null, null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
            //db.close();
        }
    }

    /**
     * 检查指定区域坐标是否有任何建筑（新增方法）
     */
    public boolean hasAnyBuildingAt(int userId, int areaX, int areaY) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("building", null,
                    "user_id=? AND area_x=? AND area_y=?",
                    new String[]{String.valueOf(userId), String.valueOf(areaX), String.valueOf(areaY)},
                    null, null, null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
            //db.close();
        }
    }

    /**
     * 获取指定区域坐标的建筑类型（新增方法）
     */
    public String getBuildingTypeAt(int userId, int areaX, int areaY) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("building", new String[]{"building_type"},
                    "user_id=? AND area_x=? AND area_y=?",
                    new String[]{String.valueOf(userId), String.valueOf(areaX), String.valueOf(areaY)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return "未知建筑";
        } finally {
            if (cursor != null) cursor.close();
            //db.close();
        }
    }

    /**
     * 更新指定区域坐标的建筑类型
     */
    public void updateBuildingType(int userId, int areaX, int areaY, String newType) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("building_type", newType);
        db.update("building", values,
                "user_id=? AND area_x=? AND area_y=?",
                new String[]{String.valueOf(userId), String.valueOf(areaX), String.valueOf(areaY)});
        //db.close();
    }

    public void saveUserTerrain(int userId, int areaX, int areaY, String terrainType) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("user_id", userId); // 确保存入正确的用户ID
            values.put("area_x", areaX);
            values.put("area_y", areaY);
            values.put("terrain_type", terrainType);
            // 用replace确保覆盖旧数据（若同一坐标多次建造）
            long rowId = db.replace("user_terrain", null, values);
            Log.d("DBHelperSaveTerrain", "保存地形：userId=" + userId + "，(" + areaX + "," + areaY + ")=" + terrainType + "，rowId=" + rowId);
        } catch (Exception e) {
            Log.e("DBHelperSaveTerrain", "保存失败：" + e.getMessage());
        } finally {
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // db.close();
        }
    }

    /**
     * 新增带区域坐标的建筑记录（适配4参数调用）
     */
    public void addBuilding(int userId, String buildingType, int areaX, int areaY) {
        Log.i("DBHelper", "=== 开始添加建筑到数据库 ===");
        Log.i("DBHelper", "建筑信息: 用户ID=" + userId + ", 建筑类型=" + buildingType + ", 坐标=(" + areaX + ", " + areaY + ")");

        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            // 强制检查参数是否有效（非负数，避免无效值）
            if (areaX < 0 || areaY < 0) {
                Log.e("DBHelper", "无效坐标：areaX=" + areaX + ", areaY=" + areaY);
                Log.e("DBHelper", "建筑添加失败：坐标无效");
                return;
            }
            Log.i("DBHelper", "坐标验证通过");

            // 严格对应表字段名，确保没有拼写错误（如area_x而非areax）
            values.put("user_id", userId);
            values.put("building_type", buildingType);
            values.put("area_x", areaX); // 必须赋值，且字段名是area_x
            values.put("area_y", areaY); // 必须赋值，且字段名是area_y
            values.put("count", 1); // 若表中有count字段，赋默认值

            Log.i("DBHelper", "准备插入建筑数据到building表");
            // 执行插入并捕获错误
            long rowId = db.insertOrThrow("building", null, values);
            if (rowId == -1) {
                Log.e("DBHelper", "插入建筑失败：未知错误");
                Log.e("DBHelper", "建筑数据库插入失败");
            } else {
                Log.d("DBHelper", "建筑插入成功：rowId=" + rowId + "，坐标(" + areaX + "," + areaY + ")");
                Log.i("DBHelper", "建筑数据库插入成功，rowId=" + rowId);
            }
        } catch (SQLiteConstraintException e) {
            // 明确打印约束失败的字段，便于排查
            Log.e("DBHelper", "插入建筑约束失败：" + e.getMessage() + "，参数：userId=" + userId + ", type=" + buildingType + ", areaX=" + areaX + ", areaY=" + areaY);
            Log.e("DBHelper", "建筑添加失败：数据库约束冲突");
        } catch (Exception e) {
            Log.e("DBHelper", "插入建筑异常：" + e.getMessage());
            Log.e("DBHelper", "建筑添加失败：异常发生");
        } finally {
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // db.close();
            Log.i("DBHelper", "数据库连接由单例模式统一管理");
        }
        Log.i("DBHelper", "=== 建筑添加操作完成 ===");
    }

    // 核心：查询用户自定义地形数据
    public List<GameMap.TerrainData> getUserTerrains(int userId) {
        List<GameMap.TerrainData> terrains = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "user_terrain",
                    new String[]{"area_x", "area_y", "terrain_type"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );
            while (cursor.moveToNext()) {
                int x = cursor.getInt(cursor.getColumnIndexOrThrow("area_x"));
                int y = cursor.getInt(cursor.getColumnIndexOrThrow("area_y"));
                String terrainType = cursor.getString(cursor.getColumnIndexOrThrow("terrain_type"));
                terrains.add(new GameMap.TerrainData(x, y, terrainType));
            }
        } catch (Exception e) {
            Log.e("DBHelper", "查询用户地形失败：" + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            // 重要修复：不要关闭数据库连接，由单例模式统一管理
            // db.close();
        }
        return terrains;
    }
    // 保存用户坐标（示例）
    public void saveUserCoord(int userId, int x, int y) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_x", x);
        values.put("current_y", y);
        db.update("user_status", values, "user_id=?", new String[]{String.valueOf(userId)});
        // 重要修复：不要关闭数据库连接，由单例模式统一管理
        // db.close();
    }

    // 在DBHelper中添加获取最后登录用户ID的方法
    public Integer getLastLoginUserId() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int userId = -1;
        try {
            // 查询is_login=1的用户（最多1个，确保登录状态唯一）
            cursor = db.query(
                    "user",
                    new String[]{"id"},
                    "is_login = ?",
                    new String[]{"1"},
                    null, null, null, "1"
            );
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            Log.e("DBHelper", "查询最后登录用户失败：" + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return userId;
    }

    public void loginUser(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 先将所有用户的is_login设为0
            ContentValues cv = new ContentValues();
            cv.put("is_login", 0);
            db.update("user", cv, null, null);

            // 再将当前登录用户设为1
            cv.clear();
            cv.put("is_login", 1);
            db.update("user", cv, "id = ?", new String[]{String.valueOf(userId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    // 1. 删除指定位置的指定类型建筑
    public boolean removeBuildingAt(int userId, int x, int y, String buildingType) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(
                "building", // 修正：建筑表名为building
                "user_id = ? AND area_x = ? AND area_y = ? AND building_type = ?",
                new String[]{String.valueOf(userId), String.valueOf(x), String.valueOf(y), buildingType}
        );
        return rowsDeleted > 0;
    }


    public int getUserTemperature(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        // 修正：将int userId转为String
        Cursor cursor = db.query("user_status", new String[]{"temperature"}, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, null); // 这里修改
        int temp = Constant.TEMPERATURE_DEFAULT;
        if (cursor.moveToFirst()) {
            temp = cursor.getInt(0);
        }
        cursor.close();
        return temp;
    }

    public void updateUserTemperature(int userId, int newTemperature) {
        // 确保体温在合法范围
        int clampedTemp = Math.max(Constant.TEMPERATURE_MIN,
                Math.min(Constant.TEMPERATURE_MAX, newTemperature));
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("temperature", clampedTemp);
        // 修正：将int userId转为String
        db.update("user_status", values, "user_id=?", new String[]{String.valueOf(userId)}); // 这里修改
    }

    /**
     * 获取当前装备的耐久度
     */
    public int getEquippedToolDurability(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "equipment",
                new String[]{"durability"},
                "user_id = ? AND is_equipped = 1",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
        int durability = 0;
        if (cursor.moveToFirst()) {
            durability = cursor.getInt(cursor.getColumnIndexOrThrow("durability"));
        }
        cursor.close();
        return durability;
    }

    /**
     * 更新当前装备的耐久度（消耗1点）
     * @return 是否更新成功（耐久是否足够）
     */
    public boolean consumeEquippedToolDurability(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        // 先查询当前耐久
        int currentDurability = getEquippedToolDurability(userId);
        if (currentDurability <= 0) {
            return false; // 耐久不足，无法消耗
        }

        // 耐久减1
        ContentValues values = new ContentValues();
        values.put("durability", currentDurability - 1);
        int rowsAffected = db.update(
                "equipment",
                values,
                "user_id = ? AND is_equipped = 1",
                new String[]{String.valueOf(userId)}
        );

        // 如果耐久变为0，自动解除装备
        if (currentDurability - 1 == 0) {
            ContentValues unequipValues = new ContentValues();
            unequipValues.put("is_equipped", 0);
            db.update(
                    "equipment",
                    unequipValues,
                    "user_id = ? AND is_equipped = 1",
                    new String[]{String.valueOf(userId)}
            );
        }
        return rowsAffected > 0;
    }

    /**
     * 删除指定用户的所有建筑
     */
    public void deleteAllBuildings(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("building", "user_id = ?", new String[]{String.valueOf(userId)});
        Log.i("DBHelper", "删除用户 " + userId + " 的所有建筑");
    }

    /**
     * 删除指定用户的所有物品
     */
    public void deleteAllItems(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("backpack", "user_id = ?", new String[]{String.valueOf(userId)});
        Log.i("DBHelper", "删除用户 " + userId + " 的所有物品");
    }

    /**
     * 删除指定用户的所有装备
     */
    public void deleteAllEquipments(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("equipment", "user_id = ?", new String[]{String.valueOf(userId)});
        Log.i("DBHelper", "删除用户 " + userId + " 的所有装备");
    }

    /**
     * 获取当前用户的地图
     */
    public String getCurrentMap(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String currentMap = "main_world"; // 默认地图
        try {
            cursor = db.query("user_status", new String[]{"current_map"},
                    "user_id=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                currentMap = cursor.getString(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return currentMap;
    }

    /**
     * 传送到幻想大陆
     */
    public boolean teleportToFantasyContinent(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_map", "fantasy_continent");
        // 设置幻想大陆的初始坐标
        values.put("current_x", 50);
        values.put("current_y", 50);
        int rowsAffected = db.update("user_status", values, "user_id=?",
                new String[]{String.valueOf(userId)});
        return rowsAffected > 0;
    }

    /**
     * 传送到主世界
     */
    public boolean teleportToMainWorld(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_map", "main_world");
        // 设置主世界的初始坐标
        values.put("current_x", 10);
        values.put("current_y", 10);
        int rowsAffected = db.update("user_status", values, "user_id=?",
                new String[]{String.valueOf(userId)});
        return rowsAffected > 0;
    }

    /**
     * 检查当前位置是否有传送门
     */
    public boolean checkPortalAtCurrentPosition(int userId, int x, int y) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            // 查询当前位置的地形类型
            cursor = db.query("map", new String[]{"terrain_type"},
                    "user_id=? AND area_x=? AND area_y=?",
                    new String[]{String.valueOf(userId), String.valueOf(x), String.valueOf(y)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                String terrainType = cursor.getString(0);
                // 检查是否是传送门地形
                return terrainType != null &&
                        (terrainType.contains("portal") ||
                                terrainType.contains("传送门") ||
                                terrainType.contains("fantasy_portal") ||
                                terrainType.contains("main_portal"));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    /**
     * 检查当前用户所在位置是否有传送门（重载版本，自动获取坐标）
     */
    public boolean checkPortalAtCurrentPosition(int userId) {
        int currentX = getCurrentX();
        int currentY = getCurrentY();
        return checkPortalAtCurrentPosition(userId, currentX, currentY);
    }

    /**
     * 获取仓库物品
     */
    public Map<String, Integer> getWarehouse(int userId) {
        Map<String, Integer> warehouse = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("warehouse", null, "user_id=?",
                    new String[]{String.valueOf(userId)}, null, null, null);
            while (cursor.moveToNext()) {
                String itemType = cursor.getString(cursor.getColumnIndexOrThrow("item_type"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("item_count"));
                warehouse.put(itemType, count);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return warehouse;
    }

    /**
     * 获取仓库当前物品数量（按组计算）
     */
    public int getWarehouseCurrentCount(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int totalGroupCount = 0;
        try {
            cursor = db.query("warehouse", new String[]{"item_type", "item_count"},
                    "user_id=?",
                    new String[]{String.valueOf(userId)}, null, null, null);
            while (cursor.moveToNext()) {
                String itemType = cursor.getString(cursor.getColumnIndexOrThrow("item_type"));
                int itemCount = cursor.getInt(cursor.getColumnIndexOrThrow("item_count"));
                totalGroupCount += getItemGroupCount(itemType, itemCount);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return totalGroupCount;
    }

    /**
     * 获取仓库容量
     */
    public int getWarehouseCapacity(int userId) {
        // 仓库容量固定为100组物品
        return 100;
    }

    /**
     * 判断物品是否为工具类物品
     */
    public boolean isToolItem(String itemName) {
        return itemName.equals(ItemConstants.EQUIP_STONE_AXE) ||
                itemName.equals(ItemConstants.EQUIP_IRON_AXE) ||
                itemName.equals(ItemConstants.EQUIP_DIAMOND_AXE) ||
                itemName.equals(ItemConstants.EQUIP_STONE_PICKAXE) ||
                itemName.equals(ItemConstants.EQUIP_IRON_PICKAXE) ||
                itemName.equals(ItemConstants.EQUIP_DIAMOND_PICKAXE) ||
                itemName.equals(ItemConstants.EQUIP_STONE_SICKLE) ||
                itemName.equals(ItemConstants.EQUIP_IRON_SICKLE) ||
                itemName.equals(ItemConstants.EQUIP_DIAMOND_SICKLE) ||
                itemName.equals(ItemConstants.EQUIP_STONE_FISHING_ROD) ||
                itemName.equals(ItemConstants.EQUIP_IRON_FISHING_ROD) ||
                itemName.equals(ItemConstants.EQUIP_DIAMOND_FISHING_ROD);
    }

    /**
     * 获取物品的分组数量（资源99个为1组，工具1个为1组）
     */
    public int getItemGroupCount(String itemName, int count) {
        if (isToolItem(itemName)) {
            // 工具类：1个为1组
            return count;
        } else {
            // 资源类：99个为1组
            return (count + 98) / 99; // 向上取整
        }
    }

    /**
     * 从背包移动到仓库
     */
    public boolean moveItemToWarehouse(int userId, String itemName, int count) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 检查仓库容量（使用分组逻辑）
            int currentGroupCount = getWarehouseCurrentCount(userId);
            int capacity = getWarehouseCapacity(userId);
            int moveGroupCount = getItemGroupCount(itemName, count);
            if (currentGroupCount + moveGroupCount > capacity) {
                return false; // 仓库容量不足
            }

            // 检查背包中是否有足够的物品
            Cursor cursor = db.query("backpack", new String[]{"item_count", "durability"},
                    "user_id=? AND item_type=?",
                    new String[]{String.valueOf(userId), itemName}, null, null, null);

            if (cursor.moveToFirst()) {
                int backpackCount = cursor.getInt(0);
                int durability = cursor.getInt(1);

                if (backpackCount < count) {
                    return false; // 背包中物品不足
                }

                // 更新背包数量
                int newBackpackCount = backpackCount - count;
                if (newBackpackCount > 0) {
                    ContentValues backpackValues = new ContentValues();
                    backpackValues.put("item_count", newBackpackCount);
                    db.update("backpack", backpackValues,
                            "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), itemName});
                } else {
                    db.delete("backpack", "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), itemName});
                }

                // 更新仓库数量
                Cursor warehouseCursor = db.query("warehouse", new String[]{"item_count"},
                        "user_id=? AND item_type=?",
                        new String[]{String.valueOf(userId), itemName}, null, null, null);

                if (warehouseCursor.moveToFirst()) {
                    int warehouseCount = warehouseCursor.getInt(0);
                    ContentValues warehouseValues = new ContentValues();
                    warehouseValues.put("item_count", warehouseCount + count);
                    warehouseValues.put("durability", durability);
                    db.update("warehouse", warehouseValues,
                            "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), itemName});
                } else {
                    ContentValues warehouseValues = new ContentValues();
                    warehouseValues.put("user_id", userId);
                    warehouseValues.put("item_type", itemName);
                    warehouseValues.put("item_count", count);
                    warehouseValues.put("durability", durability);
                    db.insert("warehouse", null, warehouseValues);
                }
                warehouseCursor.close();
            }

            cursor.close();
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 从仓库移动到背包
     */
    public boolean moveItemToBackpack(int userId, String itemName, int count) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 检查背包容量
            int currentTotal = getBackpackCurrentCount(userId);
            int cap = getBackpackCapacity(userId);
            if (currentTotal + count > cap) {
                return false; // 背包容量不足
            }

            // 检查仓库中是否有足够的物品
            Cursor cursor = db.query("warehouse", new String[]{"item_count", "durability"},
                    "user_id=? AND item_type=?",
                    new String[]{String.valueOf(userId), itemName}, null, null, null);

            if (cursor.moveToFirst()) {
                int warehouseCount = cursor.getInt(0);
                int durability = cursor.getInt(1);

                if (warehouseCount < count) {
                    return false; // 仓库中物品不足
                }

                // 更新仓库数量
                int newWarehouseCount = warehouseCount - count;
                if (newWarehouseCount > 0) {
                    ContentValues warehouseValues = new ContentValues();
                    warehouseValues.put("item_count", newWarehouseCount);
                    db.update("warehouse", warehouseValues,
                            "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), itemName});
                } else {
                    db.delete("warehouse", "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), itemName});
                }

                // 更新背包数量
                Cursor backpackCursor = db.query("backpack", new String[]{"item_count"},
                        "user_id=? AND item_type=?",
                        new String[]{String.valueOf(userId), itemName}, null, null, null);

                if (backpackCursor.moveToFirst()) {
                    int backpackCount = backpackCursor.getInt(0);
                    ContentValues backpackValues = new ContentValues();
                    backpackValues.put("item_count", backpackCount + count);
                    backpackValues.put("durability", durability);
                    db.update("backpack", backpackValues,
                            "user_id=? AND item_type=?",
                            new String[]{String.valueOf(userId), itemName});
                } else {
                    ContentValues backpackValues = new ContentValues();
                    backpackValues.put("user_id", userId);
                    backpackValues.put("item_type", itemName);
                    backpackValues.put("item_count", count);
                    backpackValues.put("durability", durability);
                    db.insert("backpack", null, backpackValues);
                }
                backpackCursor.close();
            }

            cursor.close();
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 更新仓库物品数量（直接添加物品到仓库）
     * @param userId 用户ID
     * @param itemName 物品名称
     * @param count 物品数量
     * @return 是否成功
     */
    public boolean updateWarehouseItem(int userId, String itemName, int count) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 检查仓库容量（使用分组逻辑）
            int currentGroupCount = getWarehouseCurrentCount(userId);
            int capacity = getWarehouseCapacity(userId);
            int addGroupCount = getItemGroupCount(itemName, count);
            if (currentGroupCount + addGroupCount > capacity) {
                return false; // 仓库容量不足
            }

            // 检查仓库中是否已有该物品
            Cursor cursor = db.query("warehouse", new String[]{"item_count", "durability"},
                    "user_id=? AND item_type=?",
                    new String[]{String.valueOf(userId), itemName}, null, null, null);

            if (cursor.moveToFirst()) {
                // 仓库中已有该物品，更新数量
                int currentCount = cursor.getInt(0);
                int durability = cursor.getInt(1);
                int newCount = currentCount + count;
                
                ContentValues values = new ContentValues();
                values.put("item_count", newCount);
                values.put("durability", durability);
                
                db.update("warehouse", values,
                        "user_id=? AND item_type=?",
                        new String[]{String.valueOf(userId), itemName});
            } else {
                // 仓库中没有该物品，插入新记录
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("item_type", itemName);
                values.put("item_count", count);
                values.put("durability", 100); // 默认耐久度
                
                db.insert("warehouse", null, values);
            }
            cursor.close();
            
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // 成就相关方法

    /**
     * 初始化用户成就数据
     */
    public void initUserAchievements(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // 定义所有成就类型和等级
            String[] achievementTypes = {
                    "resource_collect", "exploration", "synthesis", "building",
                    "cooking", "smelting", "trading", "reincarnation"
            };

            for (String type : achievementTypes) {
                // 根据成就类型设置不同的最大等级
                int maxLevel = 10;
                if ("building".equals(type)) {
                    maxLevel = 8; // 解锁建筑成就只有8个等级
                } else if ("cooking".equals(type)) {
                    maxLevel = 5; // 烹饪成就只有5个等级
                } else if ("resource_collect".equals(type)) {
                    maxLevel = 5; // 资源收集成就只有5个等级
                }

                for (int level = 1; level <= maxLevel; level++) {
                    ContentValues values = new ContentValues();
                    values.put("user_id", userId);
                    values.put("achievement_type", type);
                    values.put("level", level);
                    values.put("progress", 0);
                    values.put("is_completed", 0);
                    values.put("is_claimed", 0);

                    // 插入成就记录（如果不存在）
                    db.insertWithOnConflict("achievements", null, values,
                            SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取用户所有成就
     */
    public List<AchievementItem> getUserAchievements(int userId) {
        List<AchievementItem> achievements = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // 先检查achievements表是否存在
            if (!isTableExists(db, "achievements")) {
                // 如果表不存在，先创建表并初始化数据
                createAchievementsTable(db);
                initUserAchievements(userId);
            }

            cursor = db.query("achievements", null,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, "achievement_type, level");

            while (cursor.moveToNext()) {
                AchievementItem item = new AchievementItem(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))),
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("achievement_type")),
                        cursor.getString(cursor.getColumnIndexOrThrow("achievement_type")), // category = achievement_type
                        "", // name
                        cursor.getInt(cursor.getColumnIndexOrThrow("level")),
                        0, // target
                        0  // reward
                );
                item.setCurrent(cursor.getInt(cursor.getColumnIndexOrThrow("progress")));
                item.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
                item.setClaimed(cursor.getInt(cursor.getColumnIndexOrThrow("is_claimed")) == 1);

                achievements.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果查询失败，返回空列表
        } finally {
            if (cursor != null) cursor.close();
        }

        return achievements;
    }

    /**
     * 更新成就进度
     */
    public void updateAchievementProgress(int userId, String achievementType, int progress) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            // 获取该类型的所有成就
            Cursor cursor = db.query("achievements",
                    new String[]{"id", "level", "progress", "is_completed", "is_claimed"},
                    "user_id = ? AND achievement_type = ?",
                    new String[]{String.valueOf(userId), achievementType},
                    null, null, "level");

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
                int currentProgress = cursor.getInt(cursor.getColumnIndexOrThrow("progress"));
                boolean isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1;
                boolean isClaimed = cursor.getInt(cursor.getColumnIndexOrThrow("is_claimed")) == 1;

                // 如果已经完成并领取，跳过
                if (isCompleted && isClaimed) continue;

                // 获取该等级的目标值
                int target = getAchievementTarget(achievementType, level);

                // 更新进度（不超过目标值）
                int newProgress = Math.min(progress, target);

                ContentValues values = new ContentValues();
                values.put("progress", newProgress);

                // 自动检测成就是否完成（如果进度达到目标值且未领取）
                if (newProgress >= target && !isClaimed) {
                    values.put("is_completed", 1);
                    Log.d("DBHelper", "成就自动完成: 类型=" + achievementType + ", 等级=" + level);
                }

                db.update("achievements", values, "id = ?", new String[]{String.valueOf(id)});
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 领取成就奖励
     */
    public boolean claimAchievementReward(int userId, String achievementType, int level) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            // 检查成就是否已完成且未领取
            Cursor cursor = db.query("achievements",
                    new String[]{"id", "progress", "is_claimed"},
                    "user_id = ? AND achievement_type = ? AND level = ?",
                    new String[]{String.valueOf(userId), achievementType, String.valueOf(level)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                int currentProgress = cursor.getInt(cursor.getColumnIndexOrThrow("progress"));
                int target = getAchievementTarget(achievementType, level);
                boolean isClaimed = cursor.getInt(cursor.getColumnIndexOrThrow("is_claimed")) == 1;
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

                Log.d("DBHelper", "检查成就领取条件: 类型=" + achievementType + ", 等级=" + level +
                        ", 进度=" + currentProgress + "/" + target + ", 已领取=" + isClaimed);

                // 检查进度是否达到目标值且未领取
                if (currentProgress >= target && !isClaimed) {
                    // 标记为已完成和已领取
                    ContentValues values = new ContentValues();
                    values.put("is_completed", 1);
                    values.put("is_claimed", 1);
                    int rowsUpdated = db.update("achievements", values, "id = ?", new String[]{String.valueOf(id)});

                    if (rowsUpdated > 0) {
                        // 添加希望点数奖励
                        int rewardPoints = getAchievementReward(level);
                        addHopePoints(userId, rewardPoints);

                        Log.d("DBHelper", "成就奖励领取成功: 类型=" + achievementType + ", 等级=" + level +
                                ", 奖励点数=" + rewardPoints);

                        cursor.close();
                        return true;
                    }
                } else {
                    Log.d("DBHelper", "成就领取条件不满足: " +
                            (currentProgress >= target ? "进度满足" : "进度不足") + ", " +
                            (!isClaimed ? "未领取" : "已领取"));
                }
            } else {
                Log.d("DBHelper", "未找到成就记录: 类型=" + achievementType + ", 等级=" + level);
            }

            cursor.close();
        } catch (Exception e) {
            Log.e("DBHelper", "领取成就奖励失败", e);
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 获取成就目标值
     */
    private int getAchievementTarget(String achievementType, int level) {
        switch (achievementType) {
            case "resource_collect":
                return 100 * level; // Lv1:100, Lv2:200, ..., Lv5:500
            case "exploration":
            case "synthesis":
            case "smelting":
            case "trading":
            case "reincarnation":
                switch (level) {
                    case 1: return 100;
                    case 2: return 500;
                    case 3: return 1000;
                    case 4: return 2000;
                    case 5: return 5000;
                    case 6: return 10000;
                    case 7: return 20000;
                    case 8: return 50000;
                    case 9: return 100000;
                    case 10: return 200000;
                    default: return 0;
                }
            case "building":
                // 建筑解锁成就
                return level; // Lv1:1, Lv2:2, ..., Lv8:8
            case "cooking":
                return level * 5; // Lv1:5, Lv2:10, ..., Lv5:25
            default:
                return 0;
        }
    }

    /**
     * 获取成就奖励点数
     */
    private int getAchievementReward(int level) {
        return level * 10; // Lv1:10, Lv2:20, ..., Lv10:100
    }

    /**
     * 添加希望点数
     */
    private void addHopePoints(int userId, int points) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            // 获取当前希望点数
            Cursor cursor = db.query("user_status",
                    new String[]{"hope_points"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor.moveToFirst()) {
                int currentPoints = cursor.getInt(cursor.getColumnIndexOrThrow("hope_points"));
                int newPoints = currentPoints + points;

                ContentValues values = new ContentValues();
                values.put("hope_points", newPoints);
                db.update("user_status", values, "user_id = ?",
                        new String[]{String.valueOf(userId)});
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查表是否存在
     */
    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'", null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * 获取背包物品数量
     * @param userId 用户ID
     * @param itemName 物品名称
     * @return 物品数量
     */
    public int getBackpackItemCount(int userId, String itemName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("backpack", new String[]{"item_count"},
                    "user_id=? AND item_type=?",
                    new String[]{String.valueOf(userId), itemName},
                    null, null, null);

            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("item_count"));
            }
            return 0;
        } catch (Exception e) {
            Log.e("DBHelper", "获取背包物品数量失败：" + e.getMessage());
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * 添加背包物品
     * @param userId 用户ID
     * @param itemName 物品名称
     * @param count 物品数量
     */
    public void addBackpackItem(int userId, String itemName, int count) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("item_type", itemName);
        values.put("item_count", count);
        values.put("durability", 0); // 非工具类物品，耐久为0
        values.put("is_equipped", 0); // 默认未装备

        db.insert("backpack", null, values);
        Log.d("DBHelper", "添加背包物品：" + itemName + "，数量：" + count);

        // 检查是否是料理物品，如果是则更新烹饪成就进度
        checkAndUpdateCookingAchievement(userId, itemName);
    }

    /**
     * 检查并更新烹饪成就进度（当获得新料理时调用）
     * @param userId 用户ID
     * @param itemName 物品名称
     */
    private void checkAndUpdateCookingAchievement(int userId, String itemName) {
        // 定义所有料理物品的列表
        String[] foodItems = {
                ItemConstants.ITEM_GRILLED_FISH,
                ItemConstants.ITEM_GRILLED_CRAWFISH,
                ItemConstants.ITEM_FISH_SOUP,
                ItemConstants.ITEM_MUSHROOM_SOUP,
                ItemConstants.ITEM_KELP_SOUP,
                ItemConstants.ITEM_FRUIT_PIE,
                ItemConstants.ITEM_ADVANCED_HERB,
                ItemConstants.ITEM_ICY_SNOW_LOTUS_COCONUT,
                ItemConstants.ITEM_HONEY_APPLE_SLICE,
                ItemConstants.ITEM_RICE_PORRIDGE,
                ItemConstants.ITEM_KELP_WINTER_MELON_SOUP,
                ItemConstants.ITEM_ROASTED_POTATO,
                ItemConstants.ITEM_FRUIT_SMOOTHIE,
                ItemConstants.ITEM_CRAWFISH_SHELL_SOUP,
                ItemConstants.ITEM_STEAMED_CORN,
                ItemConstants.ITEM_MUSHROOM_TRUFFLE_FRY,
                ItemConstants.ITEM_BERRY_HONEY_BREAD,
                ItemConstants.ITEM_BEET_HONEY_DRINK,
                ItemConstants.ITEM_BOILED_SPINACH,
                ItemConstants.ITEM_ROASTED_ACORN,
                ItemConstants.ITEM_CACTUS_FRUIT_ICE_DRINK,
                ItemConstants.ITEM_CARROT_POTATO_SOUP,
                ItemConstants.ITEM_COCONUT_BERRY_DRINK,
                ItemConstants.ITEM_TRUFFLE_MUSHROOM_SOUP,
                ItemConstants.ITEM_APPLE_HONEY_DRINK,
                ItemConstants.ITEM_KELP_FISH_SOUP,
                ItemConstants.ITEM_WINTER_MELON_CRAWFISH_SOUP,
                ItemConstants.ITEM_DARK_FOOD
        };

        // 检查添加的物品是否是料理
        boolean isFoodItem = false;
        for (String food : foodItems) {
            if (food.equals(itemName)) {
                isFoodItem = true;
                break;
            }
        }

        if (isFoodItem) {
            // 统计已解锁的料理数量
            int unlockedCount = 0;
            SQLiteDatabase db = getReadableDatabase();

            try {
                for (String foodItem : foodItems) {
                    Cursor cursor = db.query("backpack",
                            new String[]{"item_count"},
                            "user_id = ? AND item_type = ?",
                            new String[]{String.valueOf(userId), foodItem},
                            null, null, null);

                    if (cursor.moveToFirst()) {
                        int count = cursor.getInt(cursor.getColumnIndexOrThrow("item_count"));
                        if (count > 0) {
                            unlockedCount++;
                        }
                    }
                    cursor.close();
                }

                Log.d("DBHelper", "检测到料理物品添加，更新烹饪成就进度：已解锁料理数量=" + unlockedCount);

                // 更新烹饪成就进度
                updateAchievementProgress(userId, "cooking", unlockedCount);

            } catch (Exception e) {
                Log.e("DBHelper", "更新烹饪成就进度失败", e);
            }
        }
    }

    /**
     * 删除背包物品
     * @param userId 用户ID
     * @param itemName 物品名称
     */
    public void deleteBackpackItem(int userId, String itemName) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete("backpack",
                "user_id=? AND item_type=?",
                new String[]{String.valueOf(userId), itemName});

        if (rowsDeleted > 0) {
            Log.d("DBHelper", "删除背包物品：" + itemName);
        } else {
            Log.w("DBHelper", "删除背包物品失败，物品不存在：" + itemName);
        }
    }

    /**
     * 清空资源冷却记录
     * @param userId 用户ID
     */
    public void deleteAllResourceCDs(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            int rowsDeleted = db.delete("resource_cd",
                    "user_id=?",
                    new String[]{String.valueOf(userId)});

            if (rowsDeleted > 0) {
                Log.d("DBHelper", "删除资源冷却记录：" + rowsDeleted + " 条记录");
            }
        } catch (Exception e) {
            Log.e("DBHelper", "删除资源冷却记录失败", e);
        }
    }

    /**
     * 清空背包（仅清空物品，不移除建筑）
     * @param userId 用户ID
     */
    public void clearBackpack(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            int rowsDeleted = db.delete("backpack",
                    "user_id=?",
                    new String[]{String.valueOf(userId)});

            if (rowsDeleted > 0) {
                Log.d("DBHelper", "清空背包：" + rowsDeleted + " 件物品");
            }
        } catch (Exception e) {
            Log.e("DBHelper", "清空背包失败", e);
        }
    }

    /**
     * 获取用户金币数量
     */
    public int getUserGold(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int gold = 0;
        try {
            cursor = db.query("user_status", new String[]{"gold"},
                    "user_id=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                gold = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return gold;
    }

    /**
     * 获取用户经验值
     */
    public int getUserExp(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int exp = 0;
        try {
            cursor = db.query("user_status", new String[]{"exp"},
                    "user_id=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                exp = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return exp;
    }

    /**
     * 获取用户等级
     */
    public int getUserLevel(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int level = 1; // 默认等级为1
        try {
            cursor = db.query("user_status", new String[]{"level"},
                    "user_id=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                level = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return level;
    }

    /**
     * 更新用户金币
     */
    public void updateUserGold(int userId, int newGold) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("gold", newGold);
        db.update("user_status", values, "user_id=?", 
                new String[]{String.valueOf(userId)});
    }

    /**
     * 更新用户经验
     */
    public void updateUserExp(int userId, int newExp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("exp", newExp);
        db.update("user_status", values, "user_id=?", 
                new String[]{String.valueOf(userId)});
    }

    /**
     * 更新用户等级
     */
    public void updateUserLevel(int userId, int newLevel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("level", newLevel);
        db.update("user_status", values, "user_id=?", 
                new String[]{String.valueOf(userId)});
    }

}