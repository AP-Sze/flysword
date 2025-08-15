package com.bird.flysword.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bird.flysword.Flysword;

/**
 * 數學計算驗證工具
 */
public class MathVerificationUtils implements CommandExecutor {

    private final Flysword plugin;

    public MathVerificationUtils(Flysword plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c這個指令只能由玩家執行！");
            return true;
        }

        Player player = (Player) sender;

        player.sendMessage("§6=== 飛劍數學計算驗證 ===");
        
        // 測試各種飛行狀態
        testStaticState(player);
        testHorizontalFlight(player);
        testVerticalFlight(player);
        testDiagonalFlight(player);
        testHighSpeedFlight(player);
        
        return true;
    }

    private void testStaticState(Player player) {
        player.sendMessage("§e1. 靜止狀態測試:");
        
        // 模擬靜止狀態
        Vector mockVelocity = new Vector(0, 0, 0);
        Vector rotation = SwordMathUtils.calculatePresetRotation(SwordMathUtils.FlightMode.DYNAMIC, mockVelocity);
        
        player.sendMessage("  §7速度: (0, 0, 0) → 角度: " + formatRotation(rotation));
        player.sendMessage("  §a預期: 水平放置，無傾斜");
    }

    private void testHorizontalFlight(Player player) {
        player.sendMessage("§e2. 水平飛行測試:");
        
        // 模擬向前飛行
        Vector mockVelocity = new Vector(0, 0, 1); // 向北飛行
        Vector rotation = SwordMathUtils.calculatePresetRotation(SwordMathUtils.FlightMode.DYNAMIC, mockVelocity);
        
        player.sendMessage("  §7速度: (0, 0, 1) → 角度: " + formatRotation(rotation));
        
        // 模擬向右飛行
        mockVelocity = new Vector(1, 0, 0); // 向東飛行
        rotation = SwordMathUtils.calculatePresetRotation(SwordMathUtils.FlightMode.DYNAMIC, mockVelocity);
        
        player.sendMessage("  §7速度: (1, 0, 0) → 角度: " + formatRotation(rotation));
    }

    private void testVerticalFlight(Player player) {
        player.sendMessage("§e3. 垂直飛行測試:");
        
        // 模擬向上飛行
        Vector mockVelocity = new Vector(0, 1, 0);
        Vector rotation = SwordMathUtils.calculatePresetRotation(SwordMathUtils.FlightMode.DYNAMIC, mockVelocity);
        
        player.sendMessage("  §7速度: (0, 1, 0) → 角度: " + formatRotation(rotation));
        player.sendMessage("  §a預期: 劍尖向上，約-90°俯仰角");
        
        // 模擬向下飛行
        mockVelocity = new Vector(0, -1, 0);
        rotation = SwordMathUtils.calculatePresetRotation(SwordMathUtils.FlightMode.DYNAMIC, mockVelocity);
        
        player.sendMessage("  §7速度: (0, -1, 0) → 角度: " + formatRotation(rotation));
        player.sendMessage("  §a預期: 劍尖向下，約+90°俯仰角");
    }

    private void testDiagonalFlight(Player player) {
        player.sendMessage("§e4. 對角飛行測試:");
        
        // 模擬45度向上向前飛行
        Vector mockVelocity = new Vector(0, 0.707, 0.707); // 45度角
        Vector rotation = SwordMathUtils.calculatePresetRotation(SwordMathUtils.FlightMode.DYNAMIC, mockVelocity);
        
        player.sendMessage("  §7速度: (0, 0.707, 0.707) → 角度: " + formatRotation(rotation));
        player.sendMessage("  §a預期: 約-45°俯仰角");
        
        // 模擬右前方飛行
        mockVelocity = new Vector(0.707, 0, 0.707);
        rotation = SwordMathUtils.calculatePresetRotation(SwordMathUtils.FlightMode.DYNAMIC, mockVelocity);
        
        player.sendMessage("  §7速度: (0.707, 0, 0.707) → 角度: " + formatRotation(rotation));
    }

    private void testHighSpeedFlight(Player player) {
        player.sendMessage("§e5. 高速飛行測試:");
        
        // 模擬高速向前飛行
        Vector mockVelocity = new Vector(0, 0, 2); // 高速
        Vector rotation = SwordMathUtils.calculatePresetRotation(SwordMathUtils.FlightMode.DYNAMIC, mockVelocity);
        
        player.sendMessage("  §7速度: (0, 0, 2) → 角度: " + formatRotation(rotation));
        player.sendMessage("  §a預期: 稍微上翹的角度，模擬空氣動力學");
        
        player.sendMessage("");
        player.sendMessage("§6=== 數學原理驗證完成 ===");
        player.sendMessage("§a所有計算都基於真實的3D向量數學");
        player.sendMessage("§a使用 /smartsword auto 啟用智能計算！");
    }

    private String formatRotation(Vector rotation) {
        return String.format("(%.1f°, %.1f°, %.1f°)", 
            rotation.getX(), rotation.getY(), rotation.getZ());
    }
}
