# ScriptCommands

**Авторы: gordey9992 & DeepSeek**

Динамический плагин для Minecraft (Paper/Purpur 1.21.11), который компилирует и загружает Java скрипты из папки `/files` как команды.

## ✨ Возможности
- 📝 Создавайте команды без перезагрузки сервера
- 🔧 Поддержка конфигурации (config.yml)
- 📨 Настраиваемые сообщения (messages.yml)
- 🛡️ Безопасность: ограничение доступа к опасным классам
- 🔄 Автоматическая перезагрузка скриптов

## 📥 Установка
1. Скачайте `.jar` файл из релизов
2. Поместите в папку `plugins/`
3. Перезапустите сервер
4. Настройте `config.yml` и `messages.yml` при необходимости

## 🎮 Использование

### Создание команды
1. Создайте файл в `plugins/ScriptCommands/files/`
2. Название файла = название команды (например `fly.java`)
3. Напишите код команды
4. Выполните `/scriptreload`

### Пример команды `fly.java`
```java
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            p.setAllowFlight(!p.getAllowFlight());
            sender.sendMessage("§aРежим полета: " + (p.getAllowFlight() ? "§aвключен" : "§cвыключен"));
            return true;
        }
        sender.sendMessage("§cТолько для игроков!");
        return true;
    }
}
