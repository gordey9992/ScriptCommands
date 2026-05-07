package me.gordey9992.scriptcommands;

import org.bukkit.command.*;

public class UpdateCommand implements CommandExecutor {
    
    private final ScriptCommands plugin;
    
    public UpdateCommand(ScriptCommands plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if (cmd.getName().equalsIgnoreCase("scupdateyes")) {
            if (!plugin.isUpdateAsked()) {
                sender.sendMessage("§cНет ожидающих обновлений!");
                return true;
            }
            plugin.performUpdate(sender);
            
        } else if (cmd.getName().equalsIgnoreCase("scupdateno")) {
            plugin.setUpdateAsked(false);
            sender.sendMessage("§7Обновление пропущено.");
        }
        
        return true;
    }
}
