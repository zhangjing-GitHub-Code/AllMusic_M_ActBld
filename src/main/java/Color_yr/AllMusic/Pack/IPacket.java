package Color_yr.AllMusic.Pack;

import net.minecraft.util.PacketByteBuf;

public interface IPacket {
    String read(PacketByteBuf packetByteBuf);
}