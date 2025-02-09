package io.taraxacum.finaltech.core.menu.machine;

import io.taraxacum.finaltech.core.item.machine.AbstractMachine;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;

/**
 * @author Final_ROOT
 * @since 2.2
 */
public class SimulateClickMachineMenu extends AbstractMachineMenu {
    private static final int[] BORDER = new int[]{0, 1, 4, 7, 8};
    private static final int[] INPUT_BORDER = new int[]{2};
    private static final int[] OUTPUT_BORDER = new int[]{6};
    private static final int[] INPUT_SLOT = new int[]{3};
    private static final int[] OUTPUT_SLOT = new int[]{5};

    public SimulateClickMachineMenu(@Nonnull AbstractMachine machine) {
        super(machine);
    }

    @Override
    protected int[] getBorder() {
        return BORDER;
    }

    @Override
    protected int[] getInputBorder() {
        return INPUT_BORDER;
    }

    @Override
    protected int[] getOutputBorder() {
        return OUTPUT_BORDER;
    }

    @Override
    public int[] getInputSlot() {
        return INPUT_SLOT;
    }

    @Override
    public int[] getOutputSlot() {
        return OUTPUT_SLOT;
    }

    @Override
    protected void updateInventory(@Nonnull Inventory inventory, @Nonnull Location location) {

    }
}
