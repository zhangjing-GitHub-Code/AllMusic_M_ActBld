package Color_yr.All_music;

import io.netty.buffer.ByteBuf;
import javazoom.jl.player.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.net.URL;

@Mod(modid = ALLMUSIC.MODID, version = ALLMUSIC.VERSION, acceptedMinecraftVersions = "[1.9,)")
public class ALLMUSIC {
    static final String MODID = "allmusic";
    static final String VERSION = "1.3.0";
    public static int v = -1;
    public static boolean isPlay = false;
    private final Player nowPlaying = new Player();

    public final Thread thread = new Thread(() -> {
        while (true) {
            try {
                int nowV = (int) (Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS) *
                        Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER) * 100);
                if (v != nowV) {
                    nowPlaying.Set(nowV);
                }
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    @Mod.EventHandler
    public void preload(final FMLPreInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(this);
        thread.start();
        NetworkRegistry.INSTANCE.newEventDrivenChannel("allmusic:channel").register(this);
    }

    @SubscribeEvent
    public void onSound(final PlaySoundEvent e) {
        if (!isPlay)
            return;
        SoundCategory data = e.getSound().getCategory();
        switch (data) {
            case MUSIC:
            case RECORDS:
                e.setResultSound(null);
        }
    }

    @SubscribeEvent
    public void onServerQuit(final FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        stopPlaying();
    }

    @SubscribeEvent
    public void onClicentPacket(final FMLNetworkEvent.ClientCustomPacketEvent evt) {
        new Thread(() -> {
            final ByteBuf directBuf = evt.getPacket().payload();
            byte[] array = new byte[directBuf.readableBytes()];
            directBuf.getBytes(directBuf.readerIndex(), array);
            array[0] = 0;
            String message = new String(array).substring(1);
            if (message.equals("[Stop]")) {
                stopPlaying();
            } else if (message.startsWith("[Play]")) {
                try {
                    Minecraft.getMinecraft().getSoundHandler().stop("", SoundCategory.MUSIC);
                    Minecraft.getMinecraft().getSoundHandler().stop("", SoundCategory.RECORDS);
                    URL nowURL = new URL(message.replace("[Play]", ""));
                    stopPlaying();
                    nowPlaying.SetMusic(nowURL.openStream());
                    nowPlaying.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopPlaying() {
        nowPlaying.close();
    }
}
