package com.bird.flysword.utils;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;
import com.bird.flysword.data.SwordEnchant;
import com.bird.flysword.data.SwordSkin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestUtils implements CommandExecutor {
    
    private final Flysword plugin;
    
    public TestUtils(Flysword plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("flysword.admin")) {
            sender.sendMessage("§c您沒有權限執行此指令！");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "test":
                runTests(sender);
                break;
            case "setup":
                setupTestData(sender);
                break;
            case "cleanup":
                cleanupTestData(sender);
                break;
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== 測試工具指令 ===");
        sender.sendMessage("§e/flyswordtest test §7- 運行功能測試");
        sender.sendMessage("§e/flyswordtest setup §7- 設置測試數據");
        sender.sendMessage("§e/flyswordtest cleanup §7- 清理測試數據");
    }
    
    private void runTests(CommandSender sender) {
        sender.sendMessage("§a開始運行飛劍系統測試...");
        
        // 測試皮膚管理器
        testSkinManager(sender);
        
        // 測試附魔管理器
        testEnchantManager(sender);
        
        // 測試數據管理器
        testDataManager(sender);
        
        sender.sendMessage("§a測試完成！");
    }
    
    private void testSkinManager(CommandSender sender) {
        sender.sendMessage("§e測試皮膚管理器...");
        
        // 檢查默認皮膚
        if (plugin.getSkinManager().hasSkin("default")) {
            sender.sendMessage("§a✓ 默認皮膚載入成功");
        } else {
            sender.sendMessage("§c✗ 默認皮膚載入失敗");
        }
        
        // 檢查皮膚數量
        int skinCount = plugin.getSkinManager().getAllSkins().size();
        sender.sendMessage("§a✓ 載入皮膚數量: " + skinCount);
        
        // 測試創建飛劍
        try {
            plugin.getSkinManager().createSwordWithSkin("default");
            sender.sendMessage("§a✓ 飛劍創建成功");
        } catch (Exception e) {
            sender.sendMessage("§c✗ 飛劍創建失敗: " + e.getMessage());
        }
    }
    
    private void testEnchantManager(CommandSender sender) {
        sender.sendMessage("§e測試附魔管理器...");
        
        // 檢查默認附魔
        String[] defaultEnchants = {"speed", "stability", "regen", "shield"};
        for (String enchantId : defaultEnchants) {
            if (plugin.getEnchantManager().hasEnchant(enchantId)) {
                sender.sendMessage("§a✓ 附魔 " + enchantId + " 載入成功");
            } else {
                sender.sendMessage("§c✗ 附魔 " + enchantId + " 載入失敗");
            }
        }
        
        // 檢查附魔數量
        int enchantCount = plugin.getEnchantManager().getAllEnchants().size();
        sender.sendMessage("§a✓ 載入附魔數量: " + enchantCount);
        
        // 測試附魔效果計算
        double speedEffect = plugin.getEnchantManager().getEnchantEffect("speed", 3);
        sender.sendMessage("§a✓ 速度附魔效果計算: " + speedEffect);
    }
    
    private void testDataManager(CommandSender sender) {
        sender.sendMessage("§e測試數據管理器...");
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData playerData = plugin.getDataManager().getPlayerData(player);
            
            // 測試數據讀取
            sender.sendMessage("§a✓ 玩家數據讀取成功");
            sender.sendMessage("§7- 當前皮膚: " + playerData.getSelectedSkin());
            sender.sendMessage("§7- 耐久度: " + playerData.getDurability() + "%");
            sender.sendMessage("§7- 已解鎖皮膚數量: " + playerData.getUnlockedSkins().size());
            
            // 測試數據保存
            try {
                plugin.getDataManager().savePlayerData(player);
                sender.sendMessage("§a✓ 玩家數據保存成功");
            } catch (Exception e) {
                sender.sendMessage("§c✗ 玩家數據保存失敗: " + e.getMessage());
            }
        } else {
            sender.sendMessage("§e此測試需要玩家執行");
        }
    }
    
    private void setupTestData(CommandSender sender) {
        sender.sendMessage("§a設置測試數據...");
        
        // 為所有在線玩家解鎖所有皮膚
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = plugin.getDataManager().getPlayerData(player);
            
            for (SwordSkin skin : plugin.getSkinManager().getAllSkins().values()) {
                playerData.unlockSkin(skin.getId());
            }
            
            // 設置一些測試附魔
            playerData.setEnchantLevel("speed", 3);
            playerData.setEnchantLevel("stability", 2);
            playerData.setEnchantLevel("regen", 1);
            
            // 恢復耐久度
            playerData.setDurability(100);
            
            plugin.getDataManager().savePlayerData(player);
            player.sendMessage("§a您已獲得所有測試數據！");
        }
        
        sender.sendMessage("§a測試數據設置完成！");
    }
    
    private void cleanupTestData(CommandSender sender) {
        sender.sendMessage("§a清理測試數據...");
        
        // 重置所有在線玩家的數據
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = plugin.getDataManager().getPlayerData(player);
            
            // 只保留默認皮膚
            playerData.getUnlockedSkins().clear();
            playerData.unlockSkin("default");
            playerData.setSelectedSkin("default");
            
            // 清除所有附魔
            playerData.getEnchantLevels().clear();
            
            // 恢復耐久度
            playerData.setDurability(100);
            
            plugin.getDataManager().savePlayerData(player);
            player.sendMessage("§a您的數據已重置為默認狀態！");
        }
        
        sender.sendMessage("§a測試數據清理完成！");
    }
}
