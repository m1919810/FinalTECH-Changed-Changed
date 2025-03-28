package io.taraxacum.finaltech.core.item.machine.cargo;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.common.util.JavaUtil;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.dto.SimpleCargoDTO;
import io.taraxacum.finaltech.core.helper.*;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.menu.cargo.AdvancedLineTransferMenu;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.util.*;
import io.taraxacum.libs.plugin.dto.InvWithSlots;
import io.taraxacum.libs.plugin.dto.ServerRunnableLockFactory;
import io.taraxacum.libs.plugin.util.ParticleUtil;
import me.matl114.matlib.Utils.Inventory.InventoryRecords.InventoryRecord;
import me.matl114.matlib.SlimefunUtils.BlockInventory.Records.OldSlimefunInventoryRecord;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Final_ROOT
 * @since 1.0
 */
public class AdvancedLineTransfer extends AbstractCargo implements RecipeItem {
    private final int particleInterval = 2;

    public AdvancedLineTransfer(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent blockPlaceEvent) {
                Block block = blockPlaceEvent.getBlock();
                Location location = block.getLocation();

                IgnorePermission.HELPER.checkOrSetBlockStorage(location);
                BlockStorage.addBlockInfo(location, ConstantTableUtil.CONFIG_UUID, blockPlaceEvent.getPlayer().getUniqueId().toString());

                BlockSearchMode.LINE_HELPER.checkOrSetBlockStorage(location);
                BlockSearchOrder.HELPER.checkOrSetBlockStorage(location);
                CargoOrder.HELPER.checkOrSetBlockStorage(location);
                BlockSearchCycle.HELPER.checkOrSetBlockStorage(location);
                BlockSearchSelf.HELPER.checkOrSetBlockStorage(location);

                CargoNumber.HELPER.checkOrSetBlockStorage(location);
                CargoNumberMode.HELPER.checkOrSetBlockStorage(location);
                CargoMode.HELPER.checkOrSetBlockStorage(location);
                CargoFilter.HELPER.checkOrSetBlockStorage(location);

                SlotSearchSize.INPUT_HELPER.checkOrSetBlockStorage(location);
                SlotSearchOrder.INPUT_HELPER.checkOrSetBlockStorage(location);
                CargoLimit.HELPER.checkOrSetBlockStorage(location);
                SlotSearchSize.OUTPUT_HELPER.checkOrSetBlockStorage(location);
                SlotSearchOrder.OUTPUT_HELPER.checkOrSetBlockStorage(location);
            }
        };
    }

    @Nonnull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return MachineUtil.simpleBlockBreakerHandler(this, AdvancedLineTransferMenu.ITEM_MATCH);
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new AdvancedLineTransferMenu(this);
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        BlockMenu blockMenu = BlockStorage.getInventory(block);
        JavaPlugin javaPlugin = this.getAddon().getJavaPlugin();
        boolean drawParticle = blockMenu.hasViewer() || RouteShow.VALUE_TRUE.equals(RouteShow.HELPER.getOrDefaultValue(config));


        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Directional)) {
            return;
        }
        BlockFace blockFace = ((Directional) blockData).getFacing();
        List<InventoryRecord> blockList = this.searchBlock(block, blockFace, BlockSearchMode.LINE_HELPER.getOrDefaultValue(config));

//            if (!PermissionUtil.checkOfflinePermission(location, config, LocationUtil.transferToLocation(blockList))) {
//                return;
//            }
        InventoryRecord blockRecord = new OldSlimefunInventoryRecord(blockMenu.toInventory(), blockMenu);
        switch (BlockSearchSelf.HELPER.getOrDefaultValue(config)) {
            case BlockSearchSelf.VALUE_START -> blockList.add(0, blockRecord);
            case BlockSearchSelf.VALUE_END -> blockList.add(blockRecord);
        }

        final List<InventoryRecord> finalBlockList;
        switch (BlockSearchOrder.HELPER.getOrDefaultValue(config)) {
            case BlockSearchOrder.VALUE_POSITIVE -> finalBlockList = blockList;
            case BlockSearchOrder.VALUE_REVERSE -> finalBlockList = JavaUtil.reserve(blockList);
            case BlockSearchOrder.VALUE_RANDOM -> finalBlockList = JavaUtil.shuffle(blockList);
            default -> finalBlockList = null;
        }
        if (finalBlockList == null || finalBlockList.isEmpty()) {
            return;
        }

        if (BlockSearchCycle.VALUE_TRUE.equals(BlockSearchCycle.HELPER.getOrDefaultValue(config)) && finalBlockList.size() > 1) {
            if (CargoOrder.VALUE_REVERSE.equals(CargoOrder.HELPER.getOrDefaultValue(config))) {
                finalBlockList.add(0, finalBlockList.get(finalBlockList.size() - 1));
            } else {
                finalBlockList.add(finalBlockList.get(0));
            }
        }

        if (drawParticle && !finalBlockList.isEmpty() && FinalTechChanged.getSlimefunTickCount() % this.particleInterval == 0) {
            javaPlugin.getServer().getScheduler().runTaskAsynchronously(javaPlugin, () -> ParticleUtil.drawCubeByBlock(javaPlugin, Particle.ELECTRIC_SPARK, this.particleInterval * Slimefun.getTickerTask().getTickRate() * 50L / finalBlockList.size(), finalBlockList.stream().map(InventoryRecord::invLocation).toArray(Location[]::new)));
        }
        Runnable task = ()->{
            String cargoNumberMode = CargoNumberMode.HELPER.getOrDefaultValue(config);
            String cargoOrder = CargoOrder.HELPER.getOrDefaultValue(config);
            String cargoMode = CargoMode.HELPER.getOrDefaultValue(config);
            String inputSize = SlotSearchSize.INPUT_HELPER.getOrDefaultValue(config);
            String inputOrder = SlotSearchOrder.INPUT_HELPER.getOrDefaultValue(config);
            String outputSize = SlotSearchSize.OUTPUT_HELPER.getOrDefaultValue(config);
            String outputOrder = SlotSearchOrder.OUTPUT_HELPER.getOrDefaultValue(config);
            SimpleCargoDTO simpleCargoDTO = new SimpleCargoDTO();
            simpleCargoDTO.setInputSize(inputSize);
            simpleCargoDTO.setInputOrder(inputOrder);
            simpleCargoDTO.setOutputSize(outputSize);
            simpleCargoDTO.setOutputOrder(outputOrder);
            simpleCargoDTO.setCargoLimit(CargoLimit.HELPER.getOrDefaultValue(config));
            simpleCargoDTO.setCargoFilter(CargoFilter.HELPER.getOrDefaultValue(config));
            simpleCargoDTO.setFilterInv(blockMenu.toInventory());
            simpleCargoDTO.setFilterSlots(AdvancedLineTransferMenu.ITEM_MATCH);
            int number;
            int cargoNumber = Integer.parseInt(CargoNumber.HELPER.getOrDefaultValue(config));
            for (int i = 0, size = finalBlockList.size(); i < size - 1; i++) {
                InventoryRecord inputBlock;
                InventoryRecord outputBlock;
                InvWithSlots inputMap;
                InvWithSlots outputMap;
                switch (cargoOrder) {
                    case CargoOrder.VALUE_POSITIVE:
                        inputBlock = finalBlockList.get(i);
                        outputBlock = finalBlockList.get((i + 1) % size);
                        break;
                    case CargoOrder.VALUE_REVERSE:
                        inputBlock = finalBlockList.get((i + 1) % size);
                        outputBlock = finalBlockList.get(i);
                        break;
                    default:
                        continue;
                }

                if (inputBlock.invLocation().equals(outputBlock.invLocation())) {
                    continue;
                }

                if (outputBlock.isSlimefunInv() && CargoMode.VALUE_INPUT_MAIN.equals(cargoMode)) {
                    outputMap = null;
                } else {
                    outputMap = CargoUtil.getInvWithSlots(outputBlock, outputSize, outputOrder);
                }
                if (inputBlock.isSlimefunInv() && CargoMode.VALUE_OUTPUT_MAIN.equals(cargoMode)) {
                    inputMap = null;
                } else {
                    inputMap = CargoUtil.getInvWithSlots(inputBlock, inputSize, inputOrder);
                }

                if (inputMap != null && outputMap != null && LocationUtil.isSameLocation(inputMap.getInventory().getLocation(), outputMap.getInventory().getLocation())) {
                    continue;
                }
                simpleCargoDTO.setInputBlock(inputBlock);
                simpleCargoDTO.setInputMap(inputMap);
                simpleCargoDTO.setOutputBlock(outputBlock);
                simpleCargoDTO.setOutputMap(outputMap);
                simpleCargoDTO.setCargoNumber(cargoNumber);
                number = CargoUtil.doSimpleCargoAsync(simpleCargoDTO, cargoMode);
                if (CargoNumberMode.VALUE_UNIVERSAL.equals(cargoNumberMode)) {
                    cargoNumber -= number;
                    if (cargoNumber <= 0) {
                        break;
                    }
                }
            }
        };
        if(CargoUtil.isAsyncMode()){
            Bukkit.getScheduler().runTaskAsynchronously(javaPlugin,task);
        }else {
            task.run();
        }
    }

    @Nonnull
    public List<InventoryRecord> searchBlock(@Nonnull Block begin, @Nonnull BlockFace blockFace, @Nonnull String blockSearchMode) {
        List<InventoryRecord> list = new ArrayList<>();
        Block block = begin.getRelative(blockFace);
        if (BlockSearchMode.VALUE_ZERO.equals(blockSearchMode)) {
            InventoryRecord blockRecord = OldSlimefunInventoryRecord.getInventoryRecord(block.getLocation(),true);
            if(blockRecord.inventory()!=null){
                list.add(blockRecord);
            }
            block = block.getRelative(blockFace);
            blockRecord = OldSlimefunInventoryRecord.getInventoryRecord(block.getLocation(),true);
            if (blockRecord.inventory()!=null) {
                list.add(blockRecord);
            }
            return list;
        }
        InventoryRecord blockRecord = OldSlimefunInventoryRecord.getInventoryRecord(block.getLocation(),true);
        while (blockRecord.inventory()!=null) {
            if (blockRecord.isSlimefunInv() && ((BlockMenu)(blockRecord.optionalHolder())).getPreset().getID().equals(FinalTechItemStacks.LINE_TRANSFER.getItemId())) {
                if (BlockSearchMode.VALUE_PENETRATE.equals(blockSearchMode)) {
                    block = block.getRelative(blockFace);
                    blockRecord = OldSlimefunInventoryRecord.getInventoryRecord(block.getLocation(),true);
                    continue;
                } else if (BlockSearchMode.VALUE_INTERRUPT.equals(blockSearchMode)) {
                    list.add(blockRecord);
                    break;
                }
            }
            list.add(blockRecord);
            block = block.getRelative(blockFace);
            blockRecord = OldSlimefunInventoryRecord.getInventoryRecord(block.getLocation(),true);
        }
        return list;
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this);
    }
}
