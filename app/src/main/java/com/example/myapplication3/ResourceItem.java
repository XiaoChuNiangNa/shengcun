package com.example.myapplication3;

// 资源图鉴数据模型
public class ResourceItem {
    private String name;
    private String description;
    private String usage;
    private String imageName;
    private boolean unlocked;

    public ResourceItem(String name, String description, String usage, String imageName, boolean unlocked) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.imageName = imageName;
        this.unlocked = unlocked;
    }

    // getter方法
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getImageName() {
        return imageName;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}