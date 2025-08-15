package com.bird.flysword.utils;

import com.bird.flysword.Flysword;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    
    private final Flysword plugin;
    private FileConfiguration config;
    private FileConfiguration skinsConfig;
    private FileConfiguration enchantsConfig;
    private FileConfiguration messagesConfig;
    
    public ConfigManager(Flysword plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        // 主配置文件
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // 皮膚配置文件
        File skinsFile = new File(plugin.getDataFolder(), "skins.yml");
        if (!skinsFile.exists()) {
            plugin.saveResource("skins.yml", false);
        }
        skinsConfig = YamlConfiguration.loadConfiguration(skinsFile);
        
        // 附魔配置文件
        File enchantsFile = new File(plugin.getDataFolder(), "enchants.yml");
        if (!enchantsFile.exists()) {
            plugin.saveResource("enchants.yml", false);
        }
        enchantsConfig = YamlConfiguration.loadConfiguration(enchantsFile);
        
        // 訊息配置文件
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        File skinsFile = new File(plugin.getDataFolder(), "skins.yml");
        skinsConfig = YamlConfiguration.loadConfiguration(skinsFile);
        
        File enchantsFile = new File(plugin.getDataFolder(), "enchants.yml");
        enchantsConfig = YamlConfiguration.loadConfiguration(enchantsFile);
        
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getSkinsConfig() {
        return skinsConfig;
    }
    
    public FileConfiguration getEnchantsConfig() {
        return enchantsConfig;
    }
    
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    public void saveSkinsConfig() {
        try {
            File skinsFile = new File(plugin.getDataFolder(), "skins.yml");
            skinsConfig.save(skinsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("無法保存皮膚配置文件: " + e.getMessage());
        }
    }
    
    public void saveEnchantsConfig() {
        try {
            File enchantsFile = new File(plugin.getDataFolder(), "enchants.yml");
            enchantsConfig.save(enchantsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("無法保存附魔配置文件: " + e.getMessage());
        }
    }
}
