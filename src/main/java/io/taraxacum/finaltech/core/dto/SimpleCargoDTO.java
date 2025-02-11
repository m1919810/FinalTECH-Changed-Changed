package io.taraxacum.finaltech.core.dto;

import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.libs.plugin.dto.InvWithSlots;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.matl114.matlib.Utils.Inventory.InventoryRecords.InventoryRecord;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

/**
 * @author Final_ROOT
 * @see io.taraxacum.finaltech.util.CargoUtil#doSimpleCargoAsync(SimpleCargoDTO, String)
 * @since 2.0
 */
@Setter
@Getter
@Data
public class SimpleCargoDTO extends CargoDTO{

    private InvWithSlots inputMap;

    private InvWithSlots outputMap;

    public SimpleCargoDTO(InvWithSlots inputMap, InventoryRecord inputBlock, String inputSize, String inputOrder, InvWithSlots outputMap, InventoryRecord outputBlock, String outputSize, String outputOrder, int cargoNumber, String cargoLimit, String cargoFilter, Inventory filterInv, int[] filterSlots) {
        super(FinalTechChanged.getInstance(),inputBlock,inputSize,inputOrder,outputBlock,outputSize,outputOrder,cargoNumber,cargoLimit,cargoFilter,filterInv,filterSlots);
        this.inputMap = inputMap;
        this.outputMap = outputMap;
    }

    public SimpleCargoDTO(CargoDTO cargoDTO, InvWithSlots inputMap, InvWithSlots outputMap) {
        super(cargoDTO);
        this.inputMap = inputMap;
        this.outputMap = outputMap;
    }
    public SimpleCargoDTO(){
        super();
    }



}
