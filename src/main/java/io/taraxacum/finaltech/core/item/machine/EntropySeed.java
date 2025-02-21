package io.taraxacum.finaltech.core.item.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.item.machine.range.point.EquivalentConcept;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.finaltech.util.ConstantTableUtil;
import io.taraxacum.finaltech.util.RecipeUtil;
import me.matl114.matlib.Algorithms.DataStructures.Complex.ObjectLockFactory;
import me.matl114.matlibAdaptor.Algorithms.DataStructures.LockFactory;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EntropySeed extends AbstractMachine implements RecipeItem {
    private final double equivalentConceptLife = ConfigUtil.getOrDefaultItemSetting(8.0, this, "life");
    private final int equivalentConceptRange = ConfigUtil.getOrDefaultItemSetting(4, this, "range");
    private final String key = "key";
    private final String value = "value";

    public EntropySeed(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                Location location = e.getBlock().getLocation();
                addLocationInfoCache(location, EntropySeed.this.key, EntropySeed.this.value);
            }
        };
    }

    @Nonnull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return new BlockBreakHandler(false, true) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent blockBreakEvent, @Nonnull ItemStack item, @Nonnull List<ItemStack> drops) {
                blockBreakEvent.setDropItems(false);
                drops.clear();
            }
        };
    }
    protected SlimefunItem EQUIVALENT_CONCEPT ;
    protected SlimefunItem getEquivalentConcept() {
        if (EQUIVALENT_CONCEPT == null) {
            EQUIVALENT_CONCEPT =  SlimefunItem.getByItem(FinalTechItemStacks.EQUIVALENT_CONCEPT);
        }
        return EQUIVALENT_CONCEPT;
    }
    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return null;
    }
    //for EntropySeed and relevent things
    private static final ConcurrentHashMap<Location, Map<String,String>> fastCache = new ConcurrentHashMap<>();
    public static void addLocationInfoCache(Location loc,String key,String value){
        fastCache.computeIfAbsent(loc,l->new ConcurrentHashMap<>()).put(key,value);
    }
    public static void computeLocationInfoCache(Location loc, String key, Function<String,String> compute){
        fastCache.computeIfAbsent(loc,l->new ConcurrentHashMap<>()).compute(key,(key1,value)->compute.apply(value));
    }
    public static void removeLocationInfoCache(Location loc,String key){
        fastCache.getOrDefault(loc,new HashMap<>()).remove(key);
    }
    public static String getLocationInfoCache(Location loc,String key){
        return fastCache.computeIfAbsent(loc,l->new ConcurrentHashMap<>()).get(key);
    }
    public static void removeBlock(Location loc){
        fastCache.remove(loc);
        BlockStorage.clearBlockInfo(loc);
    }
    public static void removeCache(Location loc){
        fastCache.remove(loc);
    }
    private static LockFactory<Location> blockCreatorLockFactory = new ObjectLockFactory<>(Location.class,Location::clone).init(FinalTechChanged.getInstance()).setupRefreshTask(30*60*20);
    public static boolean tryCreateBlock(Location loc, String id, boolean updateTicker){
        if(blockCreatorLockFactory.checkThreadStatus(loc)){
            return blockCreatorLockFactory.ensureLock(()->{
                if(BlockStorage.hasBlockInfo(loc)){
                    return false;
                }else{
                    fastCache.remove(loc);
                    BlockStorage.addBlockInfo(loc,ConstantTableUtil.CONFIG_ID,id,updateTicker);
                    return true;
                }
            },loc);
        }else {
            return false;
        }
    }
    public static String getBlock(Location loc){
        return BlockStorage.getLocationInfo(loc, ConstantTableUtil.CONFIG_ID);
    }
    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        // TODO optimization
        //TODO optimize
        Location location = block.getLocation();
        String locationKey = getLocationInfoCache(location, this.key);
        if (this.value.equals(locationKey)) {
            removeLocationInfoCache(location, this.key);
            SlimefunItem sfItem = getEquivalentConcept();
            if (sfItem instanceof EquivalentConcept concept) {
                removeBlock(location);
                JavaPlugin javaPlugin = this.getAddon().getJavaPlugin();
                javaPlugin.getServer().getScheduler().runTaskLaterAsynchronously(javaPlugin, () -> {
                   tryCreateBlock(location, FinalTechItemStacks.EQUIVALENT_CONCEPT.getItemId(), true);
                    addLocationInfoCache(location,EquivalentConcept.KEY_LIFE,String.valueOf( EntropySeed.this.equivalentConceptLife));
                    addLocationInfoCache(location,EquivalentConcept.KEY_RANGE,String.valueOf(EntropySeed.this.equivalentConceptRange));
                }, Slimefun.getTickerTask().getTickRate() + 1);
            }
        } else {
            removeBlock(location);
            JavaPlugin javaPlugin = this.getAddon().getJavaPlugin();
            javaPlugin.getServer().getScheduler().runTaskLaterAsynchronously(javaPlugin, () -> {
                tryCreateBlock(location,  FinalTechItemStacks.JUSTIFIABILITY.getItemId(), true);
            }, Slimefun.getTickerTask().getTickRate() + 1);
        }
    }

    @Override
    protected boolean isSynchronized() {
        return false;
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this);
    }
}
