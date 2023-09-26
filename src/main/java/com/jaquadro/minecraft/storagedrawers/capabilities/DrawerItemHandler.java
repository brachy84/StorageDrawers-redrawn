package com.jaquadro.minecraft.storagedrawers.capabilities;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class DrawerItemHandler implements IItemHandler {

    private final IDrawerGroup group;

    public DrawerItemHandler(IDrawerGroup group) {
        this.group = group;
    }

    @Override
    public int getSlots() {
        return group.getDrawerCount();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        int[] order = group.getAccessibleDrawerSlots();
        slot = (slot >= 0 && slot < order.length) ? order[slot] : -1;

        IDrawer drawer = group.getDrawer(slot);
        if (!drawer.isEnabled() || drawer.isEmpty())
            return ItemStack.EMPTY;

        return drawer.getPublicItemStack();
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        int[] order = group.getAccessibleDrawerSlots();
        int orderedSlot = (slot >= 0 && slot < order.length) ? order[slot] : -1;
        return insertItemInternal(orderedSlot, stack, simulate);
    }

    @NotNull
    private ItemStack insertItemInternal(int slot, @NotNull ItemStack stack, boolean simulate) {
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
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int[] order = group.getAccessibleDrawerSlots();
        slot = (slot >= 0 && slot < order.length) ? order[slot] : -1;

        IDrawer drawer = group.getDrawer(slot);
        if (!drawer.isEnabled() || drawer.isEmpty() || drawer.getStoredItemCount() == 0)
            return ItemStack.EMPTY;

        ItemStack prototype = drawer.getStoredItemPrototype();
        int remaining = (simulate)
                ? Math.max(amount - drawer.getStoredItemCount(), 0)
                : drawer.adjustStoredItemCount(-amount);

        return stackResult(prototype, amount - remaining);
    }

    @Override
    public int getSlotLimit(int slot) {
        IDrawer drawer = group.getDrawer(slot);
        if (!drawer.isEnabled())
            return 0;
        if (drawer.isEmpty())
            return drawer.getMaxCapacity(ItemStack.EMPTY);

        return drawer.getMaxCapacity();
    }

    private ItemStack stackResult(@NotNull ItemStack stack, int amount) {
        ItemStack result = stack.copy();
        result.setCount(amount);
        return result;
    }
}
