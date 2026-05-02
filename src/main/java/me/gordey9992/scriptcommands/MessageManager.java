package me.gordey9992.scriptcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class MessageManager {
    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private File messagesFile;
    private String prefix;
    
    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        reloadMessages();
    }
    
public void reloadMessages() {
    if (messagesFile == null) {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
    }
    messages = YamlConfiguration.loadConfiguration(messagesFile);
    prefix = colorize(messages.getString("prefix", "&8[&6ScriptCommands&8] &7"));
}
    
    public void saveDefaultMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }
    
    public String getMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            return colorize("&cСообщение не найдено: " + path);
        }
        return colorize(prefix + message);
    }
    
    public String getMessage(String path, String[] placeholders, String[] replacements) {
        String message = getMessage(path);
        for (int i = 0; i < placeholders.length && i < replacements.length; i++) {
            message = message.replace(placeholders[i], replacements[i]);
        }
        return message;
    }
    
    public String getRawMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            return "Message not found: " + path;
        }
        return colorize(message);
    }
    
    public String getRawMessage(String path, String[] placeholders, String[] replacements) {
        String message = getRawMessage(path);
        for (int i = 0; i < placeholders.length && i < replacements.length; i++) {
            message = message.replace(placeholders[i], replacements[i]);
        }
        return message;
    }
    
    public void sendMessage(CommandSender sender, String path) {
        if (sender != null) {
            sender.sendMessage(getMessage(path));
        }
    }
    
    public void sendMessage(CommandSender sender, String path, String[] placeholders, String[] replacements) {
        if (sender != null) {
            sender.sendMessage(getMessage(path, placeholders, replacements));
        }
    }
    
    public void sendConsoleMessage(String path) {
        plugin.getLogger().info(stripColor(getMessage(path)));
    }
    
    public void sendConsoleMessage(String path, String[] placeholders, String[] replacements) {
        plugin.getLogger().info(stripColor(getMessage(path, placeholders, replacements)));
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    private String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    private String stripColor(String message) {
        if (message == null) return "";
        return ChatColor.stripColor(colorize(message));
    }
}
