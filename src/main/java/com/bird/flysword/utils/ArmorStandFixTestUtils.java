package com.bird.flysword.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.bird.flysword.Flysword;

/**
 * 盔甲座固定測試工具
 */
public class ArmorStandFixTestUtils implements CommandExecutor {

    private final Flysword plugin;

    public ArmorStandFixTestUtils(Flysword plugin) {
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
                showArmorStandInfo(player);
                break;
            case "test":
                testArmorStandFixed(player);
                break;
            case "stress":
                stressTestArmorStand(player);
                break;
            case "movement":
                testPlayerMovement(player);
                break;
            case "rotation":
                testRotationStability(player);
                break;
            default:
                showUsage(player);
                break;
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§6=== 盔甲座固定測試工具 ===");
        player.sendMessage("§e/armorstandtest info - 顯示盔甲座狀態");
        player.sendMessage("§e/armorstandtest test - 基礎固定測試");
        player.sendMessage("§e/armorstandtest stress - 壓力測試");
        player.sendMessage("§e/armorstandtest movement - 玩家移動測試");
        player.sendMessage("§e/armorstandtest rotation - 角度穩定性測試");
        player.sendMessage("§7");
        player.sendMessage("§7修復說明:");
        player.sendMessage("§7• 盔甲座現在設置了 AI=false");
        player.sendMessage("§7• 每tick重置速度為0防止移動");
        player.sendMessage("§7• 強制傳送確保位置正確");
    }

    private void showArmorStandInfo(Player player) {
        if (!plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§c您沒有在飛行中，無法檢查盔甲座！");
            return;
        }

        player.sendMessage("§6=== 盔甲座狀態檢查 ===");
        
        // 從配置顯示位置偏移
        double x = plugin.getConfig().getDouble("flight.sword_offset.x", 0.0);
        double y = plugin.getConfig().getDouble("flight.sword_offset.y", -2.0);
        double z = plugin.getConfig().getDouble("flight.sword_offset.z", 0.0);
        
        player.sendMessage("§e配置的位置偏移:");
        player.sendMessage("  §7X: §a" + x + " §7Y: §a" + y + " §7Z: §a" + z);
        
        // 顯示角度設定
        double rotX = plugin.getConfig().getDouble("flight.sword_rotation.x", 0);
        double rotY = plugin.getConfig().getDouble("flight.sword_rotation.y", 0);
        double rotZ = plugin.getConfig().getDouble("flight.sword_rotation.z", 0);
        
        player.sendMessage("§e當前角度設定:");
        player.sendMessage("  §7X: §a" + rotX + "° §7Y: §a" + rotY + "° §7Z: §a" + rotZ + "°");
        
        player.sendMessage("§a盔甲座應該固定在您的腳下 " + y + " 格處");
        player.sendMessage("§7如果還會亂跑，請使用其他測試指令");
    }

    private void testArmorStandFixed(Player player) {
        player.sendMessage("§6=== 盔甲座固定測試 ===");
        
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§a您已在飛行中，開始觀察盔甲座...");
        } else if (plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§a飛行已啟動，開始觀察盔甲座...");
        } else {
            player.sendMessage("§c無法啟動飛行！");
            return;
        }
        
        player.sendMessage("§e測試項目:");
        player.sendMessage("§7• 盔甲座是否跟隨玩家移動");
        player.sendMessage("§7• 盔甲座是否保持相對位置固定");
        player.sendMessage("§7• 飛劍是否正確顯示在頭盔位置");
        
        // 30秒後自動停止
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getFlightController().isFlying(player.getUniqueId())) {
                plugin.getFlightController().stopFlight(player);
                player.sendMessage("§e基礎固定測試完成");
            }
        }, 600L);
    }

    private void stressTestArmorStand(Player player) {
        player.sendMessage("§6=== 盔甲座壓力測試 ===");
        player.sendMessage("§e將快速切換位置和角度來測試穩定性");
        
        if (!plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§c無法啟動飛行！");
            return;
        }
        
        new BukkitRunnable() {
            private int count = 0;
            private final double[] testAngles = {0, 45, 90, 135, 180, -45, -90};
            
            @Override
            public void run() {
                if (count >= 14 || !plugin.getFlightController().isFlying(player.getUniqueId())) {
                    plugin.getFlightController().stopFlight(player);
                    player.sendMessage("§a壓力測試完成！如果盔甲座沒有亂跑就說明修復成功");
                    this.cancel();
                    return;
                }
                
                // 快速切換角度
                double angle = testAngles[count % testAngles.length];
                plugin.getConfig().set("flight.sword_rotation.y", angle);
                player.sendMessage("§e測試角度: §a" + angle + "° §7(" + (count + 1) + "/14)");
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 40L); // 每2秒一次
    }

    private void testPlayerMovement(Player player) {
        player.sendMessage("§6=== 玩家移動測試 ===");
        player.sendMessage("§e現在請四處移動、跳躍、轉身");
        player.sendMessage("§e觀察盔甲座是否始終跟隨您的腳部位置");
        
        if (!plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§c無法啟動飛行！");
            return;
        }
        
        player.sendMessage("§a飛行已啟動，請開始移動測試！");
        player.sendMessage("§7• 走路、跑步、跳躍");
        player.sendMessage("§7• 快速轉身");
        player.sendMessage("§7• 上下看");
        
        // 60秒測試時間
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getFlightController().isFlying(player.getUniqueId())) {
                plugin.getFlightController().stopFlight(player);
                player.sendMessage("§e移動測試時間結束");
            }
        }, 1200L);
    }

    private void testRotationStability(Player player) {
        player.sendMessage("§6=== 角度穩定性測試 ===");
        player.sendMessage("§e將測試角度變化是否會影響盔甲座位置");
        
        if (!plugin.getFlightController().startFlight(player)) {
            player.sendMessage("§c無法啟動飛行！");
            return;
        }
        
        new BukkitRunnable() {
            private int step = 0;
            
            @Override
            public void run() {
                if (step >= 8 || !plugin.getFlightController().isFlying(player.getUniqueId())) {
                    // 重置為預設角度
                    plugin.getConfig().set("flight.sword_rotation.x", 0.0);
                    plugin.getConfig().set("flight.sword_rotation.y", 0.0);
                    plugin.getConfig().set("flight.sword_rotation.z", 0.0);
                    
                    plugin.getFlightController().stopFlight(player);
                    player.sendMessage("§a角度穩定性測試完成！");
                    this.cancel();
                    return;
                }
                
                // 測試不同軸的角度
                switch (step) {
                    case 0:
                        plugin.getConfig().set("flight.sword_rotation.x", 90.0);
                        player.sendMessage("§e測試 X軸 90°");
                        break;
                    case 1:
                        plugin.getConfig().set("flight.sword_rotation.y", 90.0);
                        player.sendMessage("§e測試 Y軸 90°");
                        break;
                    case 2:
                        plugin.getConfig().set("flight.sword_rotation.z", 90.0);
                        player.sendMessage("§e測試 Z軸 90°");
                        break;
                    case 3:
                        plugin.getConfig().set("flight.sword_rotation.x", 45.0);
                        plugin.getConfig().set("flight.sword_rotation.y", 45.0);
                        player.sendMessage("§e測試複合角度 45°+45°");
                        break;
                    default:
                        double angle = step * 30;
                        plugin.getConfig().set("flight.sword_rotation.y", angle);
                        player.sendMessage("§e測試角度: " + angle + "°");
                        break;
                }
                
                step++;
            }
        }.runTaskTimer(plugin, 0L, 60L); // 每3秒一次
    }
}
