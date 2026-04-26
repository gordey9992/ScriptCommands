// Команда: /heal
// Полное исцеление игрока

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class HealCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            // Исцеление
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setSaturation(20);
            
            // Снятие эффектов
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            
            sender.sendMessage("§a❤ Вы полностью исцелены!");
            sender.sendMessage("§e🍗 Голод восстановлен!");
            return true;
        }
        sender.sendMessage("§c❌ Эта команда только для игроков!");
        return true;
    }
}
