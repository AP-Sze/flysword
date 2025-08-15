package com.bird.flysword.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.bird.flysword.Flysword;

/**
 * 飛劍系統主指令 Tab 補全
 */
public class FlyswordTabCompleter implements TabCompleter {

    private final Flysword plugin;

    public FlyswordTabCompleter(Flysword plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一個參數：主要子命令
            List<String> subCommands = Arrays.asList(
                "reload", "give", "unlock", "list", "menu", "select", "info", 
                "unlockitem", "listitems"
            );
            
            // 根據權限過濾命令
            for (String subCommand : subCommands) {
                if (hasPermissionForSubCommand(sender, subCommand) && 
                    subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "give":
                case "unlock":
                case "unlockitem":
                    // 玩家名稱補全
                    completions.addAll(getOnlinePlayerNames(args[1]));
                    break;
                case "select":
                    // 皮膚ID補全
                    completions.addAll(getSkinIds(args[1]));
                    break;
                default:
                    break;
            }
            
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "give":
                case "unlock":
                    // 皮膚ID補全
                    completions.addAll(getSkinIds(args[2]));
                    break;
                case "unlockitem":
                    // 解鎖道具ID補全
                    completions.addAll(getUnlockItemIds(args[2]));
                    break;
                default:
                    break;
            }
            
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            
            if ("unlockitem".equals(subCommand)) {
                // 數量補全
                completions.addAll(Arrays.asList("1", "5", "10", "64"));
            }
        }
        
        return completions;
    }
    
    private boolean hasPermissionForSubCommand(CommandSender sender, String subCommand) {
        switch (subCommand.toLowerCase()) {
            case "reload":
            case "give":
            case "unlock":
            case "list":
            case "unlockitem":
            case "listitems":
                return sender.hasPermission("flysword.admin");
            case "menu":
            case "select":
            case "info":
                return sender.hasPermission("flysword.use");
            default:
                return false;
        }
    }
    
    private List<String> getOnlinePlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    private List<String> getSkinIds(String prefix) {
        return plugin.getSkinManager().getAllSkinIds().stream()
                .filter(skinId -> skinId.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    private List<String> getUnlockItemIds(String prefix) {
        return plugin.getUnlockItemManager().getAllUnlockItemIds().stream()
                .filter(itemId -> itemId.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
