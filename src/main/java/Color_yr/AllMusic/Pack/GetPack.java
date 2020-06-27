package Color_yr.AllMusic.Pack;

import Color_yr.AllMusic.AllMusic;
import net.minecraft.network.PacketByteBuf;

import java.nio.charset.StandardCharsets;

public class GetPack implements IPacket {
    @Override
    public void read(PacketByteBuf buf) {
        byte[] buff = new byte[buf.readableBytes()];
        buf.readBytes(buff);
        buff[0] = 0;
        String data = new String(buff, StandardCharsets.UTF_8).substring(1);
        AllMusic.onClicentPacket(data);
    }
}