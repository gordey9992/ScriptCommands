package me.gordey9992.scriptcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class ScriptCommands extends JavaPlugin {
    
    private static ScriptCommands instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ScriptLoader scriptLoader;
    private ScriptCompiler scriptCompiler;
    
    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        scriptCompiler = new ScriptCompiler(this);
        scriptLoader = new ScriptLoader(this, scriptCompiler);
        
        configManager.saveDefaultConfig();
        messageManager.saveDefaultMessages();
        
        // Регистрация команды /screload
        Command command = getCommand("screload");
        if (command != null) {
            command.setExecutor(new ReloadCommand(this));
            command.setPermission("scriptcommands.reload");
        } else {
            getLogger().warning("Команда screload не зарегистрирована в plugin.yml!");
        }
        
        scriptLoader.loadAllScripts();
        
        getLogger().info("=========================================");
        getLogger().info("ScriptCommands v" + getDescription().getVersion());
        getLogger().info("Авторы: gordey9992 & DeepSeek");
        getLogger().info("=========================================");
    }
    
    @Override
    public void onDisable() {
        if (scriptLoader != null) {
            scriptLoader.unloadAllScripts();
        }
    }
    
    public static ScriptCommands getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ScriptLoader getScriptLoader() { return scriptLoader; }
}
