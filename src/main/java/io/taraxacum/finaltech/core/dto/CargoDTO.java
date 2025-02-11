package io.taraxacum.finaltech.core.dto;

import io.taraxacum.finaltech.core.helper.CargoFilter;
import io.taraxacum.finaltech.core.helper.CargoLimit;
import io.taraxacum.finaltech.core.helper.SlotSearchOrder;
import io.taraxacum.finaltech.core.helper.SlotSearchSize;
import lombok.*;
import me.matl114.matlib.Utils.Inventory.InventoryRecords.InventoryRecord;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Final_ROOT
 * @see io.taraxacum.finaltech.util.CargoUtil#doCargo(CargoDTO, String)
 * @since 2.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CargoDTO {
    private JavaPlugin javaPlugin;

    /**
     * Source #{@link Location} of #{@link BlockMenu} or #{@link Inventory}
     */
    private InventoryRecord inputBlock;

    /**
     * #{@link SlotSearchSize}
     */
    private String inputSize;

    /**
     * #{@link SlotSearchOrder}
     */
    private String inputOrder;

    /**
     * Target #{@link Location} of #{@link BlockMenu} or #{@link Inventory}
     */
    private InventoryRecord outputBlock;

    /**
     * #{@link SlotSearchSize}
     */
    private String outputSize;

    /**
     * #{@link SlotSearchOrder}
     */
    private String outputOrder;

    /**
     * Number limited in one cargo action
     */
    private int cargoNumber;

    /**
     * #{@link CargoLimit}
     */
    private String cargoLimit;

    /**
     * #{@link CargoFilter}
     */
    @Getter
    private String cargoFilter;

    /**
     * #{@link Inventory} for #{@link CargoFilter} to use
     */
    private Inventory filterInv;

    /**
     * the slots of the filterInv to be used
     */
    private int[] filterSlots;
    public CargoDTO(CargoDTO that){
        this(that.javaPlugin,that.inputBlock,that.inputSize,that.inputOrder,that.outputBlock,that.outputSize,that.outputOrder,that.cargoNumber,that.cargoLimit,that.cargoFilter,that.filterInv,that.filterSlots);
    }

}
