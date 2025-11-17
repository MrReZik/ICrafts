package com.mrrezik.icrafts.listeners;

import com.mrrezik.icrafts.ICrafts;
import com.mrrezik.icrafts.objects.CustomRecipe;
import com.mrrezik.icrafts.objects.RecipeType;
import com.mrrezik.icrafts.managers.RecipeManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

public class RecipeListener implements Listener {

    private final ICrafts plugin;
    private final RecipeManager recipeManager;

    public RecipeListener(ICrafts plugin, RecipeManager recipeManager) {
        this.plugin = plugin;
        this.recipeManager = recipeManager;
    }

    // --- 1. Обработка Верстака (WORKBENCH) ---
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING &&
                event.getInventory().getType() != InventoryType.WORKBENCH) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        CustomRecipe recipe = recipeManager.findWorkbenchRecipe(matrix);

        if (recipe != null) {
            // Если найден кастомный рецепт, заменяем результат
            event.getInventory().setResult(recipe.getResult());
        }
    }

    // --- 2. Обработка Печи (FURNACE) ---
    // Используем FurnaceSmeltEvent, который срабатывает, когда предмет готов
    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();

        CustomRecipe recipe = recipeManager.findFurnaceRecipe(source);

        if (recipe != null) {
            // Если найден кастомный рецепт, заменяем результат
            event.setResult(recipe.getResult());
            // Мы не можем контролировать опыт здесь напрямую,
            // но можно использовать отдельный плагин или событие FurnaceExtractEvent
            // для более сложной логики. Для базовой версии оставляем так.
        }
    }

    // --- 3. Обработка Наковальни (ANVIL) ---
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);

        if (first == null && second == null) return;

        // Поиск кастомного рецепта
        CustomRecipe recipe = recipeManager.findAnvilRecipe(first, second);

        if (recipe != null) {
            event.setResult(recipe.getResult());

            // Устанавливаем стоимость опыта
            int finalCost = recipe.getXpCost();

            // Если результат - не null, устанавливаем стоимость
            if (event.getResult() != null && event.getResult().getType() != Material.AIR) {
                // Если результат - Repairable (например, инструменты), мы можем попытаться установить уровень ремонта,
                // но для кастомных крафтов просто устанавливаем стоимость опыта.

                // В Paper/Spigot InventoryUtils.class имеет setRepairCost, но в Bukkit API его нет.
                // Приходится использовать обходные пути или устанавливать уровень.
                inventory.setRepairCost(finalCost);
            }
        } else {
            // Если кастомный рецепт не найден, но предметы есть,
            // даем Bukkit/Paper обработать стандартную логику наковальни.
        }
    }
}