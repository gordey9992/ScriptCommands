package me.gordey9992.scriptcommands;

import org.bukkit.command.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

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
            sender.sendMessage("§aНачинаю обновление ScriptCommands...");
            downloadAndUpdate(sender);
            
        } else if (cmd.getName().equalsIgnoreCase("scupdateno")) {
            plugin.setUpdateAsked(false);
            sender.sendMessage("§7Обновление пропущено. Напомню при следующем запуске сервера.");
        }
        
        return true;
    }
    
    private void downloadAndUpdate(CommandSender sender) {
        try {
            String latestVersion = plugin.getLatestVersion();
            String downloadUrl = "https://github.com/gordey9992/ScriptCommands/releases/download/" + latestVersion + "/ScriptCommands-" + latestVersion + ".jar";
            
            File pluginsDir = plugin.getDataFolder().getParentFile();
            File currentJar = new File(pluginsDir, "ScriptCommands-" + plugin.getDescription().getVersion() + ".jar");
            File tempJar = new File(pluginsDir, "ScriptCommands-update-temp.jar");
            
            sender.sendMessage("§eСкачивание " + downloadUrl);
            
            try (InputStream in = new URL(downloadUrl).openStream()) {
                Files.copy(in, tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            sender.sendMessage("§aФайл скачан, устанавливаю...");
            
            if (currentJar.exists()) {
                currentJar.delete();
            }
            Files.move(tempJar.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            sender.sendMessage("§aОбновление установлено! Перезагрузите сервер или выполните §6/restart§a.");
            
            plugin.setUpdateAsked(false);
            
        } catch (Exception e) {
            sender.sendMessage("§cОшибка обновления: " + e.getMessage());
            plugin.getLogger().severe("Ошибка обновления: " + e.getMessage());
        }
    }
}
