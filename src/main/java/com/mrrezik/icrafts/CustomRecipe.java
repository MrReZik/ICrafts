package com.mrrezik.icrafts.objects;

import org.bukkit.inventory.ItemStack;

public class CustomRecipe {

    private final String name;
    private final RecipeType type;
    private final ItemStack result;
    private final ItemStack[] ingredients;
    private final int xpCost;

    public CustomRecipe(String name, RecipeType type, ItemStack result, ItemStack[] ingredients, int xpCost) {
        this.name = name;
        this.type = type;
        this.result = result;
        this.ingredients = ingredients;
        this.xpCost = xpCost;
    }

    public String getName() {
        return name;
    }

    public RecipeType getType() {
        return type;
    }

    public ItemStack getResult() {
        return result.clone();
    }

    public ItemStack[] getIngredients() {
        return ingredients;
    }

    public int getXpCost() {
        return xpCost;
    }

    public boolean matches(ItemStack[] inputMatrix) {
        if (type == RecipeType.WORKBENCH) {
            if (inputMatrix.length != 9) return false;
            for (int i = 0; i < 9; i++) {
                ItemStack recipeItem = ingredients[i];
                ItemStack inputItem = inputMatrix[i];

                if (recipeItem == null && inputItem == null) continue;
                if (recipeItem == null || inputItem == null) return false;
                if (!recipeItem.isSimilar(inputItem)) return false;
                if (inputItem.getAmount() < recipeItem.getAmount()) return false;
            }
            return true;

        } else if (type == RecipeType.FURNACE) {
            if (inputMatrix.length != 1) return false;
            return ingredients[0] != null && ingredients[0].isSimilar(inputMatrix[0]);

        } else if (type == RecipeType.ANVIL) {
            if (inputMatrix.length != 2) return false;
            return ingredients[0] != null && ingredients[1] != null &&
                    ingredients[0].isSimilar(inputMatrix[0]) &&
                    ingredients[1].isSimilar(inputMatrix[1]);
        }
        return false;
    }
}