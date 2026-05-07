package me.gordey9992.scriptcommands;

import org.bukkit.command.*;

public class ScriptCommand implements CommandExecutor {
    
    private final ScriptCommands plugin;
    
    public ScriptCommand(ScriptCommands plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("§cИспользование: /script update");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("update")) {
            if (!sender.hasPermission("scriptcommands.update.accept")) {
                sender.sendMessage("§cУ вас нет прав на обновление!");
                return true;
            }
            
            if (plugin.getPendingUpdateVersion() == null) {
                sender.sendMessage("§cНет ожидающих обновлений!");
                return true;
            }
            
            plugin.performUpdate(sender);
            return true;
        }
        
        sender.sendMessage("§cНеизвестная подкоманда. Используйте /script update");
        return true;
    }
}
