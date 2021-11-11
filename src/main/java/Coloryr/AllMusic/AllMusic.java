package Coloryr.AllMusic;

import Coloryr.AllMusic.Hud.HudUtils;
import Coloryr.AllMusic.player.APlayer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Mod(modid = AllMusic.MODID, version = AllMusic.VERSION)
public class AllMusic extends CommandBase {
    static final String MODID = "allmusic";
    static final String VERSION = "2.5.11";
    public static boolean isPlay = false;
    private static URL nowURL;
    private APlayer nowPlaying;
    public static Logger logger;
    private HudUtils HudUtils;

    public static URL Get(URL url) {
        if (url.toString().contains("https://music.163.com/song/media/outer/url?id=")
                || url.toString().contains("http://music.163.com/song/media/outer/url?id=")) {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4 * 1000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36 Edg/84.0.522.52");
                connection.setRequestProperty("Host", "music.163.com");
                connection.connect();
                if (connection.getResponseCode() == 302) {
                    return new URL(connection.getHeaderField("Location"));
                }
                return connection.getURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    @Mod.EventHandler
    public void test(final FMLPostInitializationEvent event) {
        nowPlaying = new APlayer();
        HudUtils = new HudUtils();
    }

    @Mod.EventHandler
    public void test1(FMLServerStartingEvent event) {
        ICommandManager commandManager = event.getServer().getCommandManager();
        if (commandManager instanceof ServerCommandManager) {
            ServerCommandManager server = (ServerCommandManager) commandManager;
            server.registerCommand(this);
        }
    }

    @Mod.EventHandler
    public void preload(final FMLPreInitializationEvent evt) {
        logger = evt.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.newEventDrivenChannel("allmusic:channel").register(this);
    }

    @SubscribeEvent
    public void onSound(final PlaySoundEvent17 e) {
        if (!isPlay)
            return;
        SoundCategory data = e.category;
        if (data == null)
            return;
        switch (data) {
            case MUSIC:
            case RECORDS:
                new Thread(()->{
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    FMLClientHandler.instance().getClient().func_152344_a(()->{
                        e.manager.stopSound(e.sound);
                    });
                }).start();
        }
    }

    @SubscribeEvent
    public void onServerQuit(final FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        try {
            stopPlaying();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        HudUtils.Lyric = HudUtils.Info = HudUtils.List = "";
        HudUtils.haveImg = false;
        HudUtils.save = null;
    }

    @SubscribeEvent
    public void onClicentPacket(final FMLNetworkEvent.ClientCustomPacketEvent evt) {
        new Thread(() -> {
            try {
                final ByteBuf directBuf = evt.packet.payload();
                byte[] array = new byte[directBuf.readableBytes()];
                directBuf.getBytes(directBuf.readerIndex(), array);
                array[0] = 0;
                String message = new String(array, StandardCharsets.UTF_8).substring(1);
                if (message.equals("[Stop]")) {
                    stopPlaying();
                } else if (message.startsWith("[Play]")) {
                    Minecraft.getMinecraft().getSoundHandler().stopSounds();
                    Minecraft.getMinecraft().getSoundHandler().stopSounds();
                    nowURL = new URL(message.replace("[Play]", ""));
                    nowURL = Get(nowURL);
                    if (nowURL == null)
                        return;
                    stopPlaying();
                    nowPlaying.SetMusic(nowURL);
                } else if (message.startsWith("[Lyric]")) {
                    HudUtils.Lyric = message.substring(7);
                } else if (message.startsWith("[Info]")) {
                    HudUtils.Info = message.substring(6);
                } else if (message.startsWith("[List]")) {
                    HudUtils.List = message.substring(6);
                } else if (message.startsWith("[Img]")) {
                    HudUtils.SetImg(message.substring(5));
                } else if (message.startsWith("[Pos]")) {
                    nowPlaying.set(message.substring(5));
                } else if (message.equalsIgnoreCase("[clear]")) {
                    HudUtils.Lyric = HudUtils.Info = HudUtils.List = "";
                    HudUtils.haveImg = false;
                } else if (message.startsWith("{")) {
                    HudUtils.Set(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "allmusic").start();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderOverlay(RenderGameOverlayEvent.Post e) {
        if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            HudUtils.update();
        }
    }

    private void stopPlaying() {
        nowPlaying.close();
        HudUtils.stop();
    }

    @Override
    public String getCommandName() {
        return "allmusic";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "allmusic";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if ("play".equals(args[0])) {
                if (args.length != 2) {
                    sender.addChatMessage(new ChatComponentText("error"));
                    return;
                }
                try {
                    nowURL = new URL("https://music.163.com/song/media/outer/url?id=" + args[1]);
                    nowURL = Get(nowURL);
                    if (nowURL == null)
                        return;
                    stopPlaying();
                    nowPlaying.SetMusic(nowURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return;
            }
            if(args[0].equalsIgnoreCase("pos"))
            {
                if (args.length != 2) {
                    sender.addChatMessage(new ChatComponentText("error"));
                    return;
                }
                nowPlaying.set(Integer.parseInt(args[1]));
            }
            return;
        }
        sender.addChatMessage(new ChatComponentText("error"));
    }
}
