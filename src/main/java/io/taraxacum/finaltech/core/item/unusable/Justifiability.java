package io.taraxacum.finaltech.core.item.unusable;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.item.machine.EntropySeed;
import io.taraxacum.finaltech.core.item.machine.range.point.EquivalentConcept;
import io.taraxacum.finaltech.util.RecipeUtil;
import io.taraxacum.libs.plugin.dto.ItemWrapper;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.libs.slimefun.interfaces.SimpleValidItem;
import io.taraxacum.libs.slimefun.util.SfItemUtil;
import me.matl114.matlib.Utils.Inventory.ItemStacks.CleanItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
 
public class Justifiability extends UnusableSlimefunItem implements RecipeItem, SimpleValidItem {
    private final ItemWrapper templateValidItem;

    public Justifiability(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        ItemStack validItem = CleanItemStack.ofBukkitClean(this.getItem());
        SfItemUtil.setSpecialItemKey(validItem);
        this.templateValidItem = new ItemWrapper(validItem);
    }
    @Override
    public void preRegister() {
        this.addItemHandler(new BlockTicker() {
            public void tick(Block b, SlimefunItem sf, Config data) {
                try {
                    if (FinalTechChanged.y) {
                        FinalTechChanged.getInstance().getServer().getScheduler().runTask(FinalTechChanged.getInstance(), () -> b.setType(Material.AIR));
                        EntropySeed.removeBlock(b.getLocation());
                        return ;
                    }
                    String lifeStr = EntropySeed.getLocationInfoCache(b.getLocation(), "life");
                    if (lifeStr != null) {
                        int i = Integer.parseInt(lifeStr);
                        if (i == 0) {
                            FinalTechChanged.getInstance().getServer().getScheduler().runTask(FinalTechChanged.getInstance(), () -> b.setType(Material.AIR));
                            EntropySeed.removeBlock(b.getLocation());
                            return ;
                        }
                        i--;
                        EntropySeed.addLocationInfoCache(b.getLocation(), "life", String.valueOf(i));
                    } else EntropySeed.addLocationInfoCache(b.getLocation(), "life", "5");
                } catch (Exception ignore) {
                }
            }

            public boolean isSynchronized() {
                return false;
            }
        });
    }

    @Nonnull
    @Override
    public Collection<ItemStack> getDrops() {
        ArrayList<ItemStack> drops = new ArrayList<>();
        drops.add(this.getValidItem());
        return drops;
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this);
    }

    @Nonnull
    @Override
    public ItemStack getValidItem() {
        return ItemStackUtil.cloneItem(this.templateValidItem.getItemStack());
    }

    @Override
    public boolean verifyItem(@Nonnull ItemStack itemStack) {
        return ItemStackUtil.isItemSimilar(itemStack, this.templateValidItem);
    }
}
