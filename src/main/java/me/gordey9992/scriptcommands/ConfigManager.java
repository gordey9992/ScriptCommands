package me.gordey9992.scriptcommands;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.List;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    
    // Поля для доступа из других классов
    private String scriptsFolder;
    private boolean autoReload;
    private int autoReloadInterval;
    private String tempFolder;
    private String permissionPrefix;
    private boolean allowConsole;
    private boolean allowCommandBlock;
    private List<String> allowedPackages;
    private List<String> blacklistedClasses;
    private long maxExecutionTime;
    private boolean debugEnabled;
    private boolean showStacktraces;
    private boolean logCompilations;
    private int maxScripts;
    private long maxFileSize;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        reloadConfig();
    }
    
    public void reloadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Загружаем значения в поля
        scriptsFolder = config.getString("compiler.scripts-folder", "files");
        autoReload = config.getBoolean("compiler.auto-reload", false);
        autoReloadInterval = config.getInt("compiler.auto-reload-interval", 30);
        tempFolder = config.getString("compiler.temp-folder", "compiled");
        permissionPrefix = config.getString("commands.permission-prefix", "scriptcommands.use");
        allowConsole = config.getBoolean("commands.allow-console", true);
        allowCommandBlock = config.getBoolean("commands.allow-command-block", false);
        allowedPackages = config.getStringList("security.allowed-packages");
        blacklistedClasses = config.getStringList("security.blacklisted-classes");
        maxExecutionTime = config.getLong("security.max-execution-time", 5000);
        debugEnabled = config.getBoolean("debug.enabled", false);
        showStacktraces = config.getBoolean("debug.show-stacktraces", true);
        logCompilations = config.getBoolean("debug.log-compilations", false);
        maxScripts = config.getInt("limits.max-scripts", 0);
        maxFileSize = config.getLong("limits.max-file-size", 0);
    }
    
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }
    
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }
    
    // Геттеры
    public String getScriptsFolder() { return scriptsFolder; }
    public boolean isAutoReload() { return autoReload; }
    public int getAutoReloadInterval() { return autoReloadInterval; }
    public String getTempFolder() { return tempFolder; }
    public String getPermissionPrefix() { return permissionPrefix; }
    public boolean isAllowConsole() { return allowConsole; }
    public boolean isAllowCommandBlock() { return allowCommandBlock; }
    public List<String> getAllowedPackages() { return allowedPackages; }
    public List<String> getBlacklistedClasses() { return blacklistedClasses; }
    public long getMaxExecutionTime() { return maxExecutionTime; }
    public boolean isDebugEnabled() { return debugEnabled; }
    public boolean isShowStacktraces() { return showStacktraces; }
    public boolean isLogCompilations() { return logCompilations; }
    public int getMaxScripts() { return maxScripts; }
    public long getMaxFileSize() { return maxFileSize; }
}
