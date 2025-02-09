package io.taraxacum.finaltech.core.operation;

import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.setup.FinalTechItems;
import io.taraxacum.finaltech.util.ConstantTableUtil;
import io.taraxacum.libs.plugin.dto.ItemWrapper;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Final_ROOT
 * @since 1.0
 */
public class ItemCopyCardOperation implements ItemSerializationConstructorOperation {
    private final int difficulty;
    private final ItemStack matchItem;
    private final ItemWrapper matchItemWrapper;
    private final ItemStack copyCardItem;
    private final ItemStack showItem;
    private double count;

    protected ItemCopyCardOperation(@Nonnull ItemStack item) {
        this.count = item.getAmount();
        this.difficulty = ConstantTableUtil.ITEM_COPY_CARD_AMOUNT;
        this.matchItem = item.clone();
        this.matchItem.setAmount(1);
        this.matchItemWrapper = new ItemWrapper(this.matchItem);
        this.copyCardItem = FinalTechItems.COPY_CARD.getValidItem(this.matchItem, "1");
        this.showItem = new CustomItemStack(item.getType(), FinalTechChanged.getLanguageString("items", FinalTechItems.ITEM_SERIALIZATION_CONSTRUCTOR.getId(), "copy-card", "name"));
        this.updateShowItem();
    }

    public double getCount() {
        return this.count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    @Nonnull
    public ItemStack getMatchItem() {
        return this.matchItem;
    }

    @Override
    public int getType() {
        return ItemSerializationConstructorOperation.COPY_CARD;
    }

    @Nonnull
    @Override
    public ItemStack getShowItem() {
        return this.showItem;
    }

    @Override
    public void updateShowItem() {
        ItemStackUtil.setLore(this.showItem, FinalTechChanged.getLanguageManager().replaceStringArray(FinalTechChanged.getLanguageStringArray("items", FinalTechItems.ITEM_SERIALIZATION_CONSTRUCTOR.getId(), "copy-card", "lore"),
                ItemStackUtil.getItemName(this.matchItem),
                String.format("%.8f", this.count),
                String.valueOf(this.difficulty),
                String.valueOf(FinalTechItems.ITEM_SERIALIZATION_CONSTRUCTOR.getEfficiency() > 0 ? 1 / FinalTechItems.ITEM_SERIALIZATION_CONSTRUCTOR.getEfficiency() : "INFINITY")));
    }
    private static final Material PHONY_MATERIAL =  FinalTechItemStacks.PHONY.getType();
    @Override
    public int addItem(@Nullable ItemStack itemStack) {
        if (!this.isFinished() && itemStack!=null ) {
            if (ItemStackUtil.isItemSimilar(itemStack, this.matchItemWrapper)) {
                double efficiency = FinalTechItems.ITEM_SERIALIZATION_CONSTRUCTOR.getEfficiency();
                efficiency = Math.min(efficiency, 1);
                if (efficiency <= 0) {
                    return 0;
                }
                int amount = (int) Math.ceil((this.difficulty - this.count) / efficiency);
                int itemAmount = itemStack.getAmount();
                if(amount <= itemAmount) {
                    itemStack.setAmount(itemAmount - amount);
                    this.count += amount * efficiency;
                    return amount;
                }else{
                    itemStack.setAmount(0);
                    this.count += itemAmount * efficiency;
                    return itemAmount;
                }
//                if (itemStack.getAmount() * efficiency + this.count < this.difficulty) {
//                    int amount = itemStack.getAmount();
//                    itemStack.setAmount(itemStack.getAmount() - amount);
//                    this.count += amount * efficiency;
//                    return amount;
//                } else {
//
//                    itemStack.setAmount(itemStack.getAmount() - amount);
//                    this.count = this.difficulty;
//                    return amount;
//                }
            } else if (itemStack.getType()== PHONY_MATERIAL && FinalTechItems.ITEM_PHONY.verifyItem(itemStack)) {
                double amount = Math.min(itemStack.getAmount(), this.difficulty - this.count);
                itemStack.setAmount(itemStack.getAmount() - (int) Math.ceil(amount));
                this.count += amount;
                return (int) Math.ceil(amount);
            }
        }
        return 0;
    }

    @Override
    public boolean isFinished() {
        return this.count >= this.difficulty;
    }

    @Nonnull
    @Override
    public ItemStack getResult() {
        return this.copyCardItem;
    }

    @Deprecated
    @Override
    public void addProgress(int i) {

    }

    @Deprecated
    @Override
    public int getProgress() {
        return (int) Math.floor(this.count);
    }

    @Deprecated
    @Override
    public int getTotalTicks() {
        return this.difficulty;
    }
}
