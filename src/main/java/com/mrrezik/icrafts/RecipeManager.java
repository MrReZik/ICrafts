package com.mrrezik.icrafts.managers;

import com.mrrezik.icrafts.ICrafts;
import com.mrrezik.icrafts.objects.CustomRecipe;
import com.mrrezik.icrafts.objects.RecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class RecipeManager {

    private final ICrafts plugin;
    private final Map<String, CustomRecipe> recipes = new HashMap<>();

    public RecipeManager(ICrafts plugin) {
        this.plugin = plugin;
    }

    public void loadRecipes() {
        recipes.clear();
        FileConfiguration data = plugin.getConfigManager().getData();
        ConfigurationSection recipesSection = data.getConfigurationSection("recipes");

        if (recipesSection == null) return;

        for (String key : recipesSection.getKeys(false)) {
            try {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(key);

                String typeString = recipeSection.getString("type");
                RecipeType type = RecipeType.valueOf(typeString.toUpperCase());

                ItemStack result = recipeSection.getItemStack("result");

                // Ингредиенты хранятся как список ItemStack
                List<?> ingredientsList = recipeSection.getList("ingredients");
                ItemStack[] ingredients = null;
                if (ingredientsList != null) {
                    ingredients = ingredientsList.toArray(new ItemStack[0]);
                }

                int xpCost = recipeSection.getInt("xp_cost", 1); // Для Наковальни

                CustomRecipe recipe = new CustomRecipe(key, type, result, ingredients, xpCost);
                recipes.put(key, recipe);

            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при загрузке рецепта '" + key + "': " + e.getMessage());
            }
        }
        plugin.getLogger().info("Загружено " + recipes.size() + " кастомных рецептов.");
    }

    public void saveRecipes() {
        FileConfiguration data = plugin.getConfigManager().getData();
        data.set("recipes", null); // Очищаем старую секцию

        for (CustomRecipe recipe : recipes.values()) {
            String path = "recipes." + recipe.getName();
            data.set(path + ".type", recipe.getType().name());
            data.set(path + ".result", recipe.getResult());

            // Сохраняем ингредиенты как List
            if (recipe.getIngredients() != null) {
                data.set(path + ".ingredients", Arrays.asList(recipe.getIngredients()));
            }

            if (recipe.getType() == RecipeType.ANVIL) {
                data.set(path + ".xp_cost", recipe.getXpCost());
            }
        }
        plugin.getConfigManager().saveData();
    }

    public void saveRecipe(CustomRecipe recipe) {
        recipes.put(recipe.getName(), recipe);
        saveRecipes();
    }

    public void deleteRecipe(String name) {
        recipes.remove(name);
        saveRecipes();
    }

    public CustomRecipe getRecipe(String name) {
        return recipes.get(name);
    }

    // --- 🔥 НОВЫЕ МЕТОДЫ ПОИСКА РЕЦЕПТОВ 🔥 ---

    // 1. Поиск рецепта Верстака
    public CustomRecipe findWorkbenchRecipe(ItemStack[] matrix) {
        // Нормализация матрицы: замена null на Material.AIR
        ItemStack[] normalizedMatrix = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            normalizedMatrix[i] = matrix[i] != null ? matrix[i] : new ItemStack(Material.AIR);
        }

        for (CustomRecipe recipe : recipes.values()) {
            if (recipe.getType() == RecipeType.WORKBENCH) {
                ItemStack[] required = recipe.getIngredients();

                if (required == null || required.length != 9) continue;

                // Сравниваем матрицы. Будем использовать строгое сравнение 3x3.
                boolean matches = true;
                for (int i = 0; i < 9; i++) {
                    // Используем isSimilar для игнорирования количества, но учитываем ItemMeta
                    if (!isSimilarIgnoreAmount(normalizedMatrix[i], required[i])) {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    // В будущем здесь может быть логика для "бесформенных" рецептов
                    return recipe;
                }
            }
        }
        return null;
    }

    // 2. Поиск рецепта Печи
    public CustomRecipe findFurnaceRecipe(ItemStack input) {
        if (input == null || input.getType() == Material.AIR) return null;

        for (CustomRecipe recipe : recipes.values()) {
            if (recipe.getType() == RecipeType.FURNACE) {
                ItemStack[] required = recipe.getIngredients();

                if (required == null || required.length < 1 || required[0] == null) continue;

                // Сравниваем только входной ингредиент
                if (isSimilarIgnoreAmount(input, required[0])) {
                    return recipe;
                }
            }
        }
        return null;
    }

    // 3. Поиск рецепта Наковальни
    public CustomRecipe findAnvilRecipe(ItemStack first, ItemStack second) {
        // Наковальня требует хотя бы один предмет
        if (first == null && second == null) return null;

        ItemStack normFirst = first != null ? first : new ItemStack(Material.AIR);
        ItemStack normSecond = second != null ? second : new ItemStack(Material.AIR);

        for (CustomRecipe recipe : recipes.values()) {
            if (recipe.getType() == RecipeType.ANVIL) {
                ItemStack[] required = recipe.getIngredients();

                if (required == null || required.length < 2) continue;

                ItemStack reqFirst = required[0] != null ? required[0] : new ItemStack(Material.AIR);
                ItemStack reqSecond = required[1] != null ? required[1] : new ItemStack(Material.AIR);

                // Сравниваем строго по двум слотам
                if (isSimilarIgnoreAmount(normFirst, reqFirst) &&
                        isSimilarIgnoreAmount(normSecond, reqSecond)) {
                    return recipe;
                }
            }
        }
        // Если кастомный не найден, возвращаем null, чтобы сработала стандартная логика наковальни
        return null;
    }

    // Вспомогательный метод для сравнения ItemStack, игнорируя количество
    private boolean isSimilarIgnoreAmount(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            // Если оба null или оба AIR
            return (stack1 == null || stack1.getType() == Material.AIR) && (stack2 == null || stack2.getType() == Material.AIR);
        }
        // Специальная обработка для сравнения AIR, так как isSimilar() требует не-null/не-AIR
        if (stack1.getType() == Material.AIR && stack2.getType() == Material.AIR) return true;

        // Временная копия для сравнения, так как isSimilar() в Bukkit API работает не всегда идеально
        // Самый надежный способ - сравнить материалы и ItemMeta
        if (stack1.getType() != stack2.getType()) return false;

        // Сравнение ItemMeta
        return plugin.getServer().getItemFactory().equals(stack1.getItemMeta(), stack2.getItemMeta());
    }
}