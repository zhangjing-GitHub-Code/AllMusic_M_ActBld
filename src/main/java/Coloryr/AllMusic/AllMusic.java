package coloryr.allmusic;

import coloryr.allmusic.player.APlayer;
import coloryr.allmusic.hud.HudUtils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.command.*;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Mod(modid = "allmusic", version = "2.6.3")
public class AllMusic {
    private static APlayer nowPlaying;
    private HudUtils HudUtils;
    private String url;

    public static void sendMessage(String data) {
        FMLClientHandler.instance().getClient().func_152344_a(() -> {
            if (FMLClientHandler.instance().getClient().thePlayer == null)
                return;
            FMLClientHandler.instance().getClient().thePlayer.sendChatMessage(data);
        });
    }

    @Mod.EventHandler
    public void test(final FMLLoadCompleteEvent event) {
        nowPlaying = new APlayer();
        HudUtils = new HudUtils();
    }

    @Mod.EventHandler
    public void preload(final FMLPreInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        NetworkRegistry.INSTANCE.newEventDrivenChannel("allmusic:channel").register(this);
    }

    @SubscribeEvent
    public void onSound(final PlaySoundEvent17 e) {
        if (!nowPlaying.isPlay())
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
                stopPlaying();
                url = message.replace("[Play]", "");
                nowPlaying.setMusic(url);
            } else if (message.startsWith("[Lyric]")) {
                HudUtils.Lyric = message.substring(7);
            } else if (message.startsWith("[Info]")) {
                HudUtils.Info = message.substring(6);
            } else if (message.startsWith("[Img]")) {
                HudUtils.setImg(message.substring(5));
            } else if (message.startsWith("[Pos]")) {
                nowPlaying.set(message.substring(5));
            } else if (message.startsWith("[List]")) {
                HudUtils.List = message.substring(6);
            } else if (message.equalsIgnoreCase("[clear]")) {
                HudUtils.Lyric = HudUtils.Info = HudUtils.List = "";
                HudUtils.haveImg = false;
            } else if (message.startsWith("{")) {
                HudUtils.setPos(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderOverlay(final RenderGameOverlayEvent.Post e) {
        if (e.type == RenderGameOverlayEvent.ElementType.PORTAL) {
            HudUtils.update();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTick(final TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END)
            nowPlaying.tick();
    }

    public static float getVolume() {
        return Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
    }

    public static void drawPic(int textureID, int size, int x, int y) {
        GL11.glBindTexture(3553,textureID);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, 0.0f);
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, (float) size, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f((float) size, (float) size, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f((float) size, 0.0f, 0.0f);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public static void drawText(String item, float x, float y) {
        GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        gui.drawString(font, item, (int) x, (int) y, 0xffffff);
    }

    private void stopPlaying() {
        nowPlaying.closePlayer();
        HudUtils.close();
    }

    public static void runMain(Runnable runnable){
        FMLClientHandler.instance().getClient().func_152344_a(runnable);
    }
}
