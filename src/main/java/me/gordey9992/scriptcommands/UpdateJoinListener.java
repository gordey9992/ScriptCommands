package me.gordey9992.scriptcommands;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateJoinListener implements Listener {
    
    private final ScriptCommands plugin;
    
    public UpdateJoinListener(ScriptCommands plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getPendingUpdateVersion() == null) return;
        if (!player.hasPermission("scriptcommands.update.msg")) return;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("§e=========================================");
            player.sendMessage("§e[ScriptCommands] §6Доступна новая версия " + plugin.getPendingUpdateVersion() + "§e!");
            player.sendMessage("§eВведите §6/script update§e для обновления");
            player.sendMessage("§e=========================================");
        }, 40L);
    }
}
