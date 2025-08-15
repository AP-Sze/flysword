package com.bird.flysword.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 飛劍物品工具類
 */
public class SwordItemUtils {
    
    /**
     * 檢查物品是否為飛劍
     */
    public static boolean isFlySword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return false;
        }
        
        String displayName = meta.getDisplayName();
        return displayName.contains("御劍") || displayName.contains("飛劍");
    }
    
    /**
     * 獲取飛劍的皮膚ID
     */
    public static String getSkinId(ItemStack item) {
        if (!isFlySword(item)) {
            return "default";
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore() && meta.getLore().size() > 1) {
            String loreLine = meta.getLore().get(1);
            if (loreLine.contains("皮膚:")) {
                // 從 lore 中提取皮膚名稱
                String skinName = loreLine.replace("§7皮膚: ", "").trim();
                return skinName.toLowerCase().replace(" ", "_");
            }
        }
        
        return "default";
    }
    
    /**
     * 檢查飛劍是否還有耐久度
     */
    public static boolean hasDurability(ItemStack item) {
        if (item == null || item.getType().getMaxDurability() <= 0) {
            return true; // 沒有耐久度系統的物品視為有耐久度
        }
        
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
            org.bukkit.inventory.meta.Damageable damageable = 
                (org.bukkit.inventory.meta.Damageable) item.getItemMeta();
            
            int currentDamage = damageable.getDamage();
            int maxDurability = item.getType().getMaxDurability();
            
            return currentDamage < maxDurability;
        }
        
        return true;
    }
    
    /**
     * 獲取飛劍耐久度百分比
     */
    public static double getDurabilityPercentage(ItemStack item) {
        if (item == null || item.getType().getMaxDurability() <= 0) {
            return 100.0; // 沒有耐久度系統
        }
        
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
            org.bukkit.inventory.meta.Damageable damageable = 
                (org.bukkit.inventory.meta.Damageable) item.getItemMeta();
            
            int currentDamage = damageable.getDamage();
            int maxDurability = item.getType().getMaxDurability();
            int remainingDurability = maxDurability - currentDamage;
            
            return (double) remainingDurability / maxDurability * 100.0;
        }
        
        return 100.0;
    }
}
