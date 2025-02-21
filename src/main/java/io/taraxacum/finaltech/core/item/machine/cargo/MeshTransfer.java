package io.taraxacum.finaltech.core.item.machine.cargo;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.dto.SimpleCargoDTO;
import io.taraxacum.finaltech.core.helper.*;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.menu.cargo.MeshTransferMenu;
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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Final_ROOT
 * @since 1.0
 */
public class MeshTransfer extends AbstractCargo implements RecipeItem {
    private final double particleDistance = 0.25;
    private final int particleInterval = 2;

    public MeshTransfer(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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

                CargoFilter.HELPER.checkOrSetBlockStorage(location);
                BlockSearchMode.MESH_INPUT_HELPER.checkOrSetBlockStorage(location);
                BlockSearchMode.MESH_OUTPUT_HELPER.checkOrSetBlockStorage(location);

                BlockStorage.addBlockInfo(block, PositionInfo.KEY, "");
            }
        };
    }

    @Nonnull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return MachineUtil.simpleBlockBreakerHandler(this, MeshTransferMenu.ITEM_MATCH);
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new MeshTransferMenu(this);
    }

    @Override
    public void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        BlockMenu blockMenu = BlockStorage.getInventory(block);
        Inventory inventory = blockMenu.toInventory();
        InventoryRecord blockRecord = new OldSlimefunInventoryRecord(inventory,blockMenu);
        JavaPlugin javaPlugin = this.getAddon().getJavaPlugin();
        boolean drawParticle = blockMenu.hasViewer() || RouteShow.VALUE_TRUE.equals(RouteShow.HELPER.getOrDefaultValue(config));

        BlockFace[] outputBlockFaces = PositionInfo.getBlockFaces(config, PositionInfo.VALUE_OUTPUT, PositionInfo.VALUE_INPUT_AND_OUTPUT);
        BlockFace[] inputBlockFaces = PositionInfo.getBlockFaces(config, PositionInfo.VALUE_INPUT, PositionInfo.VALUE_INPUT_AND_OUTPUT);
        InventoryRecord[] outputBlocks = new InventoryRecord[outputBlockFaces.length];
        InventoryRecord[] inputBlocks = new InventoryRecord[inputBlockFaces.length];
        String outputBlockSearchMode = BlockSearchMode.MESH_OUTPUT_HELPER.getOrDefaultValue(config);
        String inputBlockSearchMode = BlockSearchMode.MESH_INPUT_HELPER.getOrDefaultValue(config);

        for (int i = 0; i < outputBlocks.length; i++) {
            outputBlocks[i] = this.searchBlock(block, outputBlockFaces[i], outputBlockSearchMode, drawParticle);
        }
        for (int i = 0; i < inputBlocks.length; i++) {
            inputBlocks[i] = this.searchBlock(block, inputBlockFaces[i], inputBlockSearchMode, drawParticle);
        }

//        if (!PermissionUtil.checkOfflinePermission(location, config, LocationUtil.transferToLocation(inputBlocks)) || !PermissionUtil.checkOfflinePermission(location, config, LocationUtil.transferToLocation(outputBlocks))) {
//            return;
//        }

        if (drawParticle) {
            javaPlugin.getServer().getScheduler().runTaskLaterAsynchronously(javaPlugin, () -> {
                ParticleUtil.drawCubeByBlock(javaPlugin, Particle.WAX_OFF, 0, Arrays.stream(inputBlocks).map(InventoryRecord::invLocation).toArray(Location[]::new));
                ParticleUtil.drawCubeByBlock(javaPlugin, Particle.WAX_OFF, 0,Arrays.stream(outputBlocks).map(InventoryRecord::invLocation).toArray(Location[]::new));
            }, Slimefun.getTickerTask().getTickRate());
        }
        Runnable task = ()->{
            String outputSize = SlotSearchSize.OUTPUT_HELPER.defaultValue();
            String outputOrder = SlotSearchOrder.OUTPUT_HELPER.defaultValue();
            int outputCargoNumber = Integer.parseInt(CargoNumber.OUTPUT_HELPER.defaultValue());
            String outputCargoNumberMode = CargoNumberMode.OUTPUT_HELPER.defaultValue();
            String outputCargoLimit = CargoLimit.OUTPUT_HELPER.defaultValue();

            String inputSize = SlotSearchSize.INPUT_HELPER.defaultValue();
            String inputOrder = SlotSearchOrder.INPUT_HELPER.defaultValue();
            int inputCargoNumber = Integer.parseInt(CargoNumber.INPUT_HELPER.defaultValue());
            String inputCargoNumberMode = CargoNumberMode.INPUT_HELPER.defaultValue();
            String inputCargoLimit = CargoLimit.INPUT_HELPER.defaultValue();

            String cargoFilter = CargoFilter.HELPER.getOrDefaultValue(config);

            InvWithSlots sourceInputMap = new InvWithSlots(blockMenu.toInventory(), this.getInputSlot());
            InvWithSlots sourceOutputMap = new InvWithSlots(blockMenu.toInventory(), this.getOutputSlot());

            // do cargo for outputs

            SimpleCargoDTO simpleCargoDTO = new SimpleCargoDTO();
            simpleCargoDTO.setCargoFilter(cargoFilter);
            simpleCargoDTO.setFilterInv(blockMenu.toInventory());
            simpleCargoDTO.setFilterSlots(MeshTransferMenu.ITEM_MATCH);

            simpleCargoDTO.setInputMap(sourceOutputMap);
            simpleCargoDTO.setInputBlock(blockRecord);
            simpleCargoDTO.setInputSize(SlotSearchSize.VALUE_OUTPUTS_ONLY);
            simpleCargoDTO.setInputOrder(SlotSearchOrder.VALUE_ASCENT);

            simpleCargoDTO.setOutputSize(outputSize);
            simpleCargoDTO.setOutputOrder(outputOrder);
            simpleCargoDTO.setCargoLimit(outputCargoLimit);

            for (InventoryRecord outputBlock : outputBlocks) {
                if(outputBlock.inventory() == null){
                    continue;
                }
                InvWithSlots outputMap;
                if (outputBlock.isSlimefunInv()) {
                    outputMap = null;
                } else {
                    outputMap = CargoUtil.getInvWithSlots(outputBlock, outputSize, outputOrder);
                    if (outputMap == null) {
                        continue;
                    }
                }
                simpleCargoDTO.setOutputMap(outputMap);
                simpleCargoDTO.setOutputBlock(outputBlock);
                simpleCargoDTO.setCargoNumber(outputCargoNumber);

                int result =  CargoUtil.getCargoLockFactory().ensureLock(()->{
                    return CargoUtil.doSimpleCargoInputMain(simpleCargoDTO,sourceInputMap,outputMap);
                },block.getLocation(),outputBlock.invLocation());
                if (CargoNumberMode.VALUE_UNIVERSAL.equals(outputCargoNumberMode)) {
                    outputCargoNumber -= result;
                    if (outputCargoNumber == 0) {
                        break;
                    }
                }
            }

            // do cargo for itself

            simpleCargoDTO.setInputMap(sourceInputMap);
//            simpleCargoDTO.setInputBlock(block);
            simpleCargoDTO.setInputSize(SlotSearchSize.VALUE_INPUTS_ONLY);
//            simpleCargoDTO.setInputOrder(SlotSearchOrder.VALUE_ASCENT);

            simpleCargoDTO.setOutputMap(sourceOutputMap);
            simpleCargoDTO.setOutputBlock(blockRecord);
            simpleCargoDTO.setOutputSize(SlotSearchSize.VALUE_OUTPUTS_ONLY);
            simpleCargoDTO.setOutputOrder(SlotSearchOrder.VALUE_ASCENT);

            simpleCargoDTO.setCargoNumber(576);
            simpleCargoDTO.setCargoLimit(CargoLimit.VALUE_ALL);
            CargoUtil.getCargoLockFactory().ensureLock(()->{
                CargoUtil.doSimpleCargoStrongSymmetry(simpleCargoDTO,sourceInputMap,sourceOutputMap);
            },block.getLocation());

            // do cargo for input

            simpleCargoDTO.setInputSize(inputSize);
            simpleCargoDTO.setInputOrder(inputOrder);
            simpleCargoDTO.setOutputMap(sourceInputMap);
//            simpleCargoDTO.setOutputBlock(block);
            simpleCargoDTO.setOutputSize(SlotSearchSize.VALUE_INPUTS_ONLY);
//            simpleCargoDTO.setOutputOrder(SlotSearchOrder.VALUE_ASCENT);
            simpleCargoDTO.setCargoLimit(inputCargoLimit);
//            simpleCargoDTO.setCargoFilter(cargoFilter);
//            simpleCargoDTO.setFilterInv(blockMenu.toInventory());
//            simpleCargoDTO.setFilterSlots(MeshTransferMenu.ITEM_MATCH);

            for (InventoryRecord inputBlock : inputBlocks) {
                if(inputBlock.inventory() == null){
                    continue;
                }
                InvWithSlots inputMap;
                if (inputBlock.isSlimefunInv()) {
                    inputMap = null;
                } else {
                    inputMap = CargoUtil.getInvWithSlots(inputBlock, inputSize, inputOrder);
                    if (inputMap == null) {
                        continue;
                    }
                }
                simpleCargoDTO.setInputMap(inputMap);
                simpleCargoDTO.setInputBlock(inputBlock);
                simpleCargoDTO.setCargoNumber(inputCargoNumber);
                int result = CargoUtil.doSimpleCargoOutputMain(simpleCargoDTO,inputMap,simpleCargoDTO.getOutputMap());
                if (CargoNumberMode.VALUE_UNIVERSAL.equals(inputCargoNumberMode)) {
                    inputCargoNumber -= result;
                    if (inputCargoNumber == 0) {
                        break;
                    }
                }
            }
        };
        // parse block storage
        if(CargoUtil.isAsyncMode()){
            Bukkit.getScheduler().runTaskAsynchronously(javaPlugin,task);
        }else {
            task.run();
        }

    }

    @Nonnull
    public InventoryRecord searchBlock(@Nonnull Block sourceBlock, @Nonnull BlockFace blockFace, @Nonnull String searchMode, boolean drawParticle) {
        List<Location> particleLocationList = new ArrayList<>();
        particleLocationList.add(LocationUtil.getCenterLocation(sourceBlock));
        Block result = sourceBlock.getRelative(blockFace);
        if (BlockSearchMode.VALUE_ZERO.equals(searchMode)) {
            particleLocationList.add(LocationUtil.getCenterLocation(result));
            if (drawParticle && FinalTechChanged.getSlimefunTickCount() % this.particleInterval == 0) {
                JavaPlugin javaPlugin = this.getAddon().getJavaPlugin();
                javaPlugin.getServer().getScheduler().runTaskAsynchronously(javaPlugin, () -> ParticleUtil.drawLineByDistance(javaPlugin, Particle.CRIT_MAGIC, this.particleInterval * Slimefun.getTickerTask().getTickRate() * 50L / particleLocationList.size(), this.particleDistance, particleLocationList));
            }
            return OldSlimefunInventoryRecord.getInventoryRecord(result.getLocation(),true);
        }
        while (true) {
            particleLocationList.add(LocationUtil.getCenterLocation(result));
            if (result.getType() == Material.CHAIN) {
                result = result.getRelative(blockFace);
                continue;
            }
            if (BlockSearchMode.VALUE_PENETRATE.equals(searchMode) && BlockStorage.hasInventory(result) && BlockStorage.getInventory(result).getPreset().getID().equals(FinalTechItemStacks.MESH_TRANSFER.getItemId())) {
                result = result.getRelative(blockFace);
                continue;
            }
            break;
        }
        if (drawParticle && FinalTechChanged.getSlimefunTickCount() % this.particleInterval == 0) {
            JavaPlugin javaPlugin = this.getAddon().getJavaPlugin();
            javaPlugin.getServer().getScheduler().runTaskAsynchronously(javaPlugin, () -> ParticleUtil.drawLineByDistance(javaPlugin, Particle.CRIT_MAGIC, this.particleInterval * Slimefun.getTickerTask().getTickRate() * 50L / particleLocationList.size(), this.particleDistance, particleLocationList));
        }
        return OldSlimefunInventoryRecord.getInventoryRecord(result.getLocation(),true);
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this);
    }
}
