package me.gordey9992.scriptcommands;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class ScriptCommands extends JavaPlugin {
    private static ScriptCommands instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ScriptLoader scriptLoader;
    private ScriptCompiler scriptCompiler;

    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация менеджеров
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        
        // Сохраняем конфиги
        configManager.saveDefaultConfig();
        messageManager.saveDefaultMessages();
        
        // Инициализация компилятора и загрузчика
        scriptCompiler = new ScriptCompiler(this);
        scriptLoader = new ScriptLoader(this, scriptCompiler);
        
        // Регистрация команды перезагрузки
        if (getCommand("scriptreload") != null) {
            getCommand("scriptreload").setExecutor(new ReloadCommand(this));
        } else {
            getLogger().warning("Команда scriptreload не зарегистрирована в plugin.yml!");
        }
        
        // Загрузка скриптов
        scriptLoader.loadAllScripts();
        
        // Авто-перезагрузка если включена
        if (configManager.isAutoReload()) {
            scriptLoader.startAutoReloadTask();
        }
        
        // Вывод сообщения о запуске
        getLogger().info("=========================================");
        getLogger().info("ScriptCommands v" + getDescription().getVersion());
        getLogger().info("Авторы: gordey9992 & DeepSeek");
        getLogger().info("Состояние: §aВключен");
        getLogger().info("Папка со скриптами: " + scriptLoader.getScriptsFolder());
        getLogger().info("Авто-перезагрузка: " + (configManager.isAutoReload() ? "§aВкл" : "§cВыкл"));
        getLogger().info("=========================================");
        
        // Отправка сообщения в консоль с цветом через MessageManager
        messageManager.sendConsoleMessage("plugin.enabled", 
            new String[]{"{version}"}, 
            new String[]{getDescription().getVersion()});
    }

    @Override
    public void onDisable() {
        if (scriptLoader != null) {
            scriptLoader.stopAutoReloadTask();
            scriptLoader.unloadAllScripts();
        }
        
        messageManager.sendConsoleMessage("plugin.disabled");
        getLogger().info("ScriptCommands выключен");
    }
    
    public static ScriptCommands getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public ScriptLoader getScriptLoader() {
        return scriptLoader;
    }
    
    public ScriptCompiler getScriptCompiler() {
        return scriptCompiler;
    }
}
