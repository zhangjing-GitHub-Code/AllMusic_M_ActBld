package Color_yr.AllMusic.Pack;

import net.minecraft.network.PacketByteBuf;

public interface IPacket {
    void read(PacketByteBuf packetByteBuf);
}