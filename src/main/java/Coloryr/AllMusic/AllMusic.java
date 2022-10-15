package coloryr.allmusic;

import coloryr.allmusic.hud.HudUtils;
import coloryr.allmusic.player.APlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.command.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Mod(modid = "allmusic", version = "2.6.3", acceptedMinecraftVersions = "[1.8,)")
public class AllMusic {
    private static APlayer nowPlaying;
    private HudUtils HudUtils;
    private String url;

    @Mod.EventHandler
    public void test(final FMLLoadCompleteEvent event) {
        nowPlaying = new APlayer();
        HudUtils = new HudUtils();
    }

    @Mod.EventHandler
    public void preload(final FMLPreInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.newEventDrivenChannel("allmusic:channel").register(this);
    }

    @SubscribeEvent
    public void onSound(final PlaySoundEvent e) {
        if (!nowPlaying.isPlay())
            return;
        SoundCategory data = e.category;
        if (data == null)
            return;
        switch (data) {
            case MUSIC:
            case RECORDS:
                Minecraft.getMinecraft().addScheduledTask(() -> e.manager.stopSound(e.sound));
        }
    }

    @SubscribeEvent
    public void onServerQuit(final FMLNetworkEvent.ServerDisconnectionFromClientEvent e) {
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
    public void onRenderOverlay(RenderGameOverlayEvent.Post e) {
        if (e.type == RenderGameOverlayEvent.ElementType.PORTAL) {
            HudUtils.update();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTick(TickEvent.ClientTickEvent event) {
        nowPlaying.tick();
    }

    private void stopPlaying() {
        nowPlaying.closePlayer();
        HudUtils.close();
    }

    public static float getVolume() {
        return Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
    }

    public static void drawPic(int textureID, int size, int x, int y) {
        GlStateManager.bindTexture(textureID);
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
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        gui.drawString(font, item, (int) x, (int) y, 0xffffff);
    }

    public static void runMain(Runnable runnable) {
        Minecraft.getMinecraft().addScheduledTask(runnable);
    }

    public static void sendMessage(String data) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().thePlayer == null)
                return;
            Minecraft.getMinecraft().thePlayer.sendChatMessage(data);
        });
    }
}
