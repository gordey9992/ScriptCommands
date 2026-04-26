// Команда: /god
// Режим бессмертия

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GodCommand implements CommandExecutor {
    private final Map<UUID, Boolean> godMode = new HashMap<>();
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            boolean current = godMode.getOrDefault(p.getUniqueId(), false);
            boolean newState = !current;
            
            godMode.put(p.getUniqueId(), newState);
            p.setInvulnerable(newState);
            
            if (newState) {
                sender.sendMessage("§a🛡 Режим бога ВКЛЮЧЕН!");
            } else {
                sender.sendMessage("§c🛡 Режим бога ВЫКЛЮЧЕН!");
            }
            return true;
        }
        sender.sendMessage("§cТолько для игроков!");
        return true;
    }
}
