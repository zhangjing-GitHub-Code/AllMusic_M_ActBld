package Color_yr.ALLMusic_mod;

import Color_yr.ALLMusic_mod.Pack.GetPack;
import Color_yr.ALLMusic_mod.Pack.IPacket;
import javazoom.jl.player.*;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

import java.net.URL;

public class ALLMusic_mod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final Identifier ID = new Identifier(ALLMusic_mod.modID, ALLMusic_mod.channel);

    private static Player nowPlaying;
    private static URL nowURL;

    public static final String modID  = "allmusic";
    public static final String channel = "channel";

    public static <T extends IPacket> void registerPacket(Identifier id, Class<T> packetClass) {
        ClientSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> {
            try {
                IPacket packet = packetClass.newInstance();
                packet.read(buffer);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        });
    }

    @Override
	public void onInitialize() {
        set(100);
        registerPacket(ID, GetPack.class);
    }

    public static void onServerQuit() {
        stopPlaying();
    }

    public static void onClicentPacket(final String message) {
        final Thread asyncThread = new Thread(() -> {
            if (message.equals("[Stop]")) {
                stopPlaying();
            } else if (message.startsWith("[Play]")) {
                try {
                    stopPlaying();
                    nowURL = new URL(message.replace("[Play]", ""));
                    nowPlaying .SetMusic(nowURL.openStream());
                    nowPlaying.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (message.startsWith("[V]")) {
                try {
                    String a = message.replace("[V]", "");
                    set(Integer.parseInt(a));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        asyncThread.start();
    }

    private static void stopPlaying() {
        nowPlaying.close();
    }

    private static void set(int a) {
        try {
            float temp = (a == 0) ? -80.0f : ((float)(a * 0.2 - 20.0));
            nowPlaying.Set(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
