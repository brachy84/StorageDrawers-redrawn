package com.jaquadro.minecraft.storagedrawers.block.tile;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.FractionalDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.config.SDConfig;
import com.jaquadro.minecraft.storagedrawers.network.CountUpdateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityDrawersComp extends TileEntityDrawers {

    @CapabilityInject(IDrawerAttributes.class)
    static Capability<IDrawerAttributes> DRAWER_ATTRIBUTES_CAPABILITY = null;

    private final GroupData groupData;

    private int capacity = 0;

    public TileEntityDrawersComp() {
        groupData = new GroupData(3);
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

    private class GroupData extends FractionalDrawerGroup {

        public GroupData(int slotCount) {
            super(slotCount);
        }

        @Override
        protected World getWorld() {
            return TileEntityDrawersComp.this.getWorld();
        }

        @Override
        protected void log(String message) {
            if (!getWorld().isRemote && SDConfig.general.enableDebugLogging)
                StorageDrawers.log.info(message);
        }

        @Override
        protected int getStackCapacity() {
            return upgrades().getStorageMultiplier() * getEffectiveDrawerCapacity();
        }

        @Override
        protected void onItemChanged() {
            if (getWorld() != null && !getWorld().isRemote) {
                markDirty();
                markBlockForUpdate();
            }
        }

        @Override
        protected void onAmountChanged() {
            if (getWorld() != null && !getWorld().isRemote) {
                IMessage message = new CountUpdateMessage(getPos(), 0, getPooledCount());
                NetworkRegistry.TargetPoint targetPoint = new NetworkRegistry.TargetPoint(getWorld().provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 500);

                StorageDrawers.network.sendToAllAround(message, targetPoint);

                markDirty();
            }
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == TileEntityDrawersComp.DRAWER_ATTRIBUTES_CAPABILITY
                    || super.hasCapability(capability, facing);

        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == TileEntityDrawersComp.DRAWER_ATTRIBUTES_CAPABILITY)
                return (T) TileEntityDrawersComp.this.getDrawerAttributes();

            return super.getCapability(capability, facing);
        }
    }

    @Override
    public int getDrawerCapacity() {
        if (getWorld() == null || getWorld().isRemote)
            return super.getDrawerCapacity();

        if (capacity == 0) {
            capacity = SDConfig.blocks.compdrawers.baseStorage;

            if (capacity <= 0)
                capacity = 1;
        }

        return capacity;
    }

    @Override
    public boolean dataPacketRequiresRenderUpdate() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void clientUpdateCount(final int slot, final int count) {
        if (!getWorld().isRemote)
            return;

        Minecraft.getMinecraft().addScheduledTask(() -> TileEntityDrawersComp.this.clientUpdateCountAsync(count));
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdateCountAsync(int count) {
        groupData.setPooledCount(count);
    }

    @Override
    public String getName() {
        return hasCustomName() ? super.getName() : "storagedrawers.container.compDrawers";
    }
}
