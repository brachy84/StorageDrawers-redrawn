package com.jaquadro.minecraft.storagedrawers.network;

import com.jaquadro.minecraft.storagedrawers.config.PlayerConfig;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Nullable;

public class BoolConfigUpdateMessage implements IPacket {

    private boolean invertShift;
    private boolean invertClick;

    public BoolConfigUpdateMessage() {
    }

    public BoolConfigUpdateMessage(boolean invertShift, boolean invertClick) {
        this.invertShift = invertShift;
        this.invertClick = invertClick;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBoolean(this.invertShift);
        buf.writeBoolean(this.invertClick);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.invertShift = buf.readBoolean();
        this.invertClick = buf.readBoolean();
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        PlayerConfig.setConfig(handler.player, this.invertShift, this.invertClick);
        return null;
    }
}
