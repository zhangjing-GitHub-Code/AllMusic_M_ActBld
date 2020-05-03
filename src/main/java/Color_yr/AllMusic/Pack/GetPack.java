package Color_yr.AllMusic.Pack;

import Color_yr.AllMusic.AllMusic;
import net.minecraft.util.PacketByteBuf;

import java.nio.charset.StandardCharsets;

public class GetPack implements IPacket {
    @Override
    public String read(PacketByteBuf buf) {
        byte[] buff = new byte[buf.readableBytes()];
        buf.readBytes(buff);
        buff[0] = 0;
        return new String(buff, StandardCharsets.UTF_8).substring(1);
    }
}