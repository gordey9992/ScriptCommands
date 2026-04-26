package me.gordey9992.scriptcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;  // ← ДОБАВИТЬ ЭТУ СТРОКУ!
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptLoader {
    private final JavaPlugin plugin;
    private final ScriptCompiler compiler;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final Path scriptsFolder;
    private final Map<String, CommandExecutor> loadedCommands = new ConcurrentHashMap<>();
    private final Map<String, Long> scriptTimestamps = new HashMap<>();
    private int autoReloadTaskId = -1;
    
    public ScriptLoader(JavaPlugin plugin, ScriptCompiler compiler) {
        this.plugin = plugin;
        this.compiler = compiler;
        this.configManager = ((ScriptCommands) plugin).getConfigManager();
        this.messageManager = ((ScriptCommands) plugin).getMessageManager();
        this.scriptsFolder = plugin.getDataFolder().toPath().resolve(configManager.getScriptsFolder());
        createFolders();
    }
    
    private void createFolders() {
        try {
            if (Files.notExists(scriptsFolder)) {
                Files.createDirectories(scriptsFolder);
                messageManager.sendConsoleMessage("scripts.folder-created", 
                    new String[]{"{folder}"}, 
                    new String[]{scriptsFolder.toString()});
                createExampleScripts();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось создать папку: " + e.getMessage());
        }
    }
    
    private void createExampleScripts() {
        String flyScript = """
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
            """;
        
        String healScript = """
            import org.bukkit.command.*;
            import org.bukkit.entity.Player;
            
            public class HealCommand implements CommandExecutor {
                public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
                    if (sender instanceof Player p) {
                        p.setHealth(p.getMaxHealth());
                        p.setFoodLevel(20);
                        p.setSaturation(10);
                        sender.sendMessage("§aВы полностью исцелены!");
                        return true;
                    }
                    sender.sendMessage("§cТолько для игроков!");
                    return true;
                }
            }
            """;
        
        try {
            Path flyFile = scriptsFolder.resolve("fly.java");
            Path healFile = scriptsFolder.resolve("heal.java");
            
            if (Files.notExists(flyFile)) {
                Files.writeString(flyFile, flyScript);
                plugin.getLogger().info("Создан пример: fly.java");
            }
            if (Files.notExists(healFile)) {
                Files.writeString(healFile, healScript);
                plugin.getLogger().info("Создан пример: heal.java");
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось создать примеры скриптов");
        }
    }
    
    public void loadAllScripts() {
        long startTime = System.currentTimeMillis();
        unloadAllScripts();
        
        try {
            List<Path> javaFiles = Files.walk(scriptsFolder)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.getFileName().toString().startsWith("_"))
                .toList();
            
            if (javaFiles.isEmpty()) {
                plugin.getLogger().info("Скриптов не найдено. Примеры созданы: fly.java, heal.java");
                return;
            }
            
            int maxScripts = configManager.getMaxScripts();
            if (maxScripts > 0 && javaFiles.size() > maxScripts) {
                plugin.getLogger().warning("Количество скриптов (" + javaFiles.size() + 
                    ") превышает лимит (" + maxScripts + ")");
                return;
            }
            
            int loaded = 0;
            for (Path javaFile : javaFiles) {
                if (loadScript(javaFile)) {
                    loaded++;
                }
            }
            
            long time = System.currentTimeMillis() - startTime;
            plugin.getLogger().info("Загружено скриптов: " + loaded + " за " + time + "ms");
            
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сканирования: " + e.getMessage());
        }
    }
    
    private boolean loadScript(Path javaFile) {
        try {
            String fileName = javaFile.getFileName().toString();
            String commandName = fileName.replace(".java", "").toLowerCase();
            
            scriptTimestamps.put(commandName, Files.getLastModifiedTime(javaFile).toMillis());
            
            CommandExecutor executor = compiler.compile(javaFile, commandName);
            
            if (executor != null) {
                registerCommand(commandName, executor);
                loadedCommands.put(commandName, executor);
                plugin.getLogger().info("✓ Команда '/" + commandName + "' загружена из " + fileName);
                return true;
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("✗ Ошибка загрузки " + javaFile + ": " + e.getMessage());
            if (configManager.isShowStacktraces()) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    private void registerCommand(String commandName, CommandExecutor executor) {
        try {
            CommandMap commandMap = Bukkit.getCommandMap();
            PluginCommand command = plugin.getCommand(commandName);
            
            if (command == null) {
                // Создаем команду через рефлексию
                java.lang.reflect.Constructor<PluginCommand> constructor = 
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                constructor.setAccessible(true);
                command = constructor.newInstance(commandName, plugin);
                commandMap.register(plugin.getDescription().getName().toLowerCase(), command);
            }
            
            command.setExecutor(executor);
            command.setPermission(configManager.getPermissionPrefix() + "." + commandName);
            command.setPermissionMessage(messageManager.getRawMessage("general.no-permission"));
            command.setUsage(messageManager.getRawMessage("general.invalid-usage", 
                new String[]{"{usage}"}, new String[]{"/" + commandName}));
            command.setDescription("Динамическая команда из скрипта: " + commandName);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось зарегистрировать команду: " + commandName);
            if (configManager.isShowStacktraces()) {
                e.printStackTrace();
            }
        }
    }
    
    public void unloadAllScripts() {
        for (String cmdName : loadedCommands.keySet()) {
            try {
                CommandMap commandMap = Bukkit.getCommandMap();
                java.lang.reflect.Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                
                @SuppressWarnings("unchecked")
                Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
                
                knownCommands.remove(cmdName);
                knownCommands.remove(plugin.getDescription().getName().toLowerCase() + ":" + cmdName);
                
                PluginCommand command = plugin.getCommand(cmdName);
                if (command != null) {
                    command.setExecutor(null);
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка выгрузки команды: " + cmdName);
            }
        }
        loadedCommands.clear();
        scriptTimestamps.clear();
        plugin.getLogger().info("Все скрипты выгружены");
    }
    
    public void startAutoReloadTask() {
        if (autoReloadTaskId != -1) return;
        
        int interval = configManager.getAutoReloadInterval();
        autoReloadTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            checkForChanges();
        }, interval * 20L, interval * 20L);
        
        plugin.getLogger().info("Авто-перезагрузка запущена (интервал: " + interval + " сек)");
    }
    
    public void stopAutoReloadTask() {
        if (autoReloadTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autoReloadTaskId);
            autoReloadTaskId = -1;
        }
    }
    
    private void checkForChanges() {
        try {
            List<Path> javaFiles = Files.walk(scriptsFolder)
                .filter(p -> p.toString().endsWith(".java"))
                .toList();
            
            boolean needReload = false;
            
            for (Path javaFile : javaFiles) {
                String name = javaFile.getFileName().toString().replace(".java", "").toLowerCase();
                long currentTime = Files.getLastModifiedTime(javaFile).toMillis();
                Long savedTime = scriptTimestamps.get(name);
                
                if (savedTime == null || currentTime > savedTime) {
                    needReload = true;
                    break;
                }
            }
            
            if (needReload) {
                plugin.getLogger().info("Обнаружены изменения в скриптах, перезагрузка...");
                loadAllScripts();
            }
            
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка проверки изменений: " + e.getMessage());
        }
    }
    
    public Path getScriptsFolder() {
        return scriptsFolder;
    }
    
    public Map<String, CommandExecutor> getLoadedCommands() {
        return Collections.unmodifiableMap(loadedCommands);
    }
}
