package com.bird.flysword.scheduler;

import com.bird.flysword.Flysword;
import com.bird.flysword.flight.FlightController;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlightScheduler {
    
    private final Flysword plugin;
    private final FlightController flightController;
    private final List<UUID> activePlayers;
    private int taskId;
    
    public FlightScheduler(Flysword plugin) {
        this.plugin = plugin;
        this.flightController = plugin.getFlightController();
        this.activePlayers = new ArrayList<>();
    }
    
    /**
     * 啟動飛行調度器
     */
    public void start() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllFlights();
            }
        }.runTaskTimer(plugin, 1L, 1L).getTaskId();
        
        plugin.getLogger().info("飛行調度器已啟動");
    }
    
    /**
     * 停止飛行調度器
     */
    public void stop() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        
        plugin.getLogger().info("飛行調度器已停止");
    }
    
    /**
     * 更新所有飛行
     */
    private void updateAllFlights() {
        // 獲取所有在線玩家
        List<Player> onlinePlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        
        // 批量處理飛行更新
        for (Player player : onlinePlayers) {
            if (flightController.isFlying(player.getUniqueId())) {
                try {
                    flightController.updateFlight(player);
                } catch (Exception e) {
                    plugin.getLogger().warning("更新玩家 " + player.getName() + " 的飛行時發生錯誤: " + e.getMessage());
                    // 發生錯誤時停止該玩家的飛行
                    flightController.stopFlight(player);
                }
            }
        }
        
        // 清理離線玩家的飛行狀態
        cleanupOfflinePlayers();
    }
    
    /**
     * 清理離線玩家的飛行狀態
     */
    private void cleanupOfflinePlayers() {
        List<UUID> toRemove = new ArrayList<>();
        
        for (UUID uuid : activePlayers) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                toRemove.add(uuid);
                flightController.handlePlayerQuit(uuid);
            }
        }
        
        activePlayers.removeAll(toRemove);
    }
    
    /**
     * 添加活躍玩家
     */
    public void addActivePlayer(UUID uuid) {
        if (!activePlayers.contains(uuid)) {
            activePlayers.add(uuid);
        }
    }
    
    /**
     * 移除活躍玩家
     */
    public void removeActivePlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }
    
    /**
     * 獲取活躍玩家數量
     */
    public int getActivePlayerCount() {
        return activePlayers.size();
    }
    
    /**
     * 獲取所有活躍玩家
     */
    public List<UUID> getActivePlayers() {
        return new ArrayList<>(activePlayers);
    }
}
