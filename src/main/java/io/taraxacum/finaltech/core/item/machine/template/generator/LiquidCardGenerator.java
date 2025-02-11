package io.taraxacum.finaltech.core.item.machine.template.generator;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import me.matl114.matlib.Utils.Inventory.ItemStacks.CleanItemStack;
import org.bukkit.inventory.ItemStack;

public class LiquidCardGenerator extends AbstractGeneratorMachine {
    public LiquidCardGenerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    void registerRandomOutputRecipes() {
        this.registerRecipe(CleanItemStack.ofBukkitClean(FinalTechItemStacks.WATER_CARD));
        this.registerRecipe(CleanItemStack.ofBukkitClean(FinalTechItemStacks.LAVA_CARD));
        this.registerRecipe(CleanItemStack.ofBukkitClean(FinalTechItemStacks.MILK_CARD));
    }
}
