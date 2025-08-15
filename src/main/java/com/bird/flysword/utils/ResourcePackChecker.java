package com.bird.flysword.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import com.bird.flysword.Flysword;

/**
 * 資源包檢查工具
 * 確保玩家使用正確的資源包來顯示飛劍模型
 */
public class ResourcePackChecker implements Listener {
    
    private final Flysword plugin;
    private final boolean checkRequired;
    private final String resourcePackUrl;
    private final String resourcePackHash;
    
    public ResourcePackChecker(Flysword plugin) {
        this.plugin = plugin;
        this.checkRequired = plugin.getConfig().getBoolean("skins.resource_pack.check_required", false);
        this.resourcePackUrl = plugin.getConfig().getString("skins.resource_pack.download_url", "");
        this.resourcePackHash = plugin.getConfig().getString("skins.resource_pack.hash", "");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 如果不需要檢查資源包，直接返回
        if (!checkRequired) {
            return;
        }
        
        // 延遲發送資源包請求，等玩家完全載入
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            sendResourcePackRequest(player);
        }, 20L); // 延遲1秒
    }
    
    /**
     * 發送資源包請求
     */
    private void sendResourcePackRequest(Player player) {
        if (resourcePackUrl.isEmpty()) {
            // 如果沒有設置資源包URL，只是提醒玩家
            player.sendMessage("§e§l⚠ 注意：請確保您使用了正確的飛劍資源包！");
            player.sendMessage("§7沒有資源包的話，飛劍模型可能無法正確顯示。");
            return;
        }
        
        try {
            if (resourcePackHash.isEmpty()) {
                // 沒有Hash驗證
                player.setResourcePack(resourcePackUrl);
            } else {
                // 有Hash驗證 - 需要將hex字符串轉換為byte數組
                byte[] hashBytes = hexStringToByteArray(resourcePackHash);
                player.setResourcePack(resourcePackUrl, hashBytes);
            }
            
            player.sendMessage("§a§l📦 正在發送飛劍資源包...");
            player.sendMessage("§7請接受資源包下載以正確顯示飛劍模型！");
            
        } catch (Exception e) {
            plugin.getLogger().warning("無法為玩家 " + player.getName() + " 發送資源包: " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        
        switch (status) {
            case SUCCESSFULLY_LOADED:
                player.sendMessage("§a§l✓ 飛劍資源包載入成功！");
                player.sendMessage("§7現在您可以看到所有飛劍模型了。");
                break;
                
            case DECLINED:
                player.sendMessage("§c§l✗ 您拒絕了資源包下載！");
                player.sendMessage("§7警告：沒有資源包，飛劍模型將無法正確顯示。");
                break;
                
            case FAILED_DOWNLOAD:
                player.sendMessage("§c§l✗ 資源包下載失敗！");
                player.sendMessage("§7請檢查網路連接或聯繫管理員。");
                break;
                
            case ACCEPTED:
                player.sendMessage("§e§l📥 正在下載資源包...");
                break;
                
            default:
                break;
        }
    }
    
    /**
     * 手動為玩家發送資源包
     */
    public void sendResourcePack(Player player) {
        if (player.hasPermission("flysword.admin")) {
            player.sendMessage("§e正在發送資源包...");
            sendResourcePackRequest(player);
        } else {
            player.sendMessage("§c您沒有權限執行此操作！");
        }
    }
    
    /**
     * 檢查玩家是否需要資源包
     */
    public boolean needsResourcePack() {
        return checkRequired && !resourcePackUrl.isEmpty();
    }
    
    /**
     * 獲取資源包信息
     */
    public String getResourcePackInfo() {
        StringBuilder info = new StringBuilder();
        info.append("§6=== 資源包信息 ===\n");
        info.append("§7檢查要求: ").append(checkRequired ? "§a啟用" : "§c停用").append("\n");
        info.append("§7下載連結: ").append(resourcePackUrl.isEmpty() ? "§c未設置" : "§a已設置").append("\n");
        info.append("§7SHA-1驗證: ").append(resourcePackHash.isEmpty() ? "§c未設置" : "§a已設置").append("\n");
        
        return info.toString();
    }
    
    /**
     * 將hex字符串轉換為byte數組
     */
    private byte[] hexStringToByteArray(String hex) {
        // 移除可能的空格和冒號
        hex = hex.replaceAll("[\\s:-]", "");
        
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
