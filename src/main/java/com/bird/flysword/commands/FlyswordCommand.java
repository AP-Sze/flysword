package com.bird.flysword.commands;

import com.bird.flysword.Flysword;
import com.bird.flysword.data.PlayerData;
import com.bird.flysword.data.SwordSkin;
import com.bird.flysword.gui.SkinMenu;
import com.bird.flysword.managers.SkinManager;
import com.bird.flysword.managers.UnlockItemManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class FlyswordCommand implements CommandExecutor {

    private final Flysword plugin;

    public FlyswordCommand(Flysword plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                showHelp((Player) sender);
            } else {
                sender.sendMessage("§c此指令只能由玩家執行！");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (sender.hasPermission("flysword.admin")) {
                    reloadPlugin(sender);
                } else {
                    sender.sendMessage("§c您沒有權限執行此指令！");
                }
                break;

            case "give":
                if (sender.hasPermission("flysword.admin")) {
                    if (args.length < 2) {
                        sender.sendMessage("§c用法: /flysword give <玩家> [皮膚ID]");
                        return true;
                    }
                    giveSword(sender, args);
                } else {
                    sender.sendMessage("§c您沒有權限執行此指令！");
                }
                break;

            case "unlock":
                if (sender.hasPermission("flysword.admin")) {
                    if (args.length < 3) {
                        sender.sendMessage("§c用法: /flysword unlock <玩家> <皮膚ID>");
                        return true;
                    }
                    unlockSkin(sender, args);
                } else {
                    sender.sendMessage("§c您沒有權限執行此指令！");
                }
                break;

            case "unlockitem":
                if (sender.hasPermission("flysword.admin")) {
                    if (args.length < 3) {
                        sender.sendMessage("§c用法: /flysword unlockitem <玩家> <道具ID> [數量]");
                        return true;
                    }
                    giveUnlockItem(sender, args);
                } else {
                    sender.sendMessage("§c您沒有權限執行此指令！");
                }
                break;

            case "list":
                if (sender.hasPermission("flysword.admin")) {
                    listSkins(sender);
                } else {
                    sender.sendMessage("§c您沒有權限執行此指令！");
                }
                break;

            case "listitems":
                if (sender.hasPermission("flysword.admin")) {
                    listUnlockItems(sender);
                } else {
                    sender.sendMessage("§c您沒有權限執行此指令！");
                }
                break;

            case "menu":
                if (sender instanceof Player) {
                    if (sender.hasPermission("flysword.use")) {
                        openSkinMenu((Player) sender);
                    } else {
                        sender.sendMessage("§c您沒有權限使用飛劍系統！");
                    }
                } else {
                    sender.sendMessage("§c此指令只能由玩家執行！");
                }
                break;

            case "select":
                if (sender instanceof Player) {
                    if (sender.hasPermission("flysword.use")) {
                        if (args.length < 2) {
                            sender.sendMessage("§c用法: /flysword select <皮膚ID>");
                            return true;
                        }
                        selectSkin((Player) sender, args[1]);
                    } else {
                        sender.sendMessage("§c您沒有權限使用飛劍系統！");
                    }
                } else {
                    sender.sendMessage("§c此指令只能由玩家執行！");
                }
                break;

            case "info":
                if (sender instanceof Player) {
                    if (sender.hasPermission("flysword.use")) {
                        showPlayerInfo((Player) sender);
                    } else {
                        sender.sendMessage("§c您沒有權限使用飛劍系統！");
                    }
                } else {
                    sender.sendMessage("§c此指令只能由玩家執行！");
                }
                break;

            default:
                if (sender instanceof Player) {
                    showHelp((Player) sender);
                } else {
                    sender.sendMessage("§c未知的子指令！");
                }
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§6§l=== 飛劍系統指令 ===");
        player.sendMessage("§e/flysword menu §7- 打開皮膚選單");
        player.sendMessage("§e/flysword select <皮膚ID> §7- 選擇皮膚");
        player.sendMessage("§e/flysword info §7- 查看個人資訊");

        if (player.hasPermission("flysword.admin")) {
            player.sendMessage("§c/flysword reload §7- 重新載入插件");
            player.sendMessage("§c/flysword give <玩家> [皮膚ID] §7- 給予飛劍");
            player.sendMessage("§c/flysword unlock <玩家> <皮膚ID> §7- 解鎖皮膚");
            player.sendMessage("§c/flysword unlockitem <玩家> <道具ID> [數量] §7- 給予解鎖道具");
            player.sendMessage("§c/flysword list §7- 列出所有皮膚");
            player.sendMessage("§c/flysword listitems §7- 列出所有解鎖道具");
        }
    }

    private void reloadPlugin(CommandSender sender) {
        plugin.getConfigManager().reloadConfigs();
        plugin.getSkinManager().loadSkins();
        plugin.getEnchantManager().loadEnchants();
        plugin.getUnlockItemManager().reloadUnlockItems();
        sender.sendMessage("§a插件配置已重新載入！");
    }

    private void giveSword(CommandSender sender, String[] args) {
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage("§c找不到玩家: " + playerName);
            return;
        }

        String skinId = args.length > 2 ? args[2] : "default";
        SkinManager skinManager = plugin.getSkinManager();

        if (!skinManager.hasSkin(skinId)) {
            sender.sendMessage("§c找不到皮膚: " + skinId);
            return;
        }

        ItemStack sword = skinManager.createSwordWithSkin(skinId);
        target.getInventory().addItem(sword);

        sender.sendMessage("§a已給予 " + target.getName() + " 一把飛劍 (皮膚: " + skinId + ")");
        target.sendMessage("§a您收到了一把飛劍！");
    }

    private void unlockSkin(CommandSender sender, String[] args) {
        String playerName = args[1];
        String skinId = args[2];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage("§c找不到玩家: " + playerName);
            return;
        }

        SkinManager skinManager = plugin.getSkinManager();
        if (!skinManager.hasSkin(skinId)) {
            sender.sendMessage("§c找不到皮膚: " + skinId);
            return;
        }

        PlayerData playerData = plugin.getDataManager().getPlayerData(target);
        if (playerData.hasSkin(skinId)) {
            sender.sendMessage("§c玩家 " + target.getName() + " 已經擁有皮膚 " + skinId);
            return;
        }

        playerData.unlockSkin(skinId);
        plugin.getDataManager().savePlayerData(target);

        sender.sendMessage("§a已為 " + target.getName() + " 解鎖皮膚: " + skinId);
        target.sendMessage("§a您解鎖了新皮膚: " + skinId);

        // 播放解鎖特效
        plugin.getEffectManager().playSkinUnlockEffect(target, skinId);
    }

    private void giveUnlockItem(CommandSender sender, String[] args) {
        String playerName = args[1];
        String unlockItemId = args[2];
        int amount = args.length > 3 ? Integer.parseInt(args[3]) : 1;

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§c找不到玩家: " + playerName);
            return;
        }

        UnlockItemManager unlockItemManager = plugin.getUnlockItemManager();
        if (!unlockItemManager.hasUnlockItem(unlockItemId)) {
            sender.sendMessage("§c找不到解鎖道具: " + unlockItemId);
            return;
        }

        unlockItemManager.giveUnlockItem(target, unlockItemId, amount);
        sender.sendMessage("§a已給予 " + target.getName() + " " + amount + " 個解鎖道具: " + unlockItemId);
    }

    private void listSkins(CommandSender sender) {
        SkinManager skinManager = plugin.getSkinManager();
        sender.sendMessage("§6§l=== 所有飛劍皮膚 ===");

        for (SwordSkin skin : skinManager.getAllSkins().values()) {
            sender.sendMessage("§e" + skin.getId() + " §7- " + skin.getDisplayName());
        }
    }

    private void listUnlockItems(CommandSender sender) {
        UnlockItemManager unlockItemManager = plugin.getUnlockItemManager();
        sender.sendMessage("§6§l=== 所有解鎖道具 ===");

        for (UnlockItemManager.UnlockItem item : unlockItemManager.getAllUnlockItems().values()) {
            sender.sendMessage("§e" + item.getId() + " §7- " + item.getDisplayName() + " (皮膚: " + item.getSkinId() + ")");
        }
    }

    private void openSkinMenu(Player player) {
        SkinMenu skinMenu = new SkinMenu(plugin);
        skinMenu.openMenu(player);
    }

    private void selectSkin(Player player, String skinId) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        SkinManager skinManager = plugin.getSkinManager();

        if (!skinManager.hasSkin(skinId)) {
            player.sendMessage("§c找不到皮膚: " + skinId);
            return;
        }

        if (!playerData.hasSkin(skinId)) {
            player.sendMessage("§c您尚未解鎖此皮膚！");
            return;
        }

        playerData.setSelectedSkin(skinId);
        plugin.getDataManager().savePlayerData(player);

        player.sendMessage("§a已選擇皮膚: " + skinId);
    }

    private void showPlayerInfo(Player player) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player);
        SkinManager skinManager = plugin.getSkinManager();

        player.sendMessage("§6§l=== 飛劍資訊 ===");
        player.sendMessage("§e當前皮膚: §7" + playerData.getSelectedSkin());
        player.sendMessage("§e耐久度: §7" + playerData.getDurability() + "%");
        player.sendMessage("§e飛行狀態: §7" + (playerData.isFlying() ? "§a飛行中" : "§c未飛行"));

        player.sendMessage("§e已解鎖皮膚:");
        for (String skinId : playerData.getUnlockedSkins()) {
            SwordSkin skin = skinManager.getSkin(skinId);
            if (skin != null) {
                player.sendMessage("§7- " + skin.getDisplayName() + " (" + skinId + ")");
            }
        }

        player.sendMessage("§e附魔等級:");
        for (Map.Entry<String, Integer> entry : playerData.getEnchantLevels().entrySet()) {
            player.sendMessage("§7- " + entry.getKey() + ": " + entry.getValue());
        }
    }
}
