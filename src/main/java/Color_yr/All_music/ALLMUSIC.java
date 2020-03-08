package Color_yr.All_music;

import io.netty.buffer.ByteBuf;
import javazoom.jl.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Arrays;

@Mod(modid = ALLMUSIC.MODID, version = ALLMUSIC.VERSION, acceptedMinecraftVersions = "[1.9,)")
public class ALLMUSIC {
    static final String MODID = "allmusic";
    static final String VERSION = "1.1.0";
    public static Logger logger;
    private static FMLEventChannel channel;
    private static Player nowPlaying = new Player();
    private static URL nowURL;

    @Mod.EventHandler
    public void preload(final FMLPreInitializationEvent evt) {
        logger = evt.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        (ALLMUSIC.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("allmusic:channel")).register(this);
    }

    @SubscribeEvent
    public void onServerQuit(final FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        stopPlaying();
    }

    @SubscribeEvent
    public void onClicentPacket(final FMLNetworkEvent.ClientCustomPacketEvent evt) {
        new Thread(() -> {
            final ByteBuf directBuf = evt.getPacket().payload();
            final int length = directBuf.readableBytes();
            byte[] array = new byte[length];
            directBuf.getBytes(directBuf.readerIndex(), array);
            String message = new String(Arrays.copyOfRange(array, 1, array.length));
            if (message.equals("[Stop]")) {
                stopPlaying();
            } else if (message.startsWith("[Play]")) {
                try {
                    ALLMUSIC.nowURL = new URL(message.replace("[Play]", ""));
                    stopPlaying();
                    nowPlaying.SetMusic(ALLMUSIC.nowURL.openStream());
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
        }).start();
    }

    private void stopPlaying() {
        nowPlaying.close();
    }

    private void set(int a) {
        try {
            float temp = a == 0 ? -80F : (float) (((float) a * 0.2) - 20F);
            nowPlaying.Set(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
