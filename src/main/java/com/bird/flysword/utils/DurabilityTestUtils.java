package com.bird.flysword.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.bird.flysword.Flysword;

/**
 * 耐久度測試工具類
 */
public class DurabilityTestUtils implements CommandExecutor {

    private final Flysword plugin;

    public DurabilityTestUtils(Flysword plugin) {
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
            case "check":
                checkDurability(player);
                break;
            case "set":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /durabilitytest set <百分比>");
                    return true;
                }
                try {
                    double percentage = Double.parseDouble(args[1]);
                    setDurability(player, percentage);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c請輸入有效的數字！");
                }
                break;
            case "damage":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /durabilitytest damage <點數>");
                    return true;
                }
                try {
                    int damage = Integer.parseInt(args[1]);
                    damageSword(player, damage);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c請輸入有效的整數！");
                }
                break;
            case "repair":
                repairSword(player);
                break;
            default:
                showUsage(player);
                break;
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§6=== 耐久度測試指令 ===");
        player.sendMessage("§e/durabilitytest check - 檢查手持物品耐久度");
        player.sendMessage("§e/durabilitytest set <百分比> - 設定耐久度百分比");
        player.sendMessage("§e/durabilitytest damage <點數> - 損壞指定點數");
        player.sendMessage("§e/durabilitytest repair - 完全修復");
    }

    private void checkDurability(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().getMaxDurability() <= 0) {
            player.sendMessage("§c手持物品沒有耐久度系統！");
            return;
        }

        if (item.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) item.getItemMeta();
            int currentDamage = damageable.getDamage();
            int maxDurability = item.getType().getMaxDurability();
            int remainingDurability = maxDurability - currentDamage;
            double percentage = (double) remainingDurability / maxDurability * 100;

            player.sendMessage("§6=== 耐久度信息 ===");
            player.sendMessage("§e物品: " + item.getType().name());
            player.sendMessage("§e最大耐久度: " + maxDurability);
            player.sendMessage("§e當前損害: " + currentDamage);
            player.sendMessage("§e剩餘耐久度: " + remainingDurability);
            player.sendMessage("§e百分比: " + String.format("%.1f%%", percentage));
        }
    }

    private void setDurability(Player player, double percentage) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().getMaxDurability() <= 0) {
            player.sendMessage("§c手持物品沒有耐久度系統！");
            return;
        }

        if (item.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) item.getItemMeta();
            int maxDurability = item.getType().getMaxDurability();
            int newDamage = (int) (maxDurability * (1 - percentage / 100));
            newDamage = Math.max(0, Math.min(maxDurability - 1, newDamage));

            damageable.setDamage(newDamage);
            item.setItemMeta(damageable);
            player.getInventory().setItemInMainHand(item);

            player.sendMessage("§a已設定耐久度為 " + percentage + "%");
        }
    }

    private void damageSword(Player player, int damageAmount) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().getMaxDurability() <= 0) {
            player.sendMessage("§c手持物品沒有耐久度系統！");
            return;
        }

        if (item.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) item.getItemMeta();
            int currentDamage = damageable.getDamage();
            int newDamage = Math.min(item.getType().getMaxDurability() - 1, currentDamage + damageAmount);

            damageable.setDamage(newDamage);
            item.setItemMeta(damageable);
            player.getInventory().setItemInMainHand(item);

            player.sendMessage("§a已對物品造成 " + damageAmount + " 點損害");
            checkDurability(player);
        }
    }

    private void repairSword(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().getMaxDurability() <= 0) {
            player.sendMessage("§c手持物品沒有耐久度系統！");
            return;
        }

        if (item.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) item.getItemMeta();
            damageable.setDamage(0);
            item.setItemMeta(damageable);
            player.getInventory().setItemInMainHand(item);

            player.sendMessage("§a物品已完全修復！");
        }
    }
}
