package com.bird.flysword.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bird.flysword.Flysword;

/**
 * 飛劍顯示系統測試工具
 */
public class SwordDisplayTestUtils implements CommandExecutor {

    private final Flysword plugin;

    public SwordDisplayTestUtils(Flysword plugin) {
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
                showDisplayInfo(player);
                break;
            case "test":
                testDisplay(player);
                break;
            case "presets":
                testPresets(player);
                break;
            case "reset":
                resetToDefaults(player);
                break;
            case "cycle":
                cycleRotations(player);
                break;
            default:
                showUsage(player);
                break;
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§6=== 飛劍顯示系統測試 ===");
        player.sendMessage("§e/sworddisplaytest info - 顯示當前設定");
        player.sendMessage("§e/sworddisplaytest test - 測試當前設定");
        player.sendMessage("§e/sworddisplaytest presets - 測試所有預設角度");
        player.sendMessage("§e/sworddisplaytest cycle - 循環測試不同角度");
        player.sendMessage("§e/sworddisplaytest reset - 重置所有設定");
        player.sendMessage("§7");
        player.sendMessage("§7新功能說明:");
        player.sendMessage("§7• 盔甲架現在固定在玩家腳下");
        player.sendMessage("§7• 飛劍戴在盔甲架頭上而不是拿著");
        player.sendMessage("§7• 支持三軸角度調整");
    }

    private void showDisplayInfo(Player player) {
        player.sendMessage("§6=== 飛劍顯示系統狀態 ===");
        
        // 位置信息
        double x = plugin.getConfig().getDouble("flight.sword_offset.x", 0.0);
        double y = plugin.getConfig().getDouble("flight.sword_offset.y", -2.0);
        double z = plugin.getConfig().getDouble("flight.sword_offset.z", 0.0);
        
        player.sendMessage("§e位置偏移:");
        player.sendMessage("  §7X: §a" + x + " §7(左右)");
        player.sendMessage("  §7Y: §a" + y + " §7(上下)");
        player.sendMessage("  §7Z: §a" + z + " §7(前後)");
        
        // 角度信息
        double rotX = plugin.getConfig().getDouble("flight.sword_rotation.x", 0);
        double rotY = plugin.getConfig().getDouble("flight.sword_rotation.y", 0);
        double rotZ = plugin.getConfig().getDouble("flight.sword_rotation.z", 0);
        
        player.sendMessage("§e角度設定:");
        player.sendMessage("  §7X: §a" + rotX + "° §7(俯仰)");
        player.sendMessage("  §7Y: §a" + rotY + "° §7(偏航)");
        player.sendMessage("  §7Z: §a" + rotZ + "° §7(翻滾)");
        
        // 飛行狀態
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§a您正在飛行中 - 可以直接觀察效果！");
        } else {
            player.sendMessage("§7使用 /sworddisplaytest test 開始測試");
        }
    }

    private void testDisplay(Player player) {
        player.sendMessage("§6=== 開始顯示測試 ===");
        
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§c請先停止飛行再進行測試！");
            return;
        }
        
        // 啟動測試飛行
        if (plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§a飛行已啟動！");
            player.sendMessage("§e觀察飛劍顯示效果:");
            player.sendMessage("§7• 盔甲架應該在您腳下");
            player.sendMessage("§7• 飛劍應該戴在盔甲架頭上");
            player.sendMessage("§7• 角度應該符合配置設定");
            
            // 15秒後自動停止
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getFlightController().isFlying(player.getUniqueId())) {
                    plugin.getFlightController().stopFlight(player);
                    player.sendMessage("§e顯示測試已自動結束");
                }
            }, 300L);
            
        } else {
            player.sendMessage("§c無法啟動飛行測試！請檢查條件。");
        }
    }

    private void testPresets(Player player) {
        player.sendMessage("§6=== 測試預設角度 ===");
        player.sendMessage("§e將循環測試所有預設角度，每個持續5秒");
        
        String[] presets = {"horizontal", "vertical", "diagonal", "upward", "downward"};
        String[] presetNames = {"水平", "垂直", "對角", "向上", "向下"};
        
        for (int i = 0; i < presets.length; i++) {
            final int index = i;
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                applyPresetQuickly(presets[index]);
                player.sendMessage("§e正在測試: §a" + presetNames[index] + " 飛劍");
                
                // 如果沒有在飛行，啟動飛行
                if (!plugin.getFlightController().isFlying(player.getUniqueId())) {
                    plugin.getFlightController().startFlight(player);
                }
            }, i * 100L); // 每個預設間隔5秒
        }
        
        // 測試結束後停止飛行
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getFlightController().isFlying(player.getUniqueId())) {
                plugin.getFlightController().stopFlight(player);
            }
            player.sendMessage("§a預設角度測試完成！");
        }, presets.length * 100L + 20L);
    }

    private void cycleRotations(Player player) {
        player.sendMessage("§6=== 循環角度測試 ===");
        
        if (!plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§c無法啟動飛行！");
            return;
        }
        
        player.sendMessage("§e開始循環角度測試，每2秒變換一次");
        
        // 循環不同角度
        for (int i = 0; i < 8; i++) {
            final int step = i;
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                double angle = step * 45; // 0, 45, 90, 135, 180, 225, 270, 315
                plugin.getConfig().set("flight.sword_rotation.y", angle);
                player.sendMessage("§e角度: §a" + angle + "°");
            }, i * 40L); // 每2秒一次
        }
        
        // 測試結束
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getFlightController().stopFlight(player);
            plugin.getConfig().set("flight.sword_rotation.y", 0.0);
            player.sendMessage("§a循環角度測試完成！");
        }, 320L + 40L);
    }

    private void resetToDefaults(Player player) {
        // 重置位置
        plugin.getConfig().set("flight.sword_offset.x", 0.0);
        plugin.getConfig().set("flight.sword_offset.y", -2.0);
        plugin.getConfig().set("flight.sword_offset.z", 0.0);
        
        // 重置角度
        plugin.getConfig().set("flight.sword_rotation.x", 0.0);
        plugin.getConfig().set("flight.sword_rotation.y", 0.0);
        plugin.getConfig().set("flight.sword_rotation.z", 0.0);
        
        player.sendMessage("§a已重置所有顯示設定為預設值！");
        player.sendMessage("§7位置: 玩家腳下 (0, -2, 0)");
        player.sendMessage("§7角度: 預設 (0°, 0°, 0°)");
        
        showDisplayInfo(player);
    }

    private void applyPresetQuickly(String preset) {
        double x = 0, y = 0, z = 0;

        switch (preset.toLowerCase()) {
            case "horizontal":
                x = 0; y = 0; z = 90;
                break;
            case "vertical":
                x = 90; y = 0; z = 0;
                break;
            case "diagonal":
                x = 45; y = 45; z = 0;
                break;
            case "upward":
                x = -45; y = 0; z = 0;
                break;
            case "downward":
                x = 45; y = 0; z = 0;
                break;
        }

        plugin.getConfig().set("flight.sword_rotation.x", x);
        plugin.getConfig().set("flight.sword_rotation.y", y);
        plugin.getConfig().set("flight.sword_rotation.z", z);
    }
}
