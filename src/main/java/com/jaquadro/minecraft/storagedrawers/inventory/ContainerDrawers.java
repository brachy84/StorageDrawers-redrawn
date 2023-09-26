package com.jaquadro.minecraft.storagedrawers.inventory;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.client.renderer.StorageRenderItem;
import com.jaquadro.minecraft.storagedrawers.item.ItemUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ContainerDrawers extends Container {

    private static final int InventoryX = 8;
    private static final int InventoryY = 117;
    private static final int HotbarY = 175;

    private static final int UpgradeX = 26;
    private static final int UpgradeY = 86;

    private final IInventory upgradeInventory;

    private final List<Slot> storageSlots;
    private final List<Slot> upgradeSlots;
    private final List<Slot> playerSlots;
    private final List<Slot> hotbarSlots;

    @SideOnly(Side.CLIENT)
    public StorageRenderItem activeRenderItem;

    private final boolean isRemote;

    public ContainerDrawers(InventoryPlayer playerInventory, TileEntityDrawers tileEntity) {
        upgradeInventory = new InventoryUpgrade(tileEntity);

        storageSlots = new ArrayList<>();
        for (int i = 0; i < tileEntity.getDrawerCount(); i++)
            storageSlots.add(addSlotToContainer(new SlotDrawer(this, tileEntity.getGroup(), i, getStorageSlotX(i), getStorageSlotY(i))));

        upgradeSlots = new ArrayList<>();
        for (int i = 0; i < 7; i++)
            upgradeSlots.add(addSlotToContainer(new SlotUpgrade(upgradeInventory, i, UpgradeX + i * 18, UpgradeY)));

        playerSlots = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++)
                playerSlots.add(addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, InventoryX + j * 18, InventoryY + i * 18)));
        }

        hotbarSlots = new ArrayList<>();
        for (int i = 0; i < 9; i++)
            hotbarSlots.add(addSlotToContainer(new Slot(playerInventory, i, InventoryX + i * 18, HotbarY)));

        isRemote = tileEntity.getWorld().isRemote;
    }

    public void setLastAccessedItem(ItemStack stack) {
        if (isRemote && activeRenderItem != null)
            activeRenderItem.overrideStack = stack;
    }

    protected int getStorageSlotX(int slot) {
        return 0;
    }

    protected int getStorageSlotY(int slot) {
        return 0;
    }

    public List<Slot> getStorageSlots() {
        return storageSlots;
    }

    public List<Slot> getUpgradeSlots() {
        return upgradeSlots;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return upgradeInventory.isUsableByPlayer(player);
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(slotIndex);

        int storageStart = storageSlots.get(0).slotNumber;
        int storageEnd = storageSlots.get(storageSlots.size() - 1).slotNumber + 1;
        int upgradeStart = upgradeSlots.get(0).slotNumber;
        int upgradeEnd = upgradeSlots.get(upgradeSlots.size() - 1).slotNumber + 1;

        // Assume inventory and hotbar slot IDs are contiguous
        int inventoryStart = playerSlots.get(0).slotNumber;
        int hotbarStart = hotbarSlots.get(0).slotNumber;
        int hotbarEnd = hotbarSlots.get(hotbarSlots.size() - 1).slotNumber + 1;

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();

            // Try merge upgrades to inventory
            if (slotIndex >= upgradeStart && slotIndex < upgradeEnd) {
                if (!mergeItemStack(slotStack, inventoryStart, hotbarEnd, true))
                    return ItemStack.EMPTY;
                slot.onSlotChange(slotStack, itemStack);
            }

            // Try merge inventory to upgrades
            else if (slotIndex >= inventoryStart && slotIndex < hotbarEnd && !slotStack.isEmpty()) {
                if (slotStack.getItem() instanceof ItemUpgrade) {
                    ItemStack slotStack1 = slotStack.copy();
                    slotStack1.setCount(1);

                    if (!mergeItemStack(slotStack1, upgradeStart, upgradeEnd, false)) {
                        if (slotIndex >= inventoryStart && slotIndex < hotbarStart) {
                            if (!mergeItemStack(slotStack, hotbarStart, hotbarEnd, false))
                                return ItemStack.EMPTY;
                        } else if (slotIndex >= hotbarStart && slotIndex < hotbarEnd && !mergeItemStack(slotStack, inventoryStart, hotbarStart, false))
                            return ItemStack.EMPTY;
                    } else {
                        slotStack.shrink(1);
                        if (slotStack.getCount() == 0)
                            slot.putStack(ItemStack.EMPTY);
                        else
                            slot.onSlotChanged();

                        slot.onTake(player, slotStack);
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex >= inventoryStart && slotIndex < hotbarStart) {
                    if (!mergeItemStack(slotStack, hotbarStart, hotbarEnd, false))
                        return ItemStack.EMPTY;
                } else if (slotIndex >= hotbarStart && slotIndex < hotbarEnd && !mergeItemStack(slotStack, inventoryStart, hotbarStart, false))
                    return ItemStack.EMPTY;
            }

            // Try merge stack into inventory
            else if (!mergeItemStack(slotStack, inventoryStart, hotbarEnd, false))
                return ItemStack.EMPTY;

            int slotStackSize = slotStack.getCount();
            if (slotStackSize == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();

            if (slotStackSize == itemStack.getCount())
                return ItemStack.EMPTY;

            slot.onTake(player, slotStack);
        }

        return itemStack;
    }
}
