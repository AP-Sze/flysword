package com.bird.flysword.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bird.flysword.Flysword;

/**
 * 飛劍角度調整指令
 * 用於管理員調整飛劍的顯示角度
 */
public class SwordRotationCommand implements CommandExecutor {

    private final Flysword plugin;

    public SwordRotationCommand(Flysword plugin) {
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
                showCurrentRotation(player);
                break;
            case "set":
                if (args.length != 4) {
                    player.sendMessage("§c用法: /swordrotation set <X> <Y> <Z>");
                    return true;
                }
                setRotation(player, args);
                break;
            case "reset":
                resetToDefault(player);
                break;
            case "test":
                testRotation(player);
                break;
            case "save":
                saveToConfig(player);
                break;
            case "reload":
                reloadFromConfig(player);
                break;
            case "preset":
                if (args.length < 2) {
                    showPresets(player);
                } else {
                    applyPreset(player, args[1]);
                }
                break;
            default:
                showUsage(player);
                break;
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§6=== 飛劍角度調整指令 ===");
        player.sendMessage("§e/swordrotation show - 顯示當前角度");
        player.sendMessage("§e/swordrotation set <X> <Y> <Z> - 設定角度 (度數)");
        player.sendMessage("§e/swordrotation preset [預設名] - 使用預設角度");
        player.sendMessage("§e/swordrotation reset - 重置為預設值");
        player.sendMessage("§e/swordrotation test - 測試當前角度");
        player.sendMessage("§e/swordrotation save - 儲存到配置文件");
        player.sendMessage("§e/swordrotation reload - 從配置文件重新載入");
        player.sendMessage("§7");
        player.sendMessage("§7角度說明:");
        player.sendMessage("§7X: 俯仰角 (向上/向下傾斜)");
        player.sendMessage("§7Y: 偏航角 (左右旋轉)");
        player.sendMessage("§7Z: 翻滾角 (左右傾斜)");
    }

    private void showCurrentRotation(Player player) {
        double x = plugin.getConfig().getDouble("flight.sword_rotation.x", 0);
        double y = plugin.getConfig().getDouble("flight.sword_rotation.y", 0);
        double z = plugin.getConfig().getDouble("flight.sword_rotation.z", 0);

        player.sendMessage("§6=== 當前飛劍角度 ===");
        player.sendMessage("§eX (俯仰): §a" + x + "°");
        player.sendMessage("§eY (偏航): §a" + y + "°");
        player.sendMessage("§eZ (翻滾): §a" + z + "°");
        
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§7提示: 您正在飛行中，可以直接觀察當前角度效果");
        } else {
            player.sendMessage("§7提示: 使用 /swordrotation test 來測試角度效果");
        }
    }

    private void setRotation(Player player, String[] args) {
        try {
            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);

            // 將角度規範化到 -180 到 180 度範圍
            x = normalizeAngle(x);
            y = normalizeAngle(y);
            z = normalizeAngle(z);

            // 暫時設定到記憶中（不儲存到文件）
            plugin.getConfig().set("flight.sword_rotation.x", x);
            plugin.getConfig().set("flight.sword_rotation.y", y);
            plugin.getConfig().set("flight.sword_rotation.z", z);

            player.sendMessage("§a已設定飛劍角度:");
            player.sendMessage("§eX (俯仰): §a" + x + "° §7(向上/向下傾斜)");
            player.sendMessage("§eY (偏航): §a" + y + "° §7(左右旋轉)");
            player.sendMessage("§eZ (翻滾): §a" + z + "° §7(左右傾斜)");
            player.sendMessage("§7");
            player.sendMessage("§7使用 §e/swordrotation test §7來測試效果");
            player.sendMessage("§7測試滿意後使用 §e/swordrotation save §7儲存到配置文件");

        } catch (NumberFormatException e) {
            player.sendMessage("§c請輸入有效的數字！");
        }
    }

    private void showPresets(Player player) {
        player.sendMessage("§6=== 飛劍角度預設 ===");
        player.sendMessage("§e/swordrotation preset horizontal - 水平飛劍");
        player.sendMessage("§e/swordrotation preset vertical - 垂直飛劍");
        player.sendMessage("§e/swordrotation preset diagonal - 對角飛劍");
        player.sendMessage("§e/swordrotation preset spinning - 旋轉飛劍");
        player.sendMessage("§e/swordrotation preset upward - 向上飛劍");
        player.sendMessage("§e/swordrotation preset downward - 向下飛劍");
    }

    private void applyPreset(Player player, String preset) {
        double x = 0, y = 0, z = 0;
        String presetName = "";

        switch (preset.toLowerCase()) {
            case "horizontal":
                x = 0; y = 0; z = 90;
                presetName = "水平飛劍";
                break;
            case "vertical":
                x = 90; y = 0; z = 0;
                presetName = "垂直飛劍";
                break;
            case "diagonal":
                x = 45; y = 45; z = 0;
                presetName = "對角飛劍";
                break;
            case "spinning":
                x = 0; y = 0; z = 45;
                presetName = "旋轉飛劍";
                break;
            case "upward":
                x = -45; y = 0; z = 0;
                presetName = "向上飛劍";
                break;
            case "downward":
                x = 45; y = 0; z = 0;
                presetName = "向下飛劍";
                break;
            default:
                showPresets(player);
                return;
        }

        plugin.getConfig().set("flight.sword_rotation.x", x);
        plugin.getConfig().set("flight.sword_rotation.y", y);
        plugin.getConfig().set("flight.sword_rotation.z", z);

        player.sendMessage("§a已套用預設: §e" + presetName);
        player.sendMessage("§7角度: X=" + x + "°, Y=" + y + "°, Z=" + z + "°");
        player.sendMessage("§7使用 §e/swordrotation test §7來測試效果");
    }

    private void resetToDefault(Player player) {
        plugin.getConfig().set("flight.sword_rotation.x", 0.0);
        plugin.getConfig().set("flight.sword_rotation.y", 0.0);
        plugin.getConfig().set("flight.sword_rotation.z", 0.0);

        player.sendMessage("§a已重置飛劍角度為預設值!");
        showCurrentRotation(player);
    }

    private void testRotation(Player player) {
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§c請先停止飛行再進行測試！");
            return;
        }

        // 嘗試啟動飛行來測試角度
        if (plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§a已啟動測試飛行！");
            player.sendMessage("§e觀察飛劍角度是否合適");
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
            player.sendMessage("§a飛劍角度設定已儲存到配置文件！");
            player.sendMessage("§7配置將在下次伺服器重啟時生效");
        } catch (Exception e) {
            player.sendMessage("§c儲存配置時發生錯誤: " + e.getMessage());
        }
    }

    private void reloadFromConfig(Player player) {
        try {
            plugin.reloadConfig();
            player.sendMessage("§a已從配置文件重新載入飛劍角度設定！");
            showCurrentRotation(player);
        } catch (Exception e) {
            player.sendMessage("§c重新載入配置時發生錯誤: " + e.getMessage());
        }
    }

    private double normalizeAngle(double angle) {
        angle = angle % 360;
        if (angle > 180) {
            angle -= 360;
        } else if (angle < -180) {
            angle += 360;
        }
        return angle;
    }
}
