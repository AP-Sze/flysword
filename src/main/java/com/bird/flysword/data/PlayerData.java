package com.bird.flysword.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class PlayerData {
    
    private final UUID playerUUID;
    private String selectedSkin;
    private final Set<String> unlockedSkins;
    private final Map<String, Integer> enchantLevels;
    private int durability;
    private long lastFlightTime;
    private boolean isFlying;
    
    // 新增擴展功能
    private int skinTokens; // 皮膚券數量
    private final Map<String, Object> activityRecords; // 活動記錄
    private final Map<String, Long> achievementProgress; // 成就進度
    private String currentVipLevel; // VIP等級
    private long totalFlightTime; // 總飛行時間
    private int totalFlights; // 總飛行次數
    
    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.selectedSkin = "default";
        this.unlockedSkins = new HashSet<>();
        this.unlockedSkins.add("default"); // 默認皮膚
        this.enchantLevels = new HashMap<>();
        this.durability = 100;
        this.lastFlightTime = 0;
        this.isFlying = false;
        
        // 初始化新增字段
        this.skinTokens = 0;
        this.activityRecords = new HashMap<>();
        this.achievementProgress = new HashMap<>();
        this.currentVipLevel = "NONE";
        this.totalFlightTime = 0L;
        this.totalFlights = 0;
    }
    
    public static PlayerData fromConfig(FileConfiguration config) {
        UUID uuid = UUID.fromString(config.getString("uuid"));
        PlayerData playerData = new PlayerData(uuid);
        
        playerData.selectedSkin = config.getString("selectedSkin", "default");
        playerData.unlockedSkins.addAll(config.getStringList("unlockedSkins"));
        if (playerData.unlockedSkins.isEmpty()) {
            playerData.unlockedSkins.add("default");
        }
        
        ConfigurationSection enchantSection = config.getConfigurationSection("enchants");
        if (enchantSection != null) {
            for (String enchant : enchantSection.getKeys(false)) {
                playerData.enchantLevels.put(enchant, enchantSection.getInt(enchant, 0));
            }
        }
        
        playerData.durability = config.getInt("durability", 100);
        playerData.lastFlightTime = config.getLong("lastFlightTime", 0);
        playerData.isFlying = config.getBoolean("isFlying", false);
        
        // 載入新增字段
        playerData.skinTokens = config.getInt("skinTokens", 0);
        playerData.currentVipLevel = config.getString("vipLevel", "NONE");
        playerData.totalFlightTime = config.getLong("totalFlightTime", 0L);
        playerData.totalFlights = config.getInt("totalFlights", 0);
        
        // 載入活動記錄
        ConfigurationSection activitySection = config.getConfigurationSection("activityRecords");
        if (activitySection != null) {
            for (String key : activitySection.getKeys(false)) {
                playerData.activityRecords.put(key, activitySection.get(key));
            }
        }
        
        // 載入成就進度
        ConfigurationSection achievementSection = config.getConfigurationSection("achievementProgress");
        if (achievementSection != null) {
            for (String key : achievementSection.getKeys(false)) {
                playerData.achievementProgress.put(key, achievementSection.getLong(key, 0L));
            }
        }
        
        return playerData;
    }
    
    public void saveToConfig(FileConfiguration config) {
        config.set("uuid", playerUUID.toString());
        config.set("selectedSkin", selectedSkin);
        config.set("unlockedSkins", new ArrayList<>(unlockedSkins));
        config.set("durability", durability);
        config.set("lastFlightTime", lastFlightTime);
        config.set("isFlying", isFlying);
        
        // 保存新增字段
        config.set("skinTokens", skinTokens);
        config.set("vipLevel", currentVipLevel);
        config.set("totalFlightTime", totalFlightTime);
        config.set("totalFlights", totalFlights);
        
        // 保存活動記錄
        if (!activityRecords.isEmpty()) {
            ConfigurationSection activitySection = config.createSection("activityRecords");
            for (Map.Entry<String, Object> entry : activityRecords.entrySet()) {
                activitySection.set(entry.getKey(), entry.getValue());
            }
        }
        
        // 保存成就進度
        if (!achievementProgress.isEmpty()) {
            ConfigurationSection achievementSection = config.createSection("achievementProgress");
            for (Map.Entry<String, Long> entry : achievementProgress.entrySet()) {
                achievementSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        // 保存附魔等級
        ConfigurationSection enchantSection = config.createSection("enchants");
        for (Map.Entry<String, Integer> entry : enchantLevels.entrySet()) {
            enchantSection.set(entry.getKey(), entry.getValue());
        }
    }
    
    // Getter 和 Setter 方法
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getSelectedSkin() {
        return selectedSkin;
    }
    
    public void setSelectedSkin(String selectedSkin) {
        this.selectedSkin = selectedSkin;
    }
    
    public Set<String> getUnlockedSkins() {
        return unlockedSkins;
    }
    
    public boolean hasSkin(String skinId) {
        return unlockedSkins.contains(skinId);
    }
    
    public void unlockSkin(String skinId) {
        unlockedSkins.add(skinId);
    }
    
    public Map<String, Integer> getEnchantLevels() {
        return enchantLevels;
    }
    
    public int getEnchantLevel(String enchantId) {
        return enchantLevels.getOrDefault(enchantId, 0);
    }
    
    public void setEnchantLevel(String enchantId, int level) {
        enchantLevels.put(enchantId, level);
    }
    
    public int getDurability() {
        return durability;
    }
    
    public void setDurability(int durability) {
        this.durability = Math.max(0, Math.min(100, durability));
    }
    
    public void addDurability(int amount) {
        setDurability(durability + amount);
    }
    
    public long getLastFlightTime() {
        return lastFlightTime;
    }
    
    public void setLastFlightTime(long lastFlightTime) {
        this.lastFlightTime = lastFlightTime;
    }
    
    public boolean isFlying() {
        return isFlying;
    }
    
    public void setFlying(boolean flying) {
        isFlying = flying;
    }
    
    // 新增字段的 getter 和 setter 方法
    
    public int getSkinTokens() {
        return skinTokens;
    }
    
    public void setSkinTokens(int skinTokens) {
        this.skinTokens = Math.max(0, skinTokens);
    }
    
    public void addSkinTokens(int amount) {
        setSkinTokens(skinTokens + amount);
    }
    
    public boolean consumeSkinToken() {
        if (skinTokens > 0) {
            skinTokens--;
            return true;
        }
        return false;
    }
    
    public Map<String, Object> getActivityRecords() {
        return activityRecords;
    }
    
    public void setActivityRecord(String key, Object value) {
        activityRecords.put(key, value);
    }
    
    public Object getActivityRecord(String key) {
        return activityRecords.get(key);
    }
    
    public Map<String, Long> getAchievementProgress() {
        return achievementProgress;
    }
    
    public void setAchievementProgress(String achievementId, long progress) {
        achievementProgress.put(achievementId, progress);
    }
    
    public long getAchievementProgress(String achievementId) {
        return achievementProgress.getOrDefault(achievementId, 0L);
    }
    
    public void addAchievementProgress(String achievementId, long amount) {
        long current = getAchievementProgress(achievementId);
        setAchievementProgress(achievementId, current + amount);
    }
    
    public String getCurrentVipLevel() {
        return currentVipLevel;
    }
    
    public void setCurrentVipLevel(String vipLevel) {
        this.currentVipLevel = vipLevel != null ? vipLevel : "NONE";
    }
    
    public boolean hasVipLevel(String requiredLevel) {
        if ("NONE".equals(currentVipLevel) || "NONE".equals(requiredLevel)) {
            return "NONE".equals(requiredLevel);
        }
        
        // 簡單的 VIP 等級比較邏輯，可以根據需要擴展
        String[] levels = {"BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND"};
        int currentIndex = -1;
        int requiredIndex = -1;
        
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equals(currentVipLevel)) currentIndex = i;
            if (levels[i].equals(requiredLevel)) requiredIndex = i;
        }
        
        return currentIndex >= requiredIndex;
    }
    
    public long getTotalFlightTime() {
        return totalFlightTime;
    }
    
    public void setTotalFlightTime(long totalFlightTime) {
        this.totalFlightTime = Math.max(0, totalFlightTime);
    }
    
    public void addFlightTime(long time) {
        setTotalFlightTime(totalFlightTime + time);
    }
    
    public int getTotalFlights() {
        return totalFlights;
    }
    
    public void setTotalFlights(int totalFlights) {
        this.totalFlights = Math.max(0, totalFlights);
    }
    
    public void incrementTotalFlights() {
        totalFlights++;
    }
}
