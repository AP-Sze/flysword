package com.bird.flysword.managers;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;
import com.bird.flysword.flight.FlightSession;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlightManager {
    
    private final Flysword plugin;
    private final Map<UUID, FlightSession> activeFlights;
    
    public FlightManager(Flysword plugin) {
        this.plugin = plugin;
        this.activeFlights = new HashMap<>();
    }
    
    public boolean startFlight(Player player) {
        if (isFlying(player)) {
            return false;
        }
        
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        if (playerData.getDurability() <= 0) {
            player.sendMessage("§c飛劍耐久度不足，無法啟動飛行！");
            return false;
        }
        
        FlightSession session = new FlightSession(plugin, player);
        activeFlights.put(player.getUniqueId(), session);
        playerData.setFlying(true);
        
        player.sendMessage("§a飛劍模式已啟動！");
        return true;
    }
    
    public boolean stopFlight(Player player) {
        FlightSession session = activeFlights.remove(player.getUniqueId());
        if (session != null) {
            session.stop();
            PlayerData playerData = plugin.getDataManager().getPlayerData(player);
            playerData.setFlying(false);
            player.sendMessage("§c飛劍模式已關閉！");
            return true;
        }
        return false;
    }
    
    public boolean isFlying(Player player) {
        return activeFlights.containsKey(player.getUniqueId());
    }
    
    public FlightSession getFlightSession(Player player) {
        return activeFlights.get(player.getUniqueId());
    }
    
    public void stopAllFlights() {
        for (FlightSession session : activeFlights.values()) {
            session.stop();
        }
        activeFlights.clear();
    }
    
    public void updateFlight(Player player) {
        FlightSession session = activeFlights.get(player.getUniqueId());
        if (session != null) {
            session.update();
        }
    }
    
    public void handlePlayerQuit(Player player) {
        stopFlight(player);
    }
    
    public void handlePlayerDeath(Player player) {
        stopFlight(player);
    }
}
