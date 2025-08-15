package com.bird.flysword.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import com.bird.flysword.Flysword;

/**
 * 飛劍模型測試工具
 * 用於診斷和測試飛劍模型顯示問題
 */
public class ModelTestUtils implements CommandExecutor {
    
    private final Flysword plugin;
    
    public ModelTestUtils(Flysword plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("flysword.admin")) {
            player.sendMessage("§c你沒有權限使用此命令！");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "spawn":
                spawnTestSword(player, args.length > 1 ? args[1] : "default");
                break;
                
            case "clear":
                clearTestSwords(player);
                break;
                
            case "info":
                showSkinInfo(player, args.length > 1 ? args[1] : "default");
                break;
                
            case "give":
                giveSwordItem(player, args.length > 1 ? args[1] : "default");
                break;
                
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * 顯示幫助信息
     */
    private void showHelp(Player player) {
        player.sendMessage("§6=== 飛劍模型測試工具 ===");
        player.sendMessage("§e/flyswordtest spawn [皮膚ID] - 生成測試飛劍");
        player.sendMessage("§e/flyswordtest clear - 清除附近的測試飛劍");
        player.sendMessage("§e/flyswordtest info [皮膚ID] - 查看皮膚信息");
        player.sendMessage("§e/flyswordtest give [皮膚ID] - 給予飛劍物品");
    }
    
    /**
     * 生成測試飛劍
     */
    private void spawnTestSword(Player player, String skinId) {
        var skin = plugin.getSkinManager().getSkin(skinId);
        if (skin == null) {
            player.sendMessage("§c皮膚 '" + skinId + "' 不存在！");
            return;
        }
        
        Location spawnLoc = player.getLocation().add(2, 0, 0);
        ArmorStand armorStand = player.getWorld().spawn(spawnLoc, ArmorStand.class);
        
        // 配置 ArmorStand
        armorStand.setVisible(false);          // 隱藏身體
        armorStand.setGravity(false);          // 無重力
        armorStand.setInvulnerable(true);      // 無敵
        armorStand.setMarker(true);            // 標記模式（可穿過）
        armorStand.setSmall(true);             // 小型
        armorStand.setBasePlate(false);        // 隱藏底盤
        armorStand.setArms(true);              // 顯示手臂（重要！）
        
        // 創建飛劍物品
        ItemStack swordItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = swordItem.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§l" + skin.getDisplayName());
            // 只有當 customModelData 大於 0 時才設置
            if (skin.getCustomModelData() > 0) {
                meta.setCustomModelData(skin.getCustomModelData());
            }
            swordItem.setItemMeta(meta);
        }
        
        // 設置物品到主手
        armorStand.getEquipment().setItemInMainHand(swordItem);
        
        // 設置手臂姿勢（讓劍看起來更自然）
        armorStand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
        
        // 添加標籤
        armorStand.setCustomName("§e測試飛劍 - " + skin.getDisplayName());
        armorStand.setCustomNameVisible(true);
        
        player.sendMessage("§a已生成測試飛劍！");
        player.sendMessage("§7皮膚: " + skin.getDisplayName());
        if (skin.getCustomModelData() > 0) {
            player.sendMessage("§7CustomModelData: " + skin.getCustomModelData());
        } else {
            player.sendMessage("§7使用原版鑽石劍模型");
        }
        player.sendMessage("§7模型路徑: " + skin.getModelPath());
    }
    
    /**
     * 清除測試飛劍
     */
    private void clearTestSwords(Player player) {
        Location playerLoc = player.getLocation();
        int count = 0;
        
        for (var entity : player.getWorld().getNearbyEntities(playerLoc, 50, 50, 50)) {
            if (entity instanceof ArmorStand armorStand) {
                String name = armorStand.getCustomName();
                if (name != null && name.contains("測試飛劍")) {
                    armorStand.remove();
                    count++;
                }
            }
        }
        
        player.sendMessage("§a已清除 " + count + " 個測試飛劍！");
    }
    
    /**
     * 顯示皮膚信息
     */
    private void showSkinInfo(Player player, String skinId) {
        var skin = plugin.getSkinManager().getSkin(skinId);
        if (skin == null) {
            player.sendMessage("§c皮膚 '" + skinId + "' 不存在！");
            return;
        }
        
        player.sendMessage("§6=== 皮膚信息 ===");
        player.sendMessage("§7ID: §f" + skin.getId());
        player.sendMessage("§7名稱: §f" + skin.getDisplayName());
        player.sendMessage("§7描述: §f" + skin.getDescription());
        if (skin.getCustomModelData() > 0) {
            player.sendMessage("§7CustomModelData: §f" + skin.getCustomModelData());
        } else {
            player.sendMessage("§7模型類型: §f原版鑽石劍");
        }
        player.sendMessage("§7模型路徑: §f" + skin.getModelPath());
        player.sendMessage("§7解鎖類型: §f" + skin.getUnlockType());
        player.sendMessage("§7解鎖值: §f" + skin.getUnlockValue());
    }
    
    /**
     * 給予飛劍物品
     */
    private void giveSwordItem(Player player, String skinId) {
        ItemStack swordItem = plugin.getSkinManager().createSwordWithSkin(skinId);
        if (swordItem != null) {
            player.getInventory().addItem(swordItem);
            player.sendMessage("§a已給予你飛劍物品！");
            
            // 顯示物品信息
            if (swordItem.hasItemMeta()) {
                ItemMeta meta = swordItem.getItemMeta();
                if (meta.hasCustomModelData()) {
                    player.sendMessage("§7CustomModelData: " + meta.getCustomModelData());
                } else {
                    player.sendMessage("§c警告：物品沒有 CustomModelData！");
                }
            }
        } else {
            player.sendMessage("§c創建飛劍物品失敗！");
        }
    }
}
