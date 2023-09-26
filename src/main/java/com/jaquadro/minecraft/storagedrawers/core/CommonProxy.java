package com.jaquadro.minecraft.storagedrawers.core;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonProxy {

    public final ResourceLocation iconConcealmentOverlayResource = new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/overlay/shading_concealment");
    public final ResourceLocation iconIndicatorCompOnResource = new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/indicator/indicator_comp_on");
    public final ResourceLocation iconIndicatorCompOffResource = new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/indicator/indicator_comp_off");

    public final ResourceLocation[] iconIndicatorOnResource = new ResourceLocation[]{
            null,
            new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/indicator/indicator_1_on"),
            new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/indicator/indicator_2_on"),
            null,
            new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/indicator/indicator_4_on"),
    };
    public final ResourceLocation[] iconIndicatorOffResource = new ResourceLocation[]{
            null,
            new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/indicator/indicator_1_off"),
            new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/indicator/indicator_2_off"),
            null,
            new ResourceLocation(StorageDrawers.MOD_ID + ":blocks/indicator/indicator_4_off"),
    };

    public void registerRenderers() {
    }

    public void updatePlayerInventory(EntityPlayer player) {
        if (player instanceof EntityPlayerMP)
            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
    }

    @SubscribeEvent
    public void playerRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == EnumHand.MAIN_HAND && event.getItemStack().isEmpty()) {
            TileEntity tile = event.getWorld().getTileEntity(event.getPos());
            if (tile instanceof TileEntityDrawers) {
                event.setUseBlock(Event.Result.ALLOW);
            }
        }
    }
}
