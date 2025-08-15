package com.bird.flysword.utils;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.SwordSkin;

/**
 * 飛行系統調試工具
 */
public class FlightTestUtils implements CommandExecutor {
    
    private final Flysword plugin;
    private final NamespacedKey skinKey;
    
    public FlightTestUtils(Flysword plugin) {
        this.plugin = plugin;
        this.skinKey = new NamespacedKey(plugin, "flysword_skin");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "give":
                giveFlyingSword(player);
                break;
            case "check":
                checkItem(player);
                break;
            case "flight":
                testFlight(player);
                break;
            case "info":
                showFlightInfo(player);
                break;
            case "durability":
                if (args.length > 1) {
                    setDurability(player, args[1]);
                } else {
                    showDurability(player);
                }
                break;
            case "repair":
                repairSword(player);
                break;
            case "height":
                if (args.length > 1) {
                    teleportToHeight(player, args[1]);
                } else {
                    showHeightInfo(player);
                }
                break;
            case "testlimits":
                testHeightLimits(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6=== FlySword 測試工具 ===");
        player.sendMessage("§e/flysword-test give - 給予飛劍");
        player.sendMessage("§e/flysword-test check - 檢查手中物品");
        player.sendMessage("§e/flysword-test flight - 測試飛行");
        player.sendMessage("§e/flysword-test info - 顯示飛行狀態");
        player.sendMessage("§e/flysword-test durability [數值] - 設置/查看耐久度");
        player.sendMessage("§e/flysword-test repair - 完全修復飛劍");
        player.sendMessage("§e/flysword-test height [高度] - 傳送到指定高度/顯示高度信息");
        player.sendMessage("§e/flysword-test testlimits - 測試高度限制");
    }
    
    private void giveFlyingSword(Player player) {
        try {
            // 取得預設飛劍皮膚
            SwordSkin defaultSkin = plugin.getSkinManager().getSkin("default");
            if (defaultSkin == null) {
                player.sendMessage("§c錯誤：找不到預設飛劍皮膚！");
                return;
            }
            
            ItemStack sword = defaultSkin.createItemStack();
            player.getInventory().addItem(sword);
            player.sendMessage("§a已給予飛劍！");
            player.sendMessage("§7皮膚：" + defaultSkin.getId());
            player.sendMessage("§7模型ID：" + defaultSkin.getCustomModelData());
        } catch (Exception e) {
            player.sendMessage("§c生成飛劍時出錯：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void checkItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§c請手持物品！");
            return;
        }
        
        player.sendMessage("§6=== 物品信息 ===");
        player.sendMessage("§e類型：§f" + item.getType());
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            player.sendMessage("§e顯示名稱：§f" + 
                (meta.hasDisplayName() ? meta.getDisplayName() : "無"));
            
            // 檢查皮膚標記
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(skinKey, PersistentDataType.STRING)) {
                String skinId = container.get(skinKey, PersistentDataType.STRING);
                player.sendMessage("§e飛劍皮膚：§a" + skinId);
                player.sendMessage("§a✓ 這是飛劍！");
            } else {
                player.sendMessage("§c✗ 這不是飛劍！");
            }
        } else {
            player.sendMessage("§c物品沒有元數據！");
        }
    }
    
    private void testFlight(Player player) {
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            plugin.getFlightController().stopFlight(player);
            player.sendMessage("§c停止飛行！");
        } else {
            plugin.getFlightController().startFlight(player);
            player.sendMessage("§a開始飛行！");
        }
    }
    
    private void showFlightInfo(Player player) {
        player.sendMessage("§6=== 飛行狀態 ===");
        boolean isFlying = plugin.getFlightController().isFlying(player.getUniqueId());
        player.sendMessage("§e飛行狀態：" + (isFlying ? "§a飛行中" : "§c未飛行"));
        
        if (isFlying) {
            try {
                // 這裡可以添加更多飛行狀態信息
                player.sendMessage("§e可以正常使用右鍵停止飛行");
            } catch (Exception e) {
                player.sendMessage("§c獲取飛行詳情時出錯：" + e.getMessage());
            }
        }
    }
    
    private void showDurability(Player player) {
        var playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            int durability = playerData.getDurability();
            String color = durability > 60 ? "§a" : durability > 30 ? "§e" : "§c";
            player.sendMessage("§6=== 耐久度信息 ===");
            player.sendMessage("§e當前耐久度：" + color + durability + "%");
            player.sendMessage("§7狀態：" + (durability > 0 ? "§a可飛行" : "§c無法飛行"));
        } else {
            player.sendMessage("§c無法獲取玩家數據！");
        }
    }
    
    private void setDurability(Player player, String value) {
        try {
            int durability = Integer.parseInt(value);
            if (durability < 0 || durability > 100) {
                player.sendMessage("§c耐久度必須在 0-100 之間！");
                return;
            }
            
            var playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
            if (playerData != null) {
                playerData.setDurability(durability);
                plugin.getDataManager().savePlayerData(player.getUniqueId(), playerData);
                
                String color = durability > 60 ? "§a" : durability > 30 ? "§e" : "§c";
                player.sendMessage("§a已設置耐久度為 " + color + durability + "%");
            } else {
                player.sendMessage("§c無法獲取玩家數據！");
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§c請輸入有效的數字！");
        }
    }
    
    private void repairSword(Player player) {
        var playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.setDurability(100);
            plugin.getDataManager().savePlayerData(player.getUniqueId(), playerData);
            player.sendMessage("§a§l✨ 飛劍已完全修復！");
            player.sendMessage("§7耐久度：§a100%");
        } else {
            player.sendMessage("§c無法獲取玩家數據！");
        }
    }
    
    private void showHeightInfo(Player player) {
        double maxHeight = plugin.getConfig().getDouble("flight.max_height", 256);
        double minHeight = plugin.getConfig().getDouble("flight.min_height", 0);
        long forceLandingDelay = plugin.getConfig().getLong("flight.force_landing_delay", 10000);
        double currentHeight = player.getLocation().getY();
        
        player.sendMessage("§6=== 高度限制信息 ===");
        player.sendMessage("§e當前高度：§f" + String.format("%.2f", currentHeight));
        player.sendMessage("§e最大高度：§f" + maxHeight);
        player.sendMessage("§e最小高度：§f" + minHeight);
        player.sendMessage("§e強制降落延遲：§f" + (forceLandingDelay / 1000) + " 秒");
        
        if (currentHeight >= maxHeight) {
            player.sendMessage("§c⚠ 您已達到或超過最大高度限制！");
            player.sendMessage("§e超過限制時將在 " + (forceLandingDelay / 1000) + " 秒後強制降落");
        } else if (currentHeight <= minHeight) {
            player.sendMessage("§c⚠ 您已達到或低於最小高度限制！");
        } else {
            player.sendMessage("§a✓ 您在安全飛行高度範圍內");
        }
    }
    
    private void teleportToHeight(Player player, String heightStr) {
        try {
            double height = Double.parseDouble(heightStr);
            Location loc = player.getLocation().clone();
            loc.setY(height);
            player.teleport(loc);
            player.sendMessage("§a已傳送到高度 " + height);
            showHeightInfo(player);
        } catch (NumberFormatException e) {
            player.sendMessage("§c請輸入有效的高度數值！");
        }
    }
    
    private void testHeightLimits(Player player) {
        double maxHeight = plugin.getConfig().getDouble("flight.max_height", 256);
        
        player.sendMessage("§6=== 開始測試高度限制 ===");
        
        // 測試最大高度
        Location testLoc = player.getLocation().clone();
        testLoc.setY(maxHeight + 1);
        player.teleport(testLoc);
        player.sendMessage("§e1. 已傳送到最大高度+1 (" + (maxHeight + 1) + ")");
        player.sendMessage("§7請嘗試啟動飛行，應該會在10秒後強制降落");
        
        // 給予飛劍用於測試
        ItemStack sword = plugin.getSkinManager().createSwordWithSkin("default");
        player.getInventory().addItem(sword);
        player.sendMessage("§a已給予測試飛劍，請右鍵測試！");
        player.sendMessage("§e注意觀察倒計時警告訊息");
    }
}
