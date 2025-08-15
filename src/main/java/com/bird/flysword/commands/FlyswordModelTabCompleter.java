package com.bird.flysword.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.bird.flysword.Flysword;

/**
 * 飛劍模型指令 Tab 補全
 */
public class FlyswordModelTabCompleter implements TabCompleter {

    private final Flysword plugin;

    public FlyswordModelTabCompleter(Flysword plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一個參數：子命令
            List<String> subCommands = Arrays.asList(
                "test", "check", "reload", "fix", "spawn", "remove", "validate"
            );
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if ("test".equals(subCommand) || "spawn".equals(subCommand)) {
                // 皮膚 ID 補全
                completions.addAll(plugin.getSkinManager().getAllSkinIds());
            } else if ("check".equals(subCommand) || "validate".equals(subCommand)) {
                // 玩家名稱補全
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}
