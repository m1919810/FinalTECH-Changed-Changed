package io.taraxacum.finaltech.core.item.machine.range.point;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.*;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.item.machine.EntropySeed;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.util.*;
import io.taraxacum.libs.plugin.dto.ItemWrapper;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.libs.slimefun.interfaces.SimpleValidItem;
import io.taraxacum.libs.slimefun.util.SfItemUtil;
import me.matl114.matlib.Utils.Inventory.ItemStacks.CleanItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
 
public class EquivalentConcept extends AbstractPointMachine implements RecipeItem, SimpleValidItem {
    public static final String KEY_LIFE = "l";
    public static final String KEY_RANGE = "r";
    private final double attenuationRate = ConfigUtil.getOrDefaultItemSetting(0.95, this, "attenuation-rate");
    private final double life = ConfigUtil.getOrDefaultItemSetting(4.0, this, "life");
    private final int range = ConfigUtil.getOrDefaultItemSetting(2, this, "range");

    private final ItemWrapper templateValidItem;

    public EquivalentConcept(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        ItemStack validItem = CleanItemStack.ofBukkitClean(this.getItem());
        SfItemUtil.setSpecialItemKey(validItem);
        this.templateValidItem = new ItemWrapper(validItem);

        this.addItemHandler(new ItemUseHandler() {
            @Override
            @EventHandler(priority = EventPriority.LOWEST)
            public void onRightClick(PlayerRightClickEvent e) {
                e.cancel();
            }
        });
        this.addItemHandler(new WeaponUseHandler() {
            @Override
            @EventHandler(priority = EventPriority.LOWEST)
            public void onHit(@Nonnull EntityDamageByEntityEvent e, @Nonnull Player player, @Nonnull ItemStack item) {
                e.setCancelled(true);
            }
        });
        this.addItemHandler(new EntityInteractHandler() {
            @Override
            @EventHandler(priority = EventPriority.LOWEST)
            public void onInteract(PlayerInteractEntityEvent e, ItemStack item, boolean offHand) {
                e.setCancelled(true);
            }
        });
        this.addItemHandler(new ToolUseHandler() {
            @Override
            @EventHandler(priority = EventPriority.LOWEST)
            public void onToolUse(BlockBreakEvent e, ItemStack tool, int fortune, List<ItemStack> drops) {
                e.setCancelled(true);
            }
        });
    }

    @Nonnull
    @Override
    protected BlockPlaceHandler onBlockPlace() {
        return MachineUtil.BLOCK_PLACE_HANDLER_DENY;
    }

    @Nonnull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return new BlockBreakHandler(false, true) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent blockBreakEvent, @Nonnull ItemStack itemStack, @Nonnull List<ItemStack> list) {
                EntropySeed.removeCache(blockBreakEvent.getBlock().getLocation());
            }
        };
    }

    @Nonnull
    @Override
    public Collection<ItemStack> getDrops() {
        ArrayList<ItemStack> drops = new ArrayList<>();
        drops.add(this.getValidItem());
        return drops;
    }

    @Nullable
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        // this is the only
        return null;
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        try  {
            if (FinalTechChanged.y) {
                FinalTechChanged.getInstance().getServer().getScheduler().runTask(FinalTechChanged.getInstance(), () -> block.setType(Material.AIR));
                EntropySeed.removeBlock(block.getLocation());
                return ;
            }
            String sleepStr = EntropySeed.getLocationInfoCache(block.getLocation(), ConstantTableUtil.CONFIG_SLEEP);
            if (sleepStr != null) {
                double sleep = Double.parseDouble(sleepStr) - 1;
                if (sleep > 0) {
                    EntropySeed.addLocationInfoCache(block.getLocation(), ConstantTableUtil.CONFIG_SLEEP, String.valueOf(sleep));
                    return;
                } else {
                    EntropySeed.addLocationInfoCache(block.getLocation(), ConstantTableUtil.CONFIG_SLEEP, String.valueOf(0));

                }

            }
            Location l = block.getLocation();
            String lifeStr = EntropySeed.getLocationInfoCache(l, KEY_LIFE);
            double life = (lifeStr != null) ? Double.parseDouble(lifeStr) : 0;
            if (life < 1) {
                EntropySeed.removeBlock(l);
                JavaPlugin javaPlugin = this.getAddon().getJavaPlugin();
                javaPlugin.getServer().getScheduler().runTaskLaterAsynchronously(javaPlugin, () -> {
                    if ( EntropySeed.getBlock(l) == null && !l.getBlock().getType().isAir()) {
                        EntropySeed.tryCreateBlock(l, FinalTechItemStacks.JUSTIFIABILITY.getItemId(), true);
                    }
                }, Slimefun.getTickerTask().getTickRate() + 1);
                return;
            }
            String keyStr = EntropySeed.getLocationInfoCache(l, KEY_RANGE);
            final int range = (keyStr != null) ? Integer.parseInt(keyStr) : this.range;

            while (life > 1) {
                final double finalLife = life--;
                int flag = 0 ;
                for (int i = 0 ;i <5 && flag==0 ; ++i){
                    flag = this.pointFunction(block, range, location -> {
                        String blockInfo = EntropySeed.getBlock(location);
                        if (blockInfo == null) {
                            Block targetBlock = location.getBlock();
                            if (targetBlock.getType().isAir()) {
                                JavaPlugin javaPlugin = EquivalentConcept.this.getAddon().getJavaPlugin();
                                Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
                                    EntropySeed.tryCreateBlock(location,EquivalentConcept.this.getId(), true);
                                    EntropySeed.addLocationInfoCache(location, KEY_LIFE, String.valueOf(finalLife * attenuationRate));
                                    EntropySeed.addLocationInfoCache(location, KEY_RANGE, String.valueOf(range + 1));
                                    EntropySeed.addLocationInfoCache(location,ConstantTableUtil.CONFIG_SLEEP, String.valueOf(EquivalentConcept.this.life - finalLife));
                                    javaPlugin.getServer().getScheduler().runTask(javaPlugin, () -> targetBlock.setType(EquivalentConcept.this.getItem().getType()));
                                });
                                return 1;
                            }
                        }
                        return 0;
                    });
                }
            }

            EntropySeed.addLocationInfoCache(block.getLocation(), KEY_LIFE, String.valueOf(0));
        } catch (Exception e) {
            FinalTechChanged.getInstance().getLogger().warning("[FINALTECH] 物品 等概念体 出现了异常, 但不要担心这是正常情况");
        }
    }

    @Override
    protected boolean isSynchronized() {
        return false;
    }

    @Nonnull
    @Override
    public Location getTargetLocation(@Nonnull Location location, int range) {
        int y = location.getBlockY() - range + FinalTechChanged.getRandom().nextInt(range + range);
        y = Math.min(location.getWorld().getMaxHeight(), y);
        y = Math.max(location.getWorld().getMinHeight(), y);
        return new Location(location.getWorld(), location.getX() - range + FinalTechChanged.getRandom().nextInt(range + range), y, location.getZ() - range + new Random().nextInt(range + range));
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
