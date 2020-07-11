package Color_yr.AllMusic;

import Color_yr.AllMusic.Hud.Hud;
import Color_yr.AllMusic.Pack.GetPack;
import Color_yr.AllMusic.Pack.IPacket;
import Color_yr.AllMusic.player.AudioPlayer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.net.URL;

public class AllMusic implements ModInitializer {
    public static final Identifier ID = new Identifier("allmusic", "channel");
    private static final AudioPlayer nowPlaying = new AudioPlayer();
    public static boolean isPlay = false;
    public static int v = -1;
    private static URL nowURL;

    public final Thread thread = new Thread(() -> {
        while (true) {
            try {
                if (MinecraftClient.getInstance().options != null) {
                    int nowV = (int) (MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS) *
                            MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER) * 100);
                    if (v != nowV) {
                        nowPlaying.Set(nowV);
                    }
                }
                Thread.sleep(100);
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
                e.printStackTrace();
            }
        });
    }

    public static void onServerQuit() {
        stopPlaying();
        Hud.Lyric = Hud.Info = Hud.List = "";
        Hud.save = null;
    }

    public static void onClicentPacket(final String message) {
        final Thread asyncThread = new Thread(() -> {
            try {
                if (message.equals("[Stop]")) {
                    stopPlaying();
                } else if (message.startsWith("[Play]")) {
                    MinecraftClient.getInstance().getSoundManager().stopSounds(null, SoundCategory.MUSIC);
                    MinecraftClient.getInstance().getSoundManager().stopSounds(null, SoundCategory.RECORDS);
                    stopPlaying();
                    nowURL = new URL(message.replace("[Play]", ""));
                    nowPlaying.SetMusic(nowURL.openStream());
                    nowPlaying.play();
                } else if (message.startsWith("[Lyric]")) {
                    Hud.Lyric = message.substring(7);
                } else if (message.startsWith("[Info]")) {
                    Hud.Info = message.substring(6);
                } else if (message.startsWith("[List]")) {
                    Hud.List = message.substring(6);
                } else if (message.equalsIgnoreCase("[clear]")) {
                    Hud.Lyric = Hud.Info = Hud.List = "";
                } else if (message.startsWith("{")) {
                    Hud.Set(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        asyncThread.start();
    }

    private static void stopPlaying() {
        nowPlaying.close();
    }

    @Override
    public void onInitialize() {
        registerPacket(ID, GetPack.class);
        thread.start();
    }
}
