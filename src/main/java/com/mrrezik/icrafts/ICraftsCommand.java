package com.mrrezik.icrafts.commands;

import com.mrrezik.icrafts.ICrafts;
import com.mrrezik.icrafts.managers.ConfigManager;
import com.mrrezik.icrafts.objects.RecipeType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ICraftsCommand implements CommandExecutor {

    private final ICrafts plugin;

    public ICraftsCommand(ICrafts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager config = plugin.getConfigManager();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            config.getMessageList("help-message").forEach(sender::sendMessage);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            // Исправленный вызов: reloadConfigs() публичный
            plugin.getConfigManager().reloadConfigs();
            plugin.getRecipeManager().loadRecipes();
            sender.sendMessage(config.getMessage("prefix") + config.getMessage("messages.reloaded"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getMessage("prefix") + config.getMessage("messages.must-be-player"));
            return true;
        }

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.usage-create"));
                return true;
            }

            String recipeName = args[1];
            RecipeType type;

            try {
                type = RecipeType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.invalid-type").replace("%types%", Arrays.toString(RecipeType.values())));
                return true;
            }

            if (plugin.getRecipeManager().getRecipe(recipeName) != null) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.recipe-exists").replace("%recipe%", recipeName));
                return true;
            }

            plugin.getGuiManager().openCreator(player, type, recipeName);
            return true;
        }

        if (args[0].equalsIgnoreCase("edit")) {
            if (args.length < 2) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.usage-edit"));
                return true;
            }

            String recipeName = args[1];
            var recipe = plugin.getRecipeManager().getRecipe(recipeName);

            if (recipe == null) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.recipe-not-found").replace("%recipe%", recipeName));
                return true;
            }

            plugin.getGuiManager().openEditor(player, recipe);
            return true;
        }

        if (args[0].equalsIgnoreCase("view")) {
            if (args.length < 2) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.usage-view"));
                return true;
            }

            String recipeName = args[1];
            var recipe = plugin.getRecipeManager().getRecipe(recipeName);

            if (recipe == null) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.recipe-not-found").replace("%recipe%", recipeName));
                return true;
            }

            plugin.getGuiManager().openViewer(player, recipe);
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.usage-delete"));
                return true;
            }

            String recipeName = args[1];
            var recipe = plugin.getRecipeManager().getRecipe(recipeName);

            if (recipe == null) {
                player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.recipe-not-found").replace("%recipe%", recipeName));
                return true;
            }

            plugin.getRecipeManager().deleteRecipe(recipeName);
            player.sendMessage(config.getMessage("prefix") + config.getMessage("messages.recipe-deleted").replace("%recipe%", recipeName));
            return true;
        }

        // По умолчанию или при неправильной команде
        config.getMessageList("help-message").forEach(sender::sendMessage);
        return true;
    }
}