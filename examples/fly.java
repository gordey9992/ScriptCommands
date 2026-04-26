// Команда: /fly
// Включает/выключает режим полета

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            p.setAllowFlight(!p.getAllowFlight());
            p.setFlying(p.getAllowFlight());
            
            if (p.getAllowFlight()) {
                sender.sendMessage("§a✈ Режим полета включен!");
            } else {
                sender.sendMessage("§c✈ Режим полета выключен!");
            }
            return true;
        }
        sender.sendMessage("§c❌ Эта команда только для игроков!");
        return true;
    }
}
