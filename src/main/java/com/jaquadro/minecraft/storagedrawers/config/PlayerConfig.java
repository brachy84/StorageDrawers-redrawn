package com.jaquadro.minecraft.storagedrawers.config;

import com.jaquadro.minecraft.storagedrawers.network.BoolConfigUpdateMessage;
import com.jaquadro.minecraft.storagedrawers.network.NetworkHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

public class PlayerConfig {

    @SideOnly(Side.CLIENT)
    public static final PlayerConfig CLIENT = new PlayerConfig(SDConfig.general.invertShift, SDConfig.general.invertClick);

    private static final Map<EntityPlayerMP, PlayerConfig> configMap = new Object2ObjectOpenHashMap<>();

    public static PlayerConfig getPlayerConfig(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            PlayerConfig playerConfig = configMap.get(player);
            if (playerConfig == null) {
                playerConfig = new PlayerConfig(SDConfig.general.invertShift, SDConfig.general.invertClick);
                configMap.put((EntityPlayerMP) player, playerConfig);
            }
            return playerConfig;
        }
        return CLIENT;
    }

    public static void setConfig(EntityPlayer player, boolean invertShift, boolean invertClick) {
        if (player instanceof EntityPlayerMP) {
            PlayerConfig playerConfig = configMap.get(player);
            if (playerConfig == null) {
                playerConfig = new PlayerConfig(invertShift, invertClick);
                configMap.put((EntityPlayerMP) player, playerConfig);
            } else {
                playerConfig.invertClick = invertClick;
                playerConfig.invertShift = invertShift;
            }
        } else if (player.world.isRemote) {
            CLIENT.invertShift = invertShift;
            CLIENT.invertClick = invertClick;
            syncToServer();
        }
    }

    public static void onPlayerDisconnect(EntityPlayerMP player) {
        configMap.remove(player);
    }

    private boolean invertShift;
    private boolean invertClick;

    public PlayerConfig(boolean invertShift, boolean invertClick) {
        this.invertShift = invertShift;
        this.invertClick = invertClick;
    }

    public boolean isInvertClick() {
        return invertClick;
    }

    public boolean isInvertShift() {
        return invertShift;
    }

    public static void syncToServer() {
        NetworkHandler.sendToServer(new BoolConfigUpdateMessage(CLIENT.invertShift, CLIENT.invertClick));
    }
}
