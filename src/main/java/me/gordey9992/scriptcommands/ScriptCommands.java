package me.gordey9992.scriptcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class ScriptCommands extends JavaPlugin {
    
    private static ScriptCommands instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ScriptLoader scriptLoader;
    private ScriptCompiler scriptCompiler;
    private String latestVersion = null;
    private boolean updateAsked = false;
    
    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        scriptCompiler = new ScriptCompiler(this);
        scriptLoader = new ScriptLoader(this, scriptCompiler);
        
        configManager.saveDefaultConfig();
        messageManager.saveDefaultMessages();
        
        // Регистрация команд
        PluginCommand cmdScriptReload = getCommand("scriptreload");
        if (cmdScriptReload != null) {
            cmdScriptReload.setExecutor(new ReloadCommand(this));
        }
        
        PluginCommand cmdSCReload = getCommand("screload");
        if (cmdSCReload != null) {
            cmdSCReload.setExecutor(new ReloadCommand(this));
        }
        
        PluginCommand cmdUpdateYes = getCommand("scupdateyes");
        if (cmdUpdateYes != null) {
            cmdUpdateYes.setExecutor(new UpdateCommand(this));
        }
        
        PluginCommand cmdUpdateNo = getCommand("scupdateno");
        if (cmdUpdateNo != null) {
            cmdUpdateNo.setExecutor(new UpdateCommand(this));
        }
        
        scriptLoader.loadAllScripts();
        
        getLogger().info("=========================================");
        getLogger().info("ScriptCommands v" + getDescription().getVersion());
        getLogger().info("Авторы: gordey9992 & DeepSeek");
        getLogger().info("Папка со скриптами: " + configManager.getScriptsFolder());
        getLogger().info("=========================================");
        
        // Проверка обновлений
        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);
    }
    
    @Override
    public void onDisable() {
        if (scriptLoader != null) {
            scriptLoader.unloadAllScripts();
        }
    }
    
    private void checkForUpdates() {
        try {
            String currentVersion = getDescription().getVersion();
            URL url = new URL("https://api.github.com/repos/gordey9992/ScriptCommands/releases/latest");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String json = reader.lines().collect(Collectors.joining());
            reader.close();
            
            JSONObject obj = new JSONObject(json);
            latestVersion = obj.getString("tag_name");
            
            if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                updateAsked = true;
                Bukkit.getScheduler().runTask(this, () -> {
                    getLogger().info("§e=========================================");
                    getLogger().info("§eДоступна новая версия §6" + latestVersion + "§e! (текущая: " + currentVersion + ")");
                    getLogger().info("§eВыполните §6/scupdateyes§e для обновления");
                    getLogger().info("§eИли §6/scupdateno§e чтобы пропустить (напомню при следующем запуске)");
                    getLogger().info("§e=========================================");
                });
            }
        } catch (Exception e) {
            getLogger().warning("Не удалось проверить обновления: " + e.getMessage());
        }
    }
    
    public boolean isUpdateAsked() { return updateAsked; }
    public void setUpdateAsked(boolean asked) { updateAsked = asked; }
    public String getLatestVersion() { return latestVersion; }
    
    public static ScriptCommands getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ScriptLoader getScriptLoader() { return scriptLoader; }
}
