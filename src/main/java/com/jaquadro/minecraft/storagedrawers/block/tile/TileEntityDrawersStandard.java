package com.jaquadro.minecraft.storagedrawers.block.tile;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.event.DrawerPopulatedEvent;
import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.block.BlockStandardDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.StandardDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.core.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityDrawersStandard extends TileEntityDrawers {

    @CapabilityInject(IDrawerAttributes.class)
    static Capability<IDrawerAttributes> DRAWER_ATTRIBUTES_CAPABILITY = null;

    private static final String[] GUI_IDS = new String[]{
            null, StorageDrawers.MOD_ID + ":basicDrawers1", StorageDrawers.MOD_ID + ":basicDrawers2", null, StorageDrawers.MOD_ID + ":basicDrawers4"
    };

    private int capacity = 0;

    public static class Slot1 extends TileEntityDrawersStandard {

        private final GroupData groupData = new GroupData(1);

        public Slot1() {
            groupData.setCapabilityProvider(this);
            injectPortableData(groupData);
        }

        @Override
        public IDrawerGroup getGroup() {
            return groupData;
        }

        @Override
        protected void onAttributeChanged() {
            groupData.syncAttributes();
        }
    }

    public static class Slot2 extends TileEntityDrawersStandard {

        private final GroupData groupData = new GroupData(2);

        public Slot2() {
            groupData.setCapabilityProvider(this);
            injectPortableData(groupData);
        }

        @Override
        public IDrawerGroup getGroup() {
            return groupData;
        }

        @Override
        protected void onAttributeChanged() {
            groupData.syncAttributes();
        }
    }

    public static class Slot4 extends TileEntityDrawersStandard {

        private final GroupData groupData = new GroupData(4);

        public Slot4() {
            groupData.setCapabilityProvider(this);
            injectPortableData(groupData);
        }

        @Override
        public IDrawerGroup getGroup() {
            return groupData;
        }

        @Override
        protected void onAttributeChanged() {
            groupData.syncAttributes();
        }
    }

    public static class Legacy extends TileEntityDrawersStandard {

        private final GroupData groupData = new GroupData(4);

        public Legacy() {
            groupData.setCapabilityProvider(this);
            injectPortableData(groupData);
        }

        @Override
        public IDrawerGroup getGroup() {
            return groupData;
        }

        @Override
        protected void onAttributeChanged() {
            groupData.syncAttributes();
        }

        public void replaceWithCurrent() {
            TileEntityDrawersStandard replacement = createEntity(groupData.getDrawerCount());
            if (replacement != null) {
                replacement.deserializeNBT(serializeNBT());
                getWorld().setTileEntity(getPos(), replacement);
                replacement.markDirty();
            }
        }

        @Override
        public void validate() {
            super.validate();
            getWorld().scheduleBlockUpdate(getPos(), ModBlocks.basicDrawers, 1, 0);
        }

        @Override
        public NBTTagCompound writeToPortableNBT(NBTTagCompound tag) {
            return super.writeToPortableNBT(tag);
        }
    }

    public static TileEntityDrawersStandard createEntity(int slotCount) {
        return switch (slotCount) {
            case 1 -> new Slot1();
            case 2 -> new Slot2();
            case 4 -> new Slot4();
            default -> null;
        };
    }

    @Override
    public IDrawerGroup getGroup() {
        return null;
    }

    @Override
    public int getDrawerCapacity() {
        if (getWorld() == null || getWorld().isRemote)
            return super.getDrawerCapacity();

        if (capacity == 0) {
            IBlockState blockState = getWorld().getBlockState(this.pos);
            if (!blockState.getPropertyKeys().contains(BlockStandardDrawers.BLOCK))
                return 1;

            EnumBasicDrawer type = blockState.getValue(BlockStandardDrawers.BLOCK);
            capacity = type.getBaseStorage();

            if (capacity <= 0)
                capacity = 1;
        }

        return capacity;
    }

    private class GroupData extends StandardDrawerGroup {

        public GroupData(int slotCount) {
            super(slotCount);
        }

        @Nonnull
        @Override
        protected DrawerData createDrawer(int slot) {
            return new StandardDrawerData(this, slot);
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == TileEntityDrawersStandard.DRAWER_ATTRIBUTES_CAPABILITY
                    || super.hasCapability(capability, facing);

        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == TileEntityDrawersStandard.DRAWER_ATTRIBUTES_CAPABILITY)
                return (T) TileEntityDrawersStandard.this.getDrawerAttributes();

            return super.getCapability(capability, facing);
        }
    }

    private class StandardDrawerData extends StandardDrawerGroup.DrawerData {

        private final int slot;

        public StandardDrawerData(StandardDrawerGroup group, int slot) {
            super(group);
            this.slot = slot;
        }

        @Override
        protected int getStackCapacity() {
            return upgrades().getStorageMultiplier() * getEffectiveDrawerCapacity();
        }

        @Override
        protected void onItemChanged() {
            DrawerPopulatedEvent event = new DrawerPopulatedEvent(this);
            MinecraftForge.EVENT_BUS.post(event);

            if (getWorld() != null && !getWorld().isRemote) {
                markDirty();
                markBlockForUpdate();
            }
        }

        @Override
        protected void onAmountChanged() {
            if (getWorld() != null && !getWorld().isRemote) {
                syncClientCount(slot, getStoredItemCount());
                markDirty();
            }
        }
    }
}
