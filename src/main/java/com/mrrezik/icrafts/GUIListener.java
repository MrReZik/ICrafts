package com.mrrezik.icrafts.listeners;

import com.mrrezik.icrafts.ICrafts;
import com.mrrezik.icrafts.objects.CustomRecipe;
import com.mrrezik.icrafts.objects.RecipeType;
import com.mrrezik.icrafts.utils.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class GUIListener implements Listener { // Имя класса соответствует имени файла!

    private final ICrafts plugin;

    public GUIListener(ICrafts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        String[] parsedTitle = GUIManager.parseTitle(title);

        if (parsedTitle == null) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        RecipeType type = RecipeType.valueOf(parsedTitle[1]);
        String mode = parsedTitle[0];
        String craftName = parsedTitle[2];

        int xpSlot = GUIManager.getSlotFromConfig(RecipeType.ANVIL, "ANVIL_XP_SLOT");
        int resultSlot = GUIManager.getSlotFromConfig(type, type == RecipeType.WORKBENCH ? "WORKBENCH_RESULT_SLOT" : type == RecipeType.FURNACE ? "FURNACE_RESULT_SLOT" : "ANVIL_RESULT_SLOT");

        // 1. Контроль кликов в верхнем инвентаре (наш GUI)
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {

            // Если клик по слоту, который НЕ является слотом ингредиента, результата, XP или кнопки сохранения, то отменяем (это декорации/заполнитель).
            if (!GUIManager.isGUISlot(type, slot) && !GUIManager.isSaveButton(type, slot)) {
                event.setCancelled(true);
            }

            // Если это кнопка сохранения или XP, отменяем, так как там своя логика ниже
            if (GUIManager.isSaveButton(type, slot) || (type == RecipeType.ANVIL && slot == xpSlot)) {
                event.setCancelled(true);
            }
        }

        // 2. Клик в нижнем инвентаре (инвентарь игрока)
        // Если клик произошел в нижнем инвентаре, мы его НЕ отменяем, чтобы игрок мог свободно перетаскивать предметы.

        // 3. Блокируем клики в режиме просмотра (VIEW_MODE)
        if (mode.equals(GUIManager.VIEW_MODE)) {
            // Разрешаем брать предмет из слота результата
            if (slot == resultSlot) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        // 4. Обработка клика по кнопке сохранения
        if (GUIManager.isSaveButton(type, slot)) {
            // event.setCancelled(true) уже выполнилось в п.1
            CustomRecipe recipe = GUIManager.buildRecipeFromGUI(event.getInventory(), craftName, type);

            if (recipe == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + plugin.getConfigManager().getMessage("messages.no-result"));
                return;
            }

            plugin.getRecipeManager().saveRecipe(recipe);
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + plugin.getConfigManager().getMessage("messages.recipe-saved").replace("%recipe%", craftName));
            player.closeInventory();
            return;
        }

        // 5. Логика изменения XP стоимости для Наковальни
        if (type == RecipeType.ANVIL && slot == xpSlot) {
            // event.setCancelled(true) уже выполнилось в п.1
            ItemStack xpItem = event.getCurrentItem();
            if (xpItem == null || xpItem.getType() != Material.EXPERIENCE_BOTTLE) return;

            ClickType click = event.getClick();
            int currentCost = xpItem.getAmount();
            int newCost = currentCost;
            int changeAmount;

            if (click.isLeftClick()) {
                changeAmount = click.isShiftClick() ? 10 : 1;
                newCost = currentCost + changeAmount;
            } else if (click.isRightClick()) {
                changeAmount = click.isShiftClick() ? 10 : 1;
                newCost = currentCost - changeAmount;
            }

            newCost = Math.max(1, newCost);
            newCost = Math.min(64, newCost);

            if (newCost == currentCost) return;

            xpItem.setAmount(newCost);
            ItemMeta meta = xpItem.getItemMeta();
            meta.setLore(Collections.singletonList(GUIManager.formatXpLore(newCost)));
            xpItem.setItemMeta(meta);

            event.getInventory().setItem(xpSlot, xpItem);
            player.updateInventory();
            return;
        }
    }
}