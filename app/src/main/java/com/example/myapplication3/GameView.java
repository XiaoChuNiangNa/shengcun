package com.example.myapplication3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 游戏地图显示视图（带地形背景自动切换）
 */
public class GameView extends View {
    private GameMap gameMap;
    private int currentX; // 当前角色X坐标（默认1）
    private int currentY; // 当前角色Y坐标（默认1）
    private Paint paint;
    private Bitmap playerIcon; // 角色图标
    private OnCoordChangeListener coordChangeListener; // 坐标变化监听器

    // 构造方法
    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 初始化地图实例
        gameMap = GameMap.getInstance(context);
        // 初始化画笔（抗锯齿、文字样式）
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        // 加载角色图标（你的图标是player2.png，保持不变）
        playerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.player2);
        // 设置初始坐标（避免0,0导致不绘制）
        setCurrentCoord(1, 1);
    }

    /**
     * 设置当前角色坐标 + 触发背景刷新
     */
    public void setCurrentCoord(int x, int y) {
        if (gameMap.isValidCoord(x, y)) {
            this.currentX = x;
            this.currentY = y;
            invalidate(); // 刷新视图，触发onDraw重绘背景
        }
    }

    /**
     * 计算坐标在视图中的绘制位置（格子居中）
     */
    private PointF getDrawPosition(int x, int y) {
        int cellWidth = getWidth() / gameMap.getWidth();
        int cellHeight = getHeight() / gameMap.getHeight();
        // 格子左上角X + 格子一半宽度 = 格子中心点X
        float drawX = (x - Constant.MAP_MIN) * cellWidth + cellWidth / 2f;
        float drawY = (y - Constant.MAP_MIN) * cellHeight + cellHeight / 2f;
        return new PointF(drawX, drawY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 防止初始坐标异常导致不绘制
        if (currentX == 0 || currentY == 0) return;

        // 1. 先绘制所有格子的地形背景（最底层）
        drawMapGridBackground(canvas);
        // 2. 绘制网格线（在背景之上，区分格子）
        drawMapGridLines(canvas);
        // 3. 绘制角色（在最上层，不被遮挡）
        drawPlayer(canvas);
        // 4. 绘制坐标信息（左上角显示）
        drawCoordInfo(canvas);
    }

    /**
     * 绘制所有格子的地形背景（核心：每个格子加载对应拼音背景图）
     */
    private void drawMapGridBackground(Canvas canvas) {
        int cellWidth = getWidth() / gameMap.getWidth();
        int cellHeight = getHeight() / gameMap.getHeight();

        // 遍历所有坐标（从MAP_MIN到MAP_MAX，共7x7格子）
        for (int y = Constant.MAP_MIN; y <= Constant.MAP_MAX; y++) {
            for (int x = Constant.MAP_MIN; x <= Constant.MAP_MAX; x++) {
                // 获取当前格子的地形（如"树林"、"草原"）
                String terrain = gameMap.getTerrainType(x, y);
                // 根据地形获取drawable资源ID（拼音文件名）
                int bgResId = getTerrainResId(terrain);
                if (bgResId == 0) continue; // 资源不存在则跳过

                // 加载背景图 + 缩放适配格子大小
                Bitmap bgBitmap = BitmapFactory.decodeResource(getResources(), bgResId);
                Bitmap scaledBg = Bitmap.createScaledBitmap(bgBitmap, cellWidth, cellHeight, true);
                // 计算当前格子的左上角位置
                float bgLeft = (x - Constant.MAP_MIN) * cellWidth;
                float bgTop = (y - Constant.MAP_MIN) * cellHeight;
                // 绘制背景图到当前格子
                canvas.drawBitmap(scaledBg, bgLeft, bgTop, null);

                // 回收Bitmap，避免内存泄漏
                scaledBg.recycle();
                bgBitmap.recycle();
            }
        }
    }

    /**
     * 绘制网格线（灰色细线，区分格子）
     */
    private void drawMapGridLines(Canvas canvas) {
        int cellWidth = getWidth() / gameMap.getWidth();
        int cellHeight = getHeight() / gameMap.getHeight();
        int mapSize = gameMap.getWidth(); // 7格

        paint.setColor(Color.parseColor("#888888")); // 浅灰色，不遮挡背景
        paint.setStrokeWidth(1); // 细线

        // 绘制竖线（X轴方向）
        for (int x = 0; x <= mapSize; x++) {
            float lineX = x * cellWidth;
            canvas.drawLine(lineX, 0, lineX, getHeight(), paint);
        }

        // 绘制横线（Y轴方向）
        for (int y = 0; y <= mapSize; y++) {
            float lineY = y * cellHeight;
            canvas.drawLine(0, lineY, getWidth(), lineY, paint);
        }
    }

    /**
     * 核心：根据地形名获取drawable拼音资源ID（必须与你的图片名完全匹配）
     */
    private int getTerrainResId(String terrain) {
        switch (terrain) {
            case "草原":       return R.drawable.caoyuan;    // 草原 → caoyuan.png
            case "河流":       return R.drawable.heliu;      // 河流 → heliu.png
            case "沙漠":       return R.drawable.shamo;      // 沙漠 → shamo.png
            case "沼泽":       return R.drawable.zhaoze;     // 沼泽 → zhaoze.png
            case "岩石":       return R.drawable.yanshiqu;   // 岩石 → yanshiqu.png（与你原代码一致）
            case "岩石区":     return R.drawable.yanshiqu;   // 岩石区 → 同岩石图
            case "针叶林":     return R.drawable.zhenyelin;  // 针叶林 → zhenyelin.png
            case "雪山":       return R.drawable.xueshan;    // 雪山 → xueshan.png
            case "树林":       return R.drawable.shuling;     // 树林 → shulin.png（修正原代码的shuling）
            case "废弃营地":   return R.drawable.feiqiyingdi;// 废弃营地 → feiqiyingdi.jpg
            case "海洋":       return R.drawable.haiyang;    // 海洋 → haiyang.png
            case "海滩":       return R.drawable.haitan;     // 海滩 → haitan.jpg
            case "深海":       return R.drawable.shenhai;    // 深海 → shenhai.png
            case "雪原":       return R.drawable.xueyuan;    // 雪原 → xueyuan.png
            case "村落":       return R.drawable.cunluo;     // 村落 → cunluo.png
            case "茅草屋":       return R.drawable.maocaowu;
            case "小木屋":       return R.drawable.xiaomuwu;
            case "小石屋":       return R.drawable.xiaoshiwu;
            case "砖瓦屋":       return R.drawable.zhuanwawu;
            default:           return R.drawable.weizhiquyu;    // 未知地形 → unknown.png（建议准备）
        }
    }

    /**
     * 绘制角色图标（居中显示在当前格子）
     */
    private void drawPlayer(Canvas canvas) {
        if (playerIcon == null) return;

        PointF playerPos = getDrawPosition(currentX, currentY);
        // 角色图标大小：屏幕1/10，避免过大
        int iconSize = Math.min(getWidth() / 10, getHeight() / 10);
        // 缩放图标（保持比例）
        Bitmap scaledPlayer = Bitmap.createScaledBitmap(playerIcon, iconSize, iconSize, true);
        // 计算图标左上角位置（让图标中心与格子中心对齐）
        float iconLeft = playerPos.x - iconSize / 2f;
        float iconTop = playerPos.y - iconSize / 2f;

        canvas.drawBitmap(scaledPlayer, iconLeft, iconTop, null);
        scaledPlayer.recycle(); // 回收资源
    }

    /**
     * 绘制当前坐标和地形信息（左上角显示，黑色文字更清晰）
     */
    private void drawCoordInfo(Canvas canvas) {
//        String terrain = gameMap.getTerrainType(currentX, currentY);
//        String info = "坐标: (" + currentX + "," + currentY + ") | 地形: " + terrain;

//        paint.setColor(Color.BLACK);
//        paint.setShadowLayer(1, 0, 0, Color.WHITE); // 文字加白阴影，避免与背景融合
//        canvas.drawText(info, 20, 40, paint);
        paint.clearShadowLayer(); // 清除阴影，避免影响其他绘制
    }

    /**
     * 触摸事件：点击地图任意格子，移动角色并切换背景
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) { // 手指抬起时触发
            int cellWidth = getWidth() / gameMap.getWidth();
            int cellHeight = getHeight() / gameMap.getHeight();

            // 计算点击位置对应的格子坐标（屏幕X→格子X，屏幕Y→格子Y）
            int touchX = (int) (event.getX() / cellWidth) + Constant.MAP_MIN;
            int touchY = (int) (event.getY() / cellHeight) + Constant.MAP_MIN;

            // 触发坐标变化（MainActivity中监听，更新角色位置）
            if (coordChangeListener != null) {
                coordChangeListener.onCoordChanged(touchX, touchY);
            }
            return true; // 消费事件，避免穿透
        }
        return super.onTouchEvent(event);
    }

    // ------------------- 监听器相关（保持不变）-------------------
    public void setOnCoordChangeListener(OnCoordChangeListener listener) {
        this.coordChangeListener = listener;
    }

    public interface OnCoordChangeListener {
        void onCoordChanged(int newX, int newY);
    }

    // ------------------- 资源回收（避免内存泄漏）-------------------
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (playerIcon != null && !playerIcon.isRecycled()) {
            playerIcon.recycle();
        }
    }
}