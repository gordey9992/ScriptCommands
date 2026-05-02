package me.gordey9992.scriptcommands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    
    private final ScriptCommands plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final ScriptLoader scriptLoader;
    
    public ReloadCommand(ScriptCommands plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.scriptLoader = plugin.getScriptLoader();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Перезагружаем config.yml
            configManager.reloadConfig();
            plugin.getLogger().info("config.yml перезагружен");
            
            // 2. Перезагружаем messages.yml
            messageManager.reloadMessages();
            plugin.getLogger().info("messages.yml перезагружен");
            
            // 3. Перезагружаем все скрипты (команды)
            scriptLoader.loadAllScripts();
            plugin.getLogger().info("Все скрипты перезагружены");
            
            long time = System.currentTimeMillis() - startTime;
            
            // Сообщение игроку (из messages.yml)
            String msg = messageManager.getMessage("plugin.reloaded");
            msg = msg.replace("{time}", String.valueOf(time));
            
            if (sender instanceof Player) {
                sender.sendMessage(msg);
            } else {
                plugin.getLogger().info("Плагин перезагружен за " + time + "ms");
            }
            
        } catch (Exception e) {
            String errorMsg = messageManager.getRawMessage("plugin.reload-error");
            sender.sendMessage(errorMsg);
            plugin.getLogger().severe("Ошибка перезагрузки: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
}
