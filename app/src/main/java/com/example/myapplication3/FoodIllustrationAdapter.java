package com.example.myapplication3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodIllustrationAdapter extends RecyclerView.Adapter<FoodIllustrationAdapter.FoodViewHolder> {
    
    private Context context;
    private List<FoodItem> foodList;
    
    public FoodIllustrationAdapter(Context context, List<FoodItem> foodList) {
        this.context = context;
        this.foodList = foodList;
    }
    
    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_grid, parent, false);
        return new FoodViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem foodItem = foodList.get(position);
        
        // 设置食物图片
        if (foodItem.isUnlocked()) {
            // 已解锁：显示正常图片
            holder.foodImage.setImageResource(foodItem.getImageResId());
            holder.lockOverlay.setVisibility(View.GONE);
            holder.questionMark.setVisibility(View.GONE);
        } else {
            // 未解锁：显示马赛克效果和问号
            Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), foodItem.getImageResId());
            if (originalBitmap != null) {
                Bitmap mosaicBitmap = createMosaicEffect(originalBitmap);
                holder.foodImage.setImageBitmap(mosaicBitmap);
            } else {
                // 如果图片资源不存在，使用默认图标
                holder.foodImage.setImageResource(R.drawable.ic_cooking);
            }
            holder.lockOverlay.setVisibility(View.VISIBLE);
            holder.questionMark.setVisibility(View.VISIBLE);
        }
        
        holder.foodName.setText(foodItem.getName());
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (foodItem.isUnlocked()) {
                // 这里需要调用Activity中的方法显示详情对话框
                // 由于RecyclerView的点击事件处理在Activity中，这里需要回调
                if (context instanceof FoodIllustrationActivity) {
                    ((FoodIllustrationActivity) context).showFoodDetailDialog(foodItem);
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return foodList.size();
    }
    
    private Bitmap createMosaicEffect(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        // 创建马赛克效果：将图片分成大块并取平均颜色
        int blockSize = 20; // 马赛克块大小
        Bitmap mosaic = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mosaic);
        Paint paint = new Paint();
        
        for (int x = 0; x < width; x += blockSize) {
            for (int y = 0; y < height; y += blockSize) {
                int blockWidth = Math.min(blockSize, width - x);
                int blockHeight = Math.min(blockSize, height - y);
                
                // 计算块的平均颜色
                int pixelCount = 0;
                int redSum = 0, greenSum = 0, blueSum = 0;
                
                for (int i = x; i < x + blockWidth; i++) {
                    for (int j = y; j < y + blockHeight; j++) {
                        int pixel = original.getPixel(i, j);
                        redSum += Color.red(pixel);
                        greenSum += Color.green(pixel);
                        blueSum += Color.blue(pixel);
                        pixelCount++;
                    }
                }
                
                if (pixelCount > 0) {
                    int avgRed = redSum / pixelCount;
                    int avgGreen = greenSum / pixelCount;
                    int avgBlue = blueSum / pixelCount;
                    
                    paint.setColor(Color.rgb(avgRed, avgGreen, avgBlue));
                    canvas.drawRect(x, y, x + blockWidth, y + blockHeight, paint);
                }
            }
        }
        
        return mosaic;
    }
    
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName;
        View lockOverlay;
        TextView questionMark;
        
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.food_image);
            foodName = itemView.findViewById(R.id.food_name);
            lockOverlay = itemView.findViewById(R.id.lock_overlay);
            questionMark = itemView.findViewById(R.id.question_mark);
        }
    }
}