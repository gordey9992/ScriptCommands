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
    
    // Compiler settings
    public String getScriptsFolder() {
        return getConfig().getString("compiler.scripts-folder", "files");
    }
    
    public boolean isAutoReload() {
        return getConfig().getBoolean("compiler.auto-reload", false);
    }
    
    public int getAutoReloadInterval() {
        return getConfig().getInt("compiler.auto-reload-interval", 30);
    }
    
    public String getTempFolder() {
        return getConfig().getString("compiler.temp-folder", "compiled");
    }
    
    // Command settings
    public String getPermissionPrefix() {
        return getConfig().getString("commands.permission-prefix", "scriptcommands.use");
    }
    
    public boolean isAllowConsole() {
        return getConfig().getBoolean("commands.allow-console", true);
    }
    
    public boolean isAllowCommandBlock() {
        return getConfig().getBoolean("commands.allow-command-block", false);
    }
    
    // Security settings
    public List<String> getAllowedPackages() {
        return getConfig().getStringList("security.allowed-packages");
    }
    
    public List<String> getBlacklistedClasses() {
        return getConfig().getStringList("security.blacklisted-classes");
    }
    
    public long getMaxExecutionTime() {
        return getConfig().getLong("security.max-execution-time", 5000);
    }
    
    // Debug settings
    public boolean isDebugEnabled() {
        return getConfig().getBoolean("debug.enabled", false);
    }
    
    public boolean isShowStacktraces() {
        return getConfig().getBoolean("debug.show-stacktraces", true);
    }
    
    public boolean isLogCompilations() {
        return getConfig().getBoolean("debug.log-compilations", false);
    }
    
    // Limits
    public int getMaxScripts() {
        return getConfig().getInt("limits.max-scripts", 0);
    }
    
    public long getMaxFileSize() {
        return getConfig().getLong("limits.max-file-size", 0);
    }
}
