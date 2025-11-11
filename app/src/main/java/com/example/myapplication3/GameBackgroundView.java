package com.example.myapplication3;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GameBackgroundView extends View {

    private int currentX;
    private int currentY;
    private Bitmap backgroundBitmap;
    private Paint paint;
    private OnCoordChangeListener coordChangeListener;
    // 地形中文名 → 拼音（drawable文件名）
    private static final Map<String, String> TERRAIN_PINYIN = new HashMap<>();
    // LRU缓存管理Bitmap，限制最大缓存大小（以KB为单位）
    private final LruCache<String, Bitmap> bitmapCache;

    static {
        TERRAIN_PINYIN.put("草原", "caoyuan");
        TERRAIN_PINYIN.put("河流", "heliu");
        TERRAIN_PINYIN.put("沙漠", "shamo");
        TERRAIN_PINYIN.put("沼泽", "zhaoze");
        TERRAIN_PINYIN.put("岩石", "yanshiqu");
        TERRAIN_PINYIN.put("岩石区", "yanshiqu");
        TERRAIN_PINYIN.put("针叶林", "zhenyelin");
        TERRAIN_PINYIN.put("雪山", "xueshan");
        TERRAIN_PINYIN.put("树林", "shuling");
        TERRAIN_PINYIN.put("废弃营地", "feiqiyingdi");
        TERRAIN_PINYIN.put("海洋", "haiyang");
        TERRAIN_PINYIN.put("海滩", "haitan");
        TERRAIN_PINYIN.put("深海", "shenhai");
        TERRAIN_PINYIN.put("雪原", "xueyuan");
        TERRAIN_PINYIN.put("村落", "cunluo");
        TERRAIN_PINYIN.put("茅草屋", "maocaowu");
        TERRAIN_PINYIN.put("小木屋", "xiaomuwu");
        TERRAIN_PINYIN.put("小石屋", "xiaoshiwu");
        TERRAIN_PINYIN.put("砖瓦屋", "zhuanwawu");
        TERRAIN_PINYIN.put("传送门", "chuansongmen");
    }

    // 初始化LRU缓存（最大缓存5张图片，根据实际内存情况调整）
    private int getMaxCacheSize() {
        // 获取应用最大可用内存的1/8作为缓存（单位：KB）
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        return maxMemory / 8;
    }

    public GameBackgroundView(Context context) {
        super(context);
        bitmapCache = new LruCache<String, Bitmap>(getMaxCacheSize()) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024; // 以KB为单位计算
            }
        };
        init();
    }

    public GameBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bitmapCache = new LruCache<String, Bitmap>(getMaxCacheSize()) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
        init();
    }

    public GameBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bitmapCache = new LruCache<String, Bitmap>(getMaxCacheSize()) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
        init();
    }

    private void init() {
        // 延迟初始化Paint，在首次使用时创建
    }

    // 延迟获取Paint实例
    private Paint getPaint() {
        if (paint == null) {
            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            paint.setAntiAlias(true);
            paint.setShadowLayer(2, 0, 0, Color.BLACK);
        }
        return paint;
    }

    /**
     * 设置当前坐标，并刷新背景（允许强制刷新）
     * @param x 新X坐标
     * @param y 新Y坐标
     * @param forceRefresh 强制刷新（即使坐标未变）
     */
    public void setCurrentCoord(int x, int y, boolean forceRefresh) {
        boolean isCoordChanged = (x != currentX || y != currentY);

        if (isCoordChanged || forceRefresh) {
            this.currentX = x;
            this.currentY = y;

            if (isCoordChanged && coordChangeListener != null) {
                coordChangeListener.onCoordChanged(x, y);
            }

            if (getWidth() == 0 || getHeight() == 0) {
                addOnLayoutChangeListener(new OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (getWidth() > 0 && getHeight() > 0) {
                            loadBackground();
                            invalidate();
                            removeOnLayoutChangeListener(this);
                        }
                    }
                });
            } else {
                loadBackground();
                invalidate();
            }
        }
    }

    public void setCurrentCoord(int x, int y) {
        setCurrentCoord(x, y, false);
    }

    /**
     * 强制刷新背景（无论坐标是否变化）
     */
    public void forceRefreshBackground() {
        loadBackground();
        invalidate();
    }

    /**
     * 根据当前坐标加载背景图（使用缓存优化）
     */
    private void loadBackground() {
        GameMap gameMap = GameMap.getInstance(getContext());
        String terrain = gameMap.getTerrainType(currentX, currentY);
        Log.d("BackgroundDebug", "加载背景 - 坐标(" + currentX + "," + currentY + ")，地形: " + terrain);

        String pinyin = TERRAIN_PINYIN.getOrDefault(terrain, "unknown");
        int resId = getResources().getIdentifier(pinyin, "drawable", getContext().getPackageName());

        if (resId == 0) {
            Log.e("BackgroundError", "未找到地形图片！地形: " + terrain + "，拼音: " + pinyin);
            resId = getResources().getIdentifier("unknown", "drawable", getContext().getPackageName());
            if (resId == 0) {
                Log.e("BackgroundError", "默认图片也不存在！使用空白图");
                backgroundBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                return;
            }
        }

        if (getWidth() > 0 && getHeight() > 0) {
            // 生成包含尺寸和亮度的缓存键
            boolean isNight = ((BaseActivity) getContext()).isNightTime();
            String cacheKey = pinyin + "_" + getWidth() + "_" + getHeight() + "_" + (isNight ? "night" : "day");

            // 尝试从缓存获取
            Bitmap cachedBitmap = bitmapCache.get(cacheKey);
            if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
                backgroundBitmap = cachedBitmap;
                Log.d("BackgroundCache", "使用缓存图片: " + cacheKey);
                return;
            }

            // 缓存未命中，解码并处理图片
            Bitmap originalBitmap = decodeSampledBitmapFromResource(getResources(), resId, getWidth(), getHeight());
            if (originalBitmap == null) {
                Log.e("BackgroundError", "图片解码失败，资源ID: " + resId);
                backgroundBitmap = null;
                return;
            }

            // 调整亮度并缓存
            float brightness = isNight ? 0.6f : 1.0f;
            backgroundBitmap = adjustBrightness(originalBitmap, brightness);

            // 存入缓存
            if (backgroundBitmap != null) {
                bitmapCache.put(cacheKey, backgroundBitmap);
                Log.d("BackgroundCache", "缓存图片: " + cacheKey + "，当前缓存大小: " + bitmapCache.size());
            }

            // 回收原始bitmap（如果创建了新的bitmap）
            if (originalBitmap != backgroundBitmap && !originalBitmap.isRecycled()) {
                originalBitmap.recycle();
            }
        } else {
            Log.w("BackgroundDebug", "视图尺寸未初始化，延迟加载");
            post(this::loadBackground);
        }
    }

    /**
     * 按需要的宽高加载压缩后的图片
     */
    private Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        // 优化：使用RGB_565减少内存占用（如果不需要透明通道）
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap original = BitmapFactory.decodeResource(res, resId, options);
        if (original == null) {
            Log.e("GameBackground", "解码失败，资源ID: " + resId);
            return Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565);
        }

        return Bitmap.createScaledBitmap(original, reqWidth, reqHeight, true);
    }

    /**
     * 计算缩放比例
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (options.outHeight == 0 || options.outWidth == 0 || reqWidth == 0 || reqHeight == 0) {
            return 1;
        }

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap,
                    new Rect(0, 0, backgroundBitmap.getWidth(), backgroundBitmap.getHeight()),
                    new Rect(0, 0, getWidth(), getHeight()), null);
        } else {
            canvas.drawColor(Color.GRAY);
            Log.w("onDraw", "backgroundBitmap为null，绘制灰色背景");
        }

        // 调试用：显示当前地形（可选开启）
        // String terrain = GameMap.getInstance(getContext()).getTerrainType(currentX, currentY);
        // canvas.drawText("地形: " + terrain, 20, 60, getPaint());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 尺寸变化时清除相关缓存并重新加载
        clearRelatedCache();
        loadBackground();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 清除所有缓存并回收资源
        bitmapCache.evictAll();
        if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) {
            backgroundBitmap.recycle();
            backgroundBitmap = null;
        }
        // 释放Paint资源
        paint = null;
    }

    /**
     * 调整Bitmap亮度（直接在原图上修改，减少对象创建）
     */
    private Bitmap adjustBrightness(Bitmap bitmap, float brightness) {
        if (bitmap == null) return null;

        // 直接在原图上操作（避免创建新Bitmap）
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(brightness, brightness, brightness, 1);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmap;
    }

    /**
     * 清除与当前尺寸相关的缓存
     */
    private void clearRelatedCache() {
        for (String key : bitmapCache.snapshot().keySet()) {
            if (key.contains(getWidth() + "_" + getHeight())) {
                bitmapCache.remove(key);
            }
        }
    }

    // 坐标变化监听器接口
    public interface OnCoordChangeListener {
        void onCoordChanged(int x, int y);
    }

    public void setOnCoordChangeListener(OnCoordChangeListener listener) {
        this.coordChangeListener = listener;
    }
}