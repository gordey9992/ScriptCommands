package me.gordey9992.scriptcommands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final ScriptCommands plugin;
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    
    public ReloadCommand(ScriptCommands plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            // Перезагрузка конфигов
            configManager.reloadConfig();
            messageManager.reloadMessages();
            
            // Перезагрузка скриптов
            plugin.getScriptLoader().stopAutoReloadTask();
            plugin.getScriptLoader().loadAllScripts();
            
            // Запуск авто-перезагрузки если включена
            if (configManager.isAutoReload()) {
                plugin.getScriptLoader().startAutoReloadTask();
            }
            
            // Сообщение об успехе
            messageManager.sendMessage(sender, "plugin.reloaded");
            
            if (configManager.isDebugEnabled() && sender instanceof Player) {
                sender.sendMessage("§7Скриптов загружено: §e" + 
                    plugin.getScriptLoader().getLoadedCommands().size());
            }
            
        } catch (Exception e) {
            messageManager.sendMessage(sender, "plugin.reload-error");
            plugin.getLogger().severe("Ошибка перезагрузки: " + e.getMessage());
            if (configManager.isShowStacktraces()) {
                e.printStackTrace();
            }
        }
        
        return true;
    }
}
