package com.bird.flysword.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * 飛劍位置調整指令 Tab 補全
 */
public class SwordPositionTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一個參數：子指令
            List<String> subCommands = Arrays.asList(
                "show", "set", "reset", "test", "save", "reload"
            );
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            
        } else if (args.length >= 2 && "set".equals(args[0].toLowerCase())) {
            // set 指令的參數補全
            if (args.length == 2) {
                // X 座標建議
                completions.addAll(Arrays.asList("0.8", "1.0", "0.5", "-0.5", "0"));
            } else if (args.length == 3) {
                // Y 座標建議
                completions.addAll(Arrays.asList("-1.5", "-1.0", "-2.0", "0", "1.0"));
            } else if (args.length == 4) {
                // Z 座標建議
                completions.addAll(Arrays.asList("0", "0.5", "-0.5", "1.0", "-1.0"));
            }
        }
        
        return completions;
    }
}
