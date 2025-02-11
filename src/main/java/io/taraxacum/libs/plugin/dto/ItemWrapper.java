package io.taraxacum.libs.plugin.dto;

import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import me.matl114.matlib.Utils.Algorithm.LazyInitReference;
import me.matl114.matlib.Utils.Inventory.ItemStacks.CleanItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemWrapper implements Cloneable{
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    //immutable
    private static final ItemWrapper INSTANCE = new ItemWrapper(null);
    //immutable
    private static final ItemWrapper AIR_INSTANCE = new ItemWrapper(AIR);
    @Nonnull
    private ItemStack itemStack;
    @Nonnull
    private LazyInitReference<ItemMeta> itemMetaReference;

    public ItemWrapper() {
        this.itemStack = AIR;
        this.itemMetaReference = LazyInitReference.ofValue(null);
    }
    public static ItemWrapper empty(){
        return AIR_INSTANCE.clone();
    }
    public ItemWrapper(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMetaReference = LazyInitReference.ofEmpty();
       // this.itemMeta = this.itemStack.hasItemMeta() ? this.itemStack.getItemMeta() : null;
    }
    public static ItemWrapper of(@Nonnull ItemStack itemStack) {
        ItemWrapper wrapper = INSTANCE.clone();
        wrapper.itemStack = itemStack;
        wrapper.itemMetaReference = LazyInitReference.ofEmpty();
        return wrapper;
    }
    public static ItemWrapper ofNullable(@Nullable ItemStack itemStack) {
        return itemStack == null?null:of(itemStack);

    }

    /**
     * wrap the input itemStack and set its meta reference as the input itemMeta
     * @param itemStack
     * @param itemMeta
     * @return
     */
    public static ItemWrapper ofMeta(ItemStack itemStack,ItemMeta itemMeta) {
        ItemWrapper wrapper = INSTANCE.clone();
        wrapper.itemMetaReference = LazyInitReference.ofValue(itemMeta);
        wrapper.itemStack = itemStack;
        return wrapper;
    }
    public static ItemWrapper ofNullableMeta(@Nullable ItemStack itemStack,ItemMeta itemMeta) {
        if (itemStack == null) {
            return null;
        }else {
            return ofMeta(itemStack, itemMeta);
        }
    }
//    public static ItemWrapper ofMetaRef(ItemStack itemStack,AtomicReference<ItemMeta> itemMeta) {
//        ItemWrapper wrapper = INSTANCE.clone();
//        wrapper.itemMeta = itemMeta;
//        wrapper.itemStack = itemStack;
//        wrapper.initMeta = true;
//        return wrapper;
//    }
    public ItemWrapper(@Nonnull ItemStack itemStack, @Nullable ItemMeta itemMeta) {
        this.itemStack = itemStack;
        this.itemMetaReference = LazyInitReference.ofValue(itemMeta);
    }

    /**
     * conpletely copy itemStack reference and itemMeta reference
     * @param itemWrapper
     * @return
     */
    public static ItemWrapper copyOf(ItemWrapper itemWrapper) {
        return itemWrapper.clone();
    }

    /**
     * wrap the input itemStack, and set ItemMeta Reference from the input wrapper
     * @param itemStack
     * @param itemWrapper
     * @return
     */
    public static ItemWrapper setMetaOf(ItemStack itemStack,ItemWrapper itemWrapper) {
        ItemWrapper wrapper = itemWrapper.clone();
        wrapper.itemStack = itemStack;
        return wrapper;
    }
    public static ItemWrapper setNullableMetaOf(ItemStack itemStack,ItemWrapper itemWrapper){
        if(itemStack == null){
            return null;
        }else {
            return setMetaOf(itemStack, itemWrapper);
        }
    }

    /**
     * wrap the input itemStack'cloneItem, and set ItemMeta Reference from the input wrapper
     * @param itemStack
     * @param itemWrapper
     * @return
     */
    public static ItemWrapper copyMetaOf(ItemStack itemStack,ItemWrapper itemWrapper) {
        ItemWrapper wrapper = itemWrapper.clone();
        wrapper.itemStack = ItemStackUtil.cloneItem(itemStack);
        return wrapper;
    }

    @Nonnull
    public static ItemStack[] getItemArray(@Nonnull ItemWrapper[] itemWrappers) {
        ItemStack[] itemStacks = new ItemStack[itemWrappers.length];
        for (int i = 0, length = itemStacks.length; i < length; i++) {
            itemStacks[i] = itemWrappers[i].getItemStack();
        }
        return itemStacks;
    }

    @Nonnull
    public static ItemStack[] getItemArray(@Nonnull List<? extends ItemWrapper> itemWrapperList) {
        ItemStack[] itemStacks = new ItemStack[itemWrapperList.size()];
        for (int i = 0, length = itemStacks.length; i < length; i++) {
            itemStacks[i] = itemWrapperList.get(i).getItemStack();
        }
        return itemStacks;
    }

    @Nonnull
    public static ItemStack[] getCopiedItemArray(@Nonnull List<? extends ItemWrapper> itemWrapperList) {
        ItemStack[] itemStacks = new ItemStack[itemWrapperList.size()];
        for (int i = 0, length = itemStacks.length; i < length; i++) {
            itemStacks[i] = ItemStackUtil.cloneItem(itemWrapperList.get(i).getItemStack());
        }
        return itemStacks;
    }

    @Nonnull
    public static List<ItemStack> getItemList(@Nonnull ItemWrapper[] itemWrappers) {
        List<ItemStack> itemStackList = new ArrayList<>(itemWrappers.length);
        for (ItemWrapper itemWrapper : itemWrappers) {
            itemStackList.add(itemWrapper.getItemStack());
        }
        return itemStackList;
    }

    @Nonnull
    public static List<ItemStack> getItemList(@Nonnull List<? extends ItemWrapper> itemWrapperList) {
        List<ItemStack> itemStackList = new ArrayList<>(itemWrapperList.size());
        for (ItemWrapper itemWrapper : itemWrapperList) {
            itemStackList.add(itemWrapper.getItemStack());
        }
        return itemStackList;
    }

    @Nonnull
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Nonnull
    public Material getItemType(){
        return this.itemStack.getType();
    }

    /**
     * equals getItemStack.getAmount
     * @return
     */
    public int getItemAmount(){
        return this.itemStack.getAmount();
    }

    /**
     * equals getItemStack.getMaxStackSize
     * @return
     */
    public int getMaxStackCnt(){
        return this.itemStack.getMaxStackSize();
    }

    public void setItemStack(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Nullable
    public ItemMeta getItemMeta() {
        if(!itemMetaReference.init){
            this.itemMetaReference.value = this.itemStack.hasItemMeta() ? this.itemStack.getItemMeta() : null;
            itemMetaReference.init = true;
        }
        return this.itemMetaReference.value;
    }

    public boolean quickMetaRefMatch(ItemWrapper itemWrapper) {
        return this.itemMetaReference == itemWrapper.itemMetaReference;
    }

//    public void setItemMeta(@Nullable ItemMeta itemMeta) {
//        this.itemMeta = itemMeta;
//        this.initMeta = true;
//    }

//    public boolean hasItemMeta() {
//        return getItemMeta() != null;
//    }

    public void updateItemMeta() {
        this.itemMetaReference = LazyInitReference.ofValue(this.itemStack.hasItemMeta() ? this.itemStack.getItemMeta() : null);
    }

    public void newWrap(@Nonnull ItemStack itemStack) {
        //reset all data
        this.itemStack = itemStack;
        this.itemMetaReference = LazyInitReference.ofEmpty();
    }

    public void newWrap(@Nonnull ItemStack itemStack, @Nullable ItemMeta itemMeta) {
        this.itemStack = itemStack;
        this.itemMetaReference = LazyInitReference.ofValue(itemMeta);
    }

    @Nonnull
    public ItemWrapper shallowClone() {
        return this.clone();
        //return this.initMeta? new ItemWrapper(this.itemStack, this.itemMeta);
    }

    @Nonnull
    public ItemWrapper deepClone() {
        ItemWrapper  that = this.clone();
        that.itemStack = CleanItemStack.ofBukkitClean(this.itemStack);
        return that;
    }

    @Override
    public int hashCode() {
        int hash = 31 + this.itemStack.getType().hashCode();
        hash = hash * 31 + this.itemStack.getAmount();
        hash = hash * 31 + (this.itemStack.getDurability() & 0xffff);
       // hash = hash * 31 + (this.itemMeta != null ? (this.itemMeta.hashCode()) : 0);
        return hash;
    }

    @Override
    public boolean equals(@Nonnull Object obj) {
        if (this.itemStack instanceof ItemStackWrapper) {
            return CleanItemStack.ofBukkitClean(this.itemStack).equals(obj);
        } else {
            return this.itemStack.equals(obj);
        }
    }

    @Override
    public ItemWrapper clone() {
        try {
            ItemWrapper clone = (ItemWrapper) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
