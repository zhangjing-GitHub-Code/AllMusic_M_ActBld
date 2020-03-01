package Color_yr.ALLMusic_mod.Pack;

import Color_yr.ALLMusic_mod.ALLMusic_mod;
import net.minecraft.util.PacketByteBuf;

public class GetPack implements IPacket {
    @Override
    public void read(PacketByteBuf buf) {
        byte[] buff = new byte[buf.readableBytes()];
        buf.readBytes(buff);
        buff[0] = 0;
        String data = new String(buff).substring(1);
        ALLMusic_mod.onClicentPacket(data);
    }
}