package me.gordey9992.scriptcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.stream.Collectors;

public class ScriptCommands extends JavaPlugin {
    
    private static ScriptCommands instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ScriptLoader scriptLoader;
    private ScriptCompiler scriptCompiler;
    private boolean updateAsked = false;
    private String latestVersion = null;
    private String pendingUpdateUrl = null;
    
    private String pendingUpdateUrl = null;
    private String pendingUpdateVersion = null;
    
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
        
        PluginCommand cmdScript = getCommand("script");
        if (cmdScript != null) {
            cmdScript.setExecutor(new ScriptCommand(this));
        }
        
        scriptLoader.loadAllScripts();
        
        getLogger().info("=========================================");
        getLogger().info("ScriptCommands v" + getDescription().getVersion());
        getLogger().info("Авторы: gordey9992 & DeepSeek");
        getLogger().info("Папка со скриптами: " + configManager.getScriptsFolder());
        getLogger().info("=========================================");
        
        // Проверка обновлений при старте
        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);
    }
    
    @Override
    public void onDisable() {
        if (scriptLoader != null) {
            scriptLoader.unloadAllScripts();
        }
    }
    
    // ========== АВТООБНОВЛЕНИЕ ==========
    
    private void checkForUpdates() {
        try {
            String currentVersion = getDescription().getVersion();
            URL url = new URL("https://api.github.com/repos/gordey9992/ScriptCommands/releases/latest");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String json = reader.lines().collect(Collectors.joining());
            reader.close();
            
            JSONObject obj = new JSONObject(json);
            String latestVersion = obj.getString("tag_name");
            
            if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                String downloadUrl = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                
                Bukkit.getScheduler().runTask(this, () -> {
                    getLogger().info("§e=========================================");
                    getLogger().info("§eДоступна новая версия §6" + latestVersion + "§e!");
                    getLogger().info("§eДля обновления введите §6/script update§e");
                    getLogger().info("§e=========================================");
                    
                    // Уведомление игроков с правом
                    Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("scriptcommands.update.msg"))
                        .forEach(p -> p.sendMessage("§e[ScriptCommands] Доступна новая версия " + latestVersion + "! Введите §6/script update§e"));
                    
                    pendingUpdateUrl = downloadUrl;
                    pendingUpdateVersion = latestVersion;
                });
            }
        } catch (Exception e) {
            getLogger().warning("Не удалось проверить обновления: " + e.getMessage());
        }
    }
    
    public void performUpdate(org.bukkit.command.CommandSender sender) {
        if (pendingUpdateUrl == null) {
            sender.sendMessage("§cНет ожидающих обновлений!");
            return;
        }
        
        sender.sendMessage("§aНачинаю обновление до версии " + pendingUpdateVersion + "...");
        
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                // Скачиваем новый JAR во временный файл
                File pluginsDir = getDataFolder().getParentFile();
                File tempJar = new File(pluginsDir, "ScriptCommands-update-temp.jar");
                try (InputStream in = new URL(pendingUpdateUrl).openStream()) {
                    Files.copy(in, tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                
                // Находим текущий JAR
                File currentJar = null;
                for (File f : pluginsDir.listFiles()) {
                    if (f.getName().startsWith("ScriptCommands") && f.getName().endsWith(".jar") && !f.getName().contains("update")) {
                        currentJar = f;
                        break;
                    }
                }
                
                if (currentJar == null) {
                    sender.sendMessage("§cНе найден текущий JAR плагина!");
                    return;
                }
                
                // Заменяем JAR
                Files.move(tempJar.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                sender.sendMessage("§aФайл обновлён! Перезагружаю плагин...");
                
                // Перезагружаем плагин
                Bukkit.getScheduler().runTask(this, () -> {
                    Bukkit.getPluginManager().disablePlugin(this);
                    Bukkit.getPluginManager().enablePlugin(this);
                    sender.sendMessage("§aПлагин обновлён до версии " + pendingUpdateVersion + "!");
                });
                
                pendingUpdateUrl = null;
                pendingUpdateVersion = null;
                
            } catch (Exception e) {
                sender.sendMessage("§cОшибка обновления: " + e.getMessage());
                getLogger().severe("Ошибка обновления: " + e.getMessage());
            }
        });
    }
    
    public String getPendingUpdateVersion() { return pendingUpdateVersion; }
    
    public static ScriptCommands getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ScriptLoader getScriptLoader() { return scriptLoader; }
    public boolean isUpdateAsked() { return updateAsked; }
    public void setUpdateAsked(boolean asked) { this.updateAsked = asked; }
    public String getLatestVersion() { return latestVersion; }
    public String getPendingUpdateUrl() { return pendingUpdateUrl; }
    public void setPendingUpdateUrl(String url) { this.pendingUpdateUrl = url; }
    public String getPendingUpdateVersion() { return pendingUpdateVersion; }
}
