package com.bird.flysword.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SwordSkin {
    
    private final String id;
    private final String displayName;
    private final String description;
    private final int customModelData;
    private final String modelPath;
    private final String unlockType;
    private final String unlockValue;
    
    public SwordSkin(String id, String displayName, String description, int customModelData, String modelPath) {
        this(id, displayName, description, customModelData, modelPath, "default", "");
    }
    
    public SwordSkin(String id, String displayName, String description, int customModelData, String modelPath, String unlockType, String unlockValue) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.customModelData = customModelData;
        this.modelPath = modelPath;
        this.unlockType = unlockType;
        this.unlockValue = unlockValue;
    }
    
    public static SwordSkin fromConfig(String id, ConfigurationSection config) {
        String displayName = config.getString("displayName", id);
        String description = config.getString("description", "");
        int customModelData = config.getInt("customModelData", 1);
        String modelPath = config.getString("modelPath", id);
        String unlockType = config.getString("unlockType", "default");
        String unlockValue = config.getString("unlockValue", "");
        
        return new SwordSkin(id, displayName, description, customModelData, modelPath, unlockType, unlockValue);
    }
    
    public void saveToConfig(ConfigurationSection config) {
        config.set("displayName", displayName);
        config.set("description", description);
        config.set("customModelData", customModelData);
        config.set("modelPath", modelPath);
        config.set("unlockType", unlockType);
        config.set("unlockValue", unlockValue);
    }
    
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§l" + displayName);
            meta.setLore(java.util.Arrays.asList(
                "§7" + description,
                "§7皮膚ID: " + id,
                "§7模型: " + modelPath
            ));
            
            // 只有當 customModelData 大於 0 時才設置 CustomModelData
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            
            // 添加持久化數據
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(org.bukkit.NamespacedKey.fromString("flysword:skin_id"), PersistentDataType.STRING, id);
            
            item.setItemMeta(meta);
        }
        
        return item;
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
    
    public int getCustomModelData() {
        return customModelData;
    }
    
    public String getModelPath() {
        return modelPath;
    }
    
    public String getUnlockType() {
        return unlockType;
    }
    
    public String getUnlockValue() {
        return unlockValue;
    }
}
