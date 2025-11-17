package com.mrrezik.icrafts;

import com.mrrezik.icrafts.commands.ICraftsCommand;
import com.mrrezik.icrafts.listeners.GUIListener;
import com.mrrezik.icrafts.listeners.RecipeListener;
import com.mrrezik.icrafts.managers.ConfigManager;
import com.mrrezik.icrafts.managers.RecipeManager;
import com.mrrezik.icrafts.utils.GUIManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ICrafts extends JavaPlugin {

    private ConfigManager configManager;
    private RecipeManager recipeManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        getLogger().info("iCrafts v" + getDescription().getVersion() + " загружается...");

        // Инициализация менеджеров
        this.configManager = new ConfigManager(this);
        this.recipeManager = new RecipeManager(this);
        this.guiManager = new GUIManager(this);

        // Загрузка рецептов
        this.recipeManager.loadRecipes();

        // Регистрация команд
        getCommand("icrafts").setExecutor(new ICraftsCommand(this));

        // Регистрация слушателей событий
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        // 🔥 ИСПРАВЛЕНИЕ: Передаем два аргумента: ICrafts (this) и RecipeManager
        getServer().getPluginManager().registerEvents(new RecipeListener(this, recipeManager), this);

        getLogger().info("iCrafts включен.");
    }

    @Override
    public void onDisable() {
        // Сохранение данных перед выключением
        if (recipeManager != null) {
            recipeManager.saveRecipes();
        }
        getLogger().info("iCrafts выключен.");
    }

    // Геттеры
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}