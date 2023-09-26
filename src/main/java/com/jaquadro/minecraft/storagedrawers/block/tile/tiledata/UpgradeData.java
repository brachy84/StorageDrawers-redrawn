package com.jaquadro.minecraft.storagedrawers.block.tile.tiledata;

import com.jaquadro.minecraft.chameleon.block.tiledata.TileDataShim;
import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributesModifiable;
import com.jaquadro.minecraft.storagedrawers.config.ConfigManager;
import com.jaquadro.minecraft.storagedrawers.core.ModItems;
import com.jaquadro.minecraft.storagedrawers.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class UpgradeData extends TileDataShim {

    private final ItemStack[] upgrades;
    private int storageMultiplier;
    private EnumUpgradeStatus statusType;
    private EnumUpgradeRedstone redstoneType;

    // TODO: Do we need to provide these?
    private boolean hasOneStack;
    private boolean hasVoid;
    private boolean hasUnlimited;
    private boolean hasVending;
    private boolean hasConversion;

    private IDrawerAttributesModifiable attrs;

    public UpgradeData(int slotCount) {
        upgrades = new ItemStack[slotCount];
        Arrays.fill(upgrades, ItemStack.EMPTY);

        syncStorageMultiplier();
    }

    public void setDrawerAttributes(IDrawerAttributesModifiable attrs) {
        this.attrs = attrs;
        syncUpgrades();
    }

    public int getSlotCount() {
        return upgrades.length;
    }

    @Nonnull
    public ItemStack getUpgrade(int slot) {
        slot = MathHelper.clamp(slot, 0, upgrades.length - 1);
        return upgrades[slot];
    }

    public boolean addUpgrade(@Nonnull ItemStack upgrade) {
        int slot = getNextUpgradeSlot();
        if (slot == -1)
            return false;

        setUpgrade(slot, upgrade);
        return true;
    }

    public boolean setUpgrade(int slot, @Nonnull ItemStack upgrade) {
        slot = MathHelper.clamp(slot, 0, upgrades.length - 1);

        if (!upgrade.isEmpty()) {
            upgrade = upgrade.copy();
            upgrade.setCount(1);
        }

        ItemStack prevUpgrade = upgrades[slot];
        if (!prevUpgrade.isEmpty() && !canRemoveUpgrade(slot))
            return false;

        upgrades[slot] = ItemStack.EMPTY;
        syncStorageMultiplier();

        if (!upgrade.isEmpty() && !canAddUpgrade(upgrade)) {
            upgrades[slot] = prevUpgrade;
            syncStorageMultiplier();
            return false;
        }

        upgrades[slot] = upgrade;

        syncUpgrades();
        onUpgradeChanged(prevUpgrade, upgrade);

        return true;
    }

    public boolean canAddUpgrade(@Nonnull ItemStack upgrade) {
        if (upgrade.isEmpty())
            return false;
        if (!(upgrade.getItem() instanceof ItemUpgrade candidate))
            return false;

        if (candidate.getAllowMultiple())
            return true;

        for (ItemStack stack : upgrades) {
            if (stack.isEmpty())
                continue;

            if (!(stack.getItem() instanceof ItemUpgrade reference))
                continue;

            if (candidate == reference)
                return false;
        }

        return true;
    }

    public boolean canRemoveUpgrade(int slot) {
        slot = MathHelper.clamp(slot, 0, upgrades.length - 1);
        return !upgrades[slot].isEmpty();
    }


    /**
     * A util method, to see if the upgrades can be swapped
     *
     * @param slot The slot where the upgrade is being removed
     * @param add  The ItemStack of the upgrade being added
     * @return Whether the upgrades can be swapped
     */
    public boolean canSwapUpgrade(int slot, @Nonnull ItemStack add) {
        return canAddUpgrade(add) && canRemoveUpgrade(slot);
    }

    public int getStorageMultiplier() {
        return storageMultiplier;
    }

    public EnumUpgradeStatus getStatusType() {
        return statusType;
    }

    public EnumUpgradeRedstone getRedstoneType() {
        return redstoneType;
    }

    public boolean hasOneStackUpgrade() {
        return hasOneStack;
    }

    public boolean hasUnlimitedUpgrade() {
        return hasUnlimited;
    }

    public boolean hasVendingUpgrade() {
        return hasVending;
    }

    public boolean hasConversionUpgrade() {
        return hasConversion;
    }

    private int getNextUpgradeSlot() {
        for (int i = 0; i < upgrades.length; i++) {
            if (upgrades[i].isEmpty())
                return i;
        }

        return -1;
    }

    private void syncUpgrades() {
        if (this.attrs == null)
            return;

        syncStorageMultiplier();
        syncStatusLevel();
        syncRedstoneLevel();

        hasOneStack = false;
        hasVoid = false;
        hasUnlimited = false;
        hasVending = false;
        hasConversion = false;

        for (ItemStack stack : upgrades) {
            Item item = stack.getItem();
            if (item == ModItems.upgradeOneStack)
                hasOneStack = true;
            else if (item == ModItems.upgradeVoid)
                hasVoid = true;
            else if (item == ModItems.upgradeConversion)
                hasConversion = true;
            else if (item == ModItems.upgradeCreative) {
                EnumUpgradeCreative type = EnumUpgradeCreative.byMetadata(stack.getMetadata());
                if (type == EnumUpgradeCreative.STORAGE)
                    hasUnlimited = true;
                else if (type == EnumUpgradeCreative.VENDING)
                    hasVending = true;
            }
        }

        attrs.setIsVoid(hasVoid);
        attrs.setIsDictConvertible(hasConversion);
        attrs.setIsUnlimitedStorage(hasUnlimited);
        attrs.setIsUnlimitedVending(hasVending);
    }

    private void syncStorageMultiplier() {
        ConfigManager config = StorageDrawers.config;
        storageMultiplier = 0;

        for (ItemStack stack : upgrades) {
            if (stack.getItem() == ModItems.upgradeStorage) {
                int level = EnumUpgradeStorage.byMetadata(stack.getMetadata()).getLevel();
                storageMultiplier += config.getStorageUpgradeMultiplier(level);
            }
        }

        if (storageMultiplier == 0)
            storageMultiplier = config.getStorageUpgradeMultiplier(1);
    }

    private void syncStatusLevel() {
        statusType = null;

        for (ItemStack stack : upgrades) {
            if (stack.getItem() == ModItems.upgradeStatus) {
                statusType = EnumUpgradeStatus.byMetadata(stack.getMetadata());
                break;
            }
        }
    }

    private void syncRedstoneLevel() {
        redstoneType = null;

        for (ItemStack stack : upgrades) {
            if (stack.getItem() == ModItems.upgradeRedstone) {
                redstoneType = EnumUpgradeRedstone.byMetadata(stack.getMetadata());
                break;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        Arrays.fill(upgrades, ItemStack.EMPTY);

        if (!tag.hasKey("Upgrades"))
            return;

        NBTTagList tagList = tag.getTagList("Upgrades", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound upgradeTag = tagList.getCompoundTagAt(i);

            int slot = upgradeTag.getByte("Slot");
            upgrades[slot] = new ItemStack(upgradeTag);
        }

        syncUpgrades();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < upgrades.length; i++) {
            if (!upgrades[i].isEmpty()) {
                NBTTagCompound upgradeTag = upgrades[i].writeToNBT(new NBTTagCompound());
                upgradeTag.setByte("Slot", (byte) i);

                tagList.appendTag(upgradeTag);
            }
        }

        tag.setTag("Upgrades", tagList);
        return tag;
    }

    protected void onUpgradeChanged(ItemStack oldUpgrade, ItemStack newUpgrade) {
    }
}
