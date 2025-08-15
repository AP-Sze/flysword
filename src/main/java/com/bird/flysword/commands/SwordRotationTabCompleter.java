package com.bird.flysword.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * 飛劍角度調整指令 Tab 補全
 */
public class SwordRotationTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一個參數：子指令
            List<String> subCommands = Arrays.asList(
                "show", "set", "preset", "reset", "test", "save", "reload"
            );
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            
        } else if (args.length == 2) {
            if ("set".equals(args[0].toLowerCase())) {
                // set 指令的 X 角度建議
                completions.addAll(Arrays.asList("0", "45", "90", "-45", "-90", "180"));
            } else if ("preset".equals(args[0].toLowerCase())) {
                // 預設角度選項
                completions.addAll(Arrays.asList(
                    "horizontal", "vertical", "diagonal", "spinning", "upward", "downward"
                ));
            }
        } else if (args.length == 3 && "set".equals(args[0].toLowerCase())) {
            // set 指令的 Y 角度建議
            completions.addAll(Arrays.asList("0", "45", "90", "-45", "-90", "180"));
        } else if (args.length == 4 && "set".equals(args[0].toLowerCase())) {
            // set 指令的 Z 角度建議
            completions.addAll(Arrays.asList("0", "45", "90", "-45", "-90", "180"));
        }
        
        return completions;
    }
}
