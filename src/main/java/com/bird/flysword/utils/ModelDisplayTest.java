package com.bird.flysword.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import com.bird.flysword.Flysword;

/**
 * 飛劍模型顯示測試工具
 */
public class ModelDisplayTest implements CommandExecutor {
    
    private final Flysword plugin;
    
    public ModelDisplayTest(Flysword plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "test":
                testDefaultSwordDisplay(player);
                break;
            case "give":
                giveDefaultSword(player);
                break;
            case "spawn":
                spawnArmorStandWithSword(player);
                break;
            case "clear":
                clearNearbyArmorStands(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6=== 模型顯示測試工具 ===");
        player.sendMessage("§e/modeltest test - 完整測試預設飛劍顯示");
        player.sendMessage("§e/modeltest give - 給予預設飛劍");
        player.sendMessage("§e/modeltest spawn - 生成測試 ArmorStand");
        player.sendMessage("§e/modeltest clear - 清除附近測試 ArmorStand");
    }
    
    private void testDefaultSwordDisplay(Player player) {
        player.sendMessage("§a開始測試預設飛劍模型顯示...");
        
        // 1. 給予預設飛劍
        ItemStack sword = plugin.getSkinManager().createSwordWithSkin("default");
        player.getInventory().addItem(sword);
        player.sendMessage("§71. 已給予預設飛劍");
        
        // 2. 測試 ArmorStand 顯示
        Location loc = player.getLocation().add(2, 0, 0);
        ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(true);
        stand.setCustomName("§e測試 - 預設飛劍模型");
        stand.setCustomNameVisible(true);
        
        // 3. 設置劍到主手
        ItemStack testSword = new ItemStack(Material.DIAMOND_SWORD);
        stand.getEquipment().setItemInMainHand(testSword);
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
        
        player.sendMessage("§72. 已生成測試 ArmorStand（應該顯示原版鑽石劍）");
        
        // 4. 5秒後清理
        new BukkitRunnable() {
            @Override
            public void run() {
                stand.remove();
                player.sendMessage("§73. 測試完成，已清理測試實體");
            }
        }.runTaskLater(plugin, 100L); // 5秒
        
        player.sendMessage("§a測試完成！請檢查：");
        player.sendMessage("§7- 背包中的飛劍是否為鑽石劍外觀");
        player.sendMessage("§7- ArmorStand 是否顯示鑽石劍模型");
        player.sendMessage("§7- 右鍵飛劍是否能啟動飛行");
    }
    
    private void giveDefaultSword(Player player) {
        ItemStack sword = plugin.getSkinManager().createSwordWithSkin("default");
        player.getInventory().addItem(sword);
        player.sendMessage("§a已給予預設飛劍！");
        
        if (sword.hasItemMeta() && sword.getItemMeta().hasCustomModelData()) {
            player.sendMessage("§7CustomModelData: " + sword.getItemMeta().getCustomModelData());
        } else {
            player.sendMessage("§7使用原版鑽石劍模型（無 CustomModelData）");
        }
    }
    
    private void spawnArmorStandWithSword(Player player) {
        Location loc = player.getLocation().add(1, 0, 0);
        ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class);
        
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(true);
        stand.setCustomName("§e飛劍模型測試");
        stand.setCustomNameVisible(true);
        
        // 使用預設飛劍
        ItemStack sword = plugin.getSkinManager().createSwordWithSkin("default");
        stand.getEquipment().setItemInMainHand(sword);
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
        
        player.sendMessage("§a已生成測試 ArmorStand！");
        player.sendMessage("§7如果看到鑽石劍，說明模型顯示正常");
    }
    
    private void clearNearbyArmorStands(Player player) {
        int count = 0;
        for (ArmorStand stand : player.getNearbyEntities(10, 10, 10).stream()
                .filter(entity -> entity instanceof ArmorStand)
                .map(entity -> (ArmorStand) entity)
                .toList()) {
            if (stand.getCustomName() != null && stand.getCustomName().contains("測試")) {
                stand.remove();
                count++;
            }
        }
        player.sendMessage("§a已清除 " + count + " 個測試 ArmorStand");
    }
}
