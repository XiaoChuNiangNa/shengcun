package com.example.myapplication3;

import java.util.Map;

// 配方数据模型
public class Recipe {
    private String name;
    private Map<String, Integer> requirements;

    public Recipe(String name, Map<String, Integer> requirements) {
        this.name = name;
        this.requirements = requirements;
    }

    // getter方法
    public String getName() {
        return name;
    }

    public Map<String, Integer> getRequirements() {
        return requirements;
    }
}
