package com.jaquadro.minecraft.storagedrawers.network;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CountUpdateMessage implements IPacket {

    private int x;
    private int y;
    private int z;
    private int slot;
    private int count;

    public CountUpdateMessage() {
    }

    public CountUpdateMessage(BlockPos pos, int slot, int count) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.slot = slot;
        this.count = count;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeVarInt(x);
        buf.writeShort(y);
        buf.writeVarInt(z);
        buf.writeByte(slot);
        buf.writeVarInt(count);
    }

    @Override
    public void read(PacketBuffer buf) {
        x = buf.readVarInt();
        y = buf.readShort();
        z = buf.readVarInt();
        slot = buf.readByte();
        count = buf.readVarInt();
    }

    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        World world = Minecraft.getMinecraft().world;
        if (world != null) {
            BlockPos pos = new BlockPos(this.x, this.y, this.z);
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityDrawers) {
                ((TileEntityDrawers) tileEntity).clientUpdateCount(this.slot, this.count);
            }
        }
        return null;
    }
}
