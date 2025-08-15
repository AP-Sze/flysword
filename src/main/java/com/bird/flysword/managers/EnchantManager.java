package com.bird.flysword.managers;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.SwordEnchant;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class EnchantManager {
    
    private final Flysword plugin;
    private final Map<String, SwordEnchant> enchants;
    
    public EnchantManager(Flysword plugin) {
        this.plugin = plugin;
        this.enchants = new HashMap<>();
    }
    
    public void loadEnchants() {
        FileConfiguration config = plugin.getConfigManager().getEnchantsConfig();
        ConfigurationSection enchantsSection = config.getConfigurationSection("enchants");
        
        if (enchantsSection != null) {
            for (String enchantId : enchantsSection.getKeys(false)) {
                ConfigurationSection enchantSection = enchantsSection.getConfigurationSection(enchantId);
                if (enchantSection != null) {
                    SwordEnchant enchant = SwordEnchant.fromConfig(enchantId, enchantSection);
                    enchants.put(enchantId, enchant);
                }
            }
        }
        
        // 添加默認附魔
        if (!enchants.containsKey("speed")) {
            enchants.put("speed", new SwordEnchant("speed", "飛行加速", "提升飛行速度", 3, 0.2));
        }
        if (!enchants.containsKey("stability")) {
            enchants.put("stability", new SwordEnchant("stability", "飛行穩定", "提升飛行穩定性", 3, 0.15));
        }
        if (!enchants.containsKey("regen")) {
            enchants.put("regen", new SwordEnchant("regen", "能量回復", "飛行時回復能量", 3, 0.1));
        }
        if (!enchants.containsKey("shield")) {
            enchants.put("shield", new SwordEnchant("shield", "降落護盾", "降落時提供保護", 3, 0.25));
        }
        
        plugin.getLogger().info("已加載 " + enchants.size() + " 個飛劍附魔");
    }
    
    public SwordEnchant getEnchant(String enchantId) {
        return enchants.get(enchantId);
    }
    
    public Map<String, SwordEnchant> getAllEnchants() {
        return enchants;
    }
    
    public boolean hasEnchant(String enchantId) {
        return enchants.containsKey(enchantId);
    }
    
    public double getEnchantEffect(String enchantId, int level) {
        SwordEnchant enchant = getEnchant(enchantId);
        if (enchant == null) return 0.0;
        return enchant.getEffectPerLevel() * level;
    }
    
    public void addEnchant(SwordEnchant enchant) {
        enchants.put(enchant.getId(), enchant);
        saveEnchants();
    }
    
    public void removeEnchant(String enchantId) {
        enchants.remove(enchantId);
        saveEnchants();
    }
    
    private void saveEnchants() {
        FileConfiguration config = plugin.getConfigManager().getEnchantsConfig();
        config.set("enchants", null);
        
        ConfigurationSection enchantsSection = config.createSection("enchants");
        for (SwordEnchant enchant : enchants.values()) {
            ConfigurationSection enchantSection = enchantsSection.createSection(enchant.getId());
            enchant.saveToConfig(enchantSection);
        }
        
        plugin.getConfigManager().saveEnchantsConfig();
    }
}
