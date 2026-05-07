package me.gordey9992.scriptcommands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import javax.tools.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class ScriptCompiler {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    
    public ScriptCompiler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configManager = ((ScriptCommands) plugin).getConfigManager();
    }
    
    // ========== ИСПРАВЛЕННЫЙ ПОИСК СЕРВЕРНОГО JAR ==========
    private File findServerJar() {
        // Папка, где лежит плагин → plugins/
        File pluginsDir = plugin.getDataFolder().getParentFile();
        if (pluginsDir == null) {
            plugin.getLogger().warning("Не удалось найти папку plugins");
            return null;
        }
        
        // Корень сервера (на уровень выше plugins)
        File serverRoot = pluginsDir.getParentFile();
        if (serverRoot == null) {
            plugin.getLogger().warning("Не удалось найти корень сервера");
            return null;
        }
        
        plugin.getLogger().info("Поиск серверного JAR в: " + serverRoot.getAbsolutePath());
        
        // 1. Ищем purpur-*.jar
        File[] purpur = serverRoot.listFiles((dir, name) -> name.matches("purpur-.*\\.jar"));
        if (purpur != null && purpur.length > 0) {
            plugin.getLogger().info("Найден: " + purpur[0].getName());
            return purpur[0];
        }
        
        // 2. Ищем server.jar
        File serverJar = new File(serverRoot, "server.jar");
        if (serverJar.exists()) {
            plugin.getLogger().info("Найден: server.jar");
            return serverJar;
        }
        
        // 3. Ищем любой .jar
        File[] anyJar = serverRoot.listFiles((dir, name) -> name.endsWith(".jar"));
        if (anyJar != null && anyJar.length > 0) {
            plugin.getLogger().info("Найден любой JAR: " + anyJar[0].getName());
            return anyJar[0];
        }
        
        plugin.getLogger().warning("Серверный JAR НЕ НАЙДЕН в " + serverRoot.getAbsolutePath());
        plugin.getLogger().warning("Скрипты не скомпилируются!");
        return null;
    }
    
    public CommandExecutor compile(Path sourceFile, String commandName) {
        try {
            Path compiledFolder = plugin.getDataFolder().toPath().resolve(configManager.getTempFolder());
            Files.createDirectories(compiledFolder);
            
            // Проверка размера файла
            if (configManager.getMaxFileSize() > 0) {
                long fileSize = Files.size(sourceFile);
                if (fileSize > configManager.getMaxFileSize()) {
                    plugin.getLogger().warning("Файл " + sourceFile + " превышает максимальный размер!");
                    return null;
                }
            }
            
            // Читаем исходный код
            String sourceCode = Files.readString(sourceFile);
            
            // Проверка безопасности
            if (!isCodeSafe(sourceCode)) {
                plugin.getLogger().severe("Скрипт " + commandName + " содержит запрещенные классы!");
                return null;
            }
            
            // Подготавливаем код для компиляции
            String className = capitalize(commandName) + "Command";
            String fullClassName = "dynamic." + className;
            
            String fullCode = wrapInPackage(sourceCode, className);
            
            // Сохраняем временный файл
            Path tempFile = compiledFolder.resolve(className + ".java");
            Files.writeString(tempFile, fullCode);
            
            // Компилируем
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                plugin.getLogger().severe("Java компилятор не найден! Установите JDK на сервер.");
                return null;
            }
            
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(tempFile.toFile());
            
            // Находим серверный JAR для classpath
            File serverJar = findServerJar();
            List<String> options = new ArrayList<>();
            options.add("-d");
            options.add(compiledFolder.toString());
            
            if (serverJar != null && serverJar.exists()) {
                options.add("-cp");
                options.add(serverJar.getAbsolutePath());
                if (configManager.isDebugEnabled()) {
                    plugin.getLogger().info("Classpath: " + serverJar.getAbsolutePath());
                }
            }
            
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
            
            if (configManager.isLogCompilations()) {
                plugin.getLogger().info("Компиляция скрипта: " + commandName);
            }
            
            if (!task.call()) {
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    plugin.getLogger().severe("Ошибка компиляции " + commandName + ": " + diagnostic.getMessage(null));
                }
                fileManager.close();
                return null;
            }
            
            fileManager.close();
            
            // Загружаем скомпилированный класс
            URLClassLoader classLoader = new URLClassLoader(
                new URL[]{compiledFolder.toUri().toURL()}, 
                plugin.getClass().getClassLoader()
            );
            
            Class<?> clazz = classLoader.loadClass(fullClassName);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            
            if (instance instanceof CommandExecutor) {
                if (configManager.isLogCompilations()) {
                    plugin.getLogger().info("✓ Скрипт " + commandName + " успешно скомпилирован");
                }
                return (CommandExecutor) instance;
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка компиляции " + commandName + ": " + e.getMessage());
            if (configManager.isShowStacktraces()) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private String wrapInPackage(String sourceCode, String className) {
        // Удаляем package если есть
        sourceCode = sourceCode.replaceAll("package\\s+[\\w.]+;\\s*", "");
        
        // Добавляем необходимые импорты
        StringBuilder imports = new StringBuilder();
        imports.append("import org.bukkit.*;\n");
        imports.append("import org.bukkit.command.*;\n");
        imports.append("import org.bukkit.entity.*;\n");
        imports.append("import org.bukkit.inventory.*;\n");
        imports.append("import org.bukkit.potion.*;\n");
        imports.append("import org.bukkit.util.*;\n");
        
        // Исправляем имя класса
        String fixedCode = sourceCode.replaceAll(
            "public class \\w+Command", 
            "public class " + className
        );
        
        return "package dynamic;\n\n" + imports.toString() + "\n" + fixedCode;
    }
    
    private boolean isCodeSafe(String code) {
        List<String> blacklisted = configManager.getBlacklistedClasses();
        for (String black : blacklisted) {
            if (code.contains(black)) {
                plugin.getLogger().warning("Обнаружен запрещенный класс: " + black);
                return false;
            }
        }
        return true;
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "Command";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
