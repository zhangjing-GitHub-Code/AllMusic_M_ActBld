package Color_yr.All_music;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javazoom.jl.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.apache.logging.log4j.Logger;

import java.net.URL;

@Mod(modid = ALLMUSIC.MODID, version = ALLMUSIC.VERSION, acceptedMinecraftVersions = "[1.9,)")
public class ALLMUSIC {
    static final String MODID = "allmusic";
    static final String VERSION = "1.2.0";
    public static Logger logger;
    private static FMLEventChannel channel;
    private static Player nowPlaying = new Player();
    private static URL nowURL;
    private static int nowV;

    private static final String channelName = "allmusic:channel";

    public final Thread thread = new Thread(() -> {
        while (true) {
            try {
                nowV = (int) (Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS) *
                        Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER) * 100);
                nowPlaying.Set(nowV);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    public static void Send(String s) {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeString(s);
        channel.sendToServer(new FMLProxyPacket(packetBuffer, channelName));
    }

    @Mod.EventHandler
    public void preload(final FMLPreInitializationEvent evt) {
        logger = evt.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        thread.start();
        (ALLMUSIC.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName)).register(this);
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
            if (message.equalsIgnoreCase("[Check]")) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Send("666");
            } else if (message.equals("[Stop]")) {
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
            }
        }).start();
    }

    private void stopPlaying() {
        nowPlaying.close();
    }
}
