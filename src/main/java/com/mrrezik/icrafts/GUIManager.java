package com.mrrezik.icrafts.utils;

import com.mrrezik.icrafts.ICrafts;
import com.mrrezik.icrafts.objects.CustomRecipe;
import com.mrrezik.icrafts.objects.RecipeType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class GUIManager {

    private final ICrafts plugin;

    public static final String GUI_TAG = ChatColor.BLACK + "" + ChatColor.RESET;
    public static final String CREATE_MODE = "Create";
    public static final String EDIT_MODE = "Edit";
    public static final String VIEW_MODE = "View";

    // Слоты 3x3 жестко заданы для логики Верстака
    private static final int[] WORKBENCH_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    public GUIManager(ICrafts plugin) {
        this.plugin = plugin;
    }

    public void openCreator(Player player, RecipeType type, String name) {
        String title = buildTitle(CREATE_MODE, type, name);
        Inventory gui = createGUI(type, title);
        player.openInventory(gui);
    }

    public void openEditor(Player player, CustomRecipe recipe) {
        String title = buildTitle(EDIT_MODE, recipe.getType(), recipe.getName());
        Inventory gui = createGUI(recipe.getType(), title);
        fillGUI(gui, recipe);
        player.openInventory(gui);
    }

    public void openViewer(Player player, CustomRecipe recipe) {
        String title = buildTitle(VIEW_MODE, recipe.getType(), recipe.getName());
        Inventory gui = createGUI(recipe.getType(), title);
        fillGUI(gui, recipe);
        player.openInventory(gui);
    }

    private String buildTitle(String mode, RecipeType type, String name) {
        YamlConfiguration config = plugin.getConfigManager().getMenuConfig(type.name().toLowerCase() + "_gui.yml");

        // 🔥 Берем только чистое название из конфига и раскрашиваем
        String baseTitle = ColorUtils.colorize(config != null ? config.getString("title", "&8Кастомный Крафт") : "&8Кастомный Крафт");

        // 🔥 Добавляем служебный тег в конец.
        return baseTitle + GUI_TAG + mode + ":" + type.name() + ":" + name;
    }

    private void fillGUI(Inventory gui, CustomRecipe recipe) {
        ItemStack[] ingredients = recipe.getIngredients();
        switch (recipe.getType()) {
            case WORKBENCH:
                for (int i = 0; i < 9; i++) {
                    gui.setItem(WORKBENCH_SLOTS[i], ingredients[i]);
                }
                gui.setItem(getSlotFromConfig(RecipeType.WORKBENCH, "WORKBENCH_RESULT_SLOT"), recipe.getResult());
                break;
            case FURNACE:
                gui.setItem(getSlotFromConfig(RecipeType.FURNACE, "FURNACE_INPUT_SLOT"), ingredients[0]);
                gui.setItem(getSlotFromConfig(RecipeType.FURNACE, "FURNACE_RESULT_SLOT"), recipe.getResult());
                break;
            case ANVIL:
                gui.setItem(getSlotFromConfig(RecipeType.ANVIL, "ANVIL_LEFT_SLOT"), ingredients[0]);
                gui.setItem(getSlotFromConfig(RecipeType.ANVIL, "ANVIL_RIGHT_SLOT"), ingredients[1]);
                gui.setItem(getSlotFromConfig(RecipeType.ANVIL, "ANVIL_RESULT_SLOT"), recipe.getResult());
                // Заполняем слот XP
                gui.setItem(getSlotFromConfig(RecipeType.ANVIL, "ANVIL_XP_SLOT"), createXpItem(recipe.getXpCost()));
                break;
        }
    }

    private Inventory createGUI(RecipeType type, String title) {
        Inventory gui;
        String fileName;

        // Определяем имя файла конфига по типу рецепта
        switch (type) {
            case WORKBENCH: fileName = "workbench_gui.yml"; break;
            case FURNACE: fileName = "furnace_gui.yml"; break;
            case ANVIL: fileName = "anvil_gui.yml"; break;
            default: return Bukkit.createInventory(null, 27, title);
        }

        YamlConfiguration menuConfig = plugin.getConfigManager().getMenuConfig(fileName);
        if (menuConfig == null) {
            plugin.getLogger().warning("Не удалось загрузить меню для " + type.name() + " из файла " + fileName + "!");
            return Bukkit.createInventory(null, 27, title); // Запасной вариант
        }

        int size = menuConfig.getInt("size", 27);
        gui = Bukkit.createInventory(null, size, title);

        // 1. Заполнитель
        Material fillerMat = Material.getMaterial(menuConfig.getString("filler.material", "GRAY_STAINED_GLASS_PANE"));
        String fillerName = ColorUtils.colorize(menuConfig.getString("filler.name", " "));
        ItemStack filler = createNamedItem(fillerMat, fillerName);

        for (int i = 0; i < gui.getSize(); i++) gui.setItem(i, filler);

        // 2. Очищаем интерактивные слоты
        if (type == RecipeType.WORKBENCH) {
            for (int slot : WORKBENCH_SLOTS) gui.setItem(slot, null);
            gui.setItem(getSlotFromConfig(type, "WORKBENCH_RESULT_SLOT"), null);
        } else if (type == RecipeType.FURNACE) {
            gui.setItem(getSlotFromConfig(type, "FURNACE_INPUT_SLOT"), null);
            gui.setItem(getSlotFromConfig(type, "FURNACE_RESULT_SLOT"), null);
        } else if (type == RecipeType.ANVIL) {
            gui.setItem(getSlotFromConfig(type, "ANVIL_LEFT_SLOT"), null);
            gui.setItem(getSlotFromConfig(type, "ANVIL_RIGHT_SLOT"), null);
            gui.setItem(getSlotFromConfig(type, "ANVIL_RESULT_SLOT"), null);

            // Слот опыта (специфичен для ANVIL)
            gui.setItem(getSlotFromConfig(type, "ANVIL_XP_SLOT"), createXpItem(1));
        }

        // 3. Декорации
        ConfigurationSection decorations = menuConfig.getConfigurationSection("decorations");
        if (decorations != null) {
            for (String slotKey : decorations.getKeys(false)) {
                try {
                    int decoSlot = Integer.parseInt(slotKey);
                    Material decoMat = Material.getMaterial(decorations.getString(slotKey + ".material", "STONE"));
                    String decoName = ColorUtils.colorize(decorations.getString(slotKey + ".name", "Декорация"));
                    gui.setItem(decoSlot, createNamedItem(decoMat, decoName));
                } catch (NumberFormatException ignored) {}
            }
        }

        // 4. Кнопка сохранения
        int saveSlot = menuConfig.getInt("slots.SAVE_SLOT", -1);
        ItemStack saveButton = createNamedItem(Material.LIME_STAINED_GLASS_PANE,
                plugin.getConfigManager().getMessage("gui.item-save"),
                plugin.getConfigManager().getMessage("gui.item-save-lore"));
        if (saveSlot != -1) gui.setItem(saveSlot, saveButton);

        return gui;
    }

    private ItemStack createNamedItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize(name));
            if (lore.length > 0) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ColorUtils.colorize(line));
                }
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createXpItem(int cost) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        item.setAmount(Math.max(1, cost));

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtils.colorize(plugin.getConfigManager().getMessage("gui.anvil-xp-name")));
        meta.setLore(Collections.singletonList(formatXpLore(item.getAmount())));
        item.setItemMeta(meta);
        return item;
    }

    public static String formatXpLore(int cost) {
        return ColorUtils.colorize(ICrafts.getPlugin(ICrafts.class).getConfigManager().getMessage("gui.anvil-xp-lore")
                .replace("%cost%", String.valueOf(cost)));
    }

    public static String[] parseTitle(String title) {
        String tag = title.substring(title.indexOf(GUI_TAG) + GUI_TAG.length());
        String[] parts = tag.split(":");
        if (parts.length == 3) {
            return parts; // [Mode, Type, Name]
        }
        return null;
    }

    public static int getSlotFromConfig(RecipeType type, String key) {
        String fileName;
        switch (type) {
            case WORKBENCH: fileName = "workbench_gui.yml"; break;
            case FURNACE: fileName = "furnace_gui.yml"; break;
            case ANVIL: fileName = "anvil_gui.yml"; break;
            default: return -1;
        }

        YamlConfiguration menuConfig = ICrafts.getPlugin(ICrafts.class).getConfigManager().getMenuConfig(fileName);
        if (menuConfig == null) {
            // Аварийный откат
            switch (key) {
                case "WORKBENCH_RESULT_SLOT": return 25;
                case "FURNACE_INPUT_SLOT": return 11;
                case "FURNACE_RESULT_SLOT": return 15;
                case "ANVIL_XP_SLOT": return 14;
                case "ANVIL_RESULT_SLOT": return 16;
                case "SAVE_SLOT": return type == RecipeType.WORKBENCH ? 49 : 22;
                default: return -1;
            }
        }
        return menuConfig.getInt("slots." + key, -1);
    }

    public static boolean isGUISlot(RecipeType type, int slot) {
        if (type == RecipeType.WORKBENCH) {
            if (slot == getSlotFromConfig(type, "WORKBENCH_RESULT_SLOT")) return true;
            for (int s : WORKBENCH_SLOTS) if (s == slot) return true;
            return false;
        } else if (type == RecipeType.FURNACE) {
            return slot == getSlotFromConfig(type, "FURNACE_INPUT_SLOT") || slot == getSlotFromConfig(type, "FURNACE_RESULT_SLOT");
        } else if (type == RecipeType.ANVIL) {
            return slot == getSlotFromConfig(type, "ANVIL_LEFT_SLOT")
                    || slot == getSlotFromConfig(type, "ANVIL_RIGHT_SLOT")
                    || slot == getSlotFromConfig(type, "ANVIL_RESULT_SLOT")
                    || slot == getSlotFromConfig(type, "ANVIL_XP_SLOT");
        }
        return false;
    }

    public static boolean isSaveButton(RecipeType type, int slot) {
        return slot == getSlotFromConfig(type, "SAVE_SLOT");
    }

    public static CustomRecipe buildRecipeFromGUI(Inventory gui, String name, RecipeType type) {
        ItemStack result = null;
        ItemStack[] ingredients = null;
        int xpCost = 0;

        switch (type) {
            case WORKBENCH:
                ingredients = new ItemStack[9];
                for (int i = 0; i < 9; i++) {
                    ingredients[i] = gui.getItem(WORKBENCH_SLOTS[i]);
                }
                result = gui.getItem(getSlotFromConfig(type, "WORKBENCH_RESULT_SLOT"));
                break;
            case FURNACE:
                ingredients = new ItemStack[1];
                ingredients[0] = gui.getItem(getSlotFromConfig(type, "FURNACE_INPUT_SLOT"));
                result = gui.getItem(getSlotFromConfig(type, "FURNACE_RESULT_SLOT"));
                break;
            case ANVIL:
                ingredients = new ItemStack[2];
                ingredients[0] = gui.getItem(getSlotFromConfig(type, "ANVIL_LEFT_SLOT"));
                ingredients[1] = gui.getItem(getSlotFromConfig(type, "ANVIL_RIGHT_SLOT"));
                result = gui.getItem(getSlotFromConfig(type, "ANVIL_RESULT_SLOT"));

                ItemStack xpItem = gui.getItem(getSlotFromConfig(type, "ANVIL_XP_SLOT"));

                if (xpItem != null && xpItem.getType() == Material.EXPERIENCE_BOTTLE) {
                    xpCost = Math.max(1, xpItem.getAmount());
                } else {
                    xpCost = 1;
                }
                break;
        }

        if(result == null || result.getType() == Material.AIR) return null;

        return new CustomRecipe(name, type, result, ingredients, xpCost);
    }
}