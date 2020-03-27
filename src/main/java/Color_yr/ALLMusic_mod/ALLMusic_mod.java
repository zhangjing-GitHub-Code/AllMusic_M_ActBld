package Color_yr.ALLMusic_mod;

import Color_yr.ALLMusic_mod.Pack.GetPack;
import Color_yr.ALLMusic_mod.Pack.IPacket;
import javazoom.jl.player.*;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

import java.net.URL;

public class ALLMusic_mod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Identifier ID = new Identifier("allmusic", "channel");
    public static boolean isPlay = false;
    public static int v = 0;

    private static final Player nowPlaying = new Player();
    private static URL nowURL;

    public final Thread thread = new Thread(() -> {
        while (true) {
            try {
                if (MinecraftClient.getInstance().options != null) {
                    int nowV = (int) (MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS) *
                            MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER)* 100);
                    if (v != nowV) {
                        nowPlaying.Set(nowV);
                    }
                }
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

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
        registerPacket(ID, GetPack.class);
        thread.start();
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
                    nowPlaying.SetMusic(nowURL.openStream());
                    nowPlaying.play();
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
}
