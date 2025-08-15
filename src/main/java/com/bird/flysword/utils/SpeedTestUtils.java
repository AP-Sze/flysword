package com.bird.flysword.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bird.flysword.Flysword;

/**
 * 飛行速度測試工具
 */
public class SpeedTestUtils implements CommandExecutor {

    private final Flysword plugin;

    public SpeedTestUtils(Flysword plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c這個指令只能由玩家執行！");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                showSpeedInfo(player);
                break;
            case "test":
                testSpeed(player);
                break;
            case "config":
                showConfigValues(player);
                break;
            case "reload":
                reloadConfig(player);
                break;
            default:
                showUsage(player);
                break;
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§6=== 飛行速度測試指令 ===");
        player.sendMessage("§e/speedtest info - 顯示當前飛行狀態");
        player.sendMessage("§e/speedtest test - 測試飛行速度");
        player.sendMessage("§e/speedtest config - 顯示配置數值");
        player.sendMessage("§e/speedtest reload - 重新載入配置");
    }

    private void showSpeedInfo(Player player) {
        if (!plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§c您目前沒有在飛行！");
            return;
        }

        player.sendMessage("§6=== 飛行速度信息 ===");
        player.sendMessage("§e飛行狀態: §a飛行中");
        
        // 獲取當前速度信息（需要在 FlightController 中添加 getter 方法）
        player.sendMessage("§e當前位置: §7" + formatLocation(player.getLocation()));
        player.sendMessage("§e當前速度向量: §7" + formatVector(player.getVelocity()));
        player.sendMessage("§e速度大小: §a" + String.format("%.2f", player.getVelocity().length()));
    }

    private void testSpeed(Player player) {
        player.sendMessage("§6=== 開始速度測試 ===");
        player.sendMessage("§e請開始飛行，觀察速度變化...");
        
        if (!plugin.getFlightController().isFlying(player.getUniqueId())) {
            // 嘗試啟動飛行
            if (plugin.getFlightController().startFlight(player)) {
                player.sendMessage("§a飛行已啟動！");
            } else {
                player.sendMessage("§c無法啟動飛行！請檢查條件。");
                return;
            }
        }
        
        // 定期報告速度（使用調度器）
        plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int count = 0;
            
            @Override
            public void run() {
                if (!plugin.getFlightController().isFlying(player.getUniqueId()) || count >= 10) {
                    player.sendMessage("§6速度測試結束！");
                    return;
                }
                
                double speed = player.getVelocity().length();
                player.sendMessage("§e第 " + (count + 1) + " 秒速度: §a" + String.format("%.3f", speed));
                count++;
            }
        }, 0L, 20L); // 每秒執行一次
    }

    private void showConfigValues(Player player) {
        player.sendMessage("§6=== 飛行速度配置 ===");
        player.sendMessage("§e基礎速度: §a" + plugin.getConfig().getDouble("flight.speed.base_speed"));
        player.sendMessage("§e最大速度: §a" + plugin.getConfig().getDouble("flight.speed.max_speed"));
        player.sendMessage("§e加速度: §a" + plugin.getConfig().getDouble("flight.speed.acceleration"));
        player.sendMessage("§e減速度: §a" + plugin.getConfig().getDouble("flight.speed.deceleration"));
        player.sendMessage("§e冷卻時間: §a" + plugin.getConfig().getLong("flight.cooldown_time") + "ms");
    }

    private void reloadConfig(Player player) {
        plugin.reloadConfig();
        player.sendMessage("§a配置文件已重新載入！");
        player.sendMessage("§e注意: 速度更改可能需要重啟飛行才能生效。");
    }

    private String formatLocation(org.bukkit.Location loc) {
        return String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ());
    }

    private String formatVector(org.bukkit.util.Vector vector) {
        return String.format("(%.3f, %.3f, %.3f)", vector.getX(), vector.getY(), vector.getZ());
    }
}
