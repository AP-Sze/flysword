package com.bird.flysword.managers;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    
    private final Flysword plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    private final File dataFolder;
    
    public DataManager(Flysword plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    public void loadData() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String uuidString = file.getName().replace(".yml", "");
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    PlayerData playerData = PlayerData.fromConfig(config);
                    playerDataMap.put(uuid, playerData);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("無效的UUID文件名: " + file.getName());
                }
            }
        }
        plugin.getLogger().info("已加載 " + playerDataMap.size() + " 個玩家數據");
    }
    
    public void saveData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            savePlayerData(entry.getKey(), entry.getValue());
        }
        plugin.getLogger().info("已保存 " + playerDataMap.size() + " 個玩家數據");
    }
    
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(uuid));
    }
    
    public void savePlayerData(UUID uuid, PlayerData playerData) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        playerData.saveToConfig(config);
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("無法保存玩家數據 " + uuid + ": " + e.getMessage());
        }
    }
    
    public void savePlayerData(Player player) {
        PlayerData playerData = getPlayerData(player);
        savePlayerData(player.getUniqueId(), playerData);
    }
    
    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }
    
    public boolean hasPlayerData(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }
}
