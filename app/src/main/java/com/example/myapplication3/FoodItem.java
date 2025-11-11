package com.example.myapplication3;

public class FoodItem {
    private String name;
    private String description;
    private int imageResId;
    private String recipe;
    private boolean unlocked;
    
    public FoodItem(String name, String description, int imageResId, String recipe, boolean unlocked) {
        this.name = name;
        this.description = description;
        this.imageResId = imageResId;
        this.recipe = recipe;
        this.unlocked = unlocked;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getImageResId() {
        return imageResId;
    }
    
    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
    
    public String getRecipe() {
        return recipe;
    }
    
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }
    
    public boolean isUnlocked() {
        return unlocked;
    }
    
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}