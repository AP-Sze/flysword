package com.bird.flysword.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bird.flysword.Flysword;

/**
 * 飛劍位置調整指令
 * 用於管理員調整飛劍相對玩家的位置
 */
public class SwordPositionCommand implements CommandExecutor {

    private final Flysword plugin;

    public SwordPositionCommand(Flysword plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c這個指令只能由玩家執行！");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("flysword.admin")) {
            player.sendMessage("§c您沒有權限使用這個指令！");
            return true;
        }

        if (args.length == 0) {
            showUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "show":
                showCurrentOffset(player);
                break;
            case "set":
                if (args.length != 4) {
                    player.sendMessage("§c用法: /swordpos set <X> <Y> <Z>");
                    return true;
                }
                setSwordOffset(player, args);
                break;
            case "reset":
                resetToDefault(player);
                break;
            case "test":
                testPosition(player);
                break;
            case "save":
                saveToConfig(player);
                break;
            case "reload":
                reloadFromConfig(player);
                break;
            default:
                showUsage(player);
                break;
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§6=== 飛劍位置調整指令 ===");
        player.sendMessage("§e/swordpos show - 顯示當前位置偏移");
        player.sendMessage("§e/swordpos set <X> <Y> <Z> - 設定位置偏移");
        player.sendMessage("§e/swordpos reset - 重置為預設值");
        player.sendMessage("§e/swordpos test - 測試當前位置");
        player.sendMessage("§e/swordpos save - 儲存到配置文件");
        player.sendMessage("§e/swordpos reload - 從配置文件重新載入");
        player.sendMessage("§7");
        player.sendMessage("§7位置說明:");
        player.sendMessage("§7X: 左右偏移 (負值=左, 正值=右)");
        player.sendMessage("§7Y: 上下偏移 (負值=下, 正值=上)");
        player.sendMessage("§7Z: 前後偏移 (負值=後, 正值=前)");
    }

    private void showCurrentOffset(Player player) {
        double x = plugin.getConfig().getDouble("flight.sword_offset.x", 0.8);
        double y = plugin.getConfig().getDouble("flight.sword_offset.y", -1.5);
        double z = plugin.getConfig().getDouble("flight.sword_offset.z", 0.0);

        player.sendMessage("§6=== 當前飛劍位置偏移 ===");
        player.sendMessage("§eX (左右): §a" + x);
        player.sendMessage("§eY (上下): §a" + y);
        player.sendMessage("§eZ (前後): §a" + z);
        
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§7提示: 您正在飛行中，可以直接觀察當前位置效果");
        } else {
            player.sendMessage("§7提示: 使用 /swordpos test 來測試位置效果");
        }
    }

    private void setSwordOffset(Player player, String[] args) {
        try {
            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);

            // 檢查合理範圍
            if (Math.abs(x) > 5 || Math.abs(y) > 5 || Math.abs(z) > 5) {
                player.sendMessage("§c警告: 偏移值過大可能造成顯示問題！建議範圍: -5 到 5");
            }

            // 暫時設定到記憶中（不儲存到文件）
            plugin.getConfig().set("flight.sword_offset.x", x);
            plugin.getConfig().set("flight.sword_offset.y", y);
            plugin.getConfig().set("flight.sword_offset.z", z);

            player.sendMessage("§a已設定飛劍位置偏移:");
            player.sendMessage("§eX: §a" + x + " §7(左右)");
            player.sendMessage("§eY: §a" + y + " §7(上下)");
            player.sendMessage("§eZ: §a" + z + " §7(前後)");
            player.sendMessage("§7");
            player.sendMessage("§7使用 §e/swordpos test §7來測試效果");
            player.sendMessage("§7測試滿意後使用 §e/swordpos save §7儲存到配置文件");

        } catch (NumberFormatException e) {
            player.sendMessage("§c請輸入有效的數字！");
        }
    }

    private void resetToDefault(Player player) {
        plugin.getConfig().set("flight.sword_offset.x", 0.8);
        plugin.getConfig().set("flight.sword_offset.y", -1.5);
        plugin.getConfig().set("flight.sword_offset.z", 0.0);

        player.sendMessage("§a已重置飛劍位置為預設值!");
        showCurrentOffset(player);
    }

    private void testPosition(Player player) {
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§c請先停止飛行再進行測試！");
            return;
        }

        // 嘗試啟動飛行來測試位置
        if (plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§a已啟動測試飛行！");
            player.sendMessage("§e觀察飛劍位置是否合適");
            player.sendMessage("§7右鍵停止飛行，或等待自動停止");
            
            // 10秒後自動停止測試
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getFlightController().isFlying(player.getUniqueId())) {
                    plugin.getFlightController().stopFlight(player);
                    player.sendMessage("§e測試飛行已自動停止");
                }
            }, 200L); // 10秒 = 200 ticks
            
        } else {
            player.sendMessage("§c無法啟動測試飛行！請檢查條件。");
        }
    }

    private void saveToConfig(Player player) {
        try {
            plugin.saveConfig();
            player.sendMessage("§a飛劍位置設定已儲存到配置文件！");
            player.sendMessage("§7配置將在下次伺服器重啟時生效");
        } catch (Exception e) {
            player.sendMessage("§c儲存配置時發生錯誤: " + e.getMessage());
        }
    }

    private void reloadFromConfig(Player player) {
        try {
            plugin.reloadConfig();
            player.sendMessage("§a已從配置文件重新載入飛劍位置設定！");
            showCurrentOffset(player);
        } catch (Exception e) {
            player.sendMessage("§c重新載入配置時發生錯誤: " + e.getMessage());
        }
    }
}
