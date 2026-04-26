// Команда: /speed <скорость>
// Меняет скорость ходьбы

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            if (args.length < 1) {
                sender.sendMessage("§cИспользование: /speed <1-10>");
                return true;
            }
            
            try {
                int speed = Integer.parseInt(args[0]);
                if (speed < 1 || speed > 10) {
                    sender.sendMessage("§cСкорость должна быть от 1 до 10!");
                    return true;
                }
                
                float walkSpeed = speed / 10.0f;
                p.setWalkSpeed(walkSpeed);
                sender.sendMessage("§aСкорость изменена на " + speed + "!");
                
            } catch (NumberFormatException e) {
                sender.sendMessage("§cВведите число от 1 до 10!");
            }
            return true;
        }
        sender.sendMessage("§cТолько для игроков!");
        return true;
    }
}
