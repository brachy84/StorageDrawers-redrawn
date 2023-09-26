package com.jaquadro.minecraft.storagedrawers.inventory;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class InventoryUpgrade implements IInventory {

    private static final int upgradeCapacity = 7;

    private final TileEntityDrawers tile;

    public InventoryUpgrade(TileEntityDrawers tileEntity) {
        tile = tileEntity;
    }

    @Override
    public int getSizeInventory() {
        return upgradeCapacity;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < upgradeCapacity; i++) {
            if (!tile.upgrades().getUpgrade(i).isEmpty())
                return false;
        }

        return true;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return tile.upgrades().getUpgrade(slot);
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int slot, int count) {
        ItemStack stack = tile.upgrades().getUpgrade(slot);
        if (count > 0)
            tile.upgrades().setUpgrade(slot, ItemStack.EMPTY);

        return stack;
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack item) {
        tile.upgrades().setUpgrade(slot, item);
    }

    @Override
    public String getName() {
        return tile.getName();
    }

    @Override
    public boolean hasCustomName() {
        return tile.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return tile.getDisplayName();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
        tile.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        BlockPos pos = tile.getPos();

        if (tile.getWorld().getTileEntity(pos) != tile)
            return false;

        return player.getDistanceSq(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack item) {
        return tile.upgrades().canAddUpgrade(item);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    public boolean canAddUpgrade(@Nonnull ItemStack item) {
        return tile.upgrades().canAddUpgrade(item);
    }

    public boolean canRemoveStorageUpgrade(int slot) {
        return tile.upgrades().canRemoveUpgrade(slot);
    }

    public boolean canSwapUpgrade(int slot, @Nonnull ItemStack item) {
        return tile.upgrades().canSwapUpgrade(slot, item);
    }
}
