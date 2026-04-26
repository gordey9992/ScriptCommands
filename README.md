# ScriptCommands

**Авторы: gordey9992 & DeepSeek**

Динамический плагин для Minecraft (Paper/Purpur 1.21.11), который компилирует и загружает Java скрипты из папки `/files` как команды.

## Установка
1. Скачайте `.jar` файл
2. Поместите в папку `plugins/`
3. Перезапустите сервер

## Использование
1. Создайте папку `plugins/ScriptCommands/files/`
2. Положите туда `.java` файлы (каждый файл = одна команда)
3. Выполните `/scriptreload`

## Пример скрипта (`fly.java`)
```java
// Имя команды: fly
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            p.setAllowFlight(!p.getAllowFlight());
            p.sendMessage("§aРежим полета: " + (p.getAllowFlight() ? "включен" : "выключен"));
        }
        return true;
    }
}
