package Color_yr.ALLMusic_mod.Pack;

import net.fabricmc.fabric.api.network.PacketContext;

public interface IPacketHandler<T extends IPacket> {
    void apply(T packet, PacketContext context);
}