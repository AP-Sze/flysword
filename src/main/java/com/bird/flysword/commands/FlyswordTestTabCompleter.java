package com.bird.flysword.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.bird.flysword.Flysword;

/**
 * 飛劍測試系統指令 Tab 補全
 */
public class FlyswordTestTabCompleter implements TabCompleter {

    private final Flysword plugin;

    public FlyswordTestTabCompleter(Flysword plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一個參數：子命令
            List<String> subCommands = Arrays.asList(
                "give", "check", "flight", "info", "durability", "repair", 
                "height", "testlimits"
            );
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if ("durability".equals(subCommand)) {
                // 耐久度數值補全
                completions.addAll(Arrays.asList("0", "25", "50", "75", "100"));
            } else if ("height".equals(subCommand)) {
                // 高度數值補全
                completions.addAll(Arrays.asList("64", "128", "192", "256", "320"));
            }
        }
        
        return completions;
    }
}
