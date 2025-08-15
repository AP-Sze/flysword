package com.bird.flysword.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bird.flysword.Flysword;

/**
 * Shift 降落功能測試工具
 */
public class ShiftLandingTestUtils implements CommandExecutor {

    private final Flysword plugin;

    public ShiftLandingTestUtils(Flysword plugin) {
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
                showShiftLandingInfo(player);
                break;
            case "test":
                testShiftLanding(player);
                break;
            case "toggle":
                toggleShiftLanding(player);
                break;
            case "setdelay":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /shiftlandingtest setdelay <秒數>");
                    return true;
                }
                try {
                    int seconds = Integer.parseInt(args[1]);
                    setDelay(player, seconds);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c請輸入有效的數字！");
                }
                break;
            default:
                showUsage(player);
                break;
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§6=== Shift 降落測試指令 ===");
        player.sendMessage("§e/shiftlandingtest info - 顯示功能狀態");
        player.sendMessage("§e/shiftlandingtest test - 開始飛行測試");
        player.sendMessage("§e/shiftlandingtest toggle - 切換功能開關");
        player.sendMessage("§e/shiftlandingtest setdelay <秒> - 設定延遲時間");
        player.sendMessage("§7");
        player.sendMessage("§7使用方式:");
        player.sendMessage("§71. 啟動飛行");
        player.sendMessage("§72. 長按 Shift 鍵 (預設10秒)");
        player.sendMessage("§73. 觀察倒計時並測試強制降落");
    }

    private void showShiftLandingInfo(Player player) {
        boolean enabled = plugin.getConfig().getBoolean("flight.shift_landing.enabled", true);
        long delay = plugin.getConfig().getLong("flight.shift_landing.delay", 10000);
        boolean showCountdown = plugin.getConfig().getBoolean("flight.shift_landing.show_countdown", true);

        player.sendMessage("§6=== Shift 降落功能狀態 ===");
        player.sendMessage("§e功能啟用: " + (enabled ? "§a是" : "§c否"));
        player.sendMessage("§e延遲時間: §a" + (delay / 1000) + " 秒");
        player.sendMessage("§e顯示倒計時: " + (showCountdown ? "§a是" : "§c否"));
        
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§7");
            player.sendMessage("§a您正在飛行中，可以直接測試功能！");
            player.sendMessage("§7持續按住 Shift 鍵來觸發強制降落");
        } else {
            player.sendMessage("§7");
            player.sendMessage("§7使用 /shiftlandingtest test 開始飛行測試");
        }
    }

    private void testShiftLanding(Player player) {
        player.sendMessage("§6=== 開始 Shift 降落測試 ===");
        
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§c您已經在飛行中！");
            player.sendMessage("§e直接長按 Shift 鍵測試功能");
            return;
        }
        
        // 嘗試啟動飛行
        if (plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§a飛行已啟動！");
            player.sendMessage("§e現在請長按 Shift 鍵來測試強制降落功能");
            player.sendMessage("§7觀察螢幕上的倒計時訊息");
            
            // 提供測試提示
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getFlightController().isFlying(player.getUniqueId())) {
                    player.sendMessage("§e§l提示: 長按 Shift 鍵開始強制降落倒計時");
                }
            }, 40L); // 2秒後提示
        } else {
            player.sendMessage("§c無法啟動飛行！請檢查條件。");
        }
    }

    private void toggleShiftLanding(Player player) {
        boolean currentState = plugin.getConfig().getBoolean("flight.shift_landing.enabled", true);
        plugin.getConfig().set("flight.shift_landing.enabled", !currentState);
        
        String status = currentState ? "§c關閉" : "§a開啟";
        player.sendMessage("§a已" + status + " Shift 降落功能！");
        
        if (!currentState) {
            player.sendMessage("§7現在可以長按 Shift 鍵來觸發強制降落");
        } else {
            player.sendMessage("§7Shift 降落功能已停用");
        }
    }

    private void setDelay(Player player, int seconds) {
        if (seconds < 1 || seconds > 60) {
            player.sendMessage("§c延遲時間必須在 1-60 秒之間！");
            return;
        }
        
        long delayMs = seconds * 1000L;
        plugin.getConfig().set("flight.shift_landing.delay", delayMs);
        
        player.sendMessage("§a已設定 Shift 降落延遲為 " + seconds + " 秒！");
        player.sendMessage("§7現在需要長按 Shift " + seconds + " 秒來觸發強制降落");
        
        // 如果玩家正在飛行，提供即時測試機會
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§e您正在飛行中，可以立即測試新設定！");
        }
    }
}
