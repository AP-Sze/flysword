package com.bird.flysword.data;

import org.bukkit.configuration.ConfigurationSection;

public class SwordEnchant {
    
    private final String id;
    private final String displayName;
    private final String description;
    private final int maxLevel;
    private final double effectPerLevel;
    
    public SwordEnchant(String id, String displayName, String description, int maxLevel, double effectPerLevel) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.maxLevel = maxLevel;
        this.effectPerLevel = effectPerLevel;
    }
    
    public static SwordEnchant fromConfig(String id, ConfigurationSection config) {
        String displayName = config.getString("displayName", id);
        String description = config.getString("description", "");
        int maxLevel = config.getInt("maxLevel", 3);
        double effectPerLevel = config.getDouble("effectPerLevel", 0.1);
        
        return new SwordEnchant(id, displayName, description, maxLevel, effectPerLevel);
    }
    
    public void saveToConfig(ConfigurationSection config) {
        config.set("displayName", displayName);
        config.set("description", description);
        config.set("maxLevel", maxLevel);
        config.set("effectPerLevel", effectPerLevel);
    }
    
    // Getter 方法
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getMaxLevel() {
        return maxLevel;
    }
    
    public double getEffectPerLevel() {
        return effectPerLevel;
    }
    
    public double getEffect(int level) {
        return effectPerLevel * Math.min(level, maxLevel);
    }
}
