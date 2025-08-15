package com.bird.flysword.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.SwordSkin;

public class SkinManager {
    
    private final Flysword plugin;
    private final Map<String, SwordSkin> skins;
    
    public SkinManager(Flysword plugin) {
        this.plugin = plugin;
        this.skins = new HashMap<>();
    }
    
    public void loadSkins() {
        FileConfiguration config = plugin.getConfigManager().getSkinsConfig();
        ConfigurationSection skinsSection = config.getConfigurationSection("skins");
        
        if (skinsSection != null) {
            for (String skinId : skinsSection.getKeys(false)) {
                ConfigurationSection skinSection = skinsSection.getConfigurationSection(skinId);
                if (skinSection != null) {
                    SwordSkin skin = SwordSkin.fromConfig(skinId, skinSection);
                    skins.put(skinId, skin);
                }
            }
        }
        
        // 確保默認皮膚存在
        if (!skins.containsKey("default")) {
            SwordSkin defaultSkin = new SwordSkin("default", "默認飛劍", "默認的飛劍皮膚", 1, "default");
            skins.put("default", defaultSkin);
        }
        
        plugin.getLogger().info("已加載 " + skins.size() + " 個飛劍皮膚");
    }
    
    public SwordSkin getSkin(String skinId) {
        return skins.get(skinId);
    }
    
    public java.util.Set<String> getAllSkinIds() {
        return skins.keySet();
    }
    
    public Map<String, SwordSkin> getAllSkins() {
        return skins;
    }
    
    public boolean hasSkin(String skinId) {
        return skins.containsKey(skinId);
    }
    
    public ItemStack createSwordWithSkin(String skinId) {
        SwordSkin skin = getSkin(skinId);
        if (skin == null) {
            skin = getSkin("default");
        }
        
        ItemStack item = skin.createItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 保存原有的 CustomModelData
            Integer originalCustomModelData = null;
            if (meta.hasCustomModelData()) {
                originalCustomModelData = meta.getCustomModelData();
            }
            
            // 計算實際的耐久度百分比
            String durabilityDisplay = getDurabilityDisplay(item);
            
            meta.setDisplayName("§6§l御劍");
            meta.setLore(java.util.Arrays.asList(
                "§7右鍵啟動飛劍模式",
                "§7皮膚: " + skin.getDisplayName(),
                "§7耐久度: " + durabilityDisplay
            ));
            
            // 恢復 CustomModelData（如果原本有的話）
            if (originalCustomModelData != null) {
                meta.setCustomModelData(originalCustomModelData);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 根據玩家數據創建飛劍（包含正確的耐久度顯示）
     */
    public ItemStack createSwordWithPlayerData(String skinId, com.bird.flysword.data.PlayerData playerData) {
        SwordSkin skin = getSkin(skinId);
        if (skin == null) {
            skin = getSkin("default");
        }
        
        ItemStack item = skin.createItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 保存原有的 CustomModelData
            Integer originalCustomModelData = null;
            if (meta.hasCustomModelData()) {
                originalCustomModelData = meta.getCustomModelData();
            }
            
            // 計算耐久度顯示
            int durability = playerData.getDurability();
            String durabilityColor;
            if (durability > 60) {
                durabilityColor = "§a"; // 綠色
            } else if (durability > 30) {
                durabilityColor = "§e"; // 黃色
            } else {
                durabilityColor = "§c"; // 紅色
            }
            
            meta.setDisplayName("§6§l御劍");
            meta.setLore(java.util.Arrays.asList(
                "§7右鍵啟動飛劍模式",
                "§7皮膚: " + skin.getDisplayName(),
                "§7耐久度: " + durabilityColor + durability + "%"
            ));
            
            // 恢復 CustomModelData（如果原本有的話）
            if (originalCustomModelData != null) {
                meta.setCustomModelData(originalCustomModelData);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void addSkin(SwordSkin skin) {
        skins.put(skin.getId(), skin);
        saveSkins();
    }
    
    public void removeSkin(String skinId) {
        skins.remove(skinId);
        saveSkins();
    }
    
    private void saveSkins() {
        FileConfiguration config = plugin.getConfigManager().getSkinsConfig();
        config.set("skins", null); // 清除現有配置
        
        ConfigurationSection skinsSection = config.createSection("skins");
        for (SwordSkin skin : skins.values()) {
            ConfigurationSection skinSection = skinsSection.createSection(skin.getId());
            skin.saveToConfig(skinSection);
        }
        
        plugin.getConfigManager().saveSkinsConfig();
    }
    
    /**
     * 獲取物品耐久度的顯示文字
     */
    private String getDurabilityDisplay(ItemStack item) {
        if (item.getType().getMaxDurability() <= 0) {
            return "§a無限"; // 沒有耐久度系統的物品
        }
        
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
            org.bukkit.inventory.meta.Damageable damageable = 
                (org.bukkit.inventory.meta.Damageable) item.getItemMeta();
            
            int currentDamage = damageable.getDamage();
            int maxDurability = item.getType().getMaxDurability();
            int remainingDurability = maxDurability - currentDamage;
            double percentage = (double) remainingDurability / maxDurability * 100;
            
            String color;
            if (percentage > 60) {
                color = "§a"; // 綠色
            } else if (percentage > 30) {
                color = "§e"; // 黃色
            } else {
                color = "§c"; // 紅色
            }
            
            return color + String.format("%.0f%%", percentage);
        }
        
        return "§a100%";
    }
}
