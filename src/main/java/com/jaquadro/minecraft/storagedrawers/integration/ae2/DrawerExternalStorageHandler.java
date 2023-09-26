package com.jaquadro.minecraft.storagedrawers.integration.ae2;

public class DrawerExternalStorageHandler // implements IExternalStorageHandler
{
    /*private IStorageBusMonitorFactory sbmFactory;

    public DrawerExternalStorageHandler (IStorageBusMonitorFactory factory) {
        sbmFactory = factory;
    }

    @Override
    public boolean canHandle (TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc) {
        return channel == StorageChannel.ITEMS && te instanceof IDrawerGroup;
    }

    @Override
    public IMEInventory getInventory (TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src) {
        if (sbmFactory != null && channel == StorageChannel.ITEMS)
            return sbmFactory.createStorageBusMonitor(new DrawerMEInventory((IDrawerGroup) te), src);

        return null;
    }*/
}
