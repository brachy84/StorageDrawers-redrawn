package com.jaquadro.minecraft.storagedrawers.core;

import com.jaquadro.minecraft.chameleon.Chameleon;
import com.jaquadro.minecraft.chameleon.resources.IconRegistry;
import com.jaquadro.minecraft.storagedrawers.config.PlayerConfig;
import com.jaquadro.minecraft.storagedrawers.config.SDConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        IconRegistry iconRegistry = Chameleon.instance.iconRegistry;
        iconRegistry.registerIcon(iconConcealmentOverlayResource);
        iconRegistry.registerIcon(iconIndicatorCompOnResource);
        iconRegistry.registerIcon(iconIndicatorCompOffResource);

        for (int i = 0; i < 5; i++) {
            if (iconIndicatorOffResource[i] != null)
                iconRegistry.registerIcon(iconIndicatorOffResource[i]);
            if (iconIndicatorOnResource[i] != null)
                iconRegistry.registerIcon(iconIndicatorOnResource[i]);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(net.minecraftforge.event.entity.EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer && event.getWorld().isRemote) {
            PlayerConfig.setConfig((EntityPlayer) event.getEntity(), SDConfig.general.invertShift, SDConfig.general.invertClick);
        }
    }
}
