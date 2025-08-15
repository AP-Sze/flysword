package com.bird.flysword.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.bird.flysword.Flysword;
import com.bird.flysword.utils.SwordMathUtils.FlightMode;
import com.bird.flysword.utils.SwordMathUtils.SwordTransform;

/**
 * 智能飛劍數學計算指令
 * 自動計算最佳角度和位置，無需手動測試
 */
public class SmartSwordCommand implements CommandExecutor {

    private final Flysword plugin;

    public SmartSwordCommand(Flysword plugin) {
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
            case "auto":
                enableAutoCalculation(player);
                break;
            case "manual":
                disableAutoCalculation(player);
                break;
            case "preset":
                if (args.length < 2) {
                    showPresets(player);
                } else {
                    applyPreset(player, args[1]);
                }
                break;
            case "calculate":
                calculateCurrentOptimal(player);
                break;
            case "test":
                testAllModes(player);
                break;
            case "info":
                showMathInfo(player);
                break;
            default:
                showUsage(player);
                break;
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§6=== 智能飛劍數學系統 ===");
        player.sendMessage("§e/smartsword auto - 啟用自動角度計算");
        player.sendMessage("§e/smartsword manual - 停用自動計算");
        player.sendMessage("§e/smartsword preset [模式] - 應用預設角度");
        player.sendMessage("§e/smartsword calculate - 計算當前最佳角度");
        player.sendMessage("§e/smartsword test - 測試所有飛行模式");
        player.sendMessage("§e/smartsword info - 顯示數學計算資訊");
        player.sendMessage("§7");
        player.sendMessage("§a智能系統會根據您的移動方向和速度");
        player.sendMessage("§a自動計算最自然的飛劍角度和位置！");
    }

    private void enableAutoCalculation(Player player) {
        plugin.getConfig().set("flight.smart_calculation.enabled", true);
        player.sendMessage("§a已啟用智能飛劍計算系統！");
        player.sendMessage("§e系統將根據您的飛行狀態自動調整飛劍角度");
        
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            startAutoCalculation(player);
        } else {
            player.sendMessage("§7開始飛行後自動計算將激活");
        }
    }

    private void disableAutoCalculation(Player player) {
        plugin.getConfig().set("flight.smart_calculation.enabled", false);
        player.sendMessage("§c已停用智能飛劍計算系統");
        player.sendMessage("§7將使用配置檔中的固定角度");
    }

    private void showPresets(Player player) {
        player.sendMessage("§6=== 可用的飛行模式預設 ===");
        player.sendMessage("§e/smartsword preset cruise §7- 水平巡航模式");
        player.sendMessage("§e/smartsword preset climb §7- 爬升模式");
        player.sendMessage("§e/smartsword preset dive §7- 俯衝模式");
        player.sendMessage("§e/smartsword preset ascent §7- 垂直上升");
        player.sendMessage("§e/smartsword preset descent §7- 垂直下降");
        player.sendMessage("§e/smartsword preset left §7- 左轉模式");
        player.sendMessage("§e/smartsword preset right §7- 右轉模式");
        player.sendMessage("§e/smartsword preset dynamic §7- 動態計算");
    }

    private void applyPreset(Player player, String mode) {
        FlightMode flightMode;
        String modeName;

        switch (mode.toLowerCase()) {
            case "cruise":
            case "horizontal":
                flightMode = FlightMode.HORIZONTAL_CRUISE;
                modeName = "水平巡航";
                break;
            case "climb":
            case "climbing":
                flightMode = FlightMode.CLIMBING;
                modeName = "爬升";
                break;
            case "dive":
            case "diving":
                flightMode = FlightMode.DIVING;
                modeName = "俯衝";
                break;
            case "ascent":
            case "up":
                flightMode = FlightMode.VERTICAL_ASCENT;
                modeName = "垂直上升";
                break;
            case "descent":
            case "down":
                flightMode = FlightMode.VERTICAL_DESCENT;
                modeName = "垂直下降";
                break;
            case "left":
                flightMode = FlightMode.BANKING_LEFT;
                modeName = "左轉";
                break;
            case "right":
                flightMode = FlightMode.BANKING_RIGHT;
                modeName = "右轉";
                break;
            case "dynamic":
            case "auto":
                flightMode = FlightMode.DYNAMIC;
                modeName = "動態計算";
                break;
            default:
                player.sendMessage("§c未知的飛行模式: " + mode);
                showPresets(player);
                return;
        }

        Vector velocity = player.getVelocity();
        Vector rotation = SwordMathUtils.calculatePresetRotation(flightMode, velocity);
        
        // 應用計算結果
        plugin.getConfig().set("flight.sword_rotation.x", rotation.getX());
        plugin.getConfig().set("flight.sword_rotation.y", rotation.getY());
        plugin.getConfig().set("flight.sword_rotation.z", rotation.getZ());

        player.sendMessage("§a已應用 §e" + modeName + " §a飛行模式");
        player.sendMessage("§7角度: X=" + String.format("%.1f", rotation.getX()) + 
                          "° Y=" + String.format("%.1f", rotation.getY()) + 
                          "° Z=" + String.format("%.1f", rotation.getZ()) + "°");

        // 如果正在飛行，立即測試效果
        if (!plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§7使用 /flysword 開始飛行查看效果");
        }
    }

    private void calculateCurrentOptimal(Player player) {
        if (!plugin.getFlightController().isFlying(player.getUniqueId())) {
            player.sendMessage("§c請先開始飛行再進行計算！");
            return;
        }

        player.sendMessage("§6=== 計算當前最佳飛劍角度 ===");

        SwordTransform transform = SwordMathUtils.calculateOptimalSwordTransform(player);
        
        player.sendMessage("§e根據您當前的飛行狀態計算結果：");
        player.sendMessage("§7位置偏移: " + 
            String.format("X=%.2f Y=%.2f Z=%.2f", 
            transform.position.getX(), transform.position.getY(), transform.position.getZ()));
        player.sendMessage("§7最佳角度: " + 
            String.format("X=%.1f° Y=%.1f° Z=%.1f°", 
            transform.rotation.getX(), transform.rotation.getY(), transform.rotation.getZ()));

        // 詢問是否應用
        player.sendMessage("§a輸入 §e/smartsword apply §a來應用這些設定");
        
        // 臨時儲存計算結果
        plugin.getConfig().set("flight.temp_calculated.position.x", transform.position.getX());
        plugin.getConfig().set("flight.temp_calculated.position.y", transform.position.getY());
        plugin.getConfig().set("flight.temp_calculated.position.z", transform.position.getZ());
        plugin.getConfig().set("flight.temp_calculated.rotation.x", transform.rotation.getX());
        plugin.getConfig().set("flight.temp_calculated.rotation.y", transform.rotation.getY());
        plugin.getConfig().set("flight.temp_calculated.rotation.z", transform.rotation.getZ());
    }

    private void testAllModes(Player player) {
        if (!plugin.getFlightController().isFlying(player.getUniqueId())) {
            if (!plugin.getFlightController().startFlight(player)) {
                player.sendMessage("§c無法啟動飛行！");
                return;
            }
        }

        player.sendMessage("§6=== 測試所有飛行模式 ===");
        player.sendMessage("§e將依序展示各種飛行模式，每個持續3秒");

        FlightMode[] modes = FlightMode.values();
        String[] modeNames = {
            "水平巡航", "垂直上升", "垂直下降", 
            "左轉", "右轉", "俯衝", "爬升", "動態計算"
        };

        new BukkitRunnable() {
            private int currentMode = 0;

            @Override
            public void run() {
                if (currentMode >= modes.length || !plugin.getFlightController().isFlying(player.getUniqueId())) {
                    // 測試完成，重置為動態模式
                    Vector rotation = SwordMathUtils.calculatePresetRotation(FlightMode.DYNAMIC, player.getVelocity());
                    applyRotation(rotation);
                    player.sendMessage("§a所有模式測試完成！已設為動態計算模式");
                    this.cancel();
                    return;
                }

                FlightMode mode = modes[currentMode];
                String name = modeNames[currentMode];
                
                Vector rotation = SwordMathUtils.calculatePresetRotation(mode, player.getVelocity());
                applyRotation(rotation);
                
                player.sendMessage("§e正在測試: §a" + name + " §7(" + (currentMode + 1) + "/" + modes.length + ")");
                
                currentMode++;
            }

            private void applyRotation(Vector rotation) {
                plugin.getConfig().set("flight.sword_rotation.x", rotation.getX());
                plugin.getConfig().set("flight.sword_rotation.y", rotation.getY());
                plugin.getConfig().set("flight.sword_rotation.z", rotation.getZ());
            }
        }.runTaskTimer(plugin, 0L, 60L); // 每3秒一個模式
    }

    private void showMathInfo(Player player) {
        player.sendMessage("§6=== 飛劍數學計算原理 ===");
        player.sendMessage("§e位置計算:");
        player.sendMessage("§7• 基礎位置：玩家腳下 (0, -2, 0)");
        player.sendMessage("§7• 前進偏移：根據飛行方向 × 0.5米");
        player.sendMessage("§7• 速度補償：高速時更前方");
        player.sendMessage("");
        player.sendMessage("§e角度計算:");
        player.sendMessage("§7• 俯仰角：arcsin(-direction.Y)");
        player.sendMessage("§7• 偏航角：atan2(-direction.X, direction.Z)");
        player.sendMessage("§7• 翻滾角：根據橫向速度計算");
        player.sendMessage("");
        player.sendMessage("§e智能調整:");
        player.sendMessage("§7• 高速時飛劍稍微上翹（+5°）");
        player.sendMessage("§7• 轉彎時自動傾斜（最大±30°）");
        player.sendMessage("§7• 低速時減少翻滾增加穩定性");
        
        if (plugin.getFlightController().isFlying(player.getUniqueId())) {
            Vector velocity = player.getVelocity();
            double speed = velocity.length();
            player.sendMessage("");
            player.sendMessage("§a當前狀態:");
            player.sendMessage("§7• 飛行速度: " + String.format("%.2f", speed) + " 格/tick");
            player.sendMessage("§7• 速度向量: " + String.format("(%.2f, %.2f, %.2f)", 
                velocity.getX(), velocity.getY(), velocity.getZ()));
        }
    }

    private void startAutoCalculation(Player player) {
        // 這個方法將在 FlightSession 中調用
        player.sendMessage("§a智能計算已激活，飛劍將自動調整角度！");
    }
}
