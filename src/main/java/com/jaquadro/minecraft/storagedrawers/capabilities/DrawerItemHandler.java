package com.jaquadro.minecraft.storagedrawers.capabilities;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class DrawerItemHandler implements IItemHandler {

    @CapabilityInject(IItemRepository.class)
    public static Capability<IItemRepository> ITEM_REPOSITORY_CAPABILITY = null;

    private final IDrawerGroup group;

    public DrawerItemHandler(IDrawerGroup group) {
        this.group = group;
    }

    @Override
    public int getSlots() {
        return group.getDrawerCount() + 1;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if (slotIsVirtual(slot))
            return ItemStack.EMPTY;

        slot -= 1;
        int[] order = group.getAccessibleDrawerSlots();
        slot = (slot >= 0 && slot < order.length) ? order[slot] : -1;

        IDrawer drawer = group.getDrawer(slot);
        if (!drawer.isEnabled() || drawer.isEmpty())
            return ItemStack.EMPTY;

        return drawer.getPublicItemStack();
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slotIsVirtual(slot)) {
            if (StorageDrawers.config.cache.enableItemConversion)
                return insertItemFullScan(stack, simulate);
            else
                return stack;
        }

        slot -= 1;
        int[] order = group.getAccessibleDrawerSlots();
        int orderedSlot = (slot >= 0 && slot < order.length) ? order[slot] : -1;
        int prevSlot = (slot >= 1 && slot < order.length) ? order[slot - 1] : -1;

        if (StorageDrawers.config.cache.enableItemConversion && orderedSlot > 0) {
            IDrawer drawer = group.getDrawer(orderedSlot);
            if (drawer.isEnabled() && drawer.isEmpty()) {
                IDrawer prevDrawer = group.getDrawer(prevSlot);
                if (!prevDrawer.isEnabled() || !prevDrawer.isEmpty())
                    return insertItemFullScan(stack, simulate);
            }
        }

        return insertItemInternal(orderedSlot, stack, simulate);
    }

    @Nonnull
    private ItemStack insertItemFullScan(@Nonnull ItemStack stack, boolean simulate) {
        IItemRepository itemRepo = group.getCapability(ITEM_REPOSITORY_CAPABILITY, null);
        if (itemRepo != null)
            return itemRepo.insertItem(stack, simulate);

        for (int i = 0; i < group.getDrawerCount(); i++) {
            stack = insertItemInternal(i, stack, simulate);
            if (stack.isEmpty())
                break;
        }

        return stack;
    }

    @Nonnull
    private ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
        IDrawer drawer = group.getDrawer(slot);
        if (!drawer.canItemBeStored(stack))
            return stack;

        if (drawer.isEmpty() && !simulate)
            drawer = drawer.setStoredItem(stack);

        boolean empty = drawer.isEmpty();
        int remainder = (simulate)
                ? Math.max(stack.getCount() - (empty ? drawer.getAcceptingMaxCapacity(stack) : drawer.getAcceptingRemainingCapacity()), 0)
                : drawer.adjustStoredItemCount(stack.getCount());

        if (remainder == stack.getCount())
            return stack;
        if (remainder == 0)
            return ItemStack.EMPTY;

        return stackResult(stack, remainder);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slotIsVirtual(slot))
            return ItemStack.EMPTY;

        slot -= 1;
        int[] order = group.getAccessibleDrawerSlots();
        slot = (slot >= 0 && slot < order.length) ? order[slot] : -1;

        IDrawer drawer = group.getDrawer(slot);
        if (!drawer.isEnabled() || drawer.isEmpty() || drawer.getStoredItemCount() == 0)
            return ItemStack.EMPTY;

        @Nonnull ItemStack prototype = drawer.getStoredItemPrototype();
        int remaining = (simulate)
                ? Math.max(amount - drawer.getStoredItemCount(), 0)
                : drawer.adjustStoredItemCount(-amount);

        return stackResult(prototype, amount - remaining);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slotIsVirtual(slot))
            return Integer.MAX_VALUE;

        IDrawer drawer = group.getDrawer(slot);
        if (!drawer.isEnabled())
            return 0;
        if (drawer.isEmpty())
            return drawer.getMaxCapacity(ItemStack.EMPTY);

        return drawer.getMaxCapacity();
    }

    private boolean slotIsVirtual(int slot) {
        return slot == 0;
    }

    private ItemStack stackResult(@Nonnull ItemStack stack, int amount) {
        ItemStack result = stack.copy();
        result.setCount(amount);
        return result;
    }
}
