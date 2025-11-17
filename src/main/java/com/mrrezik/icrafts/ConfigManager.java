package com.mrrezik.icrafts.managers;

import com.mrrezik.icrafts.ICrafts;
import com.mrrezik.icrafts.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {

    private final ICrafts plugin;
    private FileConfiguration config;
    private FileConfiguration data;
    private File dataFile;
    private FileConfiguration messages;

    private File menusFolder;
    private final Map<String, YamlConfiguration> menuConfigs = new HashMap<>();

    public ConfigManager(ICrafts plugin) {
        this.plugin = plugin;
        setupFiles();
    }

    private void setupFiles() {
        // config.yml
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // data.yml
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать data.yml!");
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        // messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Настройка папки menus
        menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        copyDefaultMenu("anvil_gui.yml");
        copyDefaultMenu("workbench_gui.yml");
        copyDefaultMenu("furnace_gui.yml");
        loadMenuConfigs();
    }

    // 🔥 НОВЫЙ ПУБЛИЧНЫЙ МЕТОД ДЛЯ ПЕРЕЗАГРУЗКИ 🔥
    public void reloadConfigs() {
        this.setupFiles();
    }

    private void copyDefaultMenu(String fileName) {
        File targetFile = new File(menusFolder, fileName);
        if (!targetFile.exists()) {
            plugin.saveResource("menus/" + fileName, false);
        }
    }

    private void loadMenuConfigs() {
        menuConfigs.clear();
        File[] menuFiles = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (menuFiles != null) {
            for (File file : menuFiles) {
                try {
                    menuConfigs.put(file.getName().toLowerCase(), YamlConfiguration.loadConfiguration(file));
                } catch (Exception e) {
                    plugin.getLogger().warning("Ошибка при загрузке меню " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public YamlConfiguration getMenuConfig(String name) {
        return menuConfigs.get(name.toLowerCase());
    }

    public FileConfiguration getData() {
        return data;
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить data.yml!");
        }
    }

    public String getMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            return "§cMessage not found: " + path;
        }
        return ColorUtils.colorize(message);
    }

    public List<String> getMessageList(String path) {
        List<String> list = messages.getStringList(path);

        if (list.isEmpty()) {
            return Collections.singletonList("§cMessage list not found: " + path);
        }

        return list.stream()
                .map(ColorUtils::colorize)
                .collect(Collectors.toList());
    }
}